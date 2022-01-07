package fftManager;

import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class FFTDataUnit extends DataUnit2D<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	private ComplexArray fftData;

	private int fftSlice;

	/**
	 * Range of useful bins. Either a two element array or null. 
	 * This may be set by localisers, etc. to 
	 * indicate which parts of the spectrum to actually use in localisation
	 * calculations, etc. It does not mean that regions of the spectrum outside this
	 * range are zero.
	 */
	private int[] usefulBinRange;

	public FFTDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration, ComplexArray fftData, int fftSlice) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.fftData = fftData;
		this.fftSlice = fftSlice;
	}

	public void setInfo(long timeMilliseconds, int channelBitmap, long startSample, long duration, int fftSlice) {
		// TODO Auto-generated method stub
		super.setInfo(timeMilliseconds, channelBitmap, startSample, duration);
		this.fftSlice = fftSlice;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		//		System.out.println("FinaliseFFT data unit at " + PamCalendar.formatDateTime(System.currentTimeMillis()));
		//		recycleComplexData(fftData);
		super.finalize();
	}

	private void recycleComplexData(Complex[] fftData) {
		FFTDataBlock fftDataBlock = (FFTDataBlock) getParentDataBlock();
		if (fftDataBlock != null && fftData != null) {
			fftDataBlock.recycleComplexArray(fftData);
		}
	}

	public ComplexArray getFftData() {
		return fftData;
	}

	public void setFftData(ComplexArray fftData) {
		this.fftData = fftData;
	}

	public int getFftSlice() {
		return fftSlice;
	}

	/**
	 * Return the values in decibels (spectrum level I think).  
	 * Should be good to go for plotting on the spectrogram.
	 */
	@Override
	public double[] getMagnitudeData() {
		/**
		 * Return the values in decibels (spectrum level I think). 
		 */
		if (fftData == null) {
			return null;
		}

		double[] magSqData =  fftData.magsq();
		AcquisitionProcess daqProcess = null;

		//		int iChannel = PamUtils.getSingleChannel(getChannelBitmap());
	
		int iChannel = this.getParentDataBlock().getARealChannel(PamUtils.getSingleChannel(getChannelBitmap()));
		
		double gain = getParentDataBlock().getCumulativeGain(iChannel);
		if (gain == 0) {
			getParentDataBlock().getCumulativeGain(iChannel);
		}

		// get the acquisition process. 
		try {
			daqProcess = (AcquisitionProcess) (getParentDataBlock().getSourceProcess());
			daqProcess.prepareFastAmplitudeCalculation(iChannel);
		}
		catch (ClassCastException e) {
			return magSqData;
		}
		double mGain = gain/gain;
		for (int i = 0; i < magSqData.length; i++) {
			magSqData[i] = daqProcess.fftAmplitude2dB(magSqData[i]/mGain, iChannel, 
					getParentDataBlock().getSampleRate(), magSqData.length*2, true, true);
		}
		return magSqData;
	}

	/**
	 * Range of useful bins. Either a two element array or null. 
	 * This may be set by localisers, etc. to 
	 * indicate which parts of the spectrum to actually use in localisation
	 * calculations, etc. It does not mean that regions of the spectrum outside this
	 * range are zero.
	 * <br> upper limit may be the length of the array, i.e. operations should loop<br>
	 * for (ibin = usefulBin[0]; ibin < usefulBin[1]; ibin++), etc ...
	 * @return the usefulBinRange
	 */
	public int[] getUsefulBinRange() {
		return usefulBinRange;
	}

	/**
	 * Range of useful bins. Either a two element array or null. 
	 * This may be set by localisers, etc. to 
	 * indicate which parts of the spectrum to actually use in localisation
	 * calculations, etc. It does not mean that regions of the spectrum outside this
	 * range are zero.
	 * <br> upper limit may be the length of the array, i.e. operations should loop<br>
	 * for (ibin = usefulBin[0]; ibin < usefulBin[1]; ibin++), etc ...
	 * @param usefulBinRange the usefulBinRange to set
	 */
	public void setUsefulBinRange(int[] usefulBinRange) {
		this.usefulBinRange = usefulBinRange;
	}

	public double[] getSpectrogramData() {
		return getMagnitudeData();
	}

}
