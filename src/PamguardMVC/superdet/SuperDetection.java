package PamguardMVC.superdet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import PamUtils.PamCalendar;
import PamUtils.PamSort;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Class added to a PAMDataunit which can be a SuperDetection.
 * Pretty much contains functionality which was previously in every data unit, but now only 
 * in designated super detections implementing the SuperDetection interface.  <p>
 * A Major change now taking place is that the list of SubdetectionInfo's in viewer mode
 * now contains all infos for the data unit, regardless of whether their actual data unit
 * is there or not. Separating out the infos from the dataunit gives much better control over
 * how superdetection data are handled. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class SuperDetection<T extends PamDataUnit> extends PamDataUnit<T, SuperDetection>{


	/**
	 * Information about subdetections (groups of objects held by this PamDataUnit)
	 */
	private Vector<SubdetectionInfo<T>> subDetections = new Vector<SubdetectionInfo<T>>();

	private Object subDetectionSyncronisation = new Object();
	
	/**
	 * List of database UID's that have been removed. Note that 
	 * this used to be  a list of Id's but that's no longer valid
	 * since clicks might now be associated with multiple event types.   
	 */
	private ArrayList<SubdetectionInfo<T>> subdetectionsRemoved = new ArrayList<SubdetectionInfo<T>>();

	private T lastData;
	

	public SuperDetection(DataUnitBaseData basicData) {
		super(basicData);
	}

	public SuperDetection(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
	}

	public SuperDetection(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	public Object getSubDetectionSyncronisation() {
		return subDetectionSyncronisation;
	}
	
	public boolean isAllowSubdetectionSharing() {
		return false;
	}
	
	/**
	 * Add multiple sub detections from a list. 
	 * @param newSubDets List of new sub detections
	 * @return total number of sub detections now in the super detection
	 */
	public int addSubDetections(List<T> newSubDets) {
		if (newSubDets == null) {
			return 0;
		}
		for (T subDet : newSubDets) {
			addSubDetection(subDet);
		}
		return subDetections.size();
	}

	/**
	 * Add a sub detection to the sub detection list. 
	 * @param subDetection - the sub detection to add.
	 * @return - the total number of sub detections now in the list. . 
	 */
	public int addSubDetection(T subDetection) {
		synchronized (subDetectionSyncronisation) {
			if (subDetections == null) {
				subDetections = new Vector<SubdetectionInfo<T>>();
			}
			if (getLocalisation() != null && 
					subDetection.getLocalisation() != null && 
							getLocalisation().getArrayOrientationVectors() == null) {
				getLocalisation().setArrayAxis(subDetection.getLocalisation().getArrayOrientationVectors());
			}
			checkExistingSuperDetections(subDetection);

			/**
			 * These things may need to go in in the right order - so ...
			 */
			int subDetInd = findSubdetectionInfo(subDetection);
			if (subDetInd >= 0) {
				// this sub det is already in this superdet, so no need to do anything. 
				SubdetectionInfo<T> subDetInfo = subDetections.get(subDetInd);
				subDetInfo.setSubDetection(subDetection); // just in case we only found the info !
				return 0;
			};
			
			SubdetectionInfo<T> info = new SubdetectionInfo<T>(subDetection, getDatabaseIndex(), getUID());
			subDetections.add(info);
			PamSort.oneIterationBackSort(subDetections); // make sure all are in order. 
			
			sortSuperDetTimes();
		}
		subDetection.addSuperDetection(this);
		setChannelBitmap(getChannelBitmap() | subDetection.getChannelBitmap());

		updateDataUnit(System.currentTimeMillis());
		return subDetections.size();
	}


	/**
	 * Check to see if this sub detection has existing super detections, i.e. is 
	 * it already assigned to a different superdet ? If sharing is not allowed, it may then
	 * be removed from any other superdetection
	 * @param subDetection
	 */
	private void checkExistingSuperDetections(T subDetection) {
		if (isAllowSubdetectionSharing()) {
			return;
		}
		// see if the data unit has a super detection. 
		SuperDetection superDet = subDetection.getSuperDetection(this.getClass());
		if (superDet == null) {
			return;
		}
		// no need to do anything if the subdet is part of this superdet, otherwise remove it. 
		if (superDet != this) {
			superDet.removeSubDetection(subDetection);
		}
	}
	
	/**
	 * Now that all data are at least represented by a Subdetectioninfo, knowing 
	 * the start and end times of a super detection is dead easy. 
	 */
	private void sortSuperDetTimes() {
		if (subDetections == null || subDetections.size() == 0) {
			return;
			// ideally this superdet should now be deleted, but that's a different problem. 
		}
		SubdetectionInfo<T> first = subDetections.get(0);
		setTimeMilliseconds(first.getChildUTC());
		T firstData = first.getSubDetection();
		if (firstData != null) {
			setStartSample(firstData.getStartSample());
		}
		
		SubdetectionInfo<T> last = subDetections.get(subDetections.size()-1);
		lastData = last.getSubDetection();
		long lastT = last.getChildUTC();
		if (lastData != null) {
			lastT = lastData.getEndTimeInMilliseconds();
		}
		setDurationInMilliseconds(lastT - getTimeMilliseconds());
		if (firstData != null && lastData != null && firstData.getStartSample() != null && lastData.getStartSample() != null) {
			long sampDur = lastData.getStartSample() - firstData.getStartSample();
			if (lastData.getSampleDuration() != null) {
				sampDur += lastData.getSampleDuration();
			}
			setSampleDuration(sampDur);
		}
	}

	/**
	 * Sorts sub detections into order. Uses the inbuilt comparator
	 * which compares the millisecond times of data units.<p> 
	 */
	public void sortSubDetections() {
		if (subDetections == null) {
			return;
		}
		synchronized (subDetectionSyncronisation) {
			Collections.sort(subDetections);
		}
	}

	/**
	 * Returns the index of the SubdetectionInfo object containing the passed subdetection
	 * 
	 * @param subToFind the subdetection to find
	 * @return index position, or -1 if the subdetection wasn't found
	 */
	public int findSubdetectionInfo(T subToFind) {
		int ind=-1;
		if (subDetections == null) {
			return -1;
		}
		/*
		 * Start at end and work backwards. Assume that subdets are 
		 * sorted so can bomb out as soon as we're getting earlier
		 * in time. this will be efficient when adding to the 
		 * end of the list but slower if we need something at the start
		 */
		int sz = subDetections.size();
		for (int i=sz-1; i>=0; i--) {
			T fromList = subDetections.get(i).getSubDetection();
			if (fromList == null) {
				// beware that there might be info without a data unit. 
				continue;
			}
			if (fromList.equals(subToFind)) {
				ind = i;
				break;
			}
			if (fromList.compareTo(subToFind) < 0) {
				break;
			}
		}
		return ind;
	}
	
	/**
	 * find a sub detection info that has the same times, id's etc. 
	 * @param otherInfo
	 * @return index of found item, or -1
	 */
	public int findSubDetectionInfo(SubdetectionInfo otherInfo) {
		if (subDetections == null) {
			return -1;
		}
		int ind = -1;
		for (SubdetectionInfo inf : subDetections) {
			ind++;
			if (inf.getChildUID() != otherInfo.getChildUID()) {
				continue;
			}
			if (inf.getChildUTC() != otherInfo.getChildUTC()) {
				continue;
			}
			if (inf.getLongName().equalsIgnoreCase(otherInfo.getLongName())) {
				return ind;
			}
		}
		return -1;
	}

	/**
	 * Get the number of sub detections in the super detection. 
	 * note that they may not all be loaded in memory. 
	 * @return Number of sub detections. 
	 */
	public int getSubDetectionsCount() {
		if (subDetections == null) return 0;
		return subDetections.size();
	}
	
	/**
	 * Get the number of sub detections that are actually in memory. 
	 * @return number of subdets linked in memory
	 */
	public int getLoadedSubDetectionsCount() {
		if (subDetections == null) return 0;
		int n = 0;
		for (SubdetectionInfo<T> subInfo:subDetections) {
			if (subInfo.getSubDetection() != null) {
				n++;
			}
		}
		return n;
	}
	
	/**
	 * Get a basic list of sub detections without their associated info
	 * This may not be ALL of the sub detections, since they may not all be loaded. 
	 * @return an array list of sub detection data units
	 */
	public ArrayList<PamDataUnit<?,?>> getSubDetections() {
		if (subDetections == null) {
			return null;
		}
		ArrayList<PamDataUnit<?,?>> subDets = new ArrayList<>(getSubDetectionsCount());
		for (SubdetectionInfo<T> subInfo:subDetections) {
			T subDet = subInfo.getSubDetection();
			if (subDet == null) {
				continue;
			}
			subDets.add(subDet);
		}
		return subDets;
	}

	public T getSubDetection(int ind) {
		synchronized (subDetectionSyncronisation) {
			if (subDetections == null) return null;
			return subDetections.get(ind).getSubDetection();
		}
	}

	/**
	 * find a sub detection based on it's time and it's channel map
	 * @param timeMillis time in milliseconds
	 * @param channelBitmap channel map
	 * @return found sub detection, or null if none exists. 
	 */
	public T findSubDetection(long timeMillis, int channelBitmap) {
		if (subDetections == null) {
			return null;
		}
//		for (T aSub:subDetections) {
		for (int i=0; i<subDetections.size(); i++) {
			T aSub = subDetections.get(i).getSubDetection();
			if (aSub.getTimeMilliseconds() == timeMillis && aSub.getChannelBitmap() == channelBitmap) {
				return aSub;
			}
		}
		return null;
	}
	
	/**
	 * Find a sub detection with a specific time, channel map and data unit class. 
	 * @param timeMillis
	 * @param channelBitmap
	 * @param dataClass
	 * @return found sub detection or null.
	 */
	public T findSubDetection(long timeMillis, int channelBitmap, Class dataClass) {
		if (subDetections == null) {
			return null;
		}
//		for (T aSub:subDetections) {
		for (int i=0; i<subDetections.size(); i++) {
			T aSub = subDetections.get(i).getSubDetection();
			if (aSub.getTimeMilliseconds() == timeMillis && 
					aSub.getChannelBitmap() == channelBitmap && 
					aSub.getClass() == dataClass) {
				return aSub;
			}
		}
		return null;
	}
	
	/**
	 * Find a subdetection with a specific UID
	 * @param UID the UID to find
	 * @return the found subdetection, or null
	 */
	public T findSubDetection(long UID) {
		if (subDetections == null) {
			return null;
		}
		for (int i=0; i<subDetections.size(); i++) {
			T aSub = subDetections.get(i).getSubDetection();
			if (aSub.getUID() == UID) {
				return aSub;
			}
		}
		return null;
	}
	
	/**
	 * Replace a sub detection in an event. <p> if the original sub detection 
	 * does not exist, then the new one is added anyway.
	 * <p>NOTE: not sure if this is working properly.  I think that in the
	 * else block, it should simply do a removeSubDetection(oldOne) and
	 * addSubDetection(newOne), which properly sets things up to be
	 * added/removed from the database.  The way it's written now, things
	 * are not deleted from the database.  
	 * Should test this out before using this method 
	 * <p>
	 * DG - so far as I understand this method, it gets called when we've been scrolling back and forth 
	 * through the data, so an event may already have sub detections which weren't cleared on data load
	 * but the sub dets were removed from the datablock but then reloaded with different id's, so the 
	 * new objects need to replace the old ones, but other information doesn't need updating. <br>
	 * but I ask why the events were'nt all cleared of all their sub detections when data were loaded?
	 * @param oldOne old sub detection
	 * @param newOne new sub detection. 
	 */
	public void replaceSubDetection(T oldOne, T newOne) {
//		int ind = subDetections.indexOf(oldOne);
		int ind = findSubdetectionInfo(oldOne);
		if (ind < 0) {
			addSubDetection(newOne);
		}
		else {
			SubdetectionInfo subDetInfo = subDetections.get(ind);
			subDetInfo.setSubDetection(newOne);
			newOne.setUniqueSuperDetection(this);
//			subDetections.remove(ind);
//			oldOne.removeSuperDetection(this);
//			SubdetectionInfo<T> info = new SubdetectionInfo(newOne, this.getUID());
//			subDetections.add(ind, info);
//			newOne.addSuperDetection((U) this);
		}
	}
	
	/**
	 * Remove all sub detection information. 
	 */
	public void removeAllSubDetections() {
		if (subDetections == null) {
			return;
		}
		synchronized (subDetectionSyncronisation) {
			ListIterator<SubdetectionInfo<T>> iter = subDetections.listIterator();
			while (iter.hasNext()) {
				SubdetectionInfo<T> subDetInf = iter.next();
				iter.remove();
				T subDet = subDetInf.getSubDetection();
				if (subDet != null) {
					subDet.removeSuperDetection(this);
				}
				subdetectionsRemoved.add(subDetInf);
			}
		}
	}

	/**
	 * Remove a sub detection from the data unit. 
	 * @param subDetection - the sub detection to remove. 
	 */
	public void removeSubDetection(T subDetection) {
		synchronized (subDetectionSyncronisation) {
			if (subDetections != null) {
//				subDetections.remove(subDetection);
				int ind = findSubdetectionInfo(subDetection);
				if (ind>=0) {
					// put it into the removed list and remove from the used list. 
					subdetectionsRemoved.add(subDetections.remove(ind));
				}
			}
			if (subDetection != null) {
				subDetection.removeSuperDetection(this);
			}
		}
		/**
		 * Need to update the start and end times of the superdetection (this)
		 * if it's the first that's been removed, then it's very likely that the next subdet
		 * is also in memory - but not guaranteed. Not a lot we can do but try.
		 */			
		int sz = subDetections.size();
		if (sz == 0) {
			/**
			 * nothing else there, so impossible to do anything. If the entire event is 
			 * being deleted, that's fine, otherwise it's going to cause an error in the start time. 
			 * Where it all goes wrong is if you delete all clicks currently in memory from an event, but there
			 * are further clicks later on, but not loaded, it becomes impossible for it to set a correct start time
			 * for the event. Even when you scroll forward, if you remove a click it thinks that it must have been from 
			 * the middle of the event since it's time is > the event start time which couldn't be updated. Aaaaaah
			 */
			return; 
		}
		sortSuperDetTimes();
//		T first = subDetections.get(0).getSubDetection();
//		T last = subDetections.get(sz-1).getSubDetection();
//		if (subDetection.getTimeMilliseconds() <= getTimeMilliseconds()) {
//			// seems like it was first, so take the time of the first that's still there. 
//			long stepMillis = first.getTimeMilliseconds()-getTimeMilliseconds();
//			Long currSamp = getStartSample();
//			Long newSamp = first.getStartSample();
//			setTimeMilliseconds(first.getTimeMilliseconds());
//			setDurationInMilliseconds(getDurationInMilliseconds()-stepMillis);
//			try {
//				// lazy, but why check for nulls myself when Java can do it. 
//				setSampleDuration(getSampleDuration()-(newSamp-currSamp));
//			}
//			catch (NullPointerException e) {
//			}
//		}
//		if (subDetection.getTimeMilliseconds() >= getEndTimeInMilliseconds()) {
//			setDurationInMilliseconds(last.getEndTimeInMilliseconds()-getTimeMilliseconds());
//			try {
//				setSampleDuration(last.getStartSample()+last.getSampleDuration()-getStartSample());
//			}
//			catch (NullPointerException e) {
//			}
//		}
		
	}

	/**
	 * Get the full list of subdetection info's (which may not all 
	 * have a sub detection in memory)
	 * @returnlist of subdetectioninfo's.
	 */
	public Vector<SubdetectionInfo<T>> getSubDetectionInfo() {
		return subDetections;
	}
	
	/**
	 * Get sub detection info at a specified index. 
	 * @param idx
	 */
	public SubdetectionInfo getSubdetectionInfo(int idx) {
		return subDetections.get(idx);
	}
	
	/**
	 * returns a list of the database indices of all subdetections that have been removed
	 * @return
	 */
	public ArrayList<SubdetectionInfo<T>> getSubdetectionsRemoved() {
		return subdetectionsRemoved;
	}
	
	/**
	 *  clears the list of subdetections that have been removed 
	 */
	public void clearSubdetectionsRemoved() {
		subdetectionsRemoved.clear();
	}

	public void addSubDetectionInfo(SubdetectionInfo<T> subDetInfo) {
		// check it doesn't already exist. 
		if (findSubDetectionInfo(subDetInfo) >= 0) {
			return;
		}
		subDetections.add(subDetInfo);
	}

	/**
	 * Clears all sub detection data. i.e. the holders of sub detection informaiotn, 
	 * should probably never be called. 
	 */
	public void clearSubDetectionData() {
		if (subDetections == null) {
			return;
		}
		subDetections.clear();
	}
	
	/**
	 * Clears actual detections from the sub detection data. This should be called whenever
	 * more viewer data are loaded, since we only want references to data units that are 
	 * in the actual data unit list. 
	 */
	public void clearSubDetectionObjects() {
		if (subDetections == null) {
			return;
		}
		for (SubdetectionInfo<T> subInf : subDetections) {
			subInf.setSubDetection(null);
		}
	}

}
