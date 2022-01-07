package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Localiser.DelayMeasurementParams;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import fftFilter.FFTFilterDialog;
import fftFilter.FFTFilterParams;

public class DelayOptionsPanel {

	private JCheckBox filterBearings, envelopeBearings, useLeadingEdge, upSample;
	private JButton filterSettings;
	private JLabel filterDescription;
	
	private DelayMeasurementParams delayMeasurementParams;
	
	private Window owner;
	
	private JPanel panel;
	
	/**
	 * Check box for restricting samples. 
	 */
	private JCheckBox restrictSamples;
	private JTextField restrictSamplesField;
	private JSpinner upSampleSpinner;
	
	/**
	 * @param owner
	 */
	public DelayOptionsPanel(Window owner) {
		super();
		this.owner = owner;
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Delay measurement options"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, filterBearings = new JCheckBox("Filter data before measurement"),  c);
		filterBearings.addActionListener(new OptChanged());
		c.gridx+=c.gridwidth;
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, filterSettings = new JButton("Settings"), c);
		filterSettings.addActionListener(new FilterSettings());
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		PamDialog.addComponent(mainPanel, filterDescription = new JLabel(" ", SwingConstants.CENTER), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		mainPanel.add(upSample = new JCheckBox("Up sample data x2"), c);
		upSample.setToolTipText("Up sampling data to a higher frequency can improve timing accuracy for Narrow Band clicks (i.e. harbour Porpoise)");
		c.gridx+=c.gridwidth;
		SpinnerListModel spinnerModel = new SpinnerListModel(Arrays.asList(new Integer[] {2,3,4})); //restrict the spinenr options. 
		mainPanel.add(upSampleSpinner = new JSpinner(spinnerModel), c);
		upSampleSpinner.addChangeListener((e)->{
			upSample.setText("Up sample data x" + this.upSampleSpinner.getValue());
		}); 
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, envelopeBearings = new JCheckBox("Use waveform envelope"),  c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new JLabel("     "), c);
		c.gridx++;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, useLeadingEdge = new JCheckBox("Use envelope leading edge only"),  c);
		envelopeBearings.addActionListener(new OptChanged());
		c.gridx++;
		c.gridwidth = 2;
		envelopeBearings.addActionListener(new OptChanged());

		//restrict
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, restrictSamples = new JCheckBox("Restrict length"),  c);
		restrictSamples.addActionListener(new OptChanged());
		c.gridx+=c.gridwidth;
		PamDialog.addComponent(mainPanel, restrictSamplesField = new JTextField(4),  c);

	
		restrictSamples.setToolTipText("In environments where echoes are an issue restricting inital samples of detections "
				+ "(e.g. click snippets) is a simple but effective way to increase the accuracy of  time delay calculations. "
				+ "WARNING: Remember that this must cover the potential time delay in grouped detections ");
		filterBearings.setToolTipText("Filter data prior to bearing measurement to imporve accuracy");
		filterSettings.setToolTipText("Setup filter options");
		envelopeBearings.setToolTipText("Using the envelope can provide more accurate bearings for some narrowband pulses");
		filterDescription.setToolTipText("Current filter settings");
		useLeadingEdge.setToolTipText("For long pulses, or where there are echoes, restrict the calculation to the leading edge of the envelope");;
		
		panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.NORTH, mainPanel);
	}
	
	private class OptChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return panel;
	}

	private void enableControls() {
		filterSettings.setEnabled(filterBearings.isSelected());
		filterDescription.setEnabled(filterBearings.isSelected());
		useLeadingEdge.setEnabled(envelopeBearings.isSelected());
		restrictSamplesField.setEnabled(restrictSamples.isSelected());
		if (!envelopeBearings.isSelected()) {
//			useLeadingEdge.setSelected(false);
		}
	}
	
	private void describeFilter() {
		if (delayMeasurementParams == null || delayMeasurementParams.delayFilterParams == null) {
			filterDescription.setText("No filter");
			return;
		}
		filterDescription.setText(delayMeasurementParams.delayFilterParams.toString());
	}
	
	public void setParams(DelayMeasurementParams delayMeasurementParams) {
		this.delayMeasurementParams = delayMeasurementParams;
		filterBearings.setSelected(delayMeasurementParams.filterBearings);
		upSample.setSelected(delayMeasurementParams.getUpSample() > 1);
		envelopeBearings.setSelected(delayMeasurementParams.envelopeBearings);
		useLeadingEdge.setSelected(delayMeasurementParams.useLeadingEdge);
		restrictSamples.setSelected(delayMeasurementParams.useRestrictedBins);
		restrictSamplesField.setText(String.format("%d", delayMeasurementParams.restrictedBins));
		
		upSample.setText("Up sample data x" + this.upSampleSpinner.getValue());
		
		enableControls();
		describeFilter();
	}
	public boolean getParams(DelayMeasurementParams delayMeasurementParams) {
		delayMeasurementParams.delayFilterParams = this.delayMeasurementParams.delayFilterParams;
		delayMeasurementParams.filterBearings = filterBearings.isSelected();
		delayMeasurementParams.setUpSample(upSample.isSelected() ? ((Integer) this.upSampleSpinner.getValue()).intValue() : 1);
		delayMeasurementParams.envelopeBearings = envelopeBearings.isSelected();
		delayMeasurementParams.useLeadingEdge = useLeadingEdge.isSelected() && delayMeasurementParams.envelopeBearings;
		
		delayMeasurementParams.useRestrictedBins=this.restrictSamples.isSelected(); 
		
		try {
			delayMeasurementParams.restrictedBins=Integer.valueOf(this.restrictSamplesField.getText()); 
		}
		catch(Exception e) {
			return PamDialog.showWarning(owner, "Delay measurement settings", "The entry in the samples text field is invalid.");
		}
		
		if (delayMeasurementParams.useRestrictedBins && delayMeasurementParams.restrictedBins<10) {
			return PamDialog.showWarning(owner, "Delay measurement settings", "The entry in the samples text field is invalid. It must be >= 10");
		}
		
		if (delayMeasurementParams.filterBearings && delayMeasurementParams.delayFilterParams == null) {
			return PamDialog.showWarning(owner, "Delay measurement settings", "Filter parameters have not been set");
		}
		return true;
	}

	private class FilterSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			FFTFilterParams newParams = FFTFilterDialog.showDialog(owner, delayMeasurementParams.delayFilterParams);
			if (newParams != null) {
				delayMeasurementParams.delayFilterParams = newParams.clone();
				describeFilter();
			}
		}
	}
	
	
}
