package ravendata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;

public class RavenFileReader {

	private RavenControl ravenControl;
	private File ravenFile;
	private ArrayList<String> columnNames;
	private BufferedReader fileReader;
	private ArrayList<RavenColumnInfo> extraColumns;
	
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
	
	public ArrayList<RavenColumnInfo> getExtraColumns() {
		return extraColumns;
	}

	public int[] getMainIndexes() {
		return mainIndexes;
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
			RavenDataRow aRow = new RavenDataRow(this, iRow++, split);
			allData.add(aRow);
		}
		
		checkExtraColumns(allData);
		
		return allData;
	}

	private void checkExtraColumns(ArrayList<RavenDataRow> allData) {
		if (extraColumns == null) {
			return;
		}
		for (int i = 0; i < extraColumns.size(); i++) {
			RavenColumnInfo col = extraColumns.get(i);
			boolean allNull = isAllNull(col.name, allData);
			if (allNull) {
				continue;
			}
			boolean allInt = isAllInt(col.name, allData);
			if (allInt) {
				col.sqlType = Types.INTEGER;
				continue;
			}
			boolean allDouble = isAllDouble(col.name, allData);
			if (allDouble) {
				col.sqlType = Types.DOUBLE;
				continue;
			}
			// otherwise keep as string
			col.sqlType = Types.CHAR;
		}
		
	}
	
	/**
	 * Is data in every column null ? 
	 * @param colName
	 * @param allData
	 * @return
	 */
	private boolean isAllNull(String colName, ArrayList<RavenDataRow> allData) {
		for (RavenDataRow aRow : allData) {
			String xData = aRow.getExtraData(colName);
			if (xData != null && xData.length() > 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Is data in every column either integer or null ? 
	 * @param colName
	 * @param allData
	 * @return
	 */
	private boolean isAllInt(String colName, ArrayList<RavenDataRow> allData) {
		for (RavenDataRow aRow : allData) {
			String xData = aRow.getExtraData(colName);
			if (xData == null || xData.length() == 0) {
				continue;
			}
			try {
				int val = Integer.valueOf(xData);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Is data in every column either integer or null ? 
	 * @param colName
	 * @param allData
	 * @return
	 */
	private boolean isAllDouble(String colName, ArrayList<RavenDataRow> allData) {
		for (RavenDataRow aRow : allData) {
			String xData = aRow.getExtraData(colName);
			if (xData == null || xData.length() == 0) {
				continue;
			}
			try {
				double val = Double.valueOf(xData);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check we have the correct standard columns. 
	 */
	private int checkColumnNames() {
		mainIndexes = new int[standardColumns.length];
		boolean[] isStandard = new boolean[columnNames.size()];
		columnErrors = 0;
		for (int i = 0; i < standardColumns.length; i++) {
			mainIndexes[i] = columnNames.indexOf(standardColumns[i]);
			if (mainIndexes[i] < 0) {
				System.out.printf("Raven error: Unable to find column \"%s\" in data table\n", standardColumns[i]);
				columnErrors++;
			}
			else {
				isStandard[mainIndexes[i]] = true;
			}
		}
		// now find any extra columns hidden away in there. 
		extraColumns = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			if (!isStandard[i]) {
				extraColumns.add(new RavenColumnInfo(i, columnNames.get(i)));
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
