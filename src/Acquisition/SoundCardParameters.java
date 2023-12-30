package Acquisition;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import simulatedAcquisition.SimProcess;

/**
 * Used by SoundCardSystem
 * @author Doug Gillespie
 * @see Acquisition.SoundCardSystem
 *
 */
public class SoundCardParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1;
	
	public int deviceNumber;
	
	public String systemType;
		
	
	/**
	 * @param systemType
	 */
	public SoundCardParameters(String systemType) {
		super();
		this.systemType = systemType;
	}

	@Override
	public SoundCardParameters clone() {
		try{
			return (SoundCardParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected this system type, just return null
		if (!DaqSystemXMLManager.isSelected(systemType)) {
			return null;
		}

		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
