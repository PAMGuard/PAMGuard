package loggerForms;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import PamView.PamTabPanel;

/**
 * Functions for handling hotkey events. 
 * @author Doug Gillespie
 *
 */
public class FormsHotKeyControl {

	private FormsControl formsControl;
	
	private JComponent dumComponent;

	public FormsHotKeyControl(FormsControl formsControl) {
		this.formsControl = formsControl;
	}

	/*
	 * Clear all previous listeners. 
	 */
	public void removeAllListeners() {
		dumComponent = findComponent();
		if (dumComponent == null) {
			return;
		}
		dumComponent.getActionMap().clear();
		dumComponent.getInputMap().clear();
	}
	
	private JComponent findComponent() {
		PamTabPanel tabPanel = formsControl.getTabPanel();
		if (tabPanel != null) {
			return tabPanel.getPanel();
		}
		return null;
	}

	/**
	 * Add a new listener for a hotkey. 
	 * @param aFD form description for the hotkey
	 * @param hotKey hotkey name, e.g. "F5"
	 */
	public void addKeyListener(FormDescription aFD, String hotKey) {
		dumComponent = findComponent();
		if (dumComponent == null) {
			return;
		}
		String actionName = String.format("Hotkey_%s", hotKey);
		dumComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(hotKey), actionName);
		dumComponent.getActionMap().put(actionName, new HotKeyAction(aFD, hotKey));
	}

	private class HotKeyAction implements Action {
		private FormDescription formDescription;
		private String hotKey;
		public HotKeyAction(FormDescription formDescription, String hotKey) {
			super();
			this.formDescription = formDescription;
			this.hotKey = hotKey;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(String.format("Hotkey %s pressed for form %s", hotKey, formDescription.getFormName()));
		}
		@Override
		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void putValue(String key, Object value) {
			
		}
		@Override
		public void setEnabled(boolean b) {
			
		}
		@Override
		public boolean isEnabled() {
			return true;
		}
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
