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

import java.util.ArrayList;

/**
 * @author Doug Gillespie
 * 
 * Structure for potential whistles created by the WhistleLinker
 * 
 */
public class WhistleShape implements Comparable<WhistleShape> {
	// data that are actually interesting
	private int channel;

	private ArrayList<WhistlePeak> whistlePeaks;

	// data that are needed during whistle construction
	// boolean peakStolen; // Slice is completed.
	int nSoundClash;

	// int deadCount;
	// WhistleShape soundClash; // pointer ot a sound this clashed with (only
	// used in pattern rec')
	// long timeOffset;
	double gradient;

	int gap;
	
//	int lastPeakFrequency;
	
//	double lastAmplitude;
	private double dBAmplitude = -1;
	
	private WhistlePeak lastPeak; 
	
	private int minFreq, maxFreq;

	WhistleShape(int channel) {
		this.channel = channel;
		whistlePeaks = new ArrayList<WhistlePeak>();
		gradient = 0;
	}

	/**
	 * Add a new peak to the contour
	 * @param peak reference to a WhistlePeak
	 * @see WhistlePeak
	 */
	public void addPeak(WhistlePeak peak) {
		whistlePeaks.add(peak);
		if (whistlePeaks.size() >= 2) {
			gradient = ((double) peak.PeakFreq - (double) lastPeak.PeakFreq)/ (gap+1);
//			gap = peak.SliceNo - lastPeak.SliceNo - 1;
		}
		lastPeak = peak;

		if (whistlePeaks.size() == 1) {
			minFreq = maxFreq = peak.PeakFreq;
		}
		else {
			minFreq = Math.min(minFreq, peak.PeakFreq);
			maxFreq = Math.max(maxFreq, peak.PeakFreq);
		}
		
		peak.whistleShape = this;
	}

	double getAmplitude() {
		double amp = 0;
		int nPeaks = whistlePeaks.size();
		if (nPeaks == 0) return 0;
		for (int i = 0; i < nPeaks; i++) {
			amp += whistlePeaks.get(i).MaxAmp;
		}
		return amp / nPeaks;
	}
	/**
	 * 
	 * @return The total number of FFT time partitions
	 * included in the contour
	 */
	public int getSliceCount() {
		return whistlePeaks.size();
	}

	/**
	 * Get the reference ot a peak at a specific reference
	 * @param iP Peak index within the contour
	 * @return peak reference
	 * @see WhistlePeak
	 */
	public WhistlePeak GetPeak(int iP) {
		return whistlePeaks.get(iP);
	}

	/**
	 * Compare this whistle to that of another whistle
	 * @param w Reference to a different Whistle
	 * @return 1 if this whistle is the longer of the two
	 * -1 otherwise. 
	 */
	public int compareTo(WhistleShape w) {
		if (this.whistlePeaks.size() > w.whistlePeaks.size())
			return 1;
		else if (this.whistlePeaks.size() < w.whistlePeaks.size())
			return -1;
		return 0;
	}

	public WhistlePeak getLastPeak() {
		return lastPeak;
	}

	public int getMaxFreq() {
		return maxFreq;
	}

	public int getMinFreq() {
		return minFreq;
	}

	/**
	 * @return Returns the dBAmplitude.
	 */
	public double getDBAmplitude() {
		return dBAmplitude;
	}

	/**
	 * @param amplitude The dBAmplitude to set.
	 */
	public void setDBAmplitude(double amplitude) {
		dBAmplitude = amplitude;
	}
}