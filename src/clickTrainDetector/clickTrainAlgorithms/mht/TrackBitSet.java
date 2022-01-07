package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.BitSet;

/**
 * Holds information on a track possibility 
 * @author Jamie Macaulay 
 *
 */
public class TrackBitSet<T> {

	/**
	 * Flag to indicate a track should be junked. 
	 */
	public static final int JUNK_TRACK = 1;

	public TrackBitSet(int size, MHTChi2<T> chi2Track) {
		trackBitSet= new BitSet(size);
		this.chi2Track=chi2Track;
	}

	//	public TrackBitSet(BitSet trackBitSet) {
	//		this.trackBitSet=trackBitSet; 
	//	}

	public TrackBitSet(BitSet trackBitSet, MHTChi2<T> chi2Track) {
		this.trackBitSet=trackBitSet; 
		this.chi2Track=chi2Track;
	}

	public TrackBitSet(BitSet bitSet) {
		this.trackBitSet=bitSet; 

	}

	/**
	 * Convenience function to get chi2 of track.
	 * @return chi2 of track
	 */
	public double getChi2() {
		if (chi2Track!=null) {
			return chi2Track.getChi2(); 
		}
		else return Double.NaN; 
	}
	/**
	 * A bit set representing the track were each true and false 
	 * represents whether a sequential detection is within the track or not
	 * 
	 */
	public BitSet trackBitSet; 

	/**
	 * The chi2 value of the track. Lower values indicate consistently changing
	 * variables e.g. bearings, amplitudes, inter-click interval. 
	 */
	public MHTChi2<T> chi2Track; 

	/***
	 * Flag which can indicate track status
	 */
	public int flag = 0; 


}