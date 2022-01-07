package mcc.mccacquisition;

import java.io.Serializable;

import Acquisition.DaqSystemXMLManager;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import analoginput.AnalogRangeData;
import simulatedAcquisition.SimProcess;

public class MCCDaqParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public int boardIndex; // index, not number
	
	private AnalogRangeData rangeData;
	
	public boolean differential;
	
//	private int[] channelList = {0, 1};

	@Override
	protected MCCDaqParams clone() {
		try {
			return (MCCDaqParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the rangeData
	 */
	public AnalogRangeData getRangeData() {
		return rangeData;
	}

	/**
	 * @param rangeData the rangeData to set
	 */
	public void setRangeData(AnalogRangeData rangeData) {
		this.rangeData = rangeData;
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected an MCC board, just return null
		if (!DaqSystemXMLManager.isSelected(MCCDaqSystem.systemType)) {
			return null;
		}

		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

	
}
