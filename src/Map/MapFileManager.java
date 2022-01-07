package Map;

import java.io.File;
import java.util.Vector;


public interface MapFileManager {
	
	Vector<Integer> getAvailableContours();
	
	MapContour getMapContour(int contourIndex);
	
	int getContourIndex(double depth);
	
	int getContourCount();
		
	/**
	 * Read all file data including contours. 
	 * @param file File object
	 * @return true if read successful. 
	 */
	boolean readFileData(File file);
	
	/**
	 * Read file data and optionally read contours. 
	 * @param file file object
	 * @param contours true if you want the contours as well
	 * @return true if read successful. 
	 */
	boolean readFileData(File file, boolean contours);
	
	void clearFileData();
	
	File selectMapFile(File currentFile);
	
}
