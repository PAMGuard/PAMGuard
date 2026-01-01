package dataGram;

/**
 * Class to pass around a few bits of information about a datagrams scale
 * @author Doug Gillespie
 *
 */
public class DatagramScaleInformation {

	public static final int PLOT_3D = 0;
	public static final int PLOT_2D = 1;
	
	private double maxValue;
	
	private double minValue;
	
	private boolean logScale;
	
	private String units;
	
	private int displayType = PLOT_3D;

	/**
	 * @param minValue
	 * @param maxValue
	 * @param units
	 * @param logScale
	 * @param displayType (PLOT_3D or PLOT_2D)
	 */
	public DatagramScaleInformation(double minValue, double maxValue, String units, 
			boolean logScale, int displayType) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.units = units;
		this.logScale = logScale;
		this.displayType = displayType;
	}

	/**
	 * @param minValue
	 * @param maxValue
	 */
	public DatagramScaleInformation(double minValue, double maxValue, String units) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.units = units;
		this.logScale = false;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the logScale
	 */
	public boolean isLogScale() {
		return logScale;
	}

	/**
	 * @param logScale the logScale to set
	 */
	public void setLogScale(boolean logScale) {
		this.logScale = logScale;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the displayType PLOT_3D or PLOT_2D
	 */
	public int getDisplayType() {
		return displayType;
	}

	/**
	 * @param displayType the displayType to set
	 */
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}
	
	
	
	
}
