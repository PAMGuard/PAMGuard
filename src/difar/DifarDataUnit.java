package difar;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import GPS.GpsData;
import PamUtils.FrequencyFormat;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clipgenerator.ClipDataUnit;
import fftManager.Complex;
import fftManager.FastFFT;
import generalDatabase.DBControlUnit;
import generalDatabase.SQLLogging;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

public class DifarDataUnit extends ClipDataUnit {

	private PamDataUnit parentDetection;

//	private double[] multiplexedData;

//	private double[][] demuxedData;
	
	/** false if the unit has had an interaction to change something 
	 * //this is a bit pointless now as action performed doesn't attempt save if timer is stopped
	 */
	private boolean canAutoSave=true;

	/**
	 * holds decimated data and the last requested non original sample rate
	 * This is a decimation of the original input data which is used 
	 * in the clip display prior to the demux taking place.  
	 */
	private double[] decimatedData;

	/**Holds the difargram surface data for the clip
	 */
	private double[][] surfaceData;
	
	/**
	 * Summary of the difargram within getFrequency() bounds vs. angle in same 
	 * angle bins as surfaceData.
	 */
	private double[] surfaceSummary;

	/**
	 * Angles of maximum power of the difargram within display frequency bounds.
	 */
	private double[] maximumAngleSummary;
	
	/**
	 * Start time in millis for start of the trigger/section (some seconds after the start of the clip which are only used for PLL locking)
	 */
	private long signalStart;

	private boolean isVessel = false;
	
	private Double difarGain = null;


	/**
	 * Choice of data for spectrogram displays. 
	 * <br> 0 = decimated original data.
	 * <br> 1 = Omni demuxed
	 * <br> 2 = EW demuxed
	 * <br> 3 = NS demuxed
	 */
	private int spectrogramSensor = 1;
	
	/**
	 * Why not just hold a reference to the entire LUT item for now - contains short
	 * and long text as well as the symbol. 
	 */
	private LookupItem lutSpeciesItem = null;

	private float displaySampleRate;
	
	/**
	 * Number of seconds of sound data added prior to the 
	 * spectrogram mark or detection. 
	 */
	private double preMarkSeconds;

	private float lastDecmiatedSampleRate;

	private Double selectedAngle = null;

	private Double selectedFrequency = null;
	
	private Double maximumAngle = null;
	
	private Double maximumFrequency = null;
	
	double[][] demuxedDecimatedData;

	private boolean[][] lockArrays;

	/*
	 * the start of the actual clip = probably before the actual start of the sound.  
	 */
	private long clipStartMillis;

	private DIFARCrossingInfo difarCrossing;
	
	private DIFARCrossingInfo tempCrossing;
	
	private String trackedGroup;

	private String tempGroup;
	
	/**
	 * Store the frequency and gain of the filter for
	 * frequency-intensity calibration
	 */
	private double [] calFreq;
	private double [] calGain;

	/**
	 * Constructor to use if storing data into the binary system. 
	 * @param timeMilliseconds
	 * @param triggerMilliseconds
	 * @param startSample
	 * @param durationSamples
	 * @param channelMap
	 * @param fileName
	 * @param triggerName
	 * @param rawData
	 * @param upperFreq 
	 */
	public DifarDataUnit(long clipStartMillis, long triggerMilliseconds,
			long startSample, int durationSamples, int channelMap, String fileName,
			String triggerName,	double[][] rawData, long signalStartMillis, 
			PamDataUnit parentDetection, double[] frequencyRange, float sourceSampleRate, float displaySampleRate,
			double[] calFreqs, double[] calGains) {
		super(signalStartMillis, triggerMilliseconds,
				startSample, durationSamples, channelMap, fileName,
				triggerName,rawData, sourceSampleRate);
		this.clipStartMillis = clipStartMillis;
		this.preMarkSeconds = (signalStartMillis-clipStartMillis) / 1000.;
		this.triggerMilliseconds = triggerMilliseconds;
		this.fileName = fileName;
		this.triggerName = triggerName;
		this.setRawData(rawData);
		if (this.fileName == null) {
			this.fileName = "";
		}
		this.signalStart = signalStart;
		this.displaySampleRate = displaySampleRate;
		this.parentDetection = parentDetection;
		setFrequencyResponse(calFreqs, calGains);
		setFrequency(frequencyRange);

	}
	
