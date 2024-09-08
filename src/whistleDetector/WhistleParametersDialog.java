package whistleDetector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;

public class WhistleParametersDialog extends PamDialog {
	
	private static WhistleParameters whistleParameters;
	
	private WhistleControl whistleControl;
	
	private static WhistleParametersDialog singleInstance = null;
	
	protected float sampleRate;
	
	private GroupedWhistleSourcePanel whistleSourcePanel;
	
	private PeakPanel peakPanel;
	
	private LinkPanel linkPanel;
	
	private WhistlePanel whistlePanel;
	
	private EventPanel eventPanel;
	
	private WhistleParametersDialog (Frame parentFrame) {
		
		super(parentFrame, "Whistle Detector Parameters", true);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		whistleSourcePanel = new GroupedWhistleSourcePanel(this, "FFT Data Source", FFTDataUnit.class, true, false, true);
		tabbedPane.addTab("Source", whistleSourcePanel.getPanel());
		tabbedPane.addTab("Peak Detection", peakPanel = new PeakPanel());
		tabbedPane.addTab("Linking", linkPanel = new LinkPanel());
		
		JPanel w = new JPanel();
		w.setLayout(new BoxLayout(w, BoxLayout.Y_AXIS));
//		tabbedPane.addTab("Whistle Selection", whistlePanel = new WhistlePanel());
//		tabbedPane.addTab("Whistle Events", eventPanel = new EventPanel());
		w.add(whistlePanel = new WhistlePanel());
		w.add(eventPanel = new EventPanel());
		tabbedPane.addTab("Whistles", w);
		
		p.add(BorderLayout.CENTER, tabbedPane);
		
		setDialogComponent(p);
		
		setHelpPoint("detectors.whistleDetectorHelp.docs.whistleDetector_Overview");
		
		pack();
	}
	
	public static WhistleParameters showDialog(Frame parentFrame, WhistleControl whistleControl,
			WhistleParameters oldParameters, float sampleRate) {
		
		whistleParameters = oldParameters.clone();
		
		if (singleInstance == null) {
			singleInstance = new WhistleParametersDialog(parentFrame);
		}
		singleInstance.whistleControl = whistleControl;
		
		singleInstance.sampleRate = sampleRate;
		
		singleInstance.setParams(whistleParameters);
		
		singleInstance.setVisible(true);
		
		return whistleParameters;
	}
	
	private void setParams(WhistleParameters whistleParameters) {
//		whistleSourcePanel.setParams(whistleParameters);

		PamDataBlock fftDataBlock = PamController.getInstance().
		getFFTDataBlock(whistleParameters.fftDataName);
		whistleSourcePanel.excludeDataBlock(whistleControl.whistleDetector.supressedSpectrogram, true);
		whistleSourcePanel.setSource(fftDataBlock);
		whistleSourcePanel.setChannelList(whistleParameters.channelBitmap);
		whistleSourcePanel.setChannelGroups(whistleParameters.channelGroups);
		whistleSourcePanel.setGrouping(whistleParameters.groupingType);
		peakPanel.setParams(whistleParameters);
		linkPanel.setParams(whistleParameters);
		whistlePanel.setParams(whistleParameters);
		eventPanel.setParams(whistleParameters);
	}
	
