package PamController.command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.pamBuoyGlobals;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import SoundRecorder.RecorderControl;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;

/**
 * Class to handle remote control of PAMGuard. 
 * <p>Can receive and execute commands via udp, some commands
 * will return a string of additional information. 
 * <p>Individual commands, actions and return strings are handled by 
 * subclasses of the ExtCommand class. 
 * @author Doug Gillespie
 *
 */
public class NetworkController extends PamControlledUnit {

	
	private PamController pamController;
	
	private ListenerThread listenerThread;
	
	private DatagramSocket receiveSocket;

	private DatagramPacket udpPacket;
	
	private boolean initialisationComplete = false;
	
	static private final int MAX_COMMAND_LENGTH = 256;
	
	private byte[] byteBuffer = new byte[MAX_COMMAND_LENGTH];
	
	private NetworkRecorderTrigger[] recorderTriggers;
	
	private static String unitType = "Network Controller";
	
	private ArrayList<ExtCommand> networkCommands = new ArrayList<ExtCommand>();

	public NetworkController(PamController pamController) {
		super(unitType, unitType);
		this.pamController = pamController;
		
		networkCommands.add(new StartCommand());
		networkCommands.add(new StopCommand());
		networkCommands.add(new PingCommand());
		networkCommands.add(new StatusCommand());
		networkCommands.add(new SummaryCommand());
		networkCommands.add(new ExitCommand());
		networkCommands.add(new KillCommand());
		
		listenerThread = new ListenerThread();
		Thread aThread = new Thread(listenerThread);
		aThread.start();
	}
	
	class ListenerThread implements Runnable {

		@Override
		public void run() {
			if (openUDPPort() == false) {
				return;
			}
			sitInLoop();
			closeUDPPort();
		}
		
	}
	
