package rawDeepLearningClassifier.dlClassification.yolo;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;


/**
 * A classifier for YOLO object detection models which detect calls within
 * spectrogram images, e.g. models trained using MATLAB's yolov4ObjectDetector.
 * <p>
 * This extends the ArchiveModelClassifier to provide functionality for
 * interpreting the results from a YOLO model, which detects objects within
 * input segments. A YOLO model can return multiple results per segment and each
 * result is a bounding box, i.e. the time and frequency limits of a result are
 * not the same as the input segment. This is different from most other models.
 * <p>
 * The spectrogram image is assumed to have the highest frequency at the top
 * (i.e. the spectrogram has been flipped) and the frequency limits are taken
 * from the last spectrogram transform.
 *
 * @author Jamie Macaulay
 *
 */
public class YoloClassifier extends ArchiveModelClassifier {

	public static String MODEL_NAME = "YOLO";


	public YoloClassifier(DLControl dlControl) {
		super(dlControl);
		archiveModelUI = new YoloUI(this);
	}


	@Override
	public String getName() {
		//important because this is used to identify model from JSON file
		return MODEL_NAME;
	}

	@Override
	public StandardModelParams makeParams() {
		return new YoloParams();
	}


	/**
	 * Get the YoloWorker. This handles loading and running the YOLO model.
	 * @return the YOLO worker.
	 */
	@Override
	public ArchiveModelWorker getModelWorker() {
		if (archiveModelWorker==null) {
			archiveModelWorker= new YoloWorker();
		}
		return archiveModelWorker;
	}

	/**
	 * Get the YOLO worker.
	 * @return the YOLO worker.
	 */
	public YoloWorker getYoloWorker() {
		return (YoloWorker) getModelWorker();
	}


	@Override
	public void prepModel() {
		setWorkerParams(getYoloWorker(), getDLParams());
		super.prepModel();
	}

	/**
	 * Set the detection threshold and bounding box merge parameters in a worker.
	 * @param worker - the YOLO worker.
	 * @param params - the parameters. These must implement YoloDetectionParams.
	 */
	public static void setWorkerParams(YoloWorker worker, StandardModelParams params) {
		if (!(params instanceof YoloDetectionParams)) return;

		YoloDetectionParams yoloParams = (YoloDetectionParams) params;

		worker.setThreshold((float) yoloParams.getMinConfidence());

		if (yoloParams.isMergeOverlap()) {
			//set the minimum overlap threshold for merging bounding boxes
			worker.setMinOverlapThreshold(yoloParams.getMinMergeOverlap());
		}
		else {
			//if not merging bounding boxes, set the threshold to 1 so that no boxes are merged.
			worker.setMinOverlapThreshold(1.0f);
		}
	}


	@Override
	protected  ArrayList<ArrayList<? extends PredictionResult>>  processModelResults(ArrayList<? extends PamDataUnit> groupedRawData, List<StandardPrediction> modelResult) {
		//the main difference between YOLO and most other models is that multiple results can be returned per segment.
		//Therefore the number of predictions does not correspond to the number of input data units

		YoloModel yoloModel = getYoloWorker().getYoloModel();
		double[] freqLims = getYoloWorker().getImageFreqLimits();

		ArrayList<ArrayList<? extends PredictionResult>>  processedResults = new ArrayList<ArrayList<? extends PredictionResult>>();
		//iterate through segments and results, matching the segment UID to the parent segment ID in the result.
		for (PamDataUnit dataUnit : groupedRawData) {

			ArrayList<YoloPrediction> aSegmentResult = new ArrayList<YoloPrediction>();

			double[] segFreqLims = freqLims;
			if (segFreqLims==null) {
				segFreqLims = new double[] {0, dataUnit.getParentDataBlock().getSampleRate()/2.0};
			}

			for (int i=0; i<modelResult.size(); i++) {

				YoloPrediction prediction = (YoloPrediction) modelResult.get(i);

				if (dataUnit.getUID() == prediction.getParentSegmentID()) {
					prediction.setClassNameID(GenericDLClassifier.getClassNameIDs(getDLParams()));

					//the detection threshold has already been applied by the model
					prediction.setBinaryClassification(true);

					setBoxLimits(prediction, dataUnit, yoloModel.getImageWidth(), yoloModel.getImageHeight(), segFreqLims);

					aSegmentResult.add(prediction);
				}
			}

			processedResults.add(aSegmentResult);
		}

		return processedResults;
	}

	/**
	 * Set the start time, duration and frequency limits of a prediction from its
	 * bounding box. The bounding box is in MATLAB pixel coordinates i.e. the image
	 * spans 0.5 to width+0.5 pixels and the first pixel is centred on 1.
	 *
	 * @param prediction  - the prediction.
	 * @param segment     - the segment the prediction was made from.
	 * @param imWidth     - the width of the image (time axis) in pixels.
	 * @param imHeight    - the height of the image (frequency axis) in pixels.
	 * @param freqLims    - the minimum and maximum frequency of the image in Hz.
	 */
	public static void setBoxLimits(YoloPrediction prediction, PamDataUnit segment, int imWidth, int imHeight, double[] freqLims) {

		//the start and end of the box as a fraction of the image width.
		double startFrac = clamp((prediction.getX() - 0.5) / imWidth);
		double endFrac = clamp((prediction.getX() + prediction.getWidth() - 0.5) / imWidth);

		prediction.setTimeMillis(segment.getTimeMilliseconds() + (long) (startFrac * segment.getDurationInMilliseconds()));
		prediction.setStartSample(segment.getStartSample() + (long) (startFrac * segment.getSampleDuration()));
		prediction.setDuratioSamples((int) ((endFrac - startFrac) * segment.getSampleDuration()));

		//the top of the image is the highest frequency.
		double topFrac = clamp((prediction.getY() - 0.5) / imHeight);
		double bottomFrac = clamp((prediction.getY() + prediction.getHeight() - 0.5) / imHeight);

		double bandWidth = freqLims[1] - freqLims[0];
		prediction.setFreqLimits(new double[] {freqLims[1] - bottomFrac*bandWidth, freqLims[1] - topFrac*bandWidth});
	}

	private static double clamp(double frac) {
		return Math.max(0, Math.min(1, frac));
	}

}
