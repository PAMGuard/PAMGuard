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

import java.awt.event.MouseEvent;

import PamController.PamController;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkObserver;
import Spectrogram.SpectrogramMarkObservers;
import dataPlotsFX.layout.TDGraphFX;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import warnings.PamWarning;
import warnings.WarningSystem;


public class RoccaWhistleSelect extends PamProcess implements SpectrogramMarkObserver {

    RoccaControl roccaControl;
    boolean startSelected = false;
    boolean endSelected = false;
    double startFreq = 0.0;
    double endFreq = 0.0;
    long startTime = 0;
    long endTime = 0;
	private static PamWarning roccaWarning = new PamWarning("Rocca", "", 2);

	public RoccaWhistleSelect(RoccaControl roccaControl) {
		super(roccaControl, null);
        this.roccaControl = roccaControl;
		SpectrogramMarkObservers.addSpectrogramMarkObserver(this);
	}

	@Override
	public void destroyProcess() {
		SpectrogramMarkObservers.removeSpectrogramMarkObserver(this);
		super.destroyProcess();
	}
	
	/**
	 * Original spectrogramNotification method.
	 * 
	 * @param display
	 * @param downUp
	 * @param channel
	 * @param startMilliseconds
	 * @param duration
	 * @param f1
	 * @param f2
	 */
	public void spectrogramNotification(SpectrogramDisplay display, int downUp, int channel,
			long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay) {
        
        if (downUp == MOUSE_DOWN) {
    		// do a quick check here of the source.  If the fft has sequence numbers, the channels are ambiguous and Rocca can't use it.  warn the user and exit
        	
    		FFTDataBlock source = null;
        	if (display!=null) {
        		source = display.getSourceFFTDataBlock();
        	} else if (tdDisplay != null) {
        		source = tdDisplay.getFFTDataBlock();
        	}
    		if (source!=null && source.getSequenceMapObject()!=null) {
    			String err = "Error: this Spectrogram uses Beamformer data as it's source, and Beamformer output does not contain "
    			+ "the link back to a single channel of raw audio data that Rocca requires for analysis.  You will not be able to select whistles "
    			+ "until the source is changed";
    			roccaWarning.setWarningMessage(err);
    			WarningSystem.getWarningSystem().addWarning(roccaWarning);
    			return;
    		}

            startSelected = true;
            endSelected = false;
            startFreq = f1;
            startTime = startMilliseconds;
            roccaControl.roccaSidePanel.setStartOfWhistle(startTime, startFreq);
//            System.out.println(String.format(
//                    "Mouse Down - Starting frequency is %3.1f at %d ms.  Duration is %d ms.  f2 is %3.1f", startFreq, startTime, duration, f2));


        } else if (downUp == MOUSE_UP && duration != 0) {
            endSelected = true;
            startFreq = f1;
            endFreq = f2;
            
            /* if the user has selected the whistle from right-to-left, reset
             * the startTime to the earlier value
             */
            if (startMilliseconds<startTime) {
                startTime  = startMilliseconds;
            }
            endTime = startMilliseconds+duration;
            roccaControl.roccaSidePanel.setStartOfWhistle(startTime, startFreq);
            roccaControl.roccaSidePanel.setEndOfWhistle(endTime, endFreq);
//            System.out.println(String.format(
//                    "Mouse Up - Starting frequency is %3.1f at %d ms.  Duration is %d ms.  f2 is %3.1f", startFreq, startTime, duration, f2));

            // set the parentDataBlock for this process to the raw acoustic data
            FFTDataBlock fullFFTDataBlock = null;
            PamRawDataBlock rawData = null;
            if (display!=null) {
            	fullFFTDataBlock = display.getSourceFFTDataBlock();
            	rawData = display.getSourceRawDataBlock();
            	
            }
            else if (tdDisplay!=null) {
            	fullFFTDataBlock = tdDisplay.getFFTDataBlock();
            	rawData = fullFFTDataBlock.getFirstRawSourceDataBlock();
            }
            if (fullFFTDataBlock==null || rawData==null) return;
            setParentDataBlock(rawData);

            /* create a new FFTDataBlock with just the data units between
             * the start and end times.  Set the natural lifetime to
             * as large a number as possible so that the FFTDataUnits will
             * not be recycled until the user closes the RoccaSpecPopUp.
             * Note that the .setNaturalLifetime method assumes the passed
             * integer is in seconds, so multiplies by 1000 to convert to
             * milliseconds.  Thus, to keep the largest number possible, it
             * is first divided by 1000 and then passed.
             */
            FFTDataBlock selectedWhistle = getDataBlockSubset(fullFFTDataBlock, channel);
            if (selectedWhistle==null) {
            	return;
            }
            selectedWhistle.setNaturalLifetimeMillis(Integer.MAX_VALUE);

            /* create a new PamRawDataBlock containing only the data from
             * the selectedWhistle.  Set the natural lifetime to
             * as large a number as possible so that the FFTDataUnits will
             * not be recycled until the user closes the RoccaSpecPopUp.
             * Note that the .setNaturalLifetime method assumes the passed
             * integer is in seconds, so multiplies by 1000 to convert to
             * milliseconds.  Thus, to keep the largest number possible, it
             * is first divided by 1000 and then passed.
             */
            PamRawDataBlock selectedWhistleRaw = getRawData(selectedWhistle);
            selectedWhistleRaw.setNaturalLifetimeMillis(Integer.MAX_VALUE);

            /* if the user hasn't added a sighting yet, do that now */
            if (roccaControl.roccaSidePanel.getSightingNum().equals(
                    RoccaSightingDataUnit.NONE)) {
                String dummy =
                        roccaControl.roccaSidePanel.sidePanel.addASighting(false);
                // if the user hit cancel, just exit
                if (dummy == RoccaSightingDataUnit.NONE) {
                    selectedWhistle.setNaturalLifetimeMillis(0);
                    selectedWhistleRaw.setNaturalLifetimeMillis(0);
                	return;
                }
            }
            
            // 2019/11/25
            // if the current sighting has been loaded from a file, it doesn't have any of the underlying data necessary to
            // recalculate the ancillary variables.  We shouldn't be letting the user add new whistles/clicks to this sighting,
            // so force them to start a new one
            if (roccaControl.roccaSidePanel.currentUnit.isSightingDataLost()) {
    			String title = "New Encounter Required";
    			String msg = "<html><p>This Encounter has been loaded from an existing Encounter Stats file.  As such, it does not contain the" + 
    					"underlying detection information necessary to recalculate ancillary variables (e.g. time between detections, detection density, " +
    					"detections/second, etc).  You will need to start a new Encounter to ensure all variables are calculated correctly.</p></html>";
    			String help = null;
    			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
                String dummy = roccaControl.roccaSidePanel.sidePanel.addASighting(false);
            }

            /* open the selected whistle in a new pop-up spectrogram */
            RoccaSpecPopUp roccaSpecPopUp = new RoccaSpecPopUp
                    (roccaControl.getRoccaProcess(),
                    selectedWhistle,
                    startFreq,
                    endFreq,
                    selectedWhistleRaw,
                    display,
                    channel);

            /**
             * DG June '22.
             * I hope IT's K to set these back here. It's possible it's also 
             * done elsewhere.  
             * 
             * MO Jan '23
             * These are set to 0 in the RoccaSpecPopUp class, before the user closes the window.  If set here, the objects are cleared before the user can trace the whistle and throws an exception
             */
//            selectedWhistle.setNaturalLifetimeMillis(0);
//            selectedWhistleRaw.setNaturalLifetimeMillis(0);
        }
	}

