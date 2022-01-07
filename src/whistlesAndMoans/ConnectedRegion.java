package whistlesAndMoans;

import java.util.ListIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fftManager.FFTDataUnit;


public class ConnectedRegion implements Cloneable{

	private int channel;

	private int regionNumber;

	private int firstSlice;

	private SliceData currentSlice;

	private int sliceHeight;

	private List<SliceData> sliceData;

	private int totalPixels = 0;

	private boolean growing;

	private int[] freqRange;

	private int[] peakFreqsBins;

	/**
	 * list of time bins - there may be gaps in the whistle !
	 */
	private int[] timesBins;	

	private int maxPeaks; // max number of peaks in any part of the system.

	/**
	 * After splitting of complex regions, these are
	 * Number of fragments connected to this at the start
	 * and at the end. 
	 * These are set in FragmentingFragmenter and used in RejoiningFragmenter. 
	 */
	private int nJoinedStart, nJoinedEnd;

	/**
	 * Constructor used during initial region detection process. 
	 * @param channel channel number
	 * @param firstSlice first slice number
	 * @param regionNumber region number
	 * @param height fft length
	 */
	public ConnectedRegion(int channel, int firstSlice, int regionNumber, int height) {
		super();
		this.channel = channel;
		this.firstSlice = firstSlice;
		this.regionNumber = regionNumber;
		this.sliceHeight = height;
		// 17/5/21 changed to array list to make searching branches more efficient. 
		// remove never used on the list, and only add to end, so this should be fine. 
		sliceData = new ArrayList<SliceData>();
	}

	/**
	 * Constructor to create a new connected region during fragmentation.
	 * Constructor starts by making a single peak in a single slice. 
	 *  
	 * @param oldRegion old mother region which is being fragmented
	 * @param sliceData start slice in the old region
	 * @param peakNumber peak number in the slice
	 * @param nJoinedStart number of contours that joined this one before the were fragmented. 
	 */
	public ConnectedRegion(ConnectedRegion oldRegion, SliceData oldSlice, int peakNumber, int nJoinedStart) {
		this.channel = oldRegion.channel;
		this.firstSlice = oldSlice.sliceNumber;
		this.regionNumber = oldRegion.regionNumber;
		this.sliceHeight = oldRegion.sliceHeight;
		setNJoinedStart(nJoinedStart);
		sliceData = new LinkedList<SliceData>();
		addSlicePeak(oldSlice, peakNumber);
	}

	/**
	 * Extend a region during fragmentation taking a single peak from a new slice. 
	 * @param oldSlice reference to slice in old unit
	 * @param peakNumber number of peak to copy
	 */
	public void extendRegion(SliceData oldSlice, int peakNumber) {
		addSlicePeak(oldSlice, peakNumber);
	}

	int nCalls = 0;
	/**
	 * Add in a slice during offline mode. 
	 * @param i slice number (or slice index as it's read back !
	 * @param peakInfo
	 */
	public void addOfflineSlice(SliceData newSlice) {
		sliceData.add(newSlice);
		int nPeaks = newSlice.nPeaks;
		int[][] peakInfo = newSlice.peakInfo;
				
		if (sliceData.size() == 1) {
			freqRange = new int[2];
			freqRange[0] = peakInfo[0][0];
			freqRange[1] = peakInfo[nPeaks-1][2];
		}
		else {
			freqRange[0] = Math.min(freqRange[0], peakInfo[0][0]);
			freqRange[1] = Math.max(freqRange[1], peakInfo[nPeaks-1][2]);
		}
	}

