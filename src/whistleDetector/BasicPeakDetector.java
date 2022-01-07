package whistleDetector;

import fftManager.FFTDataBlock;

public class BasicPeakDetector extends PeakDetector {

	public BasicPeakDetector(WhistleControl whistleControl, WhistleDetector whistleDetector, FFTDataBlock fftDataSource, int groupChannels) {
		super(whistleControl, whistleDetector, fftDataSource, groupChannels);
	}

	@Override
	public String getPeakDetectorName() {
		return "Basic Peak Detector";
	}

	@Override
	public int getNumOutputDataBlocks() {
		// TODO Auto-generated method stub
		return super.getNumOutputDataBlocks();
	}

}
