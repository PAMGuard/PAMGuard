package tethys.tasks;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Hold settings for all Tethys tasks. These all should have unique names
 * so should be OK. Many settings will be in the formed of a wrapped nilus object
 * as a NilusSettingsWrapper. 
 * @author dg50
 *
 */
public class TethysTaskParameters implements Serializable {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, TethysTaskSettings> taskSettings2 = new HashMap<>();

	public void setTaskSettings(TethysTask tethysTask, TethysTaskSettings object) {
		checkObj();
		taskSettings2.put(tethysTask.getLongName(), object);
	}
	
	public TethysTaskSettings getTaskSettings(TethysTask tethysTask) {
		checkObj();
		return taskSettings2.get(tethysTask.getLongName());
	}

	private void checkObj() {
		if (taskSettings2 == null) {
			taskSettings2 = new HashMap<String, TethysTaskSettings>();
		}
	}

}
