package Localiser.algorithms.timeDelayLocalisers.bearingLoc.annotation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import binaryFileStorage.BinaryStore;

public class BearingLocAnnotationBinary extends AnnotationBinaryHandler<BearingLocAnnotation>{

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public BearingLocAnnotationBinary(DataAnnotationType<BearingLocAnnotation> dataAnnotationType) {
		super(dataAnnotationType);
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		BearingLocAnnotation bla = (BearingLocAnnotation) annotation;
		if (dos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream(20));
		}
		else {
			bos.reset();
		}
		if (bla == null) {
			return null;
		}
		double[][] aae = bla.getAnglesAndErrors();
		if (aae == null) {
			return null;
		}
		int nAng = aae[0].length;
		int nErrors = aae.length == 2 ? nAng : 0;
		try {
			dos.writeShort(nAng);
			for (int i = 0; i < nAng; i++) {
				dos.writeFloat((float) aae[0][i]);
			}
			dos.writeShort(nErrors);
			for (int i = 0; i < nErrors; i++) {
				dos.writeFloat((float) aae[1][i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new AnnotationBinaryData((short)1, getDataAnnotationType(), 
				getDataAnnotationType().getShortIdCode(), bos.toByteArray());
	}

	@Override
	public BearingLocAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit,
			AnnotationBinaryData annotationBinaryData) {
		// TODO Auto-generated method stub
		return null;
	}


}
