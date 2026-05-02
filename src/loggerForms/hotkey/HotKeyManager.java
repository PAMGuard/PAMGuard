package loggerForms.hotkey;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class HotKeyManager {
	
	private static HotKeyManager singleInstance;
	
	private HashMap<Integer, NativeKeyListener> mappedKeys;

	private	HotKeyManager() {
		mappedKeys = new HashMap<>();
	}
	
	public static HotKeyManager getInstance() throws HotKeyException {
		if (singleInstance== null) {
			singleInstance = new HotKeyManager();
			try {
				singleInstance.init();
			} catch (NativeHookException e) {
				throw new HotKeyException((e.getMessage()));
			}
		}
		return singleInstance;
	}

	private void init() throws NativeHookException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new NativeListener());
        GlobalScreen.setEventDispatcher(new SwingDispatchService());
	}
	
	public void registerHotKey(int keyCode, NativeKeyListener keyListener) {
		mappedKeys.put(keyCode, keyListener);
	}

	private class NativeListener implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent nEvent) {
			NativeKeyListener listener = mappedKeys.get(nEvent.getRawCode());
			if (listener != null) {
				listener.nativeKeyPressed(nEvent);
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent nEvent) {
			NativeKeyListener listener = mappedKeys.get(nEvent.getRawCode());
			if (listener != null) {
				listener.nativeKeyReleased(nEvent);
			}
		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent nEvent) {
			NativeKeyListener listener = mappedKeys.get(nEvent.getRawCode());
			if (listener != null) {
				listener.nativeKeyTyped(nEvent);
			}
		}
		
	}
	
	/**
	 * Convert a string is along the lines of F15 or F5 into a Swing key identifier. 
	 * @param hkString
	 * @return
	 */
	public static int getHotKeyId(String hkString) {
		if (hkString == null || hkString.length() < 1) {
			System.out.println("Invalid hotkey id " + hkString);
			return -1;
		}
		String copy = new String(hkString);
		Character c1 = copy.charAt(0);
		if (c1 == 'F') {
			copy = copy.substring(1);
		}
		while (copy.length() > 0) {
			c1 = copy.charAt(0);
			if (Character.isDigit(c1)) {
				break;
			}
			copy = copy.substring(1);
		}
		int fnum = -1;
		try {
			fnum = Integer.valueOf(copy);
		}
		catch (NumberFormatException e) {
			System.out.println("Invalid hotkey id " + hkString);
		}
		int key = KeyEvent.VK_F1 - 1 + fnum;
		return key;
		
	}

}
