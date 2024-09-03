package export.MLExport;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamguardMVC.PamDataUnit;
import cpod.CPODClick;
import cpod.CPODClickTrainDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


/**
 * Export data for any data unit which implements raw data holder. 
 * @author Jamie Macaulay
 *
 */
public class MLCPODExport extends MLDataUnitExport<PamDataUnit>{

	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, PamDataUnit dataUnit) {
		
		if (dataUnit instanceof CPODClickTrainDataUnit) {
			System.out.println("CPOD click train datablock!!"); 
		}

		CPODClick fpodClick = (CPODClick) dataUnit; 

		Matrix wave =  Mat5.newMatrix(new int[] {0,0});
		if (fpodClick.getWaveData()!=null) {
			//the waveform
			 wave = DLMatFile.array2Matrix(fpodClick.getWaveData());
		}

		//the duration - repeat of duration in main data unit. Keeping here so strcut is the same as the struct from binary files 
		Matrix duration = Mat5.newScalar(dataUnit.getDurationInMilliseconds()); 

		mlStruct.set("durationmillis",index, duration);
		mlStruct.set("wave", index, wave);
		
//		Matrix duration = Mat5.newScalar(fpodClick.getDurationInMilliseconds()); 

		Matrix nCyc = Mat5.newScalar(fpodClick.getnCyc()); 
		Matrix bw = Mat5.newScalar(fpodClick.getBw()*1000.); 
		Matrix peakfreq = Mat5.newScalar(fpodClick.getkHz()*1000.); 
		Matrix endF = Mat5.newScalar(fpodClick.getEndF()); 
		Matrix spl = Mat5.newScalar(fpodClick.getSpl()); 
		Matrix slope = Mat5.newScalar(fpodClick.getSlope()); 
		
		mlStruct.set("numcycles",index, nCyc);
		mlStruct.set("bandwidth", index, bw);
		mlStruct.set("peakfreq", index, peakfreq);
		mlStruct.set("endfreq", index, endF);
		mlStruct.set("SPL", index, spl);
		mlStruct.set("slope", index, slope);

		
		return mlStruct;
	}

	@Override
	public Class<?> getUnitClass() {
		return CPODClick.class;
	}

	@Override
	public String getName() {
		return "raw_data_units";
	}

}



