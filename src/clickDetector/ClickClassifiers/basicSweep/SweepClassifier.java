package clickDetector.ClickClassifiers.basicSweep;

import java.awt.Frame;
import java.io.Serializable;
import java.net.URL;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.DeepCloner;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickClassifiers.ClassifyDialogPanel;
import clickDetector.ClickClassifiers.ClickIdInformation;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.ClickClassifiers.ClickTypeCommonParams;
import clickDetector.ClickClassifiers.basic.BasicClickIdentifier;
import clickDetector.layoutFX.clickClassifiers.ClassifyPaneFX;
import clickDetector.layoutFX.clickClassifiers.SweepClassifierPaneFX;

/**
 * An improvements on the BasicClickIdentifier based on work by 
 * Gillespie and Caillat in 2009. 
 * Click length is now measured based on the envelope waveform 
 * rather than a measure of total energy
 * Have also added some parameters extracted from zero crossings
 * and will include better diagnostic plots and histograms.
 *  
 * @author Doug Gillespie
 *
 */
public class SweepClassifier implements ClickIdentifier , PamSettings {

	private ClickDetector clickDetector;
	
	private ClickControl clickControl;
	
	private SweepClassifierPanel dialogPanel;
	
	private SweepClassifierPaneFX fxPane;

	
	private SweepClassifierWorker sweepClassifierWorker;
	
	protected SweepClassifierParameters sweepClassifierParameters = new SweepClassifierParameters();
	
	public SweepClassifierParameters getSweepClassifierParameters() {
		return sweepClassifierParameters;
	}

	public void setSweepClassifierParameters(SweepClassifierParameters sweepClassifierParameters) {
		this.sweepClassifierParameters = sweepClassifierParameters;
	}

