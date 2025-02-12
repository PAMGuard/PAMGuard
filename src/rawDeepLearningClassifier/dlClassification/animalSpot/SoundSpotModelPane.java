package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.io.File;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.DLStatus;

public class SoundSpotModelPane extends StandardModelPane {

	/**
	 * Reference to the currently selected sound spot classifier. 
	 */
	private SoundSpotClassifier soundSpotClassifier;



	public SoundSpotModelPane(SoundSpotClassifier soundSpotClassifier) {
		super(soundSpotClassifier);
		// TODO Auto-generated constructor stub
		this.soundSpotClassifier=soundSpotClassifier; 

	}
	
	
	/**
	 * Called whenever a new model has been selected
	 * @param file - the selected file. 
	 */
	@Override
	public DLStatus newModelSelected(File file) {
		
		//System.out.println("New  file model selected:"); 
		
		this.setCurrentSelectedFile(file);
		
		if (this.getParamsClone()==null) {
			this.setParamsClone(new StandardModelParams()); 
		}
		
		//prep the model with current parameters; 
		StandardModelParams params  = getParams(getParamsClone()); 
		
	
//		if (params.dlTransfromParams!=null) {
//			System.out.println("AnimalSpot: Decimator: " + params.dlTransfromParams.get(0).params[0]); 
//		}
//		else {
//			System.out.println("AnimalSpot: params is null" + getParamsClone().dlTransfromParams); 
//		}
		
		/**
		 * Note that the model prep will determine whether new transforms need to be loaded from the 
		 * model or to use the existing transforms in the settings. 
		 */
		DLStatus status = soundSpotClassifier.getDLWorker().prepModel(params, soundSpotClassifier.getDLControl());
		//get the model tansforms calculated from the model by SoundSpoyWorker and apply them to our temporary params clone. 
		getParamsClone().dlTransfroms = this.soundSpotClassifier.getDLWorker().getModelTransforms(); 
		
	
//		if (getParamsClone().defaultSegmentLen!=null) {
//			usedefaultSeg.setSelected(true);
//		}
		
		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());
		
		return status;
				

	}


}
