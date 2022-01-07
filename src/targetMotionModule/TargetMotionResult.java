package targetMotionModule;

import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamVector;
import targetMotionModule.algorithms.TargetMotionModel;

@SuppressWarnings("rawtypes")
public class TargetMotionResult extends PamDataUnit {

	private LatLong latLong;
	
	private TargetMotionModel model;

	private int side;

	private Double chi2;
	
	private Double probability;
	
	private Integer nDegreesFreedom;
	
	private Double perpendicularDistance;
	
	private ArrayList<ArrayList<Point3f>> MCMCJumpResults;

	private int referenceHydrophones;
	
	private GpsData beamLatLong;
	
	private GpsData startLatLong;
	
	private GpsData endLatLong;
	
	private Long beamTime;
	
	private Double[] errors = new Double[3];
	
	private PamVector localisationResults;
	
	/*
	 * Akaike Information Criterion (AIC)
	 */
	private Double aic;
	
	/**
	 * How long it took to run this method in milliseconds. 
	 */
	private Double runTimeMillis;
	
	private String comment;

	/**
	 * @param latLong
	 * @param chi2 
	 * @param side 
	 */
	public TargetMotionResult(long timeMillis, TargetMotionModel model, LatLong latLong, int side, double chi2) {
		super(timeMillis);
		this.setModel(model);
		if (latLong != null) {
			this.setLatLong(latLong.clone());
		}
		this.side = side;
		this.chi2 = chi2;
	}

	/**
	 * @return the latLong
	 */
	public LatLong getLatLong() {
		return latLong;
	}

	/**
	 * @param latLong the latLong to set
	 */
	public void setLatLong(LatLong latLong) {
		this.latLong = latLong;
	}

	/**
	 * @return the model
	 */
	public TargetMotionModel getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(TargetMotionModel model) {
		this.model = model;
	}

	/**
	 * @return the side
	 */
	public int getSide() {
		return side;
	}
	
   public void setMCMCJumps(ArrayList<ArrayList<Point3f>> MCMCResults){
		this.MCMCJumpResults=MCMCResults;
	}
	
	public ArrayList<ArrayList<Point3f>> getMCMCJumps(){
		
		return  MCMCJumpResults;
	}
	
	/**
	 * Set the error along a particular dimension
	 * @param iDim dimension (0=x, 1=y, 2=z);
	 * @param error error in metres. 
	 */
	public void setError(int iDim, double error) {
		errors[iDim] = new Double(error);
	}
	
	/**
	 * Get the error along a particular dimension
	 * @param iDim dimension (0=x, 1=y, 2=z);
	 * @return error in metres or NaN if not defined. 
	 */
	public double getError(int iDim) {
		if (errors[iDim] == null) {
			return Double.NaN;
		}
		return errors[iDim];
	}
	
	/**
	 * set Cartesian x Error;  Error should be 95% Confidence interval;
	 * @param errorX
	 */
	public void setErrorX(double errorX){
		errors[0] = new Double(errorX);
		
	}
	
	/**
	 * set Cartesian y Error;  Error should be 95% Confidence interval;
	 * @param errorY
	 */
	public void setErrorY(double errorY){
		errors[1] = new Double(errorY);
	}
	/**
	 * set Cartesian z Error. Error should be 95% Confidence interval;
	 * Note z is used to define the depth. Negative is below sea surface. 
	 * @param errorZ
	 */
	public void setErrorZ(double errorZ){
		errors[2] = new Double(errorZ);
	}
	
	/**
	 * Get x,y,z Errors. Error is 95% confidence interval. Note z is used to define the depth. Negative is below sea surface. 
	 * @return
	 */
	public Double[] getErrors(){
		return errors;
		
	}
	
	public double[] getErrorsd(){
	double[] errorsd=new double[3];
	for (int i=0; i<errorsd.length;i++){
		if (errors!=null){
		errorsd[i]=errors[i];}
		
	}

		return errorsd;

	}
	/**
	 * Get the Cartesian x error in localisation. Error is 95% confidence interval. 
	 * @return
	 */
	public double getXError(){
		if (errors[0] == null) {
			return Double.NaN;
		}
		return errors[0];
	}
	
	/**
	 * Get the Cartesian y error in localisation. Error is 95% confidence interval. 
	 * @return
	 */
	public double getYError(){
		if (errors[1] == null) {
			return Double.NaN;
		}
		return errors[1];
	}
	
	/**
	 * Get the Cartesian z error in localisation. Error is 95% confidence interval. 
	 * Note z is used to define the depth. Negative is below sea surface. 
	 * @return
	 */
	public double getZError(){
		if (errors[2] == null) {
			return Double.NaN;
		}
		return errors[2];
	}

	
	
	
	

	/**
	 * @param side the side to set
	 */
	public void setSide(int side) {
		this.side = side;
	}

	/**
	 * @return the chi2
	 */
	public Double getChi2() {
		return chi2;
	}

