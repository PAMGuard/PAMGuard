package export.MLExport;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;

public class MLWhistleMoanExport extends MLDataUnitExport<ConnectedRegionDataUnit> {

	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, ConnectedRegionDataUnit dataUnit) {
		
		//date
		//MLInt64 date = new MLInt64(null, new Long[]{dataUnit.getTimeMilliseconds()}, 1);

		//nSlices int
		Matrix nSlices = Mat5.newScalar(dataUnit.getConnectedRegion().getNumSlices());
		
		//list of structures: sliceNumber int, nPeaks int, peakData
		Struct sliceDataStruct = createSliceStruct(dataUnit); 
		
		int[][] contourData = calcPeakContourWidths( dataUnit); 

		//contour int[]
		Matrix contour = DLMatFile.array2Matrix(new int[][]{contourData[0]}); 

		//contour width double[]		
		Matrix contourWidth =  DLMatFile.array2Matrix(new int[][]{contourData[1]}); 

		//mean width 
		Matrix meanWidth =  DLMatFile.array2Matrix(new double[]{PamArrayUtils.mean(contourData[0])}); 

		
		mlStruct.set("nSlices", index, nSlices);
		mlStruct.set("sliceData",  index, sliceDataStruct);
		mlStruct.set("contour",  index, contour);
		mlStruct.set("contourWidth", index, contourWidth);
		mlStruct.set("meanWidth",  index, meanWidth);
		
		return mlStruct;
	}
	

	/**
	 * Calculate summary contour information to allow people to conveniently plot whisltle data
	 * @param dataUnit - the connected region data unit. 
	 * @return peak contour frequency int[0] and contour width int[1]. 
	 */
	public static int[][] calcPeakContourWidths(ConnectedRegionDataUnit dataUnit){
		
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
	private Struct createSliceStruct(ConnectedRegionDataUnit dataUnit){
		
//		Struct mlStructure= new MLStructure("sliceData", new int[]{dataUnit.getConnectedRegion().getSliceData().size(), 1}); 

		Struct mlStructure= Mat5.newStruct(dataUnit.getConnectedRegion().getSliceData().size(), 1);

		//the start sample.
		Matrix sliceNumber;
		Matrix nPeaks;
		Matrix peakData;
		SliceData sliceData; 
		for (int i=0; i<dataUnit.getConnectedRegion().getSliceData().size(); i++){

			//slice data
			sliceData= dataUnit.getConnectedRegion().getSliceData().get(i); 

			//the start sample.
			sliceNumber =  Mat5.newScalar(sliceData.getSliceNumber()); 

			//the duration of the detection in samples.
			nPeaks =  Mat5.newScalar(sliceData.getnPeaks()); 

			//the frequency limits.
			peakData = DLMatFile.array2Matrix(sliceData.getPeakInfo()); 
			
			mlStructure.set("sliceNumber", i, sliceNumber);
			mlStructure.set("nPeaks", i, nPeaks);
			mlStructure.set("peakData", i, peakData);
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
