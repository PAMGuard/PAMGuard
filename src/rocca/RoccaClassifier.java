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

import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.ProgressMonitorInputStream;

import weka.classifiers.AbstractClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Rocca/Weka interface for classifying a contour
 * <p>
 * @author Michael Oswald
 */
public class RoccaClassifier {

    RoccaControl roccaControl;
    AbstractClassifier roccaClassifierModel = null;
    Instances trainedDataset = null;
    RoccaRFModel modelList; 
    RoccaRFModel clickModelList;
    RoccaRFModel eventModelList;
    AbstractClassifier roccaClassifier2Model = null;
    Instances trainedDataset2 = null;
    boolean fieldsSet = false;
    int class1Size = 0;
    int class2Size = 0;
    String[] speciesList = null;
    public static final String AMBIG = "Ambig";
    public static final String NOCLASS = "NoClassifier";

    /**
     * Create RoccaClassifier object for RoccaProcess
     * <p>
     *
     * @param roccaProcess
     */
    public RoccaClassifier(RoccaProcess roccaProcess) {
        this.roccaControl = roccaProcess.roccaControl;
    }


    /**
     * NO LONGER USED, since the addition of the RoccaRFModel object
     * <p>
     * Classifies the passed contour
     * <p>
     * Checks to make sure a classifier has been loaded, then runs the
     * setAttributes method to match the passed contour to the parameter list
     * expected by the classifier.  Once the attribute vector has been created,
     * the classifier is run and the results are saved to the datablock's
     * classifiedAs field.
     *
     * @param rcdb  {@link RoccaContourDataBlock} to be classified
     */
    public void classifyContour(RoccaContourDataBlock rcdb) {
//        String classifiedAs = AMBIG;
//        // if the model hasn't been loaded yet, do that now
//        if (!roccaControl.roccaProcess.isClassifierLoaded()) {
//            roccaControl.roccaProcess.setClassifierLoaded(setUpClassifier());
//
//            // if there was an error loading the model, return "Err"
//            if (!roccaControl.roccaProcess.isClassifierLoaded()) {
//                System.err.println("Cannot load classifier");
//                rcdb.setClassifiedAs("Err");
//            }
//        }
//        
//        // set up the attribute vector
//        DenseInstance rcdbInst = setAttributes(rcdb, trainedDataset);
//        if (rcdbInst==null) {
//            System.err.println("Error creating Instance from Contour (first Classifier)");
//            rcdb.setClassifiedAs("Err");
//        }
//
//        // call the classifier
//        try {
//        	double[] treeConfArray = null;
//            double speciesNum = roccaClassifierModel.classifyInstance(rcdbInst);
//            double[] tempArray =
//                    roccaClassifierModel.distributionForInstance(rcdbInst);
//            double treeConfClassified = tempArray[(int) speciesNum];
//            rcdbInst.setClassValue(speciesNum);
//            
//            /* if the tree confidence vote is greater than the threshold value,
//             * check if this is the trigger species.  If it is, run the second classifier.
//             * Otherwise, set the classifiedAs field to the species.  
//             * If the tree confidence vote is less than the threshold, keep it
//             * as "Ambig".
//             */
//            if (treeConfClassified >=
//                    ((float) roccaControl.roccaParameters.getClassificationThreshold())
//                    /100) {
//            	
//            	/* check if this is the trigger species */            	
//            	if (speciesNum==roccaControl.roccaParameters.getStage1Link()) {
//            		
//                    // reset up the attribute vector to the 2nd classifier
//                    rcdbInst = setAttributes(rcdb, trainedDataset2);
//                    if (rcdbInst==null) {
//                        System.err.println("Error creating Instance from Contour (2nd Classifier)");
//                        rcdb.setClassifiedAs("Err");
//                    }
//
//                    // call the 2nd classifier
//                    try {
//                        speciesNum = roccaClassifier2Model.classifyInstance(rcdbInst);
//                        double[] tempArray2 =
//                                roccaClassifier2Model.distributionForInstance(rcdbInst);
//                        treeConfClassified = tempArray2[(int) speciesNum];
//                        rcdbInst.setClassValue(speciesNum);
//                        
//                        /* create the treeConfArray variable by combining tree votes from
//                         * both classifiers
//                         */
//                		int j = 0;
//            	        treeConfArray = new double[class1Size + class2Size - 1];
//            	        for (int i=0; i<class1Size; i++ ) {
//            	        	if (i!=roccaControl.roccaParameters.getStage1Link()) {
//            	        		treeConfArray[j] = tempArray[i];
//            	        		j++;
//            	        	}
//            	        }
//            	        for (int i=0; i<class2Size; i++ ) {
//            	        	treeConfArray[j] = tempArray2[i];
//            	        		j++;
//            	        }
//                        
//                        /* if the tree confidence vote is greater than the threshold value,
//                         * set the classifiedAs field to the species.  
//                         * If the tree confidence vote is less than the threshold, keep it
//                         * as "Ambig".
//                         */
//                        if (treeConfClassified >=
//                                ((float) roccaControl.roccaParameters.getClassificationThreshold())
//                                /100) {
//                            classifiedAs = trainedDataset2.classAttribute().value((int) speciesNum);
//                        }
//
//                    } catch (Exception ex) {
//                        System.err.println("2nd Classification failed: " + ex.getMessage());
//                        rcdb.setClassifiedAs("Err");
//                    }
//                    
//                /* if this isn't the trigger species, set the classification variable and
//                 * load the tree vote positions for the 2nd classifier with 0's (if there
//                 * is a 2nd classifier)
//                 */
//            	} else {
//            		classifiedAs = trainedDataset.classAttribute().value((int) speciesNum);
//            		
//            		/* if this is a single classifier system, just save the tree votes */
//            		if (trainedDataset2==null) {
//            			treeConfArray = tempArray;
//            			
//            		/* if this is a two-classifier system, save the votes from the first classifier
//            		 * and set the second set of votes to 0
//            		 */
//            		} else {
//            			int j=0;
//            			treeConfArray = new double[class1Size + class2Size - 1];
//            	        for (int i=0; i<class1Size; i++ ) {
//            	        	if (i!=roccaControl.roccaParameters.getStage1Link()) {
//            	        		treeConfArray[j] = tempArray[i];
//            	        		j++;
//            	        	}
//            	        }
//            	        for (int i=0; i<class2Size; i++ ) {
//            	        	treeConfArray[j] = 0;
//            	        		j++;
//            	        }
//            		}
//            	}
//            } else {
//            	/* AMBIG - load the tree votes here */
//            }
//
//            /* set the species field */
//            rcdb.setClassifiedAs(classifiedAs);
//
//            /* set the tree votes field */
//            rcdb.setTreeVotes(treeConfArray);
//
//
//        } catch (Exception ex) {
//            System.err.println("1st Classification failed: " + ex.getMessage());
//            rcdb.setClassifiedAs("Err");
//        }
    }


