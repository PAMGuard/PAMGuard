package tethys.tasks;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.PamSettings;
import PamView.menu.ModalPopupMenu;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import metadata.MetaDataContol;
import metadata.PamguardMetaData;
import nilus.Deployment;
import nilus.Helper;
import tethys.TethysControl;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.swing.DeploymentWizard;
import tethys.deployment.swing.RecordingGapDialog;
import tethys.niluswraps.NilusSettingsWrapper;

public class ExportDeploymentsTask extends TethysTask {

	private DeploymentHandler deploymentHandler;

	public ExportDeploymentsTask(TethysControl tethysControl, TethysTaskManager tethysTaskManager,
			PamDataBlock parentDataBlock) {
		super(tethysControl, tethysTaskManager, parentDataBlock);
		deploymentHandler = tethysControl.getDeploymentHandler();
	}

	@Override
	public String getName() {
		return "Export Tethys Deployments";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		/*
		 * There arent any data units, but should be fine to do the export from 
		 * the completeTask function since that gets called anyway. 
		 */
		return false;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRun() {
		boolean can = super.canRun();
		if (can == false) {
			return false;
		}
		// do other checks ....
		String err = deploymentHandler.canExportDeployments(true);
		if (err != null) {
			whyNot = err;
			return false;
		}		
		
		return true;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings(Component component, Point point) {
//		JPopupMenu popMenu = new JPopupMenu();
		ModalPopupMenu popMenu = new ModalPopupMenu();
		if (point == null) {
			point = new Point(0,0);
		}
		Point aP = new Point(point);
		JMenuItem menuItem = new JMenuItem("Recording Gaps");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recordingGaps(component, aP);
			}
		});
		popMenu.add(menuItem);
//		popMenu.add(new JButton("a button"));
		
		menuItem = new JMenuItem("Deployment Information");
		menuItem.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				deploymentInformation(component, aP);
			}
		});
		popMenu.add(menuItem);
		
		return (popMenu.show(component, point.x, point.y) != null);
	}
	
	private PamguardMetaData getMetaData() {
		DeploymentTaskSettings dtp = null;
		try {
			dtp = (DeploymentTaskSettings) getTethysTaskManager().getTaskParameters().getTaskSettings(this);
		}
		catch (Exception e) {
		}
		if (dtp == null) {
			dtp = new DeploymentTaskSettings(getLongName());
			getTethysTaskManager().getTaskParameters().setTaskSettings(this, dtp);
		}
		PamguardMetaData metaData = dtp.getPamguardMetaData();
		if (metaData == null) {
			metaData = MetaDataContol.getMetaDataControl().getMetaData();
		}
		if (metaData == null) {
			metaData = new PamguardMetaData();
		}
		return metaData;
	}
	
	private Deployment getOutlineDeployment() {
		Deployment outlineDeployment = null;
		PamguardMetaData metaData = getMetaData();
		outlineDeployment = metaData.getDeployment();
		
		return outlineDeployment;
	}
	
	private void setOutlineDeployment(Deployment outlineDeployment) {
		PamguardMetaData metaData = getMetaData();
		metaData.setDeployment(outlineDeployment);
	}

	protected void deploymentInformation(Component component, Point point) {
		/*
		 *  interesting, because the main deployment base object is held in the global MetaDataControl controlled unit.
		 *  That's fine, and we can use the one from the BatchConfiguration will enough. However, this will need to 
		 *  be pushed to the job databases before processing starts. Therefore it's probably best to store it here 
		 *  with the task settings since we know that they are going to be set.  
		 */
		Deployment deployment = getOutlineDeployment();
		DeploymentExportOpts exportOptions = DeploymentWizard.showWizard(getTethysControl().getGuiFrame(), getTethysControl(), deployment, deploymentHandler.getDeploymentExportOptions());
		if (exportOptions != null) {
			deploymentHandler.setDeploymentExportOptions(exportOptions);
			getMetaData().setDeployment(deployment);
		}
		
	}

	protected void recordingGaps(Component component, Point point) {
		deploymentHandler.showOptions(null);
	}


	@Override
	public void completeTask() {
		deploymentHandler.batchExport(getOutlineDeployment());
	}

	@Override
	public ArrayList<PamSettings> getSettingsProviders() {
		ArrayList list = super.getSettingsProviders(); // should come back with main Tethys settings
		// add the metadata settings to this. 
		MetaDataContol mdc = MetaDataContol.getMetaDataControl();
		list.add(mdc);
		return list;
	}

}
