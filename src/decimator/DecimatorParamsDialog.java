package decimator;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Filters.FilterBand;
import Filters.FilterDialog;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.dialog.SourcePanelMonitor;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import dataMap.filemaps.OfflineFileDialogPanel;
import dataMap.filemaps.OfflineFileParameters;

public class DecimatorParamsDialog extends PamDialog {

	private static DecimatorParamsDialog singleInstance;
	
	private static DecimatorParams decimatorParams;
	
	private DecimatorControl decimatorControl;
	
	private JTextField newSampleRate;
	
	private SourcePanel sourcePanel;
	
	private JButton filterButton, defaultFilterButton;
	
	private JLabel sourceSampleRate;
	
	private float inputSampleRate = 1;
	
	private Frame parentFrame;

	private boolean isViewer;

	private OfflineFileDialogPanel offlineDAQDialogPanel;
	
	private JLabel filterInfo;
	
	private JComboBox<String> interpolator;
	
	private DecimatorParamsDialog(Frame parentFrame, DecimatorControl decimatorControl) {
		
		super(parentFrame, "Decimator ...", true);
		
		this.parentFrame = parentFrame;
		this.decimatorControl = decimatorControl;
		
		JPanel mainPanel = new JPanel();
		
		GridBagConstraints constraints = new PamGridBagContraints();
//		constraints.insets = new Insets(2,2,2,2);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		sourcePanel = new SourcePanel(this, "Input Data Source", RawDataUnit.class, true, true);
		sourcePanel.addSourcePanelMonitor(new SPMonitor());
		sourcePanel.addSelectionListener(new SPSelection());
		mainPanel.add(sourcePanel.getPanel());		
		
		JPanel decimatorPanel = new JPanel();
		decimatorPanel.setLayout(new GridBagLayout());
		PamAlignmentPanel alp;
		mainPanel.add(alp = new PamAlignmentPanel(decimatorPanel, BorderLayout.WEST));
		alp.setBorder(new TitledBorder("Decimator settings"));
		
		constraints.gridx = 0;
		addComponent(decimatorPanel, new JLabel("Source sample rate "), constraints);
		constraints.gridx++;
		addComponent(decimatorPanel, sourceSampleRate = new JLabel(" - Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy ++;
		addComponent(decimatorPanel, new JLabel("Output sample rate "), constraints);
		constraints.gridx ++;
		addComponent(decimatorPanel, newSampleRate = new JTextField(5), constraints);
		constraints.gridx ++;
		addComponent(decimatorPanel, new JLabel(" Hz"), constraints);
		constraints.gridy ++;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		addComponent(decimatorPanel, filterButton = new JButton("Filter settings"), constraints);
		filterButton.addActionListener(new FilterButton());
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		addComponent(decimatorPanel, defaultFilterButton = new JButton("Default Filter"), constraints);
		defaultFilterButton.addActionListener(new DefaultFilterButton());
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		constraints.gridy++;
		addComponent(decimatorPanel, filterInfo = new JLabel("Filter: "), constraints);
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.gridy++;
		addComponent(decimatorPanel, new JLabel("Interpolation: ", SwingConstants.RIGHT), constraints);
		constraints.gridx += constraints.gridwidth;
		constraints.gridwidth = 2;
		addComponent(decimatorPanel, interpolator = new JComboBox<String>(), constraints);
		interpolator.addItem("None");
		interpolator.addItem("Linear");
		interpolator.addItem("Quadratic");

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (isViewer) {
			JTabbedPane tabbedPane = new JTabbedPane();
			offlineDAQDialogPanel = new OfflineFileDialogPanel(decimatorControl, this);
			tabbedPane.add("Offline Files", offlineDAQDialogPanel.getComponent());
			tabbedPane.add("Runtime Settings", mainPanel);
			setDialogComponent(tabbedPane);
		}
		else {
			setDialogComponent(mainPanel);
		}
		
		setHelpPoint("sound_processing.decimatorHelp.docs.decimator_decimator");
		filterButton.setToolTipText("Manual adjustment of filter settings");
		defaultFilterButton.setToolTipText("Set a default filter (6th order Butterworth low pass at Decimator Nyquist frequency)");
		interpolator.setToolTipText("If Decimation / upsampling is not by an integer value, you should use interpolation to improve waveform reconstruction");
	}
	
		
	private class SPSelection implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			newDataSource();
		}
	}
	
	private class SPMonitor implements SourcePanelMonitor {

		@Override
		public void channelSelectionChanged() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private void selectFilters() {
		float filtSampleRate = Math.max(inputSampleRate, getOutputSampleRate());
		FilterParams newFP = FilterDialog.showDialog(parentFrame,
				decimatorParams.filterParams, filtSampleRate);
		if (newFP != null) {
			decimatorParams.filterParams = newFP.clone();
		}
		sayFilter();
	}
	
