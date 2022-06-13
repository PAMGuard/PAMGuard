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

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Acquisition.DaqStatusDataUnit;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import PamModel.PamModel;
import Spectrogram.SpectrogramDisplay;
import fftManager.FFTDataBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.NoiseDataBlock;
import clickDetector.TrackedClickDataBlock;
import wavFiles.WavFile;
import wavFiles.WavFileWriter;
import whistlesAndMoans.AbstractWhistleDataBlock;
import whistlesAndMoans.AbstractWhistleDataUnit;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;

/**
 * Main Rocca process
 *
 *
 * @author Michael Oswald
 */
public class RoccaProcess extends PamProcess {

    // local reference to the RoccaControl controlling this process
    RoccaControl roccaControl;

    // source data block
    FFTDataBlock fftDataBlockIn;

    // contour process and parameters
    RoccaContour roccaContour;
    
    // Classifier
    RoccaClassifier roccaClassifier;

    // channel map of data source
    int channelMap;

    /** The number of detections captured since the program started - only used for the whistle and moan detector */
    private static int numDetections = 0;

    /** reference to the FFT data source process */
    AcquisitionProcess daqProcess;
    
    /** flag indicating whether or not the classifier model has been loaded */
    boolean classifierLoaded = false;

    /** reference to the Spectrogram Display */
    SpectrogramDisplay display;

    /** data block for logging contour statistics to database */
    RoccaLoggingDataBlock rldb;

    /** Whistle & Moan detector source */
	private AbstractWhistleDataBlock whistleSourceData;
	
	/** Click Detector source detected automatically */
	private ClickDataBlock autoClickSourceData;
	
	/** Click Detector source selected manually by the user */
	private TrackedClickDataBlock manClickSourceData;

	/** Click Detector noise source */
	private NoiseDataBlock clickNoiseSourceData;
	
	/** Click noise data unit */
	private ClickDetection latestClickNoise;
	
	/** To keep track of the previous click */
	private ClickDetection prevClick;
	
	/** Debug testing - keep track of Click Hashmaps to test for duplicates */
	private List<Integer> clickIDs = new ArrayList<Integer>();
	
	/** GSP source */
	private GPSDataBlock gpsSourceData;

	/** A counter for auto event names - will get updated everytime a source file changes */
	private int eventNameCounter = 0;

	/** The name of the current auto event */
	private String autoEventName = "AutoEvent0";



    /**
     * Main constructor
     *
     * @param roccaControl
     */
    public RoccaProcess (RoccaControl roccaControl) {
        super(roccaControl, null);
        this.roccaControl = roccaControl;
        roccaContour = new RoccaContour(this);
        roccaClassifier = new RoccaClassifier(this);
        rldb = new RoccaLoggingDataBlock(this, 0);

        rldb.SetLogging(new RoccaStatsLogger(roccaControl, rldb));
		rldb.setMixedDirection(PamDataBlock.MIX_INTODATABASE);
        addOutputDataBlock(rldb);

    }


    @Override
    public void pamStart() {
//        firstUnit = true;
    }


    @Override
    public void pamStop() {
    }


