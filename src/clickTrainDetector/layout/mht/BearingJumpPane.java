package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;

/**
 * A small, reusable settings pane for the maximum bearing jump cutoff and its
 * direction. It carries exactly the same controls (and help text) as the maximum
 * jump section of {@link BearingAdvMHTPane}, but works directly on plain
 * {@code (enable, maxJumpDeg, direction)} values so it can be embedded in the UKF
 * and adaptive algorithm settings panes - which do not use the chi^2 variable
 * parameter model that {@code BearingAdvMHTPane} is built around.
 * <p>
 * All jump values are handled in DEGREES.
 *
 * @author Jamie Macaulay
 */
public class BearingJumpPane extends PamGridPane {

	private static final String TIP_MAXJUMP = "The maximum bearing jump between consecutive clicks, in degrees. \n"
			+ "This sets how much bearing change the detector tolerates: a smaller \n"
			+ "value groups clicks more tightly by bearing, a larger value is more \n"
			+ "permissive.";

	private static final String TIP_CUTOFF = "Also apply the maximum bearing jump as a hard cutoff: any click whose \n"
			+ "bearing jumps more than the maximum (in the chosen direction) is \n"
			+ "rejected and the click train is stopped. Useful when there is a high \n"
			+ "SNR and few other clicks, e.g. for sperm whales.";

	private static final String TIP_DIRECTION = "The direction of the jump. POSITVE means that only  jumps in bearing \n "
			+ "which are positive in direction are penalised. BOTH means that a jump \n"
			+ "in direciton is penalised and NEGATIVE means a jump which is negative is \n"
			+ "penalised. When using a towed hydrophone array POSITIVE is often a good \n"
			+ "as animals are usually passing the vessel from postive to negative and so \n "
			+ "a large positive bearing jump unlikely belongs to the same animal";

	/** The maximum bearing jump, in degrees. Always sets the bearing tolerance. */
	private final PamSpinner<Double> maxJumpSpinner;

	/** Whether the maximum jump is additionally applied as a hard cutoff. */
	private final CheckBox maxJumpBox;

	/** The direction of the jump that is policed by the hard cutoff. */
	private final ChoiceBox<BearingJumpDrctn> jumpDirectionChoiceBox;

	public BearingJumpPane() {
		setHgap(5);
		setVgap(5);

		int row = 0;

		Label maxJumpLbl = new Label("Maximum bearing jump ");
		Tooltip maxJumpTooltip = new Tooltip(TIP_MAXJUMP);
		maxJumpLbl.setTooltip(maxJumpTooltip);
		add(maxJumpLbl, 0, row);

		// minimum kept above zero: the value also drives the (always-on) bearing
		// tolerance, which must never be zero.
		maxJumpSpinner = new PamSpinner<Double>(0.5, Double.MAX_VALUE, 3, 1);
		maxJumpSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxJumpSpinner.setTooltip(maxJumpTooltip);
		maxJumpSpinner.setEditable(true);
		maxJumpSpinner.setPrefWidth(80);
		add(maxJumpSpinner, 1, row);
		add(new Label("°"), 2, row);
		row++;

		maxJumpBox = new CheckBox("Apply as hard cutoff");
		maxJumpBox.setTooltip(new Tooltip(TIP_CUTOFF));
		maxJumpBox.setOnAction(action -> enableCutoffControls(maxJumpBox.isSelected()));
		add(maxJumpBox, 0, row);
		setColumnSpan(maxJumpBox, 3);
		row++;

		Label jumpDirectionLbl = new Label("Jump direction");
		Tooltip jumpDirectionTooltip = new Tooltip(TIP_DIRECTION);
		jumpDirectionLbl.setTooltip(jumpDirectionTooltip);
		add(jumpDirectionLbl, 0, row);

		jumpDirectionChoiceBox = new ChoiceBox<BearingJumpDrctn>();
		jumpDirectionChoiceBox.setTooltip(jumpDirectionTooltip);
		jumpDirectionChoiceBox.getItems().addAll(BearingJumpDrctn.NEGATIVE, BearingJumpDrctn.BOTH,
				BearingJumpDrctn.POSITIVE);
		add(jumpDirectionChoiceBox, 1, row);
		setColumnSpan(jumpDirectionChoiceBox, 2);
	}

	/** The direction only applies to the hard cutoff, so it follows the checkbox. */
	private void enableCutoffControls(boolean enabled) {
		jumpDirectionChoiceBox.setDisable(!enabled);
	}

	/**
	 * Set the pane's controls from the current parameter values.
	 *
	 * @param enable     - whether the maximum jump is applied as a hard cutoff.
	 * @param maxJumpDeg - the maximum bearing jump in degrees.
	 * @param drctn      - the policed jump direction ({@code null} defaults to
	 *                   {@link BearingJumpDrctn#POSITIVE}).
	 */
	public void setParams(boolean enable, double maxJumpDeg, BearingJumpDrctn drctn) {
		maxJumpBox.setSelected(enable);
		maxJumpSpinner.getValueFactory().setValue(maxJumpDeg);
		jumpDirectionChoiceBox.getSelectionModel()
				.select(drctn == null ? BearingJumpDrctn.POSITIVE : drctn);
		enableCutoffControls(enable);
	}

	/** @return whether the maximum jump is applied as a hard cutoff. */
	public boolean isJumpEnabled() {
		return maxJumpBox.isSelected();
	}

	/** @return the maximum bearing jump in degrees. */
	public double getMaxJumpDeg() {
		return maxJumpSpinner.getValue();
	}

	/** @return the policed jump direction. */
	public BearingJumpDrctn getJumpDirection() {
		BearingJumpDrctn drctn = jumpDirectionChoiceBox.getValue();
		return drctn == null ? BearingJumpDrctn.POSITIVE : drctn;
	}

	/**
	 * Enable or disable the whole pane - e.g. when bearing tracking itself is turned
	 * off. The maximum jump value and the cutoff toggle follow availability; the
	 * direction additionally requires the cutoff to be enabled.
	 *
	 * @param available - whether bearing (and hence the control) is available.
	 */
	public void setAvailable(boolean available) {
		maxJumpSpinner.setDisable(!available);
		maxJumpBox.setDisable(!available);
		jumpDirectionChoiceBox.setDisable(!available || !maxJumpBox.isSelected());
	}

}
