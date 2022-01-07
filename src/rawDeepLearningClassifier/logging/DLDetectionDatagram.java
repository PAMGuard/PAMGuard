package rawDeepLearningClassifier.logging;

import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;

/**
 * The datagram provider for classified data deep learning units. this shows the freqeuncy distirbution 
 * of the data units. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLDetectionDatagram implements DatagramProvider  {
	
	private DLControl dlControl;

	public DLDetectionDatagram(DLControl dlControl) {
		this.dlControl = dlControl; 
	}


	@Override
	public int addDatagramData(@SuppressWarnings("rawtypes") PamDataUnit dataUnit, float[] dataGramLine) {
		DLDetection click = (DLDetection) dataUnit;
		double[] fftData;
		int gramLen = getNumDataGramPoints();
		int channelMap = click.getChannelBitmap();
		int nChan = PamUtils.getNumChannels(channelMap);
//		for (int s = 0; s < gramLen; s++) {
//			if (dataGramLine[s] > 0) {
//				dataGramLine[s] = Math.log10(dataGramLine[s]);
//			}
//		}
		for (int i = 0; i < nChan; i++) {
			fftData = click.getDataTransforms().getPowerSpectrum(i, gramLen*2);
			if (fftData!=null){
			for (int s = 0; s < gramLen; s++) {
//				dataGramLine[s] += Math.log(fftData[s]);
				dataGramLine[s] += fftData[s];
			}
		}
		}
		return nChan*gramLen;
	}


	@Override
	public int getNumDataGramPoints() {
		return 512;
	}

	/* (non-Javadoc)
	 * @see dataGram.DatagramProvider#getScaleInformation()
	 */
	@Override
	public DatagramScaleInformation getScaleInformation() {
		if (dlControl.getSegmenter() == null) {
			return null;
		}
		double maxFreq = dlControl.getSegmenter().getSampleRate()/2;
		FrequencyFormat ff = FrequencyFormat.getFrequencyFormat(maxFreq);
		
		return new DatagramScaleInformation(0, maxFreq/ff.getScale(), ff.getUnitText());
	}

}
