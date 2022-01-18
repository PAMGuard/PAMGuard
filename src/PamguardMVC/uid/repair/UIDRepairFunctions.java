package PamguardMVC.uid.repair;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamController.PamViewParameters;
import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.uid.DataBlockUIDHandler;
import PamguardMVC.uid.UIDStatusReport;
import binaryFileStorage.BinaryDataSink;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.FileCopyObserver;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import dataMap.OfflineDataMap;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;

public class UIDRepairFunctions {

	private PamController pamController;
//	private UIDMessageInterface messageInterface;
	private UIDRepairDialog uidRepairDialog;
	private ArrayList<UIDRepairRequest> repairRequests;
	private Long startingNegUIDValue = -1L;
	private Long curNegUIDValue;

	public UIDRepairFunctions(PamController pamController, UIDMessageInterface messageInterface) {
		this.pamController = pamController;
//		this.messageInterface = messageInterface;
		this.curNegUIDValue=this.startingNegUIDValue;
	}

	/**
	 * Regenerate UID's offline - may have some use online too, so have kept this 
	 * out of the UIDViewerManager class. 
	 * @param totalReport 
	 * @param dbState state of the database
	 * @param binState state of the binary file system. 
	 * @return true if fixes completed successfully. 
	 */
	public boolean regenUIDs(ArrayList<UIDRepairRequest> repairRequests, UIDStatusReport totalReport) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean regenUIDs(UIDStatusReport dbState, UIDStatusReport binState) {
//		UIDStatusReport totalReport = new UIDStatusReport(0, 0, 0);
//		totalReport.addResult(dbState);
//		totalReport.addResult(binState);
		this.repairRequests = repairRequests;
		String msg = "<html>A new system of unique data identifiers (UID's) was introduced in version 1.16.00. <p>" + 
				"This requires that binary data are rewritten to a new data directory so that UID's can be added.<p>" +
				"Press Ok to continue or Cancel to quit</html>";
		int answer = WarnOnce.showWarning(pamController.getMainFrame(), "Data format change", msg, WarnOnce.OK_CANCEL_OPTION);
		if (answer == WarnOnce.CANCEL_OPTION) {
		String	msg2 = "<html>If you don't update your data format, it will not be possible to use PAMGuard offline data labelling functions</html>";
			int newAns = WarnOnce.showWarning(pamController.getMainFrame(), "Data format change", msg2, WarnOnce.OK_OPTION);
			return false;
		}
		
		// quick print out of which modules have both binary and database data. ...
		ArrayList<PamDataBlock> allDataBlocks = pamController.getDataBlocks();
		for (PamDataBlock aDataBlock:allDataBlocks) {
			int nStores = 0;
			Boolean hasDB = aDataBlock.getLogging() != null;
			Boolean hasBin = aDataBlock.getBinaryDataSource() != null;
			nStores += hasDB ? 1: 0;
			nStores += hasBin ? 1: 0;
//			System.out.printf("Data %s had database and binary stores: %d, %s %s\n", aDataBlock.getDataName(), nStores, hasDB.toString(), hasBin.toString());
		}
		
		uidRepairDialog = new UIDRepairDialog(pamController.getMainFrame(), pamController, this, totalReport);
		uidRepairDialog.setVisible(true);
		
		return false;
	}

	public void startUIDRepairJob(UIDRepairParams uidParams, UIDMessageInterface uidMessageInterface) {
		UIDRepairJob uidRepairJob = new UIDRepairJob(uidParams, uidMessageInterface);
		uidRepairJob.execute();
	}
	
	private class UIDRepairJob extends SwingWorker<Integer, UIDRepairMessage> implements UIDMessageInterface {

		private UIDRepairParams uidParams;
		private UIDMessageInterface uidMessageInterface;

		public UIDRepairJob(UIDRepairParams uidParams, UIDMessageInterface uidMessageInterface) {
			this.uidParams = uidParams;
			this.uidMessageInterface = uidMessageInterface;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				repairUIDs(uidParams, this);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void newMessage(UIDRepairMessage uidRepairMessage) {
			publish(uidRepairMessage);
		}

		@Override
		public void repairComplete(boolean repairOK) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<UIDRepairMessage> msgs) {
			for (UIDRepairMessage msg:msgs) {
				if (uidMessageInterface != null) {
					uidMessageInterface.newMessage(msg);
				}
			}
		}
		
	}
	private OfflineDataMap getDataMap(PamDataBlock dataBlock, OfflineDataStore dataStore) {
		if (dataStore == null) {
			return null;
		}
		OfflineDataMap oldm = dataBlock.getOfflineDataMap(dataStore);
		return oldm;
	}

