package tethys.tasks;

import java.io.Serializable;
import java.util.ArrayList;

import Array.ArrayManager;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamguardMVC.PamDataBlock;
import offlineProcessing.UnitTaskManager;
import tethys.TethysControl;

/**
 * Manager for offline tasks in Tethys. These probably won't be made available
 * as tasks in Tethys, but will be picked up by the batch processor which can then 
 * execute them across multiple datasets. <p>
 * These will have to be set up with their own complete sets of parameters, i.e. all the
 * information that was normally going into the export Wizards, so that they can be 
 * fully automated from the batch processor. <p>
 * A menu will be provided for the dev phase, but will probably then be removed. 
 */
public class TethysTaskManager extends UnitTaskManager implements PamSettings {

	private TethysControl tethysControl;
	
	private static String unitType = "Tethys Export Tasks";
	
	private TethysTaskParameters taskParameters = new TethysTaskParameters();
	
	public TethysTaskManager(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public int generateTasks() {
		clearAll();
		
		addSingleTask(new ExportCalibrationTask(tethysControl, this, ArrayManager.getArrayManager().getHydrophoneDataBlock()));
		addSingleTask(new ExportDeploymentsTask(tethysControl, this, null));
		ArrayList<PamDataBlock> dataBlocks = tethysControl.getExportableDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			addSingleTask(new ExportDataBlockTask(tethysControl, this, dataBlocks.get(i)));
		}
		
		return size();
	}
	
	/**
	 * Most tasks are going to be in groups of their own since they all use different 
	 * primary datablocks, so can make it easy to generate all these groups.
	 * @param tethysTask
	 * @return generated group
	 */
	public TethysTaskGroup addSingleTask(TethysTask tethysTask) {
		TethysTaskGroup tethysTaskGroup = new TethysTaskGroup(tethysTask);
		// don't add because this gets done automatically from the TaskGroup contrsuctor
//		add(tethysTaskGroup);
		return tethysTaskGroup;
	}

	@Override
	public String getUnitName() {
		return tethysControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return unitType;
	}

	@Override
	public Serializable getSettingsReference() {
		return taskParameters;
	}

	@Override
	public long getSettingsVersion() {
		return TethysTaskParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		taskParameters = (TethysTaskParameters) pamControlledUnitSettings.getSettings();
		return taskParameters != null;
	}

	/**
	 * @return the taskParameters
	 */
	public TethysTaskParameters getTaskParameters() {
		return taskParameters;
	}
	
}
