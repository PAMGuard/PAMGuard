package rawDeepLearningClassifier.dlClassification.delphinID;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.json.JSONArray;
import org.json.JSONObject;

import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image.Whistle2ImageParams;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

/**
 * Handles transforming whistles or clicks into whichever transform is required by delphinID. 
 */
public class DelphinIDTransform {

	/**
	 * Convert whistles to image. 
	 */
	public static final int WHISTLE_IMAGE = 0;

	/**
	 * Convert whistles to a spectrum. 
	 */
	public static final int WHISTLE_SPECTRUM = 1;


	/**
	 * Convert click group to spectrum
	 */
	public static final int CLICK_IMAGE = 2;


	/**
	 * Convert click group to image. 
	 */
	public static final int CLICK_SPECTRUM = 3;


	/**
	 * The type of whistle transforms. 
	 */
	public int delphinIDTransformType = -1; 

	/**
	 * Parameters for the whistle transform.. 
	 */
	public Object transformParams = null;


	public DelphinIDTransform( ) {

	}

	/**
	 * Set the JSON data. 
	 * @param jsonObject - the json object
	 * @return true if JSON was read successfully. 
	 */
	public boolean setJSONData(JSONObject jsonObject) {

		//first parse the transforms.
		JSONArray jsonArray = jsonObject.getJSONArray("transforms"); 

		JSONObject jsonObjectParams; 
		for (int i=0; i<jsonArray.length(); i++) {

			String transformName = (String) jsonArray.getJSONObject(i).get("name"); 

			if (transformName.trim().equals("whistles2image")) {

				jsonObjectParams  = (JSONObject) jsonArray.getJSONObject(i).get("params"); 

				transformParams = readWhistleImageTransform( jsonObjectParams);

				return true;
			}
			if (transformName.trim().equals("whistles2spectrum")) {

				jsonObjectParams  = (JSONObject) jsonArray.getJSONObject(i).get("params"); 

				transformParams = readWhistleSpectrumTransform( jsonObjectParams);

				return true;
			}

		}

		return false;
	}


	/**
	 * Set the whsitle data. 
	 * @param whistleGroups
	 * @param dlTransform
	 */
	public void setWhistleData(SegmenterDetectionGroup whistleGroups, DLTransform dlTransform) {

		switch (delphinIDTransformType) {

		case WHISTLE_IMAGE:
			//create the first transform and set then whistle data. Note that the absolute time limits are
			//contained within the SegmenterDetectionGroup unit. 
			Whistles2Image whistles2Image = new Whistles2Image(whistleGroups, (Whistle2ImageParams) transformParams);
			//set the spec transform
			((FreqTransform) dlTransform).setSpecTransfrom(whistles2Image.getSpecTransfrom());
			
			break;
		case WHISTLE_SPECTRUM:
			
			((SpectrumTransform) dlTransform).setSpectrum(whistles2Image.getSpecTransfrom());

			
			break;
		}


	}


	/**
	 * Read the whistle transform settings- this is not included in the JPAM library because it directly 
	 * reference PAMGuard specific detections. 
	 */
	private Whistle2ImageParams readWhistleImageTransform(JSONObject jsonObjectParams) {

		double[] freqLimits = new double[2]; 
		double[] size = new double[2];
		freqLimits[0] = jsonObjectParams.getFloat("minfreq"); 
		freqLimits[1] = jsonObjectParams.getFloat("maxfreq"); 
		size[0] = jsonObjectParams.getInt("widthpix"); 
		size[1] = jsonObjectParams.getInt("heightpix"); 
		double minfragmillis = jsonObjectParams.getDouble("minfragmillis"); 

		double lineWidth = jsonObjectParams.getDouble("linewidthpix"); 

		Whistle2ImageParams whistle2ImageParmas = new Whistle2ImageParams();
		whistle2ImageParmas.freqLimits = freqLimits;
		whistle2ImageParmas.size = size;
		whistle2ImageParmas.lineWidth = lineWidth;
		whistle2ImageParmas.minFragSize = minfragmillis;

		return whistle2ImageParmas;
	}




	private Double readWhistleSpectrumTransform(JSONObject jsonObjectParams) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the transform parameters. 
	 * @return 
	 */
	public Object getTransformParams() {
		return transformParams;
		
	}


}
