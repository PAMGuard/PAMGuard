package loggerForms;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Controls the settings for a single Logger form description. <p>
 * Doesn't handle the form design in any way, but things like the 
 * divider location as set by the user during use. <p>
 * (OK - only the divider location at the moment !).
 * @author Doug Gillespie
 *
 */
public class FormSettingsControl implements PamSettings {

	private FormDescription formDescription;
	
	private String udfName;
	
	private FormSettings formSettings = new FormSettings();

	public FormSettingsControl(FormDescription formDescription, String udfName) {
		super();
		this.formDescription = formDescription;
		this.udfName = udfName;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		formDescription.getFormSettingsData(formSettings);
		
		return formSettings;
	}

	@Override
	public long getSettingsVersion() {
		return FormSettings.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return udfName;
	}

	@Override
	public String getUnitType() {
		return "Logger Form Description Data";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.formSettings = ((FormSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the formSettings
	 */
	public FormSettings getFormSettings() {
		return formSettings;
	}
	
}
