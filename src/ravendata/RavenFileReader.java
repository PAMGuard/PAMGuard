package ravendata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RavenFileReader {

	private RavenControl ravenControl;
	private File ravenFile;
	private ArrayList<String> columnNames;
	private BufferedReader fileReader;
	
	private static String[] standardColumns = {"Selection", "View", "Channel", "Begin Time (s)", "End Time (s)", "Low Freq (Hz)", "High Freq (Hz)"};
//	private static String[] otherColumns = {"Selection", "View", "Channel", "Begin Time (s)", "End Time (s)", "end", "Low Freq (Hz)", "High Freq (Hz)",
//			"Begin File", "Delta Time (s)", "File Offset (s)", "Occupancy", "Manual Review", "Notes", "SongNumber"};
	/*
	 * Sometimes some names appear in different places. Bugger.
	 * so need to find the index of each of the main columns we want.  
	 */
	private int[] mainIndexes;
private int columnErrors;
	

	public RavenFileReader(RavenControl ravenControl, String ravenFile) throws IOException {
		this.ravenControl = ravenControl;
		this.ravenFile = new File(ravenFile);
		openFile();
	}
	
	public void closeFile() throws IOException {
		if (fileReader != null) {
			fileReader.close();
		}
	}
	
	/**
	 * open the file, read the file header and get a list of column names. 
	 * @throws IOException 
	 */
	private void openFile() throws IOException {
		fileReader = new BufferedReader(new FileReader(ravenFile));
		String firstLine = fileReader.readLine();
		if (firstLine == null) {
			throw new IOException("Empty file");
		}
		String[] cols = firstLine.split("\t");
		columnNames = new ArrayList<>();
		for (int i = 0; i < cols.length; i++) {
			columnNames.add(cols[i]);
		}
		checkColumnNames();
	}
	
	public ArrayList<RavenDataRow> readTable() throws IOException {
		ArrayList<RavenDataRow> allData = new ArrayList();
		int iRow = 0;
		while(true) {
			String aLine = fileReader.readLine();
			if (aLine == null) {
				break;
			}
			String[] split = aLine.split("\t");
			RavenDataRow aRow = new RavenDataRow(iRow++, split, mainIndexes);
			allData.add(aRow);
		}
		return allData;
	}

	/**
	 * Check we have the correct standard columns. 
	 */
	private int checkColumnNames() {
		mainIndexes = new int[standardColumns.length];
		columnErrors = 0;
		for (int i = 0; i < standardColumns.length; i++) {
			mainIndexes[i] = columnNames.indexOf(standardColumns[i]);
			if (mainIndexes[i] < 0) {
				System.out.printf("Raven error: Unable to find column \"%s\" in data table\n", standardColumns[i]);
				columnErrors++;
			}
		}
		return columnErrors;
	}

	public boolean exists() {
		return ravenFile.exists();
	}

	/**
	 * get a count of column errors. 
	 * @return the columnErrors
	 */
	protected int getColumnErrors() {
		return columnErrors;
	}

}
