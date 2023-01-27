package tethys.dbxml;

import tethys.TethysControl;

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
	
	// add whatever calls are necessary to set up schema's etc. 

}
