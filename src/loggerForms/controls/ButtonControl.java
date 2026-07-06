package loggerForms.controls;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//import com.github.kwhat.jnativehook.GlobalScreen;
//import com.github.kwhat.jnativehook.NativeHookException;
//import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
//import com.github.kwhat.jnativehook.keyboard.NativeKeyAdapter;
//import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
//import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import NMEA.NMEADataUnit;
import PamController.PamController;
import PamView.PamGui;
import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.PropertyDescription;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.actions.ActionException;
import loggerForms.actions.LoggerActions;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.FormList;
import loggerForms.hotkey.HotKeyManager;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkMessage;
import loggerForms.network.LoggerNetworkReceiver;
import loggerForms.network.LoggerNetworkSystem;

public class ButtonControl extends LoggerControl implements LoggerNetworkReceiver {
	
	private JButton button;
	private ActionMap actionMap;
	private InputMap inputMap;
	private int hotKeyId;
	private AWTListener thisListener;
	
	private String udpTopic = null; // just for testing, should probably become an option per control ? 

	public ButtonControl(ControlDescription controlDescription, LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		button = new JButton(controlDescription.getTitle());
		String hint = controlDescription.getItemInformation().getStringProperty(UDColName.Hint.toString());
		String hotKey = controlDescription.getItemInformation().getStringProperty(UDColName.Hotkey.toString());
		String tip = getTip();
		if (tip != null) {
			button.setToolTipText(tip);
		}
		component = new JPanel();
		component.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAction(e);
			}
		});
		if (hotKey != null) {
			setupHotKey(hotKey);
		}
		
		udpTopic = findUDPTopic();
		if (udpTopic != null) {
			setupUDPInput();
		}
	}
	
	private String findUDPTopic() {
		PropertyDescription uinp = loggerForm.getFormDescription().findProperty(PropertyTypes.UDPINPUT);
		if (uinp == null) {
			return null;
		}
		String baseTopic = uinp.getTopic();
		if (baseTopic == null) {
			baseTopic = "";
		}
		else {
			baseTopic = baseTopic.trim() + "/";
		}
		baseTopic += controlDescription.getTitle();
		return baseTopic;
	}
	
	@Override
	public void destroyControl() {
		super.destroyControl();
		if (thisListener != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(thisListener);
		}
		if (udpTopic != null) {
			LoggerNetworkSystem.getManager().unsubscribeTopic(null, this);
		}
	}

	private void setupUDPInput() {
		LoggerNetworkManager netManager = LoggerNetworkSystem.getManager();
		netManager.subsribeTopic(udpTopic, this);
	}

	private void setupHotKey(String hotKey) {
		if (hotKey == null) {
			return;
		}
		hotKeyId = HotKeyManager.getHotKeyId(hotKey);
		if (hotKeyId < 0) {
			return;
		}


		/**
		 * Might want to move this to a single manager with a HashMap so that it's easier to avoid repeats. 
		 */
		Toolkit.getDefaultToolkit().addAWTEventListener(thisListener = new AWTListener(), AWTEvent.KEY_EVENT_MASK);
		
		
		
//		mainFrame.addKeyListener(new HKeyListener());

//		JComponent component = button.getRootPane();
//		if (component == null) {
//			component = (JComponent) mainFrame.getComponent(0);
//		}
//		String actionName = String.format("Hotkey_%s", hotKey);
//		this.inputMap = new InputMap();
//		inputMap.put(KeyStroke.getKeyStroke(hotKey), actionName);
////		component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(hotKey), actionName);
//		this.actionMap = new ActionMap();
//		actionMap.put(actionName, new HotKeyAction(hotKey));
////		// work out the hotkey id. It was probably input as F5 or something. 
//		
//		HotKeyManager hkMan = null;
//		try {
//			hkMan = HotKeyManager.getInstance();
//		} catch (HotKeyException e) {
//			e.printStackTrace();
//			return;
//		}
//		
//		hkMan.registerHotKey(keyId, new HotListener());
		
	}
	
	private class AWTListener implements AWTEventListener {

		@Override
		public void eventDispatched(AWTEvent event) {
			if (event instanceof KeyEvent) {
				KeyEvent keyEvent = (KeyEvent) event;
				if (keyEvent.getKeyCode() == hotKeyId && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
					//						System.out.printf("Hotkey AWT Event %d mods %d %s\n", keyEvent.getKeyCode(),eventType, keyEvent.toString());
					hotKeyPressed();
				}
			}
		}
		
	}

	
	private class HotKeyAction implements Action {
		private String hotKey;
		public HotKeyAction(String hotKey) {
			super();
			this.hotKey = hotKey;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(String.format("Hotkey %s pressed", hotKey));
			hotKeyPressed();
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
	private class HKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println("Key pressed: " + e.getKeyChar());
		}
		
	}
	
