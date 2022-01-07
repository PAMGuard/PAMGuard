package targetMotionModule.panels;

import javax.swing.JPanel;

public interface TMDialogComponent {
	
	/**
	 * 
	 * @return a panel to include in the main dialog
	 */
	JPanel getPanel();
	
	/**
	 * Enable controls - based on event selection and other controls 
	 */
	void enableControls();
	
	/**
	 * 
	 * @return true if settigns on this panel think it's possible to start a run
	 */
	boolean canRun();
	
	/**
	 * Updates the panel depending on the integer flag. 
	 */
	public void update(int flag);
	
}
