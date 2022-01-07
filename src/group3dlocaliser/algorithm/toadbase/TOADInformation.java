package group3dlocaliser.algorithm.toadbase;

/**
 * Class holding information about TOAD delays, channels, etc. 
 * @author dg50
 *
 */
public class TOADInformation {

	private double[][] toadSeconds;

	private double[][] toadErrorsSeconds;
	
	private int[] channelList;
	
	private int[] hydrophoneList;
	
	private int channelMap;
	
	private int hydrophoneMap;

	private double[][] toadScores;

	/**
	 * 
	 * @param channelMap
	 * @param channelList
	 * @param hydrophoneMap
	 * @param hydrophoneList
	 * @param toadSeconds
	 * @param toadErrorsSeconds
	 * @param toadScores
	 */
	public TOADInformation(int channelMap, int[] channelList, int hydrophoneMap, int[] hydrophoneList, 
			double[][] toadSeconds,  double[][] toadErrorsSeconds, double[][] toadScores) {
		super();
		this.channelMap = channelMap;
		this.channelList = channelList;
		this.hydrophoneMap = hydrophoneMap;
		this.hydrophoneList = hydrophoneList;
		this.toadSeconds = toadSeconds;
		this.toadErrorsSeconds = toadErrorsSeconds;
		this.toadScores = toadScores;
	}


	/**
	 * @return the toadSeconds
	 */
	public double[][] getToadSeconds() {
		return toadSeconds;
	}


	/**
	 * @param toadSeconds the toadSeconds to set
	 */
	public void setToadSeconds(double[][] toadSeconds) {
		this.toadSeconds = toadSeconds;
	}


	/**
	 * @return the toadErrorsSeconds
	 */
	public double[][] getToadErrorsSeconds() {
		return toadErrorsSeconds;
	}


	/**
	 * @param toadErrorsSeconds the toadErrorsSeconds to set
	 */
	public void setToadErrorsSeconds(double[][] toadErrorsSeconds) {
		this.toadErrorsSeconds = toadErrorsSeconds;
	}


	/**
	 * @return the channelList
	 */
	public int[] getChannelList() {
		return channelList;
	}


	/**
	 * @param channelList the channelList to set
	 */
	public void setChannelList(int[] channelList) {
		this.channelList = channelList;
	}


	/**
	 * @return the hydrophoneList
	 */
	public int[] getHydrophoneList() {
		return hydrophoneList;
	}


	/**
	 * @param hydrophoneList the hydrophoneList to set
	 */
	public void setHydrophoneList(int[] hydrophoneList) {
		this.hydrophoneList = hydrophoneList;
	}


	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}


	/**
	 * @param channelMap the channelMap to set
	 */
	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}


	/**
	 * @return the hydrophoneMap
	 */
	public int getHydrophoneMap() {
		return hydrophoneMap;
	}


	/**
	 * @param hydrophoneMap the hydrophoneMap to set
	 */
	public void setHydrophoneMap(int hydrophoneMap) {
		this.hydrophoneMap = hydrophoneMap;
	}


	/**
	 * @return the toadScores (correlation heights)
	 */
	public double[][] getToadScores() {
		return toadScores;
	}


	/**
	 * @param toadScores the toadScores to set
	 */
	public void setToadScores(double[][] toadScores) {
		this.toadScores = toadScores;
	}

}
