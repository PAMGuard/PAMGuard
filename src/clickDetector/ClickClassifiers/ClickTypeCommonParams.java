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


package clickDetector.ClickClassifiers;

import clickDetector.ClickAlarm;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Superclass for click parameters, including the ClickTypeParms and
 * SweepClassifierSet classes.  Created to allow simple access to the alarm-
 * specific parameters
 * 
 * @author Michael Oswald
 */
abstract public class ClickTypeCommonParams implements Cloneable, Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	public static final long serialVersionUID = -6002534290706863277L;

	/**
	 * True for the classifier to be enabled. 
	 */
	public static final Boolean enable = true;

	/**
	 * The name of the classifier 
	 */
	protected String name;

	/**
	 * The species code of the classifier
	 */
	protected int speciesCode;
	
	/**
	 * True to discard classified clicks insteac of calssifiying them. 
	 */
	protected Boolean discard = false;

	/**
     * Alarm to be used for detection
     */
    protected ClickAlarm alarm = null;

    /**
     * boolean indicating whether or not the alarm has been enabled
     */
    protected Boolean alarmEnabled = false;

    /** the time (in milliseconds) of the previous detection */
    protected long prevTime = 0;

    /** the maximum amount of time allowed (in milliseconds) between clicks
     * to still be counted as a detection (for alarm purposes)
     */
    protected long maxTime = 0;

    /**
     * Returns the {@link ClickAlarm} associated with this click type.
     *
     * @return the ClickAlarm
     */
    public ClickAlarm getAlarm() {
        return alarm;
    }

    /**
     * Sets the alarm used for this click type.  This is used in the
     * setupProcess() method of ClickDetector to reset a species code's alarm
     * to the default alarm, in case the original alarm has been deleted.
     *
     * @param clickAlarm the clickAlarm to apply
     */
    public void setAlarm(ClickAlarm alarm) {
        this.alarm = alarm;
    }

    /**
     * Returns true/false on whether an alarm has been enabled for the click
     *
     * @return Boolean indicating status
     */
	public Boolean getAlarmEnabled() {
		return alarmEnabled;
	}
	public void setAlarmEnabled(Boolean alarmEnabled) {
		this.alarmEnabled = alarmEnabled;
	}

    public long getPrevTime() {
        return prevTime;
    }

    public void setPrevTime(long prevTime) {
        this.prevTime = prevTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

	@Override
	protected ClickTypeCommonParams clone() {
		try {
			return (ClickTypeCommonParams) super.clone();
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	public Boolean getDiscard() {
		if (discard == null) {
			discard = false;
		}
		return this.discard;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the speciesCode
	 */
	public int getSpeciesCode() {
		return speciesCode;
	}

	/**
	 * @param speciesCode the speciesCode to set
	 */
	public void setSpeciesCode(int speciesCode) {
		this.speciesCode = speciesCode;
	}

	/**
	 * @return the enable
	 */
	public static Boolean getEnable() {
		return enable;
	}

	/**
	 * @param discard the discard to set
	 */
	public void setDiscard(Boolean discard) {
		this.discard = discard;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