	public SweepClassifier(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
		clickDetector = clickControl.getClickDetector();
		sweepClassifierWorker = new SweepClassifierWorker(clickControl, this);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public int codeToListIndex(int code) {
		int n = sweepClassifierParameters.getNumSets();
		for (int i = 0; i < n; i++) {
			if (sweepClassifierParameters.getSet(i).getSpeciesCode() == code) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public JMenuItem getMenuItem(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSpeciesList() {
		int n = sweepClassifierParameters.getNumSets();
		String[] speciesList = new String[n];
		for (int i = 0; i < n; i++) {
		  speciesList[i] = sweepClassifierParameters.getSet(i).getName();	
		}		
		return speciesList;
	}
	/**
     * Returns a list of the currently-defined click types / species codes
     * @return int array with the codes
     */
	@Override
	public int[] getCodeList() {
		int n = sweepClassifierParameters.getNumSets();
		int[] codeList = new int[n];
		for (int i = 0; i < n; i++) {
			codeList[i] = sweepClassifierParameters.getSet(i).getSpeciesCode();	
		}		
		return codeList;
	}

	@Override
	public PamSymbol getSymbol(ClickDetection click) {
		if (click.getClickType() <= 0) {
			return null;
		}
		SweepClassifierSet scs = findClicktypeSet(click.getClickType());
		if (scs == null) {
			return null;
		}
		return scs.symbol;
	}
	
	private SweepClassifierSet findClicktypeSet(int iSpeciesCode) {
		int n = sweepClassifierParameters.getNumSets();
		SweepClassifierSet scs;
		for (int i = 0; i < n; i++) {
			if ((scs = sweepClassifierParameters.getSet(i)).getSpeciesCode() == iSpeciesCode) {
				return scs;
			}
		}
		return null;
	}

    /**
     * Return the superclass of the click type parameters class - currently used for
     * accessing the alarm functions.  Subclasses include ClickTypeParams and
     * SweepClassifierSet.
     *
     * @param code the click type to check
     * @return the ClickTypeCommonParams object related to the species code
     */
	@Override
    public ClickTypeCommonParams getCommonParams(int code) {
        int codeIdx = codeToListIndex(code);
        if (codeIdx<0 || codeIdx>=sweepClassifierParameters.getNumSets()) return null;
        return sweepClassifierParameters.getSet(codeIdx);
    }

	@Override
	public PamSymbol[] getSymbols() {
		int n = sweepClassifierParameters.getNumSets();
		if (n == 0) {
			return null;
		}
		PamSymbol[] symbols = new PamSymbol[n];
		for (int i = 0; i < n; i++) {
			symbols[i] = sweepClassifierParameters.getSet(i).symbol.clone();
		}
		return symbols;
	}

	@Override
	public ClassifyDialogPanel getDialogPanel(Frame windowFrame) {
		if (dialogPanel == null) {
			dialogPanel = new SweepClassifierPanel(this, windowFrame, clickControl);
		}
		return dialogPanel;
	}

	@Override
	public String getSpeciesName(int code) {
		int i = codeToListIndex(code);
		if (i < 0) {
			return null;
		}
		return sweepClassifierParameters.getSet(i).getName();
	}

	@Override
	public Serializable getSettingsReference() {
		return sweepClassifierParameters;
	}

	@Override
	public long getSettingsVersion() {
		return SweepClassifierParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return clickControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "ClickSweepClassifier";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		
		// use the DeepCloner to make a complete copy of the settings
//		sweepClassifierParameters = ((SweepClassifierParameters) pamControlledUnitSettings.getSettings()).clone();
		try {
			sweepClassifierParameters = (SweepClassifierParameters) DeepCloner.deepClone((SweepClassifierParameters) pamControlledUnitSettings.getSettings());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		// add backwards compatibility found in SweepClassifierSet.clone method
		if (sweepClassifierParameters != null && sweepClassifierParameters.getNumSets()!=0) {
			sweepClassifierParameters = sweepClassifierParameters.clone();
			for (int i=0; i<sweepClassifierParameters.getNumSets(); i++) {
				sweepClassifierParameters.getSet(i).checkCompatibility();
			}
		}
		
		return sweepClassifierParameters != null;
	}

	/**
	 * @return the clickDetector
	 */
	public ClickDetector getClickDetector() {
		return clickDetector;
	}
	
	protected int getNextFreeCode(int currCode) {
		int newCode = currCode;
		while (codeTaken(++newCode));
		return newCode;
	}
	
	protected int getPrevFreeCode(int currCode) {
		while (codeTaken(--currCode));
		return currCode;		
	}
	
	protected boolean codeTaken(int code) {
		int n = sweepClassifierParameters.getNumSets();
		for (int i = 0; i < n; i++) {
			if (sweepClassifierParameters.getSet(i).getSpeciesCode() == code) {
				return true;
			}
		}
		return false;
	}

	public boolean codeDuplicated(SweepClassifierSet sweepClassifierSet, int ignoreRow) {
		int code = sweepClassifierSet.getSpeciesCode();
		int n = sweepClassifierParameters.getNumSets();
		for (int i = 0; i < n; i++) {
			if (i == ignoreRow) {
				continue;
			}
			if (sweepClassifierParameters.getSet(i) == sweepClassifierSet) {
				continue;
			}
			if (sweepClassifierParameters.getSet(i).getSpeciesCode() == code) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the params for the sweep classifier. 
	 * @return the sweep classifier params. 
	 */
	public SweepClassifierParameters getSweepClassifierParams() {
		return sweepClassifierParameters; 
	} 
	

	/**
	 * Set the params for the sweep classifier. 
	 * @params the sweep classifier params to set. 
	 */
	public void setSeepClassifierParams(SweepClassifierParameters sweepClassifierParameters) {
		this.sweepClassifierParameters=sweepClassifierParameters; 
	} 
	

	@Override
	public synchronized ClickIdInformation identify(ClickDetection click) {
		return sweepClassifierWorker.identify(click);
	}

	@Override
	public String getParamsInfo(ClickDetection click) {
		/**
		 * Get the parameters from the first classifier in the list and trun them all into a 
		 * nicely formatted string
		 */
		if (sweepClassifierParameters.getNumSets() == 0) {
			return null;
		}
		return getParamsInfo(sweepClassifierParameters.getSet(0), click);
	}

	private String getParamsInfo(SweepClassifierSet scs, ClickDetection click) {
		sweepClassifierWorker.identify(click);
		String str = "Classifier Output:";
		int[][] lengthData = sweepClassifierWorker.getLengthData(click, scs);
		int nChan = lengthData.length;
		
		str += "<p>&#x0009Length: ";
		double sampleRate = clickDetector.getSampleRate();
		double aLen;
		double totLen = 0;
		for (int i = 0; i < nChan; i++) {
			aLen = ((double) lengthData[i][1]-lengthData[i][0]) * 1000. / sampleRate;
			totLen += aLen;
			str += String.format("ch%d=%3.2f, ", i, aLen);
		}
		totLen/=nChan;
		str += String.format("mean=%3.2f ms", totLen);
		
		
		return str;
	}
	
	/**
	 * Returns the zeroCrossingStats variable, used as an identifier in the Rocca interface.
	 * 2014/07/25 MO
	 * 
	 * @param click the Click Detection
	 * @return the ZeroCrossingStats variable
	 */
	public ZeroCrossingStats[] getZeroCrossingStats(ClickDetection click) {
		ZeroCrossingStats[] zcs;
		synchronized (sweepClassifierWorker) {
			zcs = sweepClassifierWorker.getZeroCrossingStats(click); 
		}
		return zcs;
	}
	
	/**
	 * Returns the frequency search range defined for peak frequency testing
	 * 2014/08/03 MO
	 * Split the function to first check for a valid click type set.  In the case of an
	 * unclassified click, findClicktypeSet(click.getClickType()) returns a null and
	 * causes a NullPointerException error.
	 * serialVersionUID = 21 2015/05/31  
	 * 
	 * @param click the Click Detection
	 * @return the min/max of the search range
	 */
	public double[] getPeakSearchRange(ClickDetection click) {
//		return findClicktypeSet(click.getClickType()).getPeakSearchRange();
		SweepClassifierSet scs = findClicktypeSet(click.getClickType());
		if (scs != null) {
			return scs.getPeakSearchRange();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the click length, using the times returned by the
	 * SweepClassifierWorker method getLengthData.  If there is an error, a
	 * 0 is returned.  Note that this defaults to the first channel.
	 * 
	 * 2014/10/13 MO
	 * 
	 * @param click the current click detection
	 * 
	 * @return the duration of the click, in seconds
	 */
	public double getClickLength(ClickDetection click) {
		
		// we first need to figure out which sweep classifier set matches
		// the click type.
		int n = sweepClassifierParameters.getNumSets();
		SweepClassifierSet scs;
		for (int i = 0; i < n; i++) {
			scs = sweepClassifierParameters.getSet(i);
			if (scs.getSpeciesCode()==click.getClickType()) {
				double sum = 0;
				
				// 2017/11/24 synchronize on the SweepClassifierWorker object, to prevent other threads from accessing the methods
				// and screwing up the data until we're finished
				synchronized (sweepClassifierWorker) {
					
					// clear the SweepClassifierWorker lengthData variable, or else the same data will
					// be used for every click if we rerun analysis on the same event
					// serialVersionUID = 21 2015/05/31 
					sweepClassifierWorker.resetLengthData();

					// get a list of times from the SweepClassifierWorker
					int[][] clickTimes = sweepClassifierWorker.getLengthData(click, scs);

					// calculate the duration for each channel, and take the average
					// modification 2014/11/8 - take the max, not the average duration.
					// Original code is left here so that we can easily go back
					int nChan = sweepClassifierWorker.nChannels;
					for (int j = 0; j < nChan; j++) {
						//sum += clickTimes[j][1]-clickTimes[j][0]+1;
						if (clickTimes[j][1]-clickTimes[j][0]+1>sum) {
							sum = clickTimes[j][1]-clickTimes[j][0]+1;
						};
					}
				}
				
				// convert the number of samples to a time (in seconds)
				//return sum / nChan / getClickDetector().getSampleRate();			
				return sum / getClickDetector().getSampleRate();			
			}
		}
		return 0;		
	}

	@Override
	public ClassifyPaneFX getClassifierPane() {
		if (fxPane==null) fxPane = new SweepClassifierPaneFX(this, clickControl);
		return fxPane;
	}

	@Override
	public SymbolData[] getSymbolsData() {
		return BasicClickIdentifier.pamSymbol2SymbolData(getSymbols()) ;
	}
	
	
	

}