	public static  double[] decimateData(double[] ds, float origSmp, float displaySampRate){

		if (ds == null) {
			return null;
		}
		double step = displaySampRate/origSmp;
		int outLen = (int) Math.floor(ds.length*displaySampRate/origSmp);

		double[] filteredData=new double[ds.length];

		//set up filter
		FilterParams fParams = new FilterParams();
		fParams.filterOrder = 6;
		fParams.filterType = FilterType.BUTTERWORTH;
		fParams.filterBand = FilterBand.LOWPASS;
		fParams.lowPassFreq = displaySampRate/2.f;
		FilterMethod fMethod = FilterMethod.createFilterMethod(origSmp, fParams);
		Filter filter = fMethod.createFilter(0);

		//run filter
		filter.runFilter(ds, filteredData);
		
		double [] decimatedData = new double[(int) Math.floor(filteredData.length*displaySampRate/origSmp)];
		int iPos;
		double decFac = origSmp/displaySampRate;
		for (int i = 0; i < decimatedData.length; i++) {
			iPos = (int) (i*decFac);
			decimatedData[i] = filteredData[iPos];
		}

		return decimatedData;
	}
	
	public void setFrequencyResponse(double[] f, double[] gain){
		calFreq = f;
		calGain = gain;
	}

	/**
	 * The amplitude filter provides an intensity correction
	 * for the non-flat frequency response of DIFAR sonobuoys.
	 * Presently it is hard-coded for AN/SSQ53D sonobuoys, but 
	 * eventually this should be user-adjustable.
	 * @param sampleRate
	 * @return
	 */
	private static Filter setupAmplitudeFilter(float sampleRate, double[] f, double[] gain){
		FilterParams afp = new FilterParams();
		
		/**
		 * Now use only a subset of the frequency response that is 
		 * appropriate for the desired sampleRate
		 */
		int index = f.length;
		for (int i = 1; i<f.length; i++){
			if (f[i] >= sampleRate/2){
				index = i;
				break;
			}
		}
		double[] freq = new double[index];
		double[] response = new double[index];
		System.arraycopy(f, 0, freq, 0, index);
		System.arraycopy(gain, 0, response, 0, index); 
		
		afp.filterType = FilterType.FIRARBITRARY;
		afp.filterOrder = 8;
		afp.setArbFilterShape(freq, response);
		
		FilterMethod arbMethod = FilterMethod.createFilterMethod(sampleRate, afp);
		Filter arbFilter = arbMethod.createFilter(0);
		return arbFilter;
	}
	

	/* (non-Javadoc)
	 * @see clipgenerator.ClipDataUnit#getSpectrogramWaveData(int)
	 */
	@Override
	protected double[] getSpectrogramWaveData(int channel, float displaySampRate) {
		/**
		 * In viewer mode the decimated wave data won't exist, so need
		 * to return whatever we can. 
		 */
		double[] foundData = null;
		
		/**
		 *  As a default, use the decimated data from the clip. This should always
		 *  exist in Normal and Mixed mode, even if the data unit has not yet
		 *  been demodulated
		 */
		foundData = getDecimatedWaveData(channel, displaySampRate);
		
		/**
		 * Now try to use the demodulated data.
		 */
		if (demuxedDecimatedData != null) {
			foundData = demuxedDecimatedData[spectrogramSensor-1];
		}

		if (foundData == null) {
			// Not sure why this is here... Perhaps demodulation failed? 
			if (demuxedDecimatedData != null && demuxedDecimatedData[0] != null) {
				spectrogramSensor = 1;
				return demuxedDecimatedData[0];
			}
		}
		if (calFreq != null && foundData !=null) {
			Filter calibrationFilter = setupAmplitudeFilter(displaySampleRate, calFreq, calGain);
			double[] filteredData = new double[foundData.length];
			calibrationFilter.runFilter(foundData, filteredData);
			if (filteredData !=null){
				return filteredData;
			}
		}
		return foundData;
	}
	
