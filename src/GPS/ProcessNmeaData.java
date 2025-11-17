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
package GPS;

import javax.swing.SwingUtilities;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.status.BaseProcessCheck;
import PamModel.PamModel;
import PamModel.SMRUEnable;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import geoMag.MagneticVariation;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * @author Doug Gillespie
 * 
 * PamProcess that subscribed to the AcquireNmeaData processes output data
 * block, selects just the interesting stuff in the GPRMC string and creates a
 * new data block with the string data unpacked into more usable doubles.
 */
public class ProcessNmeaData extends PamProcess implements ClockUpdateObserver {

	private GPSDataBlock gpsDataBlock;

	private GPSControl gpsController;

	private GpsLogger gpsLogger;

	//	private String wantedString = "$GPRMC";

	private GpsDataUnit previousUnit = null;

	private MagneticVariation magneticVariation;

	private PamWarning badGpsString; 

	private BaseProcessCheck processCheck;

	private GPSClockUpdater gpsClockUpdater;

	public GPSClockUpdater getGpsClockUpdater() {
		return gpsClockUpdater;
	}

	/**
	 * @param pamControlledUnit
	 *            Reference to the NMEAController
	 * @param allNmeaData
	 *            reference to the output data block of the AcquireNMEAData
	 *            Process.
	 */
	ProcessNmeaData(GPSControl gpsControl) {

		super(gpsControl, null);

		this.gpsController = gpsControl;

		badGpsString = new PamWarning(gpsControl.getUnitName(), "Bad GPS String", 1);
		badGpsString.setWarningTip("<html>Occasional bad strings are perfectly normal.<br>"
				+ "If they persist, check your antenna connection.</html>");

		setProcessName("GPS data selection");

		//allNmeaData.addObserver(this);
		findNMEADataBlock();

		gpsClockUpdater = new GPSClockUpdater(gpsControl, this);
		gpsClockUpdater.addObserver(this);

		addOutputDataBlock((gpsDataBlock = new GPSDataBlock(this)));
		if (!gpsControl.isGpsMaster()) {
			gpsDataBlock.setOverlayDraw(new GPSOverlayGraphics(gpsControl));
			gpsDataBlock.setPamSymbolManager(new StandardSymbolManager(gpsDataBlock, GPSOverlayGraphics.defSymbol, false));
		}


		//		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
		//			gpsDataBlock.setBinaryDataSource(new GPSBinaryDataSource(gpsDataBlock));
		//		}

		gpsDataBlock.setNaturalLifetime(1);
		if (PamModel.getPamModel() != null) {
			PamModel.getPamModel().setGpsDataBlock(gpsDataBlock);
		}

		gpsDataBlock.SetLogging(new GpsLogger(gpsDataBlock));
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER || SMRUEnable.isEnableDecimus()) {
			gpsDataBlock.setBinaryDataSource(new GPSBinaryDataSource(gpsDataBlock));
		}

		magneticVariation = MagneticVariation.getInstance();

