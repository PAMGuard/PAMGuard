package PamUtils.time.nmea;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class NMEATimeParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public String nmeaSource;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected NMEATimeParameters clone() {
		try {
			return (NMEATimeParameters) super.clone();
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
