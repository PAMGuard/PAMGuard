package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.defaultModels.HumpbackWhaleAtlantic;
import rawDeepLearningClassifier.defaultModels.RightWhaleModel1;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Test the generic classifier.
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericDLClassifierTest {

	/**
	 * Run a test on the Generic DL Classifier. This tests the worker can open and
	 * run a model
	 */
	@Test
	public void rightWhaleDLWorker() {
		System.out.println("*****Generic DL: Right whale test*****"); 

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
	 * Test Google's humpback whale model. 
	 */
	@Test
	public void humpbackWhaleTest() {

		System.out.println("*****Generic DL: Humpback whale test*****"); 

		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/humpback_whale_atlantic/FlatHBNA/saved_model.pb";
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/humpback_whale_atlantic/SAMOSAS_EL1_humpback.wav";
		String resultsPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/humpback_whale_atlantic/SAMOSAS_EL1_humpback_annotations.txt";

		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 

		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

		path = Paths.get(relWavPath);
		String wavFilePath = path.toAbsolutePath().normalize().toString();

		HumpbackWhaleAtlantic humpbackModel = new HumpbackWhaleAtlantic();
		humpbackModel.setParams(genericModelParams);
		
		System.out.println(genericModelParams);
		
		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);

		try {
			//load audio
			AudioData soundData = DLUtils.loadWavFile(wavFilePath);

			
			//load true predictions file. 
			File file = new File(resultsPath);
			FileReader fr = new FileReader(file);	
			BufferedReader br = new BufferedReader(fr);
			String line;
			int ind=0; 
			
			while((line = br.readLine()) != null){
				if (ind>0) {
					//read the data from the text file
					String[] data = line.split("\t");
					int chunkID = Integer.valueOf(data[0]);

					double startTimeS = Double.valueOf(data[1]);
					double endTimeS = Double.valueOf(data[2]);
					double prediction = Double.valueOf(data[5]);

					int startChunk = (int) (soundData.sampleRate*startTimeS); //humpback whale call
					int chunkSize = (int) Math.ceil((endTimeS-startTimeS)*soundData.sampleRate); 
					
					chunkSize=7755;

					GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, chunkSize, chunkSize);
					groupedRawData.copyRawData(soundData.getScaledSampleAmplitudes(), startChunk, chunkSize, 0);
					

					ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();

					groupedData.add(groupedRawData);

					ArrayList<GenericPrediction> genericPrediction = genericModelWorker.runModel(groupedData, soundData.sampleRate, 0);		

					float[] output = genericPrediction.get(0).getPrediction();

					System.out.println(String.format("Chunk %d %d Predicted output: %.2f true output: %.2f passed: %b", chunkID, startChunk,
							output[0], prediction, output[0]>prediction*0.9 && output[0]<prediction*1.1)); 

					//allow 10% scrumph to take account of slight differences in Java input. 
					assertEquals(output[0], prediction, 0.1); //humpback whale detection
				}
				  ind++;
				}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
