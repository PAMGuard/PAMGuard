package clickDetector;

import java.io.IOException;

/*
 * Class to make a buffer o shove Windows data into before writing to file in one big lump
 */
public class WindowsBuffer implements WriteWinFile {

	private byte[] bytes;
	
	private int offset = 0;
	
	WindowsBuffer() {
		
	}
	
	public WindowsBuffer(byte[] data) {
		this.bytes = data;
		this.offset = 0;
	}
	
	WindowsBuffer(int startLength) {
		checkLength(startLength);
	}
	
	private void checkLength(int required) {
		if (bytes == null || bytes.length < required) {
			byte[] newBytes = new byte[required];
			for (int i = 0; i < offset; i++) {
				newBytes[i] = bytes[i];
			}
			bytes = newBytes;
		}
	}
	
	public int getUsedBytes() {
		return offset;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	/**
	 * totally resets, including deleting of the buffer.
	 */
	public void clear() {
		bytes = null;
		reset();
	}
	
	/**
	 * Just rests the offset, so writing starts again at the beginning, but does not 
	 * delete the allocated memory.  
	 */
	public void reset() {
		offset = 0;
	}
	

	public void writeWinInt(int val) throws IOException {
		writeWinShort(val & 0xFFFF);
		writeWinShort(val >> 16);
	}
	
	public int readWinInt() throws IOException {
		int val = readWinShort();
		if (val < 0) val += 65536;
		val += readWinShort() << 16;
		return val;
	}

	public void writeWinShort(int val) throws IOException {
		/*
		 * Write out low byte first
		 */
		writeByte(0xFF & val);
		writeByte(0xFF & (val >> 8));
	}
	
	public int readWinShort() throws IOException {
		int val = readByte();
		if (val < 0) val += 256;
		val += readByte() << 8;
		return val;
	}
	
	public void writeByte(int b)throws IOException  {
		writeByte((byte) b);
	}
	
	public void writeByte(byte b) throws IOException {
		checkLength(offset+1);
		bytes[offset++] = b;
	}
	
	public int readByte() throws IOException  {
		return bytes[offset++];
	}
	
	public void writeWinFloat(float val) throws IOException {
//		java.lang.Float floatValue = new Float(val);
		int intValue = Float.floatToIntBits(val);
		writeWinInt(intValue);
	}
	public void writeWinDouble(double val) throws IOException {
		long intValue = Double.doubleToLongBits(val);
		writeWinLong(intValue);
	}
	public void writeWinLong(long longValue) throws IOException {
		writeWinInt((int) (0xFFFFFFFF & longValue));
		writeWinInt((int) (0xFFFFFFFF & (longValue >> 32)));
	}
	
}
