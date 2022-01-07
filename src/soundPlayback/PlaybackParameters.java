package soundPlayback;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Parameters controlling sound playback
 * 
 * @author Doug Gillespie
 *
 */
public class PlaybackParameters implements Cloneable, Serializable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	static public final int SIDEBAR_SHOW_GAIN = 0x1;
	static public final int SIDEBAR_SHOW_SPEED = 0x2;
	static public final int SIDEBAR_SHOW_ENVMIX = 0x4;
	static public final int SIDEBAR_SHOW_FILTER = 0x8;
	
	public static final int SIDEBAR_DEFAULT = SIDEBAR_SHOW_GAIN | SIDEBAR_SHOW_SPEED | SIDEBAR_SHOW_FILTER;
	
	/**
	 * source or raw audio data. 
	 */
	public int dataSource;
	
	/**
	 * channels to play back
	 */
	public int channelBitmap;
	
	/**
	 * number of sound card line - only used when playing back wav files, etc. Otherwise, 
	 * sound playback will be through the device that is acquiring data. 
	 */
	public int deviceNumber = 0;
	
	/**
	 * Device type only used with file playback, since for all real time 
	 * plyback, playback must be through the device aquiring data
	 */
	public int deviceType = 0;
	
	public boolean defaultSampleRate = true;
	
	/**
	 * This is the output sample rate. In real time systems it will 
	 * often be impossible to set this, however if it is set, then 
	 * the decimator will automatically cut in and sort everything out. 
	 */
	private float playbackRate = PlaybackControl.DEFAULT_OUTPUT_RATE;
	
	/**
	 * This is the speed that it will play at relative to real time.
	 * N.B. For a real time system the speed must be 1.0, though the 
	 * playback rate can be anything compatible with the device, and data
	 * will be decimated or upsampled as necessary.
	 */
	private double logPlaybackSpeed = 0.;
	
	public int playbackGain = 0;
	
	/**
	 * High pass filter. Expressed as a fraction of sample rate (i.e. a number between 0 and 0.5) 
	 */
	private double hpFilter = 0;
	
	private Integer sidebarShow = SIDEBAR_DEFAULT;

	@Override
	public PlaybackParameters clone() {

		try {
			return (PlaybackParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * The log2 of the playback speed. Speed being how much the original
	 * data are sped up by. 
	 * @return log2 of playback speed. 
	 */
	public double getLogPlaybackSpeed() {
		return logPlaybackSpeed;
	}
	
	/**
	 * Set the log2 of the playback speed (how many times real time). 
	 * @param logSpeed
	 */
	public void setLogPlaybackSpeed(double logSpeed) {
		this.logPlaybackSpeed = logSpeed;
	}
	
	/**
	 * Set the playback speed (how many time real time)
	 * @param playbackSpeed
	 */
	public void setPlaybackSpeed(double playbackSpeed) {
		this.logPlaybackSpeed = Math.log(playbackSpeed)/Math.log(2.);
	}
	
	/**
	 * Get the playback speed (how many times real time)
	 * @return playback speed
	 */
	public double getPlaybackSpeed() {
		return Math.pow(2., logPlaybackSpeed);
	}
	
	/**
	 * Get the actual playback rate in samples per second.
	 * @return actual playback rate. 
	 */
	public float getPlaybackRate() {
		return this.playbackRate;
	}
	
	public void setPlaybackRate(float playbackRate) {
		this.playbackRate = playbackRate;
	}

	/**
	 * @return the hpFilter
	 */
	public double getHpFilter() {
		return hpFilter;
	}

	/**
	 * @param hpFilter the hpFilter to set
	 */
	public void setHpFilter(double hpFilter) {
		this.hpFilter = hpFilter;
	}

	/**
	 * @return the sidebarShow
	 */
	public int getSidebarShow() {
		if (sidebarShow == null) {
			sidebarShow = SIDEBAR_DEFAULT;
		}
		return sidebarShow;
	}

	/**
	 * @param sidebarShow the sidebarShow to set
	 */
	public void setSidebarShow(int sidebarShow) {
		this.sidebarShow = sidebarShow;
	}

	public boolean isSideBarShow(int sideBarType) {
		int show = getSidebarShow();
		return ((show & sideBarType) != 0);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
