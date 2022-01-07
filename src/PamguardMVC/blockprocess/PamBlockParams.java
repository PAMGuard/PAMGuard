package PamguardMVC.blockprocess;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import gpl.GPLParameters;

/**
 * Parameters for controlling a PamBlockProcess
 * @author dg50 
 *
 */
public class PamBlockParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public long blockLengthMillis = 60000;
	
	public BlockMode blockMode = BlockMode.BLOCKED;

	public PamBlockParams(BlockMode blockMode, long blockLengthMillis) {
		super();
		this.blockMode = blockMode;
		this.blockLengthMillis = blockLengthMillis;
	}
	
	/**
	 * Generic constructor, to just go with the defaults
	 */
	public PamBlockParams() {
		super();
	}

	@Override
	public GPLParameters clone()  {
		try {
			return (GPLParameters) super.clone();
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
