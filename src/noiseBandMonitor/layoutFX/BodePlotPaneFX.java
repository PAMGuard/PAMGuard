package noiseBandMonitor.layoutFX;

import java.util.ArrayList;
import java.util.List;

import Filters.ANSIStandard;
import Filters.FilterMethod;
import noiseBandMonitor.BandAnalyser;
import noiseBandMonitor.BandPerformance;
import noiseBandMonitor.DecimatorMethod;
import noiseBandMonitor.NoiseBandControl;
import noiseBandMonitor.NoiseBandSettings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.pamChart.LogarithmicAxis;
import pamViewFX.fxNodes.pamChart.PamLineChart;

/**
 * JavaFX implementation of the Bode plot graph from the Swing NoiseBandDialog
 * using the JavaFX Charts API ({@link PamLineChart}, {@link LogarithmicAxis}).
 * <p>
 * Each band filter response, decimator response and ANSI standard envelope is
 * rendered as a separate {@link Series} in a {@link PamLineChart}. This means
 * the plot picks up the application's CSS styling automatically and resizes
 * properly without manual painting.
 * <p>
 * Supports interactive band selection by clicking near a series on the chart,
 * and a gain-scale toggle on double-click.
 *
 * @author PAMGuard
 */
public class BodePlotPaneFX extends PamBorderPane {

	/* ---- Gain scale presets (dB) ---- */
	private static final double GAIN_MAX = 1;
	private static final double[] GAIN_TOGGLES = {-80, -10, -120};

	/**
	 * Maximum number of data points per series to keep the chart responsive.
	 * Band performance arrays can contain thousands of points; we thin them
	 * down to at most this many.
	 */
	private static final int MAX_POINTS_PER_SERIES = 1000;

	/**
	 * Maximum number of decimator frequency-response points.
	 */
	private static final int DECIMATOR_POINTS = 150;

	/* ---- Style classes applied to series lines ---- */
	private static final String BAND_STYLE      = "-fx-stroke: #3366cc; -fx-stroke-width: 1.5px;";
	private static final String BAND_SEL_STYLE  = "-fx-stroke: #3366cc; -fx-stroke-width: 3px;";
	private static final String DEC_STYLE       = "-fx-stroke: #cc3333; -fx-stroke-width: 1px;";
	private static final String DEC_SEL_STYLE   = "-fx-stroke: #cc3333; -fx-stroke-width: 3px;";
	private static final String[] ANSI_STYLES   = {
			"-fx-stroke: magenta;  -fx-stroke-width: 1px;",
			"-fx-stroke: cyan;     -fx-stroke-width: 1px;",
			"-fx-stroke: orange;   -fx-stroke-width: 1px;"
	};

	/* ---- Controls ---- */
	private CheckBox logFreqScale;
	private CheckBox showGrid;
	private CheckBox showDecimators;
	private CheckBox[] showStandard = new CheckBox[3];

	/* ---- Charts ---- */
	private PamLineChart<Number, Number> logChart;
	private PamLineChart<Number, Number> linChart;
	private LogarithmicAxis logXAxis;
	private NumberAxis linXAxis;
	private NumberAxis logYAxis;
	private NumberAxis linYAxis;
	private PamBorderPane chartHolder;

	/* ---- Data ---- */
	private NoiseBandControl noiseBandControl;
	private NoiseBandSettings workingSettings;
	private BandAnalyser bandAnalyser;
	private ArrayList<DecimatorMethod> decimationFilters;
	private ArrayList<FilterMethod> bandFilters;
	private int[] decimatorIndexes;
	private float sampleRate = 1;

	/* ---- Selection ---- */
	private int selectedBand = -1;
	private int selectedDecimator = -1;
	private int gainToggleState = 0;

	/** Tracks which series belong to which band/decimator. */
	private final ArrayList<Integer> bandSeriesIndices = new ArrayList<>();
	private final ArrayList<Integer> decSeriesIndices  = new ArrayList<>();

	/** Listener notified when the selected band changes. */
	private BandSelectionListener bandSelectionListener;

	/** Guard to prevent re-entrant rebuilds while syncing checkbox state. */
	private boolean updatingControls = false;

	@FunctionalInterface
	public interface BandSelectionListener {
		void bandSelected(int bandIndex);
	}

	/* ================================================================== */

	public BodePlotPaneFX(NoiseBandControl noiseBandControl) {
		this.noiseBandControl = noiseBandControl;
		setTop(createOptionsBar());
		chartHolder = new PamBorderPane();
		createCharts();
		setCenter(chartHolder);
	}

