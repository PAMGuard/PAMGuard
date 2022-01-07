package whistlesAndMoans;

import java.util.ListIterator;

import PamUtils.PamUtils;
import PamguardMVC.debug.Debug;

/**
 * Join fragmented shapes back together again. 
 * Probably not very efficient code since it goes through the 
 * list in fragments and if it joins two fragments together, 
 * it then removes one from the list, so the list gets progressively smaller. 
 * @author Doug Gillespie
 *
 */
public class RejoiningFragmenter extends FragmentingFragmenter {
	
	static final int gradLength = 20;

	
	public RejoiningFragmenter(WhistleMoanControl whistleMoanControl) {
		super(whistleMoanControl);
		setDiscardSmallOnes(false);
	}

	@Override
	public int fragmentRegion(ConnectedRegion connectedRegion) {
		// run the fragmenter

		super.fragmentRegion(connectedRegion);
		// then try to rejoin the different regions. 
		
		if (nFragments >= 3) {
//			checkDuplicates();
//			checkDoubles();
			int oldFrags = nFragments;
			crossCrosses();
			checkDoubles();
			nFragments = fragments.size();
//			System.out.println("Number of fragments reduced from " + oldFrags + " to " + nFragments);
//			checkDuplicates();
		}
		cleanFragments();
		if (nFragments >= 2) {
			nFragments = removeShortFragments();
		}
		checkDoubles();
		// clear problem in fragmenter in that it's joining duplicates of the same whistle. 
		
		return nFragments;
	}
	
	private void checkDoubles() {
		ConnectedRegion r;
		ListIterator<SliceData> l;
		SliceData s;
		for (int i = 0; i < fragments.size(); i++) {
			r = fragments.get(i);
			l = r.getSliceData().listIterator();
			while (l.hasNext()) {
				s = l.next();
				if (s.nPeaks != 1) {
					Debug.out.println(s.nPeaks + " peaks");
				}
			}
			
		}
	}
	
	private void checkDuplicates() {
		ConnectedRegion badFrag;
		for (int i = 0; i < fragments.size(); i++) {
			if ((badFrag = fragments.get(i)).checkRepeatslices()) {
				Debug.out.println("Repeat slice in fragmentt");
			}
			for (int j = i+1; j < fragments.size(); j++) {
				if (fragments.get(i) == fragments.get(j)) {
					Debug.out.println("duplicate fragmetn in list");
				}
			}
		}
	}
	
