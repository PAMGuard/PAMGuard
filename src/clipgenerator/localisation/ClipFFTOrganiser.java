package clipgenerator.localisation;

import clipgenerator.ClipControl;
import clipgenerator.ClipDataBlock;
import fftManager.fftorganiser.FFTDataOrganiser;
import fftManager.fftorganiser.FFTInputTypes;

public class ClipFFTOrganiser extends FFTDataOrganiser {
	
	public ClipFFTOrganiser(ClipControl clipControl, ClipDataBlock clipDataBlock) {
		super(clipControl);
		setInput(clipDataBlock, FFTInputTypes.RAWDataHolder);
		setFftLength(512);
		setFftHop(256);
	}

//	@Override
//	public FFTDataList createFFTDataList(PamDataUnit pamDataUnit, double sampleRate, int channelMap)
//			throws FFTDataException {
//		ClipDataUnit clip = (ClipDataUnit) pamDataUnit;
//		double[][] wave = clip.getRawData();
//		int nChan = wave.length;
//		if (wave == null || wave.length == 0) {
//			return null;
//		}
//		int samples = wave[0].length;
//		int fftLength = getFftLength();
//		int fftHop = getFftHop();
//		int nFFT = (samples - (fftLength-fftHop)) / fftHop;
//		if (nFFT <= 0) {
//			return null;
//		}
//		ComplexArray[] specData = new ComplexArray[nFFT];
//		double[] waveBit = new double[fftLength];
////		double[] winFunc = getWindowFunc(fftLength);
//		Complex[] complexOutput = Complex.allocateComplexArray(fftLength/2);
//		int wPos = 0;
//		int m = FastFFT.log2(fftLength);
//		for (int i = 0; i < nFFT; i++) {
//			wPos = i*fftHop;
//			for (int j = 0; j < fftLength; j++) {
////				waveBit[j] = wave[j+wPos]*winFunc[j];
//				waveBit[j] = wave[j+wPos]; // no windowing for this since used in cross correlation. 
//			}
//			specData[i] = fastFFT.rfft(waveBit, fftLength);
////			fastFFT.rfft(waveBit, complexOutput, m);
////			for (int j = 0; j < fftLength/2; j++) {
////				specData[i][j] = complexOutput[j].clone();
////			}
//		}
//		return specData;
//		
//	}

}
