package d3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import d3.calibration.CalibrationInfo;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

/**
 * Class to hold various types of information about a sensor. 
 * @author Doug Gillespie
 *
 */
public class D3SensorInfo {

	private int sensorId;
	
	private int[] listIndexes = null;
	
	private String[] sensorProperties;

	private CalibrationInfo calInfo;
	
	/**
	 * Some static stuff that's used by all instances of this
	 * to store the main database of sensor information. 
	 */
	private static List<String[]> sensorDefs;
	
	private static int[] numberList;
	
	private static final int numberInd=0, nameColInd=1, calColInd=2, qualifier1ColInd=3, 
			qualifier2ColInd=4, descriptionColInd=5, commentColInd=6;
	
	private static String[] colNames = {"number", "name", "cal", "qualifier1", "qualifier2", "description", "comment"};
	
	private static int[] colIndexes = new int[colNames.length];

	public D3SensorInfo(int sensorId, int[] fullList) {
		super();
		this.setSensorId(sensorId);
		findUniqueIndexes(fullList);
		
		if (sensorDefs == null) {
			sensorDefs = readSensorCSVFile();
		}
		String[] sensoInfos = findCSVRow(sensorId);
		if (sensoInfos != null) {
			sensorProperties = new String[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				int trueInd = colIndexes[i];
				if (trueInd >= 0 && trueInd < sensoInfos.length) {
					sensorProperties[i] = sensoInfos[trueInd]; 
				}
			}
		}
	}
	
	@Override
	public String toString() {
		String str = "";
		if (sensorProperties == null) {
			return super.toString();
		}
		for (int i = 0; i < colNames.length; i++) {
			str += colNames[i];
			int colPos = colIndexes[i];
			if (colPos < 0) {
				str += ": ??";
			}
			else {
				str += ": " + sensorProperties[colPos];
			}
			if (i < colNames.length-1) {
				str += "; ";
			}
		}
		return str;
	}

	/**
	 * From a list of all sensor channels, extract a list 
	 * of the channels which we're interested in for a specific sensor 
	 * (A single sensor may appear many times in the list)
	 * @param sensorChans
	 * @param selChan
	 * @return
	 */
	private void findUniqueIndexes(int[] fullList) {
		int[] indexes = new int[fullList.length];
		int iInd = 0;
		for (int i = 0; i < fullList.length; i++) {
			if (fullList[i] == getSensorId()) {
				indexes[iInd] = i;
				iInd++;
			}
		}
		listIndexes = Arrays.copyOf(indexes, iInd);
	}
	

	private List<String[]> readSensorCSVFile() {
		// try to open the sensor csv file from the resource folder

		String filename = "Resources/d3sensordefs.csv";
		/*
		 * Use classloader to get csv out of Jar file. 
		 */
		URL url = ClassLoader.getSystemResource(filename);
		if (url == null) {
			System.out.println("Can't load D� Sensor CSV file " + filename);
			return null;
		}
		//				url.getFile();

		CSVReader reader;
		List<String[]> readList;
		try {
			reader = new CSVReader(new FileReader(url.getFile()));
		} catch (FileNotFoundException e) {
			System.out.println("unable to open csv file " + filename);
			return null;
		}

		try {
			readList = reader.readAll();
		} catch (CsvException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		if (readList.size() > 0) {
			for (int i = 0; i < colNames.length; i++) {
				colIndexes[i] = findCSVColumn(readList.get(0), colNames[i]);
			}
		}
		
		numberList = new int[readList.size()];
		int iRow = 0;
		for (String[] aRow:readList) {
			try {
				numberList[iRow] = Integer.valueOf(aRow[colIndexes[numberInd]].trim());
			}
			catch (NumberFormatException e) {
				numberList[iRow] = -1;
			}
			iRow ++;
		}
		
		return readList;
	}
	
	private String[] findCSVRow(int sensorId) {
		if (numberList == null) {
			return null;
		}
		for (int i = 0; i < numberList.length; i++) {
			if (numberList[i] == sensorId) {
				return sensorDefs.get(i);
			}
		}
		return null;
	}
	
	private int findCSVColumn(String[] firstLine, String colName) {
		colName = colName.trim();
		for (int i = 0; i < firstLine.length; i++) {
			if (firstLine[i].trim().equals(colName)) {
				return i;
			}
		}
		return -1;
	}
	
//	private String[] findCSVRow()

	/**
	 * @return the listIndexes
	 */
	public int[] getListIndexes() {
		return listIndexes;
	}

	/**
	 * @return the sensorId
	 */
	public int getSensorId() {
		return sensorId;
	}

	/**
	 * @param sensorId the sensorId to set
	 */
	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return sensorProperties[nameColInd];
	}

	/**
	 * @return the cal
	 */
	public String getCal() {
		return sensorProperties[calColInd];
	}

	/**
	 * @return the qualifier1
	 */
	public String getQualifier1() {
		return sensorProperties[qualifier1ColInd];
	}

	/**
	 * @return the qualifier2
	 */
	public String getQualifier2() {
		return sensorProperties[qualifier2ColInd];
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return sensorProperties[descriptionColInd];
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return sensorProperties[commentColInd];
	}

	/**
	 * @return the calInfo
	 */
	protected CalibrationInfo getCalInfo() {
		return calInfo;
	}

	/**
	 * @param calInfo the calInfo to set
	 */
	protected void setCalInfo(CalibrationInfo calInfo) {
		this.calInfo = calInfo;
	}

}
