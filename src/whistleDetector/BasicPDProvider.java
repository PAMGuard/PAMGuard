package whistleDetector;

import fftManager.FFTDataBlock;

public class BasicPDProvider extends PeakDetectorProvider {

	@Override
	PeakDetector createDetector(WhistleControl whistleControl,
			WhistleDetector whistleDetector, FFTDataBlock pDataSource,
			int groupChannels) {

		return new BasicPeakDetector(whistleControl, whistleDetector, pDataSource, groupChannels);
		
	}

	@Override
	String getDescription() {
		return null;
	}

	@Override
	String getName() {
		return "IFAW Whistle Peak Detector";
	}

}
