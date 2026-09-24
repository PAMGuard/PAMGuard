package rawDeepLearningClassifier.dlClassification.yolo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsTranslator;
import org.jamdev.jdl4pam.deepAcoustics.Pred2BoxDJL3.DeepAcousticsNetwork;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.jamdev.jpamutils.interpolation.ImResize;
import org.json.JSONArray;
import org.json.JSONObject;

import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;

/**
 * A YOLO v4 object detection model trained in MATLAB (e.g. a
 * yolov4ObjectDetector or a DeepAcoustics model) and exported as a TensorFlow
 * or PyTorch model. YOLO models detect objects (i.e. calls) within a
 * spectrogram image and so, unlike most other models, return bounding boxes.
 * <p>
 * The model is packaged in a zip file with a .pdtf JSON metadata file. As well
 * as the standard metadata the <code>model_info</code> object contains
 * information on how to create the input image and decode the output.
 * <ul>
 * <li><b>input_shape</b> - the input shape e.g. [-1, 288, 288, 3] or [-1, 3,
 * 192, 2368] for a channels first model.</li>
 * <li><b>anchorboxes</b> - the anchor boxes for each detection head as
 * [height, width] in pixels.</li>
 * <li><b>channels_first</b> - optional. true if the input and outputs are
 * [batch, channels, height, width] (e.g. PyTorch). Default false i.e. [batch,
 * height, width, channels].</li>
 * <li><b>colour</b> - optional. How the spectrogram is converted to a three
 * channel image. "grey" (default) copies the spectrogram to each channel.
 * "freq_slope" multiplies the second and third channels by a linear slope along
 * the frequency axis (0 to 1 and 1 to 0 from the top of the image).</li>
 * <li><b>rescale</b> - optional. If true the image is normalised between 0 and
 * 1 using the minimum and maximum of all channels (MATLAB's yolov4ObjectDetector
 * does this). Default false.</li>
 * <li><b>nms_overlap</b> - optional. The overlap threshold for non maximum
 * suppression of boxes of the same class (MATLAB's yolov4ObjectDetector uses
 * 0.5). Default is no non maximum suppression.</li>
 * <li><b>clip_boxes</b> - optional. true to clip bounding boxes to the image.
 * Default false.</li>
 * <li><b>filter_large_boxes</b> - optional. true to remove bounding boxes which
 * are wider or taller than the image (MATLAB's yolov4ObjectDetector does this
 * by default). Default false.</li>
 * </ul>
 * If the transformed spectrogram is not the same size as the input image then
 * the image is resized using the same bicubic interpolation as MATLAB's
 * imresize function. The spectrogram is coloured before it is resized.
 *
 * @author Jamie Macaulay
 */
public class YoloModel extends SimpleArchiveModel {

	/**
	 * How the spectrogram is converted to a three channel image.
	 */
	public enum ColourMode {
		/**
		 * The spectrogram is copied to each channel.
		 */
		GREY,

		/**
		 * Channel 1 is the spectrogram, channel 2 the spectrogram multiplied by a
		 * slope from 0 at the top of the image to 1 at the bottom and channel 3 the
		 * spectrogram multiplied by a slope from 1 at the top to 0 at the bottom.
		 */
		FREQ_SLOPE
	}

	/**
	 * The translator for the model
	 */
	private DeepAcousticsTranslator deepAcousticsTranslator;

	/**
	 * The predictor for the model if using images as input
	 */
	private Predictor<float[][][][], List<DeepAcousticResultArray>> objectPredictor;

	/**
	 * The network information e.g. anchor boxes and input shape.
	 */
	private DeepAcousticsNetwork network;

	/**
	 * How the spectrogram is converted to a three channel image.
	 */
	private ColourMode colourMode = ColourMode.GREY;

	/**
	 * True to normalise the image between 0 and 1.
	 */
	private boolean rescale = false;

