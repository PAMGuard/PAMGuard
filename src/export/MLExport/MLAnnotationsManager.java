package export.MLExport;

import java.util.List;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import bearinglocaliser.annotation.BearingAnnotation;
import bearinglocaliser.annotation.BearingAnnotationType;
import clickDetector.ClickClassifiers.annotation.ClickClassificationType;
import clickDetector.ClickClassifiers.annotation.ClickClassifierAnnotation;
import matchedTemplateClassifer.annotation.MatchedClickAnnotation;
import matchedTemplateClassifer.annotation.MatchedClickAnnotationType;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Array;
import us.hebi.matlab.mat.types.Matrix;
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
			//Maybe not necessary but much safer than assuming data type list is same as annotatio list. 
			DataAnnotation dataAnnotation;
			for (int j=0; j<dataUnit.getNumDataAnnotations(); j++) {
				dataAnnotation=  dataUnit.getDataAnnotation(j); 
				if (dataAnnotation.getDataAnnotationType().getAnnotationName().equals(annotType.getAnnotationName())){
					//add the annotation even if it null
					addAnnotations( mlStruct,  index,  dataUnit,  dataAnnotation, annotType);
					break;
				} 
			}

		}; 

	}

	/**
	 * Get the name for the annotation i.e. the name to be used in the struct. 
	 * @param annotationType - the annotation type
	 * @return the anme to be used in the struct for the annotation
	 */
	public static String getAnnotationNameMAT( DataAnnotationType annotationType) {

		String name = null;
		switch (annotationType.getAnnotationName()){

		case BearingAnnotationType.NAME:
			name="bearing";
			break;

		case ClickClassificationType.NAME:
			name="classification";
			break;

		case MatchedClickAnnotationType.NAME:
			name="mclassification";
			break;

		case DLAnnotationType.NAME:
			name="dlclassification";
			break;
		}

		return name;

	}

	/**
	 * Add an annotation to an existing MATLAB structure. 
	 * @param mlStruct
	 * @param index
	 * @param dataUnit
	 * @param dataAnnotation
	 * @param annotationType
	 */
	public void addAnnotations(Struct mlStruct, int index, PamDataUnit dataUnit, DataAnnotation dataAnnotation, DataAnnotationType annotationType) {


		//if we don't have an annotation
		if (dataAnnotation == null) {
			mlStruct.set(getAnnotationNameMAT(annotationType), index, Mat5.newMatrix(0, 0));
			return;
		}

		Array annotation =null;
		switch (dataAnnotation.getDataAnnotationType().getAnnotationName()){

		case BearingAnnotationType.NAME:
			BearingAnnotation bearingAnnotation = (BearingAnnotation) dataAnnotation;
			annotation = bearingAnnotation2MAT(bearingAnnotation);
			break;

		case ClickClassificationType.NAME:
			ClickClassifierAnnotation clkClassifierAnnotation = (ClickClassifierAnnotation) dataAnnotation;
			annotation = clkClassification2MAT(clkClassifierAnnotation);
			break;

		case MatchedClickAnnotationType.NAME:
			MatchedClickAnnotation matchAnnotation = (MatchedClickAnnotation) dataAnnotation;
			annotation = matchAnnotation2MAT(matchAnnotation);
			break;

		case DLAnnotationType.NAME:
			DLAnnotation dlAnnotation = (DLAnnotation) dataAnnotation;
			annotation = dlAnnoation2MAT(dlAnnotation);
			break;

		default:
			System.out.println("MLAnnotationsManager: Annotation: " + dataAnnotation.getDataAnnotationType().getAnnotationName() 
					+ " for " + dataUnit + " not yet supported: ");
			return;

		}
		
		mlStruct.set(getAnnotationNameMAT(annotationType),index,annotation);


	}


	private Array matchAnnotation2MAT(MatchedClickAnnotation matchAnnotation) {
		//now write the matched template classifier results. Results form each template are written. 
		double threshold;
		double matchCorr;
		double rejectCorr; 

		double[][] macthAnnotationM = new double[matchAnnotation.getMatchedTemplateResult().size()][];

		for (int i = 0; i<matchAnnotation.getMatchedTemplateResult().size(); i++) {
			threshold  = matchAnnotation.getMatchedTemplateResult().get(i).threshold;
			matchCorr  = matchAnnotation.getMatchedTemplateResult().get(i).matchCorr;
			rejectCorr = matchAnnotation.getMatchedTemplateResult().get(i).rejectCorr;

			macthAnnotationM[i] = new double[] {threshold, matchCorr, rejectCorr};

		}
		return DLMatFile.array2Matrix(macthAnnotationM);
	}


	private Struct bearingAnnotation2MAT(BearingAnnotation bearingAnnotation) {


		//extract the data needed for the bearing annotation
		int hydrophones  = bearingAnnotation.getBearingLocalisation().getReferenceHydrophones(); 
		int arrayType  = bearingAnnotation.getBearingLocalisation().getSubArrayType();
		int localisationContent  = bearingAnnotation.getBearingLocalisation().getLocContents().getLocContent();
		int nAngles  = bearingAnnotation.getBearingLocalisation().getAngles().length;
		double[] angles  = bearingAnnotation.getBearingLocalisation().getAngles();
		int nErrors  = bearingAnnotation.getBearingLocalisation().getAngleErrors().length;
		double[] errors  = bearingAnnotation.getBearingLocalisation().getAngleErrors();
		double[] refAngles =  bearingAnnotation.getBearingLocalisation().getReferenceAngles();

		Struct bearingStruct = Mat5.newStruct(); 

		bearingStruct.set("hydrophones", Mat5.newScalar(hydrophones));
		bearingStruct.set("arrayType", Mat5.newScalar(arrayType));
		bearingStruct.set("localisationContent", Mat5.newScalar(localisationContent));
		bearingStruct.set("nAngles", Mat5.newScalar(nAngles));
		bearingStruct.set("nErrors", Mat5.newScalar(nErrors));

		bearingStruct.set("angles", DLMatFile.array2Matrix(angles));
		bearingStruct.set("errors", DLMatFile.array2Matrix(errors));
		bearingStruct.set("refAngles", DLMatFile.array2Matrix(refAngles));

		//		for (int i=0; i<predictions.length; i++) {
		//			predictions[i] = dlAnnotation.getModelResults().get(i).getPrediction();
		//			decision[i] = dlAnnotation.getModelResults().get(i).isBinaryClassification();
		//		}
		//		
		//		Struct dlAnnotationMat = Mat5.newStruct(); 
		//		
		//		dlAnnotationMat.set("predictions", DLMatFile.array2Matrix(PamArrayUtils.float2Double(predictions)));
		//		
		//		Matrix matrix = Mat5.newMatrix(decision.length, 1);
		//		for (int i=0; i<decision.length; i++) {
		//				matrix.setBoolean(i, decision[i]);
		//		}
		//		dlAnnotationMat.set("isdecision", matrix);

		return bearingStruct;
	}

	private Struct clkClassification2MAT(ClickClassifierAnnotation clkClassifyAnnot) {

		Struct clkclsfrAnnotation = Mat5.newStruct(); 
		clkclsfrAnnotation.set("classify_set", DLMatFile.array2Matrix(clkClassifyAnnot.getClassiferSet()));

		return clkclsfrAnnotation; 

	}



	private Struct dlAnnoation2MAT(DLAnnotation dlAnnotation) {
		float[][] predictions = new float[dlAnnotation.getModelResults().size()][];
		boolean[] decision = new boolean[dlAnnotation.getModelResults().size()];

		for (int i=0; i<predictions.length; i++) {
			predictions[i] = dlAnnotation.getModelResults().get(i).getPrediction();
			decision[i] = dlAnnotation.getModelResults().get(i).isBinaryClassification();
		}

		Struct dlAnnotationMat = Mat5.newStruct(); 

		dlAnnotationMat.set("predictions", DLMatFile.array2Matrix(PamArrayUtils.float2Double(predictions)));

		Matrix matrix = Mat5.newMatrix(decision.length, 1);
		for (int i=0; i<decision.length; i++) {
			matrix.setBoolean(i, decision[i]);
		}
		dlAnnotationMat.set("isdecision", matrix);

		return dlAnnotationMat;
	}





}
