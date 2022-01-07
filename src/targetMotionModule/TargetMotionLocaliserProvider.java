package targetMotionModule;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import targetMotionModule.panels.AbstractControlPanel;


public interface TargetMotionLocaliserProvider {
	
	/**
	 * Any datablock can provide a localiser control panel for the static localiser, however it must be tailored to the specific type of detection. For example clicks would require a control panel which can sort out events, classified clicks etc.
	 * @param staticLocaliserControl
	 * @return
	 */
	public AbstractControlPanel getTMControlPanel(TargetMotionControl targetMotionControl);
	
	/**
	 * Each data block may have a unique way of calculating targetmotion information. 
	 * @param detections-list of detections in a single target motion event. 
	 * @return any class which implements TargetMotionInformation
	 */
	public TargetMotionInformation getTMInfo(ArrayList<PamDataUnit> detections);
	

}
