package PamguardMVC.superdet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import PamController.PamController;
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

/**
 * Loads the sub detections of super detections straight out of the binary store.
 * <p>
 * In viewer mode a super detection always holds the full list of
 * {@link SubdetectionInfo}s, but the sub detection data units themselves are
 * only attached while they are within the currently loaded time period. Offline
 * tools which need all the clicks of an event (e.g. generating a spectrum
 * template or training a classifier) therefore see empty events for anything
 * outside the current scroll window.
 * <p>
 * This class reads the missing units back out of the binary files. The loaded
 * units are handed back to the caller and are <em>not</em> attached to the super
 * detection, so nothing about the state of the currently loaded data is changed.
 *
 * @author Jamie Macaulay
 */
public class SubDetectionLoader {

	/**
	 * Callback for load progress, e.g. to drive a progress bar.
	 */
	public interface LoadProgress {
		/**
		 * @param fraction - fraction of the load completed, 0 to 1.
		 * @param message  - a human readable description of the current step.
		 */
		void update(double fraction, String message);
	}

	/**
	 * Margin (milliseconds) added either side of the wanted data when working out
	 * which binary files to search.
	 */
	private static final long FILE_SEARCH_MARGIN_MILLIS = 1000;

	private SubDetectionLoader() {
	}

	/**
	 * Get the sub detections of a list of super detections, loading any which are
	 * not currently in memory from the binary store.
	 *
	 * @param superDetections - the super detections to get sub detections for.
	 * @param progress        - progress callback, may be null.
	 * @return a list of sub detection lists, one per super detection and in the same
	 *         order. Sub detections which could not be loaded are simply missing.
	 */
	@SuppressWarnings("rawtypes")
	public static List<List<PamDataUnit>> loadSubDetections(List<? extends SuperDetection> superDetections,
			LoadProgress progress) {

		List<List<PamDataUnit>> loaded = new ArrayList<>(superDetections.size());

		// everything that has to come out of the store, grouped by sub detection data block.
		Map<String, WantedUnits> wantedBlocks = new HashMap<>();

		for (int i = 0; i < superDetections.size(); i++) {
			List<PamDataUnit> subDets = new ArrayList<>();
			loaded.add(subDets);
			List<SubdetectionInfo> infos = superDetections.get(i).getSubDetectionInfo();
			if (infos == null) {
				continue;
			}
			for (SubdetectionInfo info : infos) {
				PamDataUnit subDet = (PamDataUnit) info.getSubDetection();
				if (subDet != null) {
					subDets.add(subDet);
					continue;
				}
				String longName = info.getLongName();
				if (longName == null) {
					continue;
				}
				WantedUnits wanted = wantedBlocks.get(longName);
				if (wanted == null) {
					wantedBlocks.put(longName, wanted = new WantedUnits());
				}
				wanted.add(info.getChildUID(), info.getChildUTC(), i);
			}
		}

		if (wantedBlocks.isEmpty()) {
			return loaded;
		}

		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			System.err.println("SubDetectionLoader: no binary store, cannot load unloaded sub detections.");
			return loaded;
		}

		// work out the total number of files up front so progress can be reported over
		// all data blocks rather than restarting for each one.
		int nFiles = 0;
		for (Map.Entry<String, WantedUnits> entry : wantedBlocks.entrySet()) {
			WantedUnits wanted = entry.getValue();
			wanted.mapPoints = findMapPoints(binaryStore, entry.getKey(), wanted);
			nFiles += wanted.mapPoints.size();
		}
		if (nFiles == 0) {
			return loaded;
		}

		int iFile = 0;
		for (Map.Entry<String, WantedUnits> entry : wantedBlocks.entrySet()) {
			WantedUnits wanted = entry.getValue();
			for (BinaryOfflineDataMapPoint mapPoint : wanted.mapPoints) {
				if (progress != null) {
					progress.update(iFile / (double) nFiles,
							String.format("Loading data: file %d of %d", iFile + 1, nFiles));
				}
				iFile++;
				binaryStore.loadData(wanted.dataBlock, mapPoint, wanted.firstUTC, wanted.lastUTC,
						new CollectingSink(wanted, loaded));
			}
		}
		if (progress != null) {
			progress.update(1, "");
		}

