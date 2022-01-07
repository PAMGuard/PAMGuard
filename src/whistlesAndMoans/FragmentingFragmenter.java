package whistlesAndMoans;

import java.util.ListIterator;
import java.util.Vector;

public class FragmentingFragmenter extends DiscardingFragmenter {

	protected Vector<ConnectedRegion> fragments = new Vector<ConnectedRegion>();
	
	protected WhistleMoanControl whistleMoanControl;
	
	private boolean discardSmallOnes = true;
	
	public FragmentingFragmenter(WhistleMoanControl whistleMoanControl) {
		super();
		this.whistleMoanControl = whistleMoanControl;
	}

	@Override
	public int fragmentRegion(ConnectedRegion connectedRegion) {
		if (super.fragmentRegion(connectedRegion) == 1) {
			fragments.clear();
			fragments.add(motherRegion);
			return 1; // straight out if there are no fragments. 
		}

//		connectedRegion.fillSmallHoles(8, 2);
		
		createFragments();
		
		cleanFragments();
		
//		if (getNumFragments() == 5) {
//			sayFragments();
//		}
		
		return nFragments;
	}
	
	/**
	 * Split the mother region up into fragments, breaking at every branch and join
	 */
	public void createFragments() {
		fragments.clear();
		if (nSlices < 2) {
			return;
		}
		int[] forwardLinks = new int[maxPeaks];
		int[] backLinks = new int[maxPeaks];
		int[] nForwardLinks = new int[maxPeaks];
		int[] nBackLinks = new int[maxPeaks];
		ConnectedRegion[] thisSliceRegions = new ConnectedRegion[maxPeaks];
		ConnectedRegion[] lastSliceRegions = new ConnectedRegion[maxPeaks];
		ListIterator<SliceData> li = motherRegion.getSliceData().listIterator();
		SliceData thisSlice = null, prevSlice = null;
		int conType = whistleMoanControl.whistleToneParameters.getConnectType();
		/**
		 * At each step there are three basic possibilities
		 * 1) One or more growing whistles don't merge or branch and each can grow further
		 * 2) A whistle branches into two or more parts. 
		 * 3) Two or more whistles merge. 
		 * Have to allow for weird possibilities such as two branches merging to one
		 * at exactly the same time as a new branch starts, so total is always constant at 2.
		 * 
		 *  Merging is easy to detect, since an individual peak will link back to 
		 *  more than one previous whistle. 
		 */
		while (li.hasNext()) {
			thisSlice = li.next();
			if (prevSlice == null) {
				/*
				 * For the first slice, just start a contour for every peak. 
				 */
				for (int iP = 0; iP < thisSlice.nPeaks; iP++) {
					thisSliceRegions[iP] = new ConnectedRegion(motherRegion, thisSlice, iP, 0);
				}
			}
			else {
				
				/*
				 * Now identify whistles in prevSlice which either stop or branch or merge
				 * or continue on
				 */
				/*
				 * As a first possibility, try to deal with situations like this:
553 .........XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX..........................................................
554 ............XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.................................................
555 ...............XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.......................................
556 ..................XXXXXXXXXXXXXXX..XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX..............................
557 .....................XXXXXXXXXXXXXXXX.......XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.......................
558 .........................XXXXXXXXXXXXXXX..............XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX..................
559 .............................XXXXXXXXXXXXXXXX...................XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX......
				 * where there is an obvious split, but a simple system will tend to remerge it. 
				 */
				boolean matchAll = false;
				if (prevSlice.nPeaks == thisSlice.nPeaks) {
					/*
					 * take peaks in order and if there is any match, join them. 
					 */
					matchAll = true;
					for (int iP = 0; iP < prevSlice.nPeaks; iP++) {
						if (matchPeak(prevSlice.peakInfo[iP], thisSlice.peakInfo[iP], conType) == false) {
							matchAll = false;
						}
					}
					if (matchAll) {
						for (int iP = 0; iP < prevSlice.nPeaks; iP++) {
							lastSliceRegions[iP].extendRegion(thisSlice, iP);
						}
					}
				}
				if (matchAll == false) {
					/*
					 * Be  bit more individualistic about it. 
					 */
					/*
					 *  Loop over growing whistles in prevSlice and 
					 *  check for branches. kill any which branch
					 */
					clearIntArray(forwardLinks);
					clearIntArray(backLinks);
					zeroIintArray(nForwardLinks);
					zeroIintArray(nBackLinks);
					/**
					 *  Count the number of forward and back links for each peak
					 *  in each slice. Record link indexes - these will be correct if
					 *  there is a single link.
					 */
					for (int iP = 0; iP < prevSlice.nPeaks; iP++) {
						for (int iT = 0; iT < thisSlice.nPeaks; iT++) {
							if (matchPeak(prevSlice.peakInfo[iP], thisSlice.peakInfo[iT], conType)) {
								forwardLinks[iP] = iT;
								nForwardLinks[iP]++;
								backLinks[iT] = iP;
								nBackLinks[iT]++;
							}
						}
					}
					for (int iP = 0; iP < prevSlice.nPeaks; iP++) {
						if (nForwardLinks[iP] == 1 && nBackLinks[forwardLinks[iP]] == 1) {
							/*
							 * Simple situation where this peak links to one other peak
							 * so extend the whistle
							 * 
							 */
							lastSliceRegions[iP].extendRegion(thisSlice, forwardLinks[iP]);
							thisSliceRegions[forwardLinks[iP]] = lastSliceRegions[iP];
						}
						else {
							/* 
							 * end the whistle from this peak
							 */
							closeRegion(lastSliceRegions[iP], nForwardLinks[iP]);
						}
					}
					/*
					 * Now loop over peaks in the current slice. 
					 * If they have exactly one back link, then they will 
					 * have ALREADY been added to a whistle in the forward linking
					 * stage above. If however they have <> 1 backlink of the 
					 * set backlink has > 1 forward link, it will be necessary to 
					 * start a new whistle. 
					 */
					for (int iT = 0; iT < thisSlice.nPeaks; iT++) {
						if (nBackLinks[iT] == 1 && nForwardLinks[backLinks[iT]] == 1) {
							// no need to do anything
						}
						else {
							// start a new whistle. 
							thisSliceRegions[iT] = new ConnectedRegion(motherRegion, thisSlice, 
									iT, nBackLinks[iT]);
						}
					}
				}
				
			}
			prevSlice = thisSlice;
			for (int i = 0; i < maxPeaks; i++) {
				lastSliceRegions[i] = thisSliceRegions[i];
			}
		}
		/*
		 * Finally, close all regions which have a peak in the last slice	
		 */
		for (int i = 0; i < thisSlice.nPeaks; i++) {
			closeRegion(lastSliceRegions[i], 0);
		}
			
	}
	
