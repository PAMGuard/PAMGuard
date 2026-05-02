package loggerForms.hotkey;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class HotKeyException extends Exception {

	private static final long serialVersionUID = 1L;

	public HotKeyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HotKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HotKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public HotKeyException(String message) {
		super(message);
	}

	public HotKeyException(Throwable cause) {
		super(cause);
	}

}