	private double[] getDecimatedWaveData(int channel, float displaySampRate) {
		float origSmp = getSourceSampleRate(); 
		if (displaySampRate == origSmp){
			return getWaveData(channel);
		}
		if (decimatedData == null || displaySampRate != lastDecmiatedSampleRate) {
			decimatedData = null;
		}
		if (decimatedData != null) {
			return decimatedData;
		}
		else {
			decimatedData = decimateData(getWaveData(channel), origSmp, displaySampRate);
			lastDecmiatedSampleRate=displaySampRate;
			return decimatedData;
		}

	}

	public PamDataUnit getParentDetection() {
		return parentDetection;
	}

	public void setParentDetection(PamDataUnit parentDetection) {
		this.parentDetection = parentDetection;
	}

	public double[] getMultiplexedData() {
		double[][] rawData = getRawData();
		if (rawData == null || rawData.length < 1) {
			return null;
		}
		return rawData[0];
	}

//	public void setMultiplexedData(double[] multiplexedData) {
//		this.multiplexedData = multiplexedData;
//	}

	/**
	 * Signal start time in milliseconds relative to start of sound. 
	 * @return signal start time in milliseconds
	 */
	public  long getSignalStart() {
		return signalStart;
	}

	public double[][] getSurfaceData() {
		return surfaceData;
	}

	public void setSurfaceData(double[][] surfaceData) {
		this.surfaceData = surfaceData;
			difarGain = calculateDifarGain();
	}

	/**
	 * @return the isVessel
	 */
	public boolean isVessel() {
		return isVessel;
	}

	/**
	 * @param isVessel the isVessel to set
	 */
	public void setVessel(boolean isVessel) {
		this.isVessel = isVessel;
		
	}

	/**
	 * @return the lutSpeciesItem
	 */
	public LookupItem getLutSpeciesItem() {
		return lutSpeciesItem;
	}

	/**
	 * @param lutSpeciesItem the lutSpeciesItem to set
	 */
	public void setLutSpeciesItem(LookupItem lutSpeciesItem) {
		this.lutSpeciesItem = lutSpeciesItem;
	}

	/**
	 * @return the displaySampleRate
	 */
	@Override
	public float getDisplaySampleRate() {
		return displaySampleRate;
	}

	/**
	 * @param displaySampleRate the displaySampleRate to set
	 */
	public void setDisplaySampleRate(float displaySampleRate) {
		this.displaySampleRate = displaySampleRate;
	}

	/**
	 * Get the  angle selectedfrom the DIFARGram. Note
	 * that this is the angle relative to the DIFAR buoy
	 * and is therefore a MAGNETIC angle. If you want an 
	 * angle corrected for magnetic deviation, use
	 * getTrueAngle(); 
	 * @return the selectedAngle
	 */
	public Double getSelectedAngle() {
		return selectedAngle;
	}
	
	public Double getRange() {
		return null;
	}
	
	/**
	 * Get the true bearing relative to the DIFAR buoy. This is the selected angle
	 * (see getSelectedAngle() + the local magnetic deviation calulcated from the   
	 * World Magnetic Model. For map display, this bearing may be further
	 * corrected for any offset in the buoy heading in the array manager. 
	 * If there is no offset in the arraymanager, then return the magnetic bearing 
	 * since returning null for the trueBearing may cause problems down the line. 
	 * @return true angle (if the buoy is orientated right !)
	 */
	public Double getTrueAngle() {

		if (selectedAngle == null) {
			return null;
		}
		GpsData originPos = getOriginLatLong(false);
		if (originPos == null || originPos.getTrueHeading() == null) {
			return (selectedAngle % 360);
		}
		return ((selectedAngle + originPos.getTrueHeading()) % 360);
//		double dev = MagneticVariation.getInstance().getVariation(originPos);
//		return selectedAngle+dev;
	}

