package whistleClassifier.training;

import Jama.Matrix;

/**
 * Container for batch processing results
 * @author Doug Gillespie
 *
 */
public class BatchResultSet implements Comparable<BatchResultSet>{

	int fragmentLength, sectionLength;
	double minProbability;
	Matrix meanConfusion;
	Matrix stdConfusion;
	/**
	 * @param fragmentLength
	 * @param sectionLength
	 * @param minProbability
	 * @param meanConfusion
	 * @param stdConfusion
	 */
	public BatchResultSet(int fragmentLength, int sectionLength,
			double minProbability, Matrix meanConfusion, Matrix stdConfusion) {
		super();
		this.fragmentLength = fragmentLength;
		this.sectionLength = sectionLength;
		this.minProbability = minProbability;
		this.meanConfusion = meanConfusion;
		this.stdConfusion = stdConfusion;
	}
	
	@Override
	public int compareTo(BatchResultSet o) {
		double ths = getMeanCorrect();
		double that = o.getMeanCorrect();
		if (ths > that) {
			return 1;
		}
		else if (ths < that) {
			return -1;
		}
		return 0;
	}
	
	public double getMeanCorrect() {
		if (meanConfusion == null) {
			return -1.0;
		}
		int nRow = meanConfusion.getRowDimension();
		double tot = 0.;
		for (int i = 0; i < nRow; i++) {
			tot += meanConfusion.get(i, i);
		}
		tot /= nRow;
		return tot;
	}

	@Override
	public String toString() {
		return "BatchResultSet [fragmentLength=" + fragmentLength
				+ ", sectionLength=" + sectionLength + ", minProbability="
				+ minProbability + 
				", mean_correct_rate=" + getMeanCorrect() + "]"; 
		
	}


}
