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

import org.json.JSONObject;

import Acquisition.FolderInputSystem;
import nmeaEmulator.NMEAFrontEnd;
import pamguard.CommandLine;
import pamguard.GlobalArguments;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.status.BaseProcessCheck;
import nmeaEmulator.NMEAFrontEnd;
import pamguard.GlobalArguments;


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

	protected AcquireNmeaData acquireNmeaData;
//	ProcessAISData processAISData;
	protected NMEAParameters nmeaParameters = new NMEAParameters();
//	JMenuItem nmeaMenu;
	private NMEAControl nmeaControl;
	
	public static final String GlobalPortFlag = "-serialPort";
	
	public static final String nmeaUnitType = "NMEA Data";
	
	/**
	 * Command line to set com port from command start line. 
	 */
	public static final String NMEACOMCOMMAND = "-NMEAPORT";

	public NMEAControl(String unitName) {
		
		super(nmeaUnitType, unitName);
		
		nmeaControl = this;

		acquireNmeaData = new AcquireNmeaData(this, nmeaParameters);

		addPamProcess(acquireNmeaData);
		setModuleStatusManager(acquireNmeaData);
		
		PamSettingManager.getInstance().registerSettings(this);
		
		checkGlobalArguments();
		
	}

	private void checkGlobalArguments() {
		// try the new command line way of getting these things ...
		String globArg = CommandLine.getCommandLine().getCommandParameter(NMEACOMCOMMAND); // dougs way
		if (globArg == null) {
			globArg = CommandLine.getCommandLine().getCommandParameter(NMEAControl.GlobalPortFlag); // Sam's way
		}
		if (globArg == null) {
			globArg = GlobalArguments.getParam(NMEACOMCOMMAND); // old way !
		}
		if (globArg != null) {
			System.out.printf("Setting %s serial port to %s\n", getUnitName(), globArg);
			if (globArg.equalsIgnoreCase("auto")) {
				nmeaParameters.autoSerialPort = true;
			}
			else {
				nmeaParameters.serialPortName = globArg;
			}
		}
//		
//		//Sam's way -- (Doug's is better)
//		String portArg = GlobalArguments.getParam(NMEAControl.GlobalPortFlag);
//		if (portArg != null) {
//			this.nmeaParameters.serialPortName=portArg;
//		}
		
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

		@Override
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
		@Override
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
		@Override
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
	@Override
	public Serializable getSettingsReference() {
		return nmeaParameters;
	}

	@Override
	public long getSettingsVersion() {
		return NMEAParameters.serialVersionUID;
	}
	

	/*
	 * Stuff for settings interface
	 */

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		if (pamControlledUnitSettings.getUnitType().equals(this.getUnitType())
			&& pamControlledUnitSettings.getVersionNo() == NMEAParameters.serialVersionUID) {
			this.nmeaParameters = ((NMEAParameters) pamControlledUnitSettings.getSettings()).clone();
		}
		
		return true;
	}
	
	@Override
	public String getModuleSummary(boolean clear, String format) {
		if(format.equals("json")) {
			NMEADataUnit lastUnit = acquireNmeaData.getOutputDatablock().getLastUnit();
			if(lastUnit==null) {
				return "";
			}
			JSONObject json = new JSONObject();
			json.put("LastDataTime", lastUnit.getTimeMilliseconds());
			json.put("LastDataString", lastUnit.getCharData().toString());
			return json.toString();
		}
		return "";
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

	/**
	 * @return the acquireNmeaData
	 */
	public AcquireNmeaData getAcquireNmeaData() {
		return acquireNmeaData;
	}
	
}
