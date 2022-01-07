package clickDetector.echoDetection;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class SimpleEchoParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public double maxIntervalSeconds = 0.1;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected SimpleEchoParams clone() {
		try {
			return (SimpleEchoParams) super.clone();
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