	/* ------------------------------------------------------------------ */
	/*  UI construction                                                    */
	/* ------------------------------------------------------------------ */

	private HBox createOptionsBar() {
		HBox bar = new HBox(10);
		bar.setPadding(new Insets(4));

		logFreqScale = new CheckBox("Log Scale");
		logFreqScale.setSelected(true);
		logFreqScale.setOnAction(e -> {
			if (!updatingControls) { swapChart(); rebuildChart(); }
		});

		showGrid = new CheckBox("Show Grid");
		showGrid.setSelected(true);
		showGrid.setOnAction(e -> {
			if (!updatingControls) updateGridVisibility();
		});

		showDecimators = new CheckBox("Show Decimators");
		showDecimators.setSelected(true);
		showDecimators.setOnAction(e -> {
			if (!updatingControls) rebuildChart();
		});

		bar.getChildren().addAll(logFreqScale, showGrid, showDecimators);

		Label ansiLabel = new Label("   ANSI standards:");
		bar.getChildren().add(ansiLabel);
		for (int i = 0; i < 3; i++) {
			showStandard[i] = new CheckBox("Class " + i);
			showStandard[i].setOnAction(e -> {
				if (!updatingControls) rebuildChart();
			});
			bar.getChildren().add(showStandard[i]);
		}
		return bar;
	}

