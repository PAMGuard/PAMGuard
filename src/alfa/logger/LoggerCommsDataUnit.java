package alfa.logger;

import PamUtils.PamCalendar;
import alfa.comms.ALFACommsDataUnit;

public class LoggerCommsDataUnit extends ALFACommsDataUnit {

//	public static final int CURRENT_VERSION = 1;
	
	private String jsonString;

	private int loggerFormFormat;

	public LoggerCommsDataUnit(long timeMilliseconds, String jsonString, int loggerFormFormat) {
		super(timeMilliseconds);
		this.jsonString = jsonString;
		this.loggerFormFormat = loggerFormFormat;
		setReadyToSend(true);
	}

	@Override
	public String getCommsString() {
		return String.format("$PGLGR,%d,%s,%s,%s", loggerFormFormat, 
				PamCalendar.formatDate2(getTimeMilliseconds(),false), PamCalendar.formatTime2(getTimeMilliseconds(), 0, false),
				jsonString);
	}

}
