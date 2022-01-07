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

package clickDetector;

import java.awt.Color;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import clickDetector.ClickClassifiers.ClickTypeCommonParams;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Parameters for individual click identification describing a single
 *         click type
 * @see clickDetector.BasicClickIdParameters BasicClickIdParameters
 * @see clickDetector.ClickClassifiers.basic.BasicClickIdentifier BasicClickIdentifier
 */
public class ClickTypeParams extends ClickTypeCommonParams
        implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 0;
	
	public static final int ENABLE_ENERGYBAND = 0x1;
	public static final int ENABLE_PEAKFREQWIDTH= 0x2;
	public static final int ENABLE_PEAKFREQPOS = 0x4;
	public static final int ENABLE_MEANFREQUENCY = 0x8;
	public static final int ENABLE_CLICKLENGTH = 0x10;

	static private final int NUM_STANDARDS = 2;
	static public final int STANDARD_BEAKED_WHALE = 0;
	static public final int STANDARD_PORPOISE = 1;
	

	public PamSymbol symbol = new PamSymbol();
	
	public int whichSelections = ENABLE_ENERGYBAND | ENABLE_PEAKFREQPOS;

	public double[] band1Freq = new double[2]; // frequency range for control band

	public double[] band2Freq = new double[2]; // frequency range for test band

	public double[] band1Energy = new double[2]; // energy range for control band

	public double[] band2Energy = new double[2]; // energy range for test band

	public double bandEnergyDifference; // minimum difference in ban energiesin dB

	public double[] peakFrequencySearch = new double[2]; // search range for peak
													// frequency (Hz)

	public double[] peakFrequencyRange = new double[2]; // allowable range for peak
													// frequency (Hz)

	public double[] peakWidth = new double[2]; // max width of frequency peak (Hz)

	public double widthEnergyFraction; // energy fraction to use in width calculation
	
	public double[] meanSumRange = new double[2];
	
	public double[] meanSelRange = new double[2];

	public double[] clickLength = new double[2]; // allowable lengh of clicks in ms.

	public double lengthEnergyFraction; // fractin of energy to use in click length
									// calculation
	//    
	// ClickTypeParams(ClickTypeParams clickTypeParams) {
	// assign (clickTypeParams);
	// }
	//    
	// public void assign(ClickTypeParams clickTypeParams) {
	// this.name = new String(clickTypeParams.name);
	// this.code = clickTypeParams.code;
	// this.bandEnergyDifference = clickTypeParams.bandEnergyDifference;
	//    	
	// }

	public ClickTypeParams(int code) {
		this.setSpeciesCode(code);
	}
	public ClickTypeParams(int code, int iStandard) {
		this.setSpeciesCode(code);
		
		switch (iStandard) {
		case STANDARD_BEAKED_WHALE:
			setStandardBeakedWhale();
			break;
		case STANDARD_PORPOISE:
			setStandardPorpoise();
			break;
		}
	}

	@Override
	public ClickTypeParams clone() {
		try {
			ClickTypeParams newSet = (ClickTypeParams) super.clone();
			newSet.symbol = newSet.symbol.clone();
			return newSet;
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return null;
		}
	}
	

	private void setStandardPorpoise() {
		
		whichSelections = ENABLE_ENERGYBAND | ENABLE_MEANFREQUENCY | ENABLE_PEAKFREQPOS;
		
		setName("Porpoise");
		symbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, 10, 10, true, Color.RED, Color.RED);
		
		band2Freq[0] = 40000;
		band2Freq[1] = 90000;
		band1Freq[0] = 100000;
		band1Freq[1] = 150000;
		band2Energy[0] = 0;
		band2Energy[1] = 500;
		band1Energy[0] = 0;
		band1Energy[1] = 500;
		bandEnergyDifference = 5;
		
		peakFrequencySearch[0] = 20000;
		peakFrequencySearch[1] = 250000;
		peakFrequencyRange[0] = 100000;
		peakFrequencyRange[1] = 150000;

		meanSumRange[0] = 20000;
		meanSumRange[1] = 250000;
		meanSelRange[0] = 100000;
		meanSelRange[1] = 150000;
		
	}
	
	private void setStandardBeakedWhale() {
		
		whichSelections = ENABLE_ENERGYBAND | ENABLE_MEANFREQUENCY | ENABLE_PEAKFREQPOS;
		
		setName("Beaked Whale");
		symbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.MAGENTA, Color.MAGENTA);
		
		band2Freq[0] = 10000;
		band2Freq[1] = 23125;
		band1Freq[0] = 25000;
		band1Freq[1] = 40000;
		band2Energy[0] = 0;
		band2Energy[1] = 500;
		band1Energy[0] = 0;
		band1Energy[1] = 500;
		bandEnergyDifference = 5;
		
		peakFrequencySearch[0] = 10000;
		peakFrequencySearch[1] = 96000;
		peakFrequencyRange[0] = 25000;
		peakFrequencyRange[1] = 45000;

		meanSumRange[0] = 10000;
		meanSumRange[1] = 96000;
		meanSelRange[0] = 25000;
		meanSelRange[1] = 45000;
		
	}

	public static int getNUM_STANDARDS() {
		return ClickTypeParams.NUM_STANDARDS;
	}

	public static String getStandardName(int iSpecies) {
		switch (iSpecies) {
		case STANDARD_BEAKED_WHALE:
			return "Beaked Whale";
		case STANDARD_PORPOISE:
			return "Porpoise";
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
