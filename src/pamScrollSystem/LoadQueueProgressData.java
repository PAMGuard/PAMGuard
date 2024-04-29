package pamScrollSystem;

import javafx.scene.control.ProgressIndicator;
import pamViewFX.pamTask.PamTaskUpdate;

public class LoadQueueProgressData extends PamTaskUpdate {

	private int totalStreams;
	
	private int iStream;
	
	private String storeType;
	
	private String streamName;
	private long loadStart;
	private long loadEnd;
	private long loadCurrent;
	private int nLoaded;
	private int state;
	
	public static final int STATE_LOADING = 0;
	public static final int STATE_LINKINGSUBTABLE = 1;
	public static final int STATE_DONE = 2;

	public LoadQueueProgressData(String storeType, String streamName,
			int totalStreams, int stream, int state, long loadStart, long loadEnd, long loadCurrent, int nLoaded) {
		super();
		this.storeType = storeType;
		this.streamName = streamName;
		this.totalStreams = totalStreams;
		this.iStream = stream;
		this.state = state;
		this.loadStart = loadStart;
		this.loadEnd = loadEnd;
		this.loadCurrent = loadCurrent;
		this.nLoaded = nLoaded;
		setDualProgressUpdate(true);
	}
	
	public LoadQueueProgressData(int status) {
		super();
		this.setStatus(PamTaskUpdate.STATUS_DONE);
		setDualProgressUpdate(true);
	}


	/**
	 * @return the totalStreams
	 */
	public int getTotalStreams() {
		return totalStreams;
	}

	/**
	 * @return the iStream
	 */
	public int getIStream() {
		return iStream;
	}

	/**
	 * @return the storeType
	 */
	public String getStoreType() {
		return storeType;
	}

	/**
	 * @return the streamName
	 */
	public String getStreamName() {
		return streamName;
	}

	/**
	 * @return the iStream
	 */
	public int getiStream() {
		return iStream;
	}

	/**
	 * @return the loadStart
	 */
	public long getLoadStart() {
		return loadStart;
	}

	/**
	 * @return the loadEnd
	 */
	public long getLoadEnd() {
		return loadEnd;
	}

	/**
	 * @return the loadCurrent
	 */
	public long getLoadCurrent() {
		return loadCurrent;
	}

	/**
	 * @return the nLoaded
	 */
	public int getnLoaded() {
		return nLoaded;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	} 
	
	@Override
	public String getName() {
		return "Load PAM Data ";
	}

	@Override
	public double getProgress() {		
//		System.out.println( getStreamName() + " progress: " + iStream + " of " + totalStreams);
		if (getStreamName() != null) {
			return (double )iStream/ (double) totalStreams;
		}
		else return ProgressIndicator.INDETERMINATE_PROGRESS;
	} 

	@Override
	public String getProgressString() {

		if (getState() == LoadQueueProgressData.STATE_LINKINGSUBTABLE) {
			return String.format("Linking subtable data %d of %d", 
					getIStream(), getTotalStreams());
		}
		else {
			if (getStreamName() != null) {
			return String.format("Loading data block %d of %d", 
					getIStream(), getTotalStreams());
			}
			else return ""; 
		}
	} 
	
	@Override
	public double getProgress2() {	
//		System.out.println( getStreamName() + " progress2: " + getLoadStart() );
		long interval =getLoadEnd() - getLoadStart();
		if (interval <= 0) {
			return ProgressIndicator.INDETERMINATE_PROGRESS; 
		}
		else {
			long done =getLoadCurrent() - getLoadStart();
//			System.out.println( getStreamName() + " progress2: " + done + " " + interval + " start " + getLoadStart() + " end " + getLoadEnd())
			return ((double) done )/ interval;
		}
	}
	
	/**
	 * If two updates are available then this is used to return the fine progress update message.
	 * @return
	 */
	public String getProgressString2(){
		if (getState() == LoadQueueProgressData.STATE_LINKINGSUBTABLE) {
			return "Linking " + getStreamName();
		}
		else {
			return getStreamName();
		}
	}
	
	
}
