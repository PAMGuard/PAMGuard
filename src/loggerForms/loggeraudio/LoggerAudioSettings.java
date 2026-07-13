package loggerForms.loggeraudio;

import java.io.Serializable;
import java.util.HashMap;

public class LoggerAudioSettings implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, PlatformSettings> platformAudioSettings = new HashMap();
	
	public int outputDevice = 0;
	
	public PlatformSettings getStreamSettings(String senderName) {
		PlatformSettings ss = platformAudioSettings.get(senderName);
		if (ss == null) {
			ss = new PlatformSettings(senderName);
			platformAudioSettings.put(senderName, ss);
		}
		
		return ss;
	}

}
