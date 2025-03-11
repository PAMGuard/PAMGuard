package detectiongrouplocaliser;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JPopupMenu;

import PamController.PamControllerInterface;
import PamUtils.PamCalendar;
import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.OverlayMarkSwingPanel;
import PamView.paneloverlay.OverlaySwingPanel;
import PamView.paneloverlay.overlaymark.BasicMarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import detectiongrouplocaliser.logging.DetectionGroupLogging;
import detectiongrouplocaliser.logging.DetectionGroupSubLogging;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableData;
import generalDatabase.SQLLogging;
import javafx.scene.input.MouseEvent;
import pamScrollSystem.AbstractScrollManager;

public class DetectionGroupProcess extends PamProcess {

	private DetectionGroupControl detectionGroupControl;
	
	private DetectionGroupDataBlock detectionGroupDataBlock;

	private DetectionGroupLogging detectionGroupLogging;
	
	private MarkObserver markObserver;

	private DataSelector dataSelector;
	
	private EventBuilderFunctions eventBuilderFunctions;

	private GroupAnnotationHandler annotationHandler;

	private SQLLogging subTabLogging;

	public DetectionGroupProcess(DetectionGroupControl detectionGroupControl) {
		super(detectionGroupControl, null);
		this.detectionGroupControl = detectionGroupControl;
		detectionGroupDataBlock = new DetectionGroupDataBlock(detectionGroupControl.getUnitName(), detectionGroupControl, this);
		
		detectionGroupLogging = new DetectionGroupLogging(detectionGroupControl, detectionGroupDataBlock);
		detectionGroupDataBlock.SetLogging(detectionGroupLogging);
		detectionGroupLogging.setSubLogging(new DetectionGroupSubLogging(detectionGroupLogging, detectionGroupDataBlock));
		
		detectionGroupDataBlock.setOverlayDraw(new DetectionGroupGraphics(this));
		detectionGroupDataBlock.setPamSymbolManager(new DetectionGroupSymbolManager(detectionGroupDataBlock));
		addOutputDataBlock(detectionGroupDataBlock);
		AbstractScrollManager.getScrollManager().addToSpecialDatablock(detectionGroupDataBlock);
		this.dataSelector = new DataSelector();
		annotationHandler = new GroupAnnotationHandler(detectionGroupControl, detectionGroupDataBlock);
		detectionGroupDataBlock.setAnnotationHandler(annotationHandler);
		eventBuilderFunctions = new EventBuilderFunctions(this);

		markObserver = new MarkObserver();
		OverlayMarkObservers.singleInstance().addObserver(markObserver);
	}

