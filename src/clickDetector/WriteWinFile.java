package clickDetector;

import java.io.IOException;

public interface WriteWinFile {

	public abstract void writeByte(int b) throws IOException;
	
	public abstract void writeWinInt(int val) throws IOException;

	public abstract void writeWinShort(int val) throws IOException;

	public abstract void writeWinFloat(float val) throws IOException;

	public abstract void writeWinDouble(double val) throws IOException;

	public abstract void writeWinLong(long longValue) throws IOException;

}