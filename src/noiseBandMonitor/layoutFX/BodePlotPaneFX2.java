package noiseBandMonitor.layoutFX;

import java.util.ArrayList;

import Filters.ANSIStandard;
import Filters.FilterMethod;
import noiseBandMonitor.BandAnalyser;
import noiseBandMonitor.BandPerformance;
import noiseBandMonitor.DecimatorMethod;
import noiseBandMonitor.NoiseBandControl;
import noiseBandMonitor.NoiseBandSettings;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Canvas-based replacement for {@link BodePlotPaneFX}.
 * <p>
 * All rendering is done with a JavaFX {@link Canvas} / {@link GraphicsContext}
 * so there are no per-point {@link javafx.scene.Node} objects created for each
 * data point. This makes the plot fast even for very large band arrays and
 * removes the ANSI-envelope rendering artefacts that appear with the FX chart
 * API (which incorrectly clips or re-orders series).
 * <p>
 * Colors are derived from the current CSS theme: the canvas background, axis
 * text and grid lines are read from the scene's computed CSS properties once
 * the pane is attached to a live scene, so the chart looks correct in both
 * light and dark modes.
 * <p>
 * The public API is identical to {@link BodePlotPaneFX} so the two classes can
 * be used interchangeably.
 *
 * @author PAMGuard
 */
public class BodePlotPaneFX2 extends PamBorderPane {

	// ── Gain scale presets ─────────────────────────────────────────────────
	private static final double GAIN_MAX    = 1.0;
	private static final double[] GAIN_TOGGLES = {-80, -10, -120};

	// ── Decimator response resolution ──────────────────────────────────────
	private static final int DECIMATOR_POINTS = 150;

	// ── Plot margins (px) ──────────────────────────────────────────────────
	private static final double MARGIN_LEFT   = 60;
	private static final double MARGIN_RIGHT  = 15;
	private static final double MARGIN_TOP    = 15;
	private static final double MARGIN_BOTTOM = 45;

	// ── Axis tick geometry ─────────────────────────────────────────────────
	private static final double TICK_LEN     = 5;
	private static final double TICK_LEN_MIN = 3;   // minor ticks

	// ── Per-series colours ─────────────────────────────────────────────────
	private static final Color   BAND_COLOR    = Color.web("#3366cc");
	private static final Color   BAND_SEL_COLOR = BAND_COLOR;
	private static final double  BAND_WIDTH    = 1.5;
	private static final double  BAND_SEL_WIDTH = 3.0;

	private static final Color   DEC_COLOR     = Color.web("#cc3333");
	private static final Color   DEC_SEL_COLOR = DEC_COLOR;
	private static final double  DEC_WIDTH     = 1.0;
	private static final double  DEC_SEL_WIDTH = 3.0;

	private static final Color[] ANSI_COLORS = {
			Color.MAGENTA,
			Color.CYAN,
			Color.ORANGE
	};
	private static final double  ANSI_WIDTH = 1.0;

	// ── Controls ───────────────────────────────────────────────────────────
	private CheckBox logFreqScale;
	private CheckBox showGrid;
	private CheckBox showDecimators;
	private CheckBox[] showStandard = new CheckBox[3];

	// ── Canvas ─────────────────────────────────────────────────────────────
	/** Resizable canvas that grows with its container pane. */
	private final ResizableCanvas canvas;
	private final Pane             canvasContainer;

	// ── Data ───────────────────────────────────────────────────────────────
	private NoiseBandControl         noiseBandControl;
	private NoiseBandSettings        workingSettings;
	private BandAnalyser             bandAnalyser;
	private ArrayList<DecimatorMethod> decimationFilters;
	private ArrayList<FilterMethod>  bandFilters;
	private int[]                    decimatorIndexes;
	private float                    sampleRate = 1f;

	// ── Selection ──────────────────────────────────────────────────────────
	private int selectedBand       = -1;
	private int selectedDecimator  = -1;
	private int gainToggleState    = 0;

	/** Guard against re-entrant rebuilds while syncing checkbox state. */
	private boolean updatingControls = false;

	/** Optional listener for band-selection clicks. */
	private BandSelectionListener bandSelectionListener;

