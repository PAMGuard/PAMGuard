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
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Parameters for Rocca<br>
 * 		modified contour calculations to better match original Matlab routine<br>
 * <p>
 * serialVersionUID = 2<br>
 * 		2012/7/23<br>
 * 		added whistle and moan as a potential data source for classifier<br>
 * </p><p>
 * serialVersionUID = 3<br>
 * 		2012/8/12<br>
 * 		corrected problem with not resetting inflection and sweep counts when
 * 		reclassifying, resulting in doubling and tripling of the parameters<br>
 * </p><p>
 * serialVersionUID = 4<br>
 * 		2012/8/16<br>
 * 		corrected problem with freqmin=-1 when contour points dragged and end
 * 		point reselected.<br>
 * </p><p>
 * serialVersionUID = 5<br>
 * 		2012/10/12<br>
 * 		added 'pick points' option to trace out contours<br>
 * </p><p>
 * serialVersionUID = 6<br>
 * 		2012/11/11<br>
 * 		fixed problem with single step being counted multiple times<br>
 * </p><p>
 * serialVersionUID = 7<br>
 * 		2013/9/29<br>
 * 		added 2nd stage classifier<br>
 * </p><p>
 * serialVersionUID = 8<br>
 * 		2013/12/13<br>
 * 		added error checking in RoccaProcess, to catch if a PamRawDataBlock is empty<br>
 * 		added error checking in RoccaContourDataBlock to catch if the filename for the
 * 		source wav cannot be found<br>
 * </p><p>
 * serialVersionUID = 9<br>
 * 		2014/01/18<br>
 * 		added more error checking in RoccaProcess to catch if firstIndx or lastIndx cannot
 * 		be determined from the data sent by the WMD<br>
 * </p><p>
 * serialVersionUID = 10<br>
 * 		2014/02/04<br>
 * 		in case numInflections &lt; = 1, Rocca now writes a "0" to the following parameters
 * 		instead of "999":<br>
 * 			INFLMAXDELTA<br>
 * 			INFLMINDELTA<br>
 * 			INFLMAXMINDELTA<br>
 * 			INFLMEANDELTA<br>
 * 			INFLSTDDEVDELTA<br>
 * 			INFLMEDIANDELTA<br>
 * </p><p>
 * serialVersionUID = 11<br>
 * 		2014/04/09<br>
 * 		added decimal places to the date/time output in column B of RoccaContourStats<br>
 * 		added End Date/Time column to RoccaContourStats<br>
 * </p><p>
 * serialVersionUID = 12<br>
 * 		2014/05/07<br>
 * 		integrated with click detector<br>
 * 		rev 12c 2014/07/26 released to Tina for beta testing<br>
 * </p><p>
 * serialVersionUID = 13<br>
 * 		2014/09/09<br>
 * 		added notes tab to Parameters dialog and added 4 string variables to params<br>
 * </p><p>
 * serialVersionUID = 14<br>
 * 		2014/10/13<br>
 * 		*allow multiple sources<br>
 * 		*changed click duration calculation in RoccaContourDataBlock.calculateClickStats().  Instead
 * 		of calling clickDetection.clickLength, we now call SweepClassifier.getClickLength<br>
 * 		*released as Pamguard version 1_12_50<br>
 * </p><p>
 * serialVersionUID = 15<br>
 * 		2014/11/12<br>
 * 		* add time calcs to school stats file<br>
 * 		* released as Pamguard version 1.12.54 (? not positive about this)<br>
 * </p><p>
 * serialVersionUID = 16<br>
 * 		2014/12/28<br>
 * 		* remove 'GMT' from start and end times in RoccaContourStats.csv, and just use<br>
 * 		* it in the header row.  Allows easier manipulation of dates in Excel<br>
 * 		* released as Pamguard version 1.12.54b<br>
 * </p><p>
 * serialVersionUID = 17<br>
 * 		2015/01/14<br>
 * 		* added geographic location and channels to notes tab, as well as sample rate<br>
 * 		* RoccaContourStats.csv file<br>
 * 		* released as Pamguard version 1.13.02b<br>
 * </p><p>
 * serialVersionUID = 18<br>
 * 		2015/04/20<br>
 * 		* change the RoccaLoggingDataBlock unit lifetime from infinite to 10 seconds, and
 * 		  don't override the clearAll method anymore.  There were problems with the memory
 * 		  being overloaded and crashing pamguard when the Whistle and Moan Detector was run
 * 		  over multiple hours.<br>
 * 		* released as Pamguard version 1.13.03b<br>
 * </p><p>
 * serialVersionUID = 19<br>
 * 		2015/05/17<br>
 * 		* added median and variance classes to PamMaths package<br>
 * 		* added mean, median and variance calcs to ZeroCrossingStats class<br>
 * 		* added mean, median and variance of time between zero crossings to the list
 * 		  of click stats calculated by RoccaContourDataBlock.  These stats were also
 * 		  added to the RoccaContourStats enum and RoccaStatsLogger class.  Note that
 * 		  the Click Detector params had never been added to RoccaStatsLogger, so everything
 * 		  was included this time.<br>
 * 		* released as Pamguard version 1.13.03c<br>
 * </p><p>
 * serialVersionUID = 20<br>
 * 		2015/05/20<br>
 * 		* added Encounter ID and Known Species to sidebar<br>
 * 		* added test for change in Sources tab of Rocca Parameters Dialog.  If no change has
 * 		  occurred, the Observer list will not be touched.  This was causing Pamguard to crash
 * 		  if changes were made to the Parameters Dialog (the notes or output filenames) while
 * 		  Pamguard was running and handling multiple detections at the same time<br>
 * 		* released as Pamguard version 1.13.03d<br>
 * </p><p>
 * serialVersionUID = 21<br>
 * 		2015/05/31<br>
 * 		* changed Viewer Mode Rocca Event Analysis menu item.  Instead of displaying a submenu
 * 		  listing all of the available events, load a dialog with a scrolling text box<br>
 * 		* released as Pamguard version 1.13.03e<br>
 * </p><p>
 * serialVersionUID = 22<br>
 * 		2015/06/13<br>
 * 		* added ancillary variables to encounter stats for both whistles and clicks.  Up to now, clicks
 * 		  had not been included in encounter calculations.  Because we want to calc ancillary variables
 * 		  for clicks as well as whistles, clicks are now sent to the sidebar but are NOT run through
 * 		  a classifier first - they default to 'ambig' species.  To do this, we now pass the duration
 * 		  as well as the start time to the incSpeciesCount methods in RoccaSidePanel.  Note that ancillary
 * 		  variable calculations can be enabled/disabled for whistles and clicks separately in the
 * 		  parameters dialog.<br>
 * 		  Ancillary variables include:<br>
 * 			 whistle density (sum of individual durations / encounter length)<br>
 * 			 whistle overlap (total number of seconds that two whistles overlap)<br>
 * 		* Encounter duration calculation was also changed to go from the start of the first whistle OR click
 * 		  to the end of the last whistle or click<br>
 * 		* fixed bug that didn't read the encounter stats file in correctly (did not take into account timeParams variables)<br>
 * 		* fixed bug that could go past the end of the frequency array when calculating max freq bin in RoccaContour.getMaxFreqIndexFromFFTData<br>
 * 		* found missing graphics for help files<br>
 * 		* updated help files, including Click Detector help<br>
 * 		* released for testing as Pamguard version 1.13.03f on 18 July 2015<br>
 * </p><p>
 * serialVersionUID = 22<br>
 * 		2015/08/31<br>
 * 		* fixed bug in encounter stats - was incorrectly calculating average time between whistles/clicks<br>
 * 		* released as Pamguard version 1.13.05b<br>
 * </p><p>
 * serialVersionUID = 22<br>
 * 		2015/09/09<br>
 * 		* fixed memory leak issue in ViewerMode, whereby logging data units are created but never removed.  Changes made<br>
 * 		* released as Pamguard version 1.13.05c<br>
 * </p><p>
 * serialVersionUID = 22<br>
 * 		2015/09/13<br>
 * 		* changed SweepClassifierWorker method createLengthData - converted lengthData to local variable tempLengthData
 * 		  to prevent NullPointerException due to threading issues<br>
 * 		* released as Pamguard version 1.13.05d<br>
 * </p><p>
 * serialVersionUID = 22<br>
 * 		2015/09/15<br>
 * 		* changed whistle/click overlap param to average overlap, by dividing by encounter length<br>
 * 		* released as Pamguard version 1.13.05d<br>
 * </p><p>
 * serialVersionUID = 23<br>
 * 		2015/10/10<br>
 * 		* Requested changes from ONR Debrief 2015/10/06<br>
 * 		* remove white spaces and parenthesis characters from RoccaContourStats header row<br>
 * 		* report 'N/A' for min time between detections, if there is only 1 detection<br>
 * 		* released as Pamguard version 1.14.00b<br>
 * </p><p>
 * serialVersionUID = 23<br>
 * 		2016/01/04<br>
 * 		* last requested change from ONR Debrief 2015/10/06<br>
 * 		* add ability to load standard notes and use in RoccaParameters dialog window<br>
 * 		* fixed Rocca multi-threading problem which caused Click Detector to send same click multiple times<br>
 * 		* released as Pamguard version 1.14.00c<br>
 * </p><p>
 * serialVersionUID = 24<br>
 * 		2016/08/10<br>
 * 		* added selection of click and event classifiers to parameters dialog<br>
 * 		* added GPS as a potential source (to be used in conjunction with event classifier)<br>
 * 		* removed references in parameters dialog to the original 2-stage classifier, which
 * 		  required 2 separate classifier files to function<br>
 * 		* released as Pamguard version 1.15.04d<br>
 * </p><p>
 * serialVersionUID = 25<br>
 * 		2017/03/28<br>
 * 		* added pruning for auto whistle/click detector classifiers.  Note that the pruning parameters are
 * 		  specific to the classifiers developed for the ONR/LMR project N00014-14-1-0413 and documented in the report
 * 		  "Development of automated whistle and click classifiers for odontocete species in the western
 * 		  Atlantic Ocean, Temperate Pacific and the waters surrounding the Hawaiian Islands" J.N. Oswald and
 * 		  T.M. Yack, 2016.  The parameters are hard-coded into the RoccaProcess.newData method and are applied
 * 		  when the classifier model file selected by the user matches a specific name.<br>
 * </p><p>
 * 		2017/04/10<br>
 * 		* modified event calcs to match the final params that were actually used for TempPac/Hawaii/Atlantic
 * 		  classifiers.  StartHr, ProportionWhists and ProportionClicks were added.  No params were removed to 
 * 		  maintain backwards-compatibility (even though some are not used in the classifier) 
 * </p><p>
 * 		2017/10/14<br>
 * 		* subscribe to AcquisitionProcess.daqStatusDataBlock and monitor for changes in file name.  When a change
 * 		* occurs, update the event name automatically
 * </p><p>
 * 		2019/03/04<br>
 * 		* copied over the click classification code from RoccaProcess.newData to RoccaControl.analyzeOfflineClicks.
 * 		  Didn't realize that in Viewer mode, clicks were not being classified and were simply set to Ambig.  When
 * 		  RocccaProcess was upgraded with actual click classification, I forgot to change RoccaControl.
 * </p>
 * 
 * 
 * @author Michael Oswald
 */
