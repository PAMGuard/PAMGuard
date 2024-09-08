package whistlesAndMoans;

import PamguardMVC.PamDataBlock;

public class WhistleLocationDataBlock extends PamDataBlock<WhistleToneGroupedDetection> {

	private WhistleToneConnectProcess wslProcess;

	public WhistleLocationDataBlock(String dataName, WhistleToneConnectProcess wslProcess, int channelMap) {
		super(WhistleToneGroupedDetection.class, dataName, wslProcess, channelMap);
		this.wslProcess = wslProcess;
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDurationRange()
	 */
	@Override
	public double[] getDurationRange() {
		return wslProcess.getDurationRange();
	}


}
