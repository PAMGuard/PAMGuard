package export.MLExport;

import java.util.List;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.calcs.snr.SNRAnnotation;
import annotation.calcs.spl.SPLAnnotation;
import annotation.string.StringAnnotation;
import annotation.userforms.UserFormAnnotation;
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
import us.hebi.matlab.mat.types.Cell;
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
		
		if (parentblock.getAnnotationHandler()==null) return;

		DataAnnotationType annotType;
		/*
		 * The types in use, which for a handler that lets the user choose (the
		 * spectrogram annotations, for one) is a subset of the available ones - taking
		 * the first getNumUsedAnnotationTypes() of the available list instead would walk
		 * the wrong types altogether and export none of the annotations the user had
		 * switched on.
		 */
		List<DataAnnotationType<?>> annotationTypes = parentblock.getAnnotationHandler().getUsedAnnotationTypes();


		for (int i=0; i<annotationTypes.size(); i++) {

			annotType = annotationTypes.get(i);

			//now iterate through the data annotations within the data unit and find the data annotation
			//Maybe not necessary but much safer than assuming data type list is same as annotatio list.
			DataAnnotation dataAnnotation;
			DataAnnotation foundAnnotation = null;
			for (int j=0; j<dataUnit.getNumDataAnnotations(); j++) {
				dataAnnotation=  dataUnit.getDataAnnotation(j);
				if (dataAnnotation.getDataAnnotationType().getAnnotationName().equals(annotType.getAnnotationName())){
					foundAnnotation = dataAnnotation;
					break;
				}
			}

			/*
			 * Add the annotation even if the data unit hasn't got one - it then goes in as
			 * an empty array. Otherwise the field would be missing for that element of the
			 * structure array, which is what the dissimilar structure exceptions are about:
			 * a spectrogram annotation the user left blank is a very ordinary thing.
			 */
			addAnnotations( mlStruct,  index,  dataUnit,  foundAnnotation, annotType);

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

		default:
			/*
			 * Annotation types whose name is chosen by the user rather than fixed by the
			 * annotation class - the note and label of a spectrogram annotation are both
			 * StringAnnotationType, named whatever the module called them. Fall back to the
			 * annotation name itself; it must never be null, or the field it is added under
			 * would be too.
			 */
			name = toFieldName(annotationType.getAnnotationName());
			break;
		}

		return name;

	}

	/**
	 * Turn an annotation name into something which can be used as a MATLAB
	 * structure field name or an R list name: lower case, only letters, digits and
	 * underscores, and never starting with a digit.
	 *
	 * @param annotationName - the name of the annotation, may be null.
	 * @return a name safe to use as a field name.
	 */
	private static String toFieldName(String annotationName) {
		if (annotationName == null || annotationName.length() == 0) {
			return "annotation";
		}
		String name = annotationName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
		if (!Character.isLetter(name.charAt(0))) {
			name = "a_" + name;
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
			/*
			 * The annotation types which can be added to anything, and so are named by
			 * whoever added them rather than by a constant - the note, label, SPL and SNR of
			 * a spectrogram annotation, and logger form data. Dispatched on the annotation
			 * class since there is no name to switch on.
			 */
			annotation = genericAnnotation2MAT(dataAnnotation);
			if (annotation == null) {
				System.out.println("MLAnnotationsManager: Annotation: " + dataAnnotation.getDataAnnotationType().getAnnotationName()
						+ " for " + dataUnit + " not yet supported: ");
				return;
			}
			break;

		}

		mlStruct.set(getAnnotationNameMAT(annotationType),index,annotation);


	}


	/**
	 * Convert the annotation types which are not tied to one sort of detection, and
	 * so have no name constant to switch on: the note and label of a spectrogram
	 * annotation (strings), the SPL and SNR measurements, and logger form data.
	 *
	 * @param dataAnnotation - the annotation to convert.
	 * @return the MATLAB array for the annotation, or null if it isn't one of these.
	 */
	private Array genericAnnotation2MAT(DataAnnotation dataAnnotation) {

		if (dataAnnotation instanceof StringAnnotation) {
			String string = ((StringAnnotation<?>) dataAnnotation).getString();
			return Mat5.newString(string == null ? "" : string);
		}

		if (dataAnnotation instanceof SNRAnnotation) {
			return Mat5.newScalar(((SNRAnnotation) dataAnnotation).getSnr());
		}

		if (dataAnnotation instanceof SPLAnnotation) {
			SPLAnnotation spl = (SPLAnnotation) dataAnnotation;
			Struct splStruct = Mat5.newStruct();
			splStruct.set("rms", Mat5.newScalar(spl.getRms()));
			splStruct.set("zeroPeak", Mat5.newScalar(spl.getZeroPeak()));
			splStruct.set("peakPeak", Mat5.newScalar(spl.getPeakPeak()));
			splStruct.set("sel", Mat5.newScalar(spl.getIntegratedSEL()));
			return splStruct;
		}

		if (dataAnnotation instanceof UserFormAnnotation) {
			Object[] formData = ((UserFormAnnotation<?>) dataAnnotation).getLoggerFormData();
			if (formData == null) {
				formData = new Object[0];
			}
			//the form fields are of any type, so they go out as a cell array of strings.
			Cell formCell = Mat5.newCell(1, formData.length);
			for (int i = 0; i < formData.length; i++) {
				formCell.set(i, Mat5.newString(formData[i] == null ? "" : formData[i].toString()));
			}
			return formCell;
		}

		return null;
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
