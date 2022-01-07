package detectiongrouplocaliser.logging;

import java.sql.Types;

import PamController.PamViewParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import detectiongrouplocaliser.DetectionGroupControl;
import detectiongrouplocaliser.DetectionGroupDataBlock;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import detectiongrouplocaliser.dialogs.DisplayOptionsHandler;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;

public class DetectionGroupLogging extends SuperDetLogging {

	private DetectionGroupDataBlock detectionGroupDataBlock;
	
	/**
	 * Detection group control. 
	 */
	private DetectionGroupControl detectionGroupControl;
	
	private PamTableItem endTime, dataCount;

	public DetectionGroupLogging(DetectionGroupControl detectionGroupControl, DetectionGroupDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.detectionGroupControl = detectionGroupControl;
		this.detectionGroupDataBlock = pamDataBlock;
		setTableDefinition(createBaseTable());
	}
	
	/**
	 * create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(detectionGroupControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(endTime = new PamTableItem("EndTime", Types.TIMESTAMP));
		tableDef.addTableItem(dataCount = new PamTableItem("DataCount", Types.INTEGER));
		return tableDef;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DetectionGroupDataUnit dgdu = (DetectionGroupDataUnit) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(dgdu.getEndTimeInMilliseconds()));
		dataCount.setValue(dgdu.getSubDetectionsCount());
	}

	/**
	 * @return the detectionGroupControl
	 */
	public DetectionGroupControl getDetectionGroupControl() {
		return detectionGroupControl;
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		DetectionGroupDataUnit dataUnit = new DetectionGroupDataUnit(timeMilliseconds, null);
		Long endUTC = SQLTypes.millisFromTimeStamp(endTime.getValue());
		int nData = dataCount.getIntegerValue();
		if (nData == 0) {
			nData = checkSubTableCount(databaseIndex);
		}
		if (endUTC != null) {
			dataUnit.setDurationInMilliseconds(endUTC-timeMilliseconds);
		}
//		dataUnit.setnSubDetections(nData);
		return dataUnit;
	}

//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#getViewerLoadClause(generalDatabase.SQLTypes, PamController.PamViewParameters)
//	 */
//	@Override
//	public String getViewerLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
////		int loadOption = detectionGroupControl.getDetectionGroupSettings().getOfflineShowOption();
////		if (loadOption == DisplayOptionsHandler.SHOW_ALL) {
////			return getViewerEverythingClause(sqlTypes, pvp);
////		}
////		else {
////			return getViewerOverlapClause(sqlTypes, pvp, endTime.getName());
////		}
//		return getViewerEverythingClause(sqlTypes, pvp);
//	}

}
