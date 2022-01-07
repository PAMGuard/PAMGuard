package envelopeTracer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Filters.FilterDialog;
import Filters.FilterParams;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class EnvelopeDialog extends PamDialog {

	private static EnvelopeDialog singleInstance;

	private EnvelopeParams envelopeParams;

	private SourcePanel sourcePanel;

	private JCheckBox logOutput;

	private JTextField outputSampleRate;

	private JButton preFilterButton, postFilterButton;

	private JLabel preFilterText, postFilterText;

	private float sampleRate;

	private EnvelopeDialog(Window parentFrame) {
		super(parentFrame, "Envelope Tracing Parameters", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		sourcePanel = new SourcePanel(this, "Raw data source", RawDataUnit.class, true, false);
		sourcePanel.addSelectionListener(new SourceSelection());
		mainPanel.add(sourcePanel.getPanel());

		JPanel p;
		GridBagConstraints c;		

		p = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		p.setBorder(new TitledBorder("Filter Band"));
		c.gridwidth = 2;
		addComponent(p, preFilterText = new JLabel("Pre filter"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(p, preFilterButton = new JButton("Filter Band..."), c);
		preFilterButton.addActionListener(new FilterSettings(true));
		mainPanel.add(p);

		p = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		p.setBorder(new TitledBorder("Output"));
		addComponent(p, new JLabel("Sample Rate ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(p, outputSampleRate = new JTextField(7), c);
		c.gridx++;
		addComponent(p, new JLabel(" Hz", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		addComponent(p, logOutput = new JCheckBox("Log output scale"), c);
		c.gridy++;
		c.gridwidth = 3;
		addComponent(p, postFilterButton = new JButton("Output Smoothing..."), c);
		postFilterButton.addActionListener(new FilterSettings(false));
		c.gridy++;
		c.gridwidth = 3;
		addComponent(p, preFilterText = new JLabel("output filter"), c);
		mainPanel.add(p);
		
		setHelpPoint("sound_processing.EnvelopeTrace.Docs.EnvelopeOverview");

		setDialogComponent(mainPanel);
	}

	public static EnvelopeParams showDialog(Window parentFrame, EnvelopeParams envelopeParams) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new EnvelopeDialog(parentFrame);
		}
		singleInstance.envelopeParams = envelopeParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.envelopeParams;
	}

	private void setParams() {
		sourcePanel.setSource(envelopeParams.dataSourceName);
		sourcePanel.setChannelList(envelopeParams.channelMap);
		outputSampleRate.setText(String.format("%3.1f", envelopeParams.outputSampleRate));
		logOutput.setSelected(envelopeParams.logScale);

		sourceSelection();
	}

	@Override
	public void cancelButtonPressed() {
		envelopeParams = null;
	}

	@Override
	public boolean getParams() {
		PamDataBlock dataBlock = sourcePanel.getSource();
		if (dataBlock == null) {
			return false;
		}
		envelopeParams.dataSourceName = dataBlock.getDataName();
		try {
			envelopeParams.channelMap = dataBlock.getChannelMap() & sourcePanel.getChannelList();
			envelopeParams.outputSampleRate = Float.valueOf(outputSampleRate.getText());
			envelopeParams.logScale = logOutput.isSelected();
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		if (envelopeParams.channelMap == 0) {
			return showWarning("No channels selected");
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class SourceSelection implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			sourceSelection();
		}
	}

	public void sourceSelection() {
		PamDataBlock dataBlock = sourcePanel.getSource();
		if (dataBlock == null) {
			sampleRate = -1;
		}
		else {
			sampleRate = dataBlock.getSampleRate();
		}
		enableControls();
	}

	private void enableControls() {
		boolean b = (sampleRate > 0);
		preFilterButton.setEnabled(b);
		postFilterButton.setEnabled(b);
		logOutput.setEnabled(b);
		outputSampleRate.setEnabled(b);
	}

	private class FilterSettings implements ActionListener {

		boolean isFirst;
		/**
		 * @param isFirst
		 */
		public FilterSettings(boolean isFirst) {
			super();
			this.isFirst = isFirst;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (isFirst) {
				preFilterSettings();
			}
			else {
				postFilterSettings();
			}
		}

	}

	private void preFilterSettings() {
		FilterParams newParams = FilterDialog.showDialog(this.getOwner(), envelopeParams.filterSelect, sampleRate);
		if (newParams != null) {
			envelopeParams.filterSelect = newParams.clone();
		}
	}

	private void postFilterSettings() {		
		FilterParams newParams = FilterDialog.showDialog(this.getOwner(), envelopeParams.postFilterParams, sampleRate);
		if (newParams != null) {
			envelopeParams.postFilterParams = newParams.clone();
		}
	}

}
