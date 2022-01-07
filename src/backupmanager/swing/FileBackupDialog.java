package backupmanager.swing;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import backupmanager.FileLocation;
import backupmanager.FileLocationParams;
import backupmanager.swing.FileLocationComponent.LocationType;

public class FileBackupDialog extends PamDialog {

	private FileLocationParams flParams;
	
	private FileLocationComponent source, destination;

	private FileBackupDialog(Window parentFrame, String title, boolean allowSourcePath, boolean allowSourceMask) {
		super(parentFrame, title, false);
		source = new FileLocationComponent(parentFrame, LocationType.SOURCE, allowSourcePath, allowSourceMask);
		destination = new FileLocationComponent(parentFrame, LocationType.DESTINATION, false, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(source.getComponent());
		mainPanel.add(destination.getComponent());
		
		setResizable(true);
		setDialogComponent(mainPanel);
	}
	
	public static FileLocationParams showDialog(Window window, FileLocationParams flParams, String title, boolean allowSourcePath, boolean allowSourceMask) {
		FileBackupDialog fileBackupDialog = new FileBackupDialog(window, title, allowSourcePath, allowSourceMask);
		fileBackupDialog.setParams(flParams);
		fileBackupDialog.setVisible(true);
		return fileBackupDialog.flParams;
	}

	private void setParams(FileLocationParams flParams) {
		this.flParams = flParams;
		source.setParams(flParams.getSourceLocation());
		destination.setParams(flParams.getDestLocation());
	}

	@Override
	public boolean getParams() {
		FileLocation sl = source.getParams(flParams.getSourceLocation());
		FileLocation dl = destination.getParams(flParams.getDestLocation());
		if (sl == null) {
			return showWarning("No source location specified");
		}
		if (dl == null) {
			return showWarning("No destination specified");
		}
		flParams.setSourceLocation(sl);
		flParams.setDestLocation(dl);
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
