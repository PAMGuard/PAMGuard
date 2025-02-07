package test.rawDeepLearningClassifier;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.genericmodel.GenericModel;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.defaultModels.HumpbackWhaleAtlantic;
import rawDeepLearningClassifier.defaultModels.MultiSpeciesGoogle;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;


/**
 * Test models that are zipped with a PMAGuard pgdl settings file. 
 */
public class PamZipDLClassifierTest {

	/**
	 * Test the koogu classifier and tests are working properly for a PAMGuard zip model - i.e. this is a very similar model to Koogu but zipped with a .zip 
	 * filename instead of .kgu. 
	 */
	//	@Test
	//	public void blueWhaleKooguTest() {
	//		//relative paths to the resource folders.
	//		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/PamZip/blue_whale_24.zip";
	//		
	//		//the zip classifier is the same as the 
	//		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/20190527_190000.wav";
	//		String relMatPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/rawScores_20190527_190000.mat";
	//		
	//		//metadata says it should be used with Koogu classifier.
	//		KooguDLClassifierTest.runKooguClassifier( relModelPath,  relWavPath,  relMatPath);
	//	}



	/**
	 * Test Google's humpback whale model. 
	 */
	@Test
	public void multiSpeciesGoogleTest() {

		System.out.println("*****Generic DL: Humpback whale test*****"); 

		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/multispecies-whale-tensorflow2-default-v2/saved_model.pb";
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/Cross_24kHz.wav";
		String resultsPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/Cross_24kHz_scores.csv";

		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 

		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

		path = Paths.get(relWavPath);
		String wavFilePath = path.toAbsolutePath().normalize().toString();

		MultiSpeciesGoogle multiSpeciesGoogle = new MultiSpeciesGoogle();
		multiSpeciesGoogle.setParams(genericModelParams);

		System.out.println(genericModelParams);


		//TEMP 



		try {
			//load audio
			AudioData soundData = DLUtils.loadWavFile(wavFilePath);
			
//			double[] amplitudes = soundData.getScaledSampleAmplitudes();
//
//
//			float[] ampltiudesf = PamArrayUtils.double2Float(amplitudes);
//			float[] ampltiudesfchunk = new float[120000]; 
//			int offset = 24000; 
//			System.arraycopy(ampltiudesf, 0+offset, ampltiudesfchunk, 0, 120000);
//
//			//			System.out.println("MAX AMPLITUDE: " + PamArrayUtils.max(amplitudes)); 
//
//			GenericModel genericModel  = new GenericModel( genericModelParams.modelPath) ;
//			float[][] input = new float[][]{ampltiudesfchunk}; 
//			float[] output2 = genericModel.runModel(input); 
//
//			System.out.println("----TEST OUT----"); 
//			PamArrayUtils.printArray(output2);

			//prep the model
			genericModelWorker.prepModel(genericModelParams, null);


			//load true predictions file. 
			File file = new File(resultsPath);
			FileReader fr = new FileReader(file);	
			BufferedReader br = new BufferedReader(fr);
			String line;
			int ind=0; 

			int  startChunk=0; 

			int classIndex = 1; 
			while((line = br.readLine()) != null){
				if (ind>0) {
					//read the data from the text file
					String[] data = line.split(",");

					double[] predictions = new double[data.length]; 
					for (int i=0; i<data.length; i++) {
						predictions[i] = Double.valueOf(data[i]); 
					}

					//each line is a list of prediction for each class; 
					int chunkSize=120000;

					GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, chunkSize, chunkSize);
					groupedRawData.copyRawData(soundData.getScaledSampleAmplitudes(), startChunk, chunkSize, 0);

					//					System.out.println("MAX AMPLITUDE: " + PamArrayUtils.max(groupedRawData.getRawData()[0])); 

					ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();

					groupedData.add(groupedRawData);

					ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData, soundData.sampleRate, 0);		

					float[] output = genericPrediction.get(0).getPrediction();

					boolean passed  = (output[classIndex]>predictions[classIndex]-0.05 && output[classIndex]<predictions[classIndex]+0.05); 

					System.out.println(String.format("Chunk %d %d Predicted output: %.5f true output: %.5f passed: %b -- sum %.2f ", ind, startChunk,
							output[classIndex], predictions[classIndex], passed, PamArrayUtils.sum(output))); 

					//PamArrayUtils.printArray(output);

					//allow 10% scrumph to take account of slight differences in Java input. 
					//assertTrue(passed); //humpback whale detection

					startChunk+=24000; //one second step

				}
				ind++;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
