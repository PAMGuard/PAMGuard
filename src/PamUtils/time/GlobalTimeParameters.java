package PamUtils.time;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class GlobalTimeParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private String selectedTimeSource = NullTimeCorrector.class.getName();
		
	private boolean updateUTC = true;
	
	private int smoothingTimeSeconds = 600;
	
	/**
	 * startupDelay: Wait this many milliseconds at PamStart
	 * in order to synchronize the clocks;
	 */
	private Integer startupDelay = 2000;

	/**
	 * @return the selectedTimeSource
	 */
	public String getSelectedTimeSource() {
		return selectedTimeSource;
	}

	/**
	 * @param selectedTimeSource the selectedTimeSource to set
	 */
	public void setSelectedTimeSource(String selectedTimeSource) {
		this.selectedTimeSource = selectedTimeSource;
	}

	/**
	 * @return the updateUTC
	 */
	public boolean isUpdateUTC() {
		return updateUTC;
	}

	/**
	 * @param updateUTC the updateUTC to set
	 */
	public void setUpdateUTC(boolean updateUTC) {
		this.updateUTC = updateUTC;
	}

	/**
	 * @return the smoothingTimeSeconds
	 */
	public int getSmoothingTimeSeconds() {
		return smoothingTimeSeconds;
	}

	/**
	 * @param smoothingTimeSeconds the smoothingTimeSeconds to set
	 */
	public void setSmoothingTimeSeconds(int smoothingTimeSeconds) {
		this.smoothingTimeSeconds = smoothingTimeSeconds;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GlobalTimeParameters clone() {
//		try {
//			return (GlobalTimeParameters) super.clone();
		try {
			GlobalTimeParameters clonedParams = (GlobalTimeParameters) super.clone();
			if (clonedParams.startupDelay == null)
				clonedParams.startupDelay = 2000;
			return clonedParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getStartupDelay() {
		return startupDelay;
	}

	public void setStartupDelay(int startupDelay) {
		this.startupDelay = startupDelay;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
