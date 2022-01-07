package networkTransfer.receive;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class NetworkReceiveParams implements Cloneable, Serializable, ManagedParameters {
	
	public static final int CHANNELS_RENUMBER = 1;
	public static final int CHANNELS_MAINTAIN = 2;

	public static final long serialVersionUID = 1L;
	
	private static final int DEFAULTUDPPORT = 8888;
	
	public int receivePort = DEFAULTUDPPORT;
	
	public int udpControlPort = 8888;
	
	public int channelNumberOption = CHANNELS_RENUMBER;
	
//	public boolean gpsHeading = GPS_POSITIONON
	public double compassCorrection = 0;


	@Override
	public NetworkReceiveParams clone() {
		try {
			NetworkReceiveParams newParams = (NetworkReceiveParams) super.clone();
			if (newParams.udpControlPort == 0) {
				newParams.udpControlPort = DEFAULTUDPPORT;
			}
			return newParams;
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
