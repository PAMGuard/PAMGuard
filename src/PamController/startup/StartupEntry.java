package PamController.startup;

import java.io.File;

/**
 * One file in the start up dialog's lists: either a settings (.psfx) file for
 * normal mode or a database for viewer mode.
 *
 * @author Jamie Macaulay
 */
public class StartupEntry {

	/**
	 * The file.
	 */
	private final File file;

	/**
	 * The run mode the file is used for, {@link StartupHistory#MODE_NORMAL} or
	 * {@link StartupHistory#MODE_VIEWER}.
	 */
	private final int mode;

	/**
	 * Time the file was last opened in PAMGuard (millis). If PAMGuard has never
	 * recorded opening the file (e.g. it came from one of the older recent file
	 * lists) this is the file's last modified time.
	 */
	private long lastOpened;

	/**
	 * True if the user has marked the file as a favourite.
	 */
	private boolean favourite;

	public StartupEntry(File file, int mode, long lastOpened, boolean favourite) {
		this.file = file.getAbsoluteFile();
		this.mode = mode;
		this.lastOpened = lastOpened;
		this.favourite = favourite;
	}

	public File getFile() {
		return file;
	}

	public int getMode() {
		return mode;
	}

	public long getLastOpened() {
		return lastOpened;
	}

	public void setLastOpened(long lastOpened) {
		this.lastOpened = lastOpened;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	/**
	 * @return true if the file exists on disk.
	 */
	public boolean exists() {
		return file.exists();
	}

	/**
	 * Check whether this entry refers to a given file in a given mode.
	 * @param aFile - the file.
	 * @param aMode - the mode.
	 * @return true if the same file and mode.
	 */
	public boolean matches(File aFile, int aMode) {
		return aMode == mode && StartupHistory.sameFile(file, aFile);
	}

	@Override
	public String toString() {
		return file.getAbsolutePath();
	}

}
