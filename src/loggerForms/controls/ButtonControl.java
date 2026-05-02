package loggerForms.controls;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyAdapter;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import NMEA.NMEADataUnit;
import PamView.PamGui;
import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.hotkey.HotKeyException;
import loggerForms.hotkey.HotKeyManager;

public class ButtonControl extends LoggerControl {
	
	private JButton button;

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
	}
	
	private void setupHotKey(String hotKey) {
		if (hotKey == null) {
			return;
		}
		int keyId = HotKeyManager.getHotKeyId(hotKey);
		if (keyId < 0) {
			return;
		}
		// work out the hotkey id. It was probably input as F5 or something. 
		
		HotKeyManager hkMan = null;
		try {
			hkMan = HotKeyManager.getInstance();
		} catch (HotKeyException e) {
			e.printStackTrace();
			return;
		}
		
		hkMan.registerHotKey(keyId, new HotListener());
		
	}
	
	private class HotListener implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent nEvent) {
//			System.out.printf("Key press %d (0x%X)\n", nEvent.getKeyCode(), nEvent.getKeyCode());
			hotKeyPressed();
		}

		
	}

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

}
