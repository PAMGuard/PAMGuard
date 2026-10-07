package test.difar;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import difar.DifarClipPayload;
import difar.crossings.LegacyCrossing;

/**
 * Tests for the DIFAR clip payload. Version 3 is written and read back.
 * Versions 0 to 2 are built by hand, byte for byte, as old PAMGuard wrote
 * them. Every read must end exactly at the end of the payload, since a
 * misread tail would shift every later field. No PAMGuard runtime is needed.
 */
public class DifarClipPayloadTest {

	private static final long CLIP_START = 1363000000000L;
	private static final int LENGTH = 4;

	private static double[][] audio() {
		double[][] a = new double[3][LENGTH];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < LENGTH; j++) {
				a[i][j] = (i + 1) * (j - 1.5) / 10;
			}
		}
		return a;
	}

	private static DifarClipPayload clip(double[][] audio) {
		DifarClipPayload p = new DifarClipPayload();
		p.clipStartMillis = CLIP_START;
		p.displaySampleRate = 1000f;
		p.demuxData = audio;
		p.amplitude = -20f;
		p.gain = 1.5f;
		p.selectedAngle = 156.25f;
		p.selectedFrequency = 45f;
		p.speciesCode = "BmA";
		p.trackedGroup = "None";
		return p;
	}

	private static byte[] write(DifarClipPayload p) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		p.write(new DataOutputStream(bos));
		return bos.toByteArray();
	}

	/** Read, and check nothing is left over. */
	private static DifarClipPayload read(byte[] bytes, int version) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		DifarClipPayload p = DifarClipPayload.read(dis, version);
		assertEquals(0, dis.available(), "bytes left after reading version " + version);
		return p;
	}

	/**
	 * A clip as old PAMGuard wrote it, with its audio and the given tail.
	 * @param tail the crossing tail, or null to write none (version 3).
	 */
	private static byte[] oldClip(int version, double[][] audio, byte[] tail) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		if (version < 2) {
			dos.writeLong(123456L); // start sample
		}
		dos.writeLong(CLIP_START);
		if (version < 2) {
			dos.writeInt(1 << 2); // channel map
		}
		dos.writeFloat(1000f);
		dos.writeInt(audio == null ? 0 : audio[0].length);
		if (version < 2) {
			dos.writeFloat(10f);
			dos.writeFloat(90f);
		}
		dos.writeFloat(-20f);
		dos.writeFloat(1.5f);
		dos.writeFloat(156.25f);
		dos.writeFloat(45f);
		dos.writeUTF("BmA");
		if (version >= 1) {
			dos.writeUTF("None");
		}
		if (audio == null) {
			dos.writeFloat(0);
		}
		else {
			double max = 0;
			for (double[] ch : audio) {
				for (double v : ch) {
					max = Math.max(max, Math.abs(v));
				}
			}
			dos.writeFloat((float) max);
			for (double[] ch : audio) {
				for (double v : ch) {
					dos.writeShort((int) (v * 32767 / max));
				}
			}
		}
		if (tail != null) {
			dos.write(tail);
		}
		return bos.toByteArray();
	}

	/** A crossing tail of the holder plus the given partners, as {channel, time} pairs. */
	private static byte[] tail(int version, long[]... partners) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeShort(partners.length + 1);
		dos.writeFloat(-64.5f);
		dos.writeFloat(110.25f);
		if (version >= 1) {
			dos.writeFloat(250f);
			dos.writeFloat(180f);
		}
		for (long[] p : partners) {
			dos.writeShort((int) p[0]);
			dos.writeLong(p[1]);
		}
		return bos.toByteArray();
	}

	private static byte[] noTail() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new DataOutputStream(bos).writeShort(0);
		return bos.toByteArray();
	}

	private static void assertClipFields(DifarClipPayload p) {
		assertEquals(CLIP_START, p.clipStartMillis);
		assertEquals(1000f, p.displaySampleRate);
		assertEquals(-20f, p.amplitude);
		assertEquals(1.5f, p.gain);
		assertEquals(156.25f, p.selectedAngle);
		assertEquals(45f, p.selectedFrequency);
		assertEquals("BmA", p.speciesCode);
	}

	private static void assertAudio(double[][] expected, double[][] actual) {
		assertNotNull(actual);
		assertEquals(3, actual.length);
		double max = Arrays.stream(expected).flatMapToDouble(Arrays::stream).map(Math::abs).max().getAsDouble();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < LENGTH; j++) {
				assertEquals(expected[i][j], actual[i][j], max / 32767 * 1.01, "sample " + i + "," + j);
			}
		}
	}

	@Test
	public void version3RoundTripsWithoutATail() throws IOException {
		DifarClipPayload written = clip(audio());
		DifarClipPayload read = read(write(written), 3);
		assertClipFields(read);
		assertEquals("None", read.trackedGroup);
		assertEquals(LENGTH, read.demuxedLength);
		assertAudio(written.demuxData, read.demuxData);
		assertNull(read.legacyCrossing);
		assertNull(read.startSample);
	}

	@Test
	public void version3RoundTripsWithoutAudio() throws IOException {
		DifarClipPayload read = read(write(clip(null)), 3);
		assertClipFields(read);
		assertEquals(0, read.demuxedLength);
		assertNull(read.demuxData);
	}

	@Test
	public void version3IsWhatAnOldClipWasLessItsTail() throws IOException {
		assertArrayEquals(oldClip(3, audio(), null), write(clip(audio())));
	}

	@Test
	public void version2ThreeClipTailBecomesALegacyRecord() throws IOException {
		byte[] bytes = oldClip(2, audio(), tail(2, new long[] {1, CLIP_START + 1000}, new long[] {2, CLIP_START + 2000}));
		DifarClipPayload read = read(bytes, 2);
		assertClipFields(read);
		assertAudio(audio(), read.demuxData);
		LegacyCrossing x = read.legacyCrossing;
		assertNotNull(x);
		assertEquals(3, x.getClipCount());
		assertEquals(-64.5, x.getLatitude());
		assertEquals(110.25, x.getLongitude());
		assertEquals(250.0, x.getXError());
		assertEquals(180.0, x.getYError());
		assertEquals(Arrays.asList(new LegacyCrossing.Partner(1, CLIP_START + 1000),
				new LegacyCrossing.Partner(2, CLIP_START + 2000)), x.getPartners());
	}

	@Test
	public void version2WithNoCrossingHasNoLegacyRecord() throws IOException {
		assertNull(read(oldClip(2, audio(), noTail()), 2).legacyCrossing);
	}

	@Test
	public void version2CrossingOfOneClipKeepsItsLocation() throws IOException {
		LegacyCrossing x = read(oldClip(2, null, tail(2)), 2).legacyCrossing;
		assertNotNull(x);
		assertEquals(1, x.getClipCount());
		assertTrue(x.getPartners().isEmpty());
	}

	@Test
	public void version1ReadsTheFieldsLaterMovedToTheHeader() throws IOException {
		DifarClipPayload read = read(oldClip(1, audio(), tail(1, new long[] {3, CLIP_START + 500})), 1);
		assertClipFields(read);
		assertEquals(123456L, read.startSample.longValue());
		assertEquals(1 << 2, read.channelMap.intValue());
		assertArrayEquals(new double[] {10, 90}, read.frequencyRange);
		assertEquals("None", read.trackedGroup);
		assertEquals(250.0, read.legacyCrossing.getXError());
	}

	@Test
	public void version0HasNoTrackedGroupOrErrors() throws IOException {
		DifarClipPayload read = read(oldClip(0, audio(), tail(0, new long[] {1, CLIP_START + 1000})), 0);
		assertClipFields(read);
		assertNull(read.trackedGroup);
		LegacyCrossing x = read.legacyCrossing;
		assertEquals(2, x.getClipCount());
		assertTrue(Double.isNaN(x.getXError()));
		assertTrue(Double.isNaN(x.getYError()));
	}
}