	/**
	 * Find regions which are clearly crosses, then find all the bits to the left and 
	 * the bits to the right, then join them together. 
	 */
	private void crossCrosses() {
		int maxCrossLength = whistleMoanControl.whistleToneParameters.maxCrossLength;
		ConnectedRegion r, mergedRegion;
		int offset = 0;
		for (int i = 0; i < fragments.size(); i++) {
			r = fragments.get(i);
			offset = 0;
			if (r.isCross(maxCrossLength)) {
				if (jumpCross(i)) {
					i--;
				}
			}
			else{
				if (r.isMerge(maxCrossLength)) {
					if ((mergedRegion = merge(i)) != null) {
						/*
						 * if it merged, then we're interested in the one it merged onto. 
						 * since frag i will have just been thrown away. 
						 */
						r = mergedRegion;
						i = fragments.indexOf(r);
					}
//					r = fragments.get(i); 
				}
				if (r.isSplit(maxCrossLength)) {
					branch(i); // no need to drop index down if this one true. 
				}
			}
//			i+= offset;
		}
//		ListIterator<ConnectedRegion> l = fragments.listIterator();
	}
	private boolean jumpCross(int crossIndex) {
		ConnectedRegion crossRegion = fragments.get(crossIndex);
		int nInOut = crossRegion.getNJoinedEnd();
		int[] ins = new int[nInOut];
		int[] outs = new int[nInOut];
		int[] inFreq = new int[nInOut];
		int[] outFreq = new int[nInOut];
		int nIns = 0, nOuts = 0;
		int[] crossPeak, testPeak;
		SliceData crossSlice;
		crossSlice = crossRegion.getFirstSliceData();
		for (int i = 0; i < crossIndex; i++) {
			if (matchPeak(fragments.get(i).getLastSliceData(), crossSlice, whistleMoanControl.whistleToneParameters.getConnectType())) {
				inFreq[nIns] = fragments.get(i).getLastSliceData().peakInfo[0][1]; 
				ins[nIns++] = i;
			}
			if (nIns == nInOut) {
				break;
			}
		}
//		crossPeak = crossRegion.getLastSliceData().peakInfo[0];
		crossSlice = crossRegion.getLastSliceData();
		for (int i = crossIndex+1; i < fragments.size(); i++) {
//			testPeak = fragments.get(i).getFirstSliceData().peakInfo[0];
			if (matchPeak(crossSlice, fragments.get(i).getFirstSliceData(), whistleMoanControl.whistleToneParameters.getConnectType())) {
				outFreq[nOuts] = fragments.get(i).getFirstSliceData().peakInfo[0][1];
				outs[nOuts++] = i;
			}
			if (nOuts == nInOut) {
				break;
			}
		}
		/*
		 * Now link across the whistles, in crossing order, and the delete the out whistles from 
		 * the array list. 
		 */
		if (nOuts != nIns || nOuts != nInOut) {
//			System.out.println(String.format("***** Error in Whistle Rejoiner: Found cross with %d in and %d out (expected %d)", nIns, nOuts, nInOut));
			return false;
		}
		// bugger ! need to sort since frags not in frequency order
		int[] sortedIns = PamUtils.getSortedInds(inFreq);
		int[] sortedOuts = PamUtils.getSortedInds(outFreq);
		ConnectedRegion r1, r2;
		int ind2 = nInOut;
		for (int i = 0; i < nInOut; i++) {
			ind2--;
			r1 = fragments.get(ins[sortedIns[i]]);
			r2 = fragments.get(outs[sortedOuts[ind2]]);
//			System.out.println(String.format("Merge Mother at %d  frag %d through %d to %d " +
//					"(Times %d to %d, %d to %d and %d to %d)", 
//					motherRegion.getFirstSlice(), ins[sortedIns[i]],
//					crossIndex, outs[sortedOuts[ind2]], r1.getFirstSlice(), 
//					r1.getLastSliceData().sliceNumber, crossRegion.getFirstSlice(), 
//					crossRegion.getLastSliceData().sliceNumber, r2.getFirstSlice(), 
//					r2.getLastSliceData().sliceNumber));
			mergeWhistles(r1, r2);
		}
		for (int i = nInOut-1; i >= 0; i--) {
			fragments.remove(outs[i]);
		}
		fragments.remove(crossIndex);
		
		return true;
		
	}
	
	private ConnectedRegion merge(int mergeIndex) {
		ConnectedRegion mergeRegion = fragments.get(mergeIndex);
		/*
		 * Find all the regions which but into this one.  
		 */
		int nInOut = mergeRegion.getNJoinedStart();
		int[] ins = new int[nInOut];
		int[] inFreq = new int[nInOut];
		int nIns = 0;
//		int[] mergePeak, testPeak;
		SliceData mergeSlice;
		mergeSlice = mergeRegion.getFirstSliceData();
		for (int i = 0; i < mergeIndex; i++) {
			if (matchPeak(fragments.get(i).getLastSliceData(), mergeSlice, whistleMoanControl.whistleToneParameters.getConnectType())) {
				inFreq[nIns] = fragments.get(i).getLastSliceData().peakInfo[0][1]; 
				ins[nIns++] = i;
			}
			if (nIns == nInOut) {
				break;
			}
		}
		if (nIns == 0) {
			return null;
		}
		double mergeGradient = mergeRegion.getStartGradient(gradLength);
		/*
		 * Now look to see which of these has the most similar gradient to the start of the 
		 * merging region. 
		 */
		ConnectedRegion testR = fragments.get(ins[0]);
		double bestGrad = Math.abs(mergeGradient - testR.getEndGradient(gradLength)) +
				shortPenalty(testR.getNumSlices());
		int bestInd = 0;
		double newGrad;
		for (int i = 1; i < nIns; i++) {
			testR = fragments.get(ins[i]);
			if ((newGrad = Math.abs(mergeGradient - testR.getEndGradient(gradLength))+shortPenalty(testR.getNumSlices())) < bestGrad) {
				bestGrad = newGrad;
				bestInd = i;
			}
		}
		mergeWhistles((testR = fragments.get(ins[bestInd])), mergeRegion);
		fragments.remove(mergeIndex);
		return testR;
	}
	
