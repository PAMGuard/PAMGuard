package clickTrainDetector.clickTrainAlgorithms.ukf;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;

/**
 * Extra information stored for each click train detected by the UKF tracker.
 *
 * @author Jamie Macaulay
 */
public class UKFCTInfo implements CTAlgorithmInfo {

	private final double finalICI;
	private final int nClicks;
	private final String[] featureNames;

	private final UKFCTInfoJSON logging = new UKFCTInfoJSON();

	public UKFCTInfo(double finalICI, int nClicks, String[] featureNames) {
		this.finalICI = finalICI;
		this.nClicks = nClicks;
		this.featureNames = featureNames;
	}

	public double getFinalICI() {
		return finalICI;
	}

	public int getNClicks() {
		return nClicks;
	}

	public String[] getFeatureNames() {
		return featureNames;
	}

	@Override
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Final ICI: %.4f s <p>", finalICI));
		sb.append(String.format("No. clicks: %d <p>", nClicks));
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
		return UKFClickTrainAlgorithm.UKF_NAME;
	}

}
