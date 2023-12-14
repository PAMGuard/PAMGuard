package Acquisition.gpstiming;

import java.io.Serializable;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class PPSParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/*
	 * New stuff to GPS PPS timing. 
	 */
	public boolean useGpsPPS;
	
	public int gpsPPSChannel;
	
	public String gpsPPSNmeaSource;
	
	public double gpsPPSThreshold = 0.5;
	
	public int storageInterval = 60;

	@Override
	public PPSParameters clone() {
		try {
			return (PPSParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
