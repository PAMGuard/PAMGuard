package whistleDetector;

import fftManager.FFTDataBlock;

public class BetterPDProvider extends PeakDetectorProvider {

	@Override
	PeakDetector createDetector(WhistleControl whistleControl,
			WhistleDetector whistleDetector, FFTDataBlock fftDataSource,
			int groupChannels) {

		return new BetterPeakDetector(whistleControl, whistleDetector, fftDataSource, groupChannels);
		
	}

	@Override
	String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getName() {
		return "Better Peak Detector";
	}

}
