package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamUtils.PamUtils;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2;
import clickTrainDetector.layout.CTDetectorGraphics;
import clickTrainDetector.layout.ukf.UKFCTGraphics;

/**
 * A click train algorithm built around an Unscented Kalman Filter (UKF).
 * <p>
 * Pipeline (per time-batched "frame" of click detections):
 *
 * <pre>
 * click detections
 *   -&gt; UKF state prediction (nonlinear dynamics: log/exp ICI, circular bearing)
 *   -&gt; learned affinity metric (small MLP, {@link AffinityNN})
 *   -&gt; two-stage Hungarian assignment ({@link HungarianAlgorithm}, ByteTrack style)
 *   -&gt; UKF state update
 * </pre>
 *
 * Tracks ICI, amplitude and (when available) bearing. A standalone
 * {@link ClickTrainAlgorithm} registered in {@code ClickTrainControl} - it does
 * not use the MHT kernel. The tracking pipeline itself lives in
 * {@link UKFTracker}; this class is the PAMGuard glue.
 *
 * @author Jamie Macaulay
 */
public class UKFClickTrainAlgorithm implements ClickTrainAlgorithm, PamSettings {

	public static final String UKF_NAME = "UKF detector";

	private final ClickTrainControl clickTrainControl;

	private UKFParams params;

	private ArrayList<Engine> engines = new ArrayList<>();

	private UKFCTGraphics graphics;

	private final UKFCTInfoJSON infoJSON = new UKFCTInfoJSON();

	public UKFClickTrainAlgorithm(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl;
		this.params = new UKFParams();
		PamSettingManager.getInstance().registerSettings(this);
		setupAlgorithm();
	}

	@Override
	public String getName() {
		return UKF_NAME;
	}

	@Override
	public void newDataUnit(PamDataUnit<?, ?> dataUnit) {
		for (Engine engine : engines) {
			if (PamUtils.hasChannelMap(engine.channelBitMap, dataUnit.getChannelBitmap())) {
				engine.tracker.addClick(dataUnit);
				if (graphics != null) {
					graphics.notifyUpdate(ClickTrainControl.CLOCK_UPDATE, null);
				}
				break;
			}
		}
	}

	@Override
	public synchronized void update(int flag, Object info) {
		switch (flag) {
		case ClickTrainControl.PROCESSING_START:
			for (Engine engine : engines) {
				engine.tracker.reset();
			}
			break;
		case ClickTrainControl.PROCESSING_END:
			for (Engine engine : engines) {
				engine.tracker.finaliseAll();
			}
			break;
		case ClickTrainControl.NEW_PARAMS:
			setupAlgorithm();
			break;
		case ClickTrainControl.CLOCK_UPDATE:
			if (info instanceof Long) {
				double now = ((Long) info) / 1000.0;
				for (Engine engine : engines) {
					engine.tracker.closeOverdueTracks(now);
				}
			}
			break;
		}
	}

	@Override
	public CTDetectorGraphics getClickTrainGraphics() {
		if (graphics == null) {
			graphics = new UKFCTGraphics(this);
		}
		return graphics;
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging() {
		return infoJSON;
	}

	private void setupAlgorithm() {
		engines.clear();
		int[] channelGroups = clickTrainControl.getClickTrainParams().channelGroups;
		if (channelGroups != null) {
			for (int channelGroup : channelGroups) {
				engines.add(new Engine(channelGroup));
			}
		}
		getClickTrainGraphics().notifyUpdate(ClickTrainControl.NEW_PARAMS, getParams());
	}

	private boolean isBearingAvailable() {
		PamDataBlock<?> parent = clickTrainControl.getParentDataBlock();
		if (parent != null && parent.getLocalisationContents() != null) {
			return parent.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING);
		}
		return false;
	}

	/**
	 * A single UKF tracker for one channel or channel group; finished tracks are
	 * saved as {@link CTDataUnit}s.
	 */
	private class Engine {
		private final int channelBitMap;
		private final UKFTracker tracker;

		private Engine(int channelBitMap) {
			this.channelBitMap = channelBitMap;
			this.tracker = new UKFTracker(params, isBearingAvailable(), track -> saveTrack(track));
		}
	}

	/**
	 * Save a finished track as a {@link CTDataUnit} if it is long enough.
	 */
	private void saveTrack(ClickTrack track) {
		if (track.size() < params.minTrackLength) {
			return;
		}
		ArrayList<PamDataUnit> clicks = track.getClicks();
		CTDataUnit dataUnit = new CTDataUnit(clicks.get(0).getTimeMilliseconds());
		dataUnit.addSubDetections(clicks);
		dataUnit.setCTAlgorithmInfo(new UKFCTInfo(track.expectedICI(), track.size(), featureNames(track.getModel())));

		// assign a chi^2 quality metric using the same calculation as the MHT and
		// adaptive algorithms, so the value is comparable across detectors.
		double chi2 = StandardMHTChi2.calcTrackChi2(clicks, track.getModel().usesAmplitude(),
				track.getModel().usesBearing());
		if (Double.isNaN(chi2)) {
			chi2 = 0.1;
		}
		dataUnit.setCTChi2(chi2);

		clickTrainControl.getClickTrainDataBlock().addPamData(dataUnit);
	}

	private static String[] featureNames(TrackStateModel model) {
		ArrayList<String> names = new ArrayList<>();
		names.add("ICI");
		if (model.usesAmplitude()) {
			names.add("Amplitude");
		}
		if (model.usesBearing()) {
			names.add("Bearing");
		}
		return names.toArray(new String[0]);
	}

	/* -------------------- params -------------------- */

	public UKFParams getParams() {
		return params;
	}

	public void setParams(UKFParams params) {
		this.params = params;
		setupAlgorithm();
	}

	public ClickTrainControl getClickTrainControl() {
		return clickTrainControl;
	}

	/* -------------------- PamSettings -------------------- */

	@Override
	public String getUnitName() {
		return clickTrainControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getName();
	}

	@Override
	public Serializable getSettingsReference() {
		return params;
	}

	@Override
	public long getSettingsVersion() {
		return UKFParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			this.params = (UKFParams) pamControlledUnitSettings.getSettings();
			setupAlgorithm();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
