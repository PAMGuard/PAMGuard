package clickDetector;

import java.util.ListIterator;

import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.localisation.ClickGroupLocaliser;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.offlineFuncs.OfflineEventLogging;

/**
 * Something similar to ClickTrainDetector, but working on the output of tracked clicks so that 
 * accurate positions for groups of tracked clicks are calculated and can be displayed / stored.
 * <p>
 * The tracked click localiser is for clicks which have been manually annotated in real time.
 * Doesn't do the localisation, just the data management of grouping clicks together. 
 * 
 * @author Doug Gillespie
 *
 */
public class TrackedClickLocaliser extends PamProcess {

//	ClickGroupDataBlock<OfflineEventDataUnit> trackedClickGroups;
	OfflineEventDataBlock trackedClickGroups;
	
	PamDataBlock<PamDataUnit> trackedClickDataBlock;
	
	ClickControl clickControl;
	
	ClickGroupLocaliser detectionGroupLocaliser;

	private OfflineEventLogging eventLogging;
	
	public TrackedClickLocaliser(ClickControl clickControl, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(clickControl, pamDataBlock);
		this.clickControl = clickControl;
		this.trackedClickDataBlock = pamDataBlock;
		setProcessName("Tracked Click Localiser");
//		trackedClickGroups = new ClickGroupDataBlock<OfflineEventDataUnit>(OfflineEventDataUnit.class, 
//				clickControl.getUnitName() + " Tracked Click localisations", this, clickControl.clickParameters.channelBitmap);
//		addOutputDataBlock(trackedClickGroups);
		trackedClickGroups = clickControl.getClickDetector().getOfflineEventDataBlock();
		
		//create a group localiser to localiser multiple detections.  
		detectionGroupLocaliser = new ClickGroupLocaliser(clickControl); 
	}

