package difar.trackedGroups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.Timer;

import clipgenerator.ClipDisplayDataBlock;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.dataSelector.DifarDataSelectCreator;
import difar.dataSelector.DifarDataSelector;
import Array.ArrayManager;
import Array.StreamerDataUnit;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.SystemTiming;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.RequestCancellationObject;

/**
 * Data block contains summary statistics for tracked groups of difar bearings
 * Summary statistics are calculated not only on an individual channel, but 
 * individually for each sonobuoy deployment. 
 * 
 * For n sonobuoys and m groups there should be n x m data units. 
 * 
 * @author Brian Miller
 *
 */
public class TrackedGroupDataBlock extends PamDataBlock<PamDataUnit> {

	private TrackedGroupProcess trackedGroupProcess;
	private DifarControl difarControl;
	private DifarDataSelectCreator dataSelectCreator;
	

	public TrackedGroupDataBlock(String dataName, DifarControl difarControl, 
			TrackedGroupProcess parentProcess, int channelMap) {
		super(DifarDataUnit.class, dataName, parentProcess, channelMap);

		this.trackedGroupProcess = parentProcess;
		this.difarControl = difarControl;
		
		addLocalisationContents(LocContents.HAS_BEARING);
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public synchronized void clearAll() {
		if (shouldClear()) {
			super.clearAll();
		}
	}
	
	/**
	 * Work out whether or not queues should be cleared at start. 
	 * @return true if queue shoudld be cleared. 
	 */
	private boolean shouldClear() {
		if (difarControl.isViewer()) return true;
		return difarControl.getDifarParameters().clearProcessedDataAtStart;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
//	@Override
//	public DataSelectorCreator getDataSelectCreator() {
//		if (dataSelectCreator == null) {
//			dataSelectCreator = new DifarDataSelectCreator(difarControl, this);
//		}
//		return dataSelectCreator;
//	}
	
	public synchronized TrackedGroupDataUnit findDataUnit(long timeMS, int channels, String groupName) {
		
		if (getListIterator(ITERATOR_END)==null) return null;
		
		TrackedGroupDataUnit unit = null;
		ListIterator<PamDataUnit> listIterator = getListIterator(ITERATOR_END);
		while (listIterator.hasPrevious()) {
			unit = (TrackedGroupDataUnit) listIterator.previous();
			
			if (unit.getBuoyStartTime() == timeMS &&
				(channels == 0 || channels == unit.getChannelBitmap()) &&
				groupName.equals(unit.getGroupName())) {
				return unit;
			}			
			
		}
		return null;

	}

	/**
	 * Find all of the data units in memory for a given group. 
	 * @param groupName
	 * @return
	 */
	public synchronized ArrayList<PamDataUnit> findDataUnits(String groupName) {
		
		ArrayList<PamDataUnit> dataUnits = new ArrayList<PamDataUnit>();
		if (getListIterator(ITERATOR_END)==null) return null;
		
		PamDataUnit unit = null;
		ListIterator<PamDataUnit> listIterator = getListIterator(ITERATOR_END);
		while (listIterator.hasPrevious()) {
			unit = (TrackedGroupDataUnit) listIterator.previous();
			
			if (((TrackedGroupDataUnit) unit).getGroupName().equals(groupName)) {
				dataUnits.add((PamDataUnit) unit);
			}			
			
		}
		return dataUnits;
	}

	/**
	 * Given a difar bearing, find the tracked group that most closely 
	 * matches to this bearing. 
	 * @param difarDataUnit
	 * @return string containing the nearest group
	 */
	public String getNearestGroup(DifarDataUnit difarDataUnit) {
		double minError = Double.MAX_VALUE;
		double tempError;
		double threshold = 10; //TODO: Make threshold user-adjustable
		String nearestGroup = null;
		if (getListIterator(ITERATOR_END)==null) return null;

		// Find the deployment time of the sonobuoy for this difarDataUnit
		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getPreceedingUnit(
						difarDataUnit.getTimeMilliseconds(), difarDataUnit.getChannelBitmap());
		long sbStartTime = Long.MIN_VALUE;
		if (sdu != null){
			sbStartTime = sdu.getTimeMilliseconds();
		} 
		/*  Loop over all units, but only look at groups 
		 *  from the correct channel and sonobuoy. 
		 */
		TrackedGroupDataUnit unit = null;
		ListIterator<PamDataUnit> listIterator = getListIterator(ITERATOR_END);

		while (listIterator.hasPrevious()) {
			unit = (TrackedGroupDataUnit) listIterator.previous();

			// Don't eve consider the DefaultGroup, since this group will
			//  be a hodgepodge of miscellaneous bearings
			if (unit.getGroupName().equals(difarControl.getDifarParameters().DefaultGroup)) continue;  
			
			if (unit.getBuoyStartTime() == sbStartTime &&
				(difarDataUnit.getChannelBitmap() == unit.getChannelBitmap())){

				// Find the smallest angle between two bearings
				tempError = Math.abs(unit.getMeanBearing() - difarDataUnit.getTrueAngle());
				tempError = Math.abs((tempError + 180) % 360 - 180);
				
				if (tempError > threshold){
					continue;
				}
				
				if (tempError < minError  & difarControl.isTrackedGroupSelectable(unit.getGroupName())){
					minError = tempError;
					nearestGroup = unit.getGroupName();
				}
			}			
		}
		return nearestGroup;

	}

	public String getGroupSummary(String groupName) {
		TrackedGroupDataUnit unit;
		String summary = "";
		
		String sonobuoys = "";
		String lastDetections = "";
		String lastDetectionTime = "";
		String firstBuoy = "";
		long firstDetected = Long.MAX_VALUE;
		int numBearings = 0;
		double firstBearing = -999;
		LatLong crossInfo = null;
	
		ArrayList<PamDataUnit> dataUnits = findDataUnits(groupName);
		if (dataUnits.isEmpty()) return summary;
		
		for (int i = 0; i<dataUnits.size(); i++){
			unit = (TrackedGroupDataUnit) dataUnits.get(i);
			if (unit.getGroupName().equals(groupName)) {
				if (unit.getFirstDetectionTime() < firstDetected){
					firstDetected = unit.getFirstDetectionTime();
					firstBuoy = unit.getBuoyName();
					firstBearing = unit.getMeanBearing();
				}
				lastDetectionTime = PamCalendar.formatTime(unit.getMostRecentDetectionTime());
				if (firstDetected - unit.getFirstDetectionTime() > PamCalendar.millisPerDay){
					lastDetectionTime = PamCalendar.formatTime(unit.getMostRecentDetectionTime());
				}
				sonobuoys += unit.getBuoyName() + ", ";
				lastDetections += String.format("%d bearings on %s. Mean: <b>%3.0°</b> (Latest: %3.0° at %s)<br>",
						unit.getNumBearings(), unit.getBuoyName(), unit.getMeanBearing(),
						unit.getMostRecentBearing(), lastDetectionTime);
				numBearings += unit.getNumBearings();
			}
			if (unit.getDifarCrossing() != null){
				crossInfo = unit.getDifarCrossing().getCrossLocation();
			}
		}
		summary += String.format("<html>%s - ",groupName);
		if (crossInfo != null){
			summary += String.format("%s", crossInfo);
		}
		summary += String.format(" (%d bearings)<br><br>",numBearings);
		summary += lastDetections;
		summary += String.format("<br>First detected on sonobuoy %s, %3.0° at %s<br>", 
				firstBuoy, firstBearing, PamCalendar.formatDateTime(firstDetected));
		summary += String.format("Sonobuoys: %s", sonobuoys);
		return summary;
	}


}
