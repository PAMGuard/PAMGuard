package whistlesAndMoans.toad;

import PamguardMVC.PamDataBlock;
import PamguardMVC.toad.GenericTOADCalculator;
import fftManager.FFTDataBlock;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.WhistleMoanControl;

public class WSLToadCalculator extends GenericTOADCalculator {

	private WhistleMoanControl wslMoanControl;
	private ConnectedRegionDataBlock connectedRegionDataBlock;
	private WSLFFTDataOrganiser wslFFTDataOrganiser;

	public WSLToadCalculator(WhistleMoanControl wslMoanControl, ConnectedRegionDataBlock connectedRegionDataBlock) {
		super(wslMoanControl);
		this.wslMoanControl = wslMoanControl;
		this.connectedRegionDataBlock = connectedRegionDataBlock;
		setFftDataOrganiser(wslFFTDataOrganiser = new WSLFFTDataOrganiser(wslMoanControl, connectedRegionDataBlock));
		setCanUseEnvelope(false);
		prepare();
	}
	
	/**
	 * Prepare the localiser - called from constructor or if wsl dialog has been opened.
	 */
	public void prepare() {
		FFTDataBlock parentBlock = (FFTDataBlock) wslMoanControl.getWhistleToneProcess().getParentDataBlock();
		wslFFTDataOrganiser.setFftLength(parentBlock.getFftLength());
		wslFFTDataOrganiser.setFftHop(parentBlock.getFftHop());
		wslFFTDataOrganiser.setOnlyAllowedDataBlock(parentBlock);
		setTimingSource(parentBlock);
		// work out how long we need to hold FFT data for as input to the localiser ...
		// should really be clever and work this out, but can't see an easy way, so set it to 5s. 
		wslFFTDataOrganiser.setDataKeepMillis(5000);
	}

}
