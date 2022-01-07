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

import clickDetector.ClickClassifiers.basic.BasicClickIdentifier;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import PamUtils.PamUtils;
import fftManager.Complex;

/**
 * 
 * @author Doug Gillespie Click objects created by a click detector.
 *         <p>
 *         Contains a snap shot of wave data representing the click (this may or
 *         may not have been filtered). Also contains the basic data included in
 *         clicks created by RainbowClick
 * 
 * 
 * @see clickDetector.ClickDetector
 */
public class OldClickClass {

	long clickNumber;

	long startSample;

	int duration;

	int nChan;

	int channelList;

	int triggerList;

	private double[][] waveData;

	private int delay;

	boolean tracked;

	private double[] amplitude;
	
	private double dBamplitude;

	// these next three are mainly to do with saving in RC type files.
	int flags;

	byte dataType;

	long filePos;

	byte clickType;

	ClickDetector clickDetector;
	
	int eventId;
	
	double ICI = -1; // filled in by click train id stuff.

	// some stuff used many times, so held internally to avoid repeats
	private double[][] powerSpectra;

	private double[] totalPowerSpectrum;

	private Complex[][] complexSpectrum;
	
	private int currentSpectrumLength = 0;

	public OldClickClass(ClickDetector clickDetector, long startSample, int nChan,
			long duration, int channelList, int triggerList) {
		this.clickDetector = clickDetector;
		this.startSample = startSample;
		this.nChan = nChan;
		this.duration = (int) duration;
		this.channelList = channelList;
		this.triggerList = triggerList;
		amplitude = new double[nChan];
		waveData = null;// new double[nChan][this.duration];
		tracked = false;
		flags = 0;
		dataType = 0;
		filePos = 0;
	}

	public boolean isTracked() {
		return tracked;
	}

