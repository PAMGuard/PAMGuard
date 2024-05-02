package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.genericmodel.GenericModelParams;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformParser2;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.json.JSONObject;

import PamUtils.PamArrayUtils;
import PamView.dialog.warn.WarnOnce;
import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

/**
 * 
 * Runs a Ketos deep learning model and performs feature extraction.
 * <p>
 *  
 * @author Jamie Macaulay 
 *
 */
public class ArchiveModelWorker extends GenericModelWorker {


	/**
	 * The ketos model 
	 */
	private ArchiveModel dlModel;

	/**
	 * Thelast loaded model path., 
	 */
	private String currentPath; 


	/**
	 * SoundSpotWorker constructor. 
	 */
	public ArchiveModelWorker() {
		this.setEnableSoftMax(false);
	}

	/**
	 * Prepare the model.
	 * Note it is important to put a synchonized here or the model loading can fail. 
	 */
	@Override
	public synchronized void prepModel(StandardModelParams dlParams, DLControl dlControl) {
		//ClassLoader origCL = Thread.currentThread().getContextClassLoader();
		try {

			// get the plugin class loader and set it as the context class loader
			// NOTE THAT THIS IS REQUIRED TO MAKE THIS MODULE RUN AS A PLUGIN WHEN THE CLASS FILES
			// ARE BUNDLED INTO A FATJAR, HOWEVER THIS WILL STOP THE PLUGIN FROM RUNNING AS A SEPARATE
			// PROJECT IN ECLIPSE.  So while testing the code and debugging, make sure the 
			//				if (DLControl.PLUGIN_BUILD) {
			//					PluginClassloader newCL = PamModel.getPamModel().getClassLoader();
			//					Thread.currentThread().setContextClassLoader(newCL);
			//				}
			//first open the model and get the correct parameters. 
			//21/11/2022 - Added a null and filename check here to stop the mdoel reloading everytime PAMGuard hits a new file or 
			//is stopped or started - this was causing a memory leak. 
			if (dlModel==null || currentPath ==null || !Paths.get(currentPath).equals(Paths.get(dlParams.modelPath))) {


				//TODO
				//					if (ketosModel!=null && ketosModel.getModel()!=null) {
				//						ketosModel.getModel().close();
				//					}

				//System.out.println(Paths.get(genericParams.modelPath)); 
				this.currentPath = dlParams.modelPath; 
				dlModel = loadModel(currentPath);
				//System.out.println("LOAD A NEW MODEL: "); 
				//System.out.println(genericModel.getModel().getModelPath().getFileName()); 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//WarnOnce.showWarning(null, "Model Load Error", "There was an error loading the model file.", WarnOnce.OK_OPTION); 
		}

		try {

			//read the JSON string from the the file. 
			String jsonString  = DLTransformsParser.readJSONString(new File(dlModel.getAudioReprFile()));
			

			//convert the JSON string to a parameters object. 
			GenericModelParams modelParams = makeModelParams( jsonString);
			
			//important to add this for Ketos models because the JSON string does not necessarily contain and output shape. 
			//System.out.println("----Default output shape: " + ketosParams.defaultOutputShape + "  " + ketosModel.getOutShape()); 
			if (modelParams.defaultOutputShape==null) {
				modelParams.defaultOutputShape = dlModel.getOutShape();
			}

			//HACK there seems to be some sort of bug in ketos where the params input shape is correct but the model input shape is wrong. 
			if (dlModel.getInputShape()==null || !dlModel.getInputShape().equals(modelParams.defaultInputShape)) {
				System.err.println("Model input shape does not match: params: " + modelParams.defaultInputShape + " model: " + dlModel.getInputShape());
				//WarnOnce.showWarning("Model shape", "The model shape does not match the model metadata. \n Metadata shape will be used used.", WarnOnce.OK_OPTION);
				dlModel.setInputShape(modelParams.defaultInputShape);
			}


			//				///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
			//				for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
			//					if (ketosParams.dlTransforms.get(i).dltransfromType == DLTransformType.SPEC2DB) {
			//						ketosParams.dlTransforms.get(i).params = null; 
			//						//System.out.println("Set dB transform to null: " + ketosParams.dlTransforms.get(i).params);
			//					}
			//				}

			//generate the transforms from the KetosParams objects. 
			System.out.println(modelParams.dlTransforms);
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(modelParams.dlTransforms); 

//			///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
//			for (int i=0; i<modelParams.dlTransforms.size(); i++) {
//				System.out.println(modelParams.dlTransforms.get(i)); 
//			}

			//only load new transforms if defaults are selected
			if (getModelTransforms()==null || dlParams.dlTransfroms==null || dlParams.useDefaultTransfroms) {
				//only set the transforms if they are null - otherwise handled elsewhere. 
				setModelTransforms(transforms); 
				dlParams.useDefaultTransfroms = true; 
				dlParams.dlTransfroms=transforms;
			}
			else {
				//use the old transforms. 
				setModelTransforms(dlParams.dlTransfroms); 
			}
			
			//set whether a wave or spectrogram model 
			//this is important for setting the input stack into the model.
			 setWaveFreqModel(dlParams);
			
//			//enable softmax? - TODO
//			this.setEnableSoftMax(true);

			//ketosDLParams.dlTransfroms = transforms; //this is done after prep model in the settings pane. 
			dlParams.defaultSegmentLen = modelParams.segLen; //the segment length in microseconds. 
			
			if (modelParams.classNames!=null) {
				dlParams.numClasses =  modelParams.classNames.length;
			}
			else {
				dlParams.numClasses = (int) modelParams.defaultOutputShape.get(1);
			}

			//ok 0 the other values are not user selectable but this is. If we relaod the same model we probably want to keep it....
			//So this is a little bit of a hack but will probably be OK in most cases. 
			if (dlParams.binaryClassification==null || dlParams.binaryClassification.length!=dlParams.numClasses) {
				dlParams.binaryClassification = new boolean[dlParams.numClasses]; 
				for (int i=0; i<dlParams.binaryClassification.length; i++) {
					dlParams.binaryClassification[i] = true; //set default to true. 
				}
			}

			if (modelParams.classNames!=null && dlControl!=null) {
				 dlParams.classNames = dlControl.getClassNameManager().makeClassNames(modelParams.classNames); 
			}
			else {
				//set the number of class names from the default output shape
				dlParams.numClasses = (int) modelParams.defaultOutputShape.get(1);
			}
		}
		catch (Exception e) {
			dlModel=null; 
			e.printStackTrace();
			//WarnOnce.showWarning(null, "Model Metadata Error", "There was an error extracting the metadata from the model.", WarnOnce.OK_OPTION); 
		}
		//Thread.currentThread().setContextClassLoader(origCL);
	}

	/**
	 * Load a model from a file
	 * @param currentPath- the path to the model.
	 * @return- the loaded model object. 
	 * @throws MalformedModelException
	 * @throws IOException
	 */
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException {
		
		System.out.println("HELLO MODEL: " +currentPath2 );
		return new SimpleArchiveModel(new File(currentPath2)); 
	}

	/**
	 * Create the parameters from a JSON string. 
	 * @param jsonString - the json string. 
	 * @return the paramters. 
	 */
	public GenericModelParams makeModelParams(String jsonString) {

		//Need to figure out if this is the new or old format. 
		JSONObject jsonObject = new JSONObject(jsonString);

		boolean isParamsV2 =DLTransformParser2.isParamsV2(jsonObject); 

		//standard format. 
		GenericModelParams params = DLTransformParser2.readJSONParams(jsonObject);

		return params; 
	}



//	@Override
//	public float[] runModel(float[][][] transformedDataStack) {
//		System.out.println("Model input: " + transformedDataStack.length + "  " + transformedDataStack[0].length + " " + transformedDataStack[0][0].length);
//		return dlModel.runModel(transformedDataStack);
//	}


	@Override
	public StandardPrediction makeModelResult(float[]  prob, double time) {
		StandardPrediction prediction =  new StandardPrediction(prob); 
		prediction.setAnalysisTime(time);
		return prediction;
	}


	/**
	 * Destroy the model. 
	 */
	public void closeModel() {
		this.currentPath = null; 
	}


	/**
	 * Get the currently loaded mode. 
	 * @return - the currently loaded mode. 
	 */
	@Override
	public ArchiveModel getModel() {
		return dlModel;
	}
	
	protected void setModel(ArchiveModel dlModel) {
		this.dlModel = dlModel;
	}

	@Override
	public boolean isModelNull() {
		return dlModel==null;
	}


}