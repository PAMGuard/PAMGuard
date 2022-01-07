package matchedTemplateClassifer.annotation;

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
import matchedTemplateClassifer.MatchedTemplateResult;

/***
 * Annotation for the matched click classifier which adds the match and reject correlation values for each template pair
 * to the binary files.  
 * 
 * @author Jamie Macaulay
 *
 */
public class MatchedClickAnnotationBinary  extends AnnotationBinaryHandler<MatchedClickAnnotation> {

	private MatchedClickAnnotationType clickAnnotationType;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public MatchedClickAnnotationBinary(MatchedClickAnnotationType clickAnnotationType) {
		super(clickAnnotationType);
		this.clickAnnotationType = clickAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		MatchedClickAnnotation ba = (MatchedClickAnnotation) annotation;

		//write the number of results for reading back
		try {
			if (dos == null) {
				dos = new DataOutputStream(bos = new ByteArrayOutputStream(14));
			}
			else {
				bos.reset();
			}

			dos.writeShort(ba.getMatchedTemplateResult().size());

			//now write the matched template classifier results. Results form each template are written. 
			double threshold;
			double matchCorr;
			double rejectCorr; 
			for (int i = 0; i<ba.getMatchedTemplateResult().size(); i++) {

				threshold  = ba.getMatchedTemplateResult().get(i).threshold;
				matchCorr  = ba.getMatchedTemplateResult().get(i).matchCorr;
				rejectCorr = ba.getMatchedTemplateResult().get(i).rejectCorr;
//				System.out.println("MatchedClickAnnotationBinary: Adding threshold from matched click: " + threshold + " " + i); 
				dos.writeDouble(threshold);
				dos.writeDouble(matchCorr);
				dos.writeDouble(rejectCorr);

			}

			//changed to version 2
			AnnotationBinaryData abd = new AnnotationBinaryData((short) 2, 
					super.getDataAnnotationType(), getDataAnnotationType().getShortIdCode(), bos.toByteArray());

			return abd;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public MatchedClickAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit,
			AnnotationBinaryData annotationBinaryData) {
		//System.out.println("MatchedClickAnnotationBinary: Extracting threshold from matched click: "); 
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotationBinaryData.data));

		int version = annotationBinaryData.annotationVersion; //1 for original single template, 2 for multi template
		
//		System.out.println("Matched annot length: " + annotationBinaryData.data.length); 

		Double threshold = null;
		Double matchCorr = null;
		Double rejectCorr = null;

		MatchedTemplateResult matchedTemplateResult; 
		ArrayList<MatchedTemplateResult> results = new ArrayList<MatchedTemplateResult>(); 
		try {
			//read the classification data
			int nResults; 
			if (version>1) nResults = dis.readShort();
			else nResults=1; 

			for (int i =0; i<nResults; i++) {
				threshold = dis.readDouble();
				matchCorr = dis.readDouble();
				rejectCorr = dis.readDouble();

				matchedTemplateResult = new MatchedTemplateResult(threshold.doubleValue()); 
				matchedTemplateResult.matchCorr=matchCorr;
				matchedTemplateResult.rejectCorr=rejectCorr;

				results.add(matchedTemplateResult); 
			}
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Could not read annotation data from the matched click classifier");
			return null;
		}

		//System.out.println("Threshold: " + threshold);
		return new MatchedClickAnnotation(clickAnnotationType, results);
	}

}
