package clickDetector.basicalgorithm;

import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class TriggerBackgroundDataUnit extends PamDataUnit {

	private double[] backgroundData;
	
	private double[] absoluteAmplitudes;

	public TriggerBackgroundDataUnit(long timeMilliseconds, int channelBitmap, double[] backgroundData) {
		super(timeMilliseconds);
		this.setChannelBitmap(channelBitmap);
		this.backgroundData = backgroundData;
	}

	public TriggerBackgroundDataUnit(DataUnitBaseData baseData, double[] backgroundData) {
		super(baseData);
		this.backgroundData = backgroundData;
	}

	/**
	 * @return the backgroundData
	 */
	public double[] getBackgroundData() {
		return backgroundData;
	}
	
	/**
	 * 
	 * @return values in dB re 1uPa using PAMGuard calibration data. 
	 */
	public double[] getAbsoluteAmplitudes() {
		if (absoluteAmplitudes == null) {
			calculateAbsoluteAmplitudes();
		}
		return absoluteAmplitudes;
	}

	/**
	 * @param absoluteAmplitudes the absoluteAmplitudes to set
	 */
	public void setAbsoluteAmplitudes(double[] absoluteAmplitudes) {
		this.absoluteAmplitudes = absoluteAmplitudes;
	}

	/**
	 * Calculate the absolute amplitudes. 
	 */
	private void calculateAbsoluteAmplitudes() {
		if (backgroundData == null) {
			return;
		}
		int nChan = backgroundData.length;
		PamProcess sourceProcess = getParentDataBlock().getSourceProcess();
		if (sourceProcess instanceof AcquisitionProcess) {
			AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess; 
			absoluteAmplitudes = new double[nChan];
			for (int i = 0; i < nChan; i++) {
				int chan = PamUtils.getNthChannel(i, getChannelBitmap());
				absoluteAmplitudes[i] = daqProcess.rawAmplitude2dB(backgroundData[i], chan, false);
			}
		}
		else {
			absoluteAmplitudes = null;
		}
	}

	/**
	 * @param backgroundData the backgroundData to set
	 */
	public void setBackgroundData(double[] backgroundData) {
		this.backgroundData = backgroundData;
	}

	/**
	 * 
	 * @return the largest value. should all be positive so no need to 
	 * check for negative values. 
	 */
	public double getMaxValue() {
		if (backgroundData == null || backgroundData.length < 1) {
			return 0;
		}
		double max = backgroundData[0];
		for (int i = 1; i < backgroundData.length; i++) {
			max = Math.max(max, backgroundData[i]);
		}
		return max;
	}

}
