package annotation.localise.targetmotion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import Localiser.LocaliserModel;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.algorithms.locErrors.json.LocaliserErrorFactory;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import binaryFileStorage.BinaryStore;

public class TMAnnotationBinary extends AnnotationBinaryHandler<TMAnnotation> {

	private TMAnnotationType tmAnnotationType;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public TMAnnotationBinary(TMAnnotationType tmAnnotationType) {
		super(tmAnnotationType);
		this.tmAnnotationType = tmAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		TMAnnotation tmAnnotation = (TMAnnotation) annotation;
		GroupLocalisation groupLoc = tmAnnotation.getGroupLocalisation();
		if (groupLoc == null) {
			return null;
		}
		int nLocs = groupLoc.getAmbiguityCount();
		if (nLocs < 1) {
			return null;
		}
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream(100));
		}
		else {
			bos.reset();
		}
		try {
			GroupLocResult tmResult = groupLoc.getGroupLocaResult(0);
			LocaliserModel model = tmResult.getModel();
			if (model != null) {
				String modelName = model.getName();
				dos.writeUTF(modelName);
			}
			else {
				dos.writeUTF("");
			}
			dos.writeShort(nLocs);
			dos.writeInt(groupLoc.getReferenceHydrophones());
			for (int i = 0; i < nLocs; i++) {
				tmResult = groupLoc.getGroupLocaResult(i);
				LatLong latLong = tmResult.getLatLong();
				dos.writeDouble(latLong.getLatitude());
				dos.writeDouble(latLong.getLongitude());
				dos.writeFloat((float) latLong.getHeight());
				Double chi2 = tmResult.getChi2();
				if (chi2 != null) {
					dos.writeFloat(new Float(chi2));
				}
				else {
					dos.writeFloat(0.F);
				}
				String errStr = "";
				LocaliserError locError = tmResult.getLocError();
				if (locError != null) {
					errStr = locError.getJsonErrorString();
				}
				if (errStr == null) {
					errStr = "";
				}
				dos.writeUTF(errStr);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		AnnotationBinaryData abd = new AnnotationBinaryData((short)1, 
				tmAnnotationType, tmAnnotationType.getShortIdCode(), bos.toByteArray());
		return abd;
	}

	@Override
	public TMAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData) {
		byte[] data = annotationBinaryData.data;
		if (data == null) {
			return null;
		}
		/**
		 * See if the data unit already has a localisation annotation, if it's a click
		 * it probably will do and we'll want to copy over some of the array type 
		 * information
		 */
		AbstractLocalisation currentLocalisation = pamDataUnit.getLocalisation();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		GroupLocalisation groupLoc = new GroupLocalisation(pamDataUnit, null);
		try {
			String modelName = dis.readUTF();
			LocaliserModel tmModel = tmAnnotationType.findLocaliserModel(modelName);
			int nLocs = dis.readShort();
			int phones = dis.readInt();
			for (int i = 0; i < nLocs; i++) {
				double lat = dis.readDouble();
				double lon = dis.readDouble();
				double height = dis.readFloat();
				double chi2 = dis.readFloat();
				String errStr = dis.readUTF();
				GroupLocResult glr = new GroupLocResult(new LatLong(lat, lon, height), i, chi2);
				if (errStr != null) {
					LocaliserError localiserError = LocaliserErrorFactory.getErrorFromJsonString(errStr);
					glr.setError(localiserError);
				}
				glr.setModel(tmModel);
				groupLoc.addGroupLocaResult(glr);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (currentLocalisation != null) {
			groupLoc.setSubArrayType(currentLocalisation.getSubArrayType());
			groupLoc.setReferenceHydrophones(currentLocalisation.getReferenceHydrophones());
			groupLoc.setArrayAxis(currentLocalisation.getArrayOrientationVectors());
		}
		pamDataUnit.setLocalisation(groupLoc);
		return new TMAnnotation(getDataAnnotationType(), groupLoc);
	}

}
