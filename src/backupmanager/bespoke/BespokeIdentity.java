package backupmanager.bespoke;

import java.io.Serializable;

import PamController.SettingsNameProvider;

public class BespokeIdentity implements Serializable, SettingsNameProvider {

	 static final long serialVersionUID = 1L;

	 private String name;

	public BespokeIdentity(String name) {
		super();
		this.name = name;
	}

	@Override
	public String getUnitName() {
		return "Backup " + name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	 
}
