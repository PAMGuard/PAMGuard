package test.rawDeepLearningClassifier;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


/**
 * Model from Thomas webber which is a good way to test the click based stuff is working in PAMGUard.
 */
public class ClickDLTest {
	
	/**
	 * Test just one click using the zipped classifier
	 * @throws  
	 */
	@Test
	public void aclickDLTestZip()   {

		System.out.println("*****CLickDLTest: Single click test zip*****");
		
		//relative paths to the resource folders.		
		String relModelPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/model_pb.zip";
		String clicksPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/example_2000021.mat";
		
//		String matout  =	"/home/jamiemac/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/generic_classifier/example_2000021_transforms.mat";
		String matout=null;
		// load the click data up.
		Path clkPath = Paths.get(clicksPath);
		PredGroupedRawData clickData = null;
		
		Struct matclkStruct = Mat5.newStruct();
		try {
			Mat5File mfr = Mat5.readFromFile(clkPath.toAbsolutePath().normalize().toString());

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "newStruct" );
			
			Matrix clickWavM = mlArrayRetrived.get("wave", 0);

			double[][] clickWaveform= PamArrayUtils.matrix2array(clickWavM);
			clickWaveform=PamArrayUtils.transposeMatrix(clickWaveform);

			Matrix clickUID= mlArrayRetrived.get("UID", 0);
			Matrix pred= mlArrayRetrived.get("pred", 0);

			//create a click object whihc we can pass through transforms etc. 
			clickData = new PredGroupedRawData(0L, 1, 0, clickWaveform[0].length, clickWaveform[0].length);
			clickData.setUID(clickUID.getLong(0));
			clickData.setRawData(clickWaveform);
			clickData.setPrediction(new double[] {pred.getDouble(0)});

			// load the model up
			Path path = Paths.get(relModelPath);

			ArchiveModelWorker genericModelWorker = new ArchiveModelWorker(); 

			StandardModelParams genericModelParams = new StandardModelParams(); 
			genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

			//prep the model - all setting are included within the model
			genericModelWorker.prepModel(genericModelParams, null);
			System.out.println("seglen: " +  genericModelParams.defaultSegmentLen);
			
			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(clickData);

			System.out.println("Waveform input: " + groupedData.get(0).getRawData().length + " " + groupedData.get(0).getRawData()[0].length);

			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,96000, 0);	
			
			float[] outputPAMGuard = genericPrediction.get(0).getPrediction();

