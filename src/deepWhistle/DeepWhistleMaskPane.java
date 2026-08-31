package deepWhistle;

import PamController.PamControlledUnit;
import fftManager.FFTDataBlock;
import fftManager.PamFFTControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * Settings pane for the {@link DeepWhistleMask}. The DeepWhistle mask is
 * relatively simple and has a single confidence threshold parameter which is
 * set using a spinner between 0 and 1.
 * <p>
 * It also checks that the parent FFT (spectrogram) module is configured with
 * the FFT length and hop the model expects. The model was trained on a ~2 ms
 * analysis window (~500 Hz frequency resolution) with a ~8 ms hop; these
 * correspond to a number of samples that scales with sample rate. If the source
 * is not configured correctly a warning and a button to fix it are shown,
 * otherwise a confirmation message is shown.
 *
 * @author Jamie Macaulay
 */
public class DeepWhistleMaskPane extends FFTMaskSettingsPane {

	/**
	 * Analysis window length the DeepWhistle model expects, in seconds (2 ms).
	 */
	private static final double WINDOW_SECONDS = 0.002;

	/**
	 * Hop the DeepWhistle model expects, in seconds (8 ms).
	 */
	private static final double HOP_SECONDS = 0.008;

	/**
	 * Fractional tolerance allowed on the FFT length and hop before a warning is
	 * shown (10%).
	 */
	private static final double TOLERANCE = 0.10;

	/**
	 * The analysis window length the model expects, in seconds. Subclasses (e.g.
	 * the SAM-Whistle pane) override this for models trained with a different FFT
	 * length.
	 *
	 * @return the required window length in seconds.
	 */
	protected double getWindowSeconds() {
		return WINDOW_SECONDS;
	}

	/**
	 * The hop the model expects, in seconds. Subclasses override this for models
	 * trained with a different hop.
	 *
	 * @return the required hop in seconds.
	 */
	protected double getHopSeconds() {
		return HOP_SECONDS;
	}

	/**
	 * Human readable name of the model, used in the FFT-configuration messages.
	 *
	 * @return the model name.
	 */
	protected String getModelName() {
		return "Deep Whistle";
	}

	/**
	 * The main content node holding the controls.
	 */
	private PamVBox mainPane;

	/**
	 * Spinner used to set the confidence threshold (between 0 and 1).
	 */
	private PamSpinner<Double> confidenceSpinner;

	/**
	 * Container which holds either the FFT-setup warning + fix button, or the
	 * confirmation message.
	 */
	private PamVBox fftConfigBox;

	/**
	 * The currently selected FFT data source (may be null).
	 */
	private FFTDataBlock fftSource;

	public DeepWhistleMaskPane() {
		createPane();
	}

	private void createPane() {

		confidenceSpinner = new PamSpinner<Double>(0.0, 1.0, DeepWhistleParamters.DEFAULT_CONFIDENCE, 0.05);
		confidenceSpinner.setEditable(true);
		confidenceSpinner.setPrefWidth(80);
		confidenceSpinner.setTooltip(new Tooltip(
				"The confidence threshold for the mask. FFT bins with a model confidence below this value are removed."));

		Label label = new Label("Confidence threshold");

		PamHBox confidenceBox = new PamHBox();
		confidenceBox.setSpacing(5);
		confidenceBox.setAlignment(Pos.CENTER_LEFT);
		confidenceBox.getChildren().addAll(label, confidenceSpinner);

		fftConfigBox = new PamVBox();
		fftConfigBox.setSpacing(5);

		mainPane = new PamVBox();
		mainPane.setSpacing(5);
		mainPane.setPadding(new Insets(5, 0, 5, 0));
		mainPane.getChildren().addAll(confidenceBox, fftConfigBox);

		updateFFTValidation();
	}

	@Override
	public Pane getContentNode() {
		return mainPane;
	}

	@Override
	public void setParams(MaskedFFTParamters params) {
		if (params instanceof DeepWhistleParamters) {
			confidenceSpinner.getValueFactory().setValue(((DeepWhistleParamters) params).confidenceThreshold);
		}
	}

	@Override
	public void getParams(MaskedFFTParamters params) {
		if (params instanceof DeepWhistleParamters) {
			((DeepWhistleParamters) params).confidenceThreshold = confidenceSpinner.getValue();
		}
	}

	@Override
	public void setFFTSource(FFTDataBlock fftSource) {
		this.fftSource = fftSource;
		updateFFTValidation();
	}

	/**
	 * The FFT length the model expects for the given sample rate: the number of
	 * samples in a {@value #WINDOW_SECONDS} s window. It is not snapped to a power
	 * of two (PAMGuard's FFT is fast enough for arbitrary lengths and an exact
	 * window keeps the frequency resolution matched to the model), but it is
	 * rounded to an even number - PAMGuard's FFT and the spectrogram maths assume
	 * an even FFT length, and an odd length causes array-bounds errors.
	 *
	 * @param sampleRate - the sample rate in Hz.
	 * @return the required (even) FFT length in samples.
	 */
	private int requiredFFTLength(double sampleRate) {
		int n = (int) Math.round(sampleRate * getWindowSeconds());
		if (n < 2) {
			n = 2;
		}
		if (n % 2 != 0) {
			n++; //ensure even - odd FFT lengths break the half-spectrum maths.
		}
		return n;
	}

