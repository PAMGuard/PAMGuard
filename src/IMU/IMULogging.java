package IMU;

import java.sql.Types;

import angleMeasurement.AngleDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class IMULogging extends SQLLogging {

	private IMUControl imuControl;
	
	private PamTableDefinition tableDef;
	
	PamTableItem heading, headingError, pitch, pitchError, tilt, tiltError, calHeading, calPitch, calTilt;
	
	public IMULogging(IMUControl imuControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.imuControl = imuControl;

		tableDef = new PamTableDefinition(imuControl.getUnitType() + " " + imuControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		
		tableDef.addTableItem(heading = new PamTableItem("Heading", Types.DOUBLE));
		tableDef.addTableItem(headingError = new PamTableItem("Heading_Error", Types.DOUBLE));
		tableDef.addTableItem(pitch = new PamTableItem("Pitch", Types.DOUBLE));
		tableDef.addTableItem(pitchError = new PamTableItem("Pitch_Error", Types.DOUBLE));
		tableDef.addTableItem(tilt = new PamTableItem("Tilt", Types.DOUBLE));
		tableDef.addTableItem(tiltError = new PamTableItem("Tilt_Error", Types.DOUBLE));
		
		
		//Use cheat indexing. Speed up load times. 
		tableDef.setUseCheatIndexing(true);
		
		setTableDefinition(tableDef);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}
	
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		AngleDataUnit angleDataUnit=(AngleDataUnit) pamDataUnit;
		
		heading							.setValue(angleDataUnit.getTrueHeading());
		headingError					.setValue(angleDataUnit.getErrorHeading());
		pitch							.setValue(angleDataUnit.getPitch());
		pitchError						.setValue(angleDataUnit.getErrorPitch());
		tilt							.setValue(angleDataUnit.getTilt());
		tiltError						.setValue(angleDataUnit.getErrorTilt());
		
	}
	
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {
		
//		AngleDataUnit angleDataUnit
		Double headingVal=heading.getDoubleValue();
		Double headingErVal=headingError.getDoubleValue();
		
		Double pitchVal=pitch.getDoubleValue();
		Double pitchErrorVal=pitchError.getDoubleValue();
		
		Double tiltVal=tilt.getDoubleValue();
		Double tiltErrorVal=tiltError.getDoubleValue();
		
		Double[] angles={headingVal,pitchVal,tiltVal};
		Double[] angleErrors={headingErVal,pitchErrorVal,tiltErrorVal};
		
		AngleDataUnit angleDataUnit=new AngleDataUnit(dataTime, angles, angleErrors);
		angleDataUnit.setDatabaseIndex(iD);
		
		((PamDataBlock<AngleDataUnit>) getPamDataBlock()).addPamData(angleDataUnit);
		
		return angleDataUnit;
		
	}

}

