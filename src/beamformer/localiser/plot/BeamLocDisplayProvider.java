package beamformer.localiser.plot;

import PamController.PamController;
import beamformer.localiser.BeamFormLocaliserControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class BeamLocDisplayProvider implements UserDisplayProvider {
	
	private BeamFormLocaliserControl bfLocaliserControl;
	
	public BeamLocDisplayProvider(BeamFormLocaliserControl bfLocaliserControl) {
		super();
		this.bfLocaliserControl = bfLocaliserControl;
		if (PamController.getInstance().getJCompliance()>=1.8) {
			UserDisplayControl.addUserDisplayProvider(this);
		}
		else {
			System.err.println("Cannot load a JavaFX display on Java version: "+  PamController.getInstance().getJCompliance());
		}
	}

	@Override
	public String getName() {
		return bfLocaliserControl.getUnitType() + " plots";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new BeamLocDisplayComponent(this, bfLocaliserControl, userDisplayControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return BeamLocDisplayComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

}
