package rawDeepLearningClassifier.dlClassification.genericModel;

import java.io.File;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;
import pamViewFX.fxNodes.PamButton;
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

	/**
	 * The extension filter for sound spot models. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters;


	private GenericAdvPane advPane;


	private GenericDLClassifier genericDLClassifier;



	public GenericModelPane(GenericDLClassifier genericDLClassifier) {
		super(genericDLClassifier);

		this.genericDLClassifier = genericDLClassifier;

		//must add an additional import settings button. 
		extensionFilters = new ArrayList<ExtensionFilter>(); 

		//import the settings holder
		extensionFilters.add(new ExtensionFilter("TensorFlow Model", "*.pb")); 
		extensionFilters.add(new ExtensionFilter("Pytorch Model", 	"*.pk"));

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
		super.setParams(currParams);
	}

	@Override
	public void newModelSelected(File file) {
		this.setCurrentSelectedFile(file);
		this.genericDLClassifier.newModelSelected(file); 

		//this.setParamsClone(new GenericModelParams()); 
		//prep the model with current parameters; 
		genericDLClassifier.getGenericDLWorker().prepModel(getParams(getParamsClone()), genericDLClassifier.getDLControl());
		//get the model transforms calculated from the model by SoundSpoyWorker and apply them to our temporary paramters clone. 
		//getParamsClone().dlTransfroms = this.genericDLClassifier.getGenericDLWorker().getModelTransforms(); 
		///set the advanced pane parameters. 

		//now new parameters have been set in the prepModel functions so need to set new params now. 
		getAdvSettingsPane().setParams(getParamsClone());
	}


	@Override
	public ArrayList<ExtensionFilter> getExtensionFilters() {
		return extensionFilters;
	}
}


