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

package pamViewFX.fxStyles;

import java.util.ArrayList;

import PamView.ColourScheme;
import PamView.PamColors;

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
 * @author mo55
 * @author Jamie Macaulay
 *
 */
public class PamDefaultStyle {

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard GUI (but
	 * not dialogs)
	 */
	protected String guiCSS = null;

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard GUI when
	 * in night mode. If there is not a style sheet specific to night mode, set it
	 * to null or point back to the guiCSS field.
	 */
	protected String guiCSSNightMode = guiCSS;

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard standard
	 * dialogs
	 */
	protected String dialogCSS = "/Resources/css/pamDefaultDialogCSS.css";

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard std
	 * dialogs when in night mode. If there is not a style sheet specific to night
	 * mode, set it to null or point back to the dialogCSS field.
	 */
	protected String dialogCSSNightMode = dialogCSS;

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard sliding
	 * dialogs
	 */
	protected String slidingDialogCSS = "/Resources/css/pamSettingsCSS.css";

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard sliding
	 * dialogs when in night mode. If there is not a style sheet specific to night
	 * mode, set it to null or point back to the slidingDialogCSS field.
	 */
	protected String slidingDialogCSSNightMode = slidingDialogCSS;

	/**
	 * <p>
	 * Return the CSS Style sheet to be used for the Pamguard GUI (displays and
	 * such) but not the dialogs.
	 * </p>
	 * <p>
	 * If overriding this method, do not simply return a URI String. In order for
	 * the String to be in the proper format, the getClass.getResource... method
	 * should be used.
	 * 
	 * @return an array of Strings of the class containing the URI of the CSS style
	 *         sheet to use
	 */
	public ArrayList<String> getGUICSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (guiCSS==null) return cssStyles;
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& guiCSSNightMode != null) {
			cssStyles.add(getClass().getResource(guiCSSNightMode).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(guiCSS).toExternalForm());
		}
		return cssStyles;
	}

	/**
	 * <p>
	 * Return the CSS Style sheet to be used for the Pamguard settings dialogs.
	 * </p>
	 * <p>
	 * If overriding this method, do not simply return a URI String. In order for
	 * the String to be in the proper format, the getClass.getResource... method
	 * should be used.
	 * 
	 * @return an array of Strings of the class containing the URI of the CSS style
	 *         sheet to use
	 */
	public ArrayList<String> getDialogCSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (dialogCSS==null) return cssStyles;
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& dialogCSSNightMode != null) {
			cssStyles.add(getClass().getResource(dialogCSSNightMode).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(dialogCSS).toExternalForm());
		}
		return cssStyles;
	}

	/**
	 * <p>
	 * Return the CSS Style sheet to be used for the Pamguard sliding dialogs.
	 * </p>
	 * <p>
	 * If overriding this method, do not simply return a URI String. In order for
	 * the String to be in the proper format, the getClass.getResource... method
	 * should be used.
	 * 
	 * @return an array of Strings of the class containing the URI of the CSS style
	 *         sheet to use
	 */
	public ArrayList<String> getSlidingDialogCSS() {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (slidingDialogCSS==null) return cssStyles;
		if (PamColors.getInstance().getColourScheme().getName() == ColourScheme.NIGHTSCHEME
				&& slidingDialogCSSNightMode != null) {
			cssStyles.add(getClass().getResource(slidingDialogCSSNightMode).toExternalForm());
		} else {
			cssStyles.add(getClass().getResource(slidingDialogCSS).toExternalForm());
		}
		return cssStyles;
	}

}