    @Override
    /*
     * Find and subscribe to the FFTDataBlock that's been specified in the RoccaParameters
     */
    public void prepareProcess() {
        super.prepareProcess();
        
        // 2017/10/14 added
        // try to subscribe to AcquisitionProcess.daqStatusDataBlock, if it exists.  This would alert Rocca when the source file changes, and Rocca
        // can update the event name
        // 2019/11/25 only do this if we're using auto-detections from the Click Detector or WMD.  If the user is
        // only boxing whistles in the spectrogram, let them decide what Encounter to use
        if (roccaControl.roccaParameters.weAreUsingWMD() || roccaControl.roccaParameters.weAreUsingClick()) {
        	PamControlledUnit pcu = PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
        	if ((pcu != null) && (AcquisitionControl.class.isAssignableFrom(pcu.getClass()))) {
        		PamDataBlock daqStatus = ((AcquisitionControl) pcu).getAcquisitionProcess().getDaqStatusDataBlock();
        		daqStatus.addObserver(this);
        	}
        }

    	// 2016/01/04 serialVersionUID=23 test whether Pamguard is set to multi-threading or not
        Boolean multithread = PamModel.getPamModel().isMultiThread();
        
        /* if user has specified an FFT data source, subscribe to it */
        if (roccaControl.roccaParameters.weAreUsingFFT()) {
        	
        	// get list of all fftDataBlocks
        	ArrayList<PamDataBlock> allfftDataBlocks = PamController.getInstance().getFFTDataBlocks();

        	// if no fftDataBlocks exist yet, return a null
        	// 2014-10-13 serialVersionUID 14 don't do anything with the parent block, in case one of the
        	// other sources has been set as the parent
        	if (allfftDataBlocks == null || allfftDataBlocks.size() <= roccaControl.roccaParameters.fftDataBlock) {
        		//setParentDataBlock(null);
        		return;
        	}

        	// if fftDataBlocks exist, subscribe to the one specified in RoccaParameters
        	fftDataBlockIn = (FFTDataBlock) allfftDataBlocks.get(roccaControl.roccaParameters.fftDataBlock);
        	setParentDataBlock(fftDataBlockIn);
        	fftDataBlockIn.getRawSourceDataBlock2().setNaturalLifetimeMillis(5000);
        	this.channelMap = roccaControl.roccaParameters.getChannelMap();
        	
        } else {
        	if (fftDataBlockIn!=null) {
        		fftDataBlockIn.deleteObserver(this);
        	}
        }
        /* if user has specified a whistle & moan detector source, subscribe to it */
        // 2014-10-13 serialVersionUID 14 move WMD to before the click detector instead of after, and 
        // change else to if so we can use multiple source
        //} else {
        if (roccaControl.roccaParameters.weAreUsingWMD()) {
        
        	whistleSourceData = (AbstractWhistleDataBlock) PamController.getInstance().
        			getDataBlock(AbstractWhistleDataUnit.class, roccaControl.roccaParameters.getWmDataSource());
        	if (whistleSourceData == null) {
        		return;
        	}

        	setParentDataBlock(whistleSourceData);
        	whistleSourceData.getRawSourceDataBlock2().setNaturalLifetimeMillis(5000);
        	this.channelMap = whistleSourceData.getChannelMap();
        	
        	// 2014-10-13 serialVersionUID 14 check if we are also using an FFT data block as a source.
        	// If so, we just removed ourselves as an observer when we set a different parent
        	// block.  So, add us back as an observer here.  Do the same with the
        	// click detector
        	// 2016/01/04 serialVersionUID=23 added multithread flag
            if (roccaControl.roccaParameters.weAreUsingFFT() && fftDataBlockIn != null) {
            	fftDataBlockIn.addObserver(this, multithread);
            }
            
        } else {
        	if (whistleSourceData!=null) {
        		whistleSourceData.deleteObserver(this);
        	}
        }
        /* if user has specified a click detector source, subscribe to it */
        // 2014-10-13 serialVersionUID 14 change elseif to if, so that we can use multiple sources
        //} else if (roccaControl.roccaParameters.weAreUsingClick()) {
        if (roccaControl.roccaParameters.weAreUsingClick()) {
        
//        	clickSourceData = (ClickDataBlock) PamController.getInstance().
//        			getDataBlock(ClickDetection.class, roccaControl.roccaParameters.getClickDataSource());
//        	ClickSourceData = (TrackedClickDataBlock) PamController.getInstance().
//        			getDataBlock(ClickDetection.class, roccaControl.roccaParameters.getClickDataSource());
//        	if (ClickSourceData == null) {
//        		return;
//        	} else {
//    			ClickSourceData.addObserver(this);
//        	}
        	PamDataBlock genericBlock = PamController.getInstance().
        			getDataBlock(ClickDetection.class, roccaControl.roccaParameters.getClickDataSource());
//        	String thisBlock = genericBlock.getClass().getName();
        	if (genericBlock == null) {
        		return;
        	}
        	if (genericBlock.getClass().getName() == "clickDetector.ClickDataBlock") {
        		autoClickSourceData = (ClickDataBlock) genericBlock;
            	// 2016/01/04 serialVersionUID=23 added multithread flag
    			autoClickSourceData.addObserver(this,multithread);
            	setParentDataBlock(autoClickSourceData);
            	autoClickSourceData.getRawSourceDataBlock2().setNaturalLifetimeMillis(5000);
            	this.channelMap = autoClickSourceData.getChannelMap();
            	
            	// 2014-10-13 serialVersionUID 14 check if we are also using an FFT data block as a source.
            	// If so, we just removed ourselves as an observer when we set a different parent
            	// block.  So, add us back as an observer here.  Do the same check with
            	// the whistle and moan detector
            	// 2016/01/04 serialVersionUID=23 added multithread flag
                if (roccaControl.roccaParameters.weAreUsingFFT() && fftDataBlockIn != null) {
                	fftDataBlockIn.addObserver(this,multithread);
                }
                if (roccaControl.roccaParameters.weAreUsingWMD() && whistleSourceData != null) {
                	whistleSourceData.addObserver(this,multithread);
                }
                
        	} else if (genericBlock.getClass().getName()=="clickDetector.TrackedClickDataBlock") {
        		manClickSourceData = (TrackedClickDataBlock) genericBlock;
    			manClickSourceData.addObserver(this,multithread);   	// 2016/01/04 serialVersionUID=23 added multithread flag
            	setParentDataBlock(manClickSourceData);
            	manClickSourceData.getRawSourceDataBlock2().setNaturalLifetimeMillis(5000);
            	this.channelMap = manClickSourceData.getChannelMap();
            	
            	// 2014-10-13 serialVersionUID 14 check if we are also using an FFT data block as a source.
            	// If so, we just removed ourselves as an observer when we set a different parent
            	// block.  So, add us back as an observer here.  Do the same check with
            	// the whistle and moan detector
            	// 2016/01/04 serialVersionUID=23 added multithread flag
                if (roccaControl.roccaParameters.weAreUsingFFT() && fftDataBlockIn != null) {
                	fftDataBlockIn.addObserver(this,multithread);
                }
                if (roccaControl.roccaParameters.weAreUsingWMD() && whistleSourceData != null) {
                	whistleSourceData.addObserver(this,multithread);
                }
                
        	} else {
        		return;
        	}
        	
        	clickNoiseSourceData = (NoiseDataBlock) PamController.getInstance().
        			getDataBlock(ClickDetection.class, roccaControl.roccaParameters.getClickNoiseDataSource());
        	if (clickNoiseSourceData == null) {
        		return;
        	} else {
            	// 2016/01/04 serialVersionUID=23 added multithread flag
        		clickNoiseSourceData.addObserver(this,multithread);
        	}
        } else {
        	if(autoClickSourceData!=null) {
            	autoClickSourceData.deleteObserver(this);        		
        	}
        	if(manClickSourceData!=null) {
        		manClickSourceData.deleteObserver(this);
        	}
        	if(clickNoiseSourceData!=null) {
        		clickNoiseSourceData.deleteObserver(this);
        	}
        }
        
        // if we are using GPS, set up the source
        if (roccaControl.roccaParameters.weAreUsingGPS()) {
        	gpsSourceData = (GPSDataBlock) PamController.getInstance().
        			getDataBlock(GpsDataUnit.class, roccaControl.roccaParameters.getGpsSource());
        	if (gpsSourceData == null) {
        		return;
        	}
        }
   }


