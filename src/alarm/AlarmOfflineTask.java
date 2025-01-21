package alarm;

import java.awt.Frame;

import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class AlarmOfflineTask extends OfflineTask<PamDataUnit> {

	private AlarmControl alarmControl;
	private AlarmProcess alarmProcess;
	
	public AlarmOfflineTask(AlarmControl alarmControl) {
		super(alarmControl, alarmControl.getAlarmProcess().getSourceDataBlock());
		this.alarmControl = alarmControl;
		alarmProcess = alarmControl.getAlarmProcess();
		setParentDataBlock(alarmProcess.getSourceDataBlock());
		addAffectedDataBlock(alarmProcess.getAlarmDataBlock());
	}

	@Override
	public String getName() {
		return "Run Alarm";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		alarmProcess.newData(dataUnit.getParentDataBlock(), dataUnit);
		return true;
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareTask() {
		alarmProcess.pamStart();
	}

	@Override
	public void completeTask() {
		alarmProcess.pamStart();
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		Frame frame = alarmControl.getGuiFrame();
		boolean ok = alarmControl.showAlarmDialog(frame);
		if (ok) {
			setParentDataBlock(alarmProcess.getSourceDataBlock());
		}
		return ok;
			
	}

}
