package rawDeepLearningClassifier.defaultModels;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

/**
 * Blue whale D-call detector from Miller et al. (2023), trained with Koogu.
 * <p>
 * The model is a Koogu .kgu file, which contains the TensorFlow saved model and the
 * metadata (250 Hz sample rate, 20 Hz high pass filter, normalisation and 4.5 s segments)
 * PAMGuard needs to set up the transforms. Only the class names, binary classification
 * and example sound are set here.
 * <p>
 * Miller, B. S., Madhusudhana, S., Aulich, M. G., Kelly, N., (2023). Deep learning algorithm
 * outperforms experienced human observer at detection of blue whale D-calls: a double-observer
 * analysis. Remote Sensing in Ecology and Conservation 9(1), 104–116. https://doi.org/10.1002/rse2.297
 */
public class BlueWhaleDCallMiller implements DLModel {

	@Override
	public String getDescription() {
		return "Detects Antarctic blue whale (Balaenoptera musculus) D-calls";
	}

	@Override
	public String getName() {
		return "Blue Whale D-call";
	}

	@Override
	public String getCitation() {
		return "Miller, B. S., Madhusudhana, S., Aulich, M. G., Kelly, N. Deep learning algorithm outperforms experienced human observer at detection of blue whale D-calls: a double-observer analysis. Remote Sensing in Ecology and Conservation 9(1), 104–116 (2023)";
	}

	@Override
	public URI getModelURI() {
		try {
			return new URI("https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/Koogu_Blue_Whale_BmD_2022_12_20.kgu");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getModelName() {
		return "Koogu_Blue_Whale_BmD_2022_12_20.kgu";
	}

	@Override
	public URI getCitationLink() {
		try {
			return new URI("https://doi.org/10.1002/rse2.297");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(Serializable dlModelSettings) {
		StandardModelParams modelParams = (StandardModelParams) dlModelSettings;

		//transforms and segment length come from the metadata inside the .kgu file.
		//Class 0 is Bm-D (blue whale D-call), class 1 is other.
		modelParams.classNames = new DLClassName[] {new DLClassName("Blue whale D-call", (short) 0, 180528), new DLClassName("Other", (short) 1)};
		modelParams.numClasses = 2;
		modelParams.binaryClassification = new boolean[] {true, false};

		modelParams.setExampleSound(ExampleSoundType.BLUE_WHALE);
	}

}
