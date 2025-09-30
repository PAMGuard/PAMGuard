package mel.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamUtils.FrequencyFormat;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import mel.AudioFeatureExtraction;
import mel.MelControl;
import mel.MelParameters;
import noiseMonitor.ResolutionPanel;

public class MelDialog extends PamDialog {

	private static MelDialog singleInstance;
	private MelControl melControl;
	private MelParameters melParams;

	private SourcePanel sourcePanel;

	private ResolutionPanel resolutionPanel;

	private JTextField n_Mel, minFrequency, maxFrequency, power;
	private JLabel smallestStep;

	private MelDialog(MelControl melControl) {
		super(melControl.getGuiFrame(), melControl.getUnitName() + " settings", true);
		this.melControl = melControl;
		sourcePanel = new SourcePanel(singleInstance, FFTDataUnit.class, true, true);
		resolutionPanel = new ResolutionPanel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new TitledBorder("FFT Source"));
		topPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
		topPanel.add(BorderLayout.SOUTH, new WestAlignedPanel(resolutionPanel.getPanel()));
		mainPanel.add(BorderLayout.NORTH, topPanel);
		sourcePanel.addSelectionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceSelection();
			}
		});


		n_Mel = new JTextField(6);
		minFrequency = new JTextField(6);
		maxFrequency = new JTextField(6);
		power = new JTextField(6);
		smallestStep = new JLabel(" ");
		TypeAction ta = new TypeAction();
		n_Mel.addActionListener(ta);
		minFrequency.addActionListener(ta);
		maxFrequency.addActionListener(ta);	
		n_Mel.addKeyListener(ta);
		minFrequency.addKeyListener(ta);
		maxFrequency.addKeyListener(ta);
		n_Mel.addFocusListener(ta);
		minFrequency.addFocusListener(ta);
		maxFrequency.addFocusListener(ta);


		JPanel cPanel = new WestAlignedPanel(new GridBagLayout());
		cPanel.setBorder(new TitledBorder("Mel Settings"));
		mainPanel.add(BorderLayout.CENTER, cPanel);
		GridBagConstraints c = new PamGridBagContraints();
		cPanel.add(new JLabel("Min Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(minFrequency, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Max Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(maxFrequency, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Number of Mels ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(n_Mel, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Power ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(power, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Smallest Step ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(smallestStep, c);

		setDialogComponent(mainPanel);
	}

	protected void sourceSelection() {
		FFTDataBlock source = (FFTDataBlock) sourcePanel.getSource();
		if (source == null) {
			return;
		}
		resolutionPanel.setParams(source);
		checkSmallestStep();
	}

	private class TypeAction extends KeyAdapter implements ActionListener, FocusListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			checkSmallestStep();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					checkSmallestStep();
				}
			});
		}

		@Override
		public void focusGained(FocusEvent e) {
			checkSmallestStep();
		}

		@Override
		public void focusLost(FocusEvent e) {
			checkSmallestStep();
		}


	}
	
	/**
	 * Be aware that the smallest step in the mel is smaller than the FFT resulution. 
	 * @return false if there is a problem, true if it's probably OK
	 */
	private boolean warnSmallestStep(boolean showWarning) {
		Double melStep = getSmallestStep();
		if (melStep == null) {
			return true;
		}
		FFTDataBlock source = (FFTDataBlock) sourcePanel.getSource();
		if (source == null) {
			return true;
		}
		double fStep = source.getSampleRate() / source.getFftLength();
		if (melStep > fStep) {
			return true;
		}
		if (showWarning) {

			String w = String.format("<html>The smallest step in your mels spectrum is %s, which is"
					+ " smaller than the bin size in the FFT data of %s. <br>"
					+ " You should either reduce the number of mels, raise the lower frequency, "
					+ " or adjust the FFT parameters."
					+ "<br>Press Ok to continue anyway, or Cancel to change the parameters.", FrequencyFormat.formatFrequency(getSmallestStep(),  true),
					FrequencyFormat.formatFrequency(fStep, true));
			int ans = WarnOnce.showWarning("Mel Spectrogram Warning", w, WarnOnce.OK_CANCEL_OPTION);
			return ans == WarnOnce.OK_OPTION;
		}
		return false;
		
	}
	
	private void checkSmallestStep() {
		Double sStep = getSmallestStep();
		if (sStep == null) {
			smallestStep.setText(" - ");
		}
		smallestStep.setText(String.format("%3.1f Hz", sStep));
	}
	
	/**
	 * Check that the smallest mel frequency stop is not smaller than 
	 * the smallest FFT step. 
	 */
	private Double getSmallestStep() {
		FFTDataBlock source = (FFTDataBlock) sourcePanel.getSource();
		if (source == null) {
			return null;
		}
		double fStep = source.getSampleRate() / source.getFftLength();

		double minF, maxF;
		int nMel;
		try {
			minF = Double.valueOf(minFrequency.getText());
			maxF = Double.valueOf(maxFrequency.getText());
			nMel = Integer.valueOf(n_Mel.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
//		AudioFeatureExtraction af = new AudioFeatureExtraction();
//		af.setSampleRate(source.getSampleRate());
//		af.setfMin(minF);
//		af.setfMax(maxF);
//		af.setN_fft(source.getFftLength());
//		af.setN_mels(nMel);
//		af.get
		double minStep = 0;
		try {
			double step = Math.pow(maxF/minF, 1./(nMel+1));
			minStep = minF * (step-1.);
		}
		catch (Exception e) {
			return null;
		}
		return minStep;
	}

	public static MelParameters showDialog(MelControl melControl) {
		//		if (singleInstance == null || singleInstance.melControl != melControl) {
		singleInstance = new MelDialog(melControl);
		//		}
		singleInstance.setParams(melControl.getMelParameters());
		singleInstance.setVisible(true);
		return singleInstance.melParams;
	}

	private void setParams(MelParameters melParams) {
		this.melParams = melParams;
		sourcePanel.setSource(melParams.dataSource);
		sourcePanel.setChannelList(melParams.chanelMap);
		minFrequency.setText(String.format("%3.1f", melParams.minFrequency));
		maxFrequency.setText(String.format("%3.1f", melParams.maxFrequency));
		power.setText(String.format("%3.1f", melParams.power));
		n_Mel.setText(String.format("%d", melParams.nMel));
		sourceSelection();
	}

	@Override
	public boolean getParams() {
		PamDataBlock source = sourcePanel.getSource();
		if (source == null) {
			return showWarning("You must select a source of FFT data as input");
		}
		melParams.dataSource = source.getLongDataName();
		melParams.chanelMap = sourcePanel.getChannelList();
		if (melParams.chanelMap == 0) {
			return showWarning("You must select at least one inpur channel");
		}
		try {
			melParams.minFrequency = Double.valueOf(minFrequency.getText());
			melParams.maxFrequency = Double.valueOf(maxFrequency.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("Invalid frequency value");
		}
		if (melParams.maxFrequency <= melParams.minFrequency) {
			return showWarning("The maximum frequency must be greater than the minimum frequency");
		}
		try {
			melParams.nMel = Integer.valueOf(n_Mel.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("The number of Mels must be a positive integer value");
		}
		if (melParams.nMel <= 0) {
			return showWarning("The number of Mels must be a positive integer value");
		}
		try {
			melParams.power = Double.valueOf(power.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("Invalid power value");
		}
		if (melParams.power <= 0) {
			return showWarning("The power value must be greater than 0 (default is 2)");
		}
		boolean melOK = warnSmallestStep(true);
					
		if (melOK == false) {
			return false;
		}
		
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		melParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new MelParameters());
	}

}
