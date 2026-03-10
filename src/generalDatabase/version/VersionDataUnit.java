package generalDatabase.version;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

/**
 * For logging PAMGuard version information. 
 */
public class VersionDataUnit extends PamDataUnit {

	
	private String name;
	private String className;
	private String version;

	public VersionDataUnit(long timeMilliseconds, String name, String className, String version) {
		super(timeMilliseconds);
		this.name = name;
		this.className = className;
		this.version = version;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}


}
