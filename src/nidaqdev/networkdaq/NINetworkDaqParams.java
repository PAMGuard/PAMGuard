package nidaqdev.networkdaq;

import java.io.Serializable;
import java.util.ArrayList;

import Acquisition.DaqSystemXMLManager;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class NINetworkDaqParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public static final int[] SampleRates = {500000};
	
	public static final int[] NChannels = {4, 8, 12};
	
//	public static final int[] CHASSISTYPES = {9063, 90639222, 9067, 9068};
	
	public String niAddress = "169.254.88.87";
	
	public ArrayList<String> recentAddresses = new ArrayList();
	
	public int niUDPPort = 8000;
	
	public int niTCPPort = 8023;
	
	public int sampleRateIndex = 0;
	
	public int nChannelsIndex = 0;
	
//	public int chassisTypeIndex = 0;
	public ChassisConfig chassisConfig = ChassisConfig.NI90639222;
	
	private String workingDir = "/home/admin/cRioKE";
	
	private String exeName = "cRioTestC";
	
	private String linuxUser = "admin";
	
	private String linuxPassword = "";

	public int startFailures = 0;
	
	public int verboseLevel = 0;

	public NINetworkDaqParams() {
		super();
		recentAddresses.add("localhost");
	}

	/**
	 * Add a new ip address to the list of recent addresses. 
	 * @param newIpAddr new ip address. 
	 */
	public void newAddress(String newIpAddr) {
		int currInd = recentAddresses.indexOf(newIpAddr.toString());
		if (currInd >= 0) {
			recentAddresses.remove(newIpAddr);
			recentAddresses.add(0, newIpAddr);
		}
		else {
			recentAddresses.add(0, newIpAddr);
		}
		// limit the size of the remembered list to 10 items. 
		while (recentAddresses.size() > 10) {
			recentAddresses.remove(recentAddresses.size()-1);
		}
	}
	
	public int getSampleRate() {
		return SampleRates[sampleRateIndex];
	}
	
	public int getNChannels() {
		return NChannels[nChannelsIndex];
	}

	/**
	 * @return the workingDir
	 */
	public String getWorkingDir() {
		if (workingDir == null) {
			workingDir = (new NINetworkDaqParams()).getWorkingDir();
		}
		return workingDir;
	}

	/**
	 * @param workingDir the workingDir to set
	 */
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * @return the exeName
	 */
	public String getExeName() {
		if (exeName == null) {
			exeName = (new NINetworkDaqParams()).getExeName();
		}
		/*
		 * Check that the executable starts with ".\"
		 */
		if (exeName == null) {
			return null;
		}
		if (exeName.startsWith("./") == false) {
			exeName = "./" + exeName;
		}
		return exeName;
	}
	
//	public int getChassisType() {
//		return CHASSISTYPES[chassisTypeIndex];
//	}

	/**
	 * @param exeName the exeName to set
	 */
	public void setExeName(String exeName) {
		this.exeName = exeName;
	}

	@Override
	public NINetworkDaqParams clone() {
		try {
			NINetworkDaqParams newParams = (NINetworkDaqParams) super.clone();
			if (newParams.linuxUser == null) {
				newParams.linuxUser = "admin";
				newParams.linuxPassword = "";
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the linuxUser
	 */
	public String getLinuxUser() {
		return linuxUser;
	}

	/**
	 * @param linuxUser the linuxUser to set
	 */
	public void setLinuxUser(String linuxUser) {
		this.linuxUser = linuxUser;
	}

	/**
	 * @return the linuxPassword
	 */
	public String getLinuxPassword() {
		return linuxPassword;
	}

	/**
	 * @param linuxPassword the linuxPassword to set
	 */
	public void setLinuxPassword(String linuxPassword) {
		this.linuxPassword = linuxPassword;
	}

	@Override
	public PamParameterSet getParameterSet() {
		if (DaqSystemXMLManager.isSelected(NINetworkDaq.systemName)) {
			return PamParameterSet.autoGenerate(this);
		}
		return null;
	}
}
