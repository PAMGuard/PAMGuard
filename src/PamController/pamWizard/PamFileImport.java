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
