package rawDeepLearningClassifier.defaultModels;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 *
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
		return "A. N. Allen et al., ‘A Convolutional Neural Network for Automated Detection of Humpback Whale Song in a Diverse, Long-Term Passive Acoustic Dataset’, Front. Mar. Sci., vol. 8, p. 607321, Mar. 2021";
	}

	@Override
	public URI getModelURI() {
		try {
			return new URL("https://github.com/PAMGuard/deeplearningmodels/raw/master/right_whale_1/model_lenet_dropout_input_conv_all.zip").toURI();
		} catch (MalformedURLException | URISyntaxException e) {
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
			return new URL("https://doi.org/10.3389/fmars.2021.607321").toURI();
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(Serializable dlModelSettings) {
		// TODO Auto-generated method stub
		
	}
	
	

}
