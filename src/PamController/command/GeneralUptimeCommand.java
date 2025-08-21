package PamController.command;

import PamUtils.PamCalendar;

public class GeneralUptimeCommand extends ExtCommand{
	
	private final static String name = "GeneralUptime";

	public GeneralUptimeCommand() {
		super(name, true);
		
	}

	@Override
	public String execute(String command) {
		if(PamController.PamController.getInstance().getPamStatus()!=PamController.PamController.PAM_RUNNING) {
			return "0";
		}
		long now = PamCalendar.getTimeInMillis();
		long sessionStart = PamCalendar.getSessionStartTime();
		long sessionRuntime = now-sessionStart;
		return Long.toString(sessionRuntime);
	}

}
