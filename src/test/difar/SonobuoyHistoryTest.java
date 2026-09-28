package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import difar.SonobuoyHistory;
import difar.SonobuoyRecord;

/**
 * Which buoy record is in force on a channel at a given time.
 * <p>
 * The records are the real ones from the 2019 voyage database, rows 237 to
 * 244 of HydrophoneStreamers. The core array lookups returned the 20:44 and
 * 20:45 records for detections at 19:32, which swapped the positions of two
 * buoys. These tests pin down the right answer.
 */
public class SonobuoyHistoryTest {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private static long utc(String time) {
		return LocalDateTime.parse(time, FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	private static SonobuoyRecord record(int channel, String time, String name,
			Double lat, Double lon, double heading) {
		return new SonobuoyRecord(channel, utc(time), null, name, lat, lon, heading);
	}

	/**
	 * Rows 237 to 244, in database order. Positions come from the console
	 * output for the buoys in the 19:32 triplet and the two records after it.
	 * The other positions were not looked up, so they are left unknown.
	 */
	private static List<SonobuoyRecord> voyageRecords() {
		return Arrays.asList(
				record(2, "2019-02-12 16:01:23.342", "158",   -66.1627, 151.7012, 98.0539136314799),
				record(0, "2019-02-12 16:21:08.812", "157.0", null, null, 92.3),
				record(1, "2019-02-12 18:18:23.443", "159",   null, null, 93.2586030094585),
				record(0, "2019-02-12 18:51:13.485", "159.0", -66.2701, 151.8304, 93.3),
				record(1, "2019-02-12 18:52:06.661", "157.1", null, null, 92.3),
				record(1, "2019-02-12 19:22:22.933", "160",   -66.2598, 152.0555, 97.7571868974313),
				record(0, "2019-02-12 20:44:45.979", "158.0", -66.1627, 151.7012, 98.1),
				record(2, "2019-02-12 20:45:28.674", "159.2", -66.2701, 151.8304, 93.3));
	}

	private static SonobuoyHistory voyageHistory() {
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(voyageRecords());
		return history;
	}

	/**
	 * The triplet that failed on the voyage data. Each detection must get the
	 * buoy that was on its channel at 19:32, not the one deployed later.
	 */
	@Test
	public void theTripletAt1932GetsTheBuoysInForce() {
		SonobuoyHistory history = voyageHistory();

		SonobuoyRecord ch2 = history.getRecordAt(2, utc("2019-02-12 19:32:02.020"));
		SonobuoyRecord ch0 = history.getRecordAt(0, utc("2019-02-12 19:32:09.939"));
		SonobuoyRecord ch1 = history.getRecordAt(1, utc("2019-02-12 19:32:15.001"));

		assertEquals("158", ch2.getName());
		assertEquals(-66.1627, ch2.getLatitude(), 1e-9);
		assertEquals(98.0539136314799, ch2.getHeading(), 1e-9);

		assertEquals("159.0", ch0.getName());
		assertEquals(-66.2701, ch0.getLatitude(), 1e-9);
		assertEquals(93.3, ch0.getHeading(), 1e-9);

		assertEquals("160", ch1.getName());
		assertEquals(152.0555, ch1.getLongitude(), 1e-9);
	}

	/** After a later record, that record applies. */
	@Test
	public void aLaterRecordTakesOver() {
		SonobuoyHistory history = voyageHistory();
		assertEquals("159.2", history.getRecordAt(2, utc("2019-02-12 21:00:00.000")).getName());
		assertEquals("158.0", history.getRecordAt(0, utc("2019-02-12 21:00:00.000")).getName());
	}

	/** A record applies from its own time, to the millisecond. */
	@Test
	public void aRecordAppliesFromItsOwnTime() {
		SonobuoyHistory history = voyageHistory();
		assertEquals("160", history.getRecordAt(1, utc("2019-02-12 19:22:22.933")).getName());
		assertEquals("157.1", history.getRecordAt(1, utc("2019-02-12 19:22:22.932")).getName());
	}

	/**
	 * Before a channel's first record there is no buoy. The history must say
	 * so, rather than return the first record, which is later.
	 */
	@Test
	public void beforeTheFirstRecordThereIsNoBuoy() {
		SonobuoyHistory history = voyageHistory();
		assertNull(history.getRecordAt(2, utc("2019-02-12 16:01:23.341")));
		assertNull(history.getRecordAt(0, utc("2019-02-12 12:00:00.000")));
	}

	/** A channel with no records has no buoy. */
	@Test
	public void anUnknownChannelHasNoBuoy() {
		assertNull(voyageHistory().getRecordAt(5, utc("2019-02-12 19:32:00.000")));
	}

	/** An empty history has no buoys, and a fresh one is empty. */
	@Test
	public void anEmptyHistoryHasNoBuoys() {
		SonobuoyHistory history = new SonobuoyHistory();
		assertNull(history.getRecordAt(0, utc("2019-02-12 19:32:00.000")));
		history.setRecords(Collections.emptyList());
		assertNull(history.getRecordAt(0, utc("2019-02-12 19:32:00.000")));
		history.setRecords(null);
		assertEquals(0, history.getRecordCount());
	}

	/**
	 * Once a buoy has ended, its channel has no buoy until the next record.
	 * The end time itself counts as ended.
	 */
	@Test
	public void anEndedBuoyIsNoLongerInForce() {
		long deployed = utc("2019-02-12 10:00:00.000");
		long ended = utc("2019-02-12 14:00:00.000");
		long redeployed = utc("2019-02-12 16:00:00.000");
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(
				new SonobuoyRecord(0, deployed, ended, "A", -66.0, 150.0, 90.0),
				new SonobuoyRecord(0, redeployed, null, "B", -66.1, 150.1, 95.0)));

		assertEquals("A", history.getRecordAt(0, ended - 1).getName());
		assertNull(history.getRecordAt(0, ended));
		assertNull(history.getRecordAt(0, utc("2019-02-12 15:00:00.000")));
		assertEquals("B", history.getRecordAt(0, redeployed).getName());
	}

