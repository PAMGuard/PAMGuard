package clickTrainDetector.clickTrainAlgorithms.ukf;

/**
 * A learned affinity metric for track-detection association. Given a feature
 * vector describing a candidate (track, detection) pairing it returns an
 * affinity in [0, 1] (1 = certain match). Implemented as an interface so a
 * trained model can be swapped in for the default one.
 *
 * @author Jamie Macaulay
 */
public interface CTAffinity {

	/**
	 * @param features - the pairing feature vector (see
	 *                 {@code UKFClickTrainAlgorithm} for the layout).
	 * @return affinity in [0, 1].
	 */
	double affinity(double[] features);

}
