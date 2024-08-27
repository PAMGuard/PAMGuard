package AIS;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import NMEA.NMEADataUnit;

/**
 * Read in some AIS data which caused AIS to crash on certain strings. 
 * @author Doug
 *
 */
public class AISBugSearch {

	public void runTestData(AISControl aisControl) {
		String fn = "C:\\PamguardTest\\AISErrors\\NMEA_20150828.asc";
		FileReader fr = null;
		try {
			fr = new FileReader(fn);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		}

		BufferedReader reader = new BufferedReader(fr);

		String line;
		try {
			while((line = reader.readLine())!=null){
				// first comma separates date and NMEA data
				int chPos = line.indexOf(",");
				String nmeaData = line.substring(chPos+1);
				if (!nmeaData.contains("AIVDM")) {
					continue;
				}
				System.out.println(nmeaData);
				StringBuffer sb = new StringBuffer(nmeaData);
				NMEADataUnit nmeaDataUnit = new NMEADataUnit(System.currentTimeMillis(), sb);
				aisControl.aisProcess.newData(null, nmeaDataUnit);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
