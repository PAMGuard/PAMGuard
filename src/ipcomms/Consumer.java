/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
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
package ipcomms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.Timer;

public class Consumer {
	private DatagramSocket socket;

	ArrayList<byte[]> newDataUnits;

	int packetCount;

	double bytesPerSecond = 0.0;

	float lastvalue = 0.0f;

	long startTimeNano = System.nanoTime();

	long endTimeNano = System.nanoTime();

	long elaspsedTimeNano = endTimeNano - startTimeNano;

	long intervalTimeNano = 8000000000l;

	int headerSize = 20;
	int FIXMEdatasize = 100;

	Thread captureThread;
	int pktInCnt = 0;

	public double displayPacketData(byte[] b) {
		System.out.println("NEW PACKET DATA ==============================================");
		// decode packet header
		
		ByteBuffer headerBuff = ByteBuffer.wrap(b, 0, b.length);
		headerBuff.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		double pktDataSum = 0.0;
		
		System.out.println("Data length in bytes:" + b.length);
		for(int i=0; i< (FIXMEdatasize); i++){
			double elementI = headerBuff.getDouble();
	//		System.out.println(elementI);
			pktDataSum+= elementI;
		}
		
		System.out.println("Pkt(" + pktInCnt +") Data Sum= " + pktDataSum);
		
		pktInCnt++;
		System.out.println("--");
		return(pktDataSum);
	}

	public void displayPacketHeader(byte[] b) {
		System.out.println("NEW PACKET HEADER ==============================================");
				
		ByteBuffer buf = ByteBuffer.wrap(b, 0, headerSize);

		// header:
		// uint pkt no
		// char 8 PamID
		// char 5 Version
		// short dataLen
		// byte datatype
		// 
		/*
		 * int pktno = buf.getInt(); char[] pamID = new char[8]; for(int i = 0;
		 * i<8; i++) { pamID[i] = buf.getChar(); }
		 * 
		 * 
		 * String pid = new String(pamID); System.out.println("Packet:" + pktno + " " +
		 * pid);
		 */

		System.out.print("Byte Values: ");
		/*
		 * for(int i =0; i<b.length; i++){ if (Math.IEEEremainder(i,16) == 0) {
		 * System.out.println(); }
		 * 
		 * //System.out.print(" "+ (char) b[i]); System.out.print(" "+ b[i]);
		 *  }
		 */
		System.out.println();

		// decode packet header
		ByteBuffer headerBuff = ByteBuffer.wrap(b, 0, headerSize);
		headerBuff.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		// System.out.println("position:" + headerBuff.position());;

		// packet number
		int pktnum = headerBuff.getInt();
		// System.out.println("position:" + headerBuff.position());;

		// Name & Version
		byte[] name = new byte[8];
		byte[] vers = new byte[5];
		headerBuff.get(name, 0, 8);
		headerBuff.get(vers, 0, 5);
		// System.out.println("position:" + headerBuff.position());;

		short dataLen = headerBuff.getShort(); // Data Len
		// System.out.println("position:" + headerBuff.position());;

		byte datatype = headerBuff.get(); // Data Type
		// System.out.println("position:" + headerBuff.position());;

		System.out.println("pkt num:" + pktnum);
		String pamName = new String(name);
		System.out.println("Name: " + pamName);
		String pamVers = new String(vers);
		System.out.println("Version: " + pamVers);
		System.out.println("data len: " + dataLen);
		System.out.println("data type: " + datatype);
	}

