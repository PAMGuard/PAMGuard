package Array.layoutFX;

import Array.ArrayManager;
import Array.PamArray;
import PamController.SettingsPane;
import pamViewFX.PamControlledGUIFX;

/**
 * JavaFX UI bits of the Array Manager. 
 * @author Jamie Macaulay 
 *
 */
public class ArrayGUIFX extends PamControlledGUIFX {

	/**
	 * Reference to the array manager
	 */
	private ArrayManager arrayManager;
	
	private ArraySettingsPane arraySettingsPane; 
	
	public ArrayGUIFX(ArrayManager arrayManager) {
		this.arrayManager=arrayManager; 
	}
	
	/**
	 * Get the settings pane for the array manager
	 */
	public SettingsPane<?> getSettingsPane(){
		if (arraySettingsPane==null) {
			arraySettingsPane= new ArraySettingsPane(); 
		}
		return arraySettingsPane; 
	}
	
	@Override
	public void updateParams() {
		PamArray newParams=arraySettingsPane.getParams(arrayManager.getCurrentArray());
		if (newParams!=null) arrayManager.setCurrentArray(newParams);
		//setup the controlled unit. 
		arrayManager.setupControlledUnit(); 
	}
}
