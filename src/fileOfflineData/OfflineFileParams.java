package fileOfflineData;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class OfflineFileParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String offlineFolder;
	
	public boolean subFolders = true;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected OfflineFileParams clone() {
		try {
			return (OfflineFileParams) super.clone();
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
