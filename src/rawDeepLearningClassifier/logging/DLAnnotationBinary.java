package rawDeepLearningClassifier.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import binaryFileStorage.BinaryStore;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Saves deep learning annotations in binary files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLAnnotationBinary extends AnnotationBinaryHandler<DLAnnotation> {

	
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	private DLAnnotationType dlAnnotationType;

	public DLAnnotationBinary(DLAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
		this.dlAnnotationType = dataAnnotationType; 
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		//System.out.println("DLAnnotationBinary: Saving DL annotation results:"); 

		DLAnnotation ba = (DLAnnotation) annotation;

		//write the number of results for reading back
		try {
			if (dos == null) {
				dos = new DataOutputStream(bos = new ByteArrayOutputStream(14));
			}
			else {
				bos.reset();
			}

			dos.writeShort(ba.getModelResults().size());
			
//			System.out.println("DLAnnotationBinary.Number of model result to saves: " + dlDetection.getModelResults().size() + "  " + ba.getModelResults().size()); 

			for (int i=0; i<ba.getModelResults().size(); i++) {
				ModelResultBinaryFactory.getPackedData(ba.getModelResults().get(i), dos, 	ModelResultBinaryFactory.getType(ba.getModelResults().get(i)));
			}

			AnnotationBinaryData abd = new AnnotationBinaryData(BinaryStore.getCurrentFileFormat(), (short) 1, 
					super.getDataAnnotationType(), getDataAnnotationType().getShortIdCode(), bos.toByteArray());

			return abd;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public DLAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit,
			AnnotationBinaryData annotationBinaryData) {
		//System.out.println("DLAnnotationBinary: Extracting DL annotation results:"); 
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotationBinaryData.data));

		int version = annotationBinaryData.annotationVersion; //1 for original single template, 2 for multi template

		ArrayList<PredictionResult> modelResults = new ArrayList<PredictionResult>(); 
		try {

			int numModels  = dis.readShort(); 
			//System.out.println("DLAnnotationBinary: Num models: " +  numModels); 

//			System.out.println("DLAnnotationBinary.Number of model results: " + numModels); 
			for (int i =0; i<numModels; i++) {
				modelResults.add(ModelResultBinaryFactory.sinkData(dis)); 
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return new DLAnnotation(dlAnnotationType, modelResults); 
	}

}
