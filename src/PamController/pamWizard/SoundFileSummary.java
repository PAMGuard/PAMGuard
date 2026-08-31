package PamController.pamWizard;

import java.util.List;

import javax.sound.sampled.AudioFormat;

import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;

/**
 * A summary of the audio format of a set of imported sound files: what sample
 * rates and channel counts are present, and whether any of the files are
 * SoundTrap sud files.
 * <p>
 * The import wizard uses this to decide which configurations can be offered. The
 * <b>minimum</b> sample rate and channel count are what matter for that decision,
 * so that a configuration is only offered if <i>every</i> imported file meets its
 * requirements - it would be no use offering a configuration that only works on
 * some of the files.
 * <p>
 * Formats are read on the file scanning worker thread (see
 * {@link SoundFileScanner}) rather than on the event dispatch thread, since
 * reading the format of a sud file can be slow the first time.
 *
 * @author Jamie Macaulay
 */
public class SoundFileSummary {

	private final int fileCount;

	private final int formatsRead;

	private final float minSampleRate;

	private final float maxSampleRate;

	private final int minChannels;

	private final int maxChannels;

	private final boolean hasSudFiles;

	public SoundFileSummary(int fileCount, int formatsRead, float minSampleRate, float maxSampleRate,
			int minChannels, int maxChannels, boolean hasSudFiles) {
		this.fileCount = fileCount;
		this.formatsRead = formatsRead;
		this.minSampleRate = minSampleRate;
		this.maxSampleRate = maxSampleRate;
		this.minChannels = minChannels;
		this.maxChannels = maxChannels;
		this.hasSudFiles = hasSudFiles;
	}

	/**
	 * Summarise a list of scanned audio files. Files whose format could not be read
	 * are counted in {@link #getFileCount()} but do not contribute to the sample
	 * rate or channel figures.
	 *
	 * @param fileListData the scanned files, may be null.
	 * @return the summary, never null - an empty list gives a summary with a zero
	 *         file count and {@link #isValid()} false.
	 */
	public static SoundFileSummary summarise(FileListData<WavFileType> fileListData) {
		if (fileListData == null || fileListData.getFileCount() == 0) {
			return new SoundFileSummary(0, 0, 0, 0, 0, 0, false);
		}
		List<WavFileType> files = fileListData.getListCopy();

		float minSR = Float.MAX_VALUE;
		float maxSR = 0;
		int minCh = Integer.MAX_VALUE;
		int maxCh = 0;
		int nRead = 0;
		boolean anySud = false;

		for (WavFileType file : files) {
			if (file == null) {
				continue;
			}
			if (file.getName().toLowerCase().endsWith(".sud")) {
				anySud = true;
			}
			AudioFormat format = file.getAudioInfo();
			if (format == null) {
				continue;
			}
			float sampleRate = format.getSampleRate();
			int channels = format.getChannels();
			if (sampleRate <= 0 || channels <= 0) {
				continue;
			}
			minSR = Math.min(minSR, sampleRate);
			maxSR = Math.max(maxSR, sampleRate);
			minCh = Math.min(minCh, channels);
			maxCh = Math.max(maxCh, channels);
			nRead++;
		}

		if (nRead == 0) {
			return new SoundFileSummary(files.size(), 0, 0, 0, 0, 0, anySud);
		}
		return new SoundFileSummary(files.size(), nRead, minSR, maxSR, minCh, maxCh, anySud);
	}

	/**
	 * Whether the format of at least one file was read, so that the sample rate and
	 * channel figures mean something.
	 *
	 * @return true if the summary can be used for filtering.
	 */
	public boolean isValid() {
		return formatsRead > 0;
	}

	/**
	 * The number of sound files found.
	 * @return the file count.
	 */
	public int getFileCount() {
		return fileCount;
	}

	/**
	 * The number of files whose audio format was successfully read.
	 * @return the number of formats read.
	 */
	public int getFormatsRead() {
		return formatsRead;
	}

	/**
	 * The lowest sample rate across the imported files. This is the figure to test
	 * a configuration's minimum sample rate against.
	 *
	 * @return the minimum sample rate in Hz.
	 */
	public float getMinSampleRate() {
		return minSampleRate;
	}

	/**
	 * The highest sample rate across the imported files.
	 * @return the maximum sample rate in Hz.
	 */
	public float getMaxSampleRate() {
		return maxSampleRate;
	}

	/**
	 * The fewest channels in any of the imported files. This is the figure to test
	 * a configuration's minimum channel count against.
	 *
	 * @return the minimum channel count.
	 */
	public int getMinChannels() {
		return minChannels;
	}

	/**
	 * The most channels in any of the imported files.
	 * @return the maximum channel count.
	 */
	public int getMaxChannels() {
		return maxChannels;
	}

	/**
	 * Whether any of the imported files are SoundTrap sud files.
	 * @return true if at least one sud file is present.
	 */
	public boolean hasSudFiles() {
		return hasSudFiles;
	}

	/**
	 * Whether the imported files have more than one sample rate or channel count
	 * between them. Sound acquisition can only be set up with a single format, so
	 * the user should be warned when this is true.
	 *
	 * @return true if the files do not all share one format.
	 */
	public boolean isMixedFormats() {
		return minSampleRate != maxSampleRate || minChannels != maxChannels;
	}

	/**
	 * A short description of the format, for the wizard's scan summary.
	 * @return a human readable description.
	 */
	public String getFormatDescription() {
		if (!isValid()) {
			return "unknown format";
		}
		StringBuilder text = new StringBuilder();
		if (minSampleRate == maxSampleRate) {
			text.append(formatRate(minSampleRate));
		}
		else {
			text.append(formatRate(minSampleRate)).append(" to ").append(formatRate(maxSampleRate));
		}
		text.append(", ");
		if (minChannels == maxChannels) {
			text.append(minChannels).append(minChannels == 1 ? " channel" : " channels");
		}
		else {
			text.append(minChannels).append(" to ").append(maxChannels).append(" channels");
		}
		return text.toString();
	}

	/**
	 * Format a sample rate in Hz or kHz, whichever reads better, dropping any
	 * trailing zeros so that 500000 shows as "500 kHz" and 44100 as "44.1 kHz".
	 *
	 * @param sampleRate the sample rate in Hz.
	 * @return the formatted rate.
	 */
	public static String formatRate(float sampleRate) {
		if (sampleRate < 1000) {
			return String.format("%.0f Hz", sampleRate);
		}
		double kHz = sampleRate / 1000.;
		if (kHz == Math.rint(kHz)) {
			return String.format("%.0f kHz", kHz);
		}
		if (kHz * 10 == Math.rint(kHz * 10)) {
			return String.format("%.1f kHz", kHz);
		}
		return String.format("%.3f kHz", kHz);
	}

	@Override
	public String toString() {
		return String.format("%d sound file(s), %s", fileCount, getFormatDescription());
	}
}
