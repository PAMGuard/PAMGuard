package analoginput;


public class ItemAllData {

	private Integer intValue;
	
	private Double scaledValue;
	
	private Double parameterValue;
	
	private int item;

	/**
	 * @param intValue
	 * @param scaledValue
	 * @param parameterValue
	 * @param calibrationData
	 * @param rangeData
	 */
	public ItemAllData(int item, Integer intValue, Double scaledValue, Double parameterValue) {
		super();
		this.item = item;
		this.intValue = intValue;
		this.scaledValue = scaledValue;
		this.parameterValue = parameterValue;
	}

	/**
	 * @return the intValue
	 */
	public Integer getIntValue() {
		return intValue;
	}

	/**
	 * @return the scaledValue
	 */
	public Double getScaledValue() {
		return scaledValue;
	}

	/**
	 * @return the parameterValue
	 */
	public Double getParameterValue() {
		return parameterValue;
	}

	/**
	 * @return the item
	 */
	public int getItem() {
		return item;
	}

}