	/**
	 * In normal mode, asking about now gives the last buoy that has not ended,
	 * and nothing if the last buoy has ended.
	 */
	@Test
	public void nowGivesTheLastBuoyThatHasNotEnded() {
		long now = utc("2019-02-12 22:00:00.000");
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(
				new SonobuoyRecord(0, utc("2019-02-12 18:00:00.000"), null, "A", -66.0, 150.0, 90.0),
				new SonobuoyRecord(1, utc("2019-02-12 18:00:00.000"), utc("2019-02-12 21:00:00.000"),
						"B", -66.1, 150.1, 95.0)));

		assertEquals("A", history.getRecordAt(0, now).getName());
		assertNull(history.getRecordAt(1, now));
	}

	/** The viewer can load a record twice. The copies count once. */
	@Test
	public void duplicateRecordsCountOnce() {
		List<SonobuoyRecord> doubled = new ArrayList<>(voyageRecords());
		doubled.addAll(voyageRecords());
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(doubled);

		assertEquals(voyageRecords().size(), history.getRecordCount());
		assertEquals("159.0", history.getRecordAt(0, utc("2019-02-12 19:32:09.939")).getName());
	}

	/** Order of the records given does not matter. */
	@Test
	public void recordOrderDoesNotMatter() {
		List<SonobuoyRecord> reversed = new ArrayList<>(voyageRecords());
		Collections.reverse(reversed);
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(reversed);

		assertEquals("158", history.getRecordAt(2, utc("2019-02-12 19:32:02.020")).getName());
		assertEquals("159.0", history.getRecordAt(0, utc("2019-02-12 19:32:09.939")).getName());
	}

	/**
	 * Setting records replaces the old set rather than adding to it. This is
	 * how the history is rebuilt after a reload or an edit.
	 */
	@Test
	public void settingRecordsReplacesTheOldSet() {
		SonobuoyHistory history = voyageHistory();
		SonobuoyRecord edited = new SonobuoyRecord(0, utc("2019-02-12 18:51:13.485"), null,
				"159.0", -66.2700, 151.8300, 94.0);
		history.setRecords(Collections.singletonList(edited));

		assertEquals(1, history.getRecordCount());
		assertNull(history.getRecordAt(2, utc("2019-02-12 19:32:02.020")));
		SonobuoyRecord ch0 = history.getRecordAt(0, utc("2019-02-12 19:32:09.939"));
		assertNotNull(ch0);
		assertEquals(94.0, ch0.getHeading(), 1e-9);
	}

	/** All records come back earliest first, once each, as the buoy manager lists them. */
	@Test
	public void allRecordsComeBackInTimeOrder() {
		List<SonobuoyRecord> doubled = new ArrayList<>(voyageRecords());
		doubled.addAll(voyageRecords());
		Collections.reverse(doubled);
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(doubled);

		List<SonobuoyRecord> all = history.getAllRecords();
		assertEquals(voyageRecords().size(), all.size());
		for (int i = 1; i < all.size(); i++) {
			assertTrue(all.get(i - 1).getTimeMillis() <= all.get(i).getTimeMillis());
		}
		assertEquals("158", all.get(0).getName());
		assertEquals("159.2", all.get(all.size() - 1).getName());
	}

