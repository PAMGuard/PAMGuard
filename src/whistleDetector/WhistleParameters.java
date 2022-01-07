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
package whistleDetector;

import java.io.Serializable;

import PamView.dialog.GroupedSourcePanel;

/**
 * @author Doug Gillespie
 *         <p>
 *         Parameters controlling whistle detection
 * 
 */
public class WhistleParameters implements Serializable, Cloneable {
	// parameters for whistle detection
	
	static final long serialVersionUID = 3;

	String fftDataName;
	
	private double searchStartHz = 0;
	
	private double searchEndHz = 0;
	
	// Peak Detection
	public double[] peakTimeConstant = { 5., 50. };

	public double detectionThreshold = 6;
	
	public double maxPercentOverThreshold = 50;

	public int minPeakWidth = 3;

	public int maxPeakWidth = 20;

	// Whistle Linking
	public int maxGap = 3;

	public double maxDF = 35000; // Hz per second

	public double maxD2F = 200000; // Hz per second^2
	
	public double maxDA = 300; // max amplitude shift (dB) per second
	
	/**
	 * Weighting factor for DF in final link decision
	 */
	public double weightDF = 1; 

	/**
	 * Wighting factor for D2F in final link decision
	 */
	public double weightD2F = 0;

	/**
	 * Wighting factor for DA in final link decision
	 */
	public double weightDA = 0;
	
	// whistle selection
	
	public double minOccupancy = 75;
	
	public int minLength = 10;
	
	public int maxInflextions = 5;

	// Whistle event detection
	public double eventIntegrationTime = 60;
	
	public int eventMinWhistleCount = 10;
	
	public double eventMaxGapTime = 30;
	
//	public int detectionChannel = 0;
//	
//	public int bearingChannel = 1;
//	
//	public boolean measureBearings = false;
	
	int peakDetectionMethod = 1;
	
	/**
	 * the channelBitmap to analyze.  Note that this may actually be a sequenceMap, depending on the source data block
	 */
	int channelBitmap;
	
	public int[] channelGroups;
	
	public int groupingType = GroupedSourcePanel.GROUP_ALL;
	
	/**
	 * Acknowledge that this unit is out of date. 
	 */
	public boolean ackOutOfDate = false;

	public WhistleParameters() {
		peakTimeConstant[0] = 10;
		peakTimeConstant[1] = peakTimeConstant[0] * 10.;
	}

	@Override
	protected WhistleParameters clone() {
		try {
			return (WhistleParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	public double getSearchEndHz(float sampleRate) {
		if (searchEndHz <= 0 || searchEndHz > sampleRate / 2) {
			return sampleRate / 2;
		}
		return searchEndHz;
	}

	public void setSearchEndHz(double searchEndHz) {
		this.searchEndHz = searchEndHz;
	}

	public double getSearchStartHz() {
		return searchStartHz;
	}

	public void setSearchStartHz(double searchStartHz) {
		this.searchStartHz = searchStartHz;
	}
}
