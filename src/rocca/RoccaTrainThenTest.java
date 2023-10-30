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
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RoccaTrainThenTest {
	
	RoccaTrainClassifier roccaTrainClassifier;
	
	RoccaClassifyThis roccaClassifyThis;
	
	
	/**
	 * Main Constructor
	 * @param roccaProcess
	 */
	public RoccaTrainThenTest(RoccaProcess roccaProcess) {
		
		
        // let the user select the csv file containing the training and testing dataset(s)
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select csv file with the training/testing pairs");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .csv files", "csv");
        fileChooser.addChoosableFileFilter(restrict);

		int state = fileChooser.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {

		    // load the file
			try {
				File csvDataPairs = fileChooser.getSelectedFile();
				BufferedReader br = new BufferedReader(new FileReader(csvDataPairs));
		        String curPath = csvDataPairs.getParent();
				
				// main loop
				// read through the csv file one line at a time.  The first column should contain the training dataset filename,
				// and the second column the testing dataset filename.  Paths should be relative to the path containing
				// the csv file
				String line = "";  
				String splitBy = ",";  
				while ((line=br.readLine())!=null) {
					
					String[] filenames = line.split(splitBy);
					
					// train the classifier
					File arffFile = new File(curPath + File.separator + filenames[0]);
					roccaTrainClassifier = new RoccaTrainClassifier();
					String modelName = roccaTrainClassifier.trainClassifier(arffFile);
					if (modelName == null) {
						System.out.println("ERROR: could not create classifier model from "+arffFile);
						continue;
					}
										
					// set the classifier as the current one in RoccaParameters
					roccaProcess.roccaControl.roccaParameters.setRoccaClassifierModelFilename(new File(modelName));
					
					
					// test the classifier with the testing dataset
					File testFile = new File(curPath + File.separator + filenames[1]);
					roccaClassifyThis = new RoccaClassifyThis();
					roccaClassifyThis.runTheClassifier(testFile, roccaProcess);
					
				}
				
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		} else {
			return;
		}
	}

}
