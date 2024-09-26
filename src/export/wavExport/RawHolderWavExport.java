package export.wavExport;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clickDetector.ClickDetection;

/**
 * Exports click detections to a .wav file. Fills parts of the wav file without any detected clicks with white noise. 
 * @author Jamie Macaulay 
 *
 */
public class RawHolderWavExport extends WavDataUnitExport<PamDataUnit<?,?>> {

	@Override
	public double[][] getWavClip(PamDataUnit dataUnit) {
		return ((RawDataHolder) dataUnit).getWaveData();
	}

	@Override
	public Class<?> getUnitClass() {
		return RawDataHolder.class;
	}

}
