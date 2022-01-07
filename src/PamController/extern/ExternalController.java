package PamController.extern;

import java.io.IOException;

/**
 * 
 * @author dg50
 * Class to run an external executable, gather data from it and probably 
 * also offer some sort of terminal window or logging. 
 *
 */
public class ExternalController {

	/**
	 * Executable file
	 */
	private String exeFile;

	/**
	 * Command line options. 
	 */
	private String commandOptions;

	private Process process;

	private Object launchError;

	private ExternalCapture inputCapture, errorCapture;

	public ExternalController() {

	}

	public boolean launch(LineObserver capture, LineObserver captureErrors) {
		String command = makeCommandLine();
		if (command == null) {
			launchError = "No executable";
			return false;
		}
		try {
			process = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			launchError = e.getMessage();
			return false;
		}
		launchError = null;
		if (capture != null) {
			inputCapture = new ExternalCapture(process.getInputStream(), capture);
		}
		if (captureErrors != null) {
			this.errorCapture = new ExternalCapture(process.getErrorStream(), captureErrors);
		}
		return process.isAlive();
	}

	private class ProcObs implements LineObserver {

		private String streamName = "";

		public ProcObs(String streamName) {
			super();
			this.streamName = streamName;
		}

		@Override
		public void newLine(String line) {
//			if (streamName != null) {
//				System.out.println(streamName + ": " + line);
//			}
//			else {
//				System.out.println(line);
//			}
		}

	}

	private String makeCommandLine() {
		if (exeFile == null) {
			return null;
		}
		if (commandOptions == null) {
			return exeFile;
		}
		else {
			return exeFile + " " + commandOptions;
		}

	}

	public boolean kill(long waitTime, boolean forceIt) {
		if (process == null) {
			return true;
		}
		process.destroy();
		long waitEnd = System.currentTimeMillis() + waitTime;
		while (System.currentTimeMillis() < waitEnd) {
			if (process.isAlive()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			else {
				break;
			}
		}
		if (process.isAlive() && forceIt) {
			process.destroyForcibly();	
		}
		if (inputCapture != null) {
			inputCapture.stopCapture();
		}
		if (errorCapture != null) {
			errorCapture.stopCapture();
		}

		boolean alive = process.isAlive();
		process = null;
		return (!alive);
	}
	/**
	 * @return the exeFile
	 */
	public String getExeFile() {
		return exeFile;
	}

	/**
	 * @param exeFile the exeFile to set
	 */
	public void setExeFile(String exeFile) {
		this.exeFile = exeFile;
	}

	/**
	 * @return the commandOptions
	 */
	public String getCommandOptions() {
		return commandOptions;
	}

	/**
	 * @param commandOptions the commandOptions to set
	 */
	public void setCommandOptions(String commandOptions) {
		this.commandOptions = commandOptions;
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @return the launchError
	 */
	public Object getLaunchError() {
		return launchError;
	}


}
