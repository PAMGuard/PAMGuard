package test.rawDeepLearningClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.defaultModels.MultiSpeciesGoogle;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


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
	 * Test google's multi-species model against outputs obtained in Python.  
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
				//each line is a list of prediction for each class; 
				int chunkSize=120000;
	
				int classIndex = 1; 
				while((line = br.readLine()) != null){
					if (ind>0) {
						//read the data from the text file
						String[] data = line.split(",");
	
						double[] predictions = new double[data.length]; 
						for (int i=0; i<data.length; i++) {
							predictions[i] = Double.valueOf(data[i]); 
						}
	
	
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



	/**
	 * Test Google'smulti species model by feeding it two wav files, one which is upsampled to 24kHz and another which needs upsampled by the transforms 
	 * to 24kHz. Verify that the upsampling works
	 */
	@Test
	public void multiSpeciesGoogleTest2() {

		System.out.println("*****Generic DL: google-multi-species test 2*****"); 

		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/multispecies-whale-tensorflow2-default-v2/saved_model.pb";

		//path to the same file at different sample rates
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/NOPP6_EST_20090329_121500.wav";
		String relWavPath2  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/NOPP6_EST_20090329_121500_upsample.wav";
//		String relWavPath2  =	"./src/test/resources/rawDeepLearningClassifier/Generic/multi-species-Google/NOPP6_EST_20090329_121500.wav";

		String matFileOut = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/google_multi_species/google_multi_species.mat"; 


		//hold the paths in an array
		String[] soundFiles = new String[] {relWavPath, relWavPath2};


		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 

		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

		MultiSpeciesGoogle multiSpeciesGoogle = new MultiSpeciesGoogle();
		multiSpeciesGoogle.setParams(genericModelParams);
		genericModelParams.dlTransfromParams.set(0, new SimpleTransformParams(DLTransformType.DECIMATE, 24000.)); 

		System.out.println(genericModelParams);

		double segSize = 5.; //one second hop size.
		double segHop = 5.; //one second hop size.
		int classIndex = 5; //Right whale atlantic - jus for output
		
		//create MatFile for saving the image data to. 
		MatFile matFile = Mat5.newMatFile();



		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);
		
		ArrayList<float[][]> fileOutputs = new ArrayList<float[][]>();
		for (int i=0; i<soundFiles.length; i++) {
			try {

				path = Paths.get(soundFiles[i]);
				String wavFilePath = path.toAbsolutePath().normalize().toString();

				//load audio
				AudioData soundData = DLUtils.loadWavFile(wavFilePath);
//				if (i==1) {
//					soundData=soundData.interpolate(24000);
//				}

				int nseg =  (int) (soundData.samples.length/(segHop*soundData.sampleRate)); 
				float[][] outputs = new float[nseg][]; 

				int  startChunk=0; 
				
				//initialise strcuture for image data
				Struct waveStruct = Mat5.newStruct(nseg, 1);

				//each line is a list of prediction for each class; 
				int chunkSize=(int) (segSize*soundData.sampleRate);
				int ind = 0;
				
				System.out.println("Generic DL: google-multi-species test: processing file: " + i + " chunkSize: " + chunkSize + " nseg " + nseg); 

				while(startChunk<(soundData.samples.length-chunkSize)){

					GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, chunkSize, chunkSize);
					groupedRawData.copyRawData(soundData.getScaledSampleAmplitudes(), startChunk, chunkSize, 0);

					//System.out.println("MAX AMPLITUDE: " + PamArrayUtils.max(groupedRawData.getRawData()[0])); 
					ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();

					groupedData.add(groupedRawData);

					ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData, soundData.sampleRate, 0);	
					float[] output = genericPrediction.get(0).getPrediction();
					
					
					//----TEST output for MATLAB----
					float[][][] dataOut = genericModelWorker.dataUnits2ModelInput(groupedData, soundData.sampleRate, 0);	
					float[] waveIn = dataOut[0][0]; 

					Matrix modelinput = DLMatFile.array2Matrix(PamArrayUtils.float2Double(waveIn));
					Matrix modeloutput = DLMatFile.array2Matrix(PamArrayUtils.float2Double(output));

					waveStruct.set("modelinput", ind, modelinput);
					waveStruct.set("startseconds", ind, Mat5.newScalar(startChunk/soundData.sampleRate));
					waveStruct.set("prediction", ind, modeloutput);

					//					System.out.println(String.format("File %d Chunk %d %d Predicted output: %.5f ", i, ind, startChunk,
					//					output[classIndex])); 

					outputs[ind] = output;
					ind++;

					//PamArrayUtils.printArray(output);
					startChunk+=(int) (segHop*soundData.sampleRate); //one second step
				}
				
				matFile.addArray(("file_" + i + "_outputs"), waveStruct);
				matFile.addArray(("file_" + i), Mat5.newString(wavFilePath));

				fileOutputs.add(outputs);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		//now compare the outputer
		for (int i=0; i<fileOutputs.get(0).length; i++) {
			if (fileOutputs.get(0)[i]==null) continue;
			System.out.println(String.format("Chunk %d File 0 - %.3f File - 1 %.3f diff %.3f", i, 
					fileOutputs.get(0)[i][classIndex], fileOutputs.get(1)[i][classIndex],
					(Math.abs(fileOutputs.get(0)[i][classIndex]-fileOutputs.get(1)[i][classIndex])))); 
		}
		

		if (matFileOut!=null) {
			System.out.println("Writing mat file");
			// Serialize to disk using default configurations
			try {
				Mat5.writeToFile(matFile,matFileOut);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}
