package rawDeepLearningClassifier.defaultModels;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;

import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

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
		return "Detects North Atlantic right whales (Eubalaena glacialis).";
	}

	@Override
	public String getName() {
		return "North Atlantic right whale";
	}

	@Override
	public String getCitation() {
		return "Shiu, Y., Palmer, K.J., Roch, M.A. et al. Deep neural networks for automated detection of marine mammal species. Sci Rep 10, 607 (2020)";
	}

	@Override
	public URI getModelURI() {
		try {
			return new URI("https://github.com/PAMGuard/deeplearningmodels/raw/master/right_whale_1/model_lenet_dropout_input_conv_all.zip");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	/**
	 * Set the model settings once it has loaded. 
	 * @param dlModelSettings - the model settings. 
	 */
	public void setParams(Serializable dlModelSettings) {	
		
		GenericModelParams genericModelParams = (GenericModelParams) dlModelSettings;
	
		//decimation value
		float sr = 2000; 

		//create the transforms. 
		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.DECIMATE, sr)); 
		//			dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.PREEMPHSIS, preemphases)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECTROGRAM, 256, 102)); 
		//in the python code they have an sfft of 129xN where N is the number of chunks. They then
		//choose fft data between bin 5 and 45 in the FFT. 	This roughly between 40 and 350 Hz. 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECCROPINTERP, 47.0, 357.0, 40)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECNORMALISEROWSUM)); 
		
		
		genericModelParams.dlTransfromParams = dlTransformParamsArr;
		
		genericModelParams.defaultSegmentLen =  2.0*1000;
		genericModelParams.binaryClassification = new boolean[] {false, true};
				 
		genericModelParams.defaultShape= new Long[] {-1L,40l,401L,1L};
		genericModelParams.shape= new Long[] {-1L,40L,40L,1L};

		genericModelParams.classNames= new DLClassName[] {new DLClassName("Noise", (short) 0), new DLClassName("Right Whale", (short) 1)};
		genericModelParams.numClasses = 2; 
		
		//create the transforms. 
		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 
		
		genericModelParams.setExampleSound(ExampleSoundType.RIGHT_WHALE);
		
	}

	@Override
	public String getModelName() {
		return "saved_model.pb";
	}

	@Override
	public URI getCitationLink() {
		try {
			return new URI("https://doi.org/10.1038/s41598-020-57549-y");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	

}
