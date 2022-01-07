package nidaqdev;

public class NIDeviceInfo {
	private int devNumber;
	private String name;
	private String type;
	private int serialNum;
	private boolean isSimulated;
	private boolean isSimultaneous;
	private int inputChannels;
	private int outputChannels;
	private double[] aiVoltageRanges;
	double maxSingleChannelRate;
	double maxMultiChannelRate;
	private double[] aoVoltageRanges;
	
	public NIDeviceInfo(int devNumber, String name, String type,
			int serialNum, boolean isSimulated, boolean isSimultaneous, 
			int inputChannels, int outputChannels, 
			double[] aiVoltageRanges, double[] aoRanges) {
		super();
		this.devNumber = devNumber;
		this.name = name;
		this.type = type;
		this.serialNum = serialNum;
		this.isSimulated = isSimulated;
		this.isSimultaneous = isSimultaneous;
		this.inputChannels = inputChannels;
		this.outputChannels = outputChannels;
		this.aiVoltageRanges = aiVoltageRanges;
		this.aoVoltageRanges = aoRanges;
	}
	
	public int getDevNumber() {
		return devNumber;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
//		return String.format("NI %s: \"Dev%d\"", name, devNumber);
		return String.format("%s (%s)", name, type);
	}
	
	public String toLongString() {
		return String.format("Num %d; Name %s; Type %s; sn %d; simDevice %s)", 
				devNumber, name, type, serialNum, new Boolean(isSimulated).toString());
	}
	
	public String gethoverInfo() {

		int serNum = getSerialNum();
		int nChan = getInputChannels();
		String txt;
		if (isSimulated()) {
			txt = String.format("Simulated device, %d channels", nChan);
		}
		else if (serNum == 0) {
			txt = "Device not present";
		}
		else {
			txt = String.format("Serial Number %d, %d channels", serNum, nChan);
		}
		
		return txt;
	}
	
	/**
	 * 
	 * @return the device serial number (0 for simulated or missing devices)
	 */
	public int getSerialNum() {
		return serialNum;
	}
	
	/**
	 * 
	 * @return true if the device is simulated
	 */
	public boolean isSimulated() {
		return isSimulated;
	}
	
	/**
	 * @return the isSimultaneous
	 */
	public boolean isSimultaneous() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Win")) {
			return isSimultaneous;
		}
		return true; // always return true for Linux. 
	}

	public boolean isExists() {
		return (isSimulated || serialNum >= 0);
	}

	/**
	 * @return the number of inputChannels
	 */
	public int getInputChannels() {
		return inputChannels;
	}
	
	public int getOutputChannels() {
		return outputChannels;
	}

	/**
	 * Get the number of voltage ranges for the device
	 * @return number of voltage ranges
	 */
	public int getNumAIVoltageRanges() {
		if (aiVoltageRanges == null) {
			return 0;
		}
		return aiVoltageRanges.length / 2;
	}

	/**
	 * Get a voltage range as a two element array
	 * @param iRange range index
	 * @return voltage range
	 */
	public double[] getAIVoltageRange(int iRange) {
		if (iRange < 0) {
			return null;
		}
		double start = getAIVoltageRangeStart(iRange);
		double end = getAIVoltageRangeEnd(iRange);
		double[] v = {start, end};
		return v;
	}

	/**
	 * Get the lower bound of a voltage range
	 * @param iRange range index
	 * @return range in volts
	 */
	public double getAIVoltageRangeStart(int iRange) {
		int ind = iRange * 2;
		if (aiVoltageRanges == null || aiVoltageRanges.length <= ind) {
			return 0;
		}
		return aiVoltageRanges[ind];
	}
	
		
	/**
	 * Get the upper bound of a voltage range
	 * @param iRange range index
	 * @return range in volts
	 */
	public double getAIVoltageRangeEnd(int iRange) {
		int ind = iRange * 2 + 1;
		if (aiVoltageRanges == null || aiVoltageRanges.length <= ind) {
			return 0;
		}
		return aiVoltageRanges[ind];
	}


	/**
	 * Get the number of voltage ranges for the device
	 * @return number of voltage ranges
	 */
	public int getNumAOVoltageRanges() {
		if (aoVoltageRanges == null) {
			return 0;
		}
		return aoVoltageRanges.length / 2;
	}

	/**
	 * Get a voltage range as a two element array
	 * @param iRange range index
	 * @return voltage range
	 */
	public double[] getAOVoltageRange(int iRange) {
		if (iRange < 0) {
			return null;
		}
		double start = getAOVoltageRangeStart(iRange);
		double end = getAOVoltageRangeEnd(iRange);
		double[] v = {start, end};
		return v;
	}

	/**
	 * Get the lower bound of a voltage range
	 * @param iRange range index
	 * @return range in volts
	 */
	public double getAOVoltageRangeStart(int iRange) {
		int ind = iRange * 2;
		if (aoVoltageRanges == null || aoVoltageRanges.length <= ind) {
			return 0;
		}
		return aoVoltageRanges[ind];
	}
	
		
	/**
	 * Get the upper bound of a voltage range
	 * @param iRange range index
	 * @return range in volts
	 */
	public double getAOVoltageRangeEnd(int iRange) {
		int ind = iRange * 2 + 1;
		if (aoVoltageRanges == null || aoVoltageRanges.length <= ind) {
			return 0;
		}
		return aoVoltageRanges[ind];
	}

