package tethys.swing;

import java.awt.Window;

import PamView.dialog.PamDialog;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;

/**
 * Modeless dialog showing all project deployments. 
 * @author dg50
 *
 */
public class ProjectDeploymentsDialog extends PamDialog {

	private TethysDeploymentsTable deploymentsPanel;
	private TethysControl tethysControl;
	private static ProjectDeploymentsDialog singleInstance;
	
	private ProjectDeploymentsDialog(Window parentFrame, TethysControl tethysControl) {
		super(parentFrame, "Project deployments", false);
		this.tethysControl = tethysControl;
		deploymentsPanel = new TethysDeploymentsTable(tethysControl);
		setDialogComponent(deploymentsPanel.getComponent());
		setModal(false);
		setResizable(true);
		getCancelButton().setVisible(false);
	}

	public static void showDialog(Window parentFrame, TethysControl tethysControl) {
		if (singleInstance == null) {
			singleInstance = new ProjectDeploymentsDialog(parentFrame, tethysControl);
		}
		singleInstance.setVisible(true);
		singleInstance.deploymentsPanel.updateState(new TethysState(StateType.NEWPROJECTSELECTION));
//		if (singleInstance.is)
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
}
