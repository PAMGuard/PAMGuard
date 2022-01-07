package backupmanager.swing;

import java.awt.Window;

import javax.swing.JComponent;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import backupmanager.FileLocation;
import backupmanager.action.CopyFile;
import backupmanager.swing.FileLocationComponent.LocationType;

public class CopyFileDialogPanel implements PamDialogPanel {

	private CopyFile copyFile; 
	
	private Window owner;

	private FileLocationComponent fileLocationComponent;
	
	public CopyFileDialogPanel(CopyFile copyFile, Window owner) {
		this.copyFile = copyFile;
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
		fileLocationComponent = new FileLocationComponent(owner, LocationType.DESTINATION, false, false);
		return fileLocationComponent;
	}

	@Override
	public void setParams() {
		FileLocation destLoc = copyFile.getCopySettings().destLocation;
		fileLocationComponent.setParams(destLoc);
	}

	@Override
	public boolean getParams() {
		FileLocation destLoc = copyFile.getCopySettings().destLocation;
		FileLocation newLoc = fileLocationComponent.getParams(destLoc);
		if (newLoc != null) {
			copyFile.getCopySettings().destLocation = newLoc;
			return true;
		}
		return PamDialog.showWarning(owner, copyFile.getName(), "Destination location is not defined");
	}
}
