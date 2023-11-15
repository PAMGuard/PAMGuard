package clickDetector.offlineFuncs;

import offlineProcessing.OfflineTask;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickDetector.ChannelGroupDetector;
import clickDetector.echoDetection.EchoDetectionSystem;
import clickDetector.echoDetection.EchoDetector;
import clickDetector.echoDetection.EchoDialog;
import dataMap.OfflineDataMapPoint;

public class EchoDetectionTask extends OfflineTask<ClickDetection> {

	private EchoDetectionSystem echoDetectionSystem;
	
	private ClickControl clickControl;
		
	/**
	 * @param clickControl
	 */
	public EchoDetectionTask(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		echoDetectionSystem = clickControl.getEchoDetectionSystem();
		setParentDataBlock(clickControl.getClickDataBlock());
//		addAffectedDataBlock(clickControl.getClickDataBlock());
	}

	@Override
	public String getName() {
		return "Echo Detection";
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#prepareTask()
	 */
	@Override
	public void prepareTask() {
		super.prepareTask();
		echoDetectionSystem = clickControl.getEchoDetectionSystem();
		ClickDetector cd = clickControl.getClickDetector();
		int n = cd.getnChannelGroups();
		EchoDetector ed;
		for (int i = 0; i < n; i++) {
			ed = cd.getChannelGroupDetector(i).getEchoDetector();
			if (ed != null) {
				ed.initialise();
			}
		}
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
	}

	@Override
	public boolean processDataUnit(ClickDetection dataUnit) {
		ChannelGroupDetector cgd = dataUnit.getChannelGroupDetector();
		if (cgd == null) {
			return false;
		}
		
		boolean wasEcho = dataUnit.isEcho();
		if (dataUnit.getUID() == 9034013666L) {
			boolean isEcho = cgd.getEchoDetector().isEcho(dataUnit);
			
		}
		boolean isEcho = cgd.getEchoDetector().isEcho(dataUnit);
		if (isEcho != wasEcho) {
			dataUnit.setEcho(isEcho);
			return true;
		}
		return false;
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#callSettings()
	 */
	@Override
	public boolean callSettings() {
		echoDetectionSystem = clickControl.getEchoDetectionSystem();
		if (echoDetectionSystem == null) {
			return false;
		}
		return EchoDialog.showDialog(clickControl.getGuiFrame(), echoDetectionSystem);
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#canRun()
	 */
	@Override
	public boolean canRun() {
		return (clickControl.getEchoDetectionSystem() != null);
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#hasSettings()
	 */
	@Override
	public boolean hasSettings() {
		return (clickControl.getEchoDetectionSystem() != null);
	}

}
