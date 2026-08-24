package PamView;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

/**
 * Installs the Swing look and feel which goes with the currently selected
 * {@link ColourScheme}.
 * <p>
 * {@link PamColors} can only recolour components which implement
 * {@link ColorManaged} - it walks the component tree and sets the background and
 * foreground of everything which declares a {@link PamColors.PamColor}. That
 * covers PAMGuard's own panels and plots, but text fields, check boxes, combo
 * box popups, scroll bars, tab headers, internal frame title bars, dialog
 * borders and so on are painted by the look and feel from the values in
 * {@link UIManager}, and so stay light however the colour scheme is set. That is
 * what makes the old night mode look patchy.
 * <p>
 * The fix is to swap the whole look and feel when a dark scheme is selected.
 * PAMGuard already depends on FlatLaf, which has a proper dark theme and, more
 * usefully, allows the theme's colour variables to be overridden - so the night
 * scheme can be built as a FlatLaf dark theme with a red foreground rather than
 * having to be hand-painted control by control.
 * <p>
 * Light schemes (Day and Print) restore whatever look and feel PAMGuard started
 * with, so they are unchanged from previous versions: the Windows system look
 * and feel on Windows, FlatLaf Light on macOS and Linux.
 *
 * @author Jamie Macaulay
 */
public class PamLookAndFeel {

	/**
	 * Class name of the look and feel PAMGuard started with. This is restored
	 * whenever a light colour scheme is selected.
	 */
	private static String startupLafClassName;

	/**
	 * Name of the colour scheme the currently installed look and feel was built
	 * for. Used to avoid re-installing the same look and feel over and over -
	 * every install triggers a full UI update of every open window.
	 */
	private static String currentSchemeName;

	/**
	 * Extra FlatLaf defaults for the night scheme. These are the FlatLaf theme
	 * "variables" (see FlatDarkLaf.properties); everything else in the theme is
	 * derived from them, so overriding these few gives red text and near black
	 * backgrounds throughout, including in components PAMGuard never touches
	 * itself.
	 *
	 * @return the night mode overrides for FlatLaf's dark theme.
	 */
	private static Map<String, String> nightExtraDefaults() {
		Map<String, String> extra = new HashMap<>();
		extra.put("@background", "#1c1c1c");
		extra.put("@foreground", "#e04a4a");
		extra.put("@componentBackground", "#262626");
		extra.put("@menuBackground", "#141414");
		extra.put("@disabledForeground", "#7c3535");
		// a single accent colour, otherwise FlatLaf picks up the (blue) system accent
		extra.put("@accentColor", "#8c1f1f");
		extra.put("@accentBaseColor", "#8c1f1f");
		return extra;
	}

	/**
	 * Remember the look and feel PAMGuard was started with. Must be called from
	 * Pamguard.main() after the initial look and feel has been set, and before any
	 * colour scheme is restored from settings.
	 */
	public static void recordStartupLookAndFeel() {
		captureCurrentLookAndFeel();
		currentSchemeName = ColourScheme.DAYSCHEME;
	}

