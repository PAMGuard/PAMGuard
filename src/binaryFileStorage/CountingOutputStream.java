package binaryFileStorage;

import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {
	
	private OutputStream parentStream;
	
	private long byteCount = 0;

	public CountingOutputStream(OutputStream parentStream) {
		super();
		this.parentStream = parentStream;
	}

	@Override
	public void write(int b) throws IOException {
		parentStream.write(b);
		byteCount++;
	}

	@Override
	public void write(byte[] b) throws IOException {
		// be careful, since this may call into the other write functions and
		// end up incrementing the count multiple times.
		long currentCount = byteCount;
		parentStream.write(b);
		byteCount = currentCount + b.length;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// be careful, since this may call into the other write functions and
		// end up incrementing the count multiple times.
		long currentCount = byteCount;
		parentStream.write(b, off, len);
		byteCount = currentCount + len;
	}

	@Override
	public void flush() throws IOException {
		parentStream.flush();
	}

	@Override
	public void close() throws IOException {
		parentStream.close();
	}

	public long getByteCount() {
		return byteCount;
	}

}
