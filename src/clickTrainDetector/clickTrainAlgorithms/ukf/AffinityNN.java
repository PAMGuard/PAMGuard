package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A small multi-layer perceptron used as the learned affinity metric for
 * track-detection association.
 * <p>
 * The repository has no training data, so the network ships with default weights
 * that reproduce a sensible Gaussian/Mahalanobis-style gating affinity: the
 * affinity is high when the detection lies close (in normalised innovation
 * terms) to the track's predicted measurement and falls off smoothly with
 * distance. The full MLP forward pass is implemented, so trained weights can be
 * dropped in later via {@link #AffinityNN(int[], double[][][], double[][])}.
 * <p>
 * Hidden layers use tanh; the single output uses a sigmoid to bound the affinity
 * to [0, 1].
 *
 * @author Jamie Macaulay
 */
public class AffinityNN implements CTAffinity {

	/** weights[layer][outNode][inNode] */
	private final double[][][] weights;
	/** biases[layer][outNode] */
	private final double[][] biases;

	public AffinityNN(int[] layerSizes, double[][][] weights, double[][] biases) {
		this.weights = weights;
		this.biases = biases;
	}

	/**
	 * Build a network directly from its weights and biases. Hidden layers use tanh,
	 * the final layer uses sigmoid.
	 *
	 * @param weights - weights[layer][outNode][inNode].
	 * @param biases  - biases[layer][outNode].
	 */
	public AffinityNN(double[][][] weights, double[][] biases) {
		this.weights = weights;
		this.biases = biases;
	}

	/**
	 * The input (feature-vector) dimension this network expects.
	 */
	public int inputDim() {
		return weights[0][0].length;
	}

	/**
	 * The number of layers (hidden layers plus the output layer).
	 */
	public int numLayers() {
		return weights.length;
	}

	/**
	 * The output size (number of nodes) of each layer, in order. The last entry is
	 * the network's output dimension.
	 */
	public int[] layerOutputSizes() {
		int[] sizes = new int[weights.length];
		for (int i = 0; i < weights.length; i++) {
			sizes[i] = weights[i].length;
		}
		return sizes;
	}

	/**
	 * The total number of trainable parameters (all weights plus all biases).
	 */
	public int numParameters() {
		int n = 0;
		for (double[][] layer : weights) {
			for (double[] row : layer) {
				n += row.length;
			}
		}
		for (double[] layer : biases) {
			n += layer.length;
		}
		return n;
	}

	@Override
	public double affinity(double[] features) {
		double[] activations = features;
		for (int layer = 0; layer < weights.length; layer++) {
			double[] next = new double[weights[layer].length];
			for (int o = 0; o < next.length; o++) {
				double sum = biases[layer][o];
				for (int i = 0; i < activations.length; i++) {
					sum += weights[layer][o][i] * activations[i];
				}
				boolean outputLayer = layer == weights.length - 1;
				next[o] = outputLayer ? sigmoid(sum) : Math.tanh(sum);
			}
			activations = next;
		}
		return activations[0];
	}

	/**
	 * Build the default affinity network for a given feature-vector length. The
	 * default weights gate on the Mahalanobis distance (feature 0): a single tanh
	 * hidden unit forms a soft step centred at a Mahalanobis distance of 6, and the
	 * sigmoid output yields ~1 near zero distance, 0.5 at the gate centre and ~0
	 * beyond. All other features have zero weight by default (they exist so a
	 * trained network can use them).
	 *
	 * @param inputDim - the feature-vector length.
	 */
	public static AffinityNN defaultGate(int inputDim) {
		// hidden layer: 1 unit, pre-activation = -0.5 * mahalanobis + 3.0
		double[][] w1 = new double[1][inputDim];
		w1[0][0] = -0.5;
		double[][] b1 = new double[][] { { 3.0 } };

		// output layer: 1 unit, sigmoid(4 * hidden)
		double[][] w2 = new double[][] { { 4.0 } };
		double[][] b2 = new double[][] { { 0.0 } };

		return new AffinityNN(new int[] { inputDim, 1, 1 }, new double[][][] { w1, w2 },
				new double[][] { b1[0], b2[0] });
	}

	/**
	 * Load a custom affinity network from a JSON file. The file must contain
	 * {@code weights} (a 3-D array {@code [layer][outNode][inNode]}) and
	 * {@code biases} (a 2-D array {@code [layer][outNode]}). Hidden layers use tanh
	 * and the final layer uses sigmoid, so only the weights and biases are stored.
	 * Example:
	 *
	 * <pre>
	 * { "weights": [ [[-0.5, 0, 0, 0, 0]], [[4.0]] ], "biases": [ [3.0], [0.0] ] }
	 * </pre>
	 *
	 * @param file             - the JSON file.
	 * @param expectedInputDim - the required feature-vector length.
	 * @return the loaded network.
	 * @throws IOException if the file cannot be read or is malformed / the wrong
	 *                     shape.
	 */
	public static AffinityNN fromFile(File file, int expectedInputDim) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		NetworkSpec spec = mapper.readValue(file, NetworkSpec.class);
		if (spec.weights == null || spec.biases == null || spec.weights.length == 0) {
			throw new IOException("Affinity network file is missing 'weights' or 'biases'");
		}
		if (spec.weights.length != spec.biases.length) {
			throw new IOException("Affinity network has " + spec.weights.length + " weight layers but "
					+ spec.biases.length + " bias layers");
		}
		int inputDim = spec.weights[0][0].length;
		if (inputDim != expectedInputDim) {
			throw new IOException(
					"Affinity network input dimension is " + inputDim + " but " + expectedInputDim + " is required");
		}
		return new AffinityNN(spec.weights, spec.biases);
	}

	/**
	 * Write this network to a JSON file in the {@link #fromFile} format.
	 */
	public void writeJson(File file) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		ArrayNode wNode = root.putArray("weights");
		for (double[][] layer : weights) {
			ArrayNode layerNode = wNode.addArray();
			for (double[] row : layer) {
				ArrayNode rowNode = layerNode.addArray();
				for (double v : row) {
					rowNode.add(v);
				}
			}
		}
		ArrayNode bNode = root.putArray("biases");
		for (double[] layer : biases) {
			ArrayNode layerNode = bNode.addArray();
			for (double v : layer) {
				layerNode.add(v);
			}
		}
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
	}

	/** JSON binding for a custom affinity network file. */
	private static class NetworkSpec {
		public double[][][] weights;
		public double[][] biases;
	}

	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

}
