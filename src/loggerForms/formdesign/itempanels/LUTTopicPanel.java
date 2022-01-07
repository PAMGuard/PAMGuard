package loggerForms.formdesign.itempanels;

import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupList;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controls.LookupControl;
import loggerForms.formdesign.ControlTitle;

public class LUTTopicPanel extends CtrlColPanel {

	private JTextField topicName;
	
	private JButton editButton;
	
	private JPanel lutPanel;
	
	private LookupComponent lutComponent;
		
	public LUTTopicPanel(ControlTitle controlTitle, UDColName propertyName) {
		super(controlTitle, propertyName);
		lutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lutPanel.add(topicName = new JTextField(UDColName.Topic.getStringLength()/2));
		lutComponent = new LookupComponent("", null);
		lutPanel.add(lutComponent.getComponent());
		topicName.setToolTipText("Control name will be used if Topic is left blank");
		lutComponent.setToolTipText("Right click to edit lookup list items");
		
		topicName.addFocusListener(new TopicFocus());
	}
	
	class TopicFocus implements FocusListener {

		@Override
		public void focusGained(FocusEvent arg0) {
//			System.out.println("Topic Focus gained");
		}

		@Override
		public void focusLost(FocusEvent arg0) {
//			System.out.println("Topic Focus lost");
			refillTable();
		}
		
	}

	private void refillTable() {
		String name = topicName.getText();
		if (name == null  || name.length() == 0) {
			name = controlTitle.getItemInformation().getStringProperty(UDColName.Title.toString());
		}
//		lookupList.setListTopic(name);
		lutComponent.setTopic(name);
//		lutComponent.setLookupList(null);
//		lutComponent.fillList();
	}
	
	@Override
	public Component getPanel() {
		return lutPanel;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		topicName.setText(itemDescription.getStringProperty(UDColName.Topic.toString()));
		refillTable();
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String name = topicName.getText();
		if (name == null || name.length() == 0){
			return false;
		}
		itemDescription.setProperty(UDColName.Topic.toString(), name);
		itemDescription.setProperty(UDColName.Length.toString(), new Integer(LookUpTables.CODE_LENGTH));
		return true;
	}

}
