package binaryFileStorage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileAccessTest {

	private String fileName = "C:\\PamguardTest\\filetest.dat";
	
	private boolean keepReading = true;
	
	private final int nMax = 200;
	
	private final long writePause = 100;
	
	public static void main(String[] args) {
		FileAccessTest t = new FileAccessTest();
		t.run();
	}

	private void run() {
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		FileChannel fileChannel;
		try {
			 fos = new FileOutputStream(fileName);
			  fileChannel = fos.getChannel();
			 dos = new DataOutputStream(fos);
			  fileChannel.lock(0, Long.MAX_VALUE, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread rt = new Thread(new FileReader());
		rt.start();
		for (int i = 1; i <= nMax; i++) {
			try {
				dos.writeInt(i);
				System.out.printf("Wrote to file: %d\n", i);
				Thread.sleep(writePause);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		keepReading = false;
		try {
			rt.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	class FileReader implements Runnable {

		@Override
		public void run() {
			while (keepReading) {
				try {
					readFile();
					Thread.sleep(writePause);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		private void readFile() {
			FileInputStream fis;
			DataInputStream dis;
			File file = new File(fileName);
			Boolean canWrite = file.canWrite();
			Boolean canRead = file.canRead();
			int n=0;
			try {
				dis = new DataInputStream(fis = new FileInputStream(fileName));
				while (dis.available() > 4) {
					n = dis.readInt();
				}
				dis.close();
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
			}
			System.out.printf("Read from file: %d Read=%s, Write=%s\n", n, canRead.toString(), canWrite.toString());
		}

	}

}
