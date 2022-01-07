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

import javax.swing.JFileChooser;

import PamUtils.PamCalendar;



/**
 * Short script to load the data from an existing contour stats file (Rocca output), classify each row and save
 * the data into a new summary file.  This program requires a valid RoccaProcess in order to run, and
 * can therefore not be run as a standalone object.  A Reclassify button is included in the Rocca Parameters
 * GUI, but disabled by default.  In order to run this program, change the hard-coded filenames as
 * required, enable the reclassifyButton in RoccaParametersDialog, and run Pamguard.
 * 
 * The contour stats file must have the following format:
 * 		column A: Source
 * 		column B: Date-Time	 
 * 		column C: DetectionCount	 
 * 		column D: DetectionNumber	 
 * 		column E: ClassifiedSpecies	(ignored)
 * 		column F: FREQMAX	 
 * 		column G: FREQMIN	 
 * 		column H: DURATION	 
 * 		column I: FREQBEG	 
 * 		column J: FREQEND	 
 * 		column K: FREQRANGE	 
 * 		column L: DCMEAN	 
 * 		column M: DCSTDDEV	 
 * 		column N: FREQMEAN	 
 * 		column O: FREQSTDDEV	 
 * 		column P: FREQMEDIAN	 
 * 		column Q: FREQCENTER	 
 * 		column R: FREQRELBW	 
 * 		column S: FREQMAXMINRATIO	 
 * 		column T: FREQBEGENDRATIO	 
 * 		column U: FREQQUARTER1	 
 * 		column V: FREQQUARTER2	 
 * 		column W: FREQQUARTER3	 
 * 		column X: FREQSPREAD	 
 * 		column Y: DCQUARTER1MEAN	 
 * 		column Z: DCQUARTER2MEAN	 
 * 		column AA: DCQUARTER3MEAN	 
 * 		column AB: DCQUARTER4MEAN	 
 * 		column AC: FREQCOFM	 
 * 		column AD: FREQSTEPUP	 
 * 		column AE: FREQSTEPDOWN	 
 * 		column AF: FREQNUMSTEPS	 
 * 		column AG: FREQSLOPEMEAN	 
 * 		column AH: FREQABSSLOPEMEAN	 
 * 		column AI: FREQPOSSLOPEMEAN	 
 * 		column AJ: FREQNEGSLOPEMEAN	 
 * 		column AK: FREQSLOPERATIO	 
 * 		column AL: FREQBEGSWEEP	 
 * 		column AM: FREQBEGUP	 
 * 		column AN: FREQBEGDWN	 
 * 		column AO: FREQENDSWEEP	 
 * 		column AP: FREQENDUP	 
 * 		column AQ: FREQENDDWN	 
 * 		column AR: NUMSWEEPSUPDWN	 
 * 		column AS: NUMSWEEPSDWNUP	 
 * 		column AT: NUMSWEEPSUPFLAT	 
 * 		column AU: NUMSWEEPSDWNFLAT	 
 * 		column AV: NUMSWEEPSFLATUP	 
 * 		column AW: NUMSWEEPSFLATDWN	 
 * 		column AX: FREQSWEEPUPPERCENT	 
 * 		column AY: FREQSWEEPDWNPERCENT	 
 * 		column AZ: FREQSWEEPFLATPERCENT	 
 * 		column BA: NUMINFLECTIONS	 
 * 		column BB: INFLMAXDELTA	 
 * 		column BC: INFLMINDELTA	 
 * 		column BD: INFLMAXMINDELTA	 
 * 		column BE: INFLMEANDELTA	 
 * 		column BF: INFLSTDDEVDELTA	 
 * 		column BG: INFLMEDIANDELTA
 * 		column BH: INFLDUR
 * 		column BI: STEPDUR
 * 
 * Any remaining columns are ignored.	Column headers do not need to be spelled/capitalized the same,
 * but must be in this order.
 * 								


 * @author Mike
 *
 */
