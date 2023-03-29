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

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.swing.export.DetectionsExportWizard;

public class DetectionsExportPanel extends TethysGUIPanel implements StreamTableObserver {
	
	private JPanel mainPanel;
	
	private JButton exportButton;

	private PamDataBlock selectedDataBlock;

	public DetectionsExportPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new PamAlignmentPanel(BorderLayout.NORTH);
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Export"));
		exportButton = new JButton("Export");
		exportButton.setToolTipText("Export PAMGaurd data to Tethys");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExport();
			}
		});
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
		DetectionsExportWizard.showDilaog(getTethysControl().getGuiFrame(), getTethysControl(), selectedDataBlock);
	}

	@Override
	public void selectDataBlock(PamDataBlock dataBlock) {
		this.selectedDataBlock = dataBlock;
		exportButton.setEnabled(selectedDataBlock != null);
	}

}
