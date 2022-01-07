package cpod.dataSelector;

import java.io.Serializable;

public class StandardCPODFilterParams implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The peak frequency value
	 */
	public final static int PEAK_FREQ = 0; 
	
	/**
	 * The peak frequency value
	 */
	public final static int NCYCLES = 1; 
	
	/**
	 * The peak frequency value
	 */
	public final static int AMPLITUDE = 2; 
	
	/**
	 * The bandwidth value
	 */
	public final static int BW = 3; 
	
	/**
	 * The end frequency value
	 */
	public final static int END_F = 4; 
	
	public StandardCPODFilterParams() {
		
	}
	
	public StandardCPODFilterParams(int type, double min, double max) {
		this.dataType=type; 
		this.min=min; 
		this.max=max; 
	}
	
	/**
	 * The data to extract from the CPOD
	 */
	public int dataType = 0; 
	
	/**
	 * The minimum value for the CPOD data
	 */
	public double min = 0; 
	
	/**
	 * The maximum value for the CPOD data 
	 */
	public double max = 255; 
}