public class RoccaParameters implements Serializable, Cloneable, ManagedParameters {

    /** for serialization */
    public static final long serialVersionUID = 25;

    /** constant used to define areas of spectrogram that do not contain whistle */
    public static final int NO_CONTOUR_HERE = -1;

    /** use the first (0th) fft datablock in the model */
    int fftDataBlock = 0;

    /** Bitmap of channels to be used - use all available */
	int channelMap = 0xFFFF;

    /** noise sensitivity parameter (in percent) */
    double noiseSensitivity = 11.0;

    /** size of freq bin (around peak freq) to calc energy spectrum (Hz) */
    int energyBinSize = 500;

    /** directory to store contours and statistics */
    File roccaOutputDirectory = new File("C:\\");

    /** filename for contour statistics storage */
    File roccaContourStatsOutputFilename = new File("C:\\RoccaContourStats.csv");

    /** filename for contour statistics storage */
    File roccaSightingStatsOutputFilename = new File("C:\\EncounterStats.csv");

    /** filename for first stage classification model */
    File roccaClassifierModelFilename = new File("C:\\RF8sp12att.model");
    
    /** filename for click classification model */
    File roccaClickClassifierModelFilename = new File("C:\\RF8sp12att.model");

    /** filename for event classification model */
    File roccaEventClassifierModelFilename = new File("C:\\RF8sp12att.model");

