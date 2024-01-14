package PamguardMVC.toad;

import java.io.Serializable;

import Localiser.DelayMeasurementParams;
import Localiser.controls.RawOrFFTParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * General parameters for detection TOAD measurement. Is split 
 * into two parts since the RAWOrFFTParams part is used by the 
 * FFTDataGrouper which is used in both beam formers and TOAD based
 * algorithms, whereas the TOADtimingParams is only used with TOAD. 
 * @author dg50
 *
 */
public class GenericTOADSourceParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private RawOrFFTParams rawOrFFTParams;
	
	private DelayMeasurementParams toadTimingParams;
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GenericTOADSourceParams clone() {
		try {
			return (GenericTOADSourceParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @return the rawOrFFTParams
	 */
	public RawOrFFTParams getRawOrFFTParams() {
		if (rawOrFFTParams == null) {
			rawOrFFTParams = new RawOrFFTParams();
		}
		return rawOrFFTParams;
	}
	/**
	 * @param rawOrFFTParams the rawOrFFTParams to set
	 */
	public void setRawOrFFTParams(RawOrFFTParams rawOrFFTParams) {
		this.rawOrFFTParams = rawOrFFTParams;
	}
	/**
	 * @return the toadTimingParams
	 */
	public DelayMeasurementParams getToadTimingParams() {
		if (toadTimingParams == null) {
			toadTimingParams = new DelayMeasurementParams();
		}
		return toadTimingParams;
	}
	/**
	 * @param toadTimingParams the toadTimingParams to set
	 */
	public void setToadTimingParams(DelayMeasurementParams toadTimingParams) {
		this.toadTimingParams = toadTimingParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
