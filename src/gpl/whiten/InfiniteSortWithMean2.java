package gpl.whiten;

/**
 * Extension of InfiniteSort to also keep a running total of mean squared
 * values.
 * @author Doug Gillespie
 *
 */
public class InfiniteSortWithMean2 extends InfiniteSort {
	
	private double totalSq = 0;

	public InfiniteSortWithMean2(int nPoints) {
		super(nPoints);
	}

	/* (non-Javadoc)
	 * @see gpl.whiten.InfiniteSort#addData(double)
	 */
	@Override
	public double addData(double d) {
		double removed = super.addData(d);
		totalSq += d*d - removed*removed;
		return removed;
	}

	/**
	 * @return The RMS value of data in the data list. 
	 */
	public double getMeanSq() {
		return Math.sqrt(totalSq / getnPoints());
	}

	/**
	 * @return the total Squared data
	 */
	public double getTotalSq() {
		return totalSq;
	}
}