	@Override
	public boolean getParams() {
//		if (!whistleSourcePanel.getParams()) return false;
		PamDataBlock fftDataBlock = whistleSourcePanel.getSource();
		if (fftDataBlock == null) return false;
		whistleParameters.fftDataName = fftDataBlock.toString();
		whistleParameters.channelBitmap = whistleSourcePanel.getChannelList();
		whistleParameters.channelGroups = whistleSourcePanel.getChannelGroups();
		whistleParameters.groupingType = whistleSourcePanel.getGrouping();
		
		if (!peakPanel.getParams()) return false;
		if (!linkPanel.getParams()) return false;
		if (!whistlePanel.getParams()) return false;
		if (!eventPanel.getParams()) return false;
		
		if (whistleParameters.channelBitmap == 0) {
			JOptionPane.showMessageDialog(null, "Please select at least one channel", "Options error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	@Override
	public void cancelButtonPressed() {

		whistleParameters = null;
		
	}

	@Override
	public void restoreDefaultSettings() {

		setParams(new WhistleParameters());
		
	}

	void addToGrid(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
	
	class GroupedWhistleSourcePanel extends GroupedSourcePanel {

		public GroupedWhistleSourcePanel(Window ownerWindow, Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
			super(ownerWindow, sourceType, hasChannels, includeSubClasses, autoGrouping);
			// TODO Auto-generated constructor stub
		}

		public GroupedWhistleSourcePanel(Window ownerWindow, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
			super(ownerWindow, borderTitle, sourceType, hasChannels, includeSubClasses, autoGrouping);
			// TODO Auto-generated constructor stub
		}
		
	}
	
//	class WhistleSourcePanel extends JPanel implements ActionListener {
//
//		SourcePanel sourcePanel;
//		
//		JComboBox detectionChannel;
//		
//		JComboBox bearingChannel;
//		
//		JCheckBox measureBearings;
//		
//		WhistleSourcePanel() {
//			setBorder(new EmptyBorder(10,10,10,10));
////			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//			setLayout(new BorderLayout());
//
//			sourcePanel = new SourcePanel(FFTDataUnit.class, false, true);
//			sourcePanel.addSelectionListener(new SelectionListener());
//			
//			JPanel s = new JPanel();
//			s.setBorder(new TitledBorder("Data Source"));
//			s.setLayout(new BorderLayout());
//			s.add(BorderLayout.CENTER, sourcePanel.getPanel());
//			
//			JPanel b = new JPanel();
////			b.setLayout(new )
////			b.setLayout(new FlowLayout(FlowLayout.CENTER));
////			b.add(new JLabel("Detect on Channel "));
////			b.add(new )
//			
//			GridBagLayout layout = new GridBagLayout();
//			GridBagConstraints constraints = new GridBagConstraints();
//			b.setLayout(layout);
//			constraints.anchor = GridBagConstraints.EAST;
//			constraints.gridx = 0;
//			constraints.gridy = 0;
//			addToGrid(b, new JLabel("Detect on Channel "), constraints);
//			constraints.gridx++;
//			addToGrid(b, detectionChannel = new JComboBox(), constraints);
//			detectionChannel.addActionListener(this);
//			constraints.gridx = 0;
//			constraints.gridy++;
//			addToGrid(b, measureBearings = new JCheckBox("Measure Bearings on Channel "), constraints);
//			measureBearings.addActionListener(this);
//			constraints.gridx++;
//			addToGrid(b, bearingChannel = new JComboBox(), constraints);
//			
//			this.add(BorderLayout.NORTH,s);
//			this.add(BorderLayout.CENTER,b);
//		}
//		private void setParams(WhistleParameters whistleParameters) {
//			
//			ArrayList<PamDataBlock> fftBlocks = PamController.getInstance().getFFTDataBlocks();
//			sourcePanel.setSourceList();
//			sourcePanel.setSource(fftBlocks.get(whistleParameters.fftDataSource));
//			newSourceBlock();
//			measureBearings.setSelected(whistleParameters.measureBearings);
//			enableBearings();
//		}
//		private void newSourceBlock() {
//			detectionChannel.removeAllItems();
//			PamDataBlock dataBlock = sourcePanel.getSource();
//			if (dataBlock == null) return;
//			int chanList = dataBlock.getChannelMap();
//			for (int i = 0; i <= PamUtils.getHighestChannel(chanList); i++) {
//				if (((1<<i) & chanList) > 0) {
//					detectionChannel.addItem(i);
//				}
//			}
//			detectionChannel.setSelectedItem(whistleParameters.detectionChannel);
//			newDetectionChannel();
//		}
//		private int getDetectionChannel() {
//			if (detectionChannel == null) return -1;
//			if (detectionChannel.getSelectedIndex() < 0) return -1;
//			return (Integer) detectionChannel.getSelectedItem();
//		}
//		
//		private void newDetectionChannel() {
//			bearingChannel.removeAllItems();
//			int detectionChannel = getDetectionChannel();
//			PamDataBlock dataBlock = sourcePanel.getSource();
//			int chanList = 0;
//			if (dataBlock != null) {
//				chanList = dataBlock.getChannelMap();
//			}
//			for (int i = 0; i <= PamUtils.getHighestChannel(chanList); i++) {
//				if (i == detectionChannel) continue;
//				if (((1<<i) & chanList) > 0) {
//					bearingChannel.addItem(i);
//				}
//			}
//			bearingChannel.setSelectedItem(whistleParameters.bearingChannel);
//		}
//		private int getBearingChannel() {
//			if (bearingChannel == null || bearingChannel.getSelectedIndex() < 0) return -1;
//			return (Integer) bearingChannel.getSelectedItem();
//		}
//		
//		private void enableBearings() {
//			bearingChannel.setEnabled(measureBearings.isSelected());
//		}
//		
//		private boolean getParams() {
//			whistleParameters.fftDataSource = sourcePanel.getSourceIndex();
//			whistleParameters.detectionChannel = getDetectionChannel();
//			whistleParameters.bearingChannel = getBearingChannel();
//			whistleParameters.measureBearings = measureBearings.isSelected();
//			return true;
//		}
//		/* (non-Javadoc)
//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//		 */
//		public void actionPerformed(ActionEvent e) {
//			if (e.getSource() == detectionChannel) {
//				newDetectionChannel();
//			}
//			else if (e.getSource() == measureBearings) {
//				enableBearings();
//			}
//			
//		}
//		class SelectionListener implements ActionListener {
//
//			public void actionPerformed(ActionEvent e) {
//				newSourceBlock();
//			}
//		}
//	}
	class PeakPanel extends JPanel {
		
		JComboBox peakDetector;
		JTextField timeConstant0, timeConstant1, threshold, minPeakWidth, maxPeakWidth, maxOverThreshold;
		JTextField searchStartHz, searchEndHz;
		
		JButton searchStartButton, searchEndButton;
		
		PeakPanel() {
			setBorder(new EmptyBorder(10,10,10,10));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			
			JPanel p = new JPanel();
			p.setBorder(new TitledBorder("Peak Detection"));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 0;
			constraints.gridy = 0;
//			addToGrid(p, new JLabel("Peak Detector"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Search Start"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Search End"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Smoothing Constant (off)"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Smoothing Constant (on)"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Detection theshold"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Maximum bins over threshold  "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Minimum width"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Maximum width"), constraints);
			
			constraints.gridx = 0;
			constraints.gridwidth = 4;
			constraints.gridy = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			addToGrid(p, peakDetector = new JComboBox(), constraints);
			
			constraints.gridwidth = 1;
			constraints.gridx = 1;
			constraints.gridy = 1;
			addToGrid(p, searchStartHz = new JTextField(7), constraints);
			constraints.gridy++;
			addToGrid(p, searchEndHz = new JTextField(7), constraints);
			constraints.gridy++;
			addToGrid(p, timeConstant0 = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, timeConstant1 = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, threshold = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, maxOverThreshold = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, minPeakWidth = new JTextField(4), constraints);
			constraints.gridy++;
			addToGrid(p, maxPeakWidth = new JTextField(4), constraints);
			
			constraints.gridx = 2;
			constraints.gridy = 1;
			addToGrid(p, new JLabel(" Hz "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" Hz "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" s "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" s "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" dB "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" % "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" bins "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" bins "), constraints);
			constraints.gridx = 3;
			constraints.gridy = 1;
			
			addToGrid(p, searchStartButton = new JButton("Default"), constraints);
			constraints.gridy ++;
			addToGrid(p, searchEndButton = new JButton("Default"), constraints);
			searchStartButton.addActionListener(new DefButtonAction());
			searchEndButton.addActionListener(new DefButtonAction());
			
			
			this.add(p);
			
		}
		
		class DefButtonAction implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource() == searchStartButton) {
					searchStartHz.setText("0");
				}
				else if (arg0.getSource() == searchEndButton) {
					searchEndHz.setText(String.format("%.0f", sampleRate/2));
				}
				
			}
			
		}
		
		private void setParams(WhistleParameters whistleParameters) {

			peakDetector.removeAllItems();
			for (int i = 0; i < whistleControl.peakDetectorProviders.size(); i++) {
				peakDetector.addItem(whistleControl.peakDetectorProviders.get(i).getName());
			}
			peakDetector.setSelectedIndex(whistleParameters.peakDetectionMethod);
			//JTextField timeConstant0, timeConstant1, threshold, minBins, maxBins, maxOverThreshold;
			searchStartHz.setText(String.format("%.0f", whistleParameters.getSearchStartHz()));
			searchEndHz.setText(String.format("%.0f", whistleParameters.getSearchEndHz(sampleRate)));
			timeConstant0.setText(String.format("%.1f", whistleParameters.peakTimeConstant[0]));
			timeConstant1.setText(String.format("%.1f", whistleParameters.peakTimeConstant[1]));
			threshold.setText(String.format("%.1f", whistleParameters.detectionThreshold));
			minPeakWidth.setText(String.format("%d", whistleParameters.minPeakWidth));
			maxPeakWidth.setText(String.format("%d", whistleParameters.maxPeakWidth));
			maxOverThreshold.setText(String.format("%.0f", whistleParameters.maxPercentOverThreshold));
		}
		
		private boolean getParams() {
			
			double f;
			whistleParameters.peakDetectionMethod = peakDetector.getSelectedIndex();
			try {
				f = Double.valueOf(searchStartHz.getText());
				whistleParameters.setSearchStartHz(f);
				f = Double.valueOf(searchEndHz.getText());
				whistleParameters.setSearchEndHz(f);
				whistleParameters.peakTimeConstant[0] = Double.valueOf(
						timeConstant0.getText());
				whistleParameters.peakTimeConstant[1] = Double.valueOf(
						timeConstant1.getText());
				whistleParameters.detectionThreshold = Double.valueOf(
						threshold.getText());
				whistleParameters.minPeakWidth = Integer.valueOf(
						minPeakWidth.getText());
				whistleParameters.maxPeakWidth = Integer.valueOf(
						maxPeakWidth.getText());
				whistleParameters.maxPercentOverThreshold = Double.valueOf(
						maxOverThreshold.getText());
			}
			catch (Exception Ex) {
				return false;
			}
			
			return true;
		}
	}
	
	class LinkPanel extends JPanel {
		
		JTextField maxSweep, maxSweep2, maxAmplitude, maxGap;
		JTextField sweepWeight, sweepWeight2, amplitudeWeight;
		LinkPanel() {
			
			setBorder(new EmptyBorder(10,10,10,10));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel p = new JPanel();
			p.setBorder(new TitledBorder("Link Creation"));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 3;
			constraints.gridy = 0;
			addToGrid(p, new JLabel("Weight"), constraints);
			constraints.gridx = 0;
			constraints.gridy = 1;
			addToGrid(p, new JLabel("Max Sweep "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Max Sweep differential "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Max Amplitude Change "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel("Max gap "), constraints);
			constraints.gridy++;
			constraints.gridx = 1;
			constraints.gridy = 1;
			addToGrid(p, maxSweep = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, maxSweep2 = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, maxAmplitude = new JTextField(5), constraints);
			constraints.gridy+=2;
			addToGrid(p, maxGap = new JTextField(2), constraints);
			constraints.gridx = 2;
			constraints.gridy = 1;
			addToGrid(p, new JLabel(" kHz/s"), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" kHz/s^2  "), constraints);
			constraints.gridy++;
			addToGrid(p, new JLabel(" dB/s"), constraints);
			constraints.gridy+=2;
			constraints.gridwidth = 2;
			addToGrid(p, new JLabel(" time partitions "), constraints);
			constraints.gridx = 3;
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			addToGrid(p, sweepWeight = new JTextField(3), constraints);
			constraints.gridy++;
			addToGrid(p, sweepWeight2 = new JTextField(3), constraints);
			constraints.gridy++;
			addToGrid(p, amplitudeWeight = new JTextField(3), constraints);
			
			this.add(p);
		}
		private void setParams(WhistleParameters whistleParameters) {
			maxSweep.setText(String.format("%.2f", whistleParameters.maxDF/1000.));
			maxSweep2.setText(String.format("%.2f", whistleParameters.maxD2F/1000.));
			maxAmplitude.setText(String.format("%.1f", whistleParameters.maxDA));
			sweepWeight.setText(String.format("%.1f", whistleParameters.weightDF));
			sweepWeight2.setText(String.format("%.1f", whistleParameters.weightD2F));
			amplitudeWeight.setText(String.format("%.1f", whistleParameters.weightDA));
			maxGap.setText(String.format("%d", whistleParameters.maxGap));
		}
		
		private boolean getParams() {
			try {
				whistleParameters.maxDF = Double.valueOf(
						maxSweep.getText()) * 1000.;
				whistleParameters.maxD2F = Double.valueOf(
						maxSweep2.getText()) * 1000.;
				whistleParameters.maxDA = Double.valueOf(
						maxAmplitude.getText());
				whistleParameters.weightDF = Double.valueOf(
						sweepWeight.getText());
				whistleParameters.weightD2F = Double.valueOf(
						sweepWeight2.getText());
				whistleParameters.weightDA = Double.valueOf(
						amplitudeWeight.getText());
				whistleParameters.maxGap = Integer.valueOf(
						maxGap.getText());
			}
			catch (Exception Ex) {
				return false;
			}
			return true;
		}
		
	}
	
	class WhistlePanel extends JPanel {
		
		JTextField minLength, minOccupancy;
		
		WhistlePanel() {
			
			
			setBorder(new EmptyBorder(10,10,10,10));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel p = new JPanel();
			p.setBorder(new TitledBorder("Whistle Selection"));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 0;
			constraints.gridy = 0;
			addToGrid(p, new JLabel("Minimum length "), constraints);
			constraints.gridy ++;
			addToGrid(p, new JLabel("Minimum occupancy "), constraints);
			constraints.gridy ++;
			constraints.gridx = 1;
			constraints.gridy = 0;
			addToGrid(p, minLength = new JTextField(3), constraints);
			constraints.gridy++;
			addToGrid(p, minOccupancy = new JTextField(3), constraints);
			constraints.gridx = 2;
			constraints.gridy = 0;
			addToGrid(p, new JLabel(" bins "), constraints);
			constraints.gridy ++;
			addToGrid(p, new JLabel(" %"), constraints);
			
			this.add(p);
		}
		private void setParams(WhistleParameters whistleParameters) {
			minLength.setText(String.format("%d", whistleParameters.minLength));
			minOccupancy.setText(String.format("%.0f", whistleParameters.minOccupancy));
		}
		
		private boolean getParams() {
			try {
				whistleParameters.minLength = Integer.valueOf(
						minLength.getText());
				whistleParameters.minOccupancy = Double.valueOf(
						minOccupancy.getText());
			}
			catch (Exception Ex) {
				return false;
			}
			return true;
		}
		
	}
	class EventPanel extends JPanel {
		
		JTextField integrationTime, minCount, maxGap;
		
		EventPanel() {
			
			
			setBorder(new EmptyBorder(10,10,10,10));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel p = new JPanel();
			p.setBorder(new TitledBorder("Event Detection"));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 0;
			constraints.gridy = 0;
			addToGrid(p, new JLabel("Integration Time "), constraints);
			constraints.gridy ++;
			addToGrid(p, new JLabel("Minimum Whistle Count "), constraints);
			constraints.gridy ++;
			addToGrid(p, new JLabel("Maximum gap "), constraints);
			constraints.gridx = 1;
			constraints.gridy = 0;
			addToGrid(p, integrationTime = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, minCount = new JTextField(5), constraints);
			constraints.gridy++;
			addToGrid(p, maxGap = new JTextField(5), constraints);
			constraints.gridx = 2;
			constraints.gridy = 0;
			addToGrid(p, new JLabel(" s "), constraints);
			constraints.gridy +=2;
			addToGrid(p, new JLabel(" s "), constraints);
			
			this.add(p);
		}
		private void setParams(WhistleParameters whistleParameters) {
			integrationTime.setText(String.format("%.0f", whistleParameters.eventIntegrationTime));
			minCount.setText(String.format("%d", whistleParameters.eventMinWhistleCount));
			maxGap.setText(String.format("%.0f", whistleParameters.eventMaxGapTime));
		}
		
		private boolean getParams() {
			try {
				whistleParameters.eventIntegrationTime = Double.valueOf(
						integrationTime.getText());
				whistleParameters.eventMinWhistleCount = Integer.valueOf(
						minCount.getText());
				whistleParameters.eventMaxGapTime = Double.valueOf(
						maxGap.getText());
			}
			catch (Exception Ex) {
				return false;
			}
			return true;
		}
		
	}
}
