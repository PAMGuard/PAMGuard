package test.PamUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import PamUtils.Ascii6Bit;

/**
 * The 6-bit text encoding used to store settings in the database.
 */
public class Ascii6BitTest {

	/** Bytes encoded and decoded again come back unchanged. */
	@Test
	public void roundTripIsExact() {
		Random random = new Random(1);
		// multiples of three bytes fill whole characters
		for (int n : new int[] {3, 300, 30000}) {
			byte[] bytes = new byte[n];
			random.nextBytes(bytes);
			Ascii6Bit encoded = new Ascii6Bit(bytes.clone());
			Ascii6Bit decoded = new Ascii6Bit(encoded.getStringData(), encoded.getSpareBits());
			assertArrayEquals(bytes, decoded.getByteData(), n + " bytes");
		}
	}

	/**
	 * Encoding takes time in proportion to the data. Settings of a few
	 * megabytes, such as an Array Manager with many streamers, used to take
	 * minutes, because the text was built one character at a time.
	 */
	@Test
	public void largeSettingsEncodeQuickly() {
		byte[] bytes = new byte[3_000_000];
		new Random(2).nextBytes(bytes);
		long start = System.nanoTime();
		String text = new Ascii6Bit(bytes).getStringData();
		double seconds = (System.nanoTime() - start) / 1e9;
		assertTrue(text.length() == 4_000_000, "4 characters for every 3 bytes");
		assertTrue(seconds < 2, String.format("3 MB took %.2f s", seconds));
	}
}
