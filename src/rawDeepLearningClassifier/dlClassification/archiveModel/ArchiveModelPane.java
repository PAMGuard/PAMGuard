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
		
		//the model has to set some of the parameters for the UI . 
			
		//A ketos model contains information on the transforms, duration and the class names. 
		this.setCurrentSelectedFile(file);

		if (this.getParamsClone()==null) {
			this.setParamsClone(new KetosDLParams()); 
		}
		
			
		StandardModelParams params  = getParamsClone(); 
		
		
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
		
		//get the model transforms calculated from the model by the worker and apply them to our temporary params clone. 
		getParamsClone().dlTransfroms = this.archiveModelClassifier.getDLWorker().getModelTransforms(); 
		
//		if (getParamsClone().defaultSegmentLen!=null) {
//			usedefaultSeg.setSelected(true);
//		}
		
		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());
		
	}

}
