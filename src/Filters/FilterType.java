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

public enum FilterType {
	
	NONE, BUTTERWORTH, CHEBYCHEV, FIRWINDOW, FIRARBITRARY, FFT;
	
	boolean isIIR() {
		switch (this) {
		case BUTTERWORTH:
		case CHEBYCHEV:
			return true;
		case FIRARBITRARY:
		case FIRWINDOW:
		case FFT:
			return false;
		default:
			return false;
		}
	}

	boolean isFIR() {
		switch (this) {
		case BUTTERWORTH:
		case CHEBYCHEV:
			return false;
		case FIRARBITRARY:
		case FIRWINDOW:
		case FFT:
			return true;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		/*
		 * 
	private String[] filterNames = { "None", "IIR Butterworth", "IIR Chebyshev", 
			"FIR Filter (Window Method)", "Arbitrary FIR Filter", "FFT Filter"};
		 */
		switch (this) {
		case BUTTERWORTH:
			return "IIR Butterworth";
		case CHEBYCHEV:
			return "IIR Chebyshev";
		case FFT:
			return "FFT Filter";
		case FIRARBITRARY:
			return "Arbitrary FIR Filter";
		case FIRWINDOW:
			return "FIR Filter (Window Method)";
		case NONE:
			return "None";
		default:
			break;
		
		}
		return null;
	}
	
	/**
	 * Should enable the order control. 
	 * @param filterType
	 * @return
	 */
	public boolean hasOrder() {
		switch(this) {
		case BUTTERWORTH:
			return true;
		case CHEBYCHEV:
			return true;
		case FFT:
			return false;
		case FIRARBITRARY:
			return true;
		case FIRWINDOW:
			return true;
		case NONE:
			return false;
		default:
			break;
		
		}
		return false;
	}
	
	/**
	 * Should enable the order control. 
	 * @param filterType
	 * @return
	 */
	public boolean hasRipple() {
		switch(this) {
		case BUTTERWORTH:
			return false;
		case CHEBYCHEV:
			return true;
		case FFT:
			return false;
		case FIRARBITRARY:
			return true;
		case FIRWINDOW:
			return true;
		case NONE:
			return false;
		default:
			break;
		
		}
		return false;
	}
	
	
}
