package whistleDetector;

import fftManager.FFTDataBlock;

public abstract class PeakDetectorProvider {

	abstract PeakDetector createDetector(WhistleControl whistleControl, WhistleDetector whistleDetector, 
			FFTDataBlock pamFFTDataSource, int groupChannels);
	
	abstract String getName();
	
	abstract String getDescription();
	
}
