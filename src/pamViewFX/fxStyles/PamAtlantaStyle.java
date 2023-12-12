package pamViewFX.fxStyles;

/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

/**
 * Class defining the default CSS Style sheets to use for JavaFX displays.  This class can be extended and one or more methods overridden to
 * specify new CSS styles.  Style sheets can be specified for 3 different categories: sliding dialogs, regular dialogs, and all other components
 * (incl. displays, etc).  In addition, each category can have a style sheet to use for daytime mode and one to use for nighttime mode.  The
 * relative URI paths to the individual style sheets are specified as private fields, and accessed through public methods.  The day/night switch
 * is based on the name of the current PamColors colour scheme being used. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamAtlantaStyle extends PamDefaultStyle {
	
	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard GUI (but not dialogs)
	 */
	//private String guiCSS = "/Resources/css/pamCSS.css";
	//private String guiCSS = new NordDark().getUserAgentStylesheet();
	protected String primerGuiCSS = "/Resources/css/primer-light.css";
	
	
	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard standard dialogs
	 */
	//private String dialogCSS = "/Resources/css/pamSettingsCSS.css";
	//private String dialogCSS = new PrimerDark().getUserAgentStylesheet();
	protected String primerDialogCSS = "/Resources/css/primer-dark.css";

	
	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard sliding dialogs
	 */
	//private String slidingDialogCSS = "/Resources/css/pamCSS.css";
	//private String slidingDialogCSS = new PrimerDark().getUserAgentStylesheet();
	protected String primerSlidingDialogCSS = "/Resources/css/primer-pamguard.css";
	
	public PamAtlantaStyle() {
		super.guiCSS = primerGuiCSS;
		super.dialogCSS = primerDialogCSS;
		super.slidingDialogCSS = primerSlidingDialogCSS;
	}
	

}
