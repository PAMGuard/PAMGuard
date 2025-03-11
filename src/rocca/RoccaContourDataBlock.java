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

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import Acquisition.AcquisitionProcess;
import Acquisition.FileInputSystem;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.basicSweep.ZeroCrossingStats;

/**
 *
 * @author Michael Oswald
 */
public class RoccaContourDataBlock extends PamDataBlock<RoccaContourDataUnit> {

    /** Enum class with the names of the contour parameters <p>
     * Note that these names MUST match the attribute names used to create the
     * classifier (other than the case - by convention, java constants are
     * denoted by uppercase characters).
     */
//    public static enum ParamIndx {
//        FREQMAX,
//        FREQMIN,
//        DURATION,
//        FREQBEG,
//        FREQEND,
//        FREQRANGE,
//        DCMEAN,
//        DCSTDDEV,
//        FREQMEAN,
//        FREQSTDDEV,
//        FREQMEDIAN,
//        FREQCENTER,
//        FREQRELBW,
//        FREQMAXMINRATIO,
//        FREQBEGENDRATIO,
//        FREQQUARTER1,
//        FREQQUARTER2,
//        FREQQUARTER3,
//        FREQSPREAD,
////        DCQUARTERMEAN,
//        DCQUARTER1MEAN,
//        DCQUARTER2MEAN,
//        DCQUARTER3MEAN,
//        DCQUARTER4MEAN,
//        FREQCOFM,
//        FREQSTEPUP,
//        FREQSTEPDOWN,
//        FREQNUMSTEPS,
//        FREQSLOPEMEAN,
//        FREQABSSLOPEMEAN,
//        FREQPOSSLOPEMEAN,
//        FREQNEGSLOPEMEAN,
//        FREQSLOPERATIO,
//        FREQBEGSWEEP,
//        FREQBEGUP,
//        FREQBEGDWN,
//        FREQENDSWEEP,
//        FREQENDUP,
//        FREQENDDWN,
//        NUMSWEEPSUPDWN,
//        NUMSWEEPSDWNUP,
//        NUMSWEEPSUPFLAT,
//        NUMSWEEPSDWNFLAT,
//        NUMSWEEPSFLATUP,
//        NUMSWEEPSFLATDWN,
//        FREQSWEEPUPPERCENT,
//        FREQSWEEPDWNPERCENT,
//        FREQSWEEPFLATPERCENT,
//        NUMINFLECTIONS,
//        INFLMAXDELTA,
//        INFLMINDELTA,
//        INFLMAXMINDELTA,
//        INFLMEANDELTA,
//        INFLSTDDEVDELTA,
//        INFLMEDIANDELTA
//    }

    /**
     * A field mapping the sweep direction to the nominal index used to train
     * the classifier
     * <p>
     * The WEKA random forest classifier code stores all attributes as double
     * values.  For nominal attributes, WEKA assumes that the stored value refers
     * to the INDEX of the corresponding value.  For example, the FREQBEGSWEEP
     * attribute can be down, flat or up.  The training dataset uses the values
     * "-1" to indicate down, "0" to indicate flat, and "1" to indicate up.  When
     * the classifier is created, it stores these nominal values in an array of
     * length 3, with array index 0 being -1 (down), index 1 being 0 (flat) and
     * index 2 being 1 (up).  When an instance gets passed to the classifier
     * for classification, it assumes that the value stored in the attribute
     * refers to the index position (0, 1, or 2), NOT the actual nominal value
     * (-1, 0 or 1).  So this enum field is used to map the nominal down-flat-up
     * values to the corresponding array indices used to train the classifier.
     * <p>
     * IMPORTANT NOTE:
     * <p>
     * If the order or potential values of the nominal attribute ever change,
     * this enum MUST CHANGE TO MATCH.  For example, if down-flat-up are ever
     * reordered in the training set to flat-down-up, the SweepIndx enum needs
     * to be changed to flat=0, down=1 and up=2.  Similarly, if 'flat' is dropped
     * and the training set only uses down-up, SweepIndx needs to be changed
     * to flat=0 and up=1.
     */
    public enum SweepIndx {
        DOWN (0),
        FLAT (1),
        UP (2);

        private int nominalIndx;

        private SweepIndx(int i) {
            nominalIndx = i;
        }

        public int getIndx() {
            return nominalIndx;
        }
    }

    /**
     * A field mapping the sweep direction to the nominal index used to train
     * the classifier
     * <p>
     * The WEKA random forest classifier code stores all attributes as double
     * values.  For nominal attributes, WEKA assumes that the stored value refers
     * to the INDEX of the corresponding value.  For example, the FREQBEGUP
     * attribute can be true (yes, the slope is positive) or false (no, the
     * slope is not positive).  The training dataset uses the values "0" to
     * indicate false, and "1" to indicate true.  When the classifier is
     * created, it stores these nominal values in an array of length 2, with
     * array index 0 being 0 (false), and index 1 being 1 (true).  When an
     * instance gets passed to the classifier for classification, it assumes
     * that the value stored in the attribute refers to the index position
     * (0 or 1), NOT the actual nominal value (0/false or 1/true).  So this
     * enum field is used to map the nominal false-true values to the
     * corresponding array indices used to train the classifier.
     * <p>
     * IMPORTANT NOTE:
     * <p>
     * Right now, this doesn't matter because the potential values 0 and 1
     * coincidentally match the array indices 0 and 1.  But if the order or
     * potential values of the nominal attribute ever change, this enum MUST
     * CHANGE TO MATCH.  For example, if false-true are ever reordered in the
     * training set to true-false, the SweepIndx enum needs to be changed to
     * true=0 and false=1.  Similarly, if 'maybe' is added to the list as
     * false-maybe-true (using the values -1, 0, and 1 respectively), SweepIndx
     * needs to be changed to false=0, maybe=1 and true=2.
     */
    public enum PosSweepIndx {
        FALSE (0),
        TRUE (1);

        private int nominalIndx;

        private PosSweepIndx(int i) {
            nominalIndx = i;
        }

        public int getIndx() {
            return nominalIndx;
        }
    }

//    /** EnumMap linking the contour parameter to its value */
//    EnumMap<ParamIndx, Double> contour = new EnumMap<ParamIndx, Double>(ParamIndx.class);
//
//    /** ArrayList containing the EnumMap key names */
//    private ArrayList<String> keyNames = new ArrayList<String>(contour.size());
//
    /** reference to the roccaProcess object */
    RoccaProcess roccaProcess;
    
    /** flag indicating whether or not the statistics have been run */
    boolean statsRun = false;

    /** the species the block has been classified as */
    String classifiedAs = null;
    
    /** an array containing the names of the species */
    String[] spNames;

    /** an array containing the tree votes for the current classification */
    double[] treeVotes;

    /** the first data unit containing the whistle contour */
    int firstUnitWithWhistle;

    /** the last data unit containing the whistle contour */
    int lastUnitWithWhistle;

    /** the RoccaContourStats object, for saving the statistics */
    private RoccaContourStats rcs;

    /** the field in the RoccaContourStats object which contains all the stats measures */
    private EnumMap<RoccaContourStats.ParamIndx, Double> contourStats;

    /** an arraylist containing the names of the parameters (makes searching
     * easier in RoccaClassifier)
     */
    ArrayList<String> keyNames;
    
    /** the PamRawDataBlock containing the raw data corresponding to this Contour.  Note that
     * this is currently only used when the whistle comes from by the Whistle & Moan Detector
     */
//    PamRawDataBlock prdb = null;
    
    /**
     * The raw acoustic data making up this contour.  Used to save a wav clip to the output folder
     */
    private double[][] wavInfo = null;
    
    /**
     * when the source is the click detector, this is a link to the source data unit
     */
    ClickDetection clickDetection;
    
    /**
     * when the source is the click detector, the user also defines a noise measurement source
     */
    ClickDetection clickNoise;
    
    /**
     * zero crossing data, calculated in the sweep classifier
     */
    ZeroCrossingStats[] zcs;
    
