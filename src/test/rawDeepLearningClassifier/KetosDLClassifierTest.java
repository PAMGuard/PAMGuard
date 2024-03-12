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

import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.dlClassification.ketos.KetosDLParams;
import rawDeepLearningClassifier.dlClassification.ketos.KetosWorker2;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;

import org.junit.jupiter.api.Test;

public class KetosDLClassifierTest {
	
//	/**
//	 * Reference to the DL Control
//	 * 
//	 */
//	private DLControl testDLControl;
//	
//	
//	private KetosClassifier ketosClassifier_test;
//	
	

//	public KetosClassifierTest()  {
//		 System.out.println("hello unit test start"); 

//		try {
//			
//		 if (PamController.getInstance()==null || PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
//			 PamGUIManager.setType(PamGUIManager.NOGUI);
//			 PamController.create(PamController.RUN_NORMAL, null);
//		 }
//			
//		 testDLControl = new DLControl("Test_deep_learning");  
//		 
//		 ketosClassifier_test = (KetosClassifier) testDLControl.getDLModel(KetosClassifier.MODEL_NAME); 
//		 
//		 //set the ketos model as the correct model in the test. 
//		 testDLControl.getDLParams().modelSelection= testDLControl.getDLModels().indexOf(ketosClassifier_test); 
//		 
//		 System.out.println("hello unit test complete"); 
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Test the ketos classifier and tests are working properly. 
	 */
	@Test
	public void ketosClassifierTest() {
		
		
		/**
		 * List of the predicitons
		 * Start time (seconds), Length of the segment (seconds), prediciton
		 */
		double[][] ketosPredicitons = {
				{0,	5.0176,	0.1565524},
				{5,	5.0176,	0.99999917},
				{10,	5.0176,	0.99999917},
				{15, 5.0176,	0.97594243},
				{20,	5.0176,	0.8802458},
				{25,	5.0176,	0.9999999},
				{30,	5.0176,	0.999993},
				{35,	5.0176,	0.9998863},
				{40,	5.0176,	0.99998367},
				{45,	5.0176,	0.21531366},
				{50,	5.0176,	0.9999987},
				{55,	5.0176,	1},
				{60,	5.0176,	0.9999989},
				{65,	5.0176,	0.9999993},
				{70,	5.0176,	0.99999845},
				{75,	5.0176,	1},
				{80,	5.0176,	0.20126265},
				{85,	5.0176,	0.9797412},
				{90,	5.0176,	1}}; 
	
			//relative paths to the resource folders.
			String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Ketos/hallo-kw-det_v1/hallo-kw-det_v1.ktpb";
			String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Ketos/hallo-kw-det_v1/jasco_reduced.wav";

			Path path = Paths.get(relModelPath);

			KetosWorker2 ketosWorker2 = new KetosWorker2(); 

			KetosDLParams genericModelParams = new KetosDLParams(); 
			genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

			//prep the model - all setting are included within the model
			ketosWorker2.prepModel(genericModelParams, null);
			System.out.println("seglen: " +  genericModelParams.defaultSegmentLen);

			/****Now run a file ***/
			path = Paths.get(relWavPath);
			String wavFilePath = path.toAbsolutePath().normalize().toString();

			try {
				
				
				AudioData soundData = DLUtils.loadWavFile(wavFilePath);
				double[] soundDataD = soundData.getScaledSampleAmplitudes();
				
				long duration = (long) Math.ceil((genericModelParams.defaultSegmentLen/1000)*soundData.sampleRate);
				System.out.println("duration: " + duration + " " + soundData.sampleRate + "  " + genericModelParams.defaultSegmentLen);

				//dont't 
				for (int i=1; i<ketosPredicitons.length; i++) {


				GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, duration, (int) duration);
				int startChunk =(int) (ketosPredicitons[i][0]*soundData.sampleRate);

				
				groupedRawData.copyRawData(soundDataD, startChunk, (int) duration, 0);
				
				ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
				groupedData.add(groupedRawData);

				ArrayList<GenericPrediction> genericPrediciton = ketosWorker2.runModel(groupedData, soundData.sampleRate, 0);		
				float[] output =  genericPrediciton.get(0).getPrediction();
				
				boolean testPassed= output[1]> ketosPredicitons[i][2]-0.1 && output[1]< ketosPredicitons[i][2]+0.1;
				System.out.println( i+ " : Ketos whale network output: " + output[0] + "  " + output[1] + " " + testPassed);
				
				//assertTrue(output[1]> ketosPredicitons[i][2]-0.1 && output[1]< ketosPredicitons[i][2]+0.1); 
				
				}

				ketosWorker2.closeModel();

			} catch (IOException | UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertEquals(false, true);
			}
	}

	

}
