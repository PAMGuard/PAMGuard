package dataPlotsFX.overlaymark.menuOptions.MLExport;

import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLStructure;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import whistlesAndMoans.ConnectedRegionDataUnit;

/**
 * Export a click to a MATLAB structure. 
 * @author Jamie Macaulay 
 *
 */
public class MLClickExport extends MLRawExport {

	@Override
	public MLStructure addDetectionSpecificFields(MLStructure mlStruct, PamDataUnit dataUnit, int index) {
		
		mlStruct = super.addDetectionSpecificFields(mlStruct, dataUnit, index); 
		
		ClickDetection clickDetection = (ClickDetection) dataUnit; 

		//the trigger map
		MLInt32 triggerMap = new MLInt32(null, new Integer[]{clickDetection.getTriggerList()}, 1); 

		MLInt8 type = new MLInt8(null, new Byte[]{clickDetection.getClickType()}, 1); 


		MLInt32 flag = new MLInt32(null, new Integer[]{clickDetection.getClickFlags()},1); 

		//add to the structure. 
		mlStruct.setField("triggerMap", triggerMap, index);
		mlStruct.setField("type", type, index);
		mlStruct.setField("flag", flag, index);

		return mlStruct;
	}

	@Override
	public Class<?> getUnitClass() {
		return ClickDetection.class;
	}

	@Override
	public String getName() {
		return "clicks";
	}

}
