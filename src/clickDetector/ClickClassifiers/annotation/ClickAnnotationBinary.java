package clickDetector.ClickClassifiers.annotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import binaryFileStorage.BinaryStore;

/**
 * Write a click classification annotation to a binary file
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickAnnotationBinary extends AnnotationBinaryHandler<ClickClassifierAnnotation> {

	private ClickClassificationType clickAnnotationType;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public ClickAnnotationBinary(ClickClassificationType clickAnnotationType) {
		super(clickAnnotationType);
		this.clickAnnotationType = clickAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		ClickClassifierAnnotation ba = (ClickClassifierAnnotation) annotation;
		int[] classificationlist = ba.getClassiferSet(); 
		if (dos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream(14));
		}
		else {
			bos.reset();
		}
		try {
			dos.writeShort(classificationlist.length);
			for (int i = 0; i < classificationlist.length; i++) {
				//unlikey that users will have more than 2^16 classifiers. 
				dos.writeShort((short) classificationlist[i]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		AnnotationBinaryData abd = new AnnotationBinaryData((short) 1, 
				super.getDataAnnotationType(), getDataAnnotationType().getShortIdCode(), bos.toByteArray());
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return abd;
	}

	@Override
	public ClickClassifierAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit,
			AnnotationBinaryData annotationBinaryData) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotationBinaryData.data));
		int [] classificationSet = null;
		try {
			//read the classification data
			int nstuff = dis.readShort();
			classificationSet= new int[nstuff];
			for (int i=0; i<nstuff; i++) {
				classificationSet[i]=(int) dis.readShort(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new ClickClassifierAnnotation(clickAnnotationType, classificationSet);
	}

}
