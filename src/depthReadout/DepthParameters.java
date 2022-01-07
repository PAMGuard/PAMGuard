package depthReadout;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class DepthParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 0;

	public int systemNumber;

	public double pollTime = 2;

	/**
	 * This is actually a streamer map now, not hydrophones
	 * but leave it alone. 
	 */
	public int[] hydrophoneMaps;
	
	@Deprecated // now all besed on streamer, not hydrophone
	private double[] hydrophoneY;
	
	public int nSensors = 2;

	@Override
	public DepthParameters clone() {
		try {
			return (DepthParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

	/**
	 * Get a bitmap of all used streamers. 
	 * @return
	 */
	public int getTotalStreamerMap() {
		if (hydrophoneMaps == null) {
			return 0;
		}
		int totMap = 0;
		for (int i = 0; i < hydrophoneMaps.length; i++) {
			totMap |= hydrophoneMaps[i];
		}
		return totMap;
	}
}
