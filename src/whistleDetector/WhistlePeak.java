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

/**
 * @author Doug Gillespie
 *         <p>
 *         Data structure for a whistle detector peak created by the
 *         PeakDetector
 */
public class WhistlePeak {

	long startSample = 0;

	int sliceNumber = 0; // Time partition number relative to the last spec
						// slice;
//	int absoluteSliceNumber = 0;

	int MinFreq = 0; // Lower frequency bound (FFT Units)

	int PeakFreq = 0; // Frequency at signal maximum within these frequency
						// limits

	int MaxFreq = 0; // Upper frequency bound (FFT Units)

	double MaxAmp = 0; // Maximum amplitude

	double Signal = 0; // Sum of the signal within the peak

	double Noise = 0; // Sum of background withing the peak

	// double OctNoise[NOCTNOISE]; // sum of background in upper half of
	// spectrum
	boolean VetoPeak = false; // flag to say a veto cut in and sound detection
								// should abort.

	WhistleShape whistleShape; // used internally in pattern recognition - no
								// use anywhere else

	long timeOffset = 0;

	// int32 Number;
	boolean Ok = false;
	
	long timeMillis;

//	public int getAbsoluteSliceNumber() {
//		return absoluteSliceNumber;
//	}

	public double getMaxAmp() {
		return MaxAmp;
	}

	public int getMaxFreq() {
		return MaxFreq;
	}

	public int getMinFreq() {
		return MinFreq;
	}

	public double getNoise() {
		return Noise;
	}

	public boolean isOk() {
		return Ok;
	}

	public int getPeakFreq() {
		return PeakFreq;
	}

	public double getSignal() {
		return Signal;
	}

	public int getSliceNo() {
		return sliceNumber;
	}

	public long getStartSample() {
		return startSample;
	}

	public long getTimeMillis() {
		return timeMillis;
	}

	public boolean isVetoPeak() {
		return VetoPeak;
	}

	WhistlePeak(long startSample, int sliceNo,long timeMillis) {
		this.startSample = startSample;
		this.sliceNumber = sliceNo;
//		this.absoluteSliceNumber = absoluteSliceNumber;
		this.timeMillis = timeMillis;
	}
}
