package binaryFileStorage;

import java.io.File;
import java.io.Serializable;

import PamController.PamFolders;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class BinaryStoreSettings implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 0L;
	
	private String storeLocation = PamFolders.getDefaultProjectFolder();
	
	public boolean datedSubFolders = true;

	public boolean autoNewFiles = true;
	
	public int fileSeconds = 3600;
	
	public boolean limitFileSize = true;
	
	public int maxFileSize = 30; // max file size in megabytes.  
	
	public long lastZippedFileDate;
	
	private NoiseStoreType noiseStoreType = NoiseStoreType.PGNF;
	
	/**
	 * Channel shift parameter for secondary binary storage units. 
	 * Will always be zero for the main binary store. 
	 */
	public int channelShift = 0;
	
	public BinaryStoreSettings() {
		super();
		storeLocation = PamFolders.getDefaultProjectFolder() + File.separator + "PAMBinary";
	}

	/**
	 * test to see if it's necessary to open new stores
	 * @param other
	 * @return true if the output folder or sub folders flag have changed 
	 * false for other changes. 
	 */
	boolean isChanged(BinaryStoreSettings other) {
		if (storeLocation == null && other.storeLocation == null) {
			return false;
		}
		if (storeLocation == null || other.storeLocation == null) {
			return true;
		}
		if (!storeLocation.equals(other.storeLocation)) {
			return true;
		}
		if (datedSubFolders != other.datedSubFolders) {
			return true;
		}
		
		return false;
	}

	@Override
	public BinaryStoreSettings clone() {
		try {
			return (BinaryStoreSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public int getMaxSizeMegas() {
		return Math.max(1, maxFileSize) * 1024*1024;
	}
	
	public String getStoreLocation() {
		return storeLocation;
	}
	
	public void setStoreLocation(String storeLocation) {
		this.storeLocation = storeLocation;
	}
 
	public boolean isDatedSubFolders() {
		return datedSubFolders;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	/**
	 * @return the noiseStoreType
	 */
	public NoiseStoreType getNoiseStoreType() {
		if (noiseStoreType == null) {
			noiseStoreType = NoiseStoreType.PGNF;
		}
		return noiseStoreType;
	}

	/**
	 * @param noiseStoreType the noiseStoreType to set
	 */
	public void setNoiseStoreType(NoiseStoreType noiseStoreType) {
		this.noiseStoreType = noiseStoreType;
	}
	
}
