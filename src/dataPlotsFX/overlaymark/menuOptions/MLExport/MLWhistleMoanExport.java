package dataPlotsFX.overlaymark.menuOptions.MLExport;

import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLStructure;

import PamUtils.PamArrayUtils;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;

public class MLWhistleMoanExport extends MLDataUnitExport<ConnectedRegionDataUnit> {

	@Override
	public MLStructure addDetectionSpecificFields(MLStructure mlStruct, ConnectedRegionDataUnit dataUnit, int index) {
		
		//date
		//MLInt64 date = new MLInt64(null, new Long[]{dataUnit.getTimeMilliseconds()}, 1);

		//nSlices int
		MLInt32 nSlices = new MLInt32(null, new Integer[]{dataUnit.getConnectedRegion().getNumSlices()}, 1);
		
		//list of structures: sliceNumber int, nPeaks int, peakData
		MLStructure sliceDataStruct = createSliceStruct(dataUnit); 
		
		int[][] contourData = calcPeakContourWidths( dataUnit); 

		//contour int[]
		MLInt32 contour = new MLInt32(null, new int[][]{contourData[0]}); 

		//contour width double[]		
		MLInt32 contourWidth = new MLInt32(null,  new int[][]{contourData[1]}); 

		//mean width 
		MLDouble meanWidth = new MLDouble(null, new Double[]{PamArrayUtils.mean(contourData[0])}, 1); 

		
		mlStruct.setField("nSlices", nSlices, index);
		mlStruct.setField("sliceData", sliceDataStruct, index);
		mlStruct.setField("contour", contour, index);
		mlStruct.setField("contourWidth", contourWidth, index);
		mlStruct.setField("meanWidth", meanWidth, index);
		
		return mlStruct;
	}
	

	/**
	 * Calculate summary contour information to allow people to conveniently plot whisltle data
	 * @param dataUnit - the connected regio data unit. 
	 * @return peak contour frequency int[0] and contour width int[1]. 
	 */
	private int[][] calcPeakContourWidths(ConnectedRegionDataUnit dataUnit){
		
		SliceData sliceData; 
		// should change this to use iterators, not get since sliceList is linked list. 
		int[] contour= new int[dataUnit.getConnectedRegion().getSliceData().size()];
		int[] contourWidth = new int[dataUnit.getConnectedRegion().getSliceData().size()]; 
		
		int[][] contourData= new int[2][]; 
		for (int i=0; i<dataUnit.getConnectedRegion().getSliceData().size(); i++){
			//slice data
			sliceData= dataUnit.getConnectedRegion().getSliceData().get(i);
			
			contour[i] = sliceData.getPeakInfo()[0][1];
			
			contourWidth[i] = sliceData.getPeakInfo()[0][2]-sliceData.getPeakInfo()[0][0]+1;

			contourData[0]=contour;
			contourData[1]=contourWidth;
		}
		
		return contourData; 
	}
	
	/**
	 * Create array of slice structures for output.
	 * @param dataUnit
	 * @return
	 */
	private MLStructure createSliceStruct(ConnectedRegionDataUnit dataUnit){
		
		MLStructure mlStructure= new MLStructure("sliceData", new int[]{dataUnit.getConnectedRegion().getSliceData().size(), 1}); 

		//the start sample.
		MLInt32 sliceNumber;
		MLInt32 nPeaks;
		MLInt32 peakData;
		SliceData sliceData; 
		for (int i=0; i<dataUnit.getConnectedRegion().getSliceData().size(); i++){

			//slice data
			sliceData= dataUnit.getConnectedRegion().getSliceData().get(i); 

			//the start sample.
			sliceNumber = new MLInt32(null, new Integer[]{sliceData.getSliceNumber()}, 1); 

			//the duration of the detection in samples.
			nPeaks = new MLInt32(null, new Integer[]{sliceData.getnPeaks()}, 1); 

			//the frequency limits.
			peakData = new MLInt32(null, sliceData.getPeakInfo()); 
			
			mlStructure.setField("sliceNumber", sliceNumber, i);
			mlStructure.setField("nPeaks", nPeaks, i);
			mlStructure.setField("peakData", peakData, i);
		}
		
		return mlStructure;
		
	}

	@Override
	public Class<?> getUnitClass() {
		return ConnectedRegionDataUnit.class;
	}

	@Override
	public String getName() {
		return "whistles";
	}

}
