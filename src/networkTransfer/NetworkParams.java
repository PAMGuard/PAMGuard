package networkTransfer;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class NetworkParams implements Cloneable, Serializable, ManagedParameters{
	
	public String password;
	
	public String userId;
	
	public boolean useSSL = false;
	
	public boolean useSystemTrustStore;
	
	public String trustStorePath;
	
	public String trustStorePassword;
	
	public String keyStorePath;
	
	public String keyStorePassword;
	
	public String persistenceDirectory;
	
	public boolean mqtt = false;
	
	public String ipAddress = "localhost";
	
	public int portNumber = 8011;
	
	public boolean savePassword = true;
	
	public String baseTopic;
	
	public String stationId;

	@Override
	public NetworkParams clone() {
		try {
			NetworkParams newParams = (NetworkParams) super.clone();
			return newParams;
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
