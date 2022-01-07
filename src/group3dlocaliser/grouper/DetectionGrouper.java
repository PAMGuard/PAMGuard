package group3dlocaliser.grouper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.debug.Debug;

/**
 * Receives detections, which may come from multiple channel groups, which in 
 * the case of the click detector means they may not be in order (or for 
 * any other detector in fact) and may have come out of multiple threads, 
 * so can get dead messy.
 * @author dg50
 *
 */
public class DetectionGrouper {
	/**
	 * HTF is this going to work then ? 
	 * Data units arrive. May have to wait a while (indeterminate due to multi threading) 
	 * before a group can be closed. 
	 * 
	 * As each data unit arrives, it can either add to an existing group or start a new group, 
	 * Groups can be closed and sent for processing when either a) nothing has arrived for some 
	 * time or b) the latest of every channel group within the existing group has arrived. 
	 * If data arrive too fast on a big array, then groups might never close. So what ? 
	 * At TEL, array dimension is 11m = 7.3ms. Also considering large towed systems which might
	 * have hydrophone clusters 100's or metres apart - 150m = 100ms. That's quite a long time 
	 * and will inevitably lead to aliasing problems for some types of clicks. Will need to 
	 * stay up front about this - that's the reality ! Probably not the job of this class to 
	 * tell people what to do, but to make various options available to more sophisticated
	 * decision processes.  
	 * 
	 * Splitting will have to take place in two stages since group detection is either asynchronous 
	 * or is at least happening in chunks meaning that there will often be a 1/10 s delay before data 
	 * from other groups arrive. So initially make a pretty broad group, where we look for a quite large 
	 * gap between data units, then sort the units so that they are at least in order and look to 
	 * break that group up if at all possible. 
	 * 
	 * After the two stage splitting, it's still possible that there will be multiple combinations
	 * by which the data can be combined. In this case a list of possible combinations is built and
	 * passed back to the groupMontor which will have to decide what to do (e.g. process all 
	 * combinations and take the one with lowest chi2).   
	 */

	private GroupedSourceParameters groupedSourceParameters;

	private DetectionGrouperParams detectionGrouperParams = new DetectionGrouperParams();

	private Hashtable<Integer, GroupInformation> groupLasts = new Hashtable<>();

	private List<DetectionGroup> developingGroups;

	private GroupInformation[] groupLastList;

	private double[][] interGroupSamples;

	private double[] maxInterGroupSamples;

	private double maxInterGroupSample;

	private FirstGrouping motherGroup;

	private double sampleRate;

	private int nChannelGroups;

	private DetectionGroupMonitor detectionGroupMonitor;

	private DataSelector dataSelector;

	private boolean isViewer;

	public DetectionGrouper(DetectionGroupMonitor detectionGroupMonitor) {
		this.detectionGroupMonitor = detectionGroupMonitor;
		developingGroups = new LinkedList<>();
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}

	public synchronized void newData(PamDataUnit pamDataUnit) {
		if (pamDataUnit.getUID() == 7616002298L) {
			Debug.out.println("On " + pamDataUnit.getUID());
		}
		int detChans = pamDataUnit.getChannelBitmap();
		Long sampleNo = pamDataUnit.getStartSample();
//		if (pamDataUnit.getUID() == 291039474) {
//			System.out.println("Found it");
//		}
		if (sampleNo == null) {
			/*
			 *  make up a sample number based on the millis or the mother group thing can't work. 
			 *  The mother group thing needs to be in samples no tmillis since millis are
			 *  too course.  
			 */
			sampleNo = (long) ((pamDataUnit.getTimeMilliseconds()-PamCalendar.getSessionStartTime()) * sampleRate / 1000.);
			pamDataUnit.setStartSample(sampleNo);
		}
		Long sampDur = pamDataUnit.getSampleDuration();
		if (sampDur == null) {
			Double durMillis = pamDataUnit.getDurationInMilliseconds();
			if (durMillis == null) {
				pamDataUnit.setSampleDuration(0L);
			}
			else {
				pamDataUnit.setSampleDuration((long) (durMillis * sampleRate / 1000));
			}
		}
		
		//		groupLasts.
		int iChanGroup = findDataGroup(detChans);
		if (iChanGroup == -1) {
			//			This could happen if no channels in a group are selected. 
			return;
		}
		if (detectionGrouperParams.dataSelectOption == DetectionGrouperParams.DATA_SELECT_ALL) {
			if (canSelectData(pamDataUnit) == false) {
				return;
			}
		}
		/**
		 * This is very hard since data units are not in order, so 
		 * in principle, a whole load of data units from one channel group
		 * might be linked up if channels arrive fast enough on other groups. 
		 * What about things like whistles, where they may have quite 
		 * different start times, but still overlap. 
		 * Am going to try making a pretty big group, even if it has quite big
		 * gaps in one channel group and then sort it, then break it up further
		 * once everything is more nicely in order. 
		 * 
		 */
		maybeCloseMotherGroup(iChanGroup, sampleNo);
		
		motherGroup.addDataUnit(iChanGroup, pamDataUnit);
		//			DetectionGroup oldGroup = findExistingGroup(iChanGroup, pamDataUnit);
		//		if (oldGroup == null) {
		//			oldGroup = new DetectionGroup(pamDataUnit);
		//			developingGroups.add(oldGroup);
		//		}
	}

