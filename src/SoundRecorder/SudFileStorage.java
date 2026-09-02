package SoundRecorder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;

import org.pamguard.x3.sud.SudAudioOutputStream;

import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Stores recorder audio as a SoundTrap .sud file using X3 lossless compression.
 * <p>
 * SUD files are the native format of SoundTrap devices. PAMGuard can already
 * read them via {@code SudAudioInputStream}; this storage option lets the Sound
 * Recorder module <em>write</em> them too, using {@link SudAudioOutputStream}.
 * <p>
 * Audio is always written as 16-bit PCM (the only bit depth X3 supports),
 * regardless of the bit depth requested in the recorder settings. The incoming
 * {@code double[channel][sample]} blocks are converted to interleaved 16-bit
 * samples using the same scaling as PAMGuard's WAV writer
 * ({@code (short)(value * 32768.0)}) and split into fixed-size X3 chunks before
 * being handed to the output stream.
 *
 * @author PAMGuard
 * @see SoundRecorder.RecorderStorage
 */
public class SudFileStorage implements RecorderStorage {

	/**
	 * Number of audio samples (per channel) written per X3 chunk. Keeping this
	 * modest ensures the per-chunk compressed payload stays well within the 16-bit
	 * {@code DataLength} field of a SUD chunk header, even at high channel counts.
	 */
	private static final int CHUNK_SAMPLES = 1000;

	/** Scale factor to convert normalised (-1..1) doubles to 16-bit samples. */
	private static final double SHORT_SCALE = 32768.0;

	private final RecorderControl recorderControl;

	private Type audioFileType;
	
	private float sampleRate;

	private int nChannels;

	private long fileStartMillis;

	private long lastDataTime;

	/** Total number of frames (samples per channel) written to the current file. */
	private long totalFrames;

	private String fileName;

	private SudAudioOutputStream sudStream;

	/** Counts the physical (compressed) bytes flushed to the file. */
	private CountingOutputStream countingStream;

	/** Reused interleave buffer, sized {@code CHUNK_SAMPLES * nChannels}. */
	private short[] interleaveBuffer;

	private final PamWarning sudWriteWarning;

	public SudFileStorage(RecorderControl recorderControl) {
		this.recorderControl = recorderControl;
		sudWriteWarning = new PamWarning(recorderControl.getUnitName(), "SUD Write problem", 2);
	}

	@Override
	public boolean openStorage(Type fileType, long recordingStart, float sampleRate, int nChannels, int bitDepth) {
		closeStorage();
		this.audioFileType = fileType;
		this.sampleRate = sampleRate;
		this.nChannels = nChannels;
		// SUD/X3 is always 16-bit, so the requested bitDepth is ignored.
		return openStorage(recordingStart);
	}

	private boolean openStorage(long recordingStart) {
		lastDataTime = fileStartMillis = recordingStart;
		totalFrames = 0;
		interleaveBuffer = new short[CHUNK_SAMPLES * nChannels];

		String fileExtension = "." + audioFileType.getExtension();
		File outFolder = FileFunctions.getStorageFileFolder(recorderControl.recorderSettings.outputFolder,
				recordingStart, recorderControl.recorderSettings.datedSubFolders, true);
		if (outFolder == null) {
			outFolder = new File(recorderControl.recorderSettings.outputFolder);
		}
		fileName = PamCalendar.createFileNameMillis(recordingStart, outFolder.getAbsolutePath(),
				recorderControl.recorderSettings.fileInitials + "_", fileExtension);

		File f = new File(fileName);
		if (f.exists()) {
			try {
				f.delete();
			} catch (Exception e) {
				System.out.println("Unable to delete existing sud file: " + e.getMessage());
			}
		}

		try {
			countingStream = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			sudStream = new SudAudioOutputStream(countingStream, (int) sampleRate, nChannels, CHUNK_SAMPLES,
					recordingStart);
		} catch (IOException e) {
			reportError("Unable to open SUD file " + fileName + ": " + e.getLocalizedMessage());
			sudStream = null;
			countingStream = null;
			fileName = null;
			return false;
		}
		return true;
	}

	@Override
	public boolean reOpenStorage(long recordingStart) {
		closeStorage();
		return openStorage(recordingStart);
	}

	@Override
	public boolean addData(long dataTimeMillis, double[][] newData) {
		if (sudStream == null || newData == null || newData.length != nChannels) {
			return false;
		}
		lastDataTime = dataTimeMillis;
		int nFrames = newData[0].length;
		int written = 0;
		try {
			while (written < nFrames) {
				int n = Math.min(CHUNK_SAMPLES, nFrames - written);
				// Interleave [ch0s0, ch1s0, ..., ch0s1, ...] as required by SudAudioOutputStream.
				for (int ch = 0; ch < nChannels; ch++) {
					double[] chanData = newData[ch];
					for (int i = 0; i < n; i++) {
						interleaveBuffer[i * nChannels + ch] = (short) (chanData[written + i] * SHORT_SCALE);
					}
				}
				sudStream.write(interleaveBuffer, n);
				written += n;
			}
		} catch (IOException e) {
			reportError("Write error to SUD file: " + e.getLocalizedMessage());
			return false;
		}
		clearWarning();
		totalFrames += nFrames;
		return true;
	}

	@Override
	public boolean closeStorage() {
		if (sudStream == null) {
			return false;
		}
		try {
			sudStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sudStream = null;
		countingStream = null;
		fileName = null;
		// Log the completed recording, matching PamAudioFileStorage behaviour.
		recorderControl.recorderProcess.storageClosed();
		return true;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public long getFileSizeBytes() {
		if (countingStream == null) {
			return 0;
		}
		return countingStream.getCount();
	}

	@Override
	public long getFileFrames() {
		return totalFrames;
	}

	@Override
	public long getFileMilliSeconds() {
		return lastDataTime - fileStartMillis;
	}

	@Override
	public long getFileStartTime() {
		return fileStartMillis;
	}

	@Override
	public long getMaxFileSizeBytes() {
		// SUD chunk timestamps use a 32-bit unsigned seconds field, so there is no
		// practical absolute size limit; file splitting is governed by the user's
		// length/size settings (which are expressed in uncompressed terms).
		return Integer.MAX_VALUE;
	}

	private void reportError(String message) {
		sudWriteWarning.setWarningMessage(message);
		sudWriteWarning.setWarnignLevel(2);
		WarningSystem.getWarningSystem().addWarning(sudWriteWarning);
	}

	private void clearWarning() {
		if (sudWriteWarning.getWarnignLevel() > 0) {
			sudWriteWarning.setWarnignLevel(0);
			WarningSystem.getWarningSystem().removeWarning(sudWriteWarning);
		}
	}

	/**
	 * A minimal {@link FilterOutputStream} that counts the bytes passing through
	 * it, so the recorder can report the (compressed) physical file size.
	 */
	private static class CountingOutputStream extends FilterOutputStream {

		private long count = 0;

		CountingOutputStream(OutputStream out) {
			super(out);
		}

		long getCount() {
			return count;
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			count++;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			count += len;
		}
	}
}