		// units already in memory were added before the ones read from file, so put
		// each list back into time order.
		for (List<PamDataUnit> subDets : loaded) {
			subDets.sort((a, b) -> Long.compare(a.getTimeMilliseconds(), b.getTimeMilliseconds()));
		}

		return loaded;
	}

	/**
	 * Find the binary files which hold the wanted units of one sub detection data
	 * block. Also sets the data block on the wanted units.
	 *
	 * @param binaryStore - the binary store.
	 * @param longName    - the long data name of the sub detection data block.
	 * @param wanted      - the units wanted from that data block.
	 * @return the list of files to search, empty if the data block or its map cannot
	 *         be found.
	 */
	private static List<BinaryOfflineDataMapPoint> findMapPoints(BinaryStore binaryStore, String longName,
			WantedUnits wanted) {
		wanted.dataBlock = PamController.getInstance().getDataBlockByLongName(longName);
		if (wanted.dataBlock == null) {
			System.err.printf("SubDetectionLoader: cannot find sub detection data block %s\n", longName);
			return new ArrayList<>();
		}
		if (!(wanted.dataBlock.getOfflineDataMap(binaryStore) instanceof BinaryOfflineDataMap)) {
			System.err.printf("SubDetectionLoader: %s has no binary data map\n", longName);
			return new ArrayList<>();
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) wanted.dataBlock.getOfflineDataMap(binaryStore);
		// map point times come from the file headers and footers, which don't always
		// exactly bracket the data, so search a little either side.
		return dataMap.getFileList(wanted.firstUTC - FILE_SEARCH_MARGIN_MILLIS,
				wanted.lastUTC + FILE_SEARCH_MARGIN_MILLIS);
	}

	/**
	 * The units wanted from a single sub detection data block: which UIDs, which
	 * super detections in the output list they belong to, and the time range they
	 * span.
	 */
	private static class WantedUnits {

		/** UID of a wanted unit to the indexes of the super detections wanting it. */
		private final Map<Long, List<Integer>> uidIndexes = new HashMap<>();

		private long firstUTC = Long.MAX_VALUE;

		private long lastUTC = Long.MIN_VALUE;

		private PamDataBlock dataBlock;

		private List<BinaryOfflineDataMapPoint> mapPoints;

		private void add(long uid, long utc, int superDetIndex) {
			List<Integer> indexes = uidIndexes.get(uid);
			if (indexes == null) {
				uidIndexes.put(uid, indexes = new ArrayList<>(1));
			}
			indexes.add(superDetIndex);
			firstUTC = Math.min(firstUTC, utc);
			lastUTC = Math.max(lastUTC, utc);
		}
	}

	/**
	 * Binary data sink which keeps the wanted data units and throws everything else
	 * away, so that memory use stays close to the size of the data actually asked
	 * for rather than the size of the files being read.
	 */
	private static class CollectingSink implements BinaryDataSink {

		private final WantedUnits wanted;

		private final List<List<PamDataUnit>> loaded;

		private CollectingSink(WantedUnits wanted, List<List<PamDataUnit>> loaded) {
			this.wanted = wanted;
			this.loaded = loaded;
		}

		@Override
		public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit) {
			List<Integer> indexes = wanted.uidIndexes.get(dataUnit.getUID());
			if (indexes != null) {
				// the unit is never added to the data block, but it still needs to know where it
				// came from for anything asking it for e.g. its sample rate.
				dataUnit.setParentDataBlock(dataBlock);
				for (Integer index : indexes) {
					loaded.get(index).add(dataUnit);
				}
			}
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

}
