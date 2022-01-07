package radardisplay;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class RadarDisplayProvider implements UserDisplayProvider {

	private RadarParameters readyParams;
	
	@Override
	public String getName() {
		return "Radar Display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return createRadarDisplay(userDisplayControl, readyParams, uniqueDisplayName);
	}

	private UserDisplayComponent createRadarDisplay(UserDisplayControl userDisplayControl, RadarParameters radarParameters,
			String uniqueDisplayName) {
		RadarDisplayComponent rdc = new RadarDisplayComponent(userDisplayControl, radarParameters, uniqueDisplayName);
		readyParams = null;
		return rdc;
	}

	@Override
	public Class getComponentClass() {
		return RadarDisplay.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the readyParams
	 */
	public RadarParameters getReadyParams() {
		return readyParams;
	}

	/**
	 * @param readyParams the readyParams to set
	 */
	public void setReadyParams(RadarParameters readyParams) {
		this.readyParams = readyParams;
	}

}
