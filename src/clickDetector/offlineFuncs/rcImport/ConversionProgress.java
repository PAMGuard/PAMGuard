package clickDetector.offlineFuncs.rcImport;

import java.io.File;

public class ConversionProgress {
	static public final int STATE_IDLE = 0;
	static public final int STATE_RUNNING = 1;
	static public final int STATE_COUNTFILES = 2;
	static public final int STATE_DONE = 3;
	static public final int STATE_IMPORTEVENTS = 4;
	static public final int STATE_IMPORTCLICKS = 5;
	public static final int STATE_DONECLICKS = 6;
	public int state;
	public int totalFiles;
	public int convertedFiles;
	public int currentFileLength;
	public int currentFilePosition;
	public File currentFile;
	public ConversionProgress(int state, int totalFiles, int convertedFiles,
			int currentFileLength, int currentFilePosition, File file) {
		super();

		this.state = state;
		this.totalFiles = totalFiles;
		this.convertedFiles = convertedFiles;
		this.currentFileLength = currentFileLength;
		this.currentFilePosition = currentFilePosition;
		this.currentFile = file;
	}
	/**
	 * @return the totalFiles
	 */
	public int getTotalFiles() {
		return totalFiles;
	}
	/**
	 * @return the convertedFiles
	 */
	public int getConvertedFiles() {
		return convertedFiles;
	}
	/**
	 * @return the currentFileLength
	 */
	public int getCurrentFileLength() {
		return currentFileLength;
	}
	/**
	 * @return the currentFilePosition
	 */
	public int getCurrentFilePosition() {
		return currentFilePosition;
	}
	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}
}
