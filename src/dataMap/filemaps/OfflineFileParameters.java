package dataMap.filemaps;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class OfflineFileParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Enable offline file access
	 */
	public boolean enable;
	
	/**
	 * include sub folders
	 */
	public boolean includeSubFolders;
	
	/**
	 * Reference to wherever the offline files are. 
	 */
	public String folderName;

	@Override
	protected OfflineFileParameters clone()  {
		try {
			return (OfflineFileParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}
	
}