    /**
     * this method called when new data arrives
     */
    @Override
    public void newData (PamObservable o, PamDataUnit arg) {
    	
    	/* if this is a whistle and moan detection, convert to RoccaDataBlock,
    	 * calculate the statistics, classify and update the side panel.
    	 * serialVersionUID 9 Noise Data Samples
    	 * if there are any errors getting the raw data from the WMD, exit
    	 */
		if (o == whistleSourceData) {
			RoccaContourDataBlock rcdb = newWMDetectorData((ConnectedRegionDataUnit) arg);
			if (rcdb==null) {
				return;
			}
			
			// 2017/12/4 set the natural lifetime to Integer.Max, so that we definitely keep all of the data
			// units during this code block.  Set the lifetime back to 0 at the end of the block
			/*
			 * DG June '22 made sure this is the case when the function returns early !
			 */
			rcdb.setNaturalLifetimeMillis(Integer.MAX_VALUE);
			rcdb.calculateStatistics();
			
			// only classify if the user has selected a classifier
			// serialVersionUID=24 2016/08/10 added
			// 2021/05/18 always try to classify, since we now allow Rocca to run without a classifier.  But only try to prune
			// if we are using a classifier
			if (roccaControl.roccaParameters.isClassifyWhistles()) {

				// serialVersionUID=25 2017/03/28 added pruning params for ONR/LMR project N00014-14-1-0413
				// classifiers.  Check if the loaded classifier model filename matches one of the classifier
				// names created for the project.  If so, compare the whistle to the parameters used to prune
				// the datasets and exit if the whistle falls outside of the thresholds
				if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("TemPacWhist.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) < 5000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) > 25000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMIN) < 3000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMIN) > 18000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.1 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 1.5 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) < 2000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) > 28000. )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
				if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("HIWhist.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) < 5000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) > 25000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMIN) < 4000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMIN) > 15000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.1 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 1.0 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) < 4000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) > 60000.  ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQRANGE) < 800. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQRANGE) > 14000.  )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
				if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("NWAtlWhist.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) < 10600. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQMAX) > 27000. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.15 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 2.5 ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) < 9100. ||
						 rcdb.getContour().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN) > 82000. )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
			}
			
			roccaClassifier.classifyContour2(rcdb);
	        
	        /* check the side panel for a detection number.  If one has not yet been created,
	         * create a default one now.  The user can always rename it later
	         */
	        String sNum = roccaControl.roccaSidePanel.getSightingNum();
	        if (sNum.equals(RoccaSightingDataUnit.NONE)) {
//                sNum = roccaControl.roccaSidePanel.sidePanel.addASighting("S001");
                sNum = roccaControl.roccaSidePanel.sidePanel.addASighting(autoEventName);	        // 2017/10/14 added
            }	        
	        updateSidePanel(rcdb, false);	// serialVersionUID=22 2015/06/13 added boolean to indicate this is not a click
	        saveContourPoints(rcdb, rcdb.getChannelMap(), ++numDetections, sNum);
	        saveContourStats(rcdb, rcdb.getChannelMap(), numDetections, sNum);
	        saveContour(rcdb, rcdb.getChannelMap(), numDetections, sNum);
			rcdb.setNaturalLifetimeMillis(0);

	    /* if this is a click detection (signal, not noise) */
		} else if (o==manClickSourceData || o==autoClickSourceData) {
			
			// For some reason newData gets called twice each time a click is selected
			// by the user.  Until I can find the reason why this occurs, use this hack
			// to simply keep track of the current click and skip the rest of the code if it
			// comes up a second time
			// Also skip if this click type does not match one of the ones the user would
			// like to analyze.  Limit this to the autodetections only (i.e. always measure manual detections).
			ClickDetection thisDetection = (ClickDetection) arg;
			boolean useClick = false;
			int[] clickList = roccaControl.roccaParameters.getClickTypeList();
			if (clickList==null || clickList.length==0) {
				useClick = false;
			} else {
				for (int i=0; i<clickList.length;i++) {
					if (clickList[i]==(int) thisDetection.getClickType()) {
						useClick = true;
					}
				}
			}
			
			// store current click id and test for duplicate
			//if (clickIDs.contains(System.identityHashCode(thisDetection))) {
		    //    System.out.println("Duplicate Click id =" + System.identityHashCode(thisDetection));				
			//} else {
			//	System.out.println("No duplicate id ="  + System.identityHashCode(thisDetection));
			//}
				
			clickIDs.add(System.identityHashCode(thisDetection));
			if ( (thisDetection) !=prevClick && (useClick || o==manClickSourceData)) {

				// create a new RoccaContourDataBlock with the current click
				RoccaContourDataBlock rcdb = newClickDetectorData(thisDetection);
				if (rcdb==null) {
					return;
				}
				
				// 2017/12/4 set the natural lifetime to Integer.Max, so that we definitely keep all of the data
				// units during this code block.  Set the lifetime back to 0 at the end of the block
				rcdb.setNaturalLifetimeMillis(Integer.MAX_VALUE);
				
				// save the current click to compare against later
				prevClick = thisDetection;
				
				// pass the latest noise data unit to the rcdb
				rcdb.setClickNoise(latestClickNoise);
				
				// run statistics on click
				rcdb.calculateClickStats();
				
				// classify click, if user has selected a click classifier
				// serialVersionUID=24 2016/08/10 added
				//rcdb.setClassifiedAs(RoccaRFModel.AMBIG);
				// 2021/05/18 always try to classify, since we now allow Rocca to run without a classifier.  But only try to prune
				// if we are using a classifier
				if (roccaControl.roccaParameters.isClassifyClicks()) {
					
					// serialVersionUID=25 2017/03/28 added pruning params for ONR/LMR project N00014-14-1-0413
					// classifiers.  Check if the loaded classifier model filename matches one of the classifier
					// names created for the project.  If so, compare the click to the parameters used to prune
					// the datasets and exit if the click falls outside of the thresholds
					if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("TemPacClick.model") &&
							(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 35. ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.005 ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
						return;
					}
					if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("HIClick.model") &&
							(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 40. ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.01 ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
						return;
					}
					if (roccaControl.roccaParameters.roccaClassifierModelFilename.getName().equals("NWAtlClick.model") &&
							(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 35. ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.005 ||
							 rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
						return;
					}
				}

				roccaClassifier.classifyContour2(rcdb);
				
		        // check the side panel for a detection number.  If one has not yet been created,
		        // create a default one now.  The user can always rename it later
				String sNum = roccaControl.roccaSidePanel.getSightingNum();
		        if (sNum.equals(RoccaSightingDataUnit.NONE)) {
//	                sNum = roccaControl.roccaSidePanel.sidePanel.addASighting("Clk001");
	                sNum = roccaControl.roccaSidePanel.sidePanel.addASighting(autoEventName);	        // 2017/10/14 added
		        }
		        
		        // serialVersionUID=22 2015/06/13
		        // add call to update side panel.  Set the isClick flag to True
		        updateSidePanel(rcdb, true);
		        saveContourStats(rcdb, rcdb.getChannelMap(), numDetections, sNum);
		        rcdb.setNaturalLifetimeMillis(0);
			}

			
	    /* if this is a click noise detection */
		} else if (o==clickNoiseSourceData) {
				
			// just save the data unit
			latestClickNoise = (ClickDetection) arg;
		}
		
        // 2017/10/14 added
		// if this is a PamDataBlock and the data unit is a DaqStatusDataUnit, the file has changed so update the auto-event name
		// and create a new event
		if (DaqStatusDataUnit.class.isAssignableFrom(arg.getClass())) {
			DaqStatusDataUnit dsdu = (DaqStatusDataUnit) arg;
			if (dsdu.getStatus().equals("Start") || dsdu.getStatus().equals("NextFile")) {
				eventNameCounter++;
				autoEventName = "AutoEvent" + String.valueOf(eventNameCounter);
				roccaControl.roccaSidePanel.sidePanel.addASighting(autoEventName);
			}
		}
    }

    
    public void updateSidePanel(RoccaContourDataBlock rcdb, boolean isClick) {
        /* check that the species list for the current classifier matches the
         * species list of the detection we're trying to save to.  If it
         * doesn't, force the user to create a new detection number.  This can
         * happen if the user captured a sound event after scrolling backwards
         * through the detection list to an older detection created with a
         * different classifier.
         * Note that this method also checks if the classifier species list
         * contains 'Ambig', and adds it if it doesn't.
		 * serialVersionUID=22 2015/06/13 changed from private to public so RoccaControl could call it
         */
    	String sNum = roccaControl.roccaSidePanel.getSightingNum();
        RoccaSightingDataUnit roccaDataUnit = roccaControl.roccaSidePanel.rsdb.
                findDataUnitBySNum(sNum);
        if (roccaDataUnit == null) {
        	return;
        }
        String[] sNumList =  roccaDataUnit.getSpeciesAsString();
        String[] classifierList = RoccaSightingDataUnit.validateSpeciesList(
                roccaClassifier.getClassifierSpList());
        if (!Arrays.equals(sNumList, classifierList)) {
            
            /* if the arrays are not equal, throw an error and force the user
             * to create a new detection
             */
            String message = "Classifier species list does not match the \n" +
                    "encounter number species list.  You must create\n" +
                    "a new encounter for this whistle.";
            int messageType = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog
                    (null, message, "Species List conflict", messageType);
            String newSNum =
                    roccaControl.roccaSidePanel.sidePanel.addASighting(true);
            if (newSNum!=null) {
                sNum = newSNum;
            }

        } else {

            /* if the arrays are equal, add the current tree votes to the tree vote
             * tally on the sidepanel
             * serialVersionUID=15 2014/11/12 add start time to method call
             * serialVersionUID-22 2015/06/13 add duration and isClick flag to method call
             */
            roccaControl.roccaSidePanel.addSpeciesTreeVotes(sNum, rcdb.getTreeVotes());
            roccaControl.roccaSidePanel.incSpeciesCount(sNum, rcdb.getClassifiedAs(),rcdb.getStartTime(), rcdb.getDuration(), isClick);
        }
    }
    
    
    /**
     * Checks to see if classifier has been loaded
     * 
     * @return boolean indicating whether classifier has been loaded
     */
    public boolean isClassifierLoaded() {
        return classifierLoaded;
    }

    
    /**
     * Sets the classifier loaded flag
     * 
     * @param classifierLoaded true=loaded, false=not loaded
     */
    public void setClassifierLoaded(boolean classifierLoaded) {
        this.classifierLoaded = classifierLoaded;
    }

    
    /**
     * Method run on startup
     */
    @Override
	public void setupProcess() {
        // if the model hasn't been loaded yet, do that now
        if (!isClassifierLoaded()) {
            setClassifierLoaded(roccaClassifier.setUpClassifier());

            // if there was an error loading the model, return "Err"
            if (!isClassifierLoaded()) {
                System.err.println("Cannot load classifier");
            }
        }

        /* check if the EncounterStats file already exists.  If it does, give the
         * user a chance to load or rename it before it gets overwritten.
         */
        try {
            File fName = roccaControl.roccaParameters.getRoccaSightingStatsOutputFilename();

            if (fName.exists() & roccaControl.roccaSidePanel.getRSDB()
                    .getFirstUnit().getSightNum().equals(RoccaSightingDataUnit.NONE)) {

            	// reword warning message to make it clearer
            	// serialVersionUID=20 2015/05/20
                String message = "The Encounter Stats file specified by the Rocca Parameters:\n" +
                        fName.getAbsolutePath() + "\n" +
//                        "already exists.  , and it will be overwritten by\n" +
//                        "Rocca during use.  If you wish to load the contents of\n" +
//                        "the file into the Rocca sidebar, you can do so now.\n" +
//                        "If you wish to keep the file, you should rename it now\n" +
//                        "before it is overwritten.\n" +
//                        "If a database module is also being used, the encounter\n" +
//                        "information being loaded will NOT be saved to the database.";
						  "already exists.  If you wish to load this data into memory and\n" +
						  "continue recording to the file, press the Load File button.  Press\n" +
						  "the Overwrite File button to erase the existing data and start\n" +
						  "recording with an empty file.\n\n" +
						  "Note: the Encounters loaded will be missing the underlying detection\n" +
						  "information necessary to recalculate ancillary variables\n" +
						  "(e.g. time between detections, detection density, detections/second, etc).\n" +
						  "You will need to start a new Encounter before adding any new detections, \n" +
						  "to ensure all variables are calculated correctly.";
//                Object[] options = {"Load file", "Continue"};
                Object[] options = {"Load file", "Overwrite File"};
                int messageType = JOptionPane.WARNING_MESSAGE;
                int n = JOptionPane.showOptionDialog
                        (null,
                        message,
                        "Sighting Stats file already exists!",
                        JOptionPane.YES_NO_OPTION,
                        messageType,
                        null,
                        options,
                        options[0]);

                /* if the user wants to load the file, load it */
                if (n == JOptionPane.YES_OPTION) {
                    roccaControl.roccaSidePanel.sidePanel.loadSighting();
                }
            }
        } catch(Exception ex) {
            System.out.println("Problem checking the Sighting Stats file");
//            ex.printStackTrace();
        }
    }

    
    /**
     * Takes an AbstractWhistleDataUnit created in the Whistle and Moan detector,
     * and copies the data into a RoccaContourDataBlock for further analysis and
     * classification
     * 
     * serialVersionUID = 9
     * if either firstIndx or lastIndx cannot be determined, return a null to the calling method
     * 
     * @param wmDataUnit the ConnectedRegionDataUnit containing the whistle
     * @return a RoccaContourDataBlock containing the whistle's time/freq pairs
     */
    public RoccaContourDataBlock newWMDetectorData(ConnectedRegionDataUnit wmDataUnit) {

		/* get a reference to the raw data so we can save a wav clip later */
        PamRawDataBlock prdb = wmDataUnit.getParentDataBlock().getRawSourceDataBlock2();
    	roccaControl.roccaParameters.setChannelMap(wmDataUnit.getChannelBitmap());
        
        /* get a list of the time slices that the whistle occupies.  The 'connectedRegion' class contains
         * this information, and the time slice list has details (timeInMilliseconds, peakFreq, etc) about each slice */
		List<SliceData> sliceData = wmDataUnit.getConnectedRegion().getSliceData();

		/* create a new PamRawDataBlock, a subset of the prdb that will contain only
		 * the raw data in the current whistle */
//		PamRawDataBlock newBlock = new PamRawDataBlock(
//				prdb.getDataName(),
//				this,
//				roccaControl.roccaParameters.getChannelMap(),
//				prdb.getSampleRate(),
//				false);

		/* find the index number of the PamRawDataUnit closest to the start time
		 * of the first FFT data unit, and in the lowest channel
		 * position to be saved.  Use the .getPreceedingUnit method to ensure
		 * that the start time of the raw data is earlier than the start time
		 * of the FFT data (otherwise we'll crash later in RoccaContour)
		 */
//		System.out.println(String.format("First time= %d", sliceData.get(0).getFftDataUnit().getTimeMilliseconds() ));
//		System.out.println(String.format("First startSample= %d", sliceData.get(0).getFftDataUnit().getStartSample() ));
//		System.out.println(String.format("PRDB first time= at %d", prdb.getFirstUnit().getTimeMilliseconds() ));
//		System.out.println(String.format("PRDB first startSample= %d", prdb.getFirstUnit().getStartSample() ));

		int[] lowestChanList = new int[1];
		lowestChanList[0] =
				PamUtils.getLowestChannel(roccaControl.roccaParameters.channelMap);
		RawDataUnit firstRDU = prdb.getPreceedingUnit(
				sliceData.get(0).getFftDataUnit().getTimeMilliseconds(),
				PamUtils.makeChannelMap(lowestChanList));
		int firstIndx = prdb.getUnitIndex(firstRDU);
		if (firstIndx==-1) {
	        int newTime;
	        if (prdb.getNaturalLifetimeMillis() > Integer.MAX_VALUE/2) {
	        	newTime = Integer.MAX_VALUE;
	        } else {
	        	newTime = prdb.getNaturalLifetimeMillis()*2;
	        }
	        // stop it getting silly.
	        newTime = Math.min(newTime, roccaControl.getMaxDataKeepTime());
			System.out.println("RoccaProcess: Cannot determine firstIndx, raw data lifetime = " + prdb.getNaturalLifetimeMillis() + " ms");
	        prdb.setNaturalLifetimeMillis(newTime); // increase the lifetime to try and prevent this from happening again
			return null;
		}
		long startSample = firstRDU.getStartSample();
		long startMillis = firstRDU.getTimeMilliseconds();

		/* find the index number of the PamRawDataUnit closest to the start time
		 * of the last FFT data unit, and in the highest channel
		 * position to be saved.  Use the .getNextUnit method to ensure
		 * that the start time of the raw data is later than the start time
		 * of the FFT data (otherwise we'll crash later in RoccaContour)
		 */		 
//		System.out.println(String.format("Last time= %d", sliceData.get(sliceData.size()-1).getFftDataUnit().getTimeMilliseconds() ));
//		System.out.println(String.format("Last startSample= %d", sliceData.get(sliceData.size()-1).getFftDataUnit().getStartSample() ));
//		System.out.println(String.format("PRDB last time= at %d", prdb.getLastUnit().getTimeMilliseconds() ));
//		System.out.println(String.format("PRDB last startSample= %d", prdb.getLastUnit().getStartSample() ));

		int[] highestChanList = new int[1];
		highestChanList[0] =
				PamUtils.getHighestChannel(roccaControl.roccaParameters.channelMap);
        long timeToSearch = sliceData.get(sliceData.size()-1).getFftDataUnit().getTimeMilliseconds();
        RawDataUnit lastRDU = prdb.getNextUnit(
        		timeToSearch,
				PamUtils.makeChannelMap(highestChanList));
		int lastIndx = prdb.getUnitIndex(lastRDU);
		if (lastIndx==-1) {
	        int newTime;
	        if (prdb.getNaturalLifetimeMillis() > Integer.MAX_VALUE/2) {
	        	newTime = Integer.MAX_VALUE;
	        } else {
	        	newTime = prdb.getNaturalLifetimeMillis()*2;
	        }
	        newTime = Math.min(newTime, roccaControl.getMaxDataKeepTime());
	        System.out.println("RoccaProcess: Cannot determine lastIndx, raw data lifetime = " + prdb.getNaturalLifetimeMillis() + " ms");
	        prdb.setNaturalLifetimeMillis(newTime); // increase the lifetime to try and prevent this from happening again
	        return null;
		}
		long endMillis = lastRDU.getTimeMilliseconds();

		/* Having problems releasing PamRawDataBlock to Java Garbage Collection, and is slowly eating up memory.  Since all
		 * we need is the actual raw data to save to the wav clip, save that info directly into a RoccaDetectionWavInfo object
		 * and keep a reference to that instead.
		 */
		/* add the units, from firstIndx to lastIndx, to the new PamRawDataBlock and save this to the contour data block */
//		for (int j = firstIndx; j <= lastIndx; j++) {
//			newBlock.addPamData
//			(prdb.getDataUnit
//					(j, PamDataBlock.REFERENCE_CURRENT ));
//		}
//		roccaContourDataBlock.setPrdb(newBlock);
		double[][] wavData = null;
		try {
//			wavData = prdb.getSamples(startSample, lastIndx-firstIndx+1, roccaControl.roccaParameters.getChannelMap());
			wavData = prdb.getSamplesForMillis(startMillis, endMillis-startMillis, roccaControl.roccaParameters.getChannelMap());
		} catch (RawDataUnavailableException e) {
	        System.out.println("RoccaProcess: Error trying to capture raw data for wav clip");
			e.printStackTrace();
		}
		
    	// define a new RoccaContourDataBlock and RoccaContourDataUnit
    	roccaControl.roccaParameters.setChannelMap(wmDataUnit.getChannelBitmap());
    	RoccaContourDataBlock roccaContourDataBlock =
    			new RoccaContourDataBlock(this,
    					roccaControl.roccaParameters.getChannelMap());
    	roccaContourDataBlock.setSampleRate(prdb.getSampleRate(), false);
		roccaContourDataBlock.setWavInfo(wavData);
		

		/* get an array of all the peak frequencies */
    	double[] whistleFreqs = wmDataUnit.getFreqsHz();

    	/* loop through the slices one at a time, creating a RoccaContourDataUnit for each */
    	/*
    	 * SliceData is in a liked list so get() really bad. Use iterator. 
    	 */
    	RoccaContourDataUnit rcdu = null;    	
    	long prevTime = 0;
    	ListIterator<SliceData> sliceIt = sliceData.listIterator();
    	int i = 0;
    	while (sliceIt.hasNext()) {
    		SliceData aSlice = sliceIt.next();
			rcdu = new RoccaContourDataUnit (
					aSlice.getFftDataUnit().getTimeMilliseconds(),
					aSlice.getFftDataUnit().getChannelBitmap(),
					aSlice.getFftDataUnit().getStartSample(),
					aSlice.getFftDataUnit().getSampleDuration() );
			
			/* set the peak frequency */
			rcdu.setPeakFreq(whistleFreqs[i]);
			
			/* set the time in milliseconds */
			rcdu.setTime(aSlice.getFftDataUnit().getTimeMilliseconds());
			
			/* set the duty cycle, energy and windowRMS params to 0 since we're not using them */
			rcdu.setDutyCycle(0);
			rcdu.setEnergy(0);
			rcdu.setWindowRMS(0);
			
			/* add the data unit to the data block */
			roccaContourDataBlock.addPamData(rcdu);	
			
			i++;
    	}
    	
    	// specify this is a whistle, not a click
    	roccaContourDataBlock.setAsAWhistle(true);
    	
    	return roccaContourDataBlock;
    }

    /**
     * Takes a ClickDetection created in the Click detector,
     * and copies the data into a RoccaContourDataBlock for further analysis and
     * classification.  Create a RoccaContourDataUnit and populate it with information
     * from the clickDetection
     * 
     * @param clickDetection a ClickDetection object
     * @return
     */
    public RoccaContourDataBlock newClickDetectorData(ClickDetection clickDetection) {

    	// define a new RoccaContourDataBlock
    	roccaControl.roccaParameters.setChannelMap(clickDetection.getChannelBitmap());
    	RoccaContourDataBlock rcdb =
    			new RoccaContourDataBlock(this,
    					roccaControl.roccaParameters.getChannelMap());    	
    	rcdb.setClickDetection(clickDetection);
    	rcdb.setAsAWhistle(false);
    	return rcdb;
    }
    	
    /**
     * Save the clip as a wav file
     *
     * @param rcdb the contour data block containing the raw data to save
     * @return boolean indicating success
     */
    private boolean saveContour(RoccaContourDataBlock rcdb, 
    		int channel,
    		int thisDetection,
    		String sNum) {

//        if (rcdb.getPrdb() == null) {
//            System.out.println("RoccaProcess: No source audio data found");
//            return false;
//        }
//        
//        if (rcdb.getPrdb().getFirstUnit() == null) {
//            System.out.println("RoccaProcess: PamRawDataBlock is empty");
//            return false;
//        }
//        
//        long currentStartSample = rcdb.getPrdb().getFirstUnit().getStartSample();
//        long deltaMS = (rcdb.getPrdb().getLastUnit().getTimeMilliseconds()-
//        		rcdb.getPrdb().getFirstUnit().getTimeMilliseconds());
//        float deltaS = ((float) deltaMS)/1000;
//        float dur = deltaS*rcdb.getPrdb().getSampleRate();
//        int duration = (int) dur;
//
//        /* get the raw data */
//        double[][] rawDataValues = null;
//        try {
//        	rawDataValues = rcdb.getPrdb().getSamples(currentStartSample, duration, channel );
//        }
//        catch (RawDataUnavailableException e) {
//        	System.out.println(e.getMessage());	
//        }

    	double[][] rawDataValues = rcdb.getWavInfo();
        if (rawDataValues==null) {
            System.out.println(String.format
//                    ("Error obtaining raw data during contour save; currentStartSample = %d",
//                    currentStartSample));
            		("Error obtaining raw data during contour save"));
            return false;
        }

        /* figure out the filename, based on the input filename and the time */
        File contourFilename = getDataBlockFilename(rcdb, ".wav", channel, thisDetection, sNum);
        
      // if this file already exists, warn the user that it will be overwritten
      if (contourFilename.exists()) {
			String title = "Warning - Wav file already exists";
			String msg = "<html><p>The file</p><b> " + 
					contourFilename.getAbsolutePath() +
					"</b><br><br><p>already exists and will be overwritten with the new audio data.  " +
					"This will happen when re-analyzing data that has already been analyzed, but it may " +
					"also indicate that your filename template is inadequate and returning duplicate filenames.</p></html>";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.WARNING_MESSAGE, help);
      }


        /*
         * Replace with new single call to wav file writer. DG. 21.2.2016 
         */
//        /* create a new WavFile object */
//        WavFile wavFile = new WavFile(contourFilename.getAbsolutePath(), "w");
//
//        /* save the data to a wav file */
////        System.out.println(String.format("writing contour  to ... %s",
////                contourFilename));
//        wavFile.write(rcdb.getPrdb().getSampleRate(),
//                PamUtils.getNumChannels(channel),
//                rawDataValues );
        WavFileWriter.write(contourFilename.getAbsolutePath(), rcdb.getSampleRate(), rawDataValues);
        return true;
    }

    /**
     * Saves all contour stats to the contour stats summary file, as defined in
     * the Rocca Parameters.  If the file does not exist, it is created and a
     * header line is written.  If it does exist, data is appended to the end.
     * Each contour is 1 row.
     *
     * Note that the last few columns contain the random forest votes, and so
     * the number of columns depends on how many species are in the classifier.
     * This won't be a problem if the same classifier is always used, but if a
     * different classifier with a different number of species is tried out the
     * number of columns from one row to the next will not match.  More
     * importantly, the order of the species from one row to the next might
     * not match and you would never know unless you remember.  For this
     * reason, instead of saving the species names as the column headers (which
     * would only be accurate for the classifier selected when the file had
     * been created), the species names are written in the row AFTER the tree
     * votes.  The classifier name is also saved, for reference.  While this
     * is awkward, at least results from different classifiers can be
     * interpreted properly without having to guess which vote is for which species
     *
     * @return boolean indicating whether or not the save was successful
     */
    public boolean saveContourStats(RoccaContourDataBlock rcdb, 
    		int channel,
    		int thisDetection,
    		String sNum) {
    	return saveContourStats(rcdb, channel, thisDetection, sNum, "null");
    }

    
        /**
         * Saves all contour stats to the contour stats summary file, as defined in
         * the Rocca Parameters.  If the file does not exist, it is created and a
         * header line is written.  If it does exist, data is appended to the end.
         * Each contour is 1 row.
         *
         * Note that the last few columns contain the random forest votes, and so
         * the number of columns depends on how many species are in the classifier.
         * This won't be a problem if the same classifier is always used, but if a
         * different classifier with a different number of species is tried out the
         * number of columns from one row to the next will not match.  More
         * importantly, the order of the species from one row to the next might
         * not match and you would never know unless you remember.  For this
         * reason, instead of saving the species names as the column headers (which
         * would only be accurate for the classifier selected when the file had
         * been created), the species names are written in the row AFTER the tree
         * votes.  The classifier name is also saved, for reference.  While this
         * is awkward, at least results from different classifiers can be
         * interpreted properly without having to guess which vote is for which species
         *
         * @return boolean indicating whether or not the save was successful
         */
        public boolean saveContourStats(RoccaContourDataBlock rcdb, 
        		int channel,
        		int thisDetection,
        		String sNum,
        		String source) {

        // open file
        try {
            File fName = roccaControl.roccaParameters.roccaContourStatsOutputFilename;
//            System.out.println(String.format("writing contour stats to ... %s", fName.getAbsolutePath()));

            // write a header line if this is a new file
            if (!fName.exists()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
                String hdr = rcdb.createContourStatsHeader();
                writer.write(hdr);
                writer.newLine();
                writer.close();
            }
            
            // if we weren't passed a source to use, generate it
            if (source.equals("null")) {
            	source = (getDataBlockFilename(rcdb, ".csv", channel, thisDetection, sNum)).getAbsolutePath();
            }

            /* create a String containing the contour stats and associated
             * variables (filename, species list, detection number, etc) we
             * want to save, and save it to the file.  This method will also
             * create and populate a RoccaLoggingDataUnit
             */
            BufferedWriter writer = new BufferedWriter(new FileWriter(fName, true));
//            String contourStats = roccaContourDataBlock.createContourStatsString();
            String contourStats =
                    rcdb.createContourStatsString(source,thisDetection,sNum);
            writer.write(contourStats);
            writer.newLine();
            writer.close();

        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file");
            ex.printStackTrace();
            return false;
        } catch(IOException ex) {
            System.out.println("Could not write contour statistics to file");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Saves the contour points in the datablock in a csv file.  Saves the time
    * and peak frequency of each point, along with the duty cycle, energy, and
    * windows RMS
    *
     * @param rcdb the datablock containing the selected
     *                              whistle contour
     * @return saveContourPoints boolean indicating success or failure
     */
    public boolean saveContourPoints(RoccaContourDataBlock rcdb, 
    		int channel,
    		int thisDetection,
    		String sNum) {

        // figure out the filename, based on the input filename and the time
        File contourFilename = getDataBlockFilename(rcdb, ".csv", channel, thisDetection, sNum);

        // if this file already exists, warn the user that it will be overwritten
        if (contourFilename.exists()) {
  			String title = "Warning - Contour file already exists";
  			String msg = "<html><p>The file</p><b> " + 
  					contourFilename.getAbsolutePath() +
  					"</b><br><br><p>already exists and will be overwritten with the new contour data.  " +
  					"This will happen when re-analyzing data that has already been analyzed, but it may " +
  					"also indicate that your filename template is inadequate and returning duplicate filenames.</p></html>";
  			String help = null;
  			int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.WARNING_MESSAGE, help);
        }


        try {
//            System.out.println(String.format("writing contour points to ... %s",
//                    contourFilename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(contourFilename));

            // Write the header line
            String hdr = new String("Time [ms], Peak Frequency [Hz], Duty Cycle, Energy, WindowRMS");
            writer.write(hdr);
            writer.newLine();

            /* Cycle through the contour points one at a time.  Save the points
             * that are within the range selected by the user (points outside
             * of the range will have a frequency equal to the constant
             * NO_CONTOUR_HERE
             */
            RoccaContourDataUnit rcdu = null;
            int numPoints = rcdb.getUnitsCount();
            for (int i = 0; i < numPoints; i++) {
                rcdu = rcdb.getDataUnit(i, RoccaContourDataBlock.REFERENCE_CURRENT);
                if (rcdu.getPeakFreq() != RoccaParameters.NO_CONTOUR_HERE ) {
                    String contourPoints =   rcdu.getTime() + "," +
                            rcdu.getPeakFreq() + "," +
                            rcdu.getDutyCycle() + "," +
                            rcdu.getEnergy() + "," +
                            rcdu.getWindowRMS();
                    writer.write(contourPoints);
                    writer.newLine();
                }
            }
            writer.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file");
            ex.printStackTrace();
            return false;
        } catch(IOException ex) {
            System.out.println("Could not write contour points to file");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Generates a filename for the wav clip and the csv file containing the
     * contour points, based on the filename template stored in RoccaParameters
     *
     * @param rcdb The RoccaContourDataBlock containing the data to save
     * @param ext The filename extension (wav or csv)
     * @return A File
     */
    public File getDataBlockFilename
    		(RoccaContourDataBlock rcdb,
    		String ext,
    		int channel,
    		int thisDetection,
    		String sNum) {
        File soundFile = rcdb.getWavFilename();
		FileParts fileParts = new FileParts(soundFile);
		String nameBit = fileParts.getFileName();
        
        /* get the filename template */
        String filenameTemplate = roccaControl.roccaParameters.getFilenameTemplate();
        String filenameString = "";
        char c;

        /* step through the template, one character at a time, looking for codes */
        for ( int i=0,n=filenameTemplate.length(); i<n; i++ ) {
            c = filenameTemplate.charAt(i);

            /* if we've hit a symbolic code, determine which one it is and
             * substitute the correct text
             */
            if (c=='%') {
                if (i==n-1) break;
                i++;
                c = filenameTemplate.charAt(i);
                switch (c) {

                    /* source name */
                    case 'f':
//                        File soundFile = rcdb.getWavFilename();
//                        FileParts fileParts = new FileParts(soundFile);
//                        String nameBit = fileParts.getFileName();
                        filenameString = filenameString + nameBit;
                        break;

                    /* detection number */
                    case 'n':
                        filenameString = filenameString + sNum;
                        break;

                    /* detection tally */
                    case 'X':
                        filenameString = filenameString +
                                String.format("%d", thisDetection);
                        break;

                    /* channel/track number */
                    case 't':
                        filenameString = filenameString +
                                String.format("%d", channel);
                        break;

                    /* it has something to do with the date or time, so initialize
                     * some calendar variables
                     */
                    default:
                        long firstTime = rcdb.getFirstUnit().getTime();
                        Calendar cal = PamCalendar.getCalendarDate(firstTime);
                        DateFormat df = new SimpleDateFormat("yyyy");
                        DecimalFormat decf = new DecimalFormat(".#");
                        Double msAsDouble = 0.0;
                        int msAsInt = 0;

                        switch (c) {

                            /* year, 4 digits */
                            case 'Y':
                                df = new SimpleDateFormat("yyyy");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* year, 2 digits */
                            case 'y':
                                df = new SimpleDateFormat("yy");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* month */
                            case 'M':
                                df = new SimpleDateFormat("MM");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* day of the month */
                            case 'D':
                                df = new SimpleDateFormat("dd");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* day of the year (3 digits) */
                            case 'J':
                                df = new SimpleDateFormat("DDD");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* hour, 24-hour clock */
                            case 'H':
                                df = new SimpleDateFormat("kk");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* hour, 12-hour clock */
                            case 'h':
                                df = new SimpleDateFormat("hh");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* 'am' or 'pm' */
                            case 'a':
                                df = new SimpleDateFormat("a");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* minute */
                            case 'm':
                                df = new SimpleDateFormat("mm");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* second */
                            case 's':
                                df = new SimpleDateFormat("ss");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* second of the day (5 digits) */
                            case 'S':
                                int secOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
                                        cal.get(Calendar.MINUTE) * 60 +
                                        cal.get(Calendar.SECOND);
                                filenameString = filenameString +
                                        String.format("%05d", secOfDay);
                                break;

                            /* tenths of a second */
                            case 'd':
                                df = new SimpleDateFormat(".SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                msAsDouble = Double.valueOf(df.format(cal.getTime()).toString());
                                decf = new DecimalFormat(".#");
                                msAsInt = (int) (Double.valueOf(decf.format(msAsDouble))*10);
                                filenameString = filenameString + String.format("%01d", msAsInt);
                                break;

                            /* hundredths of a second */
                            case 'c':
                                df = new SimpleDateFormat(".SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                msAsDouble = Double.valueOf(df.format(cal.getTime()).toString());
                                decf = new DecimalFormat(".##");
                                msAsInt = (int) (Double.valueOf(decf.format(msAsDouble))*100);
                                filenameString = filenameString + String.format("%02d", msAsInt);
                                break;

                            /* thousandths of a second */
                            case 'i':
                                df = new SimpleDateFormat("SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* code not recognized; just use the % sign and character */
                            default:
                                filenameString = filenameString + "%" + c;
                                break;
                    }
                }

            /* if we haven't hit a code, just copy the character to the filename
             * string
             */
            } else {
               filenameString = filenameString + c;
            }
        }
        filenameString = filenameString + ext;
//       return fName;
       return new File(roccaControl.roccaParameters.roccaOutputDirectory.getAbsolutePath(),
               filenameString);
   }


	/**
	 * @return the whistleSourceData
	 */
	public AbstractWhistleDataBlock getWhistleSourceData() {
		return whistleSourceData;
	}


	/**
	 * @param whistleSourceData the whistleSourceData to set
	 */
	public void setWhistleSourceData(AbstractWhistleDataBlock whistleSourceData) {
		this.whistleSourceData = whistleSourceData;
	}


	/**
	 * @return the clickSourceData
	 */
//	public ClickDataBlock getClickSourceData() {
	public TrackedClickDataBlock getClickSourceData() {
		return manClickSourceData;
	}


	/**
	 * @param clickSourceData the clickSourceData to set
	 */
//	public void setClickSourceData(ClickDataBlock clickSourceData) {
	public void setClickSourceData(TrackedClickDataBlock clickSourceData) {
		this.manClickSourceData = clickSourceData;
	}


	/**
	 * @return the clickNoiseSourceData
	 */
	public NoiseDataBlock getClickNoiseSourceData() {
		return clickNoiseSourceData;
	}


	/**
	 * @param clickNoiseSourceData the clickNoiseSourceData to set
	 */
	public void setClickNoiseSourceData(NoiseDataBlock clickNoiseSourceData) {
		this.clickNoiseSourceData = clickNoiseSourceData;
	}


	/**
	 * @return the latestClickNoise
	 */
	public ClickDetection getLatestClickNoise() {
		return latestClickNoise;
	}


	/**
	 * @param latestClickNoise the latestClickNoise to set
	 */
	public void setLatestClickNoise(ClickDetection latestClickNoise) {
		this.latestClickNoise = latestClickNoise;
	}


	public GPSDataBlock getGpsSourceData() {
		return gpsSourceData;
	}
	
	

}
