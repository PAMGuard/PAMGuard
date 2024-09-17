package clickDetector;

import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocInfoLogging;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import pamMaths.PamVector;

public class ClickGroupLogging extends GroupLocInfoLogging {

	public ClickGroupLogging(PamDataBlock pamDataBlock, int updatePolicy) {
		super(pamDataBlock, updatePolicy);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean fillDataUnit(SQLTypes sqlTypes, PamDataUnit pamDetection) {
		// TODO Auto-generated method stub
		boolean fillOk = super.fillDataUnit(sqlTypes, pamDetection);
		GroupDetection groupDetection = (GroupDetection) pamDetection;
		GroupLocalisation tmLocalisation = groupDetection.getGroupDetectionLocalisation();
		if (tmLocalisation == null) {
			groupDetection.makeLocalisation();
			tmLocalisation = groupDetection.getGroupDetectionLocalisation();
		}
		if (tmLocalisation == null)  return false;

		GroupLocResult[] tmResult = new GroupLocResult[2];
		tmResult[0] = tmLocalisation.getGroupLocaResult(0);
		tmResult[1] = tmLocalisation.getGroupLocaResult(1);
//		GroupDetectionLocalisation gdl = groupDetection.getGroupDetectionLocalisation();
		
		Object data, data2;
		PamTableItem[] tableItems, tableItems2;
		PamTableItem tableItem;
		
		tableItems = getBearing();
		if (tableItems != null) {
			for (int i = 0; i < tableItems.length; i++) {
				data = tableItems[i].getValue();
				if (data != null) {
					tmResult[i].setFirstBearing(PamVector.fromHeadAndSlant((Double) data,0));
					tmLocalisation.addLocContents(LocContents.HAS_BEARING);
				}
			}
		}

//		tableItems = getRange();
//		if (tableItems != null) {
//			for (int i = 0; i < tableItems.length; i++) {
//				data = tableItems[i].getValue();
//				if (data != null) {
//					tmResult[i].setRange((Double) data);
//					gdl.addLocContents(AbstractLocalisation.HAS_RANGE);
//				}
//			}
//		}
		
		tableItems = getLatitude();
		tableItems2 = getLongitude();
		if (tableItems != null) {
			for (int i = 0; i < tableItems.length; i++) {
				data = tableItems[i].getValue();
				if (data != null) {
					data2 = tableItems2[i].getValue();
					tmResult[i].setLatLong(new LatLong((Double) data, (Double) data2));
					tmLocalisation.addLocContents(LocContents.HAS_LATLONG);
				}
			}
//			TargetMotionLocalisation.setNumLatLong(tableItems.length);
		}
		
		tableItem = getBearingAmbiguity();
		if (tableItem != null) {
			boolean amb = tableItem.getBooleanValue();
			if (amb) {
				tmLocalisation.addLocContents(LocContents.HAS_AMBIGUITY);
			}
		}
		
		tableItems = getParallelError();
		if (tableItems != null) {
			for (int i = 0; i < tableItems.length; i++) {
				data = tableItems[i].getDoubleValue();
				if (data != null) {
					//tmResult[i].setErrorY((Double) data);
					tmLocalisation.addLocContents(LocContents.HAS_PERPENDICULARERRORS);
				}
			}
			
		}
		
		tableItems = getPerpError();
		if (tableItems != null) {
			for (int i = 0; i < tableItems.length; i++) {
				data = tableItems[i].getDoubleValue();
				if (data != null) {
					//tmResult[i].setErrorX((Double) data);
					tmLocalisation.addLocContents(LocContents.HAS_PERPENDICULARERRORS);
				}
			}
			
		}
		
			
		return fillOk;
	}

}
