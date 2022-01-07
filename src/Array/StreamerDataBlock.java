package Array;

import GPS.NavDataSynchronisation;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class StreamerDataBlock extends PamDataBlock<StreamerDataUnit>{

	public static final String dataName = "Streamer Data";

	private HydrophoneProcess hydrophoneProcess;

	public StreamerDataBlock(HydrophoneProcess hydrophoneProcess) {
		super(StreamerDataUnit.class, dataName, hydrophoneProcess, 0);
		this.hydrophoneProcess = hydrophoneProcess;
		setSynchLock(NavDataSynchronisation.getSynchobject());
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public  void clearAll() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW ||
				PamController.getInstance().getRunMode() == PamController.RUN_MIXEDMODE) {
			super.clearAll();
		}
	}

	/**
	 * Used by DIFAR module to clear streamers in real-time
	 */
	public void forceClearAll() {
		super.clearAll();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected  int removeOldUnitsT(long currentTimeMS) {
		clearChannelIterators();
		return 0;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected  int removeOldUnitsS(long mastrClockSample) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getPreceedingUnit(long, int)
	 */
	@Override
	public  StreamerDataUnit getPreceedingUnit(long startTime,
			int channelMap) {
		// perfect for finding preceding settings for a particular time. 
		return super.getPreceedingUnit(startTime, channelMap);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getNumRequiredBeforeLoadTime()
	 */
	@Override
	public int getNumRequiredBeforeLoadTime() {
		return ArrayManager.getArrayManager().getCurrentArray().getNumStreamers()*100;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getShouldLog(PamguardMVC.PamDataUnit)
	 */
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return super.getShouldLog(pamDataUnit);
	}

	public void replaceStreamerDataUnit(StreamerDataUnit oldUnit, StreamerDataUnit newUnit){
		int index = getUnitIndex(oldUnit);
		if (index != -1) {
			pamDataUnits.set(index, newUnit);    
			if (shouldNotify()) {
				setChanged();
				notifyObservers(newUnit);
			}
			notifyOfflineObservers(newUnit);
		}
	}

}
