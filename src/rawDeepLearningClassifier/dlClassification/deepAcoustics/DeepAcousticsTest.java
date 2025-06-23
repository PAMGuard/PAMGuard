package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;
import java.io.IOException;

import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;

/**
 * A test class for the Deep Acoustics model.
 */
public class DeepAcousticsTest {
	
	public static void main(String[] args) {
		// This is a placeholder for the main method.
		// You can implement tests for the DeepAcousticsModel here.
		
		System.out.println("Deep Acoustics Test is running.");
		
		// Example: Create an instance of DeepAcousticsModel and run tests.
		// Note: You will need to provide a valid model file and network configuration.
		
		 File modelFile = new File("/Users/jdjm/Dropbox/PAMGuard_dev/Deep_Learning/deepAcoustics/ModelExports/Test_TFSavedModel_DarkNet_250404.zip");
		// DeepAcousticsNetwork network = new DeepAcousticsNetwork();
		// DeepAcousticsModel model = new DeepAcousticsModel(modelFile, network);
		
		// Add your test logic here.
		 try {
			DeepAcousticsModel model = new DeepAcousticsModel(modelFile);
			
			
			
			System.out.println("Deep Acoustics Model loaded successfully.");
		} catch (MalformedModelException | EngineException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}

}
