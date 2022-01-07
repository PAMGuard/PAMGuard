package whistlesAndMoans;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import spectrogramNoiseReduction.SpectrogramNoiseDialogPanel;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import cepstrum.CepstrumProcess;

public class WhistleToneDialog extends PamDialog {

	private static WhistleToneDialog singleInstance;
	
	private WhistleMoanControl whistleMoanControl;
	
	private WhistleToneParameters whistleToneParameters;
	
	private GroupedSourcePanel sourcePanel;
	
	private JTextField minFreq, maxFreq;
	
	private JComboBox connectType;
	
	private JTextField minLength, minPixels, maxCrossLength;
	
	private JComboBox fragmentation;
	
	private JCheckBox removeStubs;
	
	private SpectrogramNoiseDialogPanel spectrogramNoiseDialogPanel;
	
	private WhistleToneDialog(Window parentFrame, WhistleMoanControl whistleMoanControl) {
		super(parentFrame, "Whistle and Moan Detector", true);
		this.whistleMoanControl = whistleMoanControl;

		JPanel p = new JPanel(new BorderLayout());
		sourcePanel = new GroupedSourcePanel(this, "Source of FFT data", FFTDataUnit.class, true, true, true);
		p.add(BorderLayout.NORTH, sourcePanel.getPanel());
		
		JPanel d = new JPanel();
		d.setBorder(new TitledBorder("Connections"));
		d.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(d, new JLabel("Min Frequency "), c);
		c.gridx++;
		addComponent(d, minFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(d, new JLabel(" Hz"), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(d, new JLabel("Max Frequency "), c);
		c.gridx++;
		addComponent(d, maxFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(d, new JLabel(" Hz"), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(d, new JLabel("Connection Type "), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(d, connectType = new JComboBox(), c);
		connectType.addItem("Connect 4 (sides only)");
		connectType.addItem("Connect 8 (sides and diagonals)");
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 1;
		
		JPanel e = d;//new JPanel();
//		e.setLayout(new GridBagLayout());
//		c = new PamGridBagContraints();
//		e.setBorder(new TitledBorder("Selection"));
		addComponent(e, new JLabel("Minimum length "), c);
		c.gridx++;
		addComponent(e, minLength = new JTextField(5), c);
		c.gridx++;
		addComponent(e, new JLabel(" time slices"), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(e, new JLabel("Minimum total size "), c);
		c.gridx++;
		addComponent(e, minPixels = new JTextField(5), c);
		c.gridx++;
		addComponent(e, new JLabel(" pixels"), c);
		c.gridy++;
		c.gridx=0;
		addComponent(e, new JLabel(" Shape 'stubs' ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(e, removeStubs = new JCheckBox("Remove small stubs"), c);
		c.gridy++;
		c.gridx=0;
		addComponent(e, new JLabel("Crossing and Joining "), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(e, fragmentation = new JComboBox(), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addComponent(e, new JLabel("Max Cross length "), c);
		c.gridx++;
		addComponent(e, maxCrossLength = new JTextField(5), c);
		c.gridx++;
		addComponent(e, new JLabel(" time slices"), c);
		fragmentation.addItem("Leave branched regions intact");
		fragmentation.addItem("Discard branched regions");
		fragmentation.addItem("Separate all branches");
		fragmentation.addItem("Re-link across joins");
		fragmentation.addActionListener(new FragmentationListener());

		p.add(BorderLayout.CENTER, d);
//		p.add(BorderLayout.SOUTH, e);	
		
		minFreq.setToolTipText("Minimum search frequency");
		maxFreq.setToolTipText("Maximum search frequency");
		connectType.setToolTipText("Search type (sides, or sides and diagonals");
		minLength.setToolTipText("Minimum length of sound, (FFT time bins)");
		minPixels.setToolTipText("Minimum total area of sound (FFT time-frequency bins)");
		fragmentation.setToolTipText("Handling of crossing, merging and branching sounds");
		maxCrossLength.setToolTipText("Maximum duration of a crossing region when whistles cross (FFT time bins)");
		removeStubs.setToolTipText("Remove short bits of whistle shape stickint out from main contour: helps reduce whistle fragmentation");
		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Detection", p);
		
		spectrogramNoiseDialogPanel = new SpectrogramNoiseDialogPanel(whistleMoanControl.getSpectrogramNoiseProcess());
		spectrogramNoiseDialogPanel.setSourcePanel(sourcePanel);
		tabbedPane.add("Noise and Thresholding", spectrogramNoiseDialogPanel.getPanel());
		sourcePanel.excludeDataBlock(whistleMoanControl.getSpectrogramNoiseProcess().getOutputDataBlock(), true);
//		sourcePanel.excludeDataBlock(whistleToneControl.getWhistleToneProcess()., exclude)
		
		setHelpPoint("detectors.whistleMoanHelp.docs.whistleMoan_Overview");
		
		setDialogComponent(tabbedPane);
	}
	
	public static WhistleToneParameters showDialog(Frame parentFrame, WhistleMoanControl whitesWhistleControl) {
		if (singleInstance == null || whitesWhistleControl != singleInstance.whistleMoanControl ||
				parentFrame != singleInstance.getOwner()) {
			singleInstance = new WhistleToneDialog(parentFrame, whitesWhistleControl);
		}
		singleInstance.whistleToneParameters = whitesWhistleControl.whistleToneParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.whistleToneParameters;
	}

	@Override
	public void cancelButtonPressed() {
		whistleToneParameters = null;
	}

	private void setParams() {
//		sourcePanel.setSource(whistleToneParameters.dataSource);
//		sourcePanel.setChannelList(whistleToneParameters.channelList);
		sourcePanel.setParams(whistleToneParameters);
		minFreq.setText(String.format("%.0f", whistleToneParameters.getMinFrequency()));
		maxFreq.setText(String.format("%.0f", whistleToneParameters.
				getMaxFrequency(whistleMoanControl.getWhistleToneProcess().getSampleRate())));
		if (whistleToneParameters.getConnectType() == 8) {
			connectType.setSelectedIndex(1);
		}
		else {
			connectType.setSelectedIndex(0);
		}
		minLength.setText(String.format("%d", whistleToneParameters.minLength));
		minPixels.setText(String.format("%d", whistleToneParameters.minPixels));
		fragmentation.setSelectedIndex(whistleToneParameters.fragmentationMethod);
		maxCrossLength.setText(String.format("%d", whistleToneParameters.maxCrossLength));
		removeStubs.setSelected(!whistleToneParameters.keepShapeStubs);
		spectrogramNoiseDialogPanel.setParams(whistleToneParameters.getSpecNoiseSettings());
		
		enableControls();
	}
	
	@Override
	public boolean getParams() {
		if (sourcePanel.getParams(whistleToneParameters) == false) {
			return false;
		}
		if (whistleToneParameters.getChanOrSeqBitmap() == 0) {
			return showWarning("You must select at least one detection channel");
		}
		try {
			whistleToneParameters.setMinFrequency(Double.valueOf(minFreq.getText()));
			whistleToneParameters.setMaxFrequency(Double.valueOf(maxFreq.getText()));
			whistleToneParameters.minLength = Integer.valueOf(minLength.getText());
			whistleToneParameters.minPixels = Integer.valueOf(minPixels.getText());
			whistleToneParameters.maxCrossLength = Integer.valueOf(maxCrossLength.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		whistleToneParameters.keepShapeStubs = (removeStubs.isSelected() == false);
		if (connectType.getSelectedIndex() == 1) {
			whistleToneParameters.setConnectType(8);
		}
		else {
			whistleToneParameters.setConnectType(4);
		}
		whistleToneParameters.fragmentationMethod = fragmentation.getSelectedIndex();
		if (spectrogramNoiseDialogPanel.getParams(whistleToneParameters.getSpecNoiseSettings()) == false) {
			return false;
		}
//		PamDataBlock dataSource = sourcePanel.getSource();
//		if (dataSource != null) {
//			whistleToneParameters.dataSource = dataSource.getDataName();
//			whistleToneParameters.channelList = dataSource.getChannelMap();
//		}
		boolean ok = checkMethods();
		if (ok == false) {
			String msg = "For the Whistle and tone detector to work, you must use \n" +
					"the following spectrogram noise reduction methods:\n" +
					"Median filter\n" +
					"Average Subtraction\n" +
					"Thresholding\n";
			return showWarning(msg);
		}
		
		return ok;
	}
	
	private void enableControls() {
		maxCrossLength.setEnabled(fragmentation.getSelectedIndex() == WhistleToneParameters.FRAGMENT_RELINK);
	}
	
	/**
	 * check that the right noise reduction methods have been selected. 
	 * @return true if enough noise reduction is in place. 
	 */
	private boolean checkMethods() {
		if (isCepstrumSource()) {
			return true; //no obligatory methods
		}
		// The methods we really need are numbers  0, 1 and 3
		// the Gaussian smoothing is optional. 
		int[] required = {0, 1, 3};
		for (int i = 0; i < required.length; i++) {
			if (spectrogramNoiseDialogPanel.hasProcessed(required[i]) == false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find out if the source of data is a cepstrum, not a spectrogram.
	 * @return true if a parent process is a cepstrum. 
	 */
	private boolean isCepstrumSource( ) {
		if (whistleToneParameters == null || whistleToneParameters.getDataSource() == null) {
			return false;
		}

		PamDataBlock sourceData = PamController.getInstance().getDataBlock(FFTDataUnit.class, 
				whistleToneParameters.getDataSource());
		int n = 0;
		while (sourceData != null) {
			if (++n > 100) {
				return false; // stuck in a loop
			}
			PamProcess process = sourceData.getParentProcess();
			if (process == null) {
				return false;
			}
			if (process instanceof CepstrumProcess) {
				return true;
			}
			else {
				sourceData = process.getParentDataBlock();
			}
		}
		
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// leave the source alone and set other parameters dependent on the 
		// selected source. 
		PamDataBlock dataSource = sourcePanel.getSource();
		if (dataSource == null) {
			return;
		}
		WhistleToneParameters dummyParams = new WhistleToneParameters();
		whistleToneParameters.setDataSource(dataSource.getDataName());
		whistleToneParameters.setMinFrequency(0);
		whistleToneParameters.setMaxFrequency(whistleMoanControl.getWhistleToneProcess().getSampleRate()/2);
		whistleToneParameters.setConnectType(dummyParams.getConnectType());
		whistleToneParameters.minLength = dummyParams.minLength;
		whistleToneParameters.minPixels = dummyParams.minPixels;
		whistleToneParameters.fragmentationMethod = dummyParams.fragmentationMethod;
		whistleToneParameters.maxCrossLength = dummyParams.maxCrossLength;
		
		SpectrogramNoiseSettings sns = new SpectrogramNoiseSettings();
		for (int i = 0; i < 4; i++) {
			sns.setRunMethod(i, true);
		}
		whistleToneParameters.setSpecNoiseSettings(sns);
		setParams();
//		spectrogramNoiseDialogPanel.setParams(sns);
		// may have inadvertently turned on stuff that's already run, so 
		// call the enable function to sort it out
		spectrogramNoiseDialogPanel.enableControls();
	}
	
	private class FragmentationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

}
