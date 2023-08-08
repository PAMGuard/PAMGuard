package tethys.species.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import PamController.PamController;
import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;

public class DataBlockSpeciesDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private DataBlockSpeciesPanel speciesPanel;
	
	private DataBlockSpeciesDialog(Window parentFrame, PamDataBlock dataBlock) {
		super(parentFrame, dataBlock.getDataName() +  " species", false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		speciesPanel = new DataBlockSpeciesPanel(dataBlock);
		mainPanel.add(BorderLayout.CENTER, speciesPanel.getDialogComponent());
		JButton itisButton = new JButton("Go to ITIS web site");
		itisButton.setToolTipText("Go to ITIS website to search for species codes");
		itisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gotoITIS();
			}
		});
		JPanel nPanel = new JPanel(new BorderLayout());
		nPanel.add(BorderLayout.EAST, itisButton);
		mainPanel.add(BorderLayout.NORTH, nPanel);
		setDialogComponent(mainPanel);
	}
	
	protected void gotoITIS() {
		PamGui.openURL("https://www.itis.gov");
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
