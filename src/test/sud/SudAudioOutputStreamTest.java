package test.sud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudAudioOutputStream;
import org.pamguard.x3.sud.SudParams;

/**
 * Round-trip tests for {@link SudAudioOutputStream} / {@link SudAudioInputStream}.
 * <p>
 * Each test writes a SUD file using {@code SudAudioOutputStream}, then reads it
 * back with {@code SudAudioInputStream} and verifies that the decoded samples
 * match the originals. X3 compression is lossless for 16-bit PCM, so the
 * samples must be identical.
 */
public class SudAudioOutputStreamTest {

	/** A fixed start time used across all tests to get deterministic output. */
	private static final long START_TIME_MS = 1_700_000_000_000L; // 2023-11-14

	// -------------------------------------------------------------------------
	// Single-channel round-trip
	// -------------------------------------------------------------------------

	/**
	 * Writes a single-channel SUD file containing a ramp waveform, reads it back,
	 * and verifies that all samples are preserved exactly.
	 */
	@Test
	public void testSingleChannelRoundTrip(@TempDir File tempDir) throws Exception {
		int sampleRate = 48_000;
		int nChannels = 1;
		int nSamples = 3_000; // 3 chunks of 1 000 samples each

		short[] originalSamples = makeRamp(nSamples);

		File sudFile = new File(tempDir, "mono_test.sud");
		writeSudFile(sudFile, originalSamples, sampleRate, nChannels, nSamples);

		short[] readback = readSudFile(sudFile, nChannels, nSamples);

		assertArrayEquals(originalSamples, readback,
				"Single-channel round-trip: decoded samples must match originals");
	}

	// -------------------------------------------------------------------------
	// Two-channel round-trip
	// -------------------------------------------------------------------------

	/**
	 * Writes a two-channel SUD file, reads it back, and verifies all samples.
	 * <p>
	 * Samples are interleaved as {@code [ch0s0, ch1s0, ch0s1, ch1s1, ...]}.
	 */
	@Test
	public void testTwoChannelRoundTrip(@TempDir File tempDir) throws Exception {
		int sampleRate = 96_000;
		int nChannels = 2;
		int nSamples = 2_000; // 2 chunks of 1 000 samples each

		// Channel 0 contains a ramp; channel 1 is its negation.
		short[] originalSamples = makeTwoChannelRamp(nSamples);

		File sudFile = new File(tempDir, "stereo_test.sud");
		writeSudFile(sudFile, originalSamples, sampleRate, nChannels, nSamples);

		short[] readback = readSudFile(sudFile, nChannels, nSamples);

		assertArrayEquals(originalSamples, readback,
				"Two-channel round-trip: decoded samples must match originals");
	}

	// -------------------------------------------------------------------------
	// Sine wave round-trip
	// -------------------------------------------------------------------------

	/**
	 * Writes a single-channel SUD file containing a 1 kHz sine wave, reads it back,
	 * and verifies the samples are preserved exactly.
	 */
	@Test
	public void testSineWaveRoundTrip(@TempDir File tempDir) throws Exception {
		int sampleRate = 44_100;
		int nChannels = 1;
		int nSamples = 4_000; // 4 chunks

		short[] originalSamples = makeSineWave(nSamples, 1_000, sampleRate);

		File sudFile = new File(tempDir, "sine_test.sud");
		writeSudFile(sudFile, originalSamples, sampleRate, nChannels, nSamples);

		short[] readback = readSudFile(sudFile, nChannels, nSamples);

		assertArrayEquals(originalSamples, readback,
				"Sine-wave round-trip: decoded samples must match originals");
	}

	// -------------------------------------------------------------------------
	// Audio format verification
	// -------------------------------------------------------------------------

	/**
	 * Verifies that the {@link AudioFormat} reported by {@link SudAudioInputStream}
	 * matches the parameters supplied to {@link SudAudioOutputStream}.
	 */
	@Test
	public void testAudioFormatRoundTrip(@TempDir File tempDir) throws Exception {
		int sampleRate = 192_000;
		int nChannels = 2;
		int chunkSamples = 500;
		int nSamples = 1_000;

		short[] samples = makeRamp(nSamples * nChannels);

		File sudFile = new File(tempDir, "format_test.sud");
		try (SudAudioOutputStream sudOut = new SudAudioOutputStream(
				new FileOutputStream(sudFile), sampleRate, nChannels, chunkSamples, START_TIME_MS)) {
			// Write in chunks matching chunkSamples
			short[] chunk = new short[chunkSamples * nChannels];
			for (int offset = 0; offset < nSamples; offset += chunkSamples) {
				int n = Math.min(chunkSamples, nSamples - offset);
				System.arraycopy(samples, offset * nChannels, chunk, 0, n * nChannels);
				sudOut.write(chunk, n);
			}
		}

		SudParams params = new SudParams();
		params.setFileSave(false, false, false, false);

		AudioInputStream ais = SudAudioInputStream.openInputStream(sudFile, params, false);
		assertNotNull(ais, "SudAudioInputStream must open successfully");

		AudioFormat fmt = ais.getFormat();
		assertEquals(sampleRate, (int) fmt.getSampleRate(),
				"Sample rate must match");
		assertEquals(nChannels, fmt.getChannels(),
				"Channel count must match");
		assertEquals(16, fmt.getSampleSizeInBits(),
				"Bits per sample must be 16");
		assertNotNull(fmt, "AudioFormat must not be null");
		ais.close();
	}

