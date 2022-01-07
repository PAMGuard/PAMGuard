package dataPlotsFX.data;

import java.io.Serializable;

/**
 * TDDataInfo objects can't be serialised since they contain 
 * references back into datablocks and everything else in PAMguard, 
 * so have to hold a summary of what's in each one to save into psf files. 
 * @author Doug Gillespie
 *
 */
public class TDDataListInfoFX implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public Class<?> providerClass;
	
	public String providerName;
	
	public int currentDataLine;
	
	public boolean isShowing = true;
	
	public Serializable listSettings;

	/**
	 * 
	 * @param providerClass Class of the TDDataProvider
	 * @param providerName name of the TDDataProvider
	 * @param currentDataLine index of current data line in the TDDataInfo
	 * @param listSettings settings from the data info 
	 */
	public TDDataListInfoFX(Class<?> providerClass, String providerName, int currentDataLine,
			Serializable listSettings) {
		super();
		this.providerClass = providerClass;
		this.providerName = providerName;
		this.currentDataLine = currentDataLine;
		this.listSettings = listSettings;
	}
	
	
	
}
