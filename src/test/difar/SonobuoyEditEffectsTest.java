package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import difar.SonobuoyEditEffects;
import difar.SonobuoyHistory;
import difar.SonobuoyRecord;

/**
 * What changing one buoy record affects.
 * <p>
 * The buoys are those of the 2019 voyage: channel 1 carried buoy 159 from
 * 18:18:23, then 157.1 from 18:52:06, then 160 from 19:22:22.
 */
public class SonobuoyEditEffectsTest {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private static long utc(String time) {
		return LocalDateTime.parse(time, FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	/** A detection, as much of one as the rules need. */
	private static class TestDetection implements SonobuoyEditEffects.Detection {

		private final long timeMillis;
		private final int channel;
		private final boolean triangulated;

		TestDetection(String time, int channel, boolean triangulated) {
			this.timeMillis = utc(time);
			this.channel = channel;
			this.triangulated = triangulated;
		}

		@Override
		public long getTimeMillis() {
			return timeMillis;
		}

		@Override
		public int getChannel() {
			return channel;
		}

		@Override
		public boolean hasTriangulation() {
			return triangulated;
		}
	}

	private static SonobuoyRecord record(int channel, String time, String name) {
		return new SonobuoyRecord(channel, utc(time), null, name, -66.0, 151.0, 93.0);
	}

	private static SonobuoyHistory history() {
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(
				record(1, "2019-02-12 18:18:23.443", "159"),
				record(1, "2019-02-12 18:52:06.661", "157.1"),
				record(1, "2019-02-12 19:22:22.933", "160"),
				record(0, "2019-02-12 18:51:13.485", "159.0")));
		return history;
	}

	private static SonobuoyRecord buoy159() {
		return history().getRecordAt(1, utc("2019-02-12 18:30:00.000"));
	}

	private static SonobuoyEditEffects effects(List<SonobuoyEditEffects.Detection> detections) {
		return new SonobuoyEditEffects(buoy159(), history(), detections, 0, Long.MAX_VALUE);
	}

	/** Only detections on the record's own buoy, while it was in force, count. */
	@Test
	public void countsOnlyDetectionsOnThatBuoy() {
		SonobuoyEditEffects effects = effects(Arrays.asList(
				new TestDetection("2019-02-12 18:00:00.000", 1, true),   // before the record
				new TestDetection("2019-02-12 18:20:00.000", 1, true),   // in
				new TestDetection("2019-02-12 18:30:00.000", 1, false),  // in, no triangulation
				new TestDetection("2019-02-12 18:30:00.000", 0, true),   // another channel
				new TestDetection("2019-02-12 19:00:00.000", 1, true))); // after 157.1 took over

		assertEquals(2, effects.getBearings());
		assertEquals(1, effects.getTriangulations());
		assertTrue(effects.isAnythingAffected());
	}

	/** The record's own times bound the period, to the millisecond. */
	@Test
	public void theRecordsOwnTimesBoundThePeriod() {
		SonobuoyEditEffects effects = effects(new ArrayList<>());
		assertTrue(effects.covers(new TestDetection("2019-02-12 18:18:23.443", 1, false)));
		assertFalse(effects.covers(new TestDetection("2019-02-12 18:18:23.442", 1, false)));
		assertTrue(effects.covers(new TestDetection("2019-02-12 18:52:06.660", 1, false)));
		assertFalse(effects.covers(new TestDetection("2019-02-12 18:52:06.661", 1, false)));
	}

	/** Nothing to do when the buoy carries no detections. */
	@Test
	public void nothingIsAffectedWhenThereAreNoDetections() {
		SonobuoyEditEffects effects = effects(new ArrayList<>());
		assertEquals(0, effects.getBearings());
		assertEquals(0, effects.getTriangulations());
		assertFalse(effects.isAnythingAffected());
	}