	public String getTrackedGroup() {
		if (trackedGroup == null){
			return DifarParameters.DefaultGroup;
		}
		return trackedGroup;
	}
	
	public void setTrackedGroup(String newGroup) {
		trackedGroup = newGroup;
	}
	
	/**
	 * Set the selected angle from the DIFARGram. Note
	 * that this is the angle relative to the DIFAR buoy
	 * and is therefore a MAGNETIC angle. 
	 * @param selectedAngle the selectedAngle to set
	 */
	public void setSelectedAngle(Double selectedAngle) {
		this.selectedAngle = selectedAngle;
		/**
		 * Now also work out the DIFAR gain for that angle. 
		 * This means taking the closest point in the summary line
		 * and seeing what it's value is for that angle 
		 */
		difarGain = calculateDifarGain();
	}

	/**
	 * 
	 * Now also work out the DIFAR gain for that angle. 
	 * This means taking the closest point in the summary line
	 * and seeing what it's value is for that angle 
	 * @return gain. 
	 */
	public Double calculateDifarGain() {
		return calculateDifarGain(selectedAngle, selectedFrequency);
	}
	
	/**
	 * Calculate teh DIFAR gain for a given angle and frequency
	 * @param selectedAngle angle 0 - 360
	 * @param selectedFrequency Frequency Hz
	 * @return gain
	 */
	public Double calculateDifarGain(Double selectedAngle, Double selectedFrequency) {
		if (selectedAngle == null || selectedFrequency == null || surfaceData == null) {
			return null;
		}
		int nAbins = surfaceData.length;
		int nFbins = surfaceData[0].length;
				
		int angBin = (int) Math.round(selectedAngle * nAbins / 360.);
		angBin = Math.min(nAbins-1, Math.max(0, angBin));
		
		int fBin = (int) Math.round(selectedFrequency * nFbins * 2 / displaySampleRate);
		fBin = Math.min(nFbins-1, Math.max(0, fBin));
		return surfaceData[angBin][fBin];
	}


	/**
	 * @return the selectedFrequency
	 */
	public Double getSelectedFrequency() {
		return selectedFrequency;
	}

	/**
	 * @param selectedFrequency the selectedFrequency to set
	 */
	public void setSelectedFrequency(Double selectedFrequency) {
		this.selectedFrequency = selectedFrequency;
		difarGain = calculateDifarGain();
	}

	/**
	 * @return the surfaceSummary
	 */
	public double[] getSurfaceSummary() {
		return surfaceSummary;
	}

	/**
	 * @param surfaceSummary the surfaceSummary to set
	 */
	public void setSurfaceSummary(double[] surfaceSummary) {
		this.surfaceSummary = surfaceSummary;
	}

	/**
	 * @return the maximumAngleSummary
	 */
	public double[] getMaximumAngleSummary() {
		return maximumAngleSummary;
	}

	/**
	 * @param surfaceSummary the surfaceSummary to set
	 */
	public void setMaximumAngleSummary(double[] surfaceSummary) {
		this.maximumAngleSummary = surfaceSummary;
	}
	
	/**
	 * @return the maximumAngle
	 */
	public Double getMaximumAngle() {
		return maximumAngle;
	}

	/**
	 * @param maximumAngle the maximumAngle to set
	 */
	public void setMaximumAngle(Double maximumAngle) {
		this.maximumAngle = maximumAngle;
	}

	/**
	 * @return the maximumFrequency
	 */
	public Double getMaximumFrequency() {
		return maximumFrequency;
	}

	/**
	 * @param maximumFrequency the maximumFrequency to set
	 */
	public void setMaximumFrequency(Double maximumFrequency) {
		this.maximumFrequency = maximumFrequency;
	}

