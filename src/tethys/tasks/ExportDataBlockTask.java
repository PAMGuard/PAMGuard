package tethys.tasks;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import PamController.PamController;
import PamView.menu.ModalPopupMenu;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import tethys.TethysControl;
import tethys.detection.DetectionsHandler;
import tethys.output.StreamExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.swing.export.DetectionsExportWizard;

public class ExportDataBlockTask extends TethysTask {
	
	/**
	 * Need to check ITIS codes and stream export params are all OK. 
	 * No need to save anything since export params for all datablocks are 
	 * in a HashMap in the main Tethys settings 
	 */

	private PamDataBlock parentDataBlock;
	
	private DetectionsHandler detectionsHandler;

	public ExportDataBlockTask(TethysControl tethysControl, TethysTaskManager tethysTaskManager,
			PamDataBlock parentDataBlock) {
		super(tethysControl, tethysTaskManager, parentDataBlock);
		this.parentDataBlock = parentDataBlock;
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


}
