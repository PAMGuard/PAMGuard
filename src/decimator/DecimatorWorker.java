package decimator;

import java.util.Arrays;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.interpolate.Interpolator;
import Filters.interpolate.PolyInterpolator0;
import Filters.interpolate.PolyInterpolator1;
import Filters.interpolate.PolyInterpolator2;
import Filters.interpolate.SplineInterpolator;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.debug.Debug;

/**
 * Job to run the actual decimation. Separate out from 
 * DecimatorProcess so that it can be used elsewhere. 
 * <br> Note that this can both decimate and upsample. If decimating, filtering 
 * takes place before data are copied to output array. If upsampling, filtering takes
 * place AFTER data are copied to the output array. 
 * @author dg50
 *
 */
public class DecimatorWorker {
	
	private Filter filters[];
	private double[][] filteredData;
	private double[] pickSample;
	private int[] putSample;
	private long[] totalPutSamples;
	private double[][] outputData;
	private long[] outputStartMillis;
	private int channelMap;
	private double inputRate;
	private double outputRate;
	private Interpolator[] interpolators;
	private DecimatorParams decimatorParams;

	/**
	 * Make a decimator worker with given filter params, channel map and input and output rates. 
	 * Channels not in the map will be ignored. 
	 * @param decimatorParams Filter parameters for Decimator filter
	 * @param channelMap Channel map (channels not in map will return null)
	 * @param inputRate input sample rate
	 * @param outputRate output sample rate
	 */
	public DecimatorWorker(DecimatorParams decimatorParams, int channelMap, double inputRate, double outputRate) {
		this.decimatorParams = decimatorParams;
		this.channelMap = channelMap;
		this.inputRate = inputRate;
		this.outputRate = outputRate;
		createFilters();
	}

	/**
	 * Make a decimator / upsampler 
	 * @param filterOrder Filter order to use (Butterworth low pass applied before decimation or after upsampling
	 * @param channelMap channel map
	 * @param inputRate input sample rate
	 * @param outputRate output sample rate
	 */
	public DecimatorWorker(int filterOrder, int channelMap, double inputRate, double outputRate) {
		this.channelMap = channelMap;
		this.inputRate = inputRate;
		this.outputRate = outputRate;
		decimatorParams = new DecimatorParams();
		decimatorParams.filterParams = new FilterParams();
		decimatorParams.filterParams.filterBand = FilterBand.LOWPASS;
		decimatorParams.filterParams.filterType = FilterType.BUTTERWORTH;
		decimatorParams.filterParams.filterOrder = filterOrder;
		decimatorParams.filterParams.lowPassFreq = (float) (Math.min(inputRate, outputRate) / 2.);
		createFilters();
	}
	
	/**
	 * Make the decimator filters. If reducing frequency, then the filter
	 * is applied before decimation (obviously!) so is set up based on the 
	 * input sample rate. If however the 'decimator' is being used to upsample, 
	 * then filtering takes place AFTER the transfer of data to the output 
	 * arrays, so fitlering is set up based on the output sample rate.  
	 */
	protected  void createFilters() {
		int highestChan = PamUtils.getHighestChannel(channelMap);
		filters = new Filter[highestChan+1];
		filteredData = new double[highestChan+1][];
		outputData = new double[highestChan+1][];
		outputStartMillis = new long[highestChan+1];
		interpolators = new Interpolator[highestChan+1];
		double fs = Math.max(inputRate, outputRate);
		for (int i = 0; i <= highestChan; i++) {
			if ((1<<i & channelMap) == 0) {
				continue;
			}
			FilterMethod filterMethod = FilterMethod.createFilterMethod(fs, decimatorParams.filterParams);
			filters[i] = filterMethod.createFilter(i);
			filters[i].prepareFilter();
			interpolators[i] = makeInterpolator(decimatorParams.interpolation);
		}
		pickSample = new double[highestChan+1];
		putSample = new int[highestChan+1];
		/**
		 * This is a funny one - if skipping the start of a file, then the 
		 * first sample number may not be zero, but we won't know this until the 
		 * first raw data arrive, so set this null and deal with it in process(RawDataUnit)
		 */
		totalPutSamples = null;//new long[highestChan+1];
	}
	
