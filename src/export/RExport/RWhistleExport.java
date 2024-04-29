package export.RExport;

import org.renjin.sexp.ListVector.NamedBuilder;

import whistlesAndMoans.ConnectedRegionDataUnit;

/***
 * Export whisltes to RData
 * @author Jamie Macaulay
 *
 */
public class RWhistleExport extends RDataUnitExport<ConnectedRegionDataUnit> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, ConnectedRegionDataUnit dataUnit, int index) {
		// TODO Auto-generated method stub
//		//nSlices int
//		MLInt32 nSlices = new MLInt32(null, new Integer[]{dataUnit.getConnectedRegion().getNumSlices()}, 1);
//		
//		//list of structures: sliceNumber int, nPeaks int, peakData
//		MLStructure sliceDataStruct = createSliceStruct(dataUnit); 
//		
//		int[][] contourData = calcPeakContourWidths( dataUnit); 
//
//		//contour int[]
//		MLInt32 contour = new MLInt32(null, new int[][]{contourData[0]}); 
//
//		//contour width double[]		
//		MLInt32 contourWidth = new MLInt32(null,  new int[][]{contourData[1]}); 
//
//		//mean width 
//		MLDouble meanWidth = new MLDouble(null, new Double[]{PamArrayUtils.mean(contourData[0])}, 1); 
//
//		
//		mlStruct.setField("nSlices", nSlices, index);
//		mlStruct.setField("sliceData", sliceDataStruct, index);
//		mlStruct.setField("contour", contour, index);
//		mlStruct.setField("contourWidth", contourWidth, index);
//		mlStruct.setField("meanWidth", meanWidth, index);
		
		
		return rData;
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
