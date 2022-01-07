package fileOfflineData;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class OfflineFileProcess extends PamProcess {

	private OfflineFileControl d3SensorControl;
	
	private PamDataBlock pamDataBlock;

	public OfflineFileProcess(OfflineFileControl d3SensorControl) {
		super(d3SensorControl, null);
		this.d3SensorControl = d3SensorControl;
		addOutputDataBlock(pamDataBlock = d3SensorControl.createOfflineDataBlock(this));
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}


}
