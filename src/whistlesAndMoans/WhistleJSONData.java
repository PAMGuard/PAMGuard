package whistlesAndMoans;

import jsonStorage.JSONObjectData;

public class WhistleJSONData extends JSONObjectData {
	
	int nSlices;
	
	int amplitude;
	
	sliceData[] sliceData;
	
	int[] contour;
	
	int[] contWidth;
	
	double meanWidth;
	
	
	/**
	 * Main Constructor
	 */
	public WhistleJSONData() {
		super();
	}

	
	/**
	 * Create a new sliceData object
	 * @return
	 */
	public sliceData createSliceDataObject() {
		return new sliceData();
	}

	
	/**
	 * Inner array that matches the Matlab output structure.  Note that this is completely different
	 * than whistlesAndMoans.SliceData, but I wanted to stay consistent with the Matlab naming scheme
	 * 
	 * @author michaeloswald
	 *
	 */
	public class sliceData {
		int sliceNumber;
		int nPeaks;
		int[][] peakData;
	}

}
