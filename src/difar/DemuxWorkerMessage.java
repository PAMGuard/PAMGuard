package difar;

import Array.ArrayManager;
import Array.StreamerDataUnit;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;

public class DemuxWorkerMessage {

	public DifarDataUnit difarDataUnit;
	
	/**
	 * status/part of the process in operation when message sent;
	 */
	public int status;
	
	public long elapsedMillis;

	private double totalProgress = -1;
	
	private boolean lock75, lock15;
		
	public static final int STATUS_START = 0;
	public static final int STATUS_DONEDEMUX = 1;
	public static final int STATUS_STARTDIFARCALC = 2;
	public static final int STATUS_DONEDIFARCALC = 3;
	public static final int STATUS_DONEALL = 4;
	public static final int STATUS_INDEMUXCALC = 5;
	public static final int STATUS_DIFAR_DONE_POWER = 6;
	public static final int STATUS_DIFAR_DONE_CROSS = 7;
	public static final int STATUS_DIFAR_ANGLES = 8;
	public static final int STATUS_AUTOSAVEPENDING = 9;
	public static final int STATUS_AUTOSAVEINTERRUPTED = 10;
	public static final int STATUS_SAVED = 11;
	public static final int STATUS_DELETED = 12;
	
	public DemuxWorkerMessage(DifarDataUnit difarDataUnit, int status, long millis) {
		super();
		this.difarDataUnit = difarDataUnit;
		this.status = status;
		this.elapsedMillis = millis;
	}
	
	/**
	 * 
	 * @param difarDataUnit
	 * @param status
	 * @param percentProgress
	 */
	public DemuxWorkerMessage(DifarDataUnit difarDataUnit, int status, long millis, double percentProgress) {
		this.difarDataUnit = difarDataUnit;
		this.status = status;
		this.elapsedMillis = millis;
		totalProgress = getOverallProgress(status, percentProgress);
	}

	public DemuxWorkerMessage(DifarDataUnit difarDataUnit, int status, long millis, double percentProgress, Boolean lock75, Boolean lock15) {
		this.difarDataUnit = difarDataUnit;
		this.status = status;
		this.elapsedMillis = millis;
		totalProgress = getOverallProgress(status, percentProgress);
		if (lock75 != null) {
			this.lock75 = lock75;
		}
		if (lock15 != null) {
			this.lock15 = lock15;
		}
	}

	/** demux and difargram report thier own progresses as %, need tocombine this into  
	 *  a single overall percentage. 
	 * 
	 * @param statuss - current operation
	 * @param percentProgress of current operation
	 * @return
	 */
	private double getOverallProgress(int statuss, double percentProgress) {
		// demux and difargram report thier own progresses as %, need tocombine this into 
		// a single overall percentage. 
		totalProgress = percentProgress * 100;
		return totalProgress;
	}

	public String getMessageString() {
		if (difarDataUnit == null) {
			return null;
		}
		String species = difarDataUnit.getSpeciesCode();
		String streamerName = "";
		int chan = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getPreceedingUnit(difarDataUnit.getTimeMilliseconds(), 1<<chan);
		if (sdu != null){
			streamerName = sdu.getStreamerData().getStreamerName();
		}
		String dateTime = PamCalendar.formatTime(difarDataUnit.getTimeMilliseconds()); 
		return String.format("%s, sb %s, %s, %3.1f dB, %3.0f" + LatLong.deg,
				dateTime, streamerName, species, difarDataUnit.getCalculatedAmlitudeDB(), difarDataUnit.getTrueAngle()  
				);
	}

	public Object getStatusString() {
		switch (status) {
		case STATUS_START:
			return "Start Demultiplex";
		case STATUS_DONEDEMUX:
			return "Demultiplex complete";
		case STATUS_STARTDIFARCALC:
			return "Start DIFAR calc'";
		case STATUS_DONEDIFARCALC:
			return "DIFAR calc' complete";
		case STATUS_DONEALL:
			return "Processing complete";
		case STATUS_DIFAR_ANGLES:
			return "Creating DIFARgram";
		case STATUS_DIFAR_DONE_POWER:
			return "Creating DIFARgram";
		case STATUS_DIFAR_DONE_CROSS:
			return "Creating DIFARgram";
		case STATUS_INDEMUXCALC:
			return "Demultiplexing";
		case STATUS_AUTOSAVEPENDING:
			return "Autosave pending ";
		case STATUS_AUTOSAVEINTERRUPTED:
			return "Autosave interrupted";
		case STATUS_SAVED:
			return "Saved";
		case STATUS_DELETED:
			return "Deleted";
		}
		return null;
	}

	/**
	 * @return the totalProgress
	 */
	public double getTotalProgress() {
		return totalProgress;
	}

	/**
	 * @return the lock75
	 */
	public boolean isLock75() {
		return lock75;
	}

	/**
	 * @return the lock15
	 */
	public boolean isLock15() {
		return lock15;
	}
	
	
}
