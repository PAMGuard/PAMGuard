package dataModelFX;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataMap.OfflineDataMap;

public class DataMapParametersFX implements Cloneable, Serializable, ManagedParameters {

	protected static final long serialVersionUID = 1L;
	
	public int vScaleChoice = OfflineDataMap.SCALE_PERHOUR;
	
	public boolean vLogScale = true;
	
	/*
	 * Scale factor for horizontal axis. 
	 */
	public int hScaleChoice = 4;
	

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