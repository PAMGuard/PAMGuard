package qa.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import qa.analyser.QAReportOptions;

public class QAReportDialog extends PamDialog {

	private static QAReportDialog singleInstance;

	private QAReportOptions reportOptions;
	
	private JRadioButton reportSingleSounds;
	private JRadioButton reportSequences;
	
	private JRadioButton combinePlots;
	private JRadioButton separatePlots;

	private QAReportDialog(Window parentFrame) {
		super(parentFrame, "SIDE Report Options", true);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Report options"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		reportSingleSounds = new JRadioButton("Report single sound detection efficiency");
		reportSequences = new JRadioButton("Only report efficiency of detecting sequences");
		combinePlots = new JRadioButton("Combine data from multiple detectors into a single plot");
		separatePlots = new JRadioButton("Separate results from each detector into a separate plot");
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(reportSingleSounds);
		bg1.add(reportSequences);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(combinePlots);
		bg2.add(separatePlots);
		
		mainPanel.add(new JLabel("Single sounds vs Sequences"),c);
		c.gridy++;
		mainPanel.add(reportSingleSounds, c);
		c.gridy++;
		mainPanel.add(reportSequences, c);
		c.gridy++;
		mainPanel.add(new JLabel("Plot style"),c);
		c.gridy++;
		mainPanel.add(combinePlots, c);
		c.gridy++;
		mainPanel.add(separatePlots, c);
		
		setDialogComponent(mainPanel);
	}
	
	public static QAReportOptions showDialog(Window parentFrame, QAReportOptions reportOptions) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new QAReportDialog(parentFrame);
		}
		singleInstance.reportOptions = reportOptions.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.reportOptions;
	}

	private void setParams() {
		reportSingleSounds.setSelected(reportOptions.showSingleSoundResults);
		reportSequences.setSelected(!reportOptions.showSingleSoundResults);
		combinePlots.setSelected(!reportOptions.showIndividualDetectors);
		separatePlots.setSelected(reportOptions.showIndividualDetectors);
	}

	@Override
	public boolean getParams() {
		reportOptions.showSingleSoundResults = reportSingleSounds.isSelected();
		reportOptions.showIndividualDetectors = separatePlots.isSelected();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		reportOptions = null;
	}

	@Override
	public void restoreDefaultSettings() {
		reportOptions = new QAReportOptions();
		setParams();
	}

}
