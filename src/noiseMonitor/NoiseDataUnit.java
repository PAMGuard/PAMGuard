package noiseMonitor;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class NoiseDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	/**
	 * noiseBandData is an n*6 array n being the number
	 * of bands and the 6 measures being "mean", "median", "low95", "high95"
	 * respectively. 
	 * Each data unit contains data for one channel.  
	 */
	private double[][] noiseBandData;
	
	public NoiseDataUnit(long timeMilliseconds, int channelBitmap,
			long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param noiseBandData the noiseBandData to set
	 */
	public void setNoiseBandData(double[][] noiseBandData) {
		this.noiseBandData = noiseBandData;
	}

	/**
	 * @return the noiseBandData
	 */
	public double[][] getNoiseBandData() {
		return noiseBandData;
	}

}
