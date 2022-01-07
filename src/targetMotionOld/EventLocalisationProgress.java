package targetMotionOld;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
public class EventLocalisationProgress {

	public static final int LOADING_EVENT_DATA = 1;
	public static final int LOADED_EVENT_DATA = 2;
	public static final int GOT_RESULTS = 3;
	public static final int WAITING = 4;
	public static final int DONE = 5;

	public int progressType;
	public int eventIndex;

	/**
	 * @param progressType
	 */
	public EventLocalisationProgress(int progressType, int eventIndex) {
		super();
		this.progressType = progressType;
		this.eventIndex = eventIndex;
	}

	@Override
	public String toString() {
		return String.format("%s Event %d", getStatusString(), eventIndex);
	}

	private String getStatusString() {
		switch(progressType) {
		case LOADING_EVENT_DATA:
			return "Loading";
		case LOADED_EVENT_DATA:
			return "Loaded";
		case GOT_RESULTS:
			return "Have results";
		default:
			return "Unknown state";
		}
	}



}
