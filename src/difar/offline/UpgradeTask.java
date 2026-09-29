package difar.offline;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSink;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import dataMap.OfflineDataMapPoint;
import difar.DifarClipPayload;
import difar.DifarControl;
import difar.DifarDataUnit;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskGroupParams;

/**
 * Upgrades a dataset written by an older version of PAMGuard, so it can be
 * edited in the viewer.
 * <p>
 * Files before DIFAR module version 3 hold each clip's crossing inside the
 * clip, and are read only. The upgrade:
 * <ol>
 * <li>reads every DIFAR file once and checks that no two different clips
 * share a UID. If some do, it stops, since renumbering is not built yet;</li>
 * <li>backs up every DIFAR binary file, beside the binary store, and does
 * nothing more if the backup fails;</li>
 * <li>rematches every clip, exactly as {@link RematchTask} does, making
 * crossing units;</li>
 * <li>marks each clip from an older file as changed, so PAMGuard rewrites its
 * file at version 3.</li>
 * </ol>
 * The old crossings were made by older code, and stay in the backup only, as a
 * record of what was thought at the time. The new crossings are the best
 * current estimate.
 * <p>
 * Run it over all data, so that matching can find partners anywhere in the
 * dataset. Replaces the old copy to database task, which rewrote the files as
 * a side effect, with no backup and no rematch.
 */
public class UpgradeTask extends OfflineTask<DifarDataUnit> {

	private final DifarControl difarControl;
	private final RematchTask rematch;

	/**
	 * True from a prepareTask that backed up the files, to completeTask. Core
	 * completes every task in a group, even those not ticked or not prepared.
	 */
	private boolean backedUp;
	private int upgraded;

