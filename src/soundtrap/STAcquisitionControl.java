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



package soundtrap;

import java.awt.Component;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;

/**
 * @author SCANS
 *
 */
public class STAcquisitionControl extends AcquisitionControl {

	public static final String STUNITTYPE = "SoundTrap Data Acquisition";
	private STAcquisitionProcess stAcquisitionProcess;
	private STDaqSystem stDaqSystem;
	
	/*
	 * Standard voltage for soundtrap data. note that ST calibration is only 
	 * provided as end to end peak input, so the correct thing to do is set the peak to peak as 2
	 * (0-peak = 1) and then put the hydrophone sensitivity as - the end to end and all will be well. 
	 */
	public static final double SOUNDTRAPVP2P = 2.0;

	/**
	 * @param name
	 */
	public STAcquisitionControl(String name) {
		super(name);
		
		// add a second PAM process, for the click data
		registerDaqSystem(stDaqSystem = new STDaqSystem());
		addPamProcess(stAcquisitionProcess = new STAcquisitionProcess(this));

		acquisitionParameters.voltsPeak2Peak = SOUNDTRAPVP2P;
		
	}

	@Override
	/**
	 * Note that since this constructor calls the AcquisitionControl constructor directly,
	 * the actual unit type for this controlled unit is "Data Acquisition".
	 * Override here to return the Soundtrap unit type instead
	 */
	public String getUnitType() {
		return STUNITTYPE;
	}

	/**
	 * Returns the Soundtrap Acquisition Process, which handles the click data
	 * @return
	 */
	public STAcquisitionProcess getStAcquisitionProcess() {
		return stAcquisitionProcess;
	}

//	@Override
//	protected Component getStatusBarComponent() {
//		// call it to create the fields, just incase, then return null;
//		super.getStatusBarComponent();
//		return null;
//	}

//	@Override
//	protected void setupStatusBar() {
////		if (systemPanel == null) return;
////		systemPanel.removeAll();
////		DaqSystem daqSys = findDaqSystem(null);
////		if (daqSys == null) return;
////		Component specialComponent = daqSys.getStatusBarComponent();
////		if (specialComponent != null) {
////			systemPanel.add(specialComponent);
////		}
//	}

	@Override
	public DaqSystem findDaqSystem(String systemType) {
		return stDaqSystem;
	}

	

}
