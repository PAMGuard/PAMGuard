package modbustcp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ModbusTCP {

	private Socket tcpSocket;
	
	private static final int DEFAULT_PORT = 502;
	
	private static final int DEFAULT_LENGTH = 512;
	
	private static final int DEFAULT_PROTOCOL = 0;
	
	private short commandId = 0;
		
	private ByteArrayOutputStream commandOutputBytes;
	
	private byte[] inputBytes;

	private ModbusTCP(Socket socket) {
		tcpSocket = socket;
		commandOutputBytes = new ByteArrayOutputStream(DEFAULT_LENGTH);
		inputBytes = new byte[DEFAULT_LENGTH];
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	public static ModbusTCP openSocket(String address) throws ModbusTCPException {
		return openSocket(address, DEFAULT_PORT);
	}
	
	public static ModbusTCP openSocket(String address, int port) throws ModbusTCPException {
		InetAddress inetAddr = null;
		Socket socket = null;
		try {
			inetAddr = InetAddress.getByName(address);
			socket = new Socket(inetAddr, port);
			socket.setSoTimeout(1000);
		} catch (UnknownHostException e) {
//			e.printStackTrace();
			throw new ModbusTCPException("Unknown host exception: " + e.getMessage());
		} catch (IOException e) {
//			e.printStackTrace();
			throw new ModbusTCPException("TCP Socket exception: " + e.getMessage());
		}
		return new ModbusTCP(socket);
	}

	public InetAddress getInetAddress() {
		return tcpSocket.getInetAddress();
	}
	
	public int getPort() {
		return tcpSocket.getPort();
	}
	
	public boolean isAlive() {
		return !tcpSocket.isClosed();
	}
	
	public void close() {
		try {
			tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTimeout(int timeout) throws ModbusTCPException {
		try {
			tcpSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
			throw new ModbusTCPException("Can't set socket timeout: " + e.getMessage());
		}
	}
	
	public ModbusData sendCommand(int command, int ...args) throws ModbusTCPException {
		commandOutputBytes.reset();
		DataOutputStream commandOutputStream = new DataOutputStream(commandOutputBytes);
		try {
			commandOutputStream.writeShort(++commandId);
			commandOutputStream.writeShort(DEFAULT_PROTOCOL);
			int remBytes = 2 + args.length*2;
			commandOutputStream.writeShort(remBytes);
			commandOutputStream.writeByte(0xFF); //  unit id (not used)
			commandOutputStream.writeByte(command);
			for (int i = 0; i < args.length; i++) {
				commandOutputStream.writeShort(args[i]);
			}
		} catch (IOException e) {
			e.printStackTrace(); // will never happen writing to a byte array
		}
		try {
			tcpSocket.getOutputStream().write(commandOutputBytes.toByteArray());
		} catch (IOException e) {
//			e.printStackTrace();
			throw new ModbusTCPException("Can't write command to TCP: " + e.getMessage());
		}
		// now try to read a reply
		int bytesRead = 0;
		try {
			bytesRead = tcpSocket.getInputStream().read(inputBytes);
		} catch (IOException e) {
//			e.printStackTrace();
			throw new ModbusTCPException("Can't read data from TCP: " + e.getMessage());
		}
		if (bytesRead < 9) {
			throw new ModbusTCPException("Insufficient data returned from TCP");
		}
		return new ModbusData(inputBytes, bytesRead);
	}
}
