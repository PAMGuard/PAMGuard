package nidaqdev;

import java.io.Serializable;
import java.lang.reflect.Field;

import Acquisition.DaqSystemXMLManager;
import Acquisition.SoundCardParameters;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.PamConstants;

public class NIDaqParams extends SoundCardParameters implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * @param systemType
	 */
	public NIDaqParams(String systemType) {
		super(systemType);
	}

	public static final long serialVersionUID = 1L;

	private int[] deviceList;
	
	private int[] hwChannelList;
	
	public int terminalConfiguration = NIConstants.DAQmx_Val_Diff; 
	
	private double[][] aiRange;
	
	public boolean enableMultiBoard;

	public int[] getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(int[] deviceList) {
		this.deviceList = deviceList;
	}

	public int[] getHwChannelList() {
		return hwChannelList;
	}

	public void setHwChannelList(int[] hwChannelList) {
		this.hwChannelList = hwChannelList;
	}

	@Override
	public NIDaqParams clone() {
		return (NIDaqParams) super.clone();
	}
	
	public double[] getAIRange(int iChannel) {
		if (aiRange == null) {
			createDefaultAIRanges();
		}
		return aiRange[iChannel];
	}
	
	public void setAIRange(int iChannel, double[] range) {
		if (aiRange == null) {
			createDefaultAIRanges();
		}
		aiRange[iChannel] = range;
	}
	
	private void createDefaultAIRanges() {
		aiRange = new double[PamConstants.MAX_CHANNELS][2];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			aiRange[i][0] = -1;
			aiRange[i][1] = 1;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected an NI DAQ card, just return null
		if (!DaqSystemXMLManager.isSelected(NIDAQProcess.sysType)) {
			return null;
		}

		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("aiRange");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return aiRange;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
}
