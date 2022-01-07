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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Producer {
	private DatagramSocket socket;

	static final int BUFFER_SIZE = 1000;

	public Producer() {
		System.out.println("Producer()");
	}

	// ==========================
	public void send() {

		// header:
		// uint pkt no :4
		// char 8 PamID :8
		// char 5 Version :5
		// short dataLen :2
		// byte datatype :1
		// total: :20

		try {
			socket = new DatagramSocket();
		} // end try
		catch (SocketException socketException) {
			socketException.printStackTrace();
		}

		// Create a ByteBuffer using a byte array
		byte[] bytes = new byte[44];

		// Set up the header
		// for(int i =0; i< 12; i++){
		// bytes[i] = 1;
		// }

		ByteBuffer headerBuff = ByteBuffer.wrap(bytes, 0, 20);

		// packet number
		headerBuff.putInt(1);

		// Message ID
		// Create a character ByteBuffer
		// CharBuffer cbuf = headerBuff.asCharBuffer();
		// Write a string
		// cbuf.put("a string");
		// headerBuff.put((byte)'P').put((byte)'A').put((byte)'M').put((byte)'G').put((byte)'U').put((byte)'A').put((byte)'R').put((byte)'D');
		String pamid = "PAMGUARD";
		for (int i = 0; i < pamid.length(); i++) {
			headerBuff.put((byte) pamid.charAt(i));
		}

		// Version
		headerBuff.put((byte) '0');
		headerBuff.put((byte) '.');
		headerBuff.put((byte) '0');
		headerBuff.put((byte) '.');
		headerBuff.put((byte) '1');

		headerBuff.putShort((short) 14); // Data Len
		headerBuff.put((byte) 99); // Data Type

		// wrap(byte[] array, int offset, int length)
		// ByteBuffer buf = ByteBuffer.wrap(dataBytes);
		ByteBuffer buf = ByteBuffer.wrap(bytes, 20, 24);

		double[] dataArray = new double[3];

		for (int i = 0; i < 3; i++) {
			dataArray[i] = 10000.0;
			buf.putDouble(dataArray[i]);
		}

		// Create a non-direct ByteBuffer with a 10 byte capacity
		// The underlying storage is a byte array.
		// buf = ByteBuffer.allocate(10);

		// Create a direct (memory-mapped) ByteBuffer with a 10 byte
		// capacity.
		// buf = ByteBuffer.allocateDirect(10);

		try {
			// byte data[] = message.getBytes();
			byte data[] = bytes;
			DatagramPacket sendPacket = new DatagramPacket(data, data.length,
					InetAddress.getLocalHost(), 8000);
			socket.send(sendPacket);
			System.out.println("Producer:packet sent");
		} catch (IOException ioException) {
			System.out.println("Producer:ioException");
		}
	} // send}
}
