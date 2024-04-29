package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jamdev.jdl4pam.ketos.KetosModel;
import org.jamdev.jdl4pam.ketos.KetosParams;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.jamdev.jpamutils.JamArr;


/**
 * Test the Ketos models in PAMGuard. 
 * <p>
 * Note a similar class also exists in the JPAM repository but this test ensure PAMGuard dependencies are up to date 
 * etc. 
 * 
 * @author Jamie Macaulay
 *
 */
public class KetosModelTest {
	
	public static void main(String[] args) {

		//test on a right whale. 
//		File file = new File("/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/Deep_Learning/Ketos/right_whales/for_pamguard/narw.ktpb"); 
//		File file = new File("/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/Deep_Learning/Meridian/humpback_whales/SOCAL_Mn_Network.ktpb");
		//File file = new File("/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/Deep_Learning/Meridian/orca/kw_detector_v11_5s.ktpb"); 

		//the wav file to test.
//		String wavFilePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/Deep_Learning/Ketos/right_whales/for_pamguard/input.wav"; 
//		String wavFilePath = "/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/Deep_Learning/Meridian/humpback_whales/wav/5353.210403161502.wav";
//		double[] window = new double[]{0., 3.0}; 
		
//		//Minke model
//		File file = new File("/Users/au671271/Desktop/Minke_test/Minke_Network_12s.ktpb");
//		String wavFilePath = "/Users/au671271/Desktop/Minke_test/1705_FLAC_1705_20171106_185953_253.wav";
//		double windowSize = 12; 
		
//		
		File file = new File("/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/Deep_Learning/Ketos/narw_2/hallo-kw-det_v1_test/hallo-kw-det_v1.ktpb");
		String wavFilePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/Deep_Learning/Ketos/narw_2/hallo-kw-det_v1_test/audio/jasco_reduced.wav";
//		double[] window = new double[]{10., 15.0176}; 
		double[] window = new double[]{45, 50.0176}; 

		
		try {
			//the ketos model. 
			KetosModel  ketosModel = new KetosModel(file);

			//read the JSOn string from the the file. 
			String jsonString  = DLTransformsParser.readJSONString(new File(ketosModel.getAudioReprFile()));

			//get the audio representation file. 
			KetosParams ketosParams = new KetosParams(jsonString); 			
			ketosParams.defaultOutputShape = ketosModel.getOutShape();

			//System.out.println(ketosParams.toString());
			System.out.println("Output shape" + ketosParams.defaultOutputShape);
			
			System.out.println("Input shape" + ketosParams.defaultInputShape);
			
			
			//28-04-2023 seems like there is a BUG in ketos where the input shape reported by the model is incorrect. 
			ketosModel.setInputShape(ketosParams.defaultInputShape); 


			//Open wav files. 
			AudioData soundData = DLUtils.loadWavFile(wavFilePath);
			soundData = soundData.trim((int) (soundData.getSampleRate()*window[0]), (int) (soundData.getSampleRate()*window[1])); 
			System.out.println("Input sample rate is " + soundData.getSampleRate());
			
					
			//generate the transforms. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(ketosParams.dlTransforms); 
			
			
			((WaveTransform) transforms.get(0)).setWaveData(soundData); 
			
			DLTransform transform = transforms.get(0); 
			for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
				
//				try {
//				System.out.println("Transform: " +  ketosParams.dlTransforms.get(i));
//				}
//				catch(Exception e) {
//					e.printStackTrace();
//				}

				transform = transforms.get(i).transformData(transform); 
				
				//				if (i==1) {
				//					 transfromedData =  DLMatFile.array2Matrix(((FreqTransform) transform).getSpecTransfrom().getTransformedData());
				//				}
				//				if (transforms.get(i) instanceof FreqTransform) {
				//				transformedData = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
				//					System.out.println("Transform shape: " + i + " " +  transformedData.length + " " + transformedData[0].length); 
				//				}
			}

			double[][] transformedData = ((FreqTransform) transform).getSpecTransfrom().getTransformedData();


			float[] output = null; 
			float[][][] data;
			int nStack = 1; //number of specs to give to the classifier. 
			for (int i=0; i<10; i++) {
				long time1 = System.currentTimeMillis();
				data = new float[nStack][][]; 
				for (int j=0; j<nStack; j++) {
					data[j] = DLUtils.toFloatArray(transformedData); 
				}
				System.out.println("Input len: " + data.length + "  " + data[0].length + "  " + data[0][0].length); 
				output = ketosModel.runModel(data); 
				long time2 = System.currentTimeMillis();
				System.out.println("Time to run model: " + (time2-time1) + " ms"); 
			}



			double[] prob = new double[output.length]; 
			for (int j=0; j<output.length; j++) {
				//python code for this. 
				//		    	prob = torch.nn.functional.softmax(out).numpy()[n, 1]
				//	                    pred = int(prob >= ARGS.threshold)		    	
				//softmax function
				System.out.println("The output is: " + output[j]); 

				prob[j] = DLUtils.softmax(output[j], output); 
				System.out.println("The probability is: " + prob[j]); 
			}



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


}

