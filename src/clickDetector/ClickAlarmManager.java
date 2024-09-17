/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

import java.io.File;

import javax.sound.sampled.Clip;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.ClickClassifiers.ClickTypeCommonParams;

/**
 *
 * @author Michael Oswald
 */
public class ClickAlarmManager extends PamProcess {

    /** reference to clickControl */
    ClickControl clickControl;

    /** Boolean to indicate whether or not sound file has been loaded */
    boolean fileIsLoaded;
    
    /** wav clip to be played */
    Clip clickClip;

    /** wav file to be played */
    File clipFile;

    /** the time (in milliseconds) of the previous detection */
    long prevTime = 0;

    /**
     * Main Constructor
     * <p>
     * Subscribe to the Click Datablock to monitor when clicks are found
     *
     * @param clickDetector     ClickDetector process
     * 
     */
    public ClickAlarmManager(ClickControl clickControl,
            PamDataBlock<ClickDetection> clickDataBlock) {
        super(clickControl, clickDataBlock);
        this.clickControl = clickControl;
    }




    @Override
    public void pamStart() {
    }

    
    @Override
    public void pamStop() {
    }

    
    @Override
    public void setupProcess() {
        /* cycle through the alarms and load them */
    	try{
	        for (ClickAlarm alarm : clickControl.clickParameters.clickAlarmList) {
	            alarm.loadAlarm();
	        }
    	}catch (NullPointerException e){
    		System.out.println("null alarm list in manager "+e.getMessage());
    		return;
    	}
    }


    @Override
    /**
     * Whenever a new click is added to the datablock, check to see if it's
     * been classified (if it's clickType field does not equal 0).  If it has
     * been classified, check if it has an alarm attached
     *
     */
    public void newData(PamObservable o, PamDataUnit arg) {
        ClickDetection cd = (ClickDetection) arg;
//        System.out.println("ClickAlarm hashcode, time = " + System.identityHashCode(arg) + "," + cd.getTimeMilliseconds() + "," + cd.getClickType() );
        if (cd.getClickType()!=0) {
        	
            ClickTypeCommonParams commonParams = clickControl.getClickIdentifier().getCommonParams(cd.getClickType());
            
            if (commonParams==null) return;
            	
            Boolean soundAlarm = commonParams.getAlarmEnabled();

            if (soundAlarm != null && soundAlarm) {

                /*
                 * see if the time difference between this detection and the
                 * previous one is less than the max allowed (as stored in the
                 * ClickParameters).  If it is, sound the alarm
                 */
                ClickAlarm alarm = commonParams.getAlarm();
                long deltaT = cd.getTimeMilliseconds()-commonParams.getPrevTime();
                if ( deltaT <= commonParams.getMaxTime() &&
                        deltaT > 0) {
                    clickControl.clickSidePanel.sidePanel.alarmTxtAlert(alarm);
//                    System.out.println("    Click this, prev = " + cd.getTimeMilliseconds() + "," + commonParams.getPrevTime());
                    playIt(alarm);
                }
                commonParams.setPrevTime(cd.getTimeMilliseconds());
            }
        }
    }

    public void playIt(ClickAlarm alarm) {

        // check if a sound file has been loaded
        if ( alarm.isFileLoaded() ) {

            /* Check to make sure the alarm isn't currently sounding.  If it is,
             * just skip the next section.  If it isn't, reset the play position
             * to the beginning of the sound and start playback
             */
            if (!alarm.getClickClip().isRunning()) {
                try {
                    alarm.getClickClip().setFramePosition(0);
                    alarm.getClickClip().start();
                } catch (Exception ex) {
                    System.out.println("Cannot play click alarm");
                    ex.printStackTrace();
                }
            }
        }
    }

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getProcessName()
	 */
	@Override
	public String getProcessName() {
//		if (clickControl == null) {
//			return "Click alarm";
//		}
		if (clickControl == null) {
			return "Click alarm";
		}
		return clickControl.getUnitName() + " Alarms";
	}
}
