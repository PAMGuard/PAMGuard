package PamController.command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import PamController.PamController;
import PamController.pamBuoyGlobals;
import networkTransfer.send.NetworkSender;
import pamguard.GlobalArguments;

public class MulticastController extends CommandManager {
	
	//The multicast addresses are in the range 224.0.0.0 through 239.255.255.255

	private static String unitName = "Multicast Controller";
	private PamController pamController;
	private String mAddress;
	private int mPort;
	private MulticastSocket socket;
	InetAddress inetAddr;

	static private final int MAX_COMMAND_LENGTH = 4096;
	
	private byte[] byteBuffer = new byte[MAX_COMMAND_LENGTH];
	private DatagramPacket lastDatagram;

	private String testString = "Multicast Networking test complete";
	
	public MulticastController(PamController pamController) {
		super(pamController, unitName);
		this.pamController = pamController;
		this.mAddress = pamBuoyGlobals.getMulticastAddress();
		this.mPort = pamBuoyGlobals.getMulticastPort();
		
		Thread t = new Thread(new ListenerThread());
		t.start();
	}

	public void runListenerLoop() {
		try {
			//			inetAddr = InetAddress.getByName(mAddress);
			//			SocketAddress sockAddr = new SocketAddress() 
			//			socket = new MulticastSocket(mPort);
			//			socket.joinGroup(inetAddr, null);
			//			socket.joinG

			// open port
			InetAddress mcastaddr = InetAddress.getByName(mAddress);
			InetSocketAddress group = new InetSocketAddress(mcastaddr, mPort);
			NetworkInterface netIf = NetworkInterface.getByName("bge0");
			socket = new MulticastSocket(mPort);
			socket.joinGroup(group, netIf);
			socket.setSoTimeout(0);

			System.out.printf("Waiting for multicast messages at %s port %d\n", mAddress, mPort);

			//  sit in loop
			while (true) {
				try {
					DatagramPacket datagram = new DatagramPacket(byteBuffer, MAX_COMMAND_LENGTH);
					socket.receive(datagram);
					processDatagram(datagram);
				}
				catch (IOException ioE) {
					ioE.printStackTrace();
					break;
				}
			}

			// close port.

			socket.leaveGroup(group, netIf);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void processDatagram(DatagramPacket datagram) {
//		System.out.println("********************************* Multicast controller received data packet");
		lastDatagram = datagram;
		String str = new String(datagram.getData(), 0, datagram.getLength());
//		System.out.println("********************************* Multicast String:" + str);
//		str = str.substring(0, datagram.getLength());
//		System.out.println("Datagram received \"" + str + "\"");
		
		interpretCommand(str, datagram.getData());
	}

	@Override
	public boolean interpretCommand(String commandString, byte[] commandBytes) {
		// just in case this is the test string used to trigger the firewall, try to ignore it. 
		try {
			if (commandString.trim().equals(testString)) {
				System.out.println(commandString);
				return false;
			}
		}
		catch (Exception e) {
			
		}
		return super.interpretCommand(commandString, commandBytes);
	}

	@Override
	public boolean sendData(ExtCommand extCommand, String dataString) {
		if (dataString == null || dataString.length() == 0) {
			return false;
		}
		DatagramPacket senderInfo = lastDatagram;
		String commandName;
		if (extCommand == null) {
			commandName = "Unknown";
		}
		else {
			commandName = extCommand.getName();
		}
//		System.out.printf("Send back data \"%s\" to %s port %d\n", dataString, senderInfo.getAddress(), senderInfo.getPort());
		/*
		 *  for multicast, we need to send a slightly different string back which has the station id as part of
		 *  the returned string. These will be 0 if they weren't passed in at the command line.  
		 */
		String id1 = GlobalArguments.getParam(NetworkSender.ID1);
		String id2 = GlobalArguments.getParam(NetworkSender.ID2);
		String bigString = String.format("%s,%s,%s,%s", commandName, id1, id2, dataString);
		
//		dataString += "\n";
		DatagramPacket packet = new DatagramPacket(bigString.getBytes(), bigString.length());
		packet.setAddress(senderInfo.getAddress());
		packet.setPort(senderInfo.getPort());
		try {
			socket.send(packet);
//			receiveSocket.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private class ListenerThread implements Runnable {

		@Override
		public void run() {
			runListenerLoop();
		}

	}
}
