package clickDetector;

import java.io.File;
import java.io.IOException;

public class FileWriteTest {

//	static int[] ints = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int[] ints = new int[12];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = (int) Math.pow(10, i);
		}
		
		File aFile = new File("c:/clicks/testWrite.dat");
		try {
			WindowsFile wFile = new WindowsFile(aFile,"rwd");
			for (int i = 0; i < ints.length; i++) {
				wFile.writeInt(ints[i]);
			}
			wFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int[] newInts = new int[ints.length];
		try {
			WindowsFile wFile = new WindowsFile(aFile,"rwd");
			for (int i = 0; i < ints.length; i++) {
				newInts[i] = wFile.readInt();
//				System.out.println(String.format("old = %d, new = %d", ints[i], newInts[i]));
			}
			wFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
