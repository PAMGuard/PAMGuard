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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

/**
 * Container holding information about the current sighting, including pointers
 * to the contours already measured
 *
 * @author Michael Oswald
 */
public class RoccaSightingDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection {

    public static final String NONE = "<None>";

    /** Sighting number */
    String sightNum = NONE;

    /** the list of species names, saved as a JLabel array to make it easier
     to display on the side panel */
    JLabel[] species;

    /** the current whistle count for each species */
    int[] speciesClassCount;

    /** the current tree vote count for each species */
    double[] speciesTreeVotes;

    /** the overall sighting classification */
    String sightClass = NONE;

    /** boolean indicating whether or not the sighting has been saved to the
     * database
     */
    boolean sightingSaved = false;

    /** boolean indicating whether we've classified any whistles in the sighting
     * yet
     */
    boolean whistleClassified = false;
        
    /**
     * SortedMap of start times and durations for whistles.  We are using TreeMap<Double,List<Double>>
     * instead of TreeMap<Double,Double> because it is possible for two whistles to have the same
     * start time.  Since a TreeMap cannot hold duplicate keys, the durations need to be kept
     * in a list for just such an occurance.
     */
    private TreeMap<Double,List<Double>> whistleList = new TreeMap<Double,List<Double>>();
    
    /**
     * SortedMap of start times and durations for clicks.  We are using TreeMap<Double,List<Double>>
     * instead of TreeMap<Double,Double> because it is possible for two clicks to have the same
     * start time.  Since a TreeMap cannot hold duplicate keys, the durations need to be kept
     * in a list for just such an occurance
     */
    private TreeMap<Double,List<Double>> clickList = new TreeMap<Double,List<Double>>();
    
    /**
     * keep track of the RoccaControl object
     */
    RoccaControl roccaControl;
    
    /**
     * A String containing the last-calculated detectionStats line
     * serialVersionUID=24 2016/08/10
     */
    private String detectionStats;
    
    /**
     * When Pamguard is closed, we lose all the information from the classified whistles and clicks
     * that we would normally use to calculate the encounter stats.  When we load the encounter stats file
     * the next time Pamguard starts, those values are set to 0.  And then when the encounter stats file
     * gets overwritten, we lose the old information.
     * To prevent this, set a flag when we load the encounter stats file to indicate that the underlying
     * data has been lost and not to bother trying to recalculate it.  just used the previously saved
     * string instead.
     * serialVersionUID=24 2016/08/10
     */
    private boolean sightingDataLost=false;
    
    /**
     * Boolean keeping track of when new data is added to old encounters.  If we haven't added anything
     * to an old encounter, don't bother recalculating the ancillary variables because it takes too much
     * time.  Just used the saved detectionStats string from the last time it was calculated.
     * serialVersionUID=24 2016/08/10
     */
    private boolean newDataAdded=true;
    
    /**
     * Latitude when the RoccaContourDataBlock was created
	 * serialVersionUID=24 2016/08/10 added
     */
    private double latitude;
    
    /**
     * Longitude when the RoccaContourDataBlock was created
	 * serialVersionUID=24 2016/08/10 added
	 */
    private double longitude;
    

    
    

    /**
     * Create a new RoccaSightingDataUnit
     * serialVersionUID=22 2015/06/13 added roccaControl
     * serialVersionUID=24 2016/08/10 added call to setLatLong
     *
     * @param timeMilliseconds
     * @param channelBitmap
     * @param startSample
     * @param duration
     * @param sNum  the current sighting number
     * @param speciesList   a JLabel array containing the species list.
     * @param roccaControl
     */
    public RoccaSightingDataUnit(
            long timeMilliseconds,
            int channelBitmap,
            long startSample,
            long duration,
            String sNum,
            JLabel[] speciesList,
            RoccaControl roccaControl) {
		super(timeMilliseconds, channelBitmap, startSample, duration);

		this.roccaControl = roccaControl;
        this.sightNum = sNum;
        this.species = speciesList;
        speciesClassCount = new int[species.length];
        speciesTreeVotes = new double[species.length];
        clearCounts();
        setLatLong();
	}

