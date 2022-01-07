package whistlesAndMoans;

import java.util.ListIterator;

/**
 * Whistle fragmenter which completely discards all regions
 * which have more than one peak in any slice. 
 *  
 * @author Doug Gillespie
 *
 */
public class DiscardingFragmenter implements RegionFragmenter {

	protected ConnectedRegion motherRegion;
	protected int[] slicePeaks;
	protected int maxPeaks, nSlices;
	protected int totalPeaks;
	protected int nFragments = 0;
	
	@Override
	public int fragmentRegion(ConnectedRegion connectedRegion) {
		motherRegion = connectedRegion;
		if (countSlicePeaks() == 1) {
			nFragments = 1;
			return 1;
		}
		else {
			nFragments = 0;
		}
		return nFragments;
	}
	
	/**
	 * Get the maximum number of peaks in any slice of a region. 
	 * @return number of peaks. 
	 */
	private int countSlicePeaks() {
		nSlices = motherRegion.getNumSlices();
		maxPeaks = totalPeaks = 0;
		slicePeaks = new int[nSlices];
		ListIterator<SliceData> li = motherRegion.getSliceData().listIterator();
		int iSlice = 0;
		SliceData sliceData;
		while (li.hasNext()) {
			sliceData = li.next();
			maxPeaks = Math.max(slicePeaks[iSlice] = sliceData.nPeaks, maxPeaks);
			totalPeaks += slicePeaks[iSlice];
			iSlice++;
		}
		
		return maxPeaks;
	}

	@Override
	public ConnectedRegion getFragment(int fragment) {
		if (nFragments == 1) {
			return motherRegion;
		}
		return null;
	}

	@Override
	public int getNumFragments() {
		return nFragments;
	}

}
