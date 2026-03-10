package Acquisition;

import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Check data resolution to be sure that it really is being digitised at the resolution it claims. 
 * This is important for sound cards, which might give 24 bit data, but it's really only 16 bit data
 * that's been scaled up x256. 
 * @author dg50
 *
 */
public class ResolutionChecker {

	private int bitDepth;
	
	private double scaleMax = 1;
	
	private long totalSamples;
	
	private long maskSamples;
	
	private int bitMask = 0x7F;
	
	private PamWarning warning;

	private AcquisitionProcess daqProcess;

	public ResolutionChecker(AcquisitionProcess daqProcess, int bitDepth) {
		this.daqProcess = daqProcess;
		setDepth(bitDepth);
	}
	
	public void setDepth(int bitDepth) {
		this.bitDepth = bitDepth;
		scaleMax = 1<<(bitDepth-1);
		totalSamples = 0;
		maskSamples = 0;
	}

	/**
	 * Check bit resolution in the data. 
	 * @param rawData
	 */
	public void checkResolution(double[] rawData) {
		if (rawData == null) {
			return;
		}
		for (int i = 0; i < rawData.length; i++) {
			int dat = (int) Math.round(rawData[i]*scaleMax);
			dat &= bitMask;
			if (dat > 0) {
				maskSamples ++;
			}
		}
		totalSamples += rawData.length;
		checkResult();
	}

	private void checkResult() {
		if (maskSamples == 0) {
			if (warning == null) {
				String msg = String.format("Your raw audio data do not appear to have the %d bit resolution you've configured. Check device configuration", bitDepth);
				warning = new PamWarning(daqProcess.acquisitionControl.getUnitName(), msg, 1);
			}
			WarningSystem.getWarningSystem().addWarning(warning);
		}
		else {
			WarningSystem.getWarningSystem().removeWarning(warning);
			warning = null;
		}
	}

}
