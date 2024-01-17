package analoginput;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class AnalogRangeData implements Serializable, Cloneable, Comparable<AnalogRangeData>, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public enum AnalogType {VOLTS, AMPS}; 
	
	private double[] range;
	
	private AnalogType analogType;
	
	public AnalogRangeData(double[] range, AnalogType analogType) {
		this.range = range;
		this.analogType = analogType;
	}
	
	public static AnalogRangeData makeBipolarRange(double range, AnalogType analogType) {
		double[] bpRange = {-range, range};
		return new AnalogRangeData(bpRange, analogType);
	}
	
	public static AnalogRangeData makeSingleEndRange(double range, AnalogType analogType) {
		double[] bpRange = {0, range};
		return new AnalogRangeData(bpRange, analogType);
	}

	/**
	 * @return the range
	 */
	public double[] getRange() {
		return range;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(double[] range) {
		this.range = range;
	}

	/**
	 * @return the analogType
	 */
	public AnalogType getAnalogType() {
		return analogType;
	}

	/**
	 * @param analogType the analogType to set
	 */
	public void setAnalogType(AnalogType analogType) {
		this.analogType = analogType;
	}

	@Override
	public String toString() {
		if (range == null) {
			return "Unknown";
		}
		String str;
		double m = 0;
		for (int i = 0; i < range.length; i++) {
			m = Math.max(m,  Math.abs(range[i]));
		}
		double scale = 1.;
		String unit = "";
		if (m < 0.1) {
			scale = 1000.;
			unit = "m";
		}
		if (range[0] + range[1] == 0.) {
			// bipolar !
			str = String.format("\u00B1%3.2f %s", range[1]*scale, unit);
		}
		else {
			str = String.format("%3.2f to %3.2f %s", range[0]*scale, range[1]*scale, unit);
		}
		str += analogType == AnalogType.VOLTS ? "V" : "A";
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnalogRangeData == false) {
			return false;
		}
		AnalogRangeData oth = (AnalogRangeData) obj;
		if (oth.analogType != this.analogType) {
			return false;
		}
		return (oth.range[0]==this.range[0] && oth.range[1]==this.range[1]);
	}

	@Override
	protected AnalogRangeData clone() {
		try {
			return (AnalogRangeData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int compareTo(AnalogRangeData o) {
		if (this.range == null || o.range == null) {
			return 0;
		}
		double thisR = range[1]-range[0];
		double othR = o.range[1]-o.range[0];
		return Double.compare(thisR, othR);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	
}
