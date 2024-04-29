package whistlesAndMoans.alarm;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.dataSelector.DataSelectParams;

public class WMAlarmParameters extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public double minFreq = 0.;
	public double maxFreq = 30000.;
	public double minAmplitude = 90.;
	public double minLengthMillis = 0.0;
	public boolean superDetOnly = false;

	@Override
	public WMAlarmParameters clone()  {
		try {
			return (WMAlarmParameters) super.clone();
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
