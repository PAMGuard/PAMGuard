package PamUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A series of classes to load and import data stored in text files (includes .csv files)
 * @author Jamie Macaulay
 *
 */
public class TxtFileUtils {

	/**
	 * Load data from a .csv file into a 2D ArrayList. 
	 * @param filename
	 * @return a 2D array of values. 
	 */
	public static ArrayList<ArrayList<Double>> importCSVData(String filename){
		return importTxtData(filename,",");
	}

	/**
	 * Load data from a text file into a 2D ArrayList<Double>. Elements will be separated based on the delimiter string input.
	 * Note that lines containing NaN values are skipped.
	 *   
	 * @param filename- file path to import.
	 * @param delimeter- separate elements in the file using the delimiter. e.g. \\t for tab, \\s+ for space, , for .csv. 
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 */
	public static ArrayList<ArrayList<Double>> importTxtData(String filename,String delimeter){
		return importTxtData(filename, delimeter, false);
	}
	
	/**
	 * Load data from a text file into a 2D ArrayList<Double>. Elements will be separated based on the delimiter string input.
	 * Lines containing NaN values are included if nanOk = true, or skipped if nanOk=false.  NaN will be replaced with nulls.
	 *   
	 * @param filename- file path to import.
	 * @param delimeter- separate elements in the file using the delimiter. e.g. \\t for tab, \\s+ for space, , for .csv. 
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 * @param nanOk leave rows containing NaN values if true, skip rows if false
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> importTxtData(String filename,String delimeter, boolean nanOk){
		Collection<String> lines= importTxtToCollection( filename);
		if (lines==null) return null; 

		ArrayList<ArrayList<Double>> csvResults=new ArrayList<ArrayList<Double>>();

		 // Get the number instance
        NumberFormat nF
            = NumberFormat.getNumberInstance();
        
		boolean isNaN=false;
		for (String line : lines) {

			//System.out.println(line);

			String[] recordsOnLine = line.split(delimeter);

			ArrayList<Double> data=new ArrayList<Double>();

			isNaN=false;
			for (int i=0; i<recordsOnLine.length; i++){
				Double dat;
				if (recordsOnLine[i].equals("NaN")){
					isNaN=true;
					dat=null;
				}
				else 
					try {	
						//Note for some reason saving in MSDOS CSV format is an issue...
						
						
//						//This was causing isses with some numbers with e-06...dunno why so switched to parse double
//						String input = new String(recordsOnLine[i].toCharArray()); 
//						System.out.println("|" + recordsOnLine[i] + "|");
//						dat = nF.parse(input).doubleValue();
					
						//5/08/2022 - there was a bug here where there is some sort of invisible character that does not appear on the 
						//print screen - the only way you can tell is the char array is greater than the number of digits - removed all non numeric
						//characters. 
						// updated again on 15/11/23 to include - signs, or you end up with the abs(of every number!)
						String number = new String(recordsOnLine[i].strip().replaceAll("[^\\d.-]", ""));
						dat = Double.valueOf(number);
						//dat = DecimalFormat.getNumberInstance().parse(new String(recordsOnLine[i].strip().toCharArray())).doubleValue();
					}
				catch (Exception e){
					e.printStackTrace();
					dat=null;
				}

				data.add(dat);
			}

			if (!isNaN || nanOk)	csvResults.add(data);
		}

		return csvResults;

	}

	/**
	 * Import text data into an array of strings. 
	 * @param filename- file path of file to import. 
	 */
	public static List<String>  importTxtToCollection(String filename){

		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					new File(filename)
					));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return lines; 
	}

	/**
	 * Load data from a text file into a 2D ArrayList<String>. Elements will be separated based on the delimiter string input. 
	 * Any line containing NaN will be ignored
	 * 
	 * @param filePath - file path to import. 
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 */
	public static ArrayList<ArrayList<String>> importTxtDataToString(String filePath) {
		return importTxtDataToString(filePath,",",false);
	}
	
	/**
	 * Load data from a text file into a 2D ArrayList<String>. Elements will be separated based on the delimiter string input. 
	 * 
	 * @param filePath - file path to import. 
	 * @param nanOk leave rows containing NaN values if true, skip rows if false
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 */
	public static ArrayList<ArrayList<String>> importTxtDataToString(String filePath, boolean nanOk) {
		return importTxtDataToString(filePath,",", nanOk);
	}
	
	/**
	 * Load data from a text file into a 2D ArrayList<String>. Elements will be separated based on the delimiter string input. 
	 * Any line containing NaN will be ignored
	 * 
	 * @param filePath - file path to import. 
	 * @param delimeter- separate elements in the file using the delimiter. 
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 */
	public static ArrayList<ArrayList<String>> importTxtDataToString(String filePath, String delimeter) {
		return importTxtDataToString(filePath,delimeter,false);
	}
	
	/**
	 * Load data from a text file into a 2D ArrayList<String>. Elements will be separated based on the delimiter string input. 
	 * @param filename- file path to import. 
	 * @param delimeter- separate elements in the file using the delimiter. 
	 * @param nanOk leave rows containing NaN values if true, skip rows if false
	 * @return 2D ArrayList<String> of rows and columns defined by delimiter. 
	 */
	public static ArrayList<ArrayList<String>> importTxtDataToString(String filename,String delimeter, boolean nanOk){

		Collection<String> lines= importTxtToCollection( filename);
		if (lines==null) return null; 

		ArrayList<ArrayList<String>> csvResults=new ArrayList<ArrayList<String>>();
		ArrayList<String> lineDelim; 
		for (String line : lines) {
			lineDelim = parseTxtLine( line,  delimeter, nanOk);
			if (lineDelim!=null) csvResults.add(lineDelim);
		}

		return csvResults;
	}


	/**
	 * Convert into ArrayList of strings based on a delimiter.  Any lines containing NaN are ignored
	 * 
	 * @param line - the line to parse
	 * @param delimiter the delimiter to use
	 * @return split strings based on delimiter. 
	 */
	public static ArrayList<String> parseTxtLine(String line, String delimiter){
		return parseTxtLine(line, delimiter, false);
	}
	
	
	/**
	 * Convert into ArrayList of strings based on a delimiter
	 * @param line - the line to parse
	 * @param delimiter the delimiter to use
	 * @param nanOk leave rows containing NaN values if true, skip rows if false
	 * @return split strings based on delimiter. 
	 */
	public static ArrayList<String> parseTxtLine(String line, String delimiter, boolean nanOk){

		boolean isNaN=false;

		String[] recordsOnLine = line.split(delimiter);

		ArrayList<String> data=new ArrayList<String>();

		for (int i=0; i<recordsOnLine.length; i++){
			//System.out.println(i);
			String dat;
			if (recordsOnLine[i].equals("NaN")){
				isNaN=true;
				dat=null;
			}
			else 
				try {	
					dat = recordsOnLine[i];
				}
			catch (Exception e){
				dat=null;
			}

			data.add(dat);
		}

		if (isNaN==true && !nanOk) return null;
		else return data; 
	}

	/**
	 * Print out data. 
	 * @param data
	 */
	public static void printData(ArrayList<ArrayList<String>> data){

		for (int i=0; i<data.size(); i++){
			System.out.println("");
			for (int j=0; j<data.get(i).size(); j++){
				System.out.print(" - " + data.get(i).get(j));
			}
		}
		System.out.println("");

	}

	/**
	 * Write string data to a file
	 * 
	 * @param filename the filename to write to
	 * @param dataToWrite An ArrayList of String objects to write.  Each index in the array will be
	 * written on a new line
	 * @return true if successful, false if an exception was thrown
	 */
	public static boolean exportTxtData(String filename, ArrayList<String> dataToWrite) {
		
        BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			
			for (int i=0; i<dataToWrite.size(); i++) {
				writer.write(dataToWrite.get(i));
				writer.newLine();
			}
			
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        return true;
	}
	
	public static String exportMatlab2DArray(double[][] array, int dp) {
		if (array == null || array.length == 0 || array[0].length == 0) {
			return null;
		}
		int ny = array.length;
		int nx = array[0].length;
		String fmt = String.format("%%.%df", dp);
		String str = new String("[");
		for (int ix = 0; ix < nx; ix++) {
			str += String.format(fmt, array[0][ix]);
			for (int iy = 1; iy < ny; iy++) {
				str += ","+String.format(fmt, array[iy][ix]);
			}
			if (ix < nx-1) {
				str += ";"; 
			}
		}
		
		str += "];";
		return str;
	}

}
