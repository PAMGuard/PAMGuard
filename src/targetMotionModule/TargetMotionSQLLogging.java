package targetMotionModule;

import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import targetMotionModule.algorithms.TargetMotionModel;
import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class TargetMotionSQLLogging extends SQLLogging {
	
	TargetMotionTableInformation table ;
	
	TargetMotionControl targetMotionControl;
	/*
	 * @param colSuffix
	 */
	public TargetMotionSQLLogging(PamDataBlock<?> pamDataBlock, TargetMotionControl targetMotionControl) {
		super(pamDataBlock);
		table = new TargetMotionTableInformation("Target_Motion_Data",SQLLogging.UPDATE_POLICY_WRITENEW);
		this.targetMotionControl=targetMotionControl;
		setTableDefinition(table);
	}



	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		try{
		TargetMotionResult tmDataUnit=(TargetMotionResult) pamDataUnit;
		
		table.findTableItem("TMLatitude")					.setValue(tmDataUnit.getLatLong().getLatitude());
		table.findTableItem("TMLongitude")					.setValue(tmDataUnit.getLatLong().getLongitude());
		table.findTableItem("BeamLatitude")					.setValue(tmDataUnit.getBeamLatLong().getLatitude());
		table.findTableItem("BeamLongitude")				.setValue(tmDataUnit.getBeamLatLong().getLongitude());
		if (tmDataUnit.getStartLatLong()!=null){
			table.findTableItem("startLatitude")				.setValue(tmDataUnit.getStartLatLong().getLatitude());
			table.findTableItem("startLongitude")				.setValue(tmDataUnit.getStartLatLong().getLongitude());
		}
		if (tmDataUnit.getEndLatLong()!=null){
			table.findTableItem("endLatitude")					.setValue(tmDataUnit.getEndLatLong().getLatitude());
			table.findTableItem("endLongitude")					.setValue(tmDataUnit.getEndLatLong().getLongitude());
		}
//		System.out.println("BeamTime: "+tmDataUnit.getBeamTime()+ " "+ sqlTypes.getTimeStamp(tmDataUnit.getBeamTime()));
		table.findTableItem("BeamTime")						.setValue(sqlTypes.getTimeStamp(tmDataUnit.getBeamTime()));
		table.findTableItem("TMSide")						.setValue(tmDataUnit.getSide());
		table.findTableItem("TMChi2")						.setValue(tmDataUnit.getChi2());
		table.findTableItem("TMAIC")						.setValue(tmDataUnit.getAic());
		table.findTableItem("TMProbability")				.setValue(tmDataUnit.getProbability());
		table.findTableItem("TMDegsFreedom")				.setValue(tmDataUnit.getnDegreesFreedom());
		table.findTableItem("TMPerpendicularDistance")		.setValue(tmDataUnit.getPerpendicularDistance());
		table.findTableItem("TMPerpendicularDistanceError")	.setValue(tmDataUnit.getPerpendicularDistanceError());
		table.findTableItem("TMDepth")						.setValue(-tmDataUnit.getLatLong().getHeight());
		table.findTableItem("TMDepthError")					.setValue(tmDataUnit.getZError());
		table.findTableItem("TMHydrophones")				.setValue(tmDataUnit.getReferenceHydrophones());
		if (tmDataUnit.getComment()!=null){
			table.findTableItem("TMComment")					.setValue(tmDataUnit.getComment());
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {
	
		double latVal, longVal;
		if (table.getLatitude().getValue() == null) {
			return null;
		}
		latVal = table.getLatitude().getDoubleValue();
		longVal = table.getLongitude().getDoubleValue();
		LatLong ll = new LatLong(latVal, longVal);
		Double dep = table.getDepth().getDoubleValue();
		if (dep != null) {
			ll.setHeight(-dep);
		}
		
		LatLong sLL = null;
		if (table.getStartLatitude()!=null){
			latVal = table.getStartLatitude().getDoubleValue();
			longVal = table.getStartLongitude().getDoubleValue();
			sLL = new LatLong(latVal, longVal);
		}
		
		LatLong eLL = null;
		if (table.getEndLatitude()!=null){
			latVal = table.getEndLatitude().getDoubleValue();
			longVal = table.getEndLongitude().getDoubleValue();
			eLL = new LatLong(latVal, longVal);
		}
		
		latVal = table.getBeamLatitude().getDoubleValue();
		longVal = table.getBeamLongitude().getDoubleValue();
		LatLong bLL = new LatLong(latVal, longVal);
		
		
		Object ts = table.getBeamTime().getValue();
		
		int sideVal = table.getSide().getIntegerValue();
		double chiVal = table.getChi2().getDoubleValue();
	
		TargetMotionResult tmr = new TargetMotionResult(dataTime, null, ll, sideVal, chiVal);
		tmr.setBeamLatLong(new GpsData(bLL));
		tmr.setStartLatLong(new GpsData(sLL));
		tmr.setEndLatLong(new GpsData(eLL));
		if (ts != null) {
			tmr.setBeamTime(sqlTypes.millisFromTimeStamp(ts));
		}
		tmr.setAic((Double) table.getAic().getValue());
		tmr.setProbability((Double) table.getPaprob().getValue());
		tmr.setnDegreesFreedom((Integer) table.getDegsFreedom().getValue());
		tmr.setPerpendicularDistance((Double) table.getPerpDist().getValue());
		tmr.setPerpendicularDistanceError((Double) table.getPerpdistErr().getValue());
		if (table.getDepthErr().getValue() != null) {
			tmr.setError(2, (Double) table.getDepthErr().getValue());
		}
		tmr.setReferenceHydrophones((Integer) table.getPhones().getValue());
		tmr.setComment(table.getComment().getStringValue());
		String mName = table.getModelName().getStringValue();
		TargetMotionModel model = targetMotionControl.getTargetMotionLocaliser().findModelByName(mName, true);
		tmr.setModel(model);
		
		TargetMotionLocalisation tml = new TargetMotionLocalisation(tmr);
		tmr.setLocalisation(tml);
		
		return tmr;
	}




}