    /**
     * time stamp of the first data unit with a whistle.  Originally startTime
     * was a local variable.  Units: milliseconds
	 * serialVersionUID=15 2014/11/12
     */
    double startTime;
    
    /**
     * duration of whistle, in milliseconds.  To pass to the RoccaSightingDataUnit and allow 
     * extra variables to be calculated for encounter
	 * serialVersionUID=22 2015/06/13
     */
    double duration;
    
    /**
     * Boolean indicating whether this is a whistle (value=true) or a click (value=false)
     * serialVersionUID=24 2016/08/10 added
     */
    private boolean thisIsAWhistle;
    
    /**
     * Latitude
     * serialVersionUID=24 2016/08/10 added
     */
    private double latitude;
    
    /**
     * Longitude
     * serialVersionUID=24 2016/08/10 added
     */
    private double longitude;
    

    /**
     * Constructor.  Set the natural lifetime to the maximum integer value
     * possible so that the data is never deleted unless the
     */
	public RoccaContourDataBlock(PamProcess parentProcess, int channelMap) {
		super(RoccaContourDataUnit.class, "Rocca Contour", parentProcess, channelMap);
        this.roccaProcess = (RoccaProcess) parentProcess;
        this.setNaturalLifetime(Integer.MAX_VALUE/1000);
        int[] spClassCount = roccaProcess.roccaControl.roccaSidePanel.getSpeciesClassCount();
        if (spClassCount != null) {
        	this.treeVotes = new double[spClassCount.length];
        }
        rcs = new RoccaContourStats();
        contourStats = rcs.getContour();
        keyNames = rcs.getKeyNames();
        
        // add the latitude and longitude.
        if (roccaProcess.roccaControl.roccaParameters.weAreUsingGPS()) {
        	try {
        		latitude = roccaProcess.roccaControl.getRoccaProcess().getGpsSourceData().getLastUnit().getGpsData().getLatitude();
        		longitude = roccaProcess.roccaControl.getRoccaProcess().getGpsSourceData().getLastUnit().getGpsData().getLongitude();
        	} catch (Exception e) {
        		latitude=0;
        		longitude=0;
        	}
        } else {
    		latitude=0;
    		longitude=0;
        }
    }
	
//	/**
//	 * Special constructor.  Only used by RoccaFixParams file, which runs outside of Pamguard and
//	 * therefore has no RoccaProcess to reference
//	 */
//	public RoccaContourDataBlock() {
//		super();
//        rcs = new RoccaContourStats();
//        contourStats = rcs.getContour();
//        keyNames = rcs.getKeyNames();		
//	}

    /** Once the Datablock is filled, this routine will calculate the
     * statistics of the contour.
     */ 
    public void calculateStatistics() {
        
        /** Initialize all parameters in the map except FREQMIN to 0.0; set FREQMIN
         * to 10000.0.  Also save the key names into an arrayList to make it easier
         * to search when comparing to the classifier's required attributes.
         */
    	rcs.initialize(contourStats);
//        for (ParamIndx p : ParamIndx.values()) {
//            contour.put(p, 0.0);
//            keyNames.add(p.toString());
//        }
//        contour.put(ParamIndx.FREQMIN, 10000.0);
//


        // define variables
        RoccaContourDataUnit rcdu = null;
        RoccaContourDataUnit rcduPrev = null;
        int totalNumPoints = this.getUnitsCount();
        double dcQuarterSum = 0.0;
        int dcQuarterCount = 0;
        double stepSens = 11;           // sensitivity to contour step size (%)
        double slopeSum = 0.0;
        double slopeAbsSum = 0.0;
        double slopePosSum = 0.0;
        int slopePosCount = 0;
        double slopeNegSum = 0.0;
        int slopeNegCount = 0;
        double slopeBegAve = 0;
        double slopeEndAve = 0;
        int sweepUpCount = 0;
        int sweepDwnCount = 0;
        int sweepFlatCount = 0;
        double inflPrevTime = 0.0;

        /* cycle through the datablock, and find the start and end units of the
         * whistle (units not containing a whistle will have a peak frequency
         * set to RoccaContour.OUTOFRANGE
         */
        double freqValue = 0.0;
        for (firstUnitWithWhistle = 0;
             firstUnitWithWhistle < totalNumPoints;
             firstUnitWithWhistle++) {
            freqValue = this.getDataUnit
                    (firstUnitWithWhistle, REFERENCE_CURRENT).getPeakFreq();
            if (freqValue != RoccaContour.OUTOFRANGE) {
                break;
            }
        }
        for (lastUnitWithWhistle = totalNumPoints-1;
             lastUnitWithWhistle > 0;
             lastUnitWithWhistle--) {
            freqValue = this.getDataUnit
                    (lastUnitWithWhistle, REFERENCE_CURRENT).getPeakFreq();
            if (freqValue != RoccaContour.OUTOFRANGE) {
                break;
            }
        }
        int numPoints = lastUnitWithWhistle - firstUnitWithWhistle + 1;
        double[] freqArray = new double[numPoints];
        double[] dcArray = new double[numPoints];
        double[] inflDeltaTimeArray = new double[numPoints];
        double[] inflTimeArray = new double[numPoints];
        int lastSweep = 0;

        // get the beginning frequency and time
        // serialVersionUID=15 2014/11/12 change startTime from local
        // variable to class variable
        contourStats.put(RoccaContourStats.ParamIndx.FREQBEG,
                this.getDataUnit(firstUnitWithWhistle, REFERENCE_CURRENT).getPeakFreq());
        startTime = this.getDataUnit(firstUnitWithWhistle, REFERENCE_CURRENT).getTime();

        /* Cycle through the contour points one at a time.  Ideally the counter
         * should go from firstUnitWithWhistle to lastUnitWithWhistle, but this
         * makes the array calculations a lot more complicated.  So instead,
         * the counter will start at 0 and the firstUnitWithWhistle parameter
         * will be used to retrieve the correct data unit
         */
        for (int i = 0; i < numPoints; i++) {
            rcdu = this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT);
            freqArray[i]=rcdu.getPeakFreq();
            dcArray[i]=rcdu.getDutyCycle();

            // add current duty cycle to quarterly tally, and update the counter
            dcQuarterSum += rcdu.getDutyCycle();
            dcQuarterCount++;

            // if we're a quarter of the way through the list, calculate
            // the mean duty cycle in the first quarter.  Cast numPoints to a double first,
            // then round off back to an integer
            if ( i == Math.floor( ((double) numPoints)/4) ) {
                contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER1MEAN, dcQuarterSum / dcQuarterCount);
                dcQuarterSum = 0.0;
                dcQuarterCount = 0;
            }

