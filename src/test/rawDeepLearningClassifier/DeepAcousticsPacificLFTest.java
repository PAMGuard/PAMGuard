package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.deepAcoustics.DeepAcousticParams;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.dlClassification.yolo.YoloClassifier;
import rawDeepLearningClassifier.dlClassification.yolo.YoloPrediction;
import rawDeepLearningClassifier.dlClassification.yolo.YoloWorker;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Test a DeepAcoustics model (a MATLAB tiny YOLO v4 exported to TensorFlow)
 * which detects and classifies low frequency blue and fin whale calls in data
 * from the Ocean Observatories Initiative (OOI) hydrophone HYDBBA105 sampled at
 * 400 Hz.
 * <p>
 * The detections are compared to a Raven selection table of annotated calls (18
 * Hz and 20 Hz fin whale calls and blue whale A and B calls) for one hour of
 * continuous data (twelve five minute files). The model detects calls within
 * 40.96 second segments and so, unlike the other tests, this checks that most
 * annotated calls are detected with the correct class and frequency range.
 */
public class DeepAcousticsPacificLFTest {

	private static final String RESOURCE_FOLDER = "./src/test/resources/rawDeepLearningClassifier/DeepAcoustics/pacific_lf/";

	/**
	 * The annotation times are relative to the start of the first file of the
	 * day's selection table (02:00:00). The test data start at 03:15:00.
	 */
	private static final double ANNOTATION_OFFSET_SECONDS = 4500;

	private static final int BATCH_SIZE = 16;

