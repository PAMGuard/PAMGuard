package clickDetector;

import java.io.IOException;

public interface ReadWinFile {

	public abstract byte readByte() throws IOException;
	
	public abstract int readWinInt() throws IOException;

	public abstract int readWinShort() throws IOException;

	public abstract float readWinFloat() throws IOException;

	public abstract double readWinDouble() throws IOException;

	public abstract long readWinLong() throws IOException;
}
