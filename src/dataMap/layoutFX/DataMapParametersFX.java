package dataMap.layoutFX;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataMap.DataMapParameters;
import dataMap.OfflineDataMap;

public class DataMapParametersFX implements Cloneable, Serializable, ManagedParameters {

	protected static final long serialVersionUID = 1L;
	
	/**
	 * The log scale. 
	 */
	public int vScaleChoice = OfflineDataMap.SCALE_PERHOUR;
	
	/**
	 * Use a log scale. 
	 */
	public boolean vLogScale = true;
	
	/**
	 * The selected dfatagranm for colour changes. 
	 */
	public int selectedDataGram = 0; 
	

	@Override
	protected DataMapParametersFX clone() {
		try {
			return (DataMapParametersFX) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
