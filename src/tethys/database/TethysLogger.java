package tethys.database;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import tethys.TethysControl;

/**
 * Logging everything we put into Tethys in our own database. 
 * @author dg50
 *
 */
public class TethysLogger extends SQLLogging {
	
	private static TethysLogger tethysLogger;

	private TethysControl tethysControl;
	
	private TethysLogDataBlock logDataBlock;
	
	private PamTableDefinition tableDefinition;
	
	private PamTableItem collection, documentId, action, status, comment;
	
	private boolean tableChecked = false;

	private TethysLogger(TethysControl tethysControl, TethysLogDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.tethysControl = tethysControl;
		this.logDataBlock = pamDataBlock;
		tableDefinition = new PamTableDefinition("TethysLog");
		tableDefinition.addTableItem(collection = new PamTableItem("Collection", Types.VARCHAR));
		tableDefinition.addTableItem(documentId = new PamTableItem("DocumentId", Types.VARCHAR));
		tableDefinition.addTableItem(action = new PamTableItem("Action", Types.VARCHAR));
		tableDefinition.addTableItem(status = new PamTableItem("Status", Types.VARCHAR));
		tableDefinition.addTableItem(comment = new PamTableItem("Comment", Types.VARCHAR));
		tableDefinition.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		setTableDefinition(tableDefinition);
	}
	
	public static TethysLogger getTethysLogger(TethysControl tethysControl) {
		if (tethysLogger == null) {
			tethysLogger = createTethysLogger(tethysControl);
		}
		return tethysLogger;
	}
	
	private boolean checkTable() {
		if (tableChecked == true) {
			return true;
		}
		if (findDBProcess() == null) {
			return false;
		}
		else {
			tableChecked = findDBProcess().checkTable(tableDefinition);
		}
		return tableChecked;
	}
	
	public boolean logAction(String collection, String documentId, TethysActions action, boolean success, String comment) {
		PamConnection con = findDBConnection();
		if (con == null) {
			return false;
		}
		if (checkTable() == false) {
			return false;
		}
		
		TethysLogDataUnit dataUnit = new TethysLogDataUnit(System.currentTimeMillis(), collection, documentId, action, success, comment);
		return this.logData(con, dataUnit);
	}
	
	private PamConnection findDBConnection() {
		return DBControlUnit.findConnection();	
	}
	
	/**
	 * Find the database controlled unit. <br>Must exist in viewer mode surely, but perhaps 
	 * created after the Tethys module if the user is really crafty !
	 * @return the DB controlled unit.
	 */
	private DBControlUnit findDBControl() {
		return DBControlUnit.findDatabaseControl();
	}
	
	/**
	 * Fine the database process. Should exist. 
	 * @return
	 */
	private DBProcess findDBProcess() {
		DBControlUnit dbControl = findDBControl();
		if (dbControl == null) {
			return null;
		}
		return dbControl.getDbProcess();
	}

	private static TethysLogger createTethysLogger(TethysControl tethysControl) {
		TethysLogDataBlock datablock = new TethysLogDataBlock(tethysControl);
		TethysLogger newLogger = new TethysLogger(tethysControl, datablock);
		return newLogger;
	}


	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		TethysLogDataUnit tldu = (TethysLogDataUnit) pamDataUnit;
		collection.setValue(tldu.getCollection());
		documentId.setValue(tldu.getDocumentId());
		action.setValue(tldu.getAction().toString());
		status.setValue(tldu.isSuccess() ? "Success" : "Fail");
		comment.setValue(tldu.getComment());
	}
	
//	public TethysLogger(TethysControl tethysControl) {
//		this.tethysControl = tethysControl;
//	}
	
	
}
