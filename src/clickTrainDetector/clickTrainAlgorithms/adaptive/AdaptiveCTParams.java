package clickTrainDetector.clickTrainAlgorithms.adaptive;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernelParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;

/**
 * Parameters for the adaptive click train algorithm.
 * <p>
 * Unlike the standard MHT chi^2 algorithm, the adaptive algorithm does not
 * require a variance coefficient and minimum error value for every feature.
 * Instead it self-calibrates the expected jitter of each feature from the track
 * itself. As such only a handful of intuitive parameters are exposed to the
 * user.
 * <p>
 * Extends {@link MHTChi2Params} (which provides the {@code maxICI} field) so the
 * adaptive scorer can plug into the reused {@code MHTChi2Provider} machinery.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTParams extends MHTChi2Params implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * Sensitivity of the algorithm between 0 (loose) and 1 (tight). Higher values
	 * make the algorithm stricter, i.e. clicks must conform more closely to the
	 * predicted feature values to be added to a train. This scales the minimum
	 * residual "floor" used for every feature.
	 */
	public double sensitivity = 0.5;

	/**
	 * The expected probability that a click in a train is detected (0-1). This
	 * controls how readily the algorithm bridges missed clicks (coasts). A high
	 * value (e.g. 0.9) assumes few clicks are missed and so coasting is penalised
	 * more heavily.
	 */
	public double detectionProb = 0.9;

	/* ---- which features the detector uses to group clicks ---- */

	/**
	 * Use inter-click-interval (ICI) consistency to group clicks. ICI is always
	 * used internally for coasting and rejecting over-long trains; this flag only
	 * controls whether ICI consistency contributes to the matching score.
	 */
	public boolean useICI = true;

	/**
	 * Use amplitude consistency to group clicks.
	 */
	public boolean useAmplitude = true;

	/**
	 * Use bearing consistency to group clicks. Automatically ignored when no
	 * bearing information is available.
	 */
	public boolean useBearing = true;

	/* ---- maximum bearing jump ---- */

	/**
	 * The maximum bearing jump between consecutive clicks, in DEGREES. This is the
	 * single bearing tolerance control: it always sets the minimum residual scale
	 * ("floor") used to standardise bearing residuals (how much bearing change the
	 * detector tolerates), and when {@link #bearingJumpEnable} is set it is
	 * additionally applied as a hard cutoff (in the {@link #bearingJumpDrctn}
	 * direction).
	 */
	public double maxBearingJumpDeg = 3;

	/**
	 * Whether the maximum bearing jump is additionally applied as a hard cutoff. When
	 * enabled, a click whose bearing jumps more than {@link #maxBearingJumpDeg} (in
	 * the {@link #bearingJumpDrctn} direction) from the previous click in a train
	 * marks the train as junk, stopping it. Useful for high-SNR, sparse clicks (e.g.
	 * sperm whales) or towed arrays where a sudden bearing jump almost certainly
	 * belongs to a different animal. Ignored when no bearing is available.
	 */
	public boolean bearingJumpEnable = false;

	/**
	 * The direction of the bearing jump that is policed. For a towed hydrophone
	 * array {@link BearingJumpDrctn#POSITIVE} is often a good choice as animals
	 * usually pass the vessel from positive to negative bearings, so a large
	 * positive jump is unlikely to belong to the same animal. Only used when
	 * {@link #bearingJumpEnable} is set.
	 */
	public BearingJumpDrctn bearingJumpDrctn = BearingJumpDrctn.POSITIVE;

	/**
	 * Use waveform cross-correlation (similarity) to group clicks - and to refine
	 * the ICI measurement. Automatically ignored when no waveform data is
	 * available. More accurate but more processor intensive.
	 */
	public boolean useCorrelation = false;

	/**
	 * Use peak-frequency consistency to group clicks. The clicks of a train
	 * typically share a similar peak frequency, so an off-frequency click is
	 * penalised. Automatically ignored when no waveform / peak-frequency data is
	 * available.
	 */
	public boolean usePeakFreq = false;

	/**
	 * The peak-frequency "jump floor" in Hz - the minimum scale used to standardise
	 * peak-frequency residuals. Keeps peak frequency a meaningful cue on very
	 * consistent trains; raise it for species with naturally variable peak
	 * frequencies.
	 */
	public double peakFreqFloorHz = 2000.0;

	/**
	 * Parameters for the underlying MHT kernel (track search). These have sensible
	 * defaults and are not usually changed by the user.
	 */
	public MHTKernelParams mhtKernel = new MHTKernelParams();

	@Override
	public AdaptiveCTParams clone() {
		AdaptiveCTParams cloned = (AdaptiveCTParams) super.clone();
		// deep-copy the nested kernel params so editing them via the settings pane (which
		// works on a clone) does not mutate the live parameters.
		if (mhtKernel != null) {
			cloned.mhtKernel = mhtKernel.clone();
		}
		return cloned;
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
	}

}
