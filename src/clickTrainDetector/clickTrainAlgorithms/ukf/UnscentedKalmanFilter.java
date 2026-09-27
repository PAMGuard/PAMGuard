package clickTrainDetector.clickTrainAlgorithms.ukf;

/**
 * A generic n-dimensional Unscented Kalman Filter using Van der Merwe's scaled
 * sigma-point set. The process and measurement models (including handling of
 * circular quantities) are supplied by a {@link UKFModel}.
 * <p>
 * After {@link #update(double[])} the innovation, its covariance and the
 * normalised-innovation-squared (Mahalanobis distance) of the last measurement
 * are available for use by the data-association stage.
 *
 * @author Jamie Macaulay
 */
public class UnscentedKalmanFilter {

	private final UKFModel model;
	private final int n;

	/** State mean. */
	private double[] x;
	/** State covariance. */
	private double[][] P;

	/* sigma-point weights */
	private final double[] wm;
	private final double[] wc;
	private final double lambda;

	/* diagnostics from the last update */
	private double[] lastInnovation;
	private double[][] lastInnovationCov;
	private double lastNIS;

	/**
	 * The default sigma-point spread. Van der Merwe's canonical value is a very
	 * small alpha (~1e-3), which is appropriate when the state is high-dimensional
	 * and the dynamics are strongly nonlinear near the mean. Here the state is small
	 * and the only nonlinearity is the circular bearing mean, so an alpha that small
	 * collapses the sigma points essentially onto the mean and the filter degrades to
	 * a linearised (EKF-like) update that never exercises the unscented transform.
	 * A spread of 1.0 (with kappa 0 this gives lambda 0, a well-conditioned "standard"
	 * unscented transform) places the sigma points a genuine standard deviation out,
	 * so circular-bearing spread and any future nonlinearity in the model are captured.
	 */
	public static final double DEFAULT_ALPHA = 1.0;
	private static final double DEFAULT_BETA = 2.0;
	private static final double DEFAULT_KAPPA = 0.0;

	/**
	 * @param model - the process/measurement model.
	 * @param x0    - initial state mean.
	 * @param P0    - initial state covariance.
	 */
	public UnscentedKalmanFilter(UKFModel model, double[] x0, double[][] P0) {
		this(model, x0, P0, DEFAULT_ALPHA, DEFAULT_BETA, DEFAULT_KAPPA);
	}

	/**
	 * @param model - the process/measurement model.
	 * @param x0    - initial state mean.
	 * @param P0    - initial state covariance.
	 * @param alpha - sigma-point spread (0 &lt; alpha &lt;= 1). Larger spreads the
	 *                sigma points further from the mean; see {@link #DEFAULT_ALPHA}.
	 * @param beta  - prior-knowledge term (2 is optimal for a Gaussian).
	 * @param kappa - secondary scaling (usually 0).
	 */
	public UnscentedKalmanFilter(UKFModel model, double[] x0, double[][] P0, double alpha, double beta, double kappa) {
		this.model = model;
		this.n = model.stateDim();
		this.x = x0.clone();
		this.P = copy(P0);

		this.lambda = alpha * alpha * (n + kappa) - n;

		int nSig = 2 * n + 1;
		wm = new double[nSig];
		wc = new double[nSig];
		wm[0] = lambda / (n + lambda);
		wc[0] = lambda / (n + lambda) + (1 - alpha * alpha + beta);
		for (int i = 1; i < nSig; i++) {
			wm[i] = wc[i] = 1.0 / (2 * (n + lambda));
		}
	}

	/**
	 * Copy constructor - clones the mutable state (mean and covariance) and shares
	 * the immutable model and precomputed sigma-point weights. Used to branch a whole
	 * tracker state for multi-hypothesis (N-scan) search.
	 */
	private UnscentedKalmanFilter(UnscentedKalmanFilter src) {
		this.model = src.model;
		this.n = src.n;
		this.x = src.x.clone();
		this.P = copy(src.P);
		this.wm = src.wm.clone();
		this.wc = src.wc.clone();
		this.lambda = src.lambda;
		// the last-update diagnostics are recomputed on the next update, so are not copied.
	}

	/**
	 * An independent copy of this filter (cloned mean and covariance). The model and
	 * sigma-point weights are shared as they are immutable.
	 */
	public UnscentedKalmanFilter copy() {
		return new UnscentedKalmanFilter(this);
	}

	/**
	 * Generate the scaled sigma points around the current state.
	 */
	private double[][] sigmaPoints() {
		double[][] sqrt = cholesky(scale(P, n + lambda));
		int nSig = 2 * n + 1;
		double[][] sigmas = new double[nSig][n];
		sigmas[0] = x.clone();
		for (int i = 0; i < n; i++) {
			for (int k = 0; k < n; k++) {
				// columns of the matrix square root
				sigmas[i + 1][k] = x[k] + sqrt[k][i];
				sigmas[i + 1 + n][k] = x[k] - sqrt[k][i];
			}
		}
		return sigmas;
	}

