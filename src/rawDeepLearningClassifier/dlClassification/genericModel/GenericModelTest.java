package rawDeepLearningClassifier.dlClassification.genericModel;

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

	/**
	 * The bat Pytorch test. 
	 */
	public static void batPyTorchTest() {


	}

	public static void main(String args[]) {
		rightWhaleTest();
	}

}
