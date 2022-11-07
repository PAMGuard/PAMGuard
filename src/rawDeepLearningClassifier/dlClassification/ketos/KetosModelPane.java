package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;


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
		
		extensionFilters = new ArrayList<ExtensionFilter>(); 
		//import the settings holder
		extensionFilters.add(new ExtensionFilter("Ketos Model", "*.ktpb")); 
	}

	@Override
	public ArrayList<ExtensionFilter> getExtensionFilters() {
		return extensionFilters;
	}

	@Override
	public void newModelSelected(File file) {
		
		//A ketos model contains information on the transforms, duration and the class names. 
		
		this.setCurrentSelectedFile(file);

		this.setParamsClone(new KetosDLParams()); 
		
		//prep the model with current parameters; 
		ketosClassifier.getKetosWorker().prepModel(getParams(getParamsClone()), ketosClassifier.getDLControl());
		//get the model transforms calculated from the model by SoundSpoyWorker and apply them to our temporary params clone. 
//		System.out.println("Ketos transforms 1: " +  this.ketosClassifier.getKetosWorker().getModelTransforms());
		getParamsClone().dlTransfroms = this.ketosClassifier.getKetosWorker().getModelTransforms(); 
		
		if (getParamsClone().defaultSegmentLen!=null) {
			usedefaultSeg.setSelected(true);
		}
				
		//System.out.println("Ketos: " + getParamsClone().dlTransfroms.size());
		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());
		
		
		
	}

}
