package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * A small, self-contained trainer for the affinity multi-layer perceptron used
 * by the UKF detector. The network is a single-hidden-layer MLP
 * ({@code inputDim → hidden (tanh) → 1 (sigmoid)}) - exactly the shape
 * {@link AffinityNN} runs - trained by mini-batch-free stochastic gradient
 * descent with momentum on a binary cross-entropy loss.
 * <p>
 * Inputs are standardised (zero mean, unit variance) for stable training; on
 * export the standardisation is folded into the first layer's weights and biases
 * so the saved network operates directly on the raw features (the format
 * {@link AffinityNN#fromFile}) reads.
 * <p>
 * Deliberately dependency-free (no native ML engine): the network is tiny and
 * this keeps training deterministic and available offline.
 *
 * @author Jamie Macaulay
 */
public class AffinityMLPTrainer {

	private final int inputDim;
	private final int hidden;

	/* trained parameters (operating on standardised inputs) */
	private double[][] w1; // [hidden][inputDim]
	private double[] b1; // [hidden]
	private double[] w2; // [hidden]
	private double b2;

	/* input standardisation */
	private double[] mean;
	private double[] std;

	private double lastLoss = Double.NaN;

	public AffinityMLPTrainer(int inputDim, int hidden) {
		this.inputDim = inputDim;
		this.hidden = hidden;
	}

	/** Callback for training progress. */
	public interface EpochListener {
		void onEpoch(int epoch, int totalEpochs, double loss);
	}

	/**
	 * Train the network with no feature dropout.
	 *
	 * @param x        - feature rows (n x inputDim).
	 * @param y        - labels (0 or 1), length n.
	 * @param epochs   - number of passes over the data.
	 * @param lr       - learning rate.
	 * @param momentum - SGD momentum (0-1).
	 * @param seed     - RNG seed for reproducible initialisation and shuffling.
	 * @param listener - optional per-epoch progress callback.
	 */
	public void train(double[][] x, double[] y, int epochs, double lr, double momentum, long seed,
			EpochListener listener) {
		train(x, y, epochs, lr, momentum, seed, null, 0.0, 0.0, listener);
	}

	/**
	 * Train the network, optionally with <b>feature dropout</b>: during training a
	 * maskable feature is, with probability {@code dropoutProb}, replaced by its
	 * "absent" value ({@code absentValue}) for that example. This teaches the
	 * network to produce a sensible affinity even when a feature is switched off at
	 * inference, so a supplied/trained network degrades gracefully when the user
	 * disables (say) waveform correlation rather than behaving unpredictably.
	 * <p>
	 * Dropout is applied in the standardised feature space to the same standardised
	 * value the raw {@code absentValue} maps to, so it matches exactly what the
	 * exported network sees at inference when the raw feature is {@code absentValue}.
	 *
	 * @param x           - feature rows (n x inputDim).
	 * @param y           - labels (0 or 1), length n.
	 * @param epochs      - number of passes over the data.
	 * @param lr          - learning rate.
	 * @param momentum    - SGD momentum (0-1).
	 * @param seed        - RNG seed for reproducible initialisation and shuffling.
	 * @param maskable    - length-inputDim mask, true for features that may be
	 *                      dropped; null or all-false disables dropout.
	 * @param dropoutProb - per-feature, per-example dropout probability (0-1).
	 * @param absentValue - the raw value a dropped feature is set to (the sentinel
	 *                      used at inference for an absent feature).
	 * @param listener    - optional per-epoch progress callback.
	 */
	public void train(double[][] x, double[] y, int epochs, double lr, double momentum, long seed, boolean[] maskable,
			double dropoutProb, double absentValue, EpochListener listener) {
		int n = x.length;
		standardise(x);
		double[][] xs = new double[n][inputDim];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < inputDim; j++) {
				xs[i][j] = (x[i][j] - mean[j]) / std[j];
			}
		}

		// the standardised value a dropped feature takes (matches the exported,
		// standardisation-folded network fed a raw absentValue at inference).
		boolean dropout = maskable != null && dropoutProb > 0;
		double[] absentStd = new double[inputDim];
		if (dropout) {
			for (int j = 0; j < inputDim; j++) {
				absentStd[j] = (absentValue - mean[j]) / std[j];
			}
		}

		Random rnd = new Random(seed);
		initWeights(rnd);

		// velocity terms for momentum
		double[][] vw1 = new double[hidden][inputDim];
		double[] vb1 = new double[hidden];
		double[] vw2 = new double[hidden];
		double[] vb2 = new double[1];

		int[] order = new int[n];
		for (int i = 0; i < n; i++) {
			order[i] = i;
		}

		for (int epoch = 0; epoch < epochs; epoch++) {
			shuffle(order, rnd);
			double lossSum = 0;
			for (int idx : order) {
				double[] xi = xs[idx];

				// feature dropout: randomly present some maskable features as "absent" so
				// the network learns to cope when a feature is disabled at inference.
				if (dropout) {
					xi = xi.clone();
					for (int j = 0; j < inputDim; j++) {
						if (maskable[j] && rnd.nextDouble() < dropoutProb) {
							xi[j] = absentStd[j];
						}
					}
				}

				// forward
				double[] a1 = new double[hidden];
				for (int h = 0; h < hidden; h++) {
					double pre = b1[h];
					for (int j = 0; j < inputDim; j++) {
						pre += w1[h][j] * xi[j];
					}
					a1[h] = Math.tanh(pre);
				}
				double pre2 = b2;
				for (int h = 0; h < hidden; h++) {
					pre2 += w2[h] * a1[h];
				}
				double o = sigmoid(pre2);
				o = Math.min(Math.max(o, 1e-7), 1 - 1e-7);
				lossSum += -(y[idx] * Math.log(o) + (1 - y[idx]) * Math.log(1 - o));

				// backward (BCE + sigmoid: dL/dpre2 = o - y)
				double dPre2 = o - y[idx];
				double[] dA1 = new double[hidden];
				for (int h = 0; h < hidden; h++) {
					dA1[h] = dPre2 * w2[h];
				}
				// output layer update
				for (int h = 0; h < hidden; h++) {
					double grad = dPre2 * a1[h];
					vw2[h] = momentum * vw2[h] - lr * grad;
					w2[h] += vw2[h];
				}
				vb2[0] = momentum * vb2[0] - lr * dPre2;
				b2 += vb2[0];
				// hidden layer update
				for (int h = 0; h < hidden; h++) {
					double dPre1 = dA1[h] * (1 - a1[h] * a1[h]); // tanh'
					for (int j = 0; j < inputDim; j++) {
						double grad = dPre1 * xi[j];
						vw1[h][j] = momentum * vw1[h][j] - lr * grad;
						w1[h][j] += vw1[h][j];
					}
					vb1[h] = momentum * vb1[h] - lr * dPre1;
					b1[h] += vb1[h];
				}
			}
			lastLoss = lossSum / n;
			if (listener != null) {
				listener.onEpoch(epoch + 1, epochs, lastLoss);
			}
		}
	}

	/** Mean binary cross-entropy of the final epoch. */
	public double getLastLoss() {
		return lastLoss;
	}

	/** Fraction of rows classified correctly (threshold 0.5) by the trained net. */
	public double accuracy(double[][] x, double[] y) {
		AffinityNN nn = toAffinityNN();
		int correct = 0;
		for (int i = 0; i < x.length; i++) {
			double p = nn.affinity(x[i]);
			if ((p >= 0.5 ? 1 : 0) == (int) Math.round(y[i])) {
				correct++;
			}
		}
		return x.length == 0 ? 0 : (double) correct / x.length;
	}

	/**
	 * Build an {@link AffinityNN} from the trained parameters, folding the input
	 * standardisation into the first layer so it runs on raw features.
	 */
	public AffinityNN toAffinityNN() {
		double[][][] weights = foldedWeights();
		double[][] biases = foldedBiases();
		return new AffinityNN(weights, biases);
	}

	/**
	 * Write the trained network to a JSON file in the {@link AffinityNN#fromFile}
	 * format.
	 */
	public void writeJson(File file) throws IOException {
		toAffinityNN().writeJson(file);
	}

	/* --------------------------- internals --------------------------- */

	private double[][][] foldedWeights() {
		// layer 1 (fold standardisation: w'[h][j] = w[h][j]/std[j])
		double[][] foldW1 = new double[hidden][inputDim];
		for (int h = 0; h < hidden; h++) {
			for (int j = 0; j < inputDim; j++) {
				foldW1[h][j] = w1[h][j] / std[j];
			}
		}
		double[][] layer2 = new double[1][hidden];
		System.arraycopy(w2, 0, layer2[0], 0, hidden);
		return new double[][][] { foldW1, layer2 };
	}

	private double[][] foldedBiases() {
		// layer 1 bias absorbs -sum_j w[h][j]*mean[j]/std[j]
		double[] foldB1 = new double[hidden];
		for (int h = 0; h < hidden; h++) {
			double b = b1[h];
			for (int j = 0; j < inputDim; j++) {
				b -= w1[h][j] * mean[j] / std[j];
			}
			foldB1[h] = b;
		}
		return new double[][] { foldB1, { b2 } };
	}

	private void standardise(double[][] x) {
		int n = x.length;
		mean = new double[inputDim];
		std = new double[inputDim];
		for (double[] row : x) {
			for (int j = 0; j < inputDim; j++) {
				mean[j] += row[j];
			}
		}
		for (int j = 0; j < inputDim; j++) {
			mean[j] /= Math.max(1, n);
		}
		for (double[] row : x) {
			for (int j = 0; j < inputDim; j++) {
				double d = row[j] - mean[j];
				std[j] += d * d;
			}
		}
		for (int j = 0; j < inputDim; j++) {
			std[j] = Math.sqrt(std[j] / Math.max(1, n));
			if (std[j] < 1e-6) {
				std[j] = 1.0; // constant feature -> avoid divide by zero
			}
		}
	}

	private void initWeights(Random rnd) {
		w1 = new double[hidden][inputDim];
		b1 = new double[hidden];
		w2 = new double[hidden];
		b2 = 0;
		double s1 = Math.sqrt(1.0 / inputDim);
		for (int h = 0; h < hidden; h++) {
			for (int j = 0; j < inputDim; j++) {
				w1[h][j] = rnd.nextGaussian() * s1;
			}
			w2[h] = rnd.nextGaussian() * Math.sqrt(1.0 / hidden);
		}
	}

	private static void shuffle(int[] a, Random rnd) {
		for (int i = a.length - 1; i > 0; i--) {
			int j = rnd.nextInt(i + 1);
			int t = a[i];
			a[i] = a[j];
			a[j] = t;
		}
	}

	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

}
