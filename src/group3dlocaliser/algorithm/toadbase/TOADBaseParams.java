package group3dlocaliser.algorithm.toadbase;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Parameters that apply to all TOAD based localisers. 
 * Note that parameters controlling TOAD calculations are
 * held within the individual TOAD calculators since they 
 * may have data type specific options, so can't be handled by
 * this more generic part of the localisation system. 
 * @author dg50
 *
 */
public class TOADBaseParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/*
	 * Bitmap of channels to use in localisation. 
	 */
	private int channelBitmap;
	
	/**
	 * Minimum cross correlation value within a channel pair for it to be used in 
	 * localisation
	 */
	private double minCorrelation = 0;
	
	/**
	 * Minimum number of time delays that have an acceptable correlation value
	 */
	private int minTimeDelays = 4;
	
	/**
	 * Minimum number of channel groups that have at least one acceptable 
	 * correlation value in them.  
	 */
	private int minCorrelatedGroups = 0;

	/**
	 * @return the channelBitmap
	 */
	public int getChannelBitmap() {
		return channelBitmap;
	}

	/**
	 * @param channelBitmap the channelBitmap to set
	 */
	public void setChannelBitmap(int channelBitmap) {
		this.channelBitmap = channelBitmap;
	}

	@Override
	protected TOADBaseParams clone() {
		try {
			return (TOADBaseParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Minimum cross correlation value within a channel pair for it to be used in 
	 * localisation
	 * @return the minCorrelation
	 */
	public double getMinCorrelation() {
		return minCorrelation;
	}

	/**
	 * Minimum cross correlation value within a channel pair for it to be used in 
	 * localisation
	 * @param minCorrelation the minCorrelation to set
	 */
	public void setMinCorrelation(double minCorrelation) {
		this.minCorrelation = minCorrelation;
	}


	/**
	 * Minimum number of channel groups that have at least one acceptable 
	 * @return the minCorrelatedGroups
	 */
	public int getMinCorrelatedGroups() {
		return minCorrelatedGroups;
	}

	/**
	 * Minimum number of channel groups that have at least one acceptable 
	 * @param minCorrelatedGroups the minCorrelatedGroups to set
	 */
	public void setMinCorrelatedGroups(int minCorrelatedGroups) {
		this.minCorrelatedGroups = minCorrelatedGroups;
	}

	/**
	 * Minimum number of time delays that have an acceptable correlation value
	 * @return the minTimeDelays
	 */
	public int getMinTimeDelays() {
		return minTimeDelays;
	}

	/**
	 * Minimum number of time delays that have an acceptable correlation value
	 * @param minTimeDelays the minTimeDelays to set
	 */
	public void setMinTimeDelays(int minTimeDelays) {
		this.minTimeDelays = minTimeDelays;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
