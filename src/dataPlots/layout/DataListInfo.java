package dataPlots.layout;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * TDDataInfo objects can't be serialised since they contain 
 * references back into datablocks and everything else in PAMguard, 
 * so have to hold a summary of what's in each one to save into psf files. 
 * @author Doug Gillespie
 *
 */
public class DataListInfo implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public Class providerClass;
	
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
	public DataListInfo(Class providerClass, String providerName, int currentDataLine,
			Serializable listSettings) {
		super();
		this.providerClass = providerClass;
		this.providerName = providerName;
		this.currentDataLine = currentDataLine;
		this.listSettings = listSettings;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

	
}
