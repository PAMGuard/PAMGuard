package difar.calibration;

import pamScrollSystem.ViewLoadObserver;
import Array.ArrayManager;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import difar.DifarControl;
import difar.DifarProcess;

public class CalibrationDataBlock extends PamDataBlock<CalibrationDataUnit>{

	public static final String dataName = "Sonobuoy Calibration Data";
	private DifarProcess difarProcess;
	
	public CalibrationDataBlock(DifarProcess difarProcess) {
		super(CalibrationDataUnit.class, dataName, difarProcess, 0);
		this.parentProcess = difarProcess;
		this.difarProcess = difarProcess;
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public synchronized void clearAll() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW ||
				PamController.getInstance().getRunMode() == PamController.RUN_MIXEDMODE) {
			super.clearAll();
		}
	}

	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		clearChannelIterators();
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#loadViewerData(long, long, pamScrollSystem.ViewLoadObserver)
	 */
	@Override
	public boolean loadViewerData(long dataStart, long dataEnd,
			ViewLoadObserver loadObserver) {
		return super.loadViewerData(dataStart, dataEnd, loadObserver);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getPreceedingUnit(long, int)
	 */
	@Override
	public synchronized CalibrationDataUnit getPreceedingUnit(long startTime,
			int channelMap) {
		// perfect for finding preceding settings for a particular time. 
		return super.getPreceedingUnit(startTime, channelMap);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getNumRequiredBeforeLoadTime()
	 */
	@Override
	public int getNumRequiredBeforeLoadTime() {
		return 10; // Try to get at least one calibration data unit per channel
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getShouldLog(PamguardMVC.PamDataUnit)
	 */
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return super.getShouldLog(pamDataUnit);
	}
	
}
