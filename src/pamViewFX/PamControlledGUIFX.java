package pamViewFX;

import java.util.ArrayList;
import PamController.PamControlledUnitGUI;
import PamController.PamGUIManager;
import PamController.SettingsPane;
import javafx.scene.layout.Pane;
import userDisplayFX.UserDisplayNodeFX;

/**
 * The PamControlled unit for modules based on JavaFX.
 * <p>
 * Note that a controlled unit has an associated ICON. This cannot be stored in
 * the GUI class as the access requires an instatitated class.
 * {@link ModuleIconFactory} should have the icon added is used instead.
 * <p>
 * The data model requires that the controlled unit has a list of compatible
 * data unit classes. This means that the function
 * {@link PamProcess#getCompatibleDataUnits() getCompatibleDataUnits} must be
 * populated in any PamProcess which has an external data block for the FX GUI
 * to work.
 * <p>
 * It is important to consider the updateParams() function. This is called from
 * the data model if the data model has changed the parent data block.
 * <p>
 * Finally it is important to notify the controller that the source datablock
 * has changed in the data model. e.g. in
 * {@link PamControlledUnit#notifyModelChanged(int type)} with the
 * controller.... if (fftProcess.getParentDataBlock()!=null){
 * this.fftParameters.dataSourceName=fftProcess.getParentDataBlock().getDataName();
 * } else fftParameters.dataSourceName="";
 *
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class PamControlledGUIFX extends PamControlledUnitGUI {
	
	/**
	 * The settings pane contains ALL possible setting for the module in one placed.
	 * Settings panes can be divided into tabs for more space etc.
	 * 
	 * @param <T>
	 * @return a Pane containing controls to change settings for module.
	 */
	public SettingsPane<?> getSettingsPane(){
		// TODO need to pass owner information to this, so that windows can open in the correct positions
		return null;
	}

	/**
	 * A list of panes to be displayed on the side panes. These are generally
	 * utility panes allowing users to, for example, quickly check status of module.
	 * 
	 * @return side pane to be displayed.
	 */
	public ArrayList<Pane> getSidePanes(){
		// TODO need to pass owner information to this, so that windows can open in the correct positions
		return null;		
	}

	/**
	 * The display is added to a tab when PamControlledUnit is added. Note that by
	 * convention PamControlledUnits which have displays are dedicated display
	 * modules e.g. the time base display. Modules, such as the click detector, fft
	 * engine etc. should only have side panes.
	 * 
	 * @return display to add to the GUI.
	 */
	public ArrayList<UserDisplayNodeFX> getDisplays(){
		return null;
	}

	/**
	 * This is called whenever a settings pane is closed. If a pamControlledUnit has
	 * settings pane then this should be used to update settings based on info input
	 * into settings pane.
	 */
	public void updateParams() {
		// TODO Auto-generated method
	}
	
	@Override
	public int getGUIFlag() {
		return PamGUIManager.FX;
	}
	


}
