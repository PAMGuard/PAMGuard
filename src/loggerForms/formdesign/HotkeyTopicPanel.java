package loggerForms.formdesign;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.itempanels.CtrlColPanel;

public class HotkeyTopicPanel extends CtrlColPanel {

	private FormDescription formDescription;
	
	private JPanel mainPanel;
	
	private JComboBox<String> hotKeyList;

	public HotkeyTopicPanel(FormDescription formDescription, ControlTitle selTitle, UDColName propertyName) {
		super(selTitle, propertyName);
		this.formDescription = formDescription;
	
		mainPanel = new JPanel();
		hotKeyList = new JComboBox<>();
		for (int i = 1; i <= 12; i++) {
			String str = String.format("F%d", i);
			hotKeyList.addItem(str);
		}
		mainPanel.add(hotKeyList);
	}

	@Override
	public Component getPanel() {
		return mainPanel;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		String topic = itemDescription.getStringProperty(UDColName.Hotkey.toString());
		if (topic != null) {
			hotKeyList.setSelectedItem(topic);
		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String topic = (String) hotKeyList.getSelectedItem();
		itemDescription.setProperty(UDColName.Hotkey.toString(), topic);
		return true;
	}

}
