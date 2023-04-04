package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;

import pamViewFX.fxNodes.PamBorderPane;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSound;

/**
 * Contains both a list of transforms and preview image of the waveform, spectrogram or other visualisation of 
 * the transform. 
 * @author Jamie Macaulay 
 *
 */
public class DLImageTransformPane  extends PamBorderPane {

	/**
	 * The DL transform pane. 
	 */
	private DLTransformsPane dlTransformPane;

	/**
	 * Create the DL image pane. 
	 */
	private DLTransfromImagePane dlTransfromImagePane;


	/**
	 * Constructor of the DL transform pane. 
	 */
	public DLImageTransformPane () {
		dlTransformPane = new DynamicDLTransformsPane(); 

		dlTransfromImagePane = new DLTransfromImagePane(dlTransformPane); 

		this.setTop(dlTransformPane);
		this.setCenter(dlTransfromImagePane);

	}

	/**
	 * Set the transforms in the pane. 
	 * @param dlTransforms - the transforms to set. 
	 */
	public void setTransforms(ArrayList<DLTransform> dlTransforms) {
		dlTransformPane.setTransforms(dlTransforms);
		dlTransfromImagePane.newSettings();
	}

	/**
	 * Get the DL transforms. 
	 * @return the DLTransform's. 
	 */
	public ArrayList<DLTransform> getDLTransforms() {
		return dlTransformPane.getDLTransforms();
	}

	/**
	 * Get the DL transform pane. 
	 * @return the DL transform pane. 
	 */
	public DLTransformsPane getDLTransformPane() {
		return dlTransformPane;
	}

	/**
	 * New example sound is set. 
	 * @param exampleSound - the example sound. 
	 */
	private void newExampleSound(ExampleSound exampleSound) {
		if (getDLTransforms()==null) return;

		//System.out.println("Set transforms: " + sampleRate);
		
		/****This was way too much hassle in bugs than it was worth****/

		//OK so when we change samplerates massively e.g. from bat to right whale we could technically use the 
		//same transforms but this can cause weird issues - for example upsampling a 2s right whale call to 256000 Hz is 
		//not pretty. So just be nice to the user and set a few default 

//		ArrayList<DLTransform> transforms =  getDLTransforms(); 
//		WaveTransform waveTransForm ;
//		FreqTransform freqTranform ;

//		//extra things that may need changed...
//		for (DLTransform dlTransfrom:  transforms) {
//			switch (dlTransfrom.getDLTransformType()) {
//			case DECIMATE:
//				waveTransForm = ((WaveTransform)  dlTransfrom);
//				
//				//change if the example sample rate is higher to if there is a large differenc in sample rates and decimation. 
//				if (exampleSound.getSampleRate()<waveTransForm.getParams()[0].floatValue() 
//						|| exampleSound.getSampleRate()/waveTransForm.getParams()[0].floatValue()>4) {
//					waveTransForm.setParams(new Number[] {exampleSound.getSampleRate()}); //set the correct samplerate
//					dlTransformPane.getDLTransformPanes().get(transforms.indexOf(dlTransfrom)).setParams(waveTransForm);
//				}
//				break;
//			case PREEMPHSIS:
//				waveTransForm = ((WaveTransform)  dlTransfrom);
//				if (exampleSound.getSampleRate()<10000 && waveTransForm.getParams()[0].doubleValue()>0.1) {
//					waveTransForm.setParams(new Number[] {0.1}); //set the correct samplerate
//					dlTransformPane.getDLTransformPanes().get(transforms.indexOf(dlTransfrom)).setParams(waveTransForm);
//				}
//				break;
//			case SPEC2DB:
//				break;
//			case SPECCLAMP:
//				break;
//			case SPECCROPINTERP:
//
//				freqTranform = ((FreqTransform)  dlTransfrom);
//				Number[] params = freqTranform.getParams();
//				
//				double highestFreq = exampleSound.getSampleRate()/2; //nyquist
//
//				if (params[0].doubleValue()>=highestFreq) params[0]=0.0;
//
//				//this will break stuff if interp frequency is greater than nyquist
//				if (params[1].doubleValue()>highestFreq) {
////					System.out.println("----HERE 1----");
//					params[1]=highestFreq; //nyquist
//				}
//				//if we switch to a high frequency want to the interp not to just select the lowest band
//				
////				else if (params[1].doubleValue()<highestFreq/10) {
//////					System.out.println("----HERE 2----" + exampleSound.getSampleRate()/10.0 + "  " + params[1].doubleValue());
////					params[1]=highestFreq; //nyquist
////				}
//
//				//System.out.println("Interp params: " + params[0] + "  " + params[1] + "  " + params[2]); 
//				freqTranform.setParams(params);
//
//				dlTransformPane.getDLTransformPanes().get(transforms.indexOf(dlTransfrom)).setParams(freqTranform);
//
//				break;
//			case SPECNORMALISE:
//				break;
//			case SPECNORMALISEROWSUM:
//				break;
//			case SPECTROGRAM:
//				break;
//			case TRIM:
//				break;
//			default:
//				break;
//
//			}
//		}
	}



	private boolean exampleSoundUpdate = false;

	public class DynamicDLTransformsPane extends DLTransformsPane {


		@Override
		public void newSettings(int type) {
			//System.out.println("New settings: " + type); 
			switch (type) {
			case DLTransformsPane.TRANSFORM_SETTINGS_CHANGE:
				if (exampleSoundUpdate) return; //bit messy but needed to stop update before setting shave been checked. 
				//called whenever a control is updated. 
				dlTransformPane.getParams();
				//System.out.println("Update the transform image"); 
				dlTransfromImagePane.updateTransformImage(); 
				break;
			case DLTransformsPane.TRANSFORM_ORDER_CHANGE:
				dlTransfromImagePane.newSettings();
				break;
			}
		}


		@Override
		protected void addNewDLTransfrom(DLTransformType dlTransformType) {
			//TODO - need to add frequency and wave transforms in appropriate places in the list.

			//System.out.println("Add DL transform: " + dlTransformType); 
			super.addNewDLTransfrom(dlTransformType);
			dlTransfromImagePane.newSettings();
		}


		@Override
		public void dlTrandformRemoved(DLTransformPane transformPane) {
			super.dlTrandformRemoved(transformPane); 
			dlTransfromImagePane.newSettings();
		}
	}


	public class DLTransfromImagePane extends DLTransformImage {

		private DLTransformsPane dLTransformsPane;

		public DLTransfromImagePane(DLTransformsPane dLTransformsPane) {
			this.dLTransformsPane=dLTransformsPane; 
			this.newSettings();
		}

		@Override
		public ArrayList<DLTransform> getDLTransforms() {
			if (dLTransformsPane==null) return null; 
			return dLTransformsPane.getDLTransforms();
		}

		@Override
		public void updateExampleSound(ExampleSound exampleSound){
			exampleSoundUpdate=true; //stop panes from updating
			newExampleSound(exampleSound); 
			super.updateExampleSound(exampleSound);
			exampleSoundUpdate=false;
		}

	}

	/**
	 * Set the index of the example sound. 
	 * @param exampleSoundIndex
	 */
	public void setExampleSoundIndex(int exampleSoundIndex) {
		dlTransfromImagePane.getSpeciesChoiceBox().getSelectionModel().select(exampleSoundIndex);
	}

	/**
	 * Get the currently selected index of the example sound. 
	 * @return the current selected index of the example sund. 
	 */
	public int getExampleSoundIndex() {
		return 	dlTransfromImagePane.getSpeciesChoiceBox().getSelectionModel().getSelectedIndex();
	}




}
