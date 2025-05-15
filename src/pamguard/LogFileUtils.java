package pamguard;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import PamView.PamGui;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.ThreadedObserver;

/**
 * Utilities associated with log files. 
 * @author dg50
 *
 */
public class LogFileUtils {

	public static String LogFileRootName = "PamguardLog";
	/**
	 * Get the most recently modified log file. 
	 * @param logFolder
	 * @return most recently modified log file. 
	 */
	public static File getMostRectLog(String logFolder) {
		File path = new File(logFolder);
		if (path.exists() == false) {
			return null;
		}
		if (path.isDirectory() == false) {
			return null;
		}
		File[] logFiles = path.listFiles(new FilenameFilter() {
			String currentLog = Pamguard.getLogFileName();
			@Override
			public boolean accept(File dir, String name) {
				if (currentLog != null) {
					if (name.endsWith(currentLog)) {
						return false;
					}
				}
				if (name.startsWith(LogFileRootName) == false) {
					return false;
				}
				if (name.endsWith(".txt")) {
					return true;
				}
				return false;
			}
		});
		if (logFiles == null || logFiles.length == 0) {
			return null;
		}
		File newestFile = logFiles[0];
		long t = newestFile.lastModified();
		for (int i = 1; i < logFiles.length; i++) {
			if (logFiles[i].lastModified() > t) {
				newestFile = logFiles[i];
				t = newestFile.lastModified();
			}
		}
		return newestFile;
	}

	public static void checkLogFileErrors(String logFolder) {
		File newest = getMostRectLog(logFolder);
		if (newest == null) {
			return;
		}
		String err = checkErrors(newest);
		if (err == null) {
			return;
		}
//		emailLogFile(newest, err);
		handleLogError(newest, err);
	}

	private static void handleLogError(File logFile, String err) {
		String msg = String.format("<html>An error occurred last time you ran PAMGuard that is detailed in the log file<br>"
				+ "%s<br>"
				+ "Please email this file to bugs@pamguard.org</html>", logFile.getAbsolutePath());
		// can't use warnonce until PAMGuard is fully running. 
		int ans = WarnOnce.showWarning("An error occurred last time you ran PAMGuard", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			emailLogFile(logFile, err);
		}
	}

	private static void emailLogFile(File logFile, String err) {
		String url = "mailto:bugs@pamguard.org";
		if (err != null) {
			url += "?SUBJECT=" + "Automatic PAMGuard bug report";
		}
		if (logFile != null) {
			url +="&BODY=" + "Errors found in log file: "  + err;
//			url +="&ATTACH" + logFile.getAbsolutePath();
			url += "%0A%0APlease attach file file " + logFile.getAbsolutePath() + " to this email and send to us at bugs@pamguard.org";
			url += "%0A%0APlease also tell us anything else about what caused PAMGuard to throw this error";
 			url += "";
		}
		// replace some characters that mailto really doesn't like. 
		url = url.replace("\n", "%0A");
		url = url.replace("\\", "/");
		url = url.replace(" ", "%20");
		url = url.replace("(", "%28");
		url = url.replace(")", "%29");
		url = url.replace("\"", "");
		url = url.replace("\t", "%09");
//		url = "mailto:bugs@pamguard.org?SUBJECT=some test!";
		try {
			URI uri = URI.create(url); 
			Desktop.getDesktop().mail(uri);
		}
		catch (Exception e) {
			System.out.println("Error starting email " + url);
//			e.printStackTrace();
		}
	}

	/**
	 * Scan a log file and look for errors beginning with "Exception in module" 
	 * @param newest
	 * @return
	 */
	private static String checkErrors(File newest) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(newest));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// now try to read line by line and see if there is an error line. 
		String line;
		String errLine = null;
		int count = 0;
		boolean haveError = false;
		int errorLines = 0;
		try {
			while ((line = reader.readLine()) != null) {
				count++;
				int ind;
				// use contains, since the string is not th estart of the line. 
				if ((ind=line.indexOf(ThreadedObserver.MODULEEXCEPTIONLINE)) >= 0) {
//					if (ind > 0) {
//						errLine = line.substring(ind);
//					}
//					else {
						errLine = line;
						haveError = true;
						errorLines = 1;
//					}
				}
				else if (haveError) {
					// append the line so we get most of the stack trace. 
					if (line.contains("***************************************")) {
						break;
					}
					errLine += "%0A" + line;
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return errLine;
		}
		return errLine;
	}
}
