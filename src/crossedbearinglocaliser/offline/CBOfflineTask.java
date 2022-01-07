package crossedbearinglocaliser.offline;

import PamguardMVC.PamDataUnit;
import crossedbearinglocaliser.CBLocaliserControl;
import crossedbearinglocaliser.CBLocaliserProcess;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class CBOfflineTask extends OfflineTask {

	private CBLocaliserControl cbLocaliserControl;
	private CBLocaliserProcess cbLocProces;
	
	public CBOfflineTask(CBLocaliserControl cbLocaliserControl) {
		super(cbLocaliserControl.getCbLocaliserProcess().getParentDataBlock());
		this.cbLocaliserControl = cbLocaliserControl;
		cbLocProces = cbLocaliserControl.getCbLocaliserProcess();
//		setParentDataBlock(cbLocProces.getParentDataBlock());
	}

	@Override
	public String getName() {
		return cbLocaliserControl.getUnitName();
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		cbLocProces.newData(null, dataUnit);
		return true;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#hasSettings()
	 */
	@Override
	public boolean hasSettings() {
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#callSettings()
	 */
	@Override
	public boolean callSettings() {
		cbLocaliserControl.showSettingsDialog(cbLocaliserControl.getGuiFrame());
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#prepareTask()
	 */
	@Override
	public void prepareTask() {
		cbLocProces.prepareProcess();
	}

}