	private float getOutputSampleRate() {
		try {
			float fs = Float.valueOf(newSampleRate.getText());
			return fs;
		}
		catch (NumberFormatException e) {
			return inputSampleRate;
		}
	}


	class FilterButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			selectFilters();
		}
	}
	
	class DefaultFilterButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			restoreDefaultSettings();
		}
	}

	public static DecimatorParams showDialog(Frame parentFrame, DecimatorControl decimatorControl, DecimatorParams oldParams) {
		decimatorParams = oldParams.clone();
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.decimatorControl != decimatorControl) {
			singleInstance = new DecimatorParamsDialog(parentFrame, decimatorControl);
		}
		singleInstance.decimatorControl = decimatorControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return decimatorParams;
	}
	
	private void setParams() {
		sourcePanel.excludeDataBlock(decimatorControl.decimatorProcess.getOutputDataBlock(0), true);
		sourcePanel.setSourceList();
		PamRawDataBlock currentBlock = PamController.getInstance().getRawDataBlock(decimatorParams.rawDataSource);
		sourcePanel.setSource(currentBlock);
		sourcePanel.setChannelList(decimatorParams.channelMap);
		newSampleRate.setText(String.format("%.1f", decimatorParams.newSampleRate));
		newDataSource();
		if (offlineDAQDialogPanel != null) {
			offlineDAQDialogPanel.setParams();
		}
		interpolator.setSelectedIndex(decimatorParams.interpolation);
		sayFilter();
	}
	
	/**
	 * display filter information
	 */
	private void sayFilter() {
		if (decimatorParams == null || decimatorParams.filterParams == null) {
			filterInfo.setText("No filter");
		}
		else {
			filterInfo.setText("Filter: " + decimatorParams.filterParams.toString());
		}		
	}

	private void newDataSource() {
		PamDataBlock block = sourcePanel.getSource();
		if (block != null) {
			sourceSampleRate.setText(String.format("%.1f Hz", 
					inputSampleRate = block.getSampleRate()));
		}
	}
	
	@Override
	public boolean getParams() {
		try {
//			ArrayList<PamDataBlock> rawBlocks = PamController.getInstance().getRawDataBlocks();
			decimatorParams.rawDataSource =  sourcePanel.getSource().getDataName();
			decimatorParams.channelMap = sourcePanel.getChannelList();
			decimatorParams.newSampleRate = java.lang.Float.valueOf(newSampleRate.getText());
		}
		catch (Exception Ex) {
			return false;
		}
		
		if (decimatorParams.rawDataSource == null) {
			return showWarning("You must select a raw data source");
		}
		
		if (decimatorParams.channelMap == 0) {
			return showWarning("You must select at least one channel for decimation");
		}

		if (offlineDAQDialogPanel != null) {
			OfflineFileParameters ofp = offlineDAQDialogPanel.getParams();
			if (ofp == null) {
				return false;
			}
			decimatorControl.getOfflineFileServer().setOfflineFileParameters(ofp);
		}
		
		decimatorParams.interpolation = interpolator.getSelectedIndex();
		boolean isInt = decimatorControl.isIntegerDecimation(sourcePanel.getSource().getSampleRate(), decimatorParams.newSampleRate);
		if (isInt && decimatorParams.interpolation > 0) {
			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio equal to a whole number, there is no need to interpolate", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
			else {
				decimatorParams.interpolation = 0;
			}
		}
		if (!isInt && decimatorParams.interpolation == 0) {
			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio NOT equal to a whole number, it is recommended that you use linear or quadratic interpolation", 
					WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
			else {
//				decimatorParams.interpolation = 0;
			}
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		decimatorParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		/*
		 *  does not set the output sample rate, but does set sensible values for the 
		 *  filter.  
		 */
		float newFS = 0;
		try {
			newFS = java.lang.Float.valueOf(newSampleRate.getText());
		}
		catch (NumberFormatException e) {
		}
		PamDataBlock sourceblock = sourcePanel.getSource();
		if (sourceblock.getSampleRate() > 0) {
			newFS = Math.min(newFS, sourceblock.getSampleRate());
		}
		if (newFS <= 0) {
			showWarning("Invalid output samplerate : " + newSampleRate.getText());
			return;
		}
		decimatorParams.filterParams.lowPassFreq = newFS/2;
		decimatorParams.filterParams.filterType = FilterType.BUTTERWORTH;
		decimatorParams.filterParams.filterOrder = 6;
		decimatorParams.filterParams.filterBand = FilterBand.LOWPASS;
		
		sayFilter();
	}

//	void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
//		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
//		panel.add(p);
//	}
}
