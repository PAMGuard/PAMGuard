package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.dlClassification.ketos.KetosDLParams;
import rawDeepLearningClassifier.dlClassification.ketos.KetosWorker2;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Test the North Atlantic minke whale pulse-train detector (Mouy et al. 2025,
 * DOI:10.5281/zenodo.15151160). This is a Ketos model which uses a 60 second
 * median equalizer (ecosound) to denoise the spectrogram.
 * <p>
 * The reference scores were generated using generate_minke_reference.py (in the
 * resources folder) which uses the functions in the detector's
 * run_ketos_detector_denoise.py script. The Ketos model was converted to a PAMGuard
 * compatible .ktpb file using convert_kt_to_ktpb.py.
 * <p>
 * Two sets of reference scores are compared.
 * <ul>
 * <li>segment_denoise - the median equalizer is applied to each 5 second segment.
 * This is exactly what PAMGuard does and so the scores should be (almost)
 * identical.</li>
 * <li>file_denoise - the median equalizer is applied to the whole file before it is
 * segmented, as in the Python detector. PAMGuard only has access to one segment
 * at a time so there are some differences.</li>
 * </ul>
 */
public class KetosMinkeDLClassifierTest {

	private static final String RESOURCE_FOLDER = "./src/test/resources/rawDeepLearningClassifier/Ketos/minke_whale_NAMW/";

	/**
	 * The spectrogram hop in samples - segment indices in the reference file are in FFT hops.
	 */
	private static final int FFT_HOP = 80;

	/**
	 * Test every nth segment in the reference file (segments step by 0.08 s).
	 */
	private static final int SEGMENT_STEP = 25;

	private static final int BATCH_SIZE = 32;

	@Test
	public void ketosMinkeTest() throws Exception {

		String modelPath = Paths.get(RESOURCE_FOLDER + "model_NAMW_20240523.ktpb").toAbsolutePath().normalize().toString();
		String wavFilePath = Paths.get(RESOURCE_FOLDER + "79296_Brunswick03_002K_M06_UTCm5_CH6_20161227_110000.wav").toAbsolutePath().normalize().toString();
		Path refPath = Paths.get(RESOURCE_FOLDER + "minke_reference_scores.csv").toAbsolutePath().normalize();

		// columns: seg_index, start_sec, noise_file_denoise, minke_file_denoise, noise_segment_denoise, minke_segment_denoise
		ArrayList<double[]> reference = readReference(refPath.toString(), SEGMENT_STEP);

		KetosWorker2 ketosWorker2 = new KetosWorker2();

		KetosDLParams params = new KetosDLParams();
		params.modelPath = modelPath;

		DLStatus status = ketosWorker2.prepModel(params, null);
		assertEquals(DLStatus.MODEL_LOAD_SUCCESS, status);

		AudioData soundData = DLUtils.loadWavFile(wavFilePath);
		double[] soundDataD = soundData.getScaledSampleAmplitudes();

		int segSamples = (int) Math.round((params.defaultSegmentLen / 1000.) * soundData.sampleRate);
		System.out.println("Segment length: " + params.defaultSegmentLen + " ms " + segSamples + " samples");

		int nSegDenoiseMatch = 0;
		int nFileDenoiseMatch = 0;
		int nFileDenoiseDetMatch = 0;
		double maxSegDiff = 0;

		for (int i = 0; i < reference.size(); i += BATCH_SIZE) {

			ArrayList<GroupedRawData> groupedData = new ArrayList<GroupedRawData>();
			int nBatch = Math.min(BATCH_SIZE, reference.size() - i);
			for (int j = 0; j < nBatch; j++) {
				int startSample = (int) reference.get(i + j)[0] * FFT_HOP;
				GroupedRawData groupedRawData = new GroupedRawData(0, 1, startSample, segSamples, segSamples);
				groupedRawData.copyRawData(soundDataD, startSample, segSamples, 0);
				groupedData.add(groupedRawData);
			}

			ArrayList<StandardPrediction> predictions = ketosWorker2.runModel(groupedData, soundData.sampleRate, 0);

			for (int j = 0; j < nBatch; j++) {
				double[] ref = reference.get(i + j);
				float minke = predictions.get(j).getPrediction()[1];

				double segDiff = Math.abs(minke - ref[5]);
				maxSegDiff = Math.max(maxSegDiff, segDiff);
				if (segDiff < 0.05) nSegDenoiseMatch++;
				if (Math.abs(minke - ref[3]) < 0.1) nFileDenoiseMatch++;
				if ((minke >= 0.5) == (ref[3] >= 0.5)) nFileDenoiseDetMatch++;
			}
		}

		ketosWorker2.closeModel();

		int n = reference.size();
		double segMatch = nSegDenoiseMatch / (double) n;
		double fileMatch = nFileDenoiseMatch / (double) n;
		double fileDetMatch = nFileDenoiseDetMatch / (double) n;

		System.out.println(String.format("Minke whale: %d segments tested", n));
		System.out.println(String.format("Segment denoise: %.1f%% within 0.05, max difference %.4f", 100 * segMatch, maxSegDiff));
		System.out.println(String.format("File denoise: %.1f%% within 0.1, %.1f%% agree at 0.5 threshold", 100 * fileMatch, 100 * fileDetMatch));

		// PAMGuard should reproduce the segment denoised python scores.
		assertTrue(segMatch > 0.95);
		// ...and be broadly similar to the whole file denoised scores. In python, 85% of all segments
		// agree at a 0.5 threshold between segment and whole file denoising.
		assertTrue(fileDetMatch > 0.8);
	}

	/**
	 * Read the reference scores CSV file.
	 * @param path - the path to the CSV file.
	 * @param step - read every step rows.
	 * @return the reference scores.
	 */
	private static ArrayList<double[]> readReference(String path, int step) throws Exception {
		ArrayList<double[]> reference = new ArrayList<double[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			reader.readLine(); // header
			String line;
			int row = 0;
			while ((line = reader.readLine()) != null) {
				if (row++ % step != 0) continue;
				String[] vals = line.split(",");
				double[] data = new double[vals.length];
				for (int i = 0; i < vals.length; i++) {
					data[i] = Double.parseDouble(vals[i]);
				}
				reference.add(data);
			}
		}
		return reference;
	}

}
