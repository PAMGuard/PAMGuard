package clickTrainDetector.clickTrainAlgorithms.mht;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;

/**
 * Any variable used by the MHT algorithm
 * 
 * @author Jamie Macaulay
 *
 */
public interface MHTChi2<T> {

	/**
	 * Calculate the chi2 value for a series of PAM detections. The chi2 value
	 * indicates consistency of values within a detection train. e.g. it might
	 * calculate the consistency of inter-detection interval or amplitude values for
	 * a series of click trains.
	 * 
	 * @return the chi2 value for the list of detection
	 */
	public double getChi2();

	/**
	 * Get the chi2 value for n units back in the total time series.
	 * 
	 * @param pruneback - the units to go backwards- note this is the units included
	 *                  and not included in the track.
	 * @return the chi2 value for pruneback.
	 */
	public double getChi2(int pruneback);

	/**
	 * Calculate the number of number of coasts.
	 * 
	 * @return the number of coasts.
	 */
	public int getNCoasts();

	/**
	 * Update the function with a new current data unit and the current track.
	 * 
	 * @param detection0 - the current detection. This corresponds to the last
	 *                   active index (kcount) in the bitset
	 * @param trackBitSet     - the bit set.
	 * @param the        number of data units represented sequentially in the
	 *                   BitSet.
	 */
	public void update(T detection0, TrackBitSet<T> trackBitSet, int kcount);

	/**
	 * Clones the MHTChi2- it's vital that this copies the class completely as used
	 * to transfer the MHTChi2 algorithms to new branches.
	 * <p>
	 * Note: this must be a DEEP copy.
	 * 
	 * @return clone of the current instance.
	 */
	public MHTChi2<T> cloneMHTChi2();

	/**
	 * Calkled to indicate a reset and clear any variables/ reset iterators etc.
	 */
	public void clear();
	
	/**
	 * Resets to a new start data unit. i.e. the kernel now starts from a new data
	 * unit and so some settings in the chi^2 may need to change.
	 * 
	 * @param newRefIndex - the index of the new reference data unit with respect to
	 *                    the old list.
	 */
	public void clearKernelGarbage(int newRefIndex); 
	
	/**
	 * 
	 */
	public CTAlgorithmInfo getMHTChi2Info(); 
	

//	/**** Useful Functions ****/
//
//	/**
//	 * Get the channel iterator through the parent data block of a data unit.
//	 * 
//	 * @param detection0    - a detection in the data block.
//	 * @param channelBitMap - the channelBitMap to iterate through
//	 * @return the channel iterator for detection0;
//	 */
//	@SuppressWarnings("unchecked")
//	public static ListIterator<PamDataUnit> getIterator(PamDataUnit detection0, int channelBitMap) {
//		// find the position of detection0;
//		int startIndex = detection0.getParentDataBlock().getUnitIndex(detection0);
//		// System.out.println("CHANNEL: " +channelBitMap + " START INDEX: "
//		// +startIndex);
//
//		// Channel iterator for find the data units to test chi2 value for.
//		detection0.getParentDataBlock().clearChannelIterators(); // FIXME: WHY DOES THIS NEED CLEARED??
//		ListIterator<PamDataUnit> iterator;
//		if (PamUtils.getNumChannels(channelBitMap) > 1)
//			iterator = detection0.getParentDataBlock().getChannelIterator(channelBitMap, startIndex);
//		else
//			iterator = detection0.getParentDataBlock().getListIterator(0);
//
//		return iterator;
//	}

//	/**
//	 * Get all the possible data units for a section of track.
//	 * 
//	 * @param detection0 - the first detection 
//	 * @param channelBitMap - the channel map
//	 * @param kcount - the number detections inot the track. 
//	 * @return
//	 */
//	public static ArrayList<PamDataUnit> getTrackDataUnits(PamDataUnit detection0, int channelBitMap, int kcount) {
//
//		ListIterator<PamDataUnit> iterator = getIterator(detection0, channelBitMap);
//
//		// the arraylist to test.
//		ArrayList<PamDataUnit> dataUnits = new ArrayList<PamDataUnit>();
//
//		int count = 0;
//		// Get the relevent units to find chi2 of track.
//		@SuppressWarnings("unused")
//		PamDataUnit lastUnit = null;
//		PamDataUnit dataUnit;
//		while (iterator.hasNext()) {
//			dataUnit = iterator.next();
//			dataUnits.add(dataUnit);
//			// reached the last data unit. Can break out of loop here.
//			if (count == kcount) {
//				lastUnit = dataUnit;
//				break;
//			}
//		}
//
//		return dataUnits;
//	}

//	/**
//	 * Get the data units which are in a track. These are extracted from the current
//	 * data block.
//	 * 
//	 * @param detection0 - the first detection
//	 * @param bitSet     - the bit set with tracks
//	 * @param kCount     - the total number of detections since detection0. Note
//	 *                   that size() and length of BitSet to not give correct
//	 *                   answers for this parameter.
//	 * @return list of data units in the track.
//	 */
//	public static TrackDataUnits getTrackDataUnits(PamDataUnit detection0, BitSet bitSet, int channelBitMap,
//			int kcount) {
//
//		ListIterator<PamDataUnit> iterator = getIterator(detection0, channelBitMap);
//
//		// the arraylist to test.
//		ArrayList<PamDataUnit> dataUnits = new ArrayList<PamDataUnit>();
//
//		int count = 0;
//		// Get the relevent units to find chi2 of track.
//		PamDataUnit lastUnit = null;
//		PamDataUnit dataUnit;
//		while (iterator.hasNext()) {
//			dataUnit = iterator.next();
//			// only add the data unit if in the track
//			if (bitSet.get(count)) {
//				dataUnits.add(dataUnit);
//			}
//			count++;
//
//			// reached the last data unit. Can break out of loop here.
//			if (count == kcount) {
//				lastUnit = dataUnit;
//				break;
//			}
//		}
//
////		long time2=System.currentTimeMillis();
////		System.out.println("Chi2 time: "+ (time2-time1));
//
//		TrackDataUnits trackDataUnits = new TrackDataUnits(dataUnits, lastUnit);
//
////		System.out.println("StandardMHTChi2: data units: " + dataUnits.size() + 
////				" count iter: " + count+ " detection0: " + detection0);
//
//		return trackDataUnits;
//	}

	/**
	 * Print the settings for the algorithm
	 */
	public void printSettings();


}
