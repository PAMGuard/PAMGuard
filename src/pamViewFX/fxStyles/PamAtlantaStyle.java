package pamViewFX.fxStyles;

import java.util.ArrayList;

import PamView.ColourScheme;
import PamView.PamColors;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;

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
 * Class defining the default CSS Style sheets to use for JavaFX displays. This
 * class can be extended and one or more methods overridden to specify new CSS
 * styles. Style sheets can be specified for 3 different categories: sliding
 * dialogs, regular dialogs, and all other components (incl. displays, etc). In
 * addition, each category can have a style sheet to use for daytime mode and
 * one to use for nighttime mode. The relative URI paths to the individual style
 * sheets are specified as private fields, and accessed through public methods.
 * The day/night switch is based on the name of the current PamColors colour
 * scheme being used.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamAtlantaStyle extends PamDefaultStyle {

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard GUI (but
	 * not dialogs)
	 */
	// private String guiCSS = "/Resources/css/pamCSS.css";
	// private String guiCSS = new NordDark().getUserAgentStylesheet();
//	protected String primerlight = "/Resources/css/primer-light.css";
	protected String primerlight = new PrimerLight().getUserAgentStylesheet();

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard standard
	 * dialogs
	 */
	// private String dialogCSS = "/Resources/css/pamSettingsCSS.css";
	// private String dialogCSS = new PrimerDark().getUserAgentStylesheet();
//	protected String primerdark = "/Resources/css/primer-dark.css";
	protected String primerdark = new PrimerDark().getUserAgentStylesheet();

	
	/**
	 * PAMGuard specific additions to the Primer CSS style including hifing pane, pop over, etc. 
	 */
	protected String primerPAMGuard = "/Resources/css/primer-pamguard.css";
	
	/**
	 * Changes the colours in primerPAMGuard to dark style
	 */
	protected String primerPAMGuardDark = "/Resources/css/primer-pamguard-dark.css";

	public PamAtlantaStyle() {
		super.guiCSS = primerlight;
		super.dialogCSS = primerdark;
		super.slidingDialogCSS = primerPAMGuard;
	}
	

	@Override
	public ArrayList<String> getGUICSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& guiCSSNightMode != null) {
			cssStyles.add(getClass().getResource(primerdark).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(primerlight).toExternalForm());
		}
		cssStyles.add(getClass().getResource(primerPAMGuard).toExternalForm());
		return cssStyles;
	}


	@Override
	public ArrayList<String> getDialogCSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& dialogCSSNightMode != null) {
			cssStyles.add(getClass().getResource(primerdark).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(primerdark).toExternalForm());
		}
		cssStyles.add(getClass().getResource(primerPAMGuard).toExternalForm());
		cssStyles.add(getClass().getResource(primerPAMGuardDark).toExternalForm());
		return cssStyles;
	}

	@Override
	public ArrayList<String> getSlidingDialogCSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& slidingDialogCSSNightMode != null) {
			cssStyles.add(getClass().getResource(primerPAMGuardDark).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(primerPAMGuardDark).toExternalForm());
		}
		return cssStyles;
	}

}
