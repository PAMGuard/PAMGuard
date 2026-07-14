package loggerForms.loggeraudio;

import java.io.Serializable;

public class PlatformSettings implements Serializable {

	private String platform;

	private static final long serialVersionUID = 1L;

	/**
	 * Don't mix into output stream
	 */
	public boolean mute;
	
	/**
	 * output stream mixer channel
	 */
	public int outputChannel = 0;

	public PlatformSettings(String senderName) {
		this.platform = senderName;
		outputChannel = platform.startsWith("P") ? 0 : 1;
	}
	
}
