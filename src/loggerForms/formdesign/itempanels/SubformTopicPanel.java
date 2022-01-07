package loggerForms.formdesign.itempanels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamGridBagContraints;
import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.InputControlDescription;
import loggerForms.formdesign.ControlTitle;

public class SubformTopicPanel extends CtrlColPanel {

	private JComboBox<String> otherForms;
	
	private JComboBox<String> thisFormControl;
	
	private JComboBox<String> otherFormControl;
	
	private JPanel mainPanel;

	private FormDescription formDescription;
	
	private JLabel otherLabel;

	private ItemInformation itemDescription;
	
	public SubformTopicPanel(FormDescription formDescription, ControlTitle controlTitle, UDColName propertyName) {
		super(controlTitle, propertyName);
		this.formDescription = formDescription;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Sub form ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(otherForms = new JComboBox<String>());
		c.gridx++;
		mainPanel.add(new JLabel(", Cross reference this forms ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(thisFormControl = new JComboBox<String>());
		c.gridx++;
		mainPanel.add(otherLabel = new JLabel(" onto ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(otherFormControl = new JComboBox<String>());
		
		otherForms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				otherFormSelected();
			}
		});
	}

	/**
	 * Called when other form is selected. 
	 */
	protected void otherFormSelected() {
		otherFormControl.removeAllItems();
		FormsControl formsControl = formDescription.getFormsControl();
		String topic = (String) otherForms.getSelectedItem();
		if (topic == null) {
			return;
		}
		FormDescription otherDescription = formsControl.findFormDescription(topic);
		if (otherDescription == null) {
			return;
		}
		ArrayList<InputControlDescription> otherControls = otherDescription.getInputControlDescriptions();
		for (ControlDescription aControl:otherControls) {
			otherFormControl.addItem(aControl.getTitle());
		}		
		otherLabel.setText(String.format(" onto %s forms ", topic));
		setOtherFormControl();
	}
	
	@Override
	public Component getPanel() {
		return mainPanel;
	}
	
	/**
	 * Fill the list of other forms that are available. 
	 */
	private void fillFormsList() {
		otherForms.removeAllItems();
		FormsControl formsControl = formDescription.getFormsControl();
		int nForms = formsControl.getNumFormDescriptions();
		for (int i = 0; i < nForms; i++) {
			FormDescription aDescription = formsControl.getFormDescription(i);
			// I think we can let a form subform itself !
//			if (aDescription == formDescription) {
//				continue;
//			}
			otherForms.addItem(aDescription.getFormName());
		}
	}
	
	/**
	 * Fill the list of available controls on this form. 
	 */
	private void fillThisFormsControlList() {
		ArrayList<InputControlDescription> ipCtrls = formDescription.getInputControlDescriptions();
		String topic = (String) thisFormControl.getSelectedItem();
		thisFormControl.removeAllItems();
		for (ControlDescription aCtrol:ipCtrls) {
			thisFormControl.addItem(aCtrol.getTitle());
		}
		if (topic != null) {
			thisFormControl.setSelectedItem(topic);
		}
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		fillFormsList();
		this.itemDescription = itemDescription;
		String topic = itemDescription.getStringProperty(UDColName.Topic.toString());
		if (topic != null) {
			otherForms.setSelectedItem(topic);
		}
		fillThisFormsControlList();
		topic = itemDescription.getStringProperty(UDColName.Send_Control_Name.toString());
		if (topic != null) {
			thisFormControl.setSelectedItem(topic);
		}
	}

	private void setOtherFormControl() {
		if (itemDescription == null) {
			return;
		}
		String otherCtrl = itemDescription.getStringProperty(UDColName.Control_on_Subform.toString());
		otherFormControl.setSelectedItem(otherCtrl);
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String otherForm = (String) otherForms.getSelectedItem();
		itemDescription.setProperty(UDColName.Topic.toString(), otherForm);
		
		String thisFC = (String) thisFormControl.getSelectedItem();
		itemDescription.setProperty(UDColName.Send_Control_Name.toString(), thisFC);

		String otherFC = (String) otherFormControl.getSelectedItem();
		itemDescription.setProperty(UDColName.Control_on_Subform.toString(), otherFC);
		
		
		return otherForm != null;
	}

}
