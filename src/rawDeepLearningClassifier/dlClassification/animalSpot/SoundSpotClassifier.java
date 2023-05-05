package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.io.Serializable;
import java.net.URI;

import java.util.ArrayList;

import org.jamdev.jdl4pam.animalSpot.AnimalSpotModel;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * A deep learning classifier running models wihc have been created using the
 * AnimalSpot framework .
 * <p>
 * AnimalSpot uses Pytorch models which contains a JSON string for the
 * transforms Users can select a .py file - PAMGaurd will open it, find the
 * embedded JSON string and set up the transforms.
 * <p>
 * <Previous comments on why to use native Java rather than calling python> This
 * method has numerous advantages, it greatly simplifies the setup, which
 * requires only that the pytorch TorchScript library is installed and that the
 * library location is added to the virtual machine arguments e.g.
 * -Djava.library.path=/Users/au671271/libtorch/lib
 * <p>
 * It also means that np python code is called which greatly increases speed.
 * 
 * @author JamieMacaulay
 *
 */
public class SoundSpotClassifier extends StandardClassifierModel {

	/**
	 * The user interface for sound spot. 
	 */
	private SoundSpotUI soundSpotUI; 

	/**
	 * Sound spot parameters. 
	 */
	private StandardModelParams soundSpotParmas;


	/**
	 * The deep learning model worker.
	 */
	private DLModelWorker<GenericPrediction> soundSpotWorker;


	public SoundSpotClassifier(DLControl dlControl) {
		super(dlControl); 
		this.soundSpotParmas = new StandardModelParams(); 
		this.soundSpotUI= new SoundSpotUI(this); 
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);

//		System.out.println("LOADED CLASS NAMES: currParams.classNames: " + soundSpotParmas.classNames); 

	}


	@Override
	public String getName() {
		return "AnimalSpot";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return soundSpotUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return soundSpotParmas;
	}

	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_SoundSpot"; 
	}

	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_SoundSpot";
	}

	@Override
	public Serializable getSettingsReference() {
		if (soundSpotParmas==null) {
			soundSpotParmas = new StandardModelParams(); 
		}

		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(soundSpotParmas.dlTransfroms);

		soundSpotParmas.dlTransfromParams=dlTransformParams; 


		//System.out.println("SoundSpot have been saved. : " + soundSpotParmas.classNames); 
		return soundSpotParmas;

	}


	@Override
	public long getSettingsVersion() {
		return StandardModelParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		StandardModelParams newParameters = (StandardModelParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			soundSpotParmas = newParameters.clone();
			//System.out.println("SoundSpot have been restored. : " + soundSpotParmas.classNames); 
			if (soundSpotParmas.dlTransfromParams!=null) {
				soundSpotParmas.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>) soundSpotParmas.dlTransfromParams); 
			}
		}
		else soundSpotParmas = new StandardModelParams(); 
		return true;
	}

	/**
	 * Get the sound spot parameters. 
	 * @return sound spot parameters. 
	 */
	public StandardModelParams getSoundSpotParams() {
		return soundSpotParmas;
	}

	/**
	 * Set the sound spot parameters. 
	 * @param the params to set 
	 */
	public void setSoundSpotParams(StandardModelParams soundSpotParmas) {
		this.soundSpotParmas=soundSpotParmas; 
	}



	@Override
	public boolean isModelType(URI uri) {
		//TODO need to be more sophisticated here. 
		if (super.isModelExtensions(uri)) {
			//we have a PyTorch model but is it animal spot.
			
			try {
				AnimalSpotModel soundSpotModel = new AnimalSpotModel(uri.getPath()); 
				if (soundSpotModel!=null && soundSpotModel.getExtraFiles()!=null) {
					return true;
				}
				return false; 
			}
			
			catch (Exception e) {
				return false;
			}
			
		}
		
		return false; 
	}


	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		if (soundSpotWorker==null) {
			soundSpotWorker = new SoundSpotWorker(); 
		}
		return soundSpotWorker; 
	}



	@Override
	public StandardModelParams getDLParams() {
		return soundSpotParmas;
	}



//	/**
//	 * Check whether a model passes a binary test...
//	 * @param modelResult - the model results
//	 * @return the model results. 
//	 */
//	private boolean isBinaryResult(GenericPrediction modelResult) {
//		for (int i=0; i<modelResult.getPrediction().length; i++) {
//			if (modelResult.getPrediction()[i]>soundSpotParmas.threshold && soundSpotParmas.binaryClassification[i]) {
//				//System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
//				return true; 
//			}
//		}
//		return  false;
//	}





	//	public class TaskThread extends Thread {
	//
	//		private AtomicBoolean run = new AtomicBoolean(true);
	//
	//		TaskThread() {
	//			super("TaskThread");
	//			if (soundSpotWorker == null) {
	//				//create the daemons etc...
	//				soundSpotWorker = new SoundSpotWorker(); 	
	//			}
	//		}
	//
	//		public void stopTaskThread() {
	//			run.set(false);  
	//			//Clean up daemon.
	//			if (soundSpotWorker!=null) {
	//				soundSpotWorker.closeModel();
	//			}
	//			soundSpotWorker = null; 
	//		}
	//
	//		public void run() {
	//			while (run.get()) {
	//				//				System.out.println("ORCASPOT THREAD while: " + "The queue size is " + queue.size()); 
	//				try {
	//					if (queue.size()>0) {
	//						//						System.out.println("ORCASPOT THREAD: " + "The queue size is " + queue.size()); 
	//						ArrayList<GroupedRawData> groupedRawData = queue.remove(0);
	//
	//						ArrayList<SoundSpotResult> modelResult = getSoundSpotWorker().runModel(groupedRawData, 
	//								groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); //TODO channel?
	//
	//						for (int i =0; i<modelResult.size(); i++) {
	//							modelResult.get(i).setClassNameID(getClassNameIDs()); 
	//							modelResult.get(i).setBinaryClassification(isBinaryResult(modelResult.get(i))); 
	//							newResult(modelResult.get(i), groupedRawData.get(i));
	//						}
	//
	//					}
	//					else {
	//						//						System.out.println("ORCASPOT THREAD SLEEP: "); ; 
	//						Thread.sleep(10);
	//						//						System.out.println("ORCASPOT THREAD DONE: "); ; 
	//					}
	//
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//
	//	}

//	/**
//	 * Get the class name IDs
//	 * @return an array of class name IDs
//	 */
//	private short[] getClassNameIDs() {
//		if (soundSpotParmas.classNames==null || soundSpotParmas.classNames.length<=0) return null; 
//		short[] nameIDs = new short[soundSpotParmas.classNames.length]; 
//		for (int i = 0 ; i<soundSpotParmas.classNames.length; i++) {
//			nameIDs[i] = soundSpotParmas.classNames[i].ID; 
//		}
//		return nameIDs; 
//	}



}
