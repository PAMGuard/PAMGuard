package generalDatabase.backup;

import backupmanager.FileLocation;
import backupmanager.settings.ActionSettings;

public class SQLiteFtpSettings extends ActionSettings{
	
	public static final long serialVersionUID = 1L;

	public FileLocation destLocation;
	
	public SQLiteFtpSettings(String className) {
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
