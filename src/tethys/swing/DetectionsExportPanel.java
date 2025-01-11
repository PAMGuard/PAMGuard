package tethys.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.species.DataBlockSpeciesManager;
import tethys.swing.export.DetectionsExportWizard;

@Deprecated
public class DetectionsExportPanel extends TethysGUIPanel implements StreamTableObserver {
	
	private JPanel mainPanel;
	
	private JButton exportButton;

	private PamDataBlock selectedDataBlock;

	private DetectionsExportPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new PamAlignmentPanel(BorderLayout.NORTH);
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Export"));
		exportButton = new JButton("<html>Export<p>>>>></html>");
		exportButton.setToolTipText("Export PAMGaurd data to Tethys");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExport();
			}
		});
		exportButton.setToolTipText("Select a Data Block on the left to enable export");
		exportButton.setEnabled(false);
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(exportButton, c);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	private void doExport() {
		if (selectedDataBlock == null) {
			return;
		}
		
		/**
		 * Check the species map is OK before doing anything. 
		 */
		DataBlockSpeciesManager spManager = selectedDataBlock.getDatablockSpeciesManager();
		if (spManager != null) {
			String error = spManager.checkSpeciesMapError();
			if (error != null) {
				PamDialog.showWarning(PamController.getMainFrame(), "Datablock species manager error", error);
				spManager.showSpeciesDialog();
				return;
			}
		}
		
		DetectionsExportWizard.showDialog(getTethysControl().getGuiFrame(), getTethysControl(), selectedDataBlock, true);
	}

	@Override
	public void selectDataBlock(PamDataBlock dataBlock) {
		this.selectedDataBlock = dataBlock;
		exportButton.setEnabled(selectedDataBlock != null);
	}

}
