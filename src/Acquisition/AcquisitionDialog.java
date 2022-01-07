package Acquisition;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Acquisition.gpstiming.PPSDialogPanel;
import PamController.PamController;
import PamModel.SMRUEnable;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import dataMap.filemaps.OfflineFileDialogPanel;
import dataMap.filemaps.OfflineFileParameters;

/**
 * Main dialog for acquisition control. Takes plug in panels from 
 * the various sound systems to give more device specific controls where
 * necessary. 
 * 
 * @author Doug Gillespie
 *
 */
public class AcquisitionDialog extends PamDialog {

	private static AcquisitionParameters acquisitionParameters;
	
	private static AcquisitionDialog singleInstance;
	
	private static AcquisitionControl acquisitionControl;
	
	private OfflineFileDialogPanel offlineDAQDialogPanel;
	
	private PPSDialogPanel ppsDialogPanel;

	private DaqSystem currentDaqSystem;
	
	private JComboBox deviceType;
	
	private JPanel mainPanel;
	
	private JComponent deviceSpecificPanel;
	
	private JTextField sampleRate, nChannels, vPeak2Peak;
	
	private JTextField preampGain; 
//	bandwidth0, bandwidth1;
	private JCheckBox subtractDC;
	
	private JTextField dcTimeconstant;
	
//	public int channelList[] = new int[PamConstants.MAX_CHANNELS];
//
//	private JLabel panelChannelLabel[] = new JLabel[PamConstants.MAX_CHANNELS];
//	private JComboBox panelChannelList[] = new JComboBox[PamConstants.MAX_CHANNELS];
	
	private ChannelListPanel standardChannelListPanel = new StandardChannelListPanel();
	private ChannelListPanel currentChannelListPanel;
	private JPanel channelListPanelArea = new JPanel();
	//private static AcquisitionParameters acquisitionParameters; //Xiao Yan Deng
	