//	public double[] getAIRangeArray() {
//		int nR = getNumAIVoltageRanges();
//		if (nR == 0) {
//			return null;
//		}
//		double[] a = new double[nR];
//		for (int i = 0; i < nR; i++) {
//			a[i] = getAIVoltageRangeEnd(i);
//		}
//		return a;
//	}
	/**
	 * 
	 * @param iRange range index
	 * @return string describing the range (suitable for use in a combo box)
	 */
	public String getAIVoltageRangeString(int iRange) {
		double start = getAIVoltageRangeStart(iRange);
		double end = getAIVoltageRangeEnd(iRange);
		String unit = "V";
		if (end < 1) {
			unit = "mV";
			start *= 1000;
			end *= 1000;
		}
		if (Math.abs(start) == end) {
			return String.format("+/- %.1f %s", end, unit);
		}
		else {
			return String.format("%.1f - %.1f %s", start, end, unit);
		}
	}
	
	/**
	 * 
	 * Get the index within a list of a voltage range
	 * @param range two element double array
	 * @param defaultValue default value to return
	 * @return range index or defalt value.
	 */
	public int findAIRangeIndex(double[] range, int defaultValue) {
		int r = findAIRangeIndex(range);
		if (r < 0) {
			return defaultValue;
		}
		else {
			return r;
		}
	}
	/**
	 * Get the index within a list of a voltage range
	 * @param range two element double array
	 * @return range index or -1 if range not found
	 */
	public int findAIRangeIndex(double[] range) {
		if (range == null) return -1;
		for (int i = 0; i < getNumAIVoltageRanges(); i++) {
			if (getAIVoltageRangeStart(i) == range[0] &&
					getAIVoltageRangeEnd(i) == range[1]) {
				return i;
			}
		}
		return -1;
	}

	public double getMaxSingleChannelRate() {
		return maxSingleChannelRate;
	}

	public void setMaxSingleChannelRate(double maxSingleChannelRate) {
		this.maxSingleChannelRate = maxSingleChannelRate;
	}

	public double getMaxMultiChannelRate() {
		return maxMultiChannelRate;
	}

	public void setMaxMultiChannelRate(double maxMultiChannelRate) {
		this.maxMultiChannelRate = maxMultiChannelRate;
	}
	
	public boolean canSample(double sampleRate, int nChannels) {
		if (nChannels == 1) {
			return sampleRate <= maxSingleChannelRate; 
		}
		else if (isSimultaneous()) {
			return sampleRate <= maxMultiChannelRate;
		}
		else {
			return (sampleRate * nChannels) <= maxMultiChannelRate;
		}
	}
}