	/**
	 * Remember the currently installed look and feel as the one to go back to for
	 * the light colour schemes.
	 */
	private static void captureCurrentLookAndFeel() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf != null) {
			startupLafClassName = laf.getClass().getName();
		}
	}

	/**
	 * Install the look and feel which goes with the given colour scheme and update
	 * every open window so the change is immediate.
	 * <p>
	 * Does nothing if the look and feel for that scheme is already installed. Safe
	 * to call from any thread - the work is marshalled onto the event dispatch
	 * thread, since changing the look and feel from another thread can deadlock
	 * Swing.
	 *
	 * Installing the look and feel does not by itself change anything already on
	 * screen - call {@link #refreshWindows()} for that. The two are kept separate
	 * because they should not happen in the same pass of the event queue: see the
	 * note in {@link PamColors#setColors()}.
	 *
	 * @param colourScheme the newly selected colour scheme. May be null, which is
	 *                     treated as the day scheme.
	 * @return true if the look and feel is being changed, false if it was already
	 *         the right one.
	 */
	public static boolean setLookAndFeel(ColourScheme colourScheme) {
		String schemeName = colourScheme == null ? ColourScheme.DAYSCHEME : colourScheme.getName();
		/*
		 * Day and Print share a look and feel, so only the dark schemes need their own.
		 * Normalise so that switching between the two light schemes doesn't rebuild
		 * every window for nothing.
		 */
		String lafKey = isDarkSchemeName(schemeName) ? schemeName : ColourScheme.DAYSCHEME;
		if (lafKey.equalsIgnoreCase(currentSchemeName)) {
			return false;
		}
		currentSchemeName = lafKey;

		if (SwingUtilities.isEventDispatchThread()) {
			installLookAndFeel(lafKey);
		}
		else {
			SwingUtilities.invokeLater(() -> installLookAndFeel(lafKey));
		}
		return true;
	}

	/**
	 * Actually swap the look and feel and rebuild the UI of every open window. Must
	 * be called on the event dispatch thread.
	 *
	 * @param schemeName name of the scheme to install a look and feel for.
	 */
	private static void installLookAndFeel(String schemeName) {
		if (startupLafClassName == null) {
			/*
			 * Not all entry points call recordStartupLookAndFeel (unit tests, the various
			 * standalone display launchers), so grab the current look and feel now rather
			 * than lose the ability to switch back to a light scheme.
			 */
			captureCurrentLookAndFeel();
		}
		try {
			if (ColourScheme.NIGHTSCHEME.equalsIgnoreCase(schemeName)) {
				FlatDarkLaf laf = new FlatDarkLaf();
				laf.setExtraDefaults(nightExtraDefaults());
				FlatLaf.setup(laf);
			}
			else if (ColourScheme.DARKSCHEME.equalsIgnoreCase(schemeName)) {
				FlatLaf.setup(new FlatDarkLaf());
			}
			else if (startupLafClassName != null) {
				UIManager.setLookAndFeel(startupLafClassName);
			}
			else {
				return;
			}
		}
		catch (Exception e) {
			System.out.printf("Unable to set look and feel for colour scheme %s: %s\n",
					schemeName, e.getMessage());
		}
	}

	/**
	 * Rebuild the UI delegates of every open window so that the new look and feel
	 * takes effect without having to close and reopen anything.
	 * <p>
	 * Note that this resets component colours back to the look and feel defaults,
	 * so {@link PamColors#setColors()} must re-apply the colour scheme afterwards.
	 */
	/**
	 * Rebuild the UI of every open window from the currently installed look and
	 * feel, so that a change of look and feel is visible without having to close and
	 * reopen anything.
	 * <p>
	 * This resets component colours back to the look and feel defaults, so callers
	 * must re-apply the colour scheme afterwards.
	 */
	public static void refreshWindows() {
		if (SwingUtilities.isEventDispatchThread()) {
			updateAllWindows();
		}
		else {
			SwingUtilities.invokeLater(PamLookAndFeel::updateAllWindows);
		}
	}

	private static void updateAllWindows() {
		for (Window window : Window.getWindows()) {
			try {
				SwingUtilities.updateComponentTreeUI(window);
			}
			catch (Exception e) {
				// a single misbehaving window mustn't stop the rest updating
				System.out.printf("Error updating look and feel of %s: %s\n",
						window.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Colour scheme version each detached component was last restyled at. Weakly
	 * keyed so that a component being thrown away doesn't leave an entry behind.
	 */
	private static final Map<Component, Integer> styledVersions =
			Collections.synchronizedMap(new WeakHashMap<Component, Integer>());

	/**
	 * Bring a component which may have been off screen up to date with the current
	 * colour scheme.
	 * <p>
	 * {@link #refreshWindows()} only reaches components which are in an open window
	 * at the moment the scheme changes. Plenty of PAMGuard's Swing components are
	 * built once and then added and removed as the user moves around - module tool
	 * bars, the data source panels in the acquisition dialog, anything held in a
	 * static or a cached field. One of those which was detached when the scheme
	 * changed missed both the look and feel update and the recolouring, so it comes
	 * back still wearing the old scheme.
	 * <p>
	 * Call this just before showing such a component. Nothing happens if it is
	 * already up to date, so it is cheap to call every time.
	 *
	 * @param component the component about to be shown. May be null.
	 */
	public static void refreshComponentTheme(Component component) {
		if (component == null) {
			return;
		}
		PamColors pamColors = PamColors.getInstance();
		int version = pamColors.getColourSchemeVersion();
		Integer styledAt = styledVersions.get(component);
		if (styledAt != null && styledAt.intValue() == version) {
			return;
		}
		styledVersions.put(component, version);
		try {
			SwingUtilities.updateComponentTreeUI(component);
			if (component instanceof Container) {
				pamColors.notifyContianer((Container) component);
			}
			/*
			 * The new look and feel can have different insets and fonts, so the component
			 * needs laying out again as well as repainting.
			 */
			component.invalidate();
			if (component instanceof JComponent) {
				((JComponent) component).revalidate();
			}
			component.repaint();
		}
		catch (Exception e) {
			// a component which can't be restyled mustn't stop it being shown
			System.out.printf("Error restyling %s for the colour scheme: %s\n",
					component.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Is the named scheme one of the dark schemes? Kept separate from
	 * {@link ColourScheme#isDark()} so it can be asked about a name alone.
	 *
	 * @param schemeName the colour scheme name
	 * @return true for the Dark and Night schemes.
	 */
	public static boolean isDarkSchemeName(String schemeName) {
		return ColourScheme.DARKSCHEME.equalsIgnoreCase(schemeName)
				|| ColourScheme.NIGHTSCHEME.equalsIgnoreCase(schemeName);
	}

	/**
	 * @return true if a dark look and feel is currently installed.
	 */
	public static boolean isDarkLookAndFeel() {
		return isDarkSchemeName(currentSchemeName);
	}

}
