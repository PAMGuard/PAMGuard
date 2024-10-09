package export.RExport;

import java.util.List;

import org.renjin.sexp.BooleanArrayVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;
import org.renjin.sexp.ListVector.NamedBuilder;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import bearinglocaliser.annotation.BearingAnnotation;
import bearinglocaliser.annotation.BearingAnnotationType;
import clickDetector.ClickClassifiers.annotation.ClickClassificationType;
import clickDetector.ClickClassifiers.annotation.ClickClassifierAnnotation;
import export.MLExport.MLAnnotationsManager;
import matchedTemplateClassifer.annotation.MatchedClickAnnotation;
import matchedTemplateClassifer.annotation.MatchedClickAnnotationType;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;




/**
 * Adds data unit annotation to RData frame. 
 . 
 * @author Jamie Macaulay
 *
 */
public class RAnnotationManager {


	public NamedBuilder addDataAnnotations(NamedBuilder rData, PamDataUnit dataUnit, int index) {

		//first we need to find out which data annotation the data unit data block has - need to ensure we add all the annotation
		//even if they don't exist- otherwise we may get dissimilar structure exceptions. 
		PamDataBlock parentblock = dataUnit.getParentDataBlock();
		
		if (parentblock.getAnnotationHandler()==null) return rData;

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
					//add the annotation even if it is null
					addAnnotations( rData,  index,  dataUnit, dataAnnotation, annotType);
					break;
				}
			}
		}; 

		return rData;

	}


	private void addAnnotations(NamedBuilder rData, int index, PamDataUnit dataUnit, DataAnnotation dataAnnotation,
			DataAnnotationType annotType) {

		NamedBuilder rDataAnnot = new ListVector.NamedBuilder();


		//if we don't have an annotation
		if (dataAnnotation == null) {
			rData.add(MLAnnotationsManager.getAnnotationNameMAT(annotType), rDataAnnot);
			return;
		}

		switch (dataAnnotation.getDataAnnotationType().getAnnotationName()){

		case BearingAnnotationType.NAME:
			BearingAnnotation bearingAnnotation = (BearingAnnotation) dataAnnotation;
			bearingAnnotation2R(bearingAnnotation, rDataAnnot);
			break;

		case ClickClassificationType.NAME:
			ClickClassifierAnnotation clkClassifierAnnotation = (ClickClassifierAnnotation) dataAnnotation;
			clkClassification2R(clkClassifierAnnotation, rDataAnnot);
			break;

		case MatchedClickAnnotationType.NAME:
			MatchedClickAnnotation matchAnnotation = (MatchedClickAnnotation) dataAnnotation;
			matchAnnotation2R(matchAnnotation, rDataAnnot);
			break;

		case DLAnnotationType.NAME:
			DLAnnotation dlAnnotation = (DLAnnotation) dataAnnotation;
			dlAnnoation2R(dlAnnotation, rDataAnnot);
			break;

		default:
			System.out.println("RAnnotationsManager: Annotation: " + dataAnnotation.getDataAnnotationType().getAnnotationName() 
					+ " for " + dataUnit + " not yet supported: ");
		}

		rData.add(MLAnnotationsManager.getAnnotationNameMAT(annotType), rDataAnnot); 


	}


	/**
	 * Add deep learning annotations to rData.
	 * @param dlAnnotation - the deep learning annotation.
	 * @param rData - rData frame to add to.
	 */
	private void dlAnnoation2R(DLAnnotation dlAnnotation, NamedBuilder rData) {

		float[][] predictions = new float[dlAnnotation.getModelResults().size()][];
		boolean[] decision = new boolean[dlAnnotation.getModelResults().size()];

		for (int i=0; i<predictions.length; i++) {
			predictions[i] = dlAnnotation.getModelResults().get(i).getPrediction();
			decision[i] = dlAnnotation.getModelResults().get(i).isBinaryClassification();
		}

		Vector predictionsR = RDataUnitExport.doubleArr2R(PamArrayUtils.float2Double(predictions));
		rData.add("predictions",predictionsR);

		BooleanArrayVector decisionR =  BooleanArrayVector.unsafe(decision);
		rData.add("isdecision",decisionR);
		
	}


	private void matchAnnotation2R(MatchedClickAnnotation matchAnnotation, NamedBuilder rData) {
		
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
		
		Vector matchAnnotationsR = RDataUnitExport.doubleArr2R(macthAnnotationM);
		
		rData.add("matchcorr",matchAnnotationsR);

	}


	private void clkClassification2R(ClickClassifierAnnotation clkClassifierAnnotation, NamedBuilder rData) {
		rData.add("classify_set",new IntArrayVector(clkClassifierAnnotation.getClassiferSet()));
	}


	private void bearingAnnotation2R(BearingAnnotation bearingAnnotation, NamedBuilder rData) {
		//extract the data needed for the bearing annotation
		int hydrophones  = bearingAnnotation.getBearingLocalisation().getReferenceHydrophones(); 
		int arrayType  = bearingAnnotation.getBearingLocalisation().getSubArrayType();
		int localisationContent  = bearingAnnotation.getBearingLocalisation().getLocContents().getLocContent();
		int nAngles  = bearingAnnotation.getBearingLocalisation().getAngles().length;
		double[] angles  = bearingAnnotation.getBearingLocalisation().getAngles();
		int nErrors  = bearingAnnotation.getBearingLocalisation().getAngleErrors().length;
		double[] errors  = bearingAnnotation.getBearingLocalisation().getAngleErrors();
		double[] refAngles =  bearingAnnotation.getBearingLocalisation().getReferenceAngles();

		rData.add("hydrophones", hydrophones);
		rData.add("arrayType",arrayType);
		rData.add("localisationContent", localisationContent);
		rData.add("nAngles", nAngles);
		rData.add("nErrors", nErrors);

		rData.add("angles", DoubleArrayVector.unsafe(angles));
		rData.add("errors", DoubleArrayVector.unsafe(errors));
		rData.add("refAngles",   DoubleArrayVector.unsafe(refAngles));
	}


}
