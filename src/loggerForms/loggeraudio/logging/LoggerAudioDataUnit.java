package loggerForms.loggeraudio.logging;

import PamguardMVC.PamDataUnit;

public class LoggerAudioDataUnit extends PamDataUnit {

	private String platform;
	private String fileName;
	private int duration;

	public LoggerAudioDataUnit(long timeMilliseconds, String platform, String fileName, int duration) {
		super(timeMilliseconds);
		this.platform = platform;
		this.fileName = fileName;
		this.duration = duration;
	}

	/**
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}


}
