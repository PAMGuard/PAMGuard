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
public class RoccaTrainClassifier2 {

    public static void main(String[] args) {
    	
    	/* 2013-08-29 Atlantic Classifier manual dataset 
    	 * Create 2-stage model with 3 different classifiers:
    	 * Stage 1 = ScDd vs. GmTtSf
    	 * Stage 2a = Sc vs. Dd
    	 * Stage 2b = Gm vs. Tt vs. Sf
    	 */
    	
    	// Stage 2b
        RandomForest model2b = new RandomForest ();
        Instances trainData2b = null;
        RoccaRFModel[] modelList2b = new RoccaRFModel[3];

        // load the ARFF file containing the training set
        System.out.println("Loading Stage 2b data...");
        try {
            trainData2b = new Instances
                    (new BufferedReader
                    (new FileReader
//                    ("C:\\Users\\Michael\\Documents\\Work\\Java\\WEKA\\allwhists 12 vars 8sp update 1-28-10.arff")));
//                    ("C:\\Users\\Michael\\Documents\\Work\\Java\\WEKA\\weka vs R\\ETP_orcawale_whists2 modified-subset110perspecies-no_harm_ratios.arff")));
//                    ("C:\\Users\\Mike\\Documents\\Work\\Tom\\California Current\\rocca_CAC_20130318_analysis-subset-subset364perspecies.arff")));
//		              ("C:\\Users\\Mike\\Documents\\Work\\Java\\WEKA\\allwhists update all vars tab 53att 2sp Dc_Dd.arff")));
                      ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\manual 2-stage data\\manual_stage2b_Gm-Tt-Sf.arff")));
            trainData2b.setClassIndex(trainData2b.numAttributes()-1);
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
            model2b.setOptions(options);
        } catch (Exception ex) {
            System.out.println("Error setting options...");
        }

        // train the classifier
        System.out.println("Training Classifier...");
        try {
            System.out.println("Starting Training Classifier..." +
                    new Date());
            model2b.buildClassifier(trainData2b);
            System.out.println("Finished Training Classifier..."+
                    new Date());
        } catch (Exception ex) {
            System.out.println("Error training classifier...");
        }
        
        // set the RandomForest array to null (no other classifiers below this
        modelList2b[0]=null;
        modelList2b[1]=null;
        modelList2b[2]=null;
        
        // extract the header information
        Instances header2b = new Instances(trainData2b,0);
        
        // create a RoccaRFModel object for this classifier
        RoccaRFModel classifier2b = new RoccaRFModel(model2b, header2b, modelList2b);
        
        
        
    	// Stage 2a
        RandomForest model2a = new RandomForest ();
        Instances trainData2a = null;
        RoccaRFModel[] modelList2a = new RoccaRFModel[2];

        // load the ARFF file containing the training set
        System.out.println("Loading Stage 2a data...");
        try {
            trainData2a = new Instances
                    (new BufferedReader
                    (new FileReader
                      ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\manual 2-stage data\\manual_stage2a_Sc-Dd.arff")));
            trainData2a.setClassIndex(trainData2a.numAttributes()-1);
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
            model2a.setOptions(options);
        } catch (Exception ex) {
            System.out.println("Error setting options...");
        }

        // train the classifier
        System.out.println("Training Classifier...");
        try {
            System.out.println("Starting Training Classifier..." +
                    new Date());
            model2a.buildClassifier(trainData2a);
            System.out.println("Finished Training Classifier..."+
                    new Date());
        } catch (Exception ex) {
            System.out.println("Error training classifier...");
        }
        
        // set the RandomForest array to null (no other classifiers below this
        modelList2a[0]=null;
        modelList2a[1]=null;
        
        // extract the header information
        Instances header2a = new Instances(trainData2a,0);
        
        // create a RoccaRFModel object for this classifier
        RoccaRFModel classifier2a = new RoccaRFModel(model2a, header2a, modelList2a);
        
        
        
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
                      ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\manual 2-stage data\\manual_stage1_ScDd-GmTtSf.arff")));
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
        modelList1[0]=classifier2a;
        modelList1[1]=classifier2b;
        
        // extract the header information
        Instances header1 = new Instances(trainData1,0);
        
        // create a RoccaRFModel object for this classifier
        RoccaRFModel classifier1 = new RoccaRFModel(model1, header1, modelList1);
        
        
        
        // create the classifier description
        String desc = new String("2-Stage Classifier based on manual measurements of Atlantic dataset.  ")
        		+ "Initial stage - small dolphin (ScDd) vs. large dolphin (GmTtSf), 498 whistles/group.  "
        		+ "Small dolphin triggers second stage Sc vs. Dd, 249 whistles/species.  Large dolphin "
        		+ "triggers second stage Gm vs. Tt vs. Sf, 256 whistles/species.";

        // save the classifier
        System.out.println("Saving Classifier...");
        try {
            SerializationHelper.writeAll
                ("C:\\Users\\Mike\\Documents\\Work\\Tom\\Atlantic Classifier\\manual 2-stage data\\Manual_RF_2stage_TEST.model",
                 new Object[]{desc,classifier1});
        } catch (Exception ex) {
            System.out.println("Error saving classifier: " + ex.getMessage());
        }
        
        System.out.println("Finished!");
    }

}
