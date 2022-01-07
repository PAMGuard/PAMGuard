package PamguardMVC.uid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import com.sun.java.help.impl.SwingWorker;

import PamController.AWTScheduler;
import PamController.OfflineDataStore;
import PamController.PamController;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.uid.repair.UIDMessageInterface;
import PamguardMVC.uid.repair.UIDRepairFunctions;
import PamguardMVC.uid.repair.UIDRepairMessage;
import PamguardMVC.uid.repair.UIDRepairRequest;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLogging;

public class UIDViewerManager implements UIDManager {

	private PamController pamController;

	private DatabaseUIDFunctions dbUIDFuncs;

	public UIDViewerManager(PamController pamController) {
		this.pamController = pamController;
		dbUIDFuncs = new DatabaseUIDFunctions(pamController);
	}

	@Override
	public boolean runStartupChecks() {
		/**
		 * As we enter here, we may be in a number of possible states:
		 * 1. It's an old version of data without UID's in either the database or the binary files
		 * 2. It's a mixed set with UID's in one, but not the other. 
		 * 3. UID's are largely there, but some are missing or others are corrupted. 
		 * The first task is to quickly establish which of these three state's we're in. 
		 */
		AWTScheduler.getInstance().scheduleTask(new StartupChecks());
		return true;
	}

	private class StartupChecks extends javax.swing.SwingWorker<Integer, UIDRepairMessage> implements UIDMessageInterface{

		private UIDRepairFunctions uidRepair;

		@Override
		protected Integer doInBackground() throws Exception {			
			doStartupChecks();
			return null;
		}

		public void doStartupChecks() {
			long t = System.currentTimeMillis();
			//		UIDStatusReport generalStatus = getDatabaseState();
			OfflineDataStore binControl = PamController.getInstance().findOfflineDataStore(BinaryStore.class);
			ArrayList<UIDRepairRequest> repairList = makeRepairRequests();
			UIDStatusReport totalReport = checkDataMap(repairList, DBControlUnit.findDatabaseControl(), binControl);
			//			UIDStatusReport binState = checkDataMap(repairList, PamController.getInstance().findOfflineDataStore(BinaryStore.class));
			long t2 = System.currentTimeMillis();
			System.out.printf("Startup database checks took %d millis. Status = %s\n", t2-t, totalReport.toString());
			if (totalReport.getUidStatus() <= UIDStatusReport.UID_ALL_OK) {
				return;
			}

			/* 
			 * If some but not all of the data units have UIDs, there may have been a problem with a prior conversion.
			 * I'ts also possible that things are generally fine, but that more data have been added from a non UID source
			 * e.g. Decimus or a long term monitoring program where the remote PC collecting data has an old version of 
			 * PAMGuard that isn't writing UId's. In this case, we need to give the opportunity to incrementally update
			 * the UID's in place rather than making a complete data copy.     
			 */
			// Instead of simply launching
			// into the conversion process again, warn the user that they may have to fix the database manually
			if (totalReport.getUidStatus() == UIDStatusReport.UID_PARTIAL) {
				String msg = "<html>It appears that some, but not all, of your data have UID values.  Pamguard requires each data " + 
						"unit to have a proper UID, so any null values need to be corrected manually.  Please refer " +
						"to the console window for details on which database tables / binary stores hav emissing UID's.<p>" +
						"<p>If you are adding additional data from a remote source, then everything may be OK and you can update" +
						" UID's in place (without making a new folder).<p>" +
						"<p>If you have converted your data from the old system (Pamguard versions prior to 1.16.00), there may have " +
						"been errors in the process.  In order to keep UID numbers consistent during conversion, Pamguard tries to match data units " +
						"in the binary files with data units in the database.  Matching is done " +
						"by comparing UTC times between the two.  The most common problem encountered is an entry in the database that does " +
						"not have a match in the binary file, which leaves the UID value in the database null. " +
						"Before going any further, you need to examine your database and/or binary store to determine why a match was not found, " +
						"and fix any null values that appear."
						+ "<p><p>Click OK to continue to either repair or add missing UID's, or Cancel to sort it out yourself. </html>";
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), "UID values missing", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.OK_CANCEL_OPTION) {
					return;
				};
			}

