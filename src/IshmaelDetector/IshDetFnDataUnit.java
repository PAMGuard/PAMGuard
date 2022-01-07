package IshmaelDetector;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * Standard data unit for Ishmael detector output. 
 * @author David Mellinger 
 *
 */
@SuppressWarnings("rawtypes")
public class IshDetFnDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {
	double[][] detData;               //a sequence of detection function points
	
	public IshDetFnDataUnit(long timeMilliseconds, int channelBitmap, long startSample,
			long duration, double[] detData) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.detData = new double[][]{detData};
	}

	/**
	 * Get Ishmael detector data. The first 1D array (detData[0]) is the detector output
	 * The second 1D array (deData[0]) is the noise floor of the detector. Null if the detector 
	 * has a static threshold. 
	 * @return ishmael detector data 
	 */
	public double[][] getDetData() {
		return detData;
	}
	
	/**
	 * Set the Ishmael detector data. The first 1D array (detData[0]) is the detector output
	 * The second 1D array (deData[0]) is the noise floor of the detector. Null if the detector 
	 * has a static threshold. 
	 * @return Ishmael detector data 
	 */
	public void setDetData(double[][] detData) {
		this.detData = detData;
	}
	
	/**
	 * Set the Ishmael detector output data. 
	 * @return Ishmael detector output. 
	 */
	public void setDetData(double[] detData) {
		this.detData = new double[][] {detData};
	}
	
}