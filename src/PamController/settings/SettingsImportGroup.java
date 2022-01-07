package PamController.settings;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
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

	private PamModuleInfo moduleInfo;

	private ArrayList<ImportChoice> importChoices;
	
	private ImportChoice importChoice;
	/**
	 * Constructor takes the main settings
	 * @param mainSettings
	 * @param moduleInfo 
	 */
	public SettingsImportGroup(PamControlledUnitSettings mainSettings, PamModuleInfo moduleInfo) {
		this.mainSettings = mainSettings;
		this.moduleInfo = moduleInfo;
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
			ownerClass = Class.forName(mainSettings.getOwnerClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<PamControlledUnit> existingModules = 
				PamController.getInstance().findControlledUnits(ownerClass);
		int maxnumber = 1; // this will only ever be used by the Array Manager which doesn't have a moduleInfo. 
		if (this.moduleInfo != null) {
			maxnumber = moduleInfo.getMaxNumber();
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
	 * @return the moduleInfo
	 */
	public PamModuleInfo getModuleInfo() {
		return moduleInfo;
	}
	
	
}
