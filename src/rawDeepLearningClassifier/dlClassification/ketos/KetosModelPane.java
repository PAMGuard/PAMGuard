package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;


/**
 * Settings pane for a Ketos classifier. 
 * @author Jamie Macaulay 
 *
 */
public class KetosModelPane extends StandardModelPane {

	private ArrayList<ExtensionFilter> extensionFilters;
	
	
	private KetosClassifier ketosClassifier;

	public KetosModelPane(KetosClassifier soundSpotClassifier) {
		super(soundSpotClassifier);
		this.ketosClassifier = soundSpotClassifier; 
		
	}



	@Override
	public void newModelSelected(File file) {
			
		//A ketos model contains information on the transforms, duration and the class names. 
		this.setCurrentSelectedFile(file);

		if (this.getParamsClone()==null) {
			this.setParamsClone(new KetosDLParams()); 
		}
		
			
		StandardModelParams params  = getParams(getParamsClone()); 
		
		
//		if (params.dlTransfromParams!=null) {
//			System.out.println("Ketos: Decimator: " + params.dlTransfromParams.get(0).params[0]); 
//		}
//		else {
//			System.out.println("Ketos:dltransform params is null" + getParamsClone().dlTransfromParams); 
//		}
		
		//prep the model with current parameters; 
		
		/**
		 * Note that the model prep will determine whether new transforms need to be loaded from the 
		 * model or to use the existing transforms in the settings. 
		 */
		ketosClassifier.getKetosWorker().prepModel(params, ketosClassifier.getDLControl());
		//get the model transforms calculated from the model by SoundSpoyWorker and apply them to our temporary params clone. 
//		System.out.println("Ketos transforms 1: " +  this.ketosClassifier.getKetosWorker().getModelTransforms());
		getParamsClone().dlTransfroms = this.ketosClassifier.getKetosWorker().getModelTransforms(); 
		
//		if (getParamsClone().defaultSegmentLen!=null) {
//			usedefaultSeg.setSelected(true);
//		}
				
//		System.out.println("Ketos: new model selected " + getParamsClone().dlTransfroms.size());
		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());
		
		
		
	}

}