	/**
	 * Choice of data for spectrogram displays. 
	 * <br> 0 = decimated original data.
	 * <br> 1 = Omni demuxed
	 * <br> 2 = EW demuxed
	 * <br> 3 = NS demuxed
	 * @param spectrogramSensor the spectrogramSensor to set
	 */
	public void setSpectrogramSensor(int spectrogramSensor) {
		if (this.spectrogramSensor != spectrogramSensor) {
			this.spectrogramSensor = spectrogramSensor;
			clearClipSpecData();
		}
	}

	/**
	 * @return the demuxedDecimatedData
	 */
	public double[][] getDemuxedDecimatedData() {
		return demuxedDecimatedData;
	}

	/**
	 * @param demuxedDecimatedData the demuxedDecimatedData to set
	 */
	public void setDemuxedDecimatedData(double[][] demuxedDecimatedData) {
		this.demuxedDecimatedData = demuxedDecimatedData;
	}

	/**
	 * Get a clip image for a particular wave type - decimated original, Om, EW, NS
	 * @param spectrogramImageChoice
	 * @param iChannel
	 * @param fftLen
	 * @param fftHop
	 * @param scaleMin
	 * @param scaleMax
	 * @param colours
	 * @return 
	 */
	public BufferedImage getClipImage(int spectrogramImageChoice, int iChannel,
			int fftLen, int fftHop, double scaleMin, double scaleMax, Color[] colours) {
		this.setSpectrogramSensor(spectrogramImageChoice);
		return getClipImage(iChannel, fftLen, fftHop, scaleMin, scaleMax, colours);
	}

	/**
	 * 
	 * @return null, Vessel or the long name from the lookup
	 */
	public String getSpeciesName() {
		if (isVessel){
			return "Vessel";
		}
		else if (lutSpeciesItem == null) {
			return "Not set";
		}
		else {
			return lutSpeciesItem.getText();
		}
	}
	/**
	 * Get a species code - mostly used by the database. 
	 * @return a species code (the shorter bit from the lookup table)
	 * TODO: Store species parameters with each data unit
	 */
	public String getSpeciesCode() {
		if (isVessel){
			return "Vessel";
		}
		else if (lutSpeciesItem == null) {
			return "";
		}
		else {
//			return lutSpeciesItem.getText(); //TODO check call H should probably just use text for database.
			return lutSpeciesItem.getCode();
		}
	}

	/**
	 * Called in viewer when data are read back from binary store to 
	 * sort out the species luukup item. 
	 * @param speciesList list of programmed species codes
	 * @param speciesCode data units code or "Vessel"
	 */
	public void setSpeciesCode(LookupList speciesList, String speciesCode) {
		if (speciesCode == null) {
			return;
		}
		else if (speciesCode.equals(DifarParameters.CalibrationClip)) {
			isVessel = true;
			lutSpeciesItem = null;
		}
		else {
			isVessel = false;
			lutSpeciesItem = speciesList.findSpeciesCode(speciesCode);
			if (lutSpeciesItem == null) {
				lutSpeciesItem = new LookupItem(0, 0, speciesList.getListTopic(), 0, speciesCode, speciesCode, 
						true, Color.BLACK, Color.BLACK, "Circle");
				speciesList.addItem(lutSpeciesItem);
			}
			
		}
	}

	/**
	 * @param lock_15
	 * @param sampleRate
	 * @param displaySampleRate2
	 */
	public static boolean[] decimateLockArray(boolean[] lockInput, float origSmp, float displaySampRate) {
		
		boolean[] decimatedLock = new boolean[(int) Math.floor(lockInput.length*displaySampRate/origSmp)];
		
		int iPos;
		double decFac = origSmp/displaySampRate;
		for (int i = 0; i < decimatedLock.length; i++) {
			iPos = (int) (i*decFac);
			decimatedLock[i] = lockInput[iPos];
		}
		
		return decimatedLock;
	}

