package networkTransfer.receive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import PamController.PamControlledUnitSettings;
import PamController.PamSettings;

/**
 * Clas for saving buoystatus data into the serialised settings. 
 * Keeping these separate from the main Net RX settings. 
 * Status data is stored in objects in data units, so need to pull these out 
 * into an array list. 
 * @author dg50
 *
 */
public class BuoyDataSerialiser implements PamSettings {
	
	private NetworkReceiver networkReceiver;

	public BuoyDataSerialiser(NetworkReceiver networkReceiver) {
		super();
		this.networkReceiver = networkReceiver;
	}

	@Override
	public String getUnitName() {
		return networkReceiver.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Buoy Status";
	}

	@Override
	public long getSettingsVersion() {
		return BuoyStatusData.getSerialversionuid();
	}

	@Override
	public Serializable getSettingsReference() {
		ArrayList<BuoyStatusData> statusData = new ArrayList<BuoyStatusData>();
		BuoyStatusDataBlock db = networkReceiver.getBuoyStatusDataBlock();
		synchronized (db) {
			ListIterator<BuoyStatusDataUnit> it = db.getListIterator(0);
			while (it.hasNext()) {
				BuoyStatusDataUnit du = it.next();
				statusData.add(du.getBuoyStatusData());
			}
		}
		return statusData;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		ArrayList<BuoyStatusData> statusData = (ArrayList<BuoyStatusData>) pamControlledUnitSettings.getSettings();
		BuoyStatusDataBlock db = networkReceiver.getBuoyStatusDataBlock();
		for (BuoyStatusData bds : statusData) {
			db.addPamData(new BuoyStatusDataUnit(networkReceiver, bds));
		}
		return true;
	}

}
