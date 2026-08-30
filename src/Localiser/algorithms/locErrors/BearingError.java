package Localiser.algorithms.locErrors;

import Localiser.algorithms.locErrors.json.BearingErrorJsonConverter;
import pamMaths.PamVector;

public class BearingError implements LocaliserError {

	private double meanBearing;
	private double bearingError;
	private BearingErrorDraw bearingErrorDraw;

	private static BearingErrorJsonConverter bearingErrorJsonConverter = new BearingErrorJsonConverter();
	
	public BearingError(double meanBearing, double bearingError) {
		this.meanBearing = meanBearing;
		this.bearingError = bearingError;
	}
	
	@Override
	public double getError(PamVector errorDirection) {
		return bearingError;
	}

	@Override
	public PamVector getErrorDirection() {
		return new PamVector(Math.sin(meanBearing),  Math.cos(meanBearing), 0);
	}

	@Override
	public String getJsonErrorString() {
		return bearingErrorJsonConverter.getJsonString(this);
	}

	@Override
	public LocErrorGraphics getErrorDraw() {
		if (bearingErrorDraw == null) {
			bearingErrorDraw = new BearingErrorDraw(this);
		}
		return bearingErrorDraw;
	}

	@Override
	public String getStringResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getErrorMagnitude() {
		return bearingError;
	}

	/**
	 * @return the meanBearing
	 */
	public double getMeanBearing() {
		return meanBearing;
	}

	/**
	 * @return the bearingError
	 */
	public double getBearingError() {
		return bearingError;
	}

}
