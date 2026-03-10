package PamController.command;

import PamUtils.PamCalendar;

//ST: Added for configs/setups running without acoustic aquisition (ie ais only)
public class GeneralUptimeCommand extends ExtCommand{
	
	private final static String name = "generaluptime";

	public GeneralUptimeCommand() {
		super(name, true);
		
	}

	@Override
	public String execute(String command) {
		try {
			if(PamController.PamController.getInstance().getPamStatus()!=PamController.PamController.PAM_RUNNING) {
				return "0";
			}
			long now = PamCalendar.getTimeInMillis();
			long sessionStart = PamCalendar.getSessionStartTime();
			long sessionRuntime = now-sessionStart;
			return Long.toString(sessionRuntime);
		}catch(Exception e) {
			System.out.println("Caught exception attemping to return general uptime. Error: "+e.getMessage());
			return "0";
		}
		
	}

}
