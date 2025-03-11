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
public class MultiSpeciesGoogle implements DLModel {

	@Override
	public String getDescription() {
		return "A model developed by Google to detect multiple cetacean species including Humpback, Minke, Bryde's, Blue, Fin and Right whales alongside Orca whistle, calls and echolocation clicks.";
	}

	@Override
	public String getName() {
		return "Multi Species Google";
	}

	@Override
	public String getCitation() {
		return "Allen, A.N. et al. (2024) ‘Bryde’s whales produce Biotwang calls, which occur seasonally in long-term acoustic recordings from the central and western North Pacific’, Frontiers in Marine Science, 11, p. 1394695"; 
	}

	@Override
	public URI getModelURI() {
		try {
			//				return new URI("https://github.com/PAMGuard/deeplearningmodels/raw/master/humpback_whale_2/humpback_whale_2.zip");
			return new URI("https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/multispecies-whale-tensorflow2-default-v2.zip");
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
			return new URI("https://doi.org/10.3389/fmars.2024.1394695 ");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(Serializable dlModelSettings) {
		GenericModelParams genericModelParams = (GenericModelParams) dlModelSettings;

		//decimation value
		float sr = 24000; 

		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//Performed long series of test son which decimator is best for upsampling and it is, by far, the DECIMATE transform. 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE, sr)); 

		genericModelParams.dlTransfromParams = dlTransformParamsArr;

		genericModelParams.defaultSegmentLen =   5. * 1000.;
		genericModelParams.binaryClassification = new boolean[] {true};
		// see table at https://www.kaggle.com/models/google/multispecies-whale
		genericModelParams.classNames= new DLClassName[] {
				new DLClassName("Oo", (short) 0, 180469), // Orca
				new DLClassName("Mn", (short) 1, 180529), // Humpback
				new DLClassName("Eg", (short) 2, 180537), // Right Whale (Atlantic)
				new DLClassName("Be", (short) 3, 180525), // Bryde's
				new DLClassName("Upcall", (short) 4, 612591), // Right Whale (Pacific)
				new DLClassName("Bp", (short) 5, 180527), // Fin Whale
				new DLClassName("Call", (short) 6, 180469), // Orca Call
				new DLClassName("Gunshot", (short) 7, 612591), // Right Whale Pacific
				new DLClassName("Echolocation", (short) 8, 180469), // Orca Echolocation
				new DLClassName("Bm", (short) 9, 180528), // Blue Whale
				new DLClassName("Whistle", (short) 10, 180469), // Orca Whistle
				new DLClassName("Ba", (short) 12, 180524), // Minke
				};
		genericModelParams.numClasses = genericModelParams.classNames.length; 

		genericModelParams.defaultShape= new Long[] {-1L,-1L,-1L,1L};
		genericModelParams.shape= new Long[] {-1L,-1L,-1L,1L};
		genericModelParams.outputShape = new Long[] {-1L,12L}; 


		//create the transforms. 
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 

		genericModelParams.setExampleSound(ExampleSoundType.HUMPBACK_WHALE);
	}



}