//	private class HotListener implements NativeKeyListener {
//
//		@Override
//		public void nativeKeyPressed(NativeKeyEvent nEvent) {
////			System.out.printf("Key press %d (0x%X)\n", nEvent.getKeyCode(), nEvent.getKeyCode());
//			hotKeyPressed();
//		}
//
//		
//	}

	public String getTip() {
		String hint = controlDescription.getItemInformation().getStringProperty(UDColName.Hint.toString());
		String hotKey = controlDescription.getItemInformation().getStringProperty(UDColName.Hotkey.toString());
		if (hint == null && hotKey == null) {
			return null;
		}
		if (hotKey == null) {
			return hint;
		}
		if (hint == null) {
			return hotKey;
		}
		return hint + " (" + hotKey + ")"; 
	}

	public void hotKeyPressed() {
		buttonAction(null);
		bringToFront();
	}

	protected void buttonAction(ActionEvent e) {
		// find the other form, that we're to open. 
		String formName = controlDescription.getTopic();
		FormDescription fd = loggerForm.getFormDescription().getFormsControl().findFormDescription(formName);
		
		if (fd == null) {
			System.out.printf("Button form %s - %s unable to find form %s to open\n", loggerForm.getFormDescription().getFormName(),
					controlDescription.getTitle(), formName);
			return;
		}
		
		/*
		 *  open the other form - depending on it's type if it's subtabs, or popup, then open a new instance, 
		 *  otherwise, if it's normal, just go to that form. 
		 */
		LoggerForm form = fd.activateForm();
		//then set the appropriate field within the opened form. 
		String toSetData = controlDescription.getPostTitle();
		LoggerControl toSetCtrl = form.findInputControl(controlDescription.getControlOnSubform());
		if (form != null && toSetCtrl != null) {
			toSetCtrl.setData(toSetData);
		}
		
		runLoggerActions();
	}
	
	/**
	 * Run logger actions. Any valid actions is an ACTION control that comes after this button in the form description, but 
	 * before the next button. 
	 */
	private void runLoggerActions() {
		FormDescription fd = loggerForm.getFormDescription();
		FormList<ControlDescription> cdList = fd.getControlDescriptions();
		int thisInd = cdList.indexOf(controlDescription);
		if (thisInd < 0) {
			return;
		}
		int nOk = 0, nErr = 0;
		for (int i = thisInd + 1; i < cdList.size(); i++) {
			ControlDescription cd = cdList.get(i);
			if (cd.getEType() == ControlTypes.ACTION) {
				boolean ok = runLoggerAction(cd);
				if (ok) {
					nOk++;
				}
				else {
					nErr++;
				}
			}
			else {
				// get out as soon as it's not a ACTION, so we only do the actions immediately after the button 
				break;
			}
		}
	}

	private boolean runLoggerAction(ControlDescription cd) {
		if (cd == null) {
			return false;
		}
		String topic = cd.getTopic();
		if (topic == null) {
			System.out.println("Unnamed Logger action in button form " + getLoggerForm().getFormDescription().getFormName());
			return false;
		}
		boolean ran = false;
		try {
			ran = LoggerActions.getInstance().runAction(topic, loggerForm, this);
		} catch (ActionException e) {
			e.printStackTrace();
			return false;
		}
		return ran;
	}

	/**
	 * Bring PAMGuard to the front of the display, and make sure it's not minimized. 
	 */
	private void bringToFront() {
		Window win = PamGui.findComponentWindow(button);
		if (win instanceof JFrame) {
			JFrame frame = (JFrame) win;
			int state = frame.getExtendedState();
			state &= ~JFrame.ICONIFIED;
			frame.setExtendedState(state);
			frame.setAlwaysOnTop(true);
			win.setVisible(true);
			win.toFront();
			win.requestFocus();
			frame.setAlwaysOnTop(false);
		}
		else if (win != null) {
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					System.out.println("Move to front: " + win.toString());
					win.setVisible(true);
					win.toFront();
					win.requestFocus();
				}
			});
//			if (win.is)
		}
	}

	@Override
	public String getDataError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefault() {
		// TODO Auto-generated method stub

	}

	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newMessage(LoggerNetworkMessage message) {
		System.out.println("Logger network message received: " + message.toString());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				hotKeyPressed();
			}
		});
		return true;
	}

}