	/**
	 * @return the difarGain
	 */
	public Double getDifarGain() {
		return difarGain;
	}

	/**
	 * @param difarGain the difarGain to set
	 */
	public void setDifarGain(double difarGain) {
		this.difarGain = difarGain;
	}

	/**
	 * Set the lock array iformation in the data unit.
	 * This is a 2xn boolean array of the 7.5 and 15kHz locks, 
	 * decimated to the same freqeuncy as the demuxed audio data.  
	 * @param lockArrays
	 * TODO: Move all lock related code into the Greeneridge demux
	 */
	public void setLockDecimatedData(boolean[][] lockArrays) {	
		this.lockArrays = lockArrays;
	}
	//TODO: Move all lock related code into the Greeneridge demux
	public boolean[][] getLockDecimatedData() {
		return this.lockArrays;
	}

	/**
	 * Gets called just before unit is added to processed data. 
	 * Some things are no longer needed at this point, so can clean them up. 
	 */
	public void cleanUpData() {
		setRawData(null);
		surfaceData = null;
		surfaceSummary = null;
//		lockArrays = null;
	}

	/**
	 * Used by some displays
	 * @return
	 */
	public double getDurationInSeconds() {
		return  (double) getSampleDuration() / (double) getParentDataBlock().getSampleRate();
	}

	/* (non-Javadoc)
	 * @see PamDetection.AcousticDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
//		String sumString = super.getSummaryString();
//		sumString += "<br>Call type: " + getSpeciesName();
//		sumString += String.format("<br>DIFAR gain: %3.1f", getDifarGain());
//		return sumString;
		GpsData origin = getOriginLatLong(false);
		if (origin==null) {
			return "No streamer/hydrophone associated with this data unit";
		}
		String str =  "<html>" + getParentDataBlock().getDataName();
		str += String.format("<br> Start: %s %s", 
				PamCalendar.formatDate(getTimeMilliseconds()),
				PamCalendar.formatTime(getTimeMilliseconds(), true));
		if (isVessel()) {
			str += "<br>Vessel calibration point";
		}
		else {
			LookupItem lsi = getLutSpeciesItem();
			if (lsi == null) {
				str += "<br>Unknown species information";
			}
			else {
				str += "<br>Species: " + lsi.getText();
			}
		}
		str += "<br>Group: ";
		if (trackedGroup == null) {
			str += "No group assigned";
		}else {
			str += trackedGroup;
		}
		str += String.format("<br> Channel: %s", PamUtils.getChannelList(getChannelBitmap()));
		str += "<br>"+FrequencyFormat.formatFrequencyRange(getFrequency(), true);
		str += String.format("<br>Amplitude: %3.1fdB", getAmplitudeDB());
		Double ang = getSelectedAngle();
		Double buoyHead = origin.getTrueHeading();
		if (ang != null) {
			str += "<br>" + String.format("DIFAR angle %4.1f%s", ang, LatLong.deg);
			if (buoyHead == null) {
				str += " (not calibrated)";
			}
			else {
				str += String.format(" rel. %4.1f%s true", ang+buoyHead, LatLong.deg);
				str += String.format("<br>calibrated at %s", PamCalendar.formatDateTime(origin.getTimeInMillis()));
			}
			if (difarGain == null) {
				str += "<br>DIFAR gain: Undefined";
			}
			else {
				str += String.format("<br>DIFAR gain: %3.1fdB", 20*Math.log10(difarGain));
			}
		}
		else {
			str += String.format("<br>Buoy head %3.1f%s, calibrated at %s", buoyHead, LatLong.deg, 
					PamCalendar.formatDateTime(origin.getTimeInMillis()));
		}
		DIFARCrossingInfo xInfo = difarCrossing;
		if (xInfo == null) xInfo = tempCrossing;
		if (xInfo != null) {
			int range = (int) origin.distanceToMetres(xInfo.getCrossLocation());
			str += "<br>" + String.format("Range %dm,  Location %s %s", range, 
					xInfo.getCrossLocation().formatLatitude(),xInfo.getCrossLocation().formatLongitude());
		}
		str += "</html>";
		return str;
	}

	/**
	 * @return the Number of seconds added to the sound prior to the specrogram mark or detection. 
	 */
	public double getPreMarkSeconds() {
		return preMarkSeconds;
	}