	@Test
	public void pacificLFTest() throws Exception {

		String modelPath = Paths.get(RESOURCE_FOLDER + "PacificLF_05_TinyYOLO_FIXED.zip").toAbsolutePath().normalize().toString();

		DeepAcousticParams params = new DeepAcousticParams();
		params.modelPath = modelPath;

		YoloWorker yoloWorker = new YoloWorker();
		DLStatus status = yoloWorker.prepModel(params, null);
		assertEquals(DLStatus.MODEL_LOAD_SUCCESS, status);
		assertEquals(11, params.numClasses);

		//the default DeepAcoustics settings - 0.4 confidence and merged overlapping boxes.
		YoloClassifier.setWorkerParams(yoloWorker, params);

		//the class names in the model
		List<String> classNames = Arrays.asList("Noise", "18 Hz", "20 Hz", "30 Hz", "40-80 Hz", "A", "B", "Bb", "Be", "D", "UNID");

		//load the continuous data from all the wav files.
		File[] wavFiles = new File(RESOURCE_FOLDER).listFiles((dir, name) -> name.endsWith(".wav"));
		Arrays.sort(wavFiles);
		assertEquals(12, wavFiles.length);

		float sampleRate = 0;
		ArrayList<double[]> fileData = new ArrayList<double[]>();
		int nSamples = 0;
		for (File wavFile : wavFiles) {
			AudioData soundData = DLUtils.loadWavFile(wavFile.getAbsolutePath());
			sampleRate = soundData.sampleRate;
			fileData.add(soundData.getScaledSampleAmplitudes());
			nSamples += fileData.get(fileData.size() - 1).length;
		}
		double[] data = new double[nSamples];
		int pos = 0;
		for (double[] d : fileData) {
			System.arraycopy(d, 0, data, pos, d.length);
			pos += d.length;
		}

		int segSamples = (int) Math.round((params.defaultSegmentLen / 1000.) * sampleRate);
		System.out.println("Segment length: " + params.defaultSegmentLen + " ms " + segSamples + " samples");

		//run the model on consecutive segments. Detections are [start (s), end (s), min freq (Hz), max freq (Hz), class]
		ArrayList<double[]> detections = new ArrayList<double[]>();
		ArrayList<GroupedRawData> batch = new ArrayList<GroupedRawData>();
		int iSeg = 0;
		for (int start = 0; start < data.length; start += segSamples) {
			GroupedRawData segment = new GroupedRawData((long) (1000. * start / sampleRate), 1, start, segSamples, segSamples);
			segment.setUID(iSeg++);
			segment.setDurationInMilliseconds(1000. * segSamples / sampleRate);
			System.arraycopy(data, start, segment.getRawData()[0], 0, Math.min(segSamples, data.length - start));
			batch.add(segment);

			if (batch.size() == BATCH_SIZE || start + segSamples >= data.length) {
				ArrayList<StandardPrediction> predictions = yoloWorker.runModel(batch, sampleRate, 0);
				assertNotNull(predictions);
				for (StandardPrediction prediction : predictions) {
					YoloPrediction yoloPrediction = (YoloPrediction) prediction;
					GroupedRawData parent = batch.get((int) (yoloPrediction.getParentSegmentID() - batch.get(0).getUID()));

					//convert the bounding box to time and frequency as the classifier does.
					YoloClassifier.setBoxLimits(yoloPrediction, parent, yoloWorker.getYoloModel().getImageWidth(),
							yoloWorker.getYoloModel().getImageHeight(), yoloWorker.getImageFreqLimits());

					int classIndex = argMax(yoloPrediction.getPrediction());
					if (classIndex == 0) continue; //noise

					double startSeconds = yoloPrediction.getStartSample() / (double) sampleRate;
					detections.add(new double[] { startSeconds, startSeconds + yoloPrediction.getSampleDuration() / (double) sampleRate,
							yoloPrediction.getFreqLimits()[0], yoloPrediction.getFreqLimits()[1], classIndex });
				}
				batch.clear();
			}
		}
		yoloWorker.closeModel();

		//compare to the annotations
		ArrayList<Annotation> annotations = readAnnotations(RESOURCE_FOLDER + "OOI_105_Blue_Fin_20170203_031500_041500_annotations.txt");

		int nDetected = 0;
		int nCorrectClass = 0;
		int nFreqOverlap = 0;
		for (Annotation annotation : annotations) {
			boolean detected = false, correctClass = false, freqOverlap = false;
			for (double[] detection : detections) {
				if (Math.min(annotation.end, detection[1]) <= Math.max(annotation.start, detection[0])) continue;
				detected = true;
				if (Math.min(annotation.maxFreq, detection[3]) > Math.max(annotation.minFreq, detection[2])) freqOverlap = true;
				if (classNames.get((int) detection[4]).equals(annotation.label)) correctClass = true;
			}
			if (detected) nDetected++;
			if (correctClass) nCorrectClass++;
			if (freqOverlap) nFreqOverlap++;
		}

		int nTrueDetections = 0;
		for (double[] detection : detections) {
			for (Annotation annotation : annotations) {
				if (Math.min(annotation.end, detection[1]) > Math.max(annotation.start, detection[0])) {
					nTrueDetections++;
					break;
				}
			}
		}

		int n = annotations.size();
		System.out.println(String.format("Pacific LF: %d annotated calls, %d detections (excluding noise)", n, detections.size()));
		System.out.println(String.format("Detected: %.1f%%, correct class: %.1f%%, frequency overlap: %.1f%%, detections on an annotated call: %.1f%%",
				100. * nDetected / n, 100. * nCorrectClass / n, 100. * nFreqOverlap / n, 100. * nTrueDetections / detections.size()));

		assertEquals(172, n);
		// the model finds almost all annotated calls with the correct class.
		// (98% and 97% when this test was written)
		assertTrue(nDetected / (double) n > 0.95);
		assertTrue(nCorrectClass / (double) n > 0.92);
		// the frequency limits of the bounding boxes are correct.
		assertTrue(nFreqOverlap / (double) nDetected > 0.95);
		// most detections are annotated calls.
		assertTrue(nTrueDetections / (double) detections.size() > 0.75);
	}

	/**
	 * An annotated call from the Raven selection table.
	 */
	private static class Annotation {
		double start, end, minFreq, maxFreq;
		String label;
	}

	/**
	 * Read the annotations from a Raven selection table. Times are converted to
	 * seconds from the start of the test data.
	 */
	private static ArrayList<Annotation> readAnnotations(String path) throws Exception {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			List<String> header = Arrays.asList(reader.readLine().split("\t"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) continue;
				String[] vals = line.split("\t");
				Annotation annotation = new Annotation();
				annotation.start = Double.parseDouble(vals[header.indexOf("Begin Time (s)")]) - ANNOTATION_OFFSET_SECONDS;
				annotation.end = Double.parseDouble(vals[header.indexOf("End Time (s)")]) - ANNOTATION_OFFSET_SECONDS;
				annotation.minFreq = Double.parseDouble(vals[header.indexOf("Low Freq (Hz)")]);
				annotation.maxFreq = Double.parseDouble(vals[header.indexOf("High Freq (Hz)")]);
				annotation.label = vals[header.indexOf("Annotation")].trim();
				annotations.add(annotation);
			}
		}
		return annotations;
	}

	private static int argMax(float[] arr) {
		int index = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > arr[index]) index = i;
		}
		return index;
	}

}
