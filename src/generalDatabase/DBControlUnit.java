package generalDatabase;


import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import generalDatabase.backup.DatabaseBackupStream;
import pamScrollSystem.ViewLoadObserver;
import pamViewFX.pamTask.PamTaskUpdate;
import PamController.AWTScheduler;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.status.ModuleStatus;
import PamController.status.QuickRemedialAction;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import backupmanager.BackupInformation;

/**
 * Version of DBControl for normal use while PAMGUARD is running 
 * - reading and writing of data
 * @author Doug Gillespie
 * @see DBControl
 *
 */
public class DBControlUnit extends DBControl implements OfflineDataStore {


	
	private DBControlUnit THIS;
	private boolean initialisationComplete;
	
	private BackupInformation backupInformation;

	public DBControlUnit(String unitName) {
		super(unitName, whichStore(), true);
		THIS = this;
		setFullTablesCheck(true);
		//		int runMode = PamController.getInstance().getRunMode();
		//		if (runMode == PamController.RUN_MIXEDMODE ||
		//				runMode == PamController.RUN_PAMVIEW) {
		//			PamSettingManager.getInstance().registerSettings(this, PamSettingManager.LIST_DATABASESTUFF);
		//		}
		backupInformation = new BackupInformation(new DatabaseBackupStream(this));
	}

	private static int whichStore() {
		if (PamController.getInstance() == null) {
			return 0;
		}
		int runMode = PamController.getInstance().getRunMode();
		if (runMode == PamController.RUN_MIXEDMODE ||
				runMode == PamController.RUN_PAMVIEW) {
			return PamSettingManager.LIST_DATABASESTUFF;
		}
		else {
			return PamSettingManager.LIST_UNITS;
		}
	}

	/**
	 * GEt a list of keywords which might cause havoc 
	 * in SQL statements if they are used as columnn names. 
	 * These are returned on a dbms by dbms basis since they 
	 * may vary or be overridden through work arounds such 
	 * as wrapping names in "". 
	 * @return
	 */
	private String getKeywords() {
		return getDatabaseSystem().getKeywords();
	}

	/* (non-Javadoc)
	 * @see generalDatabase.DBControl#selectSystem(int, boolean)
	 */
	@Override
	public boolean selectSystem(int systemNumber, boolean openDatabase) {
		boolean ans =  super.selectSystem(systemNumber, openDatabase);
		if (ans && PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			createOfflineDataMap(null);
		}		
		return ans;
	}


	/* (non-Javadoc)
	 * @see generalDatabase.DBControl#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			if (isViewer) {
				createOfflineDataMap(null);
			}
			getDbProcess().checkTables();
			break;
		case PamController.ADD_DATABLOCK:
			if (initialisationComplete) {
				getDbProcess().checkTables();
			}
			break;
		case PamController.ADD_CONTROLLEDUNIT:
			if (initialisationComplete) {
				PamController pc = PamController.getInstance();
				int nUnit = pc.getNumControlledUnits();
				if (nUnit > 0) {
					PamControlledUnit newestUnit = pc.getControlledUnit(nUnit-1);
					createOfflineDataMap(null, newestUnit);
				}
			}
			
			break;
		}
	}

	//	/* (non-Javadoc)
	//	 * @see generalDatabase.DBControl#selectDatabase(java.awt.Frame)
	//	 */
	//	@Override
	//	protected boolean selectDatabase(Frame frame) {
	//		// TODO Auto-generated method stub
	//		boolean ans =  super.selectDatabase(frame);
	//		if (ans && PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
	//			createOfflineDataMap(frame);
	//		}
	//		return ans;
	//	}

	/**
	 * update the datamap for a single data block. 
	 * @param pamDataBlock
	 */
	public void updateDataMap(PamDataBlock pamDataBlock) {
		if (pamDataBlock == null) {
			return;
		}
		if (!isViewer) {
			return;
		}
		pamDataBlock.removeOfflineDataMap(THIS);
		ArrayList<PamDataBlock> al = new ArrayList<PamDataBlock>();
		al.add(pamDataBlock);

		AWTScheduler.getInstance().scheduleTask(new CreateDataMap(al));
	}
	
	/**
	 * Update the data map for an array list of data blocks. 
	 * @param pamDataBlock
	 */
	public void updateDataMap(ArrayList<PamDataBlock> pamDataBlocks) {
		
		if (pamDataBlocks == null) {
			return;
		}
		if (!isViewer) {
			return;
		}
		
		ArrayList<PamDataBlock> updateDataBlocks=new ArrayList<PamDataBlock>();
		for (int i=0; i<pamDataBlocks.size(); i++){
			if (pamDataBlocks.get(i)==null || pamDataBlocks.get(i).getNumOfflineDataMaps()<1) continue;
			pamDataBlocks.get(i).removeOfflineDataMap(THIS);
			updateDataBlocks.add(pamDataBlocks.get(i));
		}
		
		AWTScheduler.getInstance().scheduleTask(new CreateDataMap(updateDataBlocks));
		
	}
	