	// -------------------------------------------------------------------------
	// XML metadata verification
	// -------------------------------------------------------------------------

	/**
	 * Confirms that the SUD file produced by {@link SudAudioOutputStream} is a
	 * non-empty regular file that can be opened without throwing an exception.
	 */
	@Test
	public void testFileIsNonEmpty(@TempDir File tempDir) throws Exception {
		File sudFile = new File(tempDir, "nonempty_test.sud");
		try (SudAudioOutputStream sudOut = new SudAudioOutputStream(
				new FileOutputStream(sudFile), 48_000, 1, 1_000, START_TIME_MS)) {
			sudOut.write(makeRamp(1_000), 1_000);
		}

		assertTrue(sudFile.exists(), "SUD file must exist");
		assertTrue(sudFile.length() > 0, "SUD file must not be empty");

		// Must open without exception.
		SudParams params = new SudParams();
		params.setFileSave(false, false, false, false);
		AudioInputStream ais = SudAudioInputStream.openInputStream(sudFile, params, false);
		assertNotNull(ais);
		ais.close();
	}

	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------

	/**
	 * Writes {@code nSamples} samples from {@code samples} to a SUD file.
	 * Samples are written in chunks of 1 000 (the default chunk size).
	 */
	private void writeSudFile(File file, short[] samples, int sampleRate, int nChannels, int nSamples)
			throws IOException {
		int chunkSamples = 1_000;
		try (SudAudioOutputStream sudOut = new SudAudioOutputStream(
				new FileOutputStream(file), sampleRate, nChannels, chunkSamples, START_TIME_MS)) {
			int offset = 0;
			while (offset < nSamples) {
				int n = Math.min(chunkSamples, nSamples - offset);
				short[] chunk = new short[n * nChannels];
				System.arraycopy(samples, offset * nChannels, chunk, 0, n * nChannels);
				sudOut.write(chunk, n);
				offset += n;
			}
		}
	}

	/**
	 * Reads back {@code nSamples} samples per channel from the given SUD file.
	 * Returns an interleaved short array of length {@code nSamples * nChannels}.
	 */
	private short[] readSudFile(File sudFile, int nChannels, int nSamples) throws Exception {
		SudParams params = new SudParams();
		params.setFileSave(false, false, false, false);

		AudioInputStream ais = SudAudioInputStream.openInputStream(sudFile, params, false);
		assertNotNull(ais, "SudAudioInputStream must open successfully");

		int bytesPerSample = 2; // 16-bit PCM
		int totalBytes = nSamples * nChannels * bytesPerSample;
		byte[] rawBytes = new byte[totalBytes];

		int totalRead = 0;
		while (totalRead < totalBytes) {
			int read = ais.read(rawBytes, totalRead, totalBytes - totalRead);
			if (read < 0) {
				break;
			}
			totalRead += read;
		}
		ais.close();

		assertTrue(totalRead >= totalBytes,
				"Must read at least " + totalBytes + " bytes; got " + totalRead);

		// Convert little-endian byte pairs to shorts.
		short[] result = new short[nSamples * nChannels];
		for (int i = 0; i < result.length; i++) {
			int lo = rawBytes[i * 2] & 0xFF;
			int hi = rawBytes[i * 2 + 1] & 0xFF;
			result[i] = (short) (lo | (hi << 8));
		}
		return result;
	}

	/** Creates a mono ramp: sample[i] = (short)(i % 32768). */
	private static short[] makeRamp(int length) {
		short[] data = new short[length];
		for (int i = 0; i < length; i++) {
			data[i] = (short) (i % 32768);
		}
		return data;
	}

	/**
	 * Creates an interleaved two-channel ramp:
	 * ch0 = (short)(i % 32768), ch1 = (short)(-(i % 32768)).
	 */
	private static short[] makeTwoChannelRamp(int nSamples) {
		short[] data = new short[nSamples * 2];
		for (int i = 0; i < nSamples; i++) {
			data[i * 2] = (short) (i % 32768);
			data[i * 2 + 1] = (short) (-(i % 32768));
		}
		return data;
	}

	/**
	 * Creates a mono sine wave of the specified frequency.
	 * Amplitude is 8 000 (well within the 16-bit range to avoid clipping).
	 */
	private static short[] makeSineWave(int nSamples, double freqHz, int sampleRate) {
		short[] data = new short[nSamples];
		double amplitude = 8_000.0;
		for (int i = 0; i < nSamples; i++) {
			data[i] = (short) (amplitude * Math.sin(2.0 * Math.PI * freqHz * i / sampleRate));
		}
		return data;
	}
}
