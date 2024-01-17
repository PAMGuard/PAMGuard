package difar;

import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.KeyStroke;

import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.PamCalendar;
import PamView.PamGui;
import PamView.PamView;
import PamguardMVC.PamDataBlock;
import difar.demux.GreenridgeParams;

public class DifarParameters implements Serializable, Cloneable, ManagedParameters {
	
	
	public static final long serialVersionUID = 10L;
	
	
	/**
	 * 
	 */
	public DifarParameters() {
		speciesParams=getSpeciesDefaults();
	}
	
	/**
	 * keep rawdata in rawdatablock for this amount of time may make this display dependant
	 */
	public int keepRawDataTime=120;//seconds
	
	/**
	 * name of unit to get raw data from including the frequency bands in which the multiplexed signals are within
	 */
	public String rawDataName;
	
	/**
	 * natural lifetime for the queued data datablock-note there is also 
	 * some control over this withing the clip generator history section of the hiding panel
	 */
	public int queuedDataKeepTime = 60; // minutes
	
	/**
	 * natural lifetime for the processed data datablock
	 */
	public int processedDataKeepTime = 10; // minutes
	
	public boolean clearQueueAtStart = true;
	
	public boolean clearProcessedDataAtStart = true;
	
	/**
	 * seconds to prepend to each clip to allow for signal locking of the demux algorithm
	 */
	public double secondsToPreceed = 0;
	
	/**
	 * name of the detector module which can trigger difar clips to be made - eg whistle and moan detector
	 * currently limited to one detector
	 */

	public ArrayList<DifarTriggerParams> triggerParams = null;

	/**
	 * Choices for calibration dialog when user right clicks 
	 * and / or new data arrive. 
	 */
	public static final int CALIBRATION_USE_MODE = 1;
	public static final int CALIBRATION_USE_MEAN = 2;
	public int calibrationChoice = CALIBRATION_USE_MODE;
	
	/**
	 * Stores paramaters to correct the frequency response for DIFAR buoys
	 */
	public FilterParams difarFreqResponseFilterParams = getDefaultFreqResponseFilter();
	
	/**
	 * Choices for propagation loss; 
	 */
	/**
	 * Propagation loss is not estimated and the bearing lines 
	 * for detections are drawn with a fixed, user-specified length
	 */
	public static final int PROPLOSS_NONE = 0; 
	/**
	 * The length of bearing lines is estimated using measured
	 * Received levels, RL, and user specified Source Levels (SL) 
	 * and attentuation coefficient, alpha such that RL = SL-PL
	 * Propagation loss, PL, is estimated as PL = alpha*log(range)  
	 */
	public static final int PROPLOSS_GEOMETRIC = 1; // Range is based on propagation loss a*log(range)

	/**
	 * The length of bearing lines is estimated using measured
	 * Received levels, RL, and user specified Source Levels (SL) 
	 * and surface duct distance, h0 such that RL = SL-PL
	 * Propagation loss, PL, is estimated as:
	 *  PL = 10*log(range) - 10*log(h0);  
	 */
	public static final int PROPLOSS_CYLINDRICAL = 2;
	
	//display

	/**
	 * vertical divider - possible between difargram and spectrogram 
	 */
	public Integer difarGramDividerPos;
	
	/**
	 * used when the DIFARcontainers were joined - no longer
	 */
	public Integer horizontalDividerPos;
	
	/**
	 * list of audio paramerters for processing difar clips - currently just vessel/whale but could be expanded for set for each species of whale 
	 */
	private ArrayList<SpeciesParams> speciesParams = getSpeciesDefaults();
	
	public  ArrayList<SpeciesParams> getSpeciesDefaults(){
		ArrayList<SpeciesParams> defaults = new ArrayList<SpeciesParams>();
		defaults.add(new SpeciesParams(CalibrationClip, true));
		defaults.add(new SpeciesParams(Default, true));
		return defaults;
	}
	
	//********* Side Panel and spectrogram marking parameters
	/**
	 * Automatically assign user-defined DIFAR classification when clicking on the Spectrogram 
	 */
	public LookupItem selectedClassification = null;
	
	/**
	 * Spectrogram marks will generate clips from all channels instead of the one clicked on
	 */
	public boolean multiChannelClips = false;
	
