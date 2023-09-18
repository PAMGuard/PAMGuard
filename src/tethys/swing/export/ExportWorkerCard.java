package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.detection.DetectionExportObserver;
import tethys.detection.DetectionExportProgress;
import tethys.detection.DetectionsHandler;
import tethys.output.StreamExportParams;

public class ExportWorkerCard extends ExportWizardCard implements DetectionExportObserver {
	
	private JProgressBar progressBar;
	
	private JTextField progressText;
	
	private JTextField itemCount, skipCount, projectedCount;
	
	private JButton export, cancel;

	private StreamExportParams streamExportParams;

	private DetectionsExportWizard detectionsExportWizard;

	public ExportWorkerCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, "Export", dataBlock);
		this.detectionsExportWizard = detectionsExportWizard;
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Export data"));
		JPanel exPanel = new PamNorthPanel(new GridBagLayout());
		this.add(BorderLayout.WEST, exPanel);
		GridBagConstraints c = new PamGridBagContraints();
		progressBar = new JProgressBar();
		progressText = new JTextField(30);
		itemCount = new JTextField(6);
		skipCount = new JTextField(6);
		projectedCount = new JTextField(6);
		progressText.setEditable(false);
		itemCount.setEditable(false);
		skipCount.setEditable(false);
		projectedCount.setEditable(false);
		export = new JButton("Export data");
		cancel = new JButton("Cancel export");
		cancel.setEnabled(false);
		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportData();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelExport();
			}
		});
		
		exPanel.add(new JLabel("Items written ", JLabel.RIGHT), c);
		c.gridx++;
		exPanel.add(itemCount, c);
		c.gridx = 0;
		c.gridy++;
		exPanel.add(new JLabel("Items skipped ", JLabel.RIGHT), c);
		c.gridx++;
		exPanel.add(skipCount, c);
		c.gridx = 0;
		c.gridy++;
		exPanel.add(new JLabel("Total expected ", JLabel.RIGHT), c);
		c.gridx++;
		exPanel.add(projectedCount, c);
		c.gridx = 0;
		c.gridy++;

		exPanel.add(new JLabel("Progress ... ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 3;
		exPanel.add(progressBar, c);
		c.gridy++;
		exPanel.add(progressText, c);
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy++;
		exPanel.add(export, c);
		c.gridx++;
		exPanel.add(cancel, c);
	}

	protected void exportData() {
		DetectionsHandler detHandler = getTethysControl().getDetectionsHandler();
		detHandler.startExportThread(getDataBlock(), streamExportParams, this);
		enableControls(DetectionExportProgress.STATE_GATHERING);
	}

	protected void cancelExport() {
		DetectionsHandler detHandler = getTethysControl().getDetectionsHandler();
		detHandler.cancelExport();
	}

	@Override
	public boolean getParams(StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setParams(StreamExportParams streamExportParams) {
		this.streamExportParams = streamExportParams;
	}

	@Override
	public void update(DetectionExportProgress progress) {
		if (progress == null) {
			return;
		}
		if (progress.totalUnits > 0) {
			itemCount.setText(String.format("%d", progress.exportCount));
			skipCount.setText(String.format("%d", progress.skipCount));
			long totExpected = progress.totalUnits;
			if (progress.exportCount +progress.skipCount > 0) {
				totExpected *= progress.exportCount/(progress.exportCount+progress.skipCount);
			}
			projectedCount.setText(String.format("%d",  totExpected));
			itemCount.setText(String.format("%d", totExpected));
			long perc = (progress.exportCount+progress.skipCount) * 100 / progress.totalUnits;
			progressBar.setValue((int) perc);
		}
		switch (progress.state) {
		case DetectionExportProgress.STATE_GATHERING:
			progressText.setText("Running export");
			break;
		case DetectionExportProgress.STATE_COUNTING:
			progressText.setText("Counting data");
			break;
		case DetectionExportProgress.STATE_CANCELED:
			progressText.setText("Export cancelled");
			break;
		case DetectionExportProgress.STATE_COMPLETE:
			progressText.setText("Export complete");
			detectionsExportWizard.getCancelButton().setText("Close");
			detectionsExportWizard.getPreviousButton().setEnabled(false);
			break;
		case DetectionExportProgress.STATE_WRITING:
			progressText.setText("Writing to Tethys: " + progress.currentDetections.getId());
			break;
		}
		enableControls(progress.state);
	}

	private void enableControls(int state) {
		boolean stopped = state == DetectionExportProgress.STATE_CANCELED || state == DetectionExportProgress.STATE_COMPLETE;
		export.setEnabled(stopped);
		cancel.setEnabled(!stopped);
	}

}
