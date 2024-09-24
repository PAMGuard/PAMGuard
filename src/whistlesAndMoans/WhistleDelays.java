package whistlesAndMoans;

import java.util.List;
import java.util.ListIterator;

import Localiser.algorithms.Correlations;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;

/**
 * Class for estimating whistle delays from a whistle shape. 
 * <br>to save processing, one of these should be created for each channel group
 * so that multiple delay measures can be pre prepared and populated 
 * simultaneously - i.e. each bit of FFT data will need to go into one 
 * measure as it, but into the next as it's complex conjugate. 
 * <br>Doing everything at once should save data access times. 
 * @author Doug Gillespie
 *
 */
public class WhistleDelays {

	private WhistleMoanControl whistleMoanControl;
	
	private WhistleToneConnectProcess whProcess;
	
	private FFTDataBlock sourceData;
	
	private int channelMap; // channel map for one detector group
	private int nChannels; // total num of channels
	private int[] channelList; // list of channels. 
	int nDelays;
	
	private DelayMeasure[] delayMeasures;
	
	private FastFFT fft = new FastFFT();

	private int hydrophoneMap;

	private float sampleRate;
	
	private Correlations correlations = new Correlations();

	public WhistleDelays(WhistleMoanControl whistleMoanControl, int channelMap) {
		super();
		this.whistleMoanControl = whistleMoanControl;
		this.channelMap = channelMap;
		whProcess = whistleMoanControl.getWhistleToneProcess();
		nChannels = PamUtils.getNumChannels(channelMap);
		channelList = PamUtils.getChannelArray(channelMap);
		nDelays = nChannels * (nChannels - 1) / 2;
	}
	
	public void prepareBearings() {
		sourceData = (FFTDataBlock) whProcess.getParentDataBlock();
		if (nDelays <= 0) {
			return;
		}
		delayMeasures = new DelayMeasure[nDelays];
		for (int i = 0; i < nDelays; i++) {
			delayMeasures[i] = new DelayMeasure(sourceData.getFftLength());
		}
		try {
		PamDataBlock rawDataSource = whistleMoanControl.getWhistleToneProcess().getRawSourceDataBlock(0);
		hydrophoneMap = ((PamRawDataBlock) rawDataSource).getChannelListManager().channelIndexesToPhones(this.channelMap);
		}
		catch (Exception e) {
			hydrophoneMap = this.channelMap;
		}
		
		this.sampleRate = whistleMoanControl.getWhistleToneProcess().getSampleRate();
	}
	
	/**
	 * Gets the delays for a connected region.
	 * nChan(nChan-1)/2 delays will be returned. 
	 * Delays are channels 0-1, 0-2, 1-2, etc.  
	 * @param channelBitMap channel bitmap
	 * @param region connected region
	 * @return array of delays from cross correlation in samples. 
	 */
	public double[] getDelays(ConnectedRegion region) {
		if (nDelays <= 0) {
			return null;
		}
		double[] delays = new double[nDelays];
		/**
		 * Get the max delays for each hydrophone pair. 
		 * 
		 */
		double[] maxDelays = correlations.getMaxDelays(this.sampleRate, this.hydrophoneMap, region.getStartMillis());
		
		/**
		 * The whistle will have ended in one of the last slices of the data block, 
		 * so the plan is to iterate backwards through the FFT data until we either 
		 * reach the start of the region OR run out of FFT data (It may have been discarded if
		 * the region was very long).
		 * 
		 *   We want a channel list of groupChannels, so set up a wantedChannels flag 
		 */
		int wantedChannels = 0;
		int firstSlice = region.getFirstSlice();
		List<SliceData> sliceList = region.getSliceData();
		ListIterator<SliceData> sliceIterator = sliceList.listIterator(sliceList.size()-1);
		SliceData sliceData;
		ComplexArray[] channelFFTData = new ComplexArray[PamConstants.MAX_CHANNELS];
		int iChan;
		for (int iC = 0; iC < nDelays; iC++) {
			delayMeasures[iC].clear(sourceData.getFftLength());
		}
		
		FFTDataUnit fftDataUnit;
//		System.out.println("Bearings for region length " + region.getDuration() + " start " + region.getFirstSlice());
		synchronized (sourceData) {
			ListIterator<FFTDataUnit> fftIterator = sourceData.getListIterator(PamDataBlock.ITERATOR_END);
			while (sliceIterator.hasPrevious()) {
				sliceData = sliceIterator.previous();
				wantedChannels = 0;
				while (fftIterator.hasPrevious()) {
					fftDataUnit = fftIterator.previous();
					if (fftDataUnit.getFftSlice() > sliceData.sliceNumber) {
						continue;
					}
					else if (fftDataUnit.getFftSlice() < firstSlice) {
						break;
					}
//					wantedChannels |= fftDataUnit.getChannelBitmap();	// use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
					wantedChannels |= fftDataUnit.getSequenceBitmap();
//					iChan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());	// use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
					iChan = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
					channelFFTData[iChan] = fftDataUnit.getFftData();
					if ((wantedChannels & channelMap) == channelMap) {
//						System.out.println("Complete slice data channels " + channelMap + " fft slice " + fftDataUnit.getFftSlice());
						/**
						 *  can now get the data out of the channelDataUnits which correspond to
						 *  the frequencies with hits in this slice.  
						 */
						for (int iPeak = 0; iPeak < sliceData.nPeaks; iPeak++) {
							for (int iF = sliceData.peakInfo[iPeak][0]; iF <= sliceData.peakInfo[iPeak][2]; iF++) {
								int iD = 0;
								for (int iC = 0; iC < nChannels-1; iC++) {
									for (int iC2 = iC+1; iC2 < nChannels; iC2++) {
										delayMeasures[iD++].addFFTData(channelFFTData[channelList[iC]], 
												channelFFTData[channelList[iC2]], iF, sliceData.sliceNumber);
									}
								}
							}
						}
						break; // break out of backward search through FFT data blocks.  
					}
				}
			}			
		} // end of source data synchronisation
		for (int iD = 0; iD < nDelays; iD++) {
			delays[iD] = delayMeasures[iD].getDelay(maxDelays[iD]);
		}
		
		return delays;
	}
	
