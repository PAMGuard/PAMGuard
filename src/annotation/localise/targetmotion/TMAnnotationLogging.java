package annotation.localise.targetmotion;

import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLTypes;
import targetMotionOld.TargetMotionSQLLogging;

public class TMAnnotationLogging extends TargetMotionSQLLogging {

	private TMAnnotationType tmAnnotationType;

	public TMAnnotationLogging(TMAnnotationType tmAnnotationType) {
		super(2);
		this.tmAnnotationType =tmAnnotationType;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		boolean ans = super.loadData(sqlTypes, pamTableDefinition, pamDataUnit);
		if (ans == false) return false;
		AbstractLocalisation loc = pamDataUnit.getLocalisation();
		if (loc == null) return false;
		if (GroupLocalisation.class.isAssignableFrom(loc.getClass()) == false) {
			return false;
		}
		TMAnnotation tmAnnotation = new TMAnnotation(tmAnnotationType, (GroupLocalisation) loc);
		pamDataUnit.addDataAnnotation(tmAnnotation);
		return ans;
	}


}
