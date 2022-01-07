package PamController.command;

import PamController.PamController;

/**
 * Class to manage more sophisticated communications with PAMDog. 
 * Currently PAMDog is only thinking along the lines of the four states
 * of PAMGUARD: <br>
 *	public static final int PAM_IDLE = 0; <br>
 *	public static final int PAM_RUNNING = 1;<br>
 *	public static final int PAM_STALLED = 3;<br>
 *	public static final int PAM_INITIALISING = 4;<br>
 * and PAMdog will reset the software on anything by PAM_RUNNING. <br>
 * This class can modify the response, based on a more sophisticated analysis 
 * of PAMGuards state, and may even decide to send a PAM_RUNNING response even when 
 * it isn't so that we can do a bit more sophisticated stopping starting even when the watchdog is running. 
 * @author dg50
 *
 */
public class WatchdogComms {

	private PamController pamController;

	public WatchdogComms(PamController pamController) {
		this.pamController = pamController;
	}
	
	public int getModifiedWatchdogState(int oldState) {
		if (pamController.isManualStop()) {
			return PamController.PAM_RUNNING;
		}
		return oldState;
	}

}
