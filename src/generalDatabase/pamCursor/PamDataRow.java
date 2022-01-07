package generalDatabase.pamCursor;

/**
 * holds a single row of data for a non-scrollable result set
 * wrapped up with NonScrollablePamCursor.
 * <p>
 *  
 * @author Doug Gillespie
 *
 */
public class PamDataRow {

	protected int iD;
	
	protected Object[] data;
	
	protected boolean updated = false;
	
	protected boolean deleted = false;
	
	PamDataRow(int iD, int nColumns) {
		this.iD = iD;
		data = new Object[nColumns];
	}
	
}
