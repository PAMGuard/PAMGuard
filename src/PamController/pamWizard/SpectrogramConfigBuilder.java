package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import Acquisition.AcquisitionControl;
import PamController.pamWizard.configurations.DefaultConfigLoader;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import fftManager.FFTParameters;
import fftManager.PamFFTControl;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import dataMap.DataMapControl;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewerScrollerManager;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import dataPlotsFX.TDDisplayController;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * Builds a "view a spectrogram of sound files" PAMGuard configuration: Sound
 * Acquisition (pointed at the dropped files) + optional Decimator + FFT + an FX
 * Time Display showing a spectrogram, with a Spectrogram Annotation overlay for manual annotation
 * (viewer only). Works in both GUIs - in the FX GUI the time display is its own
 * module ({@link TDDisplayController}); in the Swing GUI a User Display module is
 * added and the FX time display is created within it.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramConfigBuilder {

	public static final String ACQUISITION_CLASS = "Acquisition.AcquisitionControl";
	public static final String FFT_CLASS = "fftManager.PamFFTControl";
	public static final String SPECTROGRAM_ANNOTATION_CLASS = "annotationMark.spectrogram.SpectrogramAnnotationModule";

	/** Unit name of the decimator added when the user asks for decimation. */
	public static final String DECIMATOR_NAME = "Decimator";

	/** Visible time range of the spectrogram display, in milliseconds. */
	public static final long VISIBLE_MILLIS = 20_000L;

	/** Viewer loaded data window, in milliseconds. */
	public static final long LOADED_MILLIS = 60L * 60L * 1000L; // 1 hour

	private AcquisitionControl acquisitionControl;
	private DecimatorControl decimatorControl;
	private PamControlledUnit fftControl;
	private PamControlledUnit annotationControl;

	/**
	 * Build the spectrogram configuration.
	 *
	 * @param files  the dropped/scanned files.
	 * @param viewer true for the viewer-mode build (adds the Spectrogram Annotation
	 *               module and the 1 h / auto-load viewer setup), false for a live
	 *               real-time build.
	 */
	public void build(PamFileImport files, boolean viewer) {
		build(files, viewer, null);
	}

	/**
	 * Build the spectrogram configuration.
	 *
	 * @param files   the dropped/scanned files.
	 * @param viewer  true for the viewer-mode build (adds the Spectrogram Annotation
	 *                module and the 1 h / auto-load viewer setup), false for a live
	 *                real-time build.
	 * @param options decimation and FFT choices from the wizard. Null uses the FFT
	 *                module defaults, with no decimation.
	 */
	public void build(PamFileImport files, boolean viewer, SpectrogramOptions options) {
		if (options == null) {
			options = new SpectrogramOptions();
		}

		// 1. Add acquisition (+ decimator) + FFT, and the (GUI-dependent) time display.
		acquisitionControl = (AcquisitionControl) PamAutoConfigUtils.addModule(ACQUISITION_CLASS, "Sound Acquisition");
		if (options.isDecimate()) {
			decimatorControl = (DecimatorControl) PamAutoConfigUtils.addModule(DefaultConfigLoader.DECIMATOR_CLASS,
					DECIMATOR_NAME);
		}
		fftControl = PamAutoConfigUtils.addModule(FFT_CLASS, "FFT Engine");
		Supplier<TDDisplayFX> displaySupplier = PamAutoConfigUtils.addTimeDisplay("Spectrogram");
		if (viewer) {
			// Spectrogram Annotation module provides manual annotation marks over the
			// spectrogram.
			annotationControl = PamAutoConfigUtils.addModule(SPECTROGRAM_ANNOTATION_CLASS, "Spectrogram Annotation");
		}

		// 2. Point acquisition at the dropped sound files (and enable the offline map).
		configureAcquisition(files, viewer);

		// 2b. Chain acquisition -> (decimator ->) FFT with the chosen FFT length / hop.
		//     Must follow configureAcquisition, which sets the sample rate and channels.
		configureDecimator(options);
		configureFFT(options);

		// 3. Rebuild the data model so connections form and displays appear.
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);

		// 4. Viewer-only: build the sound-file datamap (asynchronous - independent of
		//    the display).
		if (viewer) {
			acquisitionControl.createOfflineDataMap(PamController.getMainFrame());
		}

		// 5. The FX time display is created asynchronously on the JavaFX thread (it lives
		//    in a JFXPanel in the Swing GUI) and, in viewer mode, the sound-file datamap
		//    is built asynchronously too. Defer adding the spectrogram and setting the
		//    scroller (which triggers the data load) until BOTH are ready - otherwise the
		//    scroller load fires before there is any data, and the spectrogram stays
		//    blank until PAMGuard is restarted.
		final boolean isViewer = viewer;
		PamAutoConfigUtils.whenReady(displaySupplier, isViewer ? this::isDataMapReady : null, display -> {
			configureDisplay(display);
			if (isViewer) {
				// Force the scroll system to re-initialise for the newly-added modules
				// (it normally only initialises once at startup, which is why the
				// spectrogram stayed blank until a restart), then apply our 1 h / 20 s
				// window.
				AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
				if (scrollManager instanceof ViewerScrollerManager) {
					((ViewerScrollerManager) scrollManager).reinitialiseScrollers();
				}
				configureScroller(files, display);
			}
		});
	}

	/**
	 * @return the Sound Acquisition module added by {@link #build}, or null before then.
	 */
	public AcquisitionControl getAcquisitionControl() {
		return acquisitionControl;
	}

	/**
	 * @return the annotation module added by a viewer {@link #build}, or null.
	 */
	public PamControlledUnit getAnnotationControl() {
		return annotationControl;
	}

	/**
	 * @return true once the sound-file datamap has been built (has a valid first time).
	 */
	private boolean isDataMapReady() {
		DataMapControl dmc = DataMapControl.getDataMapControl();
		if (dmc == null) {
			return false;
		}
		long firstTime = dmc.getFirstTime();
		return firstTime > 0 && firstTime != Long.MAX_VALUE;
	}

	/**
	 * Set the acquisition module to read the dropped files via its folder input
	 * system, using the sample rate / channel count from the scanned audio. In viewer
	 * mode also enable and point the offline file server so the data map can be built.
	 */
	private void configureAcquisition(PamFileImport files, boolean viewer) {
		AcquisitionConfigurer.configure(acquisitionControl, files, viewer);
	}

	/**
	 * Point the decimator (if there is one) at the acquisition raw data, decimating
	 * every channel to the chosen sample rate.
	 */
	private void configureDecimator(SpectrogramOptions options) {
		if (decimatorControl == null) {
			if (options.isDecimate()) {
				System.err.println("SpectrogramConfigBuilder: decimator module could not be added - FFT will use full rate data");
			}
			return;
		}
		PamRawDataBlock acquisitionData = acquisitionControl.getAcquisitionProcess().getRawDataBlock();
		DecimatorParams params = new DecimatorParams(options.getDecimatedSampleRate(),
				DefaultConfigLoader.DECIMATOR_FILTER_ORDER);
		params.rawDataSource = acquisitionData.getLongDataName();
		params.channelMap = acquisitionData.getChannelMap();
		decimatorControl.setDecimatorParams(params);
		decimatorControl.setupControlledUnit();
	}

	/**
	 * Set the FFT length and hop, and its source - the decimator output if there is
	 * a decimator, otherwise the acquisition raw data.
	 */
	private void configureFFT(SpectrogramOptions options) {
		if (!(fftControl instanceof PamFFTControl)) {
			return;
		}
		PamFFTControl pamFFTControl = (PamFFTControl) fftControl;
		PamRawDataBlock sourceData;
		if (decimatorControl != null) {
			sourceData = decimatorControl.getDecimatorProcess().getOutputDataBlock();
		}
		else {
			sourceData = acquisitionControl.getAcquisitionProcess().getRawDataBlock();
		}
		FFTParameters params = pamFFTControl.getFftParameters().clone();
		params.fftLength = options.getFftLength();
		params.fftHop = options.getFftHop();
		params.dataSourceName = sourceData.getLongDataName();
		/*
		 * The Swing FFT dialog selects its source by index, not by name, so set the
		 * index too - otherwise the dialog shows (and on OK reverts to) the first raw
		 * data block, which is Sound Acquisition.
		 */
		int sourceIndex = PamController.getInstance().getDataBlocks(RawDataUnit.class, true).indexOf(sourceData);
		if (sourceIndex >= 0) {
			params.dataSource = sourceIndex;
		}
		params.channelMap = sourceData.getChannelMap();
		pamFFTControl.setFFTParameters(params);
		pamFFTControl.setupControlledUnit();
	}

	/**
	 * Add the spectrogram (FFT data block) and the annotation overlay to the FX time
	 * display, and set the 20 s visible range.
	 */
	private void configureDisplay(TDDisplayFX display) {
		if (display == null) {
			System.err.println("SpectrogramConfigBuilder: TD display not available - spectrogram not added");
			return;
		}

		// spectrogram from the FFT output data block - this adds an FFTPlotInfo to a
		// TDGraphFX on the display.
		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance().getFFTDataBlocks();
		if (fftBlocks != null && !fftBlocks.isEmpty()) {
			display.addDataBlock(fftBlocks.get(0), null);
		}
		else {
			System.err.println("SpectrogramConfigBuilder: no FFT data block found for the spectrogram");
		}

		// annotation overlay (spectrogram annotation) on the same graph as the
		// spectrogram, plus wiring so marks made on the display reach the annotation module.
		PamDataBlock annotationBlock = PamAutoConfigUtils.getOutputDataBlock(annotationControl);
		if (annotationBlock != null && !display.getTDGraphs().isEmpty()) {
			display.addDataBlock(annotationBlock, display.getTDGraphs().get(0));
		}
		linkAnnotationsToDisplay(display);

		// set the visible time range.
		if (display.getTimeScroller() != null) {
			display.getTimeScroller().setVisibleMillis(VISIBLE_MILLIS);
		}
	}

	/**
	 * Register the time display's overlay markers with the Spectrogram Annotation
	 * module's mark observer, so that marks drawn on the spectrogram are passed to
	 * the annotation module (which turns them into annotations).
	 * <p>
	 * The mark-relationship defaults to <i>off</i> for every marker/observer pair,
	 * and each graph's marker is normally only subscribed once - when the graph is
	 * created (before the annotation module existed). We therefore turn the
	 * relationship on for each of the display's graph markers and re-subscribe.
	 */
	private void linkAnnotationsToDisplay(TDDisplayFX display) {
		if (annotationControl == null || display.getTDGraphs().isEmpty()) {
			return;
		}
		OverlayMarkObserver observer = findMarkObserver(annotationControl.getUnitName());
		if (observer == null) {
			System.err.println("SpectrogramConfigBuilder: annotation mark observer not found - marks will not annotate");
			return;
		}
		MarkRelationships markRelationships = MarkRelationships.getInstance();
		for (TDGraphFX graph : display.getTDGraphs()) {
			OverlayMarker marker = findMarker(graph.getUniqueName());
			if (marker == null) {
				continue;
			}
			markRelationships.setRelationship(marker, observer, true);
			markRelationships.subscribeObservers(marker);
		}
	}

	/**
	 * Find the mark observer with the given name, or null.
	 */
	private OverlayMarkObserver findMarkObserver(String observerName) {
		for (OverlayMarkObserver observer : OverlayMarkObservers.singleInstance().getMarkObservers()) {
			if (observerName.equals(observer.getObserverName())) {
				return observer;
			}
		}
		return null;
	}

	/**
	 * Find the overlay marker (mark provider) with the given name, or null.
	 */
	private OverlayMarker findMarker(String markerName) {
		for (OverlayMarker marker : OverlayMarkProviders.singleInstance().getMarkProviders()) {
			if (markerName.equals(marker.getMarkerName())) {
				return marker;
			}
		}
		return null;
	}

	/**
	 * Viewer-only: set the scroll bar to a 1 hour loaded window starting at the first
	 * file, with a 20 s visible range, positioned at the start. Runs on the JavaFX
	 * thread once the display exists.
	 */
	private void configureScroller(PamFileImport files, TDDisplayFX display) {
		if (display == null || display.getTimeScroller() == null) {
			return;
		}
		// prefer the authoritative datamap first time; fall back to the wav-scan time.
		long start = Long.MAX_VALUE;
		DataMapControl dmc = DataMapControl.getDataMapControl();
		if (dmc != null) {
			start = dmc.getFirstTime();
		}
		if (start <= 0 || start == Long.MAX_VALUE) {
			start = getFirstFileStart(files);
		}
		// Setting the range with notify=true triggers the scroll manager to load data
		// for the new window, which makes the spectrogram order and render its data.
		display.getTimeScroller().setVisibleMillis(VISIBLE_MILLIS);
		display.getTimeScroller().setRangeMillis(start, start + LOADED_MILLIS, true);
		display.getTimeScroller().setValueMillis(start);
	}

	/**
	 * Start time of the first scanned sound file, or 0 if unknown.
	 */
	private long getFirstFileStart(PamFileImport files) {
		FileListData<WavFileType> soundFiles = files.getSoundFiles();
		if (soundFiles == null || soundFiles.getFileCount() == 0) {
			return 0;
		}
		soundFiles.sortFileList();
		return soundFiles.getListCopy().get(0).getStartMilliseconds();
	}
}
