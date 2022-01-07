package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.util.ArrayList;

import org.jamdev.jdl4pam.ketos.KetosModel;
import org.jamdev.jdl4pam.ketos.KetosParams;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;

import PamModel.PamModel;
import PamModel.PamModel.PluginClassloader;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;

/**
 * 
 * Runs a Ketos deep learning model and performs feature extraction.
 * <p>
 *  
 * @author Jamie Macaulay 
 *
 */
public class KetosWorker extends DLModelWorker<KetosResult> {


	/**
	 * The ketos model 
	 */
	private KetosModel ketosModel; 


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
		ClassLoader origCL = Thread.currentThread().getContextClassLoader();
		try {

			// get the plugin class loader and set it as the context class loader
			// NOTE THAT THIS IS REQUIRED TO MAKE THIS MODULE RUN AS A PLUGIN WHEN THE CLASS FILES
			// ARE BUNDLED INTO A FATJAR, HOWEVER THIS WILL STOP THE PLUGIN FROM RUNNING AS A SEPARATE
			// PROJECT IN ECLIPSE.  So while testing the code and debugging, make sure the 
			if (DLControl.PLUGIN_BUILD) {
				PluginClassloader newCL = PamModel.getPamModel().getClassLoader();
				Thread.currentThread().setContextClassLoader(newCL);
			}
			//first open the model and get the correct parameters. 
			ketosModel = new KetosModel(new File(ketosDLParams.modelPath)); 
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



			///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
			for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
				if (ketosParams.dlTransforms.get(i).dltransfromType == DLTransformType.SPEC2DB) {
					ketosParams.dlTransforms.get(i).params = null; 
					//System.out.println("Set dB transform to null: " + ketosParams.dlTransforms.get(i).params);
				}
			}
		
			//generate the transforms from the KetosParams objectts. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(ketosParams.dlTransforms); 
			
			///HACK here for now to fix an issue with dB and Ketos transforms having zero length somehow...
			for (int i=0; i<ketosParams.dlTransforms.size(); i++) {
				System.out.println(ketosParams.dlTransforms.get(i)); 
			}

			//System.out.println("Ketos transforms: " + transforms); 
			

			//set the transforms. 
			setModelTransforms(transforms); 

			//ketosDLParams.dlTransfroms = transforms; //this is done after prep model in the settings pane. 
			ketosDLParams.defaultSegmentLen = ketosParams.seglen*1000.; //the segment length in microseconds. 
			//ketosParams.classNames = new String[] {"Noise", "Right Whale"}; // FIXME; 
			ketosDLParams.numClasses = (int) ketosModel.getOutShape().get(1); 

			//ok 0 the other values are not user selectable but this is. If we relaod the same model we probably want to keep it....
			//So this is a little bt of a hack but will probably be OK in most cases. 
			if (ketosDLParams.binaryClassification==null || ketosDLParams.binaryClassification.length!=ketosDLParams.numClasses) {
				ketosDLParams.binaryClassification = new boolean[ketosDLParams.numClasses]; 
				for (int i=0; i<ketosDLParams.binaryClassification.length; i++) {
					ketosDLParams.binaryClassification[i] = true; //set default to true. 
				}
			}


			//			if (dlParams.classNames!=null) {
			//				for (int i = 0; i<dlParams.classNames.length; i++) {
			//					System.out.println("Class name " + i + "  "  + dlParams.classNames[i]); 
			//				}
			//			}
			//			ketosDLParams.classNames = dlControl.getClassNameManager().makeClassNames(ketosParams.classNames); 
			//
			//						if (ketosParams.classNames!=null) {
			//							for (int i = 0; i<ketosDLParams.classNames.length; i++) {
			//								System.out.println("Class name " + i + "  "  + ketosDLParams.classNames[i].className + " ID " + ketosDLParams.classNames[i].ID ); 
			//							}
			//						}

		}
		catch (Exception e) {
			ketosModel=null; 
			e.printStackTrace();
			//WarnOnce.showWarning(null, "Model Metadata Error", "There was an error extracting the metadata from the model.", WarnOnce.OK_OPTION); 
		}
		Thread.currentThread().setContextClassLoader(origCL);
	}



	@Override
	public float[] runModel(float[][][] transformedDataStack) {
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
	}


	/**
	 * Get the currently loaded mode. 
	 * @return - the currently loaded mode. 
	 */
	public KetosModel getModel() {
		return ketosModel;
	}


}
