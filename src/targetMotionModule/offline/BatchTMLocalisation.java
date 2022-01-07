package targetMotionModule.offline;

import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;
import targetMotionModule.TargetMotionControl;
import PamguardMVC.PamDataUnit;

@SuppressWarnings("rawtypes")
public class BatchTMLocalisation extends  OfflineTask<PamDataUnit>{
	
	TargetMotionControl targetMotionControl;
	
	public BatchTMLocalisation(TargetMotionControl targetMotionControl){
		super(targetMotionControl.getCurrentDataBlock());
		this.targetMotionControl=targetMotionControl;
	}

	@Override
	public String getName() {
		return "Batch Localise";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		
		
		
		return false;
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
	
	
	

}
