package userDisplayFX;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataUnit;
import pamViewFX.PamControlledGUIFX;
import userDisplay.DisplayProviderParameters;

/**
 * 
 * Class which acts as a plug in for a display in PAMGUARDFX. Modules may have
 * their own displays or a UserDisplayControlFX can be used to create a more
 * generic display, which may, for example, plug in to many different modules.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class UserDisplayControlFX extends PamControlledUnit {
	
	public static String defUnitType = "User Display FX";

	/**
	 * Holds basic information on the position of the display. 
	 */
	private DisplayProviderParameters displayProviderParams; 
	
	/**
	 * The user display process. 
	 */
	private UserDisplayProcess userDisplayProcess;

	private DisplayControlGUI displayControlGUI;
		
	public UserDisplayControlFX(String unitName) {
		super(defUnitType, unitName);
		/**
		 *Create a PamProcess which is just used to check compatible data units. This is is slightly messy but 
		 *makes data model code a lot less complicated- don't want to have to deal with individual class instances of modules. 
		 */
		this.addPamProcess(userDisplayProcess=new UserDisplayProcess(this,null)); 
	}
	
	/**
	 * Get the process for the user display. This handles compatible data units and current parent data blocks etc. 
	 * @return the user display process. 
	 */
	public UserDisplayProcess getUserDisplayProcess(){
		return userDisplayProcess;
	}
	
	public void addCompatibleUnit(Class<?extends PamDataUnit> newDataClass){
		userDisplayProcess.getCompatibleDataUnits().add(newDataClass);
	}
	
	public int getNCompatibleDataUnits(){
		return userDisplayProcess.getCompatibleDataUnits().size(); 
	}
	
	public boolean removeCompatibleDataUnit(Class<?extends PamDataUnit> newDataClass){
		return userDisplayProcess.getCompatibleDataUnits().remove(newDataClass);
	}
	
	public boolean removeCompatibleDataUnits(){
		return userDisplayProcess.getCompatibleDataUnits().removeAll(userDisplayProcess.getCompatibleDataUnits());
	}
	
	/**
	 * True if the display can accept multiple parent data blocks at the same time.  
	 * @return  true if the display can accept multiple parent data blocks. 
	 */
	public boolean isMultiParent(){
		return this.userDisplayProcess.isMultiplex(); 
	}
	
	/**
	 * Set whether the display can accept multiple parent data blocks at the same time.  
	 * @param multiParent  true if the display can accept multiple parent data blocks. 
	 */
	public void setMultiParent(boolean multiParent){
		this.userDisplayProcess.setMultiplex(multiParent);
		//this.isMultiParent=multiParent; 
	}
	
	public abstract ArrayList<UserDisplayNodeFX> getDisplays(); 

	

	/**
	 * Holds info on the GUI components of the display. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class DisplayControlGUI extends PamControlledGUIFX {

		/**
		 * Reference to the user display controlled unit.
		 */
		private UserDisplayControlFX userDisplayControlFX;

		public DisplayControlGUI(UserDisplayControlFX userDisplayControlFX) {
			this.userDisplayControlFX=userDisplayControlFX; 
		}
		
		@Override
		public ArrayList<UserDisplayNodeFX> getDisplays(){
			return UserDisplayControlFX.this.getDisplays();
		}
	
	}
	
	
	@Override
	public DisplayControlGUI getGUI(int flag) {
		if (displayControlGUI==null) displayControlGUI=new DisplayControlGUI(this);
		return displayControlGUI; 
	}
	
}
