package qa.analyser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Summary of sequence data for a test report
 * @author dg50
 *
 */
public class SequenceSummary implements Cloneable {

	private double[] seqRanges;
	
	private int[] seqSounds;
	
	private int[] seqHits;

	/**
	 * @param seqRanges ranges to each sequence
	 * @param seqSounds number of generated sounds in each sequence
	 * @param seqHits number of detected sounds in each sequence
	 */
	public SequenceSummary(double[] seqRanges, int[] seqSounds, int[] seqHits) {
		super();
		this.seqRanges = seqRanges;
		this.seqSounds = seqSounds;
		this.seqHits = seqHits;
	}
	
	/**
	 * Get ranges and hit/miss data for single sounds so 
	 * they can be used in logistic regression
	 * @return array of ranges and 1/0 hit miss data. 
	 */
	public double[][] getSingleSoundData() {
		int n = 0;
		for (int i = 0; i < seqSounds.length; i++) {
			n += seqSounds[i];
		}
		double[] ranges = new double[n];
		double[] hit = new double[n];
		int[] misses = getMisses();
		int is = 0;
		for (int i = 0; i < seqSounds.length; i++) {
			for (int j = 0; j < seqHits[i]; j++, is++) {
				ranges[is] = seqRanges[i];
				hit[is] = 1.;
			}
			for (int j = 0; j < misses[i]; j++, is++) {
				ranges[is] = seqRanges[i];
				hit[is] = 0.;
			}
		}
		double[][] dat = {ranges, hit};
		return dat;
	}
	
	/**
	 * Get sequence hit data in same format as single sound hit data, i.e. 
	 * two arrays of doubles. 
	 * @param minHit minimum number of sounds detected to count as hit. 
	 * @return array of ranges and 1/0 hit data. 
	 */
	public double[][] getSequenceHitData(int minHit) {
		int[] hits = getCountGtThan(minHit);
		// convert that to double. 
		double[] d = new double[hits.length];
		for (int i = 0; i < d.length; i++) {
			d[i] = hits[i]; // hits are already 1 or 0
		}
		double[][] hitDat = {seqRanges, d};
		return hitDat;
	}
	
	public double getMaxRange() {
		double m = 0;
		for (int i = 0; i < seqRanges.length; i++) {
			m = Math.max(m, seqRanges[i]);
		}
		return m;
	}

	/**
	 * @return the seqRanges
	 */
	public double[] getSeqRanges() {
		return seqRanges;
	}

	/**
	 * @return the seqSounds
	 */
	public int[] getSeqSounds() {
		return seqSounds;
	}

	/**
	 * @return the seqHits
	 */
	public int[] getSeqHits() {
		return seqHits;
	}
	
	/**
	 * Return array of 0's and 1s, 1's if hit count >= parameter
	 * @param than greater than or == this to count as a detected sequence
	 * @return binary array for sequence its. 
	 */
	public int[] getCountGtThan(int than) {
		int[] hits = new int[seqHits.length];
		for (int i = 0; i < hits.length; i++) {
			hits[i] = seqHits[i] >= than ? 1 : 0;
		}
		return hits;
	}
	
	/**
	 * Get the number of missed sounds for each range. 
	 * @return array count of missed sounds. 
	 */
	public int[] getMisses() {
		int[] misses = new int[seqSounds.length];
		for (int i = 0; i < misses.length; i++) {
			misses[i] = seqSounds[i] - seqHits[i];
		}
		return misses;
	}
	
	/**
	 * 
	 * @return detection rate - detected sounds / generated sounds
	 */
	public double[] getDetectionRate() {
		double[] rate = new double[seqSounds.length];
		for (int i = 0; i < seqSounds.length; i++) {
			rate[i] = (double) seqHits[i] / (double) seqSounds[i];
		}
		return rate;
	}

	@Override
	public SequenceSummary clone() {
		try {
			return (SequenceSummary) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Add the content of another summary into this summary and 
	 * return a new object containing the data from both.
	 * @param otherSummary Other summary
	 * @return combined summary. 
	 */
	public SequenceSummary add(SequenceSummary otherSummary) {
		SequenceSummary newSum = this.clone();
		newSum.seqRanges = ArrayUtils.addAll(newSum.seqRanges, otherSummary.seqRanges);
		newSum.seqSounds = ArrayUtils.addAll(newSum.seqSounds, otherSummary.seqSounds);
		newSum.seqHits = ArrayUtils.addAll(newSum.seqHits, otherSummary.seqHits);
		return newSum;
	}

}
