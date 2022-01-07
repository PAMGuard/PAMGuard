package PamguardMVC.dataSelector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Data select parameters. 
 * 
 * @author Doug Gillespie
 *
 */
abstract public class DataSelectParams implements Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public static final int DATA_SELECT_AND = 0;
	public static final int DATA_SELECT_OR = 1;
	public static final int DATA_SELECT_DISABLE = 2;
	
	private int combinationFlag = DATA_SELECT_DISABLE;

	/**
	 * How to enable / disable /  combine one or more dataselectors, 
	 * particularly in a CompundDataSelector in which case we may 
	 * want to disable some or all of the different data selectors from 
	 * different annotations as well as the main data selector for the type
	 * of data in the block. <p>
	 * DATA_SELECT_AND (0) will AND this with previous output (take the minimum of two double results)<br>
	 * DATA_SELECT_OR (1) will OR this with previous output (take the maximum of two double results)<br>
	 * DATA_SELECT_DISABLE (2) will disable this data selector (return always 1)<br>
	 * @return the combinationFlag
	 */
	public int getCombinationFlag() {
		return combinationFlag;
	}

	/**	 
	 * How to enable / disable /  combine one or more dataselectors, 
	 * particularly in a CompundDataSelector in which case we may 
	 * want to disable some or all of the different data selectors from 
	 * different annotations as well as the main data selector for the type
	 * of data in the block. <p>
	 * DATA_SELECT_AND (0) will AND this with previous output (take the minimum of two double results)<br>
	 * DATA_SELECT_OR (1) will OR this with previous output (take the maximum of two double results)<br>
	 * DATA_SELECT_DISABLE (2) will disable this data selector (return always 1)<br>
	 * @param combinationFlag the combinationFlag to set
	 */
	public void setCombinationFlag(int combinationFlag) {
		this.combinationFlag = combinationFlag;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	
}
