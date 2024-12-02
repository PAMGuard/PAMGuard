package export.MLExport;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * Export a click to a MATLAB structure. 
 * @author Jamie Macaulay 
 *
 */
public class MLClickExport extends MLRawExport {

	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, PamDataUnit dataUnit) {
		
		mlStruct = super.addDetectionSpecificFields(mlStruct, index, dataUnit); 
		
		ClickDetection clickDetection = (ClickDetection) dataUnit; 

		//the trigger map
		Matrix triggerMap = Mat5.newScalar(clickDetection.getTriggerList()); 

		Matrix type = Mat5.newScalar(clickDetection.getClickType()); 

		Matrix flag = Mat5.newScalar(clickDetection.getClickFlags()); 

		//add to the structure. 
		mlStruct.set("triggerMap", index, triggerMap);
		mlStruct.set("type", index, type);
		mlStruct.set("flag", index, flag);

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
