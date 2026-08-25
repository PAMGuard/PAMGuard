package PamController.pamWizard;

/**
 * The types of data file that can be recognised when files are dragged into a
 * blank PAMGuard configuration. {@link #SOUND}, {@link #SUD_CLICKS},
 * {@link #CPOD} and {@link #FPOD} are scanned for; {@link #BINARY} is a
 * placeholder so that auto-configurations which view PAMGuard binary data can be
 * added later without changing the framework.
 *
 * @author Jamie Macaulay
 */
public enum PamImportFileType {

	/** Raw audio / sound files (wav, etc.). */
	SOUND("Sound files"),

	/** FPOD detection files. */
	FPOD("FPOD files"),

	/** CPOD detection files. */
	CPOD("CPOD files"),

	/** PAMGuard binary data files. */
	BINARY("PAMGuard binary files"),

	/**
	 * SoundTrap sud files which were recorded with the click detector running, and
	 * so contain click detections that can be extracted while the audio is
	 * processed. Note that this is about the detections inside the file: a sud file
	 * is always also a {@link #SOUND} file.
	 */
	SUD_CLICKS("SoundTrap sud files with click detections");

	private final String name;

	PamImportFileType(String name) {
		this.name = name;
	}

	/**
	 * A human readable name for the file type.
	 * @return the name of the file type.
	 */
	public String getName() {
		return name;
	}
}
