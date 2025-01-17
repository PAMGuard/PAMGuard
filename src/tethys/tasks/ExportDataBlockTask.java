package tethys.tasks;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamController;
import PamController.PamSettings;
import PamView.menu.ModalPopupMenu;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTaskGroup.TaskGroupWorker;
import offlineProcessing.TaskActivity;
import offlineProcessing.TaskMonitorData;
import offlineProcessing.TaskStatus;
import tethys.TethysControl;
import tethys.detection.DetectionExportObserver;
import tethys.detection.DetectionExportProgress;
import tethys.detection.DetectionsHandler;
import tethys.output.StreamExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesMapManager;
import tethys.swing.export.DetectionsExportWizard;

public class ExportDataBlockTask extends TethysTask implements DetectionExportObserver {
	
	/**
	 * Need to check ITIS codes and stream export params are all OK. 
	 * No need to save anything since export params for all datablocks are 
	 * in a HashMap in the main Tethys settings 
	 */

	private PamDataBlock parentDataBlock;
	
	private DetectionsHandler detectionsHandler;

	private ExportDatablockGroup exportDatablockGroup;

	private TaskGroupWorker taskGroupWorker;

	public ExportDataBlockTask(TethysControl tethysControl, TethysTaskManager tethysTaskManager,
			PamDataBlock parentDataBlock) {
		super(tethysControl, tethysTaskManager, parentDataBlock);
		this.parentDataBlock = parentDataBlock;
		detectionsHandler = tethysControl.getDetectionsHandler();
	}

	@Override
	public String getName() {
		return "Export " + getDataBlock().getLongDataName();
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
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
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings(Component component, Point point) {
		ModalPopupMenu popMenu = new ModalPopupMenu();
		JMenuItem menuItem = new JMenuItem("ITIS Species Codes");
		popMenu.add(menuItem);
		if (point == null) {
			point = new Point(0,0);
		}
		Point aP = point;
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				itisCodes(component, aP);
			}
		});


		menuItem = new JMenuItem("Export Options");
		popMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exportOptions(component, aP);
			}
		});

		return	(popMenu.show(component, point.x, point.y) != null);
	}

	protected void exportOptions(Component component, Point point) {
		// use standard export dialog, but don't do the actual exporting. 
//		StreamExportParams streamParams = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), parentDataBlock);
//		WrappedDescriptionType wrappedDesc = streamParams.getDetectionDescription();
		DetectionsExportWizard.showDialog(PamController.getMainFrame(), getTethysControl(), parentDataBlock, false);
		StreamExportParams streamParams = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), parentDataBlock);
		streamParams.reSerialize();
//		wrappedDesc.reSerialise();
	}

	protected void itisCodes(Component component, Point point) {
		DataBlockSpeciesManager speciesManager = parentDataBlock.getDatablockSpeciesManager();
		if (speciesManager == null) {
			System.out.println("No species manager available for " + parentDataBlock.getLongDataName());
			return;
		}
		speciesManager.showSpeciesDialog();
	}

	@Override
	public boolean canRun() {		
		boolean can = super.canRun();
		if (!can) {
			return false;
		}

		StreamExportParams exportParams = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), parentDataBlock);
		if (exportParams == null) {
			whyNot = "No export configuration for this task";
			return false;
		}
		if (exportParams.exportDetections == false && exportParams.exportLocalisations == false) {
			whyNot = "You must select to export Detection, Localisations, or both";
			return false;
		}
		DataBlockSpeciesManager speciesManager = parentDataBlock.getDatablockSpeciesManager();
		if (speciesManager == null) {
			whyNot = "No species manager is available for this data strea";
			return false;
		}
		String speciesError = speciesManager.checkSpeciesMapError();
		if (speciesError != null) {
			whyNot = speciesError;
			return false;
		}
		
		
		return true;
	}

	@Override
	public String whyNot() {
		// TODO Auto-generated method stub
		return super.whyNot();
	}

	/**
	 * Called back from modified offlinetaskgroup which will let
	 * the detectionshander do all the data load / management in it's own way
	 * @param taskGroupWorker 
	 * @param exportDatablockGroup
	 */
	public void runEntireTask(TaskGroupWorker taskGroupWorker, ExportDatablockGroup exportDatablockGroup) {
		this.exportDatablockGroup = exportDatablockGroup;
		this.taskGroupWorker = taskGroupWorker;
		StreamExportParams exportParams = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), parentDataBlock);
		if (exportParams.exportDetections == false && exportParams.exportLocalisations == false) {
			exportParams.exportDetections = true;
		}
		detectionsHandler = getTethysControl().getDetectionsHandler();
		detectionsHandler.setActiveExport(true);
		detectionsHandler.exportDetections(parentDataBlock, exportParams, this);
		detectionsHandler.setActiveExport(false);
	}

	@Override
	public void update(DetectionExportProgress progress) {
		TaskGroupWorker tgw = taskGroupWorker;
		if (tgw == null) return; // copy reference for thread safety.
		TaskMonitorData tmd = null;
		if (progress.state == DetectionExportProgress.STATE_COMPLETE) {
			tmd = new TaskMonitorData(TaskStatus.COMPLETE, TaskActivity.PROCESSING, progress.nMapPoints, progress.doneMapPoints, "Processing", progress.lastUnitTime);
		}
		else {
			tmd = new TaskMonitorData(TaskStatus.RUNNING, TaskActivity.PROCESSING, progress.nMapPoints, progress.doneMapPoints, "Processing", progress.lastUnitTime);
		}
		tgw.publish(tmd);
	}

	@Override
	public ArrayList<PamSettings> getSettingsProviders() {
		// TODO Auto-generated method stub
		ArrayList<PamSettings> list = super.getSettingsProviders();
		// add the ITIS codes manager since datablocks will need it for Tethys tasks.
		list.add(SpeciesMapManager.getInstance());
		return list;
	}


}
