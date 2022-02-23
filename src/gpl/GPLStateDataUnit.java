package gpl;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class GPLStateDataUnit extends PamDataUnit {

	private double baseline;
	private int peakState;
	private double ceilnoise;
	private double threshfloor;

	/**
	 * Constructor that sets both channel map and sequence map for this data unit
	 * @param timeMilliseconds
	 * @param sequenceNum
	 * @param channelMap
	 * @param baseline
	 * @param threshfloor 
	 * @param ceilnoise 
	 * @param peakState
	 */
	public GPLStateDataUnit(long timeMilliseconds, int sequenceNum, int channelMap, double baseline, 
			double ceilnoise, double threshfloor, int peakState) {
		super(timeMilliseconds);
		setSequenceBitmap(1<<sequenceNum);
		setChannelBitmap(channelMap);
		this.baseline = baseline;
		this.peakState = peakState;
		this.ceilnoise = ceilnoise;
		this.threshfloor = threshfloor;
	}
	
	/**
	 * constructor used reading back from binary files. 
	 * @param baseData
	 * @param baseline
	 * @param ceilnoise
	 * @param threshfloor
	 * @param peakState
	 */
	public GPLStateDataUnit(DataUnitBaseData baseData,double baseline, 
			double ceilnoise, double threshfloor, int peakState) {
		super(baseData);
		this.baseline = baseline;
		this.peakState = peakState;
		this.ceilnoise = ceilnoise;
		this.threshfloor = threshfloor;
	}
	
	/**
	 * @return the ceilnoise
	 */
	public double getCeilnoise() {
		return ceilnoise;
	}

	/**
	 * @return the threshfloor
	 */
	public double getThreshfloor() {
		return threshfloor;
	}

	/**
	 * Constructor that sets only channel map for this unit
	 * @param timeMilliseconds
	 * @param channel
	 * @param baseline
	 * @param peakState
	 */
	@Deprecated
	public GPLStateDataUnit(long timeMilliseconds, int channel, double baseline, int peakState) {
		super(timeMilliseconds);
		setChannelBitmap(1<<channel);
		this.baseline = baseline;
		this.peakState = peakState;
	}

	/**
	 * @return the baseline
	 */
	public double getBaseline() {
		return baseline;
	}

	/**
	 * @return the peakState
	 */
	public int getPeakState() {
		return peakState;
	}

}
