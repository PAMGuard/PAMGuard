package networkTransfer.receive;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import networkTransfer.NetworkParams;

public class NetworkReceiveParams extends NetworkParams implements Cloneable, Serializable, ManagedParameters {
	
	public static final int CHANNELS_RENUMBER = 1;
	public static final int CHANNELS_MAINTAIN = 2;
	
	public static final int CONNECTIONTYPE_STANDARD_TCP = 1;
	public static final int CONNECTIONTYPE_MQTT = 2;


	public static final long serialVersionUID = 1L;
	
	private static final int DEFAULTUDPPORT = 8888;
//	
	public int receivePort = DEFAULTUDPPORT;
	
	public int udpControlPort = 8888;
	
	public int connectionType = CONNECTIONTYPE_STANDARD_TCP;
	
	public int channelNumberOption = CHANNELS_RENUMBER;
	
//	public boolean gpsHeading = GPS_POSITIONON
	public double compassCorrection = 0;
	
	//public String stationName = "base";

	@Override
	public NetworkReceiveParams clone() {
		NetworkReceiveParams newParams = (NetworkReceiveParams) super.clone();
		if (newParams.udpControlPort == 0) {
			newParams.udpControlPort = DEFAULTUDPPORT;
		}
		return newParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
