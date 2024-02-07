package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

/**
 * Parameters for the SoundSpot model. 
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardModelParams implements Serializable, Cloneable {

	/**
	 * 
	 */
	public static final long serialVersionUID = 6L;
	
	/**
	 * The model path
	 */
	public String modelPath;

	/**
	 * True to use CUDA. 
	 */
	public boolean useCUDA = false;
	
	/**
	 * Use default transforms
	 */
	public boolean useDefaultTransfroms = false; 

	
	/**
	 * The threshold between zero and one. This is used to allow binary classification. 
	 */
	public double threshold = 0.9; 

	/*
	 * The number of output classes. 
	 */
	public int numClasses = 0; 
	
	/**
	 * The class names. e.g. porpoise, noise, bat
	 */
	public DLClassName[] classNames; 
	
	
	/**
	 * List of transforms for the raw data e.g. filtering, spectrogram, spectrogram normalisation etc. 
	 * This is only used for saving serialised settings
	 * 
	 */
	public List<DLTransfromParams> dlTransfromParams = null; 
	
	
	/**
	 * The DL custom transforms if the default transforms for the model are not being used. 
	 */
	public transient ArrayList<DLTransform> dlTransfroms = null;
	

	/**
	 * The default segment length of the model in milliseconds. 
	 */
	public 	Double defaultSegmentLen = null;
	
	
	/**
	 * Use the default segment length
	 */
	public 	boolean useDefaultSegLen = false;




	/**
	 * Which classes to apply binary classification to. 
	 */
	public boolean[] binaryClassification; 
	
	
	/**
	 * The index of the example sound which should be shown. 
	 * <p>
	 * Note: It is a little messy putting this here but otherwise would need
	 * a while new settings class for the advanced UI which would need to be linked to this 
	 * params class somehow and gets very complicated. 
	 */
	public int exampleSoundIndex = 0; 
	
	
	/**
	 * Set the example sound that should be associated with parameters. 
	 * @param soundType - the example sound type. 
	 */
	public void setExampleSound(ExampleSoundType soundType) {
		ExampleSoundType[] values = ExampleSoundType.values();
		for (int i=0; i<values.length; i++) {
			if (values[i].equals(soundType)) {
				exampleSoundIndex = i;
				return;
			}
		}
	}
	
	
	/**
	 * Set the URI in standard model params. 
	 * @param uri - the uri to a model
	 * @param standarModelParams - the standard model params. 
	 * @return the standard model params. 
	 */
	public static StandardModelParams setModel(URI uri, StandardModelParams standarModelParams) {
		standarModelParams.modelPath = new File(uri).getPath(); 
		return standarModelParams; 
	}
	
	@Override
	public StandardModelParams clone() {
		StandardModelParams newParams = null;
		try {
			newParams = (StandardModelParams) super.clone();
			
//			if (newParams.spectrogramNoiseSettings == null) {
//				newParams.spectrogramNoiseSettings = new SpectrogramNoiseSettings();
//			}
//			else {
//				newParams.spectrogramNoiseSettings = this.spectrogramNoiseSettings.clone();
//			}
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}
	
	@Override
	public String toString() {
		super.toString();
		String string = "-------Transforms------\n";
		if (dlTransfromParams==null) {
			string+="There are no transform params\n"; 
		}
		else {
			string += dlTransfromParams.size() + " transforms: " + "\n"; 
			for (int i=0; i<dlTransfromParams.size(); i++) {
				string+= dlTransfromParams.get(i).toString() + "\n"; 
			}
		}
		
		string+= "-------Class Names-------\n";
		string+= "Num classes: " + this.numClasses + "\n"; ;

		if (classNames==null) {
			string+="There are no class names params\n"; 
		}
		else {
			for (int i=0; i<classNames.length; i++) {
				string+= classNames[i].className + "\n"; 
			}
		}
		
		string+= "-------Classification-------\n";

		string+= "Threshold: " + threshold + "\n"; ;
		
		
		string+= "-------Segments-------\n";

		string+= "defaultSegmentLen: " + defaultSegmentLen + "\n"; ;

		return string; 

	}
	

}
