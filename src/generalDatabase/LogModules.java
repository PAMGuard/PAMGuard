package generalDatabase;

import java.sql.Connection;
import java.sql.Types;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;


/**
 * Functions for writing a list of modules into any database as character data
 * Runs at DAQ start, goes through the settings manager list and for each
 * set of settings, it serialises the settings data into a binary array, this
 * is then converted from binary data to 6 bit ascii data (using the character set
 * from the AIS standard, which should be compatible with any DBMS). This character
 * string is then broken up into parts < 255 characters long and written to the 
 * Pamguard_Settings table in the database. 
 * <br>
 * This will allow 1) an audit of exactly how Pamguard was configured at any particular
 * time, 2) when looking at data offline, the database will contain all information 
 * required to reconstruct the Pamguard data model and displays, the database thereby
 * becomes a self contained document of operations, there being no need to keep hold
 * of psf settings files. 
 * 
 * @author Doug Gillespie
 * @see LogSettings
 *
 */
public class LogModules extends DbSpecial {

	PamTableDefinition tableDef;
	PamTableItem moduleType, moduleName;
	
	public LogModules(DBControl dbControl) {
		
		super(dbControl);
	
		tableDef = new PamTableDefinition("PamguardModules", SQLLogging.UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(moduleType = new PamTableItem("Module Type", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		tableDef.addTableItem(moduleName = new PamTableItem("Module Name", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		tableDef.setUseCheatIndexing(true);
		
		setTableDefinition(tableDef);
	}
	

	@Override
	public void pamStart(PamConnection con) {

		// go through the Pamguard model and make list of all loaded modules.
		long now = System.currentTimeMillis();
		
		PamController pamController = PamController.getInstance();
		PamControlledUnit pcu;
		for (int i = 0; i < pamController.getNumControlledUnits(); i++) {
			pcu = pamController.getControlledUnit(i);
			saveUnit(now, pcu);
		}
		
	}

	@Override
	public void pamStop(PamConnection con) {

	}

	private void saveUnit(long timeMillis, PamControlledUnit pUnit) {

		ModulesDataUnit mu = new ModulesDataUnit(timeMillis, pUnit.getUnitType(), pUnit.getUnitName());
		
		this.logData(mu);
		
	}
	
	private class ModulesDataUnit extends PamDataUnit {
		
		String name;
		String type;

		public ModulesDataUnit(long timeMilliseconds, String name, String type) {
			super(timeMilliseconds);
			this.type = type;
			this.name = name;
			
		}
		
	}
	
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		ModulesDataUnit mdu = (ModulesDataUnit) pamDataUnit;
		moduleType.setValue(mdu.type);
		moduleName.setValue(mdu.name);
		
	}

}
