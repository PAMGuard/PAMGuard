package networkTransfer;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.FileFunctions;
import pamguard.Pamguard;

public class NetworkParams implements Cloneable, Serializable, ManagedParameters{
	
	private static final long serialVersionUID = 1L;

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
	
	
	/**
	 * Check the setting for base topic. If it has not been set in these parameters, then autofill with 'Base Station' 
	 * (It will always be set in an APS run, so a safe default for base station contexts.)
	 */
	public void checkStationID() {
		if(this.stationId==null || this.stationId.isEmpty() || this.stationId.isBlank()) {
			this.stationId = "BaseStation";
		}
	}
	
	/**
	 * Check the setting for base topic. If it has not been set in these parameters, then autofill with 'APS'
	 */
	public void checkBaseTopic() {
		if(this.baseTopic==null || this.baseTopic.isEmpty() || this.baseTopic.isBlank()) {
			this.baseTopic = "APS";
		}
		
	}
	
	/**
	 * Double check that the persistance directory is correct. 
	 * It should generally be in the pamguard home folder, 
	 * but for some reason if it is set to something else, 
	 * and the path structure is consistent with the OS, leave it as saved in parameters. 
	 * 
	 */
	public void verifyCorrectPersistanceDirectory() {
		//Check if the configured directory is Pamguard Home. If so, make sure that it is the correct path for the machine being used. 
		if(this.persistenceDirectory!=null) {
			if(FileFunctions.isWindows()) {
				if(isLinuxFilepath(this.persistenceDirectory)) {
					this.persistenceDirectory = Pamguard.getSettingsFolder();
				}else if(isWindowsPGHomeDir(this.persistenceDirectory)) {
					this.persistenceDirectory = Pamguard.getSettingsFolder();
				}
			}else {
				if(!isLinuxFilepath(this.persistenceDirectory)) {
					this.persistenceDirectory = Pamguard.getSettingsFolder();
				}
			}
		}else {
			this.persistenceDirectory = Pamguard.getSettingsFolder();
		}
	}
	
	//Helper functions for verifyCorrectPersistanceDirectory()
	private boolean isWindowsPGHomeDir(String dir) {
		boolean users_there = false;
		boolean pg_there = false;
		int levels = 0;
		for (Path p : Paths.get(dir)) {
			if(levels==0 && p.toString().equals("Users")) {users_there=true;}
			if(levels==2 && p.toString().equals("Pamguard")) {pg_there=true;}
		    levels++;
		}
		if(levels == 3 && users_there && pg_there) {
			return true;
		}
		return false;
	}
	
	private boolean isLinuxFilepath(String path) {
		if(path.charAt(1)==':') {
			return false;
		}
		return true;
	}
		

}
