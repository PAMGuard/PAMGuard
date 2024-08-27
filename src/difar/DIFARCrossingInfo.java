package difar;

import PamUtils.LatLong;
import pamMaths.PamVector;
import targetMotionModule.TargetMotionResult;

/**
 * 
 * @author Doug Gillespie
 *
 */
public class DIFARCrossingInfo {

	private LatLong crossLocation;
	private DifarDataUnit[] matchedUnits;
	private PamVector xyz;
	private Double[] errors;

	public DIFARCrossingInfo(DifarDataUnit[] matchedUnits, TargetMotionResult difarCrossing) {
		this.matchedUnits = matchedUnits;
		this.xyz = difarCrossing.getLocalisationXYZ();
		this.errors = difarCrossing.getErrors();
		crossLocation = difarCrossing.getLatLong();
		
	}

	/**
	 * Constructor to use when readin gback in binary data. 
	 * @param matchedUnits
	 * @param latLong
	 */
	public DIFARCrossingInfo(DifarDataUnit[] matchedUnits, LatLong latLong, Double[] errors) {
		this.matchedUnits = matchedUnits;
		this.crossLocation = latLong;
		this.errors = errors;
	}

	/**
	 * Constructor to use when readin gback in binary data. 
	 * @param matchedUnits
	 * @param latLong
	 */
	public DIFARCrossingInfo(DifarDataUnit[] matchedUnits, LatLong latLong) {
		this.matchedUnits = matchedUnits;
		this.crossLocation = latLong;
	}
	
	public void setLocation(LatLong latLong) {
		this.crossLocation = latLong;
	}

	/**
	 * @return the crossLocation
	 */
	public LatLong getCrossLocation() {
		return crossLocation;
	}

	/**
	 * @return the matchedUnits
	 */
	public DifarDataUnit[] getMatchedUnits() {
		return matchedUnits;
	}

	/**
	 * matchedUnits.length will be equal to the number of 
	 * channels regardless of whether they match so this
	 * method will return the number of non-null matching units
	 * (i.e. actual matches).
	 * @return The number of non-null matched units
	 */
	public int getNumberOfMatchedUnits(){
		int n = 0;
		for (int i = 0; i<matchedUnits.length; i++){
			if (matchedUnits[i] !=null)
				n++;
		}
		return n;
	}
	
	/**
	 * @return the xyz
	 */
	public PamVector getXyz() {
		return xyz;
	}

	/**
	 * @return the errors
	 */
	public Double[] getErrors() {
		return errors;
	}


}
