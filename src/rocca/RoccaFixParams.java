/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package rocca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;



/**
 * Short script to load an existing contour file (Rocca output), recalculate the parameters and save
 * the data into a new summary file.
 * 
 * The contour file must have the following format:
 * 		column A: Time [ms]
 * 		column B: Peak Frequency [Hz]
 * 		column C: Duty Cycle
 * 		column D: Energy
 * 		column E: WindowRMS
 * 
 * 
 * 
 * 

 * @author Mike
 *
 */
public class RoccaFixParams {

	private RoccaContourDataBlock rcdb;
	
	private File dir;
	
	private String csvIn;
	
	private int numFiles=0;
	
	private File[] listOfFiles;
	
	
	public RoccaFixParams(RoccaProcess roccaProcess) {
		
		/* initialize the BufferedReader */
		BufferedReader currentFile = null;
		
		/* initialize the RoccaContourDataUnit */
		RoccaContourDataUnit rcdu = null;

		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select folder containing csv time-freq files...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int state = fileChooser.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {
            this.dir = fileChooser.getSelectedFile();
		} else {
			System.out.println("Cannot find folder");
			return;
		}

		
		/* set the directory */
//		this.dir = new File("C:\\Users\\Mike\\Documents\\Work\\Tom\\Hawaii dataset problems\\need new params");
		
		/* get a list of csv files in that directory */
//		this.csvIn = new String("Detection1-D08_20110928_231007_W02-Channel0-20110928_231008.csv");
//		this.numFiles = 1;
//		listOfFiles = new File[numFiles];
//		listOfFiles[0] = new File(dirIn, csvIn);
		
		listOfFiles = this.dir.listFiles(new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".csv");
		    }
		});
		numFiles = listOfFiles.length;
		
		
		/* loop through the files one at a time */
		for (int i=0; i<numFiles; i++) {
			
			/* initialize the RoccaContourDataBlock */
			rcdb = new RoccaContourDataBlock(roccaProcess,0);
			
			/* load the file */
			try {
				currentFile = new BufferedReader(new FileReader(listOfFiles[i]));
			} catch (FileNotFoundException e) {
				System.out.println("Cannot load file "+listOfFiles[i]);
				e.printStackTrace();
				return;
			}
			System.out.println("Recalculating "+listOfFiles[i]);
			
			/* The first line is the header; read it and ignore it.  Start processing
			 * after reading in the second line
			 */
			String dataRow = null;
			try {
//				dataRow = currentFile.readLine(); 2017/01/19 modification - files do not have a header; 2019/03/14 mod - files do not have a header
				dataRow = currentFile.readLine();
			} catch (IOException e) {
				System.out.println("Cannot read first 2 lines from "+listOfFiles[i]);
				e.printStackTrace();
				try {
					currentFile.close();
				} catch (IOException e1) {
					System.out.println("Cannot close "+listOfFiles[i]);
					e1.printStackTrace();
				}
				return;
			}
			
			long dummyTime = 0;	// 2019/03/14 modification
			
			/* Once the data is null, it means we've hit the end of the file */
			while (dataRow != null){

				/* split the row up */
				String[] dataArray = dataRow.split(",");

				/* load the parameters into the RoccaContourDataUnit, and add it
				 * to the RoccaContourDataBlock
				 */
				rcdu = new RoccaContourDataUnit(0,0,0,0);
//				rcdu.setTimeMilliseconds(Long.parseLong(dataArray[0]));
//				rcdu.setTime(Long.parseLong(dataArray[0]));
//				rcdu.setPeakFreq(Double.parseDouble(dataArray[1]));
//				rcdu.setDutyCycle(Double.parseDouble(dataArray[2]));
//				rcdu.setEnergy(Double.parseDouble(dataArray[3]));
//				rcdu.setWindowRMS(Double.parseDouble(dataArray[4]));
				
				// Special modification 2019/03/14 - no header, and only
				// a frequency column.  Each frequency point is 10ms
				// after the last, so add in arbitrary times
				rcdu.setPeakFreq(Double.parseDouble(dataArray[0]));
				rcdu.setTime(dummyTime);
				rcdu.setDutyCycle(0.);
				rcdu.setEnergy(0.);
				rcdu.setWindowRMS(0.);
				dummyTime+=10;
				
				// Special modification 2017/01/19 - load time-freq
				// contours created in Ishmael.  Only have time
				// and frequency columns, so set the rest to 0.  Also,
				// time column is in seconds not milliseconds so convert
				// first and drop decimals
//				rcdu.setPeakFreq(Double.parseDouble(dataArray[1]));
//				double timeInMillis = Double.parseDouble(dataArray[0]) * 1000.;
//				rcdu.setTimeMilliseconds(Math.round(timeInMillis));
//				rcdu.setTime(Math.round(timeInMillis));
//				rcdu.setDutyCycle(0.);
//				rcdu.setEnergy(0.);
//				rcdu.setWindowRMS(0.);
				
				// Special modification 2014/06/18 - bin files were converted to
				// wav files using 40kHz sampling rate instead of 80kHz.  Therefore,
				// all the original frequency measurements are half of what they should
				// be while the duration are double.  To fix this, we need to double the
				// frequencies and halve the times before creating the rcdu object
//				rcdu.setTimeMilliseconds(Long.parseLong(dataArray[0])/2);
//				rcdu.setTime(Long.parseLong(dataArray[0])/2);
//				rcdu.setPeakFreq(Double.parseDouble(dataArray[1])*2);
//				rcdu.setDutyCycle(Double.parseDouble(dataArray[2]));
//				rcdu.setEnergy(Double.parseDouble(dataArray[3]));
//				rcdu.setWindowRMS(Double.parseDouble(dataArray[4]));
				
				// add data unit to the data block
				rcdb.addPamData(rcdu);


				/* read the next line of data */
				try {
					dataRow = currentFile.readLine();
				} catch (IOException e) {
					System.out.println("Cannot read line from "+listOfFiles[i]);
					e.printStackTrace();
					try {
						currentFile.close();
					} catch (IOException e1) {
						System.out.println("Cannot close "+listOfFiles[i]);
						e1.printStackTrace();
					}
					return;
				}
			} // parse the next line
			
			/* close the file */
			try {
				currentFile.close();
			} catch (IOException e) {
				System.out.println("Cannot close "+listOfFiles[i]);
				e.printStackTrace();
				return;
			}  
		
			/* calculate parameters */
			rcdb.setAsAWhistle(true);
			rcdb.calculateStatistics();
	        roccaProcess.roccaClassifier.classifyContour2(rcdb);
			
			/* save the output */
	        roccaProcess.saveContourStats(rcdb, 0, i, "xxx",listOfFiles[i].getName());

		
		} // read the next file

		System.out.println("Stats Calculated");
		
		
	} // end of constructor
	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		// create a Fixer
//		new RoccaFixParams();
//	}
}
