package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.DLDownloadManager;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.dlClassification.yolo.YoloClassifier;
import rawDeepLearningClassifier.dlClassification.yolo.YoloModel.YoloType;
import rawDeepLearningClassifier.dlClassification.yolo.YoloParams;
import rawDeepLearningClassifier.dlClassification.yolo.YoloPrediction;
import rawDeepLearningClassifier.dlClassification.yolo.YoloWorker;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Test an Ultralytics YOLO11 model - the BioDCASE 2026 Task 2 baseline which
 * detects Antarctic blue whale (bmabz), D calls (d) and fin whale 20 Hz pulses
 * (bp) in 30 s spectrograms at 250 Hz. The weights are from Zenodo record
 * 20281116 (CC BY 4.0), exported to TorchScript.
 * <p>
 * The wav files are one hour recordings at 1 kHz from the Kerguelen 2015
 * dataset (AcousticTrends_BlueFinLibrary, CC BY 4.0). The reference boxes were
 * made in Python by Ultralytics predict with conf 0.1 and agnostic NMS on
 * images made by the BioDCASE preprocessing. The March reference
 * (boxes_20150328_090000.csv) is from Brian Miller and the April reference was
 * made by make_biodcase_reference.py, which reproduces the March reference.
 * <p>
 * PAMGuard resamples each segment rather than the whole file and the image
 * resize is not always bit exact with OpenCV, so occasionally a box close to the
 * 0.1 threshold may differ.
 */
public class BioDCASEYoloTest {

	private static final String RESOURCE_FOLDER = "./src/test/resources/rawDeepLearningClassifier/YOLO/biodcase_task2/";

	/**
	 * The model on the PAMGuard deeplearningmodels GitHub repository.
	 */
	public static final String MODEL_URL = "https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/biodcase_task2_yolo11s.zip";

	/**
	 * The class names in the order of the model output.
	 */
	private static final String[] CLASS_NAMES = new String[] {"bmabz", "d", "bp"};

	/**
	 * The segment length and hop in seconds (the same as BioDCASE).
	 */
	private static final double SEG_LEN = 30, SEG_HOP = 15;

	/**
	 * The minimum time-frequency IoU of a PAMGuard and Python box for them to be
	 * the same detection.
	 */
	private static final double MIN_IOU = 0.8;

	/**
	 * The number of segments to run through the model at once.
	 */
	private static final int BATCH_SIZE = 8;

	@Test
	public void blueWhaleMarchTest() throws Exception {
		testFile("20150328_090000");
	}

	@Test
	public void finWhaleAprilTest() throws Exception {
		testFile("20150420_070000");
	}

