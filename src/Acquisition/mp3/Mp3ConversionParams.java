package Acquisition.mp3;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class Mp3ConversionParams implements Serializable, Cloneable, ManagedParameters {
	
	public String rawDataSource;
	public int channelMap;

	@Override
	public PamParameterSet getParameterSet() {
		// TODO Auto-generated method stub
		return null;
	}

}
