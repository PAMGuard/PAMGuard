package Acquisition;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Used by FileInputSystem
 * @author Doug Gillespie
 * @see Acquisition.FileInputSystem
 *
 */
public class FileInputParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 1;
	
	static public final int MAX_RECENT_FILES = 20;
	
	/**
	 * A list of the recent files or folders selected. 
	 */
	public ArrayList<String> recentFiles = new ArrayList<String>();
	
	public boolean realTime;

	public String getMostRecentFile() {
		if (recentFiles.size() > 0) {
			return recentFiles.get(0);
		}
		else {
			return null;
		}
	}
	
	public String systemType;
	
	/**
	 * Repeat in an infinite loop
	 */
	public boolean repeatLoop; 
	
	/**
	 * Skip a section an initial section of the file in millis. For example, use with SoundTrap calibration values. 
	 */
	public long skipStartFileTime = 0;
	
	public int bitDepth;
	
	/**
	 * @param systemType
	 */
	public FileInputParameters(String systemType) {
		super();
		this.systemType = systemType;
	}

	@Override
	protected FileInputParameters clone() {
		try{
			FileInputParameters newParams = (FileInputParameters) super.clone();
			if (newParams.bitDepth == 0) {
				newParams.bitDepth = 16;
			}
			return newParams;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected this system type, just return null
		if (!DaqSystemXMLManager.isSelected(systemType)) {
			return null;
		}

		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
}