	/**
	 * Track a click. 
	 * @param click
	 * @param whaleId 
	 */
	public void trackClick(ClickDetection click, int whaleId) {
		/**
		 * First need to work out if this click is already part of a super group. 
		 */
		OfflineEventDataUnit superDet = (OfflineEventDataUnit) click.getSuperDetection(OfflineEventDataUnit.class);
		if (superDet != null) {
			if (superDet.getEventId() == whaleId) {
				// it's already part of this event, so no need to do anything. 
//				System.out.printf("Click %d is already part of event %d\n", click.clickNumber, click.eventId);
				return;
			}
			else {
//				System.out.printf("Remove click %d from event %d\n", click.clickNumber, click.eventId);
				removeClick(superDet, click);
			}
		}
		/*
		 * Now add the click to it's selected group - which may or may not already exist still. 
		 * This is always called. Do this before updating the click in the datablock so that it can
		 * get loged to the database with it's new event id. 
		 */
		OfflineEventDataUnit bestTrain = groupClicks(click, whaleId);
		bestTrain.setComment("Manual Click Train Detection");
		if (superDet != null) {
			trackedClickDataBlock.updatePamData(click, PamCalendar.getTimeInMillis());
		}
		else {
			trackedClickDataBlock.addPamData(click);
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		/*
		 * Nothing doing here anymore since it's all called directly. 
		 */
//		if (o == clickDataBlock) {
//			TrackedClickGroup bestTrain = groupClicks((ClickDetection) arg);
//			if (bestTrain != null) { // localisation is done in groupclicks
//				localiseGroup(bestTrain);
//			}
//		}
	}

	protected OfflineEventDataUnit groupClicks(ClickDetection newClick, int whaleId) {
		
//		all we need to do is find an event with the same event number and add the clicks to it. 

		if (whaleId == 0) return null;
				
		OfflineEventDataUnit bestTrain = null;
		OfflineEventDataUnit aTrain;
		boolean newGroup = false;
		
		synchronized (trackedClickGroups.getSynchLock()) {
			
			ListIterator<OfflineEventDataUnit> tcIterator = trackedClickGroups.getListIterator(0);
			while (tcIterator.hasNext()) {
				aTrain = tcIterator.next();
				//System.out.println("test train");
				if (aTrain.getStatus() == ClickTrainDetection.STATUS_CLOSED) continue;
				if (aTrain.getStatus() == ClickTrainDetection.STATUS_BINME){
					continue;
				}
				if (whaleId == aTrain.getEventId()) {
					bestTrain = aTrain;
					break; // no need to look further ...
				}
			}
		}
		if (bestTrain != null) {
			bestTrain.addSubDetection(newClick);
		}
		else {
			bestTrain = new OfflineEventDataUnit(newClick);
			newGroup = true;
		}
		
		localiseGroup(bestTrain);
		
		if (newGroup) {
			trackedClickGroups.addPamData(bestTrain);
			/**
			 * Now it gets interesting - if the detection was written to the database then it should have 
			 * a new event number, in which case we'll need to set this on the click itself and update the click. 
			 */
			int dbIndex = bestTrain.getDatabaseIndex();
			if (dbIndex > 0) {
				bestTrain.setEventId(dbIndex);
				bestTrain.setColourIndex(dbIndex);
//				System.out.printf("New click event: Change click id from %d to %d\n", newClick.eventId, dbIndex);
//				newClick.eventId = dbIndex;
				// no need to update click since it's not added to the data block until later. 
//				trackedClickDataBlock.updatePamData(newClick, newClick.getTimeMilliseconds());
			}
			else {
				bestTrain.setEventId((int) bestTrain.getUID());
				bestTrain.setColourIndex(bestTrain.getEventId());
			}
		}
		else {
			trackedClickGroups.updatePamData(bestTrain, newClick.getTimeMilliseconds());
		}
		
		
		return bestTrain;
	}
	
	/*
	 * return true if a localisation is calculated, false otherwise. 
	 */
	public boolean localiseGroup(GroupDetection detectionGroup) {
		if (detectionGroup.getSubDetectionsCount() < 2) {
			return false;
		}
		/*
		 * work out the most likely crossing point of all the clicks
		 * taking their origins from the correct hydrophones, etc. 
		 * The final position will be given as a range and bearing relative to the 
		 * LAST click in the sequence. 
		 * 
		 * Want to do everything by distance, rather than the times used in the click train 
		 * localiser. Will therefore have to get hydrophone positions for every click - which 
		 * could take time, so will modify click localisation to only ever calculate these once
		 * (May have to add to AbstractLocalisation class to achieve this). Will also allow for 
		 * ship course changes, so it will be necessary to do the fit for both the L and R sides 
		 * of the vessel. 
		 * 
		 */ 
//		detectionGroup.getGroupDetectionLocalisation().clearFitData();
		
		//the localiser adds the updates the group localisation. 
		GroupLocalisation groupLoc=detectionGroupLocaliser.runModel(detectionGroup, clickControl.clickParameters.clickLocParams);
		
		return groupLoc!=null;
		
//		boolean gotLoc = detectionGroupLocaliser.localiseDetectionGroup(detectionGroup, 1);
//		boolean gotLoc2 = detectionGroupLocaliser.localiseDetectionGroup(detectionGroup, -1);
//		return gotLoc || gotLoc2;
	}

	protected double closeTrainTime() {
		return 600 * 1000.; // 10 minutes. 
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		
	}

	/**
	 * Remove a click from a click train. 
	 * @param removeClick
	 */
	public void removeClick(ClickDetection click) {
		OfflineEventDataUnit superDet = (OfflineEventDataUnit) click.getSuperDetection(OfflineEventDataUnit.class);
		if (superDet == null) {
			return;
		}
		removeClick(superDet, click);
//		click.eventId = 0;
		click.tracked = false;
		clickControl.getClickDetector().getTrackedClicks().remove(click, true);
	}
	
	/**
	 * Remove a click from a group, deleting the group if it's now empty. <br>
	 * Note though that this function doesn't update the click or it's database entries
	 * (so that functions can update the click rather than remove it entirely if 
	 * it's been assigned to a different event). 
	 * @param clickGroup Click Group
	 * @param click Click to remove. 
	 */
	private void removeClick(OfflineEventDataUnit clickGroup, ClickDetection click) {
		clickGroup.removeSubDetection(click);
		if (clickGroup.getSubDetectionsCount() == 0) {
			// if it's empty, delete the group
			trackedClickGroups.remove(clickGroup, true);
		}
		else {
			// relocalise the group and update it. 
			localiseGroup(clickGroup);
			trackedClickGroups.updatePamData(clickGroup, PamCalendar.getTimeInMillis());
		}
	}

}