	/**
	 * Check for slices repeating twice. 
	 * @return
	 */
	public boolean checkRepeatslices() {
		ListIterator<SliceData> l1, l2;
		l1 = sliceData.listIterator();
		SliceData s1, s2;
		int n = 0;
		while (l1.hasNext()) {
			s1 = l1.next();
			l2 = sliceData.listIterator(++n);
			while (l2.hasNext()) {
				s2 = l2.next();
				if (s1.sliceNumber == s2.sliceNumber) {
					return true;
				}
				if (s1.fftDataUnit.getStartSample() == s2.fftDataUnit.getStartSample()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Extend a region during fragmentation
	 * @param oldSlice old slice
	 * @param peakNumber peak to steal. 
	 */
	private void addSlicePeak(SliceData oldSlice, int peakNumber) {
		sliceData.add(currentSlice = new SliceData(oldSlice, peakNumber));
		totalPixels += oldSlice.peakInfo[peakNumber][2]-oldSlice.peakInfo[peakNumber][0]+1;
		if (freqRange == null) {
			freqRange = new int[2];
			freqRange[0] = oldSlice.peakInfo[peakNumber][0];
			freqRange[1] = oldSlice.peakInfo[peakNumber][2];
		}
		else {
			freqRange[0] = Math.min(freqRange[0], oldSlice.peakInfo[peakNumber][0]);
			freqRange[1] = Math.max(freqRange[1], oldSlice.peakInfo[peakNumber][2]);
		}
	}

	/**
	 * Prepare a region for re-use (saves some time reallocating the object
	 * if they were only used very briefly). 
	 * @param channel
	 * @param firstSlice
	 * @param regionNumber
	 * @param height
	 */
	public void resetRegion(int channel, int firstSlice, int regionNumber, int height) {
		this.channel = channel;
		this.firstSlice = firstSlice;
		this.regionNumber = regionNumber;
		this.sliceHeight = height;
		sliceData.clear();
	}

	public void addPixel(int iCol, int iRow, FFTDataUnit fftDataUnit) {
		if (currentSlice == null || currentSlice.sliceNumber != iCol) {
			currentSlice = findSlice(iCol);
			if (currentSlice == null) {
				addSlice(iCol, sliceHeight, fftDataUnit);
			}
		}
		//		System.out.println("Add pixel to slice " + iCol + " pixel " + iRow);
		currentSlice.slicePixs[iRow] = true;
		totalPixels++;
		growing = true;
	}

	private SliceData addSlice(int iSlice, int sliceLen, FFTDataUnit fftDataUnit) {
		currentSlice = new SliceData(iSlice, sliceLen, fftDataUnit);
		sliceData.add(currentSlice);
		return currentSlice;
	}

	private SliceData findSlice(int iSlice) {
		if (currentSlice != null && currentSlice.sliceNumber == iSlice) {
			return currentSlice;
		}
		ListIterator<SliceData> sliceIterator = sliceData.listIterator(sliceData.size());
		SliceData sliceData;
		while (sliceIterator.hasPrevious()) {
			sliceData = sliceIterator.previous();
			if (sliceData.sliceNumber == iSlice) {
				return currentSlice = sliceData;
			}
		}
		return null;
	}

	/**
	 * Fill small holes in the regions which will mess up later fragmentation
	 * @param maxHeight max height of hole in pixels
	 * @param maxWidth max width of hole in pixels
	 */
	protected void fillSmallHoles(int maxHeight, int maxWidth) {
		int[] nPeaks = new int[getNumSlices()];
		int[] nHoles = new int[getNumSlices()];
		int maxPeaks;
		SliceData[] sliceArray = new SliceData[getNumSlices()];
		ListIterator<SliceData> li = this.getSliceData().listIterator();
		int iSlice = 0;
		SliceData sliceData;
		while (li.hasNext()) {
			sliceData = li.next();
			nPeaks[iSlice] = sliceData.nPeaks;
			sliceArray[iSlice] = sliceData;
			for (int i = 1; i < sliceData.nPeaks; i++) {
				if (sliceData.peakInfo[i][0] - sliceData.peakInfo[i-1][2] <= maxHeight) {
					nHoles[iSlice]++;
				}
			}
			iSlice++;
		}
		int firstHoleSlice = 0;
		boolean hasHoles = false;
		for (int i = 0; i < getNumSlices(); i++) {
			if (hasHoles == false && nHoles[i] > 0) {
				firstHoleSlice = i;
				hasHoles = true;
			}
			else if (hasHoles && nHoles[i] == 0) {
				if (i - firstHoleSlice <= maxWidth) {
					for (int h = firstHoleSlice; h < i-1; h++) {
						fillHoles(sliceArray[h], maxHeight, nHoles[h]);
					}
				}
			}
		}
	}

	/**
	 * Fill in small holes in the slice. 
	 * @param slice slice to fill 
	 * @param maxHeight max hole height. 
	 */
	private void fillHoles(SliceData slice, int maxHeight, int nHoles) {
		int nPeak = slice.nPeaks;
		int[][] newPeaks = new int[slice.nPeaks-nHoles][4];
		int iNew = 0;

		for (int i = 0; i < 4; i++) {
			newPeaks[0][i] = slice.peakInfo[0][i];
		}
		for (int i = 1; i < slice.nPeaks; i++) {
			if (slice.peakInfo[i][0] - newPeaks[iNew][2] <= maxHeight) {
				// fill the hole
				newPeaks[iNew][2] = slice.peakInfo[i][2];
			}
			else {
				iNew++;
				for (int j = 0; j < 4; j++) {
					newPeaks[iNew][j] = slice.peakInfo[i][j];
				}
			}
		}
		slice.peakInfo = newPeaks;
		slice.nPeaks = newPeaks.length;
	}

	/**
	 * Merge another region into this region and then tell the array 
	 * of regions that all references to that other region now refer to this
	 * one.  
	 * @param region other region
	 * @param regionArray array of region references to update. 
	 */
	public void mergeRegion(ConnectedRegion region) {
		SliceData otherSlice;
		ListIterator<SliceData> otherSliceIterator = region.sliceData.listIterator();
		while (otherSliceIterator.hasNext()) {
			otherSlice = otherSliceIterator.next();
			for (int i = 0; i < sliceHeight; i++) {
				if (otherSlice.slicePixs[i]) {
					addPixel(otherSlice.sliceNumber, i, otherSlice.fftDataUnit);
				}
			}
		}
	}

	/**
	 * for merging two regions during the fragmentation process. 
	 * Can be assumed that all slices in r2 come AFTER those in this.
	 * @param r2
	 * @return true if there is a time clash
	 */
	public void mergeFragmentedRegion(ConnectedRegion r2) {
		ListIterator<SliceData> r2Slices = r2.getSliceData().listIterator();
		SliceData sd;
		while (r2Slices.hasNext()) {
			sliceData.add(sd=r2Slices.next());
			totalPixels += sd.peakInfo[0][2]-sd.peakInfo[0][0]+1;
			freqRange[0] = Math.min(freqRange[0], sd.peakInfo[0][0]);
			freqRange[1] = Math.max(freqRange[1], sd.peakInfo[0][2]);
		}
		setNJoinedEnd(r2.getNJoinedEnd());
	}

	public void recycle() {
		// clear the main array ready for recycling to free up 
		// memory and save doing it later. 
		sliceData.clear();
		totalPixels = 0;
	}

	/**
	 * Go through the data and remove 
	 * create triplets of into about each part of 
	 * each slice in the region. 
	 * Called durign initial detection, not after refragmentation of complex shapes. 
	 */
	public void condenseInfo() {

		//		breakShortBridges();
		//		fillSmallHoles();

		SliceData slice, prevSlice = null;
		ListIterator<SliceData> sliceIterator = sliceData.listIterator();
		freqRange = new int[2];
		freqRange[0] = Integer.MAX_VALUE;
		freqRange[1] = 0;
		int[] sliceRange;
		maxPeaks = 0;
		timesBins = new int[sliceData.size()];
		peakFreqsBins = new int[sliceData.size()];
		int iSlice = 0;
		while (sliceIterator.hasNext()) {
			slice = sliceIterator.next();
			sliceRange = slice.condenseInfo(prevSlice);
			freqRange[0] = Math.min(freqRange[0], sliceRange[0]);
			freqRange[1] = Math.max(freqRange[1], sliceRange[1]);
			maxPeaks = Math.max(maxPeaks, slice.nPeaks);
			prevSlice = slice;
			timesBins[iSlice] = slice.sliceNumber;
			peakFreqsBins[iSlice] = slice.getPeakBin();
			iSlice++;
		}
	}

	/**
	 * similar to condenseInfo, but for refragmetned fragments. 
	 */
	public void cleanFragmentedFragment() {

		timesBins = new int[sliceData.size()];
		peakFreqsBins = new int[sliceData.size()];
		ListIterator<SliceData> li = getSliceData().listIterator();
		SliceData sliceData;
		if (getSliceData().size() < 1) {
			return;
		}
		int iSlice = 0;
		while (li.hasNext()) {
			sliceData = li.next();
			sliceData.peakInfo[0][3] = 0;
			timesBins[iSlice] = sliceData.sliceNumber;
			peakFreqsBins[iSlice] = sliceData.peakInfo[0][1];
			iSlice++;
		}
	}

	/**
	 * Break short bridges in time. 
	 */
	protected void breakShortBridges() {
		SliceData slices[] = new SliceData[3];
		if (getNumSlices() < 3) {
			return;
		}
		ListIterator<SliceData>  li = sliceData.listIterator();
		slices[0] = li.next();
		slices[1] = li.next();
		while (li.hasNext()) {
			slices[2] = li.next();
			for (int i = 0; i < slices[0].sliceLength; i++) {
				if (slices[0].slicePixs[i] == false & 
						slices[1].slicePixs[i] == true & 
						slices[2].slicePixs[i] == false) {
					slices[1].slicePixs[i] = false;
				}
			}
			slices[0] = slices[1];
			slices[1] = slices[2];
		}
	}

	protected void fillSmallHoles() {
		SliceData slices[] = new SliceData[3];
		if (getNumSlices() < 3) {
			return;
		}
		ListIterator<SliceData>  li = sliceData.listIterator();
		slices[0] = li.next();
		slices[1] = li.next();
		while (li.hasNext()) {
			slices[2] = li.next();
			for (int i = 0; i < slices[0].sliceLength; i++) {
				if (slices[0].slicePixs[i] == true & 
						slices[1].slicePixs[i] == false & 
						slices[2].slicePixs[i] == true) {
					slices[1].slicePixs[i] = true;
				}
			}
			slices[0] = slices[1];
			slices[1] = slices[2];
		}
	}


	public boolean isGrowing() {
		return growing;
	}

	public void setGrowing(boolean growing) {
		this.growing = growing;
	}

	public int getChannel() {
		return channel;
	}

	public int getRegionNumber() {
		return regionNumber;
	}

	public int getFirstSlice() {
		return firstSlice;
	}

	public int getSliceHeight() {
		return sliceHeight;
	}

	public List<SliceData> getSliceData() {
		return sliceData;
	}

	public int getNumSlices() {
		if (sliceData == null) {
			return 0;
		}
		return sliceData.size();		
	}

	public int getTotalPixels() {
		return totalPixels;
	}

	public long getStartMillis() {
		if (sliceData == null) {
			return 0;
		}
		if (sliceData.get(0).fftDataUnit == null) {
			return 0;
		}
		return sliceData.get(0).fftDataUnit.getTimeMilliseconds();
	}

	public long getDuration() {
		if (sliceData == null) {
			return 0;
		}
		if (sliceData.get(0).fftDataUnit == null) {
			return 0;
		}
		return sliceData.get(sliceData.size()-1).fftDataUnit.getStartSample() - 
		sliceData.get(0).fftDataUnit.getStartSample() +
		sliceData.get(sliceData.size()-1).fftDataUnit.getSampleDuration();
	}

	public long getStartSample() {
		if (sliceData == null) {
			return 0;
		}
		if (sliceData.get(0).fftDataUnit == null) {
			return 0;
		}
		return sliceData.get(0).fftDataUnit.getStartSample();
	}

	public void sayRegion() {
		System.out.println("Region " + regionNumber + " has " + sliceData.size() + " slices with " + totalPixels + " pixs");
		char[] chars = new char[sliceHeight];

		ListIterator<SliceData> sliceIterator = sliceData.listIterator(0);
		SliceData sliceData;
		while (sliceIterator.hasNext()) {
			sliceData = sliceIterator.next();
			for (int i = 0; i < sliceHeight; i++) {
				if (sliceData.slicePixs[i]) 
					chars[i] = 'X';
				else
					chars[i] = '.';
			}
			for (int iP = 0; iP < sliceData.nPeaks; iP++) {
				chars[sliceData.peakInfo[iP][0]] = 'l';
				chars[sliceData.peakInfo[iP][2]] = 'u';
				chars[sliceData.peakInfo[iP][1]] = 'p';
			}
			System.out.println(sliceData.sliceNumber + " " + new String(chars));

		}
	}

	public double calculateRMSAmplitude() {
		double a = 0;
		int n = 0;
		ListIterator<SliceData> si;
		synchronized (sliceData) {
			si = sliceData.listIterator();
			while (si.hasNext()) {
				a += si.next().getRmsAmplitude();
				n++;
			}
		}
		return Math.sqrt(a/n);
	}

	public int[] getFreqRange() {
		return freqRange;
	}

	public int getMaxPeaks() {
		return maxPeaks;
	}

	public int[] getPeakFreqsBins() {
		return peakFreqsBins;
	}

	public int[] getTimesBins() {
		return timesBins;
	}

	public SliceData getFirstSliceData() {
		if (sliceData == null || sliceData.size() == 0) {
			return null;
		}
		return sliceData.get(0);
	}

	public SliceData getLastSliceData() {
		if (sliceData == null || sliceData.size() == 0) {
			return null;
		}
		return sliceData.get(sliceData.size()-1);
	}

	/**
	 * @param nJoinedStart the nJoinedStart to set
	 */
	public void setNJoinedStart(int nJoinedStart) {
		this.nJoinedStart = nJoinedStart;
	}

	/**
	 * @return the nJoinedStart
	 */
	public int getNJoinedStart() {
		return nJoinedStart;
	}

	/**
	 * @param nJoinedEnd the nJoinedEnd to set
	 */
	public void setNJoinedEnd(int nJoinedEnd) {
		this.nJoinedEnd = nJoinedEnd;
	}

	/**
	 * @return the nJoinedEnd
	 */
	public int getNJoinedEnd() {
		return nJoinedEnd;
	}

	/**
	 * Gradient at the start of the shape for the first peak
	 * in pixels per pixel. 
	 * <br>
	 * Used for rapid region matching in RejoiningFragmenter
	 * @param nBins number of bins to measure over.
	 * @return gradient. 
	 */
	public double getStartGradient(int nBins) {
		nBins = Math.min(nBins, getNumSlices());
		return ((double) (sliceData.get(nBins-1).peakInfo[0][1]-getFirstSliceData().peakInfo[0][1])) /
		(double) (nBins-1);
	}

	/**
	 * Gradient at the end of the shape for the first peak
	 * in pixels per pixel. 
	 * <br>
	 * Used for rapid region matching in RejoiningFragmenter
	 * @param nBins number of bins to measure over.
	 * @return gradient. 
	 */
	public double getEndGradient(int nBins) {
		int totBins = getNumSlices();
		nBins = Math.min(nBins, totBins);
		return ((double) (sliceData.get(totBins-1).peakInfo[0][1]-sliceData.get(totBins-nBins).peakInfo[0][1])) /
		(double) (nBins-1);
	}

	/**
	 * 
	 * @return true if two or more whistles cross in this region
	 */
	public boolean isCross(int maxCrossLength) {
		return (nJoinedEnd == nJoinedStart && nJoinedStart > 1 && getNumSlices() <= maxCrossLength);
	}

	/**
	 * 
	 * @return true if two or more whistles merged into this Region
	 */
	public boolean isMerge(int maxCrossLength) {
		if (isCross(maxCrossLength)) {
			return false;
		}
		return (nJoinedStart > 1);
	}

	/**
	 * 
	 * @return true if this region splits into two or more parts
	 */
	public boolean isSplit(int maxCrossLength) {
		if (isCross(maxCrossLength)) {
			return false;
		}
		return (nJoinedEnd > 1);
	}

	/**
	 * 
	 * @return true if > 2 sounds joined this region at the start and > 2
	 * left at the end, but they were not the same number of sounds. 
	 */
	public boolean isRightMess() {
		return (nJoinedStart > 2 && nJoinedEnd > 2 && nJoinedStart != nJoinedEnd);
	}
}