	/**
	 * Finish off a region if no more bits are to be added to it. 
	 * @param region region to close
	 * @param nJoinedEnd number of other bits coming out of this point 
	 * @return true if the region is big enough to be worth keeping. 
	 */
	protected boolean closeRegion(ConnectedRegion region, int nJoinedEnd) {
		if (discardSmallOnes) {
			if (region.getTotalPixels() < whistleMoanControl.whistleToneParameters.minPixels ||
					region.getNumSlices() < whistleMoanControl.whistleToneParameters.minLength) {
				return false;
			}
		}
		region.setNJoinedEnd(nJoinedEnd);
		fragments.add(region);
		nFragments = fragments.size();
		return true;
	}
	
	/**
	 * Match to peaks - return true if they merge into one another. 
	 * @param peak1 first peak
	 * @param peak2 second peak
	 * @param connectType connect 4 or connect 8
	 * @return true if they touch according to 4 / 8 rule
	 */
	protected boolean matchPeak(int[] peak1, int[] peak2, int connectType) {
		if (connectType == 4) {
			if (peak1[0] > peak2[2] || peak2[0] > peak1[2]) {
				return false;
			}
		}
		else {
			if (peak1[0] > peak2[2]+1 || peak2[0] > peak1[2]+1) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Doesn't really zero the array but sets all elements to -1 so that
	 * we know they don't contain a valid index to any other peak. 
	 * @param array
	 */
	private void clearIntArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = -1;
		}
	}
	
	/**
	 * Set all array elements to zero 
	 * @param array
	 */
	private void zeroIintArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
	}
	
	/**
	 * Clean up fragments - mostly sorting out links between peaks. 
	 */
	protected void cleanFragments() {
		for (int i = 0; i < fragments.size(); i++) {
			fragments.get(i).cleanFragmentedFragment();
		}
	}
	
	
	@Override
	public ConnectedRegion getFragment(int fragment) {
		if (nFragments == 1) {
			return motherRegion;
		}
		return fragments.get(fragment);
	}

	@Override
	public int getNumFragments() {
		return nFragments;
	}

	public boolean isDiscardSmallOnes() {
		return discardSmallOnes;
	}

	/**
	 * Set by teh RejoinFragments superclass so that all fragments are kept
	 * @param discardSmallOnes
	 */
	public void setDiscardSmallOnes(boolean discardSmallOnes) {
		this.discardSmallOnes = discardSmallOnes;
	}
	
	/**
	 * Dump information to screen. 
	 */
	protected void sayFragments() {
		System.out.println("**** Connected Region broken into " + fragments.size() + " sub regions");
		motherRegion.sayRegion();
		sayFragment(-1, motherRegion);
		for (int i = 0; i < fragments.size(); i++) {
			sayFragment(i, fragments.get(i));
		}
	}
	
	/**
	 * Dump information on a single fragment ot screen. 
	 * @param iFrag
	 * @param region
	 */
	protected void sayFragment(int iFrag, ConnectedRegion region) {
		String str = String.format("%d. bin %d to %d ", iFrag, 
				region.getFirstSliceData().sliceNumber, region.getLastSliceData().sliceNumber);
		
		System.out.println(str);
	}

}
