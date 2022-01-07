package PamView;

import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Class to add to a window in order to intercept and interpret Ctrl key
 * strokes. Used in components such as the Click BT display.
 * @author Doug Gillespie
 *
 */
public class CtrlKeyManager extends KeyAdapter {

	private ArrayList<CtrlKeyListener> ctrlKeyListeners = new ArrayList<CtrlKeyListener>();

	@Override
	public void keyPressed(KeyEvent k) {
		int mods = k.getModifiers();
		if (mods == 2) {
			ctrlKeyAction(k);
		}
	}

	private void ctrlKeyAction(KeyEvent k) {
//		System.out.println("Key press " + k.getID() + ", " + k.getKeyChar() + ", " + k.getKeyCode());
		CtrlKeyListener ctrlKeyListener = findCtrlKeyListener(k.getKeyCode());
		if (ctrlKeyListener != null) {
			ctrlKeyListener.actionListener.actionPerformed(null);
		}
	}
	
	public void addCtrlKeyListener(char key, ActionListener actionListener) {
		CtrlKeyListener exVal = findCtrlKeyListener(Character.toUpperCase(key));
		if (exVal != null) {
			ctrlKeyListeners.remove(exVal);
		}
		ctrlKeyListeners.add(new CtrlKeyListener(key, actionListener));
	}
	
	private CtrlKeyListener findCtrlKeyListener(int key) {
		CtrlKeyListener ckl;
//		System.out.println(key);
		for (int i = 0; i < ctrlKeyListeners.size(); i++) {
			ckl = ctrlKeyListeners.get(i);
			if (ckl.ascChar == key) {
				return ckl;
			}
		}
		return null;
	}
	
	private class CtrlKeyListener {
		char key;
		ActionListener actionListener;
		int ascChar;
		
		/**
		 * @param key
		 * @param actionListener
		 */
		public CtrlKeyListener(char key, ActionListener actionListener) {
			super();
			this.key = key;
			this.actionListener = actionListener;
			ascChar = Character.toUpperCase(key);
//			ascChar = new Character(key).
//			Character c = Character.valueOf(key);
//			
//			System.out.println("Set command for ascii " + ascChar + " " + c.charValue() + ", " + Character.getNumericValue(key));
		}
		
	}

	/**
	 * Clear all ctrl key listeners from the list. 
	 */
	public void clearAll() {
		ctrlKeyListeners.clear();
	}
}
