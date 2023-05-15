package PamController.extern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExternalCapture extends ExternalController {

	private InputStream inputStream;
	private LineObserver lineObserver;
	private BufferedReader bufferedReader;
	private volatile boolean keepReading = true;

	public ExternalCapture(InputStream inputStream, LineObserver lineObserver) {
		this.inputStream = inputStream;
		this.lineObserver = lineObserver;
		InputStreamReader isr = new InputStreamReader(inputStream);
		bufferedReader = new BufferedReader(isr);
		Thread t = new Thread(new StreamReader());
		t.start();		
	}
	
	private class StreamReader implements Runnable {

		@Override
		public void run() {
			readInputStream();
		}
		
	}
	
	public void stopCapture() {
		keepReading = false;
	}

	public void readInputStream() {
		try {
			while (keepReading) {
				String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				if (lineObserver != null) {
					lineObserver.newLine(line); 
				}
			}
		} catch (IOException e) {
			System.out.println("IOError in ExternalCapture.readInputStream: " + e.getMessage());
		}
	}


}
