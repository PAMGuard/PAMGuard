package difar;

import java.util.ListIterator;

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
		if (naturalLifetime == 0) {
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
