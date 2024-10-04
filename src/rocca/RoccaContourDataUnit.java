/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
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


package rocca;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

/**
 *
 * @author Michael Oswald
 */
public class RoccaContourDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection {

    long timeInMilliseconds = 0;                  // time (in milliseconds) of the middle of the FFT data unit
    double dutyCycle = 0.0;                 // duty cycle
    double peakFreq = 0.0;                  // peak frequency
    double energy = 0.0;                    // energy in the frequency band around peak freq
    double windowRMS = 0.0;                 // RMS of the entire window
    int sweep = 0;                          // upsweep=1, downsweep=-1, horiz=0
    int step = 0;                           // step up=1, step down=2, no step=0
    double slope = 0.0;                     // slope of current unit, compared to prev
    boolean userChanged = false;            // whether or not the user has changed the point

    
	public RoccaContourDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

    public double getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getMeanTime() {
        return timeInMilliseconds/1000;
    }

    public long getTime() {
        return timeInMilliseconds;
    }
    public void setTime(long time) {
        this.timeInMilliseconds = time;
    }

    public double getPeakFreq() {
        return peakFreq;
    }

    public void setPeakFreq(double peakFreq) {
        this.peakFreq = peakFreq;
    }

    public double getWindowRMS() {
        return windowRMS;
    }

    public void setWindowRMS(double windowRMS) {
        this.windowRMS = windowRMS;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getSweep() {
        return sweep;
    }

    public void setSweep(int sweep) {
        this.sweep = sweep;
    }

    public boolean hasUserChanged() {
        return userChanged;
    }

    public void setUserChanged(boolean userChanged) {
        this.userChanged = userChanged;
    }



}
