package PamguardMVC.uid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;

/**
 * Class for managing UID's during real time operations. Basic functions that 
 * will be needed are to retrieve latest UIDs from the database and binary store at start up 
 * and to work out methods of storing id's persistently between runs and if / when PAMGUard crashes
 * without correctly saving UID's.  
 * @author dg50
 *
 */
public class UIDOnlineManager implements UIDManager {

	private PamController pamController;
	private DatabaseUIDFunctions dbUIDFuncs;
	private binaryUIDFunctions binUIDFuncs;

	public UIDOnlineManager(PamController pamController) {
		super();
		this.pamController = pamController;
		dbUIDFuncs = new DatabaseUIDFunctions(pamController);
		binUIDFuncs = new binaryUIDFunctions(pamController);
	}
	
	
	/**
	 * Finds largest UID for each PamDataBlock.  Originally this method came up with a list of all UIDs from the
	 * database, another list of all UIDs from the binary files, and then tried to match up the PamDataBlocks
	 * with both lists.  That has been commented out now and reworked to loop through the PamDataBlocks
	 * first, and only search for UIDs specific to each block.  The original code was working, and therefore only
	 * commented out and not deleted in case we need to go back to it.
	 */
	@Override
	public boolean runStartupChecks() {
		
		// get information from the database, if we're using one
//		List<UIDTrackerData> dbData = new ArrayList<UIDTrackerData>();
		boolean dbAvailable = false;
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbAvailable=true;
//
//			// Check if the tracker table exists.  If it does, read the information for each table
//			if (dbUIDFuncs.checkTableExists()) {
//				dbData = dbUIDFuncs.getAllUIDsfromTrackerTable();
//
//				// if the tracker table does not exist, Pamguard may have crashed previously before the table
//				// was written.  In that case, we've got to go through all the tables one at a time and find
//				// the highest UID in each
//			} else {
//				dbData = dbUIDFuncs.getUIDsFromAllTables();
//			}
//			
//			// do a quick check to see if there was actually data found.  If Pamguard crashed, it's possible that the database
//			// store is set up but there isn't actually any data.  This could also happen if there simply isn't any data
//			// in the database yet.  Either way, change the flag back to false
//			if (dbData.isEmpty()) {
//				dbAvailable=false;
//			}
		}
//
//		// get information from the binary store, if we're using one
//		List<UIDTrackerData> binData = new ArrayList<UIDTrackerData>();
		boolean binAvailable = false;
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore != null) {
			binAvailable=true;
//
//			// now check if the binary store log file exists.  If it does, read the information for
//			// each table
//			if (binUIDFuncs.checkFileExists()) {
//				binData = binUIDFuncs.readLogFile();
//
//				// if the log file doesn't exist, Pamguard may have crashed previously before the file
//				// was written.  In that case, we've got to...
//			} else {
//				binData = binUIDFuncs.getUIDsFromBinaryStores();
//			}
//			
//			// do a quick check to see if there was actually data found.  If Pamguard crashed, it's possible that the binary
//			// store is set up but there isn't actually any data.  If so, change the flag back to false
//			if (binData==null) {
//				binAvailable=false;
//			}
		}