	/**
	 * Constructor for the YoloModel.
	 *
	 * @param file The model file.
	 * @throws MalformedModelException If the model is malformed.
	 * @throws IOException If there is an I/O error.
	 * @throws EngineException If there is an engine error.
	 */
	public YoloModel(File file) throws MalformedModelException, IOException, EngineException {
		super(file);

		String jsonString = DLTransformsParser.readJSONString(new File(this.getAudioReprFile()));
		JSONObject mainObject = new JSONObject(jsonString);

		if (!mainObject.has("model_info")) {
			throw new MalformedModelException("There is no model_info in the YOLO model metadata " + this.getAudioReprFile());
		}

		JSONObject modelObject = mainObject.getJSONObject("model_info");

		// the input shape in the metadata takes precedence because PyTorch models do not specify an input shape
		Shape inputShape = this.getInputShape();
		if (modelObject.has("input_shape")) {
			JSONArray shapeArray = modelObject.getJSONArray("input_shape");
			long[] shape = new long[shapeArray.length()];
			for (int i = 0; i < shape.length; i++) {
				shape[i] = shapeArray.getLong(i);
			}
			inputShape = new Shape(shape);
		}

		// Load the network from the metadata
		network = loadNetwork(modelObject, inputShape);

		if (network == null) {
			throw new MalformedModelException("Failed to load YOLO network information from JSON data " + this.getAudioReprFile());
		}

		if (modelObject.has("colour")) {
			colourMode = ColourMode.valueOf(modelObject.getString("colour").trim().toUpperCase());
		}
		rescale = modelObject.optBoolean("rescale", false);

		// Initialize the translator with the loaded network
		deepAcousticsTranslator = new DeepAcousticsTranslator(network);

		//predictor for the model if using images as input
		objectPredictor = getModel().newPredictor(deepAcousticsTranslator);
	}

	/**
	 * Load the network information from the model_info JSON object. This adds
	 * some extra information on anchor boxes, the input layout and post processing
	 * that is not in the generalised JSON format.
	 *
	 * @param modelObject - the model_info JSON object.
	 * @param inputShape  - the input shape of the model.
	 * @return the network information.
	 */
	public static DeepAcousticsNetwork loadNetwork(JSONObject modelObject, Shape inputShape) {

		if (!modelObject.has("anchorboxes")) {
			return null;
		}

		JSONArray dataArray = modelObject.getJSONArray("anchorboxes");

		// You can choose to store it in a 2D array of a specific type
		ArrayList<double[][]> anchorBoxes = new ArrayList<double[][]>();

		double[] anchorBox;
		for (int i = 0; i < dataArray.length(); i++) {
			JSONArray innerArray = dataArray.getJSONArray(i);

			if (innerArray.get(0) instanceof BigDecimal || innerArray.get(0) instanceof Number) {
				//backwards compatibility where there was a single 2D array of anchor boxes
				anchorBox = new double[innerArray.length()];
				for (int j = 0; j < innerArray.length(); j++) {
					anchorBox[j] = innerArray.getDouble(j);
				}
				anchorBoxes.add(new double[][] {anchorBox});
			}
			else {
				//updated format with multiple sets of anchor boxes
				double[][] multiAnchorBox = new double[innerArray.length()][];
				for (int j = 0; j < innerArray.length(); j++) {
					JSONArray boxArray = innerArray.getJSONArray(j);
					anchorBox = new double[boxArray.length()];
					for (int k = 0; k < boxArray.length(); k++) {
						anchorBox[k] = boxArray.getDouble(k);
					}
					multiAnchorBox[j] = anchorBox;
				}
				anchorBoxes.add(multiAnchorBox);
			}
		}

		DeepAcousticsNetwork network = new DeepAcousticsNetwork(inputShape, anchorBoxes);

		network.setChannelsFirst(modelObject.optBoolean("channels_first", false));
		network.setNmsOverlap(modelObject.optDouble("nms_overlap", -1));
		network.setClipBoxes(modelObject.optBoolean("clip_boxes", false));
		network.setFilterLargeBoxes(modelObject.optBoolean("filter_large_boxes", false));

		return network;
	}

