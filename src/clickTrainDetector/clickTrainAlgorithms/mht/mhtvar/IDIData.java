package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

/**
 * Holds IDI data. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class IDIData {


	public IDIData(Double medianICI, double[] idiSeries2, double[] timeSeries2, double timeDiff) {
		this.medianIDI=medianICI;
		this.idiSeries=idiSeries2;
		this.timeSeries=timeSeries2;
		this.timeDiff = timeDiff; 
	}



	/**
	 * The difference between the last recieved data unit and the last tdata unit in the track in seconds. 
	 */
	public double timeDiff;

	/**
	 * The median IDI in seconds
	 */
	public Double medianIDI; 

	/**
	 * The IDI series in seconds
	 */
	public double[] idiSeries;

	/**
	 * The time series in seconds counting from time[0] = 0. 
	 */
	public double[] timeSeries;


}
