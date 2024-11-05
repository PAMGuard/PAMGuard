package export.MLExport;

import PamguardMVC.PamDataUnit;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Struct;

public class MLClickEventExport extends MLSuperDetExport {

	public MLClickEventExport(MLDetectionsManager mlDetectionsManager) {
		super(mlDetectionsManager);
		
	}
	
	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, PamDataUnit dataUnit) {
		super.addDetectionSpecificFields(mlStruct, index, dataUnit);
		
		OfflineEventDataUnit clickEvent = (OfflineEventDataUnit) dataUnit; 
		
		
		mlStruct.set("event_id",index,  Mat5.newScalar(clickEvent.getEventId()));
		mlStruct.set("event_type", index, Mat5.newString(clickEvent.getEventType()));
		mlStruct.set("comment", index, Mat5.newString(clickEvent.getComment()));
		
		return mlStruct; 
	}

	
	@Override
	public Class getUnitClass() {
		return OfflineEventDataUnit.class;
	}

}
