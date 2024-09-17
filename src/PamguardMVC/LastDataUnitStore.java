package PamguardMVC;

import java.util.Enumeration;
import java.util.Hashtable;

/** 
 * Stores a set of latest data units, only one per data block and can 
 * retreive them in various ways. 
 * @author doug
 *
 */
public class LastDataUnitStore {

//	private ArrayList<LastUnitItem> lastUnits = new ArrayList<>();
	private Hashtable<PamDataBlock, DataBlockRXInfo> lastDataUnits = new Hashtable<>();
	
	/**
	 * Add a new unit to the store, returning the previous one if it existed. 
	 * @param dataBlock Data Block
	 * @param dataUnit Data unit
	 */
	public void addDataUnit(PamDataBlock dataBlock, PamDataUnit dataUnit, int dataSize) {
		DataBlockRXInfo rxInfo = findOrCreate(dataBlock);
		rxInfo.newDataunit(dataUnit, dataSize);
	}
	
	private synchronized DataBlockRXInfo findOrCreate(PamDataBlock dataBlock) {
		DataBlockRXInfo rxInfo = lastDataUnits.get(dataBlock);
		if (rxInfo != null) {
			return rxInfo;
		}
		else {
			rxInfo = new DataBlockRXInfo();
			lastDataUnits.put(dataBlock, rxInfo);
			return rxInfo;
		}
	}

	public PamDataUnit findLastDataUnit(PamDataBlock dataBlock) {
		return findOrCreate(dataBlock).getLastDataUnit();
	}
	
	public DataBlockRXInfo getDataBlockInfo(PamDataBlock dataBlock) {
		return findOrCreate(dataBlock);
	}
	
	/**
	 * Find a data unit by it's class type
	 * @param unitClass Data Unit class
	 * @return last data unit of that class, or null. 
	 */
	public synchronized PamDataUnit findLastDataUnit(Class unitClass) {
		Enumeration<DataBlockRXInfo> els = lastDataUnits.elements();
		while (els.hasMoreElements()) {
			DataBlockRXInfo u = els.nextElement();
			PamDataUnit pamDataUnit = u.getLastDataUnit();
			if (pamDataUnit == null) {
				continue;
			}
			if (pamDataUnit.getClass() == unitClass) {
				return pamDataUnit;
			}
		}
		return null;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
}