    /** filename for 2nd stage classification model (default null)
     * CURRENTLY NOT USED */
    // File roccaClassifier2ModelFilename = null;
    
    /**
     * index number of the attribute in the Stage 1 Classifier that will trigger the Stage 2 classifier
     * (default is -1, indicating no Stage 2 classifier)
     */
    int stage1Link = -1;
    
    /** the percent of classification tree votes required to explicitly classify
     * a contour.  Contours with less than the required votes are classified as
     * 'Ambig'
     */
    int classificationThreshold = 40;

    /** the percent of classification tree votes required for a species to
     * explicitly classify a sighting.  Sightings with less than the required
     * votes are classified as 'Ambig'
     */
    int sightingThreshold = 40;

    /** the filename template, modelled after the Ishmael template */
    String filenameTemplate = "Encounter%X-%f-Channel%t-%Y%M%D_%H%m%s";

    /**
     * the name of the whistle and moan data source
     */
    String wmDataSource;
    
    /**
     * the name of the click detector source
     */
    String clickDataSource;
    
    /**
     * the name of the click noise detector source
     */
    String clickNoiseDataSource;
    
    /**
     * boolean indicating whether to use the FFT as the source (true)
     */
    boolean useFFT = true;
    
    /**
     * boolean indicating whether to use Click Detector as the source (true)
     */
    boolean useClick = false;
    
