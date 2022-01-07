package binaryFileStorage;

import java.awt.Window;

import PamView.dialog.PamDialog;

public class BinaryStorageDialog extends PamDialog {

	private static BinaryStorageDialog singleInstance;

	private BinaryStoreSettings binaryStoreSettings;
	
	private BinaryStorageDialogPanel binaryStorageDialogPanel;

	private BinaryStore binaryStore;

	private BinaryStorageDialog(Window parentFrame, BinaryStore binaryStore) {
		super(parentFrame, binaryStore.getUnitName() + " Options", false);
		this.binaryStore = binaryStore;

		String help = "utilities.BinaryStore.docs.binarystore_overview";
		setHelpPoint(help);

		binaryStorageDialogPanel = new BinaryStorageDialogPanel(parentFrame, binaryStore.getClass() == SecondaryBinaryStore.class);
		setDialogComponent(binaryStorageDialogPanel.getPanel());
	}

	public static BinaryStoreSettings showDialog(Window parentFrame, BinaryStore binaryStore) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.binaryStore != binaryStore) {
			singleInstance = new BinaryStorageDialog(parentFrame, binaryStore);
		}
		singleInstance.binaryStoreSettings = binaryStore.binaryStoreSettings.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.binaryStoreSettings;
	}

	private void setParams() {
		binaryStorageDialogPanel.setParams(binaryStoreSettings);
	}

	@Override
	public void cancelButtonPressed() {
		binaryStoreSettings = null;
	}

	@Override
	public boolean getParams() {
		if (binaryStoreSettings == null) {
			binaryStoreSettings = new BinaryStoreSettings();
		}
		return binaryStorageDialogPanel.getParams(binaryStoreSettings);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
