package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.dlClassification.koogu.KooguModelWorker;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;

public class KooguDLClassifierTest {


	/**
	 * Test the koogu classifier and tests are working properly. This tests loading the koogu model and also using
	 * functions in KooguWorker.
	 */
	@Test
	public void kooguClassifierTest() {
		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/blue_whale_24.kgu";
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/20190527_190000.wav";
		String relMatPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/rawScores_20190527_190000.mat";
		
		runKooguClassifier( relModelPath,  relWavPath,  relMatPath);
	}
	
	public static void runKooguClassifier(String relModelPath, String relWavPath, String relMatPath) {


		Path path = Paths.get(relModelPath);

		KooguModelWorker kooguWorker = new KooguModelWorker(); 

		StandardModelParams genericModelParams = new StandardModelParams(); 
		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();

		//prep the model - all setting are included within the model
		kooguWorker.prepModel(genericModelParams, null);
		System.out.println("seglen: " +  genericModelParams.defaultSegmentLen);
		
		
		for (int i=0; i<genericModelParams.dlTransfroms.size(); i++) {
			System.out.println(genericModelParams.dlTransfroms.get(i)); 
		}
		

		/****Now run a file ***/
		path = Paths.get(relWavPath);
		String wavFilePath = path.toAbsolutePath().normalize().toString();

		//load predictions. 
		path = Paths.get(relMatPath);

		Mat5File file;
		double[][] kooguPredicitions  = null;
		try {
			file = Mat5.readFromFile(path.toAbsolutePath().normalize().toString());
			Matrix matArray = file.getArray("scores");
			kooguPredicitions = DLMatFile.matrix2array(matArray); 
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		try {
			AudioData soundData = DLUtils.loadWavFile(wavFilePath);
			double[] soundDataD = soundData.getScaledSampleAmplitudes();


			long duration = (long) Math.ceil((genericModelParams.defaultSegmentLen/1000)*soundData.sampleRate);
			System.out.println("duration: " + duration + " " + soundData.sampleRate + "  " + genericModelParams.defaultSegmentLen);

			int truecount=0;
			//dont't use the first and last because these are edge cases with zero padding
			for (int i=0; i<kooguPredicitions.length; i++) {

				GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, duration, (int) duration);

				//koogu predictions are in samples
				int startChunk =(int) (kooguPredicitions[i][0]*soundData.sampleRate/250); ///the start chunk is in decimated samples - uuurgh
				

				groupedRawData.copyRawData(soundDataD, startChunk, (int) duration, 0);

				ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
				groupedData.add(groupedRawData);

				ArrayList<StandardPrediction> genericPrediciton = kooguWorker.runModel(groupedData, soundData.sampleRate, 0);		
				float[] output =  genericPrediciton.get(0).getPrediction();

				boolean testPassed= output[1]> kooguPredicitions[i][2]-0.1 && output[1]< kooguPredicitions[i][2]+0.1;
				
				System.out.println(String.format("Chunk %d %d output[0]: predicted %.5f true %.5f ; output[1]: predicted %.5f true %.5f %b",i, startChunk,
						output[0], kooguPredicitions[i][1], output[1], kooguPredicitions[i][2],testPassed)); 
				
				if (testPassed) {
					truecount++;
				}
				
				
			}
			
			//there are occasionaly slight differences between PMAGuard and Python so just make sure most data points are the same. 
			double percTrue = 100*((double) truecount)/kooguPredicitions.length; 

			System.out.println(String.format("Percentage results true: %.2f  count %d", percTrue, truecount));
			
		    //at least 90% of results must match for the dataset
			assertTrue(percTrue>0.9);

			kooguWorker.closeModel();

		} catch (IOException | UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

}