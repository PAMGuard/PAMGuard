/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamModel;

import java.io.File;

import fftManager.FFTDataUnit;

/**
 * Interface for PAMGuard plugins.
 * <p>
 * @author MO
 *
 */
public interface PamPluginInterface extends CommonPluginInterface {

	/**
	 * Used in conjunction with {@link #allowedModes() allowedModes()} method to specify the plugin can
	 * be used in any PAMGUARD mode (Normal, Mixed and Viewer)
	 */
	public static final int ALLMODES = 0;

	/**
	 * Used in conjunction with {@link #allowedModes() allowedModes()} method to specify the plugin can
	 * only be used in PAMGUARD Viewer mode
	 */
	public static final int VIEWERONLY = 1;

	/**
	 * Used in conjunction with {@link #allowedModes() allowedModes()} method to specify the plugin can
	 * be used in PAMGUARD Normal and Mixed modes, but not Viewer mode
	 */
	public static final int NOTINVIEWER = 2;
	
	/**
	 * The class name of the plugin.  This should be the class that extends
	 * PamControlledUnit.  The correct format is PackageName.ClassName, with
	 * no extension on the class name.  Thus if the package name is MyFirstPlugin
	 * and the class name is MyPluginControl.java, the return should be:<p>
	 * {@code return "MyFirstPlugin.MyPluginControl";}
	 * <p>
	 * This field cannot be null.
	 * 
	 * @return the class name as a String
	 */
	public String getClassName();
	
	/**
	 * A short description of the plug in module.  This text is used in various informational
	 * windows displayed to the user.  The value returned here is typically the same as the text returned
	 * from the {@link #getDefaultName() getDefaultName()} method (e.g. <em>Click Detector</em> or <em>FFT (Spectrogram) Engine</em>).
	 * <br>This is the text used in the main 'Add Modules' menus and is used as the 
	 * second argument to PamModuleInfo.registerControlledUnit
	 * <p>
	 * This field cannot be null.
	 * @return String describing the plugin.  Cannot be null.
	 */
	public String getDescription();
	
	/**
	 * Due to the large number of PAMGUARD modules now in existence, you may want
	 * to have your module listed in one of the sub menus of the File/Add Modules menu.
	 * You can add your module to one of the existing groups or you can create a new
	 * group of your own. The return string of this method specifies the name of the
	 * menu group the plug-in should belong to.  Current PAMGuard menus include:
	 * <ul>
	 *<li>Maps and Mapping</li>
	 *<li>Sound Processing</li>
	 *<li>Detectors</li>
	 *<li>Classifiers</li>
	 *<li>Localisers</li>
	 *<li>Displays</li>
	 *<li>Utilities</li>
	 *<li>Visual Methods</li>
	 *<li>Sensors</li>
	 *<li>Seiche Modules</li>
	 *<li>Sound Measurements</li>
	 *</ul>
	 * If the string passed does not match one of the above options, a new menu
	 * group is added with that name.
	 * <p>
	 * This field cannot be null.
	 * @return the name of the menu group to include the plugin in.  Cannot be null.
	 */
	public String getMenuGroup();
	
	/**
	 * String containing the tool tip that will be displayed when hovering over
	 * the menu item.
	 * <p>
	 * This field cannot be null.
	 * 
	 * @return ToolTip, as a String.  Cannot be null.
	 */
	public String getToolTip();
	
	/**
	 * Your module may be dependent on data from some other PAMGUARD module.
	 * For example, the click detector requires raw data from an acquisition module,
	 * the whistle detector required raw data from a FFT module and the GPS module
	 * requires data from an NMEA data source.If the plug-in requires a different
	 * module to function, specify that dependent module here (see
	 * {@link PamDependency PamDependency} for the constructors).<p>
	 * For example, a dependency on the FFT Engine would be defined as:<p>
	 * {@code return new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl");}<p>
	 * which tells Pamguard that the plug-in is dependent on some source of FFTDataUnit, and
	 * that a possible source of this type of data is the fftManager.PamFFTControl module.<p>
	 * If the plug-in has no dependencies, this field should be null.
	 * @return
	 * @see PamDependency
	 */
	public PamDependency getDependency();
	
	/**
	 * Minimum number of instances of this plugin.  If the minimum number is greater than 0, 
	 * then PAMGuard will automatically create that number of modules at start-up.
	 * <p>
	 * This field must be an integer >= 0
	 * @return
	 */
	public int getMinNumber();
	
	/**
	 * Maximum number of instances of this plugin.  PAMGuard will prevent the user from
	 * creating more modules than the number specified here.  Returning 0 indicates
	 * that there is no maximum number of instances.
	 * <p>
	 * This field can be an integer >= 1 and > the minimum number (to specify a specific
	 * number), or 0 (to indicate no limits on the number).
	 * @return
	 */
	public int getMaxNumber();
	
	/**
	 * The number of instances to instantiate when this module is loaded
	 * <p>
	 * This field must be an integer >= the minimum number
	 * @return
	 */
	public int getNInstances();
	
	/**
	 * Whether or not this plugin should be hidden in the menu.  True=hidden, False=visible.
	 * Except for rare circumstances, this method should return false.
	 * <p>
	 * This field cannot be null.
	 * @return Hide status.  True=hidden, False=visible.  Cannot be null.
	 */
	public boolean isItHidden();
	
	/**
	 * Specifies the type of PAMGuard mode that the plugin is allowed to run in.  Options are:
	 * <ul>
	 *<li>{@link #ALLMODES PamPluginInterface.ALLMODES}</li>
	 *<li>{@link #VIEWERONLY PamPluginInterface.VIEWERONLY}</li>
	 *<li>{@link #NOTINVIEWER PamPluginInterface.NOTINVIEWER}</li>
	 *</ul>
	 * 
	 * @return allowable run modes.  Cannot be null
	 */
	public int allowedModes();
	
}
