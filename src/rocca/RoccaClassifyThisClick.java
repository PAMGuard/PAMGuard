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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;

import PamUtils.PamCalendar;



/**
 * Short script to load the data from an existing parameter stats file, classify each row and save
 * the data into a new summary file.  This program requires a valid RoccaProcess in order to run, and
 * can therefore not be run as a standalone object.  A Reclassify button is included in the Rocca Parameters
 * GUI, but disabled by default.  In order to run this program, change the hard-coded filenames as
 * required, enable the reclassifyButton in RoccaParametersDialog, and run Pamguard.
 * 
 * NOTE - THIS VERSION IS SPECIFIC TO CLICKS, NOT WHISTLES.  CHECK TO SEE WHETHER THE RECLASSIFY BUTTON RUNS
 * THIS OR RoccaClassifyThis.
 * 
 * The parameter stats file must have the following format:
 * 		column A: Source
 * 		column B: ClassifiedSpecies	(ignored)
 * 		column C: DURATION	 
 * 		column D: FREQCENTER	 
 * 		column E: FREQPEAK
 * 		column F: BW3DB	 
 * 		column G: BW3DBLOW	 
 * 		column H: BW3DBHIGH
 * 		column I: BW10DB	 
 * 		column J: BW10DBLOW	 
 * 		column K: BW10DBHIGH	 
 * 		column L: NCROSSINGS	 
 * 		column M: SWEEPRATE	 
 * 		column N: MEANTIMEZC	 
 * 		column O: MEDIANTIMEZC	 
 * 		column P: VARIANCETIMEZC	 
 * 
 * Any remaining columns are ignored.	Column headers do not need to be spelled/capitalized the same,
 * but must be in this order.
 * 								


 * @author Mike
 *
 */
public class RoccaClassifyThisClick {
	
	/** the RoccaContourDataBlock where the stats will be stored */
	private RoccaContourDataBlock rcdb;
	
	/** dummy RoccaContourDataUnit to hold the time */
	private RoccaContourDataUnit rcdu;
	
    /** the field in the RoccaContourStats object which contains all the stats measures */
    private EnumMap<RoccaContourStats.ParamIndx, Double> contourStats;

	private String dirIn;

	/** the input filename */
	private String csvIn;
	
	/** the input file */
	private File statsFileIn;
	
	/** the output filename */
	private String csvOut;

	/** the output file */
	private File statsFileOut;
	