	/** Records at the same time on different channels are listed by channel. */
	@Test
	public void recordsAtTheSameTimeAreListedByChannel() {
		long time = utc("2019-02-12 12:00:00.000");
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(
				new SonobuoyRecord(2, time, null, "C", -66.0, 150.0, 90.0),
				new SonobuoyRecord(0, time, null, "A", -66.0, 150.0, 90.0),
				new SonobuoyRecord(1, time, null, "B", -66.0, 150.0, 90.0)));

		List<SonobuoyRecord> all = history.getAllRecords();
		assertEquals("A", all.get(0).getName());
		assertEquals("B", all.get(1).getName());
		assertEquals("C", all.get(2).getName());
	}

	/** The list of all records belongs to the history and cannot be changed. */
	@Test
	public void allRecordsCannotBeChanged() {
		List<SonobuoyRecord> all = voyageHistory().getAllRecords();
		assertThrows(UnsupportedOperationException.class, () -> all.remove(0));
	}

	/** The short constructor makes a saved record with no UID or depth. */
	@Test
	public void theShortConstructorMakesASavedRecord() {
		SonobuoyRecord record = new SonobuoyRecord(0, 0L, null, "A", -66.0, 150.0, 90.0);
		assertTrue(record.isSaved());
		assertNull(record.getUid());
		assertNull(record.getDepth());
	}

	/** The full constructor keeps the UID, depth and saved state it is given. */
	@Test
	public void theFullConstructorKeepsEverything() {
		SonobuoyRecord record = new SonobuoyRecord(1, 5L, 9L, "B", -66.0, 150.0, 90.0,
				-100.0, 3816053L, false);
		assertEquals(Long.valueOf(3816053L), record.getUid());
		assertEquals(-100.0, record.getDepth(), 1e-9);
		assertFalse(record.isSaved());
		assertEquals(Long.valueOf(9L), record.getEndTimeMillis());
	}


	/** The last record on a channel is found whether or not its buoy has ended. */
	@Test
	public void theLastRecordOnAChannelIsFound() {
		SonobuoyHistory history = voyageHistory();
		assertEquals("159.2", history.getLastRecord(2).getName());
		assertEquals("160", history.getLastRecord(1).getName());
		assertNull(history.getLastRecord(5));
	}

	/** An ended buoy is still the last record on its channel. */
	@Test
	public void anEndedBuoyIsStillTheLastRecord() {
		long deployed = utc("2019-02-12 10:00:00.000");
		long ended = utc("2019-02-12 14:00:00.000");
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Collections.singletonList(
				new SonobuoyRecord(0, deployed, ended, "A", -66.0, 150.0, 90.0)));

		assertNull(history.getRecordAt(0, utc("2019-02-12 15:00:00.000")));
		assertEquals("A", history.getLastRecord(0).getName());
	}

	/** A record holds until the next one on its channel takes over. */
	@Test
	public void aRecordHoldsUntilTheNextOnItsChannel() {
		SonobuoyHistory history = voyageHistory();
		SonobuoyRecord ch1At1818 = history.getAllRecords().get(2);
		assertEquals("159", ch1At1818.getName());
		assertEquals(Long.valueOf(utc("2019-02-12 18:52:06.661")), history.getInForceUntil(ch1At1818));
	}

	/** The last record on a channel, with no end time, runs on. */
	@Test
	public void theLastRecordOnAChannelRunsOn() {
		SonobuoyHistory history = voyageHistory();
		List<SonobuoyRecord> all = history.getAllRecords();
		assertNull(history.getInForceUntil(all.get(all.size() - 1)));
	}

	/** An end time before the next record wins, and after it does not. */
	@Test
	public void theEarlierOfEndTimeAndNextRecordWins() {
		long deployed = utc("2019-02-12 10:00:00.000");
		long ended = utc("2019-02-12 14:00:00.000");
		long replaced = utc("2019-02-12 16:00:00.000");
		SonobuoyRecord endsFirst = new SonobuoyRecord(0, deployed, ended, "A", -66.0, 150.0, 90.0);
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(endsFirst,
				new SonobuoyRecord(0, replaced, null, "B", -66.1, 150.1, 95.0)));
		assertEquals(Long.valueOf(ended), history.getInForceUntil(endsFirst));

		SonobuoyRecord endsLater = new SonobuoyRecord(1, deployed, replaced + 1000, "C", -66.0, 150.0, 90.0);
		history.setRecords(Arrays.asList(endsLater,
				new SonobuoyRecord(1, replaced, null, "D", -66.1, 150.1, 95.0)));
		assertEquals(Long.valueOf(replaced), history.getInForceUntil(endsLater));
	}
}
