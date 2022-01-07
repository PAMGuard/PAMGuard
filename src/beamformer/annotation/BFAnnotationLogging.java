package beamformer.annotation;

import java.sql.Types;

import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import beamformer.loc.BeamFormerLocalisation;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class BFAnnotationLogging implements SQLLoggingAddon {

	private BFAnnotationType bfAnnotationType;
	
	private PamTableItem[] angle = new PamTableItem[2];
	private PamTableItem bfPhones, bfArrayType, bfContents;
	
	public BFAnnotationLogging(BFAnnotationType bfAnnotationType) {
		super();
		this.bfAnnotationType = bfAnnotationType;
		angle[0] = new PamTableItem("BeamAngle1", Types.REAL);
		angle[1] = new PamTableItem("BeamAngle2", Types.REAL);
		bfPhones = new PamTableItem("BeamPhones", Types.INTEGER);
		bfArrayType = new PamTableItem("BeamArrayType", Types.SMALLINT);
		bfContents = new PamTableItem("BeamLocContents", Types.INTEGER);
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(bfPhones);
		pamTableDefinition.addTableItem(bfArrayType);
		pamTableDefinition.addTableItem(bfContents);
		pamTableDefinition.addTableItem(angle[0]);
		pamTableDefinition.addTableItem(angle[1]);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		bfPhones.setValue(null);
		bfArrayType.setValue(null);
		bfContents.setValue(null);
		angle[0].setValue(null);
		angle[1].setValue(null);
		BeamFormerAnnotation bearingAnnotation = (BeamFormerAnnotation) pamDataUnit.findDataAnnotation(BeamFormerAnnotation.class);
		if (bearingAnnotation == null) {
			return false;
		}
		BeamFormerLocalisation bearingLoc = bearingAnnotation.getBeamFormerLocalisation();
		double[] angles = bearingLoc.getAngles();
		if (angles == null) {
			return false;
		}
		int n = Math.min(2, angles.length);
		for (int i = 0; i < n; i++) {
			angle[i].setValue(new Float(angles[i]));
		}
		bfPhones.setValue(bearingLoc.getReferenceHydrophones());
		bfArrayType.setValue((short) bearingLoc.getSubArrayType());
		bfContents.setValue(bearingLoc.getLocContents().getLocContent());
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		Float[] Angles = new Float[2];
		for (int i = 0; i < 2; i++) {
			Angles[i] = (Float) angle[i].getFloatValue();
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
		}
		int phones = bfPhones.getIntegerValue();
		int array = bfArrayType.getShortValue();
		int locCont = bfContents.getIntegerValue();
		BeamFormerLocalisation bfl = new BeamFormerLocalisation(pamDataUnit, locCont, phones, angles, 0);
		bfl.setReferenceHydrophones(phones);
		bfl.setSubArrayType(array);
		BeamFormerAnnotation bfa = new BeamFormerAnnotation(bfAnnotationType, bfl);
		pamDataUnit.addDataAnnotation(bfa);
		pamDataUnit.setLocalisation(bfl);
		return true;
	}

	@Override
	public String getName() {
		return bfAnnotationType.getAnnotationName();
	}

}
