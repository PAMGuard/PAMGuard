package helpFX;

import javafx.application.Platform;

/**
 * Singleton manager for the PAMGuardFX help system.
 *
 * <p>Call {@link #openHelp()} to open the help viewer at its default landing
 * page, or {@link #openHelp(HelpPoint)} / {@link #openHelp(String)} to jump
 * directly to a specific module's help page or anchor.</p>
 *
 * <p>The viewer window is re-used across calls so repeated button presses
 * simply bring it to the front rather than opening multiple windows.</p>
 *
 * @author PAMGuard team
 */
public class HelpManager {

	private static HelpManager instance;

	/** The single help viewer window. Created lazily on first call. */
	private HelpViewerFX viewer;

	private HelpManager() {
	}

	/** @return the singleton {@code HelpManager} instance */
	public static synchronized HelpManager getInstance() {
		if (instance == null) {
			instance = new HelpManager();
		}
		return instance;
	}

	/**
	 * Open the help viewer at the default landing page (index or welcome page).
	 */
	public void openHelp() {
		openHelp((HelpPoint) null);
	}

	/**
	 * Open the help viewer at a specific help point identified by a shorthand
	 * string.  The string is interpreted as either:
	 * <ul>
	 *   <li>{@code "file.md"} – open that file at the top</li>
	 *   <li>{@code "file.md#anchor"} – open at that heading anchor</li>
	 * </ul>
	 *
	 * @param helpString shorthand help address, or {@code null} for the default page
	 */
	public void openHelp(String helpString) {
		if (helpString == null || helpString.isBlank()) {
			openHelp((HelpPoint) null);
			return;
		}
		String[] parts = helpString.split("#", 2);
		HelpPoint hp = parts.length == 2
				? new HelpPoint(parts[0], parts[1])
				: new HelpPoint(parts[0]);
		openHelp(hp);
	}

	/**
	 * Open the help viewer at the given {@link HelpPoint}.
	 *
	 * @param helpPoint the page and optional anchor to display, or {@code null}
	 *                  to show the default landing page
	 */
	public void openHelp(HelpPoint helpPoint) {
		Platform.runLater(() -> {
			if (viewer == null || !viewer.isShowing()) {
				viewer = new HelpViewerFX();
			}
			if (helpPoint != null) {
				viewer.navigateTo(helpPoint);
			} else {
				viewer.navigateToIndex();
			}
			viewer.show();
			viewer.toFront();
		});
	}
}