	/**
	 * Run the model with the given spectrogram image input.
	 *
	 * @param specImage The input spectrogram images. This must be the same shape
	 *                  as the model input. See {@link #formatDataStack4D(float[][][])}
	 * @return The results of the model prediction as a DeepAcousticResultArray.
	 */
	public List<DeepAcousticResultArray> runModel(float[][][][] specImage) {
		try {
			return objectPredictor.predict(specImage);
		} catch (Throwable e) {
			//if the model failed on the GPU (Apple MPS), reload on the CPU and retry once.
			if (fallbackToCpu()) {
				try {
					return objectPredictor.predict(specImage);
				} catch (Throwable e2) {
					e2.printStackTrace();
				}
			} else {
				System.err.println("Error running YOLO model. The model graph failed to execute - "
						+ "this usually indicates the exported model itself is broken (e.g. a channel-split/route "
						+ "slice exported with a batch size of 0 instead of -1) rather than a problem with the input data.");
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Rebuild the object detection predictor against the reloaded model (called when
	 * the model falls back from the GPU to the CPU).
	 */
	@Override
	protected void onModelReloaded() {
		objectPredictor = getModel().newPredictor(deepAcousticsTranslator);
	}

	/**
	 * Set the minimum score for a detection.
	 *
	 * @param threshold - the minimum score (objectness multiplied by the maximum
	 *                  class probability) for a detection.
	 */
	public void setThreshold(float threshold) {
		deepAcousticsTranslator.setThresh(threshold);
	}

	/**
	 * Get the network information e.g. anchor boxes and input shape.
	 *
	 * @return the network information.
	 */
	public DeepAcousticsNetwork getNetwork() {
		return network;
	}

	/**
	 * Get the height of the input image in pixels.
	 *
	 * @return the image height (frequency axis).
	 */
	public int getImageHeight() {
		return network.getImageHeight();
	}

	/**
	 * Get the width of the input image in pixels.
	 *
	 * @return the image width (time axis).
	 */
	public int getImageWidth() {
		return network.getImageWidth();
	}

	/**
	 * Convert a stack of transformed spectrograms into model input images. Each
	 * spectrogram is coloured, resized to the model input size, optionally
	 * normalised and arranged in the order expected by the model.
	 *
	 * @param transformedDataStack - the spectrograms [image][time][frequency]. The
	 *                             first frequency bin is the top of the image.
	 * @return the images [image][height][width][channels] or
	 *         [image][channels][height][width] for channels first models.
	 */
	public float[][][][] formatDataStack4D(float[][][] transformedDataStack) {
		float[][][][] dataStack4D = new float[transformedDataStack.length][][][];
		for (int i = 0; i < transformedDataStack.length; i++) {
			dataStack4D[i] = formatImage(transformedDataStack[i]);
		}
		return dataStack4D;
	}

	/**
	 * Convert a spectrogram into a model input image.
	 *
	 * @param spec - the spectrogram [time][frequency].
	 * @return the image [height][width][channels] or [channels][height][width].
	 */
	private float[][][] formatImage(float[][] spec) {

		int height = network.getImageHeight();
		int width = network.getImageWidth();
		int nChannels = network.getImageChannels();

		//the image channels - each is [time][frequency]
		double[][][] channels = colourImage(spec, nChannels, colourMode);

		for (int c = 0; c < nChannels; c++) {
			if (channels[c].length != width || channels[c][0].length != height) {
				channels[c] = ImResize.imresizeBicubic(channels[c], width, height);
			}
		}

		double min = 0;
		double range = 1;
		if (rescale) {
			min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (double[][] channel : channels) {
				for (double[] row : channel) {
					for (double val : row) {
						min = Math.min(min, val);
						max = Math.max(max, val);
					}
				}
			}
			range = max > min ? max - min : 1;
		}

		float[][][] image;
		if (network.isChannelsFirst()) {
			image = new float[nChannels][height][width];
			for (int c = 0; c < nChannels; c++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						image[c][h][w] = (float) ((channels[c][w][h] - min) / range);
					}
				}
			}
		}
		else {
			image = new float[height][width][nChannels];
			for (int c = 0; c < nChannels; c++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						image[h][w][c] = (float) ((channels[c][w][h] - min) / range);
					}
				}
			}
		}
		return image;
	}

	/**
	 * Convert a spectrogram to a multi channel image.
	 *
	 * @param spec       - the spectrogram [time][frequency]. The first frequency
	 *                   bin is the top of the image.
	 * @param nChannels  - the number of channels.
	 * @param colourMode - the colour mode.
	 * @return the channels [channel][time][frequency].
	 */
	public static double[][][] colourImage(float[][] spec, int nChannels, ColourMode colourMode) {
		int nTime = spec.length;
		int nFreq = spec[0].length;

		double[][][] channels = new double[nChannels][nTime][nFreq];
		for (int c = 0; c < nChannels; c++) {
			for (int j = 0; j < nFreq; j++) {
				double scale = 1;
				if (colourMode == ColourMode.FREQ_SLOPE && nFreq > 1) {
					//MATLAB linspace(0,1,nFreq) and linspace(1,0,nFreq)
					if (c == 1) scale = j / (double) (nFreq - 1);
					if (c == 2) scale = 1 - j / (double) (nFreq - 1);
				}
				for (int i = 0; i < nTime; i++) {
					channels[c][i][j] = spec[i][j] * scale;
				}
			}
		}
		return channels;
	}

}
