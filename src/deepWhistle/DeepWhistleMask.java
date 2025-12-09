package deepWhistle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepWhistle.DeepWhistleTest.DeepWhistleInfo;
import org.jamdev.jdl4pam.deepWhistle.SpectrumTranslator;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class DeepWhistleMask implements PamFFTMask {
	
	/**
	 * FFT length used in the model
	 */
	private DeepWhistleProcess maksedFFTProcess;

	public DeepWhistleMask(DeepWhistleProcess maksedFFTProcess) {
		this.maksedFFTProcess = maksedFFTProcess;
	}

	Predictor<float[][], float[]> specPredictor;
	//TEMP
	String modelPath = "/Users/jdjm/Dropbox/PAMGuard_dev/Deep_Learning/deepWhistle/DWC-I.pt";
	private DeepWhistleInfo modelInfo; 


	@Override
	public boolean initMask() {
		
		if (specPredictor!=null) {
			//already initialised
			specPredictor.close();
		}
		
		
		FFTDataBlock fftDataBlock = maksedFFTProcess.getInputFFTData();
		int fftLen = fftDataBlock.getFftLength();
		int fftHop = fftDataBlock.getFftHop();

		MaskedFFTParamters fftParams = this.maksedFFTProcess.getMaskFFTParams();

		specPredictor =  loadPyTorchdeepWhistleModel(modelPath,  fftLen,  maksedFFTProcess.getUnitsToBuffer());

		if (specPredictor == null) {
			System.err.println("DeepWhistleMask: failed to load deepWhistle model from "+modelPath);
			return false;
		}
		
		 modelInfo = new DeepWhistleInfo(fftLen, fftHop, 5000.0f , 50000.0f , (float) fftParams.bufferSeconds);		


		return true;
	}


	/**
	 * Create the transform params used in deepWhistle pre-processing
	 * @param modelInfo - the model info
	 * @return
	 */
	private ArrayList<DLTransfromParams> createTrasnforms(DeepWhistleInfo modelInfo){
		

		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();
		
		//so that transforms are
		// spectrogram - create a spectrogram - note silbido does not use a window function weirdly...
		//then trim the frequency
		//multiply by log10. 
		///clamp the values between mac_clip and min_clip - this is between 0 and 6 in Silbido
		//transforms
		//Then do a min mac normalisation
		//	dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECTROGRAM, 1024,512)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECTROGRAM, modelInfo.fftLen, modelInfo.fftHop)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECFREQTRIM, modelInfo.minFreq, modelInfo.maxFreq)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_LOG10));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_ADD, 2.1));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECCLAMP, 0, 6.));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECNORMALISE_MINIMAX, 0, 6)); 

	
		return dlTransformParamsArr; 
	}
	
	/**
	 * Set the FFT data into the transforms. 
	 * @return
	 */
	public ArrayList<DLTransfromParams> setFFTData(List<FFTDataUnit> batch, ArrayList<DLTransfromParams> transforms){
		return null;
	}
	
	
	

	@Override
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Load the deepWhistle into memory and create a predictor that can be called to run the model. 
	 * @param modelPathS - the path to the PyTorch model file
	 * @param fftLen - the fft length used in the model
	 * @param fftNum - the number of runs (i.e. number of FFT
	 * @return the predictor which returns a flattened confidence surface. 
	 */
	public static Predictor<float[][], float[]>  loadPyTorchdeepWhistleModel(String modelPathS, long fftLen, long fftNum) {

		Path modelPath = Paths.get(modelPathS); 

		//get the parent
		Path modelDirectory = modelPath.getParent();
		// Define the name of your ONNX model file
		String modelName = modelPath.getFileName().toString();

		Model loadedModel = Model.newInstance("DeepWhistle"); 
		try {

			loadedModel.load(modelDirectory, modelName);

			System.out.println("Model input description: " + loadedModel.describeInput());

			if (loadedModel.describeInput()!=null) {
				for (int i=0; i<loadedModel.describeInput().size() ;i++) {
					System.out.println(loadedModel.describeInput().get(i).getValue());
				}
			}

			System.out.println("Model properties: " + loadedModel.getProperties());

			SpectrumTranslator spectrumTranslator = new SpectrumTranslator(new Shape(new long[] {1, 1,fftLen, fftNum}), new Shape(new long[] {fftLen}));

			//predictor for the model if using images as input
			Predictor<float[][], float[]> specPredictor = loadedModel.newPredictor(spectrumTranslator);
			return specPredictor;

		} catch (MalformedModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
	}


}
