package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

import org.pamguard.x3.sud.SUDClickDetectorInfo;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileMap;

import Acquisition.pamAudio.PamAudioFileManager;
import soundtrap.STXMLFile;

/**
 * Scans dropped files for SoundTrap sud files which were recorded with the click
 * detector running, and so contain click detections alongside the audio.
 * <p>
 * This matters because some configurations only make sense for such files: a
 * configuration containing a SoundTrap Click Detector module extracts the stored
 * detections into PAMGuard binary files while the audio is processed, and there
 * is nothing for it to do if the sud files hold audio only.
 * <p>
 * Working out whether a sud file holds detections is done as cheaply as
 * possible, stopping at the first file that gives an answer:
 * <ol>
 * <li>the {@code .log.xml} file SoundTrap writes alongside the sud file, which
 * describes the detector settings;</li>
 * <li>the {@code .sudx} map file PAMGuard caches next to a sud file it has
 * already read;</li>
 * <li>failing both, the sud file itself is opened through
 * {@link PamAudioFileManager}, which builds and caches the map. This is slow the
 * first time, so only one file is ever opened this way.</li>
 * </ol>
 * Scanning runs on its own thread since any of these can touch the disk.
 *
 * @author Jamie Macaulay
 */
public class SudClickScanner implements PamFileTypeScanner {

	/**
	 * Extension of a SoundTrap sud file.
	 */
	private static final String SUD_EXTENSION = ".sud";

	/**
	 * How deep to recurse into dropped folders. The same limit is used by the sound
	 * file scanner via its file list worker.
	 */
	private static final int MAX_FOLDER_DEPTH = 20;

	@Override
	public PamImportFileType getFileType() {
		return PamImportFileType.SUD_CLICKS;
	}

	@Override
	public void scan(List<File> droppedFiles, FileScanComplete callback) {
		Thread scanThread = new Thread(() -> {
			int count = 0;
			try {
				count = countClickFiles(droppedFiles);
			}
			catch (Throwable e) {
				// never let a bad file stop the wizard from opening.
				System.out.println("Error scanning for SoundTrap click detections: " + e.getMessage());
			}
			callback.scanComplete(new PamFileTypeResult(PamImportFileType.SUD_CLICKS, count, null));
		}, "SudClickScanner");
		scanThread.setDaemon(true);
		scanThread.start();
	}

	/**
	 * Find the sud files and work out whether they hold click detections.
	 *
	 * @param droppedFiles the dropped files and folders.
	 * @return the number of sud files present if they hold detections, otherwise zero.
	 */
	private int countClickFiles(List<File> droppedFiles) {
		List<File> sudFiles = new ArrayList<>();
		collectSudFiles(droppedFiles, sudFiles, 0);
		if (sudFiles.isEmpty()) {
			return 0;
		}

		// the cheap checks, over every file.
		for (File sudFile : sudFiles) {
			Boolean fromXML = hasDetectionsFromXML(sudFile);
			if (fromXML != null) {
				return fromXML ? sudFiles.size() : 0;
			}
			Boolean fromMap = hasDetectionsFromSudx(sudFile);
			if (fromMap != null) {
				return fromMap ? sudFiles.size() : 0;
			}
		}

		// nothing cached, so read one file the slow way.
		Boolean fromFile = hasDetectionsFromSudFile(sudFiles.get(0));
		if (fromFile != null && fromFile) {
			return sudFiles.size();
		}
		return 0;
	}

	/**
	 * Recursively collect sud files from the dropped files and folders.
	 *
	 * @param files    files and folders to look in.
	 * @param sudFiles list to add found sud files to.
	 * @param depth    current recursion depth.
	 */
	private void collectSudFiles(List<File> files, List<File> sudFiles, int depth) {
		if (files == null || depth > MAX_FOLDER_DEPTH) {
			return;
		}
		for (File file : files) {
			if (file == null || file.isHidden()) {
				continue;
			}
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				if (children != null) {
					collectSudFiles(List.of(children), sudFiles, depth + 1);
				}
			}
			else if (file.getName().toLowerCase().endsWith(SUD_EXTENSION)) {
				sudFiles.add(file);
			}
		}
	}

	/**
	 * Look for detector information in the {@code .log.xml} file SoundTrap writes
	 * next to the sud file.
	 * <p>
	 * Note that {@link STXMLFile#findXMLFile(File)} only knows about wav files, so
	 * the xml name is built here from the sud file name.
	 *
	 * @param sudFile the sud file.
	 * @return true or false if the xml file answered the question, null if there is
	 *         no readable xml file.
	 */
	private Boolean hasDetectionsFromXML(File sudFile) {
		String path = sudFile.getAbsolutePath();
		int dot = path.toLowerCase().lastIndexOf(SUD_EXTENSION);
		if (dot < 0) {
			return null;
		}
		File xmlFile = new File(path.substring(0, dot) + ".log.xml");
		if (!xmlFile.exists()) {
			return null;
		}
		try {
			STXMLFile xml = STXMLFile.openXMLFile(xmlFile);
			if (xml == null) {
				return null;
			}
			SUDClickDetectorInfo info = xml.getSudDetectorInfo();
			return isRunning(info);
		}
		catch (Throwable e) {
			System.out.println("Unable to read SoundTrap xml file " + xmlFile.getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Look for detector information in the {@code .sudx} map file PAMGuard caches
	 * beside a sud file it has already read.
	 *
	 * @param sudFile the sud file.
	 * @return true or false if the map answered the question, null if there is no
	 *         readable map file.
	 */
	private Boolean hasDetectionsFromSudx(File sudFile) {
		File sudxFile = new File(sudFile.getAbsolutePath() + "x");
		if (!sudxFile.exists()) {
			return null;
		}
		try {
			SudFileMap sudMap = SudAudioInputStream.loadSudMap(sudxFile);
			if (sudMap == null) {
				return null;
			}
			return isRunning(sudMap.detectorInfo);
		}
		catch (Throwable e) {
			// an out of date or partly written map file - fall through to the next check.
			return null;
		}
	}

	/**
	 * Open the sud file to build its map and read the detector information from it.
	 * This goes through {@link PamAudioFileManager} rather than the x3 library
	 * directly, so that the map is built and cached exactly as it would be by any
	 * other part of PAMGuard, and so that concurrent reads of the same file are
	 * handled properly.
	 *
	 * @param sudFile the sud file.
	 * @return true or false if the file answered the question, null if it could not
	 *         be read.
	 */
	private Boolean hasDetectionsFromSudFile(File sudFile) {
		AudioInputStream stream = null;
		try {
			stream = PamAudioFileManager.getInstance().getAudioInputStream(sudFile);
			if (stream instanceof SudAudioInputStream) {
				SudFileMap sudMap = ((SudAudioInputStream) stream).getSudMap();
				if (sudMap != null) {
					return isRunning(sudMap.detectorInfo);
				}
			}
		}
		catch (Throwable e) {
			System.out.println("Unable to read sud file " + sudFile.getName() + ": " + e.getMessage());
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {
					// nothing useful to do here.
				}
			}
		}
		return null;
	}

	/**
	 * Whether a set of detector information describes a click detector that was
	 * actually running. A detector with no sample rate was not, so there will be no
	 * detections in the file.
	 *
	 * @param info the detector information, may be null.
	 * @return true if the click detector was running.
	 */
	private boolean isRunning(SUDClickDetectorInfo info) {
		return info != null && info.sampleRate > 0;
	}
}
