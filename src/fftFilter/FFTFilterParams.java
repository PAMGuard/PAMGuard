package fftFilter;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Filters.FilterBand;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.FrequencyFormat;

public class FFTFilterParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public FilterBand filterBand = FilterBand.HIGHPASS;

	public double lowPassFreq, highPassFreq;

	@Override
	public FFTFilterParams clone() {
		try {
			return (FFTFilterParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean equals(Object other) {	
		if (other == null) return false;
		if (FFTFilterParams.class.isAssignableFrom(other.getClass()) == false) {
			return false;
		}
		FFTFilterParams that = (FFTFilterParams) other;
		return (this.lowPassFreq == that.lowPassFreq & 
				this.highPassFreq == that.highPassFreq &
				this.filterBand == that.filterBand);
	}


	@Override
	public String toString() {
		double[] f = new double[2];
		f[0] = Math.min(highPassFreq, lowPassFreq);
		f[1] = Math.max(highPassFreq, lowPassFreq);
		switch(filterBand) {
		
		case HIGHPASS:
			return String.format("High pass filter %s", FrequencyFormat.formatFrequency(highPassFreq, true));
		case LOWPASS:
			return String.format("Low pass filter %s", FrequencyFormat.formatFrequency(lowPassFreq, true));
		case BANDPASS:
			return String.format("Band pass filter %s", FrequencyFormat.formatFrequencyRange(f, true));
		case BANDSTOP:
			return String.format("Band stop filter %s", FrequencyFormat.formatFrequencyRange(f, true));
		}
		return super.toString();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
}