		processCheck = new BaseProcessCheck(this, NMEADataUnit.class, 0.2, 0.2);
		processCheck.setNeverIdle(true);
		processCheck.setInputAveraging(5);
		processCheck.setOutputAveraging(5);
		setProcessCheck(processCheck);

	}

	boolean findNMEADataBlock() {

		PamDataBlock newDataBlock = null;
		if (!gpsController.isGpsMaster()) {
			return false;
		}

		newDataBlock = PamController.getInstance().getDataBlockByLongName(gpsController.gpsParameters.nmeaSource);
		if (newDataBlock == null) {
			newDataBlock = PamController.getInstance().getDataBlock(NMEADataUnit.class, 0);
		}

		gpsController.nmeaDataBlock = (NMEADataBlock) newDataBlock;
		//		newDataBlock.addObserver(this);
		setParentDataBlock(newDataBlock);

		return true;
	}
	//
	//	public void setupNMEA(NMEAParameters nmeaParameters) {
	//		this.nmeaParameters = nmeaParameters;
	//
	//	}

	public long firstRequiredTime(PamObservable o, Object arg) {
		return -1;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			noteNewSettings();
		}
	}

	@Override
	public void noteNewSettings() {
		findNMEADataBlock();
		//		setWantedString();
		double minRate = Math.max(.2, 1./gpsController.gpsParameters.readInterval);
		processCheck.getOutputCounter().setMinRate(minRate);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit pamData) {
		/*
		 * Here is where you look at the string type, if it's GPRMC, then unpack
		 * it into your GpsData class and add that to the output data block
		 */

		NMEADataUnit nmeaData = (NMEADataUnit) pamData;
		GpsData gpsData;
		GpsDataUnit newUnit;
		StringBuffer nmeaString = nmeaData.getCharData();
		String stringId = NMEADataBlock.getSubString(nmeaString, 0);

		//		if (stringId.equalsIgnoreCase(wantedString)) {
		if (gpsController.wantString(stringId)) {
			gpsData = new GpsData(nmeaString, gpsController.gpsParameters.mainString); // GpsData constructor which
			// unpacks the string.
			if (gpsData.isDataOk()) {

				newUnit = new GpsDataUnit(nmeaData.getTimeMilliseconds(), gpsData);

				gpsClockUpdater.newGPSData(newUnit);

				if (gpsController.doAutoClockUpdate) {
					gpsController.doAutoClockUpdate = false;
					if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
						gpsClockUpdater.updateOnNext();
					}
					else {
						SwingUtilities.invokeLater(new Runnable() {
							// invoke later or it will lock the thread. 
							@Override
							public void run() {
								UpdateClockDialog.showDialog(null, gpsController, gpsController.gpsParameters, true);		
							}
						});
					}
				}

				//				newUnit.data = gpsData;
				if (!wantDataUnit(newUnit)) {
					return;
				}

				double magVar = magneticVariation.getVariation(newUnit.getGpsData());
				newUnit.getGpsData().setMagneticVariation(magVar);
				//				System.out.println(String.format("Magnetic variation = %3.1f", magVar));

				// if it gets this far, see if there is any true or magnetic
				// heading data from the last 4 seconds to add.
				ProcessHeadingData phd = gpsController.headingProcess;
				long ht = phd.getTrueTime();
				if (nmeaData.getTimeMilliseconds()-ht < 4000) {
					gpsData.setTrueHeading(phd.getTrueHeading());
				}

				ht = phd.getMagneticTime();
				if (nmeaData.getTimeMilliseconds()-ht < 4000) {
					gpsData.setMagneticHeading(phd.getMagneticHeading());
					if (phd.getMagneticVariation() != null) {
						gpsData.setMagneticVariation(phd.getMagneticVariation());
					}
				}

				gpsDataBlock.addPamData(newUnit);
				gpsController.checkGPSTime(newUnit);
				//				MasterReferencePoint.setRefLatLong(newUnit.getGpsData(), "Latest GPS Data");
				previousUnit = newUnit;
			}
			else {
				String msg = "Bad GPS String : " + nmeaString;
				badGpsString.setWarningMessage(msg);
				badGpsString.setEndOfLife(PamCalendar.getTimeInMillis() + 6000);
				WarningSystem.getWarningSystem().addWarning(badGpsString);
				//				System.out.println(msg);
			}
		}
	}

	/**
	 * Decide if we want this unit or not based on the gpsPArameters and 
	 * what the time, angle change and speed change are since the last unit. 
	 * @return true if GPS data should be stored / used. 
	 */
	protected boolean wantDataUnit(GpsDataUnit newUnit) {

		// always return true if this is the first one. 
		if (previousUnit == null) {
			previousUnit = newUnit;
			return true;
		}
		if (previousUnit.getTimeMilliseconds() > newUnit.getTimeMilliseconds()) {
			/*
			 * going backward in time (in viewer mode hopefully !)
			 */
			previousUnit = newUnit;
			return true;
		}
		switch (gpsController.gpsParameters.readType) {
		case GPSParameters.READ_ALL:
			return true;
		case GPSParameters.READ_TIMER:
			if ((newUnit.getTimeMilliseconds() - previousUnit.getTimeMilliseconds()) / 1000 >=
			gpsController.gpsParameters.readInterval) {
				previousUnit = newUnit;
				return true;
			}
			else {
				return false;
			}
		case GPSParameters.READ_DYNAMIC:
			if ((newUnit.getTimeMilliseconds() - previousUnit.getTimeMilliseconds()) / 1000 >=
			gpsController.gpsParameters.readInterval) {
				previousUnit = newUnit;
				return true;
			}
			double speedChange = Math.abs(newUnit.getGpsData().getSpeed() - 
					previousUnit.getGpsData().getSpeed());
			if (speedChange >= gpsController.gpsParameters.speedInterval) {
				previousUnit = newUnit;
				return true;
			}
			double headChange = newUnit.getGpsData().getCourseOverGround() - 
					previousUnit.getGpsData().getCourseOverGround();
			headChange = Math.abs(PamUtils.constrainedAngle(headChange, 180));
			if (headChange >= gpsController.gpsParameters.courseInterval) {
				previousUnit = newUnit;
				return true;
			}
			else {
				return false;
			}
		}
		return true; // this last option should never happen ! Should always be one of the above. 
	}


	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#PamStart() Probably dont need to use these
	 *      either - but need them to keep the interface happy
	 */
	@Override
	public void pamStart() {
		previousUnit = null;
	}

	@Override
	public void pamStop() {
	}

	@Override
	public void clearOldData() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL){
			return; 
		}
		super.clearOldData();
	}

	//	public String getWantedString() {
	//		return wantedString;
	//	}
	//	
	//	public void setWantedString() {
	//		wantedString = gpsController.getWantedString();
	//	}

	public GPSControl getGpsController() {
		return gpsController;
	}

	public GPSDataBlock getGpsDataBlock() {
		return gpsDataBlock;
	}

	public GpsLogger getGpsLogger() {
		return gpsLogger;
	}

	@Override
	public void clockUpdated(boolean success, long timeMillis, String message) {
		if (success) {
			System.out.printf("PC Clock sucessfully updated to %s UTC\n", PamCalendar.formatDBDateTime(timeMillis, true));
		}
		else {
			System.out.println("PC Clock update failed");
		}
	}

	@Override
	public void newTime(long timeMillis) {
		// TODO Auto-generated method stub

	}

}
