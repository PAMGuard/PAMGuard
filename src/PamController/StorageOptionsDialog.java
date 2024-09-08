package PamController;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
import generalDatabase.DBControlUnit;

public class StorageOptionsDialog extends PamDialog {

	private static final long serialVersionUID = 1L;
	
	private static StorageOptionsDialog singleInstance;

	private StorageParameters storageParameters;
	
	private JPanel mainPanel;

	private ArrayList<PamDataBlock> dataBlocks;

	private ArrayList<PamDataBlock> usedDataBlocks = new ArrayList<PamDataBlock>();
	
	private ArrayList<JCheckBox> dbCheckBoxes = new ArrayList<JCheckBox>();
	private ArrayList<JCheckBox> bsCheckBoxes = new ArrayList<JCheckBox>();

	public StorageOptionsDialog(Window parentFrame) {
		super(parentFrame, "Storage Options", false);
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Data Storage"));
		mainPanel.setLayout(new GridBagLayout());
		
		setDialogComponent(mainPanel);
	}

	public static StorageParameters showDialog(JFrame parentFrame,
			StorageParameters storageParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new StorageOptionsDialog(parentFrame);
		}
		singleInstance.storageParameters = storageParameters.clone();
		singleInstance.createControls();
		singleInstance.setVisible(true);
		return singleInstance.storageParameters;
		
	}

	private void createControls() {
		mainPanel.removeAll();
		PamController pamController = PamController.getInstance();
		BinaryStore binaryStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		DBControlUnit database = DBControlUnit.findDatabaseControl();
		dataBlocks = pamController.getDataBlocks();
		PamDataBlock aDataBlock;
		boolean hasDatabase, hasBinaryStore;
		usedDataBlocks.clear();
		dbCheckBoxes.clear();
		bsCheckBoxes.clear();
		
		JLabel l;
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		addComponent(mainPanel, l = new JLabel("  Binary Store  "), c);
		if (binaryStore == null) {
			l.setToolTipText("Binary Storage module is not loaded");
		}
		else {
			l.setToolTipText("Binary Storage is more efficient than the database for many types of detection data");
		}
		c.gridx++;
		addComponent(mainPanel, l = new JLabel("  Database  ", SwingConstants.CENTER), c);
		if (binaryStore == null) {
			l.setToolTipText("Database module is not loaded");
		}
		else {
			l.setToolTipText("Database Storage can be slow for high volume data !");
		}
		JCheckBox cb;
		PamControlledUnit pcu;
		
		for (int i = 0; i < dataBlocks.size(); i++) {
			aDataBlock = dataBlocks.get(i);
			hasBinaryStore = aDataBlock.getBinaryDataSource() != null;
			if (hasBinaryStore) {
				hasBinaryStore = aDataBlock.getBinaryDataSource().isDoBinaryStore();
			}
			hasDatabase = aDataBlock.getLogging() != null;
			if (!hasBinaryStore && !hasDatabase) {
				continue;
			}
			usedDataBlocks.add(aDataBlock);
			c.gridx = 0;
			c.gridy++;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(mainPanel, l = new JLabel(aDataBlock.getDataName(), SwingConstants.RIGHT), c);
			pcu = aDataBlock.getParentProcess().getPamControlledUnit();
			l.setToolTipText("Module: " + pcu.getUnitName());

			c.fill = GridBagConstraints.NONE;
			
			c.gridx++;
			addComponent(mainPanel, cb = new JCheckBox(), c);
//			cb.setVisible(hasBinaryStore);
			cb.setEnabled(hasBinaryStore && binaryStore != null);
			if (!hasBinaryStore) {
				cb.setToolTipText("Binary storage is not available for this data block");
			}
			if (hasBinaryStore) {
				cb.setSelected(storageParameters.isStoreBinary(aDataBlock, true));
			}
			bsCheckBoxes.add(cb);
			
			c.gridx++;
			addComponent(mainPanel, cb = new JCheckBox(), c);
//			cb.setVisible(hasDatabase);
			cb.setEnabled(hasDatabase && database != null);
			if (!hasDatabase) {
				cb.setToolTipText("Database storage is not available for this data block");
			}
			if (hasDatabase) {
				boolean shouldUseDatabase = !hasBinaryStore & aDataBlock.getShouldLog();
				cb.setSelected(storageParameters.isStoreDatabase(aDataBlock, shouldUseDatabase));
			}
			dbCheckBoxes.add(cb);
		}
		
		pack();
	}

	@Override
	public void cancelButtonPressed() {
		storageParameters = null;
	}

	@Override
	public boolean getParams() {
		boolean storeDatabase, storeBinary;
		int errors = 0;
		for (int i = 0; i < usedDataBlocks.size(); i++) {
			storeDatabase = dbCheckBoxes.get(i).isSelected();
			storeBinary = bsCheckBoxes.get(i).isSelected();
			if (!storeDatabase && !storeBinary) {
				errors++;
			}
			storageParameters.setStorageOptions(usedDataBlocks.get(i), storeDatabase, storeBinary);
		}
		if (errors > 0) {
			int ans = JOptionPane.showOptionDialog(getOwner(),  
					"At least one data stream is not connected to any storage type", "Storage Options",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, null);
			return (ans == JOptionPane.OK_OPTION);
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
