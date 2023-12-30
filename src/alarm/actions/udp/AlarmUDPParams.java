package alarm.actions.udp;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class AlarmUDPParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public String destAddr = "localhost";
	
	public int destPort = 8111;

	@Override
	public AlarmUDPParams clone() {
		try {
			return (AlarmUDPParams) super.clone();
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
