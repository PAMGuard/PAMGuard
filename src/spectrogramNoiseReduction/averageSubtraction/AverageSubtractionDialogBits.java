package spectrogramNoiseReduction.averageSubtraction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamGridBagContraints;
import spectrogramNoiseReduction.SpecNoiseDialogComponent;

public class AverageSubtractionDialogBits implements SpecNoiseDialogComponent {

	private AverageSubtraction averageSubtraction;
	
	private JPanel dialogPanel;
	
	private JTextField updateConstant;
	
	
	public AverageSubtractionDialogBits(
			AverageSubtraction averageSubtraction) {
		super();
		this.averageSubtraction = averageSubtraction;
		
		dialogPanel = new JPanel();
		GridBagLayout gb;
		dialogPanel.setLayout(gb = new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		gb.setConstraints(dialogPanel, c);
		dialogPanel.add(new JLabel("Update constant (e.g. .02) "));
		c.gridx++;
		gb.setConstraints(dialogPanel, c);
		dialogPanel.add(updateConstant = new JTextField(6));
	}
	
	@Override
	public void setSelected(boolean selected) {
		updateConstant.setEnabled(selected);
		
	}



	@Override
	public boolean getParams() {
		try {
			double newVal = 
				Double.valueOf(updateConstant.getText());
			if (newVal <= 0 || newVal > 0.5) {
				JOptionPane.showMessageDialog(null, 
						"Average Subtraction update constant must be between 0 and 0.5");
				return false;
			}
			averageSubtraction.averageSubtractionParameters.updateConstant = newVal;
		}
		catch (Exception e) {
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

		updateConstant.setText(String.format("%.3f", 
				averageSubtraction.averageSubtractionParameters.updateConstant));
		
	}


}
