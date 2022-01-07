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
import java.io.FileReader;
import java.util.Date;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 *
 * @author Michael Oswald
 */
public class RoccaTrainClassifier3 {

    public static void main(String[] args) {
    	
    	/* 2013-09-03 Atlantic Classifier Auto dataset 
    	 * Create 2-stage model with 2 different classifiers:
    	 * Stage 1 = Gm vs. Other
    	 * Stage 2 = Dd vs. Sc vs. Sf vs. Tt
    	 */
    	
    	// Stage 2
        RandomForest model2 = new RandomForest ();
        Instances trainData2 = null;
        RoccaRFModel[] modelList2 = new RoccaRFModel[4];

        // load the ARFF file containing the training set
        System.out.println("Loading Stage 2 data...");
        try {
            trainData2 = new Instances
                    (new BufferedReader
                    (new FileReader
                      ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\auto 2-stage data\\auto_stage2_Dd-Tt-Sc-Sf.arff")));
            trainData2.setClassIndex(trainData2.numAttributes()-1);
        } catch (Exception ex) {
            System.out.println("Error Loading...");
        }
        
        // set the classifier parameters (500 trees)
        System.out.println("Setting Options...");
        String[] options = new String[6];
        options[0] = "-I";
        options[1] = "500";
        options[2] = "-K";
        options[3] = "0";
        options[4] = "-S";
        options[5] = "1";

        try {
            model2.setOptions(options);
        } catch (Exception ex) {
            System.out.println("Error setting options...");
        }

        // train the classifier
        System.out.println("Training Classifier...");
        try {
            System.out.println("Starting Training Classifier..." +
                    new Date());
            model2.buildClassifier(trainData2);
            System.out.println("Finished Training Classifier..."+
                    new Date());
        } catch (Exception ex) {
            System.out.println("Error training classifier...");
        }
        
        // set the RandomForest array to null (no other classifiers below this
        modelList2[0]=null;
        modelList2[1]=null;
        modelList2[2]=null;
        modelList2[3]=null;
        
        // extract the header information
        Instances header2 = new Instances(trainData2,0);
        
        // create a RoccaRFModel object for this classifier
        RoccaRFModel classifier2 = new RoccaRFModel(model2, header2, modelList2);
        
        
        
    	// Stage 1
        RandomForest model1 = new RandomForest ();
        Instances trainData1 = null;
        RoccaRFModel[] modelList1 = new RoccaRFModel[2];

        // load the ARFF file containing the training set
        System.out.println("Loading Stage 1 data...");
        try {
            trainData1 = new Instances
                    (new BufferedReader
                    (new FileReader
                      ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\auto 2-stage data\\auto_stage1_Gm-Other.arff")));
            trainData1.setClassIndex(trainData1.numAttributes()-1);
        } catch (Exception ex) {
            System.out.println("Error Loading...");
        }
        
        // set the classifier parameters (500 trees)
        System.out.println("Setting Options...");
        options[0] = "-I";
        options[1] = "500";
        options[2] = "-K";
        options[3] = "0";
        options[4] = "-S";
        options[5] = "1";

        try {
            model1.setOptions(options);
        } catch (Exception ex) {
            System.out.println("Error setting options...");
        }

        // train the classifier
        System.out.println("Training Classifier...");
        try {
            System.out.println("Starting Training Classifier..." +
                    new Date());
            model1.buildClassifier(trainData1);
            System.out.println("Finished Training Classifier..."+
                    new Date());
        } catch (Exception ex) {
            System.out.println("Error training classifier...");
        }
        
        // set the RandomForest array to null (no other classifiers below this
        modelList1[0]=null;
        modelList1[1]=classifier2;
        
        // extract the header information
        Instances header1 = new Instances(trainData1,0);
        
        // create a RoccaRFModel object for this classifier
        RoccaRFModel classifier1 = new RoccaRFModel(model1, header1, modelList1);
        
        
        
        // create the classifier description
        String desc = new String("2-Stage Classifier based on automatic measurements of Atlantic dataset.  ")
        		+ "Initial stage - Gm vs. Other Whistles, 749 whistles/group.  "
        		+ "Other whistles triggers second stage Dd vs. Sc vs. Sf vs. Tt, 475 whistles/species.";

        // save the classifier
        System.out.println("Saving Classifier...");
        try {
            SerializationHelper.writeAll
//                ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\auto 2-stage data\\Auto_RF_2stage.model",
                  ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\auto 2-stage data\\Auto_RF_2stage_TEST.model",
                 new Object[]{desc,classifier1});
        } catch (Exception ex) {
            System.out.println("Error saving classifier: " + ex.getMessage());
        }
        
        System.out.println("Finished!");
    }

}
