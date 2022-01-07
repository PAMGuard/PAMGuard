package PamUtils.time.nmea;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;

import GPS.GPSControl;
import GPS.GPSParameters;
import GPS.GpsData;
import GPS.GpsDataUnit;
import NMEA.NMEADataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.time.GlobalTimeManager;
import PamUtils.time.PCTimeCorrector;
import PamUtils.time.TimeCorrection;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class PamNMEATime implements PamSettings, PCTimeCorrector {

	private GlobalTimeManager globalTimeManager;
	
	private NMEATimeParameters nmeaTimeParameters = new NMEATimeParameters();

	private PamDataBlock gpsDataBlock;
	
	private GpsDataObserver gpsDataObserver = new GpsDataObserver();

	public PamNMEATime(GlobalTimeManager globalTimeManager) {
		this.globalTimeManager = globalTimeManager;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getUnitName() {
		return "NMEA Global Time";
	}

	@Override
	public String getName() {
		return "NMEA / GPS";
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean showDialog(Window frame) {
		NMEATimeParameters newParams = NMEATimeDialog.showDialog(frame, nmeaTimeParameters);
		if (newParams != null) {
			nmeaTimeParameters = newParams;
		}
		return newParams != null;
	}

	@Override
	public int getUpdateInterval() {
		return 1;
	}

	@Override
	public void stop() {
		if (gpsDataBlock != null) {
			gpsDataBlock.deleteObserver(gpsDataObserver);
		}
	}

	@Override
	public boolean start() {
		gpsDataBlock = PamController.getInstance().getDataBlockByLongName(nmeaTimeParameters.nmeaSource);
		if (gpsDataBlock != null && gpsDataBlock.getUnitClass() != NMEADataUnit.class) {
			gpsDataBlock = null;
		}
		if (gpsDataBlock == null) {
			ArrayList<PamDataBlock> gpsBlocks = PamController.getInstance().getDataBlocks(NMEADataUnit.class, false);
			if (gpsBlocks.size() > 0) {
				gpsDataBlock = gpsBlocks.get(0);
			}
			if (gpsDataBlock != null) {
				nmeaTimeParameters.nmeaSource = gpsDataBlock.getLongDataName();
			}
		}
		if (gpsDataBlock == null) {
			return false;
		}
		gpsDataBlock.addObserver(gpsDataObserver);
		return true;
	}

	public void newNMEAData(NMEADataUnit nmeaDataUnit) {
		String nmeaType = nmeaDataUnit.getStringId();
		if (nmeaType.endsWith("RMC") == false) {
			return;
		}
		GpsData gpsData = new GpsData(nmeaDataUnit.getCharData(), GPSParameters.READ_RMC);
		long gpsSystemTime = nmeaDataUnit.getSystemTime();
		long gpsTrueTime = gpsData.getTimeInMillis();
		String source = gpsDataBlock.getDataName();
		globalTimeManager.updateUTCOffset(new TimeCorrection(gpsSystemTime, gpsTrueTime, source));
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUnitType() {
		return "NMEA Global Time";
	}

	@Override
	public Serializable getSettingsReference() {
		return nmeaTimeParameters;
	}

	@Override
	public long getSettingsVersion() {
		return NMEATimeParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		nmeaTimeParameters = ((NMEATimeParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	private class GpsDataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return getName();
		}

		/* (non-Javadoc)
		 * @see PamguardMVC.PamObserverAdapter#update(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			newNMEAData((NMEADataUnit) arg);
		}
		
	}

}
