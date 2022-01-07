package crossedbearinglocaliser;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.PamDataBlock;
import annotation.localise.targetmotion.TMAnnotationOptions;

public class CBLocaliserSettngs implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private String parentDataBlock;
	
	private TMAnnotationOptions tmAnnotationOptions;
	
	private int minDetections = 2;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected CBLocaliserSettngs clone() {
		try {
			CBLocaliserSettngs cloned = (CBLocaliserSettngs) super.clone();
			return cloned;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the parentDataBlock
	 */
	public String getParentDataBlock() {
		return parentDataBlock;
	}

	/**
	 * @param parentDataBlock the parentDataBlock to set
	 */
	public void setParentDataBlock(String parentDataBlock) {
		this.parentDataBlock = parentDataBlock;
	}

	/**
	 * @return the tmAnnotationOptions
	 */
	public TMAnnotationOptions getTmAnnotationOptions() {
		if (tmAnnotationOptions == null) {
			tmAnnotationOptions = new TMAnnotationOptions("Crossed Bearing Localiser");
		}
		return tmAnnotationOptions;
	}

	/**
	 * @param tmAnnotationOptions the tmAnnotationOptions to set
	 */
	public void setTmAnnotationOptions(TMAnnotationOptions tmAnnotationOptions) {
		this.tmAnnotationOptions = tmAnnotationOptions;
	}

	/**
	 * @return the minDetections
	 */
	public int getMinDetections() {
		return Math.max(minDetections, 2);
	}

	/**
	 * @param minDetections the minDetections to set
	 */
	public void setMinDetections(int minDetections) {
		this.minDetections = minDetections;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
