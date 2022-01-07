package PamUtils.time.ntp;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class NTPTimeParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int MININTERVAL = 300;

	public String serverName = "time.nist.com"; /*"time.google.com"*/;

	public int intervalSeconds = 300; // query once per minute.
	
	public boolean activate = false;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected NTPTimeParameters clone()  {
		try {
			return (NTPTimeParameters) super.clone();
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
