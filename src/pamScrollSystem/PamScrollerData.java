package pamScrollSystem;

import java.io.Serializable;

public class PamScrollerData implements Serializable, Cloneable {


	private static final long serialVersionUID = 1L;

	/*
	 * Name for the scroller - gets used in various dialogs. 
	 */
	protected String name;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Minimum time of data loaded for this scroller
	 */
	protected long minimumMillis;

	/**
	 * Maximum time of data loaded for this scroller
	 */
	protected long maximumMillis;

	/**
	 * Scroller step size in millis (this will often be seconds or even minutes)
	 */
	private int stepSizeMillis;


	/**
	 * Page step size - percentage of (maximumMills-mnimumMillis) to
	 * move back or forth through the data in response to a
	 * new load data command. 
	 */
	protected int pageStep = 75;

	/**
	 * Default data load time. 
	 */
	protected long defaultLoadtime = 120000;
	
	private double playSpeed = 1.0;
	

	@Override
	public PamScrollerData clone() {
		try {
			return (PamScrollerData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * A quick way of getting the duration of loaded data
	 * @return maximumMillis - minimumMillis
	 */
	public long getLength() {	
		return maximumMillis - minimumMillis;
	}

	/**
	 * The start time of the scroller. 
	 * @return the start time of scroller in millis datenmum. 
	 */
	public long getMinimumMillis() {
		return minimumMillis;
	}

	/**
	 * Set the start time of the scroller. 
	 * @return the start time of scroller in millis datenmum. 
	 */
	public void setMinimumMillis(long minimumMillis) {
		this.minimumMillis = minimumMillis;
	}

	
	/**
	 * The end time of the scroller. 
	 * @return the end time of scroller in millis datenmum. 
	 */
	public long getMaximumMillis() {
		return maximumMillis;
	}

	/**
	 * Set the end time of the scroller. 
	 * @param the end time of scroller in millis datenmum. 
	 */
	public void setMaximumMillis(long maximumMillis) {
		this.maximumMillis = maximumMillis;
	}

	/**
	 * Get the step size. This the jump that is made during a page forward or page back. 
	 * @return % jump forward/backward during a page. 
	 */
	public int getPageStep() {
		return pageStep;
	}
	
	/**
	 * Set the step size. This the jump that is made during a page forward or page back. 
	 * @param % jump forward/backward during a page. 
	 */
	public void setPageStep(int pageStep) {
		this.pageStep = pageStep;
	}

	public int getStepSizeMillis() {
		return stepSizeMillis;
	}

	/**
	 * @param stepSizeMillis the stepSizeMillis to set
	 */
	public void setStepSizeMillis(int stepSizeMillis) {
		this.stepSizeMillis = stepSizeMillis;
	}

	/**
	 * @return the playSpeed
	 */
	public double getPlaySpeed() {
		if (playSpeed <= 0) {
			playSpeed = 1.0;
		}
		return playSpeed;
	}

	/**
	 * @param playSpeed the playSpeed to set
	 */
	public void setPlaySpeed(double playSpeed) {
		this.playSpeed = playSpeed;
	}
}
