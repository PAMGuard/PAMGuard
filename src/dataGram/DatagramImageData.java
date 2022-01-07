package dataGram;

/**
 * DatagramManager needs to return more than just an image - it also needs
 * the start and end times of the data, which will have been extended out to
 * interval boundaries. 
 * @author Doug Gillespie
 *
 */
public class DatagramImageData {

	public long imageStartTime;
	public long imageEndTime;
	public double[][] imageData;
	/**
	 * @param imageStartTime
	 * @param imageEndTime
	 * @param imageData
	 */
	public DatagramImageData(long imageStartTime, long imageEndTime,
			double[][] imageData) {
		super();
		this.imageStartTime = imageStartTime;
		this.imageEndTime = imageEndTime;
		this.imageData = imageData;
	}
	
}
