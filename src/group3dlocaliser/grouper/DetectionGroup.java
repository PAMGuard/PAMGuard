package group3dlocaliser.grouper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import PamguardMVC.PamDataUnit;

public class DetectionGroup {

	private List<PamDataUnit> dataUnits[];
	
	private int totalChannelMap;

	private int nChannelGroups;
	
	private long[] lastSamples;
	
	private long veryLastSample;
	
	public DetectionGroup(int nChannelGroups, int channelGroup, PamDataUnit pamDataUnit) {
		this.nChannelGroups = nChannelGroups;
		lastSamples = new long[nChannelGroups];
		dataUnits = new List[nChannelGroups];
		for (int i = 0; i < nChannelGroups; i++) {
			dataUnits[i] = new ArrayList<>();
		}
		if (pamDataUnit != null) {
			addDataUnit(channelGroup, pamDataUnit);
		}
	}
	
	public void addDataUnit(int channelGroup, PamDataUnit pamDataUnit) {
		dataUnits[channelGroup].add(pamDataUnit);
		totalChannelMap |= pamDataUnit.getChannelBitmap();
		lastSamples[channelGroup] = pamDataUnit.getStartSample();
		if (pamDataUnit.getSampleDuration() != null) {
			lastSamples[channelGroup] += pamDataUnit.getSampleDuration();
		}
		veryLastSample = Math.max(veryLastSample, lastSamples[channelGroup]);
	}

	/**
	 * @return the dataUnits
	 */
	public List<PamDataUnit> getDataUnits(int iChannelGroup) {
		return dataUnits[iChannelGroup];
	}
	
	public int getNumUnits() {
		int n = 0;
		for (int i = 0; i < nChannelGroups; i++) {
			n += dataUnits[i].size();
		}
		return n;
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

}
