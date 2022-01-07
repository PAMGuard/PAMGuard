package pamViewFX.symbol;

import java.util.ArrayList;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModifier;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxSettingsPanes.DynamicSettings;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.fxSettingsPanes.SettingsListener;

/**
 * Pane which allows the changing the settings of a symbol modifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SymbolModifierPane extends PamBorderPane implements DynamicSettings {

	private SymbolModifier symbolModifier;
	
	ArrayList<SettingsListener> settingsListeners = new ArrayList<SettingsListener>();
	
	
	private boolean allowNotify = true; 


	public SymbolModifierPane(SymbolModifier symbolModifer) {
		this.symbolModifier =symbolModifer;
	}

	/**
	 * Get the parameter for a symbol modifier.
	 * @param currParams - a symbol modifier to alter params. 
	 * @return reference to the same symbol modifier but with params now changed. 
	 */
	public StandardSymbolOptions getParams() {
		return  null;
	}

	/**
	 * 	Set the controls to show params in the symbol modifer. 
	 * @param input - the symbol modifer params. 
	 */
	public void setParams() {
		// TODO Auto-generated method stub
		
	}
	
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	public SymbolModifier getSymbolModifier() {
		return symbolModifier;

	}

	/**
	 * Add a settings listener to the pane. This will register any change in controls which changes settings. 
	 * @param settingsListener - the settings listener to add
	 */
	public void addSettingsListener(SettingsListener settingsListener){
		settingsListeners.add(settingsListener);
	} 

	/**
	 * Remove a settings listener
	 * @param settingsListener - the settings to remove. 
	 */
	public void removeSettingsListener(SettingsListener settingsListener){
		settingsListeners.add(settingsListener);
	}

	/**
	 * Notify all settings listeners of a settings change. 
	 * @param obsVal - the observable value
	 * @param oldVal - the old value	
	 * @param newVal	- the new value. 
	 */
	public void notifySettingsListeners(){
		if (allowNotify) {
			for (int i=0; i<settingsListeners.size(); i++){
				settingsListeners.get(i).settingsChanged();;
			}
		}
	}

	/**
	 * Check whether the pane can notify settings listeners
	 * @return true if settings listeners are notified on calling notifySettingsListeners()
	 */
	public boolean isAllowNotify() {
		return allowNotify;
	}

	/**
	 * Set settings listeners to be notified. Set to false to disable all listeners. 
	 * @param allowNotify - true to allow notifucation of settings listeners. 
	 */
	public void setAllowNotify(boolean allowNotify) {
		this.allowNotify = allowNotify;
	}
	
	

}
