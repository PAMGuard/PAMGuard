package dataGram;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class DatagramSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public static long[] defaultDatagramSeconds={10000,30000,60000,120000,300000,600000,1800000,3600000}; //millis

	public int datagramSeconds = 600;
	
	public int displayMultiplier = 1;
	
	public boolean validDatagramSettings = false;

	@Override
	public DatagramSettings clone() {
		try {
			DatagramSettings ds =  (DatagramSettings) super.clone();
			if (ds.displayMultiplier == 0) {
				ds.displayMultiplier = 1;
			}
			return ds;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
