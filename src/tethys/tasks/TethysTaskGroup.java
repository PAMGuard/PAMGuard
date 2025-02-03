package tethys.tasks;

import PamController.PamControlledUnit;
import offlineProcessing.OfflineTaskGroup;

public class TethysTaskGroup extends OfflineTaskGroup {

	public TethysTaskGroup(TethysTask tethysTask) {
		super(tethysTask.getTethysControl(), tethysTask.getName());
		addTask(tethysTask);
	}


}
