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
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.dlClassification.ketos.KetosDLParams;
import rawDeepLearningClassifier.dlClassification.ketos.KetosWorker2;
import rawDeepLearningClassifier.dlClassification.koogu.KooguModelWorker;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;

public class KooguDLClassifierTest {
	
//	
//	/**
//	 * Test the koogu classifier and tests are working properly. This tests loading the koogu model and also using
//	 * functions in KooguWorker.
//	 */
//	@Test
//	public void kooguClassifierTest() {
//
//		//relative paths to the resource folders.
//		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/Ketos/hallo-kw-det_v1/hallo-kw-det_v1.ktpb";
//		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Ketos/hallo-kw-det_v1/jasco_reduced.wav";
//
//		Path path = Paths.get(relModelPath);
//
//		KooguModelWorker kooguWorker = new KooguModelWorker(); 
//
//		StandardModelParams genericModelParams = new StandardModelParams(); 
//		genericModelParams.modelPath =  path.toAbsolutePath().normalize().toString();
//
//		//prep the model - all setting are included within the model
//		kooguWorker.prepModel(genericModelParams, null);
//		System.out.println("seglen: " +  genericModelParams.defaultSegmentLen);
//
//		/****Now run a file ***/
//		path = Paths.get(relWavPath);
//		String wavFilePath = path.toAbsolutePath().normalize().toString();
//
//		try {
//
//
//			AudioData soundData = DLUtils.loadWavFile(wavFilePath);
//			double[] soundDataD = soundData.getScaledSampleAmplitudes();
//
//
//			long duration = (long) Math.ceil((genericModelParams.defaultSegmentLen/1000)*soundData.sampleRate);
//			System.out.println("duration: " + duration + " " + soundData.sampleRate + "  " + genericModelParams.defaultSegmentLen);
//
//			//dont't use the first and last because these are edge cases with zero padding
//			for (int i=1; i<ketosPredicitons.length-1; i++) {
//				
//				GroupedRawData groupedRawData = new GroupedRawData(0, 1, 0, duration, (int) duration);
//
//				/**
//				 * This is super weird but Ketos has some sort of very strange system of
//				 * grabbing chunks of data from a sound file - seems like it grabs a little more
//				 * data pre the official start time. Whatever the reason this does not matter
//				 * for PG usually because segments simply start at the start of the wav file.
//				 * However for testing we have to get this right to compare results and so
//				 * 0.0157 is subtract from the sound chunk
//				 */
//				int startChunk =(int) ((ketosPredicitons[i][0]-0.0157)*soundData.sampleRate);
//
//
//				groupedRawData.copyRawData(soundDataD, startChunk, (int) duration, 0);
//
//				ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
//				groupedData.add(groupedRawData);
//
//				ArrayList<GenericPrediction> genericPrediciton = ketosWorker2.runModel(groupedData, soundData.sampleRate, 0);		
//				float[] output =  genericPrediciton.get(0).getPrediction();
//
//				boolean testPassed= output[1]> ketosPredicitons[i][2]-0.1 && output[1]< ketosPredicitons[i][2]+0.1;
//				System.out.println( i+ " : Ketos whale network output: " + output[0] + "  " + output[1] + " " + testPassed);
//
//				assertTrue(testPassed); 
//
//			}
//
//			ketosWorker2.closeModel();
//
//		} catch (IOException | UnsupportedAudioFileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			assertEquals(false, true);
//		}
//	}
//	
}