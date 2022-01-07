package backupmanager.action;

import backupmanager.FileLocation;
import backupmanager.settings.ActionSettings;;

public class CopySettings extends ActionSettings {

	public static final long serialVersionUID = 1L;

	public FileLocation destLocation;
	
	public CopySettings(String className) {
		super(className);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public String toString() {
		if (destLocation == null) {
			return "unknown";
		}
		else {
			return destLocation.path;
		}
	}
	

}
