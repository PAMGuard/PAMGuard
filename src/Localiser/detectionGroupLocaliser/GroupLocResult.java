package Localiser.detectionGroupLocaliser;

import Localiser.LocaliserModel;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.algorithms.locErrors.SimpleError;
import PamUtils.LatLong;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamVector;
import targetMotionOld.TargetMotionModel;

/**
 *
 * Result class for the a group localiser. Usually this will be a target motion localisation, however could also be a group of DIFAR buoys 
 * or other systems which uses a set of detections to localise animals. 
 * 
 * @author Doug Gillespie
 *
 */
public class GroupLocResult implements Comparable<GroupLocResult>, LocalisationChi2{

	private LatLong latLong;

	private int side;

	private Double chi2;

	private Double probability;

	private Integer nDegreesFreedom;

	private Double perpendicularDistance;

	private int referenceHydrophones;

	private LatLong beamLatLong;

	private Long beamTime;
	
	private LocaliserError error; 

	/**
	 * The first bearing in the localaisation
	 */
	private PamVector firstBearing;

	private Double firstHeading;

	/*
	 * Akaike Information Criterion (AIC)
	 */
	private Double aic;

	/**
	 * How long it took to run this method in milliseconds. 
	 */
	private Double runTimeMillis;

	private String comment;

	private LocaliserModel targetMotionModel;
	
	/**
	 * The direction of the perpendicular error. This is 
	 * the error perpendicular to the track line. If not a track line situation 
	 * then this is direction from the origin of the hydrophone array to the 
	 * Localisation. 
	 */
	private PamVector perpVecor;

	/**
	 * The number of dimensions the result contains. 
	 */
	private int dim=3;

	/**
	 * @param latLong
	 * @param chi2 
	 * @param side 
	 */
	public GroupLocResult(LocaliserModel targetMotionModel, LatLong latLong, int side, double chi2) {
		super();
		this.targetMotionModel=targetMotionModel;
		if (latLong != null) {
			this.setLatLong(latLong.clone());
		}
		this.side = side;
		this.chi2 = chi2;
	}
	
