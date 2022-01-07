package spectrogramNoiseReduction.medianFilter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamGridBagContraints;
import spectrogramNoiseReduction.SpecNoiseDialogComponent;

public class MedianFilterDialogBits implements SpecNoiseDialogComponent {

	private SpectrogramMedianFilter spectrogramMedianFilter;
	
	private JPanel dialogPanel;
	
	private JTextField filterLength;
	
	
	public MedianFilterDialogBits(
			SpectrogramMedianFilter spectrogramMedianFilter) {
		super();
		this.spectrogramMedianFilter = spectrogramMedianFilter;
		
		dialogPanel = new JPanel();
		GridBagLayout gb;
		dialogPanel.setLayout(gb = new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		gb.setConstraints(dialogPanel, c);
		dialogPanel.add(new JLabel("Filter length (should be odd) "));
		c.gridx++;
		gb.setConstraints(dialogPanel, c);
		dialogPanel.add(filterLength = new JTextField(6));
	}
	
	

	@Override
	public void setSelected(boolean selected) {
		filterLength.setEnabled(selected);
		
	}



	@Override
	public boolean getParams() {
		try {
			int newVal = 
				Integer.valueOf(filterLength.getText());
			if (newVal < 3 || newVal%2 == 0) {
				JOptionPane.showMessageDialog(null, 
						"Filter length must be odd and >= 3");
				return false;
			}
			spectrogramMedianFilter.medianFilterParams.filterLength = newVal;
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public JComponent getSwingComponent() {
		return dialogPanel;
	}

	@Override
	public void setParams() {

		filterLength.setText(String.format("%d", spectrogramMedianFilter.medianFilterParams.filterLength));
		
	}

}
