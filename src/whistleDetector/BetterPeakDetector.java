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

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamObservable;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class BetterPeakDetector extends BasicPeakDetector {

	double[] localAverage;

	double[] magSquareData;

	public BetterPeakDetector(WhistleControl whistleControl,
			WhistleDetector whistleDetector, FFTDataBlock fftDataSource, int groupChannels) {
		super(whistleControl, whistleDetector, fftDataSource, groupChannels);
	}

	@Override
	public void searchForPeaks(PamObservable obs, FFTDataUnit newDataUnit) {

		slicesAnalysed++;

		ComplexArray newFftData = newDataUnit.getFftData();
		Enum peakOn = PeakStatus.PEAK_OFF;
		double newval;
		int nOver = 0;
		int downCount = 0;
		int peakWidth;
		WhistlePeak newPeak = new WhistlePeak(newDataUnit.getStartSample(),
				newDataUnit.getFftSlice(), newDataUnit.getTimeMilliseconds());
		ArrayList<WhistlePeak> peakList = new ArrayList<WhistlePeak>();

		int localAverageLen = 5; // makes life easier if these are
									// symmetrical !
		int localAverageGap = 5;
		/*
		 * Allocate memory for the local average as necessary
		 */
		if (localAverage == null
				|| localAverage.length != whistleDetector.fftLength / 2) {
			localAverage = new double[whistleDetector.fftLength / 2];
		}
		if (magSquareData == null
				|| magSquareData.length != whistleDetector.fftLength / 2) {
			magSquareData = new double[whistleDetector.fftLength / 2];
		}

		if (slicesAnalysed <= WARMUP_SLICES) {
			// just average the slice data into the background measure to get
			// started
			for (int i = 0; i < whistleDetector.fftLength / 2; i++) {
				spectrumAverage[i] += (newFftData.magsq(i) / WARMUP_SLICES);
			}
			return;
		}
		// otherwise, update the background according to whether each bin is
		// over threshold or not.
		// and at the same time see if it's over threshold
		for (int i = searchBin0; i <= searchBin1; i++) {
			magSquareData[i] = newFftData.magsq(i);
			newval = magSquareData[i] / spectrumAverage[i];
			if ((overThreshold[i] = newval > detectionThreshold)) {
				spectrumAverage[i] *= bgndUpdate1_1;
				spectrumAverage[i] += magSquareData[i] * bgndUpdate1;
				nOver++;
			} else {
				spectrumAverage[i] *= bgndUpdate0_1;
				spectrumAverage[i] += magSquareData[i] * bgndUpdate0;
			}
			nrData[i] = magSquareData[i] - spectrumAverage[i];
			localAverage[i] = 0;
		}
		
		if (nOver * 100 / overThreshold.length > whistleControl.whistleParameters.maxPercentOverThreshold) return;

		/*
		 * Work out a local average level around each point which can be used to
		 * remove broad band clicks
		 */
		int lao = localAverageLen / 2;
		int k = whistleDetector.fftLength / 2;
		int l;
		for (int i = 0; i < localAverageLen - lao; i++) {
			k--;
			l = whistleDetector.fftLength / 2;
			for (int j = 0; j < localAverageLen; j++) {
				l--;
				localAverage[i] += magSquareData[j];
				localAverage[k] += magSquareData[l];
			}
			localAverage[i] /= localAverageLen;
			localAverage[k] /= localAverageLen;

		}
		for (int i = localAverageLen - lao; i < whistleDetector.fftLength / 2
				- lao; i++) {
			localAverage[i] += (magSquareData[i + lao] - magSquareData[i - lao])
					/ localAverageLen;
		}

		/*
		 * Now go through and subtract off the local averages from the mag
		 * squared data to make new mag squared data
		 */
		int sao = (localAverageLen / 2) + (localAverageGap / 2); // yes, I
																	// did mean
																	// to round
																	// each of
																	// these
																	// down
																	// separately
																	// !
		for (int i = 0; i < sao; i++) {
			magSquareData[i] = 0.;
			magSquareData[whistleDetector.fftLength / 2 - 1 - i] = 0.;
		}
		for (int i = sao + searchBin0; i < (searchBin1 - sao); i++) {
			magSquareData[i] -= (localAverage[i - sao] + localAverage[i + sao]) / 2.;
			overThreshold[i] = ((magSquareData[i] + spectrumAverage[i])
					/ spectrumAverage[i] > detectionThreshold);
			// now just create simple peaks which are over or under threshold
			if (peakOn == PeakStatus.PEAK_OFF) {
				if (overThreshold[i]) {
					newPeak.MinFreq = newPeak.MaxFreq = newPeak.PeakFreq = i;
					newPeak.Signal = newPeak.MaxAmp = spectrumAverage[i];
					newPeak.Noise = spectrumAverage[i];
					newPeak.whistleShape = null;
					newPeak.VetoPeak = false;
					newPeak.timeOffset = 0;
					newPeak.Ok = overThreshold[i];
					peakOn = PeakStatus.PEAK_ON;
					downCount = 0;
				}
			} else if (peakOn == PeakStatus.PEAK_ON) {
				if (!overThreshold[i]) {
					peakOn = PeakStatus.PEAK_OFF;
					peakWidth = newPeak.MaxFreq - newPeak.MinFreq + 1;
					if (peakWidth >= whistleControl.whistleParameters.minPeakWidth
							&& peakWidth <= whistleControl.whistleParameters.maxPeakWidth) {
						peakList.add(newPeak);
						// then immediately generate a new Peak so data in the
						// old one is not overridden.
						newPeak = new WhistlePeak(newDataUnit.getStartSample(),
								newDataUnit.getFftSlice(), newDataUnit.getTimeMilliseconds());
					}
				} else { // continue peak
					newPeak.MaxFreq = i;
					if ((magSquareData[i] = newFftData.magsq(i)) > newPeak.MaxAmp) {
						newPeak.MaxAmp = magSquareData[i];
						newPeak.PeakFreq = i;
					}
					newPeak.Signal += magSquareData[i];
					newPeak.Noise += spectrumAverage[i];
					newPeak.Ok |= overThreshold[i];
				}
			}
		}
//
//		PamDataUnit newPeakUnit = new PamDataUnit(newDataUnit.startSample,
//				newDataUnit.duration, 1 << whistleDetector.channel, peakList);

		PeakDataUnit peakDataUnit = new PeakDataUnit(absSamplesToMilliseconds(newDataUnit.getStartSample()),
				1 << getDetectionChannel(), newDataUnit.getStartSample(), 
				whistleDetector.fftLength, peakList, newDataUnit.getAbsBlockIndex());
		peakDataUnit.sortOutputMaps(newDataUnit.getChannelBitmap(), newDataUnit.getSequenceBitmapObject(), 1 << getDetectionChannel());
		peakDataBlock.addPamData(peakDataUnit);
	}
	
	@Override
	public String getPeakDetectorName() {
		return "Better Peak Detector";
	}

}
