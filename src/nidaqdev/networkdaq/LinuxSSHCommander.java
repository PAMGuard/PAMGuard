package nidaqdev.networkdaq;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
//import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Class for issuing commands directly to the linux shell on the cRio
 * @author Doug Gillespie
 *
 */
public class LinuxSSHCommander {

	private JSch jSch;

	private Session shellSession;

	private Channel shellChannel;

	private PamWarning warning = new PamWarning("Linux Commander", "", 0);

	//	private ByteOutputStream byteOutputStream;

	private PipedOutputStream pipedOutputStream;

	private Session commandSession;
	
	private CRioErrorLog cRioErrorLog = new CRioErrorLog();

	private NINetworkDaq niNetworkDaq;

	public LinuxSSHCommander(NINetworkDaq niNetworkDaq) {
		super();
		this.niNetworkDaq = niNetworkDaq;
		jSch = new JSch();
	}

	/**
	 * Close the Shell session
	 */
	public void closeShell() {
		if (shellSession == null) {
			return;
		}
		if (shellChannel != null) {
			shellChannel.disconnect();
			shellChannel = null;
		}
		shellSession.disconnect();
		shellSession = null;
	}
	
	/**
	 * Get the current host address. 
	 * @return the current host address. 
	 */
	public String getShellHost() {
		if (shellSession == null) {
			return null;
		}
		return shellSession.getHost();
	}

	/**
	 * Open a shell session
	 * @param host host ip address.
	 * @param user user name
	 * @param password password
	 * @return true if opened ok. 
	 */
	public boolean openShell(String host, String user, String password) {

		closeShell();
		
		cRioErrorLog.clearErrorLog();

		SessionUser sessionUser = new SessionUser(password);

		try {
			shellSession = jSch.getSession(user, host, 22);
			//			session.setX11Port();
			shellSession.setPassword(password);
			shellSession.setUserInfo(sessionUser);
			shellSession.connect(30000);
		} catch (JSchException e) {
			//			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			warning.setWarningMessage(e.getLocalizedMessage());
			warning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(warning);
			shellSession = null;
			return false;
		}
		warning.setWarningMessage("Connected to cRio " + host);
		warning.setWarnignLevel(0);
		WarningSystem.getWarningSystem().removeWarning(warning);

		try {
			shellChannel = shellSession.openChannel("shell");
		} catch (JSchException e) {
			System.out.println("session.openChannel: " + e.getLocalizedMessage());
			warning.setWarningMessage(e.getLocalizedMessage());
			warning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(warning);
			shellSession = null;
			shellChannel = null;
			return false;
		}

//		shellChannel.setOutputStream(System.out);
		shellChannel.setOutputStream(new ShellOutputStream());

		PipedInputStream pipedInputStream = null;
		try {
			pipedInputStream = new PipedInputStream(pipedOutputStream = new PipedOutputStream());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		shellChannel.setInputStream(pipedInputStream);


		try {
			shellChannel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
		}

		return true;	
	}

	/**
	 * Write a linux command to the open shell <br>
	 * A carriage return will automatically be added if necessary. 
	 * @param command Command string
	 */
	public void writeCommand(String command) {
		if (command == null) {
			command = "";
		}
		if (command.endsWith("\n") == false) {
			command += "\n"; 
		}
		try {
			//				System.out.println("Write command " + command);
			pipedOutputStream.write(command.getBytes());
			//				System.out.println("Wrote command " + command);
		} catch (IOException e) {
			System.err.println("Unable to execute Linux command \"" + command + "\" " + e.getMessage());
		} catch (NullPointerException e2) {
			String str = String.format("Unable to execute Linux command \"%s\" (No Output Stream to Linux Shell)", command);
			System.err.println(str);
		}
	}

	/**
	 * Check there is a carriage return on the end of a command string. 
	 * @param command
	 * @return command with carriage return added. 
	 */
	private String checkCarriageReturn(String command) {
		if (command == null) {
			command = "";
		}
		if (command.endsWith("\n")) {
			return command;
		}
		else {
			return command + "\n";
		}
	}

	public boolean openCommander(String host, String user, String password) {
		SessionUser sessionUser = new SessionUser(password);

		try {
			commandSession = jSch.getSession(user, host, 22);
			//			session.setX11Port();
			commandSession.setPassword(password);
			commandSession.setUserInfo(sessionUser);
			commandSession.connect(30000);
		} catch (JSchException e) {
			//			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			warning.setWarningMessage(e.getLocalizedMessage());
			warning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(warning);
			return false;
		}
		warning.setWarningMessage("Connected to cRio " + host);
		warning.setWarnignLevel(0);
		WarningSystem.getWarningSystem().addWarning(warning);

		try {
			shellChannel = commandSession.openChannel("exec");
		} catch (JSchException e) {
			System.out.println("commandSession.openChannel: " + e.getLocalizedMessage());
			warning.setWarningMessage(e.getLocalizedMessage());
			warning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(warning);
			return false;
		}
		return true;
	}

	public String execCommand(String command) {

//		ByteOutputStream bos = new ByteOutputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		boolean ok = execCommand(command, new BufferedOutputStream(bos));
//		boolean ok = execCommand(command, null);
		if (ok == false) {
			return null;
		}

//		String output = new String(bos.getBytes());
		String output = new String(bos.toByteArray());
//		ByteInputStream is = bos.newInputStream();
		ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray(),0,bos.size());
		int nBytes = is.available();


		System.out.println("Cammand result: \"" + nBytes +"\"" + output + "\"");
		return output;
	}

