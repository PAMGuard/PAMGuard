package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.ketos.KetosModel;
import org.jamdev.jdl4pam.ketos.KetosParams;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;

import PamView.dialog.warn.WarnOnce;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

/**
 * 
 * Runs a Ketos deep learning model and performs feature extraction.
 * <p>
 *  
 * @author Jamie Macaulay 
 *
 */
public class KetosWorker extends DLModelWorker<StandardPrediction> {


	/**
	 * The ketos model 
	 */
	private KetosModel ketosModel;
	
	/**
	 * Thelast loaded model path., 
	 */
	private String currentPath; 


	/**
	 * SoundSpotWorker constructor. 
	 */
	public KetosWorker() {
		this.setEnableSoftMax(false);
	}

	/**
	 * Prepare the model 
	 */
	public void prepModel(StandardModelParams ketosDLParams, DLControl dlControl) {
		//ClassLoader origCL = Thread.currentThread().getContextClassLoader();
		try {

			// get the plugin class loader and set it as the context class loader
			// NOTE THAT THIS IS REQUIRED TO MAKE THIS MODULE RUN AS A PLUGIN WHEN THE CLASS FILES
			// ARE BUNDLED INTO A FATJAR, HOWEVER THIS WILL STOP THE PLUGIN FROM RUNNING AS A SEPARATE
			// PROJECT IN ECLIPSE.  So while testing the code and debugging, make sure the 
//			if (DLControl.PLUGIN_BUILD) {
//				PluginClassloader newCL = PamModel.getPamModel().getClassLoader();
//				Thread.currentThread().setContextClassLoader(newCL);
//			}
			//first open the model and get the correct parameters. 
			//21/11/2022 - Added a null and filename check here to stop the mdoel reloading everytime PAMGuard hits a new file or 
			//is stopped or started - this was causing a memory leak. 
			if (ketosModel==null || currentPath ==null || !Paths.get(currentPath).equals(Paths.get(ketosDLParams.modelPath))) {
				
				
				//TODO
//				if (ketosModel!=null && ketosModel.getModel()!=null) {
//					ketosModel.getModel().close();
//				}

				//System.out.println(Paths.get(genericParams.modelPath)); 
				this.currentPath = ketosDLParams.modelPath; 
				ketosModel = new KetosModel(new File(ketosDLParams.modelPath)); 
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
			String jsonString  = DLTransformsParser.readJSONString(new File(ketosModel.getAudioReprFile()));
			
			//convert the JSON string to a parameters object. 
			KetosParams ketosParams = new KetosParams(jsonString); 		
			
			//important to add this for Ketos models because the JSON string does not necessarily contain and output shape. 
			//System.out.println("----Default output shape: " + ketosParams.defaultOutputShape + "  " + ketosModel.getOutShape()); 
			if (ketosParams.defaultOutputShape==null) {
				ketosParams.defaultOutputShape = ketosModel.getOutShape();
			}
			
			//HACK there seems to be some sort of bug in ketos where the params input shape is correct but the model input shape is wrong. 
			if (ketosModel.getInputShape()==null || !ketosModel.getInputShape().equals(ketosParams.defaultInputShape)) {
				WarnOnce.showWarning("Model shape", "The model shape does not match the model metadata. \n Metadata shape will be used used.", WarnOnce.OK_OPTION);
				ketosModel.setInputShape(ketosParams.defaultInputShape);
			}
			
			
			///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
			for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
				if (ketosParams.dlTransforms.get(i).dltransfromType == DLTransformType.SPEC2DB) {
					ketosParams.dlTransforms.get(i).params = null; 
					//System.out.println("Set dB transform to null: " + ketosParams.dlTransforms.get(i).params);
				}
			}
		
			//generate the transforms from the KetosParams objects. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(ketosParams.dlTransforms); 
			
			///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
			for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
				System.out.println(ketosParams.dlTransforms.get(i)); 
			}
			
			//only load new transforms if defaults are selected
			if (getModelTransforms()==null || ketosDLParams.dlTransfroms==null || ketosDLParams.useDefaultTransfroms) {
				//System.out.println("  " + transforms); 
				//System.out.println("SET MODEL TRANSFORMS: " + ketosDLParams.dlTransfroms + "  " +  ketosDLParams.useDefaultTransfroms); 

				//only set the transforms if they are null - otherwise handled elsewhere. 
				setModelTransforms(transforms); 
				ketosDLParams.useDefaultTransfroms = true; 
			}
			else {
				//System.out.println("SET CURRENT TRANSFORMS: " + ketosDLParams.dlTransfroms + "  " +  ketosDLParams.useDefaultTransfroms); 
				//use the old transforms. 
				setModelTransforms(ketosDLParams.dlTransfroms); 
			}

			//ketosDLParams.dlTransfroms = transforms; //this is done after prep model in the settings pane. 
			ketosDLParams.defaultSegmentLen = ketosParams.segLen*1000.; //the segment length in microseconds. 
			//ketosParams.classNames = new String[] {"Noise", "Right Whale"}; // FIXME; 
			
									
			//ok 0 the other values are not user selectable but this is. If we relaod the same model we probably want to keep it....
			//So this is a little bt of a hack but will probably be OK in most cases. 
			if (ketosDLParams.binaryClassification==null || ketosDLParams.binaryClassification.length!=ketosDLParams.numClasses) {
				ketosDLParams.binaryClassification = new boolean[ketosDLParams.numClasses]; 
				for (int i=0; i<ketosDLParams.binaryClassification.length; i++) {
					ketosDLParams.binaryClassification[i] = true; //set default to true. 
				}
			}
			
			if (ketosParams.classNames!=null) {
				ketosDLParams.classNames = dlControl.getClassNameManager().makeClassNames(ketosParams.classNames); 
				ketosDLParams.numClasses = ketosDLParams.classNames.length;
			}
			else {
				//set the number of class names from the default output shape
				ketosDLParams.numClasses = (int) ketosParams.defaultOutputShape.get(1);
			}


//						if (dlParams.classNames!=null) {
//							for (int i = 0; i<dlParams.classNames.length; i++) {
//								System.out.println("Class name " + i + "  "  + dlParams.classNames[i]); 
//							}
//						}
//			ketosDLParams.classNames = dlControl.getClassNameManager().makeClassNames(ketosParams.classNames); 
//									if (ketosParams.classNames!=null) {
//										for (int i = 0; i<ketosDLParams.classNames.length; i++) {
//											System.out.println("Class name " + i + "  "  + ketosDLParams.classNames[i].className + " ID " + ketosDLParams.classNames[i].ID ); 
//										}
//									}

		}
		catch (Exception e) {
			ketosModel=null; 
			e.printStackTrace();
			//WarnOnce.showWarning(null, "Model Metadata Error", "There was an error extracting the metadata from the model.", WarnOnce.OK_OPTION); 
		}
		//Thread.currentThread().setContextClassLoader(origCL);
	}



	@Override
	public float[] runModel(float[][][] transformedDataStack) {
		System.out.println("Model input: " + transformedDataStack.length + "  " + transformedDataStack[0].length + " " + transformedDataStack[0][0].length);
		return ketosModel.runModel(transformedDataStack);
	}


	@Override
	public KetosResult makeModelResult(float[]  prob, double time) {
		KetosResult soundSpotResult =  new KetosResult(prob); 
		soundSpotResult.setAnalysisTime(time);
		return soundSpotResult;
	}


	/**
	 * Destroy the model. 
	 */
	public void closeModel() {
		//TODO
		this.currentPath = null; 
	}


	/**
	 * Get the currently loaded mode. 
	 * @return - the currently loaded mode. 
	 */
	public KetosModel getModel() {
		return ketosModel;
	}

	@Override
	public boolean isModelNull() {
		return ketosModel==null;
	}


}
