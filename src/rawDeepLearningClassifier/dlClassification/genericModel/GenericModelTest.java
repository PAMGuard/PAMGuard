package rawDeepLearningClassifier.dlClassification.genericModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.genericmodel.GenericModel;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

public class GenericModelTest {

	public static void rightWhaleTest() {

		//the model path
		String modelPath = "/Users/au671271/git/PAMGuard_resources/deep_learning/right_whale_tutorial/model_lenet_dropout_input_conv_all/saved_model.pb";

		//the audio file to test
		String wavFilePath = "/Users/au671271/Google Drive/PAMGuard_dev/Deep_Learning/Right_whales_DG/SouthernRightWhale001-v1/sar98_trk3_8000.wav";

		wavFilePath = "/Users/au671271/Google Drive/PAMGuard_dev/Deep_Learning/Right_whales_DG/SouthernRightWhale001-v1/wav_files_timestamp/PAM_20010327_113000.wav";
		wavFilePath = "/Users/au671271/git/PAMGuard_resources/deep_learning/right_whale_tutorial/wav/wav_files_timestamp/PAM_20010327_113200.wav"; 

		//define some bits and pieces we need for the classiifer. 
		float sr = 2000; 
		int startchunk =  (int) (181.2*sr); //right whale call
		//int startchunk =  (int) (190.2*sr); 

		int chunkSize = 4000; 

		AudioData soundData;
		try {
			soundData = DLUtils.loadWavFile(wavFilePath);

			//generic classifier
			GenericModel genericModel = new GenericModel(modelPath);

			//create the transforms. 
			ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

			//waveform transforms. 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE, sr)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.TRIM, startchunk, startchunk+chunkSize)); 
			//			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PREEMPHSIS, preemphases)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECTROGRAM, 256, 100)); 
			//in the python code they have an sfft of 129xN where N is the number of chunks. They then
			//choose fft data between bin 5 and 45 in the FFT. 	This roughly between 40 and 350 Hz. 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECCROPINTERP, 47.0, 357.0, 40)); 
			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECNORMALISEROWSUM)); 



			//open .wav files. 

			//generate the transforms. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(dlTransformParamsArr); 


			((WaveTransform) transforms.get(0)).setWaveData(soundData); 

			DLTransform transform = transforms.get(0); 
			for (int i=0; i<transforms.size(); i++) {
				transform = transforms.get(i).transformData(transform); 
			}


			float[] output = null; 
			float[][][] data;
			for (int i=0; i<10; i++) {
				//long time1 = System.currentTimeMillis();
				data = new float[][][] {DLUtils.toFloatArray(((FreqTransform) transform).getSpecTransfrom().getTransformedData())}; 

				//data = new float[][][] { DLUtils.makeDummySpectrogram(40, 40)}; 

				//System.out.println("data len: " + data.length + " " + data[0].length + " " +  data[0][0].length); 

				output = genericModel.runModel(data); 
				//long time2 = System.currentTimeMillis();
				//System.out.println("Time to run model: " + (time2-time1) + " ms"); 
			}

			double[] prob = new double[output.length]; 
			for (int j=0; j<output.length; j++) {
				//python code for this. 
				//				    	prob = torch.nn.functional.softmax(out).numpy()[n, 1]
				//			                    pred = int(prob >= ARGS.threshold)		    	
				//softmax function
				prob[j] = DLUtils.softmax(output[j], output); 
				System.out.println("The probability is: " + prob[j]); 
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void clickDLTest() {

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
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, 250000.)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PEAK_TRIM, 128, 1)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.NORMALISE_WAV, 0., 1, AudioData.ZSCORE)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 


		//create the clicks. 
		path = Paths.get(clicksPath);
		ArrayList<GroupedRawData> clicks = importClicks(path.toAbsolutePath().normalize().toString(),  SAMPLE_RATE); 

		//prep the model
		genericModelWorker.prepModel(genericModelParams, null);

		System.out.println("Model has loaded"); 

		ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();


		float prediction = 0; 

		for (int i=0; i<clicks.size() ; i++) {
			groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(clicks.get(i)); //TODO for loop

//			System.out.println("Waveform input: " + groupedData.get(i).getRawData().length + " " + groupedData.get(i).getRawData()[0].length + "  " + + groupedData.get(i).getRawData()[0][0]);

			//RUN THE RAW MODEL
//			System.out.println("Min max before: "); 
//			PamArrayUtils.printArray(PamArrayUtils.minmax(groupedData.get(i).getRawData()[0])); 

//			double[] wav = PamArrayUtils.normalise(groupedData.get(i).getRawData()[0]);
//
//			System.out.println("Min max: "); 
//			PamArrayUtils.printArray(PamArrayUtils.minmax(wav));
//			float[][] input1 = new float[][] {PamArrayUtils.double2Float(wav)};
//			float[] output1 = genericModelWorker.getModel().runModel(input1);
//			System.out.println("Output1: " );
//			PamArrayUtils.printArray(output1);

			//RUN THROUGH THE GENERIC MODEL CLASSIIFER. 
			ArrayList<StandardPrediction> genericPrediction = genericModelWorker.runModel(groupedData,SAMPLE_RATE, 0);		

			float[] output = genericPrediction.get(0).getPrediction();

			System.out.println(String.format("Click %d Predicted output: %.6f true output: %.6f passed: %b", clicks.get(i).getUID(),
					output[0], prediction, output[0]>prediction*0.9 && output[0]<prediction*1.1)); 
		}
	}

	/**
	 * Import a bunch of clicks from a .mat file
	 */
	public static ArrayList<GroupedRawData> importClicks(String filePath, float sR) {
		try {
			Mat5File mfr = Mat5.readFromFile(filePath);

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "clickpreds" );

			int numClicks= mlArrayRetrived.getNumCols();
			ArrayList<GroupedRawData> clicks = new ArrayList<GroupedRawData>(numClicks); 

			GroupedRawData clickData;
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

				clickData = new GroupedRawData(clickmillis.getLong(0), channelMap.getInt(0), startSample.getLong(0), sampleDuration.getLong(0), sampleDuration.getInt(0));
				clickData.setUID(clickUID.getLong(0));
				clickData.setRawData(clickwaveform);
				
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



	/**
	 * The bat Pytorch test. 
	 */
	public static void batPyTorchTest() {


	}

	public static void main(String args[]) {
		//		rightWhaleTest();
		clickDLTest();
	}

}
