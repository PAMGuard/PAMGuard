package clickTrainDetector.clickTrainAlgorithms.ukf;

/**
 * The UKF process and measurement model for a click train track.
 * <p>
 * State (built from the enabled features):
 * <ul>
 * <li>{@code logICI} - natural log of the inter-click interval (seconds)</li>
 * <li>{@code vLogICI} - rate of change of log-ICI (constant-velocity drift)</li>
 * <li>{@code amp} - amplitude (dB), random walk - optional</li>
 * <li>{@code bearing} - bearing (radians), circular random walk - optional</li>
 * </ul>
 * The measurement is {@code [logICI, amp?, bearing?]}. The log/exp ICI transform
 * (used when gating in the time domain) and the circular bearing residual make
 * the model nonlinear, which is what the unscented transform handles.
 *
 * @author Jamie Macaulay
 */
public class TrackStateModel implements UKFModel {

	private final UKFParams params;
	private final boolean useAmplitude;
	private final boolean useBearing;

	private final int stateDim;
	private final int measDim;

	/* state indices */
	private static final int LOGICI = 0;
	private static final int VLOGICI = 1;
	private final int ampState;
	private final int bearingState;

	/* measurement indices */
	private static final int M_LOGICI = 0;
	private final int ampMeas;
	private final int bearingMeas;

	public TrackStateModel(UKFParams params, boolean useAmplitude, boolean useBearing) {
		this.params = params;
		this.useAmplitude = useAmplitude;
		this.useBearing = useBearing;

		int s = 2; // logICI + vLogICI
		int m = 1; // logICI
		ampState = useAmplitude ? s++ : -1;
		ampMeas = useAmplitude ? m++ : -1;
		bearingState = useBearing ? s++ : -1;
		bearingMeas = useBearing ? m++ : -1;
		stateDim = s;
		measDim = m;
	}

	@Override
	public int stateDim() {
		return stateDim;
	}

	@Override
	public int measDim() {
		return measDim;
	}

	public boolean usesAmplitude() {
		return useAmplitude;
	}

	public boolean usesBearing() {
		return useBearing;
	}

	public int ampMeasIndex() {
		return ampMeas;
	}

	public int bearingMeasIndex() {
		return bearingMeas;
	}

	/** Build an initial state vector for a new track. */
	public double[] initialState(double logICI, double amp, double bearing) {
		double[] x = new double[stateDim];
		x[LOGICI] = logICI;
		x[VLOGICI] = 0;
		if (useAmplitude) {
			x[ampState] = amp;
		}
		if (useBearing) {
			x[bearingState] = bearing;
		}
		return x;
	}

	/** Build the (loose) initial covariance for a new track. */
	public double[][] initialCovariance() {
		double[][] p = new double[stateDim][stateDim];
		p[LOGICI][LOGICI] = 1.0; // ICI uncertain by ~e^1 until a second click arrives
		p[VLOGICI][VLOGICI] = 0.01;
		if (useAmplitude) {
			p[ampState][ampState] = 25.0;
		}
		if (useBearing) {
			p[bearingState][bearingState] = Math.toRadians(30) * Math.toRadians(30);
		}
		return p;
	}

	/** Build a measurement vector from an inter-click interval and features. */
	public double[] measurement(double ici, double amp, double bearing) {
		double[] z = new double[measDim];
		z[M_LOGICI] = Math.log(Math.max(ici, 1e-6));
		if (useAmplitude) {
			z[ampMeas] = amp;
		}
		if (useBearing) {
			z[bearingMeas] = bearing;
		}
		return z;
	}

	/** The current log-ICI from a state vector. */
	public double logICI(double[] state) {
		return state[LOGICI];
	}

	@Override
	public double[] f(double[] x) {
		double[] out = x.clone();
		out[LOGICI] = x[LOGICI] + x[VLOGICI]; // constant-velocity log-ICI drift
		// vLogICI, amp and bearing are random walks (unchanged here; noise via Q)
		return out;
	}

	@Override
	public double[] h(double[] x) {
		double[] z = new double[measDim];
		z[M_LOGICI] = x[LOGICI];
		if (useAmplitude) {
			z[ampMeas] = x[ampState];
		}
		if (useBearing) {
			z[bearingMeas] = x[bearingState];
		}
		return z;
	}

	@Override
	public double[][] processNoise() {
		double[][] q = new double[stateDim][stateDim];
		q[LOGICI][LOGICI] = params.iciProcessNoise;
		q[VLOGICI][VLOGICI] = params.iciRateProcessNoise;
		if (useAmplitude) {
			q[ampState][ampState] = params.ampProcessNoise;
		}
		if (useBearing) {
			q[bearingState][bearingState] = params.bearingProcessNoise;
		}
		return q;
	}

	@Override
	public double[][] measurementNoise() {
		double[][] r = new double[measDim][measDim];
		r[M_LOGICI][M_LOGICI] = params.iciMeasNoise;
		if (useAmplitude) {
			r[ampMeas][ampMeas] = params.ampMeasNoise;
		}
		if (useBearing) {
			r[bearingMeas][bearingMeas] = params.bearingMeasNoiseRad2();
		}
		return r;
	}

	@Override
	public double[] stateResidual(double[] a, double[] b) {
		double[] d = new double[stateDim];
		for (int i = 0; i < stateDim; i++) {
			d[i] = a[i] - b[i];
		}
		if (useBearing) {
			d[bearingState] = wrap(a[bearingState] - b[bearingState]);
		}
		return d;
	}

	@Override
	public double[] measResidual(double[] a, double[] b) {
		double[] d = new double[measDim];
		for (int i = 0; i < measDim; i++) {
			d[i] = a[i] - b[i];
		}
		if (useBearing) {
			d[bearingMeas] = wrap(a[bearingMeas] - b[bearingMeas]);
		}
		return d;
	}

	@Override
	public double[] stateMean(double[][] sigmas, double[] weights) {
		double[] mean = weightedMean(sigmas, weights, stateDim, useBearing ? bearingState : -1);
		return mean;
	}

	@Override
	public double[] measMean(double[][] sigmas, double[] weights) {
		return weightedMean(sigmas, weights, measDim, useBearing ? bearingMeas : -1);
	}

	/**
	 * Weighted mean of sigma points, treating the component at {@code circularIndex}
	 * (if >= 0) as a circular quantity.
	 */
	private static double[] weightedMean(double[][] sigmas, double[] weights, int dim, int circularIndex) {
		double[] mean = new double[dim];
		double sinSum = 0;
		double cosSum = 0;
		for (int i = 0; i < sigmas.length; i++) {
			for (int k = 0; k < dim; k++) {
				if (k == circularIndex) {
					sinSum += weights[i] * Math.sin(sigmas[i][k]);
					cosSum += weights[i] * Math.cos(sigmas[i][k]);
				} else {
					mean[k] += weights[i] * sigmas[i][k];
				}
			}
		}
		if (circularIndex >= 0) {
			mean[circularIndex] = Math.atan2(sinSum, cosSum);
		}
		return mean;
	}

	/** Wrap an angle to (-pi, pi]. */
	private static double wrap(double angle) {
		return Math.atan2(Math.sin(angle), Math.cos(angle));
	}

}
