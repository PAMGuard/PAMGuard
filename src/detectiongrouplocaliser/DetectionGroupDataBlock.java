package detectiongrouplocaliser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import detectiongrouplocaliser.DetectionGroupProcess.DataSelector;
import detectiongrouplocaliser.tethys.DetectionGroupSpeciesManager;
import detectiongrouplocaliser.tethys.DetectionGroupTethysProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

public class DetectionGroupDataBlock extends SuperDetDataBlock<DetectionGroupDataUnit, PamDataUnit> {

	private DetectionGroupProcess detectionGroupProcess;
	
	private DetectionGroupTethysProvider detectionGroupTethysProvider;
	
	private DetectionGroupSpeciesManager detectionGroupSpeciesManager;

	private DetectionGroupControl detectionGroupControl;

	public DetectionGroupDataBlock(String dataName, DetectionGroupControl detectionGroupControl, DetectionGroupProcess detectionGroupProcess) {
		super(DetectionGroupDataUnit.class, dataName, detectionGroupProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.detectionGroupControl = detectionGroupControl;
		this.detectionGroupProcess = detectionGroupProcess;
	}

	@Override
	public boolean saveViewerData() {
		return super.saveViewerData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public void addPamData(DetectionGroupDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
		this.sortData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit, java.lang.Long)
	 */
	@Override
	public void addPamData(DetectionGroupDataUnit pamDataUnit, Long uid) {
		super.addPamData(pamDataUnit, uid);
		this.sortData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#updatePamData(PamguardMVC.PamDataUnit, long)
	 */
	@Override
	public void updatePamData(DetectionGroupDataUnit pamDataUnit, long updateTimeMillis) {
		super.updatePamData(pamDataUnit, updateTimeMillis);
		this.sortData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		int n = super.removeOldUnitsT(currentTimeMS);
		if (n > 0) {
			detectionGroupProcess.getDetectionGroupControl().notifyGroupDataChanged();
		}
		return n;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		int n = super.removeOldUnitsS(mastrClockSample);
		if (n > 0) {
			detectionGroupProcess.getDetectionGroupControl().notifyGroupDataChanged();
		}
		return n;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#needViewerDataLoad(PamguardMVC.dataOffline.OfflineDataLoadInfo)
	 */
	@Override
	public boolean needViewerDataLoad(OfflineDataLoadInfo offlineDataLoadInfo) {
		// always reload htese data to make sure everything gets linked up correclty. 
		return true;
	}

	@Override
	public void clearAll() {
		super.clearAll();
		
		// also need to clear the flag holding onto the lastModifiedDataUnit, or else it will show up in the dialog as one of the options (when it shouldn't)
		detectionGroupProcess.getEventBuilderFunctions().clearLastModifiedDataUnit();
	}

	@Override
	public boolean canSuperDetection(PamDataBlock subDataBlock) {
		if (!super.canSuperDetection(subDataBlock)) {
			return false;
		}
		DataSelector ds = detectionGroupProcess.getDataSelector();
		return ds.wantDataBlock(subDataBlock);
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (detectionGroupTethysProvider == null) {
			detectionGroupTethysProvider = new DetectionGroupTethysProvider(tethysControl, this, detectionGroupControl);
		}
		return detectionGroupTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<DetectionGroupDataUnit> getDatablockSpeciesManager() {
		if (detectionGroupSpeciesManager == null) {
			detectionGroupSpeciesManager = new DetectionGroupSpeciesManager(this);
		}
		return detectionGroupSpeciesManager;
	}

}
