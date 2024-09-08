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

import java.util.ListIterator;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 *
 * @author Michael Oswald
 */
public class RoccaContour {

    FFTDataBlock fftDataBlockIn;
    PamRawDataBlock rawDataBlockIn;
    RoccaProcess roccaProcess;
    RoccaParameters roccaParameters;
    RoccaControl roccaControl;
    RoccaContourDataBlock oldDataBlock = null;
    double senseLow;
    double senseHigh;
    int energyBinSize;
    
    /**
     * a list containing only the input channel the whistle contour was
     * taken from (a list is required in order to create a channel map specific
     * to that channel)
     */
    int[] channelListToUse = new int[1];

    /**
     * boolean indicating whether or not the user has manually changed the contour
     */
    boolean contourChanged = false;

    /**
     * frequency of the lowpass filter used for peak freq searches
     */
    double lowPass = Double.MAX_VALUE;

    /**
     * frequency of the highpass filter used for peak freq searches
     */
    double highPass = 0;

    /**
     * parameter
     */
    public static final double OUTOFRANGE = -1.0;

    /**
     * Basic constructor
     * @param roccaProcess
     */
    public RoccaContour (RoccaProcess roccaProcess) {
        this.roccaProcess = roccaProcess;
    }

    public RoccaContourDataBlock generateContour
            (FFTDataBlock fftDataBlockIn,
            long contourStartTime,
            double startFreq,
            long contourEndTime,
            double endFreq,
            PamRawDataBlock rawDataBlockIn,
            int channelToUse,
            RoccaParameters roccaParameters) {

        // set the data block field
        this.fftDataBlockIn = fftDataBlockIn;
        this.rawDataBlockIn = rawDataBlockIn;
        this.channelListToUse[0] = channelToUse;
        int channelMap = PamUtils.makeChannelMap(channelListToUse);
        this.roccaParameters = roccaParameters;

        /* set the prevPeakFreq parameter */
        double prevPeakFreq = startFreq;

        // noise sensitivity and frequency bin size for energy calcs
        senseLow = 1 - roccaParameters.getNoiseSensitivity()/100;
        senseHigh = 1 + roccaParameters.getNoiseSensitivity()/100;
        energyBinSize = roccaParameters.getEnergyBinSize()/2;

        // define a new RoccaContourDataBlock and RoccaContourDataUnit
        RoccaContourDataBlock roccaContourDataBlock =
                new RoccaContourDataBlock(roccaProcess,
                channelMap);
        RoccaContourDataUnit rcdu = null;
        
        // reference to the raw acoustic data the FFT was constructed from
        if (rawDataBlockIn == null) {
            System.out.println("RoccaContour: No source audio data found");
            return null;
        }

        // set static fields
        float rawDataSamplingRate = fftDataBlockIn.getSampleRate();
        int fftLength = fftDataBlockIn.getFftLength();
        int fftHop = fftDataBlockIn.getFftHop();
        double freqBinSize = rawDataSamplingRate / fftLength; // Hz/point
        int energyBinDelta = (int) (energyBinSize / freqBinSize);

        // set parameters

        // synchronize on the FFT data block input to prevent anyone else from accessing
        // while we generate the contour
        synchronized(fftDataBlockIn) {

            // get the ListIterator to step through fftDataBlockIn
            ListIterator<FFTDataUnit> fftDataBlockInIterator = fftDataBlockIn.getListIterator(0);

            /* if the user has changed the contour manually, load up a list
             * iterator for the oldDataBlock as well
             */
            RoccaContourDataUnit oldContourPoint = null;
            ListIterator<RoccaContourDataUnit> oldDataIterator = null;
            if (contourChanged) {
                oldDataIterator = oldDataBlock.getListIterator(0);
                oldContourPoint = oldDataIterator.next();
            }

            // fields for current data window
            FFTDataUnit currentFFTDataUnit;
            long currentMeanTime = 0;
            double[][] currentRawData;
            ComplexArray currentFFT;
            double currentDutyCycle;
            double currentPeakFreq;
            double currentEnergy;
            double currentWindowRMS;
            double prevWindowRMS;
            long currentStartSample;

            // fields for next data window
            FFTDataUnit nextFFTDataUnit;
            double[][] nextRawData;
            ComplexArray nextFFT;
            double nextDutyCycle;
            double nextWindowRMS;
            long nextFFTStartTime;
            long nextDeltaTime;
            float nextApproxStartSample;
            long nextStartSample;

            // load up current data window data
            currentFFTDataUnit = fftDataBlockInIterator.next();

            /* calculate the starting sample of the raw data, based on the start
             * time of the FFTDataUnit
             */
            long fftStartTime = currentFFTDataUnit.getTimeMilliseconds();
            long rawStartTime = rawDataBlockIn.getFirstUnit().getTimeMilliseconds();
            long rawStartSample = rawDataBlockIn.getFirstUnit().getStartSample();
            long deltaTime = fftStartTime - rawStartTime;
            float approxStartSample = rawStartSample +
                    deltaTime*fftDataBlockIn.getSampleRate()/1000;    // because deltaTime is ms, and SampleRate in s
            currentStartSample = (long) approxStartSample;
//            currentStartSample = currentFFTDataUnit.getStartSample();
            //System.out.println(String.format("currentStartSample = %d", currentStartSample));
            try {
            	currentRawData = rawDataBlockIn.getSamples
            	(currentStartSample, fftLength, channelMap );
            }
            catch (RawDataUnavailableException e) {
            	System.out.println("RawDataUnavailableException in ROCCAContour.generateContour: " + e.getMessage());	
            	currentRawData = null;
            }
            /* every now and then the FFTDataUnit start sample doesn't match up
             * with the rawDataUnit start sample.  If this is the case, just
             * return a null so that the whole program doesn't lock up
             */
            if (currentRawData==null) {
                System.out.println(String.format
                        ("Error obtaining first raw data; currentStartSample = %d",
                        currentStartSample));
                return null;
            }
            currentFFT = currentFFTDataUnit.getFftData();
            currentDutyCycle = getDutyCycleFromRaw(currentRawData);
            prevWindowRMS = getRMSFromRaw(currentRawData);

            /*
            System.out.println(String.format("Number of units in FFT Data Block is %d", fftDataBlockIn.getUnitsCount()));
            if (currentRawData == null){
                System.out.println("RoccaContour: Not able to retrieve source audio window");
            } else {
                System.out.println(String.format("Number of units in raw data block %d ***", rawDataBlockIn.getUnitsCount()));
                System.out.println(String.format("Raw Data row size = %d", currentRawData.length));
                System.out.println(String.format("Raw Data col size = %d", currentRawData[0].length));
            }

            for (int i = 0; i < currentFFT.length; i++) {
                System.out.println(String.format("   Real, Imag = %f %f", currentFFT[i].real, currentFFT[i].imag));
            }
             */

            while (fftDataBlockInIterator.hasNext()) {

                // load up next data window data
                nextFFTDataUnit = fftDataBlockInIterator.next();
                nextFFTStartTime = currentFFTDataUnit.getTimeMilliseconds();
                nextDeltaTime = nextFFTStartTime - rawStartTime;
                nextApproxStartSample = rawStartSample +
                        nextDeltaTime*fftDataBlockIn.getSampleRate()/1000;    // because deltaTime is ms, and SampleRate in s
                nextStartSample = (long) nextApproxStartSample;
                try {
                	nextRawData = rawDataBlockIn.getSamples(nextStartSample, fftLength, channelMap );
                }
                catch (RawDataUnavailableException e) {
                	System.out.println("RawDataUnavailableException (2) in ROCCAContour.generateContour: " + e.getMessage());
                	nextRawData = null;
                }
                nextFFT = nextFFTDataUnit.getFftData();
                nextDutyCycle = getDutyCycleFromRaw(nextRawData);

//                System.out.println(String.format("Slice, Max Amp, DC = %d, %f, %f" , currentFFTDataUnit.getFftSlice(), currentMaxAmp, currentDutyCycle));
//                System.out.println(String.format("FFT Data Unit Slice = %d", currentFFTDataUnit.getFftSlice()));
//                System.out.println(String.format("   Duty Cycle = %f", currentDutyCycle));
//                System.out.println(String.format("   Raw Data size = %d", currentRawData[0].length));

                /* if we haven't reached the starting point of the whistle yet,
                 * or we've gone past the ending point of the whistle,
                 * set the frequency to OUTOFRANGE and jump to the next point
                 */
                if ((nextFFTStartTime < contourStartTime) ||
                            nextFFTStartTime > contourEndTime) {
                    rcdu = new RoccaContourDataUnit (
                    currentFFTDataUnit.getTimeMilliseconds(),
//                    fftDataBlockIn.getChannelMap(),
                    fftDataBlockIn.getSequenceMap(),
                    currentFFTDataUnit.getStartSample(),
                    currentFFTDataUnit.getSampleDuration() );

                    rcdu.setDutyCycle(0.0);
                    rcdu.setEnergy(0.0);
                    rcdu.setTime(nextFFTStartTime);
                    rcdu.setPeakFreq(OUTOFRANGE);
                    rcdu.setWindowRMS(0.0);
                    roccaContourDataBlock.addPamData(rcdu);

                /* if the contour has been changed by the user, then check if
                 * the next contour point has been affected.  If yes, then simply
                 * add the old dataUnit to the new dataBlock and continue on
                 * to the next point
                 */
//                } else if (contourChanged && oldContourPoint.hasUserChanged()) {
//                    roccaContourDataBlock.addPamData(oldContourPoint);
//                    prevPeakFreq = oldContourPoint.getPeakFreq();
//                    prevWindowRMS = oldContourPoint.getWindowRMS();                    

                } else {

                    /* if we're looking at the starting point, just save
                     * the value and don't try to figure out the peak frequency
                     */
                    int currentPeakFreqIndex = 0;
                    if (nextFFTStartTime==contourStartTime) {
                        currentPeakFreq = startFreq;
                        currentPeakFreqIndex = (int) (currentPeakFreq/freqBinSize);
                        currentMeanTime = contourStartTime;

                    /* if we're looking at the ending frequency, just save
                     * the value and don't try to figure out the peak frequency
                     */
                    } else if (nextFFTStartTime==contourEndTime) {
                        currentPeakFreq = endFreq;
                        currentPeakFreqIndex = (int) (currentPeakFreq/freqBinSize);
                        currentMeanTime = contourEndTime;

                    /* if we're here, it means we're somewhere in the middle of
                     * the contour
                     */
                    } else {
                    	
                    	// if the user has changed this point, set the peak freq and don't try to calculate it
                    	if (contourChanged && oldContourPoint.hasUserChanged()) {
                    		currentPeakFreq = oldContourPoint.getPeakFreq();
                            currentPeakFreqIndex = (int) (currentPeakFreq/freqBinSize);
                            currentMeanTime = oldContourPoint.getTime();
                    	}

                        // find the peak frequency within senseLow/senseHigh of the previous peak frequency
                    	else {
                    		double lowerFreqBin = Math.max(prevPeakFreq*senseLow, highPass);
                    		double upperFreqBin = Math.min(prevPeakFreq*senseHigh, lowPass);
                    		int lowerFreqBinIndex = (int) (lowerFreqBin / freqBinSize);
                    		int upperFreqBinIndex = (int) (upperFreqBin / freqBinSize);
                    		currentPeakFreqIndex = getMaxFreqIndexFromFFTData(currentFFT, lowerFreqBinIndex, upperFreqBinIndex);
                    		//               System.out.println(String.format("   prevPeakFreq = %f", prevPeakFreq));
                    		//               System.out.println(String.format("   Lower Index = %d", lowerFreqBinIndex));
                    		//               System.out.println(String.format("   Higher Index = %d", upperFreqBinIndex));
                    		currentPeakFreq = currentPeakFreqIndex * freqBinSize;
                    		
                            // ensure random transient peaks in spectrum are not mistaken for the fundamental peak frequency
                            if (currentPeakFreq * senseHigh <= currentFFT.length() * freqBinSize ) {
                                if ( (currentPeakFreq < prevPeakFreq * senseLow) ||
                                     (currentPeakFreq > prevPeakFreq * senseHigh )) {
                                    currentPeakFreq = prevPeakFreq;
                                }
                            } else {
                            	currentPeakFreq = rawDataSamplingRate/2;
                            }
                            currentPeakFreqIndex = (int) (currentPeakFreq/freqBinSize); // set the index here again, just in case currentPeakFreq got changed
                            
                            // get the time of the data unit and convert from milliseconds to seconds
                            currentMeanTime = currentFFTDataUnit.getTimeMilliseconds();

                    	}
                    }

                    // calculate the energy spectrum in a bin +/- energyBinSize around currentPeakFreq
                    int lowerFreqBinIndex = currentPeakFreqIndex - energyBinDelta;
                    int upperFreqBinIndex = currentPeakFreqIndex + energyBinDelta;
                    currentEnergy = energySum(currentFFT, lowerFreqBinIndex, upperFreqBinIndex);
//                    System.out.println(String.format("time, lower, upper, energy = %d, %d, %d, %f", currentMeanTime, lowerFreqBinIndex, upperFreqBinIndex, currentEnergy));
                    //System.out.println(String.format(" Freq, Magic = %f, %f", currentPeakFreq, currentEnergy));

                    // calculate the RMS of the current raw data unit and compare to previous and next
                    // units; if current window is more than 20% larger than either previous or next,
                    // set it to the average of the two
                    currentWindowRMS = getRMSFromRaw(currentRawData);
                    nextWindowRMS = getRMSFromRaw(nextRawData);
                    if ( (currentWindowRMS > 1.2 * prevWindowRMS) ||
                         (currentWindowRMS > 1.2 * nextWindowRMS )) {
                        currentWindowRMS = (prevWindowRMS+nextWindowRMS)/2;
                    }

                    // create a new RoccaContourDataUnit, save data and add to RoccaContourDataBlock
                    rcdu = new RoccaContourDataUnit (
                    currentFFTDataUnit.getTimeMilliseconds(),
//                    fftDataBlockIn.getChannelMap(),
                    fftDataBlockIn.getSequenceMap(),
                    currentFFTDataUnit.getStartSample(),
                    currentFFTDataUnit.getSampleDuration() );

                    rcdu.setDutyCycle(currentDutyCycle);
                    rcdu.setEnergy(currentEnergy);
                    rcdu.setTime(currentMeanTime);
                   // System.out.println("RCDU Time = " + currentMeanTime);
                    rcdu.setPeakFreq(currentPeakFreq);
                    rcdu.setWindowRMS(currentWindowRMS);
                    roccaContourDataBlock.addPamData(rcdu);


                    // move next data window to current
                    prevPeakFreq = currentPeakFreq;
                    prevWindowRMS = currentWindowRMS;
                } // end the contourChanged statement

                /* move the next set of data into the current position */
                currentFFTDataUnit = nextFFTDataUnit;
                currentFFT = nextFFT;
                currentRawData = nextRawData;
                currentDutyCycle = nextDutyCycle;

                /* if the contour has been modified by the user, grab the next
                 * oldData point.  Note that because of the way the loops work,
                 * there will be one less contour point than FFT point.
                 */
                if (contourChanged && oldDataIterator.hasNext()) {
                    oldContourPoint = oldDataIterator.next();
                }
            } // end while loop
        } // end synocronization

        return roccaContourDataBlock;
    }

