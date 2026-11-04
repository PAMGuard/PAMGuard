package PamController.startup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import PamController.PamSettingManager;

/**
 * Keeps the lists of recently opened and favourite settings files (normal mode)
 * and databases (viewer mode) shown in the start up dialog.
 * <p>
 * The lists are held in a small text file in the PAMGuard settings folder,
 * separate from the older recent file lists (the psg files), which are left
 * alone so that the original start up dialogs keep working exactly as before.
 * The older lists are merged in whenever the start up dialog opens so that
 * files used without the new dialog still appear.
 *
 * @author Jamie Macaulay
 */
public class StartupHistory {

	/**
	 * Normal (real time) mode - the lists hold psfx settings files.
	 */
	public static final int MODE_NORMAL = 0;

	/**
	 * Viewer mode - the lists hold databases.
	 */
	public static final int MODE_VIEWER = 1;

	/**
	 * Maximum number of non favourite files kept for each mode.
	 */
	public static final int MAX_RECENT = 30;

	private static final String HISTORY_FILE = "startupHistory.txt";

	private static final String LINE_MODE = "MODE";
	private static final String LINE_ENTRY = "E";
	private static final String LINE_HIDDEN = "H";

	private static StartupHistory singleInstance;

	private final ArrayList<StartupEntry> entries = new ArrayList<>();

	/**
	 * Files the user removed from the list. Kept so that they don't keep coming
	 * back from the older recent file lists.
	 */
	private final ArrayList<StartupEntry> hidden = new ArrayList<>();

	private int lastMode = MODE_NORMAL;

	private boolean loaded = false;

	private StartupHistory() {
	}

	public static synchronized StartupHistory getInstance() {
		if (singleInstance == null) {
			singleInstance = new StartupHistory();
		}
		return singleInstance;
	}

	/**
	 * @return the file the history is stored in.
	 */
	public static File getHistoryFile() {
		return new File(pamguard.Pamguard.getSettingsFolder() + File.separator + HISTORY_FILE);
	}