	/**
	 * @param difarControl the DIFAR module.
	 */
	public UpgradeTask(DifarControl difarControl) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarControl = difarControl;
		this.rematch = new RematchTask(difarControl, null);
		addRequiredDataBlock(difarControl.getDifarProcess().getCrossingDataBlock());
		addAffectedDataBlock(difarControl.getDifarProcess().getCrossingDataBlock());
		addAffectedDataBlock(getDataBlock());
	}

	@Override
	public String getName() {
		return "Upgrade old DIFAR files to version " + DifarClipPayload.CURRENT_VERSION;
	}

	@Override
	public void prepareTask() {
		upgraded = 0;
		backedUp = false;
		if (getOfflineTaskGroup().getTaskGroupParams().dataChoice != TaskGroupParams.PROCESS_ALL) {
			warn("The upgrade runs over all data only, so nothing was upgraded. "
					+ "Choose all data and run it again.");
			return;
		}
		UidCensus census = takeCensus();
		if (census == null) {
			return;
		}
		System.out.println("DIFAR: " + census.summary());
		if (!census.isUnique()) {
			warn(String.format("%d different clips share %d UIDs, probably because the UID count "
					+ "restarted when PAMGuard crashed during recording. The upgrade needs each clip "
					+ "to have its own UID, and cannot renumber them yet, so nothing was upgraded.",
					census.getClipsSharingUIDs(), census.getSharedUIDs()));
			return;
		}
		if (census.getExactDuplicates() > 0) {
			System.out.printf("DIFAR: %d clips are stored twice. The upgrade keeps both copies for now\n",
					census.getExactDuplicates());
		}
		backedUp = backUp();
		if (backedUp) {
			rematch.prepareTask();
		}
	}

	/**
	 * Show a warning, and print it to the console.
	 * @param message the warning.
	 */
	private void warn(String message) {
		System.out.println("DIFAR: " + message);
		// the task runs off the Swing thread
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(difarControl.getGuiFrame(),
				message, getName(), JOptionPane.WARNING_MESSAGE));
	}

	/**
	 * Read every DIFAR binary file once, without loading its clips into the
	 * data block, and count how their UIDs are used.
	 * @return the count, or null if there are no files or one cannot be read.
	 */
	private UidCensus takeCensus() {
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			System.out.println("DIFAR: no binary store, so nothing was upgraded");
			return null;
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) getDataBlock().getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			System.out.println("DIFAR: no DIFAR binary files, so nothing was upgraded");
			return null;
		}
		UidCensus census = new UidCensus();
		CensusSink sink = new CensusSink(census);
		for (BinaryOfflineDataMapPoint mapPoint : dataMap.getMapPoints()) {
			if (!binaryStore.loadData(getDataBlock(), mapPoint, Long.MIN_VALUE, Long.MAX_VALUE, sink)) {
				warn("Could not read " + mapPoint.getBinaryFile(binaryStore) + ", so nothing was upgraded.");
				return null;
			}
		}
		return census;
	}

	/**
	 * Counts each clip read from a file, and keeps nothing else.
	 */
	private static class CensusSink implements BinaryDataSink {

		private final UidCensus census;

		private CensusSink(UidCensus census) {
			this.census = census;
		}

		@Override
		public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit) {
			census.add(dataUnit.getUID(), dataUnit.getChannelBitmap(), dataUnit.getTimeMilliseconds());
			return true;
		}

		@Override
		public void newFileHeader(BinaryHeader binaryHeader) {
		}

		@Override
		public void newModuleHeader(BinaryObjectData binaryObjectData, ModuleHeader moduleHeader) {
		}

		@Override
		public void newModuleFooter(BinaryObjectData binaryObjectData, ModuleFooter moduleFooter) {
		}

		@Override
		public void newFileFooter(BinaryObjectData binaryObjectData, BinaryFooter binaryFooter) {
		}

		@Override
		public void newDatagram(BinaryObjectData binaryObjectData) {
		}
	}

	/**
	 * Copy every DIFAR binary file, with its index and noise files.
	 * @return true if every copy succeeded.
	 */
	private boolean backUp() {
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			System.out.println("DIFAR: no binary store, so nothing was upgraded");
			return false;
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) getDataBlock().getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			System.out.println("DIFAR: no DIFAR binary files, so nothing was upgraded");
			return false;
		}
		ViewerBackup backup = new ViewerBackup(new File(binaryStore.getBinaryStoreSettings().getStoreLocation()));
		int nFiles = 0;
		try {
			for (BinaryOfflineDataMapPoint mapPoint : dataMap.getMapPoints()) {
				File data = mapPoint.getBinaryFile(binaryStore);
				backup.copy(data);
				backup.copy(binaryStore.swapFileType(data, BinaryStore.indexFileType));
				backup.copy(binaryStore.swapFileType(data, BinaryStore.noiseFileType));
				nFiles++;
			}
		}
		catch (IOException e) {
			System.out.println("DIFAR: backup failed, so nothing was upgraded: " + e);
			return false;
		}
		System.out.printf("DIFAR: %d files backed up to %s before upgrading\n", nFiles, backup.getBackupRoot());
		return true;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		rematch.newDataLoad(startTime, endTime, mapPoint);
	}

	/**
	 * @return true for a clip from an older file, so its file is rewritten at
	 * the current version.
	 */
	@Override
	public boolean processDataUnit(DifarDataUnit clip) {
		if (!backedUp) {
			return false;
		}
		rematch.processDataUnit(clip);
		if (clip.getBinaryVersion() >= DifarClipPayload.CURRENT_VERSION) {
			return false;
		}
		clip.setBinaryVersion(DifarClipPayload.CURRENT_VERSION);
		upgraded++;
		return true;
	}

	@Override
	public void loadedDataComplete() {
		if (backedUp) {
			rematch.loadedDataComplete();
		}
	}

	@Override
	public void completeTask() {
		if (!backedUp) {
			return;
		}
		backedUp = false;
		System.out.printf("DIFAR: upgrade rewrote %d clips at version %d\n", upgraded, DifarClipPayload.CURRENT_VERSION);
		rematch.completeTask();
	}
}
