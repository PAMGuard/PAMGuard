package pamguard;
/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006, Doug Gillespie, Paul Redmond, David McLaren, Rick Dewar
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import Acquisition.FolderInputSystem;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamguardVersionInfo;
import PamController.pamBuoyGlobals;
import PamController.fileprocessing.ReprocessStoreChoice;
import PamModel.SMRUEnable;
import PamUtils.FileFunctions;
import PamUtils.PamExceptionHandler;
import PamUtils.PlatformInfo;
import PamUtils.Splash;
import PamUtils.PlatformInfo.OSType;
import PamView.FullScreen;
import PamView.ScreenSize;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.debug.Debug;
import binaryFileStorage.BinaryStore;
import dataPlotsFX.JamieDev;
import generalDatabase.DBControl;
import networkTransfer.send.NetworkSender;
import offlineProcessing.OfflineTaskManager;
import rocca.RoccaDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
//import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * Pamguard main class. 
 * 
 * @author Douglas Gillespie
 *
 *
 */
public class Pamguard {

	/**
	 * PAMGUARD can be started in three different modes. <p>
	 * Normal mode (no args in) is for everyday data processing 
	 * and collection. <p>
	 * -v = Viewer mode which will connect to a database and re-display data
	 * from a given time period. <p>
	 * -m = Mixed mode which will connect to a database, Sounds are analysed from file
	 * and new results written to the database, but other data, such as GPS data, are read from
	 * the database synchronised in time to the audio data to correctly reconstruct 
	 * tracks, etc. 
	 * PAMGuard generally needs more memory than allocated by default and you'll need to tell it where 
	 * various library files are for access to Windows i/o devices. You will therefore need the following 
	 * two commands to the jre, which if you're running from Eclipse are entered into the 
	 * Arguments / VM arguments section of the run configuration:
	 * -Xmx6g
	 * -Djava.library.path=lib64
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Debug.setPrintDebug(false); // make sure the class instantiates static members. 
		try {			
			if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else {
				//do not use the mac version...it's awful
				//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
			//		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			//		        if ("Nimbus".equals(info.getName())) {
			//		            UIManager.setLookAndFeel(info.getClassName());
			//		            break;
			//		        }
			//		    }
			//			javax.swing.UIManager.put("ScrollBar.thumb", new javax.swing.plaf.ColorUIResource(255,255,0));
			//			javax.swing.UIManager.put("Button.foreground", new javax.swing.plaf.ColorUIResource(0,255,255));
			//			System.out.println("Dump ui preferences");
			//			javax.swing.UIDefaults ui = javax.swing.UIManager.getDefaults();
			//			java.util.Enumeration keys = ui.keys();
			//			Object nxt;
			//			while (keys.hasMoreElements()) {
			//				nxt = keys.nextElement();
			//			  System.out.println(keys.nextElement() + ": " + ui.get(nxt));
			//			}
			//			PamColors.getInstance().setColors();
		} catch (Exception e) { }

		int runMode = PamController.RUN_NORMAL;
		String InputPsf = "NULL";
		


		// set up the system to output to both a log file and the console window.  Also
		// set up a monitor to check for the size of the folder every hour - if it gets
		// too big, just stop logging the messages
		String logFile = getSettingsFolder() + File.separator + "PamguardLog";
		System.setOut(new ProxyPrintStream(System.out, logFile));
		System.setErr(new ProxyPrintStream(System.err, logFile));   
		FolderSizeMonitor folderSizeMon = new FolderSizeMonitor();
		Thread folderSizeThread  = new Thread(folderSizeMon);
		folderSizeThread.start();

//		TimeZone.setDefault(PamCalendar.defaultTimeZone);

		System.out.println("**********************************************************");
		// print out the entire command line
		if (args != null && args.length > 0) {
			System.out.printf("Command line options: ");
			for (int i = 0; i < args.length; i++) {
				System.out.printf("\"%s\" ", args[i]);
			}
			System.out.printf("\n");
		}
		try {
			// get the java runnable file name. 
			//	    	http://stackoverflow.com/questions/4294522/jar-file-name-form-java-code
			System.out.println(Pamguard.class.getProtectionDomain().getCodeSource().getLocation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean showSplash = true;
		if (args != null) {
			int nArgs = args.length;
			int iArg = 0;
			String anArg;
			while (iArg < nArgs) {
				anArg = args[iArg++];
				if (anArg.equalsIgnoreCase("-v")) {
					runMode = PamController.RUN_PAMVIEW;
					System.out.println("PAMGUARD Viewer");
				}
				else if (anArg.equalsIgnoreCase("-m")) {
					runMode = PamController.RUN_MIXEDMODE;
					System.out.println("PAMGUARD Offline mixed mode");
				}
				else if (anArg.equalsIgnoreCase("-nr")) {
					runMode = PamController.RUN_NETWORKRECEIVER;
					System.out.println("PAMGUARD Network Reciever Mode");
				}
				else if (anArg.equalsIgnoreCase("-nosplash")) {
					showSplash = false;
				}
				else if (anArg.equalsIgnoreCase(GlobalArguments.BATCHFLAG)) {
					// flag to say we're in batch processing mode. Can be used
					// to avoid one or two dialogs that pop up in Viewer mode. 
					GlobalArguments.setParam(GlobalArguments.BATCHFLAG, Boolean.TRUE.toString());
				}

				//	removed SEICHE switch when the two SEICHE modules were converted to plugins				
				//				else if (anArg.equalsIgnoreCase("-seiche")) {
				//					SEICHEEnable.setEnable(true);
				//					System.out.println("Enabling Seiche modules.");
				//				}
				else if (anArg.equalsIgnoreCase("-smru")) {
					SMRUEnable.setEnable(true);
					System.out.println("Enabling SMRU modules.");
				}

				else if (anArg.equalsIgnoreCase("-fullscreen")) {
					FullScreen.setGoFullScreen(true);
					System.out.println("Setting full screen view");
				}
				else if (anArg.equalsIgnoreCase("-decimus")) {
					SMRUEnable.setEnableDecimus(true);
					System.out.println("Enabling DECIMUS modules.");
				}
				else if (anArg.equalsIgnoreCase("-meygen17")) {
					SMRUEnable.setMeygen17(true);
					System.out.println("Enabling Meygen Turbine 2017 fudges.");
				}
				else if (anArg.equalsIgnoreCase("-jamie")) {
					JamieDev.setEnabled(true);
					System.out.println("Enabling Jamie Macaulay modifications.");
				}
				else if (anArg.equalsIgnoreCase("-rocca")) {
					RoccaDev.setEnabled(true);
					System.out.println("Enabling Rocca development mode");
				}
				else if (anArg.equalsIgnoreCase(Debug.flag)) {
					Debug.setPrintDebug(true);
					Debug.out.println("Enabling debug terminal output.");
				}
				else if (anArg.equalsIgnoreCase("-r")) {
					runMode = PamController.RUN_REMOTE;
					System.out.println("remote non gui operation.");
				}
				//set up the GUI
				else if (anArg.equalsIgnoreCase("-swing")) {
					PamGUIManager.setType(PamGUIManager.SWING);
				}
				else if (anArg.equalsIgnoreCase("-fx")) {
					PamGUIManager.setType(PamGUIManager.FX);
				}
				else if (anArg.equalsIgnoreCase("-nogui")) {
					PamGUIManager.setType(PamGUIManager.NOGUI);
					System.out.println("no gui operation.");
				}
				else if (anArg.equalsIgnoreCase("-psf") || anArg.equalsIgnoreCase("-psfx")) {
					String autoPsf = args[iArg++];
					PamSettingManager.remote_psf = autoPsf;
					System.out.println("Running using settings from " + autoPsf);
				}
				else if (anArg.equalsIgnoreCase("-port")) {
					// port id to open a udp port to receive commands
					String port = args[iArg++];
					pamBuoyGlobals.setNetworkControlPort(Integer.parseInt(port));
					System.out.println("Setting UDP control port " + port);
				}
				else if (anArg.equalsIgnoreCase("-multicast") || anArg.equalsIgnoreCase("-mport")) {
					// multicast control (for multiple PAMGuards) 
					String mAddr = args[iArg++];
					int mPort = Integer.parseInt(args[iArg++]);
					pamBuoyGlobals.setMultiportConfig(mAddr, mPort);
					System.out.printf("Setting multicast control addr %s port %d\n", mAddr, mPort);
				}
				else if (anArg.equalsIgnoreCase(OfflineTaskManager.commandFlag)) {
					String taskName = args[iArg++];
					OfflineTaskManager.getManager().addCommandLineTask(taskName);
				}
				else if (anArg.equalsIgnoreCase("-nolog")) {
					System.out.println("Disabling log file from command line switch...");
					ProxyPrintStream.disableLogFile();
				}
				else if (anArg.equalsIgnoreCase(BinaryStore.GlobalFolderArg)) {
					// output folder for binary files. 
					String binFolder = args[iArg++];
					GlobalArguments.setParam(BinaryStore.GlobalFolderArg, binFolder);
					System.out.println("Setting output folder for binary files to " + binFolder);
				}
				else if (anArg.equalsIgnoreCase(DBControl.GlobalDatabaseNameArg)) {
					// database file name
					String dbName = args[iArg++];
					GlobalArguments.setParam(DBControl.GlobalDatabaseNameArg, dbName);
					System.out.println("Setting output database file to " + dbName);
				}
				else if (anArg.equalsIgnoreCase(FolderInputSystem.GlobalWavFolderArg)) {
					// source folder for wav files (or other supported sound files)
					String wavFolder = args[iArg++];
					GlobalArguments.setParam(FolderInputSystem.GlobalWavFolderArg, wavFolder);
					System.out.println("Setting input wav file folder to " + wavFolder);
				}
				else if (anArg.equalsIgnoreCase(PamController.AUTOSTART)) {
					// auto start processing. 
					GlobalArguments.setParam(PamController.AUTOSTART, PamController.AUTOSTART);
					System.out.println("Setting autostart ON");
				}
				else if (anArg.equalsIgnoreCase(PamController.AUTOEXIT)) {
					// auto exit at end of processing. 
					GlobalArguments.setParam(PamController.AUTOEXIT, PamController.AUTOEXIT);
					System.out.println("Setting autoexit ON");
				}
				else if (anArg.equalsIgnoreCase(NetworkSender.ADDRESS)) {
					// auto exit at end of processing. 
					GlobalArguments.setParam(NetworkSender.ADDRESS, args[iArg++]);
				}
				else if (anArg.equalsIgnoreCase(NetworkSender.ID1)) {
					// auto exit at end of processing. 
					GlobalArguments.setParam(NetworkSender.ID1, args[iArg++]);
				}
				else if (anArg.equalsIgnoreCase(NetworkSender.ID2)) {
					// auto exit at end of processing. 
					GlobalArguments.setParam(NetworkSender.ID2, args[iArg++]);
				}
				else if (anArg.equalsIgnoreCase(NetworkSender.PORT)) {
					// auto exit at end of processing. 
					GlobalArguments.setParam(NetworkSender.PORT, args[iArg++]);
				}
				else if (anArg.equalsIgnoreCase(ReprocessStoreChoice.paramName)) {
					String arg = args[iArg++];
					ReprocessStoreChoice choice = ReprocessStoreChoice.valueOf(arg);
					if (choice == null) {
						String warn = String.format("Reprocessing storage input parameter %s value \"%s\" is not a recognised value", ReprocessStoreChoice.paramName, arg);
						WarnOnce.showWarning("Invalid input parameter", warn, WarnOnce.WARNING_MESSAGE);
					}
					GlobalArguments.setParam(ReprocessStoreChoice.paramName, arg);
				}
				else if (anArg.equalsIgnoreCase("-help")) {
					System.out.println("--PamGuard Help");
					System.out.println("\n--For standard GUI deployment run without any options.\n");
					System.out.println("\n--For command line deployment the following options are valid.\n");

					System.out.println("  -r                           : run as a non GUI application");
					System.out.println("  -psf <filename>              : use psf settings from filename");
					System.out.println("  -port  <value>               : UDP connection port.");
					System.out.println("  -devDebug  <str>             : Debug String - Used to activate debug messages.");
					System.out.println("\n--Example command lines.\n");
					System.out.println("java -Djava.library.path=./lib -jar pamGuardGui.jar -r -psf ../experimentalData/opNiTestVisualFft.psf");
					System.out.println("\n");
					System.exit(0);
				}
			}
		}
		//going to need the run mode inside a Runnable later 
		final int chosenRunMode = runMode;
		if(runMode != PamController.RUN_REMOTE && PamGUIManager.getGUIType() != PamGUIManager.NOGUI) {
			//			ScreenSize.startScreenSizeProcess();
			ScreenSize.getScreenBounds();
		}

		/*
		 * 
		 */
		// put some text onto the console at the start saying which version 
		// and run mode we're in, etc.
		// write version information to a String
		if (System.getProperty("os.name").equals("Linux")) {
			String[] command = {
					"cat",
					"/proc/cpuinfo"
			};
			ProcessBuilder pb = new ProcessBuilder(command);
			try {
				Process p = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = br.readLine();
				System.out.println("PAMGUARD running on");
				while (line != null) {
					System.out.println(line);
					if (line.startsWith("cpuid level")) {
						line = null;                  
					}else{
						line = br.readLine();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("PAMGUARD Version " + PamguardVersionInfo.version + " branch " + PamguardVersionInfo.getReleaseType().toString());
//		System.out.println("Revision " + PamguardVersionInfo.getRevision());
		System.out.println("Build Date " + PamguardVersionInfo.date);
		writePropertyString("user.dir");
		writePropertyString("java.home");
		writePropertyString("java.name");
		String javaV = writePropertyString("java.version");
		writePropertyString("java.vendor");
		writePropertyString("java.vm.version");
		writePropertyString("java.vm.name");
		//		writePropertyString("java.specification.name");
		writePropertyString("os.name");
		writePropertyString("os.arch");
		writePropertyString("os.version");
		writePropertyString("java.library.path");
		System.out.println("Logging system messages to " + getSettingsFolder() + File.separator + ProxyPrintStream.getLogfileName());
		//		Pam3DUtils.say3DVersion();
		System.out.println("For further information and bug reporting visit " + PamguardVersionInfo.webAddress);
		System.out.println("If possible, bug reports and support requests should \ncontain a copy of the full text displayed in this window.");
		System.out.println("(Windows users right click on window title bar for edit / copy options)");
		System.out.println("");

		if (checkJavaVersion(javaV) == false) {
			System.exit(0);
		};

		int spashTime = 5000;
		if (SMRUEnable.isEnable()) {
			spashTime = 2000;
		}
		if(runMode == PamController.RUN_REMOTE) {
			spashTime = 0;
		}
		if (showSplash && spashTime > 0 && (PamGUIManager.getGUIType() != PamGUIManager.NOGUI)) {
			new Splash(spashTime, chosenRunMode);
		}
		//		
		final Runnable createPamguard = new Runnable() {
			public void run() {
				PamController.create(chosenRunMode);
			}
		};

		// Add a new exception handler to catch run-time exceptions
		Thread.setDefaultUncaughtExceptionHandler(new PamExceptionHandler());
		System.setProperty("sun.awt.exception.handler", PamExceptionHandler.class.getName());

		/*
		 * Amongst other stuff the call to PamController.create()
		 * will build and show the GUI and the user can't
		 * do much else until that's done so let's have all
		 * that kicked off from with the EDT CJB 2009-06-16
		 * Either of these will call .create, just one is in a different 
		 * thread, so it's at the end of the create function that other automatic 
		 * processes should be started. 
		 *  
		 */

		if (PamGUIManager.isSwing()) {
			SwingUtilities.invokeLater(createPamguard);
		}
		else {
			PamController.create(chosenRunMode);
		}		
		

	}


	static private String writePropertyString(String key) {
		String property = System.getProperty(key);
		if (property == null) {
			System.out.println(String.format("%s: No such property", key));
		}
		else {
			System.out.println(String.format("%s %s", key, property));
		}
		return property;
	}

	/**
	 * Get the settings folder name and if necessary, 
	 * create the folder since it may not exist. 
	 * Originally found in PamSettingManager - moved here and made static
	 * 
	 * @return folder name string, (with no file separator on the end)
	 */
	static public String getSettingsFolder() {
		String settingsFolder = System.getProperty("user.home");
		settingsFolder += File.separator + "Pamguard";
		// now check that folder exists
		File f = FileFunctions.createNonIndexedFolder(settingsFolder);
		return settingsFolder;
	}

	/**
	 * Return the log file name 
	 */
	static public String getLogFileName() {
		return ProxyPrintStream.getLogfileName();
	}

	/**
	 * Disable the log file
	 */
	static public void disableLogFile() {
		ProxyPrintStream.disableLogFile();
	}

	/**
	 * Create a new log file
	 */
	static public void enableLogFile() {
		ProxyPrintStream.createLogFile();
		ProxyPrintStream.createLogFileStream();
	}
	
	/**
	 * Check the java version that is passed to this method against the min and max
	 * java versions specified in PamguardVersionInfo.
	 * 
	 * @param javaV The java version to check
	 * 
	 * @return true if the java version falls within the min/max range, false otherwise
	 */
	public static boolean checkJavaVersion(String javaV) {
		if (javaV == null) return true;
		String[] vBits = javaV.split("[.]");
		String[] minBits = PamguardVersionInfo.minJavaVersion.split("[.]");
		String[] maxBits = PamguardVersionInfo.maxJavaVersion.split("[.]");
		int nBits = Math.min(vBits.length, Math.min(minBits.length, maxBits.length));
		boolean high = false, low = false;
		/*
		 * 		Make up a longer integer number that encapsulates the entire string in each case
		 * allowing three digits per field. 
		 */
		
		int bigV = 0, bigMin = 0, bigMax = 0;
		for (int i = 0; i < nBits; i++) {
			try {
				int v = Integer.valueOf(vBits[i]);
				int max = Integer.valueOf(maxBits[i]);
				int min = Integer.valueOf(minBits[i]);
				int scale = (int) Math.pow(10, (nBits-i-1)*3);
				bigV += v*scale;
				bigMin += min*scale;
				bigMax += max*scale;
			}
			catch (NumberFormatException e) {
				continue;
			}
		}
		String err = null;
		if (bigV < bigMin) {
			err = String.format("The minimum Java version for this PAMGuard release is %s. You are running version %s.\n" +
					"It is likely that some features may not run correctly and we strongly recommend that you\n" +
					"switch to a Java version in the range %s to %s.", PamguardVersionInfo.minJavaVersion, javaV,
					PamguardVersionInfo.minJavaVersion, PamguardVersionInfo.maxJavaVersion);
		}
		else if (bigV > bigMax) {
			err = String.format("The maximum Java version for this PAMGuard release is %s. You are running version %s.\n" +
					"It is possible that some features may not run correctly and we strongly recommend that you\n" +
					"update to a newer version of PAMGuard or switch to a Java version in the range %s to %s.",
					PamguardVersionInfo.maxJavaVersion, javaV, PamguardVersionInfo.minJavaVersion, PamguardVersionInfo.maxJavaVersion);
		}
		if (err != null) {
			System.out.println("************************************************************************************************");
			System.out.println(err);
			System.out.println("************************************************************************************************");
			err += "\n Press OK to continue anyway, Cancel to exit.";
			int ans = WarnOnce.showWarning(null, "Java Version Warning", err, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		return true;
	}


	/**
	 * PrintStream class to use as a proxy for the standard output - prints to both the console and a file
	 *  
	 * http://www.jcgonzalez.com/java-system-err-system-out-examples and
	 * https://stackoverflow.com/questions/38605689/how-to-tie-a-printstream-to-the-system-out-and-err-streams
	 * 
	 * @author mo55
	 *
	 */
	private static class ProxyPrintStream extends PrintStream{

		/** The log file path and prefix, without timestamp and extension */
		private static String fileName = null;

		/** The log file, including the timestamp and extension */
		private static File logFile = null;

		/** The PrintStream for the log file */
		private static PrintStream fileStream = null;

		/** The PrintStream for the console */
		private PrintStream origPrintStream = null;

		public ProxyPrintStream(PrintStream origStream, String filename) {
			super(origStream);
			fileName = filename;
			origPrintStream = origStream;
			createLogFile();
			createLogFileStream();
		}

		/**
		 * Convert the millisecond timestamp into a String with the format yyyyMMdd_HHmmss
		 * 
		 * @param timeInMillis time to convert, in milliseconds
		 * @return
		 */
		private static String getTimeStamp(long timeInMillis) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timeInMillis);
			TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");
			c.setTimeZone(defaultTimeZone);
			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
			df.setTimeZone(defaultTimeZone);
			Date d = c.getTime();
			return df.format(d);
		}

		/**
		 * Print to both the console and the file
		 */
		public synchronized void print(final String str) {
			if (str.contains("WARN org.docx4j") || str.contains("INFO org.docx4j")) return;	// don't bother printing these messages out
			origPrintStream.print(str);
			if (fileStream!=null) {
				String timeStamp = ProxyPrintStream.getTimeStamp(System.currentTimeMillis());
				fileStream.print(timeStamp + ": " + str);
				checkFileSize();
			}
		}

		/**
		 * Print to both the console and the file
		 */
		public synchronized void println(final String str) {
			if (str == null) {
				println("null");
				return;
			}
			if (str.contains("WARN org.docx4j") || str.contains("INFO org.docx4j")) return;	// don't bother printing these messages out
			origPrintStream.println(str);
			if (fileStream!=null) {
				String timeStamp = ProxyPrintStream.getTimeStamp(System.currentTimeMillis());
				fileStream.println(timeStamp + ": " + str);
				checkFileSize();
			}
		}

		/**
		 * Need to override the printf method as well, otherwise it calls
		 * System.out.print between each argument and that puts timestamps throughout
		 * the string.  Instead, compile it all into a single string first
		 * and then call print
		 */
		public synchronized PrintStream printf(String format, Object... args) {
			String theString = String.format(format, args);
			print(theString);
			return this;

		}

		/**
		 * Check the size of the file.  If it is over 50Mb, create a new one
		 */
		private synchronized void checkFileSize() {
			if (logFile == null) return;
			double size = (logFile.length()/1000000.);
			if (size>=50) {
				createLogFile();
				String timeStamp = ProxyPrintStream.getTimeStamp(System.currentTimeMillis());
				fileStream.println(timeStamp + ": Log file continued in " + logFile.getName());
				fileStream.close();
				createLogFileStream();
			}
		}

		/**
		 * Return the name of the current log file, or null if we are not logging
		 * 
		 * @return
		 */
		private static synchronized String getLogfileName() {
			String name=null;
			if (logFile!=null) name = logFile.getName();
			return name;
		}

		/**
		 * Disable the log file
		 */
		private static synchronized void disableLogFile() {
			logFile = null;
			fileStream.close();
			fileStream = null;
		}

		/**
		 * Create a txt filename that includes the fileName path and prefix, and a timestamp.
		 * Note that this just creates the File object.  To create the stream, call createLogFileStream()
		 */
		private static synchronized void createLogFile() {
			String fullName = fileName + "_" + String.valueOf(getTimeStamp(System.currentTimeMillis())) + ".txt";
			logFile = new File(fullName);
		}

		/**
		 * Create a new log file stream from the logFile File object.  This operation is separate from the
		 * createLogFile() method so that when a log file gets too big and we start a new one, we can generate
		 * a new filename and write it to the old stream before switching over
		 */
		private static synchronized void createLogFileStream() {
			try {
				FileOutputStream fout = new FileOutputStream(logFile,true);
				fileStream = new PrintStream(fout);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fileStream = null;
				logFile = null;
			}
		}

	}

	private static class FolderSizeMonitor implements Runnable {
		@Override
		public void run() {
			while(true) {
				long length = 0;
				File dir = new File(getSettingsFolder());
				File[] txtFiles = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".txt");
					}
				});
				for (File file : txtFiles) {
					if (file.isFile()) length += file.length();
				}

				// check if the total size of all txt files in the folder is > 2Gb.  If
				// so, something is going wrong so disable the log file
				if (length > 2000000000) {
					System.out.println("**** Warning - size of txt files in " 
							+ getSettingsFolder()
							+ " is > 2 Gb.  Disabling logging for now.  Please clean up the folder to restore logging ****");
					disableLogFile();
				}

				// now sleep for an hour before trying again
				try {
					Thread.sleep(60*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}


}
