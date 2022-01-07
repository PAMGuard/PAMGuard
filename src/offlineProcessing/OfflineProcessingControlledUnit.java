/**
 * 
 */
package offlineProcessing;

import java.util.ArrayList;

import dataMap.DataMapPanel;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;

/**
 * @author GrahamWeatherup
 *only run in Viewer mode to run all Offline Tasks
 */
public class OfflineProcessingControlledUnit extends PamControlledUnit{

//	static OfflineProcessingControlledUnit singleInstance;
	
	ArrayList<OfflineTaskGroup> taskGroups;
	
	
	/**
	 * @param unitType
	 * @param unitName
	 */
	public OfflineProcessingControlledUnit(String unitName) {
		super("OfflineProcessing", unitName);
		// TODO Auto-generated constructor stub
		taskGroups=findAllTaskGroups();
		
	}
	
	
//	/**
//	 * @param unitType
//	 * @param unitName
//	 */
//	public OfflineProcessingControlledUnit(String unitType, String unitName) {
//		super(unitType, unitName);
//		// TODO Auto-generated constructor stub
//		taskGroups=findAllTaskGroups();
//		
//	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			if (true){//(PamController.getAutoStartProcessing...){
//				startProcesses();
			}
			
			
			break;
		}
	}
	
	
//	public static OfflineProcessingControlledUnit getInstance(){
//		if (singleInstance==null) singleInstance= new OfflineProcessingControlledUnit("OfflineProcessing","OfflineProcessing" );
//		return singleInstance;
//	}

	void startProcesses(){
		for (OfflineTaskGroup otg:getAllTaskGroups()){
			
			otg.runTasks();
			
			
		}
		
	}
	
	
	public ArrayList<OfflineTaskGroup> getAllTaskGroups(){
		if (taskGroups==null){
			taskGroups= findAllTaskGroups();
		}
		return taskGroups;
		
	}
	
	private ArrayList<OfflineTaskGroup> findAllTaskGroups(){
		ArrayList<OfflineTaskGroup> tgs = new ArrayList<OfflineTaskGroup>();
		int numUnits=PamController.getInstance().getNumControlledUnits();
		for (int iUnit=0;iUnit<numUnits;iUnit++){
			PamControlledUnit unit = PamController.getInstance().getControlledUnit(iUnit);
			int numGroups = unit.getNumOfflineTaskGroups();
			for (int iGp=0;iGp<numGroups;iGp++){
				tgs.add( unit.getOfflineTaskGroup(iGp));
				
			}
		}
		return tgs;
	}
}
