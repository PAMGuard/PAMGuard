package gpl.whiten;

/**
 * Wraps multiple Infinitesorts to provide something akin to 
 * Matrix sorting. 
 * @author Doug Gillespie
 *
 */
public class InfiniteSortGroup {

	private int nTimeBins, nRows;
	
	private InfiniteSort[] rowSorts;
	
	public InfiniteSortGroup(int nRows, int nTimeBins) {
		super();
		this.nRows = nRows;
		this.nTimeBins = nTimeBins;
		rowSorts = new InfiniteSort[nRows];
		for (int i = 0; i < nRows; i++) {
			rowSorts[i] = new InfiniteSort(nTimeBins);
		}
	}
	
	/**
	 * Add a row of data to an array of individual InfiniteSorts. 
	 * @param newData array of new data. Must be equal to the number of rows. 
	 * @return array of removed data. 
	 */
	public double[] addData(double[] newData) {
		double[] removed = new double[newData.length];
		for (int i = 0; i < newData.length; i++) {
			removed[i] = rowSorts[i].addData(newData[i]); 
		}
		return removed;
	}
	
	/**
	 * Get an array of the central means of all sorters in the group. 
	 * @return array of central means. 
	 */
	public double[] getCentralMean() {
		double[] centralMean = new double[nRows];
		for (int i = 0; i < nRows; i++) {
			centralMean[i] = rowSorts[i].getCentralMean();
		}
		return centralMean;
	}
	
	/**
	 * Tell the sorters to automatically fill themselves with repeated
	 * data at startup when autoInitialise input datas have arrived. This 
	 * can speed up algorithm settling at detector startup. 
	 * @param autoInitialise
	 */
	public void setAutoInitialise(int autoInitialise) {
		for (int i = 0; i < rowSorts.length; i++) {
			rowSorts[i].setAutoInitialise(autoInitialise);
		}
	}
	
	
}
