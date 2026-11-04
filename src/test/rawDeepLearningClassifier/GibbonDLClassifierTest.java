package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Test the Gibbon model, a waveform model which is packaged in a zip file with a
 * PAMGuard settings file. All the settings (7 second segments, decimation to 8kHz
 * and three output classes) are read from the zip file.
 * <p>
 * This is based on org.jamdev.jdl4pam.generic.GibbonTest in jpam and compares
 * PAMGuard's predictions to the same outputs from Python.
 */
public class GibbonDLClassifierTest {

	private static final String RESOURCE_FOLDER = "./src/test/resources/rawDeepLearningClassifier/PamZip/gibbon/";

	/**
	 * The expected output for consecutive 7 second segments from running the model
	 * in Python. The last segment in the Python output is not included because it
	 * runs off the end of the file and is zero padded.
	 */
	private static final double[][] PREDICTIONS = new double[][] {
		{9.7423404e-01, 1.6867964e-03, 2.8357367e-04},
		{5.9026042e-03, 5.8442029e-06, 1.1701107e-06},
		{3.6076629e-03, 2.2650822e-06, 6.4889866e-07},
		{9.9562621e-01, 5.5051147e-05, 1.8434161e-04},
		{4.1060164e-04, 9.9579030e-01, 1.3399041e-04},
		{2.2205876e-02, 5.9773076e-01, 2.4194127e-05},
		{8.6745679e-01, 4.0851589e-03, 2.0674334e-04},
		{3.6767789e-05, 3.4960653e-08, 1.5367555e-09}
	};

	/**
	 * The maximum allowed difference between a PAMGuard and Python prediction. The
	 * largest measured difference is 0.033 - PAMGuard decimates each segment whilst
	 * the Python outputs are from decimating the whole file.
	 */
	private static final double TOLERANCE = 0.05;

	@Test
	public void gibbonModelTest() throws Exception {

		System.out.println("*****PAMGuard zip DL: Gibbon test*****");

		String modelPath = Paths.get(RESOURCE_FOLDER + "gibbon_model.zip").toAbsolutePath().normalize().toString();
		String wavFilePath = Paths.get(RESOURCE_FOLDER + "gibbon_detection.wav").toAbsolutePath().normalize().toString();

		ArchiveModelWorker worker = new ArchiveModelWorker();

		StandardModelParams params = new StandardModelParams();
		params.modelPath = modelPath;

		DLStatus status = worker.prepModel(params, null);
		assertEquals(DLStatus.MODEL_LOAD_SUCCESS, status);

		//check the settings have been read from the zip file.
		assertEquals(7000., params.defaultSegmentLen, 0.001);
		assertEquals(3, params.numClasses);

		AudioData soundData = DLUtils.loadWavFile(wavFilePath);
		double[] soundDataD = soundData.getScaledSampleAmplitudes();

		int segSamples = (int) Math.round((params.defaultSegmentLen / 1000.) * soundData.sampleRate);

		int nPassed = 0;
		for (int i = 0; i < PREDICTIONS.length; i++) {

			int startSample = i * segSamples;

			GroupedRawData groupedRawData = new GroupedRawData(0, 1, startSample, segSamples, segSamples);
			groupedRawData.copyRawData(soundDataD, startSample, segSamples, 0);

			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			groupedData.add(groupedRawData);

			float[] output = worker.runModel(groupedData, soundData.sampleRate, 0).get(0).getPrediction();
			assertEquals(PREDICTIONS[i].length, output.length);

			boolean passed = true;
			for (int j = 0; j < output.length; j++) {
				passed = passed && Math.abs(output[j] - PREDICTIONS[i][j]) < TOLERANCE;
			}
			if (passed) nPassed++;

			System.out.println(String.format("Segment %d start %d: predicted %.5f %.5f %.5f true %.5f %.5f %.5f passed: %b", i, startSample,
					output[0], output[1], output[2], PREDICTIONS[i][0], PREDICTIONS[i][1], PREDICTIONS[i][2], passed));
		}

		worker.closeModel();

		assertEquals(PREDICTIONS.length, nPassed);
	}

}
