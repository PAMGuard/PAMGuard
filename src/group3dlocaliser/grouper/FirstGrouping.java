package group3dlocaliser.grouper;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;

/**
 * Single list of detections, all in the same list so that 
 * they can easily be sorted prior to final grouping....
 * @author dg50
 *
 */
public class FirstGrouping {
	
	private List<PamDataUnit> dataUnits;

	private int totalChannelMap;

	private int nChannelGroups;
	
	private long[] lastSamples;
	
	private long veryLastSample;
	
	private int[] groupCount;
	
	private int lastChannelGroup;

	public FirstGrouping(int nChannelGroups, int channelGroup, PamDataUnit pamDataUnit) {
		this.nChannelGroups = nChannelGroups;
		lastSamples = new long[nChannelGroups];
		dataUnits = new ArrayList<>();
		groupCount = new int[nChannelGroups];
		if (pamDataUnit != null) {
			addDataUnit(channelGroup, pamDataUnit);
		}
	}
	
	public void addDataUnit(int channelGroup, PamDataUnit pamDataUnit) {
		dataUnits.add(pamDataUnit);
		groupCount[channelGroup] ++;
		totalChannelMap |= pamDataUnit.getChannelBitmap();
		lastSamples[channelGroup] = pamDataUnit.getStartSample();
		if (pamDataUnit.getSampleDuration() != null) {
			lastSamples[channelGroup] += pamDataUnit.getSampleDuration();
		}
		veryLastSample = Math.max(veryLastSample, lastSamples[channelGroup]);
		lastChannelGroup = channelGroup;
	}

//	/**
//	 * @return the dataUnits
//	 */
//	public List<PamDataUnit> getDataUnits(int iChannelGroup) {
//		return dataUnits[iChannelGroup];
//	}
	
	public int getNumUnits() {
		return dataUnits.size();
	}

	/**
	 * @return the totalChannelMap
	 */
	public int getTotalChannelMap() {
		return totalChannelMap;
	}

	/**
	 * @return the nChannelGroups
	 */
	public int getnChannelGroups() {
		return nChannelGroups;
	}

	/**
	 * @return the lastSamples
	 */
	public long[] getLastSamples() {
		return lastSamples;
	}

	/**
	 * @return the veryLastSample
	 */
	public long getVeryLastSample() {
		return veryLastSample;
	}

	/**
	 * @return the dataUnits
	 */
	public List<PamDataUnit> getDataUnits() {
		return dataUnits;
	}

	/**
	 * @return the lastChannelGroup
	 */
	public int getLastChannelGroup() {
		return lastChannelGroup;
	}

}
