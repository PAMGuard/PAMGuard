package clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter;

import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;


/**
 * Filter which checks whether possible track might be electrical noise.
 * 
 * @author Jamie Macaulay
 *
 */
public interface ElectricalNoiseFilter {
	
	
	/**
	 * Add electric noise penalty to the chi2 value. 
	 * @param chi2 - the chi2 value to add the penalty to.
	 * @param mhtChi2Vars - the current mhtCh2Vars.
	 * @param bitSet - the bit set representing the current track.
	 * @param bitCount - the bit count i.e. number of clicks in the train. 
	 * @param kcount - the total data unit count. 
	 * @param nPruneback - the prune back value i.e. what detections on end of track to ignore.  
	 * @return chi2 - the input chi2 value. 
	 */
	public double addChi2Penalties(double chi2, TrackBitSet<?> bitSet, int bitCount,
			int kcount, int nPruneback);

}
