package clickDetector.toad;

import Localiser.DelayMeasurementParams;
import Localiser.algorithms.UpSampler;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import fftFilter.FFTFilterParams;
import fftManager.FFTDataUnit;
import fftManager.fftorganiser.FFTDataException;
import fftManager.fftorganiser.FFTDataList;
import fftManager.fftorganiser.FFTDataOrganiser;

public class ClickFFTOrganiser extends FFTDataOrganiser {

	private ClickControl clickControl;
	private UpSampler upSampler;
	private DelayMeasurementParams delayParams;

	/**
	 * @param clickControl
	 */
	public ClickFFTOrganiser(ClickControl clickControl) {
		super(clickControl);
		this.clickControl = clickControl;
		upSampler = new UpSampler(1);
	}

	/* (non-Javadoc)
	 * @see fftManager.fftorganiser.FFTDataOrganiser#createFFTDataList(PamguardMVC.PamDataUnit, int)
	 */
	@Override
	public synchronized FFTDataList createFFTDataList(PamDataUnit pamDataUnit, double sampleRate, int channelMap) throws FFTDataException {
		ClickDetection click = (ClickDetection) pamDataUnit;

		// if it's envelope waveform (or leading edge) 
		if (delayParams != null && delayParams.envelopeBearings) {
			return getEnvelopedFFTData(pamDataUnit, channelMap, delayParams);
		}
		if (delayParams != null && delayParams.getUpSample() > 1) {
			double[][] upSampled = upSampler.upSample(click.getWaveData(), delayParams.getUpSample());
			double upSampledRate = sampleRate * delayParams.getUpSample();
			FFTDataList fftDataList = super.rawToFFTData(upSampled, pamDataUnit.getTimeMilliseconds(), 
					pamDataUnit.getStartSample()*delayParams.getUpSample(), 
					upSampledRate, pamDataUnit.getChannelBitmap(), channelMap);
			filterFFTDataList(fftDataList, delayParams, upSampledRate);
			return fftDataList;
		}
		else { // normal behaviour + add useful bin range from filter parameters. 
			FFTDataList fftDataList = super.createFFTDataList(pamDataUnit, sampleRate, channelMap);
			filterFFTDataList(fftDataList, delayParams, sampleRate);
			return fftDataList;
		}
	}

	private void filterFFTDataList(FFTDataList fftDataList, DelayMeasurementParams delayParams, double sampleRate) {
		if (delayParams == null || fftDataList == null || fftDataList.getFftDataUnits().size() == 0) {
			return;
		}
		if (delayParams.envelopeBearings == false) {
			FFTFilterParams filterParams = delayParams.delayFilterParams;
			if (filterParams != null) {
				int actualFFTLen = fftDataList.getFftDataUnits().get(0).getFftData().length()*2;
				double binScale = actualFFTLen/sampleRate;
				int[] binLims = {0, actualFFTLen/2};			
				double b1 = filterParams.highPassFreq*binScale;
				double b2 = filterParams.lowPassFreq*binScale;	
				switch(filterParams.filterBand) {
				case BANDPASS:
					binLims[0] = Math.max(0, (int)Math.floor(Math.min(b1, b2))); 
					binLims[1] = Math.min(actualFFTLen/2, (int)Math.ceil(Math.max(b1, b2))); 
					break;
				case BANDSTOP:
					// use temporarily to zero part of the data, then set null 
					binLims[0] = Math.max(0, (int)Math.floor(Math.min(b1, b2))); 
					binLims[1] = Math.min(actualFFTLen/2, (int)Math.ceil(Math.max(b1, b2)));
					// now zero the data in that range since there isn't another easy way 
					// of zeroing it.
					// also copy is since the internal stuff may not want an ugly gap !
					for (FFTDataUnit aUnit:fftDataList.getFftDataUnits()) {
						ComplexArray copyArray = aUnit.getFftData().clone();
						for (int i = binLims[0]; i < binLims[1]; i++) {
							copyArray.set(i, 0, 0);
						}
						aUnit.setFftData(copyArray);
					}
					binLims = null; // set to the entire range now.
					break;
				case HIGHPASS:
					binLims[0] = Math.max(0, (int) Math.floor(filterParams.highPassFreq));
					break;
				case LOWPASS:
					binLims[1] = Math.min(actualFFTLen/2, (int) Math.ceil(filterParams.lowPassFreq));
					break;
				default:
					break;

				}
				for (FFTDataUnit aUnit:fftDataList.getFftDataUnits()) {
					aUnit.setUsefulBinRange(binLims);
				}
			}
		}
	}

