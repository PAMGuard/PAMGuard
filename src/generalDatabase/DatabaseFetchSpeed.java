package generalDatabase;

/**
 * Run some tests to try to work out why it's so much slower to load 
 * data when the query is based on a non indexed column compared 
 * to an index one, even though the actual query execution time 
 * is a small fraction of the total time. 
 * @author dg50
 *
 */
public class DatabaseFetchSpeed {

	String dbName = "C:\\ProjectData\\meyGenMayData\\Database\\MeygenTritechDetectHDD_03_24.06.2022.sqlite3";
	long[] idRange = {9906111, 9906977};
	long[] uidRange = {8030630, 8031496};
	String[] utcRange = {"2022-05-24 09:27:34", "2022-05-24 09:27:52"};

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public DatabaseFetchSpeed() {
//
//		qStr{1} = sprintf('SELECT Id FROM Gemini_Threshold_Detector_Targets WHERE Id BETWEEN %d and %d', ...
//				idRange(1), idRange(2));
//		qStr{2} = sprintf('SELECT Id FROM Gemini_Threshold_Detector_Targets WHERE UID BETWEEN %d and %d', ...
//				uidRange(1), uidRange(2));
//		qStr{3} = sprintf('SELECT Id FROM Gemini_Threshold_Detector_Targets WHERE UTC BETWEEN ''%s'' and ''%s''', ...
//				utcRange{1}, utcRange{2});

	}
}
