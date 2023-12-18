package whistlesAndMoans;

import java.util.List;
import java.util.ListIterator;

public class StubRemover {

	private WhistleMoanControl whistleControl;

	public StubRemover(WhistleMoanControl whistleControl) {
		this.whistleControl = whistleControl;
	}
	
	/**
	 * Remove short stubs that stick out from the sides of a CR. 
	 * @param region
	 * @param searchy 
	 * @param searchx 
	 * @return
	 */
	public int removeStubs(ConnectedRegion region) {
		List<SliceData> sliceData = region.getSliceData();
		/*
		 * sliceData was a linked list, so not good to use getters, have changed to Array list 
		 * so that this function can search efficiently. 
		 * it should contain both a binary map of everything AND a set of separate peaks
		 * each slice peak has three numbers, low peak and high bins and a link to peak index in 
		 * previous slice. 
		 */
		int nSlice = sliceData.size();
		if (nSlice < 2) {
			return 0;
		}
		int diagGap = whistleControl.getWhistleToneParameters().getConnectType() == 4 ? 0 : 1;
		
		SliceData aSlice;;
		for (int i = 0; i < nSlice; i++) {
			aSlice = sliceData.get(i);
			if (aSlice.nPeaks > 1) {
				/*
				 *  now need to follow through and trace every one of those to find
				 *  out how big it is. 
				 */
				int[] sizes = new int[aSlice.nPeaks];
				for (int p = 0; p < aSlice.nPeaks; p++) {
					sizes[p] = aSlice.peakInfo[p][2]-aSlice.peakInfo[p][0]+1;
					sizes[p] = searchStubSize(sliceData, i, p, 1, diagGap, sizes[p]);
				}
				removeSmallStubs(aSlice, sizes);
			}
		}
		// now run backwards. 
		for (int i = nSlice-1; i >= 0; i--) {
			aSlice = sliceData.get(i);
			if (aSlice.nPeaks > 1) {
				/*
				 *  now need to follow through and trace every one of those to find
				 *  out how big it is. 
				 */
				int[] sizes = new int[aSlice.nPeaks];
				for (int p = 0; p < aSlice.nPeaks; p++) {
					sizes[p] = aSlice.peakInfo[p][2]-aSlice.peakInfo[p][0]+1;
					sizes[p] = searchStubSize(sliceData, i, p, -1, diagGap, sizes[p]);
				}
				removeSmallStubs(aSlice, sizes);
			}
		}
		
		return 0;
	}


	/**
	 * Iterate through slices, finding joined bits and adding up total area.
	 * @param sliceData all slice data
	 * @param currentSlice current slice index
	 * @param peakInd current peak index
	 * @param searchDir search direction
	 * @param currentSize current size. 
	 * @return total size
	 */
	private int searchStubSize(List<SliceData> sliceData, int currentSlice, int peakInd, int searchDir, int diagGap, int currentSize) {
		int nSlice = sliceData.size();
		int nextSliceInd = currentSlice + searchDir;
		/**
		 * This function is only every used to throw away very small stubs, so there is no need to get the full size 
		 * of every one. It's OK to return as soon as the size is bigger than the minimum required to make
		 * something worth keeping. This reduces the time spent tracing down every little alley which was 
		 * severely impacting performance for larger whistles. 
		 */
		if (nextSliceInd < 0 || nextSliceInd >= nSlice-1 || currentSize > whistleControl.getWhistleToneParameters().minPixels) {
			return currentSize;
		}
		SliceData nextSlice = sliceData.get(nextSliceInd);
		boolean endSlice = nextSliceInd <= 0 || nextSliceInd >= nSlice-1;
		int[] thisPeak = sliceData.get(currentSlice).peakInfo[peakInd];
		int nPeak = nextSlice.nPeaks;
		for (int p = 0; p < nPeak; p++) {
			// check overlap. accept diagonals for now. but may change to deal better with con4. 
			int[] nextPeak = nextSlice.peakInfo[p];
			if (nextPeak[0]-thisPeak[2] > diagGap || thisPeak[0]-nextPeak[2] > diagGap) {
				// next peak was too high or too low to match this one, so continue. 
				continue;
			}
			currentSize += nextPeak[2]-nextPeak[0]+1; // add the size of this peak. 
			if (!endSlice) {
				// keep going forwards (or backwards) looking for more peaks to match nextPeak. 
				currentSize += searchStubSize(sliceData, nextSliceInd, p, searchDir, diagGap, currentSize);
			}
		}
		return currentSize;
	}
	
	/**
	 * Remove small stubs. Criteria for removal are NOT being the largest and also 
	 * being smaller than a minimum size. 
	 * @param aSlice
	 * @param sizes
	 */
	private void removeSmallStubs(SliceData aSlice, int[] sizes) {
		int biggestInd = 0;
		int biggestSize = sizes[0];
		for (int i = 1; i < sizes.length; i++) {
			if (sizes[i] > biggestSize) {
				biggestSize = sizes[i];
				biggestInd = i;
			}
		}
		int minSize = whistleControl.getWhistleToneParameters().minPixels;
		boolean[] keep = new boolean[sizes.length];
		int nKeep = 0;
		for (int i = 0; i < sizes.length; i++) {
			if (keep[i] = (sizes[i] >= minSize || sizes[i] == biggestSize)) {
				nKeep++;
			}
		}
		int[][] newPeakData = new int[nKeep][];
		int ik = 0;
		for (int i = 0; i < sizes.length; i++) {
			if (keep[i]) {
				newPeakData[ik++] = aSlice.peakInfo[i];
			}
		}
		aSlice.peakInfo = newPeakData;
		aSlice.nPeaks = nKeep;
		
	}
}
