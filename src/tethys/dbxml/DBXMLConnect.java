package tethys.dbxml;

import tethys.TethysControl;

/**
 * Class containing functions for managing the database connection. Opening, closing,
 * writing, keeping track of performance, etc. 
 * @author Doug Gillespie, Katie O'Laughlin
 *
 */
public class DBXMLConnect {

	private TethysControl tethysControl;

	public DBXMLConnect(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
	}
	
	public boolean openDatabase() {
		
		return true;
	}
	
	public void closeDatabase() {
		
	}
	
	// add whatever calls are necessary ... 

}
