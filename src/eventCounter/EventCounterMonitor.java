package eventCounter;

import PamguardMVC.PamDataUnit;

public interface EventCounterMonitor {

	public void startEvent(long timeMillis, PamDataUnit dataUnit);
	
	public void continueEvent(long timeMillis, PamDataUnit dataUnit);
	
	public void endEvent(long timeMillis, PamDataUnit dataUnit);
}
