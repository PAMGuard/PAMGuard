package PamController.pamWizard;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;

/**
 * Holds information on a set of dropped files, aggregated by file type. Each
 * registered {@link PamFileTypeScanner} contributes a {@link PamFileTypeResult},
 * so an auto-configuration can ask which types are present (e.g. "are both FPOD
 * detection files and sound files here?") and act on the combination.
 *
 * @author Jamie Macaulay
 */
public class PamFileImport {

	private final List<File> droppedFiles;

	private final Map<PamImportFileType, PamFileTypeResult> results = new EnumMap<>(PamImportFileType.class);

	public PamFileImport(List<File> droppedFiles) {
		this.droppedFiles = droppedFiles;
	}

	/**
	 * Add the result of a file-type scan.
	 * @param result the scan result.
	 */
	public void addResult(PamFileTypeResult result) {
		if (result != null) {
			results.put(result.getFileType(), result);
		}
	}

	/**
	 * The raw list of files/folders that were dropped.
	 * @return the dropped files.
	 */
	public List<File> getDroppedFiles() {
		return droppedFiles;
	}

	/**
	 * Whether any files of the given type were found.
	 * @param fileType the file type.
	 * @return true if at least one matching file is present.
	 */
	public boolean hasType(PamImportFileType fileType) {
		PamFileTypeResult result = results.get(fileType);
		return result != null && result.hasFiles();
	}

	/**
	 * Get the scan result for a file type.
	 * @param fileType the file type.
	 * @return the result, or null if that type was not scanned for / not found.
	 */
	public PamFileTypeResult getResult(PamImportFileType fileType) {
		return results.get(fileType);
	}

	/**
	 * A summary of the audio format of the imported sound files - the sample rates
	 * and channel counts present. This is what configurations are filtered against.
	 *
	 * @return the sound file summary, or null if no sound files were scanned.
	 */
	public SoundFileSummary getSoundSummary() {
		PamFileTypeResult result = results.get(PamImportFileType.SOUND);
		return (result == null) ? null : result.getSoundSummary();
	}

	/**
	 * The number of files of a given type that were found.
	 *
	 * @param fileType the file type.
	 * @return the file count, or zero if that type was not scanned for / not found.
	 */
	public int getFileCount(PamImportFileType fileType) {
		PamFileTypeResult result = results.get(fileType);
		return (result == null) ? 0 : result.getFileCount();
	}

	/**
	 * A short description of everything found among the imported files apart from
	 * the sound files themselves - sud files holding click detections, CPOD/FPOD
	 * detection files, and so on. Shown on the first page of the import wizard so
	 * that it is obvious what PAMGuard made of what was dropped on it.
	 * <p>
	 * Built here rather than in the wizards so that the Swing and JavaFX versions
	 * always say the same thing.
	 *
	 * @return the description, or an empty string if nothing else was found.
	 */
	public String getOtherTypesDescription() {
		StringBuilder types = new StringBuilder();

		SoundFileSummary summary = getSoundSummary();
		if (summary != null && summary.hasSudFiles()) {
			append(types, "SoundTrap sud files");
		}
		if (hasType(PamImportFileType.SUD_CLICKS)) {
			append(types, "click detections");
		}
		int cpodFiles = getFileCount(PamImportFileType.CPOD);
		if (cpodFiles > 0) {
			append(types, cpodFiles + " CPOD file" + (cpodFiles == 1 ? "" : "s"));
		}
		int fpodFiles = getFileCount(PamImportFileType.FPOD);
		if (fpodFiles > 0) {
			append(types, fpodFiles + " FPOD file" + (fpodFiles == 1 ? "" : "s"));
		}

		return types.toString();
	}

	/**
	 * Add an item to a comma separated list.
	 */
	private static void append(StringBuilder list, String item) {
		if (list.length() > 0) {
			list.append(", ");
		}
		list.append(item);
	}

	/**
	 * Convenience accessor for the scanned sound files.
	 * @return the audio file list data, or null if no sound files were found.
	 */
	@SuppressWarnings("unchecked")
	public FileListData<WavFileType> getSoundFiles() {
		PamFileTypeResult result = results.get(PamImportFileType.SOUND);
		if (result == null || !(result.getData() instanceof FileListData)) {
			return null;
		}
		return (FileListData<WavFileType>) result.getData();
	}
}
