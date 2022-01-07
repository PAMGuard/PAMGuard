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
public interface CommonPluginInterface {
	
	/**
	 * The default name of the plugin.
	 * <p>
	 * This field cannot be null.
	 * @return the default name of the plugin as a String.  Cannot be null.
	 */
	public String getDefaultName();
	
	/**
	 * Return the name of the helpset file.  For information on the helpset format,
	 * see {@link https://docs.oracle.com/cd/E19253-01/819-0913/author/helpset.html}.<p>
	 * If the helpset file is in a package folder of the plugins folder, make sure to
	 * include that in the filename.  For example, if the package name is MyFirstPlugin
	 * and the help file is MyPluginHelp.hs, the return should be:<p>
	 * {@code return "MyFirstPlugin/MyPluginHelp.hs";}<p>
	 * If there is no helpset file, return null.
	 * 
	 * @return
	 */
	public String getHelpSetName();

	/**
	 * Sets the name of the jar file (including path) holding the plugin code.  This
	 * method is called from {@link PamModel#loadPlugins(PamModuleInfo)} for every valid interface file
	 * found in the plugins folder.
	 * 
	 * @param jarFile String containing the jarFile name and absolute path
	 */
	public void setJarFile(String jarFile);

	/**
	 * Returns the name of the jar file holding the plugin.	The jarFile String should be declared as
	 * a class field, but does not need to be initialised to anything specific as it will be set
	 * by Pamguard in the {@link PamModel#loadPlugins(PamModuleInfo)} method when the plugin was
	 * first found.
	 * @return
	 */
	public String getJarFile();

	/**
	 * Returns the name of the developer.  Can be company name or individual.
	 * @return String containing the name of the developer.  Cannot be null.
	 */
	
	public String getDeveloperName();
	
	/**
	 * Returns the developer's contact email
	 * @return String containing the developer's contact email.  Cannot be null.
	 */
	public String getContactEmail();
	
	/**
	 * Returns the version number of the plugin
	 * @return String containing the plugin version number.  Cannot be null
	 */
	public String getVersion();
	
	/**
	 * The Pamguard version number that the plugin was developed on.
	 * @return String containing the version number of Pamguard that the plugin was developed
	 * on.  Cannot be null.
	 */
	public String getPamVerDevelopedOn();
		
	/**
	 * The Pamguard version number that the plugin has been tested on.
	 * @return String containing the latest version of Pamguard that the plugin has been tested
	 * on.  Cannot be null.
	 */
	public String getPamVerTestedOn();

	/**
	 * A brief description of the plugin.  This will be displayed in the Help>About window.  Could
	 * include not just a description of the plugin, but also the developer's website and additional
	 * contact information.
	 * @return String containing a description of the plugin.  Cannot be null
	 */
	public String getAboutText();
	
	
	
}
