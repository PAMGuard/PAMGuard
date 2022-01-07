package clickDetector;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;

public class BTProjector extends GeneralProjector {

	private ClickBTDisplay btDisplay;
	
	/**
	 * @param btDisplay
	 */
	public BTProjector(ClickBTDisplay btDisplay) {
		super();
		this.btDisplay = btDisplay;
	}

	@Override
	public Coordinate3d getCoord3d(double d1, double d2, double d3) {
		// d1 alwasy time, 
		double c1 = btDisplay.getxAxis().getPosition(d1) + btDisplay.gethScrollManager().getDisplayStartMillis();
		double c2 = btDisplay.getyAxis().getPosition(d2);
		return new Coordinate3d(c1, c2, 0);
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
		return getCoord3d(dataObject.getCoordinate(0), dataObject.getCoordinate(1), 0);
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		double d1 = screenPosition.getCoordinate(0) - btDisplay.gethScrollManager().getDisplayStartMillis();
		d1 = btDisplay.getxAxis().getDataValue(d1);
		double d2 = btDisplay.getyAxis().getDataValue(screenPosition.getCoordinate(1));
		return new Coordinate3d(d1, d2, 0);
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getParmeterType(int)
	 */
	@Override
	public ParameterType getParmeterType(int iDim) {
		if (iDim == 0) {
			return ParameterType.TIME;
		}
		if (iDim == 1) {
			switch(btDisplay.getVScaleManager().getCoordinateType()) {
			case BTDisplayParameters.DISPLAY_BEARING:
				return ParameterType.BEARING;
			case BTDisplayParameters.DISPLAY_AMPLITUDE:
				return ParameterType.AMPLITUDE;
			case BTDisplayParameters.DISPLAY_ICI:
				return ParameterType.ICI;
			case BTDisplayParameters.DISPLAY_SLANT:
				return ParameterType.SLANTANGLE;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getParameterTypes()
	 */
	@Override
	public ParameterType[] getParameterTypes() {
		ParameterType[] t = {getParmeterType(0), getParmeterType(1)};
		return t;
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getParmeterUnits(int)
	 */
	@Override
	public ParameterUnits getParmeterUnits(int iDim) {
		return super.getParmeterUnits(iDim);
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getParameterUnits()
	 */
	@Override
	public ParameterUnits[] getParameterUnits() {
		return super.getParameterUnits();
	}

}
