package whistlesAndMoans;

import java.util.List;
import java.util.ListIterator;

import PamUtils.FrequencyFormat;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class WMDatagramProvider implements DatagramProvider {

	private ConnectedRegionDataBlock crDataBlock;
	
	private WhistleToneConnectProcess wmProcess;
	
	/**
	 * @param crDataBlock
	 */
	public WMDatagramProvider(ConnectedRegionDataBlock crDataBlock) {
		super();
		this.crDataBlock = crDataBlock;
		wmProcess = crDataBlock.getParentProcess();
	}

	@Override
	public int getNumDataGramPoints() {
		return wmProcess.getFFTLen()/2;
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		if (!(dataUnit instanceof ConnectedRegionDataUnit)) {
			return 0;
		}
		ConnectedRegionDataUnit crDataUnit = (ConnectedRegionDataUnit) dataUnit;
		ConnectedRegion cr = crDataUnit.getConnectedRegion();
		int n = cr.getNumSlices();
		List<SliceData> sliceList = cr.getSliceData();
		SliceData aSlice;
		int nP, p;
		int totalPoints = 0;
		ListIterator<SliceData> it = sliceList.listIterator();
		while (it.hasNext()) {
			aSlice = it.next();
			nP = aSlice.nPeaks;
			for (int j = 0; j < nP; j++) {
				p = aSlice.peakInfo[j][1];
				if (p < dataGramLine.length) {
				dataGramLine[p] += 1;
				totalPoints++;
				}
			}
		}
		return totalPoints;
	}

	/* (non-Javadoc)
	 * @see dataGram.DatagramProvider#getScaleInformation()
	 */
	@Override
	public DatagramScaleInformation getScaleInformation() {
		double maxFreq = crDataBlock.getSampleRate()/2;
		FrequencyFormat ff = FrequencyFormat.getFrequencyFormat(maxFreq);
		return new DatagramScaleInformation(0, maxFreq/ff.getScale(), ff.getUnitText());
	}

}
