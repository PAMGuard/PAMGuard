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

public class MultiportController extends CommandManager {
	
	//The multicast addresses are in the range 224.0.0.0 through 239.255.255.255

	private static String unitName = "Multiport Controller";
	private PamController pamController;
	private String mAddress;
	private int mPort;
	private MulticastSocket socket;
	InetAddress inetAddr;

	static private final int MAX_COMMAND_LENGTH = 4096;
	
	private byte[] byteBuffer = new byte[MAX_COMMAND_LENGTH];
	private DatagramPacket lastDatagram;

	public MultiportController(PamController pamController) {
		super(pamController, unitName);
		this.pamController = pamController;
		this.mAddress = pamBuoyGlobals.getMultiportAddress();
		this.mPort = pamBuoyGlobals.getMuliportPort();
		
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
		lastDatagram = datagram;
		String str = new String(datagram.getData(), 0, datagram.getLength());
//		str = str.substring(0, datagram.getLength());
		System.out.println("Datagram received \"" + str + "\"");
		interpretCommand(str);
	}

	@Override
	public boolean sendData(String dataString) {
		DatagramPacket senderInfo = lastDatagram;
		System.out.printf("Send back data \"%s\" to %s port %d\n", dataString, senderInfo.getAddress(), senderInfo.getPort());
//		dataString += "\n";
		DatagramPacket packet = new DatagramPacket(dataString.getBytes(), dataString.length());
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
