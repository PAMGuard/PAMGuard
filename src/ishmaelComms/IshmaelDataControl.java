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

package ishmaelComms;

import java.io.Serializable;

import NMEA.NMEAParameters;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettings;


/**
 * @author Hisham, Qayum, Doug Gillespie, David McLaren, Paul Redmond
 *         <p>
 *         Controller for NMEA data acquisition into the Pamguard framework
 *         <p>
 *         The controller contains two processes, each of which produces a
 *         single output PamDataBlock.
 *         <p>
 *         The first process adds all NMEA strings to it's output block in their
 *         original format
 *         <p>
 *         The second process takes only the GPRMC data and adds them to a
 *         separate data block. the map should subscribe to this second process
 *         and tell it how much data it wished to keep using the
 *         FirstRequiredSample method of the PamObserver interface.
 * 
 */
public class IshmaelDataControl extends PamControlledUnit implements PamSettings {

	IshmaelDataControl ishmaelDataControl;
	ProcessIshmaelData ishmaelClientProcess;


	public IshmaelDataControl(String unitName) {
		
		super("Ishmael Data", unitName);
		
		ishmaelDataControl = this;

		//
		//addPamProcess(new ProcessIshmaelData(this, acquireNmeaData.getOutputDataBlock(0), new NMEAParameters()));
		addPamProcess(ishmaelClientProcess = new ProcessIshmaelData(this));

		// Not doing settings yet
		//PamSettingManager.getInstance().registerSettings(this);
		
		//not doing menus yet.
//		nmeaMenu = createNMEAMenu();
	}

	
	/*
	 * Stuff for settings interface
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// interface bluffing
		return true;
	}
	/*
	 * public void setNmeaSource(int source){
	 * acquireNmeaData.setNMEASource(source); }
	 */
	@Override
	public Serializable getSettingsReference() {
		// interface bluffing
		return null;
	}
	@Override
	public long getSettingsVersion() {
		return NMEAParameters.serialVersionUID;
	}
}
