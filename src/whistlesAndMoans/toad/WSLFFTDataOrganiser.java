package whistlesAndMoans.toad;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataUnit;
import PamguardMVC.TFContourData;
import fftManager.FFTDataUnit;
import fftManager.fftorganiser.FFTDataException;
import fftManager.fftorganiser.FFTDataList;
import fftManager.fftorganiser.FFTDataOrganiser;
import whistlesAndMoans.ConnectedRegion;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;

public class WSLFFTDataOrganiser extends FFTDataOrganiser {

	private WhistleMoanControl wslMoanControl;
	private ConnectedRegionDataBlock connectedRegionDataBlock;

	public WSLFFTDataOrganiser(WhistleMoanControl wslMoanControl, ConnectedRegionDataBlock connectedRegionDataBlock) {
		super(wslMoanControl);
		this.wslMoanControl = wslMoanControl;
		this.connectedRegionDataBlock = connectedRegionDataBlock;
//		this.setOnlyAllowedDataBlock(connectedRegionDataBlock);
		
	}

	/* (non-Javadoc)
	 * @see fftManager.fftorganiser.FFTDataOrganiser#createFFTDataList(PamguardMVC.PamDataUnit, int)
	 */
	@Override
	public FFTDataList createFFTDataList(PamDataUnit pamDataUnit, double sampleRate, int channelMap) throws FFTDataException {
		FFTDataList fftDataList =  super.createFFTDataList(pamDataUnit, sampleRate, channelMap);
		if (fftDataList == null) {
			return null;
		}
		/**
		 * Now work out all the filtering to limit the range of data used ...
		 */
		ConnectedRegionDataUnit crDataUnit = (ConnectedRegionDataUnit) pamDataUnit;

		int nFFT = fftDataList.getFftDataUnits().size();
//		System.out.printf("CR Start %d end %d first FFt %d last %d nslice %d nFFT %d (%d extra)\n", 
//				pamDataUnit.getTimeMilliseconds(), pamDataUnit.getEndTimeInMilliseconds(), 
//				fftDataList.getFftDataUnits().get(0).getTimeMilliseconds(), 
//				fftDataList.getFftDataUnits().get(nFFT-1).getTimeMilliseconds(), 
//				crDataUnit.getConnectedRegion().getNumSlices(), nFFT/4, 
//				nFFT/4-crDataUnit.getConnectedRegion().getNumSlices());
		
		long[] fftTimes = new long[nFFT/4];
		for (int i = 0; i < nFFT/4; i++) {
			fftTimes[i] = fftDataList.getFftDataUnits().get(i*4).getTimeMilliseconds();
		}
		
		TFContourData tfContour = crDataUnit.getTFContourData();
		if (tfContour == null) {
			return fftDataList;
		}
//		double fftMillis = getFftLength() / this.connectedRegionDataBlock.getSampleRate() * 1000;
//		ConnectedRegion cr = crDataUnit.getConnectedRegion();
		long[] tfTimes = tfContour.getContourTimes();
		double[] tfLow = tfContour.getLowFrequency();
		double[] tfHigh = tfContour.getHighFrequecy();
		ArrayList<FFTDataUnit> fftList = fftDataList.getFftDataUnits();
		int iF = 0;
		int nChan = PamUtils.PamUtils.getNumChannels(fftDataList.getChannelMap());
		double fftScale = getFftLength() / this.connectedRegionDataBlock.getSampleRate();
		int[] setLims = null;
		for (int i = 0; i < tfTimes.length && iF < nFFT; i++) {
			int b0 = (int) Math.round(tfLow[i]*fftScale);
			int b1 = (int) Math.round(tfHigh[i]*fftScale)+1;
			int[] binLims = {b0, b1};
			for (int f = 0; f < nChan; f++) {
				FFTDataUnit fftUnit = fftList.get(iF++);
				fftUnit.setUsefulBinRange(setLims = binLims);
			}
		}
		/*
		 *  generally we get an extra FFT because of FFT overlap - the start of the last FFT is within
		 *  the time limits of the whistle, though it extends beyond it's end. Just give this the 
		 *  same bin lims as the last one ....  
		 */
		for (; iF < nFFT; iF++) {
			fftList.get(iF).setUsefulBinRange(setLims);
		}		
		
		return fftDataList;
	}


}
