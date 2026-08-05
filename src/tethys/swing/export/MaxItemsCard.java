package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.wizard.PamWizard;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

/**
 * Card to set the maximum number of items to be exported to a single Tethys document. 
 * Good to keep this below about 100k
 */
public class MaxItemsCard extends ExportWizardCard {
	
	private JTextField maxItems;
	
	private JTextField totalItems;
	
	private JLabel granularity;

	private DetectionsExportWizard detectionsExportWizard;

	public MaxItemsCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, detectionsExportWizard, "Items per document", dataBlock);

		this.detectionsExportWizard = detectionsExportWizard;
		
		this.setLayout(new BorderLayout());
		JPanel nPanel = new JPanel(new GridBagLayout());
		this.add(BorderLayout.NORTH, nPanel);
		this.setBorder(new TitledBorder("Max items per exported document"));
		
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
//		nPanel.add(new JLabel(" "), c);
//		c.gridy++;
		nPanel.add(granularity = new JLabel(" ", JLabel.LEFT), c);
		c.gridy++;
		c.gridwidth = 1;
		nPanel.add(new JLabel("Number of items in data block: ", JLabel.RIGHT), c);
		c.gridx++;
		nPanel.add(totalItems = new JTextField(7), c);
		c.gridx = 0;
		c.gridy++;
		nPanel.add(new JLabel("Max items per export document: ", JLabel.RIGHT), c);
		c.gridx++;
		nPanel.add(maxItems = new JTextField(7), c);
		
		totalItems.setToolTipText("Total number of data items to export");
		maxItems.setToolTipText("Recommended not to have more than about 100 thousand items in each exported document");
		totalItems.setEditable(false);
	}

	@Override
	public void setParams(StreamExportParams cardParams) {
		int n = estimateNItems(cardParams);
		boolean isEst = cardParams.granularity != GranularityEnumType.CALL;
		granularity.setText("Current granularity is " + cardParams.granularity);
		if (isEst) {
			totalItems.setText(String.format("%d (Est')", n));
		}
		else {
			totalItems.setText(String.format("%d", n));
		}
		maxItems.setText(String.format("%d", cardParams.getMaxDetectionItems()));
		
		if (isEst) {
			totalItems.setToolTipText("This is an estimate of the maximum number of items to export. "
					+ "\nThe true number of items may be a lot less, depending on the data");
		}
		else {
			totalItems.setToolTipText("Total number of data items to export");
		}
	}

	@Override
	public boolean getParams(StreamExportParams cardParams) {
		int n = 0;
		try {
			n = Integer.valueOf(maxItems.getText());
			cardParams.setMaxDetectionItems(n);
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(detectionsExportWizard, "Invalid max items value",  "Invalid max items value: must be integer");
		}
		int nMax = StreamExportParams.DEFAULTMAXITEMS * 2;
		if (n > nMax) {
			String wText = "Exporting too many items into a single document can cause problems. \n"
					+ "You're recommended to keep the number of items per document to around " + StreamExportParams.DEFAULTMAXITEMS +
					"\n\nDo you want to proceed anyway ? ";
			int ans = WarnOnce.showWarning(detectionsExportWizard, "Export warning", wText, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * Get an estimate of the max number of items to export. 
	 * @return
	 */
	private int estimateNItems(StreamExportParams cardParams) {
		OfflineDataMap dm = getDataBlock().getPrimaryDataMap();
		int n = 0;
		if (dm == null) {
			return 0;
		}
		n = dm.getDataCount();
		long t = dm.getLastDataTime() - dm.getFirstDataTime();
		switch (cardParams.granularity) {
		case BINNED:
			return (int) (t / (cardParams.binDurationS * 1000));
		case CALL:
			return n;
		case ENCOUNTER:
			return (int) (t / (cardParams.encounterGapS * 1000));
		case GROUPED:
			return (int) (t / (cardParams.encounterGapS * 1000));
		default:
			break;
		
		}
	
		return n;
	}

}
