package backupmanager.swing;

import java.awt.Window;

import javax.swing.JComponent;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import backupmanager.FileLocation;
import backupmanager.action.FTPFile;
import backupmanager.swing.FileLocationComponent.LocationType;

public class FtpFileDialogPanel implements PamDialogPanel{

	private FTPFile ftpFile; 
	
	private Window owner;

	private FileLocationComponent fileLocationComponent;
	
	public FtpFileDialogPanel(FTPFile ftpFile, Window owner) {
		this.ftpFile = ftpFile;
		this.owner = owner;
	}
	@Override
	public JComponent getDialogComponent() {
		if (fileLocationComponent == null) {
			fileLocationComponent = createComponent();
		}
		return fileLocationComponent.getComponent();
	}
	

	private FileLocationComponent createComponent() {
		fileLocationComponent = new FileLocationComponent(owner, LocationType.REMOTE_DESTINATION, true, false);
		return fileLocationComponent;
	}

	@Override
	public void setParams() {
		FileLocation destLoc = ftpFile.getFtpSettings().destLocation;
		fileLocationComponent.setParams(destLoc);
	}

	@Override
	public boolean getParams() {
		FileLocation destLoc = ftpFile.getFtpSettings().destLocation;
		FileLocation newLoc = fileLocationComponent.getParams(destLoc);
		if (newLoc != null) {
			ftpFile.getFtpSettings().destLocation = newLoc;
			return true;
		}
		return PamDialog.showWarning(owner, ftpFile.getName(), "Destination location is not defined");
	}

}
