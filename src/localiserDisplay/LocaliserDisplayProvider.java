package localiserDisplay;

import PamController.PamController;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

/**~
 * Creates AWT instance of the localiser embedded with a swing container. 
 * @author Jamie Macaulay
 *
 */
public class LocaliserDisplayProvider  implements UserDisplayProvider {

	static {
		if (PamController.getInstance().getJCompliance()>=1.8) {
//			System.out.println("HELLL000000");
			UserDisplayControl.addUserDisplayProvider(new LocaliserDisplayProvider());
		}
		else {
			System.err.println("Cannot load a JavaFX display on Java version: "+  PamController.getInstance().getJCompliance());
		}
	}
	
	public LocaliserDisplayProvider( ) {
		super(); 
	}

	@Override
	public String getName() {
		return "Localiser Display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new LocaliserDisplayControlAWT(this, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return LocaliserDisplayControlAWT.class; 
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Gets called from user display - does nothing except force
	 * instantiation of methods - and registration !
	 */
	public static void register() {}
	
	

}
