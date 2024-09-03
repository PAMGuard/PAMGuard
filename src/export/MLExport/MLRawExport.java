package export.MLExport;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * Export data for any data unit which implements raw data holder. 
 * @author Jamie Macaulay
 *
 */
public class MLRawExport extends MLDataUnitExport<PamDataUnit>{

	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, PamDataUnit dataUnit) {

		RawDataHolder rawDataHolder = (RawDataHolder) dataUnit; 
		
		//the waveform
		Matrix wave = DLMatFile.array2Matrix(rawDataHolder.getWaveData()); 

		
		//the number of channels
		Matrix nChan = Mat5.newScalar(PamUtils.getNumChannels(dataUnit.getChannelBitmap())); 

		//the duration - repeat of duration in main data unit. Keeping here so struct is the same as the struct from binary files 
		Matrix duration = Mat5.newScalar(dataUnit.getSampleDuration()); 

		mlStruct.set("nChan", index, nChan);
		mlStruct.set("duration",index, duration);
		mlStruct.set("wave", index, wave);

		Matrix angles;
		Matrix angleErrors;
		if (dataUnit.getLocalisation()!=null) {
			//bearing angles 
			angles = DLMatFile.array2Matrix(dataUnit.getLocalisation().getAngles() == null ? new double[] {0.} : dataUnit.getLocalisation().getAngles()); 
			//angle errors 
			angleErrors = DLMatFile.array2Matrix(dataUnit.getLocalisation().getAngleErrors() == null ? new double[] {0.} : dataUnit.getLocalisation().getAngleErrors()); 
		}
		else {
			//bearing angles 
			angles = Mat5.newScalar(0.);
			//angle errors 
			angleErrors = Mat5.newScalar(0.);
		}

		mlStruct.set("angles", index, angles);
		mlStruct.set("angleErrors", index, angleErrors);


		return mlStruct;
	}

	@Override
	public Class<?> getUnitClass() {
		return RawDataHolder.class;
	}

	@Override
	public String getName() {
		return "raw_data_units";
	}

}
