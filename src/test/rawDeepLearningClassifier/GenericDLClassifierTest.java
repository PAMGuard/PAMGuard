package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.defaultModels.RightWhaleModel1;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;

/**
 * Test the generic classifier.
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericDLClassifierTest {

	/**
	 * Run a test on the Generic DL Classifier. This tests the worker can open and run a model 
	 */
	@Test
	public void rightWhaleDLWorker() {
		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/right_whale/model_lenet_dropout_input_conv_all/saved_model.pb";
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/right_whale/right_whale_example.wav";

		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 

		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

		RightWhaleModel1 rightWhaleModel = new RightWhaleModel1();
		rightWhaleModel.setParams(genericModelParams);

		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);

		/****Now run a file ***/
		path = Paths.get(relWavPath);
		String wavFilePath = path.toAbsolutePath().normalize().toString();

		AudioData soundData;
		try {
			soundData = DLUtils.loadWavFile(wavFilePath);

			long duration = (long) ((genericModelParams.defaultSegmentLen/1000)*soundData.sampleRate);

			GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, duration, (int) duration);
			groupedRawData.copyRawData(soundData.getScaledSampleAmplitudes(), 0, soundData.getScaledSampleAmplitudes().length, 0);
			
			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			
			
			groupedData.add(groupedRawData);

			ArrayList<GenericPrediction> gwenericPrediciton = genericModelWorker.runModel(groupedData, soundData.sampleRate, 0);		
			
			float[] output = gwenericPrediciton.get(0).getPrediction();
			
			System.out.println("Right whale network output: " + output[0] + "  " + output[1]);
			
			//test the predicitons are true. 
			assertTrue( output[0]<0.01 ); //noise
			assertTrue( output[1]>0.98 ); //right whale

			genericModelWorker.closeModel();

		} catch (IOException | UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals(false, true);
		}
	}
	
	
	
	/**
	 * Test the ketos classifier and tests are working properly. This tests loading the ketos model and also using
	 * functions in KetosWorker.
	 */
	@Test
	public void humpbackWhaleTest() {
		
		
	}
	

}
