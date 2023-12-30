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

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnit;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.FrequencyFormat;

/**
 * @author Doug Gillespie
 *         <p>
 *         Parameters for digital filter design - just the filter on it's own,
 *         not the complete set with data sources and everything eles.
 */
public class FilterParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1;

	public FilterType filterType = FilterType.BUTTERWORTH;

	public FilterBand filterBand = FilterBand.BANDPASS;

	public int filterOrder;

	public float lowPassFreq, highPassFreq;
	
	/**
	 * Centre frequency for band pass filters. This is really only
	 * used with the filter designs forANSI standard filters used in the noise module. 
	 */
	private double centreFreq; 

	public double passBandRipple;

	public double stopBandRipple;
	
	public double chebyGamma = 3;

	/**
	 * Scale type just used for drawing dialog
	 */
	public int scaleType = SCALE_LOG;

	/**
	 * Some extras for arbitrary filters. 
	 */
	public File lastImportFile;

	public double[] arbFreqs;

	public double[] arbGains;

	public static final int SCALE_LOG = 0;
	public static final int SCALE_LIN = 1;

	/**
	 * Construct a filter parameter set with default params
	 */
	public FilterParams() {
		filterOrder = 4;
		lowPassFreq = 20000;
		highPassFreq = 2000;
		passBandRipple = 2;
		stopBandRipple = 2;
	}
	
	/**
	 * Construct a filter params set with given params
	 * @param type Filter type
	 * @param band Filter band
	 * @param lowPassFreq low pass frequency
	 * @param highPassFreq high pass frequency
	 * @param order filter order
	 */
	public FilterParams(FilterType type, FilterBand band, float lowPassFreq, float highPassFreq, int order) {
		this();
		filterType = type;
		filterBand = band;
		this.lowPassFreq = lowPassFreq;
		this.highPassFreq = highPassFreq;
		this.filterOrder = order;
	}

	public boolean equals(FilterParams p) {

		return (this.filterType == p.filterType
				&& this.filterBand == p.filterBand
				&& this.filterOrder == p.filterOrder
				&& this.lowPassFreq == p.lowPassFreq
				&& this.highPassFreq == p.highPassFreq
				&& this.passBandRipple == p.passBandRipple && this.stopBandRipple == p.stopBandRipple);

	}

	public void assign(FilterParams p) {

		this.filterType = p.filterType;
		this.filterBand = p.filterBand;
		this.filterOrder = p.filterOrder;
		this.lowPassFreq = p.lowPassFreq;
		this.highPassFreq = p.highPassFreq;
		this.passBandRipple = p.passBandRipple;
		this.stopBandRipple = p.stopBandRipple;
	}

	@Override
	public FilterParams clone() {
		try {
			FilterParams newP = (FilterParams) super.clone();
			if (newP.chebyGamma <= 0) {
				newP.chebyGamma = 3;
			}
			return newP;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the filter type as a string
	 */
	public String sayType() {
		switch (filterType) {
		case NONE:
			return "None";
		case CHEBYCHEV:
			return "Chebychev";
		case BUTTERWORTH:
			return "Butterworth";
		case FIRWINDOW:
			return "FIR Filter";
		case FIRARBITRARY:
			return "FIR Arbitrary";
		default:
			return "Unknown";
		}
	}

	/**
	 * 
	 * @return the filter band as a string
	 */
	public String sayBand() {
		switch (filterBand) {
		case BANDPASS:
			return "Bandpass";
		case BANDSTOP:
			return "Bandstop";
		case HIGHPASS:
			return "Highpass";
		case LOWPASS:
			return "Lowpass";
		default:
			return "Unknown";
		}
	}
	@Override
	public String toString() {
		String str;
		if (filterType == Filters.FilterType.NONE){
			return "No Filter";
		}
		else if (filterType == Filters.FilterType.CHEBYCHEV){
			str = "Chebychev";
		}
		else if (filterType == Filters.FilterType.BUTTERWORTH){
			str = "Butterworth";
		}
		else if (filterType == Filters.FilterType.FIRWINDOW){
			str = "FIR filter";
		}
		else {
			return "Unknown Filter parameters";
		}
		if (filterBand == FilterBand.LOWPASS) {
			str += String.format(" Low pass %s", FrequencyFormat.formatFrequency(lowPassFreq, true));
		}
		else if (filterBand == FilterBand.HIGHPASS) {
			str += String.format(" High pass %s", FrequencyFormat.formatFrequency(highPassFreq, true));
		}
		else if (filterBand == FilterBand.BANDPASS) {
			str += String.format(" Band pass %s to %s", FrequencyFormat.formatFrequency(highPassFreq, true), FrequencyFormat.formatFrequency(lowPassFreq, true));
		}
		else if (filterBand == FilterBand.BANDSTOP) {
			str += String.format(" Band stop %s to %s", FrequencyFormat.formatFrequency(highPassFreq, true), FrequencyFormat.formatFrequency(lowPassFreq, true));
		}
		else {
			str += " Unknown band type";
		}
		return str;
	}

	public double getCenterFreq() {
		if (centreFreq != 0) {
			return centreFreq;
		}
		return Math.sqrt(lowPassFreq * highPassFreq);
	}
	
	/**
	 * This method only included so that centreFreq gets automatically
	 * added to PamParameterSet in getParameterSet method
	 * @return
	 */
	public double getCentreFreq() {
		return getCenterFreq();
	}

	public void setCentreFreq(double d) {
		centreFreq = d;
	}

	public void setArbFilterShape(double[] f, double[] gain) {
		this.arbFreqs = f;
		this.arbGains = gain;
	}

	/**
	 * @param sampleRate sample rate (NOT Niquist)
	 * @return the arbFreqs normalised to Niquist. 
	 */
	public double[] getArbFreqsReNiquist(double sampleRate) {
		if (sampleRate <= 0) sampleRate = 1.;
		if (arbFreqs == null) return null;
		double[] f = new double[arbFreqs.length];
		for (int i = 0; i < f.length; i++) {
			f[i] = arbFreqs[i] / (sampleRate/2.);
		}
		return f;
	}
	
	public double[] getArbFreqs() {
		return arbFreqs;
	}

	/**
	 * @return the arbGains
	 */
	public double[] getArbGainsdB() {
		return arbGains;
	}

	/**
	 * 
	 * @return the nominal pass band of the filter. 
	 */
	public double[] getFrequencyLimits(double sampleRate) {
		double[] freqLims = {0., sampleRate/2.};
		switch (filterType) {
		case BUTTERWORTH:
		case CHEBYCHEV:
		case FIRWINDOW:
			return getStandardFreqLims(freqLims);
		case FIRARBITRARY:
			return getArbitraryFreqLims(freqLims);
		case NONE:
			break;
		default:
			break;
		
		}
		return freqLims;
	}
	
	/**
	 * Work out -3dB limits for an arbitrary IIR filter. 
	 * @param freqLims nominal 0 - Niquist limits
	 * @return modified limits according to filter shape. 
	 */
	private double[] getArbitraryFreqLims(double[] freqLims) {
		if (arbFreqs == null || arbGains == null) {
			return freqLims;
		}
		double maxGain = arbGains[0];
		for (int i = 1; i < arbGains.length; i++) {
			maxGain = Math.max(maxGain, arbGains[i]);
		}
		double gainLim = maxGain - 3;
		int i = 0;
		for (; i < arbGains.length; i++) {
			if (arbGains[i] >= gainLim) {
				freqLims[0] = arbFreqs[i];
				break;
			}
		} 
		for (; i < arbGains.length; i++) {
			if (arbGains[i] >= gainLim) {
				freqLims[1] = arbFreqs[i];
			}
		}
		return freqLims;
	}

	/**
	 * Get the frequency limits for a standard IIR of Window method FIR filter. 
	 * @param freqLims nominal 0 - Niquist limits
	 * @return modified limits according to filter band. 
	 */
	private double[] getStandardFreqLims(double[] freqLims) {
		switch (filterBand) {
		case BANDPASS:
			freqLims[0] = Math.min(highPassFreq, lowPassFreq);
			freqLims[1] = Math.max(highPassFreq, lowPassFreq);
		case BANDSTOP:
			// can't really do anything for band stop - will be 0 to Niquist with a hole in the middle. . 
			break;
		case HIGHPASS:
			freqLims[0] = highPassFreq;
			break;
		case LOWPASS:
			freqLims[1] = lowPassFreq;
			break;
		default:
			break;
		
		}
		return freqLims;
	}

	/**
	 * @return the arbGains
	 */
	public double[] getArbGainsFact() {
		if (arbGains == null) return null;
		double[] g = new double[arbGains.length];
		for (int i = 0; i < g.length; i++) {
			g[i] = Math.pow(10., arbGains[i]/20);
		}
		return g;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}



}
