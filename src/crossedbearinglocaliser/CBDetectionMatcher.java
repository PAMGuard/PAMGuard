package crossedbearinglocaliser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class CBDetectionMatcher {

	private List<CBMatchGroup> matchGroups = new LinkedList<CBMatchGroup>();
	private List<PamDataUnit> unusedList = new LinkedList<>();
	private double speedOfSound;

	public CBDetectionMatcher(double speedOfSound) {
		this.speedOfSound = speedOfSound;
	}

	/**
	 * Add new data into the matching system. First search existing groups, 
	 * then see if we can match with existing unused data units, then give
	 * up and put it in the list of unused units. 
	 * @param dataUnit New data unit.
	 * @return new group or group this unit was added to. 
	 */
	public synchronized CBMatchGroup newData(PamDataUnit dataUnit) {
		/*
		 * Either find an existing group or start a new one by matching to data in 
		 * the unused list, or if both those fail, add to the unused list.  
		 */
		for (CBMatchGroup matchGroup:matchGroups) {
			if (matchGroup.canMatch(dataUnit)) {
				matchGroup.addDataUnit(dataUnit);
				/*
				 * Found an existing group
				 */
				return matchGroup;
			}
		}
		ListIterator<PamDataUnit> it = unusedList.listIterator();
		while (it.hasNext()) {
			PamDataUnit oldDataUnit = it.next();
			if (canMatch(oldDataUnit, dataUnit)) {
				CBMatchGroup newGroup = new CBMatchGroup(oldDataUnit, dataUnit);
				/*
				 * Remove the old unit from the unused list
				 */
				it.remove();
				/*
				 * Make and return a new group
				 */
				matchGroups.add(newGroup);
				return newGroup;
			}
		}
		/*
		 * Doesnt' match anything, so put it into the spares list. 
		 */
		unusedList.add(dataUnit);

		/**
		 * Nothing worth returning ...
		 */
		return null;
	}
	
	/**
	 * Clean up old data - anything having a time in it < then 
	 * the given time here since it will no longer 
	 * be possible for anything to match. 
	 * @param timeMillis latest time for a group or data unit start. 
	 */
	public synchronized void cleanOldData(long timeMillis, int minToKeep) {
		/**
		 * Modify so it always leaves a couple of items in the list...
		 * If it's detections they will be pretty recent (perhaps not 
		 * for baleen whales) if it's manual data they may be older but 
		 * there will only be one or two of them. 
		 */
		timeMillis -= 3000;
		Iterator<CBMatchGroup> groupIt = matchGroups.iterator();
		int maxToDelete = matchGroups.size() - minToKeep;
		while (groupIt.hasNext() && maxToDelete > 0) {
			CBMatchGroup g = groupIt.next();
			if (g.getTimeMilliseconds() < timeMillis) {
				groupIt.remove();
				maxToDelete --;
			}
			else {
				break; // no point in looking further !
			}
		}
		Iterator<PamDataUnit> duIt = unusedList.iterator();
		maxToDelete = unusedList.size() - minToKeep;
		while (duIt.hasNext() && maxToDelete > 0) {
			PamDataUnit du = duIt.next();
			if (du.getTimeMilliseconds() < timeMillis) {
				duIt.remove();
				maxToDelete --;
			}
			else {
				break;
			}
		}
	}
	
	/**
	 * Can two data units match, based on the distance between their origins and
	 * the time difference between them. 
	 * @param unit1 Data unit 1
	 * @param unit2 Data unit 2
	 * @return true if it's possible they are the same call on different hydrophone groups. 
	 */
	public boolean canMatch(PamDataUnit unit1, PamDataUnit unit2) {
		double originDist = unit1.getOriginLatLong(false).distanceToMetres(unit2.getOriginLatLong(false));
		double timeDist = Math.abs(unit1.getTimeMilliseconds()-unit2.getTimeMilliseconds()) * speedOfSound / 1000.;
		/**
		 * timeDist should be less than originDist. All 10% leeway. 
		 */
		if (timeDist > originDist*1.1) {
			return false;
		}
		else {
			return true;
		}
	}

	public class CBMatchGroup extends SuperDetection<PamDataUnit>{

//		private ArrayList<PamDataUnit> detList = new ArrayList<>();
		
		private long groupEndMillis;

		public CBMatchGroup(PamDataUnit dataUnit) {
			super(dataUnit.getTimeMilliseconds());
			addSubDetection(dataUnit);
			groupEndMillis = dataUnit.getTimeMilliseconds();
		}

		public CBMatchGroup(PamDataUnit dataUnit1, PamDataUnit dataUnit2) {
			super(dataUnit1.getTimeMilliseconds());
			addSubDetection(dataUnit1);
			addSubDetection(dataUnit2);
			setTimeMilliseconds(Math.min(dataUnit1.getTimeMilliseconds(), dataUnit2.getTimeMilliseconds()));
			groupEndMillis = Math.max(dataUnit1.getTimeMilliseconds(), dataUnit2.getTimeMilliseconds());
		}
		
		public void addDataUnit(PamDataUnit dataUnit) {
			addSubDetection(dataUnit);
			setTimeMilliseconds(Math.min(getTimeMilliseconds(), dataUnit.getTimeMilliseconds()));
			groupEndMillis = Math.max(groupEndMillis, dataUnit.getTimeMilliseconds());
		}
		
		/**
		 * Can the data unit match in time with every other data unit
		 * in the system. 
		 * @param dataUnit
		 * @return true if match is possible. 
		 */
		public boolean canMatch(PamDataUnit dataUnit) {
			/**
			 * All data units that get into this system should have an origin
			 * so no point in doing any more testing for null's. Will have to 
			 * change this if class used in a different way in the future. 
			 */
			int nSub = getSubDetectionsCount();
			for (int i = 0; i < nSub; i++) {
				if (!CBDetectionMatcher.this.canMatch(dataUnit, getSubDetection(i))) {
					return false;
				}
					
			}
			return true; // no failures !
		}
	}

}
