package annotation.classifier;

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

public class BaseClassificationBinary extends AnnotationBinaryHandler<BaseClassificationAnnotation> {
	
	private static final int OUTSIZE = 20; 
	
	private static final short BINVERSION = 1;

	private BaseClassificationAnnotationType narwAnnotationType;

	public BaseClassificationBinary(BaseClassificationAnnotationType narwAnnotationType) {
		super(narwAnnotationType);
		this.narwAnnotationType = narwAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		BaseClassificationAnnotation narwAnnotation = (BaseClassificationAnnotation) annotation;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(OUTSIZE);
		DataOutputStream dos = new DataOutputStream(bos);
		String name = narwAnnotation.getLabel();
		if (name == null) {
			name = "";
		}
		String method = narwAnnotation.getMethod();
		if (method == null) {
			method = "";
		}
		try {
			dos.writeUTF(name);
			dos.writeUTF(method);
			dos.writeFloat((float) narwAnnotation.getScore()); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new AnnotationBinaryData(BINVERSION, narwAnnotationType, narwAnnotationType.getShortIdCode(), bos.toByteArray());
	}

	@Override
	public BaseClassificationAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData) {
		ByteArrayInputStream bis = new ByteArrayInputStream(annotationBinaryData.data);
		DataInputStream dis = new DataInputStream(bis);
		String name = null;
		String method = null;
		double score = 0;
		try {
			name = dis.readUTF();
			method = dis.readUTF();
			score = dis.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BaseClassificationAnnotation(narwAnnotationType, score, name, method);
	}

}
