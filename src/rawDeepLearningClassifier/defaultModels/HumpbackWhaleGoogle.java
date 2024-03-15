package rawDeepLearningClassifier.defaultModels;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;

import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

public class HumpbackWhaleGoogle implements DLModel {

	@Override
	public String getDescription() {
		return "A model developed by Google to detect Humpback whales (Megaptera novaeangliae) in the Pacific ocean";
	}

	@Override
	public String getName() {
		return "Humpback Whale Pacific";
	}

	@Override
	public String getCitation() {
		return "A. N. Allen et al., ‘A Convolutional Neural Network for Automated Detection of Humpback Whale Song in a Diverse, Long-Term Passive Acoustic Dataset’, Front. Mar. Sci., vol. 8, p. 607321, Mar. 2021";
	}

	@Override
	public URI getModelURI() {
		try {
			return new URI("https://github.com/PAMGuard/deeplearningmodels/raw/master/humpback_whale_1/humpback_whale_1.zip");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getModelName() {
		return "saved_model.pb";
	}
	
	@Override
	public URI getCitationLink() {
		try {
			return new URI("https://doi.org/10.3389/fmars.2021.607321");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(Serializable dlModelSettings) {
		GenericModelParams genericModelParams = (GenericModelParams) dlModelSettings;
		
		//decimation value
		float sr = 10000; 

		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, sr)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		
		genericModelParams.defaultSegmentLen =  3.92*1000;
		genericModelParams.binaryClassification = new boolean[] {true};
		genericModelParams.classNames= new DLClassName[] {new DLClassName("Humpback whale", (short) 1)};
		genericModelParams.numClasses = 1; 
		
		
		genericModelParams.defaultShape= new Long[] {-1L,-1L,-1L,1L};
		genericModelParams.shape= new Long[] {-1L,-1L,-1L,1L};

		//create the transforms. 
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 
		
		genericModelParams.setExampleSound(ExampleSoundType.HUMPBACK_WHALE);

		
	}
	

}

