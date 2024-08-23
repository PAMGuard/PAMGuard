package export.RExport;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import export.MLExport.MLWhistleMoanExport;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Struct;

import org.renjin.sexp.ListVector.NamedBuilder;

import PamUtils.PamArrayUtils;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;

/***
 * Export whisltes to RData
 * @author Jamie Macaulay
 *
 */
public class RWhistleExport extends RDataUnitExport<ConnectedRegionDataUnit> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, ConnectedRegionDataUnit dataUnit, int index) {
	
		
		rData.add("nSlices", dataUnit.getConnectedRegion().getNumSlices()); 
		
		int[][] contourData = MLWhistleMoanExport.calcPeakContourWidths( dataUnit); 

		IntArrayVector contours = new IntArrayVector(contourData[0]);
		IntArrayVector contourWidth = new IntArrayVector(contourData[0]);
		
		//need to generate a slice struct
		ListVector.NamedBuilder peakDatas = createSliceStruct( dataUnit);

		rData.add("nSlices", dataUnit.getConnectedRegion().getNumSlices()); 
		rData.add("sliceData", peakDatas); 
		rData.add("contour", contours); 
		rData.add("contourWidth", contourWidth); 
		rData.add("meanWidth", PamArrayUtils.mean(contourData[0])); 

	
		// TODO Auto-generated method stub
		//nSlices int
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
	
	
	
	/**
	 * Create array of slice structures for output.
	 * @param dataUnit
	 * @return
	 */
	private ListVector.NamedBuilder createSliceStruct(ConnectedRegionDataUnit dataUnit){
		
//		Struct mlStructure= new MLStructure("sliceData", new int[]{dataUnit.getConnectedRegion().getSliceData().size(), 1}); 
		
		ListVector.NamedBuilder peakDatas = new ListVector.NamedBuilder(); ;

		
		Struct mlStructure= Mat5.newStruct(dataUnit.getConnectedRegion().getSliceData().size(), 1);

		//the start sample.
		int sliceNumber;
		int nPeaks;
		int[][] peakData;
		SliceData sliceData; 
		ListVector.NamedBuilder rData;
		for (int i=0; i<dataUnit.getConnectedRegion().getSliceData().size(); i++){
			
			rData = new ListVector.NamedBuilder();
			
			//slice data
			sliceData= dataUnit.getConnectedRegion().getSliceData().get(i); 
		
			//the start sample.
			sliceNumber =  sliceData.getSliceNumber(); 

			//the duration of the detection in samples.
			nPeaks = sliceData.getnPeaks(); 

			//the frequency limits.
			peakData = sliceData.getPeakInfo(); 
			
			int n=0;
			int nbins =peakData.length*peakData[0].length;
			int[] flattenedArr  = new int[nbins];
			//System.out.println("Number of bins: " + nbins);
			for (int ii=0; ii<peakData.length; ii++) {
				for (int j=0; j<peakData[ii].length; j++) {
//					System.out.println("Current: " + i + " "+ j 
//							+ " nchan: " + dataUnit.getNChan() + "  wave size: " 
//							+ dataUnit.getWaveLength() +"len concat: " + concatWaveform.length);
					flattenedArr[n++] = peakData[ii][j];
				}
			}

			IntArrayVector peakDataR = new IntArrayVector(flattenedArr, AttributeMap.builder().setDim(peakData.length, peakData[0].length).build());
					
			rData.add("sliceNumber", sliceNumber);
			rData.add("nPeaks", nPeaks);
			rData.add("peakData", peakDataR);
			
			peakDatas.add(String.valueOf(sliceNumber), rData); 

		}
		
		return peakDatas;
		
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
