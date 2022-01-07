package soundPlayback;

/**
 * Receives notificaitons from the playback system detailing how
 * far playback has got in viewer mode. 
 * @author Doug Gillespie
 *
 */
public interface PlaybackProgressMonitor {

	static public final int PLAY_START = 1;
	static public final int PLAY_END = 2;
	/**
	 * Notify progress during playback in viewer mode.<p>
	 * The times are given in two ways. As an absolute time
	 * in milliseconds and as a percentage.  
	 * @param timeMillis milliseconds
	 * @param percent percentage progress
	 */
	void setProgress(int channels, long timeMillis, double percent);
	
	/**
	 * Notify playback status. 
	 * Valid values are PLAY_START and PLAY_END
	 * @param status playback status
	 */
	void setStatus(int status);
	
}
