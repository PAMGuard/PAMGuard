package beamformer.continuous;

import PamguardMVC.PamProcess;
import beamformer.BeamFormerBaseProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class BeamFormerDataBlock extends FFTDataBlock {

	public BeamFormerDataBlock(String dataName, PamProcess parentProcess, int channelMap, int fftHop, int fftLength) {
		super(dataName, parentProcess, channelMap, fftHop, fftLength);
		
	}

	@Override
	public int getChannelsForSequenceMap(int sequenceMap) {
		return ((BeamFormerBaseProcess) this.getParentProcess()).getChannelsForSequenceMap(sequenceMap);
	}
	
	@Override
	public double getDataGain(int iChan) {
		return 1;
	}

	

}