            // if we're halfway through the list, calculate
            // the mean duty cycle in the second quarter.  Cast numPoints to a double first,
            // then round off back to an integer
            if ( i == Math.floor( ((double) numPoints)/2) ) {
                contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER2MEAN, dcQuarterSum / dcQuarterCount);
                dcQuarterSum = 0.0;
                dcQuarterCount = 0;
            }

            // if we're three quarters of the way through the list, calculate
            // the mean duty cycle in the third quarter.  Cast numPoints to a double first,
            // then round off back to an integer
            if ( i == Math.floor( 3*((double) numPoints)/4) ) {
                contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER3MEAN, dcQuarterSum / dcQuarterCount);
                dcQuarterSum = 0.0;
                dcQuarterCount = 0;
            }

            // calculate sweep, slope, step and inflection parameters
            // Slope and Step: current freq compared to prev
            // Sweep: this should be done by comparing the current freq to the prev
            // and next freqs.  BUT since this loop only works on the current
            // contour point (and not the next) we'll compare the current to
            // the previous TWO and then set the PREVIOUS data unit sweep
            // Inflection: based on the sweep parameter
            if ( i==0 ) {
                rcdu.setSweep(0);
                rcdu.setStep(0);
                rcdu.setSlope(0);

            } else {
                // evaluate slope
            	double thisTime = (double) rcdu.getTime();
            	double prevTime = (double) rcduPrev.getTime();
            	Double timeDiff = (thisTime-prevTime)/1000;		// time difference in seconds, not milliseconds
            	
            	// quick check of the time difference.  Sometimes the Whistle&Moan Detector will send consecutive data units with the
            	// same time.  In such a case, just set the slope to 0 (better than Infinite or NaN)
            	if (timeDiff.isNaN() || timeDiff == 0 ) {
            		rcdu.setSlope(0.);
            	} else {
//                rcdu.setSlope((freqArray[i]-freqArray[i-1])/(rcdu.getMeanTime()-rcduPrev.getMeanTime()));
            		rcdu.setSlope((freqArray[i]-freqArray[i-1])/(timeDiff));
            	}
                Double testSlope = rcdu.getSlope();  
                if ( testSlope.isInfinite() || testSlope.isNaN()) {
                	System.out.println("*** Problem calculating slope: " + rcdu.getSlope() + " ***");
                }
                slopeSum += rcdu.getSlope();
                slopeAbsSum += Math.abs(rcdu.getSlope());
                if (rcdu.getSlope() > 0.0) {
                    slopePosSum += rcdu.getSlope();
                    slopePosCount++;
                } else if (rcdu.getSlope() < 0.0) {
                    slopeNegSum += rcdu.getSlope();
                    slopeNegCount++;
                }

                /* evaluate step
                 * update 2012-11-06 - added check for previous step value (rcduPrev.getStep).  Only
                 * count the current jump as a step if the previous one was flat.  Without this, if a
                 * step covers 2 or 3 time bins, Rocca will count it as multiple steps instead
                 * of just one long one.
                 */
                if (rcduPrev.getStep()==0 && 
                		(freqArray[i] >= freqArray[i-1]*(1+stepSens/100))) {
                    rcdu.setStep(1);
                    double count = contourStats.get(RoccaContourStats.ParamIndx.FREQSTEPUP);
                    contourStats.put(RoccaContourStats.ParamIndx.FREQSTEPUP, ++count);
                } else if (rcduPrev.getStep()==0 && 
                		(freqArray[i] <= freqArray[i-1]*(1-stepSens/100))) {
                    rcdu.setStep(2);
                    double count = contourStats.get(RoccaContourStats.ParamIndx.FREQSTEPDOWN);
                    contourStats.put(RoccaContourStats.ParamIndx.FREQSTEPDOWN, ++count);
                } else {
                    rcdu.setStep(0);
                }

                /* evaluate sweep (we need to be past the first 2 array cells)
                 * Not sure if this calculation is entirely correct, but was written to match Matlab Rocca.
                 * There's a problem with this set of 3 'if' statements.  The first 'if'
                 * counts up-up, up-flat, flat-up, and flat-flat as an upsweep.  The second 'if' 
                 * counts down-down, flat-down, down-flat and flat-flat as a downsweep.
                 * The third 'if' only counts 'flat-flat' and overrides the other two ifs.
                 * BUT if the contour is up-down or down-up, the sweep variable will not be
                 * set by these if statements.  In the Matlab version, it would have been saved as it's
                 * previous value.  To emulate that here, we keep a 'lastSweep' parameter equal to the previous
                 * sweep result and use it as a default before the calcs occur
                 */
                rcduPrev.setSweep(lastSweep);	// default to the last sweep value, in case the current calc fails
                if ( i > 1 ) {
                    if (freqArray[i-2] <= freqArray[i-1] &          // upsweep
                        freqArray[i-1] <= freqArray[i] ) {
                        rcduPrev.setSweep(SweepIndx.UP.getIndx());
                        sweepUpCount++;
                        lastSweep=SweepIndx.UP.getIndx();
                    }
                    
                    if  (freqArray[i-2] >= freqArray[i-1] &   // downsweep
                        freqArray[i-1] >= freqArray[i] ) {
                        rcduPrev.setSweep(SweepIndx.DOWN.getIndx());
                        sweepDwnCount++;
                        lastSweep=SweepIndx.DOWN.getIndx();
                    } 
                    
                    if  (freqArray[i-2] == freqArray[i-1] &   // horizontal
                            freqArray[i-1] == freqArray[i] ) {
                        rcduPrev.setSweep(SweepIndx.FLAT.getIndx());
                        sweepFlatCount++;
                        lastSweep=SweepIndx.FLAT.getIndx();

// The following was removed in order to better match the original Matlab version.  It is now in it's own loop, after this one
//                        
//                        // test for inflection points
//                        if (freqArray[i-2] < freqArray[i-1] &          // up-down
//                            freqArray[i-1] > freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN, ++count);
//                            count = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMINFLECTIONS, ++count);
//                            int numInfl = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS).intValue();
//                            if (numInfl==1) {
//                                inflPrevTime = rcduPrev.getTime();
//                            } else {
//                                inflDeltaTimeArray[numInfl-1]=(rcduPrev.getTime()-inflPrevTime)/1000;	// convert the delta time to seconds
//                                inflPrevTime = rcduPrev.getTime();
//                            }
//                        } else if (freqArray[i-2] > freqArray[i-1] &          // down-up
//                            freqArray[i-1] < freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP, ++count);
//                            count = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMINFLECTIONS, ++count);
//                            int numInfl = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS).intValue();
//                            if (numInfl==1) {
//                                inflPrevTime = rcduPrev.getTime();
//                            } else {
//                                inflDeltaTimeArray[numInfl-1]=(rcduPrev.getTime()-inflPrevTime)/1000;	// convert the delta time to seconds
//                                inflPrevTime = rcduPrev.getTime();
//                            }
//                        } else if (freqArray[i-2] < freqArray[i-1] &          // up-flat
//                            freqArray[i-1] == freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT, ++count);
//                        } else if (freqArray[i-2] > freqArray[i-1] &          // down-flat
//                            freqArray[i-1] == freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT, ++count);
//                        } else if (freqArray[i-2] == freqArray[i-1] &          // flat-up
//                            freqArray[i-1] < freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP, ++count);
//                        } else if (freqArray[i-2] == freqArray[i-1] &          // flat-down
//                            freqArray[i-1] > freqArray[i] ) {
//                            double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN);
//                            contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN, ++count);
//                        }
                    }
                }
            }

            // move the data unit pointer for the next loop
            rcduPrev = rcdu;
        }
        
        /*
         * Inflection point calculations
         * Compare the current sweep with the previous one.  Since the very first rcdu (cell 0) does not have a valid sweep parameter
         * (it's always 0), we need to compare cell 2 with cell 1.  So start this loop with cell 2, and set the direction
         * parameter to the contents of cell 1.  We count an inflection when the current sweep parameter is opposite the
         * direction parameter
         */
        int curSweep = 0;
        int prevSweep = 0;
        int direction = this.getDataUnit(firstUnitWithWhistle+1, REFERENCE_CURRENT).getSweep();
        
        for (int i=2; i<numPoints; i++) {
        	
        	// load up the current and previous sweep values
            curSweep = this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT).getSweep();
            prevSweep = this.getDataUnit(firstUnitWithWhistle+i-1, REFERENCE_CURRENT).getSweep();
            
            // up-down
            if (prevSweep==SweepIndx.UP.getIndx() & curSweep==SweepIndx.DOWN.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN, ++count);
        		
        	// down-up
        	} else if (prevSweep==SweepIndx.DOWN.getIndx() & curSweep==SweepIndx.UP.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP, ++count);
        		
        	// up-flat
        	} else if (prevSweep==SweepIndx.UP.getIndx() & curSweep==SweepIndx.FLAT.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT, ++count);
        		
        	// down-flat
        	} else if (prevSweep==SweepIndx.DOWN.getIndx() & curSweep==SweepIndx.FLAT.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT, ++count);
        		
        	// flat-up
        	} else if (prevSweep==SweepIndx.FLAT.getIndx() & curSweep==SweepIndx.UP.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP, ++count);
        		
        	// flat-down
        	} else if (prevSweep==SweepIndx.FLAT.getIndx() & curSweep==SweepIndx.DOWN.getIndx()) {
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN, ++count);
        	}
            
            /* increment the NUMINFLECTIONS counter if we've changed directions.  Save the time of the current inflection
             * for analysis later.  Note that the commented section (along with the small_array calculations later)
             * calculates the time difference between each inflection point and the one prior.  The Matlab routine actually
             * calculates the time difference between each inflection point and the last inflection point.  So if there
             * are 4 inflections found, Matlab calculates #4-#1, #4-#2, and #4-#3.
             */ 
            if ((curSweep==SweepIndx.UP.getIndx() & direction==SweepIndx.DOWN.getIndx()) |
            		(curSweep==SweepIndx.DOWN.getIndx() & direction==SweepIndx.UP.getIndx())) {
            	direction = curSweep;
        		double count = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS);
        		contourStats.put(RoccaContourStats.ParamIndx.NUMINFLECTIONS, ++count);
        		int numInfl = contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS).intValue();
        		inflTimeArray[numInfl]=this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT).getTime();
