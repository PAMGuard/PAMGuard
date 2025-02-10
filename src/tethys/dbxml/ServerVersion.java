package tethys.dbxml;

public class ServerVersion {

	private float versionNo;
	private String versionName;
	
	public ServerVersion(float versionNo, String versionName) {
		super();
		this.versionNo = versionNo;
		this.versionName = versionName;
	}
	/**
	 * @return the versionNo
	 */
	public float getVersionNo() {
		return versionNo;
	}
	/**
	 * @return the versionName
	 */
	public String getVersionName() {
		return versionName;
	}
	
	@Override
	public String toString() {
		Float f = versionNo;
		return String.format("%s %s", f.toString(), versionName);
	}


}
