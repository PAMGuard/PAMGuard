package export.RExport;

import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.superdet.SuperDetection;
import clickDetector.offlineFuncs.OfflineEventDataUnit;

public class RClickEventExport extends RSuperDetectionExport{

	public RClickEventExport(RExportManager mlDetectionsManager) {
		super(mlDetectionsManager);
	}
	
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, SuperDetection dataUnit, int index) {
		
		OfflineEventDataUnit clickEvent = (OfflineEventDataUnit) dataUnit; 

		rData.add("event_id",  clickEvent.getEventId());
		rData.add("event_type", clickEvent.getEventType());
		String comment = "";
		if (clickEvent.getComment()!=null) comment = clickEvent.getComment();
		rData.add("comment", comment);
		
		return rData;
	}

	
	
	@Override
	public Class getUnitClass() {
		return OfflineEventDataUnit.class;
	}


}
