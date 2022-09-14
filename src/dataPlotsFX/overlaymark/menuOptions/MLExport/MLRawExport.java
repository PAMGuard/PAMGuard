package dataPlotsFX.overlaymark.menuOptions.MLExport;

import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLStructure;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;

/**
 * Export data for any data unit which implements raw data holder. 
 * @author au671271
 *
 */
public class MLRawExport extends MLDataUnitExport<PamDataUnit>{

	@Override
	public MLStructure addDetectionSpecificFields(MLStructure mlStruct, PamDataUnit dataUnit, int index) {

		RawDataHolder rawDataHolder = (RawDataHolder) dataUnit; 
		
		//the waveform
		MLDouble wave = new MLDouble(null, rawDataHolder.getWaveData()); 


		//the number of channels
		MLInt32 nChan = new MLInt32(null, new Integer[]{PamUtils.getNumChannels(dataUnit.getChannelBitmap())}, 1); 

		//the duration - repeat of duration in main data unit. Keeping here so strcut is the same as the struct from binary files 
		MLInt64 duration = new MLInt64(null, new Long[]{dataUnit.getSampleDuration()}, 1); 

		mlStruct.setField("nChan", nChan, index);
		mlStruct.setField("duration", duration, index);
		mlStruct.setField("wave", wave, index);

		MLDouble angles;
		MLDouble angleErrors;
		if (dataUnit.getLocalisation()!=null) {
			//bearing angles 
			angles = new MLDouble(null, dataUnit.getLocalisation().getAngles(), 1); 
			//angle errors 
			angleErrors = new MLDouble(null, dataUnit.getLocalisation().getAngleErrors(), 1); 
		}
		else {
			//bearing angles 
			angles = new MLDouble(null, new double[] {0}, 1); 
			//angle errors 
			angleErrors = new MLDouble(null, new double[] {0}, 1); 
		}

		mlStruct.setField("angles", angles, index);
		mlStruct.setField("angleErrors", angleErrors, index);


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
