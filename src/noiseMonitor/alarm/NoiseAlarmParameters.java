package noiseMonitor.alarm;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class NoiseAlarmParameters implements Serializable, Cloneable, ManagedParameters {


	public static final long serialVersionUID = 0L;
	
	public boolean[] selectedBands;
	
	public int usedMeasure = 0;

	@Override
	protected NoiseAlarmParameters clone() {
		try {
			return (NoiseAlarmParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
