package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cpod.CPODUtils;
import cpod.CPODUtils.CPODFileType;

/**
 * Scans dropped files/folders for CPOD (CP1/CP3) or FPOD (FP1/FP3) detection
 * files. One instance is registered per {@link PamImportFileType}, i.e. one for
 * {@link PamImportFileType#CPOD} and one for {@link PamImportFileType#FPOD}, so
 * that a configuration can require one or the other, even though in practice
 * both are imported by the same CPOD module.
 * <p>
 * The scan is only a directory walk, but it still runs on its own thread since
 * it is started from the GUI thread and a dropped folder may be large or on slow
 * media.
 *
 * @author Jamie Macaulay
 */
public class PODFileScanner implements PamFileTypeScanner {

	/**
	 * How deep to recurse into dropped folders. The same limit is used by the sud
	 * file scanner, and it stops the walk if a folder tree turns out to be
	 * pathological (e.g. a symlink loop).
	 */
	private static final int MAX_FOLDER_DEPTH = 20;

	/**
	 * The import file type this scanner reports on.
	 */
	private final PamImportFileType fileType;

	/**
	 * The POD file types which count towards {@link #fileType}.
	 */
	private final CPODFileType[] podTypes;

	/**
	 * @param fileType either {@link PamImportFileType#CPOD} or
	 *                 {@link PamImportFileType#FPOD}.
	 */
	public PODFileScanner(PamImportFileType fileType) {
		this.fileType = fileType;
		switch (fileType) {
		case CPOD:
			podTypes = new CPODFileType[] { CPODFileType.CP1, CPODFileType.CP3 };
			break;
		case FPOD:
			podTypes = new CPODFileType[] { CPODFileType.FP1, CPODFileType.FP3 };
			break;
		default:
			throw new IllegalArgumentException("PODFileScanner can only scan for CPOD or FPOD files, not " + fileType);
		}
	}

	@Override
	public PamImportFileType getFileType() {
		return fileType;
	}

	@Override
	public void scan(List<File> droppedFiles, FileScanComplete callback) {
		Thread scanThread = new Thread(() -> {
			List<File> found = new ArrayList<File>();
			try {
				collectPODFiles(droppedFiles, found, 0);
				// sort so that files are imported in name (i.e. usually time) order.
				found.sort(Comparator.comparing(File::getName));
			}
			catch (Throwable e) {
				// never let a bad file or folder stop the wizard from opening.
				System.out.println("Error scanning for POD files: " + e.getMessage());
			}
			callback.scanComplete(new PamFileTypeResult(fileType, found.size(), found));
		}, "PODFileScanner (" + fileType.getName() + ")");
		scanThread.setDaemon(true);
		scanThread.start();
	}

	/**
	 * Recursively collect POD files of this scanner's type from the dropped files
	 * and folders.
	 *
	 * @param files files and folders to look in.
	 * @param found list to add found POD files to.
	 * @param depth current recursion depth.
	 */
	private void collectPODFiles(List<File> files, List<File> found, int depth) {
		if (files == null || depth > MAX_FOLDER_DEPTH) {
			return;
		}
		for (File file : files) {
			if (file == null || file.isHidden() || !file.exists()) {
				continue;
			}
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				if (children != null) {
					collectPODFiles(List.of(children), found, depth + 1);
				}
			}
			else if (isPODFile(file)) {
				found.add(file);
			}
		}
	}

	/**
	 * @return true if the file is a POD file of the type this scanner looks for.
	 */
	private boolean isPODFile(File file) {
		CPODFileType type = CPODUtils.getFileType(file);
		if (type == null) {
			return false;
		}
		for (CPODFileType podType : podTypes) {
			if (podType == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * All POD files - both CPOD and FPOD - found in a set of dropped files. The
	 * CPOD module imports both, so a configuration which views POD data generally
	 * wants them together.
	 *
	 * @param importHandler the imported and scanned files.
	 * @return a modifiable list of CP1/CP3/FP1/FP3 files. Never null.
	 */
	public static List<File> getPODFiles(PamFileImport importHandler) {
		List<File> podFiles = new ArrayList<File>();
		if (importHandler == null) {
			return podFiles;
		}
		addFiles(podFiles, importHandler.getResult(PamImportFileType.CPOD));
		addFiles(podFiles, importHandler.getResult(PamImportFileType.FPOD));
		return podFiles;
	}

	/**
	 * The number of POD files - both CPOD and FPOD - found in a set of dropped
	 * files.
	 *
	 * @param importHandler the imported and scanned files.
	 * @return the number of POD files.
	 */
	public static int getPODFileCount(PamFileImport importHandler) {
		return getPODFiles(importHandler).size();
	}

	/**
	 * Add the files held in a scan result to a list.
	 */
	private static void addFiles(List<File> podFiles, PamFileTypeResult result) {
		if (result == null || !(result.getData() instanceof List)) {
			return;
		}
		for (Object aFile : (List<?>) result.getData()) {
			if (aFile instanceof File) {
				podFiles.add((File) aFile);
			}
		}
	}
}
