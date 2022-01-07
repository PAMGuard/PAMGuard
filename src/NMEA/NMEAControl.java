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
package NMEA;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import nmeaEmulator.NMEAFrontEnd;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.status.BaseProcessCheck;


/**
 * @author Doug Gillespie, David McLaren, Paul Redmond
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
public class NMEAControl extends PamControlledUnit implements PamSettings {

	AcquireNmeaData acquireNmeaData;
//	ProcessAISData processAISData;
	NMEAParameters nmeaParameters = new NMEAParameters();
//	JMenuItem nmeaMenu;
	NMEAControl nmeaControl;
	
	public static final String nmeaUnitType = "NMEA Data";

	public NMEAControl(String unitName) {
		
		super(nmeaUnitType, unitName);
		
		nmeaControl = this;

		acquireNmeaData = new AcquireNmeaData(this, nmeaParameters);

		addPamProcess(acquireNmeaData);
		setModuleStatusManager(acquireNmeaData);

//		addPamProcess(new ProcessNmeaData(this, acquireNmeaData.getOutputDataBlock(0), new NMEAParameters()));

//		addPamProcess(processAISData = new ProcessAISData(this, acquireNmeaData.getOutputDataBlock(0)));
		
		PamSettingManager.getInstance().registerSettings(this);
		
//		nmeaMenu = createNMEAMenu();
	}

	public JMenuItem createNMEAMenu(Frame parentFrame) {
		JMenuItem menuItem;
		JMenu subMenu = new JMenu(getUnitName());
		menuItem = new JMenuItem("NMEA Parameters ...");
		menuItem.addActionListener(new NMEASettings(parentFrame));
		subMenu.add(menuItem);
		menuItem = new JMenuItem("NMEA Strings ...");
		menuItem.addActionListener(new NMEAStrings(parentFrame));
		subMenu.add(menuItem);
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			menuItem = new JMenuItem("Emulate Serial NMEA data ...");
			menuItem.addActionListener(new NMEAEmulate(parentFrame));
			subMenu.add(menuItem);
		}
//		menuItem = new JMenuItem("AIS Ship Data ...");
//		menuItem.addActionListener(new AISShipData());
//		subMenu.add(menuItem);
		/*menuItem = new JMenuItem("Update PC Clock ...");
		menuItem.addActionListener(new UpdateClock(parentFrame));
		subMenu.add(menuItem);*/
		return subMenu;
	}

	public NMEAParameters getNmeaParameters() {
		return nmeaParameters;
	}

	class NMEASettings implements ActionListener {
		Frame parentFrame;
		
		public NMEASettings(Frame parentFrame) {
			super();
			// TODO Auto-generated constructor stub
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {
			showNMEADialog(parentFrame);
		}		
	}
	
	public void showNMEADialog(Frame parentFrame ) {
		
			NMEAParameters ans = NMEAParametersDialog
					.showDialog(parentFrame, nmeaParameters);
			if (ans != null) {
				nmeaParameters = ans.clone(); // copy across new settings
				acquireNmeaData.startNmeaSource(ans.sourceType); // restart the process with new settings
				
			}
	}
	
	class NMEAStrings implements ActionListener {
		private Frame parentFrame;
		public NMEAStrings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		public void actionPerformed(ActionEvent e) {
			NMEAStringsTable.show(parentFrame, nmeaControl);
		}
	}
	class NMEAEmulate implements ActionListener {
		Frame parentFrame;
		public NMEAEmulate(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		public void actionPerformed(ActionEvent e) {
			emulateNMEAData(parentFrame);
		}
	}
	
	private void emulateNMEAData(Frame parentFrame) {
		new NMEAFrontEnd(this, parentFrame);
	}
	

	/*class menuDetection implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			ClickParameters newParameters = ClickParamsDialog
					.Show(clickParameters);
			if (newParameters != null) {
				clickParameters = newParameters;
				clickDetector.NewParameters();
			}
		}
	}*/

	/*
	 * public void setNmeaSource(int source){
	 * acquireNmeaData.setNMEASource(source); }
	 */
	public Serializable getSettingsReference() {
		return nmeaParameters;
	}

	public long getSettingsVersion() {
		return NMEAParameters.serialVersionUID;
	}
	

	/*
	 * Stuff for settings interface
	 */

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		if (pamControlledUnitSettings.getUnitType().equals(this.getUnitType())
			&& pamControlledUnitSettings.getVersionNo() == NMEAParameters.serialVersionUID) {
			this.nmeaParameters = ((NMEAParameters) pamControlledUnitSettings.getSettings()).clone();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		super.setupControlledUnit();
		acquireNmeaData.startNmeaSource(this.nmeaParameters.sourceType);
		
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return createNMEAMenu(parentFrame);
	}

	@Override
	public void pamClose() {
		acquireNmeaData.stopNMEASource();
	}
	
	/**
	 * Get the NMEA DataBlock.
	 * @return the NMEA data block
	 */
	public NMEADataBlock getNMEADataBLock() {
		return acquireNmeaData.getOutputDatablock();
	}
	
}
