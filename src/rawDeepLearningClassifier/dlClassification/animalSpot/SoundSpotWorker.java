package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.nio.file.Paths;

import org.jamdev.jdl4pam.animalSpot.AnimalSpotModel;
import org.jamdev.jdl4pam.animalSpot.AnimalSpotParams;

import ai.djl.engine.EngineException;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;


/**
 * 
 * Runs the deep learning model and performs feature extraction.
 * <p>
 *  
 * 
 * @author Jamie Macaulay 
 *
 */
public class SoundSpotWorker extends DLModelWorker<StandardPrediction> {


	/**
	 * Sound spot model. 
	 */
	private AnimalSpotModel soundSpotModel;


	private String currentPath; 


	/**
	 * SoundSpotWorker constructor. 
	 */
	public SoundSpotWorker() {

	}

	/**
	 * Prepare the model 
	 */
	public DLStatus prepModel(StandardModelParams soundSpotParams, DLControl dlControl) {
		//ClassLoader origCL = Thread.currentThread().getContextClassLoader();

		//System.out.println("prepModel: " + soundSpotParams.useDefaultTransfroms); 

		try {

			//			// get the plugin class loader and set it as the context class loader
			//			// NOTE THAT THIS IS REQUIRED TO MAKE THIS MODULE RUN AS A PLUGIN WHEN THE CLASS FILES
			//			// ARE BUNDLED INTO A FATJAR, HOWEVER THIS WILL STOP THE PLUGIN FROM RUNNING AS A SEPARATE
			//			// PROJECT IN ECLIPSE.  So while testing the code and debugging, make sure the 
			//			if (DLControl.PLUGIN_BUILD) {
			//				PluginClassloader newCL = PamModel.getPamModel().getClassLoader();
			//				Thread.currentThread().setContextClassLoader(newCL);
			//			}
			if (soundSpotModel==null || currentPath ==null || !Paths.get(currentPath).equals(Paths.get(soundSpotParams.modelPath))) {
				//System.out.println("Sound spot path: " + soundSpotParams.modelPath);
				//first open the model and get the correct parameters. 
				soundSpotModel = new AnimalSpotModel(soundSpotParams.modelPath); 
				this.currentPath = soundSpotParams.modelPath; 

			}
		}
		catch (EngineException e) {
			return DLStatus.MODEL_ENGINE_FAIL;
		}
		catch (Exception e) {
			e.printStackTrace();
			return DLStatus.MODEL_LOAD_FAIL;
			//WarnOnce.showWarning(null, "Model Load Error", "There was an error loading the model file.", WarnOnce.OK_OPTION); 
		}

		try {
			//create the DL parameters.
			AnimalSpotParams dlParams = new AnimalSpotParams(soundSpotModel.getTransformsString());

			//only load new transforms if defaults are selected
			if (getModelTransforms()==null || soundSpotParams.dlTransfroms==null || soundSpotParams.useDefaultTransfroms) {
				//only set the transforms if they are null - otherwise handled elsewhere. 
				setModelTransforms(model2DLTransforms(dlParams)); 
				soundSpotParams.useDefaultTransfroms = true; 
			}
			else {
				//use the old transforms. 
				setModelTransforms(soundSpotParams.dlTransfroms); 
			}

			soundSpotParams.defaultSegmentLen = dlParams.seglen; //the segment length in microseconds. 
			soundSpotParams.numClasses = dlParams.classNames.length; 

			//ok 0 the other values are not user selectable but this is. If we relaod the same model we probably want to keep it....
			//So this is a little bt of a hack but will probably be OK in most cases. 
			if (soundSpotParams.binaryClassification==null || soundSpotParams.binaryClassification.length!=soundSpotParams.numClasses) {
				soundSpotParams.binaryClassification = new boolean[soundSpotParams.numClasses]; 
				for (int i=0; i<soundSpotParams.binaryClassification.length; i++) {
					soundSpotParams.binaryClassification[i] = true; //set default to true. 
				}
			}
			//			if (dlParams.classNames!=null) {
			//				for (int i = 0; i<dlParams.classNames.length; i++) {
			//					System.out.println("Class name " + i + "  "  + dlParams.classNames[i]); 
			//				}
			//			}
			soundSpotParams.classNames = dlControl.getClassNameManager().makeClassNames(dlParams.classNames); 

			//			if (dlParams.classNames!=null) {
			//				for (int i = 0; i<soundSpotParams.classNames.length; i++) {
			//					System.out.println("Class name " + i + "  "  + soundSpotParams.classNames[i].className + " ID " + soundSpotParams.classNames[i].ID ); 
			//				}
			//			}
			//TODO
			//need to load the classifier metadata here...
			//System.out.println("Model transforms: " + this.modelTransforms.size());
		}
		catch (Exception e) {
			soundSpotModel=null; 
			e.printStackTrace();
			return DLStatus.MODEL_META_FAIL;

			//WarnOnce.showWarning(null, "Model Metadata Error", "There was an error extracting the metadata from the model.", WarnOnce.OK_OPTION); 
		}

		return DLStatus.MODEL_LOAD_SUCCESS;

		//Thread.currentThread().setContextClassLoader(origCL);
	}



	@Override
	public float[] runModel(float[][][] transformedDataStack) {
		return soundSpotModel.runModel(transformedDataStack);
	}


	@Override
	public SoundSpotResult makeModelResult(float[]  prob, double time) {
		SoundSpotResult soundSpotResult =  new SoundSpotResult(prob); 
		soundSpotResult.setAnalysisTime(time);
		return soundSpotResult;
	}


	/**
	 * Destroy the model. 
	 */
	public void closeModel() {
		//TODO - need to be able to access model in JPAM API.
		this.currentPath = null; 
	}


	/**
	 * Get the currently loaded mode. 
	 * @return - the currently loaded mode. 
	 */
	public AnimalSpotModel getModel() {
		return soundSpotModel;
	}


	@Override
	public boolean isModelNull() {
		return soundSpotModel==null;
	}


}