	/**
	 * Spectrogram marks will be assigned the selected classification in sidebar
	 */
	public boolean assignClassification = false;
	//********* 
	
	/**
	 * The default Frequency Response Filter used for correction/calibration
	 * of the frequency response of military sonobuoys.
	 * This default frequency response is flat from 5 Hz to 48 kHz, but 
	 * most sonobuoys have a non-flat frequency response, so this filter 
	 * can be adjusted (via the DIFAR parameters Dialog) measurement of 
	 * absolute sound pressure levels is required.
	 * @return
	 */
	public FilterParams getDefaultFreqResponseFilter() {
		difarFreqResponseFilterParams = new FilterParams();
		difarFreqResponseFilterParams.filterType = FilterType.NONE;
		difarFreqResponseFilterParams.filterOrder = 12;
		double[] freq = null;
		double[] gain = null;
		difarFreqResponseFilterParams.setArbFilterShape(freq, gain);
		return difarFreqResponseFilterParams;
	}
	
	
	private ArrayList<String> groupList = new ArrayList<String>();
	public  ArrayList<String> getGroupDefaults(){
		ArrayList<String> defaults = new ArrayList<String>();
		defaults.add(DefaultGroup);
		return defaults;
	}
	
	//used for buoy calibration
	public int vesselClipLength=10;
	public int vesselClipSeparation=10;
	public int vesselClipNumber=20;
	public boolean hideVesselClips=false;
	
	/**
	 * List of species that can be selected for each DIFAR clip. 
	 */
	private LookupList speciesList;
	
	private LookupItem[] favSpecies;
	
	/**
	 * @return the favSpecies which will be shown next to each clip
	 */
	public LookupItem[] getFavSpecies() {
		if (favSpecies==null) favSpecies = new LookupItem[numberOfFavouriteWhales];
		return favSpecies;
	}
	
	/**
	 * @param favSpecies the favSpecies to set
	 */
	public void setFavSpecies(LookupItem[] favSpecies) {
		this.favSpecies = favSpecies;
	}
	
	
	//Some options for the DIFARGram display
	/**
	 * save difargram when frequency/bearing selection is made
	 */
	private boolean singleClickSave = false;
	/**
	 * key on difargram - edited on right click
	 */
	public boolean showDifarGramKey = true;
	/**
	 * summary line - addition of amplitude/angle withing frequency bands on difargram - edited on right click
	 */
	public boolean showDifarGramSummary = true;
	
	/**
	 * show detection limits on difargram - edited on right click
	 */
	public boolean showDifarGramFreqLimits = true;

	public static enum DifarDemuxTypes{GREENERIDGE, AMMC_EXPERIMENTAL};

	public DifarDemuxTypes demuxType=DifarDemuxTypes.AMMC_EXPERIMENTAL;
	
	public static final String Default = "Unclassified";

	public static final String CalibrationClip = "Vessel";
	
	public static final String DefaultGroup = "No group";
	
	public static enum DifarOutputTypes{BARTLETT, MVDR};
	
	/**
	 * time afterwhich item has been sitting in difargram to autosave if no user interaction
	 */
	public float autoSaveTime =  3.0f; //seconds
	
	public boolean autoSaveDResult=false;
	
	/**
	 * when auto saving the difar result either the angle can be saved or the angle and range can be saved if it has range
	 */
	public boolean autoSaveAngleOnly=false;
	
	/**
	 * species to process a difarclip with. if null autoprocess dieabled
	 */
	public LookupItem Species = null;

	public static enum FirstOrders{OldestFirst,NewestFirst}
	public static enum SecondOrders{None,Whales,Vessels,VesselsOnly}
	
	public FirstOrders firstOrder=FirstOrders.OldestFirst;
	public SecondOrders secondOrder=SecondOrders.None;
	
	
	//////////////////////////////////////////////////////
//	DISPLAY

	/**
	 * Auto scale line length depending on range
	 */
	public boolean amplitudeScaledLineLength;
	
	/**
	 * Model to use for determining the length of a bearing line
	 */
	public int propLossModel;
	
	/**
	 * Nominal source level used to determine line lengths. 
	 */
	public double nominalSourceLevel = 180;
	/**
	 * Nominal spreading, alpha:
	 * PL = alpha*Log(range);
	 */
	public double nominalSpreading = 20;
	
