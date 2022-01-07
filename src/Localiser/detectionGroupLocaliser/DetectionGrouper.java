package Localiser.detectionGroupLocaliser;

import java.util.ArrayList;
import java.util.ListIterator;

import PamController.PamControlledUnit;
import PamDetection.PamDetection;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

abstract public class DetectionGrouper<t extends PamDataUnit> {

	private PamControlledUnit pamControlledUnit;
	
	private PamDataBlock<t> sourceDataBlock;
	
	private GroupedSourceParameters groupedSourceParameters;
	
	int nGroups;
	
	private ArrayList<t> matches = new ArrayList<t>();
	
	public DetectionGrouper(PamControlledUnit pamControlledUnit, PamDataBlock<t> sourceDataBlock) {
		this.pamControlledUnit = pamControlledUnit;
		this.sourceDataBlock = sourceDataBlock;
	}

	public void setupGroups(GroupedSourceParameters groupedSourceParameters) {
		this.groupedSourceParameters = groupedSourceParameters;
		nGroups = groupedSourceParameters.countChannelGroups();
	}
	
	/**
	 * Call this just before a detection is added to a PamDataBlock so that 
	 * localisation information can be added to the detection before it goes
	 * off into the PAMGUARD system. 
	 * @param pamDetection new Detection
	 * @return list of matches to this detection. 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<t> findGroups(t pamDetection) {
		if (nGroups < 2) {
			return null;
		}
		/*
		 * pamDetection should not yet be in the sourceDataBlock 
		 */
		t recentUnit;
		matches.clear();
		synchronized (sourceDataBlock.getSynchLock()) {
			ListIterator<t> recentData = sourceDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			while (recentData.hasPrevious()) {
				recentUnit = recentData.previous();
				if (match(pamDetection, recentUnit)) {
					matches.add(recentUnit);
					if (recentUnit.getTimeMilliseconds() < pamDetection.getTimeMilliseconds() - 2000) {
						break;
					}
				}
			}
		}
		if (matches.size() == 0) {
			return null;
		}
		
		return matches;
		
	}
	
	abstract public boolean match(PamDataUnit currentData, PamDataUnit olderData);


	
	
}
