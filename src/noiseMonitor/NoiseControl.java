package noiseMonitor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamTabPanel;

/**
 * Extract noise statistics from acoustic data.
 * <p> 
 * Measurements will be written out to a database at 
 * regular intervals (about once a minute), however, not
 * every sample of incoming data will be used, but a sub sample
 * from within the measuremetnInterval. <p>For each measure, the 
 * output will include the mean, and the median and some other 
 * statistics from the distribution of noise measures. 
 * 
 * <p>
 * Note that this method does not conform to ISO R 266 and ANSI S1.6-1984
 * <p>
 * http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=1168968
 * 
 * 
 * @author Doug Gillespie
 *
 */
public class NoiseControl extends PamControlledUnit implements PamSettings {
	
	protected NoiseSettings noiseSettings = new NoiseSettings();
	
	private NoiseProcess noiseProcess;
	
	private NoiseTabPanel noiseTabPanel;
	
	/**
	 * Centre frequencies for third octave bands. Bands extend from f^(-1/6) to f^(1/6)
	 * or from 0.891f to 1.122f (Richardson et al. p24.)
	 */
	public static final double[] THIRDOCTAVES = {1.0, 1.25, 1.6, 2.0, 2.5, 3.15, 4.0, 5.0, 6.3, 8.0};
	
	public String[] measureNames = {"Mean", "Median", "Lower 95%", "Upper 95%", "Minimum", "Maximum"};

	public NoiseControl(String unitName) {
		super("Noise Monitor", unitName);

		addPamProcess(noiseProcess = new NoiseProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);

		noiseProcess.newSettings();

		noiseTabPanel = new NoiseTabPanel(this, noiseProcess.getNoiseDataBlock());
		
		sortBandEdges();
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		return menuItem;
	}
	
	/**
	 * Work out the frequency edges of third octave bands between fmin and fmax. 
	 * fmin should be > 0 and should in practice also be greater than the minimum 
	 * frequency resolution of the FFT you are using. fmax will probably be niquist
	 * but the highest band edge will probably end up being below this, i.e. it will
	 * stop at 20kHz for 44kHz sampled data. 
	 * @param fmin
	 * @param fmax
	 * @return array of edges of bands. Final number of bands will be this - 1.
	 */
	protected double[][] createThirdOctaveBands(double fmin, double fmax) {
		double[] centres = new double[0];
		double[] hiEdges = new double[0];
		double[] loEdges = new double[0];
		double multiplier = 1;
		int nTO = THIRDOCTAVES.length;
		int ind3 = 0;
		while (multiplier * THIRDOCTAVES[0] > fmin) {
			multiplier /= 10.;
		}
		while (multiplier * THIRDOCTAVES[0] < fmin/10) {
			multiplier *= 10;
		}
		double currCent, prevCent, nextCent;
		double upEdge, lowEdge;
		while (true) {
			currCent = THIRDOCTAVES[ind3] * multiplier;
			prevCent = getPrevCent(ind3, multiplier);
			nextCent = getNextCent(ind3, multiplier);
			lowEdge = Math.sqrt(currCent*prevCent);
			upEdge = Math.sqrt(currCent*nextCent);
			ind3++;
			if (ind3 == nTO) {
				multiplier *=10;
				ind3 = 0;
			}
			if (lowEdge < fmin) {
				continue;
			}
			if (upEdge > fmax) {
				break;
			}
			centres = Arrays.copyOf(centres, centres.length+1);
			loEdges = Arrays.copyOf(loEdges, loEdges.length+1);
			hiEdges = Arrays.copyOf(hiEdges, hiEdges.length+1);
			centres[centres.length-1] = currCent;
			loEdges[loEdges.length-1] = lowEdge;
			hiEdges[hiEdges.length-1] = upEdge;
		}
		double edges[][] = new double[centres.length][2];
		double spread = Math.pow(2., 1/6.);
		for (int i = 0; i < centres.length; i++) {
			edges[i][0] = loEdges[i];
			edges[i][1] = hiEdges[i];
		}
		
		return edges;
	}
	/**
	 * Get next third octave centre
	 * @param index
	 * @param multiplier
	 * @return
	 */
	private double getNextCent(int index, double multiplier) {
		int nTO = THIRDOCTAVES.length;
		if (++index >= nTO) {
			multiplier *= 10;
			index = 0;
		}
		return THIRDOCTAVES[index]*multiplier;
	}
	
