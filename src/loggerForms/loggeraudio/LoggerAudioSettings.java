package loggerForms.loggeraudio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import Acquisition.SoundCardSystem;

public class LoggerAudioSettings implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, PlatformSettings> platformAudioSettings = new HashMap();
	
	private int outputDeviceIndex = 0;
	public String outputDeviceName = null;
	
	public int bufferSeconds = 10; // time before button press. 
	public int recordSeconds = 60; // time after button press
	
	public String outputFolder = null;
	public boolean outputSubFolders = true;
	
	public PlatformSettings getStreamSettings(String senderName) {
		PlatformSettings ss = platformAudioSettings.get(senderName);
		if (ss == null) {
			ss = new PlatformSettings(senderName);
			platformAudioSettings.put(senderName, ss);
		}
		
		return ss;
	}
	
	/**
	 * Find a mixer, ideally based on it's name, if not on the last index. 
	 * @return try really hard to return something !
	 */
	public Info findMixer() {
		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		if (mixers == null || mixers.size() == 0) {
			return null;
		}
		int ind = Math.min(outputDeviceIndex, mixers.size()-1);
		if (outputDeviceName == null) {
			return mixers.get(ind);
		}
		int i = 0;
		for (Info mi : mixers) {
			if (mi.getName().equals(outputDeviceName)) {
				outputDeviceIndex = i;
				return mi;
			}
			i++;
		}
		return mixers.get(ind);
	}
	
	/**
	 * Get all the current platform names. 
	 * @return set of current platform names. 
	 */
	public Set<String> getPlatformNames() {
		return platformAudioSettings.keySet();
	}

}