	/**
	 * Prediction step: propagate sigma points through the process model.
	 */
	public void predict() {
		double[][] sigmas = sigmaPoints();
		double[][] propagated = new double[sigmas.length][];
		for (int i = 0; i < sigmas.length; i++) {
			propagated[i] = model.f(sigmas[i]);
		}
		x = model.stateMean(propagated, wm);
		double[][] cov = copy(model.processNoise());
		for (int i = 0; i < propagated.length; i++) {
			double[] d = model.stateResidual(propagated[i], x);
			addOuter(cov, d, d, wc[i]);
		}
		P = cov;
	}

	/**
	 * Measurement update step.
	 *
	 * @param z - the measurement vector.
	 */
	public void update(double[] z) {
		double[][] sigmas = sigmaPoints();
		int m = model.measDim();
		double[][] zSig = new double[sigmas.length][];
		for (int i = 0; i < sigmas.length; i++) {
			zSig[i] = model.h(sigmas[i]);
		}
		double[] zMean = model.measMean(zSig, wm);

		// innovation covariance S and cross covariance Pxz
		double[][] S = copy(model.measurementNoise());
		double[][] Pxz = new double[n][m];
		for (int i = 0; i < sigmas.length; i++) {
			double[] dz = model.measResidual(zSig[i], zMean);
			addOuter(S, dz, dz, wc[i]);
			double[] dx = model.stateResidual(sigmas[i], x);
			for (int r = 0; r < n; r++) {
				for (int c = 0; c < m; c++) {
					Pxz[r][c] += wc[i] * dx[r] * dz[c];
				}
			}
		}

		double[][] Sinv = invert(S);
		double[][] K = multiply(Pxz, Sinv); // n x m

		double[] innovation = model.measResidual(z, zMean);
		// x = x + K * innovation
		for (int r = 0; r < n; r++) {
			double add = 0;
			for (int c = 0; c < m; c++) {
				add += K[r][c] * innovation[c];
			}
			x[r] += add;
		}
		// P = P - K S K^T
		double[][] KS = multiply(K, S);
		double[][] KSKt = multiplyByTranspose(KS, K);
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				P[r][c] -= KSKt[r][c];
			}
		}

		this.lastInnovation = innovation;
		this.lastInnovationCov = S;
		this.lastNIS = quadratic(innovation, Sinv);
	}

	/**
	 * Predict the measurement that the current state expects, without modifying the
	 * filter. Used by the association stage.
	 */
	public double[] predictedMeasurement() {
		return model.h(x);
	}

	/**
	 * The innovation covariance that the current state would produce for a
	 * measurement (predicted measurement covariance + R), without modifying the
	 * filter.
	 */
	public double[][] predictedMeasurementCov() {
		double[][] sigmas = sigmaPoints();
		double[][] zSig = new double[sigmas.length][];
		for (int i = 0; i < sigmas.length; i++) {
			zSig[i] = model.h(sigmas[i]);
		}
		double[] zMean = model.measMean(zSig, wm);
		double[][] S = copy(model.measurementNoise());
		for (int i = 0; i < sigmas.length; i++) {
			double[] dz = model.measResidual(zSig[i], zMean);
			addOuter(S, dz, dz, wc[i]);
		}
		return S;
	}

	/**
	 * A reusable snapshot of the current predicted measurement and the inverse of
	 * its innovation covariance. Computing this involves a full unscented transform
	 * (sigma points, Cholesky, matrix inversion) but depends only on the track's
	 * state, not on any candidate detection - so during association it should be
	 * computed once per track and reused for every candidate detection.
	 */
	public static final class Prediction {
		private final double[] zPred;
		private final double[][] sInv;

		private Prediction(double[] zPred, double[][] sInv) {
			this.zPred = zPred;
			this.sInv = sInv;
		}

		/** The predicted measurement. */
		public double[] zPred() {
			return zPred;
		}
	}

	/**
	 * Compute the predicted measurement and inverse innovation covariance once, for
	 * reuse against many candidate detections. Does not modify the filter.
	 */
	public Prediction predictMeasurement() {
		double[][] sigmas = sigmaPoints();
		double[][] zSig = new double[sigmas.length][];
		for (int i = 0; i < sigmas.length; i++) {
			zSig[i] = model.h(sigmas[i]);
		}
		double[] zMean = model.measMean(zSig, wm);
		double[][] s = copy(model.measurementNoise());
		for (int i = 0; i < sigmas.length; i++) {
			double[] dz = model.measResidual(zSig[i], zMean);
			addOuter(s, dz, dz, wc[i]);
		}
		return new Prediction(zMean, invert(s));
	}

	/**
	 * Mahalanobis distance of a candidate measurement against a precomputed
	 * {@link Prediction} (cheap: just a residual and a quadratic form).
	 */
	public double mahalanobis(double[] z, Prediction prediction) {
		double[] innov = model.measResidual(z, prediction.zPred);
		return quadratic(innov, prediction.sInv);
	}

	/**
	 * Mahalanobis distance (normalised innovation squared) of a candidate
	 * measurement against the current predicted measurement, without modifying the
	 * filter. Recomputes the prediction each call - prefer
	 * {@link #mahalanobis(double[], Prediction)} in hot loops.
	 */
	public double mahalanobis(double[] z) {
		return mahalanobis(z, predictMeasurement());
	}

	public double[] getState() {
		return x;
	}

	public double[][] getCovariance() {
		return P;
	}

	public double getLastNIS() {
		return lastNIS;
	}

	public double[] getLastInnovation() {
		return lastInnovation;
	}

	public double[][] getLastInnovationCov() {
		return lastInnovationCov;
	}

	/* ------------------------- small dense linear algebra ------------------------- */

	private static double[][] copy(double[][] a) {
		double[][] b = new double[a.length][];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i].clone();
		}
		return b;
	}

	private static double[][] scale(double[][] a, double s) {
		double[][] b = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				b[i][j] = a[i][j] * s;
			}
		}
		return b;
	}

	/** Add weight * a * b^T into m. */
	private static void addOuter(double[][] m, double[] a, double[] b, double weight) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				m[i][j] += weight * a[i] * b[j];
			}
		}
	}

	/** x^T A x. */
	private static double quadratic(double[] x, double[][] a) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			double row = 0;
			for (int j = 0; j < x.length; j++) {
				row += a[i][j] * x[j];
			}
			sum += x[i] * row;
		}
		return sum;
	}

	private static double[][] multiply(double[][] a, double[][] b) {
		int r = a.length;
		int c = b[0].length;
		int k = b.length;
		double[][] out = new double[r][c];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				double s = 0;
				for (int t = 0; t < k; t++) {
					s += a[i][t] * b[t][j];
				}
				out[i][j] = s;
			}
		}
		return out;
	}

	/** a * b^T. */
	private static double[][] multiplyByTranspose(double[][] a, double[][] b) {
		int r = a.length;
		int c = b.length;
		int k = a[0].length;
		double[][] out = new double[r][c];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				double s = 0;
				for (int t = 0; t < k; t++) {
					s += a[i][t] * b[j][t];
				}
				out[i][j] = s;
			}
		}
		return out;
	}

	/**
	 * Lower-triangular Cholesky factor (with a small diagonal jitter for numerical
	 * robustness). Returns L such that L L^T = a.
	 */
	private static double[][] cholesky(double[][] a) {
		int n = a.length;
		double[][] l = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				double sum = a[i][j];
				for (int k = 0; k < j; k++) {
					sum -= l[i][k] * l[j][k];
				}
				if (i == j) {
					l[i][j] = Math.sqrt(Math.max(sum, 1e-12));
				} else {
					l[i][j] = sum / l[j][j];
				}
			}
		}
		return l;
	}

	/**
	 * Invert a small symmetric positive-definite matrix by Gauss-Jordan
	 * elimination with partial pivoting.
	 */
	private static double[][] invert(double[][] a) {
		int n = a.length;
		double[][] m = copy(a);
		double[][] inv = new double[n][n];
		for (int i = 0; i < n; i++) {
			inv[i][i] = 1.0;
		}
		for (int col = 0; col < n; col++) {
			int pivot = col;
			for (int r = col + 1; r < n; r++) {
				if (Math.abs(m[r][col]) > Math.abs(m[pivot][col])) {
					pivot = r;
				}
			}
			double[] tmp = m[col];
			m[col] = m[pivot];
			m[pivot] = tmp;
			tmp = inv[col];
			inv[col] = inv[pivot];
			inv[pivot] = tmp;

			double d = m[col][col];
			if (Math.abs(d) < 1e-12) {
				d = d < 0 ? -1e-12 : 1e-12;
			}
			for (int j = 0; j < n; j++) {
				m[col][j] /= d;
				inv[col][j] /= d;
			}
			for (int r = 0; r < n; r++) {
				if (r != col) {
					double factor = m[r][col];
					for (int j = 0; j < n; j++) {
						m[r][j] -= factor * m[col][j];
						inv[r][j] -= factor * inv[col][j];
					}
				}
			}
		}
		return inv;
	}

}