	/**
	 * @param chi2 the chi2 to set
	 */
	public void setChi2(double chi2) {
		this.chi2 = chi2;
	}

	/**
	 * @return the perpendicularDistance
	 */
	public Double getPerpendicularDistance() {
		return perpendicularDistance;
	}

	/**
	 * @param perpendicularDistance the perpendicularDistance to set
	 */
	public void setPerpendicularDistance(Double perpendicularDistance) {
		this.perpendicularDistance = perpendicularDistance;
	}
	
	
	

	/**
	 * @return the perpendicularDistanceError
	 */
	public Double getPerpendicularDistanceError() {
		return errors[1];
	}

	/**
	 * @param perpendicularDistanceError the perpendicularDistanceError to set
	 */
	public void setPerpendicularDistanceError(Double perpendicularDistanceError) {	
		errors[1] = perpendicularDistanceError;
	}

	/**
	 * @return the aic
	 */
	public Double getAic() {
		return aic;
	}

	/**
	 * @param aic the aic to set
	 */
	public void setAic(Double aic) {
		this.aic = aic;
	}

	/**
	 * @return the runTimeMillis
	 */
	public Double getRunTimeMillis() {
		return runTimeMillis;
	}

	/**
	 * @param runTimeMillis the runTimeMillis to set
	 */
	public void setRunTimeMillis(Double runTimeMillis) {
		this.runTimeMillis = runTimeMillis;
	}

	/**
	 * @return the nDegreesFreedom
	 */
	public Integer getnDegreesFreedom() {
		return nDegreesFreedom;
	}

	/**
	 * @param nDegreesFreedom the nDegreesFreedom to set
	 */
	public void setnDegreesFreedom(Integer nDegreesFreedom) {
		this.nDegreesFreedom = nDegreesFreedom;
	}

	/**
	 * @return the probability
	 */
	public Double getProbability() {
		return probability;
	}

	/**
	 * @param probability the probability to set
	 */
	public void setProbability(Double probability) {
		this.probability = probability;
	}

	/**
	 * @return the referenceHydrophones
	 */
	public int getReferenceHydrophones() {
		return referenceHydrophones;
	}

	/**
	 * @param referenceHydrophones the referenceHydrophones to set
	 */
	public void setReferenceHydrophones(int referenceHydrophones) {
		this.referenceHydrophones = referenceHydrophones;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	/**
	 * @param beamLatLong the beamLatLong to set
	 */
	public void setBeamLatLong(GpsData beamLatLong) {
		if (beamLatLong != null) {
			this.beamLatLong = beamLatLong.clone();
		}
		else {
			this.beamLatLong = null;
		}
	}

	/**
	 * @return the beamLatLong
	 */
	public GpsData getBeamLatLong() {
		return beamLatLong;
	}

	/**
	 * @return the beamTime
	 */
	public Long getBeamTime() {
		return beamTime;
	}

	/**
	 * @param beamTime the beamTime to set
	 */
	public void setBeamTime(Long beamTime) {
		this.beamTime = beamTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String strModel = "unknown model";
		if (model != null) {
			strModel = model.getName();
		}
		String perpDistStr = null;
		if (perpendicularDistance != null) {
			perpDistStr = String.format("%3.1f", perpendicularDistance);
			if (errors[1] != null) {
				perpDistStr+=String.format("±%3.2f", errors[1]);
			}
			perpDistStr += "m";
		}
		String depthStr = null;
		depthStr = String.format("%3.1f", -latLong.getHeight());
		if (errors[2] != null) {
			depthStr+=String.format("±%3.2f", errors[2]);
		}
		depthStr += "m";
		String str = String.format("%s: (%s,%s), perp dist' %s, depth %s", 
				strModel, latLong.formatLatitude(), latLong.formatLongitude(), perpDistStr, depthStr);
		
		return str;
	}

	public PamVector getLocalisationXYZ() {
		return localisationResults;
	}

	/**
	 * Get the location on the track line where the first acoustic detection was made. 
	 * @return latLong of the location of the first location on the track line where a detection was made. 
	 */
	public GpsData getStartLatLong() {
		return startLatLong;
	}

	/**
	 * Get the location on the track line where the last acoustic detection was made. 
	 * @return latLong of the location of the last location on the track line where a detection was made. 
	 */
	public GpsData getEndLatLong() {
		return endLatLong;
	}

	/**
	 * Set the location on the track line of the first detection used in this target motion loclaisation. 
	 * @param startLatLong-latLong first detection origin
	 */
	public void setStartLatLong(GpsData startLatLong) {
		this.startLatLong = startLatLong;
	}

	/**
	 * Set the location on the track line of the last detection used in this target motion loclaisation. 
	 * @param startLatLong-latLong- last detection origin. 
	 */
	public void setEndLatLong(GpsData endLatLong) {
		this.endLatLong = endLatLong;
	}




	
}
