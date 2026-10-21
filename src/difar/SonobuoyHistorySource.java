package difar;

import java.util.ArrayList;
import java.util.List;

import Array.ArrayManager;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import GPS.GpsData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import annotation.DataAnnotation;
import annotation.timestamp.TimestampAnnotation;

/**
 * Keeps a sonobuoy history in step with the streamer records.
 * <p>
 * The array's streamer data block holds a record for each buoy deployment and
 * calibration, in normal and viewer mode. This reads those records into plain
 * sonobuoy records, and rebuilds the history the next time it is asked after
 * anything has changed. Nothing is written back.
 * <p>
 * It learns of changes in three ways, so that it does not depend on any one of
 * them: updates announced by the data block, the viewer telling it that data
 * has loaded, and a change in the number of records. The last is needed
 * because in viewer mode the block does not announce new records.
 * <p>
 * DIFAR uses channel N for streamer N, so a record's channel is its streamer
 * index.
 */
public class SonobuoyHistorySource extends PamObserverAdapter {

	private final String endTimeAnnotationName;

	private final SonobuoyHistory history = new SonobuoyHistory();

	private StreamerDataBlock streamerDataBlock;

	private volatile boolean stale = true;

	private int recordsAtLastBuild = -1;

	/**
	 * @param endTimeAnnotationName name of the annotation holding a buoy's end
	 * time, or null if buoys have no end times.
	 */
	public SonobuoyHistorySource(String endTimeAnnotationName) {
		this.endTimeAnnotationName = endTimeAnnotationName;
	}

	/**
	 * Start watching the streamer records. Safe to call more than once.
	 */
	public synchronized void connect() {
		StreamerDataBlock block = ArrayManager.getArrayManager().getStreamerDatabBlock();
		if (block == streamerDataBlock) {
			return;
		}
		if (streamerDataBlock != null) {
			streamerDataBlock.deleteObserver(this);
		}
		streamerDataBlock = block;
		if (block != null) {
			block.addObserver(this, false);
		}
		markStale();
	}

	/**
	 * Rebuild the history the next time it is asked for.
	 */
	public void markStale() {
		stale = true;
	}

	/**
	 * @return the history, rebuilt first if the records have changed.
	 */
	public synchronized SonobuoyHistory getHistory() {
		if (streamerDataBlock == null) {
			connect();
		}
		if (streamerDataBlock != null
				&& (stale || streamerDataBlock.getUnitsCount() != recordsAtLastBuild)) {
			rebuild();
		}
		return history;
	}

	/**
	 * Read every streamer record into the history. The stale flag is cleared
	 * before reading, so a change made during the read triggers another rebuild.
	 */
	private void rebuild() {
		stale = false;
		ArrayList<StreamerDataUnit> units = streamerDataBlock.getDataCopy();
		List<SonobuoyRecord> records = new ArrayList<>(units.size());
		for (StreamerDataUnit unit : units) {
			SonobuoyRecord record = toRecord(unit, endTimeAnnotationName);
			if (record != null) {
				records.add(record);
			}
		}
		history.setRecords(records);
		recordsAtLastBuild = units.size();
	}

	/**
	 * Turn one streamer record into a sonobuoy record.
	 * @param unit the streamer record.
	 * @param endTimeAnnotationName name of the end time annotation, or null.
	 * @return the sonobuoy record, or null if the unit has no streamer.
	 */
	static SonobuoyRecord toRecord(StreamerDataUnit unit, String endTimeAnnotationName) {
		Streamer streamer = unit.getStreamerData();
		if (streamer == null) {
			return null;
		}
		Long endTime = null;
		if (endTimeAnnotationName != null) {
			DataAnnotation annotation = unit.findDataAnnotation(TimestampAnnotation.class, endTimeAnnotationName);
			if (annotation instanceof TimestampAnnotation) {
				endTime = ((TimestampAnnotation) annotation).getTimestamp();
			}
		}
		Double latitude = null, longitude = null;
		GpsData position = unit.getGpsData();
		if (position != null) {
			latitude = position.getLatitude();
			longitude = position.getLongitude();
		}
		return new SonobuoyRecord(streamer.getStreamerIndex(), unit.getTimeMilliseconds(), endTime,
				streamer.getStreamerName(), latitude, longitude, streamer.getHeading(),
				streamer.getZ(), unit.getUID(), unit.getDatabaseIndex() > 0);
	}

	@Override
	public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
		markStale();
	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		markStale();
	}

	@Override
	public String getObserverName() {
		return "DIFAR sonobuoy history";
	}
}