    /**
     * Create a new RoccaSightingDataUnit
     * serialVersionUID=22 2015/06/13 added roccaControl
     * serialVersionUID=24 2016/08/10 added call to setLatLong
     * 
     * @param timeMilliseconds
     * @param channelBitmap
     * @param startSample
     * @param duration
     * @param sNum  the current sighting number
     * @param speciesList   a String array containing the species list.
     * @param roccaControl
     */
    public RoccaSightingDataUnit(
            long timeMilliseconds,
            int channelBitmap,
            long startSample,
            long duration,
            String sNum,
            String[] speciesList,
            RoccaControl roccaControl) {
		super(timeMilliseconds, channelBitmap, startSample, duration);

		this.roccaControl = roccaControl;
        this.sightNum = sNum;
        setSpecies(speciesList);
        setLatLong();
	}

    /**
     * Create a new RoccaSightingDataUnit.  This constructor is used primarily
     * when the sidePanel is created and the user loads the classifier, but
     * no sighting number exists yet.
     * serialVersionUID=22 2015/06/13 added roccaControl
     * serialVersionUID=24 2016/08/10 added call to setLatLong
     * 
     * @param timeMilliseconds
     * @param channelBitmap
     * @param startSample
     * @param duration
     * @param roccaControl
     */
    public RoccaSightingDataUnit(
            long timeMilliseconds,
            int channelBitmap,
            long startSample,
            long duration,
            RoccaControl roccaControl) {
		super(timeMilliseconds, channelBitmap, startSample, duration);

//        this.sightNum = "<Add Sighting>";
		this.roccaControl = roccaControl;
        species = new JLabel[1];
        species[0] = new JLabel(RoccaClassifier.AMBIG);
        speciesClassCount = new int[species.length];
        speciesTreeVotes = new double[species.length];
        setLatLong();
	}
    
    /**
     * Calls the GPS routines to set latitude and longitude, if the user has set it up
     */
    public void setLatLong() {
        // add the latitude and longitude.
        if (roccaControl.roccaParameters.weAreUsingGPS()) {
        	try {
        		latitude = roccaControl.getRoccaProcess().getGpsSourceData().getLastUnit().getGpsData().getLatitude();
        		longitude = roccaControl.getRoccaProcess().getGpsSourceData().getLastUnit().getGpsData().getLongitude();
        	} catch (Exception e) {
        		latitude=0;
        		longitude=0;
        	}
        } else {
    		latitude=0;
    		longitude=0;
        }
    }
    

    public String getSightNum() {
        return sightNum;
    }

