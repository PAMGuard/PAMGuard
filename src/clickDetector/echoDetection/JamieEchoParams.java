package clickDetector.echoDetection;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class JamieEchoParams implements Serializable, Cloneable, ManagedParameters {
	
public static final long serialVersionUID = 3L;
	
//these are default settings for a porpoise;

	public double maxAmpDifference=0.5;
	public double maxICIDifference=0.2;
	public double maxIntervalSeconds = 0.0012;


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected JamieEchoParams clone() {
		try {
			return (JamieEchoParams) super.clone();
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
