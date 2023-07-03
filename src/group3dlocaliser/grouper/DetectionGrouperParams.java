package group3dlocaliser.grouper;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class DetectionGrouperParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int GROUPS_ALL_POSSIBLE = 0;
	public static final int GROUPS_FIRST_ONLY = 1;

	public int maxPerSubGroup = 10;
	
	public int maxPerGroup = 20;
	
	public int minSubGroups = 2;
	
	/**
	 * The primary detection groups. The slected detection groups must be included in the combination for a loclaisation 
	 * 
	 * null means there is no primary group. 
	 */
	public int[] primaryDetGroup = null; 
	
	public int groupingChoice = GROUPS_ALL_POSSIBLE;
	
	// data selection options
	public static final int DATA_SELECT_ALL = 0;
	public static final int DATA_SELECT_MIN_N = 1;
	public int dataSelectOption = DATA_SELECT_ALL;
	public int dataSelectMinimum;

	@Override
	protected DetectionGrouperParams clone() {
		try {
			return (DetectionGrouperParams) super.clone();
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
