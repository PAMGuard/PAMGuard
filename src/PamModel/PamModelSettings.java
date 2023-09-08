package PamModel;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class PamModelSettings implements Cloneable, Serializable, ManagedParameters {

	static public final long serialVersionUID = 0;

	/**
	 * Enables multi-threading in the data handling between modules. 
	 */
	public boolean multiThreading = true;
	
	private int threadingJitterMillis = 1000;
	
	private boolean oldJitterParam = false;

	public boolean enableGC = false;
	
	public int gcInterval = 2;
	
	@Override
	protected PamModelSettings clone() {

		try {
			PamModelSettings newParams = (PamModelSettings) super.clone();
			if (newParams.gcInterval == 0) {
				newParams.gcInterval = 2;
			}
			return newParams;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param threadingJitterMillis the threadingJitterMillis to set
	 */
	public void setThreadingJitterMillis(int threadingJitterMillis) {
		this.threadingJitterMillis = threadingJitterMillis;
	}

	/**
	 * @return the threadingJitterMillis
	 */
	public int getThreadingJitterMillis() {
		/*
		 * Use the oldJitterParam to set this to 100 millis
		 * the first time it's used (when loaded from an old
		 * settings file). After that, the user can set to zero
		 * if they want to. 
		 */
		if (oldJitterParam == false) {
			threadingJitterMillis = 1000;
			oldJitterParam = true;
		}
		return threadingJitterMillis;
	}

	public boolean equals(PamModelSettings other) {
		if (other.multiThreading != multiThreading) {
			return false;
		}
		if (other.multiThreading && other.threadingJitterMillis != threadingJitterMillis) {
			return false;
		}
		return true;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