	// ── Cached axis ranges (computed at paint time) ────────────────────────
	private double xMin, xMax, yMin, yMax;

	// ── Cached theme colours ───────────────────────────────────────────────
	/** Updated lazily whenever the theme may have changed. */
	private Color themeBackground = Color.WHITE;
	private Color themeText       = Color.BLACK;
	private Color themeGrid       = Color.LIGHTGRAY;
	private Color themeAxisBorder = Color.DARKGRAY;

	// ── Selection listener interface ───────────────────────────────────────
	@FunctionalInterface
	public interface BandSelectionListener {
		void bandSelected(int bandIndex);
	}

	// ======================================================================

	public BodePlotPaneFX2(NoiseBandControl noiseBandControl) {
		this.noiseBandControl = noiseBandControl;

		setTop(createOptionsBar());

		canvas = new ResizableCanvas();
		canvasContainer = new Pane(canvas);
		canvas.widthProperty().bind(canvasContainer.widthProperty());
		canvas.heightProperty().bind(canvasContainer.heightProperty());
		canvas.widthProperty().addListener(obs -> repaint());
		canvas.heightProperty().addListener(obs -> repaint());

		VBox.setVgrow(canvasContainer, Priority.ALWAYS);
		setCenter(canvasContainer);

		// Derive theme colors once we are part of a live scene
		sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) c -> {
					updateThemeColors();
					repaint();
				});
				updateThemeColors();
				repaint();
			}
		});

		// Mouse handlers
		canvas.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				toggleGainScale();
			} else {
				handleCanvasClick(e.getX(), e.getY());
			}
		});
	}

	// ── UI construction ──────────────────────────────────────────────────

	private HBox createOptionsBar() {
		HBox bar = new HBox(10);
		bar.setPadding(new Insets(4));

		logFreqScale = new CheckBox("Log Scale");
		logFreqScale.setSelected(true);
		logFreqScale.setOnAction(e -> { if (!updatingControls) repaint(); });

		showGrid = new CheckBox("Show Grid");
		showGrid.setSelected(true);
		showGrid.setOnAction(e -> { if (!updatingControls) repaint(); });

		showDecimators = new CheckBox("Show Decimators");
		showDecimators.setSelected(true);
		showDecimators.setOnAction(e -> { if (!updatingControls) repaint(); });

		bar.getChildren().addAll(logFreqScale, showGrid, showDecimators);

		bar.getChildren().add(new Label("   ANSI standards:"));
		for (int i = 0; i < 3; i++) {
			showStandard[i] = new CheckBox("Class " + i);
			showStandard[i].setOnAction(e -> { if (!updatingControls) repaint(); });
			bar.getChildren().add(showStandard[i]);
		}
		return bar;
	}

	// ── Public API (matches BodePlotPaneFX) ───────────────────────────────

	/**
	 * Supply new analysis results and repaint.
	 */
	public void update(NoiseBandSettings settings,
			BandAnalyser analyser,
			ArrayList<DecimatorMethod> decimationFilters,
			ArrayList<FilterMethod> bandFilters,
			int[] decimatorIndexes,
			float sampleRate) {
		this.workingSettings   = settings;
		this.bandAnalyser      = analyser;
		this.decimationFilters = decimationFilters;
		this.bandFilters       = bandFilters;
		this.decimatorIndexes  = decimatorIndexes;
		this.sampleRate        = sampleRate;

		updatingControls = true;
		try {
			logFreqScale.setSelected(settings.logFreqScale);
			showGrid.setSelected(settings.showGrid);
			showDecimators.setSelected(settings.showDecimators);
			for (int i = 0; i < 3; i++) {
				showStandard[i].setSelected(settings.getShowStandard(i));
			}
		} finally {
			updatingControls = false;
		}
		gainToggleState = settings.scaleToggleState;

		repaint();
	}

	public void writeDisplayOptions(NoiseBandSettings settings) {
		settings.logFreqScale      = logFreqScale.isSelected();
		settings.showGrid          = showGrid.isSelected();
		settings.showDecimators    = showDecimators.isSelected();
		for (int i = 0; i < 3; i++) {
			settings.setShowStandard(i, showStandard[i].isSelected());
		}
		settings.scaleToggleState = gainToggleState;
	}

	public void setBandSelectionListener(BandSelectionListener l) {
		this.bandSelectionListener = l;
	}

	public int getSelectedBand() { return selectedBand; }

	public void setSelectedBand(int band) {
		if (band != selectedBand) {
			selectedBand = band;
			selectedDecimator = -1;
			if (decimatorIndexes != null && selectedBand >= 0
					&& selectedBand < decimatorIndexes.length) {
				selectedDecimator = decimatorIndexes[selectedBand];
			}
			repaint();
		}
	}

	// ── Theme color detection ────────────────────────────────────────────

	/**
	 * Read the actual computed paint for a CSS colour variable by temporarily
	 * attaching a probe {@link javafx.scene.layout.Region} to the live scene,
	 * applying the named CSS variable as its background, and reading the result.
	 *
	 * @param cssVar e.g. {@code "-fx-pambackground"} or {@code "-fx-plotbackground"}
	 * @return the resolved {@link Color}, or {@code null} if it could not be read
	 */
	private Color probeCssColor(String cssVar) {
		if (getScene() == null) return null;
		if (!(getScene().getRoot() instanceof javafx.scene.layout.Pane)) return null;
		javafx.scene.layout.Pane pRoot = (javafx.scene.layout.Pane) getScene().getRoot();

		javafx.scene.layout.Region probe = new javafx.scene.layout.Region();
		probe.setStyle("-fx-background-color: " + cssVar + ";");
		probe.setVisible(false);
		probe.setTranslateX(-99999);
		probe.setTranslateY(-99999);
		probe.setPrefSize(10, 10);
		pRoot.getChildren().add(probe);
		probe.applyCss();
		probe.layout();

		Color result = null;
		javafx.scene.layout.Background bg = probe.getBackground();
		if (bg != null && !bg.getFills().isEmpty()) {
			javafx.scene.paint.Paint p = bg.getFills().get(0).getFill();
			if (p instanceof Color) result = (Color) p;
		}
		pRoot.getChildren().remove(probe);
		return result;
	}

	/**
	 * Derive background / text / grid / axis colours directly from the live CSS
	 * theme variables:
	 * <ul>
	 *   <li>{@code -fx-plotbackground} → canvas fill</li>
	 *   <li>{@code -fx-pambackground} → fallback canvas fill</li>
	 *   <li>Label {@code textFill} → axis text colour</li>
	 *   <li>Grid derived by blending text and background at 20 % opacity</li>
	 * </ul>
	 */
	private void updateThemeColors() {
		if (getScene() == null) return;
		if (!(getScene().getRoot() instanceof javafx.scene.layout.Pane)) return;

		// ── 1. Canvas / plot background ──────────────────────────────────────
		Background bg = this.getBackground();
		Color result = Color.WHITE; // default if CSS lookup fails
		if (bg != null && !bg.getFills().isEmpty()) {
			javafx.scene.paint.Paint p = bg.getFills().get(0).getFill();

			if (p instanceof Color) result = (Color) p;
		}

		themeBackground = result;


		// Derive text as contrasting colour from the background
		double lum = luminance(themeBackground);
		themeText = lum > 0.5 ? Color.BLACK : Color.WHITE;


		// ── 3. Grid ───────────────────────────────────────────────────────────
		// 20 % text blended into the background → subtle dashed lines
		themeGrid = themeText.interpolate(themeBackground, 0.78);

		// ── 4. Axis border ────────────────────────────────────────────────────
		// Slightly more prominent than the grid (40 % blend)
		themeAxisBorder = themeText.interpolate(themeBackground, 0.55);
	}

	/** Perceived luminance (ITU-R BT.601). */
	private static double luminance(Color c) {
		return 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
	}

	// ── Main repaint entry point ─────────────────────────────────────────

	private void repaint() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		if (w <= 0 || h <= 0) return;

		// recompute theme colors each repaint so mode changes are picked up
		updateThemeColors();

		// axis ranges
		yMin = GAIN_TOGGLES[gainToggleState % GAIN_TOGGLES.length];
		yMax = GAIN_MAX;
		double nyquist = sampleRate / 2.0;
		if (nyquist <= 0) nyquist = 25000;
		xMax = nyquist;
		if (workingSettings != null && workingSettings.getMinFrequency() > 0) {
			double mf = workingSettings.getMinFrequency();
			xMin = Math.pow(10., Math.floor(Math.log10(mf)));
		} else {
			xMin = 10.0;
		}
		if (logFreqScale.isSelected() && xMin <= 0) xMin = 1.0;

		// clear
		gc.clearRect(0, 0, w, h);
		gc.setFill(themeBackground);
		gc.fillRect(0, 0, w, h);

		// draw grid then content then axes
		if (showGrid.isSelected()) drawGrid(gc, w, h);
		drawDecimators(gc, w, h);
		drawAnsiStandards(gc, w, h);
		drawBands(gc, w, h);
		drawAxes(gc, w, h);
	}

	// ── Coordinate transforms ────────────────────────────────────────────

	/** Returns plot width in pixels (excluding margins). */
	private double plotW() { return canvas.getWidth() - MARGIN_LEFT - MARGIN_RIGHT; }
	/** Returns plot height in pixels (excluding margins). */
	private double plotH() { return canvas.getHeight() - MARGIN_TOP - MARGIN_BOTTOM; }

	/** Frequency → canvas X. */
	private double fx(double freq) {
		if (freq <= 0) return MARGIN_LEFT;
		double pw = plotW();
		if (logFreqScale.isSelected()) {
			double lo = Math.log10(Math.max(xMin, 1e-9));
			double hi = Math.log10(xMax);
			return MARGIN_LEFT + (Math.log10(freq) - lo) / (hi - lo) * pw;
		} else {
			return MARGIN_LEFT + (freq - xMin) / (xMax - xMin) * pw;
		}
	}

	/** Gain (dB) → canvas Y (top = high gain). */
	private double gy(double gain) {
		double ph = plotH();
		return MARGIN_TOP + (1.0 - (gain - yMin) / (yMax - yMin)) * ph;
	}

	/** Canvas X → frequency. */
	private double xf(double cx) {
		double pw = plotW();
		double t = (cx - MARGIN_LEFT) / pw;
		if (logFreqScale.isSelected()) {
			double lo = Math.log10(Math.max(xMin, 1e-9));
			double hi = Math.log10(xMax);
			return Math.pow(10., lo + t * (hi - lo));
		} else {
			return xMin + t * (xMax - xMin);
		}
	}

	/** Canvas Y → gain (dB). */
	private double yg(double cy) {
		double ph = plotH();
		return yMin + (1.0 - (cy - MARGIN_TOP) / ph) * (yMax - yMin);
	}

	// ── Drawing helpers ──────────────────────────────────────────────────

	private void drawGrid(GraphicsContext gc, double w, double h) {
		gc.setStroke(themeGrid);
		gc.setLineWidth(0.5);
		double[] dashes = {4, 4};
		gc.setLineDashes(dashes);

		// Vertical frequency grid lines
		for (double f : gridFrequencies()) {
			double x = fx(f);
			if (x < MARGIN_LEFT || x > w - MARGIN_RIGHT) continue;
			gc.strokeLine(x, MARGIN_TOP, x, h - MARGIN_BOTTOM);
		}

		// Horizontal gain grid lines (every 10 dB)
		double tick10 = 10.0;
		for (double g = Math.ceil(yMin / tick10) * tick10; g <= yMax; g += tick10) {
			double y = gy(g);
			gc.strokeLine(MARGIN_LEFT, y, w - MARGIN_RIGHT, y);
		}

		gc.setLineDashes((double[]) null);
	}

	private void drawBands(GraphicsContext gc, double w, double h) {
		if (bandAnalyser == null) return;
		BandPerformance[] perfs = bandAnalyser.getBandPerformances();
		if (perfs == null) return;

		for (int b = 0; b < perfs.length; b++) {
			boolean sel = (b == selectedBand);
			gc.setStroke(BAND_COLOR);
			gc.setLineWidth(sel ? BAND_SEL_WIDTH : BAND_WIDTH);
			drawFreqGainCurve(gc, perfs[b].getFrequencyList(), perfs[b].getGainListdB());
		}
	}

	private void drawDecimators(GraphicsContext gc, double w, double h) {
		if (!showDecimators.isSelected() || decimationFilters == null) return;

		for (int d = 0; d < decimationFilters.size(); d++) {
			boolean sel = (d == selectedDecimator);
			gc.setStroke(DEC_COLOR);
			gc.setLineWidth(sel ? DEC_SEL_WIDTH : DEC_WIDTH);
			drawDecimatorCurve(gc, decimationFilters.get(d));
		}
	}

	private void drawDecimatorCurve(GraphicsContext gc, DecimatorMethod dm) {
		FilterMethod fm = dm.getFilterMethod();
		double sr  = fm.getSampleRate();
		double fc  = fm.getFilterGainConstant();
		if (fc == 0) return;

		double lo    = Math.log10(Math.max(xMin, 1.0));
		double hi    = Math.log10(Math.min(xMax, sr / 2.0));
		double step  = (hi - lo) / DECIMATOR_POINTS;

		boolean first = true;
		gc.beginPath();
		for (int i = 0; i <= DECIMATOR_POINTS; i++) {
			double freq  = Math.pow(10., lo + i * step);
			if (freq <= 0 || freq >= sr / 2.0) continue;
			double omega = freq / sr * Math.PI * 2.0;
			double g     = fm.getFilterGain(omega) / fc;
			if (g <= 0) continue;
			double gdB   = 20.0 * Math.log10(g);
			double cx = fx(freq);
			double cy = gy(gdB);
			if (first) { gc.moveTo(cx, cy); first = false; }
			else        gc.lineTo(cx, cy);
		}
		gc.stroke();
	}

	private void drawAnsiStandards(GraphicsContext gc, double w, double h) {
		if (bandFilters == null || workingSettings == null) return;
		double[] relFreq = ANSIStandard.getRelFreq(workingSettings.bandType);
		if (relFreq == null) return;

		for (FilterMethod fm : bandFilters) {
			double centre = fm.getFilterParams().getCenterFreq();
			for (int cls = 0; cls < 3; cls++) {
				if (!showStandard[cls].isSelected()) continue;
				double[] minAtt = ANSIStandard.getMinAttenuation(cls);
				double[] maxAtt = ANSIStandard.getMaxAttenuation(cls);
				if (minAtt == null || maxAtt == null) continue;

				gc.setStroke(ANSI_COLORS[cls]);
				gc.setLineWidth(ANSI_WIDTH);

				// Each class produces 4 curves (upper/lower halves of min and max attenuation)
				drawAnsiCurve(gc, centre, relFreq, minAtt, true);
				drawAnsiCurve(gc, centre, relFreq, minAtt, false);
				drawAnsiCurve(gc, centre, relFreq, maxAtt, true);
				drawAnsiCurve(gc, centre, relFreq, maxAtt, false);
			}
		}
	}

	/**
	 * Draw a single ANSI attenuation boundary curve.
	 *
	 * @param centre   centre frequency of the band
	 * @param relFreq  relative frequency ratios from {@link ANSIStandard#getRelFreq}
	 * @param atten    attenuation values (positive = attenuation below 0 dB)
	 * @param upper    {@code true} → use {@code centre * relFreq[i]} (upper sideband),
	 *                 {@code false} → use {@code centre / relFreq[i]} (lower sideband)
	 */
	private void drawAnsiCurve(GraphicsContext gc, double centre,
			double[] relFreq, double[] atten, boolean upper) {
		// Walk the points and draw connected segments, skipping infinite attenuation
		boolean first = true;
		gc.beginPath();
		for (int i = 0; i < relFreq.length; i++) {
			double f   = upper ? centre * relFreq[i] : centre / relFreq[i];
			double att = atten[i];
			// Clamp infinite / very large attenuation to just below y-axis minimum
			double gainDB = (att > 9000) ? (yMin - 1) : -att;
			// Clamp to plot range so lines don't fly off-screen
			gainDB = Math.max(yMin - 1, Math.min(yMax + 1, gainDB));

			double cx = fx(f);
			double cy = gy(gainDB);
			if (first) { gc.moveTo(cx, cy); first = false; }
			else        gc.lineTo(cx, cy);
		}
		gc.stroke();
	}

	/**
	 * Draw a frequency vs gain-dB curve, skipping invalid points (&lt; −200 dB).
	 */
	private void drawFreqGainCurve(GraphicsContext gc, double[] freq, double[] gainDB) {
		if (freq == null || gainDB == null || freq.length == 0) return;
		boolean first = true;
		gc.beginPath();
		for (int i = 0; i < freq.length; i++) {
			if (gainDB[i] < -200) { first = true; continue; } // gap in data
			double cx = fx(freq[i]);
			double cy = gy(gainDB[i]);
			if (first) { gc.moveTo(cx, cy); first = false; }
			else        gc.lineTo(cx, cy);
		}
		gc.stroke();
	}

	// ── Axis drawing ─────────────────────────────────────────────────────

	private void drawAxes(GraphicsContext gc, double w, double h) {
		gc.setStroke(themeText);
		gc.setFill(themeText);
		gc.setLineWidth(1.0);
		gc.setLineDashes((double[]) null);
		gc.setFont(Font.font(10));

		double plotRight  = w - MARGIN_RIGHT;
		double plotBottom = h - MARGIN_BOTTOM;

		// Plot border
		gc.setStroke(themeAxisBorder);
		gc.strokeRect(MARGIN_LEFT, MARGIN_TOP, plotW(), plotH());
		gc.setStroke(themeText);

		// ── X axis labels ──
		for (double f : gridFrequencies()) {
			double x = fx(f);
			if (x < MARGIN_LEFT || x > plotRight) continue;
			gc.strokeLine(x, plotBottom, x, plotBottom + TICK_LEN);
			String label = formatFreq(f);
			double tw = textWidth(gc, label);
			gc.fillText(label, x - tw / 2, plotBottom + TICK_LEN + 12);
		}
		// Minor ticks (log scale only)
		if (logFreqScale.isSelected()) {
			for (double f : minorLogFrequencies()) {
				double x = fx(f);
				if (x < MARGIN_LEFT || x > plotRight) continue;
				gc.strokeLine(x, plotBottom, x, plotBottom + TICK_LEN_MIN);
			}
		}
		// X axis title
		String xTitle = "Frequency (Hz)";
		double xTW = textWidth(gc, xTitle) * 1.0; // estimate, no real metrics
		gc.fillText(xTitle, MARGIN_LEFT + plotW() / 2 - xTW / 2, h - 4);

		// ── Y axis labels ──
		double tick10 = 10.0;
		for (double g = Math.ceil(yMin / tick10) * tick10; g <= yMax; g += tick10) {
			double y = gy(g);
			gc.strokeLine(MARGIN_LEFT - TICK_LEN, y, MARGIN_LEFT, y);
			String label = String.valueOf((int) g);
			double tw = textWidth(gc, label);
			gc.fillText(label, MARGIN_LEFT - TICK_LEN - tw - 2, y + 4);
		}
		// Y axis title (rotated)
		gc.save();
		gc.translate(12, MARGIN_TOP + plotH() / 2);
		gc.rotate(-90);
		String yTitle = "Gain (dB)";
		double yTW = textWidth(gc, yTitle);
		gc.fillText(yTitle, -yTW / 2, 0);
		gc.restore();
	}

	// ── Grid frequency helpers ────────────────────────────────────────────

	/**
	 * Returns major tick/grid frequencies spanning [xMin, xMax].
	 * For a log scale these are powers of 10 and their halves; for linear
	 * scale they are at nice round intervals.
	 */
	private double[] gridFrequencies() {
		java.util.List<Double> freqs = new java.util.ArrayList<>();
		if (logFreqScale.isSelected()) {
			int lo = (int) Math.floor(Math.log10(xMin));
			int hi = (int) Math.ceil(Math.log10(xMax));
			for (int exp = lo; exp <= hi; exp++) {
				double base = Math.pow(10., exp);
				// 1×, 2×, 5× per decade
				for (double mult : new double[]{1, 2, 5}) {
					double f = base * mult;
					if (f >= xMin && f <= xMax) freqs.add(f);
				}
			}
		} else {
			double step = niceStep(xMax - xMin, 8);
			for (double f = Math.ceil(xMin / step) * step; f <= xMax; f += step) {
				freqs.add(f);
			}
		}
		return freqs.stream().mapToDouble(Double::doubleValue).toArray();
	}

	/**
	 * Minor tick frequencies for a log scale (every integer multiple within
	 * each decade).
	 */
	private double[] minorLogFrequencies() {
		java.util.List<Double> freqs = new java.util.ArrayList<>();
		int lo = (int) Math.floor(Math.log10(xMin));
		int hi = (int) Math.ceil(Math.log10(xMax));
		for (int exp = lo; exp <= hi; exp++) {
			double base = Math.pow(10., exp);
			for (int m = 1; m <= 9; m++) {
				double f = base * m;
				if (f >= xMin && f <= xMax) freqs.add(f);
			}
		}
		return freqs.stream().mapToDouble(Double::doubleValue).toArray();
	}

	// ── Mouse interaction ─────────────────────────────────────────────────

	private void handleCanvasClick(double cx, double cy) {
		if (bandAnalyser == null) { setSelectedBand(-1); return; }
		BandPerformance[] perfs = bandAnalyser.getBandPerformances();
		if (perfs == null) { setSelectedBand(-1); return; }

		int    closestBand = -1;
		double closestDist = Double.MAX_VALUE;

		for (int b = 0; b < perfs.length; b++) {
			double[] f = perfs[b].getFrequencyList();
			double[] g = perfs[b].getGainListdB();
			for (int i = 0; i < f.length; i += 20) {
				if (g[i] < -200) continue;
				double dx = fx(f[i]) - cx;
				double dy = gy(g[i]) - cy;
				double d  = dx * dx + dy * dy;
				if (d < closestDist) { closestDist = d; closestBand = b; }
			}
		}

		if (closestBand >= 0 && closestDist <= 400) {
			setSelectedBand(closestBand);
		} else {
			setSelectedBand(-1);
		}
		if (bandSelectionListener != null) {
			bandSelectionListener.bandSelected(selectedBand);
		}
	}

	private void toggleGainScale() {
		gainToggleState = (gainToggleState + 1) % GAIN_TOGGLES.length;
		repaint();
	}

	// ── Utilities ─────────────────────────────────────────────────────────

	private static String formatFreq(double f) {
		if (f >= 1000) {
			double k = f / 1000.0;
			return (k == Math.floor(k)) ? ((int) k) + "k" : k + "k";
		}
		return String.valueOf((int) Math.round(f));
	}

	/** Rough text-width estimate (≈ 6 px per character at font size 10). */
	private static double textWidth(GraphicsContext gc, String s) {
		return s.length() * 6.0;
	}

	private static double niceStep(double range, int targetTicks) {
		if (range <= 0) return 1;
		double rough = range / targetTicks;
		double mag   = Math.pow(10., Math.floor(Math.log10(rough)));
		double norm  = rough / mag;
		double nice;
		if      (norm < 1.5) nice = 1;
		else if (norm < 3.5) nice = 2;
		else if (norm < 7.5) nice = 5;
		else                 nice = 10;
		return nice * mag;
	}

	// ── ResizableCanvas ───────────────────────────────────────────────────

	/**
	 * A Canvas that correctly reports its preferred size so that the containing
	 * {@link Pane} can expand it to fill available space.
	 */
	private static class ResizableCanvas extends Canvas {

		ResizableCanvas() {
			super();
		}

		@Override
		public boolean isResizable() { return true; }

		@Override
		public double prefWidth(double height)  { return getWidth();  }

		@Override
		public double prefHeight(double width)  { return getHeight(); }

		@Override
		public double minWidth(double height)   { return 0; }

		@Override
		public double minHeight(double width)   { return 0; }
	}
}
