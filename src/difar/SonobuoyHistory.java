package difar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Which sonobuoy record was in force on each channel at any time.
 * <p>
 * This answers one question, for normal and viewer mode alike: given a channel
 * and a time, which record applies? A record applies from its own time until
 * the next record on the same channel, or until the buoy's end time. Before a
 * channel's first record, or after its buoy has ended, there is no record, and
 * the answer is null. A later record is never returned in place of an earlier
 * one.
 * <p>
 * The history holds no link to the database or the array. It is given the
 * full set of records and replaces what it held before. Lookups do not change
 * anything, so any number of callers can use it, in any order, on any thread.
 */
public class SonobuoyHistory {

	/**
	 * Records for each channel, keyed by the time they come into force. Replaced
	 * as a whole, never changed, so readers always see a complete set.
	 */
	private volatile Map<Integer, NavigableMap<Long, SonobuoyRecord>> recordsByChannel =
			Collections.emptyMap();

	/**
	 * Replace every record with a new set.
	 * <p>
	 * The records may be in any order. Where a channel has two records at the
	 * same time, the later one in the collection is kept. This removes the
	 * duplicates that the viewer can load.
	 * @param records the full set of records. Null entries are ignored.
	 */
	public void setRecords(Collection<SonobuoyRecord> records) {
		Map<Integer, NavigableMap<Long, SonobuoyRecord>> newRecords = new HashMap<>();
		if (records != null) {
			for (SonobuoyRecord record : records) {
				if (record == null) {
					continue;
				}
				newRecords.computeIfAbsent(record.getChannel(), c -> new TreeMap<>())
						.put(record.getTimeMillis(), record);
			}
		}
		for (Map.Entry<Integer, NavigableMap<Long, SonobuoyRecord>> entry : newRecords.entrySet()) {
			entry.setValue(Collections.unmodifiableNavigableMap(entry.getValue()));
		}
		recordsByChannel = Collections.unmodifiableMap(newRecords);
	}

	/**
	 * The record in force on a channel at a time.
	 * @param channel the channel.
	 * @param timeMillis the time in milliseconds.
	 * @return the latest record at or before this time, or null if there is
	 * none, or if that buoy had ended by this time.
	 */
	public SonobuoyRecord getRecordAt(int channel, long timeMillis) {
		NavigableMap<Long, SonobuoyRecord> records = recordsByChannel.get(channel);
		if (records == null) {
			return null;
		}
		Map.Entry<Long, SonobuoyRecord> entry = records.floorEntry(timeMillis);
		if (entry == null) {
			return null;
		}
		SonobuoyRecord record = entry.getValue();
		if (record.hasEndedBy(timeMillis)) {
			return null;
		}
		return record;
	}

	/**
	 * When a record stops being the one in force on its channel.
	 * <p>
	 * A record holds until the next record on its channel takes over, or until
	 * its buoy ends, whichever comes first.
	 * @param record a record, which need not be one of those held.
	 * @return the time it stops being in force, or null if nothing supersedes
	 * it and it has no end time.
	 */
	public Long getInForceUntil(SonobuoyRecord record) {
		Long endTime = record.getEndTimeMillis();
		NavigableMap<Long, SonobuoyRecord> records = recordsByChannel.get(record.getChannel());
		Map.Entry<Long, SonobuoyRecord> next = records == null ? null
				: records.higherEntry(record.getTimeMillis());
		if (next == null) {
			return endTime;
		}
		if (endTime != null && endTime < next.getKey()) {
			return endTime;
		}
		return next.getKey();
	}

	/**
	 * @param channel the channel.
	 * @return the last record on a channel, whether or not its buoy has ended,
	 * or null if the channel has none.
	 */
	public SonobuoyRecord getLastRecord(int channel) {
		NavigableMap<Long, SonobuoyRecord> records = recordsByChannel.get(channel);
		if (records == null || records.isEmpty()) {
			return null;
		}
		return records.lastEntry().getValue();
	}

	/**
	 * @return every record held, earliest first, and by channel where two
	 * records share a time. The list cannot be changed.
	 */
	public List<SonobuoyRecord> getAllRecords() {
		List<SonobuoyRecord> all = new ArrayList<>();
		for (NavigableMap<Long, SonobuoyRecord> records : recordsByChannel.values()) {
			all.addAll(records.values());
		}
		all.sort(Comparator.comparingLong(SonobuoyRecord::getTimeMillis)
				.thenComparingInt(SonobuoyRecord::getChannel));
		return Collections.unmodifiableList(all);
	}

	/**
	 * @return the number of records held, over all channels.
	 */
	public int getRecordCount() {
		int n = 0;
		for (NavigableMap<Long, SonobuoyRecord> records : recordsByChannel.values()) {
			n += records.size();
		}
		return n;
	}
}
