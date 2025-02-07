package rawDeepLearningClassifier.dlClassification.genericModel;

import java.io.File;
import pamViewFX.fxNodes.PamButton;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * 
 * Settings pane for the generic pane. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class GenericModelPane extends StandardModelPane  {



	private GenericAdvPane advPane;


	private GenericDLClassifier genericDLClassifier;



	public GenericModelPane(GenericDLClassifier genericDLClassifier) {
		super(genericDLClassifier);

		this.genericDLClassifier = genericDLClassifier;

		//this.getVBoxHolder().getChildren().add(2, new Label("Classifier Settings"));
		usedefaultSeg.setDisable(true); 
		defaultSegBox.setVisible(false);

		setAdvSettingsPane(advPane = new GenericAdvPane(genericDLClassifier)); 

		advPane.setParams(genericDLClassifier.getGenericDLParams());
	}


	@Override
	public void showAdvPane(PamButton advSettingsButton) {
		//need to set this because the params are shared between two settings. 
		//a little messy but meh

		//System.out.println("GenericModelPane - showAdvPane getParamsClone(): 1 " + getParamsClone());
		//System.out.println("GenericModelPane - showAdvPane getParamsClone(): 2 " + getParams(getParamsClone()));

		//set the params clone i.e. the temporary params class. 
		this.setParamsClone(getParams(getParamsClone())); 


		super.showAdvPane(advSettingsButton);

		//bit messy 
		this.popOver.setOnHidden((event)->{
			//need to set the class p
			
			//get all params updates from the adv pane.
			StandardModelParams newParams = getParams(getParamsClone());
			
			//System.out.println("GenericModelPane: Adv pane set: " + ((GenericModelParams) newParams).classNames[0].className); 
			//			System.out.println("Set params: binary classification len" + newParams.binaryClassification.length); 
			//			for (int i=0; i<newParams.binaryClassification.length; i++) {
			//				System.out.println("Set params: binary classification[i] " + newParams.binaryClassification[i]);
			//			}
			this.setParams(newParams); 
		});

	}

	@Override
	public void setParams(StandardModelParams currParams) {
//		System.out.println("SET PARAMS GENERIC PANE: " + currParams);

		super.setParams(currParams);
	}
	
	@Override
	public StandardModelParams getParams(StandardModelParams currParams) {
//		System.out.println("GET GENERIC PARAMS: " + currParams);

		return super.getParams(currParams);
	}

	@Override
	public DLStatus newModelSelected(File file) {
		this.setCurrentSelectedFile(file);
		
		//this.setParamsClone(new GenericModelParams()); 
		
		//prep the model with current parameters; 
		
//		genericDLClassifier.getGenericDLWorker().prepModel(getParams(getParamsClone()), genericDLClassifier.getDLControl());
		//do not have getParam here as it resets some of the setting before set params has been called.
		DLStatus status = genericDLClassifier.getGenericDLWorker().prepModel(getParamsClone(), genericDLClassifier.getDLControl());


		//now new parameters have been set in the prepModel functions so need to set new params now. 
		getAdvSettingsPane().setParams(getParamsClone());
		
		
		return status;
	}


}