public class RoccaClassifyThis {
	
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
	public RoccaClassifyThis(RoccaProcess roccaProcess) {
		
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
		this.csvIn = new String("Atl_TestDFNoTrain_Call_W_160831.csv");
		statsFileIn = new File(dirIn, csvIn);
		this.csvOut = new String("Atl_TestDFNoTrain_Call_W_160829-classified.csv");
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
        (roccaProcess.roccaClassifier.setUpClassifier());
			
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
			
			// specify whether or not this is a whistle
			rcdb.setAsAWhistle(true);

			// load the parameters into the RoccaContourDataBlock
			// NOTE: for the ONR Classifier datasets, DC params were not available.  Extra columns were added to
			// the dataset csv file to account for these missing params and keep the remaining columns matching the list above, and
			// the DC params were commented out below.  This is also true for FREQBEGUP, FREQBEGDWN, FREQENDUP and FREQENDDWN.
	        contourStats.put(RoccaContourStats.ParamIndx.FREQMAX, Double.parseDouble(dataArray[5]));
	        contourStats.put(RoccaContourStats.ParamIndx.FREQMIN, Double.parseDouble(dataArray[6]));
	        contourStats.put(RoccaContourStats.ParamIndx.DURATION, Double.parseDouble(dataArray[7]));
	        contourStats.put(RoccaContourStats.ParamIndx.FREQBEG, Double.parseDouble(dataArray[8]));
	    	contourStats.put(RoccaContourStats.ParamIndx.FREQEND, Double.parseDouble(dataArray[9]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQRANGE, Double.parseDouble(dataArray[10]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCMEAN, Double.parseDouble(dataArray[11]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCSTDDEV, Double.parseDouble(dataArray[12]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQMEAN, Double.parseDouble(dataArray[13]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSTDDEV, Double.parseDouble(dataArray[14]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQMEDIAN, Double.parseDouble(dataArray[15]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQCENTER, Double.parseDouble(dataArray[16]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQRELBW, Double.parseDouble(dataArray[17]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQMAXMINRATIO, Double.parseDouble(dataArray[18]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQBEGENDRATIO, Double.parseDouble(dataArray[19]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER1, Double.parseDouble(dataArray[20]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER2, Double.parseDouble(dataArray[21]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER3, Double.parseDouble(dataArray[22]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSPREAD, Double.parseDouble(dataArray[23]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER1MEAN, Double.parseDouble(dataArray[24]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER2MEAN, Double.parseDouble(dataArray[25]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER3MEAN, Double.parseDouble(dataArray[26]));
			//contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER4MEAN, Double.parseDouble(dataArray[27]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQCOFM, Double.parseDouble(dataArray[28]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSTEPUP, Double.parseDouble(dataArray[29]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSTEPDOWN, Double.parseDouble(dataArray[30]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQNUMSTEPS, Double.parseDouble(dataArray[31]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSLOPEMEAN, Double.parseDouble(dataArray[32]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN, Double.parseDouble(dataArray[33]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN, Double.parseDouble(dataArray[34]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN, Double.parseDouble(dataArray[35]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSLOPERATIO, Double.parseDouble(dataArray[36]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQBEGSWEEP, Double.parseDouble(dataArray[37]));
			//contourStats.put(RoccaContourStats.ParamIndx.FREQBEGUP, Double.parseDouble(dataArray[38]));
			//contourStats.put(RoccaContourStats.ParamIndx.FREQBEGDWN, Double.parseDouble(dataArray[39]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQENDSWEEP, Double.parseDouble(dataArray[40]));
			//contourStats.put(RoccaContourStats.ParamIndx.FREQENDUP, Double.parseDouble(dataArray[41]));
			//contourStats.put(RoccaContourStats.ParamIndx.FREQENDDWN, Double.parseDouble(dataArray[42]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN, Double.parseDouble(dataArray[43]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP, Double.parseDouble(dataArray[44]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT, Double.parseDouble(dataArray[45]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT, Double.parseDouble(dataArray[46]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP, Double.parseDouble(dataArray[47]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN, Double.parseDouble(dataArray[48]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, Double.parseDouble(dataArray[49]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT, Double.parseDouble(dataArray[50]));
			contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPFLATPERCENT, Double.parseDouble(dataArray[51]));
			contourStats.put(RoccaContourStats.ParamIndx.NUMINFLECTIONS, Double.parseDouble(dataArray[52]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLMAXDELTA, Double.parseDouble(dataArray[53]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLMINDELTA, Double.parseDouble(dataArray[54]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLMAXMINDELTA, Double.parseDouble(dataArray[55]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLMEANDELTA, Double.parseDouble(dataArray[56]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLSTDDEVDELTA, Double.parseDouble(dataArray[57]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLMEDIANDELTA, Double.parseDouble(dataArray[58]));
			contourStats.put(RoccaContourStats.ParamIndx.INFLDUR, Double.parseDouble(dataArray[59]));
			contourStats.put(RoccaContourStats.ParamIndx.STEPDUR, Double.parseDouble(dataArray[60]));	
			
			// Run the classifier
	        roccaProcess.roccaClassifier.classifyContour2(rcdb);
			
			// generate the output line
			String contourStats =
					rcdb.createContourStatsString(dataArray[0],Integer.parseInt(dataArray[2]),dataArray[3]);
			
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
