package rawDeepLearningClassifier.dlClassification.yolo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsTranslator;
import org.jamdev.jdl4pam.deepAcoustics.Pred2BoxDJL3.DeepAcousticsNetwork;
import org.jamdev.jdl4pam.deepAcoustics.YoloV8Translator;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.jamdev.jdl4pam.utils.InputShapeException;
import org.jamdev.jpamutils.interpolation.ImResize;
import org.json.JSONArray;
import org.json.JSONObject;

import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;

/**
 * A YOLO object detection model. YOLO models detect objects (i.e. calls) within
 * a spectrogram image and so, unlike most other models, return bounding boxes.
 * Two types of YOLO model are supported.
 * <ul>
 * <li>YOLO v4 models trained in MATLAB (e.g. a yolov4ObjectDetector or a
 * DeepAcoustics model) and exported as a TensorFlow or PyTorch model.</li>
 * <li>Anchor free Ultralytics models (YOLOv8, YOLO11 etc.) exported to
 * TorchScript without NMS. The output is [batch, 4 + nClasses, nAnchors].</li>
 * </ul>
 * <p>
 * The model is packaged in a zip file with a .pdtf JSON metadata file. As well
 * as the standard metadata the <code>model_info</code> object contains
 * information on how to create the input image and decode the output.
 * <ul>
 * <li><b>yolo_type</b> - optional. "yolov4" (default) for MATLAB models or
 * "ultralytics" (or "yolov8", "yolo11") for Ultralytics models.</li>
 * <li><b>input_shape</b> - the input shape e.g. [-1, 288, 288, 3] or [-1, 3,
 * 192, 2368] for a channels first model.</li>
 * <li><b>anchorboxes</b> - YOLO v4 only. The anchor boxes for each detection
 * head as [height, width] in pixels.</li>
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
 * <li><b>agnostic_nms</b> - optional, Ultralytics only. true to run non maximum
 * suppression over boxes of all classes (Ultralytics agnostic_nms). Default
 * false.</li>
 * <li><b>max_det</b> - optional, Ultralytics only. The maximum number of boxes
 * per segment. Default 300.</li>
 * <li><b>uint8</b> - optional, Ultralytics only. true to quantise the image to
 * 8 bits as Ultralytics does with image files. Default false.</li>
 * </ul>
 * For YOLO v4 models, if the transformed spectrogram is not the same size as
 * the input image then the image is resized using the same bicubic
 * interpolation as MATLAB's imresize function. The spectrogram is coloured
 * before it is resized. For Ultralytics models the image is letterboxed in the
 * same way as Ultralytics, i.e. resized keeping the aspect ratio and padded,
 * and bounding boxes are returned in pixels of the transformed spectrogram.
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
	 * The type of YOLO model.
	 */
	public enum YoloType {
		/**
		 * A YOLO v4 model with anchor boxes trained in MATLAB.
		 */
		YOLOV4,

		/**
		 * An anchor free Ultralytics model e.g. YOLOv8 or YOLO11.
		 */
		ULTRALYTICS
	}

	/**
	 * The type of YOLO model.
	 */
	private YoloType yoloType = YoloType.YOLOV4;

	/**
	 * The translator for YOLO v4 models.
	 */
	private DeepAcousticsTranslator deepAcousticsTranslator;

	/**
	 * The translator for Ultralytics models.
	 */
	private YoloV8Translator yoloV8Translator;

	/**
	 * The model input shape.
	 */
	private Shape inputShape;

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
		inputShape = this.getInputShape();
		if (modelObject.has("input_shape")) {
			JSONArray shapeArray = modelObject.getJSONArray("input_shape");
			long[] shape = new long[shapeArray.length()];
			for (int i = 0; i < shape.length; i++) {
				shape[i] = shapeArray.getLong(i);
			}
			inputShape = new Shape(shape);
		}

		yoloType = parseYoloType(modelObject.optString("yolo_type", "yolov4"));

		if (modelObject.has("colour")) {
			colourMode = ColourMode.valueOf(modelObject.getString("colour").trim().toUpperCase());
		}
		rescale = modelObject.optBoolean("rescale", false);

		if (yoloType == YoloType.ULTRALYTICS) {
			if (inputShape == null || inputShape.dimension() != 4) {
				throw new MalformedModelException("An Ultralytics YOLO model needs an input_shape of [batch, channels, height, width] in " + this.getAudioReprFile());
			}
			yoloV8Translator = loadYoloV8Translator(modelObject, inputShape);
		}
		else {
			// Load the network from the metadata
			network = loadNetwork(modelObject, inputShape);

			if (network == null) {
				throw new MalformedModelException("Failed to load YOLO network information from JSON data " + this.getAudioReprFile());
			}

			// Initialize the translator with the loaded network
			deepAcousticsTranslator = new DeepAcousticsTranslator(network);
		}

		//predictor for the model if using images as input
		objectPredictor = newObjectPredictor();
	}

	/**
	 * Get the YOLO type from the yolo_type string in the metadata.
	 *
	 * @param yoloTypeString - the YOLO type e.g. "yolov4", "ultralytics", "yolov8" or "yolo11".
	 * @return the YOLO type.
	 * @throws MalformedModelException if the type is not recognised.
	 */
	public static YoloType parseYoloType(String yoloTypeString) throws MalformedModelException {
		String type = yoloTypeString.trim().toLowerCase();
		if (type.equals("yolov4") || type.equals("matlab")) {
			return YoloType.YOLOV4;
		}
		if (type.equals("ultralytics") || type.matches("yolo(v)?(5u|8|9|10|11|12)")) {
			return YoloType.ULTRALYTICS;
		}
		throw new MalformedModelException("Unknown yolo_type " + yoloTypeString + ". Use yolov4 or ultralytics.");
	}

	/**
	 * Create the translator for an Ultralytics model from the model_info JSON
	 * object.
	 *
	 * @param modelObject - the model_info JSON object.
	 * @param inputShape  - the input shape [batch, channels, height, width].
	 * @return the translator.
	 */
	public static YoloV8Translator loadYoloV8Translator(JSONObject modelObject, Shape inputShape) {
		YoloV8Translator translator = new YoloV8Translator(inputShape);
		translator.setNmsOverlap(modelObject.optDouble("nms_overlap", 0.7));
		translator.setAgnosticNMS(modelObject.optBoolean("agnostic_nms", false));
		translator.setMaxDet(modelObject.optInt("max_det", 300));
		translator.setUint8(modelObject.optBoolean("uint8", false));
		return translator;
	}

	/**
	 * Create a predictor using the translator for the type of YOLO model.
	 * @return the predictor.
	 */
	private Predictor<float[][][][], List<DeepAcousticResultArray>> newObjectPredictor() {
		if (yoloType == YoloType.ULTRALYTICS) {
			return getModel().newPredictor(yoloV8Translator);
		}
		return getModel().newPredictor(deepAcousticsTranslator);
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
			//the input images do not fit the model - reloading the model will not help.
			InputShapeException shapeException = InputShapeException.find(e);
			if (shapeException != null) {
				System.err.println("Error running YOLO model: " + shapeException.getMessage());
				return null;
			}
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
		objectPredictor = newObjectPredictor();
	}

	/**
	 * Set the minimum score for a detection.
	 *
	 * @param threshold - the minimum score (objectness multiplied by the maximum
	 *                  class probability) for a detection.
	 */
	public void setThreshold(float threshold) {
		if (yoloType == YoloType.ULTRALYTICS) {
			yoloV8Translator.setThresh(threshold);
		}
		else {
			deepAcousticsTranslator.setThresh(threshold);
		}
	}

	/**
	 * Get the type of YOLO model.
	 *
	 * @return the YOLO type.
	 */
	public YoloType getYoloType() {
		return yoloType;
	}

	/**
	 * Get the position of the left (or top) edge of the image in the bounding box
	 * pixel coordinates. YOLO v4 models from MATLAB use MATLAB pixel coordinates
	 * where the first pixel is centred on 1 and so the image starts at 0.5.
	 * Ultralytics boxes start at 0.
	 *
	 * @return the pixel coordinate of the image edge.
	 */
	public double getBoxPixelOffset() {
		return yoloType == YoloType.ULTRALYTICS ? 0 : 0.5;
	}

	/**
	 * Get the network information e.g. anchor boxes and input shape. This is
	 * null for Ultralytics models.
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
		if (yoloType == YoloType.ULTRALYTICS) {
			return (int) inputShape.get(2);
		}
		return network.getImageHeight();
	}

	/**
	 * Get the width of the input image in pixels.
	 *
	 * @return the image width (time axis).
	 */
	public int getImageWidth() {
		if (yoloType == YoloType.ULTRALYTICS) {
			return (int) inputShape.get(3);
		}
		return network.getImageWidth();
	}

	/**
	 * Convert a stack of transformed spectrograms into model input images. Each
	 * spectrogram is coloured, resized to the model input size, optionally
	 * normalised and arranged in the order expected by the model.
	 * <p>
	 * For Ultralytics models the images are not resized because the translator
	 * letterboxes them. The images are [image][channels][height][width] where
	 * the height is the number of frequency bins and the width the number of time
	 * bins.
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

		boolean ultralytics = yoloType == YoloType.ULTRALYTICS;

		//Ultralytics images are letterboxed by the translator so keep the size of the spectrogram.
		int height = ultralytics ? spec[0].length : network.getImageHeight();
		int width = ultralytics ? spec.length : network.getImageWidth();
		int nChannels = ultralytics ? (int) inputShape.get(1) : network.getImageChannels();
		boolean channelsFirst = ultralytics || network.isChannelsFirst();

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
		if (channelsFirst) {
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
