package annotation.userforms.species;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.userforms.UserFormAnnotation;
import annotation.userforms.UserFormAnnotationType;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesManagerObserver;

public class FormsAnnotationSpeciesManager extends DataBlockSpeciesManager implements PamSettings {

	private UserFormAnnotationType userFormAnnotationType;
	
	private FormsSpeciesSettings speciesSettings = new FormsSpeciesSettings();
	
	private FormsAnnotationSpeciesCodes speciesCodes;

	public FormsAnnotationSpeciesManager(UserFormAnnotationType userFormAnnotationType, PamDataBlock dataBlock) {
		super(dataBlock);
		this.userFormAnnotationType = userFormAnnotationType;
		this.speciesCodes = new FormsAnnotationSpeciesCodes();
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return speciesCodes;
	}

	@Override
	public String getSpeciesCode(PamDataUnit dataUnit) {
		UserFormAnnotation annot = (UserFormAnnotation) dataUnit.findDataAnnotation(userFormAnnotationType.getAnnotationClass());
		if (annot == null) {
			return null;
		}
		// now find the control with the right name. 
		Object data[] = annot.getLoggerFormData();
		int ind = getLoggerItemIndex();
		if (ind < 0 || ind > data.length) {
			return null;
		}
		Object dat = data[ind];
		if (dat == null) {
			return null;
		}
		return dat.toString();
	}

	private int getLoggerItemIndex() {
		int ctrlInd = userFormAnnotationType.getFormDescription().findInputControlByName(speciesSettings.selectedControl);
		return ctrlInd;
	}

	public class FormsAnnotationSpeciesCodes extends DataBlockSpeciesCodes {

		public FormsAnnotationSpeciesCodes() {
			super(null);
			// TODO Auto-generated constructor stub
		}

		@Override
		public ArrayList<String> getSpeciesNames() {
			int ctrlInd = userFormAnnotationType.getFormDescription().findInputControlByName(speciesSettings.selectedControl);
			ArrayList<InputControlDescription> ipControls = userFormAnnotationType.getFormDescription().getInputControlDescriptions();
			if (ctrlInd < 0 || ctrlInd >= ipControls.size()) {
				return null;
			}
			InputControlDescription ipCD = ipControls.get(ctrlInd);
			if (ipCD.getEType() != ControlTypes.LOOKUP) {
				return null;
			}
			CdLookup lutCtrl = (CdLookup) ipCD;
			LookupList lutList = lutCtrl.getLookupList();
			Vector<LookupItem> lutItems = lutList.getList();
			ArrayList<String> names = new ArrayList<>();
			for (int i = 0; i < lutItems.size(); i++) {
				names.add(lutItems.get(i).getCode());
			}
			return names;
		}
	}


	@Override
	public PamDialogPanel getDialogPanel(SpeciesManagerObserver speciesManagerObserver) {
		return new FormsSpeciesOptionsPanel(this, speciesManagerObserver);
	}

	@Override
	public String getUnitName() {
		return userFormAnnotationType.getTargetDataBlock().getLongDataName();
	}

	@Override
	public String getUnitType() {
		return "Forms Annotion Species Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return speciesSettings;
	}

	@Override
	public long getSettingsVersion() {
		return FormsSpeciesSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		speciesSettings = (FormsSpeciesSettings) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the userFormAnnotationType
	 */
	public UserFormAnnotationType getUserFormAnnotationType() {
		return userFormAnnotationType;
	}

	/**
	 * @return the speciesSettings
	 */
	public FormsSpeciesSettings getSpeciesSettings() {
		return speciesSettings;
	}

}