			// otherwise, start the conversion process
			//				else {
			/** 
			 * trim the repair list. 
			 */
			repairList = UIDRepairRequest.trimRepairList(repairList);
			regenUIDs(repairList, totalReport);
			if (uidRepair!=null && uidRepair.errorsOccured()) {
				String msg = "<html>Errors occured while converting data.<p>" +
						"<p>In order to keep UID numbers consistent during conversion, Pamguard tries to match data units " +
						"in the binary files with data units in the database.  Matching is done " +
						"by comparing UTC times between the two.  The most common problem encountered is an entry in the database that does " +
						"not have a match in the binary file, which leaves the UID value in the database NULL.  Pamguard requires each data" +
						"unit to have a proper UID, so to prevent data corruption and make it easy for you to find the offending units, all null " + 
						"values have been replaced with negative numbers.  Before going any further, it is recommended that you examine your " + 
						"database and binary store to determine why a match was not found, and manually fix any UID values that are inconsistent.<p>" +
						"<p>Please refer to the console window for details on which database tables / binary stores contain errors.</html>";
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), "Error converting data", msg, WarnOnce.OK_OPTION);
				return;
			}

			String msg = "<html>Data has been converted.  It is recommended that you restart Pamguard now, in order to initialise all data blocks correctly.</html>";
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), "Conversion Complete", msg, WarnOnce.OK_OPTION);
			return;
			//				}

		}


		/**
		 * Some UID's are missing or absent, so need to regenerate. 
		 * @param totalReport 
		 * @param dbState state of the database.
		 * @param binState state of the binary store
		 */
		private void regenUIDs(ArrayList<UIDRepairRequest> repairRequests, UIDStatusReport totalReport) {
			uidRepair = new UIDRepairFunctions(pamController, this);
			boolean fixedOK = uidRepair.regenUIDs(repairRequests, totalReport);
		}

		@Override
		public void newMessage(UIDRepairMessage uidRepairMessage) {
			publish(uidRepairMessage);
		}

		@Override
		public void repairComplete(boolean repairOK) {
			publish(null);
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<UIDRepairMessage> msgs) {
			for (UIDRepairMessage msg:msgs) {
				if (uidRepair != null) {
					//					uidRepair.newRepairMessage(msg);
				}
			}
		}
	}


	@Override
	public boolean runShutDownOps() {
		return false;
	}

	private UIDStatusReport getDatabaseState() {

		ArrayList<PamDataBlock> allDataBlocks = pamController.getDataBlocks();

		PamConnection pamCon = null;
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			pamCon = dbControl.getConnection();
		}
		else {
			return null;
		}
		UIDStatusReport databaseReport = new UIDStatusReport(0, 0, 0, 0);

		// database checks. 
		if (pamCon != null) {
			boolean ok = true;
			for (PamDataBlock aDataBlock:allDataBlocks) {
				UIDStatusReport result = dbUIDFuncs.checkDataBlock(dbControl, pamCon, aDataBlock);
				// try to access the datamaps for this datablock and see what we can find there. 
				if (result != null) {
					databaseReport.addResult(result);
				}
			}
		}

		return databaseReport;
	}

	UIDStatusReport checkDataMap(ArrayList<UIDRepairRequest> repairList, OfflineDataStore dbStore, OfflineDataStore binStore) {
		UIDStatusReport fullReport = new UIDStatusReport(0, 0, 0, 0);
		for (UIDRepairRequest repairItem:repairList) {
//			if (repairItem.dataBlock.getDataName().contains("Clicks")) {
//				System.out.println("makeRepairRequests: Clicks");
//			}
			if (dbStore != null) {
				UIDStatusReport result = checkDataMap(dbStore, repairItem.dataBlock);
				// need to check to see if there are subtables which may have missing link data
				UIDStatusReport result2 = checkSubTables(dbStore, repairItem.dataBlock);
				if (result != null && result2 != null) {
					result.addResult(result2);
				}
				if (result != null) {
					fullReport.addResult(result);
				}
				if (result!=null && result.uidStatus > 1) {
					System.out.println( "* Database missing UID's in " + repairItem.dataBlock.getDataName());
				}
				repairItem.dbStatusReport = result;				
			}
			if (binStore != null) {
				UIDStatusReport result = checkDataMap(binStore, repairItem.dataBlock);
				fullReport.addResult(result);
				repairItem.binStatusReport = result;
				if (result!=null && result.uidStatus > 1) {
					System.out.println("* Binary Store missing UID's in " + repairItem.dataBlock.getDataName());
				}
			}
//			if (fullReport.uidStatus > 1) {
//				System.out.println("Missing UID's in " + repairItem.dataBlock.getDataName());
//			}
		}
		return fullReport;
	}

	/**
	 * Check for sub tables of the main tables in teh datablock. 
	 * @param dbStore database store
	 * @param dataBlock datablock. 
	 * @return updated status report. 
	 */
	private UIDStatusReport checkSubTables(OfflineDataStore dbStore, PamDataBlock aBlock) {
		if (aBlock.getLogging() == null) {
			return null;
		}
		if (aBlock instanceof SuperDetDataBlock == false) {
			return null;
		}
		SuperDetDataBlock dataBlock = (SuperDetDataBlock) aBlock;
		SQLLogging subLogging = dataBlock.getLogging().getSubLogging();
		if (subLogging == null) {
			return null;
		}
		// subtable should always extend PamSubtableDefinition. If it doesn't
		// then let it crash !
		PamSubtableDefinition subTable = (PamSubtableDefinition) subLogging.getTableDefinition();
		PamConnection pamCon = DBControlUnit.findConnection();
		if (pamCon == null) return null;
		
		// check to see that the table is correctly populated
		String sel = subTable.getBasicSelectString(pamCon.getSqlTypes());
		String qStr = sel + String.format(" WHERE %s = NULL OR %s = NULL OR %s = NULL", 
				PamSubtableDefinition.PARENTIDNAME, PamSubtableDefinition.PARENTUIDNAME, PamSubtableDefinition.LONGDATANAME);
//		System.out.println(qStr);
		int nNulls = 0;
		try {
			Statement stmt = pamCon.getConnection().createStatement();
			ResultSet resultSet = stmt.executeQuery(qStr);
			while (resultSet.next()) {
				nNulls++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (nNulls== 0) {
			return null;
		}
		return new UIDStatusReport(0, nNulls, 0);
	}

	UIDStatusReport checkDataMap(OfflineDataStore dataStore, PamDataBlock dataBlock) {
		OfflineDataMap dataMap = dataBlock.getOfflineDataMap(dataStore);
		if (dataMap == null) {
			return null;
		}
		UIDStatusReport result = new UIDStatusReport(dataMap.getDataCount()-dataMap.getMissingUIDs(), 
				dataMap.getMissingUIDs(), dataMap.getHighestUID() == null ? 0 : dataMap.getHighestUID());
		return result;
	}

	/**
	 * Make a list of all datablocks with a few extra bits of info about each. 
	 * @return
	 */
	public static ArrayList<UIDRepairRequest> makeRepairRequests() {
		ArrayList<UIDRepairRequest> rList = new ArrayList<>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aDataBlock:allDataBlocks) {
//			if (aDataBlock.getDataName().contains("Clicks")) {
//				System.out.println("makeRepairRequests: Clicks");
//			}
			rList.add(new UIDRepairRequest(aDataBlock));
		}
		return rList;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.uid.UIDManager#synchUIDs(boolean)
	 */
	@Override
	public void synchUIDs(boolean onlyNewDataBlocks) {
		// TODO Auto-generated method stub
		
	}
}
