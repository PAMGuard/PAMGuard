package clipgenerator.clipDisplay;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;

/**
 * Dummy projector to use with the clip display which is needed as a way of passing a referecne
 * via this projector, to the clipDisplayPanel through to the symbolchooser. 
 * @author dg50
 *
 */
public class ClipDisplayProjector extends GeneralProjector<PamCoordinate> {

	private ClipDisplayPanel clipDisplayPanel;
	
	private static final ParameterType[] pTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
	
	private static final ParameterUnits[] pUnits = {ParameterUnits.SECONDS, ParameterUnits.HZ};
	
	public ClipDisplayProjector(ClipDisplayPanel clipDisplayPanel) {
		super();
		this.clipDisplayPanel = clipDisplayPanel;
		
	}

	@Override
	public Coordinate3d getCoord3d(double d1, double d2, double d3) {
		return null;
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
		return null;
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		return null;
	}

	/**
	 * @return the clipDisplayPanel
	 */
	public ClipDisplayPanel getClipDisplayPanel() {
		return clipDisplayPanel;
	}

	@Override
	public ParameterType[] getParameterTypes() {
		return pTypes;
	}

	@Override
	public ParameterUnits[] getParameterUnits() {
		return pUnits;
	}

}
