package PamController.pamWizard;

import java.io.File;
import java.util.List;

/**
 * Examines a set of dropped files/folders and reports whether (and how many)
 * files of a particular {@link PamImportFileType} are present. This is the main
 * extension point of the drag-and-drop importer: to support a new file type
 * (e.g. FPOD or CPOD detection files) add a new scanner and register it with the
 * {@link PamWizardManager}.
 * <p>
 * Scanning may be asynchronous (the sound-file scan runs on a worker thread), so
 * the result is delivered via a {@link FileScanComplete} callback rather than
 * returned directly.
 *
 * @author Jamie Macaulay
 */
public interface PamFileTypeScanner {

	/**
	 * The file type this scanner looks for.
	 * @return the file type.
	 */
	public PamImportFileType getFileType();

	/**
	 * Scan the dropped files/folders for files of this scanner's type. The result
	 * is delivered to the callback when scanning completes (which may be on another
	 * thread). Implementations must always call back exactly once, even if no files
	 * are found (with a zero-count result).
	 *
	 * @param droppedFiles the files and folders that were dropped.
	 * @param callback     called when the scan is complete.
	 */
	public void scan(List<File> droppedFiles, FileScanComplete callback);

	/**
	 * Callback for a completed {@link PamFileTypeScanner#scan}.
	 */
	public interface FileScanComplete {
		public void scanComplete(PamFileTypeResult result);
	}
}
