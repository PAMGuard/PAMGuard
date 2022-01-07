package whistleClassifier.offline;

import java.awt.Frame;

import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

import whistleClassifier.WhistleClassifierControl;

/**
 * Stuff for re-running whistle classification offline. 
 * @author Doug Gillespie. 
 *
 */
public class ReclassifyWhistles {

	private WhistleClassifierControl whistleClassifierControl;
	private OfflineTaskGroup whistleTaskGroup;
	private ReclassifyTask reclassifyTask;

	public ReclassifyWhistles(WhistleClassifierControl whistleClassifierControl) {
		super();
		this.whistleClassifierControl = whistleClassifierControl;
		whistleTaskGroup = new OfflineTaskGroup(whistleClassifierControl, "Offline Tasks");
		whistleTaskGroup.addTask(reclassifyTask = new ReclassifyTask(whistleClassifierControl));
	}

	public void runReclassify(Frame parentFrame) {
		OLProcessDialog d = new OLProcessDialog(parentFrame, whistleTaskGroup, "Whistle Classification");
		d.setVisible(true);
	}

	/**
	 * @return the whistleTaskGroup
	 */
	public OfflineTaskGroup getWhistleTaskGroup() {
		return whistleTaskGroup;
	}

	/**
	 * @return the reclassifyTask
	 */
	public ReclassifyTask getReclassifyTask() {
		return reclassifyTask;
	} 
	
	
}
