package networkTransfer.receive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ListIterator;

import PamUtils.PamCalendar;
import PamguardMVC.debug.Debug;
import networkTransfer.NetworkObject;
import networkTransfer.send.NetworkObjectPacker;

public class NetworkReceiveThread implements Runnable {
	
	private Socket clientSocket;
	private InputStream inStream;
//	private byte[] duBuffer = new byte[0];
//	private int duBufferPos;
	private int duSize;
	private short buoyId1;
	private short buoyId2;
	private short dataId1;
	private int dataId2;
	private int dataLen;
//	private int unitBytesRead;
	private volatile boolean keepRunning = true;
	private NetworkDataUser networkDataUser;
	public static final int HEADID = 0xFFEEDDCC;
	long threadCreatedAt;

	public NetworkReceiveThread(Socket clientSocket, NetworkDataUser networkDataUser) {
		super();
		this.clientSocket = clientSocket;
		this.networkDataUser = networkDataUser;
		threadCreatedAt = System.currentTimeMillis();
//		System.out.printf("finished thread constructor at %s\n", PamCalendar.formatTime(threadCreatedAt, true));
	}

	public void stopThread() {
		keepRunning = false;
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {	
		receiveData();
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void receiveData() {
		byte[] receiveBuffer = new byte[1024*20];
		int bytesRead;
		int bytesLeft;
		DataInputStream dis;
		OutputStream doStream;
		int headInt;
		short dataVersion;
		NetworkObjectPacker netObjectPacker = null;
		try {
			long t  = System.currentTimeMillis();
			clientSocket.setTcpNoDelay(true);
			inStream = clientSocket.getInputStream();
			dis = new DataInputStream(inStream);
			doStream = clientSocket.getOutputStream();
			int badHeads;
			long now = System.currentTimeMillis();
//			System.out.printf("Socket opened on input stream %s at %s, %d millis after creation\n", this.toString(),
//					PamCalendar.formatTime(System.currentTimeMillis(), true), now-threadCreatedAt);
//			System.out.printf("Thread start took %d millis, constructed at %s\n", now-t, 
//					PamCalendar.formatTime(threadCreatedAt, true));
			while (keepRunning) {
				/**
				 * Ensure (or try to) that we're always starting on an object boundary, beginning with 
				 * 0xFEDC 
				 */
				badHeads = 0;
				headInt = dis.readInt();
				long readStart;
				while (headInt != HEADID) {
					/*
					 * This shouldn't normally happen, but does happen if data were
					 * interrupted in some way due to network problems. 
					 * Keep reading one byte at a time until it's all correctly lined up again !
					 */
					badHeads++;
					if (badHeads == 1) {
						System.out.printf("Unable to find head in network stream try %d got 0x%x %s\n", badHeads, headInt, this.toString());
					}
//					else if (badHeads%10 == 0) {
//						System.out.printf(".");
//					}
					int oneByte = dis.readUnsignedByte();
					headInt = ((headInt&0xFFFFFF)<<8) | oneByte;
				}
				readStart = System.currentTimeMillis();
				if (badHeads > 0) {
					System.out.printf("Correct network header alignment restored after %d bytes\n", badHeads);
				}
				// now must be aligned on the start of real data. 
				duSize = dis.readInt();        //4
				dataVersion = dis.readShort(); //6
				buoyId1 = dis.readShort();     //8
				buoyId2 = dis.readShort();     //10
				dataId1 = dis.readShort();     //12
				dataId2 = dis.readInt();       //16
				dataLen = dis.readInt();       //20
				bytesLeft = duSize - 24;       // 24 is the size of the above + the HEADID code. 
				if (receiveBuffer.length < bytesLeft) {
					receiveBuffer = new byte[bytesLeft];
				}		
				bytesRead = 0;
				// use readFully - will block until all data have arrived. Important with large packets. 
				dis.readFully(receiveBuffer, 0, bytesLeft);
				bytesRead = bytesLeft;
				//					while (bytesRead < bytesLeft) {
				//						bytesRead += dis.read(receiveBuffer, bytesRead, bytesLeft-bytesRead);
				//					}
				//					if (bytesRead< 0) {
				//						break;
				//					}
				NetworkObject receivedObject = new NetworkObject(clientSocket, dataVersion, buoyId1, buoyId2, dataId1, dataId2, receiveBuffer, dataLen);
//				Debug.out.println("Received " + receivedObject.toString());
				
				long interpretStart = System.currentTimeMillis();
				NetworkObject returnedData = networkDataUser.interpretData(receivedObject);
				long interpretEnd = System.currentTimeMillis();
				if (returnedData != null) {
					if (netObjectPacker == null) {
						netObjectPacker = new NetworkObjectPacker();
					}
					byte[] bytes = netObjectPacker.packData(returnedData);

					long writeStart = System.currentTimeMillis();
					doStream.write(bytes, 0, bytes.length);
					long writeEnd = System.currentTimeMillis();
//					System.out.printf("Returned %d bytes of data %s", bytes.length, returnedData.toString());
//					System.out.printf("   Read in %d ms, Interpret %dms, write %dms\n", interpretStart-readStart,
//							interpretEnd-interpretStart, writeEnd-interpretEnd);
//					System.out.println("Data written " + returnedData.toString());
				}
				//					interpretReadData(receiveBuffer, bytesRead);
				//					System.out.println(String.format("Data received from network = " + bytesRead + " bytes"));

			}

		} catch (IOException e) {
			System.out.printf("Socket closed on input stream %s: %s\n", this.toString(), e.getMessage());
//			receiveThreads.remove(this);
		}
		catch (Exception e) {
			System.out.println(String.format("General exception in input stream %s", this.toString()));
			e.printStackTrace();
		}
		networkDataUser.socketClosed(this);
		
//		System.out.println("Receive thread terminated");
//					clientSocket.close();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Receiver Thread address %s, channel %s, port %d", clientSocket.getInetAddress().getHostName(), 
				clientSocket.getChannel(), clientSocket.getPort());
	}

	/**
	 * @return the clientSocket
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}
}