	/**
	 * Called back from the dialog. Will somehow need to get messages back to the 
	 * dialog in the AWT thread from within here. All very confusing as to 
	 * which thread we are now in !
	 * @param uidParams
	 * @param uidMessageInterface 
	 */
	public void repairUIDs(UIDRepairParams uidParams, UIDMessageInterface uidMessageInterface) {
		// yet again work out if we've a bin store or just a database. 
		BinaryStore binaryStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		// will also have to check for every data block whether it's got either or both binary and database. 
		float totalAllDataUnits = 0;
		float totalProcessedUnits = 0;
		for (UIDRepairRequest rq:repairRequests) {
			OfflineDataMap bestMap = rq.dataBlock.getPrimaryDataMap();
			OfflineDataMap binMap = getDataMap(rq.dataBlock, binaryStore);
			OfflineDataMap dbMap = getDataMap(rq.dataBlock, dbControl);
			if (bestMap != null && !(binMap == null && dbMap == null)) {
				totalAllDataUnits += bestMap.getDataCount();
			}
		}

		for (UIDRepairRequest rq:repairRequests) {
			PamDataBlock aDataBlock = rq.dataBlock;
//			System.out.println("Repair datablock " + aDataBlock.getDataName());
			OfflineDataMap binMap = getDataMap(aDataBlock, binaryStore);
			OfflineDataMap dbMap = getDataMap(aDataBlock, dbControl);
			if (binMap == null && dbMap == null) continue;
			uidMessageInterface.newMessage(new UIDRepairMessage(UIDRepairMessage.TYPE_BLOCKPROGRESS, aDataBlock, 0, 0));
			if (binMap != null && binMap.getDataCount() > 0) {
				repairBinaryStore(uidMessageInterface, aDataBlock, uidParams, binaryStore, dbControl);
			}
			else {
				repairDatabaseOnly(uidMessageInterface, aDataBlock, uidParams, dbControl);
			}
			/**
			 * Database subform repair - for data without any UID this really only applies to the 
			 * offline click event stuff, so should be striaght forward, but will still need to 
			 * be done in a general way. 
			 */
			if (aDataBlock instanceof SuperDetDataBlock) {
				doSubTableRepairs((SuperDetDataBlock) aDataBlock);
			}
			OfflineDataMap bestMap = aDataBlock.getPrimaryDataMap();
			if (bestMap != null) {
				totalProcessedUnits += bestMap.getDataCount();
				int perc = (int) ((100.* totalProcessedUnits) / totalAllDataUnits);
//				System.out.println("Processed = " + totalProcessedUnits + "; Total = " + totalAllDataUnits + "; Percent = " + perc);
				uidMessageInterface.newMessage(new UIDRepairMessage(UIDRepairMessage.TYPE_TOTALPROGRESS, perc));
			}
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		uidMessageInterface.newMessage(null);
		if (binaryStore != null) {
			binaryStore.getBinaryStoreSettings().setStoreLocation(uidParams.newBinaryFolder);
		}
	}

	private void repairBinaryStore(UIDMessageInterface uidMessageInterface, PamDataBlock dataBlock, UIDRepairParams uidParams, BinaryStore binaryStore, DBControlUnit dbControl) {
		BinaryOfflineDataMap binMap = (BinaryOfflineDataMap) getDataMap(dataBlock, binaryStore);
		int nPoints = binMap.getNumMapPoints();
		List<BinaryOfflineDataMapPoint> mapPoints = binMap.getMapPoints();
		int iPoint = 0;
		DataBlockUIDHandler uidHandler = dataBlock.getUidHandler();
		uidHandler.setCurrentUID(0);
		SQLLogging logging = dataBlock.getUIDRepairLogging();
		
		// make sure the table and all it's columns exist, then clear the UID column so we can start clean
		if (logging != null) {
			DBControlUnit.findDatabaseControl().getDbProcess().checkTable(logging.getTableDefinition());
			String qStr = String.format("UPDATE %s SET UID = NULL", logging.getTableDefinition().getTableName());
			try {
				Statement stmt = DBControlUnit.findDatabaseControl().getConnection().getConnection().createStatement();
				boolean result = stmt.execute(qStr);
				DBControlUnit.findDatabaseControl().commitChanges();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		// loop through the binary files one at a time, adding UIDs and matching the database rows 
		for (BinaryOfflineDataMapPoint mapPoint:mapPoints) {
			UIDRepairMessage uidMessage = new UIDRepairMessage(UIDRepairMessage.TYPE_BLOCKPROGRESS, dataBlock, nPoints, iPoint);
			uidMessageInterface.newMessage(uidMessage);
//			if (nDBRecs >= 0) {
//				System.out.printf("Stream %s has %d points between %s and %s\n", dataBlock.getDataName(), nDBRecs, 
//						PamCalendar.formatDateTime(mapPoint.getStartTime()), PamCalendar.formatTime(mapPoint.getEndTime()));
//			}
			/*
			 *  here is the point at which we have to copy a file and while
			 *  doing so, introduce UID's and possibly make other changes to 
			 *  the file format. 
			 */
			int nDataUnits = 0;
			if (mapPoint.getBinaryFooter() != null) {
				nDataUnits = mapPoint.getBinaryFooter().getNObjects();
			}
			boolean copyOk = copyDataFile(binaryStore, dataBlock, mapPoint, uidParams, true, new FileCopyMonitor(uidMessageInterface, nDataUnits));
			long uidVal = uidHandler.getCurrentUID();
			uidHandler.roundUpUID(DataBlockUIDHandler.ROUNDINGFACTOR);
			long uidVal2 = uidHandler.getCurrentUID();
			if (uidVal2 < uidVal) {
				System.out.printf("UID numbers jumping backwards. Now %d was %d", uidVal2, uidVal);
			}
			iPoint++;
		}
		
		// we've gone through all the binary files now.  If there was a database for this datablock as well, run a quick check to see
		// if there are any null values left for the UID.  If so, it means Pamguard couldn't find a match for that unit in the binary
		// file.  Null values might screw processing up later, so set the UIDs to negative numbers and continue.  Flag this, so that
		// we can warn the user at the end.
		if (logging != null) {
			linkedDatabaseUpdate = new LinkedDatabaseUpdate(dataBlock);
			linkedDatabaseUpdate.loadUIDNulls(DBControlUnit.findConnection());
			Long newValue = linkedDatabaseUpdate.replaceUIDsWithNegatives(curNegUIDValue);
			if (newValue < curNegUIDValue) {
				System.out.printf("Error converting datablock %s: Null UID values replaced with negative numbers %d to %d%n",
						dataBlock.getDataName(),curNegUIDValue,newValue);
				curNegUIDValue=newValue;
				linkedDatabaseUpdate.updateDatabase();
				DBControlUnit.findDatabaseControl().commitChanges();
			}
		}
		
		// Update the repair message and continue to the next binary file
		UIDRepairMessage uidMessage = new UIDRepairMessage(UIDRepairMessage.TYPE_BLOCKPROGRESS, dataBlock, nPoints, nPoints);
		uidMessageInterface.newMessage(uidMessage);
	}
	
	/**
	 * See how many database points there are within a data map time frame. 
	 * @param dataBlock
	 * @param mapPoint
	 * @return number of entries in the database (might just return 1 after a while ?)
	 */
	private int countDatabasePoints(PamDataBlock dataBlock, BinaryOfflineDataMapPoint mapPoint) {
		SQLLogging logging = dataBlock.getUIDRepairLogging();
		if (logging == null) {
			return -1;
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return -1;
		}

		/*
		 * Now just have to query the database and then update every record with a nice new UID. Might as well set these
		 * to be the same as the Id values since we know they will be unique. 
		 */
		PamConnection con = dbControl.getConnection();
		SQLTypes sqlTypes = con.getSqlTypes();
		PamViewParameters pvp = new PamViewParameters();
		pvp.setViewStartTime(mapPoint.getStartTime());
		pvp.setViewEndTime(mapPoint.getEndTime());

		// change this string - instead of searching between two times, search instead
		// for entries less than the end time and where UID equals null
//		String qStr = logging.getViewerLoadClause(sqlTypes, pvp);
		String qStr = logging.getViewerLessThanClause(sqlTypes, pvp, true, "UID");
		qStr = String.format("SELECT Id, UID FROM %s %s", 
				logging.getTableDefinition().getTableName(), qStr);
//		System.out.println(qStr);
		int nRecords = 0;
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			while (result.next()) {
				nRecords++;
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return nRecords;
	}
	long preceedingUID = 0;
	/**
	 * Copy a data file. It will be rewritten in the latest file format to a new directory. 
	 * @param mapPoint Datamap point with information about the file. 
	 * @param uidParams params controlling the copy
	 * @param regenerateUIDs regenerate UID's 
	 * @param fileCopyObserver Observer for file progress information. 
	 */
	private LinkedDatabaseUpdate linkedDatabaseUpdate;
	public boolean copyDataFile(BinaryStore binaryStore, PamDataBlock dataBlock, BinaryOfflineDataMapPoint mapPoint, 
			UIDRepairParams uidParams, boolean regenerateUIDs, FileCopyObserver fileCopyObserver) {
		BinaryDataSource dataSource = dataBlock.getBinaryDataSource();
		int nDBRecs = countDatabasePoints(dataBlock, mapPoint);
//		nDBRecs = 0;
		File inFile = mapPoint.getBinaryFile(binaryStore);
		if (inFile == null || inFile.exists() == false) {
			return false;
		}
//		Debug.out.printf("Processing fil?e %s\n", mapPoint.getBinaryFile(""));
		if (true && uidParams.doPartial && mapPoint.getHighestUID() != null) {
//			Debug.out.printf("No missing ?UID's in file %s\n", inFile.getName());
			DataBlockUIDHandler uidHandler = dataBlock.getUidHandler();
			if (mapPoint.getHighestUID() > 0) {
				uidHandler.setCurrentUID(mapPoint.getHighestUID());
			}
			return true;
		}
		/** 
		 * output file names for temp an dfinal files. will need to change this to a temp file
		 */
		File outFile = mapPoint.getBinaryFile(uidParams.newBinaryFolder);
		File tempFile = new File(outFile.getAbsolutePath()+".temp");
//		File outFile = mapPoint.getBinaryFile(uidParams.newBinaryFolder);
		// check the output folder exists. 
		File folder = FileFunctions.createNonIndexedFolder(uidParams.newBinaryFolder);
		if (folder==null || folder.exists()==false) {
			return false;
		}
		// also need to check the sub folder which will be part of the main file name. 
		File parentFolder = tempFile.getParentFile();
		if (parentFolder.exists() == false) {
			if (parentFolder.mkdirs() == false) {
				return false;
			};
			FileFunctions.setNonIndexingBit(parentFolder);
		}
		// create the data output stream. 
		BinaryOutputStream binaryOutputStream = new BinaryOutputStream(binaryStore, dataBlock);
		dataSource.setBinaryStorageStream(binaryOutputStream);
		boolean fOpen = binaryOutputStream.openPGDFFile(tempFile);
		if (fOpen == false) return false;

		SQLLogging logging = dataBlock.getUIDRepairLogging();
		if (nDBRecs > 0 && logging != null) {
			linkedDatabaseUpdate = new LinkedDatabaseUpdate(dataBlock);
			linkedDatabaseUpdate.loadFileDatabaseData(DBControlUnit.findConnection(), mapPoint);
		}
		else {
			linkedDatabaseUpdate = null;
		}
		
		DataCopySink copySink = new DataCopySink(binaryStore, mapPoint, dataSource, nDBRecs>0, binaryOutputStream, fileCopyObserver);
		boolean loadOk = binaryStore.loadData(dataBlock, mapPoint, Long.MIN_VALUE, Long.MAX_VALUE, copySink);
		binaryOutputStream.closeFile();
		/**
		 * Hopefully both the original file and the temp file are now closes, so can
		 * copy one in place of the other
		 */
		if (loadOk) {
			try {
				outFile.delete();
			}
			catch (SecurityException e) {
				System.out.printf("UID Repair: Error deleting old file %s\n", outFile.getPath());
			}
			try {
				loadOk = tempFile.renameTo(outFile);
			}
			catch (SecurityException e) {
				System.out.printf("UID Repair: Error renaming file %s to %d\n", tempFile.getPath(), outFile.getName());
			}
		}
		String fileName = outFile.getAbsolutePath();
		String indexName = fileName.substring(0, fileName.length()-4) + "pgdx";
		binaryStore.rewriteIndexFile(dataBlock, mapPoint, new File(indexName));
		if (nDBRecs > 0) {
//			System.out.printf("Updated %d database records for %s %s to %s\n", nDBRecs, dataBlock.getDataName(),
//					PamCalendar.formatDateTime(mapPoint.getStartTime()), PamCalendar.formatTime(mapPoint.getEndTime()));
			if (linkedDatabaseUpdate != null) {
				linkedDatabaseUpdate.updateDatabase();
			}
			DBControlUnit.findDatabaseControl().commitChanges();
		}
		
		return loadOk;
	}

	private class DataCopySink implements BinaryDataSink {

		private BinaryOutputStream binaryOutputStream;
		private BinaryDataSource dataSource;
		int unitsCopied = 0;
		private FileCopyObserver fileCopyObserver;
		private BinaryObjectData dataGramData;
		private BinaryOfflineDataMapPoint mapPoint;
		private long firstUID;
		private long lastUID;
		private DataBlockUIDHandler uidHandler;
		private boolean updateDatabase;
		private PamConnection dbCon;
		private int dbUpdates = 0;
		private BinaryStore binaryStore;

		public DataCopySink(BinaryStore binaryStore, BinaryOfflineDataMapPoint mapPoint, BinaryDataSource dataSource, 
				boolean updateDatabase, BinaryOutputStream binaryOutputStream, FileCopyObserver fileCopyObserver) {
			this.binaryStore = binaryStore;
			this.mapPoint = mapPoint;
			this.dataSource = dataSource;
			this.updateDatabase = updateDatabase;
//			if (updateDatabase) {
//				System.out.println("Include database update for stream " + dataSource.getSisterDataBlock().getLongDataName());
//			}
			this.binaryOutputStream = binaryOutputStream;			
			this.fileCopyObserver = fileCopyObserver;
			uidHandler = dataSource.getSisterDataBlock().getUidHandler();
			firstUID = lastUID = uidHandler.getCurrentUID();
			dbCon = DBControlUnit.findConnection();
		}

		@Override
		public void newFileHeader(BinaryHeader binaryHeader) {
			int inputFormat = binaryHeader.getHeaderFormat();
			binaryHeader.setHeaderFormat(binaryStore.getCurrentFileFormat());
			
			binaryOutputStream.writeHeader(binaryHeader.getDataDate(), binaryHeader.getAnalysisDate());
			binaryHeader.setHeaderFormat(inputFormat);
		}

		@Override
		public void newModuleHeader(BinaryObjectData binaryObjectData, ModuleHeader moduleHeader) {
			byte[] moduleHeaderData = dataSource.getModuleHeaderData();
			binaryOutputStream.writeModuleHeader(moduleHeaderData);
//			binaryOutputStream.storeData(binaryObjectData);
		}

		@Override
		public void newModuleFooter(BinaryObjectData binaryObjectData, ModuleFooter moduleFooter) {
			byte[] moduleFooterData = dataSource.getModuleFooterData();
			binaryOutputStream.writeModuleFooter(moduleFooterData);
//			binaryOutputStream.storeData(binaryObjectData);
		}

		@Override
		public void newFileFooter(BinaryObjectData binaryObjectData, BinaryFooter binaryFooter) {
			/*
			 * Will need to add the first and last UID's of these data before 
			 * writing the footer and will also have to restore this back in the data mpa point. 
			 */
			binaryFooter.setLowestUID(firstUID);
			binaryFooter.setHighestUID(lastUID);
			mapPoint.setBinaryFooter(binaryFooter);
			binaryOutputStream.writeFileFooter(binaryFooter);
		}

		@Override
		public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit) {
//			dataSource.saveData(dataUnit);
			// should probably convert to and back from the data unit to incorporate other format changes. 
			lastUID = uidHandler.getNextUID(dataUnit);
			binaryObjectData.getDataUnitBaseData().setUID(lastUID);
			dataUnit.setUID(lastUID);
//			System.out.println("***Setting UID = " + String.valueOf(lastUID));
			
			if (dataUnit.getUID() <= preceedingUID) {
				System.out.printf("UID jumped back from %d to %d in %s\n", preceedingUID, dataUnit.getUID(), dataUnit.getSummaryString());
			}
			preceedingUID = dataUnit.getUID();
			
			if (linkedDatabaseUpdate != null) {
				linkedDatabaseUpdate.updateCursorRecord(dataUnit);
//				dbUpdates += updateDatabaseRecord(dbCon, dataBlock, dataUnit);
			}
//			binaryOutputStream.storeData(binaryObjectData);
			dataBlock.getBinaryDataSource().saveData(dataUnit);
			unitsCopied++;
			fileCopyObserver.copyProgress(null, null, unitsCopied);
			return true;
		}

		@Override
		public void newDatagram(BinaryObjectData binaryObjectData) {
			this.dataGramData = binaryObjectData;
		}

	}

	/**
	 * Update a record in the database so that it has the same UID as the data in the binary store. 
	 * @param dbCon database connection
	 * @param dataBlock Datablock
	 * @param dataUnit data unit
	 */
	private int updateDatabaseRecord(PamConnection dbCon, PamDataBlock dataBlock, PamDataUnit dataUnit) {
		SQLLogging logging = dataBlock.getUIDRepairLogging();
		String updateClause = logging.getUIDMatchClause(dataUnit, dbCon.getSqlTypes());
		String uStr = String.format("UPDATE %s SET UID=%d WHERE %s", 
				logging.getTableDefinition().getTableName(), dataUnit.getUID(), updateClause);
		Statement uStmt;
		try {
			uStmt = dbCon.getConnection().createStatement();
			uStmt.execute(uStr);
			uStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 *  try a select string to see if we're actually managing to match anything
		 */
//		String qStr = String.format("SELECT Id, UID FROM %s WHERE %s", logging.getTableDefinition().getTableName(), updateClause);
//		int nFound = 0;
//		try {
//			Statement stmt = dbCon.getConnection().createStatement();
//			ResultSet r = stmt.executeQuery(qStr);
//			while (r.next()) {
//				nFound++;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return 0;
	}
	
	private class FileCopyMonitor implements FileCopyObserver {
		private UIDMessageInterface uidMessageInterface;
		private int nFileDataUnits;

		/**
		 * @param uidMessageInterface
		 * @param nFileDataUnits 
		 */
		public FileCopyMonitor(UIDMessageInterface uidMessageInterface, int nFileDataUnits) {
			super();
			this.uidMessageInterface = uidMessageInterface;
			this.nFileDataUnits = nFileDataUnits;
		}

		@Override
		public boolean copyProgress(File source, File dest, int unitsCopied) {
			int perc; 
			if (nFileDataUnits!=0)  perc = (100*unitsCopied) / nFileDataUnits;
			else perc=100; 
//			System.out.printf("File %s %3.1f%% complete\n", source.getName(), percentComplete);
			uidMessageInterface.newMessage(new UIDRepairMessage(UIDRepairMessage.TYPE_FILEPROGRESS, perc));
			return true;
		}
	}
	private void repairDatabaseOnly(UIDMessageInterface uidMessageInterface, PamDataBlock dataBlock, UIDRepairParams uidParams, DBControlUnit dbControl) {
		SQLLogging sqlLogging = dataBlock.getLogging();
		if (sqlLogging == null || dbControl == null) {
			return;
		}
		/*
		 * Now just have to query the database and then update every record with a nice new UID. Might as well set these
		 * to be the same as the Id values since we know they will be unique. 
		 */
		PamConnection con = dbControl.getConnection();
		SQLTypes sqlTypes = con.getSqlTypes();
		String q = String.format("UPDATE %s SET UID = Id", sqlTypes.formatColumnName(sqlLogging.getTableDefinition().getTableName()));
//		System.out.println(q);
		boolean r = false;
		if (con == null) {
			return;
		}
		try {
			Statement stmt = con.getConnection().createStatement();
			r = stmt.execute(q);
			dbControl.commitChanges();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Database subform repair - for data without any UID this really only applies to the 
	 * offline click event stuff, so should be straight forward, but will still need to 
	 * be done in a general way. 
	 * @param aDataBlock
	 */
	private void doSubTableRepairs(SuperDetDataBlock aDataBlock) {
		 SuperDetLogging sqlLogging = aDataBlock.getLogging();
		if (sqlLogging == null) {
			return;
		}
		SQLLogging subTableLogging = sqlLogging.getSubLogging();
		if (subTableLogging == null) {
			return;
		}
		sqlLogging.doSubTableUIDRepairs();
	}
	
	public void resetNegativeUIDCounter() {
		this.curNegUIDValue=this.startingNegUIDValue;
	}
	
	public boolean errorsOccured() {
		if (curNegUIDValue<startingNegUIDValue) {
			return true;
		} else {
			return false;
		}
	}


}
