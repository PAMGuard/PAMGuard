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
package PamController;

import java.io.Serializable;

/**
 * @author Doug Gillespie
 *         <p>
 *         Implement this in any class that has settings you want to store
 *         between runs.
 *         <p>
 *         A class implementing PamSettings must register itself with the
 *         settings manager with a call such as
 *         <p>
 *         PamSettingManager.getInstance().RegisterSettings(this);
 * 
 */
public interface PamSettings extends SettingsNameProvider {



	/**
	 * @return A Name specific to the type, e.g. Click detector
	 */
	public String getUnitType();

	/**
	 * @return The serialisable object that will be stored
	 */
	public Serializable getSettingsReference();

	/**
	 * @return An integer version number for the settings
	 */
	public long getSettingsVersion();

	/**
	 * @param pamControlledUnitSettings
	 * @return true if this Object knows what to do with this particular
	 *         settings object (based on unitType, unitName and settingsVersion)
	 */
//	public boolean IsSettingsUnit(
//			PamControlledUnitSettings pamControlledUnitSettings);

	/**
	 * @param pamControlledUnitSettings
	 * @return true if successful The object performs final checks (if needed)
	 *         and then casts the settings data
	 *         pamcontrolledunitSettings.settings into the correct type and uses
	 *         as required
	 */
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings);

}