	/**
	 * Distance travelled in surface duct, h0:
	 * 10*log(range) - 10*log(h0);
	 */
	public double cylindricalStartDistance = 2000;
	
	/**
	 * range in meters to dislpay on map
	 */
	public double defaultLength = 10000;

	
	public boolean timeScaledOpacity = true;
	
	/**
	 * check this compared with map show data time 30 min?
	 */
	public long timeToFade = 60;
	
	public int minimumOpacity = 5; // 0 is fully transparent; 255 is fully opaque.
		
	public boolean showVesselBearings = true;
	
	
	/**not user configurable for now
	 */
	public int numberOfFavouriteWhales=6;

	/**
	 * whether to autoprocess vessel clips(now the only ones that can be)
	 */
	public boolean autoProcess=true;

	public boolean zoomDifarFrequency;
	
	/**
	 * If true use the average the energy across the classification frequency band 
	 * in the difarGram, before picking the peak. Otherwise use the absolute maximum
	 */
	public boolean useSummaryLine = true; 
	
	//////////////////////////////////////////////////////
// 	Keyboard shortcuts
	public String saveKey = "F8";
	public String deleteKey = "F7";
	public String saveWithoutCrossKey = "shift F7";
	public String nextClassKey = "F6";
	public String prevClassKey = "F5";

	public String calibrationGpsSource;

	public boolean loadViewerClips = false;

	public boolean useMaxSourceLevel = false;

	public double maxSourceLevel = 180.;

	public float bearingLineWidth = 0.5f;

	public boolean amplitudeScaledOpacity = false;