	class DelayMeasure {
		
		private ComplexArray complexData;
		
		private int fftLength;

		private double scale2;

		private double scale1;

		private int lastSlice = -1;

		private int sliceCount;
		
		DelayMeasure(int fftLength) {
			this.fftLength = fftLength;
			complexData = new ComplexArray(fftLength);
		}
		
		private void clear(int fftLength) {
			if (complexData == null || complexData.length() != fftLength/2) {
				complexData = new ComplexArray(fftLength/2);
				this.fftLength = fftLength;
			}
			else {
				complexData.setZero();
			}
			scale1 = scale2 = 0;
			lastSlice = -1;
		}
		
		private void addFFTData(ComplexArray ch1, ComplexArray ch2, int iFreq, int iSlice) {
			scale1 += ch1.magsq(iFreq);
			scale2 += ch2.magsq(iFreq);
			if (iSlice == lastSlice) {
				sliceCount++;
			}
			else {
				sliceCount = 1;
				lastSlice = iSlice;
			}
			
			double[] d = complexData.getData();
			double ch1r = ch1.getReal(iFreq);
			double ch1i = ch1.getImag(iFreq);
			double ch2r = ch2.getReal(iFreq);
			double ch2i = ch2.getImag(iFreq);
			d[iFreq*2] += (ch1r*ch2r + ch1i*ch2i);// (ch1.real*ch2.real + ch1.imag*ch2.imag);
			d[iFreq*2+1] += (ch1i*ch2r - ch2i*ch1r);//(ch1.imag*ch2.real - ch2.imag*ch1.real);
		}
		
		private double getDelay(double maxDelay) {
//			int i2;
//			for (int i = 0; i < fftLength / 2; i++) {
//				i2 = fftLength - 1 - i;
//				complexData.setReal(i2, complexData.getReal(i));
//				complexData.setImag(i2, -complexData.getImag(i));
//			}
//			fft.ifft(complexData, fftLength);
//			ComplexArray bearingData = complexData;
//			double[] corrPeak = correlations.getInterpolatedPeak(complexData, scale, maxDelay);
			// swap to use more efficient inverse FFT that assumes conjugate. Faster and more accurate. 
			double scale = Math.sqrt(scale1*scale2)*2/fftLength;
			double[] xCorr = fft.realInverse(complexData);
			double[] corrPeak = correlations.getInterpolatedPeak(xCorr, scale, maxDelay);
			return corrPeak[0];
			
		}
		
		private double getPeakPos(Complex[] bearingData) {
			double[] realData = new double[bearingData.length];
			double max = 0;
			double maxInd = 0;
			int l2 = bearingData.length/2;
			for (int i = 0; i < l2; i++) {
				realData[i+l2] = bearingData[i].real;
				realData[i] = bearingData[i+l2].real;
			}
			int l3 = l2*2-1;
			double y1, y2, y3;
			double a, b, c;
			double x, y;
			// work out peak height for every position. 
			for (int i = 1; i < l3; i++) {
				y1 = realData[i-1];
				y2 = realData[i];
				y3 = realData[i+1];
				if (y2 > y1 && y2 > y3) {
					a = (y3+y1-2*y2)/2.;
					b = (y3-y1)/2;
					c = y2;
					x = -b/2./a;
					y = a*x*x+b*x+c;
					if (y > max) {
						max = y;
						maxInd = i+x;
					}
				}
//				if (bearingData[i].real > max) {
//					max = bearingData[i].real;
//					maxInd = i;
//				}
			}			
			return maxInd - l2;
		}
		
	}
}
