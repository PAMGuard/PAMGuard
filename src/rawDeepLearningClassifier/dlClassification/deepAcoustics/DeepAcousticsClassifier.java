package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;

import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;


/**
 * A classifier based on the Deep Acoustics method which uses object detection
 * models within spectrograms to predict dolphin whistle detections and then
 * classify to species.
 * 
 * This extends the ArchiveModelClassifier to provide functionality for
 * interpreting the results from the Deep Acoustics model, which detects objects
 * within input segments. Specifically the deep acoustics model can return
 * multiple results per segment and bounding boxes (i.e. time and frequency
 * limits of input data is not necassarily the same as the output) which is
 * different from most other models
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsClassifier extends ArchiveModelClassifier {

	public static String MODEL_NAME = "DeepAcoustics";


	public DeepAcousticsClassifier(DLControl dlControl) {
		super(dlControl);
		archiveModelUI = new DeepAcousticsUI(this);
	}


	@Override
	public String getName() {
		//important because this is used to identify model from JSON file
		return MODEL_NAME;
	}

	@Override
	public StandardModelParams makeParams() {
		return new DeepAcousticParams();
	}


	/**
	 * Get the KetosWorker. this handles loading and running the Ketos model. 
	 * @return the Ketos worker. 
	 */
	public ArchiveModelWorker getModelWorker() {
		if (archiveModelWorker==null) {
			archiveModelWorker= new DeepAcousticsWorker(); 
		}
		return archiveModelWorker;
	}


	@Override
	protected  ArrayList<ArrayList<? extends PredictionResult>>  processModelResults(ArrayList<? extends PamDataUnit> groupedRawData, List<StandardPrediction> modelResult) {
//		System.out.println("DeepAcousticsClassifier: processModelResults called with " + modelResult.size() + " results for " + groupedRawData.size() + " segments.");
		//the main difference between deepAcoustics and most other models is that multiple results can be returned per segment. 
		//Therefore the number of prediction does not correspond to the number of input data units
		DeepAcousticsPrediction deepAcousticsPrediction;
		
		//so to calculate the time we need the start time of the segment, the position of the bounding box in the segment and the segment length.

		int imWidth = (int) archiveModelWorker.getModel().getInputShape().get(1);
		int imHeight = (int) archiveModelWorker.getModel().getInputShape().get(2);
		long boxMillis;
		long boxSamples;
		
		ArrayList<ArrayList<? extends PredictionResult>>  processedResults = new ArrayList<ArrayList<? extends PredictionResult>>();
		//iterate through segments and results, matching the segment UID to the parent segment ID in the result.
		for (PamDataUnit dataUnit : groupedRawData) {

			ArrayList<DeepAcousticsPrediction> aSegmentResult = new ArrayList<DeepAcousticsPrediction>();
			
			for (int i=0; i<modelResult.size(); i++) {
				
				deepAcousticsPrediction = (DeepAcousticsPrediction) modelResult.get(i);
			
				if (dataUnit.getUID() == deepAcousticsPrediction.getParentSegmentID()) {
					//we have the correct segment for the bounding box. Now need to set the correct absolute sample and datetime values
					deepAcousticsPrediction.setClassNameID(GenericDLClassifier.getClassNameIDs(getDLParams())); 
					
					//set whether the results is a binary classification or not
					deepAcousticsPrediction.setBinaryClassification(isDecision(modelResult.get(i), getDLParams())); 
					deepAcousticsPrediction.setBinaryClassification(true); //TODO: this is a temporary fix to get the binary classification working.

					boxMillis = DeepAcousticResultArray.calcBoundingBoxMillis(dataUnit.getTimeMilliseconds(), dataUnit.getDurationInMilliseconds(), deepAcousticsPrediction.getResult(),  imWidth);
					boxSamples = DeepAcousticResultArray.calcBoundingBoxMillis(dataUnit.getStartSample(), dataUnit.getSampleDuration(), deepAcousticsPrediction.getResult(),  imWidth);
					
					deepAcousticsPrediction.setTimeMillis(boxMillis);

					deepAcousticsPrediction.setStartSample(boxSamples);
					
					double width = deepAcousticsPrediction.getResult().getWidth();
					//there appears to be a bug in the deep acoustics model where the bounding box getx plus the width is larger than the image width.
					if (deepAcousticsPrediction.getResult().getX() + deepAcousticsPrediction.getResult().getWidth() > imWidth) {
						width = imWidth - deepAcousticsPrediction.getResult().getX();
					}
					
					int boxSampleDuration = (int) (width/imWidth * dataUnit.getSampleDuration());
										
					deepAcousticsPrediction.setDuratioSamples(boxSampleDuration);
					
					//now set the correct frequency limits. 
					double startFreq =  (1-deepAcousticsPrediction.getResult().getY()/imHeight)*dataUnit.getParentDataBlock().getSampleRate()/2.0;
					double bandWidth =  (deepAcousticsPrediction.getResult().getHeight()/imHeight)*dataUnit.getParentDataBlock().getSampleRate()/2.0;

					deepAcousticsPrediction.setFreqLimits(new double[] { startFreq, startFreq - bandWidth});
					
//					System.out.println("DeepAcousticsClassifier: processModelResults boxStartSample "  + boxSamples + " boxSampleDuration " + boxSampleDuration + 
//							" for segment " + dataUnit.getUID() + " with "  + dataUnit.getSampleDurationAsInt() + " samples from "  + dataUnit.getStartSample()
//							+ " width " +  deepAcousticsPrediction.getResult().getWidth() + " x " + deepAcousticsPrediction.getResult().getX() ); 
					
					aSegmentResult.add(deepAcousticsPrediction);

				}
			}
			
			processedResults.add(aSegmentResult);
			
			//TODO
			//now the model results UID should be the UID of the input data. Also, the bounding box will have frequency and time limits and these need to be set. 
			//deepAcousticsPrediction.setTimeMillis(deepAcousticsPrediction.startTimeMillis);
		}
		
//		System.out.println("DeepAcousticsClassifier: processModelResults returned with " + processedResults.size() + " results for " + groupedRawData.size() + " segments.");
//		for (ArrayList<? extends PredictionResult> dataUnit : processedResults) {
//			System.out.println("DeepAcousticsClassifier: processModelResults returned with " + dataUnit.size() + " results for a segment.");
//		}

		return processedResults;
	}

}






