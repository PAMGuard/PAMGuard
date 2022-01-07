package targetMotionOld.dialog;

import javax.swing.JPanel;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
public interface TMDialogComponent {
	
	/**
	 * 
	 * @return a panel to include in the main dialog
	 */
	JPanel getPanel();
	
	/**
	 * Current event has been set (possibly in one of the other panels)
	 * @param event
	 * @param sender
	 */
	void setCurrentEventIndex(int eventIndex, Object sender);
	
	/**
	 * Enable controls - based on event selection and other controls 
	 */
	void enableControls();
	
	/**
	 * 
	 * @return true if settigns on this panel think it's possible to start a run
	 */
	boolean canRun();
}