    public void setSightNum(String sightNum) {
        this.sightNum = sightNum;

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * Creates a new list of species and sets the counts to 0.
     * This method will add 'Ambig' to the species list, if it isn't already
     * included.
     *
     * @param speciesAsString   a string array of species names (max 5 characters).
     */
    public void setSpecies(String[] speciesAsString) {
        this.species = null;
        this.speciesClassCount = null;
        this.speciesTreeVotes = null;
        this.whistleClassified = false;
        
        /* add 'Ambig' to the species list, if it isn't already there */
        String[] speciesListWithAmbig = validateSpeciesList(speciesAsString);

        /* create the species JLabel array, tally count array and tree vote
         * array.  Set the tally counts and tree votes to 0
         */
        species = new JLabel[speciesListWithAmbig.length];
        speciesClassCount = new int[speciesListWithAmbig.length];
        speciesTreeVotes = new double[speciesListWithAmbig.length];
        for (int i=0; i<speciesListWithAmbig.length; i++) {
            species[i] = new JLabel(speciesListWithAmbig[i]);
            speciesClassCount[i]=0;
            speciesTreeVotes[i] = 0;
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * Checks the passed String array for 'Ambig' (RoccaClassifier.AMBIG).  If
     * it's there, the array is returned without any changes.  If it's not there,
     * it is added to the beginning of the array and the new array is returned.
     * <p>
     * Note that this method is static, so calls to it do not need a specific
     * RoccaSightingDataUnit instance.  This was done to make it easier to
     * call from the RoccaSpecPopUp object, which does not see any instances.
     * 
     * 2021/05/11 renamed method and added check for no classifier
     * 
     * @param speciesAsString
     * @return
     */
    public static String[] validateSpeciesList(String[] speciesAsString) {
    	// check first if the passed array is null.  If so, no classifier
    	// has been loaded.  Just return an array with AMBIG and NOCLASS
    	if (speciesAsString==null) {
    		speciesAsString = new String[2];
    		speciesAsString[0] = RoccaClassifier.AMBIG;
    		speciesAsString[1] = RoccaClassifier.NOCLASS;
    		return speciesAsString;
    	}
        /* check if the passed array already contains 'Ambig'.  If so, just
         * return the original array
         */
        for (String species : speciesAsString) {
            if (species.equals(RoccaClassifier.AMBIG)) {
                return speciesAsString;
            }
        }

        /* if we got here, it means Ambig is not in the list.  Add it to the
         * beginning, and return the new array
         */
        String[] newSpList = new String[speciesAsString.length+1];
        newSpList[0]=RoccaClassifier.AMBIG;
        System.arraycopy(speciesAsString, 0, newSpList, 1, speciesAsString.length);
        return newSpList;
    }

    /**
     * clear the counts for the species list
     */
    public void clearCounts() {
        for (int i=0; i<species.length; i++) {
            speciesClassCount[i] = 0;
            speciesTreeVotes[i] = 0;
            whistleClassified = false;
        }
    }


    /**
     * returns the species list as a JLabel array
     *
     * @return a JLabel array containing the species list
     */
    public JLabel[] getSpecies() {
        return species;
    }


    /**
     * returns the species list as a String array
     *
     * @return a String array containing the species list
     */
    public String[] getSpeciesAsString() {
        String[] speciesListString = new String[species.length];
        for (int i=0; i<species.length; i++) {
            speciesListString[i] = species[i].getText();
        }
        return speciesListString;
    }


    /**
     * Returns the classification counts for the current species list
     *
     * @return  an int array containing the classification counts
     */
    public int[] getSpeciesClassCount() {
        return speciesClassCount;
    }

    /**
     * Returns the classification count for a single species
     *
     * @param speciesNum the index of the desired species count
     * @return the classification count
     */
    public int getClassCount(int speciesNum) {
        return speciesClassCount[speciesNum];
    }


    /**
     * Sets all the species counts.
     *
     * @param speciesClassCount   an int array with the counts for each species.
     *   The length of the array must match the length of the species list
     */
    public void setSpeciesClassCount(int[] speciesClassCount) {
        if (speciesClassCount.length == this.species.length) {
            this.speciesClassCount = speciesClassCount;
            whistleClassified = true;
        } else {
            System.out.println("RSDU Error - passed species tally is the wrong size");
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * Increments a specific species classification count.
     * DO NOT USE THIS METHOD - use incSpeciesCount(int, double, double, boolean) method
     * instead
     *
     * @param speciesToInc the index of the species to increment
     */
    public void incSpeciesCount(int speciesToInc) {
        if (speciesToInc<species.length) {
            speciesClassCount[speciesToInc]++;
            whistleClassified = true;
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }
    
    /**
     * Increment a specific species classification count, and save the starting
     * time.  Note that the starting time is passed in milliseconds, but converted
     * to seconds before saving.
	 * serialVersionUID=15 2014/11/12
     * DO NOT USE THIS METHOD - use incSpeciesCount(int, double, double , boolean) method
     * instead
     * 
     * @param speciesToInc
     * @param startTime the starting time of the whistle, in milliseconds
     */
    public void incSpeciesCount(int speciesToInc, double startTime) {
        if (speciesToInc<species.length) {
        	incSpeciesCount(speciesToInc);
        }
    }
    

    /**
     * Increment a specific species classification count, and save the starting
     * time and duration.  Note that both the starting time and duration are passed
     * in milliseconds, but converted to seconds before saving.
	 * serialVersionUID=22 2015/06/13 added duration and isClick flag, and separated into whistles and clicks
     * 
     * @param speciesToInc
     * @param startTime the starting time of the whistle, in milliseconds
     * @param dur the duration of the whistle, in milliseconds
     * @param isClick a flag indicating whether or not this is a click
     */
    public void incSpeciesCount(int speciesToInc, double startTime, double dur, boolean isClick) {
        if (speciesToInc<species.length) {
        	if (isClick && roccaControl.roccaParameters.areWeRunningAncCalcsOnClicks()) {
        		if (clickList.containsKey(startTime)) {
        			List<Double> list = clickList.get(startTime);
        			list.add(dur);
    	            clickList.put(startTime, list);
        		} else {
        			List<Double> list = new ArrayList<Double>();
        			list.add(dur);
    	            clickList.put(startTime, list);
        		}
        	} else if (!isClick && roccaControl.roccaParameters.areWeRunningAncCalcsOnWhistles()) {
        		if (whistleList.containsKey(startTime)) {
        			List<Double> list = whistleList.get(startTime);
        			list.add(dur);
        			whistleList.put(startTime, list);
        		} else {
        			List<Double> list = new ArrayList<Double>();
        			list.add(dur);
        			whistleList.put(startTime, list);
        		}
        	}
        	incSpeciesCount(speciesToInc);
        }
    }
    

    /**
     * Increments a specific species classification count
     *
     * @param speciesToInc the index of the species to increment
     * DO NOT USE THIS METHOD - use incSpeciesCount(String, double, double, bool) method
     * instead
     */
    public void incSpeciesCount(String speciesToInc) {
        for (int i=0; i<species.length; i++) {
            if (species[i].getText().equalsIgnoreCase(speciesToInc)) {
                speciesClassCount[i]++;
                whistleClassified = true;
            }
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * Increments a specific species classification count and save the starting
     * time.  This is a copy of method incSpeciesCount(String speciesToInc), with
     * the addition of the startTime variable.  Note that the starting time is 
     * passed in milliseconds, but converted to seconds before saving.
	 * serialVersionUID=15 2014/11/12
     * DO NOT USE THIS METHOD - use incSpeciesCount(String, double, double, bool) method
     * instead
     *
     * @param speciesToInc the index of the species to increment
     * @param startTime the starting time of the whistle, in milliseconds
     */
    public void incSpeciesCount(String speciesToInc, double startTime) {
        for (int i=0; i<species.length; i++) {
            if (species[i].getText().equalsIgnoreCase(speciesToInc)) {
                speciesClassCount[i]++;
                whistleClassified = true;
            }
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * Increments a specific species classification count and save the starting
     * time and duration.  This is a copy of method incSpeciesCount(String
     * speciesToInc, double startTime), with the addition of the dur variable.
     * Note that the starting time and duration are passed in milliseconds, 
     * but converted to seconds before saving.
	 * serialVersionUID=22 2015/06/13 added duration, isClick flag and break statement, and separated into whistles and clicks
    *
     * @param speciesToInc the index of the species to increment
     * @param startTime the starting time of the whistle, in milliseconds
     * @param dur the duration of the whistle, in milliseconds
     * @param isClick a flag indicating whether or not this is a click
     */
    public void incSpeciesCount(String speciesToInc, double startTime, double dur, boolean isClick) {
        for (int i=0; i<species.length; i++) {
            if (species[i].getText().equalsIgnoreCase(speciesToInc)) {
                speciesClassCount[i]++;
                whistleClassified = true;
                if (isClick && roccaControl.roccaParameters.runAncCalcs4Clicks) {
            		if (clickList.containsKey(startTime)) {
            			List<Double> list = clickList.get(startTime);
            			list.add(dur);
        	            clickList.put(startTime, list);
            		} else {
            			List<Double> list = new ArrayList<Double>();
            			list.add(dur);
        	            clickList.put(startTime, list);
            		}
            	} else if (!isClick && roccaControl.roccaParameters.runAncCalcs4Whistles) {
            		if (whistleList.containsKey(startTime)) {
            			List<Double> list = whistleList.get(startTime);
            			list.add(dur);
            			whistleList.put(startTime, list);
            		} else {
            			List<Double> list = new ArrayList<Double>();
            			list.add(dur);
            			whistleList.put(startTime, list);
            		}
                }
            	if (clickList.size()!=speciesClassCount[i]) {
            		@SuppressWarnings("unused")
					String message = "Error";
            	}
                break;
            }
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    public String getSightClass() {
        return sightClass;
    }

    /**
     * Sets the sighting classification to the passed string
     *
     * @param sightClass a string containing the class
     */
    public void setSightClass(String sightClass) {
        this.sightClass = sightClass;

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * classify the sighting, based on the tree votes.  Divide the total number
     * of tree votes for each species by the overall whistle count, to get an
     * average for that species.  The sighting threshold is passed, and used
     * to evaluate the vote.  If the vote is less than the threshold, the
     * class is set to Ambiguous.
     *
     * @param threshold the threshold to compare the tree votes against
     */
    public void classifySighting(int threshold) {
    	
    	// if no classifier models are loaded, just return a "No Classifier"
    	if (!roccaControl.getRoccaProcess().isClassifierLoaded()) {
    		sightClass = RoccaClassifier.NOCLASS;
    		return;
    	}
    	
        double maxVote = 0.0;
        if (whistleClassified) {
            int whistleCount = countWhistles();
            maxVote = speciesTreeVotes[1]/whistleCount;
            sightClass = species[1].getText();
            for (int i=2; i<speciesTreeVotes.length; i++) {
                if (speciesTreeVotes[i]/whistleCount>maxVote) {
                    maxVote = speciesTreeVotes[i]/whistleCount;
                    sightClass = species[i].getText();
                }
            }
        }
        if (maxVote < (((double) threshold)/100)) {
            sightClass = RoccaClassifier.AMBIG;
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    /**
     * classify the sighting, based on the tree votes.  Divide the total number
     * of tree votes for each species by the overall whistle count, to get an
     * average for that species (note that Ambig is not included in this calc).
     *
     * @return The tree vote of the classified species
     */
    public double classifySighting() {
        double maxVote = 0.0;
        if (whistleClassified) {
            int whistleCount = countWhistles();
            maxVote = speciesTreeVotes[1]/whistleCount;
            sightClass = species[1].getText();
            for (int i=2; i<speciesTreeVotes.length; i++) {
                if (speciesTreeVotes[i]/whistleCount>maxVote) {
                    maxVote = speciesTreeVotes[i]/whistleCount;
                    sightClass = species[i].getText();
                }
            }
        }

        /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }

        return maxVote;
    }

    public int countWhistles() {
        int sum = 0;
        for (int i=0; i<speciesClassCount.length; i++) {
            sum+=speciesClassCount[i];
        }
        return sum;
    }

    /**
     * returns the array of tree votes
     * @return a double array of tree votes
     */
    public double[] getSpeciesTreeVotes() {
        return speciesTreeVotes;
    }

    /**
     * Adds the passed array of tree votes to the current array.  Note that
     * the passed array may not include an 'Ambig' class, so the size will be
     * one smaller than the current array.  If that is the case, make sure to
     * skip the first position (the Ambig position) when adding.
     * 
     * @param newTreeVotes a double array of tree votes
     */
    public void addSpeciesTreeVotes(double[] newTreeVotes) {
        if (newTreeVotes.length == speciesTreeVotes.length-1) {
            for (int i=0; i<newTreeVotes.length; i++) {
                speciesTreeVotes[i+1]+=newTreeVotes[i];
            }
        } else if (newTreeVotes.length == speciesTreeVotes.length) {
            for (int i=0; i<newTreeVotes.length; i++) {
                speciesTreeVotes[i]+=newTreeVotes[i];
            }
        }

    /* If this data unit has already been added to a data block (that is,
         * if it has a parent block), notify the observers that this data unit
         * has been updated (to log it to the database, if necessary).
         * If the data unit has not been added to a data block (if the parent
         * block is null) then don't notify the observers, because they will
         * be notified once it's properly added
         */
        if (this.getParentDataBlock() != null) {
            this.getParentDataBlock().updatePamData(this, getTimeMilliseconds());
        }
    }

    public boolean isSightingSaved() {
        return sightingSaved;
    }

    public void setSightingSaved(boolean sightingSaved) {
        this.sightingSaved = sightingSaved;
    }

    /**
     * checks to see if any whistles have been classified yet
     * @return boolean indicating whether or not a whistle has been classified
     * yet
     */
    public boolean isWhistleClassified() {
        return whistleClassified;
    }

    /**
     * Create a single string containing the species, using
     * the '-' character separating the values
     */
    public String createSpList() {
        String spList = "(";
        for (int i=0; i<species.length; i++) {
            spList = spList + species[i].getText() + "-";
        }
        spList = spList + ")";
        return spList;
    }

    /**
     * Reads the species from the passed string and sets the species list.  The
     * string starts with '(' and ends with '-)', so drop the first character
     * and last two characters.  Each species name is separated from the
     * other by a '-', so parse on that delimiter.
     *
     * @param spListSingleString the String to parse.  See method description
     * for the String format required
     */
    public void parseAndSetSpList(String spListSingleString) {
        String tempSpecies = spListSingleString.trim();
        String tempSpeciesSub = tempSpecies.substring(1,tempSpecies.length()-2);
        String[] speciesArray = tempSpeciesSub.split("[-]");
        this.setSpecies(speciesArray);
    }

    /**
     * Create a single string containing the species class counts, using
     * the '-' character separating the values
     */
    public String createClassCountList() {
        String classCountList = "(";
        for (int i=0; i<speciesClassCount.length; i++) {
            classCountList = classCountList + speciesClassCount[i] + "-";
        }
        classCountList = classCountList + ")";
        return classCountList;
    }

    /**
     * Reads the class counts from the passed string and sets the tally.  The
     * string starts with '(' and ends with '-)', so drop the first character
     * and last two characters.  Each tally count is separated from the
     * other by a '-', so parse on that delimiter.
     *
     * @param classCountListSingleString the String to parse.  See method
     * description for the String format required
     */
    public void parseAndSetClassCountList(String classCountListSingleString) {
        String tempCount = classCountListSingleString.trim();
        String tempCountSub = tempCount.substring(1,tempCount.length()-2);
        String[] tallyCountAsString = tempCountSub.split("[-]");
        int[] tallyCount = new int[tallyCountAsString.length];
        for (int i=0; i<tallyCountAsString.length; i++) {
            tallyCount[i]=Integer.parseInt(tallyCountAsString[i].trim());
        }
        this.setSpeciesClassCount(tallyCount);
    }

    /**
     * Create a single string containing the tree votes, using
     * the '-' character separating the values.  Note that the votes are rounded
     * to 5 decimal places to stop the string from getting too long.
     */
    public String createVoteList() {
        String voteList = "(";
        DecimalFormat df = new DecimalFormat("#.#####");
        double roundedVal = 0.0;
        for (int i=0; i<speciesTreeVotes.length; i++) {
            roundedVal = Double.valueOf(df.format(speciesTreeVotes[i]));
            voteList = voteList + roundedVal + "-";
        }
        voteList = voteList + ")";
        return voteList;
    }

    /**
     * Reads the class counts from the passed string and sets the tally.  The
     * string starts with '(' and ends with '-)', so drop the first character
     * and last two characters.  Each tally count is separated from the
     * other by a '-', so parse on that delimiter.
     *
     * @param voteListSingleString the String to parse.  See method
     * description for the String format required
     */
    public void parseAndSetVoteList(String voteListSingleString) {
        String tempVotes = voteListSingleString.trim();
        String tempVotesSub = tempVotes.substring(1,tempVotes.length()-2);
        String[] votesAsString = tempVotesSub.split("[-]");
        double[] votes = new double[votesAsString.length];
        for (int i=0; i<votesAsString.length; i++) {
            votes[i]=Double.parseDouble(votesAsString[i].trim());
        }
        this.addSpeciesTreeVotes(votes);
    }
    
    /**
     * Calculate sighting parameters based on time data.  Calculations are returned
     * in the double array in this order:
     * 		timeParams[0] = duration of whistles in sighting (end of the last whistle - start of the first whistle) [ms]
     * 		timeParams[1] = minimum time between whistles [ms]
     * 		timeParams[2] = maximum time between whistles [ms]
     * 		timeParams[3] = average time between whistles [ms]
     * 		timeParams[4] = whistles/second
     * 
     * serialVersionUID=22 2015/06/23 separated whistles and clicks, and added timeParams[5]-[9]:
     * 		timeParams[5] = whistle density (sum of durations / encounter length)
     * 		timeParams[6] = start time of first whistle [ms]
     * 		timeParams[7] = end time of last whistle [ms]
     * 		timeParams[8] = whistle overlap (sum of overlap / encounter length)
     * 		timeParams[9] = number of whistles
     * 
     * 
     */
    public double[] calculateWhistleTimeParams() {
    	double[] timeParams = {0, Double.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, whistleList.size()};

    	// if we only have 1 whistle, exit immediately
    	if (whistleList.size()==1 && whistleList.get(whistleList.firstKey()).size()==1) {
    		return timeParams;
    	}
    	
    	// calculate params
    	double diff;
    	double sumDiff=0;
    	double sumDur=0;
    	double lastStart=0;
    	double clickEnd=0;
    	double numClicks=0;
    	
    	// get the starting duration.  Need to loop over the list in case
    	// there is more than 1 entry
    	List<Double> list = whistleList.get(whistleList.firstKey());
    	for (Double duration : list) {
    		sumDur += duration;
    		numClicks++;
    	}

    	// loop through the treemap
    	for(Map.Entry<Double,List<Double>> entry : whistleList.entrySet()) {
    		if (entry.getKey()==whistleList.firstKey()) {
    			lastStart=entry.getKey();
    			list = entry.getValue();
    			clickEnd=entry.getKey()+Collections.max(list);
    		} else {
    			diff=entry.getKey()-lastStart;
	    		sumDiff += diff;
	    		if (diff<timeParams[1]) {
	    			timeParams[1] = diff;		// min time between whistles
	    		}
	    		if (diff>timeParams[2]) {
	    			timeParams[2] = diff;		// max time between whistles
	    		}
	    		
    			list = entry.getValue();
    	    	for (Double duration : list) {
    	    		sumDur += duration;
    	    		numClicks++;
    	    	}
	    		lastStart=entry.getKey();
	    		if (entry.getKey()+Collections.max(list)>clickEnd) {
	    			clickEnd = entry.getKey()+Collections.max(list);
	    		}
    		}
    	}
    	timeParams[0]=clickEnd-whistleList.firstKey();
    	timeParams[3]=sumDiff/(numClicks-1);	// ave time between whistles
    	timeParams[4]=numClicks/timeParams[0]*1000.0;	// whistles/second
    	timeParams[5]=sumDur/timeParams[0];					// whistle density
    	timeParams[6]=whistleList.firstKey();				// start of first whistle
    	timeParams[7]=clickEnd;	// end of last whistle
    	timeParams[9]=numClicks;
    	
    	// calculate whistle overlap
    	// Loop through the list, one item at a time
    	Double overlap=0.0;
    	for (Map.Entry<Double,List<Double>> curWhistle : whistleList.entrySet()) {
    		
    		// load the list of durations from the current whistle
    		list = curWhistle.getValue();
    	    
    	    // loop through the list of durations for this start time
    	    for (int i=0; i<list.size(); i++) {
    	    	
    	    	// first compare against the other durations with the same start time
    	    	for (int j=i+1; j<list.size(); j++) {
    	    		overlap+=Math.min(list.get(i), list.get(j));
    	    	}

    	    	// figure out the end of the current whistle
        	    Double curEnd = curWhistle.getKey() + list.get(i);
        	    
    	    	// now compare against the other whistles.  Find the next whistle in the list.  Exit if there isn't one
        	    Map.Entry<Double,List<Double>> nextWhistle = whistleList.higherEntry(curWhistle.getKey());
        	    while (nextWhistle != null) {
    	    	    // if the next whistle starts before the current one ends, calculate the
    	    	    // amount of overlap.  If the next whistle is completely contained within
    	    	    // the current, log the entire duration.  Otherwise, only log what overlaps.
        	    	// As with the current whistle, we need to take into account the possibility
        	    	// that there are multiple durations for the next start time, and iterate through them
    	    	    if (nextWhistle.getKey() < curEnd) {
    	    	    	List<Double> nextList = nextWhistle.getValue();
    	    	    	for (Double nextDuration : nextList) {
    	    	    		if (nextWhistle.getKey()+nextDuration<=curEnd) { 
        	    	    		overlap+=nextDuration;
        	    	    	} else {
        	    	    		overlap+=curEnd-nextWhistle.getKey();
        	    	    	}
    	    	    	}
    	    	    	
    	    	    // if the next whistle starts after the end of the current whistle, exit the loop right now
    	    	    } else {
    	    	    	break;
    	    	    }
    	    	    	    	    
    	    	    // go to the next whistle
    	    	    nextWhistle = whistleList.higherEntry(nextWhistle.getKey());
        	    }
    	    }
       	}
    	timeParams[8]=overlap/timeParams[0];
    	    
    	// return the array
    	return timeParams;
    }

    /**
     * serialVersionUID=22 2015/06/23 added function to calculate time params on clicks
     * Calculate sighting parameters based on time data.  Calculations are returned
     * in the double array in this order:
     * 		timeParams[0] = duration of clicks in sighting (end of last click - start of first click)
     * 		timeParams[1] = minimum time between clicks
     * 		timeParams[2] = maximum time between clicks
     * 		timeParams[3] = average time between clicks
     * 		timeParams[4] = clicks/second
     * 		timeParams[5] = click density (sum of durations / encounter length)
     * 		timeParams[6] = start time of first click
     * 		timeParams[7] = end time of last click
     * 		timeParams[8] = average click overlap (sum of overlap / encounter length)
     * 		timeParams[9] = number of clicks
     * 
     * 
     */
    public double[] calculateClickTimeParams() {
    	double[] timeParams = {0, Double.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, clickList.size()};
    	
    	// if we only have 1 click, exit immediately
    	if (clickList.size()==1 && clickList.get(clickList.firstKey()).size()==1) {
    		return timeParams;
    	}
    	
    	// calculate other params
    	double diff;
    	double sumDiff=0;
    	double sumDur=0;
    	double lastStart=0;
    	double clickEnd=0;
    	double numClicks=0;
    	
    	// get the starting duration.  Need to loop over the list in case
    	// there is more than 1 entry
    	List<Double> list = clickList.get(clickList.firstKey());
    	for (Double duration : list) {
    		sumDur += duration;
    		numClicks++;
    	}

    	// loop through the treemap
    	for(Map.Entry<Double, List<Double>> entry : clickList.entrySet()) {
    		if (entry.getKey()==clickList.firstKey()) {
    			lastStart=entry.getKey();
    			list = entry.getValue();
    			clickEnd=entry.getKey()+Collections.max(list);
    		} else {
    			diff=entry.getKey()-lastStart;
	    		sumDiff += diff;
	    		if (diff<timeParams[1]) {
	    			timeParams[1] = diff;		// min time between whistles
	    		}
	    		if (diff>timeParams[2]) {
	    			timeParams[2] = diff;		// max time between whistles
	    		}
	    		
    			list = entry.getValue();
    	    	for (Double duration : list) {
    	    		sumDur += duration;
    	    		numClicks++;
    	    	}
	    		lastStart=entry.getKey();
	    		if (entry.getKey()+Collections.max(list)>clickEnd) {
	    			clickEnd = entry.getKey()+Collections.max(list);
	    		}
    		}
    	}
    	timeParams[0]=clickEnd-clickList.firstKey();
    	timeParams[3]=sumDiff/(numClicks-1);	// ave time between whistles
    	timeParams[4]=numClicks/timeParams[0]*1000.0;
    	timeParams[5]=sumDur/timeParams[0];
    	timeParams[6]=clickList.firstKey();
    	timeParams[7]=clickEnd;
    	timeParams[9]=numClicks;
    	
    	// calculate whistle overlap
    	// Loop through the list, one item at a time
    	Double overlap=0.0;
    	for (Map.Entry<Double,List<Double>> curWhistle : clickList.entrySet()) {
    		
    		// load the list of durations from the current whistle
    		list = curWhistle.getValue();
    	    
    	    // loop through the list of durations for this start time
    	    for (int i=0; i<list.size(); i++) {
    	    	
    	    	// first compare against the other durations with the same start time
    	    	for (int j=i+1; j<list.size(); j++) {
    	    		overlap+=Math.min(list.get(i), list.get(j));
    	    	}

    	    	// figure out the end of the current whistle
        	    Double curEnd = curWhistle.getKey() + list.get(i);
        	    
    	    	// now compare against the other whistles.  Find the next whistle in the list.  Exit if there isn't one
        	    Map.Entry<Double,List<Double>> nextWhistle = clickList.higherEntry(curWhistle.getKey());
        	    while (nextWhistle != null) {
    	    	    // if the next whistle starts before the current one ends, calculate the
    	    	    // amount of overlap.  If the next whistle is completely contained within
    	    	    // the current, log the entire duration.  Otherwise, only log what overlaps.
        	    	// As with the current whistle, we need to take into account the possibility
        	    	// that there are multiple durations for the next start time, and iterate through them
    	    	    if (nextWhistle.getKey() < curEnd) {
    	    	    	List<Double> nextList = nextWhistle.getValue();
    	    	    	for (Double nextDuration : nextList) {
    	    	    		if (nextWhistle.getKey()+nextDuration<=curEnd) { 
        	    	    		overlap+=nextDuration;
        	    	    	} else {
        	    	    		overlap+=curEnd-nextWhistle.getKey();
        	    	    	}
    	    	    	}
    	    	    	
    	    	    // if the next whistle starts after the end of the current whistle, exit the loop right now
    	    	    } else {
    	    	    	break;
    	    	    }
    	    	    	    	    
    	    	    // go to the next whistle
    	    	    nextWhistle = clickList.higherEntry(nextWhistle.getKey());
        	    }
    	    }
       	}
    	timeParams[8]=overlap/timeParams[0];
    	    
    	// return the array
    	return timeParams;
    }

	public int getNumWhistles() {
		return whistleList.size();
	}
	
	public int getNumClicks() {
		return clickList.size();
	}

	public String getDetectionStats() {
		return detectionStats;
	}

	public void setDetectionStats(String detectionStats) {
		this.detectionStats = detectionStats;
	}

	public boolean isSightingDataLost() {
		return sightingDataLost;
	}

	public void setSightingDataLost(boolean sightingDataLost) {
		this.sightingDataLost = sightingDataLost;
	}

	public boolean wasNewDataAdded() {
		return newDataAdded;
	}

	public void setNewDataAdded(boolean newDataAdded) {
		this.newDataAdded = newDataAdded;
	}
    
	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
    

}
