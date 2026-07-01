package loggerForms.formdesign.itempanels;

import java.awt.Component;
import java.util.Set;

import javax.swing.JComboBox;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.actions.LoggerAction;
import loggerForms.actions.LoggerActions;
import loggerForms.formdesign.ControlTitle;

public class ActionTopicPanel extends CtrlColPanel {
	
	private JComboBox<String> actionsList;
	private LoggerActions loggerActions;
	private Set<String> actionKeys;

	public ActionTopicPanel(ControlTitle controlTitle, UDColName propertyName) {
		super(controlTitle, propertyName);
		actionsList = new JComboBox<String>();
		loggerActions = LoggerActions.getInstance();
		actionKeys = loggerActions.getActionKeys();
		for (String key : actionKeys) {
			actionsList.addItem(key);
		}
		
	}

	@Override
	public Component getPanel() {
		return actionsList;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		String topic = itemDescription.getStringProperty(UDColName.Topic.toString());
		if (topic != null) {
			actionsList.setSelectedItem(topic);
		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String selItem = (String) actionsList.getSelectedItem();
		itemDescription.setProperty(UDColName.Topic.toString(), selItem);
		return selItem != null;
	}

}
