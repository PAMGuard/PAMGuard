package bearinglocaliser.offline;

import java.util.ListIterator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingProcess;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class BLOfflineTask extends OfflineTask {

	private BearingLocaliserControl bearingLocaliserControl;
	private BearingProcess bearingProcess;
	private PamDataBlock detectionBlock, rawOrFFTBlock;
	
	public BLOfflineTask(BearingLocaliserControl bearingLocaliserControl) {
		super(bearingLocaliserControl, bearingLocaliserControl.getDetectionMonitor().getParentDataBlock());
		this.bearingLocaliserControl = bearingLocaliserControl;
		bearingProcess = bearingLocaliserControl.getBearingProcess();
		checkDataBlocks();
//		PamDataBlock detectionSource = bearingLocaliserControl.getDetectionMonitor().getParentDataBlock();
//		this.setParentDataBlock(detectionSource);
//		setParentDataBlock(bearingProcess.getParentDataBlock());
	}
	
	/**
	 *  do checks on the datablocks. Needed when this is running as an offline task since
	 *  if was probably impossible to set these when it was constructed. 
	 */
	public void checkDataBlocks() {
		detectionBlock = bearingLocaliserControl.getDetectionMonitor().getParentDataBlock();
		this.addRequiredDataBlock(rawOrFFTBlock = bearingProcess.getParentDataBlock());
		setParentDataBlock(detectionBlock);
		addAffectedDataBlock(detectionBlock);
		
	}

	@Override
	public String getName() {
		if (bearingLocaliserControl == null) {
			return null;
		}
		return bearingLocaliserControl.getUnitName();
	}

	@Override
	public boolean hasSettings() {
		return true;
	}
	@Override
	public boolean callSettings() {
		bearingLocaliserControl.showDetectionMenu(bearingLocaliserControl.getGuiFrame());
		return true;
	}

	@Override
	public void prepareTask() {
		super.prepareTask();
		bearingProcess.prepareProcess();
		bearingProcess.prepareBearingGroups();
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		if (rawOrFFTBlock != detectionBlock) {
			checkSourceDataLoad(rawOrFFTBlock, dataUnit);
		}
		bearingProcess.estimateBearings(dataUnit);
		return true;
	}

	/**
	 * Check source data are correclty loaded. For many detection types they will need raw or FFT data 
	 * from source in order to be able to localise. This probably won't have been loaded correctly. 
	 * @param rawOrFFTBlock2
	 * @param dataUnit
	 */
	private void checkSourceDataLoad(PamDataBlock rawOrFFTBlock, PamDataUnit dataUnit) {
		if (rawOrFFTBlock == null) {
			return;
		}
		long dataStart = dataUnit.getTimeMilliseconds();
		long dataEnd = dataUnit.getEndTimeInMilliseconds();
		boolean haveData = rawOrFFTBlock.hasDataRange(dataStart, dataEnd);
		if (haveData == false) {
//		ListIterator it = rawOrFFTBlock.getListIterator(dataUnit.getTimeMilliseconds(), rawOrFFTBlock.getChannelMap(), 
//				PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
//		if (it == null || it.hasNext() == false) {
//			if (dataEnd-dataStart <= 0) {
//				dataEnd = dataStart + 1000;
//			}
			OfflineDataLoadInfo offlineLoadInfo = new OfflineDataLoadInfo(dataStart, dataEnd);
			offlineLoadInfo.setLoadKeepLayers(2);
			rawOrFFTBlock.getOfflineData(offlineLoadInfo);
//			System.out.printf("Loaded some FFT data I hope\n");
//			rawOrFFTBlock.loadViewerData(dataStart, dataEnd, null);
		}
		
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canRun() {
		// TODO Auto-generated method stub
		super.canRun();
		return true;
	}

}