    /**
     * Classifies the passed contour
     * <p>
     * Checks to make sure a classifier has been loaded, then calls the RoccaRFModel object
     * to classify the contour.  Clears the rcdb tree votes first, since the RccaRFModel object
     * adds to the votes as it iterates through the different classifiers
     *
     * @param rcdb  {@link RoccaContourDataBlock} to be classified
     */
    public void classifyContour2(RoccaContourDataBlock rcdb) {
    	
    	// change this code, since we now allow the user to run Rocca without
    	// a classifier.  Trying to load a non-existent classifier for every
    	// whistle or click is just a waste of time - it should have been loaded
    	// during startup or when set in the dialog
//
        // if the model hasn't been loaded yet, do that now
//        if (!roccaControl.roccaProcess.isClassifierLoaded()) {
//            roccaControl.roccaProcess.setClassifierLoaded(setUpClassifier());
//
//            // if there was an error loading the model, return "Err"
//            if (!roccaControl.roccaProcess.isClassifierLoaded()) {
//                System.err.println("Cannot load classifier");
//                rcdb.setClassifiedAs("Err");
//                return;
//            }
//        }
    	if (!roccaControl.roccaProcess.isClassifierLoaded()) {
    		double[] treeVotes = new double[] {0,1};
    		rcdb.setTreeVotes(treeVotes);
    		rcdb.setClassifiedAs(NOCLASS);
    		return;
    	}

        // clear the tree votes
        double[] treeVotes=rcdb.getTreeVotes();
        for (int i=0; i<treeVotes.length; i++) {
        	treeVotes[i]=0;
        }
        rcdb.setTreeVotes(treeVotes);

        // call the top-level classifier
        if (rcdb.isThisAWhistle()) {
        	modelList.classifyContour(this, rcdb);
        } else {
        	clickModelList.classifyContour(this, rcdb);
        }
    }
    
    
    /**
     * Use the event classifier to classify the current sighting
     * @param detectionStats a String generated in the RoccaSidePanel object, containing
     * @return the species classification
     */
    public String classifySighting(String detectionHeader, String detectionStats) {
    	
        // if the model hasn't been loaded yet, do that now
        if (!roccaControl.roccaProcess.isClassifierLoaded()) {
        	
        	// since we're now allowing the user to run Rocca without a classifier, don't
        	// try and load it here.  Just return a "No Classifier" classification
//            roccaControl.roccaProcess.setClassifierLoaded(setUpClassifier());
//
//            // if there was an error loading the model, return "Err"
//            if (!roccaControl.roccaProcess.isClassifierLoaded()) {
//                System.err.println("Cannot load classifier");
//                return "Err";
//            }
        	return NOCLASS;
        }
        
    	// parse both Strings into arrays, and then copy into new arrays that are 1 index larger for the numWhistle/numClicks ratio
        // serialVersionUID=25 2017/04/10 moved ratio calc to RoccaSidePanel (where it belongs), so don't increase the array size.
        // The array copy was left here so that the rest of the method would not have to be refactored
        String[] headerTemp = detectionHeader.split("[,]+");
        String[] statTemp = detectionStats.split("[,]+");
        int k = headerTemp.length;
        String[] headers = Arrays.copyOf(headerTemp, k);
        String[] stats = Arrays.copyOf(statTemp, k);
        
        // add in the NumWhistles / NumClicks ratio parameter
        // note that the column headers Number_of_Whistles and Number_of_Clicks is hard-coded here.  If that ever changes
        // in RoccaSidePanel.createDetectionStatsHeader, it needs to get updated here as well.
        // serialVersionUID=25 2017/04/10 moved ratio calc to RoccaSidePanel.createDetectionStatsString.  Not sure why it was here
        // in the first place
//        int numWhistIdx = Arrays.asList(headers).indexOf("Number_of_Whistles");
//        int numClickIdx = Arrays.asList(headers).indexOf("Number_of_Clicks");
//        headers[k] = "NumWhistle-NumClick_Ratio";
//        if (Float.parseFloat(stats[numClickIdx])==0) {
//        	stats[k]="0";
//        } else {
//        	stats[k] = Float.toString(Float.parseFloat(stats[numWhistIdx]) / Float.parseFloat(stats[numClickIdx]));
//        }
        
        // serialVersionUID=25 2017/04/10 quick check - if there haven't been any detections yet, exit immediately
        int numWhistIdx = Arrays.asList(headers).indexOf("Number_of_Whistles");
        int numClickIdx = Arrays.asList(headers).indexOf("Number_of_Clicks");
        if ((Float.parseFloat(stats[numClickIdx])+Float.parseFloat(stats[numClickIdx])==0)) {
            return "";
        }

        // finally, need to normalize the tree votes or else they are useless.
        // first, find them
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < headers.length; i++) {
            if(headers[i].contains("_votes")) {
                indexList.add(i);
            }
        }
        