	@Override
	public void createOfflineDataMap(Window parentFrame) {
		if (getConnection() == null) {
			return;
		}
		if (!isViewer) {
			return;
		}

		AWTScheduler.getInstance().scheduleTask(new CreateDataMap(getLoggingDataBlocks()));
	}
	
	/**1
	 * Create offline datamap when modules are added in viewer mode AFTER initialisation. 
	 * @param parentFrame not used
	 * @param pamControlledUnit unit added. 
	 */
	public void createOfflineDataMap(Window parentFrame, PamControlledUnit pamControlledUnit) {
		// get a list of all datablocks in that unit. 
		ArrayList<PamDataBlock> loggingBlocks = new ArrayList<>();
		for (int i = 0; i < pamControlledUnit.getNumPamProcesses(); i++) {
			PamProcess pamProcess = pamControlledUnit.getPamProcess(i);
			for (int iB = 0; iB < pamProcess.getNumOutputDataBlocks(); iB++) {
				PamDataBlock dataBlock = pamProcess.getOutputDataBlock(iB);
				if (dataBlock.getLogging() != null) {
					loggingBlocks.add(dataBlock);
				}
			}
		}
		if (loggingBlocks.size() > 0) {
			AWTScheduler.getInstance().scheduleTask(new CreateDataMap(loggingBlocks));
		}
	}
	
	/**
	 * Function to map a single new datablock. 
	 * @param parentFrame parent frame for dialog
	 * @param dataBlock datablock to map. 
	 */
	public void mapNewDataBlock(Window parentFrame, PamDataBlock dataBlock) {
		ArrayList<PamDataBlock> oneDataBlock = new ArrayList<PamDataBlock>();
		oneDataBlock.add(dataBlock);
		AWTScheduler.getInstance().scheduleTask(new CreateDataMap(oneDataBlock));
	}
	
	/**
	 * Map a list of data blocks. 
	 * @param parentFrame parent frame for dialog
	 * @param dataBlocks Array list of datablocks. 
	 */
	public void mapNewDataBlock(Window parentFrame, ArrayList<PamDataBlock> dataBlocks) {
		AWTScheduler.getInstance().scheduleTask(new CreateDataMap(dataBlocks));
	}

	class CreateDataMap extends SwingWorker<Integer, CreateMapInfo> {

		private ArrayList<PamDataBlock> loggingBlocks;
		private DBMapMakingDialog dbMapDialog;

