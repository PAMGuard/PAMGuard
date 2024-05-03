package rawDeepLearningClassifier.dlClassification.delphinID;


import java.io.File;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import PamguardMVC.PamDataUnit;
import ai.djl.Model;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image.Whistle2ImageParams;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

/**
 * 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDWorker extends  ArchiveModelWorker {

	/**
	 * Parameters for the whistle to image transform. 
	 */
	private Whistle2ImageParams whistleImageParams;


	@Override
	public void prepModel(StandardModelParams dlParams, DLControl dlControl) {
		//most of the model prep is done in the perent class. 
		super.prepModel(dlParams, dlControl);

		//now have to read the whsitle2image transform to get correct parameters for that. 
		String jsonString  = DLTransformsParser.readJSONString(new File(this.getModel().getAudioReprFile()));
		whistleImageParams = readWhistleImageTransform(new JSONObject(jsonString)) ;
		if (whistleImageParams==null) {
			System.err.println("Error: could not find whistle2image transform in DelphinID JSON file. Model will not work.");
			this.setModel(null); // set model to null to make sure nothing works and errors are thrown
		}
		
		dlParams.binaryClassification = new boolean[dlParams.numClasses];
		for (int i=0; i<dlParams.numClasses; i++) {
			dlParams.binaryClassification[i]=true;
		}
	}


	/**
	 * Read the whistle transform settings- this is not included in the JPAM library because it directly 
	 * reference PAMGuard specific detections. 
	 */
	private Whistle2ImageParams readWhistleImageTransform(JSONObject mainObject) {
		//first parse the transforms.
		JSONArray jsonArray = mainObject.getJSONArray("transforms"); 

		JSONObject jsonObjectParams; 
		for (int i=0; i<jsonArray.length(); i++) {

			String transformName = (String) jsonArray.getJSONObject(i).get("name"); 

			if (transformName.trim().equals("whistles2image")) {

				jsonObjectParams  = (JSONObject) jsonArray.getJSONObject(i).get("params"); 

				double[] freqLimits = new double[2]; 
				double[] size = new double[2];
				freqLimits[0] = jsonObjectParams.getFloat("minfreq"); 
				freqLimits[1] = jsonObjectParams.getFloat("maxfreq"); 
				size[0] = jsonObjectParams.getInt("widthpix"); 
				size[1] = jsonObjectParams.getInt("heightpix"); 

				Whistle2ImageParams whistle2ImageParmas = new Whistle2ImageParams();
				whistle2ImageParmas.freqLimits = freqLimits;
				whistle2ImageParmas.size = size;

				return whistle2ImageParmas;
			}

		}
		//something has gone wrong if we get here. 
		return null; 
	}


	@Override
	public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){
		//Get a list of of the model transforms. 
		ArrayList<DLTransform> modelTransforms = getModelTransforms();

		@SuppressWarnings("unchecked")
		ArrayList<SegmenterDetectionGroup> whistleGroups = (ArrayList<SegmenterDetectionGroup>) dataUnits;

		//the number of chunks. 
		int numChunks = whistleGroups.size(); 

		//data input into the model - a stack of spectrogram images. 
		float[][][] transformedDataStack = new float[numChunks][][]; 

		double[][] transformedData2; //spectrogram data
		for (int j=0; j<numChunks; j++) {

			System.out.println("Number of whisltes to process: " + whistleGroups.get(j).getSubDetectionsCount() + "  " + whistleGroups.get(j).getSegmentStartMillis());
			//create the first transform and set then whistle data. Note that the absolute time limits are
			//contained within the SegmenterDetectionGroup unit. 
			Whistles2Image whistles2Image = new Whistles2Image(whistleGroups.get(j), whistleImageParams);

			//set the spec transform
			((FreqTransform) this.getModelTransforms().get(0)).setSpecTransfrom(whistles2Image.getSpecTransfrom());

			//process all the transforms. 
			DLTransform transform = modelTransforms.get(0); 
			for (int i =0; i<modelTransforms.size(); i++) {
				transform = modelTransforms.get(i).transformData(transform); 
			}
			
			transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
			transformedDataStack[j] = DLUtils.toFloatArray(transformedData2); 
		}


		return transformedDataStack;
	} 


}