	private Interpolator makeInterpolator(int order) {
		switch (order) {
		case 0:
			return new PolyInterpolator0();
		case 1:
			return new PolyInterpolator1();
		case 2:
			return new PolyInterpolator2();
		default:
			return new PolyInterpolator0();
		}
	}

//	long lastSampDur;
	/**
	 * Run the decimator on the input data, return null if it's not 
	 * in the channel list. <br>Also sometimes null if decimation ratio is not an integer factor
	 * @param inputData
	 * @return a new data unit or null
	 */
	public RawDataUnit process(RawDataUnit inputData) {
		if (totalPutSamples == null) {
			/**
			 * Have to do this here, since the sample number of the first data unit may be 
			 * >> 0 if we're skipping the start of a file, so need to apply a scaled
			 * version of this offset to the output data to get correct sample numbers. 
			 */
			long firstSample = (long) (inputData.getStartSample() * outputRate / inputRate);
			totalPutSamples = new long[putSample.length];
			Arrays.fill(totalPutSamples, firstSample);
		}
		RawDataUnit retUnit = null;
		int chanMap = inputData.getChannelBitmap();
		if ((chanMap & channelMap) == 0) {
			return null;
		}
		int chan = PamUtils.getSingleChannel(chanMap);
		long nInputSamps = inputData.getSampleDuration();
		if (inputRate > outputRate) { // decimation
			if (filteredData[chan] == null || filteredData[chan].length != nInputSamps) {
				filteredData[chan] = new double[(int) nInputSamps];
			}
			filters[chan].runFilter(inputData.getRawData(), filteredData[chan]);
		}
		else { // upsampling - filter later. 
			filteredData[chan] = inputData.getRawData();
		}
		/**
		 * When processing offline files, sample numbers can change, e.g. at end of file 
		 * there will be a partially filled data unit, so size will get smaller, then 
		 * if cursor moved, size will go back to default. So need to be able to handle
		 * varying block sizes. 
		 */		
		int nOutSamps;
		if (outputData[chan] == null) {
			/*
			 * number of output samples is sounded up so that one inputData always fits into 
			 * one output decimated data. However, there will be rare occasions when this function returns null 
			 * 
			 */
			nOutSamps = (int) Math.ceil(inputData.getSampleDuration() * outputRate / inputRate);
			outputData[chan] = new double[nOutSamps];
			outputStartMillis[chan] = inputData.getTimeMilliseconds();
		}
		else {
			nOutSamps = outputData[chan].length;
		}
		Interpolator interpolator = interpolators[chan];
		interpolator.setInputData(filteredData[chan]);
		try {
			while (pickSample[chan] < (nInputSamps-.5)) {
//				outputData[chan][putSample[chan]++] = filteredData[chan][(int) Math.round(pickSample[chan])];
				outputData[chan][putSample[chan]++] = interpolator.getOutputValue(pickSample[chan]);
				totalPutSamples[chan]++;
				if (putSample[chan] == nOutSamps) {
					retUnit = new RawDataUnit(outputStartMillis[chan], inputData.getChannelBitmap(), totalPutSamples[chan]-nOutSamps, nOutSamps);
					retUnit.setRawData(outputData[chan], true);
					outputData[chan] = new double[nOutSamps];
					outputStartMillis[chan] = inputData.getTimeMilliseconds() + (long) (pickSample[chan] / inputRate * 1000.);
					putSample[chan] = 0;
				}
				pickSample[chan] += inputRate / outputRate;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		pickSample[chan] -= nInputSamps; // ready for the next one - should be > 0. 

		/*
		 * If upsampling, need to run the upsample filter now
		 */
		if (retUnit != null && inputRate < outputRate) {
			filters[chan].runFilter(retUnit.getRawData());
		}
		return retUnit;
	}

	
}
