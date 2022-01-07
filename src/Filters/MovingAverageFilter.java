package Filters;

/**
 * Moving average filter. Stores a local array of values
 * input to the filter and subtracts off the oldest value
 * as it adds the newest one. This makes it as fast as a 
 * decaying average filter.  
 * 
 * @author Doug Gillespie
 *
 */
public class MovingAverageFilter extends AbstractFilter {

	private int filterLen;
	
	private double[] filterData;
	
	private int currIndex;
	
	private double filterTotal;

	public MovingAverageFilter(int filterLen) {
		super();
		this.filterLen = filterLen;
	}

	@Override
	public void prepareFilter() {
		
		filterData = new double[filterLen];
		currIndex = 0;
		filterTotal = 0;
		
	}

	@Override
	public double runFilter(double data) {

		filterTotal -= filterData[currIndex];
		filterTotal += data;
		filterData[currIndex] = data;
		if (++currIndex == filterLen) {
			currIndex = 0;
		}
		return filterTotal / filterLen;
	}

	@Override
	public int getFilterDelay() {
		return filterLen / 2;
	}


	
}
