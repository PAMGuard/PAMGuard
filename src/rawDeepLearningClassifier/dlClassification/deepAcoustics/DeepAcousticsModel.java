package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.checkerframework.common.returnsreceiver.qual.This;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsTranslator;
import org.jamdev.jdl4pam.deepAcoustics.Pred2BoxDJL3.DeepAcousticsNetwork;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.json.JSONArray;
import org.json.JSONObject;

import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import ai.djl.inference.Predictor;
import ai.djl.translate.TranslateException;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;

/**
 * The DeepAcousticsModel class extends SimpleArchiveModel to provide functionality
 * for running deep acoustics models packaged with a settings file.
 * @author Jamie Macaulay
 */
public class DeepAcousticsModel extends SimpleArchiveModel {

	/**
	 * The translator for the model
	 */
	private DeepAcousticsTranslator deepAcousticsTranslator;

	/**
	 * The predictor for the model if using images as input
	 */
	private Predictor<float[][][][], DeepAcousticResultArray> objectPredictor;

	/**
	 * Constructor for the DeepAcousticsModel.
	 * 
	 * @param file The model file.
	 * @param network The network configuration.
	 * @throws MalformedModelException If the model is malformed.
	 * @throws IOException If there is an I/O error.
	 * @throws EngineException If there is an engine error.
	 */
	public DeepAcousticsModel(File file) throws MalformedModelException, IOException, EngineException {
		super(file);

		// Load the deep acoustics network from the audio representation file
		DeepAcousticsNetwork network = loadDeepAcousticNetwork(this.getAudioReprFile());
		
		if (network== null) {
			throw new MalformedModelException("Failed to load DeepAcousticsNetwork information from JSON data " + this.getAudioReprFile());
		}

		// Initialize the translator with the loaded network
		deepAcousticsTranslator = new DeepAcousticsTranslator(network);

		//predictor for the model if using images as input
		objectPredictor = getModel().newPredictor(deepAcousticsTranslator);
		
	}

	/**
	 * Load the DeepAcousticsNetwork from the JSON file that is associated with the model. This adds
	 * some extra information on anchor boxes that is not in the generalised JSON format. 
	 * 
	 * @param jsonPath The path to the JSON file.
	 * @return The loaded DeepAcousticsNetwork.
	 */
	private DeepAcousticsNetwork loadDeepAcousticNetwork(String jsonPath) {

		String jsonString  = DLTransformsParser.readJSONString(new File(this.getAudioReprFile()));

		JSONObject mainObject = new JSONObject(jsonString);

		if (mainObject.has("model_info")) {

			JSONObject modelObject = mainObject.getJSONObject("model_info"); 

			JSONArray dataArray = modelObject.getJSONArray("anchorboxes");

			// You can choose to store it in a 2D array of a specific type
			ArrayList<double[][]> anchorBoxes = new ArrayList<double[][]>();

			double[] anchorBox;
			for (int i = 0; i < dataArray.length(); i++) {
				JSONArray innerArray = dataArray.getJSONArray(i);
				anchorBox = new double[innerArray.length()];
				for (int j = 0; j < innerArray.length(); j++) {
					anchorBox[j] = innerArray.getDouble(j);
				}
				anchorBoxes.add(new double[][] {anchorBox});
			}
			
			//Print out some results
			System.out.println("Anchor boxes: " + anchorBoxes.size() + " " + anchorBoxes.get(0).length + " " + anchorBoxes.get(0)[0].length);
			
			
			DeepAcousticsNetwork network = new DeepAcousticsNetwork(this.getInputShape(), anchorBoxes);
			
			
			return network;
		}
		
		
		return null;

	}

	/**
	 * Run the model with the given spectrogram image input.
	 * 
	 * @param specImage The input spectrogram image as a 4D float array.
	 * @return The results of the model prediction as a DeepAcousticResultArray.
	 */
	public DeepAcousticResultArray runModel(float[][][][] specImage) {
		try {
			DeepAcousticResultArray results  = objectPredictor.predict(specImage);
			//DLUtils.printArray(results);
			return results; 
		} catch (TranslateException e) {
			System.out.println("Error on deep acoustics model: "); 
			e.printStackTrace();
		}
		return null;
	}




}
