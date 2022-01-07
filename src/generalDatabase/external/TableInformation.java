package generalDatabase.external;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;

public class TableInformation {
	
	/**
	 * Source table is empty, so nothing to do. 
	 */
	public static final int SOURCE_EMPTY = 1;
	/**
	 * copy to a table which is either empty or doesn't exist
	 */
	public static final int DEST_EMPTY = 2; 
	/**
	 * add data to an existing table that appears to have some data
	 */
	public static final int COPY_ADDDATAOVERLAP = 3;
	/**
	 * source table has no Id or UTC column, so probably not PAM data.
	 */
	public static final int COPY_NO_SRC_ID = 4; 
	
	/**
	 * Tables seem to have the same data - same n records, same id's etc.
	 */
	public static final int TABLES_MATCH = 5;
	/**
	 * can't work out what's going on 
	 */
	public static final int COPY_CONFUSED = 6;  
	
	
	
	private CopyTableDefinition sourceTableDef;
	
	private DestinationTableDefinition destTableDef;
	
	private String currentAction;
	
	private Integer sourceRecords, destRecords;
	
	private Boolean destTableExists, destTableOk;
	
	private int tableIndex;
	
//	public static enum CopyChoice{COPY, DONTCOPY};
	
//	private CopyChoice copyChoice;
	private ImportOption copyChoice;
	
	private CopyStatus copyStatus = CopyStatus.NOTSTARTED;

	private int rowsCopied;

	private double copyRate;
	private int copyScenario;
	private int newRecords; // new records with a later date. 
	
	private static final String indexColName = EmptyTableDefinition.indexColName;
	
	private static final String utcColName = PamTableDefinition.utcColName;

	public TableInformation(int tableIndex, CopyTableDefinition sourceTableDef, DestinationTableDefinition destTableDef) {
		super();
		this.tableIndex = tableIndex;
		this.sourceTableDef = sourceTableDef;
		this.destTableDef = destTableDef;
	}

	/**
	 * @return the currentAction
	 */
	public String getCurrentAction() {
		return currentAction;
	}

	/**
	 * @param currentAction the currentAction to set
	 */
	public void setCurrentAction(String currentAction) {
		this.currentAction = currentAction;
	}

	/**
	 * @return the sourceRecords
	 */
	public Integer getSourceRecords() {
		return sourceRecords;
	}

	/**
	 * @param sourceRecords the sourceRecords to set
	 */
	public void setSourceRecords(Integer sourceRecords) {
		this.sourceRecords = sourceRecords;
	}

	/**
	 * @return the destRecords
	 */
	public Integer getDestRecords() {
		return destRecords;
	}

	/**
	 * @param destRecords the destRecords to set
	 */
	public void setDestRecords(Integer destRecords) {
		this.destRecords = destRecords;
	}

	public Boolean getDestTableOk() {
		return destTableOk;
	}

	public void setDestTableOk(Boolean destTableOk) {
		this.destTableOk = destTableOk;
	}

	public boolean getDestTableExists() {
		return (destTableExists != null && destTableExists);
	}

	public void setDestTableExists(Boolean destTableExists) {
		this.destTableExists = destTableExists;
	}

	/**
	 * @return the tableDef
	 */
	public CopyTableDefinition getSourceTableDef() {
		return sourceTableDef;
	}

	/**
	 * @return the tableIndex
	 */
	public int getTableIndex() {
		return tableIndex;
	}
	
	public int getWarningLevel() {
		if (destTableOk == null || destTableOk == false) {
			return 2;
		}
		if (destRecords != null && destRecords > 0) {
			return 1;
		}
		return 0;
	}
	
	public String getWarnings() {
		String warning = "";
		if (destTableExists == null || destTableExists == false) {
			warning += "Table does not exist in destination databse";
		}
		if (destTableOk == null || destTableOk == false) {
			if (warning.length() > 0) warning += "<p>";
			warning += "Column Errors in the output databse table";
		}
		if (destRecords == null) {
			if (warning.length() > 0) warning += "<p>";
			warning += "Unable to determine if destination table already contains data";
		}
		else if (destRecords > 0) {
			if (warning.length() > 0) warning += "<p>";
			warning += String.format("The destination table already contains %d records", destRecords);
		}
		
		return "<html>" + warning + "</html>";
	}

	/**
	 * @return the destTableDef
	 */
	public DestinationTableDefinition getDestTableDef() {
		return destTableDef;
	}

	/**
	 * @param destTableDef the destTableDef to set
	 */
	public void setDestTableDef(DestinationTableDefinition destTableDef) {
		this.destTableDef = destTableDef;
	}
	
	/**
	 * 
	 * @return The table name
	 */
	public String getTableName() {
		if (sourceTableDef == null) {
			// can never really be since list is made from source database. 
			return null;
		}
		return sourceTableDef.getTableName();
	}

	/**
	 * @return the copyChoice
	 */
	public ImportOption getCopyChoice() {
		return copyChoice;
	}

	/**
	 * @param copyChoice the copyChoice to set
	 */
	public void setCopyChoice(ImportOption copyChoice) {
		this.copyChoice = copyChoice;
	}

	/**
	 * @return the copyStatus
	 */
	public CopyStatus getCopyStatus() {
		return copyStatus;
	}

	/**
	 * @param copyStatus the copyStatus to set
	 */
	public void setCopyStatus(CopyStatus copyStatus) {
		this.copyStatus = copyStatus;
	}

	/**
	 * 
	 * @param nRowsCopied the number of rows copied. 
	 */
	public void setRowsCopied(int nRowsCopied) {
		this.rowsCopied = nRowsCopied;
	}

	/**
	 * @return the rowsCopied
	 */
	public int getRowsCopied() {
		return rowsCopied;
	}

	/**
	 * Set the copy rate (copies per second)
	 * @param rate
	 */
	public void setCopyRate(double rate) {
		copyRate = rate;
	}
	
	/**
	 * 
	 * @return get the copy rate (copies per second)
	 */
	public double getCopyRate() {
		return copyRate;
	}

	/**
	 * Possible copy scenarios (add to, etc)
	 * @param copyScenario
	 */
	public void setCopyScenario(int copyScenario) {
		this.copyScenario = copyScenario;
	}

	/**
	 * Possible copy scenarios (add to, etc)
	 * @return the copyScenario
	 */
	public int getCopyScenario() {
		return copyScenario;
	}

	/**
	 * Set number of new records - that's records in the source data with a later date
	 * than is in the existing dest database. 
	 * @param newRecords
	 */
	public void setNewRecords(Integer newRecords) {
		if (newRecords != null) {
			this.newRecords = newRecords;
		}
	}

	/**
	 * Get number of new records - that's records in the source data with a later date
	 * than is in the existing dest database. 
	 * @return the newRecords
	 */
	public int getNewRecords() {
		return newRecords;
	}
}