	/** Constructor */	
	public RoccaClassifyThisClick(RoccaProcess roccaProcess) {
		
		// initialize the BufferedReader
		BufferedReader inputFile = null;
		
		// set the directory
//		this.dirIn = new String("C:\\Users\\Mike\\Documents\\Work\\Java\\EclipseWorkspace\\testing\\RoccaClassifyThis_testing");
//		this.dirIn = new String("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\manual 2-stage data");
//		this.dirIn = new String("C:\\Users\\Mike\\Documents\\Work\\Tom\\Hawaii dataset problems");
		this.dirIn = new String("C:\\Users\\SCANS\\Documents\\Work\\Biowaves\\ONR classifier");
		
		// Define the input and output filenames
		// Hard-coded for now.  To Do: query the user for the filename
//		this.csvIn = new String("JAX_Deployment1_ROCCAcontourstatscombined_Allsites_MASTER-corrected.csv");
//		this.csvIn = new String("Manual_5sp_April 9 2013.csv");
//		this.csvIn = new String("CombinedContourStats-fixed.csv");
//		this.csvOut = new String("RoccaContourStatsReclassified.csv");
		this.csvIn = new String("Atl_TestDFNoTrain_Call_C_160831b.csv");
		statsFileIn = new File(dirIn, csvIn);
		this.csvOut = new String("Atl_TestDFNoTrain_Call_C_160829-classifiedb.csv");
		statsFileOut = new File(dirIn, csvOut);

		
//		JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Select spreadsheet to recalculate...");
//        fileChooser.setFileHidingEnabled(true);
//        fileChooser.setApproveButtonText("Select");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//
//		int state = fileChooser.showOpenDialog(this.dirIn);
//		if (state == JFileChooser.APPROVE_OPTION) {
		
		
		
		
		
		
		
		
		
		
		// load the classifier
		System.out.println("Loading classifier...");
        roccaProcess.setClassifierLoaded
        (roccaProcess.getRoccaClassifier().setUpClassifier());
			
		// open the input file
		try {
			System.out.println("Opening input file "+statsFileIn);
			inputFile = new BufferedReader(new FileReader(statsFileIn));
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Cannot load file "+statsFileIn);
			e.printStackTrace();
			return;
		}
		
		// The first line is the header; read it and ignore it.  Start processing
		// after reading in the second line
		String dataRow = null;
		int lineNum = 1;
		try {
			System.out.println("Reading Line "+String.valueOf(lineNum)+"...");
			dataRow = inputFile.readLine();
			dataRow = inputFile.readLine();
		} catch (IOException e) {
			System.out.println("ERROR: Cannot read first 2 lines from "+statsFileIn);
			e.printStackTrace();
		}
		
		// Once the data is null, it means we've hit the end of the file
		while (dataRow != null){

			// split the row up
			String[] dataArray = dataRow.split(",");

			// create a new RoccaContourDataBlock and get a reference to the contour stats field
			rcdb = new RoccaContourDataBlock(roccaProcess,0);	
			rcdb.setStatsRun(true);
			rcdu = new RoccaContourDataUnit(PamCalendar.getTimeInMillis(),0,0,0);
			rcdu.setTime(PamCalendar.getTimeInMillis());
			rcdb.addPamData(rcdu);
			contourStats = rcdb.getContour();
			
			// specify that this is not a whistle
			rcdb.setAsAWhistle(false);

			// load the parameters into the RoccaContourDataBlock
	        contourStats.put(RoccaContourStats.ParamIndx.DURATION, Double.parseDouble(dataArray[2]));
	        contourStats.put(RoccaContourStats.ParamIndx.FREQCENTER, Double.parseDouble(dataArray[3]));
	        contourStats.put(RoccaContourStats.ParamIndx.FREQPEAK, Double.parseDouble(dataArray[4]));
	        contourStats.put(RoccaContourStats.ParamIndx.BW3DB, Double.parseDouble(dataArray[5]));
	    	contourStats.put(RoccaContourStats.ParamIndx.BW3DBLOW, Double.parseDouble(dataArray[6]));
			contourStats.put(RoccaContourStats.ParamIndx.BW3DBHIGH, Double.parseDouble(dataArray[7]));
			contourStats.put(RoccaContourStats.ParamIndx.BW10DB, Double.parseDouble(dataArray[8]));
			contourStats.put(RoccaContourStats.ParamIndx.BW10DBLOW, Double.parseDouble(dataArray[9]));
			contourStats.put(RoccaContourStats.ParamIndx.BW10DBHIGH, Double.parseDouble(dataArray[10]));
			contourStats.put(RoccaContourStats.ParamIndx.NCROSSINGS, Double.parseDouble(dataArray[11]));
			contourStats.put(RoccaContourStats.ParamIndx.SWEEPRATE, Double.parseDouble(dataArray[12]));
			contourStats.put(RoccaContourStats.ParamIndx.MEANTIMEZC, Double.parseDouble(dataArray[13]));
			contourStats.put(RoccaContourStats.ParamIndx.MEDIANTIMEZC, Double.parseDouble(dataArray[14]));
			contourStats.put(RoccaContourStats.ParamIndx.VARIANCETIMEZC, Double.parseDouble(dataArray[15]));
			
			// Run the classifier
	        roccaProcess.getRoccaClassifier().classifyContour2(rcdb);
			
			// generate the output line
			String contourStats =
					rcdb.createContourStatsString(dataArray[0],0,dataArray[1]);
			
			// write the output to the file
			try {
	            // write a header line first if this is a new file
	            if (!statsFileOut.exists()) {
	    			System.out.println("Creating output file "+statsFileOut);
	                BufferedWriter writer = new BufferedWriter(new FileWriter(statsFileOut));
	                String hdr = rcdb.createContourStatsHeader();
	                writer.write(hdr);
	                writer.newLine();
	                writer.close();
	            }
				BufferedWriter writer = new BufferedWriter(new FileWriter(statsFileOut, true));
				// print line every 500
				if (lineNum % 500 == 0) {
					System.out.println("   Writing Line "+String.valueOf(lineNum)+"...");
				}
				writer.write(contourStats);
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				System.out.println("Cannot write line to "+statsFileOut);
				e.printStackTrace();
			}

            // read the next line of data
			lineNum++;
			try {
				// print line every 500
				if (lineNum % 500 == 0) {
					System.out.println("Reading Line "+String.valueOf(lineNum)+"...");
				}
				dataRow = inputFile.readLine();
			} catch (IOException e) {
				System.out.println("ERROR: Cannot read line from "+statsFileIn);
				e.printStackTrace();
			}
		} // parse the next line
		
		// close the input file
		try {
			inputFile.close();
		} catch (IOException e) {
			System.out.println("Cannot close "+statsFileIn);
			e.printStackTrace();
			return;
		}  
		System.out.println("Finished reclassifying file");
	} // end of constructor
}
