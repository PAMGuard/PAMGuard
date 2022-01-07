package likelihoodDetectionModule;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import Spectrogram.WindowFunction;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FFTParameters;
import fftManager.FastFFT;

public class LikelihoodFFTProcess extends PamProcess {
	
	private LikelihoodFFTParameters params;
	private boolean inUse = true;
	private int logFftLength;

	private int fftOverlap;

	int[] channelPointer = new int[PamConstants.MAX_CHANNELS];

	private double[] windowFunction;
	private double[][] windowedData = new double[PamConstants.MAX_CHANNELS][];
	private Complex[] fftData;
	private double[] fftRealBlock;
	//private PamFFTControl fftControl;
	private int[] rawBlocks = new int[PamConstants.MAX_CHANNELS];
	private int[] fftBlocks = new int[PamConstants.MAX_CHANNELS];
	private int[] channelCounts;
	private FFTDataBlock outputData;
	
	private FastFFT fastFFT = new FastFFT();
	

	public LikelihoodFFTProcess( PamControlledUnit pamControlledUnit, PamDataBlock parentDataBlock, LikelihoodFFTParameters params ) {
		super(pamControlledUnit, parentDataBlock);

		this.params = params;
		
		setCanMultiThread(false);
		
		//fftControl = pamControlledUnit;
		
		setParentDataBlock(parentDataBlock); 

		outputData = 
			new FFTDataBlock( 
				pamControlledUnit.getUnitName(), 
				this, 
				this.params.getPamFFTParameters().channelMap,
				this.params.getPamFFTParameters().fftHop,
				this.params.getPamFFTParameters().fftLength );
		
		addOutputDataBlock(outputData);
	
		setupFFT();
	}

	public LikelihoodFFTParameters getParameters() {
		return this.params;
	}
	
	public void setInUse( boolean value ) {
		this.inUse = value;
	}
	
	public boolean getInUse() {
		return this.inUse;
	}
	
	@Override
	public String toString() {
		String s = new String();
		s += "[Source: " + params.getSourceId() + ", ";
		s += "Channels: " + params.getChannelMap() + ", ";
		s += "Size: " + params.getFFTSize() + ", ";
		s += "Hop: " + params.getFFTHop() + ", ";
		s += "Averages: " + params.getNumberAverages() + ", ";
		s += "Time Res: " + params.getActualTimeResolution() + ", ";
		s += "Freq Res: " + params.getActualFrequencyResolution() + "]";
		
		return s;
	}
	
	@Override
	public String getProcessName() {
		return "Likelihood Detector FFT Process";
	}

