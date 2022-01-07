package cpod;

import fileOfflineData.OfflineFileParams;

public class CPODParams extends OfflineFileParams {


	public static final long serialVersionUID = 1L;
	public double startOffset;
	public double timeStretch; // as a percentage. 
	
	public CPODParams(OfflineFileParams offlineFileParams) {
		super();
		if (offlineFileParams != null) {
			this.offlineFolder = offlineFileParams.offlineFolder;
			this.subFolders = offlineFileParams.subFolders;
		}
	}

	/* (non-Javadoc)
	 * @see fileOfflineData.OfflineFileParams#clone()
	 */
	@Override
	protected CPODParams clone() {
		return (CPODParams) super.clone();
	}


}
