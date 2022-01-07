package spectrogramNoiseReduction.threshold;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import spectrogramNoiseReduction.SpecNoiseDialogComponent;

public class ThresholdDialogComponent implements SpecNoiseDialogComponent {

	private SpectrogramThreshold spectrogramThreshold;
	
	private JPanel dialogPanel;
	
	private JTextField thresholdDB;
	
	private JComboBox<String> outputType;
	
	public ThresholdDialogComponent(SpectrogramThreshold spectrogramThreshold) {
		super();
		this.spectrogramThreshold = spectrogramThreshold;

		dialogPanel = new JPanel();
		GridBagLayout gb;
		dialogPanel.setLayout(gb = new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		gb.setConstraints(dialogPanel, c);
		dialogPanel.add(new JLabel("Threshold (dB) "));
		c.gridx++;
		PamDialog.addComponent(dialogPanel, thresholdDB = new JTextField(6), c);
//		gb.setConstraints(dialogPanel, c);
//		dialogPanel.add(thresholdDB = new JTextField(6));
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		PamDialog.addComponent(dialogPanel, new JLabel("Below threshold -> 0. Set above threshold data to ..."), c);
		c.gridx = 0;
		c.gridy++;
//		c.gridwidth = 3;
		gb.setConstraints(dialogPanel, c);
		PamDialog.addComponent(dialogPanel, outputType = new JComboBox<String>(), c);
		outputType.addItem("Binary output (0's and 1's)");
		outputType.addItem("Use the output of the preceeding step");
		outputType.addItem("Use the input from the raw FFT data");
		c.gridy++;

		PamDialog.addComponent(dialogPanel, new JLabel("(Some downstream processes may want phase information)"), c);
	}

	@Override
	public boolean getParams() {
		try {
			double newVal = 
				Double.valueOf(thresholdDB.getText());
			if (newVal <= 0) {
				JOptionPane.showMessageDialog(null, 
						"Threshold must be greater than 0");
				return false;
			}
			spectrogramThreshold.thresholdParams.thresholdDB = newVal;
		}
		catch (Exception e) {
			return false;
		}
		spectrogramThreshold.thresholdParams.finalOutput = outputType.getSelectedIndex();
		return true;
	}

	@Override
	public JComponent getSwingComponent() {
		return dialogPanel;
	}

	@Override
	public void setParams() {

		thresholdDB.setText(String.format("%.1f", 
				spectrogramThreshold.thresholdParams.thresholdDB));
		
		outputType.setSelectedIndex(spectrogramThreshold.thresholdParams.finalOutput);

	}

	@Override
	public void setSelected(boolean selected) {
		thresholdDB.setEnabled(selected);
	}

}
