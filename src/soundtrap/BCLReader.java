package soundtrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reader for BCL files. 
 * @author Doug Gillespie
 *
 */
public class BCLReader {

	private File bclFile;
	private BufferedReader reader;

	public BCLReader(File bclFile) {
		this.bclFile = bclFile;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * Read the next line from the BCL file. 
	 * @return line of data or null;
	 */
	public BCLLine nextBCLLine() {
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (line == null) {
			return null;
		}
		return BCLLine.makeBCLLine(line);
	}

	public boolean open() {

		FileReader fr = null;
		try {
			fr = new FileReader(bclFile);
		} catch (FileNotFoundException e) {
			System.out.println(String.format("Error - %s not found.  Skipping this dwv file.",bclFile.getName()));
//			e.printStackTrace();
			return false;
		}

		reader = new BufferedReader(fr);
		
		String line = null;
		// read the first line which should contain "rtime","mticks","report","state","nl","thr","scnt"
		 try {
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reader = null;
	}


}