	/**
	 * Use the function below to assign default values to newly added parameters, since they will be 
	 * null when loading older versions of the settings file.
	 */
	@Override
	public DifarParameters clone() {
		try {
			DifarParameters ndp = (DifarParameters) super.clone();
			if (ndp.nominalSpreading <= 0) {
				ndp.nominalSourceLevel = 190;
				ndp.nominalSpreading = 20;
				ndp.bearingLineWidth = 0.5f;
				ndp.maxSourceLevel = 180.;
			}
			return ndp;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public LookupList getSpeciesList(DifarControl difarControl) {
		if (speciesList == null) {
			speciesList = new LookupList(difarControl.getUnitName() + " classification list");
		}
		return speciesList;
	}

	public void setSpeciesList(LookupList speciesList) {
		this.speciesList = speciesList;
	}
	
	public void restoreDefaultSpeciesParams(){
		speciesParams = getSpeciesDefaults();
	}

	public double getDefaultRange() {
		return defaultLength ;
	}

	/**
	 * @return the singleClickSave
	 */
	public boolean isSingleClickSave() {
		return singleClickSave;
	}

	/**
	 * @param singleClickSave the singleClickSave to set
	 */
	public void setSingleClickSave(boolean singleClickSave) {
		this.singleClickSave = singleClickSave;
	}
	
	/**
	 * @return the speciesParams
	 */
	public ArrayList<SpeciesParams> getSpeciesParams() {
		if (speciesParams==null){
			speciesParams = new ArrayList<SpeciesParams>(){
				/* (non-Javadoc)
				 * @see java.util.ArrayList#add(java.lang.Object)
				 */
				@Override
				public boolean add(SpeciesParams e) {
					if (findSpeciesParams(e.lookupItemName)!=null)return false;
					return super.add(e);
				}
			};

		}
		
		return speciesParams;
	}

	public SpeciesParams findSpeciesParams(DifarDataUnit difarDataUnit) {
		if (difarDataUnit.isVessel()) {
			return findSpeciesParams(DifarParameters.CalibrationClip);
		}
		else {
			return findSpeciesParams(difarDataUnit.getLutSpeciesItem());
		}
	}
	
	public SpeciesParams findSpeciesParams(LookupItem lutItem) {
		if (lutItem == null) {
			return null;
		}
		return findSpeciesParams(lutItem.toString());
	}
	
	public SpeciesParams findSpeciesParams(String lookupItemName){
		for (SpeciesParams sp:getSpeciesParams()){
			if (sp.lookupItemName.equals(lookupItemName)){
				return sp;
			}
		}
		return new SpeciesParams(lookupItemName, true);
	}
	
	/**
	 * Create a new set of audio clip parameters for a given species
	 * @param sp - Audio parameters (e.g. Sample rate, FFT Lenght, Frequency Limits etc.)
	 * @param override - Set to true to overwrite existing parameters
	 * @return
	 */
	public boolean addSpeciesParams(SpeciesParams sp,boolean override){
		SpeciesParams spT = findSpeciesParams(sp.lookupItemName);
		if ( ! override && spT != null ) return false;
		getSpeciesParams().remove(spT);
		return getSpeciesParams().add(sp);
		
	}

	/**
	 * @param speciesParams the speciesParams to set
	 */
	public void setSpeciesParams(ArrayList<SpeciesParams> speciesParams) {
		this.speciesParams = speciesParams;
	}

	public class SpeciesParams implements Serializable, Cloneable, ManagedParameters {
		

		// serialVersionUID -  
		private static final long serialVersionUID = 1L;
		
		public SpeciesParams(String lookupItemName,boolean useDefaults) {
			this.lookupItemName=lookupItemName;
			if (useDefaults){
				if (lookupItemName==CalibrationClip){
					this.processFreqMin=700-100;
					this.processFreqMax=700+100;
					this.sampleRate=2000;
					this.FFTLength=512;
					this.FFTHop=256;
					this.setnAngleSections(360);
					this.setDifarGramIntensityScaleFactor(100);
					this.difarOutputType = DifarOutputTypes.BARTLETT;
				}else{
					this.processFreqMin=58-42;
					this.processFreqMax=58+42;
					this.sampleRate=8000;
					this.FFTLength=1024;
					this.FFTHop=256;
					this.setnAngleSections(360);
					this.setDifarGramIntensityScaleFactor(100);
					this.difarOutputType = DifarOutputTypes.BARTLETT;
				}
			}
		}
		
		public String lookupItemName;
		public float processFreqMin;
		public float processFreqMax;
		public float sampleRate;
		public int FFTLength;
		public int FFTHop;
		
		/**
		 * 	DifarGram Grid spacing
		 */
		private int nAngleSections = 360;
		
		/**
		 * This controls the contrast/intensity scale in the difarGram by setting
		 * a the floor of the difarGram (lowest value) to be equal to 
		 * max(difarGram[][])/intensityScaleFactor.
		 * Lower values (ie. 2 to 10) will yield better contrast near the maximum of
		 * the difarSurface. 10<inensityScaleFactor<10e5) will lower the floor and
		 * reduce the contrast near the peaks. A low value is recommended since the
		 * peaks are usually the features of interest in the difarGram. 
		 * 
		 */
		private double difarGramIntensityScaleFactor = 1000;
		
		public DifarOutputTypes difarOutputType = DifarOutputTypes.BARTLETT;

		/**
		 * 
		 */
		public boolean useMarkedBandsForSpectrogramClips=true;
		/**
		 * for maons, use the max/min of the detection rather than the whale defaults
		 */
		public boolean useDetectionLimitsForTriggeredDetections=true;


		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		
		@Override
		public SpeciesParams clone(){
			try{
				return (SpeciesParams)super.clone();
			}catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
		}


		/**
		 * @return the nAngleSections
		 */
		public int getnAngleSections() {
			if (nAngleSections <= 0){
				nAngleSections = 360;
			}
			return nAngleSections;
		}


		/**
		 * @param nAngleSections the nAngleSections to set
		 */
		public void setnAngleSections(int nAngleSections) {
			this.nAngleSections = nAngleSections;
		}


		/**
		 * @return the difarGramIntensityScaleFactor
		 */
		public double getDifarGramIntensityScaleFactor() {
			if (difarGramIntensityScaleFactor == 0) {
				difarGramIntensityScaleFactor = 1000;
			}
			return difarGramIntensityScaleFactor;
		}


		/**
		 * @param difarGramIntensityScaleFactor the difarGramIntensityScaleFactor to set
		 */
		public void setDifarGramIntensityScaleFactor(double difarGramIntensityScaleFactor) {
			this.difarGramIntensityScaleFactor = difarGramIntensityScaleFactor;
		}
		
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
			return ps;
		}

	}
	
	
	public class DifarTriggerParams implements Serializable, Cloneable, ManagedParameters {

		public static final long serialVersionUID = 1L;
		
		/**
		 * Include only the detected channels in the clip
		 */
		public static final int DETECTION_CHANNELS_ONLY = 0;
		
		/**
		 * Include only the first of the detected channels in the clip
		 */
		public static final int FIRST_DETECTION_CHANNEL_ONLY = 1;
		
		/**
		 * include all channels in the clip
		 */
		public static final int ALL_CHANNELS = 2;

		/**
		 * Data name of the trigger data block. 
		 */
		public String dataName;
		
		/**
		 * Enabled
		 */
		public boolean enable = true;
		
		public LookupItem speciesLookupItem = null;

		/**
		 * Name of species for auto-processing, or Default to send to DIFARQueuePanel
		 */
		public String speciesName = DifarParameters.Default; 
		
		/**
		 * Channel selection, all, first, one, etc. 
		 */
		public int channelSelection = DETECTION_CHANNELS_ONLY;
		
		
		/**
		 * @param dataName
		 */
		public DifarTriggerParams(String dataName) {
			super();
			this.dataName = dataName;
		}


		@Override
		protected DifarTriggerParams clone()  {
			try {
				return (DifarTriggerParams) super.clone();
			}
			catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
			return ps;
		}

	}


	/**
	 * 
	 * @return the number of triggers that are enabled by the DIFAR module. 
	 */
	public int getNumTriggersEnabled() {
		if (triggerParams == null) {
			return 0;
		}
		return triggerParams.size();
	}

	/**
	 * Get a trigger setting object
	 * @param i number of the trigger
	 * @return the trigger parameters
	 */
	public DifarTriggerParams getTriggerParams(int i) {
		if (triggerParams == null || i >= triggerParams.size()) {
			return null;
		}
		return triggerParams.get(i);
	}

	/**
	 * Find the trigger parameters for a specific data stream. 
	 * 
	 * @param dataName data name for the data block. 
	 * @return Trigger parameters, or null if none active. 
	 */
	public DifarTriggerParams findTriggerParams(String dataName) {
		int n = getNumTriggersEnabled();
		for (int i = 0; i < n; i++) {
			if (triggerParams.get(i).dataName.equals(dataName)) {
				return triggerParams.get(i);
			}
		}
		return null;
	}


	/**
	 * Clear all trigger parameters. 
	 */
	public void clearAllTriggerParams() {
		if (triggerParams!=null)
		triggerParams.clear();
	}

	/**
	 * Add a new trigger Parameters to the list. 
	 * @param tP
	 */
	public boolean addTriggerParams(DifarTriggerParams tP) {
		if (triggerParams == null) {
			triggerParams = new ArrayList<DifarTriggerParams>();
		}
		return triggerParams.add(tP);
	}
	
	public boolean addTrackedGroup(String listItem){
		if (groupList == null) {
			groupList = getGroupDefaults();
		}
		if (groupList.contains(listItem)){
			return false;
		}
		return groupList.add(listItem);
	}
	
	public void removeTrackedGroup(String listItem){
		// Make it impossible to remove the DefaultGroup
		if (listItem.equals(DefaultGroup)) {
			return;
		}
		// groupList should never be null, but just in case
		if (groupList == null){
			groupList = getGroupDefaults();
			return;
		}
		
		// Loop over the list and remove the item if found
		for (int i = 0; i < groupList.size(); i++){
			if (groupList.get(i).equals(listItem)){
				groupList.remove(i);
			}
		}
	}
	
	public ArrayList<String> getTrackedGroupList() {
		if (groupList == null){
			groupList = getGroupDefaults();
		} else if (groupList.size()==0){
			groupList = getGroupDefaults();
		}
		return groupList;
	}
	
	public FilterParams getDifarFreqResponseFilterParams(){
		if (difarFreqResponseFilterParams == null ||
				 !difarFreqResponseFilterParams.filterType.equals(FilterType.FIRARBITRARY)){
			difarFreqResponseFilterParams = getDefaultFreqResponseFilter();
		}
		return difarFreqResponseFilterParams;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("groupList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return groupList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("speciesList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return speciesList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
