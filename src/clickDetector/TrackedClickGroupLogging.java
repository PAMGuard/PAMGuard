package clickDetector;

import PamguardMVC.PamDataUnit;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLTypes;

public class TrackedClickGroupLogging extends ClickGroupLogging {
	
	ClickGroupDataBlock<OfflineEventDataUnit> clickGroupDataBlock;
	

	public TrackedClickGroupLogging(ClickGroupDataBlock<OfflineEventDataUnit> pamDataBlock, int updatePolicy) {
		super(pamDataBlock, updatePolicy);

		setCanView(true);
		
		this.clickGroupDataBlock = pamDataBlock;
	}

	@Override
	protected boolean fillDataUnit(SQLTypes sqlTypes, PamDataUnit pamDetection) {
		boolean basicFillOk =  super.fillDataUnit(sqlTypes, pamDetection);
		
		return basicFillOk;
	}

	@Override
	synchronized protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		OfflineEventDataUnit tcg = null;
		boolean isUpdate = true;
//		Timestamp ts = (Timestamp) getTableDefinition().getTimeStampItem().getValue();
//		long t = PamCalendar.millisFromTimeStamp(ts);
		PamTableDefinition tableDef = (PamTableDefinition) getTableDefinition();
		int updateIndex = (Integer) tableDef.getUpdateReference().getValue();
		if (updateIndex > 0) {
			tcg = this.clickGroupDataBlock.findByDatabaseIndex(updateIndex);
		}
		if (tcg == null) {
			tcg = new OfflineEventDataUnit(timeMilliseconds, (Integer) getChannelMap().getValue(), 
					(Integer) getStartSample().getValue(), (Integer) getDuration().getValue());
			isUpdate = false;
		}
		else {
			tcg.setSampleDuration((Long) getDuration().getValue());
		}
		tcg.setDatabaseIndex(databaseIndex);
		
		fillDataUnit(sqlTypes, tcg);
		
		if (isUpdate) {
			clickGroupDataBlock.updatePamData(tcg, tcg.getTimeMilliseconds());
		}
		else {
			clickGroupDataBlock.addPamData(tcg);
		}
//		
//		System.out.println(String.format("Current Id = %d update of %d",  tcg.getDatabaseIndex(), updateIndex));
//		System.out.print("Id's in block: ");
//		for (int i = 0; i < clickGroupDataBlock.getUnitsCount(); i++) {
//			System.out.print(String.format("%d, ", clickGroupDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT).getDatabaseIndex()));
//		}
//		System.out.println();
		
		return tcg;
	}
}
