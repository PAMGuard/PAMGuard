package soundtrap;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class STToolsParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String sourceFolder;
	
	public boolean sourceSubFolders = false;
	
	public String destFolder;
	
	public String clickDetName = "Click Detector";
	
	private String soundAcqName = "SoundTrap Acquisition";
	
	private String customDate = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * Returns the name of the sound acquisition module to use.  If null, sets it to
	 * the default
	 * @return the soundAcqName
	 */
	public String getSoundAcqName() {
		if (soundAcqName==null) soundAcqName = "SoundTrap Acquisition";
		return soundAcqName;
	}

	/**
	 * @param soundAcqName the soundAcqName to set
	 */
	public void setSoundAcqName(String soundAcqName) {
		this.soundAcqName = soundAcqName;
	}

	public String getCustomDate() {
		return customDate;
	}

	public void setCustomDate(String customDate) {
		this.customDate = customDate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected STToolsParams clone() {
		try {
			return (STToolsParams) super.clone();
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
