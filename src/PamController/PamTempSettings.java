package PamController;

import java.io.Serializable;

public class PamTempSettings implements PamSettings {

	private PamSettings settingsParent;
	private Serializable tempObject;

	public PamTempSettings(PamSettings settingsParent, Serializable tempObject) {
		this.settingsParent = settingsParent;
		this.tempObject = tempObject;
	}

	@Override
	public String getUnitName() {
		return settingsParent.getUnitName();
	}

	@Override
	public String getUnitType() {
		return settingsParent.getUnitType();
	}

	@Override
	public Serializable getSettingsReference() {
		return tempObject;
	}

	@Override
	public long getSettingsVersion() {
		return settingsParent.getSettingsVersion();
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		return false;
//		return settingsParent.restoreSettings(pamControlledUnitSettings);
	}

}
