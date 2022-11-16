package bearinglocaliser.annotation;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import bearinglocaliser.BearingLocalisation;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;;

public class BearingAnnotationSQL implements SQLLoggingAddon {

	private BearingAnnotationType bearingAnnotationType;

	private PamTableItem[] angle = new PamTableItem[2];
	private PamTableItem[] angleError = new PamTableItem[2];
	private PamTableItem[] refAngles = new PamTableItem[2];
	private PamTableItem bfPhones, bfArrayType, bfContents, algoName;

	public BearingAnnotationSQL(BearingAnnotationType bearingAnnotationType) {
		super();
		this.bearingAnnotationType = bearingAnnotationType;
		algoName = new PamTableItem("BearingAlgorithm", Types.CHAR, 30, "Algorithm Name");
		angle[0] = new PamTableItem("BearingAngle1", Types.REAL, "Horizontal angle (radians)");
		angle[1] = new PamTableItem("BearingAngle2", Types.REAL, "Vertical angle (radians)");
		angleError[0] = new PamTableItem("BearingError1", Types.REAL, "Horizontal angle error (radians)");
		angleError[1] = new PamTableItem("BearingError2", Types.REAL, "Vertical angle error (radians)");
		refAngles[0] = new PamTableItem("ReferenceAngle1", Types.REAL, "Horizontal angle reference");
		refAngles[1] = new PamTableItem("ReferenceAngle2", Types.REAL, "Vertical angle reference");
		bfPhones = new PamTableItem("BearingPhones", Types.INTEGER, "Used hydrophones bitmap");
		bfArrayType = new PamTableItem("BearingArrayType", Types.SMALLINT, "Array Type 1=point;2=line;3=plane;4=volume");
		bfContents = new PamTableItem("BearingLocContents", Types.INTEGER, "Bitmap of localisation information types");
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(algoName);
		pamTableDefinition.addTableItem(bfPhones);
		pamTableDefinition.addTableItem(bfArrayType);
		pamTableDefinition.addTableItem(bfContents);
		pamTableDefinition.addTableItem(angle[0]);
		pamTableDefinition.addTableItem(angle[1]);
		pamTableDefinition.addTableItem(angleError[0]);
		pamTableDefinition.addTableItem(angleError[1]);
		pamTableDefinition.addTableItem(refAngles[0]);
		pamTableDefinition.addTableItem(refAngles[1]);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		algoName.setValue(null);
		bfPhones.setValue(null);
		bfArrayType.setValue(null);
		bfContents.setValue(null);
		angle[0].setValue(null);
		angle[1].setValue(null);
		angleError[0].setValue(null);
		angleError[1].setValue(null);
		refAngles[0].setValue(null);
		refAngles[1].setValue(null);

		BearingAnnotation bearingAnnotation = (BearingAnnotation) pamDataUnit.findDataAnnotation(BearingAnnotation.class);
		if (bearingAnnotation == null) {
			return false;
		}
		BearingLocalisation bfLoc = bearingAnnotation.getBearingLocalisation();
		double[] angles = bfLoc.getAngles();
		if (angles == null) {
			return false;
		}
		int n = Math.min(2, angles.length);
		for (int i = 0; i < n; i++) {
			angle[i].setValue(new Float(angles[i]));
		}
		double[] errors = bfLoc.getAngleErrors();
		if (errors != null) {
			n = Math.min(2, errors.length);
			for (int i = 0; i < n; i++) {
				angleError[i].setValue(new Float(errors[i]));
			}
		}
		double[] refAng = bfLoc.getReferenceAngles();
		if (refAng != null) {
			n = Math.min(2, refAng.length);
			for (int i = 0; i < n; i++) {
				refAngles[i].setValue((float) refAng[i]);
			}
		}
		
		bfPhones.setValue(bfLoc.getReferenceHydrophones());
		bfArrayType.setValue((short) bfLoc.getSubArrayType());
		bfContents.setValue(bfLoc.getLocContents().getLocContent());
		algoName.setValue(bfLoc.getAlgorithmName());
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		Float[] Angles = new Float[2];
		int nNans = 0;
		for (int i = 0; i < 2; i++) {
			Angles[i] = angle[i].getFloatValue();
		}
		int nAngles = 1;
		if (Angles[0] == null) {
			return false;
		}
		if (Angles[1] != null) {
			nAngles = 2;
		}
		double[] angles = new double[nAngles];
		for (int i = 0; i < nAngles; i++) {
			angles[i] = new Double(Angles[i]);
			if (Double.isNaN(angles[i])) {
				nNans++;
			}
		}
		if (nNans == angles.length) {
			return false; // no angle info - everything is nan. 
		}
		double[] errors = new double[nAngles];
		for (int i = 0; i < nAngles; i++) {
			Float val = (Float) angleError[i].getFloatValue();
			if (val != null) {
				errors[i] = val;
			}
		}
		double[] refAng = new double[2];
		int nRef = 0;
		for (int i = 0; i < 2; i++) {
			Float val = (Float) refAngles[i].getFloatValue();
			if (val != null) {
				refAng[nRef++] = val;
			}
		}
		if (nRef == 0) {
			refAng = null;
		}
		
		
		
		int phones = bfPhones.getIntegerValue();
		Short array = bfArrayType.getShortValue();
		int locCont = bfContents.getIntegerValue();
		String algoName = this.algoName.getStringValue();
		BearingLocalisation bl = new BearingLocalisation(pamDataUnit, algoName, locCont, phones, angles, errors, null);
		bl.setReferenceHydrophones(phones);
		if (array != null) {
			bl.setSubArrayType(array);
		}
		pamDataUnit.setLocalisation(bl);
		pamDataUnit.addDataAnnotation(new BearingAnnotation(bearingAnnotationType, bl));
		return true;
	}

	@Override
	public String getName() {
		return bearingAnnotationType.getAnnotationName();
	}

}
