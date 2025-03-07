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



/**
 * Short script to load the data from an existing event (school) stats file, classify each row and save
 * the data into a new summary file.  This program requires a valid RoccaProcess in order to run, and
 * can therefore not be run as a standalone object.  A Reclassify button is included in the Rocca Parameters
 * GUI, but disabled by default.  In order to run this program, change the hard-coded filenames as
 * required, enable the reclassifyButton in RoccaParametersDialog, and run Pamguard.
 * 
 * NOTE - THIS VERSION IS SPECIFIC TO EVENTS, NOT WHISTLES OR CLICKS.  CHECK TO SEE WHETHER THE RECLASSIFY BUTTON RUNS
 * THIS, RoccaClassifyThis OR RoccaClassifyThisClick.
 * 
 * The event stats file must have the following text for the headers:
 * Min_Time_Between_Whistle_Detections_s	
 * Min_Time_Between_Click_Detections_s	
 * Max_Time_Between_Whistle_Detections_s	
 * Max_Time_Between_Click_Detections_s	
 * Ave_Time_Between_Whistle_Detections_s	
 * Whistle_Detections_per_Second	
 * Whistle_Density	
 * Ave_Whistle_Overlap	
 * Ave_Time_Between_Click_Detections_s	
 * Click_Detections_per_Second	
 * Click_Density	
 * Ave_Click_Overlap	
 * NumWhistle-NumClick_Ratio	
 * Dd_votes	
 * Gg_votes	
 * Gm_votes	
 * Sa_votes	
 * Sb_votes	
 * Sc_votes	
 * Scl_votes	
 * Sf_votes	
 * Tt_votes
 * 
 * Any remaining columns are ignored.	Column headers need to be spelled/capitalized the same,
 * but can be in a different order.
 * 
 * Note that the last 9 columns, for the species votes, can be different based on the classifier used.  But
 * all the rest of the columns are straight from the RoccaSidePanel.createDetectionStatsHeader method and
 * must match that (because the classifier should have used those column headers as well)
 * 								


 * @author Mike
 *
 */
public class RoccaClassifyThisEvent {
	
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
	public RoccaClassifyThisEvent(RoccaProcess roccaProcess) {
		
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
		this.csvIn = new String("Atl_TestDF_Event_160829-noLatLong.csv");
		statsFileIn = new File(dirIn, csvIn);
		this.csvOut = new String("Atl_TestDF_Event_160829-noLatLong-classified.csv");
		statsFileOut = new File(dirIn, csvOut);
		
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
		
		// The first line is the header; read it and save it.  Start processing
		// after reading in the second line
		String headerRow = null;
		String dataRow = null;
		int lineNum = 1;
		try {
			System.out.println("Reading Line "+String.valueOf(lineNum)+"...");
			headerRow = inputFile.readLine();
			dataRow = inputFile.readLine();
		} catch (IOException e) {
			System.out.println("ERROR: Cannot read first 2 lines from "+statsFileIn);
			e.printStackTrace();
		}
		
		// Once the data is null, it means we've hit the end of the file
		while (dataRow != null){

			// call the classifier and save the species name
			String sp = roccaProcess.getRoccaClassifier().classifySighting(headerRow,dataRow);
						
			// generate the output line
			String eventStats = sp + "," + dataRow;
			
			// write the output to the file
			try {
	            // write a header line first if this is a new file
	            if (!statsFileOut.exists()) {
	    			System.out.println("Creating output file "+statsFileOut);
	                BufferedWriter writer = new BufferedWriter(new FileWriter(statsFileOut));
	                String hdr = "Class_sp,"+headerRow;
	                writer.write(hdr);
	                writer.newLine();
	                writer.close();
	            }
				BufferedWriter writer = new BufferedWriter(new FileWriter(statsFileOut, true));
				// print line every 500
				if (lineNum % 500 == 0) {
					System.out.println("   Writing Line "+String.valueOf(lineNum)+"...");
				}
				writer.write(eventStats);
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
