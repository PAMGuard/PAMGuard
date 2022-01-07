package annotation.dataselect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class ScalarDialogPanel implements PamDialogPanel {

	private ScalarDataSelector scalarDataSelector;
	
	private JPanel mainPanel;
	
	private JTextField minVal, maxVal;
	
	private int fields;

	public ScalarDialogPanel(ScalarDataSelector scalarDataSelector) {
		super();
		this.scalarDataSelector = scalarDataSelector;
		this.fields = scalarDataSelector.getUseMinMax();
		mainPanel = new JPanel(new GridBagLayout());
		String name = scalarDataSelector.getAnnotationType().getAnnotationName();
		mainPanel.setBorder(new TitledBorder(name));
		GridBagConstraints c = new PamGridBagContraints();
		if ((fields & ScalarDataSelector.USE_MINIMUM) != 0) {
			mainPanel.add(new JLabel("Minimum: ", JLabel.RIGHT), c);
			c.gridx++;
			mainPanel.add(minVal = new JTextField(5), c);
			c.gridx = 0;
			c.gridy++;
		}
		if ((fields & ScalarDataSelector.USE_MAXIMUM) != 0) {
			mainPanel.add(new JLabel("Maximum: ", JLabel.RIGHT), c);
			c.gridx++;
			mainPanel.add(maxVal = new JTextField(5), c);
			c.gridx = 0;
			c.gridy++;
		}
		
	}
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}
	
	@Override
	public void setParams() {
		ScalarDataParams params = scalarDataSelector.getScalarDataParams();
		if (params == null) {
			return;
		}
		if (minVal != null) {
			minVal.setText(Double.valueOf(params.minValue).toString());
		}
		if (maxVal != null) {
			maxVal.setText(Double.valueOf(params.maxValue).toString());
		}
		
	}

	@Override
	public boolean getParams() {
		ScalarDataParams params = scalarDataSelector.getScalarDataParams();
		if (params == null) {
			params = new ScalarDataParams();
		}
		if (minVal != null) {
			try {
				params.minValue = Double.valueOf(minVal.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(PamController.getMainFrame(), "Number format error", "Invalid minimum value");
			}
		}
		if (maxVal != null) {
			try {
				params.maxValue = Double.valueOf(maxVal.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(PamController.getMainFrame(), "Number format error", "Invalid minimum value");
			}
		}
		scalarDataSelector.setParams(params);
		return true;
	}

}