			System.out.println("Model output PAMGuard: " + outputPAMGuard[0]);
			assertEquals(outputPAMGuard[0], 0.99, 0.05);
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false); //make sure the unit test fails
			return; 
		}
	}

	/**
	 * Test just one click
	 * @throws  
	 */
	@Test
	public void aclickDLTest()   {

		System.out.println("*****CLickDLTest: Single click test*****");

		//relative paths to the resource folders.
		System.out.println("*****Click classification Deep Learning C*****"); 

//		//relative paths to the resource folders.		
//		String relModelPath  =	"/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/model_pb/saved_model.pb";
//		String clicksPath  =	"/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/example_2000021.mat";
		
		//relative paths to the resource folders.		
		String relModelPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/model_pb/saved_model.pb";
		String clicksPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/example_2000021.mat";
		//load the click up

//		String matout  =	"/home/jamiemac/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/generic_classifier/example_2000021_transforms.mat";
		String matout=null;
		// load the click data up.
		Path clkPath = Paths.get(clicksPath);
		PredGroupedRawData clickData = null;
		
		Struct matclkStruct = Mat5.newStruct();
		try {
			Mat5File mfr = Mat5.readFromFile(clkPath.toAbsolutePath().normalize().toString());

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "newStruct" );


			Matrix clickWavM = mlArrayRetrived.get("wave", 0);
			Matrix modelInputM= mlArrayRetrived.get("wave_pad", 0);

			double[][] clickWaveform= PamArrayUtils.matrix2array(clickWavM);
			clickWaveform=PamArrayUtils.transposeMatrix(clickWaveform);

			//get the raw model input so we can test the model directly. 
			double[][] pythonModelInput= PamArrayUtils.matrix2array(modelInputM);
			pythonModelInput = PamArrayUtils.transposeMatrix(pythonModelInput);
			float[] pythonModelInputF = PamArrayUtils.double2Float(pythonModelInput[0]);

			Matrix clickUID= mlArrayRetrived.get("UID", 0);
			Matrix pred= mlArrayRetrived.get("pred", 0);

			//create a click object whihc we can pass through transforms etc. 
			clickData = new PredGroupedRawData(0L, 1, 0, clickWaveform[0].length, clickWaveform[0].length);
			clickData.setUID(clickUID.getLong(0));
			clickData.setRawData(clickWaveform);
			clickData.setPrediction(new double[] {pred.getDouble(0)});


			// load the model up
			Path path = Paths.get(relModelPath);

			GenericModelWorker genericModelWorker = new GenericModelWorker(); 

			GenericModelParams genericModelParams = new GenericModelParams(); 
			genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();


			//create the transforms. 
			ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

			//waveform transforms. 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, 96000.)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.NORMALISE_WAV, 0., 1, AudioData.ZSCORE)); //needs to be here
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PEAK_TRIM, 64, 1)); 
			
			genericModelParams.dlTransfromParams = dlTransformParamsArr;
			genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 
		
			//create the clicks. 
			path = Paths.get(clicksPath);

			//prep the model
			genericModelWorker.prepModel(genericModelParams, null);

			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(clickData);

			System.out.println("Waveform input: " + groupedData.get(0).getRawData().length + " " + groupedData.get(0).getRawData()[0].length);

			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,96000, 0);		

//			System.out.println("PAMGuard input len: " + pythonModelInputF.length); 
			
			float[] outputPAMGuard = genericPrediction.get(0).getPrediction();

			System.out.println("Model output PAMGuard: " + outputPAMGuard[0]);
			
			//run the transforms so we can take a look at the inpout
			((WaveTransform) genericModelParams.dlTransfroms.get(0)).setWaveData(new AudioData(groupedData.get(0).getRawData()[0], 248000));;
			//create the transformed wave
			DLTransform transform = genericModelParams.dlTransfroms.get(0); 
			double[] audioOut = null;
			for (int i=0; i<genericModelParams.dlTransfroms .size(); i++) {
				transform = genericModelParams.dlTransfroms.get(i).transformData(transform); 
				audioOut = ((WaveTransform)  transform).getWaveData().getScaledSampleAmplitudes(); 
				matclkStruct.set(transform.getDLTransformType().getJSONString(), DLMatFile.array2Matrix(audioOut));
			}

			//RUN THE RAW MODEL with Python transformed input

