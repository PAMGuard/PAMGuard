package bearinglocaliser.algorithms;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class BearingAlgorithmParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * The group number these parameters are linked to
	 */
	protected int groupNumber;

	/**
	 * Channel map describing the channels (hydrophones) used in this group
	 */
	protected int channelMap;

	/**
	 * Constructor used mainly by the beamogram wrapper class
	 * ({@link bearinglocaliser.beamformer.WrappedBeamFormParams WrappedBeamFormParams}),
	 *  where the bearing params aren't set but the beamogram params are
	 */
	public BearingAlgorithmParams() {
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param groupNumber
	 * @param channelMap
	 */
	public BearingAlgorithmParams(int groupNumber, int channelMap) {
		this.groupNumber = groupNumber;
		this.channelMap = channelMap;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getChannelMap() {
		return channelMap;
	}

	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
