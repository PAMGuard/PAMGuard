package detectiongrouplocaliser;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import javafx.scene.input.MouseEvent;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Functions for putting together an event, similar to functionality within
 * the click detector. This has been kept in a separate class from the 
 * main DetectionGroupProcess so that different rule sets can be 
 * generated using different versions of this if needed. 
 * @author dg50
 *
 */
public class EventBuilderFunctions {

	private DetectionGroupProcess detectionGroupProcess;
	
	private DetectionGroupControl detectionGroupControl;
	
	private DetectionGroupDataUnit lastModifiedDataUnit = null;
	
	private PamWarning noDataWarning;

	public EventBuilderFunctions(DetectionGroupProcess detectionGroupProcess) {
		super();
		this.detectionGroupProcess = detectionGroupProcess;
		detectionGroupControl = detectionGroupProcess.getDetectionGroupControl();
		noDataWarning = new PamWarning(detectionGroupControl.getUnitName(), "No data selected in display mark", 1);
	}

	public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker,
			OverlayMark overlayMark) {
		switch (markStatus) {
		case OverlayMarkObserver.MARK_END:
			return markEnded(mouseEvent, overlayMarker, overlayMark);
		}
		return true;
		
	}

	/**
	 * Don't do anything here anymore.  The popup menu at the end was commented out because it caused
	 * conflicts when this was called from an FX spectrogram.  Without that menu, everything else in this
	 * method was for local variables so not needed.  The popup menu for swing spectrograms is now called
	 * in SpectrogramDisplay.fireMouseUpEvents
	 * 
	 * Need to return a false, or else the regular right-click menu won't show up for spectrogram settings
	 * 
	 * @param mouseEvent
	 * @param overlayMarker
	 * @param overlayMark
	 * @return
	 */
	public boolean markEnded(MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
		return false;
		
		
//		List<PamDataUnit> dataList = overlayMarker.getSelectedMarkedDataUnits(overlayMark, detectionGroupProcess.getDataSelector());
//		if (dataList == null) {
//			return false;
//		}
////		System.out.printf("%d data units selected in mark\n", dataList.size());
////		for (PamDataUnit du:dataList) {
////			System.out.println(du.toString());
////		}
//		DetectionGroupSummary groupSummary = new DetectionGroupSummary(mouseEvent, overlayMarker, overlayMark, dataList);
//		
//		int nData = groupSummary.getNumDataUnits();
//		if (nData == 0) {
//			return false;
//		}
////		JPopupMenu popMenu = createPopupMenu(groupSummary);
//		
//		/**
//		 * This causes the detection group localiser to show menus automatically no matter what settings are on an overlay marker i.e. 
//		 * if show immediate menus is disabled...This should be in an overlaymarker and not here??
//		 * 
//		 */
////		JPopupMenu popMenu = getPopupMenuItems(groupSummary);
////		Point mousePoint = OverlayMark.getSwingComponentMousePos(PamController.getMainFrame(), mouseEvent);
////		popMenu.show(PamController.getMainFrame(), mousePoint.x, mousePoint.y);
//		
//		return true;
	}
	
