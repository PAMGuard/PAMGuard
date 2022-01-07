package Localiser.detectionGroupLocaliser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.SQLTypes;

public abstract class GroupLocInfoLogging extends PamDetectionLogging {

	public GroupLocInfoLogging(PamDataBlock pamDataBlock, int updatePolicy) {
		super(pamDataBlock, updatePolicy);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean fillDataUnit(SQLTypes sqlTypes, PamDataUnit pamDetection) {
		boolean basicFillOk =  super.fillDataUnit(sqlTypes, pamDetection);
		GroupDetection groupDetection = (GroupDetection) pamDetection;
		
		return basicFillOk;
	}

}
