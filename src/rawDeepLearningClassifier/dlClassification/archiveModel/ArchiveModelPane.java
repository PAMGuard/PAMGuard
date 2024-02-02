package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.File;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.ketos.KetosDLParams;

public class ArchiveModelPane extends StandardModelPane {

	private ArchiveModelClassifier archiveModelClassifier;

	public ArchiveModelPane(ArchiveModelClassifier archiveModelClassifier) {
		super(archiveModelClassifier);
		this.archiveModelClassifier = archiveModelClassifier; 

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
		archiveModelClassifier.getDLWorker().prepModel(params, archiveModelClassifier.getDLControl());
		//get the model transforms calculated from the model by SoundSpoyWorker and apply them to our temporary params clone. 
//		System.out.println("Ketos transforms 1: " +  this.ketosClassifier.getKetosWorker().getModelTransforms());
		getParamsClone().dlTransfroms = this.archiveModelClassifier.getDLWorker().getModelTransforms(); 
		
//		if (getParamsClone().defaultSegmentLen!=null) {
//			usedefaultSeg.setSelected(true);
//		}
				
//		System.out.println("Ketos: new model selected " + getParamsClone().dlTransfroms.size());
		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());
		
		
		
	}

}
