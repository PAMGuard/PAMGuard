package PamUtils.time;

import java.awt.Window;

public class NullTimeCorrector implements PCTimeCorrector {

	private GlobalTimeManager globalTimeManager; 
	
	
	public NullTimeCorrector(GlobalTimeManager globalTimeManager) {
		super();
		this.globalTimeManager = globalTimeManager;
	}

	@Override
	public String getName() {
		return "No corrections";
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean showDialog(Window frame) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getUpdateInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean start() {
		long now = System.currentTimeMillis();
		globalTimeManager.updateUTCOffset(new TimeCorrection(now, now, getName()));
		return true;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

}