//        		if (numInfl==1) {
//        			inflPrevTime = this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT).getTime();
//        		} else {
//        			inflDeltaTimeArray[numInfl-1]=
//        					(this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT).getTime()-inflPrevTime)/1000;	// convert the delta time to seconds
//        			inflPrevTime = this.getDataUnit(firstUnitWithWhistle+i, REFERENCE_CURRENT).getTime();
//        		}
            	
            } else if (direction==SweepIndx.FLAT.getIndx()) {
            	direction = curSweep;
            }
            
            // increment counter
            //i++;
    	}

        
        
        
        // set the ending frequency, duration and range
        contourStats.put(RoccaContourStats.ParamIndx.FREQEND, freqArray[freqArray.length-1]);
        contourStats.put(RoccaContourStats.ParamIndx.DURATION, (rcdu.getTime()-startTime)/1000); // duration in seconds, to be saved to Rocca Contour Stats file
        duration = rcdu.getTime()-startTime; // serialVersionUID=22 2015/06/13 in milliseconds, to be used in Encounter Stats calculations


        // calculate mean and standard devs for duty cycle and frequency
        contourStats.put(RoccaContourStats.ParamIndx.DCMEAN, getMean(dcArray));
        contourStats.put(RoccaContourStats.ParamIndx.DCSTDDEV, getStdDev(dcArray));
        contourStats.put(RoccaContourStats.ParamIndx.FREQMEAN, getMean(freqArray));
        contourStats.put(RoccaContourStats.ParamIndx.FREQSTDDEV, getStdDev(freqArray));

        // calculate Coefficient of Frequency Modulation COFM
        double freqCOFMSum = 0.0;
        for (int i = 6; i < numPoints; i+=3) {
            freqCOFMSum += Math.abs(freqArray[i]-freqArray[i-3]);
        }
        contourStats.put(RoccaContourStats.ParamIndx.FREQCOFM, freqCOFMSum / 10000);

        /*
         * figure out percentiles and spread.  Note that originally this used the getPercentile method, which calculates
         * percentiles using a NIST-recommended method.  However, to maintain compatibility with the original
         * Matlab, the getPercentile call has been commented out and replaced with a simpler algorithm.
         */
//        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER1, getPercentile(freqArray, 25.0));
//        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER2, getPercentile(freqArray, 50.0));
//        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER3, getPercentile(freqArray, 75.0));
//        contourStats.put(RoccaContourStats.ParamIndx.FREQSPREAD,
//                contourStats.get(RoccaContourStats.ParamIndx.FREQQUARTER3) -
//                contourStats.get(RoccaContourStats.ParamIndx.FREQQUARTER1));
        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER1, freqArray[((int) Math.round(((double) numPoints)/4)-1)]);
        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER2, freqArray[((int) Math.round(((double) numPoints)/2)-1)]);
        contourStats.put(RoccaContourStats.ParamIndx.FREQQUARTER3, freqArray[((int) Math.round(3*((double) numPoints)/4)-1)]);
        Arrays.sort(freqArray);     // frequency array must be sorted to calculate percentiles
        contourStats.put(RoccaContourStats.ParamIndx.FREQSPREAD,
        		getPercentile(freqArray, 75.0) - getPercentile(freqArray, 25.0));

        // calculate frequency statistics
        Arrays.sort(freqArray);     // frequency array must be sorted to calculate max, min, and median
        contourStats.put(RoccaContourStats.ParamIndx.FREQMIN, freqArray[0]);
        contourStats.put(RoccaContourStats.ParamIndx.FREQMAX, freqArray[freqArray.length-1]);
        contourStats.put(RoccaContourStats.ParamIndx.FREQRANGE, contourStats.get(RoccaContourStats.ParamIndx.FREQMAX) -
                contourStats.get(RoccaContourStats.ParamIndx.FREQMIN));
        contourStats.put(RoccaContourStats.ParamIndx.FREQMEDIAN, getMedian(freqArray));
        contourStats.put(RoccaContourStats.ParamIndx.FREQCENTER, contourStats.get(RoccaContourStats.ParamIndx.FREQMIN) +
                ((contourStats.get(RoccaContourStats.ParamIndx.FREQMAX)-contourStats.get(RoccaContourStats.ParamIndx.FREQMIN))/2));
        contourStats.put(RoccaContourStats.ParamIndx.FREQRELBW,
                (contourStats.get(RoccaContourStats.ParamIndx.FREQMAX)-contourStats.get(RoccaContourStats.ParamIndx.FREQMIN)) /
                contourStats.get(RoccaContourStats.ParamIndx.FREQCENTER));
        contourStats.put(RoccaContourStats.ParamIndx.FREQMAXMINRATIO,
                contourStats.get(RoccaContourStats.ParamIndx.FREQMAX)/contourStats.get(RoccaContourStats.ParamIndx.FREQMIN));
        contourStats.put(RoccaContourStats.ParamIndx.FREQBEGENDRATIO,
                contourStats.get(RoccaContourStats.ParamIndx.FREQBEG)/contourStats.get(RoccaContourStats.ParamIndx.FREQEND));

        // calculate the mean duty cycle of the final quarter
        contourStats.put(RoccaContourStats.ParamIndx.DCQUARTER4MEAN, dcQuarterSum / dcQuarterCount);

        // calculate the total number of steps, and divide out the duration
        contourStats.put(RoccaContourStats.ParamIndx.FREQNUMSTEPS,
                contourStats.get(RoccaContourStats.ParamIndx.FREQSTEPUP) +
                contourStats.get(RoccaContourStats.ParamIndx.FREQSTEPDOWN));
        double dur=(double) contourStats.get(RoccaContourStats.ParamIndx.DURATION);
        double steps=(double) contourStats.get(RoccaContourStats.ParamIndx.FREQNUMSTEPS);
        contourStats.put(RoccaContourStats.ParamIndx.STEPDUR, steps/dur);



        // calculate slope parameters
        contourStats.put(RoccaContourStats.ParamIndx.FREQSLOPEMEAN, slopeSum / (numPoints - 1));
        contourStats.put(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN, slopeAbsSum / (numPoints - 1));
        
        // serialVersionUID=10 check first if slopePosCount or slopeNegCount=0.  If so, set the slope
        // parameters to 0 (originally they became NaN)
        if (slopePosCount!=0) {
        	contourStats.put(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN, slopePosSum / slopePosCount);
        } else {
        	contourStats.put(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN, 0.0);
        }
        if (slopeNegCount!=0) {
        	contourStats.put(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN, slopeNegSum / slopeNegCount);
            contourStats.put(RoccaContourStats.ParamIndx.FREQSLOPERATIO,
                    contourStats.get(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN) /
                    contourStats.get(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN));
        } else {
        	contourStats.put(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN, 0.0);
        	contourStats.put(RoccaContourStats.ParamIndx.FREQSLOPERATIO, 0.0);
        }
        
        // calculate beginning slope, based on the average of the first 3 slopes
        // note that the slope of the first data unit is always 0, so skip that one
        slopeBegAve = (this.getDataUnit(firstUnitWithWhistle+1, REFERENCE_CURRENT).getSlope()+
                this.getDataUnit(firstUnitWithWhistle+2, REFERENCE_CURRENT).getSlope()+
                this.getDataUnit(firstUnitWithWhistle+3, REFERENCE_CURRENT).getSlope())/3;
        if (slopeBegAve > 0) {
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGSWEEP, (double) SweepIndx.UP.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGUP, (double) PosSweepIndx.TRUE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGDWN, (double) PosSweepIndx.FALSE.getIndx());
        } else if (slopeBegAve < 0) {
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGSWEEP, (double) SweepIndx.DOWN.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGUP, (double) PosSweepIndx.FALSE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGDWN, (double) PosSweepIndx.TRUE.getIndx());
        } else {
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGSWEEP, (double) SweepIndx.FLAT.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGUP, (double) PosSweepIndx.FALSE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQBEGDWN, (double) PosSweepIndx.FALSE.getIndx());
        }

        // calculate ending slope, based on the average of the last 3 slopes
        slopeEndAve = (this.getDataUnit(lastUnitWithWhistle-1, REFERENCE_CURRENT).getSlope()+
                this.getDataUnit(lastUnitWithWhistle-2, REFERENCE_CURRENT).getSlope()+
                this.getDataUnit(lastUnitWithWhistle-3, REFERENCE_CURRENT).getSlope())/3;
        if (slopeEndAve > 0) {
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDSWEEP, (double) SweepIndx.UP.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDUP, (double) PosSweepIndx.TRUE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDDWN, (double) PosSweepIndx.FALSE.getIndx());
        } else if (slopeEndAve < 0) {
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDSWEEP, (double) SweepIndx.DOWN.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDUP, (double) PosSweepIndx.FALSE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDDWN, (double) PosSweepIndx.TRUE.getIndx());
        } else {
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDSWEEP, (double) SweepIndx.FLAT.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDUP, (double) PosSweepIndx.FALSE.getIndx());
            contourStats.put(RoccaContourStats.ParamIndx.FREQENDDWN, (double) PosSweepIndx.FALSE.getIndx());
        }

        // calculate percentage of time in each sweep direction
        // (note the first and last data units do not have a sweep value)
        // serialVersionUID=10 check if totCount=0 (can happen with Whistle & Moan Detector)
        // if so, set the params to 0
        double totCount = sweepUpCount + sweepDwnCount + sweepFlatCount;
        if (totCount!=0) {
            double upPercent = (((double) sweepUpCount) / totCount)*100;  
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, upPercent);
//            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, (double)
//                    (sweepUpCount / (numPoints-2)));
            double downPercent = (((double) sweepDwnCount) / totCount)*100;  
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT, downPercent);
//            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT, (double)
//                    (sweepDwnCount / (numPoints-2)));
            double flatPercent = (((double) sweepFlatCount) / totCount)*100;
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPFLATPERCENT, flatPercent);
//            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, (double)
//                   ((sweepUpCount+sweepDwnCount) / (numPoints-2)));        	
        } else {
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.FREQSWEEPFLATPERCENT, 0.0);        	
        }

        
        /* Calculate inflection point time statistics.  Note that originally this section calculated
         * the time between each inflection point and the one prior.  That was changed to match Matlab
         * version of Rocca (see explanation given for the NUMINFLECTIONS counter, above).  I didn't
         * want to delete all of this code, however, in case we go back to this.  So it's simply
         * commented out.  MO 10/7/2011
         */