    public RoccaContourDataBlock regenerateContour
            (RoccaContourDataBlock oldDataBlock,
            FFTDataBlock fftDataBlockIn,
            long contourStartTime,
            double prevPeakFreq,
            long contourEndTime,
            double endFreq,
            PamRawDataBlock rawDataBlockIn,
            int channelToUse,
            RoccaParameters roccaParameters) {

        /* define the original data block so that we can watch for any points
         * that have changed
         */
        this.oldDataBlock = oldDataBlock;
        return generateContour
            (fftDataBlockIn,
            contourStartTime,
            prevPeakFreq,
            contourEndTime,
            endFreq,
            rawDataBlockIn,
            channelToUse,
            roccaParameters);
    }

    /**
     * Calculate the duty cycle from the raw data
     *
     * @param rawData a 2D double array containing the raw data.  Note that the
     * raw data should have been taken from a single channel, the channel
     * from which the contour was selected.  Thus, the first dimension of the
     * array will always be [0].
     * @return the duty cycle
     */
    private double getDutyCycleFromRaw(double[][] rawData) {
        double dutyCycle;
        double maxAmplitude = Math.abs(rawData[0][0]);
        double sumAmplitude = 0.0;

        for (int i = 0; i < rawData[0].length; i++) {
            sumAmplitude+=Math.abs(rawData[0][i]);
            if (Math.abs(rawData[0][i]) > maxAmplitude) {
                maxAmplitude = Math.abs(rawData[0][i]);
            }
        }
        dutyCycle = sumAmplitude / maxAmplitude / rawData[0].length / .636;
        return dutyCycle;
    }

