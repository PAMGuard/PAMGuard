package export.RExport;

import java.util.List;

import org.renjin.sexp.BooleanArrayVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Vector;
import org.renjin.sexp.ListVector.NamedBuilder;

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
		//the types in use rather than the first n of the available ones - see MLAnnotationsManager.
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

			//add the annotation even if the data unit hasn't got one - it then goes in as an empty list.
			addAnnotations( rData,  index,  dataUnit, foundAnnotation, annotType);
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
			/*
			 * The annotation types which can be added to anything, and so are named by
			 * whoever added them rather than by a constant - the note, label, SPL and SNR of
			 * a spectrogram annotation, and logger form data. Dispatched on the annotation
			 * class since there is no name to switch on.
			 */
			if (!genericAnnotation2R(dataAnnotation, rDataAnnot)) {
				System.out.println("RAnnotationsManager: Annotation: " + dataAnnotation.getDataAnnotationType().getAnnotationName()
						+ " for " + dataUnit + " not yet supported: ");
			}
		}

		rData.add(MLAnnotationsManager.getAnnotationNameMAT(annotType), rDataAnnot); 


	}


	/**
	 * Convert the annotation types which are not tied to one sort of detection, and
	 * so have no name constant to switch on: the note and label of a spectrogram
	 * annotation (strings), the SPL and SNR measurements, and logger form data.
	 *
	 * @param dataAnnotation - the annotation to convert.
	 * @param rData          - the list to add the annotation fields to.
	 * @return true if the annotation was one of these and has been added.
	 */
	private boolean genericAnnotation2R(DataAnnotation dataAnnotation, NamedBuilder rData) {

		if (dataAnnotation instanceof StringAnnotation) {
			String string = ((StringAnnotation<?>) dataAnnotation).getString();
			rData.add("string", string == null ? "" : string);
			return true;
		}

		if (dataAnnotation instanceof SNRAnnotation) {
			rData.add("snr", ((SNRAnnotation) dataAnnotation).getSnr());
			return true;
		}

		if (dataAnnotation instanceof SPLAnnotation) {
			SPLAnnotation spl = (SPLAnnotation) dataAnnotation;
			rData.add("rms", spl.getRms());
			rData.add("zeroPeak", spl.getZeroPeak());
			rData.add("peakPeak", spl.getPeakPeak());
			rData.add("sel", spl.getIntegratedSEL());
			return true;
		}

		if (dataAnnotation instanceof UserFormAnnotation) {
			Object[] formData = ((UserFormAnnotation<?>) dataAnnotation).getLoggerFormData();
			if (formData == null) {
				formData = new Object[0];
			}
			//the form fields are of any type, so they go out as a character vector.
			String[] formStrings = new String[formData.length];
			for (int i = 0; i < formData.length; i++) {
				formStrings[i] = formData[i] == null ? "" : formData[i].toString();
			}
			rData.add("formData", new StringArrayVector(formStrings));
			return true;
		}

		return false;
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
