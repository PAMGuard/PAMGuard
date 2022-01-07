package whistlesAndMoans;

import java.util.Arrays;
import java.util.List;

import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;
import whistlesAndMoans.WhistleJSONData.sliceData;

public class WhistleJSONDataSource extends JSONObjectDataSource<WhistleJSONData> {


	/**
	 * Call the super constructor and then initialize the objectData object as
	 * a WhistleJSONData class
	 */
	public WhistleJSONDataSource() {
		super();
		objectData = new WhistleJSONData();
	}

	
	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = WhistleBinaryDataSource.WHISTLE_MOAN_DETECTION;
	}
	
	
	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		ConnectedRegionDataUnit crdu = (ConnectedRegionDataUnit) pamDataUnit;
		ConnectedRegion cr = crdu.getConnectedRegion();
		
		// transfer over the data to this class
		objectData.nSlices = cr.getNumSlices();
		objectData.amplitude = (int) (crdu.getAmplitudeDB()*100);


		// Note that SliceData is the class inside the whistleAndMoans package, while
		// sliceData is the inner class within this class.  Matlab uses sliceData for
		// it's output structure and I wanted to stay consistent so that's why the inner
		// class is named that way.  Yes, it's confusing.  
		List<SliceData> sliceDataList = cr.getSliceData();
		objectData.sliceData = new sliceData[sliceDataList.size()];
		objectData.contour = new int[sliceDataList.size()];
		objectData.contWidth = new int[sliceDataList.size()];
		for (int i=0; i<sliceDataList.size(); i++) {
			objectData.sliceData[i] = objectData.createSliceDataObject();
			objectData.sliceData[i].sliceNumber = sliceDataList.get(i).sliceNumber;
			objectData.sliceData[i].nPeaks = sliceDataList.get(i).nPeaks;
			objectData.sliceData[i].peakData = sliceDataList.get(i).getPeakInfo();
			
			
			// NEED TO CHECK IF THIS IS CORRECT, OR IF THE INDICES SHOULD BE REVERSED
			// I THINK IT'S WRONG IN MATLAB - TEST WITH ACTUAL DATA
			objectData.contour[i] = objectData.sliceData[i].peakData[0][1];
			objectData.contWidth[i] = objectData.sliceData[i].peakData[0][2]-objectData.sliceData[i].peakData[0][0] + 1;
		}
		objectData.meanWidth = Arrays.stream(objectData.contWidth).average().orElse(Double.NaN);;
	}

	

}
