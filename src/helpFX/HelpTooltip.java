package helpFX;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * A JavaFX {@link Tooltip} subclass that includes an optional help-button
 * affordance.
 *
 * <p>When a {@link HelpPoint} (or short-hand string) is supplied via
 * {@link #setHelpTarget(HelpPoint)} or {@link #setHelpTarget(String)}, a small
 * "?" button is added to the tooltip graphic area. Clicking it opens the
 * PAMGuardFX help viewer at the specified page/anchor via
 * {@link HelpManager}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     HelpTooltip tip = new HelpTooltip("Choose the sample rate for data acquisition.");
 *     tip.setHelpTarget("sound_acquisition.md#sample-rate");
 *     Tooltip.install(sampleRateField, tip);
 * </pre>
 *
 * <p>If no help target is set the tooltip behaves exactly like a standard
 * {@link Tooltip}.</p>
 *
 * @author PAMGuard team
 */
public class HelpTooltip extends Tooltip {

	/** The help page/anchor this tooltip navigates to, or {@code null}. */
	private HelpPoint helpTarget;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/** Create an empty tooltip with no help button. */
	public HelpTooltip() {
		super();
	}

	/**
	 * Create a tooltip with the given text and no help button.
	 *
	 * @param text tooltip display text
	 */
	public HelpTooltip(String text) {
		super(text);
	}

	/**
	 * Create a tooltip with text and a help button that opens the specified page.
	 *
	 * @param text      tooltip display text
	 * @param helpPoint the help page/anchor to open on click
	 */
	public HelpTooltip(String text, HelpPoint helpPoint) {
		super(text);
		setHelpTarget(helpPoint);
	}

	// -------------------------------------------------------------------------
	// Help target
	// -------------------------------------------------------------------------

	/**
	 * Configure the help button to open the given {@link HelpPoint}.
	 * Passing {@code null} removes any existing help button.
	 *
	 * @param helpPoint the target, or {@code null} to remove
	 */
	public void setHelpTarget(HelpPoint helpPoint) {
		this.helpTarget = helpPoint;
		refreshGraphic();
	}

	/**
	 * Convenience method to set the help target from a shorthand string of the
	 * form {@code "file.md"} or {@code "file.md#anchor"}.
	 *
	 * @param helpString shorthand help address, or {@code null} to remove
	 */
	public void setHelpTarget(String helpString) {
		if (helpString == null || helpString.isBlank()) {
			setHelpTarget((HelpPoint) null);
			return;
		}
		String[] parts = helpString.split("#", 2);
		HelpPoint hp = parts.length == 2
				? new HelpPoint(parts[0], parts[1])
				: new HelpPoint(parts[0]);
		setHelpTarget(hp);
	}

	/** @return the currently configured help target, or {@code null} */
	public HelpPoint getHelpTarget() {
		return helpTarget;
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/** Build or remove the graphic (help button) based on the current target. */
	private void refreshGraphic() {
		if (helpTarget == null) {
			setGraphic(null);
			setContentDisplay(ContentDisplay.TEXT_ONLY);
			return;
		}

		// Create the help button
		Button helpBtn = new Button();
		helpBtn.getStyleClass().add("icon-button");
		helpBtn.setGraphic(PamGlyphDude.createPamIcon("mdi2h-help-circle-outline", 14));
		helpBtn.setStyle("-fx-background-color: transparent; -fx-padding: 2;");
		helpBtn.setTooltip(new Tooltip("Open help"));
		helpBtn.setOnAction(e -> {
			// Hide this tooltip before opening the viewer
			hide();
			HelpManager.getInstance().openHelp(helpTarget);
		});

		// A small spacer keeps the button to the right of any existing graphic
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox graphic = new HBox(4, spacer, helpBtn);
		graphic.setMinWidth(20);

		setGraphic(graphic);
		setContentDisplay(ContentDisplay.RIGHT);
	}
}