	@Override
	public void destroyProcess() {
		super.destroyProcess();
		AbstractScrollManager.getScrollManager().removeFromSpecialDatablock(detectionGroupDataBlock);
		OverlayMarkObservers.singleInstance().removeObserver(markObserver);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			subscribeMarkObserver();
			annotationHandler.loadAnnotationChoices();
			sortSQLLogging();
			break;
		}
	}

	private void subscribeMarkObserver() {
		MarkRelationships markRelationships = MarkRelationships.getInstance();
		markRelationships.subcribeToMarkers(markObserver);
	}
	
	/**
	 * Check all the SQL Logging additions are set up correctly. 
	 */
	protected void sortSQLLogging() {
		detectionGroupLogging.setTableDefinition(detectionGroupLogging.createBaseTable());
		if (annotationHandler.addAnnotationSqlAddons(detectionGroupLogging) > 0) {
			// will have to recheck the table in the database. 
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc != null) {
				dbc.getDbProcess().checkTable(detectionGroupLogging.getTableDefinition());
			}
		}
	}
	
	private class MarkObserver implements OverlayMarkObserver {

		@Override
		public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker,
				OverlayMark overlayMark) {
			return eventBuilderFunctions.markUpdate(markStatus, mouseEvent, overlayMarker, overlayMark);
		}

		@Override
		public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
			return eventBuilderFunctions.getPopupMenuItems(markSummaryData);
		}
		
		@Override
		public ParameterType[] getRequiredParameterTypes() {
			return null; // no required parameters.
		}

		@Override
		public String getObserverName() {
			return detectionGroupControl.getUnitName();
		}

		@Override
		public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
			return getDataSelector();
		}

		@Override
		public String getMarkName() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class DataSelector extends BasicMarkDataSelector {

		public DataSelector() {
			super(null, null);
		}

		@Override
		public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getDataSelectorName() {
			return detectionGroupControl.getUnitName();
		}

		@Override
		public OverlayMarkDataInfo getOverlayInfo(PamDataBlock dataBlock) {
			return detectionGroupControl.getDetectionGroupSettings().getMarkerInfo(dataBlock.getLongDataName());
		}

		/* (non-Javadoc)
		 * @see PamView.paneloverlay.OverlayDataManager#getSwingPanel(java.awt.Window)
		 */
		@Override
		public OverlaySwingPanel getSwingPanel(Window parentWindow) {
			return new OverlayMarkSwingPanel(this, parentWindow);
		}
		
		public boolean wantDataBlock(PamDataBlock dataBlock) {
			if (dataBlock == null) {
				return false;
			}
			OverlayMarkDataInfo overlayInfo = getOverlayInfo(dataBlock);
			if (overlayInfo == null) {
				return false;
			}
			return overlayInfo.select;
		}
		
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	/**
	 * @return the dataSelector
	 */
	public DataSelector getDataSelector() {
		return dataSelector;
	}

	/**
	 * @return the detectionGroupDataBlock
	 */
	public DetectionGroupDataBlock getDetectionGroupDataBlock() {
		return detectionGroupDataBlock;
	}

	/**
	 * @return the detectionGroupControl
	 */
	public DetectionGroupControl getDetectionGroupControl() {
		return detectionGroupControl;
	}

	/**
	 * @return the detectionGroupLogging
	 */
	public DetectionGroupLogging getDetectionGroupLogging() {
		return detectionGroupLogging;
	}

	/**
	 * @return the markObserver
	 */
	public MarkObserver getMarkObserver() {
		return markObserver;
	}

	/**
	 * @return the eventBuilderFunctions
	 */
	public EventBuilderFunctions getEventBuilderFunctions() {
		return eventBuilderFunctions;
	}

	/**
	 * @return the annotationHandler
	 */
	public GroupAnnotationHandler getAnnotationHandler() {
		return annotationHandler;
	}

	/**
	 * Add an annotation to a detection group. 
	 * @param overlayMarker
	 * @param overlayMark
	 * @param anType
	 * @param dgdu
	 * @return
	 */
	public boolean addAnnotation(OverlayMarker overlayMarker, OverlayMark overlayMark,
			DataAnnotationType anType, DetectionGroupDataUnit dgdu) {
		return annotationHandler.addAnnotation(anType, dgdu);
//		// see if it's an annotation with a dialog. 
//		AnnotationDialogPanel dialogPanel = anType.getDialogPanel();
//		if (dialogPanel == null) {
//			anType.autoAnnotate(dgdu);
//			return true;
//		}
//		else {
//			// make and show a dialog. show it close to the current mouse position
//			PointerInfo mousePointerInfo = MouseInfo.getPointerInfo();
//			Point locOnScreen = mousePointerInfo.getLocation();
//			boolean ans = AnnotationDialog.showDialog(PamController.getMainFrame(), 
//					anType, dgdu, locOnScreen);
//			//			System.out.println("Data unit has annotation " + ans);
//			return ans;
//		}
	}

	/**
	 * Called when a new group is created. 
	 * @param groupSummary
	 */
	public DetectionGroupDataUnit createNewDetectionGroup(DetectionGroupSummary groupSummary) {
		DetectionGroupDataUnit dgdu = new DetectionGroupDataUnit(groupSummary.getFirstTimeMillis(), null);
		dgdu.setParentDataBlock(detectionGroupDataBlock); // neede by some of the annotations, so set it early!
		AnnotationHandler annotationHandler = getAnnotationHandler();
		List<DataAnnotationType<?>> anTypes = annotationHandler.getUsedAnnotationTypes();
		for (DataAnnotationType anType:anTypes) {
			if (anType.getDialogPanel() == null) {
				// do the ones with a dialog panel first. 
				continue; 
			}
			if (!addAnnotation(groupSummary.getOverlayMarker(), groupSummary.getOverlayMark(), anType, dgdu)) {
				return null;
			}
		}
		// if it gets here, we want the group
//		dgdu.addDetectionList(groupSummary.getDataList());
		// call this instead of the above to make sure we make checks on
		// whether sub dets already hava  superdet. 
		addToEvent(groupSummary, dgdu, false);

		for (DataAnnotationType anType:anTypes) {
			if (anType.getDialogPanel() != null) {
				// then do the ones without a dialog panel.  
				continue; 
			}
			if (!addAnnotation(groupSummary.getOverlayMarker(), groupSummary.getOverlayMark(), anType, dgdu)) {
				return null;
			}
		}
		
		getDetectionGroupDataBlock().addPamData(dgdu);
		detectionGroupControl.notifyGroupDataChanged();
		OverlayMarker marker = groupSummary.getOverlayMarker();
		if (marker != null) {
			marker.updateMarkedDisplay();
		}
		return dgdu;
	}
	
	/**
	 * Add some detections to an existing event
	 * @param groupSummary
	 * @param superDet
	 * @param update true if need to update data unit in datablock. Make sure it's false when called from newEvent. 
	 */
	public void addToEvent(DetectionGroupSummary groupSummary, PamDataUnit superDet, boolean update) {
		if (superDet.getClass() != DetectionGroupDataUnit.class) {
			return;
		}
		DetectionGroupDataUnit dgdu = (DetectionGroupDataUnit) superDet;
		for (PamDataUnit dataUnit:groupSummary.getDataList()) {
			// check to see if it's in an existing event. 
			DetectionGroupDataUnit existingSuperDet = (DetectionGroupDataUnit) dataUnit.getSuperDetection(DetectionGroupDataUnit.class);
			if (existingSuperDet != null) {
				if (existingSuperDet == superDet) {
					continue;
				}
				else {
					existingSuperDet.removeSubDetection(dataUnit);
					if (existingSuperDet.getSubDetectionsCount() <= 0) {
						deleteDetectionGroup(existingSuperDet);
					}
					else {
						detectionGroupDataBlock.updatePamData(existingSuperDet, PamCalendar.getTimeInMillis());
					}
				}
			}
			// now add the data to the event
			dgdu.addSubDetection(dataUnit);
		}

		if (update) {
			/*
			 * Update automatic annotations, but not ones requiring user input. 
			 */
			List<DataAnnotationType<?>> anTypes = annotationHandler.getUsedAnnotationTypes();
			for (DataAnnotationType anType:anTypes) {
				if (anType.getDialogPanel() == null) {
					annotationHandler.updateAnnotation(dgdu, anType);
				}
			}


			// notify data update.
			detectionGroupDataBlock.updatePamData(dgdu, PamCalendar.getTimeInMillis());
			detectionGroupControl.notifyGroupDataChanged();
			OverlayMarker marker = groupSummary.getOverlayMarker();
			if (marker != null) {
				marker.updateMarkedDisplay();
			}
		}
	}

	/**
	 * Delete an entire detection group and remove it's entry from the database. 
	 * @param existingSuperDet
	 */
	public void deleteDetectionGroup(DetectionGroupDataUnit detectionGroupDataUnit) {
		/*
		 * First need to go through and remove any remaining sub detections from 
		 * the group. 
		 */
		int nSub = detectionGroupDataUnit.getSubDetectionsCount();
		for (int i = nSub-1; i >= 0; i--) {
			detectionGroupDataUnit.removeSubDetection(detectionGroupDataUnit.getSubDetection(i));
		}
		/*
		 * Then delete it. 
		 */
		detectionGroupDataBlock.remove(detectionGroupDataUnit, true);
		detectionGroupControl.notifyGroupDataChanged();
//		OverlayMarker marker = groupSummary.getOverlayMarker();
//		if (marker != null) {
//			marker.updateMarkedDisplay();
//		}
	}

	/**
	 * Remove a load of detections from whatever group they happen to be in. 
	 * @param groupSummary
	 */
	public void removeSubDetections(DetectionGroupSummary groupSummary) {
		for (PamDataUnit dataUnit:groupSummary.getDataList()) {
			// check to see if it's in an existing event. 
			DetectionGroupDataUnit existingSuperDet = (DetectionGroupDataUnit) dataUnit.getSuperDetection(DetectionGroupDataUnit.class);
			if (existingSuperDet != null) {
				existingSuperDet.removeSubDetection(dataUnit);
			}
		}
		for (PamDataUnit superData:groupSummary.getSuperDetections()) {
			if (!DetectionGroupDataUnit.class.isAssignableFrom(superData.getClass())) {
				continue;
			}
			DetectionGroupDataUnit dgdu = (DetectionGroupDataUnit) superData;
			if (dgdu.getLoadedSubDetectionsCount() == 0) {
				deleteDetectionGroup(dgdu);
			}
			else {
				detectionGroupDataBlock.updatePamData(dgdu, PamCalendar.getTimeInMillis());
			}
		}

		detectionGroupControl.notifyGroupDataChanged();
		OverlayMarker marker = groupSummary.getOverlayMarker();
		if (marker != null) {
			marker.updateMarkedDisplay();
		}
	}

	/**
	 * Edit a detection group - this means editing any editable annotations. 
	 * @param dgdu
	 */
	public void editDetectionGroup(DetectionGroupDataUnit dgdu) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Called whenever instructions to load more or less data are received.
	 */
	public void changeOfflineLoadSelection() {
		
	}

	/**
	 * Check the start and end times of every event against what's in the database. 
	 */
	public void checkDataIntegrity() {
		// for every event in memory, re-query the database and get the start and end times and number of subdets from 
		// the table. 
		// start by making sure the database is up to date. 
		detectionGroupDataBlock.saveViewerData();
		int nUpdates = 0;
		int nOK = 0;
		int consecutiveOK = 0;
		System.out.printf("Checking %d data units in %s ", detectionGroupDataBlock.getUnitsCount(), detectionGroupDataBlock.getDataName());
		synchronized (detectionGroupDataBlock.getSynchLock()) {
			ListIterator<DetectionGroupDataUnit> it = detectionGroupDataBlock.getListIterator(0);
			while (it.hasNext()) {
				DetectionGroupDataUnit du = it.next();
				boolean ok = checkDataIntegrity(du, false);
				if (ok) {
					nUpdates++;
					consecutiveOK = 0;
				}
				else {
					nOK++;
					consecutiveOK++;
				}
				System.out.printf(".");
				if (consecutiveOK % 80 == 0) {
					System.out.printf("\n");
				}
			}
		}
		System.out.printf("\n%s: %d out of %d data units required corrections\n", detectionGroupDataBlock.getDataName(), nUpdates, nUpdates+nOK);
		
	}

	/**
	 * Check the integrity of a specific data unit. 
	 * @param du data unit to check. 
	 * @param saveFirst flag must be true to update database prior to test if doing a single event. Otherwise data will have been updated anyway. 
	 */
	private boolean checkDataIntegrity(DetectionGroupDataUnit du, boolean saveFirst) {
		subTabLogging = detectionGroupLogging.getSubLogging();
		PamConnection con = DBControlUnit.findConnection();
		String desc = String.format("Detection group UID %d at %s", du.getUID(), PamCalendar.formatDBDateTime(du.getTimeMilliseconds()));
		String idList = "( " + du.getDatabaseIndex() + " )";
		ArrayList<PamSubtableData> stData = subTabLogging.loadSubtableData(con, detectionGroupLogging, idList, null);
		if (stData == null) {
			System.out.println("Error loading sub table data for event uid " + du.getUID());
			return false;
		}
		if (stData.size() == 0) {
			System.out.printf("%s has no sub detections and should be deleted\n", desc);
			return false;
		}
		boolean updated = false;
		int n = stData.size();
		long startT = stData.get(0).getChildUTC();
		long lastT = stData.get(n-1).getChildUTC();
		if (n != du.getSubDetectionsCount()) {
			System.out.printf("%s has %d sub detections, not %d and has been updated\n", desc, n, du.getSubDetectionsCount());
//			du.setnSubDetections(n);
			updated = true;
		}
		if (startT != du.getTimeMilliseconds()) {
			System.out.printf("%s should really have start time %s and has been updated\n", desc, PamCalendar.formatDBDateTime(startT));
			du.setTimeMilliseconds(startT);
			updated = true;
		}
		if (lastT > du.getEndTimeInMilliseconds() || lastT < du.getEndTimeInMilliseconds()-2000) {
			// this is tricky, since it can't account for the duration of the last item in there. 
			System.out.printf("%s with end time %s should really have end time %s and has been updated\n", desc, 
					PamCalendar.formatDBDateTime(du.getEndTimeInMilliseconds()), PamCalendar.formatDBDateTime(lastT));
			du.setDurationInMilliseconds(lastT-startT);
			updated = true;
		}
		if (updated) {
			du.updateDataUnit(System.currentTimeMillis());
		}
		return updated;
	}

}