	/**
	 * Main dialog for data acquisition control 
	 * <p> 
	 * When shown, the dialog contains three main panels. 
	 * <p>The top one shows 
	 * a list of available DaqSystems (e.g. sound cards, NI cards, etc. 
	 * >p>
	 * The middle panel selected based on the type of DaqSytem and is implemented differently
	 * within each DaqSystem. For instance, the sound card DaqSystem displays a list
	 * of available sound cards. The file system displays a list of recent files, 
	 * systems for other ADC cards mght display a channel selector and gain settings
	 * specific for a particular device. 
	 * <p>
	 * The bottom panel shows the number of channels, sample rate, and device sensitivity. 
	 * The selected DaqSystem is queried to see if these are fixed, unknown or user entered
	 * and enables the controls accordingly. If they are set by the DaqSystem, the 
	 * DaqSystem should set them explicity using setSampleRate(), setChannels, and
	 * setVPeak2Peak 
	 *
	 */
	private AcquisitionDialog (Frame parentFrame) {

		super(parentFrame, "Audio Data Acquisition", false);

		
		mainPanel = new JPanel();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(DeviceTypePanel());
		mainPanel.add(SamplingPanel());
		mainPanel.add(CalibrationPanel());
		
		nChannels.addActionListener(new NumChannels()); 

		setHelpPoint("sound_processing.AcquisitionHelp.docs.AcquisitionConfiguration");
		
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			JTabbedPane tabbedPane = new JTabbedPane();
			offlineDAQDialogPanel = new OfflineFileDialogPanel(acquisitionControl, this);
			tabbedPane.add("Offline Files", offlineDAQDialogPanel.getComponent());
			tabbedPane.add("DAQ Settings", mainPanel);
			setDialogComponent(tabbedPane);
		}
		else if (SMRUEnable.isEnable()) {
			JTabbedPane tabbedPane = new JTabbedPane();
			ppsDialogPanel = new PPSDialogPanel(acquisitionControl, this);
			tabbedPane.add("DAQ Settings", mainPanel);
			tabbedPane.add("GPS Timing", ppsDialogPanel.getDialogComponent());
			setDialogComponent(tabbedPane);
		}
		else {
			setDialogComponent(mainPanel);
		}
		setParams(); //Xiao Yan Deng
		sortChannelLists(); // Xiao Yan Deng
	}
	
	/**
	 * Clear the static instance so that the dialog is 
	 * totally rebuilt next time it's launched. 
	 */
	static public void clearInstance() {
		singleInstance = null;
	}
	/**
	 * Shows the data acquisition dialog. 
	 * @param daqControl the calling AcquisitionControl
	 * @param oldParams current parameters from the AcquisitionControl
	 * @return new parameters selected in the dialog
	 * @see AcquisitionControl
	 */
	static public AcquisitionParameters showDialog(Frame parentFrame, AcquisitionControl daqControl, AcquisitionParameters oldParams) {
		
		acquisitionParameters = oldParams.clone();
				
		acquisitionControl = daqControl;
		
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new AcquisitionDialog(parentFrame);
		}
		
		singleInstance.setParams();
		
		singleInstance.sortChannelLists();
		
		singleInstance.setVisible(true);
		
		singleInstance.sortChannelLists(); // Xiao Yan Deng
		
		return acquisitionParameters;
	}
	
	private void setParams() {
		
		// fill in the different device types.
		deviceType.removeAllItems();
		int ind = 0;
		for (int i = 0; i < acquisitionControl.systemList.size(); i++) {
			deviceType.addItem(acquisitionControl.systemList.get(i).getSystemType());
			if (acquisitionControl.systemList.get(i).getSystemType().equals(acquisitionParameters.daqSystemType)) {
				ind = i;
			}
		}
		deviceType.setSelectedIndex(ind);
		
		newDeviceType();
		
		setSampleRate(acquisitionParameters.sampleRate);
		
		setChannels(acquisitionParameters.nChannels);
		
		setVPeak2Peak(acquisitionParameters.voltsPeak2Peak);
		
//		preampGain.setText(String.format("%.1f", acquisitionParameters.preamplifier.getGain()));
		setPreampGain(acquisitionParameters.preamplifier.getGain());
//		bandwidth0.setText(String.format("%.1f", acquisitionParameters.preamplifier.getBandwidth()[0]));
//		bandwidth1.setText(String.format("%.1f", acquisitionParameters.preamplifier.getBandwidth()[1]));
		subtractDC.setSelected(acquisitionParameters.subtractDC);
		dcTimeconstant.setText(String.format("%3.1f", acquisitionParameters.dcTimeConstant));
		
		
		if (currentDaqSystem != null) currentDaqSystem.dialogSetParams();

		if (currentChannelListPanel != null) {
			currentChannelListPanel.setParams(acquisitionParameters.getHardwareChannelList());
		}

		if (offlineDAQDialogPanel != null) {
			offlineDAQDialogPanel.setParams();
		}
		
		if (ppsDialogPanel != null) {
			ppsDialogPanel.setParams(acquisitionParameters.getPpsParameters());
		}
		
		enableControls();
	}

	/**
	 * Called by the specific DaqSystem to set sample rate when it is set by
	 * the DaqSystem (for instance FileInputSystem will set sample rate to the 
	 * sample rate of data in the current file.  
	 * @param sampleRate Current sample rate
	 */
	public void setSampleRate(float sampleRate) {
		this.sampleRate.setText(String.format("%.0f", sampleRate));
	}
	
	/**
	 * Get the sample rate, or null if sample rate is not a valid number
	 * @return sample rate or null
	 */
	public Double getSampleRate() {
		try {
			return Double.valueOf(sampleRate.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Called by the specific DaqSystem to set the number of channels when it is set by
	 * the DaqSystem (for instance FileInputSystem will set it to the 
	 * number of channels in the current file.  
	 * @param nChannels Number of channels
	 */
	public void setChannels(int nChannels) {
		this.nChannels.setText(String.format("%d", nChannels));
	}
	
	/** 
	 * @return the number of channels or null if invalid number
	 */
	public Integer getChannels() {
		try {
			return Integer.valueOf(nChannels.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void setPreampGain(double gain) {
		preampGain.setText(String.format("%.1f", gain));
	}

	/**
	 * Called by the specific DaqSystem to set the peak to peak voltage range.
	 * This is used for calculating absolute SPL's in various detectors
	 * the DaqSystem   
	 * @param vPeak2Peak Peak to Peak input voltage
	 */
	public void setVPeak2Peak(double vPeak2Peak) {
		this.vPeak2Peak.setText(String.format("%4.3f", vPeak2Peak));
	}
	
	// read parameters back from the dialog
	@Override
	public boolean getParams() {
		try {
			acquisitionParameters.daqSystemType = (String) deviceType.getSelectedItem();
			acquisitionParameters.sampleRate = Float.valueOf(sampleRate.getText());
			//
			acquisitionParameters.nChannels = Integer.valueOf(nChannels.getText());
			acquisitionParameters.voltsPeak2Peak = Double.valueOf(vPeak2Peak.getText());
			acquisitionParameters.preamplifier.setGain(Double.valueOf(preampGain.getText()));
//			double[] bw = new double[2];
//			bw[0] = Double.valueOf(bandwidth0.getText());
//			bw[1] = Double.valueOf(bandwidth1.getText());
//			acquisitionParameters.preamplifier.setBandwidth(bw);
			
			if(!currentDaqSystem.areSampleSettingsOk(acquisitionParameters.nChannels, acquisitionParameters.sampleRate)){			
				currentDaqSystem.showSampleSettingsDialog(this);
				return false;
			}		
				
     	    int nP = getNumChannels();
     	    if (getCurrentDaqSystem().supportsChannelLists() && currentChannelListPanel != null) {
     	    	if (currentChannelListPanel.isDataOk() == false) {
     	    		return false;
     	    	}
     	    	int[] chL = currentChannelListPanel.getChannelList();
     	    	for (int i = 0; i < chL.length; i++) {
     	    		acquisitionParameters.setChannelList(i, chL[i]);
     	    	}
     	    }
     	    else {
     	    	acquisitionParameters.setDefaultChannelList();
     	    }

		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		
		acquisitionParameters.subtractDC = subtractDC.isSelected();
		if (acquisitionParameters.subtractDC) {
			try {
				acquisitionParameters.dcTimeConstant = Double.valueOf(dcTimeconstant.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid number for DC background time constant");
			}
			if (acquisitionParameters.dcTimeConstant <= 0) {
				return showWarning("The DC bacround subtractino time constant must be greater than zero");
			}
		}
		
		
		if (offlineDAQDialogPanel != null) {
			OfflineFileParameters ofp = offlineDAQDialogPanel.getParams();
			if (ofp == null) {
				return false;
			}
			acquisitionControl.getOfflineFileServer().setOfflineFileParameters(ofp);
		}

		if (currentDaqSystem != null){
			if (!currentDaqSystem.dialogGetParams()) {
				return false;
			}
		}

		if (ppsDialogPanel != null) {
			if (!ppsDialogPanel.getParams(acquisitionParameters)) {
				return false;
			}
		}
		
		return true;
	}
	
	/** 
	 * Read the latest sample rate value. 
	 * @return sample rate Hz. 
	 */
	public double readSampleRate() {
		double sr = 0;
		try {
			sr = Double.valueOf(sampleRate.getText());
		}
		catch(NumberFormatException e) {
			return -1;
		}
		return sr;
	}

	
	@Override
	public void cancelButtonPressed() {
		acquisitionParameters = null;
	}
	@Override
	public void restoreDefaultSettings() {
		
	}
	
	private class NewDeviceType implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			newDeviceType();
		}
	}
	private class NumChannels implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			sortChannelLists();
		}
	}

	private JPanel DeviceTypePanel () {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Data Source Type"));
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.CENTER, deviceType = new JComboBox());
		deviceType.setMinimumSize(new Dimension(30,5));
		deviceType.addActionListener(new NewDeviceType());
		return p;
	}
	
	/**
	 * Only need to show the channel panel for certain device types,
	 *
	 */
	private void showHideChannelPanel() {
		DaqSystem currentSystem = getCurrentDaqSystem();
		if (currentSystem == null) return;
		channelListPanelArea.removeAll();
		currentChannelListPanel = null;
		ChannelListPanel specialChannelListPanel = currentSystem.getDaqSpecificChannelListPanel(this);
		if (specialChannelListPanel != null) {
			currentChannelListPanel = specialChannelListPanel;
			channelListPanelArea.add(specialChannelListPanel.getComponent());
		}
		else if (currentSystem.supportsChannelLists()) {
			currentChannelListPanel = standardChannelListPanel;
			channelListPanelArea.add(standardChannelListPanel.getComponent());
//			channelListPanel.setVisible(currentSystem.supportsChannelLists());
		}
		
	}
	
	private JPanel SamplingPanel () {
		
		JPanel sP = new PamAlignmentPanel(new BorderLayout(), BorderLayout.WEST);
		
		JPanel p = new JPanel();
		sP.setBorder(new TitledBorder("Sampling"));
		GridBagLayout layout;
		
		p.setLayout(layout = new GridBagLayout());
		GridBagConstraints constraints = new PamGridBagContraints();
		
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(2,2,2,2);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		addComponent(p, new JLabel("Sample Rate ", SwingConstants.RIGHT), constraints);
		constraints.gridx ++;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.WEST;
		addComponent(p, sampleRate = new JTextField(8), constraints);
		constraints.gridx +=2;
		constraints.gridwidth = 1;
		addComponent(p, new JLabel(" Hz"), constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		addComponent(p, new JLabel("Number of Channels "), constraints);
		constraints.gridx ++;
		addComponent(p, nChannels = new JTextField(3), constraints);
		constraints.gridx ++;
		constraints.gridwidth = 2;
		addComponent(p, new JLabel(" (hit enter)"), constraints);
		
		sP.add(BorderLayout.NORTH, p);
		
//		constraints.gridy++;
//		constraints.gridx = 0;
//		constraints.gridwidth = 4;
		standardChannelListPanel = new StandardChannelListPanel();
//		addComponent(p, channelListPanel, constraints);
		
		constraints.insets = new Insets(2,2,2,2);
		
		
		sP.add(BorderLayout.CENTER, channelListPanelArea);
		
		return sP;
	}
	
//	
//	private JPanel createStandardChannelListPanel() {
//
//		/* code for select channel */
//		/*
//		 * put this in a separate panel so it can be hidden if 
//		 * it's not possible to change these parameters. 
//		 * 
//		 * Text information updated DG & JG 12/8/08
//		 */
//		JPanel cP;
//		cP = new JPanel();
//		cP.setLayout(new GridBagLayout());
//		GridBagConstraints c2 = new GridBagConstraints();
//		c2.gridx = 0;
//		c2.gridy = 0;
//		c2.gridwidth = 4;
//		c2.anchor = GridBagConstraints.WEST;
////		addComponent(channelListPanel, new JLabel("Select Hardware (HW) Channels"), c2);
//		addComponent(cP, new JLabel("Map Hardware (HW) to Software (SW) Channels"), c2);
//
//		c2.gridwidth = 1;
//		String spaceStr;
//		String s = "<html>PAMGUARD channel numbering starts at 0.<br>Your hardware channel numbering may start at 1.";
//		s += "<br>So be aware. If you've put a plug into socket 1, <br>you probably want to select channel 0, etc.</html>";
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){ //Xiao Yan Deng
//		//for (int i = 0; i < getNumChannels(); i++){
//			
//			if (i%2 ==0){
//				c2.gridx = 0;
//				c2.gridy ++;
//			}
//			else {
//				c2.gridx++;
//			}
//			//constraints.gridwidth = 2;
//			if (i%2 == 1) {
//				spaceStr = "           ";
//			}
//			else {
//				spaceStr = "";
//			}
//			addComponent(cP, panelChannelLabel[i] = 
//				new JLabel(spaceStr + " SW Ch " + i + " = HW Ch "), c2);
//			c2.gridx ++;
//			//constraints.gridwidth = 2;
//			addComponent(cP, panelChannelList[i] = new JComboBox(), c2);
//			panelChannelLabel[i].setToolTipText(s);
//			panelChannelList[i].setToolTipText(s);
//			
//		}
//		return cP;
//	}
//	
	private JPanel CalibrationPanel() {
			
		JPanel p = new PamAlignmentPanel(BorderLayout.WEST) ;
		
		p.setBorder(new TitledBorder("Calibration"));
		GridBagLayout layout;
		
		p.setLayout(layout = new GridBagLayout());
		GridBagConstraints constraints = new PamGridBagContraints();
		
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2,2,2,2);
		constraints.gridx = 0;
		constraints.gridy = 0;

		constraints.gridx = 0;
		//constraints.gridy = 2;
//		constraints.gridy++;
//		constraints.gridwidth = 3;
//		addComponent(p, new JLabel("Calibration data ... "), constraints);
//		constraints.gridy++;
		constraints.gridwidth = 1;
		addComponent(p, new JLabel("Peak-Peak voltage range "), constraints);
		constraints.gridx ++;
		addComponent(p, vPeak2Peak = new JTextField(5), constraints);
		constraints.gridx ++;
		addComponent(p, new JLabel(" V "), constraints);

		constraints.gridwidth = 1;
		constraints.gridx = 0;
		//constraints.gridy = 3;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
		addComponent(p, new JLabel("Preamplifier gain "), constraints);
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.WEST;
		addComponent(p, preampGain = new JTextField(5), constraints);
		constraints.gridx++;
		constraints.gridwidth = 2;
		addComponent(p, new JLabel(" dB"), constraints);

		constraints.gridwidth = 1;
		constraints.gridx = 0;
		//constraints.gridy = 4;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
//		addComponent(p, new JLabel("Bandwidth "), constraints);
//		constraints.gridx++;
//		constraints.anchor = GridBagConstraints.WEST;
//		addComponent(p, bandwidth0 = new JTextField(7), constraints);
//		constraints.gridx++;
//		addComponent(p, new JLabel(" to "), constraints);
//		constraints.gridx++;
//		addComponent(p, bandwidth1 = new JTextField(7), constraints);
//		constraints.gridx++;
//		addComponent(p, new JLabel(" Hz "), constraints);
		p.add(subtractDC = new JCheckBox("Subtract DC with "), constraints);
		constraints.gridx++;
		p.add(dcTimeconstant = new JTextField(4), constraints);
		constraints.gridx++;
		p.add(new JLabel("s time constant"), constraints);
		subtractDC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		});
		String t = "Some input devices have a fixed (DC) offset in their voltage measurement.\n" + 
		"Subtracting this off can lead to improved PAMGuard performance.";
		dcTimeconstant.setToolTipText(t);
		subtractDC.setToolTipText(t);
		
		
		
		return p;
	}

	protected void enableControls() {
		dcTimeconstant.setEnabled(subtractDC.isSelected());
	}


	/**
	 * 
	 * @return the number of channels
	 */
	int getNumChannels() { 
		try {
			return Integer.valueOf(nChannels.getText());
		}
		catch (Exception Ex) {
			return 0;
		}
	}

	private void sortChannelLists() { 
		// first of all, only show the ones in range of nPanels
		if (currentChannelListPanel != null) {
			currentChannelListPanel.setNumChannels(getNumChannels());
		}
		pack();
	}

	
	
	/**
	 * Called when the device type changes. Loads the appropriate panel for 
	 * the newly selected DaqSystem into the dialog
	 *
	 */ 
	private void newDeviceType() {
		
		int devNumber = deviceType.getSelectedIndex();
		
		if (devNumber < 0) return;
		
		// remove the old type specific panel and replace it with a new one.
		if (deviceSpecificPanel != null) {
			mainPanel.remove(deviceSpecificPanel);
		}
		currentDaqSystem = acquisitionControl.systemList.get(devNumber);
		
		deviceSpecificPanel = currentDaqSystem.getDaqSpecificDialogComponent(this);
		if (deviceSpecificPanel != null) {
			mainPanel.add(deviceSpecificPanel, 1);
			currentDaqSystem.dialogSetParams();
		}
		
		sampleRate.setEnabled(currentDaqSystem.getMaxSampleRate() != DaqSystem.PARAMETER_FIXED);
		nChannels.setEnabled(currentDaqSystem.getMaxChannels() != DaqSystem.PARAMETER_FIXED);
		vPeak2Peak.setEnabled(currentDaqSystem.getPeak2PeakVoltage(0) == DaqSystem.PARAMETER_UNKNOWN);
		
		showHideChannelPanel();
		
		if (currentChannelListPanel != null) {
			currentChannelListPanel.setNumChannels(getNumChannels());
			currentChannelListPanel.setParams(acquisitionParameters.getHardwareChannelList());
		}
		
		pack();
	}
	public DaqSystem getCurrentDaqSystem() {
		return currentDaqSystem;
	}
	
	/**
	 * 
	 * @return the sample rate component
	 */
	public JTextField getSampleRateComponent() {
		return sampleRate;
	}
	
	/**
	 * 
	 * @return the nChannels component. 
	 */
	public JTextField getnChanComponent() {
		return nChannels;
	}
	
	
	
}
