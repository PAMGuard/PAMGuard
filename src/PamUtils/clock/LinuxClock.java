package PamUtils.clock;

import java.io.File;

import PamUtils.PamCalendar;

public class LinuxClock extends OSClock {

	public LinuxClock(String osName) {
		super(osName);
	}

	@Override
	public boolean setSystemTime(long timeMilliseconds) {
		// should be able to send a string along the lines of sudo date -s "2025-11-14 17:46:00 utc"
		String dateStr = PamCalendar.formatDBDateTime(timeMilliseconds, false);
		String fullStr = String.format("sudo date -s \"%s utc\"", dateStr);
		String[] cmds = {fullStr};
		System.out.println(fullStr);

		String ud = System.getProperty("user.home");
		
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.directory(new File(ud));
//			System.out.println("Directory: " + builder.directory());
			builder.command("sh", "-c", fullStr);
			builder.start();
		} catch (Exception e) {
			System.out.println("Unable to update clock with " + fullStr);
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}

	@Override
	protected long getProcessCPUTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