	/**
	 * Need to write this one still !
	 * @param pamDataUnit
	 * @param channelMap
	 * @param delayParams 
	 * @return
	 * @throws FFTDataException
	 */
	private FFTDataList getEnvelopedFFTData(PamDataUnit pamDataUnit, int channelMap, DelayMeasurementParams delayParams) throws FFTDataException {
		double[][] envelopeData = getEnvelopeData(pamDataUnit, channelMap, delayParams);
		FFTDataList fftDataList = this.rawToFFTData(envelopeData, pamDataUnit.getTimeMilliseconds(), pamDataUnit.getStartSample(), rawOrFFTData.getSampleRate(), channelMap, channelMap);
		return fftDataList;
	}
	
	/**
	 * Get the waveform envelopes for the requested channels. 
	 * There is already a build in function within the click detector 
	 * that does the clever bit. 
	 * @param pamDataUnit Data unit
	 * @param channelMap map of wanted channels (subset of data unit's channels ? Maybe not !)
	 * @param delayParams delay parameters for specified click type 
	 * @return 2D double array of envelope waveforms
	 * @throws FFTDataException
	 */
	private double[][] getEnvelopeData(PamDataUnit pamDataUnit, int channelMap, DelayMeasurementParams delayParams) throws FFTDataException {
		ClickDetection click = (ClickDetection) pamDataUnit;
		int nUsedChan = PamUtils.getNumChannels(channelMap);
		int clickChannels = click.getChannelBitmap();
		int nChan = PamUtils.getNumChannels(clickChannels);
		double[][] envelopeData = new double[nUsedChan][];
		int iUsed = 0;
		for (int i = 0; i < nChan; i++) {
			int clickChan = PamUtils.getNthChannel(i, clickChannels);
			if ((1<<clickChan & channelMap) == 0) {
				continue;
			}
			if (delayParams != null) {
				envelopeData[iUsed++] = click.getAnalyticWaveform(i, delayParams.filterBearings, delayParams.getFftFilterParams());
			}
			else {
				envelopeData[iUsed++] = click.getAnalyticWaveform(i);
			}
		}
		if (delayParams.useLeadingEdge) {
			for (int i = 0; i < nUsedChan; i++) {
				envelopeData[i] = extractLeadingEdge(envelopeData[i], delayParams.leadingEdgeSearchRegion);
			}
		}
		return envelopeData;
	}
	/**
	 * Differentiate the envelope and find a single peak close to where the leading edge of
	 * the envelope should be. Then zero everything outside the highest peak so that the returned
	 * array should be a single positive peak representing the part of the waveform that had
	 * the most steeply rising leading edge. 
	 * @param envelope envelope function (modified in place)
	 * @param sRegion search region for a single peak to extract. 
	 */
	private double[] extractLeadingEdge(double[] envelope, int[] sRegion) {
		// differentiate it.
		double[] edge = new double[envelope.length];
		for (int i = 1, j = 0; i < envelope.length; i++, j++) {
			edge[j] = envelope[i]-envelope[j];
		}
		
		if (sRegion == null || sRegion.length != 2) {
			sRegion = new int[2];
			sRegion[1] = edge.length;
		}
		else {
			for (int i = 0; i < 2; i++) {
				// check length is within array bounds. 
				sRegion[i] = Math.max(0, Math.min(edge.length-1, sRegion[i]));
			}
		}
		
		double maxVal = edge[sRegion[0]];
		int maxPos = sRegion[0];
		for (int i = sRegion[0]; i <= sRegion[1]; i++) {
			if (edge[i] > maxVal) {
				maxVal = edge[i];
				maxPos = i;
			}
		}
		// now find the regions either side of a single peak and zero them entirely. 
		int iPos = maxPos;
		while (edge[iPos] > 0 && iPos > 0) {
			iPos--;
		}
		for (; iPos >= 0; iPos--) {
			edge[iPos] = 0;
		}
		iPos = maxPos;
		while (iPos < edge.length) {
			if (edge[iPos] > 0 ) {
				iPos++;
			}
			else {
				break;
			}
		}
		for (; iPos < edge.length; iPos++) {
			edge[iPos] = 0;
		}
		return edge;
	}

	/**
	 * @return the delayParams
	 */
	public DelayMeasurementParams getDelayParams() {
		return delayParams;
	}

	/**
	 * @param delayParams the delayParams to set
	 */
	public void setDelayParams(DelayMeasurementParams delayParams) {
		this.delayParams = delayParams;
	}

}
