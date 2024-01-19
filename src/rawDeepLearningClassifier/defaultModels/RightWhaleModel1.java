package rawDeepLearningClassifier.defaultModels;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Right whale model from Shiu et al. 2019
 * <p>
 *  * <p>
 * Shiu, Y., Palmer, K.J., Roch, M.A., Fleishman, E., Liu, X., Nosal, E.-M., Helble, T., Cholewiak, D., Gillespie, D., Klinck, H., 2020. 
 * Deep neural networks for automated detection of marine mammal species. Scientific Reports 10, 607. https://doi.org/10.1038/s41598-020-57549-y
 */
public class RightWhaleModel1 implements DLModel {

	@Override
	public String getDescription() {
		return "Detects right whales";
	}

	@Override
	public String getName() {
		return "Right Whale";
	}

	@Override
	public String getCitation() {
		return " Shiu, Y., Palmer, K.J., Roch, M.A., Fleishman, E., Liu, X., Nosal, E.-M., Helble, T., Cholewiak, D., Gillespie, D., Klinck, H., 2020."
				+ "  Deep neural networks for automated detection of marine mammal species. Scientific Reports 10, 607. https://doi.org/10.1038/s41598-020-57549-y";
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
	public StandardModelParams getModelSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelName() {
		return "saved_model.pb";
	}
	
	

}
