package difar.offline;

import java.sql.Connection;

import offlineProcessing.OfflineTask;
import binaryFileStorage.BinaryDataSource;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarProcess;

/**
 * Generic class for copying data from binary data files to database files. 
 * This will generally use the default SqlLogging and binaryDataSources which are already
 * set in the datablock so copy data from the binary to the database. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class UpdateCrossingTask<T extends PamDataUnit> extends OfflineTask<T> {

	private PamDataBlock<T> pamDataBlock;
	
	private SQLLogging sqlLogging;
	
	private BinaryDataSource binaryDataSource;

	private DBControlUnit dbControl;

	private DBProcess dbProcess;

	private PamConnection connection;
	
	
	/**
	 * @param pamDataBlock
	 */
	public UpdateCrossingTask(PamDataBlock<T> pamDataBlock) {
		super(pamDataBlock);
		this.pamDataBlock = pamDataBlock;
		this.sqlLogging = pamDataBlock.getLogging();
		this.binaryDataSource = pamDataBlock.getBinaryDataSource();
//		setParentDataBlock(pamDataBlock);
		addAffectedDataBlock(pamDataBlock);
	}

	/**
	 * @return the sqlLogging
	 */
	public SQLLogging getSqlLogging() {
		return sqlLogging;
	}

	/**
	 * @param sqlLogging the sqlLogging to set
	 */
	public void setSqlLogging(SQLLogging sqlLogging) {
		this.sqlLogging = sqlLogging;
	}

	/**
	 * @return the binaryDataSource
	 */
	public BinaryDataSource getBinaryDataSource() {
		return binaryDataSource;
	}

	/**
	 * @param binaryDataSource the binaryDataSource to set
	 */
	public void setBinaryDataSource(BinaryDataSource binaryDataSource) {
		this.binaryDataSource = binaryDataSource;
	}

	@Override
	public String getName() {
		return "Update triangulations in " + pamDataBlock.getDataName();
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		// find the database process
		dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			dbProcess = null;
			connection = null;
			return;
		}
		dbProcess = dbControl.getDbProcess();
		connection = dbControl.getConnection();
		
	}

	@Override
	public boolean processDataUnit(T dataUnit) {
		if (dbProcess == null) {
			return false;
		}
		DifarDataUnit difarDataUnit = (DifarDataUnit) dataUnit;
		DifarProcess difarProcess = (DifarProcess) pamDataBlock.getParentProcess();
		difarDataUnit.setDifarCrossing(difarProcess.getDifarRangeInfo(difarDataUnit));
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#canRun()
	 */
	@Override
	public boolean canRun() {
		// TODO Auto-generated method stub
		return super.canRun();
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

}
