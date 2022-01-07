package dataMap.filemaps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.DaqSystem;
import Acquisition.FolderInputParameters;
import Acquisition.FolderInputSystem;
import Acquisition.filedate.FileDate;
import Acquisition.filedate.FileDateDialog;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import PamController.OfflineFileDataStore;
import PamUtils.SelectFolder;
import PamView.PamGui;
import PamView.dialog.PamDialog;

/**
 * An extra panel that appears in the DAQ control when offline
 * so that user can point the DAQ at a set of wav of aif files
 * to use with the offline viewer. 
 * @author Doug Gillespie
 *
 */
public class OfflineFileDialogPanel {

	private OfflineFileDataStore offlineRawDataStore;
	
	private PamDialog parentDialog;

	private JPanel outerPanel;
	
	private JCheckBox enableOffline;
	
	private SelectFolder storageLocation;

	private JButton timeZone;
	
	/**
	 * @param offlineFileDataSource
	 * @param parentDialog
	 */
	public OfflineFileDialogPanel(OfflineFileDataStore offlineFileDataSource,
			PamDialog parentDialog) {
		super();
		this.offlineRawDataStore = offlineFileDataSource;
		this.parentDialog = parentDialog;

		timeZone = new JButton("Time Zone");
		enableOffline = new JCheckBox("Use offline files");
		
		JPanel mainPanel;
		mainPanel = new JPanel(new BorderLayout());
		storageLocation = new SelectFolder("", 30, true);
		((JPanel) storageLocation.getFolderPanel()).setBorder(null);
		JPanel nPanel = new JPanel(new BorderLayout());
		nPanel.add(BorderLayout.WEST, enableOffline);
		nPanel.add(BorderLayout.EAST, timeZone);
		mainPanel.add(BorderLayout.NORTH, nPanel);
		enableOffline.addActionListener(new EnableButton());
		JPanel centPanel = new JPanel(new BorderLayout());
		String folderName = findOfflineFolderName(offlineRawDataStore.getOfflineFileServer().getOfflineFileParameters());
		storageLocation.setFolderName(folderName);
		centPanel.add(BorderLayout.NORTH, storageLocation.getFolderPanel());
		mainPanel.add(BorderLayout.CENTER, centPanel);
		outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(BorderLayout.NORTH, mainPanel);
		outerPanel.setBorder(new TitledBorder("Offline file store"));
//		JPanel swPanel = new JPanel(new BorderLayout());
//		mainPanel.add(BorderLayout.SOUTH, swPanel);
//		swPanel.add(BorderLayout.WEST, timeZone);
		
		timeZone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeZoneButton();
			}
		});
	}
	
	protected void timeZoneButton() {
		// need to somehow reinstate this.
		OfflineFileServer offlineFileServer = offlineRawDataStore.getOfflineFileServer();
		if (offlineFileServer instanceof OfflineWavFileServer) {
			OfflineWavFileServer offlineWavFileServer = (OfflineWavFileServer) offlineFileServer;
			FileDate fileDate = offlineWavFileServer.getFileDate();
			fileDate.doSettings(parentDialog);
		}
	}

	public Component getComponent() {
		return outerPanel;
	}
	
	private class EnableButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	private void enableControls() {
		storageLocation.setEnabled(enableOffline.isSelected());
		timeZone.setEnabled(enableOffline.isSelected());
	}

	public void setParams() {
		OfflineFileParameters p = offlineRawDataStore.getOfflineFileServer().getOfflineFileParameters();
		enableOffline.setSelected(p.enable);
		if (p.folderName != null) {
			storageLocation.setFolderName(p.folderName);
			storageLocation.setIncludeSubFolders(p.includeSubFolders);
		}
//		// otherwise take the folder name from the main daq parameters.
//		if (AcquisitionControl.class.isAssignableFrom(offlineRawDataStore.getClass())) {
//			AcquisitionControl daqControl = (AcquisitionControl) offlineRawDataStore;
//			DaqSystem daqSystem = daqControl.getDaqProcess().getRunningSystem();
		// doesn't work because Daq sysstem is null offline !
//			if (FolderInputSystem.class.isAssignableFrom(daqSystem.getClass())) {
//				FolderInputSystem fis = (FolderInputSystem) daqSystem;
//				FolderInputParameters fip = fis.getFolderInputParameters();
//				storageLocation.setFolderName(fip.getMostRecentFile());
//				storageLocation.setIncludeSubFolders(fip.subFolders);
//			}
//		}
		enableControls();
	}
	
	public String findOfflineFolderName(OfflineFileParameters p) {
		if (p.folderName != null) {
			return p.folderName;
		}
		// otherwise take the folder name from the main daq parameters.
		if (AcquisitionControl.class.isAssignableFrom(offlineRawDataStore.getClass())) {
			AcquisitionControl daqControl = (AcquisitionControl) offlineRawDataStore;
//			DaqSystem daqSystem = daqControl.getDaqProcess().getRunningSystem();
			DaqSystem daqSystem = daqControl.findDaqSystem(null);
			if (daqSystem == null) {
				return null;
			}
			if (FolderInputSystem.class.isAssignableFrom(daqSystem.getClass())) {
				FolderInputSystem fis = (FolderInputSystem) daqSystem;
				FolderInputParameters fip = fis.getFolderInputParameters();
				return fip.getMostRecentFile();
			}
		}
		return null;
	}
	
	private boolean checkFolder(String file) {
		if (file == null) {
			return false;
		}
		File f = new File(file);
		if (f.exists() == false) {
			return false;
		}
		return true;
	}
	
	public OfflineFileParameters getParams() {
		OfflineFileParameters p = new OfflineFileParameters();
		p.enable = enableOffline.isSelected();
		p.includeSubFolders = storageLocation.isIncludeSubFolders();
		p.folderName = storageLocation.getFolderName(false);
		if (checkFolder(p.folderName) == false && p.enable) {
			if (p.folderName == null) {
				parentDialog.showWarning("Error in file store", "No storage folder selected");
				return null;
			}
			else {
				String err = String.format("The folder %s does not exist", p.folderName);
				parentDialog.showWarning("Error in file store", err);
				return null;
			}
		}
		return p;
	}
}