//        /* calculate inflection time statistics.  Note that the array containing the inflection
//         * information inflDeltaTimeArray is initialized with a size equal to the number of points.
//         * Since there are a LOT fewer inflection points, there are quite a few 0's in the array.
//         * So the first thing we need to do is create a new, smaller array that only contains the
//         * actual data points in inflDeltaTimeArray.  Then we can find the max, min, mean, etc on
//         * that array.
//         */
//        if (contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS) > 1) {
//            int numInfl = (int) ((double) contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS));	// double cast, b/c can't go from Double to int directly
//            double[] smallArray = new double[numInfl-1];
//            int count=0;
//            for (int j=0; j<numPoints-1; j++) {
//            	if (inflDeltaTimeArray[j]!=0) {
//            		smallArray[count]=inflDeltaTimeArray[j];
//            		count++;
//            	}
//            }
//            Arrays.sort(smallArray);
        
	      // new code starts here
        if (contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS) > 1) {
	        Arrays.sort(inflTimeArray);	// sort array, with all the 0's in the beginning
	        int numInfl = (int) ((double) contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS));	// double cast, b/c can't go from Double to int directly
	        double[] smallArray = new double[numInfl-1];
	        int count=0;
	        for (int j=numPoints-2; j>numPoints-numInfl-1; j--) {
	        	smallArray[count]=(inflTimeArray[j+1]-inflTimeArray[j])/1000;
	        	count++;
	        }
	        Arrays.sort(smallArray);
        
        
      // the code below would work with either method of inflection time calculation
            contourStats.put(RoccaContourStats.ParamIndx.INFLMAXDELTA,
            		smallArray[smallArray.length-1]);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMINDELTA,
            		smallArray[0]);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMAXMINDELTA,
                    contourStats.get(RoccaContourStats.ParamIndx.INFLMAXDELTA) /
                    contourStats.get(RoccaContourStats.ParamIndx.INFLMINDELTA) );
            contourStats.put(RoccaContourStats.ParamIndx.INFLMEANDELTA, getMean(smallArray));
            contourStats.put(RoccaContourStats.ParamIndx.INFLSTDDEVDELTA, getStdDev(smallArray));
            contourStats.put(RoccaContourStats.ParamIndx.INFLMEDIANDELTA, getMedian(smallArray));
            
        // divide the number of inflections by the duration
            double infl=(double) contourStats.get(RoccaContourStats.ParamIndx.NUMINFLECTIONS);
            contourStats.put(RoccaContourStats.ParamIndx.INFLDUR, infl/dur);

        } else {
        	/* serialVersionUID = 10 2014/02/04 replaced "999.0" with "0.0" */
            contourStats.put(RoccaContourStats.ParamIndx.INFLMAXDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMINDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMAXMINDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMEANDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLSTDDEVDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLMEDIANDELTA, 0.0);
            contourStats.put(RoccaContourStats.ParamIndx.INFLDUR, 0.0);
        }

        // set the statsRun flag to true
        statsRun = true;
    }

    /**
     * Calculate the statistics specific to a click detection
     */
    public void calculateClickStats() {
    	
        // Initialize all parameters in the map to 0.0; note that FREQMIN needs to
        // be set separately.  Also save the key names into an arrayList to make it easier
        // to search when comparing to the classifier's required attributes.
    	rcs.initialize(contourStats);
        contourStats.put(RoccaContourStats.ParamIndx.FREQMIN, 0.0);
    	
    	
    	// no need to apply butterworth filter because the user should already have
    	// defined that as a digital pre-filter
    	// apply a Hanning window to both signal and noise arrays.
        // 2017/11/30 synchronize to make sure that the data doesn't accidentally get erased halfway through the calc 
        double[] waveData;
        synchronized (clickDetection) {
        	waveData = ClickDetection.applyHanningWindow(clickDetection.getWaveData()[0]);
		}
    	
    	// calculate the peak frequency
    	// add check for a null search range (which happens when the click is unclassified).  In
    	// that case, just set peakFreq to 0
    	// serialVersionUID = 21 2015/05/31
        double[] searchRange = clickDetection.getClickDetector().getClickControl().getClickIdentifier().getPeakSearchRange(clickDetection);
//    	double peakFreq = clickDetection.peakFrequency(new double[] {0, clickDetection.getClickDetector().getSampleRate()/2});
        double peakFreq;
        if (searchRange != null) {
        	peakFreq = clickDetection.peakFrequency(searchRange);
        } else {
        	peakFreq = 0.0;
        }
        contourStats.put(RoccaContourStats.ParamIndx.FREQPEAK, peakFreq);
        
        // calculate the duration.  Use 100% of the total click energy for the calculation.
        // clickLength method returns the duration in seconds
        // Note that earlier versions only used 90% of the click energy.  However, when
        // 100% of the click energy is used then it is easy to figure out how much of
        // the duration is the presample, how much is the postsample and therefore
        // how much is truly just the click duration
        //double clickDur = clickDetection.clickLength(100.0);
        //contourStats.put(RoccaContourStats.ParamIndx.DURATION, clickDur);

        // calculate the duration, using the SweepClassifierWorker method getLengthData
        // note that getLengthData returns the duration in seconds
    	double clickDur = clickDetection.getClickDetector().getClickControl().getClickIdentifier().getClickLength(clickDetection);
        contourStats.put(RoccaContourStats.ParamIndx.DURATION, clickDur);
        startTime = this.getFirstUnit().getTime();
        duration = clickDur*1000;	// convert to milliseconds for use in encounter stats calculations

        
        // get the zero crossing data from the sweep classifier
//        zcs = clickDetection.getClickDetector().getClickControl().getClickIdentifier().getZeroCrossingStats(clickDetection);
        zcs = clickDetection.getZeroCrossingStats();
        if (zcs==null) {
	        contourStats.put(RoccaContourStats.ParamIndx.NCROSSINGS, 0.0);
	        contourStats.put(RoccaContourStats.ParamIndx.SWEEPRATE, 0.0);    
	        contourStats.put(RoccaContourStats.ParamIndx.MEANTIMEZC, 0.0);    
	        contourStats.put(RoccaContourStats.ParamIndx.MEDIANTIMEZC, 0.0);    
	        contourStats.put(RoccaContourStats.ParamIndx.VARIANCETIMEZC, 0.0);    
	        
        } else {
	        contourStats.put(RoccaContourStats.ParamIndx.NCROSSINGS, (double) zcs[0].nCrossings);
	        contourStats.put(RoccaContourStats.ParamIndx.SWEEPRATE, zcs[0].sweepRate / 1e6); // convert to kHz per millisecond.
	        contourStats.put(RoccaContourStats.ParamIndx.MEANTIMEZC, zcs[0].getMeanTime());    
	        contourStats.put(RoccaContourStats.ParamIndx.MEDIANTIMEZC, zcs[0].getMedianTime());    
	        contourStats.put(RoccaContourStats.ParamIndx.VARIANCETIMEZC, zcs[0].getVarianceTime());    
        }
        
        // calculate the -3dB bandwidth
        double[] bw = clickDetection.peakFrequencyWidthChan(peakFreq, 3, 0, true);
        contourStats.put(RoccaContourStats.ParamIndx.BW3DB, bw[0]);
        contourStats.put(RoccaContourStats.ParamIndx.BW3DBLOW, bw[1]);
        contourStats.put(RoccaContourStats.ParamIndx.BW3DBHIGH, bw[2]);
                
        // calculate the -10dB bandwidth
        bw = clickDetection.peakFrequencyWidthChan(peakFreq, 10,0, true);
        contourStats.put(RoccaContourStats.ParamIndx.BW10DB, bw[0]);
        contourStats.put(RoccaContourStats.ParamIndx.BW10DBLOW, bw[1]);
        contourStats.put(RoccaContourStats.ParamIndx.BW10DBHIGH, bw[2]);
        
        // calculate the mean/center frequency
        //double[] fullRange = {0.0, (double) clickDetection.getClickDetector().getSampleRate()};
    	// add check for a null search range (which happens when the click is unclassified).  In
    	// that case, just set meanFreq to 0
    	// serialVersionUID = 21 2015/05/31
        double meanFreq;
        if (searchRange != null) {
        	meanFreq = clickDetection.getMeanFrequency(searchRange);
        } else {
        	meanFreq = 0.0;
        }
        contourStats.put(RoccaContourStats.ParamIndx.FREQCENTER, meanFreq);
                
        
        // calculate the rms values for both signal and noise, and the resulting snr
    	double sumSignal = 0;
        for (int i = 0; i < waveData.length; i++)
        {
            sumSignal = sumSignal + (waveData[i]*waveData[i]);
        }
        double rmsSignal = 20*Math.log10(Math.sqrt(sumSignal/waveData.length));
        contourStats.put(RoccaContourStats.ParamIndx.RMSSIGNAL, rmsSignal);

        // only calculate noise if we have the data (typically not available in Viewer mode)
        if (clickNoise != null) {
        	double[] noiseData;
        	synchronized (clickNoise) {
    	    	noiseData = ClickDetection.applyHanningWindow(clickNoise.getWaveData()[0]);
			}
	    	double sumNoise = 0;
	        for (int i = 0; i < noiseData.length; i++)
	        {
	        	sumNoise = sumNoise + (noiseData[i]*noiseData[i]);
	        }
	        double rmsNoise = 20*Math.log10(Math.sqrt(sumNoise/noiseData.length));
	        contourStats.put(RoccaContourStats.ParamIndx.RMSNOISE, rmsNoise);
	        contourStats.put(RoccaContourStats.ParamIndx.SNR, rmsSignal-rmsNoise);
        } else {
	        contourStats.put(RoccaContourStats.ParamIndx.RMSNOISE, 0.0);
	        contourStats.put(RoccaContourStats.ParamIndx.SNR, 0.0);        	
        }
        
        // save the Click Type and Whale Train ID (stored in ClickDetection as EventID)
        contourStats.put(RoccaContourStats.ParamIndx.CLICKTYPE, (double) clickDetection.getClickType());
        contourStats.put(RoccaContourStats.ParamIndx.WHALETRAIN, (double) clickDetection.getEventId());
        
        // save the ICI data
        contourStats.put(RoccaContourStats.ParamIndx.ICI, clickDetection.getICI());
        
        // save the Offline Event ID
        contourStats.put(RoccaContourStats.ParamIndx.OFFLINEEVENTID, (double) clickDetection.getOfflineEventID());

        // set the statsRun flag to true
        statsRun = true;
    }
    
    
    private double getMean(double[] array) {
        double sum = 0.0;
        for (int i=0; i<array.length; i++) {
            sum += array[i];
        }
        return sum/array.length;
    }

    private double getStdDev(double[] array) {
    	// serialVersionUID=10 added check for NaN
        double sum = 0.0;
        double mean = getMean(array);
        double answer;
        for (int i=0; i<array.length; i++) {
            sum += Math.pow(array[i]-mean, 2.0);
        }
        answer = Math.sqrt(sum/(array.length-1));
        if (Double.isNaN(answer)) {
        	answer = 0.0;
        }
        return answer;
    }

    private double getMedian(double[] array) {
        // calculate the median value in the passed array
        // note that array MUST be sorted first
        int mid = array.length/2;

        if (array.length%2 == 1) {
            // Odd number of elements -- return the middle one.
            return array[mid];
        } else {
           // Even number -- return average of middle two
           return (array[mid-1] + array[mid]) / 2.0;
        }
    }

    private double getPercentile(double[] array, double percentile) {
        // using method recommended by NIST (slightly different than Matlab)
        // note that array MUST be sorted first

        double n = percentile / 100 * (array.length-1) + 1;

        if (n==1) {
            return array[0];
        } else if (n==array.length) {
            return array[array.length-1];
        } else {
            int rank = (int) Math.floor(n);
            double remainder = n - rank;
            return array[rank-1] + remainder * (array[rank]-array[rank-1]);
        }
    }
    
    /**
     * Creates the header line for the contour status summary file.  Note that
     * the individual species tree votes do not get labelled, as this may change
     * if the user switches classifiers at some point.  Instead, the species
     * list is included at the end of each row
     * 
     * 2014/09/09 added Encounter ID, Cruise ID, Data Provider and Known Species
     * 2015/10/10 serialVersionUID=23 removed spaces and parenthesis from headers
     * serialVersionUID=24 2016/08/10 added lat/long info
     *
     * @return the header string
     */
    public String createContourStatsHeader() {
//        String contourHeader = 
//               "Source, Start Date-Time (GMT), End Date-Time (GMT), EncounterCount, EncounterNumber, Sampling Rate, Num Channels, Encounter ID, Cruise ID, Data Provider, Known Species, Geographic Location, ClassifiedSpecies, ";
        String contourHeader = 
                "Source,Start_DateTime_GMT,End_DateTime_GMT,EncounterCount,EncounterNumber,SamplingRate,NumChannels,EncounterID,CruiseID,DataProvider,KnownSpecies,GeographicLocation,ClassifiedSpecies,";
        for (RoccaContourStats.ParamIndx p : RoccaContourStats.ParamIndx.values()) {
            contourHeader = contourHeader + p + ",";
        }

        /* add the tree vote information */
//        contourHeader = contourHeader + "Stage 1 Classifier, Stage 2 Classifier";
        contourHeader = contourHeader + "Lat,Long,Classifier";
        
        
        // serialVersionUID=23 added species labels to header (generic names, not the actual names)
        // use one less than the number of items in the treeVotes array because it contains 'Ambig'
        if (treeVotes != null)
        for (int i=0; i<treeVotes.length-1; i++) {
        	contourHeader = contourHeader + ",Species" + (i+1);
        }
        contourHeader = contourHeader + ",Species_List";
       

        // remove the last ", " in the string and return
//        contourHeader = contourHeader.substring(0, contourHeader.length()-2);
        return contourHeader;
    }


    /**
     * Method to create a string containing the current contour statistics.
     * Also included in the string are the filename, detection number and
     * encounter classification.  This method sets the filename equal to the raw
     * data source, and the detection count to 0
     *
     * @return a String containing the contour statistics
     */
    public String createContourStatsString() {
        String clipFile = this.getWavFilename().getAbsolutePath();
        return createContourStatsString(clipFile, 0);
    }


    /**
     * Method to create a string containing the current contour statistics.
     * Also included in the string are the filename, date/time, detection count
     * and encounter classification.  This method sets the filename equal to the
     * passed String clipFile.
     *
     * @param clipFile String to be saved in the 'filename' column
     * @param detectionCount integer specifying the detection count (the count
     * starts at 1 when the first detection is captured)
     * @return a String containing the contour statistics
     */
    public String createContourStatsString(String clipFile, int detectionCount) {
        return createContourStatsString(clipFile, detectionCount, "null");    	
    }

    /**
     * Method to create a string containing the current contour statistics.
     * Also included in the string are the filename, date/time, detection count
     * and encounter classification.  This method sets the filename equal to the
     * passed String clipFile.
     * 2015/10/10 serialVersionUID=23 removed spaces from columns
     * serialVersionUID=24 2016/08/10 added lat/long info
     *
     * @param clipFile String to be saved in the 'filename' column
     * @param detectionCount integer specifying the detection count (the count
     * starts at 1 when the first detection is captured)
     * @return a String containing the contour statistics
     */
    public String createContourStatsString(String clipFile, int detectionCount, String sightNum) {

        // first make sure the stats have actually been run
        if (!statsRun) {
            this.calculateStatistics();
        }

        // tie together all the parameters into one long string, separated by commas
        String contourStatsStr = "";
        for (RoccaContourStats.ParamIndx p : RoccaContourStats.ParamIndx.values()) {
            contourStatsStr = contourStatsStr + contourStats.get(p) + ",";
        }
        
        // if we weren't passed a sighting number to use, get it from the sidebar
        if (sightNum.equals("null")) {
        	sightNum = roccaProcess.roccaControl.roccaSidePanel.getSightingNum();
        }

        /* add the filename, sighting number and classified species to the
         * beginning of the string
         * Update 11 - added decimal places to date/time string, and added extra column
         * for End Date/Time
         * Update 13 - added Encounter ID, Cruise ID, Data Provider and Known Species
         * serialVersionUID = 16 2014/12/28 - remove 'GMT' from each row, because it prevents
         * Excel from manipulating the date string
         */
        long durationInMillis = (long) (contourStats.get(RoccaContourStats.ParamIndx.DURATION)*1000);
        
        // 2017/12/4 add a quick check to see if the data units have been deleted from the RoccaContourDataBlock.
        // If so, just use 0's for the times.  Note that this should never happen, because the natural lifetime
        // has been set to Integer.Max and won't get reset back to 0 until after this has been called.  But...
        // better safe than sorry
        String firstColumns;
        if (this.getFirstUnit() != null) {
        	firstColumns =
        			clipFile +
        			"," +
        			//                PamCalendar.formatDateTime(this.getFirstUnit().getTime()) +
        			PamCalendar.formatDate(this.getFirstUnit().getTime()) +
        			' ' +
        			PamCalendar.formatTime(this.getFirstUnit().getTime(),true) +
        			//                " GMT, " +
        			"," +
        			PamCalendar.formatDate(this.getFirstUnit().getTime()+durationInMillis) +
        			' ' +
        			PamCalendar.formatTime(this.getFirstUnit().getTime()+durationInMillis,true) +
        			//                " GMT, " +
        			",";
        } else {
        	firstColumns = clipFile + ",0,0,";
        }
        contourStatsStr =
        		firstColumns +
                detectionCount +
                "," +
                sightNum +
                "," +
                this.getSampleRate() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesChan() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesEncounterID() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesCruiseID() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesDataProvider() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesKnownSpecies() +
                "," +
                roccaProcess.roccaControl.roccaParameters.getNotesGeoLoc() +
                "," +
                classifiedAs +
                "," +
                contourStatsStr;

        /* add the classifier(s).  If the second classifier was not defined, use "N/A" instead */
//        String class2Name;
//        try {
//        	class2Name = roccaProcess.roccaControl.roccaParameters.getRoccaClassifier2ModelFilename().getName();
//        } catch (Exception ex) {
//        	class2Name = "N/A";
//        }

        // add the latitude and longitude.
        contourStatsStr += "" + latitude + "," + longitude +",";
    	
        // add the classifier
		// serialVersionUID=25 2017/03/28 added check for whistle or click, to save correct classifier name
        // 2021/05/18 added check for no classifier at all
        String className = RoccaClassifier.NOCLASS;
        if (this.isThisAWhistle()) {
        	if (roccaProcess.roccaControl.roccaParameters.getRoccaClassifierModelFilename()!=null) {
        		className = roccaProcess.roccaControl.roccaParameters.getRoccaClassifierModelFilename().getName();
        	}
        	contourStatsStr = contourStatsStr + className + ",";
        }
        else {
        	if (roccaProcess.roccaControl.roccaParameters.getRoccaClickClassifierModelFilename()!=null) {
        		className = roccaProcess.roccaControl.roccaParameters.getRoccaClickClassifierModelFilename().getName();
        	}
        	contourStatsStr = contourStatsStr + className + ",";
        }
        /* add the tree votes.  Also save the tree votes as a single String with
         * the '-' character separating the values, to save to the database.  Check
         * the length of the treeVotes array.  If it is 1 larger than the species list,
         * it means it contains the Ambig class.  If that's the case, set the offset so that
         * we ignore that one when saving the data
         */
        String voteList = "(";
        String treeSpList = "(";
        
        // 2021/05/11 since we are allowing the user to run Rocca without a classifier, no 
        // need to make a distinction here.  All whistles/clicks will be classified as
        // "NoClassifier"
//        if (roccaProcess.isClassifierLoaded()) {
        	DecimalFormat df = new DecimalFormat("#.#####");
        	double roundedVal = 0.0;
        	String[] classSpList = roccaProcess.getRoccaClassifier().getClassifierSpList();
        	int offset=0;
        	if (treeVotes.length>classSpList.length) {
        		offset=1;
        	}
        	for (int i=offset; i<treeVotes.length; i++) {
        		contourStatsStr = contourStatsStr + treeVotes[i] + ",";
        		roundedVal = Double.valueOf(df.format(treeVotes[i]));
        		voteList = voteList + roundedVal + "-";
        	}
        	voteList = voteList + ")";

        	/* add the list of species used in the classifier to the last column */
        	for (int i=0; i<classSpList.length; i++) {
        		treeSpList = treeSpList + classSpList[i] + "-";
        	}
        	treeSpList = treeSpList + ")";
        	contourStatsStr = contourStatsStr + treeSpList;
//        }
//        else {
//        	voteList = RoccaClassifier.NOCLASS;
//        	treeSpList = RoccaClassifier.NOCLASS;
//        }

//        /* remove the last ", " in the string and return */
//        contourStats = contourStats.substring(0, contourStats.length()-2);

        // create a RoccaLoggingDataUnit and save the variables to it as well
        // 2017/12/4 add a quick check to see if the data units have been deleted from the RoccaContourDataBlock.
        // If so, just use 0 for the time.  Note that this should never happen, because the natural lifetime
        // has been set to Integer.Max and won't get reset back to 0 until after this has been called.  But...
        // better safe than sorry
        long unitTime=0;
        if (this.getFirstUnit()!=null) {
        	unitTime = this.getFirstUnit().getTimeMilliseconds();
        }        
        RoccaLoggingDataUnit rldu = new RoccaLoggingDataUnit(unitTime, rcs);
        rldu.setFilename(clipFile);
        rldu.setDetectionCount(detectionCount);
        rldu.setSightingNum(roccaProcess.roccaControl.roccaSidePanel.getSightingNum());
        rldu.setClassifiedSpecies(classifiedAs);
        rldu.setClassifierUsed(className);
        rldu.setVoteList(voteList);
        rldu.setSpList(treeSpList);
        rldu.setStartSample(this.getFirstUnit().getStartSample());        // serialVersionUID=25 2017/03/28 added
        rldu.setDurationInMilliseconds(durationInMillis);        // serialVersionUID=25 2017/03/28 added
        rldu.setSampleDuration(this.getFirstUnit().getSampleDuration());       // serialVersionUID=25 2017/03/28 added

        
        // set the latitude and longitude
        // serialVersionUID=24 2016/08/10 added
        rldu.setLatitude(latitude);
        rldu.setLongitude(longitude);

        /* add the data unit to the logging data block */
        roccaProcess.rldb.addPamData(rldu);
        
        // if we are in viewer mode, write the data unit to the database
        // serialVersionUI=22 2015/09/09
        if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
	        boolean success = roccaProcess.rldb.saveViewerData();
			if (!success) {
				System.out.println("RoccaContourDataBlock:  Unable to save info to database");
	//		} else {
	//			System.out.println("RoccaContourDataBlock:  info saved to database");
			}
			
	        // delete the data unit (we don't need it once it's saved into the database)
			// don't forget to clear the removedItems list, or else the deleted data unit will be removed from the database
			// again the next time we pass through this point.
	        // serialVersionUI=22 2015/09/09
	        roccaProcess.rldb.remove(rldu);
	        roccaProcess.rldb.clearDeletedList();
        }
        

        /* return success */
        return contourStatsStr;
    }

    /**
     * Get the filename of the original wav file
     *
     * @return
     */
    public File getWavFilename() {
        File soundFile = new File("UnknownWav");
		// taken from WhistleClassifierProcess...
		try {
			AcquisitionProcess sourceProcess = (AcquisitionProcess) roccaProcess.getSourceProcess();
			Acquisition.DaqSystem daqSystem = sourceProcess.getAcquisitionControl().findDaqSystem(null);
			if (daqSystem != null & !daqSystem.isRealTime()) {
				// assume it's a file name.
                // MIGHT NOT WORK IF THIS IS A LINE INPUT - TEST
				Acquisition.FileInputSystem fileSystem = (FileInputSystem) daqSystem;
				soundFile = fileSystem.getCurrentFile();
				if (soundFile==null) {
					soundFile = new File("UnknownWav");
				}
			}
		}
		catch (Exception ex) {
            // if there's an exception, don't do anything and leave soundFile at the
            // default setting
		}
        return soundFile;
    }

    public String getClassifiedAs() {
        return classifiedAs;
    }

    public void setClassifiedAs(String classifiedAs) {
        this.classifiedAs = classifiedAs;
    }

    public boolean isStatsRun() {
        return statsRun;
    }

    public void setStatsRun(boolean statsRun) {
        this.statsRun = statsRun;
    }

    public double[] getTreeVotes() {
        return treeVotes;
    }

    public void setTreeVotes(double[] treeVotes) {
        this.treeVotes = treeVotes;
    }

    public int getFirstUnitWithWhistle() {
        return firstUnitWithWhistle;
    }

    public void setFirstUnitWithWhistle(int firstUnitWithWhistle) {
        this.firstUnitWithWhistle = firstUnitWithWhistle;
    }

    public int getLastUnitWithWhistle() {
        return lastUnitWithWhistle;
    }

    public void setLastUnitWithWhistle(int lastUnitWithWhistle) {
        this.lastUnitWithWhistle = lastUnitWithWhistle;
    }

    public ArrayList<String> getKeyNames() {
        return keyNames;
    }

    public EnumMap<RoccaContourStats.ParamIndx, Double> getContour() {
        return contourStats;
    }

    public RoccaContourStats getRCS() {
        return rcs;
    }

	/**
	 * @return the prdb
	 */
