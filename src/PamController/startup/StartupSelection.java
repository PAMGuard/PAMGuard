package PamController.startup;

import java.io.File;

import PamController.PamController;

/**
 * What the user picked in the start up dialog: the run mode and the settings
 * file (normal mode) or database (viewer mode) to open.
 *
 * @author Jamie Macaulay
 */
public class StartupSelection {

	private final int mode;

	private final File file;

	private final Boolean darkMode;

	public StartupSelection(int mode, File file) {
		this(mode, file, null);
	}

	/**
	 * @param mode - the mode.
	 * @param file - the settings file or database.
	 * @param darkMode - true or false if the user chose dark or light mode in the
	 * dialog, null if they didn't use the switch.
	 */
	public StartupSelection(int mode, File file, Boolean darkMode) {
		this.mode = mode;
		this.file = file;
		this.darkMode = darkMode;
	}

	/**
	 * @return true or false if the user chose dark or light mode, or null if they
	 * didn't, in which case PAMGuard keeps the colour scheme it was last closed with.
	 */
	public Boolean getDarkMode() {
		return darkMode;
	}

	/**
	 * @return {@link StartupHistory#MODE_NORMAL} or {@link StartupHistory#MODE_VIEWER}.
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @return the PAMGuard run mode, e.g. {@link PamController#RUN_NORMAL}.
	 */
	public int getRunMode() {
		return mode == StartupHistory.MODE_VIEWER ? PamController.RUN_PAMVIEW : PamController.RUN_NORMAL;
	}

	/**
	 * @return the settings file or database to open.
	 */
	public File getFile() {
		return file;
	}

}
