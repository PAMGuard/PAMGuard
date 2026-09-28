package clickTrainDetector.clickTrainAlgorithms.ukf;

/**
 * Defines the process and measurement models for an {@link UnscentedKalmanFilter}.
 * <p>
 * The methods that combine sigma points ({@link #stateResidual},
 * {@link #measResidual} and the weighted means) allow circular quantities such
 * as bearing to be handled correctly (wrap-around) rather than with naive
 * arithmetic.
 *
 * @author Jamie Macaulay
 */
public interface UKFModel {

	/** Dimension of the state vector. */
	int stateDim();

	/** Dimension of the measurement vector. */
	int measDim();

	/** Process model: propagate a state vector forward by one step. */
	double[] f(double[] x);

	/** Measurement model: map a state vector to the measurement space. */
	double[] h(double[] x);

	/** Process noise covariance. */
	double[][] processNoise();

	/** Measurement noise covariance. */
	double[][] measurementNoise();

	/** Residual a - b in state space (handles any circular components). */
	double[] stateResidual(double[] a, double[] b);

	/** Residual a - b in measurement space (handles any circular components). */
	double[] measResidual(double[] a, double[] b);

	/** Weighted mean of state sigma points (handles any circular components). */
	double[] stateMean(double[][] sigmas, double[] weights);

	/** Weighted mean of measurement sigma points (handles any circular components). */
	double[] measMean(double[][] sigmas, double[] weights);

}
