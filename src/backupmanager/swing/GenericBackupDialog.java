package backupmanager.swing;

import java.awt.Window;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupFunction;
import backupmanager.BackupManager;

/**
 * Generic dialog for the backup system that can take any 
 * number of  BackupFunction items, be they streams, decisions, actions, etc.  
 * @author dg50
 *
 */
public class GenericBackupDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
		
	private ArrayList<PamDialogPanel> panels;
	
	private boolean ok = true;
	
	
	private GenericBackupDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);
		mainPanel = new JPanel();
		setDialogComponent(mainPanel);
	}
	
	/**
	 * show a dialog with a variable number of input arguements. 
	 * @param owner
	 * @param title
	 * @param backupFunctions
	 * @return
	 */
	public static boolean showDialog(Window owner, String title, BackupFunction ...backupFunctions) {
		if (backupFunctions == null) {
			return false;
		}
		ArrayList<BackupFunction> functionList = new ArrayList<BackupFunction>();
		for (int i = 0; i < backupFunctions.length; i++) {
			if (backupFunctions[i] != null) {
				functionList.add(backupFunctions[i]);
			}
		}
		
		return showDialog(owner, title, functionList);
	}
	
	public static boolean showDialog(Window owner, String title, ArrayList<BackupFunction> functionList) {
		if (functionList == null || functionList.size() == 0) {
			return false;
		}
		GenericBackupDialog backupDialog = new GenericBackupDialog(owner, title);
		backupDialog.setFunctions(functionList);
		backupDialog.setParams();
		backupDialog.setVisible(true);
		
		return backupDialog.ok;
	}

	private void setFunctions(ArrayList<BackupFunction> functionList) {
		mainPanel.removeAll();
		panels = new ArrayList<PamDialogPanel>();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		for (BackupFunction function : functionList) {
			PamDialogPanel panel = function.getDialogPanel(this);
			if (panel == null) {
				continue;
			}
			panels.add(panel);
			mainPanel.add(panel.getDialogComponent());
		}
		pack();
	}

	private void setParams() {
		for (PamDialogPanel panel : panels) {
			panel.setParams();
		}
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		for (PamDialogPanel panel : panels) {
			ok &= panel.getParams();
		}
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		ok = false;

	}

	@Override
	public void restoreDefaultSettings() {
	}

}
