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
import java.io.FileReader;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Single-stage classifier creation.  Dataset must be a WEKA-formatted arff file, with the correct
 * species in the last column.
 * Input and output filenames are hard-coded below and must be changed accordingly.  This is a standalone
 * program that does not run out of Pamguard.
 * 
 * @author Michael Oswald
 */
public class RoccaTrainClassifier {

	
	/**
	 * Standalone implementation
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
    	
    	RoccaTrainClassifier rtc = new RoccaTrainClassifier();
    	File arffFile = rtc.getArff();
    	if (arffFile!=null) {
    		String modelName = rtc.trainClassifier(arffFile);
    	}
    }
    
    
    /**
     * Let user choose arff file training dataset
     * 
     * @return File the arff file containing the training dataset
     */
    public File getArff() {
//        String arffFile = "C:\\Users\\SCANS\\Documents\\Work\\Biowaves\\ONR classifier\\TP_TrainEvtDF_170408";
        
        // let the user select the arff file
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select arff file containing training data");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .arff files", "arff");
        fileChooser.addChoosableFileFilter(restrict);
        File arffFile;

 		int state = fileChooser.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {

		    // load the file
            arffFile = fileChooser.getSelectedFile();
            return arffFile;
            
		} else {
			return null;
		}
    }

    
    /**
     * Actual code to train the classifier
     * 
     */
    public String trainClassifier(File arffFile) {

        RandomForest model = new RandomForest ();
        Instances trainData = null;

        // load the ARFF file containing the training set
        System.out.println("Loading data..." + arffFile.getAbsolutePath());
        try {
            trainData = new Instances
                    (new BufferedReader
                    (new FileReader
//                    ("C:\\Users\\Mike\\Documents\\Work\\Java\\WEKA\\allwhists 12 vars 8sp update 1-28-10.arff")));
//                    ("C:\\Users\\Mike\\Documents\\Work\\Java\\WEKA\\weka vs R\\ETP_orcawale_whists2 modified-subset110perspecies-no_harm_ratios.arff")));
//                      ("C:\\Users\\SCANS\\Documents\\Work\\Biowaves\\ONR classifier\\Atl_TrainDF_Event_160829.arff")));
//                      (arffFile + ".arff")));
                    	(arffFile)));
            trainData.setClassIndex(trainData.numAttributes()-1);
        } catch (Exception ex) {
            System.out.println("Error Loading...");
    		ex.printStackTrace();
    		return null;
        }
        
        // set the classifier parameters
        // see http://weka.sourceforge.net/doc.dev/weka/classifiers/trees/RandomForest.html#setOptions-java.lang.String:A-
        // for list of params and descriptions
        System.out.println("Setting Options...");
        String[] options = new String[6];
        options[0] = "-I";		// number of iterations/trees
        options[1] = "10000";		// = 750
        options[2] = "-K";		// number of attributes (aka mtry)
        options[3] = "5";		// = 3
        options[4] = "-S";		// seed for random number generator
        options[5] = "1";		// = 1

        try {
            model.setOptions(options);
        } catch (Exception ex) {
            System.out.println("Error setting options...");
    		ex.printStackTrace();
    		return null;
        }

        // train the classifier
        System.out.println("Training Classifier...");
        try {
            System.out.println("Starting to Train Classifier..." +
                    new Date());
            model.buildClassifier(trainData);
            System.out.println("Finished Training Classifier..."+
                    new Date());
        } catch (Exception ex) {
            System.out.println("Error training classifier...");
    		ex.printStackTrace();
    		return null;
        }

        // save the classifier
//        String[] curOptions = model.getOptions();
//        Enumeration test = model.listOptions();
        Instances header = new Instances(trainData,0);
        int index = arffFile.getAbsolutePath().lastIndexOf(".");
        String modelName = arffFile.getAbsolutePath().substring(0,index) + ".model";
        System.out.println("Saving Classifier..." + modelName);
        try {
            SerializationHelper.writeAll
//                ("C:\\Users\\Mike\\Documents\\Work\\Java\\WEKA\\weka vs R\\RF_8sp_54att_110whistle-subset.model",
//                  (arffFile + ".model",
            	(modelName,
                new Object[]{model,header});
            System.out.println("Finished!");
            return modelName;
        } catch (Exception ex) {
            System.out.println("Error saving classifier...");
    		ex.printStackTrace();
        }
        return null;
    }
}