	/**
	 * @param the Number of seconds added to the sound prior to the specrogram mark or detection
	 */
	public void setPreMarkSeconds(double preMarkSeconds) {
		this.preMarkSeconds = preMarkSeconds;
	}

	/**
	 * @return the clipStartMillis. This is the start of the clip - which will 
	 * generally have been put  afew secs before the actual sound. 
	 */
	public long getClipStartMillis() {
		return clipStartMillis;
	}

	/**
	 * Autoprocess everything except "Default" clips
	 * @return
	 */
	public boolean canAutoProcess() {
		if (isVessel() || getLutSpeciesItem()!=null) return true;
		return false;
	}

	/**
	 * //this is a bit pointless now as action performed doesn't attempt save if timer is stopped
	 * @param sets canAutoSave false
	 */
	public void cancelAutoSave() {
		
		canAutoSave=false;
		
	}
	
	/**
	 * //this is a bit pointless now as action performed doesn't attempt save if timer is stopped
	 * whether the difar unit has been interacted with causing autosave to be inapropriate 
	 */
	public boolean canAutoSave() {
		return canAutoSave&&getSelectedAngle()!=null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		double[] frequency = getFrequency();
		return String.format("DifarUnit  time %s isVes %s LookUp %s freq %s %s angle %s", 
				PamCalendar.formatDateTime2(clipStartMillis),
				isVessel(),
				getLutSpeciesItem(),
				frequency[0],
				frequency[1],
				getSelectedAngle());
	}


//	/**
//	 * Set the crossing point when multiple matching DIFAR bearings are crossed. 
//	 * @param crossInfo result from DIFAR localisation. 
//	 */
//	public void setDifarCrossing(DIFARCrossingInfo crossInfo) {
//		this.difarCrossing = crossInfo;
//	}
	/**
	 * Move the crossing info from it's temp position to 
	 * a saved position. Called just at the point when the difar 
	 * unit is saved and moved from the queue to the output data block. 
	 * @param save - save it, or discard it (also from other units associated with 
	 * this crossing
	 */
	public void saveCrossing(boolean save) {
		if (tempCrossing == null) {
			return;
		}
		if (save) {
			difarCrossing = tempCrossing;
			DifarDataUnit[] detList = tempCrossing.getMatchedUnits();
			if (this == detList[0]) {
				for (int i = 1; i < detList.length; i++) {
					if (detList[i] != null)
					detList[i].saveCrossing(save);
				}
			}
		}
		tempCrossing = null;
	}
	
	/**
	 * 
	 * @return the crossing point when multiple matching DIFAR bearings are crossed. 
	 */
	public DIFARCrossingInfo getDifarCrossing() {
		return difarCrossing;
	}

	

	/**
	 * @param difarCrossing the difarCrossing to set
	 */
	public void setDifarCrossing(DIFARCrossingInfo difarCrossing) {
		this.difarCrossing = difarCrossing;
	}


	/**
	 * @return the tempCrossing
	 */
	public DIFARCrossingInfo getTempCrossing() {
		return tempCrossing;
	}


	/**
	 * @param tempCrossing the tempCrossing to set
	 */
	public void setTempCrossing(DIFARCrossingInfo tempCrossing) {
		this.tempCrossing = tempCrossing;
	}


	/**
	 * @return the tempCrossing
	 */
	public String getTempGroup() {
		return tempGroup;
	}

