package export.MLExport;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import bearinglocaliser.annotation.BearingAnnotationType;
import clickDetector.ClickClassifiers.annotation.ClickClassificationType;
import matchedTemplateClassifer.annotation.MatchedClickAnnotationType;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;
import us.hebi.matlab.mat.types.Struct;

/**
 * Adds annotations to data units. Annotations need to be managed differently. 
 * 
 * There can be different annotation for different data units and data units of the same
 * type won't necessarily have the same annotations. Therefore the annotation types are 
 * used to figure out which type of annotations are available and these are added
 * to the structure irrespective if whether there is data or not. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MLAnnotationsManager {

	/**
	 * Add annotations to a MATLAB structure. 
	 * @param mlStruct - the structure to add to.
	 * @param index  - index of the struct .
	 * @param dataUnit - the data unit with annotations to add. 
	 */
	public void addAnnotations(Struct mlStruct, int index, PamDataUnit dataUnit) {

		//first we need to find out which data annotation the data unit data block has - need to ensure we add all the annotation
		//even if they don't exist- otherwise we may get dissimilar structure exceptions. 
		PamDataBlock parentblock = dataUnit.getParentDataBlock();

		DataAnnotationType annotType;
		List<DataAnnotationType<?>> annotationTypes = parentblock.getAnnotationHandler().getAvailableAnnotationTypes();
		for (int i=0; i<parentblock.getAnnotationHandler().getNumUsedAnnotationTypes(); i++) {

			annotType = annotationTypes.get(i);
			
			//now iterate through the data annotations within the data unit and find the data annotation			
			DataAnnotation dataAnnotationType;
			for (int j=0; j<dataUnit.getNumDataAnnotations(); j++) {
				
			}
			
			//add the annotation even if it null
			addAnnotations( mlStruct,  index,  dataUnit,  dataUnit.getDataAnnotation(i), annotType);
			
		}; 

	}
	
	public void addAnnotations(Struct mlStruct, int index, PamDataUnit dataUnit, DataAnnotation dataAnnotation, DataAnnotationType annotationType) {
		switch (dataAnnotation.getDataAnnotationType().getAnnotationName()){
		case BearingAnnotationType.NAME:
			
			
			break;

		case ClickClassificationType.NAME:
			
			
			break;

		case MatchedClickAnnotationType.NAME:
			
			
			
			break;

		case DLAnnotationType.NAME:
			
			DLAnnotation dlAnnotation = (DLAnnotation) dlAnnotation;
			
			break;
		default:
			System.out.println("MLAnnotationsManager: Annotation: " + dataAnnotation.getDataAnnotationType().getAnnotationName() 
					+ " for " + dataUnit + " not supported: ");

		}

	}
 




}
