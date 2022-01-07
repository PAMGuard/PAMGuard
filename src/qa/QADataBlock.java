package qa;

import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import pamScrollSystem.ViewLoadObserver;

/**
 * Modified base datablock so that we can have data reloaded for analysis during real time operations. 
 * does cause some problems with SuperDetectionDatablock though since it insists that the 
 * logging is SuperDetLogging. 
 * @author dg50
 *
 * @param <Tunit>
 */
@Deprecated
public class QADataBlock<Tunit extends QADataUnit> extends SuperDetDataBlock<Tunit, QADataUnit> {

	private boolean isGenerator;
	private boolean isAnalyser;
	private QADataProcess qaDataProcess;
	
	private final int minLifetimeSecs = 300;

	private QADataBlock(boolean isGenerator, Class unitClass, String dataName, QADataProcess qaDataProcess) {
		super(unitClass, dataName, qaDataProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.isGenerator = isGenerator;
		this.isAnalyser = !isGenerator;
		this.qaDataProcess = qaDataProcess;
		setNaturalLifetime(300);
	}

	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		if (isGenerator) {
			return super.removeOldUnitsT(currentTimeMS);
		}
		else {
			return 0;
		}
	}

	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		if (isGenerator) {
			return super.removeOldUnitsS(mastrClockSample);
		}
		else {
			return 0;
		}
	}

	@Override
	public int getNaturalLifetime() {
		return Math.max(super.getNaturalLifetime(), this.minLifetimeSecs);
	}

	@Override
	public int getNaturalLifetimeMillis() {
	 return Math.max(super.getNaturalLifetimeMillis(), minLifetimeSecs*1000);
	}
	

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getFirstViewerUID()
	 */
	@Override
	public long getFirstViewerUID() {
		if (isGenerator) {
			return super.getFirstViewerUID();
		}
		else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getLastViewerUID()
	 */
	@Override
	public long getLastViewerUID() {
		if (isGenerator) {
			return super.getLastViewerUID();
		}
		else {
			return Long.MAX_VALUE;
		}
	}

	/**
	 * @return the isGenerator
	 */
	public boolean isGenerator() {
		return isGenerator;
	}

	/**
	 * @return the isAnalyser
	 */
	public boolean isAnalyser() {
		return isAnalyser;
	}


	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		qaDataProcess.findOpsDataUnits(this);
		return loadOk;
	}

}
