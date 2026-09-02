package PamView.paneloverlay.overlaymark;

import javafx.scene.input.KeyCombination;

/**
 * A keyboard shortcut which a mark observer offers for a set of marked data
 * units. These are the keyboard equivalents of the items the observer puts into
 * the mark popup menu, e.g. Ctrl+L to label marked clicks as an event.
 * <p>
 * The display which owns the mark is responsible for listening for key presses
 * and running the matching action. Actions are always run on the Swing event
 * dispatch thread, in the same way as the action listeners of menu items
 * converted by PamUtilsFX.swingMenuItemToFX(), so an action is free to open a
 * modal Swing dialog even though the key press itself arrives on the JavaFX
 * thread.
 *
 * @author Jamie Macaulay
 */
public class MarkKeyAction {

	private final KeyCombination keyCombination;

	private final String name;

	private final Runnable action;

	/**
	 * @param keyCombination key combination which triggers the action. Use
	 * KeyCombination.SHORTCUT_DOWN rather than CONTROL_DOWN so that the shortcut
	 * follows the platform convention.
	 * @param name human readable name for the action, e.g. for tool tips.
	 * @param action what to do, run on the Swing event dispatch thread.
	 */
	public MarkKeyAction(KeyCombination keyCombination, String name, Runnable action) {
		this.keyCombination = keyCombination;
		this.name = name;
		this.action = action;
	}

	/**
	 * @return the key combination which triggers the action.
	 */
	public KeyCombination getKeyCombination() {
		return keyCombination;
	}

	/**
	 * @return human readable name for the action.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return what to do when the key combination is pressed.
	 */
	public Runnable getAction() {
		return action;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, keyCombination == null ? "no key" : keyCombination.getDisplayText());
	}
}
