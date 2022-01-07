package levelMeter;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class LevelMeterParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int DISPLAY_FULLSCALE = 0;
	public static final int DISPLAY_VOLTS = 1;
	public static final int DISPLAY_MICROPASCAL = 2;
	public static final int DISPLAY_PEAK = 0;
	public static final int DISPLAY_RMS = 1;
	
	public String dataName;
	
	public int minLevel = -80;
	
	public int scaleReference = DISPLAY_FULLSCALE;
	public int scaleType = DISPLAY_PEAK;
	

	@Override
	protected LevelMeterParams clone() {
		try {
			return (LevelMeterParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