	/**
	 *  calculate a penalty to penalise very short fragments by when 
	 *  deciding which are the best match during a merge of a split. 
	 * @param fragLength fragment length
	 * @return penalty (to add to gradient difference)
	 */
	
	private double shortPenalty(int fragLength) {
		if (fragLength > 10) {
			return 0;
		}
		return (10-fragLength)/5;
	}
	private boolean branch(int branchIndex) {
		ConnectedRegion branchRegion = fragments.get(branchIndex);
		/*
		 * Find all the regions which but into this one.  
		 */
		int nInOut = branchRegion.getNJoinedEnd();
		int[] outs = new int[nInOut];
		int[] outFreq = new int[nInOut];
		int nOuts = 0;
		int[] branchPeak, testPeak;
		SliceData branchSlice;
		branchSlice = branchRegion.getLastSliceData();
		for (int i = branchIndex+1; i < fragments.size(); i++) {
			if (matchPeak(branchSlice, fragments.get(i).getFirstSliceData(), whistleMoanControl.whistleToneParameters.getConnectType())) {
				outFreq[nOuts] = fragments.get(i).getFirstSliceData().peakInfo[0][1]; 
				outs[nOuts++] = i;
			}
			if (nOuts == nInOut) {
				break;
			}
		}
		if (nOuts == 0) {
			return false;
		}
		double branchGradient = branchRegion.getEndGradient(gradLength);
		/*
		 * Now look to see which of these has the most similar gradient to the start of the 
		 * merging region. 
		 */
		ConnectedRegion testR = fragments.get(outs[0]);
		double bestGrad = Math.abs(branchGradient - testR.getStartGradient(gradLength))+shortPenalty(testR.getNumSlices());
		int bestInd = 0;
		double newGrad;
		for (int i = 1; i < nOuts; i++) {
			testR = fragments.get(outs[i]);
			if ((newGrad = Math.abs(branchGradient - testR.getStartGradient(gradLength))+shortPenalty(testR.getNumSlices())) < bestGrad) {
				bestGrad = newGrad;
				bestInd = i;
			}
		}
		mergeWhistles(branchRegion, (testR = fragments.get(outs[bestInd])));
		fragments.remove(testR);
		return true;
	}
	
	
	private void mergeWhistles(ConnectedRegion r1, ConnectedRegion r2) {
		r1.mergeFragmentedRegion(r2);
	}

	private boolean matchPeak(SliceData sliceData1, SliceData sliceData2, int connectType) {
		if (sliceData2.sliceNumber - sliceData1.sliceNumber != 1) {
			return false;
		}
		return matchPeak(sliceData2.peakInfo[0], sliceData1.peakInfo[0], connectType);
	}
	
	/**
	 * this will have called the super class fragmenter in a way which will have
	 * selected even the shortest fragments. Now need to get rid of those !
	 * @return number of remaining fragments.
	 */
	private int removeShortFragments() {
		/*
		 * Use an iterator so that it correctly handles removals. 
		 */
//		ListIterator<ConnectedRegion> l = fragments.listIterator();
		ConnectedRegion r;
//		while (l.hasNext()) {
//			r = l.next();
//			if (r.getTotalPixels() < whistleToneControl.whistleToneParameters.minPixels ||
//					r.getNumSlices() < whistleToneControl.whistleToneParameters.minLength) {
//				l.remove();
//			}
//		}
		for (int i = fragments.size()-1; i >= 0; i--) {
			r = fragments.get(i);
			if (r.getTotalPixels() < whistleMoanControl.whistleToneParameters.minPixels ||
					r.getNumSlices() < whistleMoanControl.whistleToneParameters.minLength) {
				fragments.remove(i);
			}
		}
		return fragments.size();
	}
	
	
	@Override
	public ConnectedRegion getFragment(int fragment) {
		return fragments.get(fragment);
	}
}
