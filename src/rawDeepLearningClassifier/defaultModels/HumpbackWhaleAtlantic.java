package rawDeepLearningClassifier.defaultModels;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;

import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

/**
 * Atlantic version of Google's humpback whale network. 
 */
public class HumpbackWhaleAtlantic implements DLModel {

	@Override
	public String getDescription() {
		return "Detects Atlantic Humpback whales";
	}

	@Override
	public String getName() {
		return "Humpback Whale Atlantic";
	}

	@Override
	public String getCitation() {
		return "Vincent Kather, Fabian Seipel, Benoit Berges, Genevieve Davis, Catherine Gibson, Matt Harvey, Lea-Anne Henry, Andrew Stevenson, Denise Risch; Development of a machine learning detector for North Atlantic humpback whale song. J. Acoust. Soc. Am. 1 March 2024; 155 (3): 2050–2064.";
	}

	@Override
	public URI getModelURI() {
		try {
//			return new URI("https://github.com/PAMGuard/deeplearningmodels/raw/master/humpback_whale_2/humpback_whale_2.zip");
			return new URI("https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/humpback_whale_2.zip");
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
			return new URI("https://doi.org/10.1121/10.0025275");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(Serializable dlModelSettings) {
		GenericModelParams genericModelParams = (GenericModelParams) dlModelSettings;
		
		//decimation value
		float sr = 2000; 

		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE_SCIPY, sr)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		
		genericModelParams.defaultSegmentLen =   3.8775*1000;
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
