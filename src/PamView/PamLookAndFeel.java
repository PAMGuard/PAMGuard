package PamView;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;

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
 * Light schemes (Day and Print) use FlatLaf Light on every platform, unless the
 * user has asked for the standard Windows look and feel - see
 * {@link #setWindowsLookAndFeel(boolean)}. That option is offered on Windows
 * only, and since the Windows look and feel has no dark mode, the dark colour
 * schemes are not available while it is selected.
 * <p>
 * This class also owns the one other platform look and feel choice PAMGuard
 * offers, which is whether the menu bar goes in the macOS status bar at the top
 * of the screen or in the PAMGuard window - see
 * {@link #setScreenMenuBar(boolean)}.
 *
 * @author Jamie Macaulay
 */
public class PamLookAndFeel {

	/**
	 * Class name of the look and feel PAMGuard started with. Kept as a fallback for
	 * the (unlikely) case of a look and feel failing to install.
	 */
	private static String startupLafClassName;

	/**
	 * Key identifying the currently installed look and feel: the name of the colour
	 * scheme for the dark schemes, {@link #WINDOWS_LAF_KEY} for the standard Windows
	 * look and feel, and {@link ColourScheme#DAYSCHEME} for FlatLaf Light. Used to
	 * avoid re-installing the same look and feel over and over - every install
	 * triggers a full UI update of every open window.
	 */
	private static String currentLafKey;

	/**
	 * Look and feel key for the standard Windows look and feel. Not a colour scheme
	 * name, so it can never clash with one.
	 */
	private static final String WINDOWS_LAF_KEY = "WindowsSystem";

	/**
	 * Has the user asked for the standard Windows look and feel in place of FlatLaf?
	 * Only ever acted on when actually running on Windows.
	 */
	private static boolean windowsLookAndFeel;

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
		currentLafKey = ColourScheme.DAYSCHEME;
	}

	/**
	 * Is the standard Windows look and feel an option here? It is offered on Windows
	 * only - the system look and feel on macOS and Linux is not what this option is
	 * about, and on Linux in particular is often unusably bad.
	 *
	 * @return true if the Windows look and feel can be selected on this platform.
	 */
	public static boolean isWindowsLookAndFeelAvailable() {
		return PlatformInfo.calculateOS() == OSType.WINDOWS;
	}

	/**
	 * Is PAMGuard using the standard Windows look and feel rather than FlatLaf for
	 * the light colour schemes?
	 *
	 * @return true if the Windows look and feel is selected and available.
	 */
	public static boolean isWindowsLookAndFeel() {
		return windowsLookAndFeel && isWindowsLookAndFeelAvailable();
	}

	/**
	 * Select the standard Windows look and feel in place of FlatLaf Light. Ignored
	 * on platforms other than Windows.
	 * <p>
	 * This only sets the flag; call {@link PamColors#setColors()} (or use
	 * {@link PamColors#setWindowsLookAndFeel(boolean)}, which does the lot) to
	 * actually install it.
	 *
	 * @param windows true for the Windows look and feel, false for FlatLaf.
	 */
	public static void setWindowsLookAndFeel(boolean windows) {
		windowsLookAndFeel = windows;
	}

	/**
	 * Preference key for the macOS menu bar location. Held in the Java preference
	 * store rather than in PAMGuard's own settings because it has to be known
	 * before any settings have been read - see {@link #applyMenuBarLocation()}.
	 */
	private static final String SCREEN_MENU_BAR_PREF = "macScreenMenuBar";

	/**
	 * Is the macOS status bar an option here? Only on macOS - Windows and Linux
	 * both put the menu bar in the window and have nowhere else to put it.
	 *
	 * @return true if the menu bar can be moved to the macOS status bar.
	 */
	public static boolean isScreenMenuBarAvailable() {
		return PlatformInfo.calculateOS() == OSType.MACOSX;
	}

	/**
	 * Has the user asked for the menu bar to go in the macOS status bar at the top
	 * of the screen rather than in the PAMGuard window?
	 * <p>
	 * Off by default. The status bar is the macOS convention, but PAMGuard's menus
	 * are rebuilt whenever modules are added or removed and are different for each
	 * of the multiple frames a configuration can have, neither of which the status
	 * bar copes with terribly well, so the Swing menu bar in the window is the
	 * safer default.
	 *
	 * @return true if the menu bar should go in the macOS status bar.
	 */
	public static boolean isScreenMenuBar() {
		if (!isScreenMenuBarAvailable()) {
			return false;
		}
		try {
			return preferences().getBoolean(SCREEN_MENU_BAR_PREF, false);
		}
		catch (Exception e) {
			// a locked down preference store mustn't stop PAMGuard starting
			return false;
		}
	}

	/**
	 * Choose where the menu bar goes on macOS. Ignored on other platforms.
	 * <p>
	 * Takes effect the next time PAMGuard is started: the AWT / FlatLaf machinery
	 * reads the system property below once, when the look and feel is first
	 * installed, and moving an existing menu bar between the window and the status
	 * bar afterwards doesn't reliably work.
	 *
	 * @param screenMenuBar true for the macOS status bar, false for a menu bar in
	 *                      the PAMGuard window.
	 */
	public static void setScreenMenuBar(boolean screenMenuBar) {
		if (!isScreenMenuBarAvailable()) {
			return;
		}
		try {
			preferences().putBoolean(SCREEN_MENU_BAR_PREF, screenMenuBar);
			preferences().flush();
		}
		catch (Exception e) {
			System.out.printf("Unable to save the menu bar location: %s\n", e.getMessage());
		}
	}

	/**
	 * Tell AWT where the menu bar is to go, before any look and feel is installed.
	 * Must be called from Pamguard.main() as the very first thing it does with the
	 * GUI - the property is read when the look and feel builds its defaults, and is
	 * not looked at again.
	 * <p>
	 * Does nothing on Windows or Linux, which have no status bar to put a menu in.
	 */
	public static void applyMenuBarLocation() {
		if (!isScreenMenuBarAvailable()) {
			return;
		}
		// the application name shown in the macOS status bar, whichever menu bar is in use
		System.setProperty("apple.awt.application.name", "PAMGuard");
		System.setProperty("apple.laf.useScreenMenuBar", Boolean.toString(isScreenMenuBar()));
	}

	/**
	 * Menu item for the macOS menu bar location, for the Display menu.
	 *
	 * @param parentFrame frame to put the restart message on. May be null.
	 * @return the menu item, or null on platforms where the option isn't offered.
	 */
	public static JMenuItem getScreenMenuBarMenuItem(JFrame parentFrame) {
		if (!isScreenMenuBarAvailable()) {
			return null;
		}
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Menu Bar in macOS Status Bar");
		menuItem.setToolTipText("<html>Put the menu bar in the macOS status bar at the top of the screen"
				+ "<br>rather than in the PAMGuard window. Takes effect when PAMGuard is next started.</html>");
		menuItem.setSelected(isScreenMenuBar());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean screenMenuBar = menuItem.isSelected();
				setScreenMenuBar(screenMenuBar);
				JOptionPane.showMessageDialog(parentFrame,
						screenMenuBar
								? "The menu bar will move to the macOS status bar the next time PAMGuard is started."
								: "The menu bar will move back into the PAMGuard window the next time PAMGuard is started.",
						"Menu Bar Location", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		return menuItem;
	}

	/**
	 * @return the Java preference store node PAMGuard keeps its startup preferences in.
	 */
	private static Preferences preferences() {
		return Preferences.userNodeForPackage(PamLookAndFeel.class);
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
		String lafKey;
		if (isDarkSchemeName(schemeName)) {
			lafKey = schemeName;
		}
		else {
			lafKey = isWindowsLookAndFeel() ? WINDOWS_LAF_KEY : ColourScheme.DAYSCHEME;
		}
		if (lafKey.equalsIgnoreCase(currentLafKey)) {
			return false;
		}
		currentLafKey = lafKey;

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
	 * @param lafKey the look and feel key, as built by {@link #setLookAndFeel(ColourScheme)}.
	 */
	private static void installLookAndFeel(String lafKey) {
		if (startupLafClassName == null) {
			/*
			 * Not all entry points call recordStartupLookAndFeel (unit tests, the various
			 * standalone display launchers), so grab the current look and feel now rather
			 * than lose the ability to switch back to a light scheme.
			 */
			captureCurrentLookAndFeel();
		}
		try {
			if (ColourScheme.NIGHTSCHEME.equalsIgnoreCase(lafKey)) {
				FlatDarkLaf laf = new FlatDarkLaf();
				laf.setExtraDefaults(nightExtraDefaults());
				FlatLaf.setup(laf);
			}
			else if (ColourScheme.DARKSCHEME.equalsIgnoreCase(lafKey)) {
				FlatLaf.setup(new FlatDarkLaf());
			}
			else if (WINDOWS_LAF_KEY.equals(lafKey)) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else {
				FlatLaf.setup(new FlatLightLaf());
			}
		}
		catch (Exception e) {
			System.out.printf("Unable to set look and feel %s: %s\n", lafKey, e.getMessage());
			/*
			 * Fall back on whatever PAMGuard started with rather than leave the UI half
			 * rebuilt in a look and feel which has just failed to install.
			 */
			try {
				if (startupLafClassName != null) {
					UIManager.setLookAndFeel(startupLafClassName);
				}
			}
			catch (Exception e2) {
				System.out.printf("Unable to restore the startup look and feel: %s\n", e2.getMessage());
			}
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
		return isDarkSchemeName(currentLafKey);
	}

	/**
	 * Is the installed look and feel something other than the one PAMGuard starts up
	 * with (FlatLaf Light)?
	 * <p>
	 * Windows built before the settings were restored - the splash screen, anything
	 * put up during loading - were given the startup look and feel, so they need
	 * rebuilding once the GUI is up if the saved settings asked for a different one.
	 *
	 * @return true if the look and feel has moved away from the startup one.
	 */
	public static boolean isChangedFromStartup() {
		return currentLafKey != null && !ColourScheme.DAYSCHEME.equalsIgnoreCase(currentLafKey);
	}

}
