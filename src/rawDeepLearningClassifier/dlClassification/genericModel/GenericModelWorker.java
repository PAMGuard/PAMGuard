package rawDeepLearningClassifier.dlClassification.genericModel;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.jamdev.jdl4pam.genericmodel.GenericModel;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;

import PamModel.PamModel;
import PamModel.PamModel.PluginClassloader;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Generic model worker. 
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericModelWorker extends DLModelWorker<GenericPrediction> {

	/**
	 * The generic model 
	 */
	private PamGenericModel genericModel;
	
	/**
	 * Frequency transform. 
	 */
	private boolean freqTransform = true;

	@Override
	public float[] runModel(float[][][] transformedDataStack) {
		//System.out.println("RUN GENERIC MODEL: " + transformedDataStack.length +  "  " + transformedDataStack[0].length +  "  " + transformedDataStack[0][0].length);
		//System.out.println("RUN GENERIC MODEL: " + transformedDataStack[0][0][0]);
		float[]  results; 
		if (freqTransform)
			results =  genericModel.runModel(transformedDataStack);
		else {
			//run a model if it is waveform info. 
			float[][] waveStack = new float[transformedDataStack.length][]; 
			for (int i=0; i<waveStack.length; i++) {
				waveStack[i] = transformedDataStack[i][0]; 
			}
			results =  genericModel.runModel(waveStack);
		}
		//System.out.println("GENERIC MODEL RESULTS: " + results== null ? null : results.length);
		return results;
	}

	@Override
	public GenericPrediction makeModelResult(float[] prob, double time) {
		GenericPrediction model = new  GenericPrediction(prob);
		model.setAnalysisTime(time);
		return model;
	}

	@Override
	public void prepModel(StandardModelParams genericParams, DLControl dlControl) {
		//ClassLoader origCL = Thread.currentThread().getContextClassLoader();
		try {
			if (genericParams.modelPath==null) return; 
			
			// get the plugin class loader and set it as the context class loader
			// NOTE THAT THIS IS REQUIRED TO MAKE THIS MODULE RUN AS A PLUGIN WHEN THE CLASS FILES
			// ARE BUNDLED INTO A FATJAR, HOWEVER THIS WILL STOP THE PLUGIN FROM RUNNING AS A SEPARATE
			// PROJECT IN ECLIPSE.  So while testing the code and debugging, make sure the 
//			if (DLControl.PLUGIN_BUILD) {
//				PluginClassloader newCL = PamModel.getPamModel().getClassLoader();
//				Thread.currentThread().setContextClassLoader(newCL);
//			}
			
//			if (genericModel!=null) {
//					System.out.println(Paths.get(genericModel.getModel().getName())); 
//					System.out.println(Paths.get(genericParams.modelPath)); 
//					System.out.println(Paths.get(genericModel.getModel().getName()).equals(Paths.get(genericParams.modelPath)));
//			}
					
			//first open the model and get the correct parameters.
			//21/11/2022 - Added a null and filename check here to stop the model reloading everytime PAMGuard hits a new file or 
			//is stopped or started - this was causing a memory leak. 
			if (genericModel==null || !Paths.get(genericModel.getModel().getName()).equals(Paths.get(genericParams.modelPath))) {
				
				if (genericModel!=null && genericModel.getModel()!=null) {
					genericModel.getModel().close();
				}
				//System.out.println(Paths.get(genericParams.modelPath)); 
				genericModel = new PamGenericModel(genericParams.modelPath); 

				//System.out.println("LOAD A NEW MODEL: "); 
				//System.out.println(genericModel.getModel().getModelPath().getFileName()); 
			}
			
			setModelTransforms(genericParams.dlTransfroms); 

			//is this a waveform or a spectrogram model?
			DLTransform transform = genericParams.dlTransfroms.get(genericParams.dlTransfroms.size()-1); 
			if (transform instanceof FreqTransform) {
				freqTransform = true; 
			}
			else {
				freqTransform = false; 
			}
			
			//use softmax or not?
			String extension = FilenameUtils.getExtension(genericParams.modelPath);
			if (extension.equals("pb")) {
				//TensorFlow models don't need softmax?? Need to look into this more. 
				this.setEnableSoftMax(false);
			}
			else {
				this.setEnableSoftMax(true);
			}
						
//			GenericModelParams genericModelParams = new GenericModelParams(); 
			
			((GenericModelParams) genericParams).defaultShape = longArr2Long(genericModel.getInputShape().getShape()); 
			((GenericModelParams) genericParams).defualtOuput = longArr2Long(genericModel.getOutShape().getShape()); 
			

		}
		catch (Exception e) {
			genericModel=null; 
			e.printStackTrace();
			//WarnOnce.showWarning(null, "Model Load Error", "There was an error loading the model file.", WarnOnce.OK_OPTION); 
		}
		
		//Thread.currentThread().setContextClassLoader(origCL);
	}
	
	/**
	 * Convert a long[] array to a Long[] array. 
	 * @param inArr - the input long[] array. 
	 * @return - copy of the input as Long objects instead of primitives. 
	 */
	private Long[] longArr2Long(long[] inArr) {
		if (inArr==null) return null; 
		Long[] longArr = new Long[inArr.length];
		
		for (int i=0; i<longArr.length ; i++) {
			longArr[i] = inArr[i]; 
		}
		return longArr; 
	}

	@Override
	public void closeModel() {
//		can be very important to prevent memory leak for long term processing. 
		if (genericModel!=null && genericModel.getModel()!=null) {
			genericModel.getModel().close();
		}
	}

	/**
	 * Generic model. 
	 * @return the generic model. 
	 */
	public PamGenericModel getModel() {
		return genericModel;
	}

	@Override
	public boolean isModelNull() {
		return genericModel==null;
	}

}
