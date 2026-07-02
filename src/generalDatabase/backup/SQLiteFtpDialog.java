package generalDatabase.backup;

import java.awt.Window;

import javax.swing.JComponent;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import backupmanager.FileLocation;
import backupmanager.swing.FileLocationComponent;
import backupmanager.swing.FileLocationComponent.LocationType;

public class SQLiteFtpDialog implements PamDialogPanel{

	private SQLiteSafeFTPBackup ftpFile; 
	
	private Window owner;

	private FileLocationComponent remoteLocationComponent;
	
	public SQLiteFtpDialog(SQLiteSafeFTPBackup ftpFile, Window owner) {
		this.ftpFile = ftpFile;
		this.owner = owner;
	}
	
	@Override
	public JComponent getDialogComponent() {
		if (remoteLocationComponent == null) {
			remoteLocationComponent = createComponent();
		}
		return remoteLocationComponent.getComponent();
	}
	

	private FileLocationComponent createComponent() {
		remoteLocationComponent = new FileLocationComponent(owner, LocationType.REMOTE_DESTINATION, true, false);
		return remoteLocationComponent;
	}

	@Override
	public void setParams() {
		FileLocation destLoc = ftpFile.getFtpSettings().destLocation;
		remoteLocationComponent.setParams(destLoc);
	}

	@Override
	public boolean getParams() {
		FileLocation destLoc = ftpFile.getFtpSettings().destLocation;
		FileLocation newLoc = remoteLocationComponent.getParams(destLoc);
		if (newLoc != null) {
			ftpFile.getFtpSettings().destLocation = newLoc;
			return true;
		}
		return PamDialog.showWarning(owner, ftpFile.getName(), "Destination location is not defined");
	}
}