	/**
	 * The FFT hop the model expects for the given sample rate.
	 *
	 * @param sampleRate - the sample rate in Hz.
	 * @return the required hop in samples.
	 */
	private int requiredHop(double sampleRate) {
		return (int) Math.round(sampleRate * getHopSeconds());
	}

	/**
	 * Rebuild the FFT-configuration section based on the current source. Shows a
	 * warning + fix button if the source FFT length / hop are not set correctly,
	 * otherwise a confirmation message.
	 */
	private void updateFFTValidation() {

		fftConfigBox.getChildren().clear();

		if (fftSource == null || fftSource.getSampleRate() <= 0) {
			//cannot validate - show neutral guidance.
			Label info = makeWrappedLabel(
					String.format("Select an FFT data source. The %s model expects a ~%.0f ms FFT "
					+ "length and ~%.0f ms hop (these depend on the sample rate).",
					getModelName(), getWindowSeconds() * 1000.0, getHopSeconds() * 1000.0),
					"mdi2i-information-outline", Color.GRAY);
			fftConfigBox.getChildren().add(info);
			return;
		}

		double sampleRate = fftSource.getSampleRate();
		int reqFFT = requiredFFTLength(sampleRate);
		int reqHop = requiredHop(sampleRate);

		int curFFT = fftSource.getFftLength();
		int curHop = fftSource.getFftHop();

		boolean fftOk = Math.abs(curFFT - reqFFT) <= TOLERANCE * reqFFT;
		boolean hopOk = Math.abs(curHop - reqHop) <= TOLERANCE * reqHop;

		if (fftOk && hopOk) {
			Label ok = makeWrappedLabel(
					String.format("FFT length (%d) and hop (%d) are set correctly for the %s model.",
							curFFT, curHop, getModelName()),
					"mdi2c-check-circle-outline", Color.web("#2e7d32"));
			fftConfigBox.getChildren().add(ok);
			return;
		}

		//not configured correctly - show warning and a fix button.
		Label warning = makeWrappedLabel(
				String.format("The FFT source is not set up for the %s model. Required for "
						+ "%.0f kHz: FFT length %d, hop %d (current: FFT length %d, hop %d).",
						getModelName(), sampleRate / 1000.0, reqFFT, reqHop, curFFT, curHop),
				"mdi2a-alert-outline", Color.web("#ef6c00"));

		PamButton setButton = new PamButton("Set FFT parameters");
		setButton.setOnAction(e -> applyFFTParameters(reqFFT, reqHop));

		PamFFTControl control = getSourceFFTControl();
		if (control == null) {
			setButton.setDisable(true);
			setButton.setTooltip(new Tooltip(
					"The FFT source is not a standard FFT Engine module, so it cannot be configured "
					+ "automatically. Set the FFT length and hop in the source module manually."));
		}
		else {
			setButton.setTooltip(new Tooltip("Set the FFT length and hop in the source FFT module to the required values."));
		}

		fftConfigBox.getChildren().addAll(warning, setButton);
	}

	/**
	 * Get the parent FFT control of the selected source, or null if the source is
	 * not produced by a standard FFT Engine module.
	 */
	private PamFFTControl getSourceFFTControl() {
		if (fftSource == null || fftSource.getParentProcess() == null) {
			return null;
		}
		PamControlledUnit pcu = fftSource.getParentProcess().getPamControlledUnit();
		if (pcu instanceof PamFFTControl) {
			return (PamFFTControl) pcu;
		}
		return null;
	}

	/**
	 * Apply the required FFT length and hop to the parent FFT module, then
	 * re-validate so the warning is replaced by the confirmation message.
	 *
	 * @param fftLength - the FFT length to set.
	 * @param fftHop - the hop to set.
	 */
	private void applyFFTParameters(int fftLength, int fftHop) {
		PamFFTControl control = getSourceFFTControl();
		if (control == null) {
			return;
		}

		control.getFftParameters().fftLength = fftLength;
		control.getFftParameters().fftHop = fftHop;

		//reconfigure the FFT process so the output block picks up the new settings.
		//(this mirrors what the FFT module's own settings dialog does on OK).
		control.setupControlledUnit();

		//re-validate - the source block now reports the new length / hop.
		updateFFTValidation();
	}

	/**
	 * Create a wrapped label with a coloured leading icon.
	 */
	private Label makeWrappedLabel(String text, String icon, Color color) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setMaxWidth(380);
		label.setGraphic(PamGlyphDude.createPamIcon(icon, color, PamGuiManagerFX.iconSize));
		label.setTooltip(new Tooltip(text));
		return label;
	}

}