	/**
	 * Updated spectrogramNotification method.  Use as an overloaded method, passing
	 * variables back to original method
	 */
	@Override
	public boolean spectrogramNotification(SpectrogramDisplay display,
			MouseEvent mouseEvent, int downUp, int channel,
			long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay) {
		spectrogramNotification(display, downUp, channel,
				startMilliseconds, duration, f1, f2, tdDisplay);
		return false;
	}
        
    /**
     * Create a subset of the FFTDataBlock being displayed, based on the
     * starting and ending times selected by the user
     *
     * @param display The spectrogram display object
     * @param channel The channel the whistle was selected from
     * @return a new FFTDataBlock containing only the FFTDataUnits between the
     * start and end times, for the selected channel
     */
    public FFTDataBlock getDataBlockSubset(FFTDataBlock fullFFTDataBlock, int channel) {

//        FFTDataBlock fullFFTDataBlock = display.getSourceFFTDataBlock();
        int[] channelList = new int[1];
        channelList[0] = channel;
        int channelBitmap = PamUtils.makeChannelMap(channelList);
        FFTDataBlock subFFTDataBlock = new FFTDataBlock(
                roccaControl.roccaSidePanel.getSightingNum(),
                this,
                channelBitmap,
                fullFFTDataBlock.getFftHop(),
                fullFFTDataBlock.getFftLength());

        /* find the index number of the FFTDataUnit closest to the start time
         * selected by the user.  If the closest FFTDataUnit is for the wrong
         * channel, step backwards through the list until we find one with the
         * right channel
         */
        int firstIndx = fullFFTDataBlock.getUnitIndex(
                fullFFTDataBlock.getPreceedingUnit(startTime, channelBitmap));
//        int firstIndx = fullFFTDataBlock.getUnitIndex(
//                fullFFTDataBlock.getClosestUnitMillis(startTime));
//        while (PamUtils.getSingleChannel(
//                fullFFTDataBlock.getDataUnit(firstIndx, FFTDataBlock.REFERENCE_CURRENT)
//                .getChannelBitmap()) != channel) {
//            firstIndx--;
//        }

        /* find the index number of the FFTDataUnit closest to the end time
         * selected by the user, in the channel desired.  Once we have the
         * preceeding unit, step forward one unit to make sure we have enough
         * data (we actually want the next unit, not the preceeding unit)
         */
        int lastIndx = fullFFTDataBlock.getUnitIndex(
                fullFFTDataBlock.getNextUnit(endTime, channelBitmap));
//        int lastIndx = fullFFTDataBlock.getUnitIndex(
//                fullFFTDataBlock.getClosestUnitMillis(endTime));
//        while (PamUtils.getSingleChannel(
//                fullFFTDataBlock.getDataUnit(lastIndx, FFTDataBlock.REFERENCE_CURRENT)
//                .getChannelBitmap()) != channel) {
//            lastIndx++;
//        }
        if (firstIndx==-1 || lastIndx==-1) {
        	return null;
        }


        /* Now step through the FFTDataUnits, from firstIndx to lastIndx, adding
         * each data unit from the correct channel to the new FFTDataBlock
         */
        FFTDataUnit unit;
        for (int i = firstIndx; i <= lastIndx; i++) {
            unit = fullFFTDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT );
            if (unit.getChannelBitmap() == channelBitmap) {
                subFFTDataBlock.addPamData(unit);
            }
        }
        return subFFTDataBlock;
    }

    public PamRawDataBlock getRawData(FFTDataBlock fftDataBlock) {

        PamRawDataBlock prdb = fftDataBlock.getRawSourceDataBlock2();
        PamRawDataBlock newBlock = new PamRawDataBlock(
                prdb.getDataName(),
                this,
                roccaControl.roccaParameters.getChannelMap(),
                prdb.getSampleRate(),
                false);

        /* find the index number of the PamRawDataUnit closest to the start time
         * of the first unit in the FFTDataBlock, and in the lowest channel
         * position to be saved.  Use the .getPreceedingUnit method to ensure
         * that the start time of the raw data is earlier than the start time
         * of the FFT data (otherwise we'll crash later in RoccaContour)
         */
        int[] lowestChanList = new int[1];
        lowestChanList[0] =
                PamUtils.getLowestChannel(roccaControl.roccaParameters.channelMap);
        int firstIndx = prdb.getUnitIndex(prdb.getPreceedingUnit(
                fftDataBlock.getFirstUnit().getTimeMilliseconds(),
                PamUtils.makeChannelMap(lowestChanList)));

        /* check to make sure firstIndx is a real number - sometimes the first Raw data Unit is at the same time
         * as the first FFT data unit, so there is no 'previous' unit and firstIndx gets returned as -1.  In such a case, set firstIndx to
         * point to the first PamRawDataBlock
         */
        if (firstIndx == -1) {
        	firstIndx = 0;
        }

        /* find the index number of the PamRawDataUnit closest to the start time
         * of the last unit in the FFTDataBlock, and in the highest channel
         * position to be saved.  Use the .getNextUnit method to ensure
         * that the start time of the raw data is later than the start time
         * of the FFT data (otherwise we'll crash later in RoccaContour)
         */
        int[] highestChanList = new int[1];
        highestChanList[0] =
                PamUtils.getHighestChannel(roccaControl.roccaParameters.channelMap);
        int lastIndx = prdb.getUnitIndex(prdb.getNextUnit(
                fftDataBlock.getLastUnit().getTimeMilliseconds(),
                PamUtils.makeChannelMap(highestChanList)));
        
        /* check to make sure lastIndx is a real number - sometimes the start time of the last FFTDataBlock is > the start
         * time of the last PamRawDataBlock, which causes lastIndx to be set to -1.  In such a case, set the lastIndx to
         * point to the last PamRawDataBlock
         */
        if (lastIndx == -1) {
        	lastIndx = prdb.getUnitsCount()-1;
        }

        /* add the units, from firstIndx to lastIndx, to the new PamRawDataBlock */
        for (int i = firstIndx; i <= lastIndx; i++) {
            newBlock.addPamData
                    (prdb.getDataUnit
                    (i, PamDataBlock.REFERENCE_CURRENT ));
        }
        return newBlock;
    }

    @Override
	public String getMarkObserverName() {
		return getProcessName();
	}

    @Override
	public void pamStart() {}

	@Override
	public void pamStop() {}

	/* (non-Javadoc)
	 * @see Spectrogram.SpectrogramMarkObserver#canMark()
	 */
	@Override
	public boolean canMark() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}

}
