package d3.calibration;

import java.util.ArrayList;

public class CalibrationSet {

	private String id;
	
	private String name;
	
	private int[] built;
	
	public CalibrationSet(String id, String name, String builder) {
		super();
		this.id = id;
		this.name = name;
		this.builder = builder;
		calibrationInfo = new CalibrationInfo(name);
	}

	private String builder;
	
	CalibrationInfo calibrationInfo;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the built
	 */
	public int[] getBuilt() {
		return built;
	}

	/**
	 * @return the builder
	 */
	public String getBuilder() {
		return builder;
	}

	public CalibrationInfo findCalibrationInfo(String string) {
		string = string.replace(".", ":");
		return calibrationInfo.findCalibrationInfo(string);
	}

	
	
}
