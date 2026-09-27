package clickTrainDetector.clickTrainAlgorithms.adaptive;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2Provider;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIManager;

/**
 * Provides {@link AdaptiveCTChi2} scorers for each track hypothesis in the MHT
 * kernel. Holds the shared {@link IDIManager} so inter-click intervals are only
 * calculated once for all hypotheses.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTChi2Provider implements MHTChi2Provider<PamDataUnit> {

	private final IDIManager idiManager;

	private AdaptiveCTParams params;

	/**
	 * Whether bearing information is available from the source data. Set once when
	 * the provider is created so it is consistent for every hypothesis.
	 */
	private final boolean bearingAvailable;

	/**
	 * Whether waveform data is available from the source data (required for the
	 * cross-correlation feature). Set once when the provider is created.
	 */
	private final boolean waveformAvailable;

	public AdaptiveCTChi2Provider(AdaptiveCTParams params, boolean bearingAvailable, boolean waveformAvailable) {
		this.params = params;
		this.bearingAvailable = bearingAvailable;
		this.waveformAvailable = waveformAvailable;
		this.idiManager = new IDIManager();
		// only refine ICI with correlation when the feature is enabled and possible.
		this.idiManager.setUseCorrelation(params.useCorrelation && waveformAvailable);
	}

	@Override
	public void addDetection(PamDataUnit detection, int kcount) {
		idiManager.addDetection(detection, kcount);
	}

	@Override
	public MHTChi2<PamDataUnit> newMHTChi2(MHTChi2<PamDataUnit> mhtChi2) {
		if (mhtChi2 == null) {
			return new AdaptiveCTChi2(this);
		}
		return mhtChi2.cloneMHTChi2();
	}

	@Override
	public MHTChi2Params getSettingsObject() {
		return params;
	}

	@Override
	public MHTChi2Params getChi2Params() {
		return params;
	}

	@Override
	public void clear() {
		idiManager.clear();
	}

	@Override
	public void setMHTParams(MHTParams mhtParams) {
		// Not used - the adaptive algorithm manages its own parameters via setParams().
	}

	@Override
	public void clearKernelGarbage(int newRefIndex) {
		idiManager.trimData(newRefIndex);
	}

	@Override
	public void printSettings() {
		System.out.println("AdaptiveCTChi2Provider: maxICI=" + params.maxICI + " sensitivity=" + params.sensitivity
				+ " detectionProb=" + params.detectionProb + " bearingAvailable=" + bearingAvailable);
	}

	/**
	 * Get the adaptive parameters.
	 */
	public AdaptiveCTParams getParams() {
		return params;
	}

	/**
	 * Set the adaptive parameters.
	 */
	public void setParams(AdaptiveCTParams params) {
		this.params = params;
		this.idiManager.setUseCorrelation(params.useCorrelation && waveformAvailable);
	}

	/**
	 * Get the shared IDI (inter-click interval) manager.
	 */
	public IDIManager getIDIManager() {
		return idiManager;
	}

	/**
	 * Whether bearing information is available from the source data.
	 */
	public boolean isBearingAvailable() {
		return bearingAvailable;
	}

	/**
	 * Whether waveform data is available from the source data.
	 */
	public boolean isWaveformAvailable() {
		return waveformAvailable;
	}

}