	/** 
	 * get rpevioud third octave centre
	 * @param index
	 * @param multiplier
	 * @return
	 */
	private double getPrevCent(int index, double multiplier) {
		int nTO = THIRDOCTAVES.length;
		if (--index < 0) {
			multiplier /= 10;
			index = nTO-1;
		}
		return THIRDOCTAVES[index]*multiplier;
	}
	/**
	 * multiple an array by 10 (used to multiply up third oct edges)
	 * @param a
	 */
	private void times10(double[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] *= 10;
		}
	}
	class SettingsMenu implements ActionListener {
		
		private Frame parentFrame;
		
		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			noiseSettings(parentFrame);
		}
	}
	
	protected void noiseSettings(Frame parentFrame) {
		NoiseSettings newSettings = NoiseDialog.showDialog(this, parentFrame, noiseSettings);
		if (newSettings != null) {
			noiseSettings = newSettings.clone();
			noiseProcess.newSettings();
			sortBandEdges();
			if (noiseTabPanel != null) {
				noiseTabPanel.newSettings();
			}
		}
	}

	protected void sortBandEdges() {
		int nBands = noiseSettings.getNumMeasurementBands();
		double[] loEdges = new double[nBands];
		double[] hiEdges = new double[nBands];
		String[] bandNames = new String[nBands];
		String[] bandLongNames = new String[nBands];
		NoiseMeasurementBand mb;
		for (int i = 0; i < nBands; i++) {
			mb = noiseSettings.getMeasurementBand(i);
			loEdges[i] = mb.f1;
			hiEdges[i] = mb.f2;
			bandNames[i] = mb.name;
			bandLongNames[i] = mb.getLongName();
		}
		NoiseDataBlock nd = noiseProcess.getNoiseDataBlock();
		nd.setBandLoEdges(loEdges);
		nd.setBandHiEdges(hiEdges);
		nd.setBandNames(bandNames);
		nd.setBandLongNames(bandLongNames);
		
		if (noiseTabPanel != null) {
			noiseTabPanel.newSettings();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem displayOpts = new JMenuItem(getUnitName() + " ...");
		displayOpts.addActionListener(new DisplayOptions(parentFrame));
		return displayOpts; 
	}
	
	class DisplayOptions implements ActionListener {

		private Frame parentFrame;
		
		/**
		 * @param parentFrame
		 */
		public DisplayOptions(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			displayOptions(parentFrame);
		}
		
	}

	public void displayOptions(Frame parentFrame) {
		noiseTabPanel.displayOptions(parentFrame);
	}

	@Override
	public Serializable getSettingsReference() {
		return noiseSettings;
	}

	@Override
	public long getSettingsVersion() {
		return NoiseSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		noiseSettings = ((NoiseSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	public String createDBColumnName(int iBand, int iMeasure) {
		NoiseMeasurementBand nmb = noiseSettings.getMeasurementBand(iBand);
		String mName = NoiseDataBlock.measureNames[iMeasure];
		return String.format("%s %d %d %s", nmb.name, (int)nmb.f1, (int)nmb.f2, mName);
	}

	@Override
	public PamTabPanel getTabPanel() {
		if (noiseTabPanel == null) {
			noiseTabPanel = new NoiseTabPanel(this, noiseProcess.getNoiseDataBlock());
		}
		return noiseTabPanel;
	}

	public int getChannelMap() {
		return noiseSettings.channelBitmap;
	}

	/**
	 * @return the noiseProcess
	 */
	public NoiseProcess getNoiseProcess() {
		return noiseProcess;
	}

	public NoiseSettings getNoiseSettings() {
		return noiseSettings;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			noiseProcess.setupProcess();
			sortBandEdges();
		}
	}

}
