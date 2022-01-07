package loggerForms.monitor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import loggerForms.FormDescription;
import loggerForms.FormsControl;

public class FormsSelPanel implements PamDialogPanel {

	private FormsDataSelector formsDataSelector;
	
	private JPanel mainPanel;
	
	private JCheckBox[] checkBoxes;

	public FormsSelPanel(FormsDataSelector formsDataSelector) {
		this.formsDataSelector = formsDataSelector;
		this.mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Select Forms"));
	}
	
	private void makeControls() {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Form Name", JLabel.CENTER),c);
		c.gridx++;
		mainPanel.add(new JLabel("Select", JLabel.CENTER),c);
		
		FormsControl formsControl = formsDataSelector.getFormsControl();
		int nForms = formsControl.getNumFormDescriptions();
		checkBoxes = new JCheckBox[nForms];
		for (int i = 0; i < nForms; i++) {
			FormDescription formDesc = formsControl.getFormDescription(i);
			c.gridx = 0;
			c.gridy++;
			JLabel label;
			mainPanel.add(label = new JLabel(formDesc.getFormName() + " ", JLabel.RIGHT), c);
			c.gridx++;
			mainPanel.add(checkBoxes[i] = new JCheckBox(""), c);
			
			label.setToolTipText("Check box to select form " + formDesc.getFormName());
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		FormsSelectorParams params = formsDataSelector.getParams();
		makeControls();
		if (checkBoxes == null) {
			return;
		}
		FormsControl formsControl = formsDataSelector.getFormsControl();
		int nForms = Math.min(checkBoxes.length, formsControl.getNumFormDescriptions());
		for (int i = 0; i < nForms; i++) {
			FormDescription formDesc = formsControl.getFormDescription(i);
			checkBoxes[i].setSelected(params.isFormSelected(formDesc.getFormName()));	
		}
	}

	@Override
	public boolean getParams() {
		FormsSelectorParams params = formsDataSelector.getParams();
		if (checkBoxes == null) {
			return false;
		}
		FormsControl formsControl = formsDataSelector.getFormsControl();
		int nForms = Math.min(checkBoxes.length, formsControl.getNumFormDescriptions());
		for (int i = 0; i < nForms; i++) {
			FormDescription formDesc = formsControl.getFormDescription(i);
			params.setFormSelected(formDesc.getFormName(), checkBoxes[i].isSelected());	
		}
		return true;
	}

}
