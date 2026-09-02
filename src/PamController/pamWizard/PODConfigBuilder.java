package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamUtils.FileFunctions;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.PeakFreqModifier;
import PamView.symbol.modifier.PeakFreqSymbolOptions;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;
import cpod.CPODControl2;
import dataMap.DataMapControl;
import dataMap.OfflineDataMap;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import javafx.concurrent.Task;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewerScrollerManager;

/**
 * Builds a "view CPOD/FPOD detections" PAMGuard configuration: Binary Storage +
 * the CPOD Detector Import module + an FX Time Display showing the imported
 * clicks coloured by their peak frequency. The dropped CP1/CP3/FP1/FP3 files are
 * imported (converted into binary files) straight away, into the folder the user
 * chose on the wizard's storage page.
 * <p>
 * Works in both GUIs - in the FX GUI the time display is its own module; in the
 * Swing GUI a User Display module is added and the FX time display created within
 * it (see {@link PamAutoConfigUtils#addTimeDisplay}).
 *
 * @author Jamie Macaulay
 */
public class PODConfigBuilder {

	public static final String BINARY_STORE_CLASS = "binaryFileStorage.BinaryStore";
	public static final String CPOD_CLASS = "cpod.CPODControl2";

	/** Visible time range of the display, in milliseconds. */
	public static final long VISIBLE_MILLIS = 30L * 60L * 1000L; // 30 minutes

	/** Viewer loaded data window, in milliseconds. */
	public static final long LOADED_MILLIS = 6L * 60L * 60L * 1000L; // 6 hours

	/**
	 * Number of 100 ms polls to wait for the data map to be built after the import.
	 * Mapping a long POD deployment can take a good while, so this is generous - it
	 * is only a backstop to stop the poll running for ever.
	 */
	private static final int DATA_MAP_MAX_TRIES = 6000; // ~10 minutes

	/**
	 * Frequency range used to colour the clicks. POD click detectors work between
	 * about 20 and 160 kHz, which covers dolphin clicks at the bottom end and NBHF
	 * (e.g. harbour porpoise) clicks at the top, so this range spreads the colour
	 * scale over the frequencies that are actually present.
	 */
	public static final double[] PEAK_FREQ_LIMITS = new double[] { 20000., 160000. };

	private CPODControl2 cpodControl;

	/**
	 * Build the POD viewer configuration and start importing the dropped files.
	 * Must be called on the Swing event thread (module management runs through the
	 * Swing-based {@link PamController}).
	 *
	 * @param files        the dropped/scanned files.
	 * @param applyContext the storage locations the user chose in the wizard. May be
	 *                     null, in which case a binary store folder is picked
	 *                     alongside the POD files.
	 */
	public void build(PamFileImport files, ConfigApplyContext applyContext) {

		List<File> podFiles = PODFileScanner.getPODFiles(files);
		if (podFiles.isEmpty()) {
			System.err.println("PODConfigBuilder: no CPOD or FPOD files found - nothing to import");
			return;
		}

		// 1. The importer converts POD files into PAMGuard binary files, so a binary
		//    store has to exist (and have a folder to write into) before it runs.
		if (!addBinaryStore(podFiles, applyContext)) {
			return;
		}

		// 2. The CPOD module - it handles both CPOD and FPOD data.
		cpodControl = (CPODControl2) PamAutoConfigUtils.addModule(CPOD_CLASS, "POD Import");
		if (cpodControl == null) {
			return;
		}

		// 3. The time display to view the clicks on.
		Supplier<TDDisplayFX> displaySupplier = PamAutoConfigUtils.addTimeDisplay("POD Clicks");

		// 4. Rebuild the data model so connections form and displays appear.
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);

		// 5. The FX time display is created asynchronously on the JavaFX thread, so add
		//    the CPOD data to it once it exists. This is independent of the import - the
		//    display is set up while the files are still converting.
		PamAutoConfigUtils.whenReady(displaySupplier, null, display -> configureDisplay(display));

