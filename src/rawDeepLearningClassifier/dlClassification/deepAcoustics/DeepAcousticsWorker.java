package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsResult;
import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

public class DeepAcousticsWorker extends ArchiveModelWorker {

	@Override
	public synchronized ArrayList<StandardPrediction> runModel(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan) {
		
		System.out.println("DeepAcousticsWorker: runModel called with " + dataUnits.size() + " data units, sample rate: " + sampleRate + ", channel: " + iChan);

		try {
			//PamCalendar.isSoundFile(); 
			//create an audio data object from the raw data chunk
			long timeStart = System.nanoTime(); 
			
			float[][][] transformedDataStack  = dataUnits2ModelInput(dataUnits,  sampleRate,  iChan);
			
			float[][][][] transformedDataStack4D = DeepAcousticsModel.formatDataStack4D(transformedDataStack);
			
			// convert to 4D array which has 3 dimensions for colour. However, the image is
			// greyscale so this third dimension is for compatibility but
			// is completely redundant.

			//run the model. 
			float[] output = null; 
			
			long time1 = System.currentTimeMillis();
			
			
			List<DeepAcousticResultArray> modelResults = getDeepAcousticsModel().runModel(transformedDataStack4D);
			
			
			System.out.println("DeepAcousticsWorker: Model out: " + modelResults.size() + " results for " + transformedDataStack4D.length + " input segments.");
			
			long time2 = System.currentTimeMillis();


			//			System.out.println(PamCalendar.formatDBDateTime(rawDataUnits.get(0).getTimeMilliseconds(), true) + 
			//					" Time to run model: " + (time2-time1) + " ms for spec of len: " + transformedDataStack.length + 
			//					"output: " + output.length + " " + numclasses); 
			
			//now we need to convert the model results into a standard prediction object.
			ArrayList<StandardPrediction> dlModelResults = new ArrayList<StandardPrediction>();
			DeepAcousticsResult modelResult;
			DeepAcousticsPrediction dlModelResult;
			
			//System.out.println("DeepAcousticsWorker: Model results size: " + modelResults.size());
			for (int i=0; i<modelResults.size(); i++) {
				for (int j=0; j<modelResults.get(i).size(); j++) {
					modelResult = modelResults.get(i).get(j);
					
					dlModelResult = new DeepAcousticsPrediction(modelResult);
					dlModelResult.setParentSegmentID(dataUnits.get(i).getUID()); //set the  ID to the UID of the input data unit (which is usually the segment UID).
					
					//add the result to the list
					dlModelResults.add(dlModelResult);
				}
			}
			System.out.println("DeepAcousticsWorker: dlModelResults out: " + dlModelResults.size());

			return dlModelResults;
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Prepare the model.
	 * Note it is important to put a synchronized here or the model loading can fail.
	 */
	private DeepAcousticsModel getDeepAcousticsModel() {
		return (DeepAcousticsModel) getModel();
	}
	
	
	@Override
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException, EngineException {
		//Override here to return a deep acoutics model. 
		return new DeepAcousticsModel(new File(currentPath2)); 
	}



}
