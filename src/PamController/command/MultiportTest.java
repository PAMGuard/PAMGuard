package PamController.command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Simple test of multiport communication. 
 * @author dg50
 *
 */
public class MultiportTest {

	private String addr = "224.100.20.20";
	private int port = 1290;
	
	private String tstStr = "ping";
	
	private int BUFFLEN = 512;
	private byte[] retBuffer = new byte[BUFFLEN];
	
	public static void main(String[] args) {
		new MultiportTest().run();
	}
	
	public MultiportTest() {
		// TODO Auto-generated constructor stub
	}

	private void run() {
		InetAddress mcastaddr;
		DatagramSocket socket = null;
//		listInterfaces();
		try {
//			mcastaddr = InetAddress.getByName(addr);
//			InetSocketAddress group = new InetSocketAddress(mcastaddr, port);
//			NetworkInterface netIf = NetworkInterface.getByName("bge0");
//			MulticastSocket socket = new MulticastSocket(port);
//			socket.joinGroup(group, netIf);
			socket = new DatagramSocket();
			socket.setSoTimeout(1000);
			
			DatagramPacket dataGram  = new DatagramPacket(tstStr.getBytes(), tstStr.length(),
					InetAddress.getByName(addr), port);
			
			socket.send(dataGram);

			DatagramPacket reGram = new DatagramPacket(retBuffer, BUFFLEN);
			while (true) {
				socket.receive(reGram);
				String retStr = new String(reGram.getData(), 0, reGram.getLength());
				System.out.printf("\"%s\" received from %s %s port %d\n", retStr, reGram.getAddress(), reGram.getSocketAddress(), reGram.getPort());
			}
			
			// clean up
//			socket.leaveGroup(group, netIf);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		socket.close();
		System.out.println("Done");
		
	}

	private void listInterfaces() {

		try {
			// there are 46 of these on my laptop, enumerated from 1. 0 is null.
			for (int i = 0; i < 50; i++) {
				NetworkInterface netIf = NetworkInterface.getByIndex(i);
				System.out.printf("Interface %d : %s\n", i, netIf);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