	/**
	 * Constructor for a group detection result. 
	 * @param latLong  - the location of the result
	 * @param side - the ambiguity
	 * @param chi2 - the chi2 value i.e. how well the localisation result fits the localisation model. 
	 */
	public GroupLocResult(LatLong latLong, int side, double chi2) {
		super();
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
	 * @return the side
	 */
	public int getSide() {
		return side;
	}




	/**
	 * Get the Cartesian z error in localisation. Error is 95% confidence interval. 
	 * Note z is used to define the depth. Negative is below sea surface. 
	 * @return
	 */
	public double getZError(){
		if (error == null) {
			return Double.NaN;
		}
		return error.getError(LocaliserError.zdirVec);
	}
	
	/**
	 * Convenience class to get errors in x y and z co-ordinate frame. 
	 * @return the x, y and z error. 
	 */
	public Double[] getXYZErrors() {
		Double[] errors =new Double[3]; 
		if (perpVecor==null){
			errors[0]=error.getError(LocaliserError.zdirVec);
			errors[1]=error.getError(LocaliserError.ydirVec);
			errors[2]=error.getError(LocaliserError.zdirVec);
		}
		else {
			errors[0]=error.getError(perpVecor);
			errors[1]=error.getError(perpVecor.rotate(Math.PI/2));
			errors[2]=error.getError(LocaliserError.zdirVec);
		}
		return errors;
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
	 * Get the error in the perpendicular direction. The perpindicular direction is defined by setErrorVector(PamVector perpVecor). 
	 * @return the perpendicularDistanceError
	 */
	public Double getPerpendicularDistanceError() {
		if (error == null) {
			return null;
		}
		if (SimpleError.class.isAssignableFrom(error.getClass())) {
			SimpleError se = (SimpleError) error;
			return se.getPerpError();
		}
		if (perpVecor!=null){
			return error.getError(perpVecor);
		}
		return error.getError(LocaliserError.ydirVec);
	}
	
	/**
	 * Get the error which is parallel to the perpendicular error. Note that the parallel error is 2D 
	 * so it is rotated only in heading. 
	 * @return the value of the parallel error
	 */
	public double getParallelError() {
		return error.getError(perpVecor.rotate(Math.PI/2));
	}
	
	/**
	 * Set the localisation error.
	 * @param error the error class for this loclisation
	 */
	public void setError(LocaliserError error) {
		this.error=error; 
	}

	/**
	 * Get thelocalisation error.
	 * @return error the error class for this loclisation
	 */
	public LocaliserError getLocError() {
		return error;
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
	public void setBeamLatLong(LatLong beamLatLong) {
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
	public LatLong getBeamLatLong() {
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
		String str;
		String strModel = "unknown model";
		if (targetMotionModel != null) {
			strModel = targetMotionModel.getName();
		}
		String perpDistStr = null;
		if (perpendicularDistance != null) {
			perpDistStr = String.format("%3.1f", perpendicularDistance);
//			if (errors[1] != null) {
//				perpDistStr+=String.format("�%3.2f", errors[1]);
//			}
			perpDistStr += "m";
		}
		String depthStr = null;
		if (latLong != null) {
			depthStr = String.format("%3.1f", -latLong.getHeight());
//			if (errors[2] != null) {
//				depthStr+=String.format("�%3.2f", errors[2]);
//			}
			depthStr += "m";
			str = String.format("%s: (%s,%s), perp dist' %s, depth %s, Chi2 %3.1f", 
					strModel, latLong.formatLatitude(), latLong.formatLongitude(), perpDistStr, depthStr, getChi2());
		}
		else {
			str = "Incomplete target motion localisation data";
		}
		return str;
	}

	/**
	 * @return the firstBearing
	 */
	public PamVector getFirstBearing() {
		if (firstBearing == null && getLatLong() != null) {
			// try to work it out from the localisation. 
//			if ()
		}
		return firstBearing;//TODO
	}

	/**
	 * @param firstBearing the firstBearing to set in RADIANS
	 */
	public void setFirstBearing(PamVector firstBearing) {
		this.firstBearing = firstBearing;
	}

	/**
	 * @return the firstHeading
	 */
	public Double getFirstHeading() {
		return firstHeading;
	}

	/**
	 * @param firstHeading the firstHeading to set
	 */
	public void setFirstHeading(Double firstHeading) {
		this.firstHeading = firstHeading;
	}

	public LocaliserModel getModel() {
		return targetMotionModel;
	}

	public void setModel(LocaliserModel model) {
		this.targetMotionModel=model;
	}
	
	

	/**
	 * The number of dimensions of the resutl, usually 2 or 3. 
	 * @param dim the number of dimensions
	 */
	public void setDim(int dim){
		this.dim=dim; 
	}

	/**
	 * Get the error in radians. 
	 * @return the error direction. 
	 */
	public double getErrorDirection() {
		if (getBeamLatLong()==null && getLatLong()==null){
			return 0; 
		}
		else return Math.toRadians(getBeamLatLong().bearingTo(getLatLong()));
	}
	
	/**
	 * Get the vector which points in perpendicular direction to the localisation in the xy plane. 
	 * This is used to work out perpendicular and parallel errors. 
	 * @return the vector which points in the perpindicualr direction to the localisation
	 */
	public PamVector getErrorVector() {
		return perpVecor;
	}

	/**
	 * Set the vector which points in perpendicular direction to the localisation in the xy plane. 
	 * This is used to work out perpendicular and parallel errors. 
	 * @param a vector representing the perpindicular direction of the error. 
	 */
	public void setPerpErrorVector(PamVector perpVecor) {
		this.perpVecor = perpVecor;
	}

	/**
	 * Compare two results based on AIC and if there isn't AIC
	 * then use Chi2. If no chi2, give up and say they are the same. 
	 * 
	 */
	@Override
	public int compareTo(GroupLocResult o) {
		/**
		 * Java specification is:
		 *  Compares this object with the specified object for order. 
		 *  Returns a negative integer, zero, or a positive integer as this object is 
		 *  less than, equal to, or greater than the specified object. 
		 *  We want result 0 to be the one with the lowest AIS of Chi2, so 
		 *  want to sort in ascending order of these parameters. 
		 */
		if (o == null) {
			return -1;
		}
		if (this.aic != null && o.getAic() != null) {
			return aic.compareTo(o.getAic());
		}
		if (this.chi2 != null && o.chi2 != null) {
			return chi2.compareTo(o.chi2);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see Localiser.detectionGroupLocaliser.LocalisationChi2#getNDF()
	 */
	@Override
	public Integer getNDF() {
		return nDegreesFreedom;
	}


	

	



}