//	private JPopupMenu createPopupMenu(DetectionGroupSummary groupSummary) {
//		
//	}
	
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary groupSummary) {
		
		//Debug.out.println("Detection Group Localiser Pop Up Options: " + groupSummary.getNumDataUnits()); 
		
		if (groupSummary == null || groupSummary.getNumDataUnits() == 0) {
			noDataWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			WarningSystem.getWarningSystem().addWarning(noDataWarning);
			return null;
		}
		
		JPopupMenu popMenu = new JPopupMenu(detectionGroupControl.getUnitName());
		// get information about what's in the currently selected data units. 
		int nData = groupSummary.getNumDataUnits();
		String dataName = "data units";
		if (groupSummary.getNumDataBlocks() == 1) {
			PamDataUnit firstData = groupSummary.getDataList().get(0);
			dataName = firstData.getParentDataBlock().getDataName();
		}
		if (nData == 1 && dataName.endsWith("s")) {	// strip of trailing s to avoid plural !
			dataName = dataName.substring(0, dataName.length()-1);
		}
		
		JMenuItem menuItem;
		HashSet<PamDataUnit> superDets = groupSummary.getSuperDetections();
		// gather information about what's in all groupdetectoindataunits in memory. 
		DetectionGroupDataBlock gdDataBlock = detectionGroupProcess.getDetectionGroupDataBlock();
		if (lastModifiedDataUnit != null) {
			String tit = String.format("Add %d %s to group with UID %d", groupSummary.getNumDataUnits(), dataName, lastModifiedDataUnit.getUID());
			Color detColor = PamColors.getInstance().getWhaleColor((int) lastModifiedDataUnit.getUID());
			PamSymbol sym = new PamSymbol(detectionGroupControl.getSymbolforMenuItems(lastModifiedDataUnit));
			menuItem = new JMenuItem(tit, sym);
			menuItem.addActionListener(new AddToExisting(groupSummary, (DetectionGroupDataUnit) lastModifiedDataUnit));
			popMenu.add(menuItem);
		}
		synchronized (gdDataBlock.getSynchLock()) {
			for (PamDataUnit superDet:superDets) {
				if (superDet.getParentDataBlock() != detectionGroupProcess.getDetectionGroupDataBlock()) {
					continue;
				}
				if (superDet == lastModifiedDataUnit) {
					continue;
				}
				String tit = String.format("Add %d %s to group with UID %d", groupSummary.getNumDataUnits(),  dataName, superDet.getUID());
				Color detColor = PamColors.getInstance().getWhaleColor((int) superDet.getUID());
				PamSymbol sym = new PamSymbol(detectionGroupControl.getSymbolforMenuItems(superDet));
				menuItem = new JMenuItem(tit, sym);
				menuItem.addActionListener(new AddToExisting(groupSummary, (DetectionGroupDataUnit) superDet));
				popMenu.add(menuItem);
			}
			menuItem = new JMenuItem("Create new data group ...");
			menuItem.addActionListener(new CreateNewGroup(groupSummary));
			popMenu.add(menuItem);
			// now a submenu listing all other groups in memory ...
			JMenu subMenu = new JMenu("Add to existing group");
			int nAdded = 0;
			ListIterator<DetectionGroupDataUnit> gdIterator = gdDataBlock.getListIterator(0);
			while (gdIterator.hasNext()) {
				DetectionGroupDataUnit gdUnit = gdIterator.next();
//				if (superDets.contains(gdUnit)) {
//					continue;
//				}
				String tit = String.format("Add %d %s to group with UID %d", groupSummary.getNumDataUnits(), dataName, gdUnit.getUID());
				Color detColor = PamColors.getInstance().getWhaleColor((int) gdUnit.getUID());
				PamSymbol sym = new PamSymbol(detectionGroupControl.getSymbolforMenuItems(gdUnit));
				menuItem = new JMenuItem(tit, sym);
				menuItem.addActionListener(new AddToExisting(groupSummary, gdUnit));
				subMenu.add(menuItem);
				nAdded++;
			}
			if (nAdded > 0) {
				popMenu.add(subMenu);
			}

			if (groupSummary.getNumSuperDetections() > 0) {
				menuItem = new JMenuItem("Remove data from group(s) ...");
				menuItem.addActionListener(new RemoveFromGroup(groupSummary));
				popMenu.add(menuItem);
			}
			/**
			 * Allow editing of annotations from the main menu. 
			 */
			for (PamDataUnit superDet:superDets) {
				menuItem = detectionGroupProcess.getAnnotationHandler().createAnnotationEditMenu(superDet);
				PamSymbol sym = new PamSymbol(detectionGroupControl.getSymbolforMenuItems(superDet));
				menuItem.setIcon(sym);
				popMenu.add(menuItem);
			}

		}

		return popMenu;
	}

	private class AddToExisting implements ActionListener {

		private DetectionGroupSummary groupSummary;
		private DetectionGroupDataUnit superDet;
		public AddToExisting(DetectionGroupSummary groupSummary, DetectionGroupDataUnit superDet) {
			this.groupSummary = groupSummary;
			this.superDet = superDet;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			detectionGroupProcess.addToEvent(groupSummary, superDet, true);
			lastModifiedDataUnit = superDet;
		}
		
	}
	
	private class CreateNewGroup implements ActionListener {

		private DetectionGroupSummary groupSummary;

		public CreateNewGroup(DetectionGroupSummary groupSummary) {
			super();
			this.groupSummary = groupSummary;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			lastModifiedDataUnit = detectionGroupProcess.createNewDetectionGroup(groupSummary);
		}

	
	}

	private class RemoveFromGroup implements ActionListener {

		private DetectionGroupSummary groupSummary;

		public RemoveFromGroup(DetectionGroupSummary groupSummary) {
			super();
			this.groupSummary = groupSummary;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			detectionGroupProcess.removeSubDetections(groupSummary);
		}
	}

	/**
	 * Clear the flag holding onto the last modified data unit
	 */
	public void clearLastModifiedDataUnit() {
		lastModifiedDataUnit = null;
	}
}
