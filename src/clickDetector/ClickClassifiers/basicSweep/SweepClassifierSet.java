package clickDetector.ClickClassifiers.basicSweep;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.swing.JOptionPane;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import fftFilter.FFTFilterParams;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import clickDetector.ClickClassifiers.ClickTypeCommonParams;

/**
 * Parameters for the sweep click classifier. Also handles some basic data cehcking
 * @author Doug Gillespie
 *
 */
public class SweepClassifierSet extends ClickTypeCommonParams
        implements Serializable, Cloneable, ManagedParameters {
	
	transient static public final String[] defaultSpecies = {"Porpoise", "Beaked Whale"};

	public static final long serialVersionUID = 1L;
	
	private static final int CURRENTVERSION = 1;
	
	private int activeVersion = CURRENTVERSION; // used to rev version number without changing serialVersionUID. 

	public PamSymbol symbol;
	
	public boolean enable;
	
	/**
	 * These next three parameters have been moved to a super class. However, for 
	 * data to deserialise correctly, they need to be left in place and set when data are
	 * read back in ...
	 */
	@Deprecated
	private String name;

	@Deprecated
	private int speciesCode;

	@Deprecated
	private boolean discard = false;
	
	// general options
	static public final int CHANNELS_REQUIRE_ALL = 0;
	static public final int CHANNELS_REQUIRE_ONE = 1;
	static public final int CHANNELS_USE_MEANS = 2;
	
	//length options
	static public final int CLICK_CENTER = 0; //click length is from the center of the analytic waveform
	static public final int CLICK_START = 1;  //click length is from the start

	
	static public String getChannelOptionsName(int iOpt) {
		switch (iOpt) {
		case CHANNELS_REQUIRE_ALL:
			return "Require positive idenitification on all channels individually";
		case CHANNELS_REQUIRE_ONE:
			return "Require positive identification on only one channel";
		case CHANNELS_USE_MEANS:
			return "Use mean parameter values over all channels";
		}
		return null;
	}
	
	public int channelChoices = CHANNELS_REQUIRE_ALL;
	
	public boolean restrictLength = true;
	
	public int restrictedBins = 128;
	
	/**
	 * The type of length restriction. O is around the click sensor
	 * and 1 is a simple trim from the start of the click. 
	 */
	public int restrictedBinstype = 0; 
	
	// length stuff
	public boolean enableLength = true;
	
	public int lengthSmoothing = 5;
	
	public double lengthdB = 6;
	
	public double minLength = 0, maxLength = 1;
	
	// energy bands stuff
	public static final transient int nControlBands = 2;
	
	public boolean enableEnergyBands = false;
	
	public double[] testEnergyBand = new double[2];
	
	public double[][] controlEnergyBand = new double[nControlBands][2];
	
	public double[] energyThresholds = new double[nControlBands];
	
	//amplitude stuff
	public boolean testAmplitude;
	
	public double[] amplitudeRange;
	
	public boolean enableFFTFilter = false;
	
	public FFTFilterParams fftFilterParams;
	
	// peak frequency stuff
	public boolean enablePeak, enableWidth, enableMean;
	public double peakSearchRange[];
	public double peakRange[];
	public double peakWidthRange[];
	public double meanRange[];
	public int peakSmoothing = 5;
	public double peakWidthThreshold = 6;
	
	// zero crossings stuff. 
	public boolean enableZeroCrossings;
	public int[] nCrossings;
	public boolean enableSweep;
	public double[] zcSweep;
	
	//cross correlation stuff
	public boolean enableMinXCrossCorr; //enable a minimum required xcorr coefficient
	public boolean enablePeakXCorr; //enable a check of whether the maximum peak is greater than the minimum peak
	public double minCorr = 0; // the minimum correlation value
	public double corrFactor = 1; //the minimum correlation factor - how much the peak has to be above the trough
	
	//bearing limits
	public boolean enableBearingLims; //enable the bearing limits;
	public boolean excludeBearingLims; //true to use exclude between bearings in case of limits. 
	public double[] bearingLims; //the bearing limits in Radians.  
	
	
	public SweepClassifierSet() {
		checkEnergyParamsAllocation();
		checkPeakFreqAllocation();
		checkZCAllocation();
	}

	@Override
	protected SweepClassifierSet clone() {
		try {
			SweepClassifierSet newSet = (SweepClassifierSet) super.clone();
			// enableSeep has been separates from enableZeroCrossings, but for backwards compatibility
			// set them to be the same if the version is being reved. 
			if (newSet.activeVersion < 1) {
				newSet.activeVersion = CURRENTVERSION;
				newSet.enableSweep = newSet.enableZeroCrossings;
			}
			if (newSet.symbol == null) {
				newSet.symbol = new PamSymbol();
			}
			else {
				newSet.symbol = newSet.symbol.clone();
			}
			if (this.name != null) {
				newSet.setName(this.name);
//				this.name = null; // so this never happens again ....
				setDiscard(this.discard);
			}
			if (this.speciesCode > 0) {
				newSet.setSpeciesCode(this.speciesCode);
//				this.speciesCode = 0; // so this never happens again
			}
			return newSet;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Copied from the clone method...
	 * enableSweep has been separated from enableZeroCrossings, but for backwards compatibility
	 * set them to be the same if the version is being reved. 
	 */
	public void checkCompatibility() {
		if (activeVersion < 1) {
			activeVersion = CURRENTVERSION;
			enableSweep = enableZeroCrossings;
		}
	}

	public void checkEnergyParamsAllocation() {
		if (testEnergyBand == null || testEnergyBand.length != 2) {
			testEnergyBand = new double[2];
		}
		if (controlEnergyBand == null || controlEnergyBand.length != nControlBands) {
			controlEnergyBand = new double[nControlBands][];
		}
		for (int i = 0; i < nControlBands; i++) {
			if (controlEnergyBand[i] == null || controlEnergyBand[i].length != 2) {
				controlEnergyBand[i] = new double[2];
			}
		}
		if (energyThresholds == null || energyThresholds.length != nControlBands) {
			energyThresholds = new double[nControlBands];
		}
	}

	public void checkPeakFreqAllocation() {
		if (peakSearchRange == null || peakSearchRange.length != 2) {
			peakSearchRange = new double[2];
		}
		if (peakRange == null || peakRange.length != 2) {
			peakRange = new double[2];
		}
		if (peakWidthRange == null || peakWidthRange.length != 2) {
			peakWidthRange = new double[2];
		}
		if (meanRange == null || meanRange.length != 2) {
			meanRange = new double[2];
		}
	}
	
	/**
	 * Check whether zero crossing parameters are set up properly
	 */
	public void checkZCAllocation() {
		if (nCrossings == null || nCrossings.length != 2) {
			nCrossings = new int[2];
		}
		if (zcSweep == null || zcSweep.length != 2) {
			zcSweep = new double[2];
		}
	}
	
	public void checkXCCorrAllocation() {
		if (corrFactor == 0 ) {
			corrFactor = 1; //corr factor must be above zero
		}
	}
	
	public void checkBearingAllocation() {
		if (bearingLims == null || bearingLims.length != 2 ) {
			bearingLims = new double[2]; //corr factor must be above zero
			bearingLims[0] = -Math.PI;
			bearingLims[1] =  Math.PI;
		}
	}

	
	/**
	 * check that the settings can be processed at the current sample rate. 
	 * @param sampleRate sample rate in Hz
	 * @param verbose true if you want visible warning messages. 
	 * @return true if OK, false otherwise. 
	 */
	public boolean canProcess(double sampleRate, boolean verbose) {
		// check that the given settings can process at 
		// the given frequency
		double nFreq = sampleRate/2;
		if (enableEnergyBands) {
			if (testEnergyBand[1] > nFreq) {
				return sayWarning("Test energy band is at too high a frequency", verbose);
			}
			for (int i = 0; i < nControlBands; i++) {
				if (controlEnergyBand[i][1] > nFreq) {
					return sayWarning("A control energy band is at too high a frequency", verbose);
				}
			}
		}
		if (enablePeak || enableWidth || enableMean) {
			if (peakSearchRange[1] > nFreq) {
				return sayWarning("Peak frequency search and integration range is at too high a frequency", verbose);
			}			
		}
		if (enablePeak && peakRange[1] > nFreq){
			return sayWarning("Peak frequency range is at too high a frequency", verbose);
		}			
		if (enableWidth && peakWidthRange[1] > nFreq){
			return sayWarning("Peak Width range is at too high a frequency", verbose);
		}			
		if (enableMean && meanRange[1] > nFreq){
			return sayWarning("Mean frequency range is at too high a frequency", verbose);
		}			
		
		return true;
	}
	
	/**
	 * 
	 * @return true if one or more tests require length data. 
	 */
	protected boolean needLength() {
		if (enableLength || restrictLength || enableZeroCrossings) {
			return true;
		}
		return false;
	}
	
	private boolean sayWarning(String warningText, boolean verbose) {
		if (verbose == false) {
			return false;
		}
		String warnTitle = "Click Classifier - ";
		if (name != null) {
			warnTitle += name;
		}
		JOptionPane.showMessageDialog(null, warningText, warnTitle, JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	public boolean setSpeciesDefaults(String species) {
		if (species.equalsIgnoreCase(defaultSpecies[0])) {
			porpoiseDefaults();
			return true;
		}
		else if (species.equalsIgnoreCase(defaultSpecies[1])) {
			beakedWhaleDefaults();
			return true;
		}
		return false;
	}
	
	public void beakedWhaleDefaults() {
		checkEnergyParamsAllocation();
		checkPeakFreqAllocation();
		checkZCAllocation();
		setName("Beaked Whale");
		symbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.MAGENTA, Color.MAGENTA);
		enableLength = false;
		minLength = 0.1;
		maxLength = 0.5;
		
		enableEnergyBands = true;
		testEnergyBand[0] = 24000;
		testEnergyBand[1] = 48000;
		controlEnergyBand[0][0] = 12000;
		controlEnergyBand[0][1] = 24000;
		controlEnergyBand[1][0] = 12000;
		controlEnergyBand[1][1] = 24000;
		energyThresholds[0] = 3;
		energyThresholds[1] = 3;
		
		enablePeak = true;
		peakSearchRange[0] = 10000;
		peakSearchRange[1] = 96000;
		peakSmoothing = 5;
		peakRange[0] = 25000;
		peakRange[1] = 48000;
		peakWidthThreshold = 6;
		enableWidth = enableMean = false;
		
		enableMean = true;
		meanRange[0] = 25000;
		meanRange[1] = 48000;
		
		enableZeroCrossings = true;
		nCrossings[0] =7;
		nCrossings[1] = 50;
		zcSweep[0] = 1;
		zcSweep[1] = 500;
		
		enableMinXCrossCorr = false;

	}
	public void porpoiseDefaults() {
		checkEnergyParamsAllocation();
		checkPeakFreqAllocation();
		checkZCAllocation();
		setName("Porpoise");
		symbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, 10, 10, true, Color.RED, Color.RED);
		enableLength = true;
		minLength = 0.03;
		maxLength = 0.22;
		lengthdB = 6;
		
		enableEnergyBands = true;
		testEnergyBand[0] = 100000;
		testEnergyBand[1] = 150000;
		controlEnergyBand[0][0] = 40000;
		controlEnergyBand[0][1] = 90000;
		controlEnergyBand[1][0] = 160000;
		controlEnergyBand[1][1] = 190000;
		energyThresholds[0] = 6;
		energyThresholds[1] = 6;
		
		enablePeak = true;
		peakSearchRange[0] = 40000;
		peakSearchRange[1] = 240000;
		peakSmoothing = 5;
		peakRange[0] = 100000;
		peakRange[1] = 150000;
		enableWidth = enableMean = false;
		
		enableZeroCrossings = true;
		nCrossings[0] = 10;
		nCrossings[1] = 50;
		zcSweep[0] = -200;
		zcSweep[1] = 200;
		
		enableMinXCrossCorr = false;
		
	}
	
	
	/**
	 * @return the peakSearchRange
	 */
	public double[] getPeakSearchRange() {
		return peakSearchRange;
	}

	/* (non-Javadoc)
	 * @see clickDetector.ClickClassifiers.ClickTypeCommonParams#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
		super.name = name;
	}

	/* (non-Javadoc)
	 * @see clickDetector.ClickClassifiers.ClickTypeCommonParams#setSpeciesCode(int)
	 */
	@Override
	public void setSpeciesCode(int speciesCode) {
		this.speciesCode = speciesCode;
		super.speciesCode = speciesCode;
	}

	/* (non-Javadoc)
	 * @see clickDetector.ClickClassifiers.ClickTypeCommonParams#setDiscard(java.lang.Boolean)
	 */
	@Override
	public void setDiscard(Boolean discard) {
		this.discard = discard;
		super.discard = discard;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("activeVersion");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return activeVersion;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("discard");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return discard;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("name");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return name;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("speciesCode");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return speciesCode;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}



}
