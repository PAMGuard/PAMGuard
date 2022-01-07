package modbustcp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ModbusData {

	private short commandId;
	private short protocol;
	private short length;
	private byte unitId;
	private DataInputStream dis;
	private byte command;
	private byte dataBytes;

	public ModbusData(byte[] inputBytes, int bytesRead) throws ModbusTCPException {
		ByteArrayInputStream bis;
		dis = new DataInputStream(bis = new ByteArrayInputStream(inputBytes));
		try {
			commandId = dis.readShort();
			protocol = dis.readShort();
			length = dis.readShort();
			unitId = dis.readByte(); // FF
			command = dis.readByte();
			dataBytes = dis.readByte();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModbusTCPException("Invalid Modbus TCP message length: " + inputBytes);
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		dis.close();
	}

	public short[] getShortData() throws ModbusTCPException {
		int len = getLength()-3;
		int nShort = len/Short.BYTES;
		if (nShort < 0) {
			throw new ModbusTCPException("Invalid number of bytes to unpack in message: " + len);
		}
		short[] shortData = new short[nShort];
		try {
			for (int i = 0; i < nShort; i++) {
				shortData[i] = dis.readShort();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModbusTCPException("Unable to unpack short data array length: " + nShort);
		}
		return shortData;
	}
	
	public float[] getRevFloatData() throws ModbusTCPException {
		int len = getLength()-3;
		int nFloat = len/Float.BYTES;
		if (nFloat < 0) {
			throw new ModbusTCPException("Invalid number of bytes to unpack in message: " + len);
		}
		float[] floatData = new float[nFloat];
		try {
			for (int i = 0; i < nFloat; i++) {
				int[] byteData = new int[4];
				for (int j = 0; j < 4; j++) {
					byteData[j] = dis.readByte();
				}
				int intVal = (byteData[0]<<24) | (byteData[1]<<16) | (byteData[2]<<8) | byteData[3];
//				int intVal = dis.readInt();
				intVal = Integer.reverseBytes(intVal);
				floatData[i] = Float.intBitsToFloat(intVal);
//				floatData[i] = dis.readFloat();
//				floatData[i] = Float.
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModbusTCPException("Unable to unpack float data array length: " + nFloat);
		}
		return floatData;
	}
	
	public float[] getFloatData() throws ModbusTCPException {
		int len = getLength()-3;
		int nFloat = len/Float.BYTES;
		if (nFloat < 0) {
			throw new ModbusTCPException("Invalid number of bytes to unpack in message: " + len);
		}
		float[] floatData = new float[nFloat];
		try {
			for (int i = 0; i < nFloat; i++) {
				floatData[i] = dis.readFloat();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModbusTCPException("Unable to unpack float data array length: " + nFloat);
		}
		return floatData;
	}

	/**
	 * @return the commandId
	 */
	public short getCommandId() {
		return commandId;
	}

	/**
	 * @return the protocol
	 */
	public short getProtocol() {
		return protocol;
	}

	/**
	 * @return the length
	 */
	public short getLength() {
		return length;
	}

	/**
	 * @return the unitId
	 */
	public byte getUnitId() {
		return unitId;
	}

	/**
	 * @return the dis
	 */
	protected DataInputStream getDataInputStream() {
		return dis;
	}

	/**
	 * @return the command
	 */
	public byte getCommand() {
		return command;
	}

	/**
	 * @return the dataBytes
	 */
	public byte getDataBytes() {
		return dataBytes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Req. %d, Command 0X%X, Data length %d", commandId, command, dataBytes);
	}

}
