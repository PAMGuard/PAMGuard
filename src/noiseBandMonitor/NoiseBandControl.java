package noiseBandMonitor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import Filters.ButterworthMethod;
import Filters.FIRFilterMethod;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamTabPanel;
import noiseMonitor.NoiseTabPanel;

public class NoiseBandControl extends PamControlledUnit implements PamSettings {

	protected NoiseBandSettings noiseBandSettings = new NoiseBandSettings();
	
	private NoiseBandProcess noiseBandProcess;
	
	private int[] decimatorIndex;

	private NoiseTabPanel tabPanel;

	private BandData bandData;
	/** 
	 * Third octave band centres using exact base 2 option 
	 * from ANSI S1.11-2004
	 */
//	public static final double bandCentres3[] = {24.80};
	
	public NoiseBandControl(String unitName) {
		super("Noise Band", unitName);
		addPamProcess(noiseBandProcess = new NoiseBandProcess(this));
		PamSettingManager.getInstance().registerSettings(this);
		noiseBandProcess.setupProcess();
		tabPanel = new NoiseTabPanel(this, noiseBandProcess.getNoiseDataBlock());
//		new BandData(BandData.THIRD_OCTAVE_BAND, 1, 250000);
//		new BandData(BandData.OCTAVE_BAND, 1, 250000);
	}

	/**
	 * @return the noiseBandSettings
	 */
	public NoiseBandSettings getNoiseBandSettings() {
		return noiseBandSettings;
	}

	/**
	 * @return the noiseBandProcess
	 */
	public NoiseBandProcess getNoiseBandProcess() {
		return noiseBandProcess;
	}

	@Override
	public PamTabPanel getTabPanel() {
		return tabPanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Settings...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		return menuItem;
	}
	
	class SettingsMenu implements ActionListener {