	/**
	 * Run the model on an hour of data and compare the boxes to the Python
	 * reference.
	 *
	 * @param name - the name of the wav file without the extension.
	 */
	private void testFile(String name) throws Exception {

		File modelFile = getModelFile();
		assertNotNull(modelFile, "Unable to download the BioDCASE model from " + MODEL_URL);

		String wavFilePath = Paths.get(RESOURCE_FOLDER + name + ".wav").toAbsolutePath().normalize().toString();
		List<RefBox> refBoxes = readReference(Paths.get(RESOURCE_FOLDER + "boxes_" + name + ".csv").toAbsolutePath().normalize().toString());

		YoloWorker yoloWorker = new YoloWorker();
		YoloParams params = new YoloParams();
		params.modelPath = modelFile.getAbsolutePath();

		assertEquals(DLStatus.MODEL_LOAD_SUCCESS, yoloWorker.prepModel(params, null));
		assertEquals(YoloType.ULTRALYTICS, yoloWorker.getYoloModel().getYoloType());
		assertEquals(3, params.numClasses);
		assertEquals(SEG_LEN*1000, params.defaultSegmentLen, 1e-6);

		//the BioDCASE baseline uses a confidence threshold of 0.1 and NMS but does not merge boxes.
		params.minConfidence = 0.1;
		params.mergeOverlap = false;
		YoloClassifier.setWorkerParams(yoloWorker, params);

		AudioData soundData = DLUtils.loadWavFile(wavFilePath);
		double[] soundDataD = soundData.getScaledSampleAmplitudes();
		float sampleRate = soundData.sampleRate;
		assertEquals(1000, sampleRate, 1e-6);

		int segSamples = (int) Math.round(SEG_LEN * sampleRate);
		int hopSamples = (int) Math.round(SEG_HOP * sampleRate);

		//make the segments in the same way as BioDCASE - every 15 s, zero padding the last segments.
		ArrayList<GroupedRawData> segments = new ArrayList<GroupedRawData>();
		for (int startSample = 0; startSample < soundDataD.length; startSample += hopSamples) {
			GroupedRawData segment = new GroupedRawData((long) (1000.*startSample/sampleRate), 1, startSample, segSamples, segSamples);
			segment.setUID(segments.size());
			segment.setDurationInMilliseconds(1000.*segSamples/sampleRate);
			int nCopy = Math.min(segSamples, soundDataD.length - startSample);
			System.arraycopy(soundDataD, startSample, segment.getRawData()[0], 0, nCopy);
			segments.add(segment);
		}
		assertEquals((int) Math.ceil(soundDataD.length / (double) hopSamples), segments.size());

		ArrayList<YoloPrediction> predictions = new ArrayList<YoloPrediction>();
		for (int i = 0; i < segments.size(); i += BATCH_SIZE) {
			ArrayList<GroupedRawData> batch = new ArrayList<GroupedRawData>(segments.subList(i, Math.min(segments.size(), i + BATCH_SIZE)));
			ArrayList<StandardPrediction> batchPredictions = yoloWorker.runModel(batch, sampleRate, 0);
			assertNotNull(batchPredictions);
			for (StandardPrediction prediction : batchPredictions) {
				predictions.add((YoloPrediction) prediction);
			}
		}

		double[] freqLims = yoloWorker.getImageFreqLimits();
		assertNotNull(freqLims);
		assertEquals(0, freqLims[0], 1e-6);
		assertEquals(125, freqLims[1], 1e-6);

		yoloWorker.closeModel();

		//convert each box to a time and frequency in the same way as the YOLO classifier.
		List<RefBox> pgBoxes = new ArrayList<RefBox>();
		for (YoloPrediction prediction : predictions) {
			GroupedRawData segment = segments.get((int) prediction.getParentSegmentID());

			//boxes are in pixels of the 363 x 257 spectrogram, not the 640 x 480 model input.
			assertEquals(363, prediction.getImageWidth());
			assertEquals(257, prediction.getImageHeight());

			YoloClassifier.setBoxLimits(prediction, segment, freqLims);

			RefBox box = new RefBox();
			box.segStart = segment.getStartSample() / sampleRate;
			box.classIndex = argMax(prediction.getPredicitions());
			box.confidence = prediction.getConfidence();
			box.tStart = prediction.getStartSample() / sampleRate;
			box.tEnd = (prediction.getStartSample() + prediction.getSampleDuration()) / sampleRate;
			box.fLow = prediction.getFreqLimits()[0];
			box.fHigh = prediction.getFreqLimits()[1];
			pgBoxes.add(box);
		}

		//match each Python box to the PAMGuard box in the same segment with the same class and the highest IoU.
		int nMatch = 0;
		double maxScoreDiff = 0, sumTimeDiff = 0, sumFreqDiff = 0;
		boolean[] pgMatched = new boolean[pgBoxes.size()];
		for (RefBox ref : refBoxes) {
			int best = -1;
			double bestIoU = 0;
			for (int j = 0; j < pgBoxes.size(); j++) {
				RefBox pg = pgBoxes.get(j);
				if (pgMatched[j] || Math.abs(pg.segStart - ref.segStart) > 1e-6 || pg.classIndex != ref.classIndex) continue;
				double iou = ref.iou(pg);
				if (iou > bestIoU) {
					bestIoU = iou;
					best = j;
				}
			}
			if (best >= 0 && bestIoU >= MIN_IOU) {
				RefBox pg = pgBoxes.get(best);
				pgMatched[best] = true;
				nMatch++;
				maxScoreDiff = Math.max(maxScoreDiff, Math.abs(pg.confidence - ref.confidence));
				sumTimeDiff += Math.abs(pg.tStart - ref.tStart) + Math.abs(pg.tEnd - ref.tEnd);
				sumFreqDiff += Math.abs(pg.fLow - ref.fLow) + Math.abs(pg.fHigh - ref.fHigh);
			}
			else {
				System.out.println(String.format("%s: no match for Python box at %.1f s class %s score %.3f %.2f-%.2f s %.1f-%.1f Hz (best IoU %.2f)",
						name, ref.segStart, CLASS_NAMES[ref.classIndex], ref.confidence, ref.tStart, ref.tEnd, ref.fLow, ref.fHigh, bestIoU));
			}
		}

		int[] pgClassCount = new int[CLASS_NAMES.length];
		int[] refClassCount = new int[CLASS_NAMES.length];
		for (RefBox box : pgBoxes) pgClassCount[box.classIndex]++;
		for (RefBox box : refBoxes) refClassCount[box.classIndex]++;

		System.out.println(String.format("%s: %d PAMGuard boxes and %d Python boxes, %d matched (IoU >= %.2f). "
				+ "Max score difference %.4f, mean edge difference %.3f s and %.2f Hz",
				name, pgBoxes.size(), refBoxes.size(), nMatch, MIN_IOU, maxScoreDiff,
				sumTimeDiff/(2*Math.max(1, nMatch)), sumFreqDiff/(2*Math.max(1, nMatch))));
		for (int i = 0; i < CLASS_NAMES.length; i++) {
			System.out.println(String.format("%s: class %s PAMGuard %d Python %d", name, CLASS_NAMES[i], pgClassCount[i], refClassCount[i]));
		}

		//nearly all Python boxes are found by PAMGuard and PAMGuard finds very few extra boxes.
		assertTrue(nMatch >= 0.99*refBoxes.size(), "Only " + nMatch + " of " + refBoxes.size() + " Python boxes were matched");
		assertTrue(pgBoxes.size() - nMatch <= 0.01*refBoxes.size(), (pgBoxes.size() - nMatch) + " PAMGuard boxes were not in the Python boxes");

		//matched boxes have the same scores and edges (one pixel is 0.08 s and 0.49 Hz).
		assertTrue(maxScoreDiff < 0.01, "Maximum score difference " + maxScoreDiff);
		assertTrue(sumTimeDiff/(2*Math.max(1, nMatch)) < 0.01, "Mean time difference " + sumTimeDiff/(2*nMatch));
		assertTrue(sumFreqDiff/(2*Math.max(1, nMatch)) < 0.1, "Mean frequency difference " + sumFreqDiff/(2*nMatch));
	}

