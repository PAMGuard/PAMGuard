package difar;

import difar.offline.ViewerClipStore;

import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clipgenerator.ClipDisplayDataBlock;
import difar.dataSelector.DifarDataSelectCreator;

public class DifarDataBlock extends ClipDisplayDataBlock<DifarDataUnit> {

	private DifarProcess difarProcess;
	private DifarControl difarControl;
	private DifarDataSelectCreator dataSelectCreator;
	private boolean isDifarQueue;

	/** Keeps clips saved and deleted in the viewer; saved clips only, made when first needed. */
	private ViewerClipStore viewerClipStore;

	/**
	 * Second copies of clips, noted while an upgrade runs so they are left out
	 * when their files are rewritten. Null at other times.
	 */
	private volatile Set<String> secondCopies;

	public DifarDataBlock(String dataName, DifarControl difarControl, boolean isDifarQueue,
			DifarProcess parentProcess, int channelMap) {
		super(DifarDataUnit.class, dataName, parentProcess, channelMap);

		this.difarProcess = parentProcess;
		this.difarControl = difarControl;
		this.isDifarQueue = isDifarQueue;
		
		if (!isDifarQueue)
			addLocalisationContents(LocContents.HAS_BEARING);
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public synchronized void clearAll() {
		if (shouldClear()) {
			super.clearAll();
		}
	}
	
	/**
	 * Work out whether or not queues should be cleared at start. 
	 * @return true if queue shoudld be cleared. 
	 */
	private boolean shouldClear() {
		if (difarControl.isViewer()) return true;
		if (isDifarQueue) {
			return difarControl.getDifarParameters().clearQueueAtStart;
		}
		else {
			return difarControl.getDifarParameters().clearProcessedDataAtStart;
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
	@Override
	public DataSelectorCreator getDataSelectCreator() {
		if (dataSelectCreator == null) {
			dataSelectCreator = new DifarDataSelectCreator(difarControl, this);
		}
		return dataSelectCreator;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		// TODO Auto-generated method stub
		return super.removeOldUnitsT(currentTimeMS);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		// 	return super.removeOldUnitsS(mastrClockSample);
		/* r4577 changed super.removeOldUnitsS(long) such that PamDataUnits
		 * are cleared when acquisition starts. This might be fine for other modules
		 * but is not OK for DIFAR module. The code below should still removes old  
		 * units, but does not clear them on acquisition start. 
		 */ 
		if (pamDataUnits.isEmpty())
			return 0;
		PamDataUnit pamUnit;

		// now take a time back from the last unit and use this as master time if it's less than mastrClockSample. 
		pamUnit = (PamDataUnit) getLastUnit();
		if (pamUnit == null) {
			return 0;
		}
		mastrClockSample = Math.min(mastrClockSample, pamUnit.getStartSample());

		long minKeepSamples = 0;
		float sr = getSampleRate();
		if (this.getNaturalLifetime() == 0) {
			minKeepSamples = (long) (sr > 100000 ? sr / 2 : sr);
		}
		else {
			minKeepSamples = (long) (this.naturalLifetime/1000. * sr);
		}

		//			long firstWantedTime = (long) (this.naturalLifetime/1000. * getSampleRate());
		long keepTime = (long) (getRequiredHistory() / 1000. * getSampleRate());
		long firstWantedTime = mastrClockSample - Math.max(minKeepSamples, keepTime);



		int unitsJustRemoved=0;
		while (!pamDataUnits.isEmpty()) {
			pamUnit = (PamDataUnit) pamDataUnits.get(0);
			if (pamUnit.getStartSample() + pamUnit.getSampleDuration() > firstWantedTime) {
				break;
			}
			pamDataUnits.remove(0);

			removedDataUnit((DifarDataUnit) pamUnit);

			//				unitsRemoved++;
			unitsJustRemoved++;
		}
		unitsRemoved+=unitsJustRemoved;
		return unitsJustRemoved;
	}


	/**
	 * @return the store for clips saved in the viewer, or null for the queue.
	 */
	public synchronized ViewerClipStore getViewerClipStore() {
		if (isDifarQueue) {
			return null;
		}
		if (viewerClipStore == null) {
			viewerClipStore = new ViewerClipStore(this, difarControl);
		}
		return viewerClipStore;
	}

	/**
	 * The viewer calls this before loading any new stretch of data, on File,
	 * Save, and on exit. Clips saved or deleted in the viewer are written
	 * first. Then the core save rewrites, in place, any other file holding a
	 * clip that has changed.
	 * <p>
	 * If the viewer save is cancelled, the core save is skipped too. Clips
	 * made in the viewer have no binary file until they are saved, and the
	 * core save cannot handle a changed clip without one.
	 */
	@Override
	public boolean saveViewerData() {
		if (viewerClipStore != null && !viewerClipStore.compact()) {
			return false;
		}
		return super.saveViewerData();
	}

	/**
	 * In the viewer, a clip is kept once even if it is found in two files.
	 * Nothing should write a clip twice, but if something ever does, this
	 * turns a silent duplicate into a message.
	 */
	@Override
	public void addPamData(DifarDataUnit pamDataUnit, Long uid) {
		if (difarControl.isViewer() && uid != null && uid > 0) {
			DifarDataUnit first = findUnitByUIDandUTC(uid, pamDataUnit.getTimeMilliseconds());
			// the same clip only: older datasets can give different clips the same UID
			if (first != null && first.getTimeMilliseconds() == pamDataUnit.getTimeMilliseconds()
					&& first.getChannelBitmap() == pamDataUnit.getChannelBitmap()) {
				System.out.printf("DIFAR: skipped a second copy of clip UID %d\n", uid);
				noteSecondCopy(uid, pamDataUnit.getChannelBitmap(), pamDataUnit.getTimeMilliseconds());
				return;
			}
		}
		super.addPamData(pamDataUnit, uid);
	}

	/**
	 * @param uid a clip's UID, as stored.
	 * @param channelMap its channel map.
	 * @param timeMillis its start time.
	 * @return a key that is the same for two copies of one clip.
	 */
	public static String clipKey(long uid, int channelMap, long timeMillis) {
		return uid + "|" + channelMap + "|" + timeMillis;
	}

	/**
	 * Start noting second copies of clips, so that rewriting their files
	 * leaves them out. Called by the upgrade.
	 */
	public void startCollectingSecondCopies() {
		secondCopies = ConcurrentHashMap.newKeySet();
	}

	/**
	 * Stop noting second copies.
	 * @return how many were noted.
	 */
	public int stopCollectingSecondCopies() {
		Set<String> copies = secondCopies;
		int n = copies == null ? 0 : copies.size();
		secondCopies = null;
		return n;
	}

	/**
	 * Note a second copy of a clip, if an upgrade is collecting them.
	 * @param uid the UID stored with the copy.
	 * @param channelMap its channel map.
	 * @param timeMillis its start time.
	 */
	public void noteSecondCopy(long uid, int channelMap, long timeMillis) {
		Set<String> copies = secondCopies;
		if (copies != null) {
			copies.add(clipKey(uid, channelMap, timeMillis));
		}
	}

	/**
	 * @param uid the UID stored with a clip read from a file.
	 * @param channelMap its channel map.
	 * @param timeMillis its start time.
	 * @return true if it is a second copy noted by a running upgrade.
	 */
	public boolean isSecondCopy(long uid, int channelMap, long timeMillis) {
		Set<String> copies = secondCopies;
		return copies != null && copies.contains(clipKey(uid, channelMap, timeMillis));
	}

	@Override
	public void addPamData(DifarDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		super.addPamData(pamDataUnit);
		if (!difarControl.isViewer()){
			this.sortData();
		}
	}

	/**
	 * Called when a cal value is set. Goes through any difar units including
	 * calibration ones are on that channel and may need to update their bearing. 
	 * @param calibrationStartTime2
	 * @param channel TODO
	 * @param originStartTime TODO
	 */
	public void clearOldOrigins(int channel, long originStartTime) {
		synchronized (getSynchLock()) {
			ListIterator<DifarDataUnit> it = getListIterator(PamDataBlock.ITERATOR_END);
			int chanMap = 1<<channel;
			while (it.hasPrevious()) {
				DifarDataUnit dataUnit = it.previous();
				if (dataUnit.getChannelBitmap() != chanMap) {
					continue;
				}
				if (dataUnit.getTimeMilliseconds() < originStartTime) {
					break;
				}
				dataUnit.clearOandAngles();
			}
		}
		
	}
	
}
