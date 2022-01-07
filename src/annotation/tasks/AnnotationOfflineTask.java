package annotation.tasks;

import annotation.DataAnnotationType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class AnnotationOfflineTask extends OfflineTask {

	private DataAnnotationType dataAnnotationType;
	private PamDataBlock dataBlock;
	
	public AnnotationOfflineTask(PamDataBlock dataBlock, DataAnnotationType dataAnnotationType) {
		super(dataBlock);
		this.dataBlock = dataBlock;
		this.dataAnnotationType = dataAnnotationType;
		setParentDataBlock(dataBlock);
	}

	@Override
	public String getName() {
		return dataAnnotationType.getAnnotationName();
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		return (dataAnnotationType.autoAnnotate(dataUnit) != null);
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
