package angleMeasurement;

import java.io.Serializable;

public class FluxgateWorldParameters extends AngleParameters implements Serializable, Cloneable {
	
	public static final long serialVersionUID = 0;
	
	String portName = "";
	
	public boolean invertAngles;
	
	public int readRateIndex = 2;
	
	public static final char[] readRateCodes = {'a','b','c','d','e','f','g','h','i', 'j'};
	public static final int[] readRateIntervals = {100, 300, 500, 1000, 5000, 10000, 15000, 20000, 30000, 60000};

	@Override
	protected FluxgateWorldParameters clone() {
		return (FluxgateWorldParameters) super.clone();
	}
	
	public String getRateCommand() {
		return String.format("$U%c",readRateCodes[readRateIndex]);
	}


}
