package alfa;

import PamModel.PamModuleInfo;

/**
 * Information about a module that needs to be monitored for status. <br>
 * Wraps around and extends PamModuleInfo to give extra functionality for 
 * advanced status monitoring. 
 * @author dg50
 *
 */
public class ControlledModuleInfo {
	

	/**
	 * Standard PAMGuard module info for the class, tells
	 * PAMGuard how to construct it, a default name, etc. 
	 */
	private PamModuleInfo pamModuleInfo;
	
	/**
	 * A fixed module name. Can be used to force a particular name rather
	 * than just using the default value. 
	 */
	private String fixedModuleName;

	public ControlledModuleInfo(PamModuleInfo pamModuleInfo) {
		this.pamModuleInfo = pamModuleInfo;
	}

	/**
	 * @param pamModuleInfo
	 * @param fixedModuleName
	 */
	public ControlledModuleInfo(PamModuleInfo pamModuleInfo, String fixedModuleName) {
		super();
		this.pamModuleInfo = pamModuleInfo;
		this.fixedModuleName = fixedModuleName;
	}

	/**
	 * @param className
	 * @param fixedModuleName
	 */
	public ControlledModuleInfo(Class className, String fixedModuleName) {
		this(PamModuleInfo.findModuleInfo(className.getName()), fixedModuleName);
	}

	/**
	 * @param className
	 */
	public ControlledModuleInfo(Class className) {
		this(PamModuleInfo.findModuleInfo(className.getName()));
	}

	/**
	 * @return the pamModuleInfo
	 */
	public PamModuleInfo getPamModuleInfo() {
		return pamModuleInfo;
	}

	/**
	 * @param pamModuleInfo the pamModuleInfo to set
	 */
	public void setPamModuleInfo(PamModuleInfo pamModuleInfo) {
		this.pamModuleInfo = pamModuleInfo;
	}

	/**
	 * @return the fixedModuleName
	 */
	public String getFixedModuleName() {
		return fixedModuleName;
	}

	/**
	 * @param fixedModuleName the fixedModuleName to set
	 */
	public void setFixedModuleName(String fixedModuleName) {
		this.fixedModuleName = fixedModuleName;
	}

	public String getDefaultName() {
		if (fixedModuleName != null) {
			return fixedModuleName;
		}
		else {
			return pamModuleInfo.getDefaultName();
		}
	}

}
