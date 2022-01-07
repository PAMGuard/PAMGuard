package backupmanager.swing;

import java.awt.Window;

import javax.swing.JComponent;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import backupmanager.FileLocation;
import backupmanager.stream.FileBackupStream;
import backupmanager.swing.FileLocationComponent.LocationType;

public class FileStreamDialogPanel implements PamDialogPanel {

	private FileBackupStream fileBackupStream;
	
	private FileLocationComponent fileLocationComponent;

	private Window owner;
	
	public FileStreamDialogPanel(FileBackupStream fileBackupStream, Window owner) {
		super();
		this.fileBackupStream = fileBackupStream;
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
		FileLocation sourceLoc = fileBackupStream.getSourceLocation();
		fileLocationComponent = new FileLocationComponent(owner, LocationType.SOURCE, sourceLoc.canEditPath, sourceLoc.canEditMask);
		return fileLocationComponent;
	}

	@Override
	public void setParams() {
		FileLocation sourceLoc = fileBackupStream.getSourceLocation();
		fileLocationComponent.setParams(sourceLoc);
	}

	@Override
	public boolean getParams() {
		FileLocation sourceLoc = fileBackupStream.getSourceLocation();
		FileLocation newLoc = fileLocationComponent.getParams(sourceLoc);
		if (newLoc != null) {
			fileBackupStream.setSourceLocation(newLoc);
			return true;
		}
		return PamDialog.showWarning(null, fileBackupStream.getName(), "Souce location is not defined");
	}

}