	public void setupFFT() {
		
		// need to find the existing source data block and remove from observing it.
		// then find the new one and subscribe to that instead. 
		channelCounts = new int[PamConstants.MAX_CHANNELS];
		
		//if (fftControl == null) return;
		PamRawDataBlock rawDataBlock = PamController.getInstance().
			getRawDataBlock( this.params.getPamFFTParameters().dataSource );
		
			setParentDataBlock(rawDataBlock); 
		
		outputData.setChannelMap( this.params.getPamFFTParameters().channelMap );
		outputData.setFftHop( this.params.getPamFFTParameters().fftHop );
		outputData.setFftLength( this.params.getPamFFTParameters().fftLength );
		
		
		// since it's used so much, make a local reference
		FFTParameters fftParameters = this.params.getPamFFTParameters();

		setProcessName("FFT - " + this.params.getPamFFTParameters().fftLength + " point, "
				+ sampleRate + " Hz");

		//outputData.
		setSampleRate(sampleRate, true);
		
		// outputData.setChannelMap(fftParameters.channelMap);
		// outputData.set
		fftOverlap = fftParameters.fftLength - fftParameters.fftHop;
		logFftLength = 1;
		while (1 << logFftLength < fftParameters.fftLength) {
			logFftLength++;
		}

		windowFunction = WindowFunction.getWindowFunc(fftParameters.windowFunction, fftParameters.fftLength);

		fftRealBlock = new double[fftParameters.fftLength];
		//		
		// and for each channel, make a double array
		// and set the pointer to zero
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (((1 << i) & this.params.getPamFFTParameters().channelMap) > 0) {
				windowedData[i] = new double[fftParameters.fftLength];
				channelPointer[i] = 0;
			}
		}
		fftData = new Complex[fftParameters.fftLength];
		for (int i = 0; i < fftParameters.fftLength; i++) {
			fftData[i] = new Complex(0, 0);
		}
		/*
		 * Tell the output data block - should then get passed on to Spectrogram
		 * display which can come back and work it out for itesef that life has
		 * changed.
		 */
		noteNewSettings();

	}

	public int getFftLength() {
		return this.params.getPamFFTParameters().fftLength;
	}

	public int getFftHop() {
		return this.params.getPamFFTParameters().fftHop;
	}

	public int getChannelMap() {
		return this.params.getPamFFTParameters().channelMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *      Gets blocks of raw audio data (single channel), blocks it up into
	 *      fftSize blcoks dealing with overlaps as appropriate. fft's each
	 *      complete block and sends it off to the output PamDataBlock, which
	 *      will in turn notify any subscribing processes and views
	 */
	@Override
	public void newData(PamObservable obs, PamDataUnit pamRawData) {
		FFTDataUnit pu;
		RawDataUnit rawDataUnit = (RawDataUnit) pamRawData;
		
		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		rawBlocks[iChan] ++;
		// see if the channel is one we want efore doing anything.
		if ((rawDataUnit.getChannelBitmap() & this.params.getPamFFTParameters().channelMap) == 0){
			return;
		}
		/*
		 * Now make blocks of overlaping FFT data and parse them to the output
		 * data block Raw data blocks should be for a single channel
		 */

		double rawData[] = rawDataUnit.getRawData();
		int dataPointer = channelPointer[iChan];
		
		//local copy
		FFTParameters fftParameters = this.params.getPamFFTParameters();

		/*
		 * Data are first copied into a new block of double data wndowedData.
		 * dataPointer is the index within windowedData. When windoewedData is
		 * full, the real data are copied over into a complex array and the
		 * window function applied so that the data in windowedData is still
		 * untouched. To alow for fft overlap, the back bit of windowedData is
		 * then copied to the front of windowedData, and the dataPointer left in
		 * the right place (at fftOverlap) so that filling can continue from the
		 * outer loop over raw data.
		 * 
		 */
		for (int i = 0; i < rawData.length; i++) {
			if (dataPointer >= 0) {
				windowedData[iChan][dataPointer] = rawData[i];
			}
			dataPointer++;
			if (dataPointer == fftParameters.fftLength) {
				/*
				 * we have a complete block, so make the FFT and send it off to
				 * the output data block. The fftOutData was created by the FFT
				 * routine, so we can pass it off to the data manager without
				 * risk of it being overwritten
				 */
				for (int w = 0; w < fftParameters.fftLength; w++) {
					fftRealBlock[w] = windowedData[iChan][w] * windowFunction[w];
				}

				/*
				 * Create a new data unit BEFORE the fft is called - or better
				 * still, get one from the recycler. The recycled unit shoudl
				 * already have the correct output data which can be parsed to
				 * the rfft routine to avoid constantly recreating complex data
				 * units.
				 */

				long startSample = rawDataUnit.getStartSample() + i - fftParameters.fftLength;
				pu = outputData.getRecycledUnit();
				
				if (pu != null) {
					pu.setInfo(absSamplesToMilliseconds(startSample), 
							rawDataUnit.getChannelBitmap(), startSample, fftParameters.fftLength, channelCounts[iChan]);
				}
				else {
					pu = new FFTDataUnit(absSamplesToMilliseconds(startSample), 
							rawDataUnit.getChannelBitmap(), startSample, fftParameters.fftLength, 
							null, channelCounts[iChan]);
				}
				channelCounts[iChan]++;
				


				/*
				 * rfft will check that the pu.data is not null and that it is
				 * the right length. If not, it will recreate the array - so
				 * normally the return value fftOutData will be the same as
				 * pu.data !
				 * 
				 * The first data block can never be a recycled one, so will
				 * always contain a null data reference.
				 */
				ComplexArray fftOutData;
				fftOutData = fastFFT.rfft(fftRealBlock, fftParameters.fftLength);

				// just in case - set the correct reference in the data block
				pu.setFftData(fftOutData);

				fftBlocks[PamUtils.getSingleChannel(pu.getChannelBitmap())] ++;
				outputData.addPamData(pu);

				/*
				 * then check out the hop - if it's < fftLength it's necssary to
				 * copy data from the end of the block back to the beginning if
				 * the hop is > fftLength, then set dataPointer to - the gap so
				 * that no data is added to fftData for a while.
				 */
				dataPointer = fftOverlap;
				if (dataPointer > 0) {
					for (int j = 0; j < fftOverlap; j++) {
						windowedData[iChan][j] = windowedData[iChan][j + fftParameters.fftHop];
					}
				}
			}
		}
		/*
		 * finally store the pointer position in the ArrayList for that channel
		 */
		channelPointer[iChan] = dataPointer;

	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public void pamStart() {
		setupFFT();
	}

	@Override
	public void pamStop() {
		// nothing to do here for this class - it all happens in update
	}

	public FFTDataBlock getOutputData() {
		return outputData;
	}
}
