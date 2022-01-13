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
		//File file = new File("/Volumes/GoogleDrive/My Drive/PAMGuard_dev/Deep_Learning/Meridian/right_whales/for_pamguard/narw.ktpb"); 
		File file = new File("/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/Deep_Learning/Meridian/humpback_whales/SOCAL_Mn_Network.ktpb");
		//File file = new File("/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/Deep_Learning/Meridian/orca/kw_detector_v11_5s.ktpb"); 

		//the wav file to test.
		String wavFilePath = "/Volumes/GoogleDrive/My Drive/PAMGuard_dev/Deep_Learning/Meridian/right_whales/for_pamguard/input.wav"; 

		try {
			//the ketos model. 
			KetosModel  ketosModel = new KetosModel(file);

			//read the JSOn string from the the file. 
			String jsonString  = DLTransformsParser.readJSONString(new File(ketosModel.getAudioReprFile()));

			//get the audio representation file. 
			KetosParams ketosParams = new KetosParams(jsonString); 			

			//System.out.println(ketosParams.toString());

			//Open wav files. 
			AudioData soundData = DLUtils.loadWavFile(wavFilePath);
			soundData = soundData.trim(0, (int) (soundData.getSampleRate()*3.0)); 

			//generate the transforms. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(ketosParams.dlTransforms); 


			((WaveTransform) transforms.get(0)).setWaveData(soundData); 
			
			
			DLTransform transform = transforms.get(0); 
			for (int i=0; i<transforms.size(); i++) {
				//				System.out.println(transforms); 
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

