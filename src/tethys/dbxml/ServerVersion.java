package tethys.dbxml;

public class ServerVersion {


	/**
	 * Minimum version that the current version of the Tethys PAMGuard interface can work with. 
	 */
	public static final float MINSERVERVERSION = 3.2F;

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
