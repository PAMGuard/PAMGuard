package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;
import java.io.IOException;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsTranslator;
import org.jamdev.jdl4pam.deepAcoustics.Pred2BoxDJL3.DeepAcousticsNetwork;

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
	public DeepAcousticsModel(File file, DeepAcousticsNetwork network) throws MalformedModelException, IOException, EngineException {
		super(file);

		deepAcousticsTranslator = new DeepAcousticsTranslator(network); 

		//predictor for the model if using images as input
		objectPredictor = getModel().newPredictor(deepAcousticsTranslator);

	}
	
	
	
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
