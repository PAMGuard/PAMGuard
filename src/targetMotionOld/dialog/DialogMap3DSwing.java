package targetMotionOld.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Localiser.detectionGroupLocaliser.GroupDetection;
import Map.MapController;
import Map.MapDetectionData;
import Map.MapDetectionsManager;
import Map.MapDetectionsParameters;
import Map.MapParameters;
import Map.SimpleMap;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SubdetectionInfo;
import clickDetector.ClickDataBlock;
import targetMotionOld.TargetMotionLocaliser;

public class DialogMap3DSwing<T extends GroupDetection> extends DialogMap<T> {
	
	public static DialogMapPanel3D dialogMapPanel3D;

	private JPanel mainPanel;

	private MapController mapController;

	private DialogMap3DSwing<T>.TargetMotionMap3D tmMap3D;

	private T currentEvent;

	
	public DialogMap3DSwing(TargetMotionLocaliser<T> targetMotionLocaliser, TargetMotionDialog<T> targetMotionDialog) {
		super(targetMotionLocaliser, targetMotionDialog);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.red);
		mapController = new MapController("TM Map");
		tmMap3D = new TargetMotionMap3D(mapController);
		tmMap3D.setPreferredSize(new Dimension(700, 400));
		
		mainPanel.add(BorderLayout.CENTER, tmMap3D.getComponent());
		MapParameters mapParams = tmMap3D.getMapParameters();
		mapParams.setAllow3D(true);
		mapParams.setShowPanZoom(false);
		mapParams.setShowGpsData(false);
		mapParams.setShowKey(false);
		tmMap3D.showMapObjects();
		
	}

	@Override
	public void setCurrentEventIndex(int eventIndex, Object sender) {

		currentEvent = targetMotionLocaliser.getCurrentEvent();
		MapDetectionsManager mapDetectionsManager = tmMap3D.getMapDetectionsManager();
		mapDetectionsManager.createBlockList();
		//		ArrayList<PamDataBlock> usedDataBlocks = new ArrayList<PamDataBlock>();
		if (currentEvent == null) {
			return;
		}
		MapDetectionData mapDetData = mapDetectionsManager.findDetectionData(currentEvent.getParentDataBlock());
		mapDetData.allAvailable = true;
		mapDetData.select = true;

		List<SubdetectionInfo<PamDataUnit>> subDets = currentEvent.getPresentSubDetections();
		
		int nSub = subDets.size();
		for (int i = 0; i < nSub; i++) {
			PamDataUnit subDet = subDets.get(i).getSubDetection();
			if (subDet == null) {
				continue;
			}
			MapDetectionData subDetData = mapDetectionsManager.findDetectionData(subDet.getParentDataBlock());
			if (subDetData != null && subDetData != mapDetData) {
				mapDetData = subDetData;
				mapDetData.allAvailable = true;
				mapDetData.select = true;
			}
		}
		/* 
		 * find the centre of the event and set that as the map centre.
		 */
		LatLong eventCentre = currentEvent.getOriginLatLong(true);
		if (nSub >= 2) {
			LatLong ll1 = subDets.get(0).getSubDetection().getOriginLatLong(true);
			LatLong ll2 = subDets.get(nSub-1).getSubDetection().getOriginLatLong(true);
			if (ll1 != null & ll2 != null) {
				double lat = (ll1.getLatitude() + ll2.getLatitude())/2.;
				double lon = (ll1.getLongitude() + ll2.getLongitude())/2.;
				eventCentre = new LatLong(lat, lon);
			}
		}
		if (eventCentre != null) {
			tmMap3D.getMapPanel().setMapCentreDegrees(eventCentre);
		}
		tmMap3D.getViewerScroller().setRangeMillis(currentEvent.getTimeMilliseconds(), currentEvent.getEndTimeInMilliseconds(), false);
		tmMap3D.getViewerScroller().setValueMillis(currentEvent.getTimeMilliseconds());
		

		//		ArrayList<MapDetectionData> mapDetectionDatas = new ArrayList<MapDetectionData>();
//		for (int i = 0; i < usedDataBlocks.size(); i++) {
//			MapDetectionData mdd = new MapDetectionData(usedDataBlocks.get(i).getLongDataName());
//			mdd.allAvailable = true;
//			mapDetectionDatas.add(mdd);
//		}
//		MapDetectionsParameters mdp = new MapDetectionsParameters();
//		mdp.setMapDetectionDatas(mapDetectionDatas);
		
	}

	@Override
	public void enableControls() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	@Override
	public void notifyNewResults() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void settings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMap(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	private class TargetMotionMap3D extends SimpleMap {

		public TargetMotionMap3D(MapController mapController) {
			// use bespoke map panel so we can change the drawing behaviour. 
			super(mapController, false, dialogMapPanel3D = new DialogMapPanel3D(mapController, null));
			getViewerScroller().getComponent().setVisible(false);
			getViewerControls().setVisible(false);
		}

		@Override
		public boolean shouldPlot(PamDataBlock pamDataBlock) {
			if (currentEvent == null) {
				return false;
			}
			else {
				return true;
			}
//			if (pamDataBlock instanceof ClickDataBlock) {
//				return true;
//			}
//			else {
//				return false;
//			}
		}

		@Override
		public boolean shouldPlot(PamDataUnit pamDataUnit, MapDetectionData mapDetectionData, long earliestToPlot,
				long now, DataSelector ds) {
			if (pamDataUnit == currentEvent) {
				return true;
			}
			if (currentEvent == pamDataUnit.getSuperDetection(0)) {
				return true;
			}
			return false;
//			// TODO Auto-generated method stub
//			return super.shouldPlot(pamDataUnit, mapDetectionData, earliestToPlot, now, ds);
		}
		
	}

}
