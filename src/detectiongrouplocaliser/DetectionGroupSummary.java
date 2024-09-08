package detectiongrouplocaliser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import javafx.scene.input.MouseEvent;

/**
 * A summary of information associated with some marked data units. 
 * <p>Should refactor at some point so that this is standard within the OverlayMarker. 
 * @author Doug Gillespie
 *
 */
public class DetectionGroupSummary {

	private List<PamDataUnit> dataList;
	
//	private ArrayList<PamDataBlock> usedDataBlocks;
	private HashSet<PamDataBlock> usedDataBlocks = new HashSet();
	
	private HashSet<PamDataUnit> superDetections = new HashSet();
	
	private HashSet<PamDataBlock> superDataBlocks = new HashSet();
	
	private long firstTimeMillis = 0;

	private long lastTimeMillis;

	private MouseEvent mouseEvent;

	private OverlayMarker overlayMarker;

	private OverlayMark overlayMark;
	
	/**
	 * The currently selected or focused data unit; 
	 */
	private int focusedUnitIndex=0;

	/**
	 * 
	 * @param overlayMark 
	 * @param overlayMarker 
	 * @param mouseEvent 
	 * @param dataList List of data units selected from a mark on one of the displays. 
	 */
	public DetectionGroupSummary(MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark, List<PamDataUnit> dataList) {
		this.dataList = dataList;
		this.mouseEvent = mouseEvent;
		this.overlayMarker = overlayMarker;
		this.overlayMark = overlayMark;
		analyseList();
	}
	
	public DetectionGroupSummary(MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark, PamDataUnit dataUnit) {
		this.dataList = new ArrayList<PamDataUnit>(); 
		this.dataList.add(dataUnit); 
		this.mouseEvent = mouseEvent;
		this.overlayMarker = overlayMarker;
		this.overlayMark = overlayMark;
		analyseList();
	}

	/**
	 * Making three extra lists - <p>
	 * 1. DataBlock parents of the data units in the list.
	 * 2. Super detections of the listed data units. 
	 * 3. DataBlock parents of the super detections.  
	 */
	private void analyseList() {
		if (dataList == null) {
			return;
		}
		firstTimeMillis = Long.MAX_VALUE;
		lastTimeMillis = Long.MIN_VALUE;
		for (PamDataUnit dataUnit:dataList) {
			usedDataBlocks.add(dataUnit.getParentDataBlock());
			firstTimeMillis = Math.min(firstTimeMillis, dataUnit.getTimeMilliseconds());
			lastTimeMillis = Math.max(lastTimeMillis, dataUnit.getTimeMilliseconds());
			int ns = dataUnit.getSuperDetectionsCount();
			for (int i = 0; i < ns; i++) {
				PamDataUnit supDet;
				superDetections.add(supDet = dataUnit.getSuperDetection(i));
				superDataBlocks.add(supDet.getParentDataBlock());
			}
		}
	}
	
	/**
	 * 
	 * @return The number of data units in the list 
	 */
	public int getNumDataUnits() {
		if (dataList == null) {
			return 0;
		}
		return dataList.size();
	}
	
	/**
	 * 
	 * @return The number of different data blocks for the units in the list. 
	 */
	public int getNumDataBlocks() {
		return usedDataBlocks.size();
	}
	
	/**
	 * 
	 * @return The number of super detections
	 */
	public int getNumSuperDetections() {
		return superDetections.size();
	}
	
	/**
	 * 
	 * @return The number of unique super detection data blocks. 
	 */
	public int getNumSuperDataBlocks() {
		return superDataBlocks.size();
	}

	/**
	 * @return the dataList
	 */
	public List<PamDataUnit> getDataList() {
		return dataList;
	}

	/**
	 * @return the usedDataBlocks
	 */
	public HashSet<PamDataBlock> getUsedDataBlocks() {
		return usedDataBlocks;
	}

	/**
	 * @return the superDetections
	 */
	public HashSet<PamDataUnit> getSuperDetections() {
		return superDetections;
	}

	/**
	 * @return the superDataBlocks
	 */
	public HashSet<PamDataBlock> getSuperDataBlocks() {
		return superDataBlocks;
	}

	/**
	 * 
	 * @return the time of the first data unit.
	 */
	public long getFirstTimeMillis() {
		return firstTimeMillis;
	}

	/**
	 * @return the time of the last data unit
	 */
	public long getLastTimeMillis() {
		return lastTimeMillis;
	}

	/**
	 * @return the mouseEvent
	 */
	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	/**
	 * @return the overlayMarker
	 */
	public OverlayMarker getOverlayMarker() {
		return overlayMarker;
	}

	/**
	 * @return the overlayMark
	 */
	public OverlayMark getOverlayMark() {
		return overlayMark;
	}

	/**
	 * Set the index of the currently focused data unit. 
	 * @param i - the index of the unit to focus. 
	 */
	public void setFocusedIndex(int index) {
		this.focusedUnitIndex=index;
	}
	
	/**
	 * Set the index of the currently focused dfata unit. 
	 * @param i - the index of the unit to focus. 
	 */
	public int getFocusedIndex() {
		return focusedUnitIndex;
	}
	

	
}
