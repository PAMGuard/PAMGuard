package Localiser.controls;

import java.io.Serializable;

import Localiser.DelayMeasurementParams;
import fftFilter.FFTFilterParams;

/**
 * Parameters needed for waveform data timing algorithms, filters, envelope / leading edge, etc. 
 * In reality this is exactly the same as the older DelayMeasurementParams so is being deprecated !
 * @author dg50
 * @deprecated use older DelayMeasurementParams instead.
 */
public class TOADTimingParams extends DelayMeasurementParams implements Serializable, Cloneable {

	/**
	 * make private to effectively deprecate this class, but leave in project since current 
	 * psf's have this serialised and will get upset if it stops existing altogether. 
	 */
	private TOADTimingParams() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
	
//	public boolean filterData;
//
//	public boolean useEnvelope;
//	
//	public boolean useLeadingEdge;
//	
//	private FFTFilterParams fftFilterParams = new FFTFilterParams();
//
//	@Override
//	public TOADTimingParams clone() {
//		try {
//			TOADTimingParams newVal = (TOADTimingParams) super.clone();
//			if (newVal.fftFilterParams != null) {
//				newVal.fftFilterParams = newVal.fftFilterParams;
//			}
//			else {
//				newVal.fftFilterParams = new FFTFilterParams();
//			}
//			return newVal;
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

//	/**
//	 * @return the fftFilterParams
//	 */
//	private FFTFilterParams getFftFilterParams() {
//		if (delayFilterParams == null) {
//			delayFilterParams = new FFTFilterParams();
//		}
//		return delayFilterParams;
//	}
//
//	/**
//	 * @param fftFilterParams the fftFilterParams to set
//	 */
//	private blic void setFftFilterParams(FFTFilterParams fftFilterParams) {
//		this.delayFilterParams = fftFilterParams;
//	}

}
