package PamController.soundMedium;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import Array.ArrayManager;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamView.dialog.warn.WarnOnce;

/**
 * Manages the global medium @see GlobalMedium
 * 
 * @author Jamie Macaulay 
 *
 */
public class GlobalMediumManager implements PamSettings {

	private PamController pamController; 
	
	private GlobalMediumParams globalMediumParams = new GlobalMediumParams(); 
	
	public String warning =  "	Changing to between air and water requires a PAMGuard restart\n"
							+ "	for some display changes to take effect. Settings such as\n"
							+ " sound speed, reciever sensitivity values and data unit amplitudes\n"
							+ " will be recalculated or set to new default values.\n"
							+ " <p>Data processing changes are ready to use immediately."; 
	
	public GlobalMediumManager(PamController pamController) {
		this.pamController = pamController; 
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Add menu items to the swing menu 
	 * @param menu - the menu item to add to 
	 */
	public void addGlobalMediumMenuItems(JMenu menu, JFrame frame) {
		//a group of radio button menu items
		JRadioButtonMenuItem rbMenuItem;
		
		ButtonGroup buttonGroup = new ButtonGroup(); 
		
		JMenu subMenu = new JMenu("Sound Medium");
		subMenu.setToolTipText("Select gloabl medium, Air or Water");
		menu.add(subMenu);
				
		for (int i=0; i<SoundMedium.values().length; i++) {
			rbMenuItem = new JRadioButtonMenuItem(SoundMedium.values()[i].toString());
			rbMenuItem.setSelected(true);
			rbMenuItem.setMnemonic(KeyEvent.VK_R);
			rbMenuItem.setToolTipText(GlobalMedium.getToolTip(SoundMedium.values()[i]));
			
			if (SoundMedium.values()[i]==globalMediumParams.currentMedium) rbMenuItem.setSelected(true);
			else rbMenuItem.setSelected(false);
			
			final int ii = i;
			rbMenuItem.addActionListener((action)->{
				if (globalMediumParams.currentMedium==SoundMedium.values()[ii]) return; //do nothing. 
				int goAhead = WarnOnce.showWarning(frame, "Changing Medium", warning, WarnOnce.OK_CANCEL_OPTION); 
//				System.out.println("WarnOnce: " +  goAhead); 
				if (goAhead==WarnOnce.OK_OPTION || goAhead<0) setCurrentMedium(SoundMedium.values()[ii]);
			});
			
			buttonGroup.add(rbMenuItem); 

			subMenu.add(rbMenuItem); 
		}
	}
	


	/**
	 * Set the current medium and notify all modules of the change. 
	 * @param notify - notify of a medium change. 
	 * @param currentMedium - the current medium. 
	 */
	public void setCurrentMedium(SoundMedium currentMedium) {
		setCurrentMedium(currentMedium, true); 
	}

	
	/**
	 * Set the current medium
	 * @param notify - notify of a medium change. 
	 * @param currentMedium - the current medium. 
	 */
	public void setCurrentMedium(SoundMedium currentMedium, boolean notify) {
//		System.out.print("Change Medium to " + currentMedium); 
		globalMediumParams.currentMedium=currentMedium;
		if (notify) {
			//critical that the array manager is updated first.
			ArrayManager.getArrayManager().notifyModelChanged(PamController.GLOBAL_MEDIUM_UPDATE);
			//then notify rest of model
			pamController.notifyModelChanged(PamController.GLOBAL_MEDIUM_UPDATE); 
		}
	}

	/**
	 * Get the string for the receiver type for the current medium e.g. hydrophone, microphone.
	 * @param caps - true to have the first letter capitalised
	 * @return the receiver name. 
	 */
	public String getRecieverString(boolean caps) {
		return GlobalMedium.getRecieverString(globalMediumParams.currentMedium, caps, false);
	}
	
	/**
	 * Get the string for the receiver type for the current medium e.g. hydrophone, microphone.
	 * @return the reciever name. 
	 */
	public String getRecieverString() {
		return GlobalMedium.getRecieverString(globalMediumParams.currentMedium, true, false);
	}

	/**
	 * Get the string for the default dB reference unit for receiver sensitivity e.g. dB re 1V/uPa
	 * @return the string for sensitivity values. 
	 */
	public String getdBSensString() {
		return GlobalMedium.getdBSensRefString(globalMediumParams.currentMedium); 
	}
	
	/**
	 * Get the string for the default dB reference unit e.g. dB re 1uPa. 
	 * @return the string for dB values. 
	 */
	public String getdBRefString() {
		return GlobalMedium.getdBRefString(globalMediumParams.currentMedium); 
	}


	/**
	 * Get the height coefficient value for displays. Heights are always stored so
	 * that +z points up (i.e. -g). In display height is input as depth as so must
	 * be multiplied by -1 before being stored. In air height is more sensible to
	 * think of as +z and so does not need multiplied (or multiplied by 1). The
	 * height coefficient is the value height inputs are multiplied before being
	 * stored. @see getZString
	 * 
	 * @return the height coefficient.
	 */
	public double getZCoeff() {
		return GlobalMedium.getZCoeff(globalMediumParams.currentMedium);
	}


	/**
	 * Get the z string for displays. Heights are always stored so
	 * that +z points up (i.e. -g). In display height is input as depth as so must
	 * be multiplied by -1 before being stored. In air height is more sensible to
	 * think of as +z and so does not need multiplied (or multiplied by 1). The
	 * height coefficient is the value height inputs are multiplied before being
	 * stored. @see getZString
	 * 
	 * @return the height coefficient.
	 */
	public String getZString() {
		return GlobalMedium.getZString(globalMediumParams.currentMedium, true, false);
	}
	

	/**
	 * Get the z string for displays. Heights are always stored so
	 * that +z points up (i.e. -g). In display height is input as depth as so must
	 * be multiplied by -1 before being stored. In air height is more sensible to
	 * think of as +z and so does not need multiplied (or multiplied by 1). The
	 * height coefficient is the value height inputs are multiplied before being
	 * stored. @see getZString
	 * 
	 * @param boolean caps - true to capitalise the first letter. 
	 * @return the height coefficient.
	 */
	public String getZString(boolean caps) {
		return GlobalMedium.getZString(globalMediumParams.currentMedium, caps, false);
	}
	
	/**
	 * Get the default sound speed for the current medium.
	 * @return the default sound speed in meters per second. 
	 */
	public double getDefaultSoundSpeed() {
		return GlobalMedium.getDefaultSoundSpeed(globalMediumParams.currentMedium); 
	}

	/**
	 * Get the default sensitivity of receivers. This is a very variable value but
	 * will be a few tens of dB out instead of a few hundred. 
	 * @return the default receiver sensitivity in getdBSensString() units. 
	 */
	public double getDefaultRecieverSens() {
		return GlobalMedium.getDefaultSens(globalMediumParams.currentMedium); 
	}
	
	/**
	 * Get the dB offset to convert form sensitivity value units to received amplitude units.
	 * Sensitivity values in PG are always stored in the same reference as received amplitude. However
	 * they may be different units in the display because terrestrial acoustics is annoying. 
	 * <p>
	 *  Add this to input from display to get stored unit. Subtract to get display units. 
	 * @return the daB offset.
	 */
	public  double getdBSensOffset() {
		return GlobalMedium.getdBSensOffset(globalMediumParams.currentMedium); 
	}
	
	
	/**
	 * Get default scales for dB/Hz. Typically this is used for spectrogram colour limits. 
	 * @return the default minim and maximum scale in dB/Hz
	 */
	public double[] getDefaultdBHzScales() {
		return GlobalMedium.getDefaultdBHzScales(globalMediumParams.currentMedium); 
	}

	
	/**
	 * Get the current medium sound is travelling through. 
	 * @return the current soun medium. 
	 */
	public SoundMedium getCurrentMedium() {
		return globalMediumParams.currentMedium;
	}

	public double[] getDefaultAmplitudeScales() {
		return GlobalMedium.getDefaultAmplitudeScales(globalMediumParams.currentMedium); 
	}
	
	
	
	/**
	 * @return the globalMediumParameters
	 */
	public GlobalMediumParams getGlobalMediumParameters() {
		return globalMediumParams;
	}


	@Override
	public String getUnitName() {
		return "Global Medium Settings";
	}

	@Override
	public String getUnitType() {
		return "Global Medium Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return globalMediumParams;
	}

	@Override
	public long getSettingsVersion() {
		return GlobalMediumParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		if ( ((GlobalMediumParams) pamControlledUnitSettings.getSettings())!=null) {
			globalMediumParams = ((GlobalMediumParams) pamControlledUnitSettings.getSettings()).clone();
		}
		else globalMediumParams =  new GlobalMediumParams();  
		return true;
	}

	
}
