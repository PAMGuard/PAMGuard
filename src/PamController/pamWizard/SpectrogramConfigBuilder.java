package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Timer;
import javafx.application.Platform;

import Acquisition.AcquisitionControl;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamModel.PamModuleInfo;
import PamguardMVC.PamDataBlock;
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
import dataPlotsFX.TDControlAWT;
import dataPlotsFX.TDDisplayController;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

/**
 * Builds a "view a spectrogram of sound files" PAMGuard configuration: Sound
 * Acquisition (pointed at the dropped files) + FFT + an FX Time Display showing a
 * spectrogram, with a Spectrogram Annotation overlay for manual annotation
 * (viewer only). Works in both GUIs - in the FX GUI the time display is its own
 * module ({@link TDDisplayController}); in the Swing GUI a User Display module is
 * added and the FX time display is created within it.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramConfigBuilder {

	public static final String ACQUISITION_CLASS = "Acquisition.AcquisitionControl";
	public static final String FFT_CLASS = "fftManager.PamFFTControl";
	public static final String TD_DISPLAY_CLASS = "dataPlotsFX.TDDisplayController";
	public static final String USER_DISPLAY_CLASS = "userDisplay.UserDisplayControl";
	public static final String SPECTROGRAM_ANNOTATION_CLASS = "annotationMark.spectrogram.SpectrogramAnnotationModule";

	/** Visible time range of the spectrogram display, in milliseconds. */
	public static final long VISIBLE_MILLIS = 20_000L;

	/** Viewer loaded data window, in milliseconds. */
	public static final long LOADED_MILLIS = 60L * 60L * 1000L; // 1 hour

	private AcquisitionControl acquisitionControl;
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

		// 1. Add acquisition + FFT, and the (GUI-dependent) time display.
		acquisitionControl = (AcquisitionControl) addModule(ACQUISITION_CLASS, "Sound Acquisition");
		fftControl = addModule(FFT_CLASS, "FFT Engine");
		Supplier<TDDisplayFX> displaySupplier = addSpectrogramDisplay();
		if (viewer) {
			// Spectrogram Annotation module provides manual annotation marks over the
			// spectrogram.
			annotationControl = addModule(SPECTROGRAM_ANNOTATION_CLASS, "Spectrogram Annotation");
		}

		// 2. Point acquisition at the dropped sound files (and enable the offline map).
		configureAcquisition(files, viewer);

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
		whenReady(displaySupplier, isViewer, display -> {
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
	 * Add the FX time display in a GUI-appropriate way and return a supplier that
	 * resolves to its {@link TDDisplayFX} (which may not exist yet, as it is created
	 * asynchronously on the JavaFX thread).
	 */
	private Supplier<TDDisplayFX> addSpectrogramDisplay() {
		if (PamGUIManager.isFX()) {
			// FX GUI: the time display is its own controlled unit.
			TDDisplayController controller = (TDDisplayController) addModule(TD_DISPLAY_CLASS, "Spectrogram");
			return () -> (controller == null) ? null : controller.getMainDisplay();
		}
		// Swing GUI: add a User Display module then create an FX time display within it.
		UserDisplayControl userDisplay = (UserDisplayControl) addModule(USER_DISPLAY_CLASS, "Spectrogram");
		if (userDisplay == null) {
			return () -> null;
		}
		UserDisplayComponent component = userDisplay.addUserDisplay(TDControlAWT.class);
		if (component instanceof TDControlAWT) {
			TDControlAWT tdControl = (TDControlAWT) component;
			return tdControl::getMainDisplay;
		}
		return () -> null;
	}

	/**
	 * Wait (by polling) until the asynchronously-created {@link TDDisplayFX} is
	 * available and - in viewer mode - the sound-file datamap has been built, then run
	 * the given action on the JavaFX thread.
	 */
	private void whenReady(Supplier<TDDisplayFX> supplier, boolean requireDataMap, Consumer<TDDisplayFX> action) {
		final int[] tries = { 0 };
		Timer timer = new Timer(100, null);
		timer.addActionListener(e -> {
			TDDisplayFX display = supplier.get();
			boolean ready = display != null && (!requireDataMap || isDataMapReady());
			if (ready) {
				timer.stop();
				runOnFx(() -> action.accept(display));
			}
			else if (++tries[0] > 600) { // ~60 seconds (datamap scan can be slow)
				timer.stop();
				System.err.println("SpectrogramConfigBuilder: timed out waiting for display / datamap to initialise");
			}
		});
		timer.setRepeats(true);
		timer.start();
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
	 * Run a task on the JavaFX application thread.
	 */
	private void runOnFx(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		}
		else {
			Platform.runLater(runnable);
		}
	}

	/**
	 * Add a module by class name and return the created controlled unit.
	 */
	private PamControlledUnit addModule(String className, String moduleName) {
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(className);
		if (moduleInfo == null) {
			System.err.println("SpectrogramConfigBuilder: could not find module " + className);
			return null;
		}
		return PamController.getInstance().addModule(moduleInfo, moduleName);
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
		PamDataBlock annotationBlock = getOutputDataBlock(annotationControl);
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

	/**
	 * Get the first output data block of a controlled unit, or null.
	 */
	private PamDataBlock getOutputDataBlock(PamControlledUnit unit) {
		if (unit == null || unit.getNumPamProcesses() == 0) {
			return null;
		}
		if (unit.getPamProcess(0).getNumOutputDataBlocks() == 0) {
			return null;
		}
		return unit.getPamProcess(0).getOutputDataBlock(0);
	}
}
