package PamController.pamWizard;

import java.io.File;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.FolderInputParameters;
import Acquisition.FolderInputSystem;
import PamController.PamController;
import dataMap.filemaps.OfflineFileParameters;
import dataMap.filemaps.OfflineFileServer;

/**
 * Sets a Sound Acquisition module up to read a set of imported sound files.
 * <p>
 * Shared by every configuration the import wizard can build, so that the various
 * details of getting this right - the DC time constant fix, and the three calls
 * the acquisition dialog makes when it is closed - live in one place rather than
 * being repeated in each builder.
 *
 * @author Jamie Macaulay
 */
public class AcquisitionConfigurer {

	/**
	 * Point a sound acquisition module at a set of imported files.
	 *
	 * @param acquisitionControl the acquisition module.
	 * @param files              the imported and scanned files.
	 * @param viewer             true to also enable the offline file server, which
	 *                           viewer mode needs in order to build a data map.
	 * @return true if the module was configured.
	 */
	public static boolean configure(AcquisitionControl acquisitionControl, PamFileImport files, boolean viewer) {
		if (acquisitionControl == null || files == null) {
			return false;
		}

		AcquisitionParameters params = acquisitionControl.getAcquisitionParameters();
		FolderInputSystem folderSystem = acquisitionControl.getFolderSystem();

		// select the folder/file input system.
		params.setDaqSystemType(folderSystem.getSystemType());

		/*
		 * Ensure a sane DC-subtraction time constant. The default is 0, which is
		 * normally corrected to 1.0 in AcquisitionParameters.clone() when a config is
		 * loaded - but we are building the config live and mutating the parameters
		 * directly, so we must apply it ourselves. A zero time constant with
		 * subtractDC=true makes the DC-removal filter blow up, producing infinite raw
		 * samples and hence an all-zero (blank) spectrogram.
		 */
		if (params.dcTimeConstant <= 0) {
			params.dcTimeConstant = 1.0;
		}

		// sample rate / channels from the scanned audio.
		SoundFileSummary summary = files.getSoundSummary();
		if (summary != null && summary.isValid()) {
			params.setSampleRate(summary.getMinSampleRate());
			params.setNChannels(summary.getMinChannels());
		}

		// point the folder system at the dropped files/folder.
		FolderInputParameters folderParams = folderSystem.getFolderInputParameters();
		List<File> dropped = files.getDroppedFiles();
		folderParams.setSelectedFiles(dropped.toArray(new File[0]));
		folderParams.subFolders = true;

		// viewer: enable + point the offline file server so createOfflineDataMap works.
		if (viewer) {
			OfflineFileServer offlineServer = acquisitionControl.getOfflineFileServer();
			if (offlineServer != null) {
				OfflineFileParameters ofp = offlineServer.getOfflineFileParameters();
				ofp.enable = true;
				ofp.includeSubFolders = true;
				ofp.folderName = getFolderName(dropped);
				offlineServer.setOfflineFileParameters(ofp);
			}
		}

		/*
		 * Apply the settings the same way the acquisition dialog does when it is closed
		 * (see AcquisitionControl.acquisitionSettings): select the system, set up the
		 * array channels and (re)build the raw data block so the sample rate and channel
		 * count propagate to everything downstream.
		 */
		acquisitionControl.setSelectedSystem();
		acquisitionControl.checkArrayChannels(PamController.getMainFrame());
		acquisitionControl.getAcquisitionProcess().setupDataBlock();

		return true;
	}

	/**
	 * The folder holding the imported files, used by the offline file server.
	 *
	 * @param dropped the dropped files and folders.
	 * @return the folder path, or null if there is nothing to use.
	 */
	public static String getFolderName(List<File> dropped) {
		if (dropped == null || dropped.isEmpty()) {
			return null;
		}
		File first = dropped.get(0);
		return first.isDirectory() ? first.getAbsolutePath() : first.getParent();
	}
}
