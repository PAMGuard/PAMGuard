package export.MLExport;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import us.hebi.matlab.mat.types.Struct;

/**
 * Adds annotations to data units. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MLAnnotationsManager {

	/**
	 * Add annotations to a MATLAB structure. 
	 * @param mlStruct
	 * @param index 
	 * @param dataUnit
	 */
	public void addAnnotations(Struct mlStruct, int index, PamDataUnit dataUnit) {
		for (int i=0; i<dataUnit.getNumDataAnnotations(); i++) {
			DataAnnotationType type = dataUnit.getDataAnnotation(i).getDataAnnotationType(); 
			//would be better to have the mlstruct updated by the annotation type but don't
			//want to be spreading MATLAB code throughout PAMGuard so keep here for now. 
			//TODO
		}; 
		
	}

	
	
	
}
