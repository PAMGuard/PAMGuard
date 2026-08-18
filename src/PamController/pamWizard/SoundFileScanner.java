package PamController.pamWizard;

import java.io.File;
import java.util.List;

import PamController.PamController;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import PamUtils.worker.filelist.WavListUser;
import PamUtils.worker.filelist.WavListWorker;

/**
 * Scans dropped files/folders for audio (sound) files, using the existing
 * {@link WavListWorker}. This is currently the only implemented
 * {@link PamFileTypeScanner}; it is what enables the spectrogram-viewer
 * auto-configurations.
 *
 * @author Jamie Macaulay
 */
public class SoundFileScanner implements PamFileTypeScanner {

	private WavListWorker wavListWorker;

	@Override
	public PamImportFileType getFileType() {
		return PamImportFileType.SOUND;
	}

	@Override
	public void scan(List<File> droppedFiles, FileScanComplete callback) {
		String[] rootList = PamWizardManager.filesToPathArray(droppedFiles);

		wavListWorker = new WavListWorker(new WavListUser() {
			@Override
			public void newFileList(FileListData<WavFileType> fileListData) {
				int count = (fileListData == null) ? 0 : fileListData.getFileCount();
				/*
				 * The audio formats were read by the worker above, so summarising here is
				 * cheap. Doing it now means the sample rate and channel count are known
				 * before the user is offered any configurations.
				 */
				SoundFileSummary summary = SoundFileSummary.summarise(fileListData);
				callback.scanComplete(new PamFileTypeResult(PamImportFileType.SOUND, count, fileListData, summary));
			}
		});

		/*
		 * Read the audio format of every file as the list is built. This happens on the
		 * worker thread with a progress dialog, which is the right place for it - the
		 * first read of a sud file has to build a map of the file and is slow.
		 */
		wavListWorker.loadAudioInfo = true;

		// scan sub-folders, and re-use a previously computed list if available.
		wavListWorker.startFileListProcess(PamController.getMainFrame(), rootList, true, true);
	}
}