    /**
     * boolean indicating whether to use Whistle and Moan Detector as the source (true)
     */
    boolean useWMD = false;
    
    /**
     * A list of click type indices to perform analysis on.  Note that this
     * is not a smart list; if one of the click types is deleted, this list
     * will not be valid but Rocca won't know.  For now, it's up to the user
     * to make sure this list gets updated whenever the click types change
     */
    int[] clickTypeList = null;
    
    /**
     * text field containing the Encounter ID
     */
    String notesEncounterID;
    
    /**
     * text field containing the Cruise ID
     */
    String notesCruiseID;
    
    /**
     * text field containing the Data Provider
     */
    String notesDataProvider;
    
    /**
     * text field containing known species
     */
    String notesKnownSpecies;
    
    /**
     * text field containing geographic location information
     */
    String notesGeoLoc;
    
    /**
     * text field containing number of channels
     */
    String notesChan;
    
    /**
     * whether or not to run the ancillary variables calculations on Clicks
     */
    boolean runAncCalcs4Clicks = false;
        
    /**
     * whether or not to run the ancillary variables calculations on Whistles
     */
    boolean runAncCalcs4Whistles = false;
    
    /**
     * whether or not to use a whistle classifier
     * serialVersionUID = 24 2016/08/10 added
     */
    private boolean classifyWhistles = false;
        
    /**
     * whether or not to use a clicks classifier
     * serialVersionUID = 24 2016/08/10 added
     */
    private boolean classifyClicks = false;
        
    /**
     * whether or not to use an event classifier
     * serialVersionUID = 24 2016/08/10 added
     */
    private boolean classifyEvents = false;
    
    /**
     * The GPS source, to be used with the event classifier
     */
    private String gpsSource;
    
    /**
     * whether or not to use GPS data with the event classifier
     */
    private boolean useGPS = false;
    
    /**
     * boolean indicating whether to trim the wav clip to only the contour (true),
     * or whether to save the entire amount that the user has boxed (false)
     */
    private boolean trimWav = false;
    
    
	public RoccaParameters() {
    }