//		
//		// loop through the datablocks looking for matches in the database and binary store.  There
//		// are 4 scenarios:
//		//	1.	the datablock uses both database and binary store.  In this case, we need to check
//		//		both and get the highest UID because the binary store and database don't necessarily
//		//		synch up
//		//	2.	the datablock uses only the database
//		//	3.	the datablock uses only the binary store
//		//	4.	the datablock doesn't use either
//		ArrayList<PamDataBlock> allDataBlocks = pamController.getDataBlocks();	
//		
//		for (PamDataBlock aDataBlock:allDataBlocks) {
//
//			// Scenario 1: datablock uses both database and binary store
//			if (dbAvailable 
//					&& aDataBlock.getCanLog()
//					&& aDataBlock.getShouldLog()
//					&& binAvailable
//					&& aDataBlock.getShouldBinary(null)) {
//				
//				// first check for highest UID in database
//				long highestDbUID = -1;
//				for (UIDTrackerData uidData:dbData) {
//					if (aDataBlock.getLogging().getTableDefinition().getTableName().equals(uidData.getName())) {
//						highestDbUID = uidData.getUid();
//						break;
//					}
//				}
//				// now get the highest binary store UID
//				long highestBinUID = -1;
//				BinaryOutputStream bss = aDataBlock.getBinaryDataSource().getBinaryStorageStream();
//				String searchName = aDataBlock.getBinaryDataSource().createFilenamePrefix();
//				for (UIDTrackerData uidData:binData) {
//					if (searchName.equals(uidData.getName())){
//						highestBinUID = uidData.getUid();
//						break;
//					}
//				}
//				// pick the highest value
//				aDataBlock.getUidHandler().setCurrentUID(Math.max(highestDbUID,highestBinUID));
//			}
//
//			// Scenario 2: datablock uses only database
//			else if (dbAvailable && aDataBlock.getCanLog() && aDataBlock.getShouldLog() && dbData != null) {
//				
//				// check if the Data Block table name matches any of the table names from the Tracker Table
//				for (UIDTrackerData uidData:dbData) {
//					if (aDataBlock.getLogging().getTableDefinition().getTableName().equals(uidData.getName())) {
//						aDataBlock.getUidHandler().setCurrentUID(uidData.getUid());
//						break;
//					}
//				}
//			}
//
//			// Scenario 3: datablock uses only binary store
//			else if (binAvailable && aDataBlock.getShouldBinary(null)) {
//				
//				// check if the Data Block binary file header info matches any of the Tracker header info
//				String searchName = aDataBlock.getBinaryDataSource().createFilenamePrefix();
//				for (UIDTrackerData uidData:binData) {
//					if (searchName.equals(uidData.getName())){
//						aDataBlock.getUidHandler().setCurrentUID(uidData.getUid());
//						break;
//					}
//				}
//
//			}
//		}
		
		
		// loop through the data blocks and try to find matching database and binary file information
		this.synchUIDs(false);
		
		// Once we have all the information, delete the tracker table and/or binary store log file
		if (dbAvailable) {
			dbUIDFuncs.removeUIDTrackerTable();
		}
		if (binAvailable) {
			binUIDFuncs.removeLogFile();
		}
		return true;
	}

	/**
	 * Loop through the data blocks and try to find matching database and binary file UID information.  Assign the highest UID
	 * value found to the data block.  If the onlyNewDataBlocks flag is true, only perform this operation on data blocks which
	 * have a current UID of 0 (which will happen if the module has just been added). 
	 * 
	 * @param onlyNewDataBlocks
	 */
	@Override
	public void synchUIDs(boolean onlyNewDataBlocks) {
		
		// check for a database
		boolean dbAvailable = false;
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbAvailable=true;
		}
		
		// check for a binary store
		boolean binAvailable = false;
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore != null) {
			binAvailable=true;
		}
		
		// loop through the data blocks
		ArrayList<PamDataBlock> allDataBlocks = pamController.getDataBlocks();	
		for (PamDataBlock aDataBlock:allDataBlocks) {

			long highestDbUID = -1;
			long highestBinUID = -1;
			
			if (!onlyNewDataBlocks || (onlyNewDataBlocks && aDataBlock.getUidHandler().getCurrentUID()==0)) {
			
				// if the data block stores information in the database, try to find the last UID
				if (dbAvailable && aDataBlock.getCanLog() && aDataBlock.getShouldLog()) {
					highestDbUID = dbUIDFuncs.findMaxUIDforDataBlock(aDataBlock);
				}

				// if the data block stores information in the binary store, try to find the last UID
				if (binAvailable && aDataBlock.getShouldBinary(null)) {
					highestBinUID = binUIDFuncs.findMaxUIDforDataBlock(aDataBlock);
				}

				// select the largest value from the current data block UID, the database UID and the binary store UID
				long overallHighest = Math.max(Math.max(highestDbUID, highestBinUID),aDataBlock.getUidHandler().getCurrentUID());
				aDataBlock.getUidHandler().setCurrentUID(overallHighest);
			}
		}
	}

	
	@Override
	public boolean runShutDownOps() {
		
		// get list of all data blocks
		ArrayList<PamDataBlock> allDataBlocks = pamController.getDataBlocks();
		
		// Create a new table (via the DatabaseUIDFunctions class) to track UIDs.  We'll
		// create this table even if there weren't any datablocks saving data to the database,
		// (i.e. the table will be empty) because that way on the next startup, PAMGuard will
		// see the table and at least know that it shut down properly the last time and didn't crash.
		boolean dbTableCreated = dbUIDFuncs.saveUIDData(allDataBlocks);
		
		// Create a new binary store log file to track UIDs.  We'll
		// create this file even if there weren't any datablocks saving data to the binary store,
		// (i.e. the file will be empty) because that way on the next startup, PAMGuard will
		// see the table and at least know that it shut down properly the last time and didn't crash.
		boolean binLogCreated = binUIDFuncs.saveBinData(allDataBlocks);
		
		
		return true;
	}

}