	public void setTracked(boolean tracked) {
		this.tracked = tracked;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	/**
	 * Gets the angle to a click from the time delay on two hydrophones based on
	 * sound speed.
	 * 
	 * @param sampleRate
	 * @return The angle to the click (ahead = 0 degrees)
	 */
	public double getAngle(float sampleRate) {
		// need to know which hydrophones are being used. 
		// just pass the array the channel bitmap. 
		// first need to converrt the channel map to a hydrophone map
		// and there is no way to do that here !
		int hydrophoneList = ((AcquisitionProcess) clickDetector.getSourceProcess()).
			getAcquisitionControl().ChannelsToHydrophones(channelList);
		double ang = (double) delay / sampleRate
				/ ArrayManager.getArrayManager().getCurrentArray().getSeparationInSeconds(hydrophoneList,0);
		ang = Math.min(1., Math.max(ang, -1.));
		return Math.acos(ang) * 180. / Math.PI;
	}

	/**
	 * Returns the complex spectrum for a given channel using the shortest
	 * possible FFT length
	 * 
	 * @param channel
	 * @return The complex spectrum
	 */
	public Complex[] getComplexSpectrum(int channel) {
		return getComplexSpectrum(channel, PamUtils.getMinFftLength(duration));
	}

	/**
	 * 
	 * Returns the complex spectrum for a given channel using a set FFT length
	 * 
	 * @param channel
	 * @param fftLength
	 * @return the complex spectrum
	 */
	public Complex[] getComplexSpectrum(int channel, int fftLength) {
		double[] paddedRawData;
		double[] rawData;
		int i, mn;
		if (complexSpectrum == null) {
			complexSpectrum = new Complex[nChan][];
		}
		if (complexSpectrum[channel] == null
				|| complexSpectrum.length != fftLength / 2) {
			paddedRawData = new double[fftLength];
			rawData = getWaveData(channel);
			mn = Math.min(fftLength, duration);
			for (i = 0; i < mn; i++) {
				paddedRawData[i] = rawData[i];
			}
			for (i = mn; i < fftLength; i++) {
				paddedRawData[i] = 0;
			}
			complexSpectrum[channel] = clickDetector.fastFFT.rfft(paddedRawData, null,
					PamUtils.log2(fftLength));
			currentSpectrumLength = fftLength;
		}
		return complexSpectrum[channel];
	}

	public int getCurrentSpectrumLength() {
		return currentSpectrumLength;
	}

	/**
	 * Returns the power spectum for a given channel (square of magnitude of
	 * complex spectrum)
	 * 
	 * @param channel
	 * @param fftLength
	 * @return Power spectrum
	 */
	public double[] getPowerSpectrum(int channel, int fftLength) {
		if (powerSpectra == null) {
			powerSpectra = new double[nChan][];
		}
		if (fftLength == 0) {
			fftLength = getCurrentSpectrumLength();
		}
		if (fftLength == 0) {
			fftLength = PamUtils.getMinFftLength(duration);
		}
		if (powerSpectra[channel] == null
				|| powerSpectra[channel].length != fftLength / 2) {
			Complex[] cData = getComplexSpectrum(channel, fftLength);
			powerSpectra[channel] = new double[fftLength / 2];
			for (int i = 0; i < fftLength / 2; i++) {
				powerSpectra[channel][i] = cData[i].magsq();
			}
		}
		return powerSpectra[channel];
	}

	/**
	 * Returns the sum of the power spectra for all channels
	 * 
	 * @param fftLength
	 * @return Sum of power spectra
	 */
	public double[] getTotalPowerSpectrum(int fftLength) {
		if (fftLength == 0) {
			fftLength = getCurrentSpectrumLength();
		}
		if (fftLength == 0) {
			fftLength = PamUtils.getMinFftLength(duration);
		}
		double[] ps;
		if (totalPowerSpectrum == null
				|| totalPowerSpectrum.length != fftLength / 2) {
			totalPowerSpectrum = new double[fftLength / 2];
			for (int c = 0; c < nChan; c++) {
				ps = getPowerSpectrum(c, fftLength);
				for (int i = 0; i < fftLength / 2; i++) {
					totalPowerSpectrum[i] += ps[i];
				}
			}
		}
		return totalPowerSpectrum;
	}

	/**
	 * Calculates the total energy within a particular frequency band
	 * 
	 * @see BasicClickIdentifier
	 * @param freqs
	 * @return In Band Energy
	 */
	public double inBandEnergy(double[] freqs) {
		double e = 0;
		int fftLen = 1024;
		double[][] specData = new double[nChan][];
		for (int i = 0; i < nChan; i++) {
			specData[i] = getPowerSpectrum(i, fftLen);
		}
		int f1 = Math.max(0, (int) Math.floor(freqs[0] * fftLen
				/ clickDetector.getSampleRate()));
		int f2 = Math.min((fftLen / 2) - 1, (int) Math.ceil(freqs[1]
				* fftLen / clickDetector.getSampleRate()));
		for (int iChan = 0; iChan < nChan; iChan++) {
			for (int f = f1; f <= f2; f++) {
				e += specData[iChan][f];
			}
		}
		if (e > 0.) {
			return 10 * Math.log10(e) + 172;
		} else
			return -100;
	}

	/**
	 * Calculates the length of a click in seconds averaged over all channels
	 * 
	 * @see BasicClickIdentifier
	 * @param percent
	 *            Fraction of total click energy to use in the calculation
	 * @return click length in seconds
	 */
	public double clickLength(double percent) {
		/*
		 * work out the length of the click - this first requries a bit of
		 * smoothing out of the rectified waveform, then an iterative search
		 * around either side of the peak, then average for all channels
		 */
		double sum = 0;
		for (int i = 0; i < nChan; i++) {
			sum += clickLength(i, percent);
		}
		return sum / nChan;
	}

	/**
	 * Calculates the length of a click in seconds for a particular channel
	 * 
	 * @see BasicClickIdentifier
	 * @param channel
	 * @param percent
	 *            Fraction of total click energy to use in the calculation
	 * @return Click Length (seconds)
	 */
	public double clickLength(int channel, double percent) {
		int length = 0;
		int nAverage = 3;
		double[] waveData = getWaveData(channel);
		double[] smoothData = new double[waveData.length];
		double squaredData;
		double totalData = 0;
		double dataMaximum = 0;
		int maxPosition = 0;
		for (int i = 0; i < smoothData.length; i++) {
			smoothData[i] = Math.pow(waveData[i], 2);
		}
		for (int i = 0; i < smoothData.length - nAverage; i++) {
			for (int j = 1; j < nAverage; j++) {
				smoothData[i] += smoothData[i + j];
			}
			totalData += smoothData[i];
			if (smoothData[i] > dataMaximum) {
				dataMaximum = smoothData[i];
				maxPosition = i;
			}
		}
		/*
		 * Now start at the maximum position and search out back and forwards
		 * until enough energy has been found use a generic peakwidth function
		 * for this, since it's the same basic process that does the width of
		 * the frequency peak
		 */
		length = getSpikeWidth(smoothData, maxPosition, percent);
		return length / clickDetector.getSampleRate();
	}

	/**
	 * Calculates the width of a peak - either time or frequency data
	 * 
	 * @param data
	 * @param peakPos
	 * @param percent
	 * @return Width of spike in whatever bins are used for raw data given
	 */
	private int getSpikeWidth(double[] data, int peakPos, double percent) {
		/*
		 * This is used both by the length measuring and the frequency peak
		 * measuring functions
		 */
		int width = 1;
		int len = data.length;
		double next, prev;
		int inext, iprev;
		double targetEnergy = 0;
		if (percent > 100) {
			return len;
		}
		for (int i = 0; i < len; i++) {
			targetEnergy += data[i];
		}
		targetEnergy *= percent / 100;
		double foundEnergy = data[peakPos];
		inext = peakPos + 1;
		iprev = peakPos - 1;
		while (foundEnergy < targetEnergy) {
			next = prev = 0;
			if (inext < len)
				next = data[inext];
			if (iprev >= 0)
				prev = data[iprev];
			if (next > prev) {
				foundEnergy += next;
				inext++;
				width++;
			} else if (next < prev) {
				foundEnergy += prev;
				iprev--;
				width++;
			} else {
				foundEnergy += (next + prev);
				inext++;
				iprev--;
				width += 2;
			}
			if (iprev < 0 && inext >= len) {
				System.out.println("Can't find required energy in click");
			}
		}

		return width;
	}

	public double peakFrequency(double[] searchRange) {
		/*
		 * search range will be in Hz, so convert to bins NB - the
		 */
		int fftLength = 1024;
		double[] powerSpec = getTotalPowerSpectrum(fftLength);

		int bin1 = (int) Math.max(0, Math.floor(searchRange[0] * fftLength
				/ clickDetector.getSampleRate()));
		int bin2 = (int) Math.min(fftLength / 2 - 1, Math.ceil(searchRange[1]
				* fftLength / clickDetector.getSampleRate()));
		int peakPos = 0;
		double peakEnergy = 0;
		for (int i = bin1; i <= bin2; i++) {
			if (powerSpec[i] > peakEnergy) {
				peakEnergy = powerSpec[i];
				peakPos = i;
			}
		}
		return peakPos * clickDetector.getSampleRate() / fftLength;
	}

	public double peakFrequencyWidth(double peakFrequency, double percent) {
		int fftLength = 1024;
		int peakPos = (int) (peakFrequency * fftLength / clickDetector
				.getSampleRate());
		int width = getSpikeWidth(getTotalPowerSpectrum(fftLength), peakPos,
				percent);
		return width * clickDetector.getSampleRate() / fftLength;
	}

	public double getAmplitude(int channel) {
		return amplitude[channel];
	}

	public void setAmplitude(int channel, double amplitude) {
		this.amplitude[channel] = amplitude;
	}

	public double[] getWaveData(int channel) {
		if (waveData == null)
			return null;
		return waveData[channel];
	}

	public double[][] getWaveData() {
		return waveData;
	}

	public void setWaveData(double[][] waveData) {
		this.waveData = waveData;
	}

	public void freeClickMemory() {
		waveData = null;
		powerSpectra = null;
		complexSpectrum = null;
		totalPowerSpectrum = null;
	}

	public double getMeanAmplitude()
	{
		double a = 0;
		for (int i = 0; i < amplitude.length; i++) {
			a += amplitude[i];
		}
		return a / amplitude.length;
	}
	/**
	 * @return Returns the dBamplitude.
	 */
	public double getDBamplitude() {
		return dBamplitude;
	}

	/**
	 * @param bamplitude The dBamplitude to set.
	 */
	public void setDBamplitude(double bamplitude) {
		dBamplitude = bamplitude;
	}

	/**
	 * @return Returns the delay.
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay The delay to set.
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getNChan() {
		return nChan;
	}

}
