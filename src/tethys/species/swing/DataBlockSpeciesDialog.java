package tethys.species.swing;

import java.awt.Window;

import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;

public class DataBlockSpeciesDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	DataBlockSpeciesPanel speciesPanel;
	
	private DataBlockSpeciesDialog(Window parentFrame, PamDataBlock dataBlock) {
		super(parentFrame, dataBlock.getDataName() +  " species", false);
		speciesPanel = new DataBlockSpeciesPanel(dataBlock);
		setDialogComponent(speciesPanel.getDialogComponent());
	}
	
	public static void showDialog(Window parentFrame, PamDataBlock dataBlock) {
		DataBlockSpeciesDialog speciesDialog = new DataBlockSpeciesDialog(parentFrame, dataBlock);
		speciesDialog.setParams();
		speciesDialog.setVisible(true);
	}

	private void setParams() {
		speciesPanel.setParams();
	}

	@Override
	public boolean getParams() {
		return speciesPanel.getParams();
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