		private Frame parentFrame;

		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsMenu(parentFrame);
		}
		
	}

	public void settingsMenu(Frame parentFrame) {
		NoiseBandSettings newSettings = NoiseBandDialog.showDialog(parentFrame, this);
		if (newSettings != null) {
			noiseBandSettings = newSettings.clone();
			noiseBandProcess.setupProcess();
			sortBandEdges();
		}
	}

	public ArrayList<FilterMethod> makeDecimatorFilters(NoiseBandSettings noiseSettings, double sampleRate) {
		ArrayList<FilterMethod> decFilters = new ArrayList<FilterMethod>();
//		float sampleRate = (float) getSampleRate();
		for (int i = 0; i < noiseSettings.endDecimation; i++) {
			FilterParams filterParams = new FilterParams();
			filterParams.chebyGamma = noiseSettings.firGamma;
			filterParams.filterBand = FilterBand.LOWPASS;
			filterParams.filterType = noiseSettings.filterType;
			filterParams.lowPassFreq = (float) sampleRate/4;
			switch(noiseSettings.filterType) {
			case BUTTERWORTH:
				filterParams.filterOrder = noiseSettings.iirOrder+2;
				filterParams.lowPassFreq /= Math.sqrt(2.);
				ButterworthMethod bm;
				decFilters.add(bm = new ButterworthMethod(sampleRate, filterParams));
				break;
			case FIRWINDOW:
				filterParams.filterOrder = noiseSettings.firOrder;
				decFilters.add(new FIRFilterMethod(sampleRate, filterParams));
				break;
			}
			sampleRate/=2;
		}
		return decFilters;
	}

	
	public ArrayList<FilterMethod> makeBandFilters(NoiseBandSettings noiseSettings, 
			ArrayList<FilterMethod> decimationFilters, 
			double topSampleRate) {
		// work out the lowest frequency we're likely to go to. 
		ArrayList<FilterMethod> bandFilters = new ArrayList<FilterMethod>();
		double minFreq = topSampleRate / Math.pow(2., noiseSettings.endDecimation+1) / 2;
//		double maxFreq = 
		double maxFreq = BandData.calcFreq(noiseSettings.highBandNumber) * BandData.getBandHalfWidth(noiseSettings.bandType);
		bandData = new BandData(noiseSettings.bandType, minFreq, maxFreq);
		// can now work through them backwards - the first three must be in the 
		// top octave, etc.
		double[] loEdges = bandData.getBandLoEdges();
		double[] hiEdges = bandData.getBandHiEdges();
		double[] centreFreqs = bandData.getBandCentres();
		FilterMethod decimator;
		bandFilters.clear();
		if (hiEdges == null) {
			return null;
		}
		decimatorIndex = new int[hiEdges.length];
		double sampleRate;
		/*
		 *  make the filters in reverse frequency order to
		 *  match descending order of decimators.  
		 */
		int iBand = 0;
		for (int i = hiEdges.length-1; i >= 0; i--) {
			decimatorIndex[iBand] = findDecimatorIndex(decimationFilters, hiEdges[i]);
			if (decimatorIndex[iBand] < 0) {
				sampleRate = topSampleRate;
			}
			else {
				decimator = decimationFilters.get(decimatorIndex[iBand]);
				sampleRate = (float) decimator.getSampleRate()/2;
			}
			FilterParams filterParams = new FilterParams();
			filterParams.chebyGamma = noiseSettings.firGamma;
			filterParams.filterBand = FilterBand.BANDPASS;
			filterParams.filterType = noiseSettings.filterType;
			filterParams.lowPassFreq = (float) (hiEdges[i]);
			filterParams.highPassFreq = (float) (loEdges[i]);
			filterParams.setCentreFreq(centreFreqs[i]);
			switch(noiseSettings.filterType) {
			case BUTTERWORTH:
//				filterParams.lowPassFreq *= 0.97;
//				filterParams.highPassFreq *= 1.03;
				filterParams.filterOrder = noiseSettings.iirOrder;
				bandFilters.add(new ButterworthMethod(sampleRate, filterParams));
//				Debug.out.printf("Creating filter method input fs %3.1fHz, filter %3.1f - %3.1fHs\n", sampleRate, filterParams.highPassFreq, filterParams.lowPassFreq);
				break;
			case FIRWINDOW:
				filterParams.lowPassFreq *= 1.01;
				filterParams.highPassFreq *= 0.98;
				filterParams.filterOrder = noiseSettings.firOrder;
				bandFilters.add(new FIRFilterMethod(sampleRate, filterParams));
				break;
			}
			iBand++;
		}
		return bandFilters;
	}

	public int[] getDecimatorIndexes() {
		return decimatorIndex;
	}
	
	int findDecimatorIndex(ArrayList<FilterMethod> decimationFilters, double hiFreq) {
		if (decimationFilters == null) {
			return -1;
		}
		double bandGap = 1;
		int nDecimators = decimationFilters.size();
		FilterMethod aFilter;
		for (int i = nDecimators-1; i >= 0; i--) {
			aFilter = decimationFilters.get(i);
			if (aFilter.getFilterParams().filterType == FilterType.BUTTERWORTH) {
				bandGap = Math.pow(2, 1./2.);
			}
			else {
				bandGap = 1.;
			}
			if (aFilter.getFilterParams().lowPassFreq > hiFreq*bandGap) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Serializable getSettingsReference() {
		return noiseBandSettings;
	}

	@Override
	public long getSettingsVersion() {
		return NoiseBandSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		noiseBandSettings = ((NoiseBandSettings)pamControlledUnitSettings.getSettings()).clone();
//		System.out.println("********************************************************");
//		System.out.println("NOISE BAND SETTINGS: " + noiseBandSettings.rawDataSource);
//		System.out.println("********************************************************");
		return true;
	}

	protected void sortBandEdges() {
		if (bandData == null) {
			return;
		}
		getNoiseBandProcess().getNoiseDataBlock().setBandLoEdges(bandData.getBandLoEdges());
		getNoiseBandProcess().getNoiseDataBlock().setBandHiEdges(bandData.getBandHiEdges());
		// also do the band names
		String[] bandNames = new String[bandData.getBandLoEdges().length];
		for (int i = 0; i < bandNames.length; i++) {
			if (noiseBandSettings.bandType == BandType.OCTAVE) {
				bandNames[i] = "Octave";
			}
			else {
				bandNames[i] = "ThirdOctave";
			}
		}
		getNoiseBandProcess().getNoiseDataBlock().setBandNames(bandNames);
		getNoiseBandProcess().getNoiseLogging().createAndCheckTable();
		tabPanel.newSettings();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			noiseBandProcess.setupProcess();
			sortBandEdges();
		}
	}
	
}
