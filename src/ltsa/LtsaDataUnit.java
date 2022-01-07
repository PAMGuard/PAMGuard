package ltsa;

import fftManager.FFTDataUnit;

public class LtsaDataUnit extends FFTDataUnit {

	private long endMilliseconds;
	
	private int nFFT;

	public LtsaDataUnit(long timeMilliseconds, long endMilliseconds, int nFFT, int channelBitmap,
			long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration, null, 0);
		this.endMilliseconds = endMilliseconds;
		this.nFFT = nFFT;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the endMilliseconds
	 */
	public long getEndMilliseconds() {
		return endMilliseconds;
	}

	/**
	 * @return the nFFT
	 */
	public int getnFFT() {
		return nFFT;
	}

}