		// 6. Import the files. The scroll bar can only be positioned once there is data
		//    and a data map, so that is done when the import finishes.
		importPODFiles(podFiles, displaySupplier);
	}

	/**
	 * Add a binary store (if there isn't one already) and point it at the folder the
	 * user chose in the wizard.
	 *
	 * @param podFiles     the POD files being imported.
	 * @param applyContext the storage locations chosen in the wizard, may be null.
	 * @return true if a binary store is available.
	 */
	private boolean addBinaryStore(List<File> podFiles, ConfigApplyContext applyContext) {
		BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
		if (binaryStore == null) {
			binaryStore = (BinaryStore) PamAutoConfigUtils.addModule(BINARY_STORE_CLASS, BinaryStore.defUnitName);
		}
		if (binaryStore == null) {
			System.err.println("PODConfigBuilder: could not create a binary store - POD data cannot be imported");
			return false;
		}

		BinaryStoreSettings settings = binaryStore.getBinaryStoreSettings();
		String storeFolder = getBinaryFolder(podFiles, applyContext);
		if (storeFolder != null && FileFunctions.createNonIndexedFolder(storeFolder) != null) {
			settings.setStoreLocation(storeFolder);
		}
		else {
			/*
			 * The chosen folder could not be created - a mistyped path, or read only
			 * media. Say so, and carry on with the store's own default rather than
			 * abandoning the import.
			 */
			String message = "The binary storage folder " + storeFolder + " could not be created, so "
					+ settings.getStoreLocation() + " will be used instead.";
			if (applyContext != null) {
				applyContext.addWarning(message);
			}
			else {
				System.out.println("PODConfigBuilder: " + message);
			}
			FileFunctions.createNonIndexedFolder(settings.getStoreLocation());
		}
		return true;
	}

	/**
	 * The folder to write binary data into. Normally this is whatever the user chose
	 * on the wizard's storage page; if the builder is used without a wizard, a
	 * PAMBinary folder is put alongside the POD files so that the converted data sit
	 * with the data they came from.
	 *
	 * @param podFiles     the POD files being imported.
	 * @param applyContext the storage locations chosen in the wizard, may be null.
	 * @return the binary store folder, or null if one could not be worked out.
	 */
	private String getBinaryFolder(List<File> podFiles, ConfigApplyContext applyContext) {
		if (applyContext != null && applyContext.getBinaryFolder() != null) {
			return applyContext.getBinaryFolder().getAbsolutePath();
		}
		for (File podFile : podFiles) {
			File parent = podFile.getParentFile();
			if (parent != null) {
				return parent.getAbsolutePath() + File.separator + ConfigApplyContext.BINARY_FOLDER_NAME;
			}
		}
		return null;
	}

	/**
	 * Start the import of the POD files and, when it has finished, build the data
	 * map and position the display on the imported data.
	 *
	 * @param podFiles        the files to import.
	 * @param displaySupplier supplier of the time display.
	 */
	private void importPODFiles(List<File> podFiles, Supplier<TDDisplayFX> displaySupplier) {
		/*
		 * Note that the importer empties the list it is given as it pairs up CP1/CP3
		 * (and FP1/FP3) files, so it must be given a list of its own.
		 */
		final Task<Integer> task = cpodControl.importPODData(new ArrayList<File>(podFiles));
		if (task == null) {
			return;
		}

		/*
		 * Task handlers have to be set before the task is started, and both the
		 * handlers and the progress display belong on the JavaFX thread.
		 */
		PamAutoConfigUtils.runOnFx(() -> {
			PODImportProgress.showProgress(task);
			// a cancelled or failed import may still have written some data, so map
			// whatever is there in all three cases.
			task.setOnSucceeded(e -> importComplete(displaySupplier));
			task.setOnCancelled(e -> importComplete(displaySupplier));
			task.setOnFailed(e -> importComplete(displaySupplier));
			cpodControl.getCpodImporter().runTasks(task);
		});
	}

	/**
	 * Called when the import has finished: build the data map from the newly written
	 * binary files, then position the display on the start of the data.
	 */
	private void importComplete(Supplier<TDDisplayFX> displaySupplier) {
		// data map creation runs through Swing, and is itself asynchronous.
		SwingUtilities.invokeLater(() -> {
			PamController.getInstance().createDataMap();
			PamController.getInstance().updateDataMap();
		});

		PamAutoConfigUtils.whenReady(displaySupplier, this::isDataMapReady, display -> {
			/*
			 * Force the scroll system to re-initialise for the newly-added modules - it
			 * normally only initialises once at start up - then apply the loaded / visible
			 * window.
			 */
			AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
			if (scrollManager instanceof ViewerScrollerManager) {
				((ViewerScrollerManager) scrollManager).reinitialiseScrollers();
			}
			configureScroller(display);
		}, DATA_MAP_MAX_TRIES);
	}

	/**
	 * @return true once the imported POD data have been mapped.
	 */
	private boolean isDataMapReady() {
		return getDataStart() > 0;
	}

	/**
	 * The time of the first imported POD detection, taken from the data map. Falls
	 * back on the overall data map extent.
	 *
	 * @return the start time in milliseconds, or 0 if not yet known.
	 */
	private long getDataStart() {
		OfflineDataMap dataMap = cpodControl.getCP1DataBlock().getPrimaryDataMap();
		if (dataMap != null && dataMap.getDataCount() > 0) {
			long firstTime = dataMap.getFirstDataTime();
			if (firstTime > 0 && firstTime != Long.MAX_VALUE) {
				return firstTime;
			}
		}
		DataMapControl dataMapControl = DataMapControl.getDataMapControl();
		if (dataMapControl != null) {
			long firstTime = dataMapControl.getFirstTime();
			if (firstTime > 0 && firstTime != Long.MAX_VALUE) {
				return firstTime;
			}
		}
		return 0;
	}

	/**
	 * Add the CPOD detections to the time display and colour them by peak frequency.
	 * Runs on the JavaFX thread.
	 */
	private void configureDisplay(TDDisplayFX display) {
		if (display == null) {
			System.err.println("PODConfigBuilder: TD display not available - POD clicks not added");
			return;
		}

		PamDataBlock cpodDataBlock = cpodControl.getCP1DataBlock();
		display.addDataBlock(cpodDataBlock, null);

		colourByPeakFrequency(cpodDataBlock, findGraph(display, cpodDataBlock));

		if (display.getTimeScroller() != null) {
			display.getTimeScroller().setVisibleMillis(VISIBLE_MILLIS);
		}
	}

	/**
	 * Find the graph on the display which is showing a data block.
	 *
	 * @param display   the time display.
	 * @param dataBlock the data block.
	 * @return the graph, or null if the data block isn't on the display.
	 */
	private TDGraphFX findGraph(TDDisplayFX display, PamDataBlock dataBlock) {
		for (TDGraphFX graph : display.getTDGraphs()) {
			for (TDDataInfoFX dataInfo : graph.getDataList()) {
				if (dataInfo.getDataBlock() == dataBlock) {
					return graph;
				}
			}
		}
		return null;
	}

	/**
	 * Set the symbols for a data block on one graph to be coloured by peak
	 * frequency. Every other symbol modifier is switched off so that the frequency
	 * colour is what's actually seen (the user can of course change this from the
	 * display's symbol options).
	 *
	 * @param dataBlock the data block being plotted.
	 * @param graph     the graph it is plotted on.
	 */
	private void colourByPeakFrequency(PamDataBlock dataBlock, TDGraphFX graph) {
		if (graph == null) {
			return;
		}
		PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
		if (symbolManager == null) {
			return;
		}
		/*
		 * The symbol chooser is held by the symbol manager and keyed on the display
		 * name, so this is the same chooser the plot itself uses.
		 */
		PamSymbolChooser symbolChooser = symbolManager.getSymbolChooser(graph.getUniqueName(), graph.getGraphProjector());
		if (symbolChooser == null) {
			return;
		}

		boolean foundPeakFreq = false;
		for (SymbolModifier modifier : symbolChooser.getSymbolModifiers()) {
			if (modifier instanceof PeakFreqModifier) {
				setPeakFreqOptions((PeakFreqModifier) modifier);
				foundPeakFreq = true;
			}
			else {
				modifier.getSymbolModifierParams().modBitMap = 0;
			}
		}
		if (!foundPeakFreq) {
			System.err.println("PODConfigBuilder: no peak frequency symbol modifier - clicks will not be coloured by frequency");
			return;
		}

		/*
		 * Modifiers are enabled by default, but make that explicit so that the peak
		 * frequency colour can't be switched off by a partially initialised enable list.
		 */
		if (symbolChooser instanceof StandardSymbolChooser) {
			StandardSymbolChooser standardChooser = (StandardSymbolChooser) symbolChooser;
			StandardSymbolOptions options = standardChooser.getSymbolOptions();
			boolean[] enabled = options.isEnabled(standardChooser);
			for (int i = 0; i < enabled.length; i++) {
				options.setEnabled(true, i);
			}
		}
	}

	/**
	 * Switch a peak frequency modifier on and give it a frequency range suited to
	 * POD data.
	 */
	private void setPeakFreqOptions(PeakFreqModifier peakFreqModifier) {
		SymbolModifierParams params = peakFreqModifier.getSymbolModifierParams();
		params.modBitMap = SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR;
		if (params instanceof PeakFreqSymbolOptions) {
			((PeakFreqSymbolOptions) params).freqLimts = PEAK_FREQ_LIMITS.clone();
		}
		peakFreqModifier.checkColourArray();
	}

	/**
	 * Set the scroll bar to a loaded window starting at the first imported
	 * detection, positioned at the start. Runs on the JavaFX thread.
	 */
	private void configureScroller(TDDisplayFX display) {
		if (display == null || display.getTimeScroller() == null) {
			return;
		}
		long start = getDataStart();
		if (start <= 0) {
			System.err.println("PODConfigBuilder: no POD data found in the data map - display not positioned");
			return;
		}
		/*
		 * Setting the range with notify=true triggers the scroll manager to load data
		 * for the new window, which is what makes the clicks appear.
		 */
		display.getTimeScroller().setVisibleMillis(VISIBLE_MILLIS);
		display.getTimeScroller().setRangeMillis(start, start + LOADED_MILLIS, true);
		display.getTimeScroller().setValueMillis(start);
	}
}
