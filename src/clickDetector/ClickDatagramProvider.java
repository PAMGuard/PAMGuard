package clickDetector;

import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class ClickDatagramProvider implements DatagramProvider {

	private ClickControl clickControl;
	private boolean isViewer;
	
	/**
	 * @param clickControl
	 */
	public ClickDatagramProvider(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
		isViewer = clickControl.isViewer(); 
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		ClickDetection click = (ClickDetection) dataUnit;
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
			fftData = click.getPowerSpectrum(i, gramLen*2);
			if (fftData!=null){
			for (int s = 0; s < gramLen; s++) {
//				dataGramLine[s] += Math.log(fftData[s]);
				dataGramLine[s] += fftData[s];
			}
		}
		}
//		for (int s = 0; s < gramLen; s++) {
//			dataGramLine[s] = Math.exp(dataGramLine[s]);
//		}
		if (isViewer) {
//			click.freeClickMemory();
			click.freeMaxMemory();
		}
		return nChan*gramLen;
	}

	@Override
	public int getNumDataGramPoints() {
		int clickLen = clickControl.clickParameters.maxLength;
		int l2 = PamUtils.log2(clickLen) - 1;
		return 1<<l2;
	}

	/* (non-Javadoc)
	 * @see dataGram.DatagramProvider#getScaleInformation()
	 */
	@Override
	public DatagramScaleInformation getScaleInformation() {
		ClickDetector cd = clickControl.clickDetector;
		if (cd == null) {
			return null;
		}
		double maxFreq = cd.getSampleRate()/2;
		FrequencyFormat ff = FrequencyFormat.getFrequencyFormat(maxFreq);
		
		return new DatagramScaleInformation(0, maxFreq/ff.getScale(), ff.getUnitText());
	}

}
