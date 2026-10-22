package rawDeepLearningClassifier.dlClassification.yolo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsResult;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;

import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;



/**
 * The YoloWorker class extends ArchiveModelWorker to handle the execution of a
 * YOLO object detection model. It processes audio data units, runs the model by
 * converting the usual 3D transformed data into a 4D array (because a
 * colourised image is required), and converts the results into a list of
 * YoloPrediction objects. Each input segment can have zero or more
 * predictions.
 *
 * @author Jamie Macaulay
 */
public class YoloWorker extends ArchiveModelWorker {

	/**
	 * The minimum overlap threshold for combining bounding boxes. 1 means that boxes are not merged.
	 */
	private float minOverlapThreshold = 0.20f;

	/**
	 * The minimum score for a detection.
	 */
	private float threshold = 0.5f;


	public YoloWorker() {
		super();
	}

	@Override
	public synchronized ArrayList<StandardPrediction> runModel(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan) {

		try {
			float[][][] transformedDataStack  = dataUnits2ModelInput(dataUnits,  sampleRate,  iChan);

			//convert to the image input for the model e.g. a colourised image of the correct size.
			float[][][][] transformedDataStack4D = getYoloModel().formatDataStack4D(transformedDataStack);

			getYoloModel().setThreshold(threshold);

			List<DeepAcousticResultArray> modelResults = getYoloModel().runModel(transformedDataStack4D);

			if (modelResults == null) {
				//the model failed to run (see the error logged by YoloModel.runModel). Return null here
				//rather than falling through to a confusing NullPointerException on modelResults.size().
				System.err.println("YoloWorker: the model returned no results - the model failed to run on the input data.");
				return null;
			}

			List<DeepAcousticsResult> flattenedModelResults = new 	ArrayList<DeepAcousticsResult>();

			//the parent IDs of the input data units, which will be used to set the parent ID of the model results.
			ArrayList<Long> parentID = new ArrayList<Long>();

			for (int i=0; i<modelResults.size(); i++) {
				List<DeepAcousticsResult> segmentResults = modelResults.get(i);
				if (minOverlapThreshold!=1) {
					//combine overlapping boxes within a segment.
					segmentResults = BoundingBoxMerger.combineBoxes(segmentResults, minOverlapThreshold);
				}
				//flatten the results into a single list
				flattenedModelResults.addAll(segmentResults);
				for (int j = 0;j < segmentResults.size(); j++) {
					//record which input data unit this result came from.
					parentID.add(dataUnits.get(i).getUID());
				}
			}

			//now we need to convert the model results into a standard prediction object.
			ArrayList<StandardPrediction> dlModelResults = new ArrayList<StandardPrediction>();
			YoloPrediction dlModelResult;

			for (int i=0; i<flattenedModelResults.size(); i++) {
				//create a new YoloPrediction object from the model result
				dlModelResult = new YoloPrediction(flattenedModelResults.get(i));
				dlModelResult.setParentSegmentID(parentID.get(i)); //set the ID to the UID of the input data unit (which is usually the segment UID).
				dlModelResults.add(dlModelResult);
			}

			return dlModelResults;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the YOLO model.
	 * @return the YOLO model.
	 */
	public YoloModel getYoloModel() {
		return (YoloModel) getModel();
	}

	/**
	 * Get the frequency limits of the spectrogram image which is input into the
	 * model. This is taken from the last spectrogram transform.
	 *
	 * @return the minimum and maximum frequency of the image in Hz, or null if there
	 *         are no spectrogram transforms or the transforms have not been run.
	 */
	public double[] getImageFreqLimits() {
		if (getModelTransforms()==null) return null;
		for (int i=getModelTransforms().size()-1; i>=0; i--) {
			DLTransform transform = getModelTransforms().get(i);
			if (transform instanceof FreqTransform) {
				double[] freqLims = ((FreqTransform) transform).getFreqlims();
				if (freqLims==null || freqLims[1]<=freqLims[0]) return null;
				return freqLims;
			}
		}
		return null;
	}


	@Override
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException, EngineException {
		//Override here to return a YOLO model.
		return new YoloModel(new File(currentPath2));
	}

	/**
	 * Get the minimum overlap threshold for combining bounding boxes.
	 *
	 * @return The minimum overlap threshold. 1 means boxes are not merged.
	 */
	public float getMinOverlapThreshold() {
		return minOverlapThreshold;
	}

	/**
	 * Set the minimum overlap threshold for combining bounding boxes.
	 *
	 * @param minOverlapThreshold The minimum overlap threshold to set. 1 means boxes are not merged.
	 */
	public void setMinOverlapThreshold(float minOverlapThreshold) {
		this.minOverlapThreshold = minOverlapThreshold;
	}

	/**
	 * Get the minimum score for a detection.
	 *
	 * @return the minimum score (objectness multiplied by the maximum class
	 *         probability).
	 */
	public float getThreshold() {
		return threshold;
	}

	/**
	 * Set the minimum score for a detection.
	 *
	 * @param threshold - the minimum score (objectness multiplied by the maximum
	 *                  class probability).
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

}
