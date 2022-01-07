package PamUtils.time;

import java.io.Serializable;
import java.util.TimeZone;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class TimeDisplayParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public static final int TIME_ZONE_UTC = 0;
	public static final int TIME_ZONE_PC = 1;
	public static final int TIME_ZONE_OTHER = 1;
	
	public int zoneType = TIME_ZONE_UTC;

	public TimeZone timeZone;
	
	@Override
	protected TimeDisplayParameters clone() {
		try {
			return (TimeDisplayParameters) super.clone();
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
