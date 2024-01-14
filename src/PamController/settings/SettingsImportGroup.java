package PamController.settings;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.UsedModuleInfo;
import PamModel.PamModuleInfo;

/**
 * Slightly more than a standard settings. Since there is no real association 
 * between settings and modules, the settings who's parent class is a PamControlledUnit
 * will be listed, then any other settings which have the same name will get lumped with them. 
 * @author Doug
 *
 */
public class SettingsImportGroup {

	private PamControlledUnitSettings mainSettings;
	
	private ArrayList<PamControlledUnitSettings> subSettings = new ArrayList<>();

	private UsedModuleInfo usedModuleInfo;

	private ArrayList<ImportChoice> importChoices;
	
	private ImportChoice importChoice;


	public SettingsImportGroup(UsedModuleInfo moduleInfo) {
		super();
		this.usedModuleInfo = moduleInfo;
	}
	
	/**
	 * Constructor takes the main settings
	 * @param mainSettings
	 * @param moduleInfo 
	 */
	public SettingsImportGroup(PamControlledUnitSettings mainSettings, UsedModuleInfo moduleInfo) {
		this.mainSettings = mainSettings;
		this.usedModuleInfo = moduleInfo;
	}

	/**
	 * add sub settings which seem somehow related to the main setting. 
	 * @param subSetting
	 */
	public void addSubSettings(PamControlledUnitSettings subSetting) {
		subSettings.add(subSetting);
	}

	/**
	 * @return the mainSettings
	 */
	public PamControlledUnitSettings getMainSettings() {
		return mainSettings;
	}

	/**
	 * @return the subSettings
	 */
	public ArrayList<PamControlledUnitSettings> getSubSettings() {
		return subSettings;
	}

	/**
	 * @return the importChoices
	 */
	public ArrayList<ImportChoice> getImportChoices() {
		if (importChoices == null) {
			createImportchoices();
		}
		return importChoices;
	}

	private void createImportchoices() {
		importChoices = new ArrayList<>();
		importChoices.add(importChoice = new ImportChoice(ImportChoice.DONT_IMPORT, null));
		Class ownerClass = null;
		try {
			ownerClass = Class.forName(usedModuleInfo.className);
		} catch (ClassNotFoundException e) {

			System.out.println("Unknown class in loaded settings: " + usedModuleInfo.className);
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		ArrayList<PamControlledUnit> existingModules = 
				PamController.getInstance().findControlledUnits(ownerClass);
		int maxnumber = 1; // this will only ever be used by the Array Manager which doesn't have a moduleInfo. 
		PamModuleInfo pamModuleInfo = getPamModuleInfo();
		if (pamModuleInfo != null) {
			maxnumber = pamModuleInfo.getMaxNumber();
		}
		if (existingModules != null) {
			for (int i = 0; i < existingModules.size(); i++) {
				importChoices.add(new ImportChoice(ImportChoice.IMPORT_REPLACE, existingModules.get(i).getUnitName()));
			}
		}
		if (maxnumber == 0 || maxnumber > existingModules.size()) {
			importChoices.add(new ImportChoice(ImportChoice.IMPORT_NEW, null));
		}
	}

	/**
	 * @return the importChoice
	 */
	public ImportChoice getImportChoice() {
		if (importChoice == null) {
			createImportchoices();
		}
		return importChoice;
	}

	/**
	 * @param importChoice the importChoice to set
	 */
	public void setImportChoice(ImportChoice importChoice) {
		this.importChoice = importChoice;
	}

	/**
	 * This is the information from an existing module, which may not
	 * have the full class name, but does have the type and name of the 
	 * module being imported. 
	 * @return the moduleInfo
	 */
	public UsedModuleInfo getUsedModuleInfo() {
		return usedModuleInfo;
	}
	
	/**
	 * this is the module information held in the PamModel which 
	 * is used to create a module. 
	 * @return
	 */
	public PamModuleInfo getPamModuleInfo() {
		return PamModuleInfo.findModuleInfo(usedModuleInfo.className);
	}

	/**
	 * @param mainSettings the mainSettings to set
	 */
	public void setMainSettings(PamControlledUnitSettings mainSettings) {
		this.mainSettings = mainSettings;
	}
	
	
}
