package loggerForms;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.logging.LogManager;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import PamView.PamTabPanel;
import PamView.panel.PamPanel;

/**
 * The top-level Logger GUI element -- a Tab in the swing GUI.
 * 
 */
public class FormsTabPanel implements PamTabPanel {
	
	private FormsControl formsControl;
	
	private JPanel mainPanel;
	
	private LoggerTabbedPane mainTabbedPane;

	public LoggerTabbedPane getMainTabbedPane() {
		return mainTabbedPane;
	}

	public FormsTabPanel(FormsControl formsControl) {
		super();
		this.formsControl = formsControl;
		mainPanel = new FormsPanel();
		
		// Application-wide hotkey manager
//		keyManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
//		keyManager.addKeyEventDispatcher(new LoggerKeyEventDispatcher());
		
		/** Global (OS-level) hotkey manager: 
		 * jnativehook supported systems: Windows, X11, MacOS
		 * 
		 */
		try {
			LogManager.getLogManager().reset();
			GlobalScreen.setEventDispatcher(new SwingDispatchService());
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new GlobalKeyListenerExample());
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
		}
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		JMenu menu = new JMenu("");
		JMenuItem plotMenu = new JMenuItem( " plot options ...");
		menu.add(plotMenu);
		return menu;
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void createForms() {
		int nForms = formsControl.getNumFormDescriptions();
		FormDescription fD;
		JComponent formComponent;
		
		for (int i = 0; i < nForms; i++) {
			fD = formsControl.getFormDescription(i);
			formComponent = fD.getTabComponent();
			if (formComponent != null) {
				mainTabbedPane.addTab(fD.getFormTabName(), null, formComponent, fD.getTabToolTip());
			}
		}
	}
	
	/**
	 * Replace a form within the tab control. 
	 * @param formDescription
	 */
	private void replaceForm(FormDescription formDescription) {
		
	}

	/**
	 * Set the name of a tab using the default name. These can change dynamically as subforms 
	 * are added and removed. 
	 * @param formDescription 
	 */
	public void setTabName(FormDescription formDescription) {
		setTabName(formDescription, null);
	}
	
	/**
	 * Set the name of a tab. These can change dynamically as subforms 
	 * are added and removed. 
	 * @param formDescription
	 * @param newName
	 */
	public void setTabName(FormDescription formDescription, String newName) {
		if (newName == null) {
			newName = formDescription.getFormTabName();
		}
		int ind = mainTabbedPane.findTabIndex(formDescription);
		if (ind > 0) {
			mainTabbedPane.setTitleAt(ind, newName);
		}
	}

	class FormsPanel extends PamPanel {

		public FormsPanel() {
			super();
			setLayout(new BorderLayout());
			
			
			add(BorderLayout.CENTER, mainTabbedPane = new LoggerTabbedPane(formsControl));
		}
		
	}

	/**
	 * Called when forms are to be regenerated. <p>
	 * will remove all tabbed panes from the display. 
	 */
	public void removeAllForms() {
		mainTabbedPane.removeAll();
	}

	/**
	 * This is an java.awt key listener that will process all keystrokes 
	 * so long as some portion of Pamguard is focused.
	 * This class has been superseded by global hotkey manager, 
	 * GlobalKeyListenerExample, but I have left the code here as an example of 
	 * application-level hotkeys.
	 * @author brian_mil
	 * 
	 */
	private class LoggerKeyEventDispatcher implements KeyEventDispatcher {
		/**TODO: Make this a global (OS-level) key-listener so that forms can be 
		 * 		 activated even if Pamguard is not in-focus. (e.g. see libary 
		 * 		 jnativehook: https://github.com/kwhat/jnativehook for an example of 
		 * 		 possible global keylistener.
		 */

		/**
		 * This always returns false in case the keyEvent is needed elsewhere, 
		 * e.g. if multiple actions have been assigned the same hotkey
		 */
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			int nForms = formsControl.getNumFormDescriptions();
			FormDescription fD;
			JComponent formComponent;

			if(e.getID()==KeyEvent.KEY_PRESSED){
				KeyStroke keyPressed = KeyStroke.getKeyStroke(e.getKeyCode(),e.getModifiers());

				for (int i = 0; i < nForms; i++) {
					fD = formsControl.getFormDescription(i);

					//					System.out.println(String.format("nativekey %s ",keyPressed));
					// Loop over all forms to see if the hotkey matches
					String formHotkey = fD.getHOTKEY();
					if (formHotkey == null) continue;
					KeyStroke k = KeyStroke.getKeyStroke(formHotkey);
					if (k==keyPressed){
						formsControl.getGuiFrame().setExtendedState(Frame.NORMAL);
						fD.processHotkeyPress();
						getMainTabbedPane().setSelectedIndex(i);

						//TODO: Focus first input component within the tab
					}
				}
			}
			return false; 		
		}
	}


	/**
	 * GlobalKeyListenerExample uses the jnativehook library to provide global
	 * (OS-level) keyboard shortcuts for Logger-Forms. This means that hotkeys, such
	 * as F5-F12 can be used to activate a form even if Pamguard is not the 
	 * top-level window. 
	 * 
	 * jnativehook supports Windows, X11, and MacOS hotkeys. For more info see 
	 * jnativehook development site at https://github.com/kwhat/jnativehook
	 * 
	 * @author brian_mil
	 *
	 */
	private class GlobalKeyListenerExample implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			int nForms = formsControl.getNumFormDescriptions();
			FormDescription fD;
			JComponent formComponent;

			KeyStroke keyPressed = KeyStroke.getKeyStroke(
					e.getRawCode(),e.getModifiers());

			for (int i = 0; i < nForms; i++) {
				fD = formsControl.getFormDescription(i);


				// System.out.println(String.format("nativekey %s ",keyPressed));
				// Loop over all forms to see if the hotkey matches
				String formHotkey = fD.getHOTKEY();
				if (formHotkey == null) continue;
				KeyStroke k = KeyStroke.getKeyStroke(formHotkey);
				if (k==keyPressed){
					formsControl.getGuiFrame().setExtendedState(Frame.NORMAL);
					fD.processHotkeyPress();
					getMainTabbedPane().setSelectedIndex(i);

					//TODO: Focus first input component within the tab
				}
			}

		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent arg0) {
			// TODO Auto-generated method stub

		}


	}
}
