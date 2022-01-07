package clickDetector.offlineFuncs;

import clickDetector.ClickControl;

public class ReDoClickId implements OfflineReProcess {

	private ClickControl clickControl;
	
	public ReDoClickId(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
	}

	@Override
	public String getName() {
		return "Click Identification";
	}

	@Override
	public void getReady() {
		
	}
	
	@Override
	public void processClick() {
		// TODO Auto-generated method stub

	}

}
