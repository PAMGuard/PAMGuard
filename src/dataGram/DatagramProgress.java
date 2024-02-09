package dataGram;

import dataMap.OfflineDataMapPoint;
import javafx.scene.control.ProgressIndicator;
import pamViewFX.pamTask.PamTaskUpdate;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryOfflineDataMapPoint;

/**
 * Progress data on datagram creation 
 * @author Doug Gillespie
 *
 */
public class DatagramProgress extends PamTaskUpdate {

	public static final int STATUS_BLOCKCOUNT = 0;
	public static final int STATUS_STARTINGBLOCK  = 1;
	public static final int STATUS_ENDINGBLOCK  = 2;
	public static final int STATUS_STARTINGFILE = 3;
	public static final int STATUS_ENDINGFILE = 4;
	public static final int STATUS_UNITCOUNT = 5;
	
	public int nDataBlocks;
		
	public int pointsToUpdate;
	
	public int currentPoint;
	
	public PamDataBlock dataBlock;
	
	public OfflineDataMapPoint dataMapPoint;
	public int processedUnits;
	public int totalUnits;

	/**
	 * @param statusType
	 * @param nDataBlocks
	 */
	public DatagramProgress(int statusType, int nDataBlocks) {
		super();
		setStatus(statusType);
		this.nDataBlocks = nDataBlocks;
		setDualProgressUpdate(true);
	}

	/**
	 * 
	 * @param statusType
	 * @param dataBlock
	 */
	public DatagramProgress(int statusType, PamDataBlock dataBlock, int pointsToUpdate) {
		super();
		super.setStatus(statusType);
		this.dataBlock = dataBlock;
		this.pointsToUpdate = pointsToUpdate;
	}

	public DatagramProgress(int statusType, int totalUnits, int processedUnits) {
		super.setStatus(statusType);
		this.totalUnits = totalUnits;
		this.processedUnits = processedUnits;
	}
	/**
	 * @param statusType
	 * @param dataMapPoint
	 */
	public DatagramProgress(int statusType, OfflineDataMapPoint dataMapPoint, int currentPoint) {
		super();
		super.setStatus(statusType);
		this.dataMapPoint = dataMapPoint;
		this.currentPoint = currentPoint;
	}

	@Override
	public String getName() {
		return "Datagram Creation Progress";
	}

	@Override
	public double getProgress() {
		System.out.println("DatagramProgress: " + processedUnits + " tot: " + totalUnits); 
		double progress= ProgressIndicator.INDETERMINATE_PROGRESS;
		switch(getStatus()) {
		case DatagramProgress.STATUS_BLOCKCOUNT:
			break;
		case DatagramProgress.STATUS_STARTINGBLOCK:
			break;
		case DatagramProgress.STATUS_STARTINGFILE:
			break;
		case DatagramProgress.STATUS_ENDINGFILE:
			progress=1.0;
			break;
		case DatagramProgress.STATUS_UNITCOUNT:	
			if (totalUnits>0) progress=((double)(processedUnits)) / totalUnits;
			else progress=0; 
			break;
		}
		return progress;
	}
	
	@Override
	public double getProgress2(){
		double progress;
		if (pointsToUpdate>0) progress=((double)currentPoint)/pointsToUpdate;
		else progress=0; 
		switch(getStatus()) {
		case DatagramProgress.STATUS_BLOCKCOUNT:
			break;
		case DatagramProgress.STATUS_STARTINGBLOCK:
			progress=0; 
			break;
		case DatagramProgress.STATUS_STARTINGFILE:
			progress=((double)currentPoint)/pointsToUpdate;
			break;
		case DatagramProgress.STATUS_ENDINGFILE:
			progress=((double)currentPoint)/pointsToUpdate;
			break;
		case DatagramProgress.STATUS_UNITCOUNT:	
			break;
		}
		return progress;
	}
	
	@Override
	public String getProgressString(){
		String textMsg=""; 
		switch(getStatus()) {
		case DatagramProgress.STATUS_BLOCKCOUNT:
			textMsg="Counting Data";	
			break;
		case DatagramProgress.STATUS_STARTINGBLOCK:
			textMsg="Counting Data";			
			break;
		case DatagramProgress.STATUS_STARTINGFILE:
			BinaryOfflineDataMapPoint currentMapPoint = null;
			if (dataMapPoint != null &&
					BinaryOfflineDataMapPoint.class.isAssignableFrom(dataMapPoint.getClass())) {
				currentMapPoint = (BinaryOfflineDataMapPoint) dataMapPoint;
			}
			else {
				currentMapPoint = null;
			}
			
			if (currentMapPoint == null) {
			textMsg = "Loading File";
			}
			else {
				textMsg=("Loading File " + currentMapPoint);
			}
						
			break;
		case DatagramProgress.STATUS_ENDINGFILE:
			textMsg="Closing file";		
			break;
			
		case DatagramProgress.STATUS_UNITCOUNT:	
			textMsg = String.format("Processing unit %d of %d", 
					processedUnits, totalUnits);
			break;
		}
		return textMsg; 
	}
	
	@Override
	public String getProgressString2(){
		if (dataBlock==null) return ""; 
		return dataBlock.getDataName();
	}

}
