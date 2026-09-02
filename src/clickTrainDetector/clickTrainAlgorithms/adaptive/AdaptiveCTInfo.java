package clickTrainDetector.clickTrainAlgorithms.adaptive;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;

/**
 * Extra information stored for each click train detected by the adaptive
 * algorithm. Holds the final mean chi^2, number of coasts and which features
 * were active.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTInfo implements CTAlgorithmInfo {

	private final double meanChi2;

	private final int nCoasts;

	private final String[] featureNames;

	private final AdaptiveCTInfoJSON logging = new AdaptiveCTInfoJSON();

	public AdaptiveCTInfo(double meanChi2, int nCoasts, String[] featureNames) {
		this.meanChi2 = meanChi2;
		this.nCoasts = nCoasts;
		this.featureNames = featureNames;
	}

	public double getMeanChi2() {
		return meanChi2;
	}

	public int getNCoasts() {
		return nCoasts;
	}

	public String[] getFeatureNames() {
		return featureNames;
	}

	@Override
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Mean X²: %.3f <p>", meanChi2));
		sb.append(String.format("No. coasts: %d <p>", nCoasts));
		sb.append("Features: ");
		for (int i = 0; i < featureNames.length; i++) {
			sb.append(featureNames[i]);
			if (i < featureNames.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmLogging() {
		return logging;
	}

	@Override
	public String getAlgorithmType() {
		return AdaptiveClickTrainAlgorithm.ADAPTIVE_NAME;
	}

}
