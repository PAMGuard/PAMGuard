package PamController.pamWizard;

/**
 * The result of a {@link PamFileTypeScanner} examining a set of dropped files:
 * which file type was looked for, how many matching files were found, and an
 * optional type-specific payload (for {@link PamImportFileType#SOUND} this is the
 * {@code FileListData<WavFileType>} of scanned audio files).
 *
 * @author Jamie Macaulay
 */
public class PamFileTypeResult {

	private final PamImportFileType fileType;

	private final int fileCount;

	private final Object data;

	private final SoundFileSummary soundSummary;

	public PamFileTypeResult(PamImportFileType fileType, int fileCount, Object data) {
		this(fileType, fileCount, data, null);
	}

	public PamFileTypeResult(PamImportFileType fileType, int fileCount, Object data, SoundFileSummary soundSummary) {
		this.fileType = fileType;
		this.fileCount = fileCount;
		this.data = data;
		this.soundSummary = soundSummary;
	}

	/**
	 * The file type this result is for.
	 * @return the file type.
	 */
	public PamImportFileType getFileType() {
		return fileType;
	}

	/**
	 * The number of matching files found.
	 * @return the file count.
	 */
	public int getFileCount() {
		return fileCount;
	}

	/**
	 * Type-specific payload describing the matched files. For
	 * {@link PamImportFileType#SOUND} this is a {@code FileListData<WavFileType>}.
	 * @return the payload, or null.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * A summary of the audio format of the matched files. Only set for
	 * {@link PamImportFileType#SOUND} results.
	 *
	 * @return the sound file summary, or null.
	 */
	public SoundFileSummary getSoundSummary() {
		return soundSummary;
	}

	/**
	 * @return true if any matching files of this type were found.
	 */
	public boolean hasFiles() {
		return fileCount > 0;
	}
}
