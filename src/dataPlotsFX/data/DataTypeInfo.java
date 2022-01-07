package dataPlotsFX.data;

import java.io.Serializable;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;

/**
 * Simple class to hold a data type and a data unit. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DataTypeInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DataTypeInfo(ParameterType dataType, ParameterUnits dataUnits) {
		this.dataType=dataType;
		this.dataUnits=dataUnits;
	}


	/**
	 * Name of the data, e.g. accelerometer x; bearing; slant angle, etc. 
	 */
	public ParameterType dataType;
	

	/**
	 * Name of the data, e.g. accelerometer x; bearing; slant angle, etc. 
	 */
	public ParameterUnits dataUnits;
	
	 
	public boolean equals(DataTypeInfo dataTypeInfo){
		if (dataTypeInfo.dataType==dataType && dataTypeInfo.dataUnits==dataUnits) return true;
		else return false;
	}
	
	/**
	 * Make a string consisting of both the data type and the data units. 
	 * @return String of type and units to use in hash table of stored settings. 
	 */
	public String getTypeString() {
		String str = "";
		if (dataType != null) {
			str = dataType.toString();
		}
		if (dataUnits != null) {
			str += "_" + dataUnits.toString();
		}
		return str;
	}

}