	/**
	 * Load the history from file (once only).
	 */
	public synchronized void load() {
		if (loaded) {
			return;
		}
		loaded = true;
		entries.clear();
		hidden.clear();
		File f = getHistoryFile();
		if (!f.exists()) {
			return;
		}
		try (BufferedReader reader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					parseLine(line);
				}
				catch (Exception e) {
					// skip a bad line, keep the rest
				}
			}
		}
		catch (IOException e) {
			System.out.println("StartupHistory: unable to read " + f + ": " + e.getMessage());
		}
	}

	private void parseLine(String line) {
		String[] bits = line.split("\t");
		if (bits.length == 0) {
			return;
		}
		switch (bits[0]) {
		case LINE_MODE:
			lastMode = Integer.parseInt(bits[1].trim()) == MODE_VIEWER ? MODE_VIEWER : MODE_NORMAL;
			break;
		case LINE_ENTRY:
			// E mode favourite lastOpened path
			bits = line.split("\t", 5);
			StartupEntry e = new StartupEntry(new File(bits[4]), Integer.parseInt(bits[1]),
					Long.parseLong(bits[3]), "1".equals(bits[2]));
			if (find(entries, e.getFile(), e.getMode()) == null) {
				entries.add(e);
			}
			break;
		case LINE_HIDDEN:
			// H mode path
			bits = line.split("\t", 3);
			hidden.add(new StartupEntry(new File(bits[2]), Integer.parseInt(bits[1]), 0, false));
			break;
		}
	}

	/**
	 * Save the history to file.
	 * @return true if successful.
	 */
	public synchronized boolean save() {
		File f = getHistoryFile();
		File tmp = new File(f.getAbsolutePath() + ".tmp");
		try (BufferedWriter writer = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8)) {
			writer.write(LINE_MODE + "\t" + lastMode);
			writer.newLine();
			for (StartupEntry e : entries) {
				writer.write(String.format("%s\t%d\t%s\t%d\t%s", LINE_ENTRY, e.getMode(), e.isFavourite() ? "1" : "0",
						e.getLastOpened(), e.getFile().getAbsolutePath()));
				writer.newLine();
			}
			for (StartupEntry e : hidden) {
				writer.write(String.format("%s\t%d\t%s", LINE_HIDDEN, e.getMode(), e.getFile().getAbsolutePath()));
				writer.newLine();
			}
		}
		catch (IOException e) {
			System.out.println("StartupHistory: unable to write " + tmp + ": " + e.getMessage());
			return false;
		}
		try {
			Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			System.out.println("StartupHistory: unable to write " + f + ": " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Merge in the files from the older recent file lists, i.e. the psfx files
	 * listed by the original settings file dialog and the Sqlite databases listed
	 * by the original viewer database dialog. Files already in the history, or
	 * that the user removed from the list, are left alone.
	 */
	public synchronized void importOldRecentLists() {
		load();
		try {
			List<File> psfFiles = PamSettingManager.getInstance().getRecentSettingsFiles();
			addOldFiles(psfFiles, MODE_NORMAL);
		}
		catch (Exception e) {
			System.out.println("StartupHistory: unable to read recent settings files: " + e.getMessage());
		}
		try {
			List<File> dbFiles = PamSettingManager.getInstance().getRecentViewerDatabases();
			addOldFiles(dbFiles, MODE_VIEWER);
		}
		catch (Exception e) {
			System.out.println("StartupHistory: unable to read recent databases: " + e.getMessage());
		}
	}

	private void addOldFiles(List<File> files, int mode) {
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file == null || !file.exists()) {
				continue;
			}
			if (find(entries, file, mode) != null || find(hidden, file, mode) != null) {
				continue;
			}
			entries.add(new StartupEntry(file, mode, file.lastModified(), false));
		}
	}

	/**
	 * Get the files for one mode.
	 * @param mode - {@link #MODE_NORMAL} or {@link #MODE_VIEWER}.
	 * @param favouritesOnly - true for the favourites only.
	 * @return the files, most recently opened first.
	 */
	public synchronized List<StartupEntry> getEntries(int mode, boolean favouritesOnly) {
		load();
		ArrayList<StartupEntry> list = new ArrayList<>();
		for (StartupEntry e : entries) {
			if (e.getMode() == mode && (!favouritesOnly || e.isFavourite())) {
				list.add(e);
			}
		}
		list.sort(Comparator.comparingLong(StartupEntry::getLastOpened).reversed());
		return list;
	}

	/**
	 * Record that a file has been opened (or used) in PAMGuard. Puts it at the top of
	 * the recent list for that mode.
	 * @param file - the file.
	 * @param mode - the mode.
	 */
	public synchronized void recordOpened(File file, int mode) {
		if (file == null) {
			return;
		}
		load();
		StartupEntry e = find(entries, file, mode);
		if (e == null) {
			e = new StartupEntry(file, mode, System.currentTimeMillis(), false);
			entries.add(e);
		}
		e.setLastOpened(System.currentTimeMillis());
		StartupEntry h = find(hidden, file, mode);
		if (h != null) {
			hidden.remove(h);
		}
		trim(mode);
	}

	/**
	 * Set or clear the favourite flag of a file.
	 */
	public synchronized void setFavourite(StartupEntry entry, boolean favourite) {
		StartupEntry e = find(entries, entry.getFile(), entry.getMode());
		if (e != null) {
			e.setFavourite(favourite);
		}
		entry.setFavourite(favourite);
	}

	/**
	 * Remove a file from the lists (doesn't touch the file itself).
	 */
	public synchronized void remove(StartupEntry entry) {
		StartupEntry e = find(entries, entry.getFile(), entry.getMode());
		if (e != null) {
			entries.remove(e);
		}
		if (find(hidden, entry.getFile(), entry.getMode()) == null) {
			hidden.add(new StartupEntry(entry.getFile(), entry.getMode(), 0, false));
		}
	}

	/**
	 * Keep the number of non favourite files down to {@link #MAX_RECENT}.
	 */
	private void trim(int mode) {
		List<StartupEntry> list = getEntries(mode, false);
		int n = 0;
		for (StartupEntry e : list) {
			if (e.isFavourite()) {
				continue;
			}
			if (++n > MAX_RECENT) {
				entries.remove(e);
			}
		}
	}

	/**
	 * @return the mode chosen last time the start up dialog was used.
	 */
	public synchronized int getLastMode() {
		load();
		return lastMode;
	}

	public synchronized void setLastMode(int lastMode) {
		this.lastMode = lastMode;
	}

	private static StartupEntry find(List<StartupEntry> list, File file, int mode) {
		for (Iterator<StartupEntry> it = list.iterator(); it.hasNext();) {
			StartupEntry e = it.next();
			if (e.matches(file, mode)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Check whether two files are the same, case insensitive on Windows.
	 */
	static boolean sameFile(File a, File b) {
		if (a == null || b == null) {
			return false;
		}
		String pa = a.getAbsolutePath();
		String pb = b.getAbsolutePath();
		if (File.separatorChar == '\\') {
			return pa.equalsIgnoreCase(pb);
		}
		return pa.equals(pb);
	}

}