        // now get all the votes, convert to float and add them together
        float[] voteList = new float[indexList.size()];
        float voteSum = 0;
        for (int i=0; i < indexList.size(); i++) {
        	voteList[i] = Float.parseFloat(stats[indexList.get(i)]);
        	voteSum+=voteList[i];
        }
        
        // put the normalized votes back into the stats array
        for (int i=0; i < indexList.size(); i++) {
        	stats[indexList.get(i)] = Float.toString(voteList[i]/voteSum);
        }
        
        // loop through the RoccaRFModel, and find the index in the detectionHeader array that matches the
        // RoccaRFModel attribute. Then, copy the detectionStats value at that index into a DenseInstance
        Instances trainedDataset = eventModelList.getTrainedDataset();
        DenseInstance sightingParams = new DenseInstance(trainedDataset.numAttributes());
        int index=0;

        // loop through the training attributes one at a time
        for (int i=0; i<trainedDataset.numAttributes(); i++) {

            // skip the index position if it's the class attribute
            if (i!=trainedDataset.classIndex()) {

                // find the index in the map of the key that matches the current attribute
                index = Arrays.asList(headers).indexOf(trainedDataset.attribute(i).name());

                // if the index is 0 or higher, the attribute has been found; load
                // it into the instance
                // serialVersionUID=24 2016/08/10 add a try/catch just in case some of the values
                // are N/A - that will happen for the min time between detections when there's only
                // 1 click or whistle
                if (index >= 0) {
                	try {
                		sightingParams.setValue(i, Float.parseFloat(stats[index]));
                	} catch (Exception e) {
                		System.out.println("Error trying to classify - may require more data points");
                		return null;
                	}
                
                // otherwise, the attribute wasn't found; return an error
                } else {
                    System.out.println("Error - Classifier attribute " +
                            trainedDataset.attribute(i).name() +
                            " not found in detection header");
                    return null;
                }
            }
        }

