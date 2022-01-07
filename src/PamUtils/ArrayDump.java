package PamUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ArrayDump {

	/**
	 * Dump an array into a text file that can be imported to Matlab or anything else 
	 * @param fileName
	 * @param array
	 */
	public static boolean dumpArray(String fileName, double[][] array) {
		File file = new File(fileName);
		FileWriter fileWriter;
		int nI = array.length;
		int nJ = array[0].length;
		try {
			fileWriter = new FileWriter(file);
			for (int i = 0; i < nI; i++) {
				for (int j = 0; j < nJ; j++) {
					fileWriter.append(new Double(array[i][j]).toString());
					if (j < nJ-1) {
						fileWriter.append(", ");
					}
				}
				fileWriter.append("\r\n");
			}

			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println(String.format("Sucessful File dump to %s of %d by %d array", 
				fileName, nI, nJ));
		return true;
	}
}
