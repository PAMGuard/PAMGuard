package networkTransfer;

import java.net.Socket;
import java.util.Arrays;

/**
 * Class for handling Network data, will reduce number of parameters in
 * Network calls and be a LOT more flexible.
 * Use with NetworkObjectPacker.  
 * @author dg50
 *
 */
public class NetworkObject {
	
	public static final int CURRENTVERSION = 1;
	private int buoyId1; 
	private int buoyId2;
	private short dataType1;
	private int dataType2;
	private byte[] data;
	private int dataLength;
	private short dataVersion;
	private Socket socket;
	
	public NetworkObject(int buoyId1, int buoyId2, short dataType1, int dataType2, byte[] data, int dataLength) {
		super();
		this.buoyId1 = buoyId1;
		this.buoyId2 = buoyId2;
		this.dataType1 = dataType1;
		this.dataType2 = dataType2;
		this.data = data;
		this.dataLength = dataLength;
	}

	public NetworkObject(int dataVersion, int buoyId1, int buoyId2, short dataType1, int dataType2, String string) {
		this(dataVersion, buoyId1, buoyId2, dataType1, dataType2, string == null ? null : string.getBytes());
	}
	
	public NetworkObject(int dataVersion, int buoyId1, int buoyId2, short dataType1, int dataType2, byte[] data) {
		super();
		this.dataVersion = (short) dataVersion;
		this.buoyId1 = buoyId1;
		this.buoyId2 = buoyId2;
		this.dataType1 = dataType1;
		this.dataType2 = dataType2;
		this.data = data;
		if (data != null) {
			this.dataLength = data.length;
		}
	}

	public NetworkObject(Socket clientSocket, int dataVersion, short buoyId1, short buoyId2, short dataType1,
			int dataType2, byte[] data, int dataLength) {
		this.socket = clientSocket;
		this.dataVersion = (short) dataVersion;
		this.buoyId1 = buoyId1;
		this.buoyId2 = buoyId2;
		this.dataType1 = dataType1;
		this.dataType2 = dataType2;
		this.data = Arrays.copyOf(data, Math.min(data.length, dataLength));
		this.dataLength = dataLength;
	}

	/**
	 * @return the buoyId1
	 */
	public int getBuoyId1() {
		return buoyId1;
	}

	/**
	 * @return the buoyId2
	 */
	public int getBuoyId2() {
		return buoyId2;
	}

	/**
	 * @return the dataType1
	 */
	public short getDataType1() {
		return dataType1;
	}

	/**
	 * @return the dataType2
	 */
	public int getDataType2() {
		return dataType2;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * This is the length of the data. The actual amount of data transmitted will be
	 * this + 24 bytes rounded up to nearest 4. 
	 * @return the dataLength
	 */
	public int getDataLength() {
		return dataLength;
	}
	
	/**
	 * Amount of data actually transferred. 24 + a few more than data length.  
	 * @return number of bytes. 
	 */
	public int getTransferedLength() {
		int txed = dataLength;
		int rem = txed%4;
		if (rem > 0) {
			txed += (4-rem);
		}
		return txed;
	}

	@Override
	public String toString() {
		if (data != null) {
			return String.format("Buoy %d(%d), Type %d(%d), Len %d, data \"%s\"", buoyId1, buoyId2, 
					dataType1, dataType2, dataLength, new String(data));
		}
		return String.format("Buoy %d(%d), Type %d(%d), Len %d, data \"%s\"", buoyId1, buoyId2, 
				dataType1, dataType2, dataLength, "no data");
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * @param socket the socket to set
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * @return the dataVersion
	 */
	public short getDataVersion() {
		return dataVersion;
	}
	
}
