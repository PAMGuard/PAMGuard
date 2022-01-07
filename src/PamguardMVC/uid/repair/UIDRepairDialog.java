package PamguardMVC.uid.repair;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.uid.UIDStatusReport;
import binaryFileStorage.BinaryStore;
import generalDatabase.DBControlUnit;

public class UIDRepairDialog extends PamDialog implements UIDMessageInterface {

	private PamController pamController;
	
	private JTextField currentBinaryFolder;
	private SelectFolder newBinaryFolder;
	private JTextField currentDatabase;
	private JTextField textProgress;
	private JProgressBar totalProgress;
	private JProgressBar dataTypeProgress;
	private JProgressBar fileProgress;
	private JCheckBox doPartial;

	private UIDRepairFunctions uidRepairFunctions;

	private UIDStatusReport totalReport;

	public UIDRepairDialog(Window parentFrame, PamController pamController, UIDRepairFunctions uidRepairFunctions, UIDStatusReport totalReport) {
		super(parentFrame, "UID Repair tool", false);
		this.pamController = pamController;
		this.uidRepairFunctions = uidRepairFunctions;
		this.totalReport = totalReport;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		int TEXTFIELDLENGTH = 50;
		
		JPanel binaryPanel = new JPanel(new GridBagLayout());
		binaryPanel.setBorder(new TitledBorder("Binary Data Storage"));
		GridBagConstraints c = new PamGridBagContraints();
		binaryPanel.add(new JLabel("Current binary storage folder"), c);
		c.gridy++;
		binaryPanel.add(currentBinaryFolder = new JTextField(TEXTFIELDLENGTH), c);
		currentBinaryFolder.setEditable(false);
		c.gridy++;
		JPanel bitPanel = new JPanel(new BorderLayout());
		bitPanel.add(BorderLayout.WEST, new JLabel("New binary storage folder"));
		bitPanel.add(BorderLayout.EAST, doPartial = new JCheckBox("Add incremental UID's in folder"));
		binaryPanel.add(bitPanel, c);
		c.gridy++;
		newBinaryFolder = new SelectFolder("New Binary Folder", TEXTFIELDLENGTH, false);
		binaryPanel.add(newBinaryFolder.getFolderPanel(), c);
		mainPanel.add(binaryPanel);
		
		JPanel dbPanel = new JPanel(new GridBagLayout());
		dbPanel.setBorder(new TitledBorder("Database storage"));
		c = new PamGridBagContraints();
		dbPanel.add(new JLabel("Current database"), c);
		c.gridy++;
		dbPanel.add(currentDatabase = new JTextField(TEXTFIELDLENGTH), c);
		currentDatabase.setEditable(false);
		mainPanel.add(dbPanel);
		
		JPanel progPanel = new JPanel(new GridBagLayout());
		progPanel.setBorder(new TitledBorder("Progress"));
		c = new PamGridBagContraints();
		c.gridwidth = 2;
		progPanel.add(textProgress = new JTextField(TEXTFIELDLENGTH), c);
		textProgress.setEditable(false);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		progPanel.add(new JLabel("Overall ", JLabel.RIGHT), c);
		c.gridx++;
		progPanel.add(totalProgress = new JProgressBar(), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		progPanel.add(new JLabel("Stream ", JLabel.RIGHT), c);
		c.gridx++;
		progPanel.add(dataTypeProgress = new JProgressBar(), c);
		c.gridy++;
		c.gridx = 0;
		progPanel.add(new JLabel("File ", JLabel.RIGHT), c);
		c.gridx++;
		progPanel.add(fileProgress = new JProgressBar(), c);
		mainPanel.add(progPanel);
		
		setHelpPoint("overview.uid.docs.uid");
		
		setParams();
		setDialogComponent(mainPanel);
		
		doPartial.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doParticalClicked();
			}
		});
		doPartial.setToolTipText("Select this function if you've been adding new data that doesn't have UID's to a folder containing data that already has UID's");
		
	}

	protected void doParticalClicked() {
		if (doPartial.isSelected()) {
			newBinaryFolder.setFolderName(currentBinaryFolder.getText());
			newBinaryFolder.setEnabled(false);
		}
		else {
			newBinaryFolder.setFolderName(getNewBinName());
			newBinaryFolder.setEnabled(true);
		}
	}
	
	private String getNewBinName() {
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore == null) {
			return null;
		}
		else {
			return binStore.getBinaryStoreSettings().getStoreLocation()+"_WithUID";
		}
	}

	private void setParams() {
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore == null) {
			currentBinaryFolder.setText("Binary store not in use");
			newBinaryFolder.setEnabled(false);
			newBinaryFolder.setFolderName(null);
		}
		else {
			currentBinaryFolder.setText(binStore.getBinaryStoreSettings().getStoreLocation());
			newBinaryFolder.setEnabled(true);
			newBinaryFolder.setFolderName(getNewBinName());
		}
		
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			currentDatabase.setText("PAMGuard database not in use");
		}
		else {
			currentDatabase.setText(dbControl.getDatabaseName());
		}
		
		boolean hasSomething = dbControl != null || binStore != null;
		if (hasSomething) {
			textProgress.setText("Click OK to start data conversion");
			getOkButton().setEnabled(true);
		}
		else {
			textProgress.setText("No data available");
			getOkButton().setEnabled(false);
		}
		
		if (totalReport.getUidStatus() == UIDStatusReport.UID_PARTIAL) {
			doPartial.setSelected(true);
		}
		else {
			doPartial.setSelected(false);
			doPartial.setEnabled(false);
		}
		doParticalClicked();
		
	}

	private UIDRepairParams getRepairParams() {
		UIDRepairParams uidRepairParams = new UIDRepairParams();

		uidRepairParams.doPartial = doPartial.isSelected();
		
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore != null) {
			String outFolder = newBinaryFolder.getFolderName(true);
			if (outFolder == null) return null;
			File outFo = new File(outFolder);
			if (outFo.exists() == false) {
				
				return null;
			}
			else {
				String inFolder = currentBinaryFolder.getText();
				if (inFolder.equalsIgnoreCase(outFolder) && !uidRepairParams.doPartial) {
					showWarning("The destination folder for converted binary data cannot be the same as the current binary data folder");
					return null;
				}
				uidRepairParams.newBinaryFolder = outFolder;
			}
		}
		return uidRepairParams;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#okButtonPressed()
	 */
	@Override
	protected void okButtonPressed() {
		UIDRepairParams uidParams = getRepairParams();
		if (uidParams == null) {
			return;
		}
		getOkButton().setEnabled(false);
		getCancelButton().setEnabled(false);
		newBinaryFolder.setEnabled(false);
		uidRepairFunctions.startUIDRepairJob(uidParams, this);
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This should always get called back in the AWT thread, so can update the 
	 * Dialog display. 
	 * @param msg
	 */
	@Override
	public void newMessage(UIDRepairMessage msg) {
		if(msg == null) {
			getCancelButton().setEnabled(true);
			getCancelButton().setText("Close");
			getOkButton().setVisible(false);
			textProgress.setText("Copy complete");
			return;
		}
		switch (msg.infoType) {
		case UIDRepairMessage.TYPE_BLOCKPROGRESS:
			if (msg.binFileIndex > 0) {
				dataTypeProgress.setValue(msg.binFileIndex*100/msg.nBinFiles);
				String stmsg = String.format("%s file %d of %d", msg.currentDataBlock.getDataName(), msg.binFileIndex, msg.nBinFiles);
				textProgress.setText(stmsg);
			}
			else {
				textProgress.setText(msg.currentDataBlock.getDataName());
			}
			break;
		case UIDRepairMessage.TYPE_TOTALPROGRESS:
			totalProgress.setValue(msg.percentComplete);
			break;
		case UIDRepairMessage.TYPE_FILEPROGRESS:
			fileProgress.setValue(msg.percentComplete);
			break;
		}
		
	}

	@Override
	public void repairComplete(boolean repairOK) {
		// TODO Auto-generated method stub
		
	}

}