//			System.out.println("Python input len: " + pythonModelInputF.length); 
//			float[] outPutPython = genericModelWorker.getModel().runModel(new float[][] {PamArrayUtils.double2Float(audioOut)});

			float[] outPutPython = genericModelWorker.getModel().runModel(new float[][] {pythonModelInputF});

			System.out.println("Model output Python: " + outPutPython[0]);
			
			assertEquals(outputPAMGuard[0], outPutPython[0], 0.05);

		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false); //make sure the unit test fails
			return; 
		}
		
		if (matout!=null) {
			// Create MAT file with a scalar in a nested struct
			MatFile matFile = Mat5.newMatFile()
			    .addArray("click_transforms", matclkStruct); 
			// Serialize to disk using default configurations
			try {
				Mat5.writeToFile(matFile, matout);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	@Test
	public void clicksDLTest() {

		float SAMPLE_RATE = 96000;
		//relative paths to the resource folders.
		System.out.println("*****CLickDLTest: Clicks test*****");

		//relative paths to the resource folders.		
		String relModelPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/model_pb/saved_model.pb";
		String clicksPath  =	"/home/jamiemac/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/Click_Detector_Click_Detector_Clicks_20220603_111000_classified.mat";

		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 
		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();


		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//waveform transforms. 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, 96000.)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.NORMALISE_WAV, 0., 1, AudioData.ZSCORE)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PEAK_TRIM, 64, 1)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 

		//create the clicks. 
		path = Paths.get(clicksPath);
		ArrayList<PredGroupedRawData> clicks = importClicks(path.toAbsolutePath().normalize().toString(), SAMPLE_RATE); 

		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);

		System.out.println("Model has loaded: n clicks " + clicks.size()); 

		float count = 0; 
		long timeStart = System.currentTimeMillis();
		for (int i=0; i<clicks.size(); i++) {

			float prediction = (float) clicks.get(i).getPrediction()[0]; 
			
			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(clicks.get(i)); //TODO for loop

			//System.out.println("Waveform input: " + groupedData.get(i).getRawData().length + " " + groupedData.get(i).getRawData()[0].length);

			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,SAMPLE_RATE, 0);		

			float[] output = genericPrediction.get(0).getPrediction();

			System.out.println(String.format("Click %d Predicted output: %.4f true output: %.4f passed: %b  delta %.2f", clicks.get(i).getUID(),
					output[0], prediction, output[0]>prediction*0.9 && output[0]<prediction*1.1, (Math.abs(output[0] -prediction)))); 
			
			if (output[0]>prediction*0.9 && output[0]<prediction*1.1) {
				count++;
			}

		}
		long timeEnd = System.currentTimeMillis();

		double perctrue = count/clicks.size();

		System.out.println(String.format("Percentage clicks passed: %.2f TIme to process %d clicks - %2f seconds", perctrue, clicks.size(), ((double) (timeEnd-timeStart))/1000.)); 
	}

	/**
	 * Import a bunch of clicks from a .mat file
	 */
	public static ArrayList<PredGroupedRawData> importClicks(String filePath, float sR) {

		try {
			Mat5File mfr = Mat5.readFromFile(filePath);

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "binarydata" );

			int numClicks= mlArrayRetrived.getNumCols();
			ArrayList<PredGroupedRawData> clicks = new ArrayList<PredGroupedRawData>(numClicks); 

			PredGroupedRawData clickData;
			for (int i=0; i<numClicks; i++) {
				Matrix clickWav= mlArrayRetrived.get("wave", i);

				double[][] clickwaveform= PamArrayUtils.matrix2array(clickWav);

				clickwaveform = PamArrayUtils.transposeMatrix(clickwaveform);
				//System.out.println("click: " + click[0].length + " num: " + numClicks);

				Matrix clickUID= mlArrayRetrived.get("UID", i);
				Matrix clickmillis= mlArrayRetrived.get("millis", i);
				Matrix channelMap= mlArrayRetrived.get("channelMap", i);
				Matrix startSample= mlArrayRetrived.get("startSample", i);
				Matrix sampleDuration= mlArrayRetrived.get("sampleDuration", i);
				Matrix pred= mlArrayRetrived.get("pred", i);

				clickData = new PredGroupedRawData(clickmillis.getLong(0), channelMap.getInt(0), startSample.getLong(0), sampleDuration.getLong(0), sampleDuration.getInt(0));
				clickData.setUID(clickUID.getLong(0));
				clickData.setRawData(clickwaveform);
				clickData.setPrediction(new double[] {pred.getDouble(0)});

				clicks.add(clickData); 
			}

			return clicks; 
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; 
		}
	}

	public static class PredGroupedRawData extends GroupedRawData {

		private double[] prediction;

		public double[] getPrediction() {
			return prediction;
		}

		public void setPrediction(double[] prediction) {
			this.prediction = prediction;
		}

		public PredGroupedRawData(long timeMilliseconds, int channelBitmap, long startSample, long duration, int samplesize) {
			super(timeMilliseconds,  channelBitmap,  startSample,  duration,  samplesize);
		}



	}


}
