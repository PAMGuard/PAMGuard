package AIS;

import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;

public class AISJsonDataSource extends JSONObjectDataSource<AISJsonData>{
	
	public AISJsonDataSource() {
		super();
		objectData = new AISJsonData();
	}

	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		
		AISDataUnit aisUnit = (AISDataUnit) pamDataUnit;
		
		objectData.MMSI = aisUnit.mmsiNumber;
		
		AISStaticData staticReport = aisUnit.getStaticData();
		AISPositionReport posReport = aisUnit.getPositionReport();
		StationType stationType = StationType.A;
		
		if(posReport!=null) {
			objectData.messageId = posReport.messageId;
			objectData.lat = posReport.getLatitude();
			objectData.lon = posReport.getLongitude();
			objectData.SOG = posReport.speedOverGround;
			objectData.COG = posReport.courseOverGround;
			objectData.navStatus = posReport.getNavStatusString();
		}
		
		if(objectData.messageId==4 || objectData.messageId==21 || objectData.messageId==0) {
			objectData.shouldIgnoreDataUnit = true;
			return;
		}
		
		if(staticReport!=null) {
			objectData.shipName = cleanShipName(staticReport.shipName);
			objectData.IMO = staticReport.imoNumber;
			objectData.shipType = staticReport.getStationTypeString(stationType, staticReport.shipType);
			objectData.dimPort = staticReport.dimC;
			objectData.dimStar = staticReport.dimD;
			objectData.dimBow = staticReport.dimA;
			objectData.dimStern = staticReport.dimB;
			objectData.draught = staticReport.staticDraught;
		}
		
	}
	
	public String cleanShipName(String name) {
		if(name==null) {
			return name;
		}
		name = name.replace("@", "");
		while(name.contains("  ")) {
			name.replace("  ", " ");
		}
		if(name.endsWith(" ")) {
			name = name.substring(0, name.length()-2);
		}
		return name;
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = -1;		
	}

}
