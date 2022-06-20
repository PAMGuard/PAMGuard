package PamModel;


/**
 * 
 * Dependency information if this module can ony work if some other module is
 * providing appropriate data. When a module is created, checks will run to make
 * sure that the other data exist and dialogs will help the user to create
 * approriate data sources for this module. 
 * @author Doug Gillespie
 * @see PamModel
 * @see PamModuleInfo
 *
 */
public class PamDependency {

	private Class requiredDataType;
	
	private String defaultProvider;
	
	private String dataBlockName;

	/**
	 * 
	 * @param requiredDataType Class of Data unit
	 * @param defaultProvider Class name of default provider.
	 * @param dataBlockName Specific data block name
	 */
	public PamDependency(Class requiredDataType, String defaultProvider, String dataBlockName) {
		this.requiredDataType = requiredDataType;
		this.defaultProvider = defaultProvider;
		this.dataBlockName = dataBlockName;
	}

	/**
	 * @param requiredDataType Class of Data unit
	 * @param defaultProvider Class name of default provider.
	 */
	public PamDependency(Class requiredDataType, String defaultProvider) {
		this.requiredDataType = requiredDataType;
		this.defaultProvider = defaultProvider;
	}

	/**
	 * @return Returns the defaultProvider.
	 */
	public String getDefaultProvider() {
		return defaultProvider;
	}

	/**
	 * @return Returns the requiredDataType.
	 */
	public Class getRequiredDataType() {
		return requiredDataType;
	}
	
	/**
	 * @return Returns the dataBlockName.
	 */
	public String getDataBlockName() {
		return dataBlockName;
	}

}
