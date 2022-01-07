package beamformer.annotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import Array.ArrayManager;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import beamformer.loc.BeamFormerLocalisation;
import binaryFileStorage.BinaryStore;
import annotation.binary.AnnotationBinaryData;

public class BFAnnotationBinary extends AnnotationBinaryHandler<BeamFormerAnnotation> {

	public BFAnnotationBinary(DataAnnotationType<BeamFormerAnnotation> dataAnnotationType) {
		super(dataAnnotationType);
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation dataAnnotation) {
		BeamFormerAnnotation bfAnnotation = (BeamFormerAnnotation) dataAnnotation;
		// write a single bearing into the binary stream as a float...
		BeamFormerLocalisation bl = bfAnnotation.getBeamFormerLocalisation();
		ByteArrayOutputStream bos;
		DataOutputStream dos = new DataOutputStream(bos = new ByteArrayOutputStream(14));
		try {
			dos.writeInt(bl.getReferenceHydrophones());
			dos.writeShort(bl.getSubArrayType());
			dos.writeInt(bl.getLocContents().getLocContent());
			double[] angles = bl.getAngles();
			dos.writeShort(angles.length);
			for (int i = 0; i < angles.length; i++) {
				dos.writeFloat((float) bl.getAngles()[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	public BeamFormerAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit,
			AnnotationBinaryData annotationBinaryData) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotationBinaryData.data));
		double[] angles = null;
		int refHydrophones = 0;
		int nAngles = 0;
		int locCont = 0;
		int arrayType = 0;
		try {
			refHydrophones = dis.readInt();
			arrayType = dis.readShort();
			locCont = dis.readInt();
			nAngles = dis.readShort();
			angles = new double[nAngles];
			for (int i = 0; i < nAngles; i++) {
				angles[i] = dis.readFloat();
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (angles == null) {
			return null;
		}
//		int locContents = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY | LocContents.HAS_BEARINGERROR;
		BeamFormerLocalisation bfl = new BeamFormerLocalisation(pamDataUnit, locCont, refHydrophones, angles, 0);
		bfl.setSubArrayType(arrayType);
		pamDataUnit.setLocalisation(bfl);
		return new BeamFormerAnnotation(getDataAnnotationType(), bfl);
	}


}
