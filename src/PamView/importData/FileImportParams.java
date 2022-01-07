package PamView.importData;

import java.util.ArrayList;

public class FileImportParams implements Cloneable{

	/**
	 * A list of the last files loaded by the IMU import utility.
	 */
	public ArrayList<String> lastFiles=new ArrayList<String>();
	
	@Override
	public FileImportParams clone() {
		try {
			return(FileImportParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