	/**
	 * Moving the deploy time earlier covers detections in the old period and
	 * the new one, since both were made with this buoy's geometry.
	 */
	@Test
	public void movingTheDeployTimeCoversBothPeriods() {
		List<SonobuoyEditEffects.Detection> detections = Arrays.asList(
				new TestDetection("2019-02-12 18:10:00.000", 1, true),
				new TestDetection("2019-02-12 18:20:00.000", 1, true));
		SonobuoyEditEffects effects = new SonobuoyEditEffects(buoy159(), history(), detections,
				0, Long.MAX_VALUE, utc("2019-02-12 18:05:00.000"), null);

		assertEquals(utc("2019-02-12 18:05:00.000"), effects.getStartTime());
		assertEquals(2, effects.getTriangulations());
	}

	/** Moving the end later covers detections in both periods too. */
	@Test
	public void movingTheEndTimeCoversBothPeriods() {
		SonobuoyRecord ending = new SonobuoyRecord(2, utc("2019-02-12 10:00:00.000"),
				utc("2019-02-12 12:00:00.000"), "A", -66.0, 151.0, 93.0);
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(ending));
		List<SonobuoyEditEffects.Detection> detections = Arrays.asList(
				new TestDetection("2019-02-12 11:00:00.000", 2, true),
				new TestDetection("2019-02-12 12:30:00.000", 2, true));

		SonobuoyEditEffects effects = new SonobuoyEditEffects(ending, history, detections,
				0, Long.MAX_VALUE, ending.getTimeMillis(), utc("2019-02-12 13:00:00.000"));

		assertEquals(Long.valueOf(utc("2019-02-12 13:00:00.000")), effects.getEndTime());
		assertEquals(2, effects.getTriangulations());
	}

	/**
	 * The period worked through reaches a minute either side, for detections
	 * matched across buoys that lie just outside the buoy's own period.
	 */
	@Test
	public void theReprocessPeriodAllowsForTravelBetweenBuoys() {
		SonobuoyEditEffects effects = effects(new ArrayList<>());
		assertEquals(effects.getStartTime() - 60000, effects.getReprocessStartTime());
		assertEquals(effects.getEndTime() + 60000, effects.getEndTimeOrLatest());
	}

	/** A buoy still in force has no end, so the period runs to the end of the data. */
	@Test
	public void aBuoyStillInForceRunsOn() {
		SonobuoyRecord last = history().getRecordAt(1, utc("2019-02-12 20:00:00.000"));
		SonobuoyEditEffects effects = new SonobuoyEditEffects(last, history(), new ArrayList<>(),
				0, Long.MAX_VALUE);
		assertEquals(null, effects.getEndTime());
		assertEquals(Long.MAX_VALUE, effects.getEndTimeOrLatest());
	}

	/** The message says what will happen, and names the buoy. */
	@Test
	public void theMessageSaysWhatWillHappen() {
		SonobuoyEditEffects effects = effects(Arrays.asList(
				new TestDetection("2019-02-12 18:20:00.000", 1, true),
				new TestDetection("2019-02-12 18:30:00.000", 1, false)));

		String message = effects.getMessage(true);
		assertTrue(message.contains("Buoy 159 on channel 1"));
		assertTrue(message.contains("2 bearings will be updated"));
		assertTrue(message.contains("1 triangulations will be worked out again"));
	}

	/** While PAMGuard runs, the message says what cannot be reached. */
	@Test
	public void theMessageWarnsAboutDataAlreadyWritten() {
		SonobuoyEditEffects effects = effects(Arrays.asList(
				new TestDetection("2019-02-12 18:20:00.000", 1, true)));

		assertTrue(effects.getMessage(false).contains("already written to file"));
		assertFalse(effects.getMessage(true).contains("already written to file"));
	}

	/** Counts are of loaded data, and the message says so when there is more. */
	@Test
	public void theMessageSaysWhenCountsAreOfLoadedDataOnly() {
		List<SonobuoyEditEffects.Detection> detections = Arrays.asList(
				new TestDetection("2019-02-12 18:20:00.000", 1, true));
		SonobuoyEditEffects partial = new SonobuoyEditEffects(buoy159(), history(), detections,
				utc("2019-02-12 18:30:00.000"), utc("2019-02-12 19:30:00.000"));
		assertTrue(partial.getMessage(true).contains("data loaded now"));
		assertFalse(effects(detections).getMessage(true).contains("data loaded now"));
	}
}
