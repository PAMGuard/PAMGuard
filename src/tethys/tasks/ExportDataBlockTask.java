package tethys.tasks;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import tethys.TethysControl;
import tethys.species.DataBlockSpeciesManager;

public class ExportDataBlockTask extends TethysTask {

	private PamDataBlock parentDataBlock;

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
		JPopupMenu popMenu = new JPopupMenu();
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

		if (component != null) {
			popMenu.show(component, point.x, point.y);
		}
		else {
			// otherwise show it wherever the mouse is. 
			PointerInfo pointer = MouseInfo.getPointerInfo();
			//			pointer.
		}
		return false;
	}

	protected void exportOptions(Component component, Point point) {
		// TODO Auto-generated method stub

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
