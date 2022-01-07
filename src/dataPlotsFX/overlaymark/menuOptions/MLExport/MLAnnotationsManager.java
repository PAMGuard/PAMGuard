package dataPlotsFX.overlaymark.menuOptions.MLExport;



import com.jmatio.types.MLStructure;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;

/**
 * Adds annotations to data units. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MLAnnotationsManager {

	/**
	 * Add annotations to a matlab structure. 
	 * @param mlStruct
	 * @param dataUnit
	 */
	public void addAnnotations(MLStructure mlStruct, PamDataUnit dataUnit) {
		for (int i=0; i<dataUnit.getNumDataAnnotations(); i++) {
			DataAnnotationType type = dataUnit.getDataAnnotation(i).getDataAnnotationType(); 
			//would be better to have the mlstruct updated by the annotation type but don't
			//want to be spreading MATLAB code throughout PAMGuard so keep here for now. 
			
			//TODO
		}; 
		
	}

	
	
	
}