		/**
		 * @param loggingBlocks
		 */
		public CreateDataMap(ArrayList<PamDataBlock> loggingBlocks) {
			super();
			this.loggingBlocks = loggingBlocks;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				String dbName = databaseSystem.getShortDatabaseName();
				publish(new CreateMapInfo(loggingBlocks.size(), dbName));
				PamConnection con = DBControlUnit.findConnection();
				if (con == null || loggingBlocks == null) return null;
				for (int i = 0; i < loggingBlocks.size(); i++) {
//					System.out.println("Create datamap point: " + i + " "+ dbName); 
					mapDataBlock(con.getSqlTypes(), i);

				}
			}
			catch (Exception e){
				System.out.println("Error in Database CreateDataMap SwingWorker thread");
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Make a data map of a datablock using one hour time intervals. 
		 * @param iBlock index of datablock in a list of logging data blocks.
		 */
		private void mapDataBlock(SQLTypes sqlTypes, int iBlock) {

			PamDataBlock pamDataBlock = loggingBlocks.get(iBlock);
			if (pamDataBlock.getOfflineDataMap(THIS) != null) {
				/*
				 * Already have a data map of this type, so don't create. 
				 */
				return; 
			}
			DBOfflineDataMap dataMap = new DBOfflineDataMap(THIS, pamDataBlock); 
			pamDataBlock.addOfflineDataMap(dataMap);
			
			
			SQLLogging sqlLogging = pamDataBlock.getLogging();
			if (sqlLogging == null) {
				System.out.println("null SQLLogging in " + pamDataBlock.getDataName());
				return;
			}
			getDbProcess().checkTable(sqlLogging);
			if (sqlLogging.getTableDefinition() == null) {
				System.out.println("null table definition in " + pamDataBlock.getDataName());
				return;				
			}
			if (sqlLogging.getTableDefinition().getTableName() == null) {
				System.out.println("null table name in " + pamDataBlock.getDataName());
				return;				
			}
			publish(new CreateMapInfo(iBlock, pamDataBlock, sqlLogging.getTableDefinition().getTableName()));
			String sql = String.format("SELECT UTC, UTCMilliseconds, UID FROM %s WHERE UTC IS NOT NULL ORDER BY UTC, UTCMilliseconds ",
					sqlLogging.getTableDefinition().getTableName());
			//			System.out.println("Mapping database " + sql);
			PamConnection con = getConnection();
			Object timestamp;
			DBOfflineDataMapPoint dataMapPoint = null;
			long mapInterval = 3600000L;
			long utcMillis;
			Integer actualMillis;
			try {
				PreparedStatement stmt = con.getConnection().prepareStatement(sql);
				ResultSet resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					timestamp = resultSet.getObject(1);//  getTimestamp(1);
					actualMillis = (Integer) resultSet.getObject(2);
					utcMillis = sqlTypes.millisFromTimeStamp(timestamp);
					if (utcMillis % 1000 == 0 && actualMillis != null && dataMapPoint != null) {
						/*
						 * dataMapPoint == null is indicate of it being the first map point in 
						 * which case we want to round down to the nearest second in any case
						 * so this line get's skipped. 
						 */
						utcMillis += actualMillis;
					}
					Long uid = sqlTypes.getLongValue(resultSet.getObject(3));
					if (dataMapPoint == null) {
						dataMapPoint = new DBOfflineDataMapPoint(utcMillis, utcMillis, 1, uid, uid, uid == null ? 1: 0);
					}
					else if (utcMillis - dataMapPoint.getStartTime() > mapInterval) {
						dataMap.addDataPoint(dataMapPoint);
						dataMapPoint = new DBOfflineDataMapPoint(utcMillis, utcMillis, 1, uid, uid, uid == null ? 1: 0);
					}
					else {
						dataMapPoint.addNewEndTime(utcMillis, uid);
					}
				}
				if (dataMapPoint != null) { // add the last data map point. 
					dataMap.addDataPoint(dataMapPoint);
					/*
					 * Round up the time of the last data map point to the nearest second. 
					 */
					long currentEnd = dataMapPoint.getEndTime();
					long lastMillis = currentEnd%1000;
					dataMapPoint.setEndTime(currentEnd + (1000L - lastMillis));
				}
			} catch (SQLException e) {
				System.out.println("Error executing SQL " + sql);
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			super.done();
			if (PamGUIManager.isSwing()) {
				if (dbMapDialog != null) {
					dbMapDialog.setVisible(false);
				}
			}
//			System.out.println("Create datamap point: DONE"); 
			PamController.getInstance().notifyTaskProgress(new CreateMapInfo(PamTaskUpdate.STATUS_DONE));
			PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_OFFLINE_DATASTORE);
//			System.out.println("Create datamap point: DONE2"); 
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<CreateMapInfo> dataList) {
			if (PamGUIManager.isSwing()) {
				if (dbMapDialog == null) {
					dbMapDialog = DBMapMakingDialog.showDialog(null);
				}
				for (int i = 0; i < dataList.size(); i++) {
					dbMapDialog.newData(dataList.get(i));
				}
			}
			else {
				for (int i = 0; i < dataList.size(); i++) {
					PamController.getInstance().notifyTaskProgress(dataList.get(i));
				}
			}
		}

	}

	@Override
	public String getDataSourceName() {
		return getUnitName();
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		SQLLogging logging = dataBlock.getLogging();
		if (logging == null) {
			return false;
		}
		return logging.loadViewerData(offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis(), loadObserver);
	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		SQLLogging logging = dataBlock.getLogging();
		if (logging == null) {
			return false;
		}
		return logging.saveOfflineData(this, getConnection());
	}

	/**
	 * Find the database connection
	 * @return the database connection or null if there is either no database
	 * module loaded or no open database. 
	 */
	public static PamConnection findConnection() {
		DBControlUnit dbControl = findDatabaseControl();
		if (dbControl == null) {
			return null;
		}
		return dbControl.getConnection();
	}

	/**
	 * Find the database controller
	 * @return database controller, or null if no database module loaded. 
	 */
	public static DBControlUnit findDatabaseControl() {
		
		DBControlUnit dbc = (DBControlUnit) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		
		
		return dbc;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock,
			OfflineDataMapPoint dmp) {
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		return null;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getModuleStatus()
	 */
	@Override
	public ModuleStatus getModuleStatus() {
		PamConnection con = getConnection();
		ModuleStatus moduleStatus;
		if (con == null) {
			moduleStatus = new ModuleStatus(ModuleStatus.STATUS_ERROR, "No database connection");
			moduleStatus.setRemedialAction(new QuickRemedialAction(this, "Fix database connection", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDatabase(getGuiFrame(), "Fix database connection");
				}
			}));
			return moduleStatus;
		}
		if (getLastErrorCount() > 0) {
			moduleStatus = new ModuleStatus(ModuleStatus.STATUS_ERROR, "Database write errors");
			moduleStatus.setRemedialAction(new QuickRemedialAction(this, "Fix database connection", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDatabase(getGuiFrame(), "Fix database connection");
				}
			}));
			return moduleStatus;
		}
		
		return new ModuleStatus(ModuleStatus.STATUS_OK);
	}

	@Override
	public BackupInformation getBackupInformation() {
		return backupInformation;
	}


}
