package RightWhaleEdgeDetector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import fftManager.FFTDataUnit;

public class RWEDialog extends PamDialog {

	private RWEParameters rweParameters;
	private static RWEDialog singleInstance;
	private SourcePanel sourcePanel;
	
	private RWEControl rweControl;
	
	private JTextField startFreq, endFreq, thresholdDB;
	private JTextField minSoundType;
	private JCheckBox downThreshold;
		
	private RWEDialog(RWEControl rweControl, Window parentFrame) {
		super(parentFrame, "Right Whale Edge Detector Settings", true);
		this.rweControl = rweControl;
		sourcePanel = new SourcePanel(this, "Source and Channels", 
				FFTDataUnit.class, true, true);
		sourcePanel.setSourceToolTip("Data source should be the output of a Spectrogram Smmothing Kernel");
		JPanel panel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Data Source", sourcePanel.getPanel());
		
		// now the params panel. 
		JPanel pp = new JPanel();
		pp.setLayout(new BorderLayout());
		
		JPanel det = new JPanel(new GridBagLayout());
		det.setBorder(new TitledBorder("Detection"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(det, new JLabel("Start Frequency ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(det, startFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(det, new JLabel(" Hz", SwingConstants.RIGHT), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(det, new JLabel("End Frequency ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(det, endFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(det, new JLabel(" Hz", SwingConstants.RIGHT), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(det, new JLabel("Threshold ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(det, thresholdDB = new JTextField(5), c);
		c.gridx++;
		addComponent(det, new JLabel(" dB", SwingConstants.RIGHT), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(det, new JLabel("Appy down threshold ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(det, downThreshold = new JCheckBox(), c);

		JPanel cl = new JPanel(new GridBagLayout());
		cl.setBorder(new TitledBorder("Detection"));
		c = new PamGridBagContraints();
		addComponent(cl, new JLabel("Minimum sound type ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(cl, minSoundType = new JTextField(3), c);
		
		startFreq.setToolTipText("Minimum frequency for sound search");
		endFreq.setToolTipText("Maximum frequency for sound search");
		thresholdDB.setToolTipText("Detection threshold");
		String txt = "<html>Sound classification types are:";
		int n = RWESound.getNumSoundTypes();
		for (int i = 0; i <= n; i++) {
			txt += String.format("<BR>%d - %s", i, RWESound.getTypeString(i));
		}
		txt += "</html>";
		minSoundType.setToolTipText(txt);
		downThreshold.setToolTipText("Applies threshold as dB down from the peak, as well as up from the noise floor");
		
		pp.add(BorderLayout.NORTH, det);
		pp.add(BorderLayout.CENTER, cl);
		tabbedPane.add("Parameters", pp);
		
		
		panel.add(BorderLayout.CENTER, tabbedPane);
		setDialogComponent(panel);
	}
	
	public static RWEParameters showDialog(Window frame, RWEControl rweControl) {
		if (singleInstance == null || frame != singleInstance.getOwner() || rweControl != singleInstance.rweControl) {
			singleInstance = new RWEDialog(rweControl, frame);
		}
		singleInstance.rweParameters = rweControl.rweParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.rweParameters;
	}

	private void setParams() {
		sourcePanel.setSource(rweParameters.dataSourceName);
		sourcePanel.setChannelList(rweParameters.channelMap);
		
		startFreq.setText(String.format("%3.1f", rweParameters.startFreq));
		endFreq.setText(String.format("%3.1f", rweParameters.endFreq));
		double thDB = 10. * Math.log10(rweParameters.threshold);
		thresholdDB.setText(String.format("%3.1f", thDB));
		downThreshold.setSelected(rweParameters.downThreshold);
		minSoundType.setText(String.format("%d", rweParameters.minSoundType));
	}

	@Override
	public boolean getParams() {
		rweParameters.channelMap = sourcePanel.getChannelList();
		rweParameters.dataSourceName = sourcePanel.getSource().getDataName();
		rweParameters.downThreshold = downThreshold.isSelected();
		if (rweParameters.dataSourceName == null) {
			return showWarning("You must select a valid input data source");
		}
		if (rweParameters.channelMap == 0) {
			return showWarning("You must select at lease one data channel");
		}
		try {
			rweParameters.startFreq = Double.valueOf(startFreq.getText());
			rweParameters.endFreq = Double.valueOf(endFreq.getText());
			double thresh = Double.valueOf(thresholdDB.getText());
			thresh = Math.pow(10., thresh/10.);
			rweParameters.threshold = thresh;
			rweParameters.minSoundType = Integer.valueOf(minSoundType.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid detection or classification parameter");
		}
//		boolean gok = rweControl.hasKernelSmoothing(sourcePanel.getSource());
//		if (gok == false) {
//			return showWarning("Right whale detector input must include Gaussian Kernel Smoothing as part of the FFT module, or a stand alone smoothing module");
//		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		rweParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		RWEParameters np = new RWEParameters();
		np.dataSourceName = rweParameters.dataSourceName;
		np.channelMap = rweParameters.channelMap;
		rweParameters = np;
		setParams();
	}

}
