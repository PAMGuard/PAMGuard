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

import java.util.ArrayList;

import weka.classifiers.AbstractClassifier;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Class to hold information about the random forest classifier.  This class was
 * created to facilitate multiple stages of classification.  The AbstractClassifier roccaClassifierModel
 * contains the random forest model itself.  The Instances trainedDataset contains the list of species
 * as well as the list of attributes.  The RoccaRFModel modelList array is the same length as the
 * species list.  If a particular species is a 'trigger' for another stage of classification
 * (eg. 'Small dolphin' which triggers a second classifer based on Common or Striped dolphin)
 * the next-stage RoccaRFModel is stored here. If the species is not a trigger, the array
 * position should be loaded with a null.
 * 
 * @author Mike
 *
 */
public class RoccaRFModel implements java.io.Serializable {

	/**
	 * Version 1 - initial release 2013-08-29
	 * 
	 */
	private static final long serialVersionUID = 01;
	
	
	private RoccaClassifier roccaClassifier;
    private AbstractClassifier roccaClassifierModel = null;
    private Instances trainedDataset = null;
    private RoccaRFModel[] modelList;
    private String[] thisSpList;
    public static final String AMBIG = "Ambig";
    

    /**
     * Constructor
     * 
     * @param roccaClassifierModel
     * @param trainedDataset
     * @param modelList
     */
	public RoccaRFModel(AbstractClassifier roccaClassifierModel,
			Instances trainedDataset, 
			RoccaRFModel[] modelList) {
		super();

		this.roccaClassifierModel = roccaClassifierModel;
		this.trainedDataset = trainedDataset;
		this.modelList = modelList;
		this.thisSpList = new String[trainedDataset.numClasses()];
		for (int i=0; i<trainedDataset.numClasses(); i++) {
			this.thisSpList[i]=trainedDataset.classAttribute().value(i);
		}
	}
    

	public void classifyContour(RoccaClassifier rc, RoccaContourDataBlock rcdb) {
        String classifiedAs = AMBIG;
        this.roccaClassifier = rc;
        
        // set up the attribute vector
        DenseInstance rcdbInst = roccaClassifier.setAttributes(rcdb, trainedDataset);
        if (rcdbInst==null) {
            System.err.println("Error creating Instance from Contour");
            rcdb.setClassifiedAs("Err");
        }

        // call the classifier
        try {
            double speciesNum = roccaClassifierModel.classifyInstance(rcdbInst);
            double[] theseVotes =
                    roccaClassifierModel.distributionForInstance(rcdbInst); 
            double treeConfClassified = theseVotes[(int) speciesNum];
            
            // save the tree votes to rcdb.  Step through the species list one at a time and
            // compare to the species in the current model.  When we find a match, save the
            // tree vote to that position in the vote array.
            // Note that we need to check and see if the size of the treeVotes array is the
            // same as the size of the fullSpList array.  If it's 1 larger, it means the treeVotes
            // array includes Ambig which fullSpList won't.  If that's the case, set the offset
            // variable to compensate.
            String[] fullSpList = roccaClassifier.getClassifierSpList();
            double[] treeVotes = rcdb.getTreeVotes();
            int offset = 0;
            if (treeVotes.length>fullSpList.length) {
            	offset = 1;
            }
            for (int i=0; i<thisSpList.length; i++) {
            	for (int j=0; j<fullSpList.length; j++) {
            		if (thisSpList[i].equals(fullSpList[j])) {
            			treeVotes[j+offset]+=theseVotes[i];
            			break; // stop comparing to this species, go on to the next
            		}
            	}
            }
            rcdb.setTreeVotes(treeVotes);
            

            // if the vote is less than the threshold, set the class to AMBIG and exit
            if (treeConfClassified <
                    ((float) roccaClassifier.roccaControl.roccaParameters.getClassificationThreshold())
                    /100) {
            	rcdb.setClassifiedAs(classifiedAs);
            	
            // otherwise, check if there is a next stage for the classified species	
            } else {
            	
            	// if there is a next stage classifier, call it
            	if (modelList[(int) speciesNum]!=null) {
            		modelList[(int) speciesNum].classifyContour(rc, rcdb);
            		
            	// if there is no next stage, just set the classified species
            	} else {
            		classifiedAs = trainedDataset.classAttribute().value((int) speciesNum);
            		rcdb.setClassifiedAs(classifiedAs);
            	}
            }
            
        } catch (Exception ex) {
            System.err.println("1st Classification failed: " + ex.getMessage());
            ex.printStackTrace();
            rcdb.setClassifiedAs("Err");
        }
 	}
	
/**
 * Classifies a sighting based on the Instance passed
 * 
 * @param sightingParams DenseInstance containing the parameters
 * @return The species with the highest tree votes
 */
	public String classifySighting(DenseInstance sightingParams, int sightingThreshold) {
        String classifiedAs = AMBIG;
        try {
            double speciesNum = roccaClassifierModel.classifyInstance(sightingParams);
            double[] theseVotes =
                    roccaClassifierModel.distributionForInstance(sightingParams); 
            double treeConfClassified = theseVotes[(int) speciesNum];
            
            if (treeConfClassified >= ((float) sightingThreshold) / 100) {
            	classifiedAs = trainedDataset.classAttribute().value((int) speciesNum);
            }            
        } catch (Exception ex) {
            System.err.println("Sighting Classification failed: " + ex.getMessage());
            classifiedAs="Err";
        }
		return classifiedAs;
	}
	
	
	/**
	 * Generates a list of species in this model.  Also, if it finds a next-stage model
	 * it will call the same method in that one in order to include those species as well.
	 * 
	 * @param spList original species list
	 */
	public void generateSpList(ArrayList<String> spList) {
		
		// loop through the class list one species at a time
		for (int i=0; i<trainedDataset.numClasses(); i++) {
			
			// if there is no next-stage classifier listed for the current species, save 
			// the species name to the list
			if (modelList[i]==null) {
				spList.add(trainedDataset.classAttribute().value(i));
				
			// otherwise, call the next-stage classifier for it's list of species.  Set the
			// spList variable to the returned ArrayList
	        } else {
	        	modelList[i].generateSpList(spList);
	        }
		}
	}
	
	
	/**
	 * @return the roccaClassifierModel
	 */
	public AbstractClassifier getRoccaClassifierModel() {
		return roccaClassifierModel;
	}


	/**
	 * @param roccaClassifierModel the roccaClassifierModel to set
	 */
	public void setRoccaClassifierModel(AbstractClassifier roccaClassifierModel) {
		this.roccaClassifierModel = roccaClassifierModel;
	}


	/**
	 * @return the trainedDataset
	 */
	public Instances getTrainedDataset() {
		return trainedDataset;
	}


	/**
	 * @param trainedDataset the trainedDataset to set
	 */
	public void setTrainedDataset(Instances trainedDataset) {
		this.trainedDataset = trainedDataset;
	}


	/**
	 * @return the modelList
	 */
	public RoccaRFModel[] getModelList() {
		return modelList;
	}


	/**
	 * @param modelList the modelList to set
	 */
	public void setModelList(RoccaRFModel[] modelList) {
		this.modelList = modelList;
	}



}