	@Override
    public RoccaParameters clone() {
		try {
			RoccaParameters n = (RoccaParameters) super.clone();
			return n;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(int channelMap) {
        this.channelMap = channelMap;
    }

    public int getEnergyBinSize() {
        return energyBinSize;
    }

    public int getFftDataBlock() {
        return fftDataBlock;
    }

    public double getNoiseSensitivity() {
        return noiseSensitivity;
    }

    public void setNoiseSensitivity(double noiseSensitivity) {
        this.noiseSensitivity = noiseSensitivity;
    }

    /**
     * return the name of the whistle classifier
     * 
     * @return
     */
    public File getRoccaClassifierModelFilename() {
        return roccaClassifierModelFilename;
    }

    /**
     * return the name of the click classifier
 	 * serialVersionUID=24
 	 * 2016/08/10
     * 
     * @return
     */
    public File getRoccaClickClassifierModelFilename() {
        return roccaClickClassifierModelFilename;
    }

    /**
     * return the name of the event classifier
 	 * serialVersionUID=24
 	 * 2016/08/10
     * 
     * @return
     */
    public File getRoccaEventClassifierModelFilename() {
        return roccaEventClassifierModelFilename;
    }

//  /**
//	 * return the 2nd stage classifier filename
//	 * NOT CURRENTLY USED
//	 */
//    public File getRoccaClassifier2ModelFilename() {
//        return roccaClassifier2ModelFilename;
//    }

    /**
     * Set the whistle classifier file
     * 
	 * @param roccaClassifierModelFilename the roccaClassifierModelFilename to set
	 */
	public void setRoccaClassifierModelFilename(File roccaClassifierModelFilename) {
		this.roccaClassifierModelFilename = roccaClassifierModelFilename;
	}

    /**
     * Set the click classifier file
 	 * serialVersionUID=24
 	 * 2016/08/10
     * 
	 * @param roccaClickClassifierModelFilename the roccaClassifierModelFilename to set
	 */
	public void setRoccaClickClassifierModelFilename(File roccaClickClassifierModelFilename) {
		this.roccaClickClassifierModelFilename = roccaClickClassifierModelFilename;
	}

    /**
     * Set the event classifier file
 	 * serialVersionUID=24
 	 * 2016/08/10
     * 
	 * @param roccaEventClassifierModelFilename the roccaEventClassifierModelFilename to set
	 */
	public void setRoccaEventClassifierModelFilename(File roccaEventClassifierModelFilename) {
		this.roccaEventClassifierModelFilename = roccaEventClassifierModelFilename;
	}


//	/**
//	 * @param roccaClassifier2ModelFilename the roccaClassifier2ModelFilename to set
//	 * NOT CURRENTLY USED
//	 */
//	public void setRoccaClassifier2ModelFilename(File roccaClassifier2ModelFilename) {
//		this.roccaClassifier2ModelFilename = roccaClassifier2ModelFilename;
//	}


	public File getRoccaContourStatsOutputFilename() {
        return roccaContourStatsOutputFilename;
    }

    /**
	 * @return the stage1Link
	 */
	public int getStage1Link() {
		return stage1Link;
	}


	/**
	 * @param stage1Link the stage1Link to set
	 */
	public void setStage1Link(int stage1Link) {
		this.stage1Link = stage1Link;
	}


	public File getRoccaOutputDirectory() {
        return roccaOutputDirectory;
    }

    public int getClassificationThreshold() {
        return classificationThreshold;
    }

    public void setClassificationThreshold(int classificationThreshold) {
        this.classificationThreshold = classificationThreshold;
    }

    public int getSightingThreshold() {
        return sightingThreshold;
    }

    public void setSightingThreshold(int sightingThreshold) {
        this.sightingThreshold = sightingThreshold;
    }

    public File getRoccaSightingStatsOutputFilename() {
        return roccaSightingStatsOutputFilename;
    }

    public String getFilenameTemplate() {
        return filenameTemplate;
    }

    public void setFilenameTemplate(String filenameTemplate) {
        this.filenameTemplate = filenameTemplate;
    }

    public String getWmDataSource() {
		return wmDataSource;
	}

	public void setWmDataSource(String wmDataSource) {
		this.wmDataSource = wmDataSource;
	}

	/**
	 * @return the clickDataSource
	 */
	public String getClickDataSource() {
		return clickDataSource;
	}


	/**
	 * @param clickDataSource the clickDataSource to set
	 */
	public void setClickDataSource(String clickDataSource) {
		this.clickDataSource = clickDataSource;
	}


	/**
	 * @return the clickNoiseDataSource
	 */
	public String getClickNoiseDataSource() {
		return clickNoiseDataSource;
	}


	/**
	 * @param clickNoiseDataSource the clickNoiseDataSource to set
	 */
	public void setClickNoiseDataSource(String clickNoiseDataSource) {
		this.clickNoiseDataSource = clickNoiseDataSource;
	}


	public boolean weAreUsingFFT() {
		return useFFT;
	}

	public void setUseFFT(boolean useFFT) {
		this.useFFT = useFFT;
	}
	
	public boolean weAreUsingClick() {
		return useClick;
	}

	public void setUseClick(boolean useClick) {
		this.useClick = useClick;
	}
	
	public boolean weAreUsingWMD() {
		return useWMD;
	}

	public void setUseWMD(boolean useWMD) {
		this.useWMD = useWMD;
	}
	
	/**
	 * @return the clickTypeList
	 */
	public int[] getClickTypeList() {
		return clickTypeList;
	}


	/**
	 * @param clickTypeList the clickTypeList to set
	 */
	public void setClickTypeList(int[] clickTypeList) {
		this.clickTypeList = clickTypeList;
	}


	/**
	 * @return the notesEncounterID
	 */
	public String getNotesEncounterID() {
		return notesEncounterID;
	}


	/**
	 * @param notesEncounterID the notesEncounterID to set
	 */
	public void setNotesEncounterID(String notesEncounterID) {
		this.notesEncounterID = notesEncounterID;
	}


	/**
	 * @return the notesCruiseID
	 */
	public String getNotesCruiseID() {
		return notesCruiseID;
	}


	/**
	 * @param notesCruiseID the notesCruiseID to set
	 */
	public void setNotesCruiseID(String notesCruiseID) {
		this.notesCruiseID = notesCruiseID;
	}


	/**
	 * @return the notesDataProvider
	 */
	public String getNotesDataProvider() {
		return notesDataProvider;
	}


	/**
	 * @param notesDataProvider the notesDataProvider to set
	 */
	public void setNotesDataProvider(String notesDataProvider) {
		this.notesDataProvider = notesDataProvider;
	}


	/**
	 * @return the notesKnownSpecies
	 */
	public String getNotesKnownSpecies() {
		return notesKnownSpecies;
	}


	/**
	 * @param notesKnownSpecies the notesKnownSpecies to set
	 */
	public void setNotesKnownSpecies(String notesKnownSpecies) {
		this.notesKnownSpecies = notesKnownSpecies;
	}


	/**
	 * @return the notesGeoLoc
	 */
	public String getNotesGeoLoc() {
		return notesGeoLoc;
	}


	/**
	 * @param notesGeoLoc the notesGeoLoc to set
	 */
	public void setNotesGeoLoc(String notesGeoLoc) {
		this.notesGeoLoc = notesGeoLoc;
	}


	/**
	 * @return the notesChan
	 */
	public String getNotesChan() {
		return notesChan;
	}


	/**
	 * @param notesChan the notesChan to set
	 */
	public void setNotesChan(String notesChan) {
		this.notesChan = notesChan;
	}


	/**
	 * @return whether or not we're running Ancillary Calcs
	 */
	public boolean areWeRunningAncCalcsOnClicks() {
		return runAncCalcs4Clicks;
	}


	/**
	 * @param runAncillaryCalcs set whether or not to  run Ancillary Calcs
	 */
	public void setClickAncCalcs(boolean runAncillaryCalcs) {
		this.runAncCalcs4Clicks = runAncillaryCalcs;
	}

	/**
	 * @return whether or not we're running Ancillary Calcs
	 */
	public boolean areWeRunningAncCalcsOnWhistles() {
		return runAncCalcs4Whistles;
	}


	/**
	 * @param runAncillaryCalcs set whether or not to  run Ancillary Calcs
	 */
	public void setWhistleAncCalcs(boolean runAncillaryCalcs) {
		this.runAncCalcs4Whistles = runAncillaryCalcs;
	}

	public boolean isClassifyWhistles() {
		return classifyWhistles;
	}

	public void setClassifyWhistles(boolean classifyWhistles) {
		this.classifyWhistles = classifyWhistles;
	}

	public boolean isClassifyClicks() {
		return classifyClicks;
	}

	public void setClassifyClicks(boolean classifyClicks) {
		this.classifyClicks = classifyClicks;
	}

	public boolean isClassifyEvents() {
		return classifyEvents;
	}

	public void setClassifyEvents(boolean classifyEvents) {
		this.classifyEvents = classifyEvents;
	}

	public boolean weAreUsingGPS() {
		return useGPS;
	}

	public void setUseGPS(boolean useGPS) {
		this.useGPS = useGPS;
	}


	public String getGpsSource() {
		return gpsSource;
	}


	public void setGpsSource(String gpsSource) {
		this.gpsSource = gpsSource;
	}


	public boolean isTrimWav() {
		return trimWav;
	}


	public void setTrimWav(boolean trimWav) {
		this.trimWav = trimWav;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("runAncCalcs4Clicks");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return runAncCalcs4Clicks;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("runAncCalcs4Whistles");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return runAncCalcs4Whistles;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useFFT");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useFFT;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useClick");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useClick;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useWMD");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useWMD;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useGPS");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useGPS;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