	/**
	 * @param tempCrossing the tempCrossing to set
	 */
	public void setTempGroup(String tempGroup) {
		if (tempGroup==null){ 
			this.tempGroup = DifarParameters.DefaultGroup;
		} else {
			this.tempGroup = tempGroup;
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getOriginLatLong(boolean)
	 */
	@Override
	public GpsData getOriginLatLong(boolean recalculate) {
		// TODO Auto-generated method stub
		return super.getOriginLatLong(recalculate);
	}

	public void saveGroup() {
		trackedGroup = getTempGroup();
		setTempGroup(null);
	}

	/**
	 * Return a spectrogram clip that has calibrated intensity
	 * Intensity is "calibrated" using an arbitrary filter to 
	 * correct for the DIFAR frequency response.
	 * @param i
	 * @param fftLength
	 * @param fftHop
	 * @return
	 */
	public double[][] getCalibratedSpectrogramData(int channel, int fftLength,
			int fftHop) {
		double[] wave = getSpectrogramWaveData(channel, getDisplaySampleRate());
		if (wave == null) {
			return null;
		}
		int nFFT = (wave.length - (fftLength-fftHop)) / fftHop;
		if (nFFT <= 0) {
			return null;
		}
		double[][] specData = new double[nFFT][fftLength/2];
		double[] waveBit = new double[fftLength];
		double[] winFunc = getWindowFunc(fftLength);
		Complex[] complexOutput = Complex.allocateComplexArray(fftLength/2);
		int wPos = 0;
		getFastFFT(fftLength);
		int m = FastFFT.log2(fftLength);
		for (int i = 0; i < nFFT; i++) {
			wPos = i*fftHop;
			for (int j = 0; j < fftLength; j++) {
				waveBit[j] = wave[j+wPos]*winFunc[j];
			}
			fastFFT.rfft(waveBit, complexOutput, m);
			for (int j = 0; j < fftLength/2; j++) {
				specData[i][j] = complexOutput[j].magsq();
			}
		}
		return specData;
	}

	@Override
	public void updateDataUnit(long updateTime) {
		// TODO Auto-generated method stub
		super.updateDataUnit(updateTime);
		/*
		 * This is getting called on the queuedDifarData, bug that's occurring since I added something
		 * to stop it reassigning datablock parent id's when units shift between data blocks. Bugger !
		 */
		SQLLogging logging = this.getParentDataBlock().getLogging();
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (logging != null && dbControl != null) {
			logging.logData(dbControl.getConnection(), this);
		}
	}
}


/**
 * can be used for any separation. Though, only likely to be used with equal separation, the math would not be much simpler so just left general.
 * 
 * data should contain no two points with the same x
 * 
 * @author gw
 *
 */class Point{
	 double x,y;
	 Point(){

	 }
	 Point(double x,double y){
		 this.x=x;
		 this.y=y;

	 }
 }
 class PolyInterp{


	 /**uses data as ys and setx xs to index number
	  * newX should be given appropriately
	  * 
	  * ie for data={0.0,1.5,0.5,2.0}
	  * 
	  * will do for {{0,0.0},{1,1.5},{2,0.5},{3,2.0}}
	  * 
	  * most likely 1<newX<2
	  * 
	  * @param newX
	  * @param data
	  * @return
	  */
	 public static Double LofX(double newX,double[] data){

		 Point[] d = new Point[data.length];

		 for(int i=0;i<data.length;i++){
			 d[i]=new Point(i,data[i]);
		 }

		 return LofX(newX, d);
	 }


	 public static Double LofX(double newX,Point[] data){

		 int degree=data.length-1;

		 double ans=0;
		 for (int j=0;j<degree+1;j++){
			 ans+=data[degree].y*basisPoly(newX,data,j);
		 }

		 return ans;
	 }

	 /**
	  * @param newX 
	  * @param data
	  * @param degree
	  * @return
	  */
	 private static Double basisPoly(double newX, Point[] data, int index) {

		 double ans=1;
		 int degree=data.length-1;

		 for (int m=0;m <degree+1;m++){
			 if(m!=index){
				 ans=ans*(	(newX-data[m].x)/
						 (data[index].x-data[m].x)	);

			 }
		 }

		 return ans;
	 }


	 


 }
