package networkTransfer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public interface NetworkReceiverInterface {
	
	public void stopConnectionThread();
		
	public int getNDataIn();
	
	public int getNPacketsIn();
	
	public static NetworkObject readNetworkObject(DataInputStream dis,Socket clientSocket) throws IOException {
		
		int duSize;
		short buoyId1;
		short buoyId2;
		short dataId1;
		int dataId2;
		int dataLen;
		short dataVersion;
		int bytesLeft;
		byte[] receiveBuffer = new byte[1024*20];
		int bytesRead;

		
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
		
		NetworkObject receivedObject = new NetworkObject(clientSocket, dataVersion, buoyId1, buoyId2, dataId1, dataId2, receiveBuffer, dataLen);
		
		return receivedObject;
	}

	public void runReceiver();

}
