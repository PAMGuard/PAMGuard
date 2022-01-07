package clickDetector.background;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.background.BackgroundDataUnit;

public class ClickBackgroundDataUnit extends BackgroundDataUnit {

	private double[] levels;

	private double meanLevel;

	public ClickBackgroundDataUnit(long timeMilliseconds, int channelBitmap, double[] levels) {
		super(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setLevels(levels);
	}

	public ClickBackgroundDataUnit(DataUnitBaseData dataUnitBaseData, double[] levels) {
		super(dataUnitBaseData);
		setLevels(levels);
	}

	@Override
	public double getCountSPL() {
		return meanLevel;
	}

	/**
	 * @return the levels
	 */
	public double[] getLevels() {
		return levels;
	}

	/**
	 * Set the levels for multiple channels and also calculate their mean. 
	 * @param levels the levels to set
	 */
	public void setLevels(double[] levels) {
		this.levels = levels;
		meanLevel = 0;
		for (int i = 0; i < levels.length; i++) {
			meanLevel += levels[i];
		}
		meanLevel /= levels.length;
	}

}