	/**
	 * Open the UDP port. 
	 * @return true if opened OK
	 */
	public boolean openUDPPort() {
		int udpPort = pamBuoyGlobals.getNetworkControlPort();
		System.out.printf("Opening UDP control port %d\n", udpPort);
		try {
			receiveSocket = new DatagramSocket(udpPort);
			receiveSocket.setSoTimeout(0);
			udpPacket = new DatagramPacket(byteBuffer, MAX_COMMAND_LENGTH);
		} catch (SocketException e) {
			System.out.printf("Cannot open UDP control port %d: %s\n", udpPort, e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Close the UDP port. 
	 */
	public void closeUDPPort() {
		// TODO Auto-generated method stub
		if (receiveSocket == null) {
			return;
		}
		receiveSocket.close();
		receiveSocket = null;
	}

	/**
	 * Infinite loop. The program sits here waiting for
	 * commands and interpreting them as needs. 
	 * <br>It will exit when InterpretCommand returns
	 * false, which it should only do when the exit 
	 * program command has been sent. 
	 */
	public void sitInLoop() {
		
		String udpCommand = null;
		
		while (true) {
			udpCommand = getCommand();
			if (udpCommand == null) {
				continue;
			}
			if (interpretCommand(udpCommand) == false) {
				break;
			}
		}
	}

	/**
	 * Interpret and act on a udp command string. 
	 * @param command command string
	 * @return false if the command was to exit
	 * the program (in which case this thread will
	 * exit and close the port). True otherwise. 
	 */
	private boolean interpretCommand(String command) {
		//System.out.println(String.format("New UDP Command %s", command));
		
		command = command.toLowerCase();
		// strip of the first two letters if they begin pg ...
		if (command.substring(0,2).equals("pg")) {
			command = command.substring(2); 
		}
		ExtCommand extCommand = findCommand(command);
		if (extCommand == null) {
			sendData("Cmd \"" + command + "\" Not Recognised.");
			return false;
		}
		if (extCommand.canExecute() == false) {
			sendData("Cmd \"" + command + "\" Cannot Execute.");
			sendData("   Cmd return string = " + extCommand.getReturnString());
			return false;
		}
		extCommand.execute();
		sendData(extCommand.getReturnString());
		
//		
//		if (command.equals("pgstart")) {
//			sendData("PgAck " + "pgstart");
//			pamController.pamStart();
//		}
//		else if (command.equals("pgstop")) {
//			sendData("PgAck " + "pgstop");
//			pamController.pamStop();
//		}
//		else if (command.equals("pgping")) {
//			sendData("PgAck " + "pgping");
//		}
//		else if (command.equals("pgstatus")) {
//			sendData("PgAck Status " + pamController.getPamStatus());
//		}
//		else if (command.equals("pgsetrec")) {
//			sendData("PgAck pgsetrec");
//			
//			//triggerRecording(String name, int seconds);
//		}
//		else if (command.equals("pgexit")) {
//			sendData("Exiting PAMGUARD");
//			System.exit(0);
//			return false;
//		}
//		else{
//			sendData("PgAck " + "Cmd Not Recognised.");
//		}

		
		return true;
	}
	
	private ExtCommand findCommand(String command) {
		for (ExtCommand aCommand:networkCommands) {
			if (aCommand.getName().equals(command)) {
				return aCommand;
			}
		}
		return null;
	}

	private boolean sendData(String dataString) {
		DatagramPacket packet = new DatagramPacket(dataString.getBytes(), dataString.length());
		packet.setAddress(udpPacket.getAddress());
		packet.setPort(udpPacket.getPort());
		try {
			receiveSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Blocking function to wait for a command string to be sent 
	 * over UDP
	 * @return command string or null if should exit. 
	 */
	private String getCommand() {
		try {
			receiveSocket.receive(udpPacket);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new String(udpPacket.getData(), 0, udpPacket.getLength());
	}

	public void notifyModelChanged(int changeType) {
		switch (changeType){
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			createRecorderTriggers();
			break;
		}
		
	}
	
	private void createRecorderTriggers() {
		ArrayList<PamControlledUnit> recorders = pamController.findControlledUnits(RecorderControl.recorderUnitType);
		if (recorders.size() == 0) {
			return;
		}
		int n = recorders.size();
		recorderTriggers = new NetworkRecorderTrigger[n];
		for (int i = 0; i < n; i++) {
			recorderTriggers[i] = new NetworkRecorderTrigger(this);
			RecorderControl.registerRecorderTrigger(recorderTriggers[i]);
		}
	}
	
	private boolean triggerRecording(String name, int seconds) {
		NetworkRecorderTrigger t = findTrigger(name);
		if (t == null) {
			return false;
		}
		RecorderTriggerData rtd = new RecorderTriggerData("Network Commands", 0, seconds);
		t.setRecorderTriggerData(rtd);
		RecorderControl.actionRecorderTrigger(t, null, PamCalendar.getTimeInMillis());
		return true;
	}
	
	private NetworkRecorderTrigger findTrigger(String name) {
		if (recorderTriggers == null) {
			return null;
		}
		for (int i = 0; i < recorderTriggers.length; i++) {
			if (recorderTriggers[i].recorderControl.getUnitName().equals(name)) {
				return recorderTriggers[i];
			}
		}
		return null;
	}

	private class NetworkRecorderTrigger extends RecorderTrigger {

		private String name;
		
		private RecorderControl recorderControl;
		
		private RecorderTriggerData recorderTriggerData;
		
		/**
		 * @param name
		 */
		public NetworkRecorderTrigger(NetworkController networkController) {
			super(null);
			this.name = "Network Control";
			recorderTriggerData = new RecorderTriggerData("Network Commands", 0,0);
		}

		@Override
		public RecorderTriggerData getDefaultTriggerData() {
			return recorderTriggerData;
		}


		/**
		 * @param recorderTriggerData the recorderTriggerData to set
		 */
		public void setRecorderTriggerData(RecorderTriggerData recorderTriggerData) {
			this.recorderTriggerData = recorderTriggerData;
		}

		@Override
		public boolean triggerDataUnit(PamDataUnit dataUnit, RecorderTriggerData rtData) {
			return true;
		}
		
	}
}