	public boolean execCommand(String command, OutputStream outStream) {
		command = checkCarriageReturn(command);
		ChannelExec channel = null;
//		ByteInputStream bis = new ByteInputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0],0,0);
		if (outStream == null) {
			outStream = System.out;
		}
		try {
			channel=(ChannelExec) commandSession.openChannel("exec");
			//			outStream = channel.getOutputStream();
			//			inStream = channel.getInputStream();
			channel.setOutputStream(outStream);
			//			channel.setInputStream(bis);
			channel.setErrStream(System.err);
			//		      channel.setInputStream(System.in);
			// need to work out a way of writing to the input stream (counter intuitive !). 
			channel.setCommand(command);
			channel.connect();
			channel.disconnect();

			// print out the stream contents
//			System.out.println("Input stream " + new String(bis.getBytes()));
			try {
				byte[] contents = new byte[bis.available()];
				bis.read(contents);
				System.out.println("Input stream " + new String(contents));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (JSchException e) {
			System.out.println(e.getLocalizedMessage());
			warning.setWarningMessage(e.getLocalizedMessage());
			warning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(warning);
			return false;
		} 
		//		catch (IOException e) {
		//			System.out.println(e.getLocalizedMessage());
		//			warning.setWarningMessage(e.getLocalizedMessage());
		//			warning.setWarnignLevel(2);
		//			WarningSystem.getWarningSystem().addWarning(warning);
		//			return false;
		//		}
		return true;
	}

	private class SessionUser extends MyUserInfo {

		private String password;

		public SessionUser(String password) {
			super();
			this.password = password;
		}

		@Override
		public String getPassphrase() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassphrase(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean promptPassword(String msg) {
			System.out.println("Prompt Password: " + msg);
			return true;
		}

		@Override
		public boolean promptYesNo(String msg) {
			if (msg != null && msg.startsWith("The authenticity of host")) {
				return true;
			}
			Object[] options={ "yes", "no" };
			int foo=JOptionPane.showOptionDialog(null, 
					msg,
					"Linux Shell Warning", 
					JOptionPane.DEFAULT_OPTION, 
					JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			return foo==0;
		}

		@Override
		public void showMessage(String msg) {
			System.out.println("Show Message: " + msg);
		}

	}
	public static abstract class MyUserInfo
	implements UserInfo, UIKeyboardInteractive{
		@Override
		public String getPassword(){ return null; }
		@Override
		public boolean promptYesNo(String str){ return false; }
		@Override
		public String getPassphrase(){ return null; }
		@Override
		public boolean promptPassphrase(String message){ return false; }
		@Override
		public boolean promptPassword(String message){ return false; }
		@Override
		public void showMessage(String message){ }
		@Override
		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo){
			return null;
		}
	}
	
	private class ShellOutputStream extends OutputStream {

		byte[] buildingString = new byte[1024];
		int stringPos = 0;
		
		@Override
		public void write(int arg0) throws IOException {
			
		}

		@Override
		public void write(byte[] byteData, int startByte, int nBytes) throws IOException {
			buildString(byteData, startByte, nBytes);
		}

		private void buildString(byte[] byteData, int startByte, int nBytes) {
			if (nBytes + stringPos >= buildingString.length) {
				buildingString = Arrays.copyOf(buildingString, nBytes + stringPos);
			}
			int p = startByte;
			for (int i = 0; i < nBytes; i++, p++) {
				char c = (char)(byteData[p]&0xFF);
				if (c == '\n') {
					String completeString = new String(buildingString, 0, stringPos);
					System.out.println("Crio:" + completeString);
					stringPos = 0;
					interpretCrioString(completeString);
				}
				else if (c == '\r') {
				}
				else {
					buildingString[stringPos++] = byteData[p];
				}
			}
		}

		@Override
		public void write(byte[] byteData) throws IOException {
			buildString(byteData, 0, byteData.length);
		}
		
	}

	/**
	 * Take a look at the text content output from the cRio and see if it indicates 
	 * an error. Errors are picked up here even when the network is failing due to 
	 * UDP and TCP port problems. 
	 * @param completeString
	 */
	public void interpretCrioString(String cRioString) {
		CRioErrorStrings cRioError = cRioErrorLog.checkString(cRioString);
		if (cRioError == null) {
			return;
		}
		niNetworkDaq.cRioCommandLineError(cRioError);
	}
}
