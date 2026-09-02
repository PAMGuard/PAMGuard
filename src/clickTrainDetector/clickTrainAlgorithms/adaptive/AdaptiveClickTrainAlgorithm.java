package clickTrainDetector.clickTrainAlgorithms.adaptive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamUtils.PamUtils;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.TempCTDataUnit;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackDataUnits;
import clickTrainDetector.layout.CTDetectorGraphics;
import clickTrainDetector.layout.adaptive.AdaptiveCTGraphics;
import clickTrainDetector.localisation.CTLocalisation;

/**
 * An adaptive predictive-residual click train algorithm.
 * <p>
 * This is a standalone {@link ClickTrainAlgorithm} (registered in
 * {@code ClickTrainControl} alongside the MHT detector). It internally reuses
 * the existing {@link MHTKernel} multi-hypothesis tracking machinery, but wires
 * it with a self-calibrating {@link AdaptiveCTChi2Provider} scorer instead of
 * the standard chi^2 calculation. The scorer learns the expected jitter of each
 * feature (ICI, amplitude, bearing) from the track itself, so the user only has
 * to set a handful of intuitive parameters rather than a variance coefficient
 * and minimum error for every feature.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveClickTrainAlgorithm implements ClickTrainAlgorithm, PamSettings {

	public static final String ADAPTIVE_NAME = "Adaptive MHT detector";

	/**
	 * The time in millis between updates of the active (unconfirmed) click trains.
	 */
	private static final int ACTIVE_TRACK_UPDATE_TIME = 5000;

	/**
	 * Hard limit on the number of detections held in a single kernel before it is
	 * reset, to prevent runaway memory use.
	 */
	private static final int DETECTION_HARD_LIMIT = 10000;

	/**
	 * Number of detections between checks for trailing dead space in a kernel.
	 */
	private static final int GARBAGE_COUNT_N_TEST = 20;

	/**
	 * Minimum amount of trailing dead space before a kernel is trimmed.
	 */
	private static final int MIN_TRIM_COUNT = 100;

	private final ClickTrainControl clickTrainControl;

	private AdaptiveCTParams params;

	private ArrayList<Engine> engines = new ArrayList<>();

	private AdaptiveCTGraphics graphics;

	private final AdaptiveCTInfoJSON infoJSON = new AdaptiveCTInfoJSON();

	public AdaptiveClickTrainAlgorithm(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl;
		this.params = new AdaptiveCTParams();
		PamSettingManager.getInstance().registerSettings(this);
		setupAlgorithm();
	}

	@Override
	public String getName() {
		return ADAPTIVE_NAME;
	}

	@Override
	public void newDataUnit(PamDataUnit<?, ?> dataUnit) {
		for (int i = 0; i < engines.size(); i++) {
			Engine engine = engines.get(i);
			if (PamUtils.hasChannelMap(engine.channelBitMap, dataUnit.getChannelBitmap())) {

				checkGarbageCollect(dataUnit, engine);

				engine.mhtKernel.addDetection(dataUnit);

				if (!clickTrainControl.isViewer()
						&& (dataUnit.getTimeMilliseconds() - engine.lastActiveTrackUpdate) > ACTIVE_TRACK_UPDATE_TIME) {
					grabUnconfirmedTrains(engine);
					engine.lastActiveTrackUpdate = dataUnit.getTimeMilliseconds();
				}

				grabDoneTrains(engine.mhtKernel);

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
				engine.mhtKernel.clearKernel();
				engine.lastActiveTrackUpdate = 0;
			}
			break;
		case ClickTrainControl.PROCESSING_END:
			for (Engine engine : engines) {
				engine.mhtKernel.confirmRemainingTracks();
				grabDoneTrains(engine.mhtKernel);
			}
			break;
		case ClickTrainControl.NEW_PARAMS:
			setupAlgorithm();
			break;
		case ClickTrainControl.CLOCK_UPDATE:
			for (Engine engine : engines) {
				checkGarbageCollect((Long) info, engine);
			}
			break;
		}
	}

	@Override
	public CTDetectorGraphics getClickTrainGraphics() {
		if (graphics == null) {
			graphics = new AdaptiveCTGraphics(this);
		}
		return graphics;
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging() {
		return infoJSON;
	}

	/**
	 * Set up an engine (kernel + scorer) for each channel group.
	 */
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

	/**
	 * Whether bearing information is available from the source data block.
	 */
	private boolean isBearingAvailable() {
		PamDataBlock<?> parent = clickTrainControl.getParentDataBlock();
		if (parent != null && parent.getLocalisationContents() != null) {
			return parent.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING);
		}
		return false;
	}

	/**
	 * Whether waveform data is available from the source data block (required for
	 * the cross-correlation feature).
	 */
	private boolean isWaveformAvailable() {
		PamDataBlock<?> parent = clickTrainControl.getParentDataBlock();
		return parent != null && RawDataHolder.class.isAssignableFrom(parent.getUnitClass());
	}

	/* -------------------- track grabbing -------------------- */

	/**
	 * Grab any finished click trains from a kernel and save them.
	 */
	private synchronized void grabDoneTrains(MHTKernel<PamDataUnit> mhtKernel) {
		int nTracks = mhtKernel.getNConfrimedTracks();
		try {
			for (int i = 0; i < nTracks; i++) {
				TrackBitSet trackBitSet = mhtKernel.getConfirmedTrack(i);
				if (trackBitSet == null || trackBitSet.flag == TrackBitSet.JUNK_TRACK) {
					continue;
				}
				TrackDataUnits trackUnits = MHTClickTrainAlgorithm.getTrackDataUnits(mhtKernel,
						trackBitSet.trackBitSet, mhtKernel.getKCount());
				// note: chi2Track.getChi2() is the kernel's log-likelihood-ratio selection
				// score, which is intentionally negative for long consistent trains. Store the
				// non-negative mean chi^2 consistency metric on the click train instead.
				CTAlgorithmInfo algorithmInfo = trackBitSet.chi2Track.getMHTChi2Info();
				trackUnits.chi2Value = consistencyChi2(algorithmInfo);
				if (Double.isNaN(trackUnits.chi2Value)) {
					trackUnits.chi2Value = 0.1;
				}
				saveClickTrain(trackUnits, algorithmInfo);
			}
		} catch (Exception e) {
			System.out.printf("Handled AdaptiveClickTrainAlgorithm Exception %s in grabDoneTrains: %s\n",
					e.getClass().getSimpleName(), e.getMessage());
		}
		mhtKernel.clearConfirmedTracks();
	}

	/**
	 * The chi^2 consistency metric to report for a click train. This is the
	 * non-negative mean Huber chi^2 per click per feature (the value shown as
	 * "Mean X²" in the info panel), <i>not</i> the kernel's internal
	 * log-likelihood-ratio selection score returned by
	 * {@link clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2#getChi2()}, which is
	 * intentionally negative for long, consistent trains.
	 *
	 * @param algorithmInfo - the info produced by the track's chi^2 calculator.
	 * @return the non-negative chi^2 consistency metric.
	 */
	private static double consistencyChi2(CTAlgorithmInfo algorithmInfo) {
		if (algorithmInfo instanceof AdaptiveCTInfo) {
			return ((AdaptiveCTInfo) algorithmInfo).getMeanChi2();
		}
		return Double.NaN;
	}

	/**
	 * Save a finished click train as a {@link CTDataUnit}.
	 */
	private void saveClickTrain(TrackDataUnits trackUnits, CTAlgorithmInfo algorithmInfo) {
		if (trackUnits.dataUnits.size() < 3) {
			return;
		}
		CTDataUnit dataUnit = new CTDataUnit(trackUnits.dataUnits.get(0).getTimeMilliseconds());
		dataUnit.addSubDetections(trackUnits.dataUnits);
		dataUnit.setCTAlgorithmInfo(algorithmInfo);
		dataUnit.setCTChi2(trackUnits.chi2Value);
		clickTrainControl.getClickTrainDataBlock().addPamData(dataUnit);
	}

	/**
	 * Grab the currently active (unconfirmed) click trains for live display.
	 */
	private synchronized void grabUnconfirmedTrains(Engine engine) {
		ClickTrainDataBlock<TempCTDataUnit> unconfirmedBlock = clickTrainControl.getClickTrainProcess()
				.getUnconfirmedCTDataBlock();
		synchronized (unconfirmedBlock.getSynchLock()) {
			ListIterator<TempCTDataUnit> iterator = unconfirmedBlock.getListIterator(0);
			while (iterator.hasNext()) {
				TempCTDataUnit tempCTUnit = iterator.next();
				tempCTUnit.removeAllSubDetections();
				tempCTUnit.clearSubdetectionsRemoved();
			}
			unconfirmedBlock.clearAll();
		}

		MHTKernel<PamDataUnit> mhtKernel = engine.mhtKernel;
		if (mhtKernel.getActiveTracks() == null) {
			return;
		}
		int nTracks = mhtKernel.getActiveTracks().size();
		for (int i = 0; i < nTracks; i++) {
			TrackBitSet trackBitSet = mhtKernel.getActiveTracks().get(i);
			TrackDataUnits trackUnits = MHTClickTrainAlgorithm.getTrackDataUnits(mhtKernel, trackBitSet.trackBitSet,
					mhtKernel.getKCount());
			if (trackUnits == null || trackUnits.dataUnits.size() < 1) {
				return;
			}
			TempCTDataUnit tempDataUnit = new TempCTDataUnit(trackUnits.dataUnits.get(0).getTimeMilliseconds(),
					trackUnits.dataUnits);
			tempDataUnit.setCTChi2(consistencyChi2(trackBitSet.chi2Track.getMHTChi2Info()));
			clickTrainControl.getClickTrainProcess().getUnconfirmedCTDataBlock().addPamData(tempDataUnit);
			tempDataUnit.setLocalisation(
					new CTLocalisation(tempDataUnit, null, clickTrainControl.getClickTrainParams().ctLocParams));
		}
	}

	/* -------------------- garbage collection -------------------- */

	/**
	 * Reset or trim a kernel to keep memory bounded. Based on the time gap to the
	 * last detection and the amount of trailing dead space in the possibility mix.
	 */
	private void checkGarbageCollect(PamDataUnit dataUnit, Engine engine) {
		MHTKernel<PamDataUnit> mhtKernel = engine.mhtKernel;

		double iciPrev = 0;
		if (mhtKernel.getLastDataUnit() != null) {
			iciPrev = (dataUnit.getTimeMilliseconds() - mhtKernel.getLastDataUnit().getTimeMilliseconds()) / 1000.;
		}
		double maxGap = mhtKernel.getMHTParams().maxCoast * params.maxICI;

		// big gap or hard limit reached - confirm, grab and reset.
		if (mhtKernel.getKCount() > mhtKernel.getMHTParams().nPruneBackStart
				&& (iciPrev > maxGap || mhtKernel.getKCount() > DETECTION_HARD_LIMIT)) {
			mhtKernel.confirmRemainingTracks();
			grabDoneTrains(mhtKernel);
			mhtKernel.clearKernel();
			return;
		}

		// periodically trim trailing dead space.
		if (mhtKernel.getKCount() != 0 && mhtKernel.getKCount() % GARBAGE_COUNT_N_TEST == 0
				&& mhtKernel.getKCount() > mhtKernel.getMHTParams().nPruneBackStart) {
			int newRefIndex = mhtKernel.getFirstDetectionIndex();
			if (newRefIndex == mhtKernel.getKCount()) {
				mhtKernel.clearKernel();
			} else if (newRefIndex > MIN_TRIM_COUNT && newRefIndex < mhtKernel.getKCount()) {
				mhtKernel.clearKernelGarbage(newRefIndex);
			}
		}
	}

	/**
	 * Reset a kernel based on a long gap in time (e.g. no clicks for a while).
	 */
	private void checkGarbageCollect(long timeMillis, Engine engine) {
		MHTKernel<PamDataUnit> mhtKernel = engine.mhtKernel;
		if (mhtKernel.getLastDataUnit() == null) {
			return;
		}
		if ((timeMillis - mhtKernel.getLastDataUnit().getTimeMilliseconds()) > mhtKernel.getMHTParams().maxCoast
				* params.maxICI * 1000.) {
			mhtKernel.confirmRemainingTracks();
			grabDoneTrains(mhtKernel);
			mhtKernel.clearKernel();
		}
	}

	/* -------------------- params -------------------- */

	public AdaptiveCTParams getParams() {
		return params;
	}

	public void setParams(AdaptiveCTParams params) {
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
		return AdaptiveCTParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			this.params = (AdaptiveCTParams) pamControlledUnitSettings.getSettings();
			setupAlgorithm();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * A single adaptive click train engine for one channel or channel group.
	 */
	private class Engine {

		private final MHTKernel<PamDataUnit> mhtKernel;

		private final int channelBitMap;

		private long lastActiveTrackUpdate = 0;

		private Engine(int channelBitMap) {
			this.channelBitMap = channelBitMap;
			AdaptiveCTChi2Provider provider = new AdaptiveCTChi2Provider(params, isBearingAvailable(),
					isWaveformAvailable());
			this.mhtKernel = new MHTKernel<>(provider);
			this.mhtKernel.setMHTParams(params.mhtKernel);
		}
	}

}
