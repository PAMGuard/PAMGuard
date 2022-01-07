package Array.sensors;

import java.io.Serializable;

import PamUtils.LatLong;

/**
 * Parameters for all of the three display - pitchroll, heading and depth
 * @author dg50
 *
 */
public class ArrayDisplayParameters implements Cloneable, Serializable{

	public static final long serialVersionUID = 1L;
	
	private double[] pitchRange = {-30, 30};
	
	private double pitchStep = 10;
	
	private String pitchRollImageFile;
	
	public static final String[] HEADNAMES = {"0 to +360"+LatLong.deg, "-180"+LatLong.deg+" to +180"+LatLong.deg}; 
	public static final int HEAD_0_360 = 0;
	public static final int HEAD_180_180 = 1;
	private int headRange = HEAD_0_360;

	@Override
	public ArrayDisplayParameters clone() {
		try {
			return (ArrayDisplayParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the pitchRollImageFile
	 */
	public String getPitchRollImageFile() {
		return pitchRollImageFile;
	}

	/**
	 * @param pitchRollImageFile the pitchRollImageFile to set
	 */
	public void setPitchRollImageFile(String pitchRollImageFile) {
		this.pitchRollImageFile = pitchRollImageFile;
	}

	/**
	 * @return the pitchStep
	 */
	public double getPitchStep() {
		if (pitchStep == 0) {
			pitchStep = 10;
		}
		return pitchStep;
	}

	/**
	 * @param pitchStep the pitchStep to set
	 */
	public void setPitchStep(double pitchStep) {
		this.pitchStep = pitchStep;
	}

	/**
	 * @return the pitchRange
	 */
	public double[] getPitchRange() {
		if (pitchRange == null || pitchRange.length != 2) {
			double[] r = {-30., 30.};
			pitchRange = r;
		}
		return pitchRange;
	}

	/**
	 * @param pitchRange the pitchRange to set
	 */
	public void setPitchRange(double[] pitchRange) {
		this.pitchRange = pitchRange;
	}

	/**
	 * Get range of heading values, either HEAD_0_360 or HEAD_180_180
	 * @return the headRange
	 */
	public int getHeadRange() {
		return headRange;
	}

	/**
	 * Set range of heading values, either HEAD_0_360 or HEAD_180_180
	 * @param headRange the headRange to set
	 */
	public void setHeadRange(int headRange) {
		this.headRange = headRange;
	}

}
