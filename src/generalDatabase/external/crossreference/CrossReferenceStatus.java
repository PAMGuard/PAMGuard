package generalDatabase.external.crossreference;

public class CrossReferenceStatus {

	private int iTable, nTables, iRec, nRecs;
	private String message;
	/**
	 * @param iTable
	 * @param nRecs
	 * @param iRec
	 * @param message
	 */
	public CrossReferenceStatus(int nTables, int iTable,  int nRecs, int iRec, String message) {
		super();
		this.nTables = nTables;
		this.iTable = iTable;
		this.nRecs = nRecs;
		this.iRec = iRec;
		this.message = message;
	}
	/**
	 * @return the iTable
	 */
	public int getiTable() {
		return iTable;
	}
	/**
	 * @return the iRec
	 */
	public int getiRec() {
		return iRec;
	}
	/**
	 * @return the nRecs
	 */
	public int getnRecs() {
		return nRecs;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return the nTables
	 */
	public int getnTables() {
		return nTables;
	}

}
