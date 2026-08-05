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
import javax.swing.JTextField;

import PamView.dialog.PamGridBagContraints;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.InputControlDescription;
import loggerForms.formdesign.ControlTitle;

/**
 * Topic panel for Button - similar to that for Subforms. 
 */
public class ButtonTopicPanel extends CtrlColPanel {

	private JComboBox<String> otherForms;

	private JComboBox<String> otherFormControl;

	private JPanel mainPanel;

	private FormDescription formDescription;

	private ItemInformation itemDescription;

	private LookupComponent otherFormLUT;
	
	private JTextField otherFormText;

	public ButtonTopicPanel(FormDescription formDescription, ControlTitle controlTitle, UDColName propertyName) {
		super(controlTitle, propertyName);
		this.formDescription = formDescription;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Form to open ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(otherForms = new JComboBox<String>());
		c.gridx++;
		mainPanel.add(new JLabel(", Set control ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(otherFormControl = new JComboBox<String>(), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("to value ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 3;
		otherFormLUT = new LookupComponent("", null);
		mainPanel.add(otherFormLUT.getComponent(), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		c.gridx = 2;
		mainPanel.add(otherFormText = new JTextField(6), c);
		
		otherForms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				otherFormSelected();
			}
		});
		
		otherFormControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				otherFormControlSelected();
			}
		});
	}

	protected void otherFormControlSelected() {
		// see what control type it is. 
		String othCtrl = (String) otherFormControl.getSelectedItem();
		FormDescription othform = getOtherDescription();
		ControlDescription othCtrlDesc = null;
		if (othform != null && othCtrl != null) {
			othCtrlDesc = othform.findControlDescription(othCtrl);
		}
		if (othCtrlDesc instanceof CdLookup) {
			otherFormLUT.setTopic(othCtrlDesc.getTopic());
			otherFormLUT.getComponent().setVisible(true);
			otherFormText.setVisible(false);
		}
		else {
			otherFormLUT.setTopic("nothingatall");
			otherFormLUT.getComponent().setVisible(false);
			otherFormText.setVisible(true);
//			otherFormLUT.setvi
		}
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
//		otherLabel.setText(String.format(" onto %s forms ", topic));
		setOtherFormControl();
	}
	
	private FormDescription getOtherDescription() {
		FormsControl formsControl = formDescription.getFormsControl();
		String topic = (String) otherForms.getSelectedItem();
		if (topic == null) {
			return null;
		}
		FormDescription otherDescription = formsControl.findFormDescription(topic);
		return otherDescription;
		
	}
	
	@Override
	public Component getPanel() {
		return mainPanel;
	}

	private void setOtherFormControl() {
		if (itemDescription == null) {
			return;
		}
		String otherCtrl = itemDescription.getStringProperty(UDColName.Control_on_Subform.toString());
		otherFormControl.setSelectedItem(otherCtrl);
	}

	
	@Override
	public void pushProperty(ItemInformation itemDescription) {
		fillFormsList();
		this.itemDescription = itemDescription;
		// topic used for form to open
		String topic = itemDescription.getStringProperty(UDColName.Topic.toString());
		if (topic != null) {
			otherForms.setSelectedItem(topic);
		}
		// control on subform for the subforms !
		String subF = itemDescription.getStringProperty(UDColName.Control_on_Subform.toString());
		if (subF != null) {
			
		}
		// post tit is used for the code we're going to write to the selected other form. 
		String postTit = itemDescription.getStringProperty(UDColName.PostTitle.toString());		
		if (otherFormLUT != null) {
			otherFormLUT.setSelectedCode(postTit);
		}
		if (otherFormText != null) {
			otherFormText.setText(postTit);
		}
////		fillThisFormsControlList();
//		topic = itemDescription.getStringProperty(UDColName.Send_Control_Name.toString());
//		if (topic != null) {
//			thisFormControl.setSelectedItem(topic);
//		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String otherForm = (String) otherForms.getSelectedItem();
		itemDescription.setProperty(UDColName.Topic.toString(), otherForm);
		
		String otherFC = (String) otherFormControl.getSelectedItem();
		itemDescription.setProperty(UDColName.Control_on_Subform.toString(), otherFC);
		
		String pushTxt = null;
		if (otherFormLUT.getComponent().isVisible()) {
			LookupItem lutITem = otherFormLUT.getSelectedItem();
			if (lutITem != null) {
				pushTxt = lutITem.getCode();
			}
		}
		if (pushTxt == null && otherFormText.isVisible()) {
			pushTxt = otherFormText.getText();
		}
		itemDescription.setProperty(UDColName.PostTitle.toString(), pushTxt);
		
		return otherForm != null;
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

}