	private void createCharts() {
		// Logarithmic frequency chart
		logXAxis = new LogarithmicAxis(10, 50000);
		logXAxis.setLabel("Frequency (Hz)");
		logYAxis = new NumberAxis(GAIN_TOGGLES[0], GAIN_MAX, 10);
		logYAxis.setLabel("Gain (dB)");
		logYAxis.setAutoRanging(false);
		logChart = new PamLineChart<>(logXAxis, logYAxis);
		logChart.setLegendVisible(false);
		logChart.setCreateSymbols(false);
		logChart.setAnimated(false);

		// Linear frequency chart
		linXAxis = new NumberAxis(0, 50000, 5000);
		linXAxis.setLabel("Frequency (Hz)");
		linXAxis.setAutoRanging(false);
		linYAxis = new NumberAxis(GAIN_TOGGLES[0], GAIN_MAX, 10);
		linYAxis.setLabel("Gain (dB)");
		linYAxis.setAutoRanging(false);
		linChart = new PamLineChart<>(linXAxis, linYAxis);
		linChart.setLegendVisible(false);
		linChart.setCreateSymbols(false);
		linChart.setAnimated(false);

		// install click handler on both charts
		logChart.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) { toggleGainScale(); }
			else { handleChartClick(logChart, e.getX(), e.getY()); }
		});
		linChart.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) { toggleGainScale(); }
			else { handleChartClick(linChart, e.getX(), e.getY()); }
		});

		// default to log
		chartHolder.setCenter(logChart);
		VBox.setVgrow(chartHolder, Priority.ALWAYS);
	}

	private void swapChart() {
		chartHolder.setCenter(logFreqScale.isSelected() ? logChart : linChart);
	}

	/* ------------------------------------------------------------------ */
	/*  Public API                                                         */
	/* ------------------------------------------------------------------ */

	/**
	 * Supply new analysis results and repaint.
	 */
	public void update(NoiseBandSettings settings, BandAnalyser analyser,
			ArrayList<DecimatorMethod> decimationFilters,
			ArrayList<FilterMethod> bandFilters, int[] decimatorIndexes,
			float sampleRate) {
		this.workingSettings = settings;
		this.bandAnalyser = analyser;
		this.decimationFilters = decimationFilters;
		this.bandFilters = bandFilters;
		this.decimatorIndexes = decimatorIndexes;
		this.sampleRate = sampleRate;

		// sync display-option checkboxes without triggering rebuild listeners
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

		swapChart();
		updateAxes();
		rebuildChart();
	}

	public void writeDisplayOptions(NoiseBandSettings settings) {
		settings.logFreqScale = logFreqScale.isSelected();
		settings.showGrid = showGrid.isSelected();
		settings.showDecimators = showDecimators.isSelected();
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
			if (decimatorIndexes != null && selectedBand >= 0 && selectedBand < decimatorIndexes.length) {
				selectedDecimator = decimatorIndexes[selectedBand];
			}
			applySelectionStyles();
		}
	}

	/* ------------------------------------------------------------------ */
	/*  Axis management                                                    */
	/* ------------------------------------------------------------------ */

	private void updateAxes() {
		double yMin = GAIN_TOGGLES[gainToggleState % GAIN_TOGGLES.length];
		double nyquist = sampleRate / 2.0;

		logYAxis.setLowerBound(yMin);
		logYAxis.setUpperBound(GAIN_MAX);
		logYAxis.setTickUnit(10);
		linYAxis.setLowerBound(yMin);
		linYAxis.setUpperBound(GAIN_MAX);
		linYAxis.setTickUnit(10);

		if (workingSettings != null && workingSettings.getMinFrequency() > 0) {
			double mf = workingSettings.getMinFrequency();
			double logLo = Math.pow(10., Math.floor(Math.log10(mf)));
			logXAxis.setLowerBound(logLo);
		} else {
			logXAxis.setLowerBound(10);
		}
		logXAxis.setUpperBound(nyquist);

		linXAxis.setLowerBound(0);
		linXAxis.setUpperBound(nyquist);
		double step = niceStep(nyquist, 8);
		linXAxis.setTickUnit(step);

		updateGridVisibility();
	}

	private void updateGridVisibility() {
		boolean show = showGrid.isSelected();
		logChart.setHorizontalGridLinesVisible(show);
		logChart.setVerticalGridLinesVisible(show);
		linChart.setHorizontalGridLinesVisible(show);
		linChart.setVerticalGridLinesVisible(show);
	}

	/* ------------------------------------------------------------------ */
	/*  Chart data construction                                            */
	/* ------------------------------------------------------------------ */

	/**
	 * Clear all series and rebuild from the current analyser data.
	 * <p>
	 * All series are collected into a list first and then set on the chart
	 * in a single {@code setAll()} call to avoid repeated layout passes.
	 */
	private void rebuildChart() {
		PamLineChart<Number, Number> chart = getActiveChart();
		bandSeriesIndices.clear();
		decSeriesIndices.clear();

		List<Series<Number, Number>> allSeries = new ArrayList<>();

		// 1) Band analysis curves
		if (bandAnalyser != null) {
			BandPerformance[] perfs = bandAnalyser.getBandPerformances();
			if (perfs != null) {
				for (int b = 0; b < perfs.length; b++) {
					Series<Number, Number> s = createBandSeries(perfs[b]);
					bandSeriesIndices.add(allSeries.size());
					allSeries.add(s);
				}
			}
		}

		// 2) Decimator responses
		if (showDecimators.isSelected() && decimationFilters != null) {
			ValueAxis<Number> xAxis = (ValueAxis<Number>) chart.getXAxis();
			for (int d = 0; d < decimationFilters.size(); d++) {
				Series<Number, Number> s = createDecimatorSeries(decimationFilters.get(d), xAxis);
				decSeriesIndices.add(allSeries.size());
				allSeries.add(s);
			}
		}

		// 3) ANSI standard overlays
		if (bandFilters != null && workingSettings != null) {
			double[] relFreq = ANSIStandard.getRelFreq(workingSettings.bandType);
			if (relFreq != null) {
				for (FilterMethod fm : bandFilters) {
					double centre = fm.getFilterParams().getCenterFreq();
					for (int cls = 0; cls < 3; cls++) {
						if (!showStandard[cls].isSelected()) continue;
						addAnsiSeries(allSeries, centre, relFreq, cls);
					}
				}
			}
		}

		// Single bulk update – avoids N layout passes
		chart.getData().setAll(allSeries);

		applySeriesStyles(chart);
		applySelectionStyles();
	}

	/* ---------- band series ---------- */

	/**
	 * Create an adaptively-thinned series for a single band performance.
	 * <p>
	 * The raw frequency/gain arrays can contain thousands of points.
	 * Uniform sub-sampling looks poor because the curved passband top
	 * needs many points while the steep roll-off and the flat stop-band
	 * floor are visually straight and need very few.
	 * <p>
	 * Strategy: walk through the array and decide per-point whether to
	 * keep it based on two criteria:
	 * <ol>
	 *   <li><b>Gain-dependent spacing</b> – near the passband (gain &gt; −6 dB)
	 *       we keep almost every point; in the transition band (−6 to −30 dB)
	 *       we thin moderately; in the stop band (&lt; −30 dB) we thin
	 *       aggressively.</li>
	 *   <li><b>Slope change</b> – any point where the gain curve changes
	 *       direction (local extremum or inflection) is always kept so that
	 *       peaks and shoulders are never lost.</li>
	 * </ol>
	 */
	private Series<Number, Number> createBandSeries(BandPerformance bp) {
		Series<Number, Number> s = new Series<>();
		double[] f    = bp.getFrequencyList();
		double[] gain = bp.getGainListdB();
		int len = f.length;
		if (len == 0) return s;

		// base step for the densest region (passband); other regions
		// are multiples of this
		int baseStep = Math.max(1, len / MAX_POINTS_PER_SERIES);

		ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();

		int sinceLastKept = 0;   // how many points skipped since last kept

		for (int i = 0; i < len; i++) {
			double g = gain[i];
			if (g < -200) continue; // invalid

			// choose step size based on gain level
			int requiredGap;
			if (g > -6) {
				// passband – keep at high density
				requiredGap = baseStep;
			} else if (g > -30) {
				// transition band – moderate thinning
				requiredGap = baseStep * 4;
			} else {
				// stop band – aggressive thinning
				requiredGap = baseStep * 12;
			}

			// always keep: first point, last point, and points at
			// slope-direction changes (local min/max)
			boolean forceKeep = (i == 0) || (i == len - 1);
			if (!forceKeep && i > 0 && i < len - 1
					&& gain[i - 1] > -200 && gain[i + 1] > -200) {
				double dPrev = g - gain[i - 1];
				double dNext = gain[i + 1] - g;
				// sign change in slope → keep the turning point
				if (dPrev * dNext < 0) {
					forceKeep = true;
				}
			}

			sinceLastKept++;
			if (forceKeep || sinceLastKept >= requiredGap) {
				data.add(new Data<>(f[i], g));
				sinceLastKept = 0;
			}
		}

		s.setData(data);
		return s;
	}

	/* ---------- decimator series ---------- */

	private Series<Number, Number> createDecimatorSeries(DecimatorMethod dm, ValueAxis<Number> xAxis) {
		Series<Number, Number> s = new Series<>();
		FilterMethod fm = dm.getFilterMethod();
		double sr = fm.getSampleRate();
		double fc = fm.getFilterGainConstant();

		double lo = xAxis.getLowerBound();
		double hi = Math.min(xAxis.getUpperBound(), sr / 2.0);
		if (lo <= 0) lo = 1;
		double logLo = Math.log10(lo);
		double logHi = Math.log10(hi);
		double logStep = (logHi - logLo) / DECIMATOR_POINTS;

		ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
		for (int i = 0; i <= DECIMATOR_POINTS; i++) {
			double freq = Math.pow(10., logLo + i * logStep);
			if (freq <= 0 || freq >= sr / 2) continue;
			double omega = freq / sr * Math.PI * 2;
			double g = fm.getFilterGain(omega) / fc;
			if (g <= 0) continue;
			double gdB = 20 * Math.log10(g);
			data.add(new Data<>(freq, gdB));
		}
		s.setData(data);
		return s;
	}

	/* ---------- ANSI standard series ---------- */

	private void addAnsiSeries(List<Series<Number, Number>> seriesList,
			double centreFreq, double[] relFreq, int cls) {
		double[] minAtt = ANSIStandard.getMinAttenuation(cls);
		double[] maxAtt = ANSIStandard.getMaxAttenuation(cls);
		if (minAtt == null || maxAtt == null) return;

		seriesList.add(makeAnsiCurve(centreFreq, relFreq, minAtt, true));
		seriesList.add(makeAnsiCurve(centreFreq, relFreq, minAtt, false));
		seriesList.add(makeAnsiCurve(centreFreq, relFreq, maxAtt, true));
		seriesList.add(makeAnsiCurve(centreFreq, relFreq, maxAtt, false));
	}

	private Series<Number, Number> makeAnsiCurve(double centreFreq,
			double[] relFreq, double[] atten, boolean upper) {
		Series<Number, Number> s = new Series<>();
		ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
		for (int i = 0; i < relFreq.length; i++) {
			double f = upper ? centreFreq * relFreq[i] : centreFreq / relFreq[i];
			data.add(new Data<>(f, -atten[i]));
		}
		s.setData(data);
		return s;
	}

	/* ------------------------------------------------------------------ */
	/*  Series styling                                                     */
	/* ------------------------------------------------------------------ */

	/**
	 * Apply default inline styles to every series after they have been added.
	 * Must be called after chart.getData() is populated since Nodes are created lazily.
	 */
	private void applySeriesStyles(PamLineChart<Number, Number> chart) {
		ObservableList<Series<Number, Number>> all = chart.getData();

		for (int i = 0; i < all.size(); i++) {
			Node line = all.get(i).getNode();
			if (line == null) continue;
			// remove default coloured class so our inline style takes effect
			line.getStyleClass().removeIf(c -> c.startsWith("default-color"));

			if (bandSeriesIndices.contains(i)) {
				line.setStyle(BAND_STYLE);
			} else if (decSeriesIndices.contains(i)) {
				line.setStyle(DEC_STYLE);
			} else {
				// ANSI series – work out which class
				int ansiIdx = getAnsiClassForSeries(i);
				line.setStyle(ANSI_STYLES[ansiIdx % ANSI_STYLES.length]);
			}
		}
	}

	/**
	 * Determine the ANSI class index (0, 1, 2) for a non-band, non-decimator series.
	 */
	private int getAnsiClassForSeries(int seriesIdx) {
		if (bandFilters == null || workingSettings == null) return 0;
		int nBands = bandFilters.size();
		int enabledClasses = 0;
		for (int c = 0; c < 3; c++) if (showStandard[c].isSelected()) enabledClasses++;
		if (enabledClasses == 0 || nBands == 0) return 0;

		// ANSI series start after band + decimator series
		int ansiOffset = seriesIdx - bandSeriesIndices.size() - decSeriesIndices.size();
		if (ansiOffset < 0) return 0;
		// 4 series per band per class
		int perClass = nBands * 4;
		int clsOrder = ansiOffset / perClass;
		// map back to actual class index
		int actual = 0, count = 0;
		for (int c = 0; c < 3; c++) {
			if (showStandard[c].isSelected()) {
				if (count == clsOrder) { actual = c; break; }
				count++;
			}
		}
		return actual;
	}

	/**
	 * Highlight / un-highlight the selected band and its decimator.
	 */
	private void applySelectionStyles() {
		PamLineChart<Number, Number> chart = getActiveChart();
		ObservableList<Series<Number, Number>> all = chart.getData();

		for (int i = 0; i < bandSeriesIndices.size(); i++) {
			int sIdx = bandSeriesIndices.get(i);
			if (sIdx >= all.size()) continue;
			Node line = all.get(sIdx).getNode();
			if (line == null) continue;
			line.setStyle(i == selectedBand ? BAND_SEL_STYLE : BAND_STYLE);
		}

		for (int i = 0; i < decSeriesIndices.size(); i++) {
			int sIdx = decSeriesIndices.get(i);
			if (sIdx >= all.size()) continue;
			Node line = all.get(sIdx).getNode();
			if (line == null) continue;
			line.setStyle(i == selectedDecimator ? DEC_SEL_STYLE : DEC_STYLE);
		}
	}

	/* ------------------------------------------------------------------ */
	/*  Mouse interaction                                                  */
	/* ------------------------------------------------------------------ */

	private void handleChartClick(PamLineChart<Number, Number> chart, double sceneX, double sceneY) {
		if (bandAnalyser == null) { setSelectedBand(-1); return; }
		BandPerformance[] perfs = bandAnalyser.getBandPerformances();
		if (perfs == null) { setSelectedBand(-1); return; }

		ValueAxis<Number> xAxis = (ValueAxis<Number>) chart.getXAxis();
		ValueAxis<Number> yAxis = (ValueAxis<Number>) chart.getYAxis();

		int closestBand = -1;
		double closestDist = Double.MAX_VALUE;

		// look up the plot area once, outside the loops
		Node plotArea = chart.lookup(".chart-plot-background");

		for (int b = 0; b < perfs.length; b++) {
			double[] f = perfs[b].getFrequencyList();
			double[] g = perfs[b].getGainListdB();
			// sample sparsely for hit-testing – every 20th point is enough
			for (int i = 0; i < f.length; i += 20) {
				double px = xAxis.getDisplayPosition(f[i]);
				double py = yAxis.getDisplayPosition(g[i]);
				if (plotArea != null) {
					javafx.geometry.Point2D pt = plotArea.localToParent(px, py);
					px = pt.getX();
					py = pt.getY();
				}
				double dx = px - sceneX;
				double dy = py - sceneY;
				double d = dx * dx + dy * dy;
				if (d < closestDist) {
					closestDist = d;
					closestBand = b;
				}
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
		updateAxes();
		rebuildChart();
	}

	/* ------------------------------------------------------------------ */
	/*  Util                                                               */
	/* ------------------------------------------------------------------ */

	private PamLineChart<Number, Number> getActiveChart() {
		return logFreqScale.isSelected() ? logChart : linChart;
	}

	private static double niceStep(double range, int targetTicks) {
		if (range <= 0) return 1;
		double rough = range / targetTicks;
		double mag   = Math.pow(10., Math.floor(Math.log10(rough)));
		double norm  = rough / mag;
		double nice;
		if (norm < 1.5)      nice = 1;
		else if (norm < 3.5) nice = 2;
		else if (norm < 7.5) nice = 5;
		else                 nice = 10;
		return nice * mag;
	}
}
