/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Filters;

import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;

/**
 * @author Doug Gillespie
 *         <p>
 *         Superclass used by various filter types to calculate poles and zeros,
 *         gains, etc.
 */
abstract public class FilterMethod {

	protected FilterParams filterParams = new FilterParams();
	
	private double sampleRate = 1.0;
	

	public FilterMethod(double sampleRate, FilterParams filterParams) {
		// bodge filterParams so that GetPoles forces a recalculation
		this.sampleRate = sampleRate;
		this.filterParams = filterParams;
	}


	public double getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	abstract String filterName();

	/**
	 * Calculate appropriate filter coefficients. These may vary depending
	 * on filter type. So for a IIRF filter, it will be poles and zeros, for
	 * a FIR filter it will be the impulse response function. 
	 * @return number of coefficients / poles, etc. 
	 */
	abstract int calculateFilter();

	/**
	 * Get the filter gain at an angular frequency (o < omega < pi). 
	 * @param omega angular frequency
	 * @return gain
	 */
	abstract public double getFilterGain(double omega);
	/**
	 * Get the filter phase at an angular frequency (o < omega < pi). 
	 * @param omega angular frequency
	 * @return phase
	 */
	abstract public double getFilterPhase(double omega);

	/**
	 * 
	 * @return any additional gain constant (needed for IIRF's)
	 */
	abstract public double getFilterGainConstant();
	

	/**
	 * 
	 * @return Create a filter object - which can actually do some filtering for us. 
	 * Note that a filterMethod object may be asked to create multiple filters for multi-channel
	 * systems. 
	 * @param channel channel number (used in filter book keeping)
	 */
	public abstract Filter createFilter(int channel);


	/**
	 * @return the filterParams
	 */
	public FilterParams getFilterParams() {
		return filterParams;
	}


	/**
	 * @param filterParams the filterParams to set
	 */
	public void setFilterParams(FilterParams filterParams) {
		this.filterParams = filterParams;
	}
	
	/**
	 * Create a filter method based on the type in the parameters. 
	 * @param sampleRate data sample rate
	 * @param filterParams filter parameters
	 * @return FilterMethod - an abject which calculates filter coefficients of some sort. 
	 */
	public static FilterMethod createFilterMethod(double sampleRate, FilterParams filterParams) {
		if (filterParams == null) {
			return null;
		}
		switch(filterParams.filterType) {
		case NONE:
			return new NullMethod(sampleRate, filterParams);
		case BUTTERWORTH:
			return new ButterworthMethod(sampleRate, filterParams);
		case CHEBYCHEV:
			return new ChebyshevMethod(sampleRate, filterParams);
		case FIRWINDOW:
			return new FIRFilterMethod(sampleRate, filterParams);
		case FIRARBITRARY:
			return new FIRArbitraryFilter(sampleRate, filterParams);
		case FFT:
			return new FFTFilterMethod(sampleRate, filterParams);
		}
		
		return new NullMethod(sampleRate, filterParams);
	}


	/**
	 * Used by new fast IIR filter system. 
	 * @return
	 */
	public double[] getFastFilterCoefficients() {
		return null;
	}
}
