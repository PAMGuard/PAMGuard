package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransform;

import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import javafx.geometry.Insets;

/**
 * 
 * Generates settings panes for different data transforms. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DataTransformPaneFactory {
	
	
	/**
	 * Get the settings pane for a DLTransfrom type. This creates a default pane with default settings based on the sample rate. 
	 * @param dlTransfromType - the transform type. 
	 * @param sR- the samplwe rate - this is required by some transforms. 
	 * @return the DlTransfrom Settings Pane. 
	 */
	public static DLTransformPane getSettingsPane(DLTransformType dlTransfromType, float sR) {
		
		DLTransform dlTransform = DLTransformsFactory.makeDLTransform(dlTransfromType, sR); 
		
		return getSettingsPane(dlTransform); 
	}


	/**
	 * Get the settings pane for a DLTransfrom
	 * @return the DlTransfrom Settings Pane. 
	 */
	public static DLTransformPane getSettingsPane(DLTransform dlTransfrom) {
		
//		System.out.println("Gte transform pane for 1: " + dlTransfrom.getDLTransformType());

		DLTransformPane settingsPane = null;
		switch (dlTransfrom.getDLTransformType()) {
		case DECIMATE:
			double sR;
			if (((SimpleTransform) dlTransfrom).getParams()!=null){
				sR = ((SimpleTransform) dlTransfrom).getParams()[0].doubleValue(); 
			}
			else {
				sR = 20000; 
				((SimpleTransform) dlTransfrom).setParams(new Number[] {Double.valueOf(sR)});
			}
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Sample rate "}, new String[]{"Hz. "});
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 100.0, Double.MAX_VALUE,   sR>10000.0 ? 1000.0 : 100.0);
			break;
		case PREEMPHSIS:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Factor "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0.0, 1.0,   0.01);
			break;
		case NORMALISE_WAV:
			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.NORMALISE_WAV.toString()); 
			settingsPane.setPadding(new Insets(0,0,0,20));
			break;
		case FILTER:
			settingsPane = new FilterTransformPane(dlTransfrom); 
			settingsPane.setParams(dlTransfrom);
			break;
		case SPEC2DB:
//			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.SPEC2DB.toString()); 
//			settingsPane.setPadding(new Insets(0,0,0,20));
			
			//System.out.println("Ketos dB Params: " + ((SimpleTransform) dlTransfrom).getParams().length); 
			if (((SimpleTransform) dlTransfrom).getParams()==null || ((SimpleTransform) dlTransfrom).getParams().length<1) {
				//need this because the min dB can often be null depending in the metadata imported. 
				((SimpleTransform) dlTransfrom).setParams(new Number[] {Double.valueOf(-100.)});
			}
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Min. dB "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, -300.0, 300.0,   1.);
			break;
		case SPECNORMALISEROWSUM:
			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.SPECNORMALISEROWSUM.toString()); 
			settingsPane.setPadding(new Insets(0,0,0,20));
			break;
		case SPECCLAMP:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Min. ", "Max. "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, -1000.0, 1000.0,   0.1);
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(1, -1000.0, 1000.0,   0.1);
			break;
		case SPECCROPINTERP:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Min. Freq. ", "Max. Freq. ", "No. bins "},  new String[]{"Hz", "Hz", ""}, 2); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0.0, 500000.0,   100.); //hmmmm would be nice to have the sample rate here...
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(1, 0.0, 500000.0,   100.);
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(2, 0, Integer.MAX_VALUE,   10);
			break;
		case SPECNORMALISE:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Min. dB ", "Reference dB"}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, -300.0, 300.0,   1.);
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(1, -300.0, 500000.0,   1.);

			break;
		case SPECTROGRAM:
			settingsPane = new FFTTransformPane((SimpleTransform) dlTransfrom, new String[]{"FFT Length ", "FFT Hop"},  new String[]{"", ""}); 
//			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 4, Integer.MAX_VALUE,   4);
			
			((FFTTransformPane) settingsPane).setSpinnerMinMaxValues(1, 4, Integer.MAX_VALUE,   4);
//			//make an FFT spinner here with doubling FFT lengths - DOES NOT WORK FOR SOME REASON...
//			((SimpleTransformPane) settingsPane).getSpinners().get(0).setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(createStepList()));
//			((SimpleTransformPane) settingsPane).getSpinners().get(0).getValueFactory().setValue(4);
//			((SimpleTransformPane) settingsPane).getSpinners().get(1).setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(createStepList()));
			break;
		case SPECTROGRAMKETOS:
			
			settingsPane = new FFTTransformPane((SimpleTransform) dlTransfrom, new String[]{"FFT Length ", "FFT Hop", "Window_Length"},  new String[]{"", "", "s"}); 
			
			((FFTTransformPane) settingsPane).setSpinnerMinMaxValues(1, 4, Integer.MAX_VALUE,   4);

			break;

		case TRIM:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Start", "End"},  new String[]{"samples ", "samples"}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0, Integer.MAX_VALUE,   500);
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(1, 0,Integer.MAX_VALUE,   500);
			break;
		case ENHANCE:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Enhancement "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0.0, Double.MAX_VALUE,   1.0);			
			break;
		case FILTER_ISOLATED_SPOTS:
			//TODO
			break;
		case GAUSSIAN_FILTER:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Sigma "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0.0, Double.MAX_VALUE,   1.0);	
			break;
		case MEDIANFILTER:
			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.MEDIANFILTER.toString()); 
			settingsPane.setPadding(new Insets(0,0,0,20));
			break;
		case REDUCETONALNOISE_MEAN:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Time const. length "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 1, Integer.MAX_VALUE,   1);	
			break;
		case REDUCETONALNOISE_MEDIAN:
			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.REDUCETONALNOISE_MEDIAN.toString()); 
			settingsPane.setPadding(new Insets(0,0,0,20));
			break;
		case SPECNORMALISESTD:
			settingsPane = new SimpleTransformPane((SimpleTransform) dlTransfrom, new String[]{"Mean ", "Std "}); 
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(0, 0.0, Double.MAX_VALUE,   0.1);
			((SimpleTransformPane) settingsPane).setSpinnerMinMaxValues(1, 0.0, Double.MAX_VALUE,   0.1);
			break;
		case SPECNORMALISE_MINIMAX:
			settingsPane = new LabelTransfromPane(dlTransfrom, DLTransformType.SPECNORMALISE_MINIMAX.toString()); 
			settingsPane.setPadding(new Insets(0,0,0,20));
			break;
		default:
			break;
	
		}
		
//		System.out.println("Get transform pane for 2: " + settingsPane);

		return settingsPane;	

	}

	


}