	/**
	 * A bounding box in seconds and Hz.
	 */
	private static class RefBox {

		double segStart;

		int classIndex;

		double confidence;

		double tStart, tEnd, fLow, fHigh;

		/**
		 * The intersection over union of two boxes in time and frequency.
		 */
		double iou(RefBox box) {
			double dt = Math.min(tEnd, box.tEnd) - Math.max(tStart, box.tStart);
			double df = Math.min(fHigh, box.fHigh) - Math.max(fLow, box.fLow);
			if (dt <= 0 || df <= 0) return 0;
			double intersect = dt * df;
			return intersect / ((tEnd - tStart) * (fHigh - fLow) + (box.tEnd - box.tStart) * (box.fHigh - box.fLow) - intersect);
		}
	}

	/**
	 * Read the Python reference boxes. The columns are file, dataset, start_s,
	 * class, confidence, t_start, t_end, f_low, f_high.
	 */
	private static List<RefBox> readReference(String path) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(path));
		List<RefBox> boxes = new ArrayList<RefBox>();
		for (int i = 1; i < lines.size(); i++) {
			String[] cols = lines.get(i).trim().split(",");
			if (cols.length < 9) continue;
			RefBox box = new RefBox();
			box.segStart = Double.parseDouble(cols[2]);
			box.classIndex = classIndex(cols[3]);
			box.confidence = Double.parseDouble(cols[4]);
			box.tStart = Double.parseDouble(cols[5]);
			box.tEnd = Double.parseDouble(cols[6]);
			box.fLow = Double.parseDouble(cols[7]);
			box.fHigh = Double.parseDouble(cols[8]);
			boxes.add(box);
		}
		return boxes;
	}

	private static int classIndex(String name) {
		for (int i = 0; i < CLASS_NAMES.length; i++) {
			if (CLASS_NAMES[i].equals(name)) return i;
		}
		throw new IllegalArgumentException("Unknown class " + name);
	}

	/**
	 * Get the BioDCASE model, downloading it if it has not already been
	 * downloaded.
	 *
	 * @return the model zip file or null if the model could not be downloaded.
	 */
	public static File getModelFile() throws Exception {
		DLDownloadManager downloadManager = new DLDownloadManager();
		URL url = new URL(MODEL_URL);
		String modelName = "biodcase_task2_yolo11s";

		if (!downloadManager.isModelDownloaded(url, modelName)) {
			System.out.println("Downloading the BioDCASE model from " + MODEL_URL);
			URI uri = downloadManager.downloadModel(new URI(MODEL_URL), modelName, new String[] {modelName + ".pdtf"});
			if (uri == null) return null;
		}

		File modelFile = downloadManager.getModelDownloadedFile(url, modelName);
		return modelFile.exists() ? modelFile : null;
	}

	private static int argMax(float[] arr) {
		int index = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > arr[index]) index = i;
		}
		return index;
	}

}
