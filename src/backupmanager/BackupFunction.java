package backupmanager;

import java.awt.Window;

import PamView.dialog.PamDialogPanel;

/**
 * Base interface for all BackupStream, BackupAction and BackupDecision items
 * to facilitate dialog creation and display. 
 * @author dg50
 *
 */
public interface BackupFunction {

	/**
	 * 
	 * @return a String name
	 */
	public String getName();
	
	/**
	 * A dialog panel
	 * @param owner owner should be the dialog, not the main PAMGuard frame
	 * @return dialog panel (can be null if no options to set).
	 */
	public PamDialogPanel getDialogPanel(Window owner);
}
