package d3.calibration;

import java.util.ArrayList;

/**
 * Information for a specific calibration type. 
 * Will hopefully be able to unpack the cal xml in a rational way
 * into these objects.
 * @author Doug Gillespie
 *
 */
public class CalibrationInfo {


	String name;
	
	double[][] poly;
	
	String method;
	
	String unit;
	
	String use;

	public Double tref;
	
	String type;
	
	String src;
	

	ArrayList<CalibrationInfo> subInfos = new ArrayList<>();

	public CalibrationInfo(String name) {
		this.name = name;
	}

	/**
	 * Apply this calibration to an aarry of data. <br>
	 * Attempts to replicate the functionality of Marks Johnson's 
	 * matlab function apply_cal(...)
	 * @param rawData unprocessed data from swv file.
	 * @param p
	 * @param t
	 * @return calibrated data
	 */
	public float[] apply_cal(float[] rawData) {
		return apply_cal(rawData, null, null);
	}
	/**
	 * Apply this calibration to an array of data. <br>
	 * Attempts to replicate the functionality of Marks Johnson's 
	 * matlab function apply_cal(...)
	 * @param rawData unprocessed data from swv file.
	 * @param p
	 * @param t
	 * @return calibrated data
	 */
	public float[] apply_cal(float[] rawData, Double p, Double t) {
		if (rawData == null) {
			return null;
		}
		float[] calData = rawData;

//		if (this.pos != null) {
//			
//		}
		
		if (use != null) {
			// TODO
			/*
			 * Need to be quite clever and find a class in the calibration package
			 * which has the same name as the use variable, can then call 
			 * a function in that class. 
			 */
		}
		
		if (poly != null) {
			// polynomial operation. 
			applyPolynomial(calData, poly);
		}
		
		/*
		 * PC may either be a value of this (not implemented) or
		 * a child struct. Deal with he child struct first ...
		 */
		CalibrationInfo pcChild = findChild("PC");
		if (pcChild != null) {
			if (pcChild.poly != null) {
				// Marks version ADDS this correction on !
				float[] cc = calData.clone();
				applyPolynomial(calData, pcChild.poly);
				for (int i = 0; i < calData.length; i++) {
					calData[i] += cc[i];
				}
			}
		}
			
		
		return calData;
	}
	
	private void applyPolynomial(float[] calData, double[][] poly) {
		applyPolynomial(calData, poly[0]);
	}

	private void applyPolynomial(float[] calData, double[] poly) {
		int nP = poly.length;
		double ipVal;
		for (int i = 0; i < calData.length; i++) {
			ipVal = calData[i];
			calData[i] = 0;
			for (int j = 0; j < nP; j++) {
				calData[i] += Math.pow(ipVal, nP-j-1) * poly[j];
			}
		}
	}

	/**
	 * Makes a new child of this calibration when the xml cal file
	 * is being scanned. 
	 * @param childName Name of xml node / calibration info.
	 * @return new calibrationinfo. 
	 */
	public CalibrationInfo getNewChild(String childName) {
		CalibrationInfo calinfo = new CalibrationInfo(childName);
		subInfos.add(calinfo);
		return calinfo;
	}


	/**
	 * Find a calibration info. Multi layered informs can be
	 * searated by a ':' character ('.' didn't work for some unknown reason. 
	 * @param string
	 * @return
	 */
	public CalibrationInfo findCalibrationInfo(String string) {
		if (string == null) {
			return null;
		}
		if (string.indexOf(':') < 0) {
			return findChild(string);
		}
		String[] bits = string.split(":");
		CalibrationInfo firstBit = findChild(bits[0]);
		if (firstBit == null) {
			return null;
		}
		else if (bits.length == 1) {
			return firstBit;
		}
		else {
			return firstBit.findCalibrationInfo(string.substring(string.indexOf(":")+1));
		}
	}

	/**
	 * find a childof the current calibration info with the 
	 * given name. 
	 * @param string
	 * @return
	 */
	private CalibrationInfo findChild(String string) {
		string = string.toUpperCase();
		for (CalibrationInfo anInfo:subInfos) {
			if (string.equals(anInfo.name)) {
				return anInfo;
			}
		}
		return null;
	}
}
