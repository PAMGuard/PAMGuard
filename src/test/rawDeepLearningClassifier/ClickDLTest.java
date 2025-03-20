package test.rawDeepLearningClassifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


/**
 * Model from Thomas webber which is a good way to test the click based stuff is working in PAMGUard.
 */
public class ClickDLTest {

	/**
	 * Test just one click
	 */
	@Test
	public void aclickDLTest() {

		System.out.println("CLickDLTest: Single click test");

		float SAMPLE_RATE = 500000;
		//relative paths to the resource folders.
		System.out.println("*****Click classification Deep Learning C*****"); 

		//relative paths to the resource folders.		
		String relModelPath  =	"/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/model_pb/saved_model.pb";
		String clicksPath  =	"/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/model_v2/example_2000021.mat";

		//load the click up

		// load the click data up.
		Path clkPath = Paths.get(clicksPath);
		PredGroupedRawData clickData = null;
		try {
			Mat5File mfr = Mat5.readFromFile(clkPath.toAbsolutePath().normalize().toString());

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "newStruct" );


			Matrix clickWavM = mlArrayRetrived.get("wave", 0);
			Matrix modelInputM= mlArrayRetrived.get("wave_pad", 0);

			double[][] clickWaveform= PamArrayUtils.matrix2array(clickWavM);

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
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, 248000.)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PEAK_TRIM, 64, 1)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.NORMALISE_WAV, 0., 1, AudioData.ZSCORE)); 

			genericModelParams.dlTransfromParams = dlTransformParamsArr;
			genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 

			//create the clicks. 
			path = Paths.get(clicksPath);

			//prep the model
			genericModelWorker.prepModel(genericModelParams, null);

			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(clickData);

			System.out.println("Waveform input: " + groupedData.get(0).getRawData().length + " " + groupedData.get(0).getRawData()[0].length);

			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,SAMPLE_RATE, 0);		

			System.out.println("PAMGuard input len: " + pythonModelInputF.length); 
			
			float[] outputPAMGuard = genericPrediction.get(0).getPrediction();

			System.out.println("Model output PAMGuard: " + outputPAMGuard[0]);

			//RUN THE RAW MODEL with Python transformed input

			System.out.println("Python input len: " + pythonModelInputF.length); 
			float[] outPutPython = genericModelWorker.getModel().runModel(new float[][] {pythonModelInputF});


			System.out.println("Model output Python: " + outPutPython[0]);

		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return; 
		}
	}



	@Test
	public void clicksDLTest() {

		float SAMPLE_RATE = 500000;
		//relative paths to the resource folders.
		System.out.println("*****Click classification Deep Learning C*****"); 

		//relative paths to the resource folders.		
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/risso_click/uniform_model/saved_model.pb";
		String clicksPath  =	"./src/test/resources/rawDeepLearningClassifier/Generic/risso_click/clicks.mat";

		Path path = Paths.get(relModelPath);

		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 
		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();


		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//waveform transforms. 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, 248000.)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PEAK_TRIM, 128, 1)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.NORMALISE_WAV, 0., 1, AudioData.ZSCORE)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 

		//create the clicks. 
		path = Paths.get(clicksPath);
		ArrayList<PredGroupedRawData> clicks = importClicks(path.toAbsolutePath().normalize().toString(),  SAMPLE_RATE); 

		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);

		System.out.println("Model has loaded"); 

		ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();

		for (int i=0; i<1; i++) {

			float prediction = (float) clicks.get(i).getPrediction()[0]; 

			groupedData.add(clicks.get(i)); //TODO for loop

			//System.out.println("Waveform input: " + groupedData.get(i).getRawData().length + " " + groupedData.get(i).getRawData()[0].length);

			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,SAMPLE_RATE, 0);		

			float[] output = genericPrediction.get(i).getPrediction();

			System.out.println(String.format("Click %d Predicted output: %.2f true output: %.2f passed: %b", clicks.get(i).getUID(),
					output[0], prediction, output[0]>prediction*0.9 && output[0]<prediction*1.1)); 

		}


	}

	/**
	 * Import a bunch of clicks from a .mat file
	 */
	public static ArrayList<PredGroupedRawData> importClicks(String filePath, float sR) {

		try {
			Mat5File mfr = Mat5.readFromFile(filePath);

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "clickpreds" );

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
				clickData.setPrediction(new double[] {pred.getDouble(0), pred.getDouble(1)});

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
