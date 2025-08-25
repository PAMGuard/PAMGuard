package binaryFileStorage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper class for an input stream which counts the number of bytes
 * read, skipped etc. so that a getPos() function can be used to check 
 * the total number of bytes now read. 
 * @author Doug Gillespie
 *
 */
public class CountingInputStream extends InputStream {

	private InputStream parentStream;
	
	private long pos = 0;

	private int markPos;
	
	public CountingInputStream(InputStream parentStream) {
		super();
		this.parentStream = parentStream;
	}

	@Override
	public int read() throws IOException {
		int read = parentStream.read();
		pos += 1;
		return read;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return parentStream.available();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		parentStream.close();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(int arg0) {
		if (markSupported()) {
			markPos = arg0;
		}
		parentStream.mark(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return parentStream.markSupported();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		// be careful, since this may call into the other write functions and
		// end up incrementing the count multiple times.
		long currPos = pos;
		int read =  parentStream.read(arg0, arg1, arg2);
		pos = currPos + read;
		return read;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] arg0) throws IOException {
		// be careful, since this may call into the other write functions and
		// end up incrementing the count multiple times.
		long currPos = pos;
		int read = parentStream.read(arg0);
		pos = currPos + read;
		return read;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		parentStream.reset();
		pos = markPos;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long arg0) throws IOException {
		long currPos = pos;
		long skipped = parentStream.skip(arg0);
		pos = currPos + skipped;
		return skipped;
	}
	
	/**
	 * @return the pos
	 */
	public long getPos() {
		return pos;
	}
	
}
