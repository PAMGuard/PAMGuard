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

import java.net.URL;
import java.util.ArrayList;

import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;
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
	 * not dialogs) with a light colour scheme.
	 */
	protected String guiCSS = defaultLightCSS();

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard GUI when
	 * in night mode. If there is not a style sheet specific to night mode, set it
	 * to null or point back to the guiCSS field.
	 * <p>
	 * Retained for sub classes which manage their own night mode sheets (see
	 * {@link PamAtlantaStyle}); {@link #getGUICSS()} now builds the dark style from
	 * {@link #darkBaseCSS} / {@link #darkOverrideCSS} / {@link #nightOverrideCSS}.
	 */
	protected String guiCSSNightMode = guiCSS;

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard standard
	 * dialogs with a light colour scheme.
	 */
	protected String dialogCSS = defaultLightCSS();

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard std
	 * dialogs when in night mode. Retained for sub classes - see
	 * {@link #guiCSSNightMode}.
	 */
	protected String dialogCSSNightMode = darkOverrideCSS();

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard sliding
	 * dialogs.
	 * <p>
	 * This is an override layered on top of the dark GUI sheets rather than a
	 * standalone style - see {@link #getSlidingDialogCSS()}. The sliding panes are
	 * always dark, whatever the colour scheme.
	 */
	protected String slidingDialogCSS = "/Resources/css/pamSettingsFlatLaf.css";

	/**
	 * Relative location of the CSS style sheet to be used for the Pamguard sliding
	 * dialogs when in night mode. If there is not a style sheet specific to night
	 * mode, set it to null or point back to the slidingDialogCSS field.
	 */
	protected String slidingDialogCSSNightMode = slidingDialogCSS;

	/**
	 * Base sheet for the dark colour schemes.
	 * <p>
	 * This is deliberately the FlatLaf sheet on every platform, not the platform
	 * specific one used for the light schemes. When a dark scheme is selected
	 * {@link PamView.PamLookAndFeel} installs FlatLaf Dark as the Swing look and
	 * feel regardless of platform (the Windows system look and feel has no dark
	 * mode), so the JavaFX panes embedded in the Swing GUI have to follow FlatLaf
	 * to match their surroundings.
	 */
	protected String darkBaseCSS = "/Resources/css/pamFlatLafLight.css";

	/**
	 * Colour override sheet layered on top of {@link #darkBaseCSS} for the Dark
	 * scheme. It redefines the palette only; it is not a standalone style.
	 */
	protected String darkOverrideCSS = "/Resources/css/pamFlatLafDark.css";

	/**
	 * Colour override sheet layered on top of {@link #darkBaseCSS} and
	 * {@link #darkOverrideCSS} for the Night scheme, turning the neutral grey/blue
	 * dark theme into the red on near black used to preserve night vision.
	 */
	protected String nightOverrideCSS = "/Resources/css/pamFlatLafNight.css";

	/**
	 * Colour override sheet layered on top of {@link #slidingDialogCSS} for the
	 * Night scheme. The sliding dialogs are always dark, so they need no Dark
	 * scheme override, only the red night one.
	 */
	protected String slidingNightOverrideCSS = "/Resources/css/pamSettingsFlatLafNight.css";

	/**
	 * Pick the default light style sheet to match the Swing look and feel set in
	 * Pamguard.main(): the Windows system look and feel on Windows, FlatLaf Light
	 * everywhere else (macOS / Linux).
	 *
	 * @return the resource path of the default light CSS style sheet
	 */
	private static String defaultLightCSS() {
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			return "/Resources/css/pamWindowsLight.css";
		}
		return "/Resources/css/pamFlatLafLight.css";
	}

	/**
	 * @return the resource path of the dark-mode override CSS style sheet.
	 */
	private static String darkOverrideCSS() {
		return "/Resources/css/pamFlatLafDark.css";
	}

	/**
	 * Get the colour scheme currently selected in {@link PamColors}.
	 *
	 * @return the current colour scheme, or null if there isn't one yet.
	 */
	protected static ColourScheme currentScheme() {
		return PamColors.getInstance().getColourScheme();
	}

	/**
	 * Is a dark (Dark or Night) colour scheme currently selected?
	 *
	 * @return true if a dark scheme is selected.
	 */
	protected static boolean isDarkScheme() {
		ColourScheme scheme = currentScheme();
		return scheme != null && scheme.isDark();
	}

	/**
	 * Is the Night (red on black) colour scheme currently selected?
	 *
	 * @return true if the night scheme is selected.
	 */
	protected static boolean isNightScheme() {
		ColourScheme scheme = currentScheme();
		return scheme != null && ColourScheme.NIGHTSCHEME.equalsIgnoreCase(scheme.getName());
	}

	/**
	 * Convert a resource path to the external form URI JavaFX needs and add it to
	 * the list. Silently ignores null paths and missing resources so that a typo in
	 * one sheet name can't take the whole GUI down.
	 *
	 * @param cssStyles list to add to
	 * @param resource  resource path of a style sheet, may be null
	 */
	protected void addSheet(ArrayList<String> cssStyles, String resource) {
		if (resource == null) {
			return;
		}
		URL url = getClass().getResource(resource);
		if (url == null) {
			System.out.println("PamDefaultStyle: unable to find style sheet " + resource);
			return;
		}
		cssStyles.add(url.toExternalForm());
	}

	/**
	 * Build the style sheet list for the main (non sliding dialog) styles. With a
	 * light scheme this is just the given light sheet; with a dark scheme it's the
	 * FlatLaf base sheet plus the dark colour override, plus the night override on
	 * top of that for the Night scheme.
	 *
	 * @param lightSheet the sheet to use for the light schemes
	 * @return the style sheets to apply, in the order they must be applied.
	 */
	private ArrayList<String> mainStyleSheets(String lightSheet) {
		ArrayList<String> cssStyles = new ArrayList<String>();
		if (isDarkScheme()) {
			addDarkSheets(cssStyles);
		}
		else {
			addSheet(cssStyles, lightSheet);
		}
		return cssStyles;
	}

	/**
	 * Add the sheets which make up the dark style: the FlatLaf base sheet, the dark
	 * colour override, and the night override on top of that for the Night scheme.
	 *
	 * @param cssStyles list to add to
	 */
	private void addDarkSheets(ArrayList<String> cssStyles) {
		addSheet(cssStyles, darkBaseCSS);
		addSheet(cssStyles, darkOverrideCSS);
		if (isNightScheme()) {
			addSheet(cssStyles, nightOverrideCSS);
		}
	}

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
		return mainStyleSheets(guiCSS);
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
		return mainStyleSheets(dialogCSS);
	}

	/**
	 * <p>
	 * Return the CSS Style sheet to be used for the Pamguard sliding dialogs.
	 * </p>
	 * <p>
	 * The sliding panes are always dark, whatever the colour scheme: a sliding pane
	 * is a panel sitting on top of a display, and in the Day scheme a light panel
	 * over a light plot would not read as one. So this is the dark GUI style - which
	 * brings every ordinary control - with the settings pane sheet on top of it to
	 * add the pane faces, hiding tabs and close buttons the GUI sheets know nothing
	 * about, and to make the palette resolve on panes which are not scene roots (see
	 * the header of pamSettingsFlatLaf.css). The night override goes last, as ever.
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
		addDarkSheets(cssStyles);
		addSheet(cssStyles, slidingDialogCSS);
		if (isNightScheme()) {
			addSheet(cssStyles, slidingNightOverrideCSS);
		}
		return cssStyles;
	}

}
