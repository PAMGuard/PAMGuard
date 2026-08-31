package NMEA.udp;

import java.awt.Window;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import NMEA.NMEAProvider;
import PamView.dialog.PamDialogPanel;

public class UdpNMEAProvider extends NMEAProvider {

	public static final String name = "External NMEA Server";
	private static boolean stopActiveNMEAsource;
	
	private Thread runningThread;
	
	public UdpNMEAProvider(NMEAControl nmeaControl) {
		super(nmeaControl);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PamDialogPanel getDialogPanel(Window frame) {
		return new UdpNMEAPanel(this);
	}

	@Override
	public boolean startAcquisition() {
		stopAcquisition();
		
		NMEAParameters nmeaParameters = getNmeaControl().getNmeaParameters();
		Runnable runnable;
		stopActiveNMEAsource = false;
		if (nmeaParameters.multicast) {
			runnable = new MulticastThread();
		}
		else {
			runnable = new UdpThread();
		}
		runningThread = new Thread(runnable);
		runningThread.start();
		return false;
	}

	@Override
	public void stopAcquisition() {
		stopActiveNMEAsource = true;
		if (runningThread != null) {
			try {
				runningThread.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			runningThread = null;
		}
	}
	class UdpThread implements Runnable {
		@Override
		public void run() {
			/*
			 * Sit here reading data from the port. Every time a new NMEA string
			 * arrives, make it into a StringBuffer and add that StringBuffer to
			 * the newStrings array list. the timer in the main thread will take
			 * them out and pass them onto the rest of Pamguard.
			 */
			DatagramSocket GPSsocket = null;
			byte[] buffer = new byte[150];
			int nmeaCount = 1;
			StringBuffer nmeaStringBuffer = new StringBuffer();
			try {

				// Create Datagram Socket
				GPSsocket = new DatagramSocket(getNmeaControl().getNmeaParameters().port);
				GPSsocket.setSoTimeout(2000); // Allows GPS source to be changed even when no udp is being received.

				// Create empty Datagram Packet
				DatagramPacket GPSpacket = new DatagramPacket(buffer,
						buffer.length);
				// receive request from client and get client info
				while (!stopActiveNMEAsource) {
					// TODO: Validate NMEA content - how ?
					// Not much to go on, nmea max len is 82char starting with
					// '$'
					// Maybe also able to check the gps'server' ip
					// InetAddress NMEAServer = GPSpacket.getAddress();

					//System.out.println("nmeaControl port:"+ nmeaControl.nmeaParameters.port);		
					try {
						GPSsocket.receive(GPSpacket);
					} catch (SocketTimeoutException e) {
//						System.out.println("NMEA Time out");
						continue; // otherwise the same string jut gets sent again every 2s !
					}

					// TODO: Recode following line to avoid String creation (may
					// ArrayList newString can hold CharSequence?)
					StringBuffer sb = new StringBuffer(new String(buffer)
					.substring(0, GPSpacket.getLength()));
					// StringBuffer sb = new StringBuffer("Test" +nmeaCount++);
					getNMEAProcess().addNewString(sb);
				}
			} catch (UnknownHostException e) {
				// System.out.println(e);
			} catch (IOException e) {
				// System.out.println(e);
			}
			stopActiveNMEAsource=false;
			// the socket must be closed on exiting the thread, otherwise, it can't
			// be started again. DG. 27/3/06
			if (GPSsocket != null) GPSsocket.close();
		}
	}

	class MulticastThread implements Runnable {
		@Override
		public void run() {
			/*
			 * Sit here reading data from the port. Every time a new NMEA string
			 * arrives, make it into a StringBuffer and add that StringBuffer to
			 * the newStrings array list. the timer in the main thread will take
			 * them out and pass them onto the rest of Pamguard.
			 */
			MulticastSocket GPSsocket = null;
			byte[] buffer = new byte[150];
			int nmeaCount = 1;
			StringBuffer nmeaStringBuffer = new StringBuffer();
			InetAddress group = null;
			try {
				group = InetAddress.getByName(getNmeaControl().getNmeaParameters().multicastGroup);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {

				// Create Datagram Socket
				GPSsocket =  new MulticastSocket(getNmeaControl().getNmeaParameters().port);

				GPSsocket.setSoTimeout(2000); // Allows GPS source to be changed even when no udp is being received.
				GPSsocket.joinGroup(group);

				// Create empty Datagram Packet
				DatagramPacket GPSpacket = new DatagramPacket(buffer,
						buffer.length);
				// receive request from client and get client info
				while (!stopActiveNMEAsource) {
					// TODO: Validate NMEA content - how ?
					// Not much to go on, nmea max len is 82char starting with
					// '$'
					// Maybe also able to check the gps'server' ip
					// InetAddress NMEAServer = GPSpacket.getAddress();

					//System.out.println("nmeaControl port:"+ nmeaControl.nmeaParameters.port);		
					try {
						GPSsocket.receive(GPSpacket);
					} catch (SocketTimeoutException e) {
//						System.out.println("NMEA Time out");
						continue; // otherwise the same string jut gets sent again every 2s !
					}

					// TODO: Recode following line to avoid String creation (may
					// ArrayList newString can hold CharSequence?)
					StringBuffer sb = new StringBuffer(new String(buffer)
					.substring(0, GPSpacket.getLength()));
					// StringBuffer sb = new StringBuffer("Test" +nmeaCount++);
					getNMEAProcess().addNewString(sb);
				}
				
			} catch (UnknownHostException e) {
				// System.out.println(e);
			} catch (IOException e) {
				// System.out.println(e);
			}
			stopActiveNMEAsource=false;
			// the socket must be closed on exiting the thread, otherwise, it can't
			// be started again. DG. 27/3/06

			if (GPSsocket != null) {
				try {
					GPSsocket.leaveGroup(group);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GPSsocket.close();
			}
		}
	}

}
