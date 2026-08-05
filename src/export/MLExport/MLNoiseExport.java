package export.MLExport;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamController.PamController;
import PamController.soundMedium.GlobalMediumManager;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import noiseMonitor.NoiseDataBlock;
import noiseMonitor.NoiseDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import us.hebi.matlab.mat.types.Cell;



/**
 * Export a noise measurement to MATLAB 
 * @author Jamie Macaulay
 *
 */
public class MLNoiseExport extends MLDataUnitExport<NoiseDataUnit>{	


	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, NoiseDataUnit noiseBandData) {

		Matrix noiseBandDataM = DLMatFile.array2Matrix(noiseBandData.getNoiseBandData());

		Matrix nBand = Mat5.newScalar(noiseBandData.getNoiseBandData().length); 
		Matrix nMeasures = Mat5.newScalar(noiseBandData.getNoiseBandData()[0].length); 

		mlStruct.set("nBand", index, nBand);
		mlStruct.set("nMeasures",index, nMeasures);
		mlStruct.set("noise", index, noiseBandDataM);

		noiseBandData.getNoiseBandData(); 

		return mlStruct;
	}

	@Override
	public Class<?> getUnitClass() {
		return NoiseDataUnit.class;
	}

	@Override
	public String getName() {
		return "noise_measurements";
	}

	@Override
	protected Struct detectionHeader(PamDataBlock pamDataBlock) {
		
		try {
			NoiseDataBlock noiseBandDataBlock = (NoiseDataBlock) pamDataBlock; 
			
			Struct struct = Mat5.newStruct(1, 1);
			
			Matrix hiEdges = DLMatFile.array2Matrix(noiseBandDataBlock.getBandHiEdges());
			Matrix loEdges = DLMatFile.array2Matrix(noiseBandDataBlock.getBandLoEdges());
			
			struct.set("hiEdges", 0, hiEdges);
			struct.set("loEdges",0, loEdges);
			
			// Get the name of the measures
			String[] strings = getMeasureNames( noiseBandDataBlock);
			
			
			
			Cell cellArray = Mat5.newCell(1, strings.length);
	
			// Set each cell with a string
			for (int i = 0; i < strings.length; i++) {
				//System.out.println("Setting cell " + i + " to " + strings[i]);
			    cellArray.set(i, Mat5.newString(strings[i]));
			}
			
			struct.set("noise_descriptors", 0, cellArray);
		
			return struct;
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

//		return null;
	}
	
	
	/**
	 * Get the names of the noise measures
	 * @param noiseBandDataBlock
	 * @return a list of names
	 */
	public static String[] getMeasureNames(NoiseDataBlock noiseBandDataBlock) {
		
		//get integer bitmap of noise types
		String[] strings = noiseBandDataBlock.getUsedMeasureNames();
		
		
		//make more descriptive by adding units
		for (int i=0; i<strings.length; i++) {
			strings[i] = (strings[i] +  " (" + PamController.getInstance().getGlobalMediumManager().getdBRefString() + ")");
		}
		
		
//		int[] types  = PamUtils.getChannelArray(statisticTypes);
//		
//		String[] strings = new String[types.length]; 
//
//		for (int i=0; i<strings.length; i++) {
//
//			strings[i] = NoiseDataBlock.getMeasureName(types[i]);
//			
//			System.out.println("statisticTypes: " + statisticTypes+ " type: " 
//			+ types[i] + "strings[i] : " + strings[i]);
//
//		}
//		
		return strings; 
	}
}