	// Start pdu packaging timer
	Timer t = new Timer(40000, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			System.out.println("timer");
			// if (newDataUnits.size() >1) {
			captureThread.stop();
					
			t.stop();
			// decode packet header
			try{
				BufferedWriter writer = new BufferedWriter(new FileWriter("packetSums.txt"));

			System.out.println("Iterate through stored DataPackets");
			for (int i = 0; i < newDataUnits.size(); i++) {
	//			displayPacketHeader(newDataUnits.get(i));
				double pktDataSum = displayPacketData(newDataUnits.get(i));
				
				writer.write("Pkt(" + i +") Data Sum= " + pktDataSum + "\n");
			}
			System.out.println(packetCount);
			
			writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// while(!newDataUnits.isEmpty()){
			// outputData.addPamData(newDataUnits.get(0));
			// System.out.println("packetCount: " + packetCount);

			// newDataUnits.remove(0);
			// }
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			socket.close();
		}
	});

	public Consumer() {
		System.out.println("Consumer()");
		try {
			socket = new DatagramSocket(9000);
		} // end try
		catch (SocketException socketException) {
			socketException.printStackTrace();
		}
		newDataUnits = new ArrayList<byte[]>();

		// Create a thread to capture the sound card input
		// and start it running.
		captureThread = new Thread(new CaptureThread());
		captureThread.start();

		t.start();
	}

	class CaptureThread implements Runnable {
		public void run() {
			/*
			 * Sit here reading data from the port. Every time a new NMEA string
			 * arrives, make it into a StringBuffer and add that StringBuffer to
			 * the newStrings array list. the timer in the main thread will take
			 * them out and pass them onto the rest of Pamguard.
			 */
			int elements = 100;
			int elementSize = 8;
			DatagramPacket recievePacket;
			byte data[] = new byte[elements * elementSize]; //
			recievePacket = new DatagramPacket(data,
					data.length);

			//setReceiveBufferSize
			// Increasing SO_RCVBUF may allow the network implementation to buffer multiple packets when packets arrive faster than are being received using receive(DatagramPacket). 
			// Note: It is implementation specific if a packet larger than SO_RCVBUF can be received.
			
			int receiveBufferSize; 
			try{
				receiveBufferSize = socket.getReceiveBufferSize();
				System.out.println("socket.getReceiveBufferSize():" + receiveBufferSize);
				receiveBufferSize = receiveBufferSize*10000;
				System.out.println("New socket.getReceiveBufferSize():" + receiveBufferSize);
				socket.setReceiveBufferSize(receiveBufferSize);
			} // end try
			catch (SocketException socketException) {
				socketException.printStackTrace();
			}	
			
			while (true) {
				try {
					/*endTimeNano = System.nanoTime();
					elaspsedTimeNano = endTimeNano - startTimeNano;
					if ((elaspsedTimeNano > intervalTimeNano)
							|| packetCount == (60000 - 1)) {
						System.out.println("packetCount == " + packetCount);
						System.out.println("Time = "
								+ (elaspsedTimeNano / 1000000000.0));
						System.exit(0);
					}*/

					
					//DatagramPacket recievePacket;
					//byte data[] = new byte[elements * elementSize]; //
					//recievePacket = new DatagramPacket(data,data.length);

					data = new byte[elements * elementSize]; //
					
					socket.receive(recievePacket);
					packetCount++;

					//newDataUnits.add(data);

					if (Math.IEEEremainder(packetCount,10000) == 0) {
					 System.out.println(packetCount);
					}
					//System.out.println("Consumer: Data packet number: " +
					//packetCount + ", received of size :" +
					//recievePacket.getLength());

					// ByteArrayInputStream bin = new ByteArrayInputStream
					// (recievePacket.getData());

					//			 
					// byte[] b = new byte[recievePacket.getLength()];
					// newDataUnits.add(b);

					// bin.read(b, 0,b.length);

					// System.out.print("Byte Values: ");
					// for(int i =0; i<b.length; i++){
					// System.out.print(" "+ (char)b[i]);
					// b[i] = (byte) (60 + i);
					// System.out.print(" "+ (byte)b[i]);
					// }
					// System.out.println();

					// Create a ByteBuffer using a byte array
					// byte[] bytes = new byte[12];
					// header = ByteBuffer.wrap(b);
					// buf = ByteBuffer.wrap(b);

					// ByteBuffer buf = ByteBuffer.wrap(b, 0,
					// elements*elementSize);
					// float[] dataArray = new float[elements];

					//				
					// for(int i = 0; i< elements; i++ ){
					// dataArray[i] = buf.getFloat();
					// }
					// float f = dataArray[0];
					// for(int i = 0; i< elements; i++ ){
					// System.out.print(" " + dataArray[i]);
					// }
					// System.out.println ("data= " + f);

					// if (f == lastvalue + 1.0f) {
					// //System.out.println("+ 1.0 pass");
					// lastvalue = f;
					// } else {
					// System.out.println("+ 1.0 fail");
					// System.exit(0);
					// }

					// if(f== 240000.0f) {
					// System.out.println("final value of 24000f recieved: " +
					// f);
					// System.exit(0);
					// }
					// System.out.println("===== END OF PACKET =====");
					// System.out.println();

				} catch (UnknownHostException e) {
					// System.out.println(e);
				} catch (IOException e) {
					// System.out.println(e);
				}
			}
		}
	}
	
	class DisplayThread implements Runnable {
		public void run() {}}
	
}
