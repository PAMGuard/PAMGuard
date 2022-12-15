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


package clickDetector;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Alarm object for the Click Alarm
 * 
 * @author Michael Oswald
 */
public class ClickAlarm implements Comparable<ClickAlarm>, Serializable, Cloneable, ManagedParameters {

    /** for serialization */
    static public final long serialVersionUID = 2;

    /** Default alarm name */
    static public final String DEFAULTNAME = "Alarm 1";

    /**
     * Default alarm sound
     */
    static public final String DEFAULTDING = new String("/sounds/down_chirp.wav");

    /** 
     * User-defined alarm sound.
     */
    private String userAlarmFile = DEFAULTDING;

    /**
     * Name of alarm
     */
    private String name = DEFAULTNAME;

    /**
     * Boolean indicating whether or not the default sound file should be used
     */
    boolean useDefault = true;

    /** Boolean to indicate whether or not sound file has been loaded */
    private boolean fileIsLoaded;

    /**
     * Wav clip to be played.  Mark clip as transient, because Audio Clips
     * can't be serialized
     */
    transient private Clip clickClip;

    /**
     * Constructor with no passed name - just use the default name
     */
    public ClickAlarm() {
        this(DEFAULTNAME);
    }

    /**
     * Constructor used to set the name of the new alarm.  Defaults to the
     * DEFAULTDING constant, and loads it right away
     */
    public ClickAlarm(String name) {
        this.name = name;
        loadAlarm();
    }

    public String getUserAlarmFile() {
        return userAlarmFile;
    }

    public void setUserAlarmFile(String userAlarmFile) {
        this.userAlarmFile = userAlarmFile;
    }

    /** Check if we're using the default sound file for the alarm.  True
     * indicates this alarm uses the default sound file.  False indicates this
     * alarm uses a user-defined sound file.
     */
    public boolean useDefault() {
        return useDefault;
    }

    /* set the useDefault variable.  True indicates this alarm uses the default
     * sound file.  False indicates this alarm uses a user-defined sound file.
     */
    public void selectDefault(boolean defaultSoundFlag) {
        useDefault = defaultSoundFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean loadAlarm() {
        clickClip = null;
        String clipFile = null;
        URL clipURL = null;

        if(PamController.getInstance().getRunMode()==PamController.RUN_NOTHING) {
    		return false;
    	}
        /* Create a URL that points to our file.  If we're using the default ding,
         * generate the URL from the clipFile location since the default ding
         * is included in the jar package.  If we're using a user-selected wav,
         * generate the URL from the absolute pathname
         */
        if (useDefault) {
            clipFile = DEFAULTDING;
            clipURL = getClass().getResource(clipFile); /* Use this method if you package your WAV inside a JAR */
        } else {
            try {
                clipFile = userAlarmFile;
                clipURL = (new File(userAlarmFile)).toURI().toURL();
            } catch (Exception ex) {
                System.out.println("Error - cannot generate URL for alarm "+userAlarmFile);
            }
        }

        try {
            if (clipURL != null) {

                /* Open as an AudioStream */
//                AudioInputStream ais = AudioSystem.getAudioInputStream(clipFile);
                AudioInputStream ais = AudioSystem.getAudioInputStream(clipURL);
                clickClip = AudioSystem.getClip();
                clickClip.open(ais);

                /* set flag */
                fileIsLoaded = true;

            } else {
                clickClip = null;
                fileIsLoaded = false;
            }

        } catch (Exception ex) {
            System.out.println("Error - cannot load alarm "+clipURL.getFile());
            clickClip = null;
            fileIsLoaded = false;
        }
        return fileIsLoaded;
    }

    public boolean isFileLoaded() {
        return fileIsLoaded;
    }

    public void setFileLoaded(boolean fileIsLoaded) {
        this.fileIsLoaded = fileIsLoaded;
    }

    public Clip getClickClip() {
        return clickClip;
    }

	@Override
    public ClickAlarm clone() {
		try {
			ClickAlarm n = (ClickAlarm) super.clone();
			return n;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(ClickAlarm alarm) {
        return name.compareTo(alarm.getName());
    }

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("fileIsLoaded");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fileIsLoaded;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useDefault");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useDefault;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