//	public PamRawDataBlock getPrdb() {
//		return prdb;
//	}

	/**
	 * @param prdb the prdb to set
	 */
//	public void setPrdb(PamRawDataBlock prdb) {
//		this.prdb = prdb;
//	}

	/**
	 * @return the clickDetection
	 */
	public ClickDetection getClickDetection() {
		return clickDetection;
	}

	/**
	 * Sets the clickDetection parameter.  Also creates a RoccaContourDataUnit object
	 * and loads it with basic information from the clickDetection.  This prevents errors
	 * later in the code where it is assumed that the RoccaContourDataBlock contains at
	 * least one data unit.
	 * 
	 * @param clickDetection the clickDetection to set
	 */
	public void setClickDetection(ClickDetection clickDetection) {
		this.clickDetection = clickDetection;
		RoccaContourDataUnit rcdu = new RoccaContourDataUnit(
				clickDetection.getTimeMilliseconds(),
				clickDetection.getChannelBitmap(),
				clickDetection.getStartSample(),
				clickDetection.getSampleDuration());
		rcdu.setTime(clickDetection.getTimeMilliseconds());	// set the time in the middle of the bin to the same as the start time
		this.addPamData(rcdu);
	}

	/**
	 * @return the clickNoise
	 */
	public ClickDetection getClickNoise() {
		return clickNoise;
	}

	/**
	 * @param clickNoise the clickNoise to set
	 */
	public void setClickNoise(ClickDetection clickNoise) {
		this.clickNoise = clickNoise;
	}

	/**
	 * @return the startTime
	 * serialVersionUID=15 2014/11/12
	 */
	public double getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 * serialVersionUID=15 2014/11/12
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the duration
	 * serialVersionUID=22 2015/06/13
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 * serialVersionUID=22 2015/06/13
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Test whether this data block is a whistle or not
	 * serialVersionUID=24 2016/08/10
	 * @return true=whistle, false=click
	 */
	public boolean isThisAWhistle() {
		return thisIsAWhistle;
	}

	/**
	 * Sets whether this data block is a whistle or not.  True=whistle, false=click
	 * serialVersionUID=24 2016/08/10
	 * @param thisIsAWhistle
	 */
	public void setAsAWhistle(boolean thisIsAWhistle) {
		this.thisIsAWhistle = thisIsAWhistle;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setWavInfo(double[][] wavInfo) {
		this.wavInfo = wavInfo;
	}

	public double[][] getWavInfo() {
		return wavInfo;
	}


//	int nS, nT;
//	@Override
//	protected int removeOldUnitsT(long currentTimeMS) {
//		// TODO Auto-generated method stub
//		nT++;
//		System.out.printf("Enter removeoldUnitsT, entry count %d\n", nT);
//		int n = super.removeOldUnitsT(currentTimeMS);
//		nT--;
//		return n;
//	}
//
//	@Override
//	protected int removeOldUnitsS(long mastrClockSample) {
//		// TODO Auto-generated method stub
//		nS++;
//		System.out.printf("Enter removeoldUnitsS, entry count %d\n", nS);
//		int n  = super.removeOldUnitsS(mastrClockSample);
//		nS--;
//		return n;
//	}
//	
	
}