        // set the dataset for the instance to the trainedDataset
        sightingParams.setDataset(trainedDataset);
        
        // now call the classifier
        String species = eventModelList.classifySighting(sightingParams, roccaControl.roccaParameters.getSightingThreshold());
        return species;  
    }

    
    /**
     * Loads the Classifier model from the file specified in RoccaParameters
     *
     * @return      boolean flag indicating success or failure of load
     *
     */
    public boolean setUpClassifier() {
    	modelList = null;	// serialVersionUID=24 2016/08/10 added to make it easier to deal with java memory limitations
    	clickModelList = null;	// serialVersionUID=24 2016/08/10 added to make it easier to deal with java memory limitations
    	eventModelList = null;	// serialVersionUID=24 2016/08/10 added to make it easier to deal with java memory limitations
        speciesList = null;

    	// load the whistle classifier
    	if (roccaControl.roccaParameters.isClassifyWhistles()) {
	        String fname = roccaControl.roccaParameters.roccaClassifierModelFilename.getAbsolutePath();
	        File f = new File(fname);
	        if (f.exists() == false) {
	        	WarnOnce.showWarning(roccaControl.getUnitName(), "Rocca whistle classifier file cannot be found at " + fname, WarnOnce.WARNING_MESSAGE);
	            roccaControl.roccaParameters.setClassifyWhistles(false);
	        }
	        else {
	        	try {
	        		BufferedInputStream input = new BufferedInputStream(
	        				(new ProgressMonitorInputStream(null, "Loading Whistle Classifier - Please wait",
	        						new FileInputStream(fname))));
	        		Object[] modelParams = SerializationHelper.readAll(input);
	        		// separate the classifier model from the training dataset info
	        		// roccaClassifierModel = (AbstractClassifier) modelParams[0];
	        		// trainedDataset = (Instances) modelParams[1];

	        		// there are 2 different styles of model file, the original version and the version
	        		// developed for the 2-stage classifier.  Both files contain 2 objects.
	        		// The original version contained the classifier and the training dataset.  The newer
	        		// version contains a String description and the classifier.  Test the first object;
	        		// if it's a String, then this is the newer version and the String is the description.
	        		// If it's not a String, assume this is the old version and create a RoccaRFModel
	        		// object from the file contents.
	        		if (modelParams[0] instanceof String) {
	        			modelList = (RoccaRFModel) modelParams[1];
	        		} else {
	        			AbstractClassifier classifier = (AbstractClassifier) modelParams[0];
	        			Instances dataset = (Instances) modelParams[1];
	        			RoccaRFModel[] models = new RoccaRFModel[dataset.numClasses()];
	        			for (int i=0; i<dataset.numClasses(); i++) {
	        				models[i]=null;
	        			}
	        			modelList = new RoccaRFModel(classifier, dataset, models);
	        		}

	        	} catch (Exception ex) {
	        		System.err.println("Deserialization of Whistle Classifier failed: " + ex.getMessage());
	        		ex.printStackTrace();
	        		roccaControl.roccaParameters.setClassifyWhistles(false);
	        	}
	        }
    	}
        
        // serialVersionUID=24 2016/08/10 added to load click classifier
    	if (roccaControl.roccaParameters.isClassifyClicks()) {
	        String fname = roccaControl.roccaParameters.roccaClickClassifierModelFilename.getAbsolutePath();
	        File f = new File(fname);
	        if (f.exists() == false) {
	        	WarnOnce.showWarning(roccaControl.getUnitName(), "Rocca click classifier file cannot be found at " + fname, WarnOnce.WARNING_MESSAGE);
	            roccaControl.roccaParameters.setClassifyClicks(false);
	        }
	        else {
	        	try {
	        		BufferedInputStream input = new BufferedInputStream(
	        				(new ProgressMonitorInputStream(null, "Loading Click Classifier - Please wait",
	        						new FileInputStream(fname))));
	        		Object[] modelParams = SerializationHelper.readAll(input);
	        		// separate the classifier model from the training dataset info
	        		// roccaClassifierModel = (AbstractClassifier) modelParams[0];
	        		// trainedDataset = (Instances) modelParams[1];

	        		// there are 2 different styles of model file, the original version and the version
	        		// developed for the 2-stage classifier.  Both files contain 2 objects.
	        		// The original version contained the classifier and the training dataset.  The newer
	        		// version contains a String description and the classifier.  Test the first object;
	        		// if it's a String, then this is the newer version and the String is the description.
	        		// If it's not a String, assume this is the old version and create a RoccaRFModel
	        		// object from the file contents.
	        		if (modelParams[0] instanceof String) {
	        			clickModelList = (RoccaRFModel) modelParams[1];
	        		} else {
	        			AbstractClassifier classifier = (AbstractClassifier) modelParams[0];
	        			Instances dataset = (Instances) modelParams[1];
	        			RoccaRFModel[] models = new RoccaRFModel[dataset.numClasses()];
	        			for (int i=0; i<dataset.numClasses(); i++) {
	        				models[i]=null;
	        			}
	        			clickModelList = new RoccaRFModel(classifier, dataset, models);
	        		}

	        	} catch (Exception ex) {
	        		System.err.println("Deserialization of Click Classifier failed: " + ex.getMessage());
	        		ex.printStackTrace();
	        		roccaControl.roccaParameters.setClassifyClicks(false);
	        	}
    		}
    	}
    	
        // if we didn't load either classifier, don't bother loading trying to load an encounter
    	// classifier because it relies on the species.  Just reset the side panel and return
        if (!roccaControl.roccaParameters.isClassifyWhistles() && !roccaControl.roccaParameters.isClassifyClicks()) {
        	resetSidePanel();
        	return false;
        }
        
        // serialVersionUID=24 2016/08/10 added to load event classifier
    	if (roccaControl.roccaParameters.isClassifyEvents()) {
	        String fname = roccaControl.roccaParameters.roccaEventClassifierModelFilename.getAbsolutePath();
	        File f = new File(fname);
	        if (f.exists() == false) {
	        	WarnOnce.showWarning(roccaControl.getUnitName(), "Rocca events classifier file cannot be found at " + fname, WarnOnce.WARNING_MESSAGE);
	            roccaControl.roccaParameters.setClassifyEvents(false);
	        }
	        else {
	        	try {
	        		BufferedInputStream input = new BufferedInputStream(
	        				(new ProgressMonitorInputStream(null, "Loading Event Classifier - Please wait",
	        						new FileInputStream(fname))));
	        		Object[] modelParams = SerializationHelper.readAll(input);

	        		// there are 2 different styles of model file, the original version and the version
	        		// developed for the 2-stage classifier.  Both files contain 2 objects.
	        		// The original version contained the classifier and the training dataset.  The newer
	        		// version contains a String description and the classifier.  Test the first object;
	        		// if it's a String, then this is the newer version and the String is the description.
	        		// If it's not a String, assume this is the old version and create a RoccaRFModel
	        		// object from the file contents.
	        		if (modelParams[0] instanceof String) {
	        			eventModelList = (RoccaRFModel) modelParams[1];
	        		} else {
	        			AbstractClassifier classifier = (AbstractClassifier) modelParams[0];
	        			Instances dataset = (Instances) modelParams[1];
	        			RoccaRFModel[] models = new RoccaRFModel[dataset.numClasses()];
	        			for (int i=0; i<dataset.numClasses(); i++) {
	        				models[i]=null;
	        			}
	        			eventModelList = new RoccaRFModel(classifier, dataset, models);
	        		}

	        	} catch (Exception ex) {
	        		System.err.println("Deserialization of Event Classifier failed: " + ex.getMessage());
	        		roccaControl.roccaParameters.setClassifyEvents(false);
	        	}
	        }
    	}

        resetSidePanel();
        return true;
    }
    
    private void resetSidePanel() {
    	// get the current species list
        String[] speciesList = getClassifierSpList();
        
        /* check to make sure a unit exists - if Pamguard has just started,
         * there is no RoccaSightingDataUnit yet.  If that's the case, create
         * a blank one first and then update the species list
         */
        if (roccaControl.roccaSidePanel.rsdb.getUnitsCount()==0) {
            RoccaSightingDataUnit unit = new RoccaSightingDataUnit
                    (PamCalendar.getTimeInMillis(),0,0,0,roccaControl);	// serialVersionUID=22 2015/06/13 added roccaControl
            roccaControl.roccaSidePanel.rsdb.addPamData(unit);
            roccaControl.roccaSidePanel.setCurrentUnit(unit);
        }
        roccaControl.roccaSidePanel.setSpecies(speciesList);
    }

    /**
     * Sets up the available datablock fields to match the classifier model
     * <p>
     * Compares the fields in the passed RoccaContourDataBlack to the fields
     * required for the classifier.  If any fields are missing, an error is
     * displayed and the method returns false.
     * If all the fields are available, an Instance is created matching the order
     * of the datablock fields to the order of the classifier fields.
     *
     * @param rcdb  {@link RoccaContourDataBlock} to be classified
     * @return      {@link Instance} containing the values of the rcdb parameters that
     *              match the classifier attributes
     */
    public DenseInstance setAttributes(RoccaContourDataBlock rcdb) {
        fieldsSet = false;
        ArrayList<String> keyNames = rcdb.getKeyNames();

        // if the model hasn't been loaded yet, do that now
        if (!roccaControl.roccaProcess.isClassifierLoaded()) {
            roccaControl.roccaProcess.setClassifierLoaded(setUpClassifier());
            // if there was an error loading the model, return null
            if (!roccaControl.roccaProcess.isClassifierLoaded()) {
                return null;
            }
        }

        // loop through the trainedDataset attribute list, making sure the
        // required fields are available in the datablock and loading them into
        // a vector
        DenseInstance rcdbInst = new DenseInstance(trainedDataset.numAttributes());
        int index;

        // loop through the training attributes one at a time
        for (int i=0; i<trainedDataset.numAttributes(); i++) {

            // skip the index position if it's the class attribute
            if (i!=trainedDataset.classIndex()) {

                // find the index in the map of the key that matches the current attribute
                index = keyNames.indexOf(trainedDataset.attribute(i).name().toUpperCase(Locale.ENGLISH));

                // if the index is 0 or higher, the attribute has been found; load
                // it into the instance
                if (index >= 0) {
                    rcdbInst.setValue(i, rcdb.getContour()
                            .get(RoccaContourStats.ParamIndx.values()[index]));
                
                // otherwise, the attirbute wasn't found; return an error
                } else {
                    System.out.println("Error - Classifier attribute " +
                            trainedDataset.attribute(i).name() +
                            " not found in contour datablock");
                    return null;
                }
            }
        }

        // set the dataset for the instance to the trainedDataset
        rcdbInst.setDataset(trainedDataset);

        // set the flag to true and return the Instance object
        fieldsSet = true;
        return rcdbInst;
    }

    /**
     * Sets up the available datablock fields to match the classifier model
     * <p>
     * Compares the fields in the passed RoccaContourDataBlack to the fields
     * required for the classifier.  If any fields are missing, an error is
     * displayed and the method returns false.
     * If all the fields are available, an Instance is created matching the order
     * of the datablock fields to the order of the classifier fields.
     *
     * @param rcdb  {@link RoccaContourDataBlock} to be classified
     * @param trainedDataset {@link Instances} containing the training data set
     * 
     * @return      {@link Instance} containing the values of the rcdb parameters that
     *              match the classifier attributes
     */
    public DenseInstance setAttributes(RoccaContourDataBlock rcdb, Instances trainedDataset) {
        fieldsSet = false;
        ArrayList<String> keyNames = rcdb.getKeyNames();

        // if the model hasn't been loaded yet, do that now
        if (!roccaControl.roccaProcess.isClassifierLoaded()) {
            roccaControl.roccaProcess.setClassifierLoaded(setUpClassifier());
            // if there was an error loading the model, return null
            if (!roccaControl.roccaProcess.isClassifierLoaded()) {
                return null;
            }
        }

        // loop through the trainedDataset attribute list, making sure the
        // required fields are available in the datablock and loading them into
        // a vector
        DenseInstance rcdbInst = new DenseInstance(trainedDataset.numAttributes());
        int index;

        // loop through the training attributes one at a time
        for (int i=0; i<trainedDataset.numAttributes(); i++) {

            // skip the index position if it's the class attribute
            if (i!=trainedDataset.classIndex()) {

                // find the index in the map of the key that matches the current attribute
                index = keyNames.indexOf(trainedDataset.attribute(i).name().toUpperCase(Locale.ENGLISH));

                // if the index is 0 or higher, the attribute has been found; load
                // it into the instance
                if (index >= 0) {
                    rcdbInst.setValue(i, rcdb.getContour()
                            .get(RoccaContourStats.ParamIndx.values()[index]));
                
                // otherwise, the attirbute wasn't found; return an error
                } else {
                    System.out.println("Error - Classifier attribute " +
                            trainedDataset.attribute(i).name() +
                            " not found in contour datablock");
                    return null;
                }
            }
        }

        // set the dataset for the instance to the trainedDataset
        rcdbInst.setDataset(trainedDataset);

        // set the flag to true and return the Instance object
        fieldsSet = true;
        return rcdbInst;
    }

    public boolean areFieldsSet() {
        return fieldsSet;
    }

    public void setFieldsSet(boolean fieldsSet) {
        this.fieldsSet = fieldsSet;
    }

    /**
     * returns list of species classes.  If variable is null, creates the list first
     * 
     * @return String[] list of species
     */
    public String[] getClassifierSpList() {

    	if (speciesList==null) {
    		speciesList = generateSpList();
    	}
    	
    	return speciesList;
    }
    
    
    /**
	 * Clears the species list for this classifier
	 */
	public void clearSpeciesList() {
		this.speciesList = null;
	}


	/**
     * generates list of species classes.
     * serialVersionUID = 24 2016/08/10 added check for whistle or click classifier
     * 
     * 2021/05/11 instead of returning a null if we are not using classifiers, return
     * a "No Classifier" species.
     * 
     * @return String[] list of species
     */
    public String[] generateSpList() {
    	ArrayList<String> tempList = new ArrayList<String>(); 
    	if (roccaControl.roccaParameters.isClassifyWhistles()) {
    		modelList.generateSpList(tempList);
    	} else if (roccaControl.roccaParameters.isClassifyClicks()) {
    		clickModelList.generateSpList(tempList);
    	} else {
    		String[] speciesAsString = new String[1];
    		speciesAsString[0] = RoccaClassifier.NOCLASS;
    		return speciesAsString;
    	}
    	String[] tempArray = tempList.toArray(new String[tempList.size()]);
    	return tempArray;
    }
    	
	/**
     * generates list of species classes.  If there are two classifiers, both lists will
     * be combined into single list based on trigger class
     * 
     * NOTE: this method is the original version, which loaded 2 separate classifier files.  No
     * longer used since RoccaRFModel class has been implemented, but I hate to delete the code...
     * 
     * @return String[] list of species
     */
    public String[] generateSpList_orig() {
    	
    	class1Size=trainedDataset.numClasses();
    	
    	/* if this is a single-classification system */
    	if (trainedDataset2==null) {
	        String[] speciesList = new String[trainedDataset.numClasses()];
	        for (int i=0; i<trainedDataset.numClasses(); i++ ) {
	            speciesList[i] = trainedDataset.classAttribute().value(i);
	        }
	        return speciesList;
	        
	    /* if this is a 2-stage classification system */
    	} else {
    		class2Size=trainedDataset2.numClasses();
    		int j = 0;
	        String[] speciesList = new String[trainedDataset.numClasses() +
	                                          trainedDataset2.numClasses() - 1];
	        for (int i=0; i<trainedDataset.numClasses(); i++ ) {
	        	if (i!=roccaControl.roccaParameters.getStage1Link()) {
	        		speciesList[j] = trainedDataset.classAttribute().value(i);
	        		j++;
	        	}
	        }
	        for (int i=0; i<trainedDataset2.numClasses(); i++ ) {
	        		speciesList[j] = trainedDataset2.classAttribute().value(i);
	        		j++;
	        }
	        return speciesList;
    	}
    }


	/**
	 * @return the random forest model
	 */
	public RoccaRFModel getRFModel() {
		return modelList;
	}


	/**
	 * @param rfModel the random forest Model to set
	 */
	public void setRFModel(RoccaRFModel rfModel) {
		this.modelList = rfModel;
	}
    
    
    
    
}
