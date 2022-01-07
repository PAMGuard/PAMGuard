package fileOfflineData;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;

public class OfflineFilesDialog extends PamDialog {

	private static OfflineFilesDialog singleInstance;
	
	private OfflineFileControl sensorControl;
	
	private OfflineFileParams d3Params;
	
	private SelectFolder selectFolder;
	
	private OfflineFilesDialog(Window parentFrame, String dataName) {
		super(parentFrame, dataName, false);
		selectFolder = new SelectFolder(dataName + " Folder", 50, true);
//		selectFolder.setShowSubFolderOption(true);
		((JPanel) selectFolder.getFolderPanel()).setBorder(null);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Data Location"));
		mainPanel.add(BorderLayout.NORTH, selectFolder.getFolderPanel());
		
		setDialogComponent(mainPanel);
//		setDialogComponent(selectFolder.getFolderPanel());
//		setResizable(true);
	}
	
	public static OfflineFileParams showDialog(Window parentFrame, OfflineFileControl sensorControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new OfflineFilesDialog(parentFrame, sensorControl.getUnitName());
		}
		
		singleInstance.d3Params = sensorControl.fileParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.d3Params;
	}

	private void setParams() {
		selectFolder.setFolderName(d3Params.offlineFolder);
		selectFolder.setIncludeSubFolders(d3Params.subFolders);
	}

	@Override
	public boolean getParams() {
		d3Params.offlineFolder = selectFolder.getFolderName(true);
		d3Params.subFolders = selectFolder.isIncludeSubFolders();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		d3Params = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
