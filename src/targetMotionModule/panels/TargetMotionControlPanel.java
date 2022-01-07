package targetMotionModule.panels;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionResult;
import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

/**
 * The targetMotionControl Panel allows the user to select a GROUP of  pamDetections which can be passed to the localisation algorithms and used in target motion analysis. The method of gettinnf the group 
 * is entirely up to the interface designed in the control panel. 
 * @author spn1
 *
 */
public interface TargetMotionControlPanel {
		
	/**
	 * @return a list of all the detection shown in the control panel. 
	 */
	public ArrayList<PamDataUnit> getTargetMotionInfo();

	/**
	 * Refresh all the data in the control panel. This is called when offline data is loaded, a datablock is changed or data is added to a datablock.  
	 */
	public void refreshData();
	

	/**
	 * Get the panel from which the user can scroll through data in the datablock
	 * @return
	 */
	public JPanel getPanel();
	
	/**
	 * Each control panel will have a different kind of data unit. saveData() is called whenever the 'save' button is pressed and saves localisation data in a format unique to the control panels' data block. e.g. click event data blocks save localisagtion info to the database. 
	 */
	public void saveData(TargetMotionResult tmResult);

	/**
	 * If the current detection has saved localisation data then set it to null i.e. get rid of it. 
	 */
	public void setNull();
	
	public void addLocaliserMenuItem(JPopupMenu menu,
			PamDataUnit selectedDetion);
	
	/**
	 * Used primarily for offline processing. Returns true if the dataunit can be localised. From the data unit the TargetMotionInformation can be determined. In the case of events this is relatively simple, however for single detections this may be more difficult.
	 * For example if using some automatic means to generate the array of detections required for target motion information
	 */
	public boolean canLocalise(PamDataUnit dataUnit);
	

}
