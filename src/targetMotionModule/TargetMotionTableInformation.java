package targetMotionModule;

import java.sql.Types;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;

public class TargetMotionTableInformation extends PamTableDefinition {
	
	//SAVEABLE FIELDS
	
	PamTableItem latitude = new PamTableItem("TMLatitude", Types.DOUBLE);
	PamTableItem longitude = new PamTableItem("TMLongitude", Types.DOUBLE);
	PamTableItem beamLatitude = new PamTableItem("BeamLatitude", Types.DOUBLE);
	PamTableItem beamLongitude = new PamTableItem("BeamLongitude", Types.DOUBLE);
	PamTableItem startLatitude = new PamTableItem("StartLatitude", Types.DOUBLE);
	PamTableItem startLongitude = new PamTableItem("StartLongitude", Types.DOUBLE);
	PamTableItem endLatitude = new PamTableItem("EndLatitude", Types.DOUBLE);
	PamTableItem endLongitude = new PamTableItem("EndLongitude", Types.DOUBLE);
	PamTableItem beamTime = new PamTableItem("BeamTime", Types.TIMESTAMP);
	PamTableItem side = new PamTableItem("TMSide", Types.INTEGER);
	PamTableItem chi2 = new PamTableItem("TMChi2", Types.DOUBLE);
	PamTableItem aic = new PamTableItem("TMAIC", Types.DOUBLE);
	PamTableItem paprob = new PamTableItem("TMProbability", Types.DOUBLE);
	PamTableItem degsFreedom = new PamTableItem("TMDegsFreedom", Types.INTEGER);
	PamTableItem perpDist = new PamTableItem("TMPerpendicularDistance", Types.DOUBLE);
	PamTableItem perpdistErr = new PamTableItem("TMPerpendicularDistanceError", Types.DOUBLE);
	PamTableItem depth = new PamTableItem("TMDepth", Types.DOUBLE);
	PamTableItem depthErr = new PamTableItem("TMDepthError", Types.DOUBLE);
	PamTableItem phones = new PamTableItem("TMHydrophones", Types.INTEGER);
	PamTableItem comment = new PamTableItem("TMComment", Types.CHAR, 80);

	public TargetMotionTableInformation(String tableName, int updatePolicy) {
		
		super(tableName, updatePolicy);
		addTableItem(latitude);
		addTableItem(longitude);
		addTableItem(beamLatitude);
		addTableItem(beamLongitude);
		addTableItem(startLatitude);
		addTableItem(startLongitude);
		addTableItem(endLatitude);
		addTableItem(endLongitude);
		addTableItem(beamTime);
		addTableItem(perpDist);
		addTableItem(perpdistErr);
		addTableItem(depth);
		addTableItem(depthErr);
		addTableItem(side);
		addTableItem(chi2);
		addTableItem(aic);
		addTableItem(paprob);
		addTableItem(degsFreedom);
		addTableItem(phones);


	}
	
	PamTableItem modelName = new PamTableItem("TMModelName", Types.CHAR, 30);
	public PamTableItem getModelName() {
		return modelName;
	}

	public PamTableItem getLatitude() {
		return latitude;
	}

	public PamTableItem getLongitude() {
		return longitude;
	}

	public PamTableItem getBeamLatitude() {
		return beamLatitude;
	}

	public PamTableItem getBeamLongitude() {
		return beamLongitude;
	}

	public PamTableItem getBeamTime() {
		return beamTime;
	}

	public PamTableItem getSide() {
		return side;
	}

	public PamTableItem getChi2() {
		return chi2;
	}

	public PamTableItem getAic() {
		return aic;
	}

	public PamTableItem getPaprob() {
		return paprob;
	}

	public PamTableItem getDegsFreedom() {
		return degsFreedom;
	}

	public PamTableItem getPerpDist() {
		return perpDist;
	}

	public PamTableItem getPerpdistErr() {
		return perpdistErr;
	}

	public PamTableItem getDepth() {
		return depth;
	}

	public PamTableItem getDepthErr() {
		return depthErr;
	}

	public PamTableItem getPhones() {
		return phones;
	}

	public PamTableItem getComment() {
		return comment;
	}
	
	public PamTableItem getStartLatitude() {
		return startLatitude;
	}

	public PamTableItem getStartLongitude() {
		return startLongitude;
	}

	public PamTableItem getEndLatitude() {
		return endLatitude;
	}

	public PamTableItem getEndLongitude() {
		return endLongitude;
	}


	public void setModelName(PamTableItem modelName) {
		this.modelName = modelName;
	}

	public void setLatitude(PamTableItem latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(PamTableItem longitude) {
		this.longitude = longitude;
	}

	public void setBeamLatitude(PamTableItem beamLatitude) {
		this.beamLatitude = beamLatitude;
	}

	public void setBeamLongitude(PamTableItem beamLongitude) {
		this.beamLongitude = beamLongitude;
	}

	public void setBeamTime(PamTableItem beamTime) {
		this.beamTime = beamTime;
	}

	public void setSide(PamTableItem side) {
		this.side = side;
	}

	public void setChi2(PamTableItem chi2) {
		this.chi2 = chi2;
	}

	public void setAic(PamTableItem aic) {
		this.aic = aic;
	}

	public void setPaprob(PamTableItem paprob) {
		this.paprob = paprob;
	}

	public void setDegsFreedom(PamTableItem degsFreedom) {
		this.degsFreedom = degsFreedom;
	}

	public void setPerpDist(PamTableItem perpDist) {
		this.perpDist = perpDist;
	}

	public void setPerpdistErr(PamTableItem perpdistErr) {
		this.perpdistErr = perpdistErr;
	}

	public void setDepth(PamTableItem depth) {
		this.depth = depth;
	}

	public void setDepthErr(PamTableItem depthErr) {
		this.depthErr = depthErr;
	}

	public void setPhones(PamTableItem phones) {
		this.phones = phones;
	}

	public void setComment(PamTableItem omment) {
		this.comment = omment;
	}

	public void setStartLatitude(PamTableItem startLatitude) {
		this.startLatitude = startLatitude;
	}

	public void setStartLongitude(PamTableItem startLongitude) {
		this.startLongitude = startLongitude;
	}

	public void setEndLatitude(PamTableItem endLatitude) {
		this.endLatitude = endLatitude;
	}

	public void setEndLongitude(PamTableItem endLongitude) {
		this.endLongitude = endLongitude;
	}


}
