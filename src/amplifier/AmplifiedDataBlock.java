package amplifier;

import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class AmplifiedDataBlock extends PamRawDataBlock {

	private double[] gain;

	public AmplifiedDataBlock(String name, PamProcess parentProcess,
			int channelMap, float sampleRate) {
		super(name, parentProcess, channelMap, sampleRate);
		// TODO Auto-generated constructor stub
	}

	public AmplifiedDataBlock(String name, PamProcess parentProcess,
			int channelMap, float sampleRate, boolean autoDisplay) {
		super(name, parentProcess, channelMap, sampleRate, autoDisplay);
		// TODO Auto-generated constructor stub
	}

	public void setDataGain(double[] gain) {
		this.gain = gain;
	}

	@Override
	public double getDataGain(int chan) {
		if (gain == null || gain.length <= chan) {
			return 1;
		}
		else {
			return gain[chan];
		}
	}
	

}
