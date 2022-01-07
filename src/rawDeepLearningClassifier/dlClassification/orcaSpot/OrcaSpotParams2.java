package rawDeepLearningClassifier.dlClassification.orcaSpot;

import java.io.File;
import java.io.Serializable;

/**
 * Stored settings for the OrcaSpot module. 
 * 
 * @author Jamie Macaulay
 *
 */
public class OrcaSpotParams2 implements Serializable, Cloneable {

	/**
	 * 
	 */
	public static final long serialVersionUID = 3L;

	
//	//TODO Decision and Counter
////	public static int Threads;
//	public static int OrcaCounter = 0;
//	
//	public static int NoiseCounter = 0;
	
		
	/**
	 * The master path to the segmentor python folder. 
	 */
	public String segmenterMasterPath =  "E:\\DeepLearning_OrcaSpot\\Segmenter";
	
	/***Python Scripts***/
	
	/**
	 * Location of the python.exe file.
	 */
	private String pythonExeFile =  "pytorch/my-venv/Scripts/python.exe";
	
//	public String pythonExeFile = "C:/Users/macst/PycharmProjects/python_test/venv/Scripts/python.exe";
	
	/**
	 * Python script for running the initial prediction stage
	 */
	private String predict_script  =  "orca-spot/src/predict.py";
	
	/**
	 * Python script for running the later classification stage. 
	 */
	private String predict2_script =  "r18_m2_a1_c7_n_run1/orca_spot_call_type/src/predict.py"; 
	
	/**
	 * Python Script running Segmentation stage
	 */
	private String segmenter_script =  "r18_m2_a1_c7_n_run1/classifier.pk";
	
	/***Classifier Models****/
	
	/**
	 * The initial prediction models
	 */
	private String model =  "r18_m2_a1_c7_n_run1/classifier.pk"; 
	
	/**
	 * The classifier model
	 */
	private String classifier_model =  "r18_m2_a1_c7_n_run1/call_type.pk"; 
	
//	/**
//	 * A Logfile for current Debbuging.
//	 */
//	public String log = segmenterMasterPath;
	
	/**
	 * True to use the detector
	 */
	public boolean useDetector; 

	
	/**
	 * Use the classifier 
	 */
	public boolean useClassifier = true;
	
	
	/**
	 * The threshold to apply to the initial prediction stage
	 */
	public String threshold = Double.toString(0.92); 
	
	/**
	 * The threshold to apply to the later classification stage. 
	 */
	public String threshold2 = Double.toString(0.8); 

	/****Graphics card***/
	
	/**
	 * Use the graphics card.
	 */
	public boolean cuda = false; 
	
	/**
	 * Number of workers?
	 */
	public String num_workers = Integer.toString(0); 
	
	
	/***Non user changeable***/
	
	public File audio_file; 
	
	/**
	 * The length of audio to analyse in seconds (This should be set the same as length of input file and is not user changeable);
	 */
	public String seq_len = "2"; 
	
	/**
	 * The hop size to use in seconds (This should be set the same as length of input file and is not use changeable)
	 */
	public String hop_size = "2";


	/**
	 * The location of .exe file that runs the Deep Learning classifier. 
	 */
	public String classiferFile = ""; 
	
	/**
	 * The classification mode. 
	 * 0 means only segmentation Orca no Orca
	 * 1 means segmentation and classification Orca; Type n something, 
	 * 2 means only Classification Type n something.
	 **/
	public String mode  = "0"; 
	
	/**
	 * The sample rate in samples per second
	 */
	public String sampleRate = "44100";


	/**
	 * Get the current classification mode.
	 * 0 means only segmentation Orca no Orca
	 * 1 means segmentation and classification Orca; Type n something, 
	 * 2 means only Classification Type n something.
	 * return the classification mode
	 */
	public String getMode() {
		return mode;
	}
	
	/**
	 * Get the sample rate in samples per second
	 * @return the sample rate. 
	 */
	public String getSample_rate() {
		return this.sampleRate;
	}

	public String getThreshold() {
		return threshold;
	}
	
	public String getThreshold2() {
		return threshold2;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	/**
	 * Get the path to the PythonExe file in the correct virtual environment. 
	 * @return the Python exe file path. 
	 */
	public String getPythonExe() {
		return segmenterMasterPath +"/" +  this.pythonExeFile; 
	}
	
	/**
	 * Get the main prediction script for running the detector and classifier. 
	 * @return the prediction script file path. 
	 */
	public String getPredict_script() {
		return segmenterMasterPath + "/" + predict_script;
	}
	
	public String getClass_script() {
		return segmenterMasterPath + "/" + predict2_script;
	}

	public String getSegmenter_script() {
		return segmenterMasterPath+"/" + segmenter_script;
	}
	
	/**
	 * Path to the log for OrcaSpot. 
	 * @return
	 */
	public String getLog_Path() {
		return segmenterMasterPath;
	}
	
	
	/*** Detection and Classification Models***/
	
	public String getDetectorModel() {
		return segmenterMasterPath +"/" + model;
	}
	
	public String getClassifierModel() {
		return segmenterMasterPath + "/" +classifier_model;
	}
	

	/** Audio Params***/
	
	public String getSeq_len() {
		return seq_len;
	}

	public void setSeq_len(String seq_len) {
		this.seq_len = seq_len;
	}

	public String getHop_size() {
		return hop_size;
	}

	public void setHop_size(String hop_size) {
		this.hop_size = hop_size;
	}

	
	public Boolean getcuda() {
		return cuda;
	}

	public void setcuda(boolean cuda) {
		this.cuda = cuda;
	}


	public String getNum_workers() {
		return num_workers;
	}

	
	@Override
	public OrcaSpotParams2 clone() {
		OrcaSpotParams2 newParams = null;
		try {
			newParams = (OrcaSpotParams2) super.clone();
//			if (newParams.spectrogramNoiseSettings == null) {
//				newParams.spectrogramNoiseSettings = new SpectrogramNoiseSettings();
//			}
//			else {
//				newParams.spectrogramNoiseSettings = this.spectrogramNoiseSettings.clone();
//			}
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}

	public void updateAllPaths() {
		// TODO Auto-generated method stub
		
	}



	
}
