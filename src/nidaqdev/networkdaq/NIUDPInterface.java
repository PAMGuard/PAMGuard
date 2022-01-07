package nidaqdev.networkdaq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * UDP commands to and from the NI CRio chassis. 
 * @author Doug Gillespie
 *
 */
public class NIUDPInterface {

	private NINetworkDaq niNetworkDaq;
	
	private static final int RXBUFFLEN = 255;
	
//	private String niAddress = "192.168.2.211";
//	private String niAddress = "localhost";

	public NIUDPInterface(NINetworkDaq niNetworkDaq) {
		this.niNetworkDaq = niNetworkDaq;
	}

	public String sendCommand(String command, String niAddress, String niUDPPort) {
		return sendCommand(command, niAddress, niUDPPort);
	}
	
	public String sendCommand(String command, String ipAddress, int port) {
		InetAddress inAddress;
		try {
			inAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			System.out.println("Unknown host address " + ipAddress);
			return null;
		}
		DatagramPacket dp = new DatagramPacket(command.getBytes(), command.length(), inAddress, port);
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.send(dp);
			byte[] rxBuff = new byte[RXBUFFLEN];
			DatagramPacket rp = new DatagramPacket(rxBuff, RXBUFFLEN);
			socket.setSoTimeout(1000);
			socket.receive(rp);
			socket.close();
			return new String(rxBuff, 0, rp.getLength());
		} 
		catch (SocketTimeoutException e) {
//			System.out.println("Socket timeout exception for command " + command);
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