	public synchronized void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		if (motherGroup == null) {
			return;
		}
		if (motherGroup.getTotalChannelMap() == 0) {
			return;
		}
//		if (maybeCloseMotherGroup(0, sampleNumber - (long) (sampleRate / 2))) {
//			System.out.println("Mother group closed on timer");
//		}
	}
	
	private synchronized boolean  maybeCloseMotherGroup(int iChanGroup, long currentSample) {
		long bufferSamples = (long) (0.00*sampleRate);
		if (shouldCloseMotherGroup(iChanGroup, currentSample, bufferSamples)) {
			//			if (motherGroup.getNumUnits() == 1 || motherGroup.getNumUnits() == 1) {
			//				shouldCloseMotherGroup(iChanGroup, pamDataUnit, bufferSamples);
			//			}
			closeMotherGroup();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Can the current mother group be closed ? Based on
	 * the arrival of a new data unit. 
	 * @param iChannelGroup group that data units lives in
	 * @param pamDataUnit the data unit. 
	 * @return true if group should be closed. 
	 */
	private boolean shouldCloseMotherGroup(int iChannelGroup, long sampleNumber, long bufferSamples) {
		/*
		 * Can close if the start time of this data unit is > bufferSamples
		 * after the end time of other data unit's on it's own channel (assuming that's 0)
		 * plus the maximum delay we'd have to wait for something on other channels.  
		 */
		if (motherGroup.getNumUnits() > nChannelGroups * 4) {
			return true;
		}
		long[] lastSamples = motherGroup.getLastSamples();
		boolean shouldClose = false;
		for (int i = 0; i < lastSamples.length; i++) {
			if (lastSamples[i] == 0) {
				continue;
			}
			long delay = sampleNumber-lastSamples[i];
			if (delay > interGroupSamples[iChannelGroup][i]+bufferSamples) {
				shouldClose = true;
			}
			else {
				return false;
			}
		}
		return shouldClose;
	}

	/**
	 * Can the current mother group be closed ? Based purely
	 * on the clock sample. this calls periodically on a timer
	 * to ensure data are used in the event that no more data 
	 * arrive. 
	 * @param currentDaqSample
	 * @return
	 */
	private boolean shouldCloseMotherGroup(long currentDaqSample) {
		if (motherGroup.getNumUnits() > nChannelGroups * 4) {
			return true;
		}
		long lastSample = motherGroup.getVeryLastSample();
		if (lastSample == 0) {
			return false;
		}
	
		long bufferSamples = (long) (0.2*sampleRate + maxInterGroupSample);
		return currentDaqSample > lastSample + bufferSamples;
	}

	/**
	 * Close the existing group and make a new one. 
	 */
	private synchronized void closeMotherGroup() {
		processFirstGroup(motherGroup);
		motherGroup = new FirstGrouping(maxInterGroupSamples.length, 0, null);
	
	}

	public boolean canSelectData(PamDataUnit dataUnit) {
		if (dataSelector == null) {
			return true;
		}
		else {
			return dataSelector.scoreData(dataUnit) > 0.;
		}
	}

	/**
	 * Have got a group of clicks which at least seem to have a gap after them. 
	 * however, due to asynchronous ops of different bits of detector it's quite 
	 * possible that this can further split into fully independent groups 
	 * before anything clever needs doing. 
	 * @param motherGroup
	 */
	private void processFirstGroup(FirstGrouping motherGroup) {
		List<PamDataUnit> dataUnits = motherGroup.getDataUnits();
//		for (int i = 0; i < dataUnits.size(); i++) {
//			if (dataUnits.get(i).getUID() == 291039474) {
//				System.out.println("Processing group with click 291039474");
//				System.out.printf("Process first group with %d datas channels %s\n", dataUnits.size(), PamUtils.getChannelList(motherGroup.getTotalChannelMap()));
//			}
//		}
		if (dataUnits.size() < Math.max(1, detectionGrouperParams.minSubGroups)) {
			return;
		}
		// sort them (this will be done on time and channel number)
		Collections.sort(dataUnits);		
		/**
		 * Make a line array and see which units match. 
		 */
		int links[] = new int[dataUnits.size()];
		PamDataUnit iUnit, jUnit;
		for (int i = 0; i < dataUnits.size(); i++) {
			iUnit = dataUnits.get(i);
			for (int j = i+1; j < dataUnits.size(); j++) {
				jUnit = dataUnits.get(j);
				if (isMatchPossible(iUnit, jUnit)) {
					for (int k = i+1; k <= j; k++) {
						links[k]++;
					}
				}
			}
		}
		// now go through links and see where there are zeros ...
		int iGroup;
		DetectionGroup splitGroup = null;
		for (int i = 0; i < dataUnits.size(); i++) {
			iUnit = dataUnits.get(i);
			iGroup = findDataGroup(iUnit.getChannelBitmap());
			if (links[i] == 0) {
				if (splitGroup != null) {
					processDetectionGroup(splitGroup);
				}
				splitGroup = new DetectionGroup(nChannelGroups, iGroup, iUnit);
			}
			else {
				splitGroup.addDataUnit(iGroup, iUnit);
			}
		}
		// and th elast one ....
		processDetectionGroup(splitGroup);

	}

	/**
	 * See if it's possible that two data units are the same sound
	 * Can assume that jUnit is coming after iUnit.
	 * @param iUnit first data unit
	 * @param jUnit second data unit
	 * @return true if they might be the same sound. 
	 */
	private boolean isMatchPossible(PamDataUnit iUnit, PamDataUnit jUnit) {
		int iGroup, jGroup;
		iGroup = findDataGroup(iUnit.getChannelBitmap());
		jGroup = findDataGroup(jUnit.getChannelBitmap());
		if (iGroup == jGroup) {
			return false;
		}
		long end1 = iUnit.getLastSample();
		long start2 = jUnit.getStartSample();
		return start2-end1 < interGroupSamples[iGroup][jGroup];
	}

	private void processDetectionGroup(DetectionGroup detectionGroup) {
//		System.out.printf("Second Process group with %d units, channels %s\n", detectionGroup.getNumUnits(), 
//				PamUtils.getChannelList(detectionGroup.getTotalChannelMap()));
		/*
		 * It's possible that the detection group has a large number of possible linkages 
		 * in it. Need to break it down into smaller units with one channel group 
		 * max from each cluster and process those. 
		 * Can this be done without assigning a master channel ?
		 */
		List<PamDataUnit>[] dataUnits = new List[nChannelGroups];
		/*
		 *  if there are n[i] units in every group, then the total number of possibilities
		 *  is (n[0]+1)*(n[1]+1), etc, minus combinations that only have one or no groups. 
		 *  will have to go through the whole bloody lot and see which are valid. 
		 *  Would be easier if the data units were split into separate groups when 
		 *  the groups were created !
		 */
		DetectionGroupedSet detectionGroupedSet = new DetectionGroupedSet();
		int totalCombinations = 1;
		int[] listInds = new int[nChannelGroups];
		for (int i = 0; i < nChannelGroups; i++) {
			dataUnits[i] = detectionGroup.getDataUnits(i);
			Collections.sort(dataUnits[i]);
			totalCombinations *= (dataUnits[i].size()+1);
			listInds[i] = 0;
		}
		int nAccepted = 0;
		PamDataUnit[] currentCombination = new PamDataUnit[nChannelGroups];
		for (int i = 0; i < totalCombinations; i++) {
			for (int g = 0; g < nChannelGroups; g++) {
				if (listInds[g] < dataUnits[g].size()) {
					currentCombination[g] = dataUnits[g].get(listInds[g]);
				}
				else {
					currentCombination[g] = null;
				}
			}
			boolean wantCombo = acceptGroupCombination(currentCombination);
			// use or bin that combination here ...
			if (wantCombo) {
				//			System.out.printf("Grouping combination %d of %d = %d", i, totalCombinations, listInds[0]);
				//			for (int g = 1; g < nChannelGroups; g++) {
				//				System.out.printf(",%d", listInds[g]);
				//			}
				//			System.out.printf(" accept = %s\n", new Boolean(wantCombo).toString());
			}
			if (wantCombo) {
				List<PamDataUnit> aList = new ArrayList<>();
				int nSel = 0;
				for (int g = 0; g < nChannelGroups; g++) {
					if (currentCombination[g] != null) {
						aList.add(currentCombination[g]);
						if (detectionGrouperParams.dataSelectOption == DetectionGrouperParams.DATA_SELECT_MIN_N) {
							if (canSelectData(currentCombination[g])) {
								nSel++;
							}
						}
						else {
							nSel++;
						}
					}
				}
				if (detectionGrouperParams.dataSelectOption == DetectionGrouperParams.DATA_SELECT_MIN_N) {
					if (nSel >= detectionGrouperParams.dataSelectMinimum) {
						nAccepted++;
						detectionGroupedSet.addGroup(aList);
					}
				}
				else {
					nAccepted++;
					detectionGroupedSet.addGroup(aList);
				}
				if (nAccepted > 0 && detectionGrouperParams.groupingChoice == DetectionGrouperParams.GROUPS_FIRST_ONLY) {
					break; // don't look for any further combinations. 
				}
			}

			// now try to increase just one of the list indexes
			for (int g = 0; g < nChannelGroups; g++) {
				if (listInds[g] < dataUnits[g].size()) {
					listInds[g]++;
					break;
				}
				else {
					listInds[g] = 0;
				}
			}
		}
		if (nAccepted > 0) {
//			System.out.printf("Accepted %d of %d data combinations\n", detectionGroupedSet.getNumGroups(), totalCombinations);
			detectionGroupMonitor.newGroupedDataSet(detectionGroupedSet);
		}

	}

	/**
	 * This group of possible group combinations seems reasonable, so use it ...
	 * @param currentCombination
	 * @return
	 */
	private boolean acceptGroupCombination(PamDataUnit[] currentCombination) {
		int nUsedGroups = 0;
		int totalChannels = 0;
		for (int i = 0; i < currentCombination.length; i++) {
			if (currentCombination[i] != null) {
				nUsedGroups ++;
				totalChannels |= currentCombination[i].getChannelBitmap();
			}
		}
		if (nUsedGroups < detectionGrouperParams.minSubGroups) {
			return false;
		}
		int usedChannels = PamUtils.getNumChannels(totalChannels);
//		if (nUsedGroups < 3) {
//			return false;
//		}
		// check all possible timing combinations are reasonable. 
		long start1, end1, start2, end2;
		for (int i = 0; i < currentCombination.length; i++) {
			PamDataUnit u1 = currentCombination[i];
			if (u1 == null) {
				continue;
			}
			end1 = start1 = u1.getStartSample();
			if (u1.getSampleDuration() != null) {
				end1 += u1.getSampleDuration();
			}
			for (int j = i+1; j < currentCombination.length; j++) {
				PamDataUnit u2 = currentCombination[j];
				if (u2 == null) {
					continue;
				}
				end2 = start2 = u2.getStartSample();
				if (u2.getSampleDuration() != null) {
					end2 += u2.getSampleDuration();
				}
				/**
				 * Interesting feature ! this is working off samples, but in NR mode
				 * the sample counts are way off so it needs to be done in milliseconds
				 */
				if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
					if (start2-end1 > interGroupSamples[i][j]) {
						return false;
					}
					if (start1-end2 > interGroupSamples[i][j]) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * See if it's possible that this detection can fit within 
	 * one of the existing detection groups. 
	 * @param channelGroupIndex
	 * @param pamDataUnit
	 * @return
	 */
	private DetectionGroup findExistingGroup(int channelGroupIndex, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Find the index of the group containing these channels. 
	 * @param detChans
	 * @return group index or -1 if one not found. 
	 */
	private int findDataGroup(int detChans) {
		if (groupLastList == null) {
			return -1;
		}
		for (int i = 0; i < groupLastList.length; i++) {
			if ((detChans & groupLastList[i].channelMap) != 0) {
				return i;
			}
		}
		return -1;
	}

	public void clear() {
		groupLasts.clear();
		developingGroups.clear();
	}
	/**
	 * @return the groupedSourceParameters
	 */
	public GroupedSourceParameters getGroupedSourceParameters() {
		return groupedSourceParameters;
	}

	/**
	 * @param groupedSourceParameters the groupedSourceParameters to set
	 */
	public void setGroupedSourceParameters(GroupedSourceParameters groupedSourceParameters, double sampleRate) {
		this.groupedSourceParameters = groupedSourceParameters;
		groupLasts.clear();
		if (groupedSourceParameters == null) {
			return;
		}
		nChannelGroups = groupedSourceParameters.countChannelGroups();
		motherGroup = new FirstGrouping(nChannelGroups, 0, null);
		groupLastList = new GroupInformation[nChannelGroups];
		for (int i = 0; i < nChannelGroups; i++) {
			int groupChans = groupedSourceParameters.getGroupChannels(i);
			GroupInformation gi = new GroupInformation(groupChans, 0);
			groupLasts.put(groupChans, gi);
			groupLastList[i] = gi;
		}
		calculateGroupDelays(sampleRate);
	}

	public void calculateGroupDelays(double sampleRate) {
		this.sampleRate = sampleRate;
		if (groupLastList == null) {
			return;
		}
		int nGroup = groupLastList.length;
		interGroupSamples = new double[nGroup][nGroup];
		maxInterGroupSamples = new double[nGroup];
		maxInterGroupSample = 0;
		// get the maximum distance between all hydrophones in both groups. 
		double maxDist;
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		SnapshotGeometry geometry = currentArray.getSnapshotGeometry(PamCalendar.getTimeInMillis());
		for (int i = 0; i < nGroup; i++) {
			GroupInformation g1 = groupLastList[i];
			int n1 = PamUtils.getNumChannels(g1.channelMap);
			for (int j = i; j < nGroup; j++) {
				GroupInformation g2 = groupLastList[j];
				int n2 = PamUtils.getNumChannels(g2.channelMap);
				maxDist = 0;
				for (int p1 = 0; p1 < n1; p1++) {
					int h1 = PamUtils.getNthChannel(p1, g1.channelMap);
					for (int p2 = 0; p2 < n2; p2++) {
						int h2 = PamUtils.getNthChannel(p2, g2.channelMap);
						double hDist = geometry.getPairDistance(h1, h2);
						maxDist = Math.max(maxDist, hDist);
					}
				}
				maxDist *= (sampleRate / currentArray.getSpeedOfSound());
				interGroupSamples[i][j] = maxDist;
				interGroupSamples[j][i] = maxDist;
				maxInterGroupSamples[i] = Math.max(maxInterGroupSamples[i], maxDist);
				maxInterGroupSample = Math.max(maxInterGroupSample, maxDist);
			}
		}

	}

	/**
	 * @return the detectionGrouperParams
	 */
	public DetectionGrouperParams getDetectionGrouperParams() {
		return detectionGrouperParams;
	}

	/**
	 * @param detectionGrouperParams the detectionGrouperParams to set
	 */
	public void setDetectionGrouperParams(DetectionGrouperParams detectionGrouperParams) {
		this.detectionGrouperParams = detectionGrouperParams;
	}

	/**
	 * @return the dataSelector
	 */
	public DataSelector getDataSelector() {
		return dataSelector;
	}

	/**
	 * @param dataSelector the dataSelector to set
	 */
	public void setDataSelector(DataSelector dataSelector) {
		this.dataSelector = dataSelector;
	}


}
