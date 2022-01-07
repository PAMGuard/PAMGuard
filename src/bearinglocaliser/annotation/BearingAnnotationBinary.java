package bearinglocaliser.annotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import bearinglocaliser.BearingLocalisation;
import binaryFileStorage.BinaryStore;

public class BearingAnnotationBinary extends AnnotationBinaryHandler<BearingAnnotation> {

	private BearingAnnotationType bearingAnnotationType;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	
	private static final int currentVersion = 2;

	public BearingAnnotationBinary(BearingAnnotationType bearingAnnotationType) {
		super(bearingAnnotationType);
		this.bearingAnnotationType = bearingAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		BearingAnnotation ba = (BearingAnnotation) annotation;
		BearingLocalisation bfl = ba.getBearingLocalisation();
		if (dos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream(14));
		}
		else {
			bos.reset();
		}
		try {
			dos.writeUTF(bfl.getAlgorithmName());
			dos.writeInt(bfl.getReferenceHydrophones());
			dos.writeShort(bfl.getSubArrayType());
			dos.writeInt(bfl.getLocContents().getLocContent());
			double[] angles = bfl.getAngles();
			dos.writeShort(angles.length);
			for (int i = 0; i < angles.length; i++) {
				dos.writeFloat((float) bfl.getAngles()[i]);
			}
			double[] errors = bfl.getAngleErrors();
			if (errors == null) {
				dos.writeShort(0);
			}
			else {
				dos.writeShort(errors.length);
				for (int i = 0; i < errors.length; i++) {
					dos.writeFloat((float) errors[i]);
				}
			}
			if (currentVersion >= 2) {
				// also write the array reference angles. 
				double[] refAngs = bfl.getReferenceAngles();
				if (refAngs == null) {
					dos.writeShort(0);
				}
				else {
					dos.writeShort(refAngs.length);
					for (int i = 0; i < refAngs.length; i++) {
						dos.writeFloat((float) refAngs[i]);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		AnnotationBinaryData abd = new AnnotationBinaryData((short) currentVersion, 
				super.getDataAnnotationType(), getDataAnnotationType().getShortIdCode(), bos.toByteArray());
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return abd;
	}

	@Override
	public BearingAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotationBinaryData.data));
		BearingLocalisation bl = null;
		try {
			String algoName = dis.readUTF();
			int phones = dis.readInt();
			short arrayType = dis.readShort();
			int locCont = dis.readInt();
			int nAng = dis.readShort();
			double[] angles = new double[nAng];
			double[] refAngs = null;
			for (int i = 0; i < nAng; i++) {
				angles[i] = dis.readFloat();
			}
			int nErr = dis.readShort();
			double[] errors = new double[nErr];
			for (int i = 0; i < nErr; i++) {
				errors[i] = dis.readFloat();
			}
			if (annotationBinaryData.annotationVersion >= 2) {
				// also write the array reference angles. 
				int nRef = dis.readShort();
				if (nRef > 0) {
					refAngs = new double[nRef];
					for (int i = 0; i < nRef; i++) {
						refAngs[i] = dis.readFloat();
					}
				}

			}
			bl = new BearingLocalisation(pamDataUnit, algoName, locCont, phones, angles, errors, refAngs);
			bl.setSubArrayType(arrayType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		pamDataUnit.setLocalisation(bl);
		
		return new BearingAnnotation(bearingAnnotationType, bl);
	}

}
