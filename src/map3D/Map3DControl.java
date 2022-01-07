package map3D;

import PamController.PamControlledUnit;
import userDisplay.UserDisplayControl;

public class Map3DControl extends PamControlledUnit {

	public static final String unitType = "Map 3D";
	
	private 
	Map3DDisplayProvider map3dDisplayProvider;
	
	public Map3DControl(String unitName) {
		super(unitType, unitName);
		map3dDisplayProvider = new Map3DDisplayProvider(this);
		UserDisplayControl.addUserDisplayProvider(map3dDisplayProvider);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		UserDisplayControl.removeDisplayProvider(map3dDisplayProvider);
		return super.removeUnit();
	}

}
