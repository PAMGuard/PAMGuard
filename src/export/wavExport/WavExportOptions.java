package export.wavExport;

import java.io.Serializable;

/**
 * Options for exporting wav files
 */
public class WavExportOptions implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Save detections as zero padded. 
	 */
	public static final int SAVEWAV_ZERO_PAD = 0;
	
	/**
	 * Save detections as concatenated.
	 */
	public static final int SAVEWAV_CONCAT = 1;

	/**
	 * Save detections as individual wav files. 
	 */
	public static final int SAVEWAV_INDIVIDUAL = 2;

	/**
	 * Flag to indicuate how to save files
	 */
	public int wavSaveChoice = SAVEWAV_CONCAT;

}