    private int getMaxFreqIndexFromFFTData(ComplexArray currentFFT, int lowerFreqBinIndex, int upperFreqBinIndex) {
        int maxFreqIndex = lowerFreqBinIndex;
        double maxFFT = currentFFT.getReal(lowerFreqBinIndex);
        if (upperFreqBinIndex >= currentFFT.length()) {	// serialVersionUID=22 2015/06/13 changed from > to >=
            upperFreqBinIndex = currentFFT.length()-1;	// serialVersionUID=22 2015/06/13 added -1
        }
        for (int i = lowerFreqBinIndex+1; i <= upperFreqBinIndex; i++) {
            if (currentFFT.getReal(i) > maxFFT) {
                maxFFT = currentFFT.getReal(i);
                maxFreqIndex = i;
            }
        }
        return maxFreqIndex;
    }

    private double energySum(ComplexArray currentFFT, int lowerFreqBinIndex, int upperFreqBinIndex) {
        double e = 0;
        for (int i = lowerFreqBinIndex; i <= upperFreqBinIndex; i++) {
            e += currentFFT.magsq(i);
        }
        return e;
    }

    /**
     * Calculate the rms value from the raw data
     *
     * @param rawData a 2D double array containing the raw data.  Note that the
     * raw data should have been taken from a single channel, the channel
     * from which the contour was selected.  Thus, the first dimension of the
     * array will always be [0].
     * @return the rms value
     */
    private double getRMSFromRaw(double[][] rawData) {
        double rms;
        double sumOfWindow = 0.0;

        for (int i = 0; i < rawData[0].length; i++) {
            sumOfWindow+=rawData[0][i] * rawData[0][i];
        }
        rms = Math.sqrt(sumOfWindow/rawData[0].length);
        return rms;
    }


    public boolean isContourChanged() {
        return contourChanged;
    }

    public void setContourChanged(boolean contourChanged) {
        this.contourChanged = contourChanged;
    }

    public void clearOldDataBlock() {
        this.oldDataBlock = null;
    }

    public void setFilters(double high, double low) {
        this.highPass = high;
        this.lowPass = low;
    }
}
