/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Spectrogram;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import PamController.PamController;
import PamUtils.PamUtils;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamNorthPanel;
import PamView.paneloverlay.overlaymark.MarkObserversPanel;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import noiseMonitor.ResolutionPanel;

public class SpectrogramParamsDialog extends PamDialog implements ActionListener {

	static private SpectrogramParamsDialog singleInstance;

//	private JComboBox sourceList;
	private SourcePanel sourcePanel;

	private JLabel panelChannelLabel[] = new JLabel[PamConstants.MAX_CHANNELS];
	private JComboBox panelChannelList[] = new JComboBox[PamConstants.MAX_CHANNELS];

//	private JLabel channel, fftLen, fftLenData, fftHop, fftHopData,
//			sampleRate, sampleRateData;
//	private JLabel source, sourceData;
	
	private ResolutionPanel resolutionPanel = new ResolutionPanel();
		
	private JTextField minFData, maxFData, nPanels;

	private JButton minDefault, maxDefault;

	private JTextField minAmplitude, maxAmplitude;
	
	private JComboBox colourList;

	private JTextField pixsPerSlice, secsPerScreen;
	
	private JRadioButton wrapDisplay, scrollDisplay;

	private JRadioButton pixs, secs;
	
	private JCheckBox showViewerSpectrogram;
	
	private int currentNumPanels;

	private JButton onePerChannelButton;

	private DialogSourcePanel dialogSourcePanel;

	private FFTDataBlock fftBlock;

	private float defaultMinFreq, defaultMaxFreq;
	
	private PluginPanel pluginPanel;
	
	private ObserverPanel observerPanel;

	private SpectrogramParameters spectrogramParameters;

	private boolean initialisingSourceList = false;

//	private OverlayMarker overlayMarker;
	
	private SpectrogramPanel[] panels;

	public MarkObserversPanel markObserversPanel;

//	private SpectrogramParamsDialog(Window parentFrame, OverlayMarker overlayMarker, SpectrogramParameters spectrogramParameters) {
	private SpectrogramParamsDialog(Window parentFrame, SpectrogramPanel[] panels, SpectrogramParameters spectrogramParameters) {
		
		super(parentFrame, "Spectrogram Parameters",false);
//		this.overlayMarker = overlayMarker;
		this.panels = panels;
		this.spectrogramParameters = spectrogramParameters;
		JPanel p = new JPanel();
//		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// p.setLayout(new BorderLayout());
		// p.add(BorderLayout.CENTER, new SourcePanel());
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JPanel[] ps = new JPanel[5];
//		ps[4] = waveformPanel = new WaveformPanel();
		ps[3] = new TimePanel();
		ps[2] = new AmplitudePanel();
		ps[1] = new FrequencyPanel();
		ps[4] = new ScrollPanel();
		dialogSourcePanel = new DialogSourcePanel();
		
		JPanel viewerPanel = new JPanel();
		viewerPanel.setBorder(new TitledBorder("Viewer Options"));
		viewerPanel.setLayout(new GridBagLayout());
		PamGridBagContraints c = new PamGridBagContraints();
		viewerPanel.add(showViewerSpectrogram = new JCheckBox("Show raw spectrogram in viewer"), c);
		showViewerSpectrogram.setToolTipText("To display spectrogram data in viewer mode you must tell the acquisition or\n"
				+ "the source of raw data supplying the FFT module where to find raw audio data");
		showViewerSpectrogram.setEnabled(PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		
		
		JPanel s2Panel =new JPanel(new BorderLayout());
		s2Panel.add(BorderLayout.CENTER, dialogSourcePanel);
		s2Panel.add(BorderLayout.SOUTH, viewerPanel);

		JTabbedPane tabbedPane = new JTabbedPane();
		p.add(tabbedPane);
		JPanel scalePanel;
		tabbedPane.addTab("Data Source", s2Panel);
		tabbedPane.addTab("Scales", scalePanel = new JPanel());
		scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS));
		
		//sourcePanel.add(ps[0]);
		scalePanel.add(ps[1]);
		scalePanel.add(ps[2]);
		scalePanel.add(ps[3]);
		scalePanel.add(ps[4]);
		
		fillSourcePanelData();

		setDialogComponent(p);
		
		pluginPanel = new PluginPanel();
		tabbedPane.addTab("Plug ins", pluginPanel);

		observerPanel = new ObserverPanel();
		tabbedPane.addTab("Mark Observers", observerPanel);
		

		sourcePanel.addSelectionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!initialisingSourceList) {
					fillSourcePanelData();
				}
			}
		});
//		sourceList.addActionListener(this);
		minDefault.addActionListener(this);
		maxDefault.addActionListener(this);
		pixs.addActionListener(this);
		secs.addActionListener(this);
		nPanels.addActionListener(this);
		
		sortChannelLists();
		
		setHelpPoint("displays.spectrogramDisplayHelp.docs.UserDisplay_Spectrogram_Configuring");
		
		setSendGeneralSettingsNotification(false);
		
		setResizable(true);
	}

//	public static SpectrogramParameters showDialog(Window parentFrame, OverlayMarker overlayMarker, SpectrogramParameters spectrogramParameters) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame || overlayMarker != singleInstance.overlayMarker) {
//			singleInstance = new SpectrogramParamsDialog(parentFrame, overlayMarker, spectrogramParameters);
//		}
	public static SpectrogramParameters showDialog(Window parentFrame, SpectrogramPanel[] panels, SpectrogramParameters spectrogramParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.panels == null || panels==null || panels.length != singleInstance.panels.length) {
			singleInstance = new SpectrogramParamsDialog(parentFrame, panels, spectrogramParameters);
		}
		singleInstance.spectrogramParameters = spectrogramParameters.clone();
		singleInstance.pluginPanel.buildList();
		if (singleInstance.markObserversPanel != null) {
			singleInstance.observerPanel.updateMarkers(panels);
			singleInstance.markObserversPanel.setParams();
		}
		singleInstance.initialiseSourcePanelData();
		singleInstance.fillSourcePanelData();
		singleInstance.setVisible(true);
		return singleInstance.spectrogramParameters;
	}

	@Override
	public void cancelButtonPressed() {
		spectrogramParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == pixs) {
			EnableButtons();

		} else if (e.getSource() == secs) {
			EnableButtons();
		} else if (e.getSource() == minDefault) {
			setDefaultMinFreq();
		} else if (e.getSource() == maxDefault) {
			setDefaultMaxFreq();
		} 
//		else if (e.getSource() == sourceList && !initialisingSourceList) {
//			fillSourcePanelData();
//		}
		else if (e.getSource() == nPanels) {
			sortChannelLists();
		}
	}

	private void EnableButtons() {
		pixsPerSlice.setEnabled(pixs.isSelected());
		secsPerScreen.setEnabled(secs.isSelected());
//		waveformPanel.enableAutoWaveforms();
	}

	class DialogSourcePanel extends JPanel {
		DialogSourcePanel() {
			super();

//			sourceList = new JComboBox();
			sourcePanel = new SourcePanel(SpectrogramParamsDialog.this, "FFT Source", FFTDataUnit.class, false, true);

//			ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
//					.getFFTDataBlocks();
//			PamProcess fftDataSource;
//			sourceList.removeAllItems();
//			for (int i = 0; i < fftBlocks.size(); i++) {
//				fftDataSource = fftBlocks.get(i).getParentProcess();
//				sourceList.addItem(fftDataSource.getProcessName() + "-"
//						+ fftBlocks.get(i).getDataName());
//			}
//			if (spectrogramParameters.fftBlockIndex < sourceList.getItemCount()) {
//				sourceList.setSelectedIndex(spectrogramParameters.fftBlockIndex);
//			}
			
			nPanels = new JTextField(3);
			nPanels.setToolTipText("Set the number of spectrogram panels to display");

//			channel = new JLabel("Channel");
//			source = new JLabel("Data source ", JLabel.RIGHT);
//			sourceData = new JLabel("");
//			fftLen = new JLabel("FFT Length ", JLabel.RIGHT);
//			fftLenData = new JLabel("11111 pt ( s)");
//			fftHop = new JLabel("FFT Hop ", JLabel.RIGHT);
//			fftHopData = new JLabel("??? pt ( s)");
//			sampleRate = new JLabel("Sample Rate ", JLabel.RIGHT);
//			sampleRateData = new JLabel(" ????? Hz");

			setBorder(BorderFactory.createTitledBorder("Source Data"));

			JPanel chanPanel = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			chanPanel.setLayout(layout);
			GridBagConstraints constraints = new PamGridBagContraints();
//			constraints.anchor = GridBagConstraints.WEST;

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 4;
//			addComponent(bot, source, constraints);
//			addComponent(bot, sourceData, constraints);
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			addComponent(chanPanel, new JLabel(" Number of Panels ", SwingConstants.RIGHT), constraints);
			constraints.gridx = 1;
			constraints.gridwidth = 1;
			addComponent(chanPanel, nPanels, constraints);
			constraints.gridx = 2;
			constraints.gridwidth = 1;
			addComponent(chanPanel, new JLabel("(hit enter) "), constraints);
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){

				constraints.gridx = 0;
				constraints.gridy ++;
				constraints.gridwidth = 1;
				addComponent(chanPanel, panelChannelLabel[i] = new JLabel(" Panel " + i + " channel ", SwingConstants.RIGHT), constraints);
				constraints.gridx += constraints.gridwidth;
				constraints.gridwidth = 1;
				addComponent(chanPanel, panelChannelList[i] = new JComboBox(), constraints);
				panelChannelList[i].setToolTipText(String.format("Set which channel to show in panel %d", i));
				
			}
			onePerChannelButton = new JButton("One panel per channel");
			onePerChannelButton.setToolTipText("Create one spectrogram panel per channel of data");
			constraints.gridx = 0;
			constraints.gridwidth = 2;
			constraints.gridy++;
			addComponent(chanPanel, onePerChannelButton, constraints);
			onePerChannelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOneChannelPerPanel();
				}
			});
//			addComponent(bot, channel, constraints);
//			addComponent(bot, channelData, constraints);
//			constraints.gridx = 0;
//			constraints.gridy ++;
//			constraints.gridwidth = 1;
//			addComponent(bot, fftLen, constraints);
//			constraints.gridx += constraints.gridwidth;
//			constraints.gridwidth = 2;
//			addComponent(bot, fftLenData, constraints);
//			constraints.gridx = 0;
//			constraints.gridy ++;
//			constraints.gridwidth = 1;
//			addComponent(bot, fftHop, constraints);
//			constraints.gridx = 1;
//			constraints.gridwidth = 2;
//			addComponent(bot, fftHopData, constraints);
//			constraints.gridx = 0;
//			constraints.gridy ++;
//			constraints.gridwidth = 1;
//			addComponent(bot, sampleRate, constraints);
//			constraints.gridx = 1;
//			constraints.gridwidth = 2;
//			addComponent(bot, sampleRateData, constraints);

			setLayout(new BorderLayout());
			add(BorderLayout.NORTH, sourcePanel.getPanel());
//			sourceList.setBorder(new TitledBorder("FFT Data Source"));
			JPanel southPanel = new JPanel(new BorderLayout());
			JComponent p;
			southPanel.add(BorderLayout.CENTER, p = new PamNorthPanel(chanPanel));
			p.setBorder(new TitledBorder("Channels"));
			southPanel.add(BorderLayout.WEST, p = new PamNorthPanel(resolutionPanel.getPanel()));
			p.setBorder(new TitledBorder("Resolution"));
			add(BorderLayout.CENTER, southPanel);

			pack();

		}
		
		/**
		 * Set up the panels so that there is one per channel. 
		 */
		private void setOneChannelPerPanel() {
			/**
			 * First get the source.
			 */
			if (fftBlock == null) {
				return;
			}
			int nChan = PamUtils.getNumChannels(fftBlock.getChannelMap());
			if (nChan <= 0) {
				return;
			}
			spectrogramParameters.nPanels = nChan;
			for (int i = 0; i < nChan; i++) {
				spectrogramParameters.channelList[i] = i;
			}
			fillSourcePanelData();
		}
//		void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
//			((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
//			panel.add(p);
//		}
		
		/**
		 * Any number of channels are allowed except zero. 
		 * Users may want to display the same spectrogram 
		 * multiple times with different overlays. 
		 */
		int getNumPanels() {
			int nP;
			try {
				nP = Integer.valueOf(nPanels.getText());
			}
			catch (NumberFormatException Ex) {
				nP = 0;
			}
			if (nP <= 0) {
				nPanels.setText("1");
				nP = 1;
			}
			if (nP > 32) {
				nP = 32;
				showWarning("The maximum number of spectrogram panels is 32");
				nPanels.setText("32");
			}
			return nP;
		}

//		int getNumPanels() {  //Xiao Yan Deng
//			try {
//				int np = Integer.valueOf(nPanels.getText());
//				if(np>0){
//					if (np<numChannel){
//						return Math.min(np, panelChannelList.length);
//					}
//					else{
//						return Math.min(numChannel, panelChannelList.length);
//					}			
//				}
//				else{
//					return Math.min(numChannel, panelChannelList.length);	
//				}
//			}
//			catch (Exception Ex) {
//				return 0;
//			}
//		}
	}

	private void initialiseSourcePanelData() {

		showViewerSpectrogram.setSelected(!spectrogramParameters.hideViewerSpectrogram);
		
		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
		.getFFTDataBlocks();

		PamProcess pamDataSource;
		
		// set the flag so that if the sourceList actionlistener is triggered prematurely, it knows not to
		// call fillSourcePanelData and set the displayed parameters yet
		/**
		 * Check that the sourcename is set, very old configurations may be 
		 * setting based off the source index.
		 */
		if (spectrogramParameters.sourceName == null) {
			PamDataBlock aBlock = PamController.getInstance().getDataBlock(FFTDataUnit.class, spectrogramParameters.getFftBlockIndex());
			if (aBlock != null) {
				spectrogramParameters.sourceName = aBlock.getLongDataName();
			}
		}
		
		initialisingSourceList = true;
		sourcePanel.setSourceList();
		sourcePanel.setSource(spectrogramParameters.sourceName);
//		sourceList.removeAllItems();
//		for (int i = 0; i < fftBlocks.size(); i++) {
//			pamDataSource = fftBlocks.get(i).getParentProcess();
//			sourceList.addItem(pamDataSource.getProcessName() + "-"
//					+ fftBlocks.get(i).getDataName());
//		}
//		if (spectrogramParameters.fftBlockIndex < sourceList.getItemCount()) {
//			sourceList.setSelectedIndex(spectrogramParameters.fftBlockIndex);
//		}
		initialisingSourceList = false;
	}

	private void fillSourcePanelData() {


//		int iset = sourceList.getSelectedIndex();
//		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance().getFFTDataBlocks();
//
//		PamProcess pamDataSource;
//
////		FFTDataBlock fftDataSource;
//		if (iset >= 0 && iset < fftBlocks.size()) {
//			fftBlock = (FFTDataBlock) fftBlocks.get(iset);
////			fftDataSource = (FFTDataSource) fftBlock.getParentProcess();
//			pamDataSource = fftBlock.getParentProcess();
//			defaultMinFreq = 0;
//			defaultMaxFreq = fftBlock.getSampleRate() / 2;
//		} else {
//			return;
//		}
//		sourcePanel.setSource(spectrogramParameters.sourceName);
		fftBlock = (FFTDataBlock) sourcePanel.getSource();
		if (fftBlock == null) {
			return;
		}
		defaultMinFreq = 0;
		defaultMaxFreq = fftBlock.getSampleRate() / 2;

		nPanels.setText(String.format("%d", spectrogramParameters.nPanels)); //Xiao Yan Deng commented
//		System.out.println("SpectrogramParameterDialog.java->fillSourcePanelData()numChannel:"+numChannel);
//		nPanels.setText(String.format("%d", numChannel)); //Xiao Yan Deng
		sortChannelLists();
		
//		sourceData.setText(pamDataSource.getProcessName() + " bla " + iset);
//		fftLenData.setText(fftBlock.getFftLength() + " samples");
//		fftHopData.setText(fftBlock.getFftHop() + " samples ");
//		sampleRateData.setText(fftBlock.getSampleRate() + " Hz");

		if (spectrogramParameters.frequencyLimits[0] == 0
				|| spectrogramParameters.frequencyLimits[0] > defaultMaxFreq) {
			spectrogramParameters.frequencyLimits[0] = defaultMinFreq;
		}
		if (spectrogramParameters.frequencyLimits[1] == 0
				|| spectrogramParameters.frequencyLimits[1] > defaultMaxFreq) {
			spectrogramParameters.frequencyLimits[1] = defaultMaxFreq;
		}
		minFData.setText(String.format("%.0f",
				spectrogramParameters.frequencyLimits[0]));
		maxFData.setText(String.format("%.0f",
				spectrogramParameters.frequencyLimits[1]));

		minAmplitude.setText(String.format("%.1f",
				spectrogramParameters.amplitudeLimits[0]));
		maxAmplitude.setText(String.format("%.1f",
				spectrogramParameters.amplitudeLimits[1]));
		colourList.setSelectedIndex(spectrogramParameters.getColourMap().ordinal());

		pixs.setSelected(!spectrogramParameters.timeScaleFixed);
		secs.setSelected(spectrogramParameters.timeScaleFixed);

		pixsPerSlice.setText(String.format("%d",
				spectrogramParameters.pixelsPerSlics));
		secsPerScreen.setText(String.format("%.1f",
				spectrogramParameters.displayLength));
		wrapDisplay.setSelected(spectrogramParameters.wrapDisplay);
		scrollDisplay.setSelected(!spectrogramParameters.wrapDisplay);

		resolutionPanel.setParams(fftBlock);
		
		EnableButtons();

	}
	private void sortChannelLists() {
		// first of all, only show the ones in range of nPanels
		int nP = dialogSourcePanel.getNumPanels();
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			panelChannelLabel[i].setVisible(i < nP);
			panelChannelList[i].setVisible(i < nP);
		}

		if (fftBlock != null) {
//			int channelMap = fftBlock.getChannelMap();
			int channelMap = fftBlock.getSequenceMap();
	
			for (int iL = 0; iL < nP; iL++) {
				panelChannelList[iL].removeAllItems();
				if (fftBlock == null) continue;
				for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
					if ((channelMap & (1 << i)) != 0) {
						panelChannelList[iL].addItem(i);
//						if (spectrogramParameters.channelList[iL] == i) {
//						panelChannelList[iL].setSelectedIndex(panelChannelList[iL].getItemCount()-1);
//						}
					}
				}
				panelChannelList[iL].setSelectedItem(spectrogramParameters.channelList[iL]); //Xiao Yan Deng commented
	
//				panelChannelList[iL].setSelectedIndex(iL); //Xiao Yan Deng
//				System.out.println("SpectrogramParameter.java->sortChannelLists:spectrogramParameters.channelList:"+iL+":"+spectrogramParameters.channelList[iL]);
			}
			
			currentNumPanels = nP;
		}
		
		pack();
	}

	private void setDefaultMinFreq() {
		minFData.setText(String.format("%.0f", this.defaultMinFreq));
	}

	private void setDefaultMaxFreq() {
		maxFData.setText(String.format("%.0f", this.defaultMaxFreq));
	}

	class FrequencyPanel extends JPanel {
		FrequencyPanel() {
			super();

			minFData = new JTextField(8);
			maxFData = new JTextField(8);

			minDefault = new JButton("Default");
			maxDefault = new JButton("Default");

			this.setBorder(BorderFactory.createTitledBorder("Frequency Range"));
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();

			addComponent(this, new JLabel("Min "), c);
			c.gridy++;
			addComponent(this, new JLabel("Max "), c);

			c.gridx++;
			c.gridy = 0;
			addComponent(this, minFData, c);
			c.gridy++;
			addComponent(this, maxFData, c);

			c.gridx++;
			c.gridy = 0;
			addComponent(this, new JLabel(" Hz "), c);
			c.gridy++;
			addComponent(this, new JLabel(" Hz "), c);

			c.gridx++;
			c.gridy = 0;
			addComponent(this, minDefault, c);
			c.gridy++;
			addComponent(this, maxDefault, c);
			
//
//			p = new JPanel();
//			p.setLayout(new GridLayout(2, 1));
//			p.add(minDefault);
//			p.add(maxDefault);
//			add(p);

		}
	}

	class AmplitudePanel extends JPanel {

		AmplitudePanel() {
			super();

			minAmplitude = new JTextField(6);
			maxAmplitude = new JTextField(6);

			this.setBorder(BorderFactory.createTitledBorder("Amplitude Range"));
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();

			addComponent(this, new JLabel("Min "), c);
			c.gridy++;
			addComponent(this, new JLabel("Max "), c);
			
			c.gridx++;
			c.gridy = 0;
			addComponent(this, minAmplitude, c);
			c.gridy++;
			addComponent(this, maxAmplitude, c);

			c.gridx++;
			c.gridy = 0;
//			String dBRef = GlobalMedium.getdBRefString(PamController.getInstance().getGlobalMediumManager().getCurrentMedium());
			String dBRef = "dB re \u00B5Pa/\u221AHz";
			addComponent(this, new JLabel(" "+dBRef), c);
			c.gridy++;
			addComponent(this, new JLabel(" "+dBRef), c);
			
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new JLabel("Colour model "), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, colourList = new JComboBox(), c);
			
			ColourArrayType[] types = ColourArray.ColourArrayType.values();
			for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
				colourList.addItem(ColourArray.getName(types[i]));
			}
			
		}
	}

	class TimePanel extends JPanel {

		TimePanel() {
			super();

			pixsPerSlice = new JTextField(4);
			secsPerScreen = new JTextField(4);
			ButtonGroup g = new ButtonGroup();
			pixs = new JRadioButton("Pixels per FFT");
			g.add(pixs);
			secs = new JRadioButton("Window length (s)");
			g.add(secs);

			this.setBorder(BorderFactory.createTitledBorder("Time Range"));
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();

			addComponent(this, pixs, c);
			c.gridy++;
			addComponent(this, secs, c);
			
			c.gridx++;
			c.gridy = 0;
			addComponent(this, pixsPerSlice, c);
			c.gridy++;
			addComponent(this, secsPerScreen, c);
			
//			c.gridx = 0;
//			c.gridy++;
//			addComponent(this, wrapDisplay = new JRadioButton("Wrap Display"),  c);
//			c.gridx++;
//			c.gridwidth = 2;
//			addComponent(this, scrollDisplay = new JRadioButton("Scroll Display"),  c);
//			ButtonGroup bg = new ButtonGroup();
//			bg.add(wrapDisplay);
//			bg.add(scrollDisplay);
		}
	}
	class ScrollPanel extends JPanel {

		ScrollPanel() {
			super();

			this.setBorder(BorderFactory.createTitledBorder("Scrolling"));
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();

			c.gridx = 0;
			c.gridy++;
			addComponent(this, wrapDisplay = new JRadioButton("Wrap Display"),  c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, scrollDisplay = new JRadioButton("Scroll Display"),  c);
			ButtonGroup bg = new ButtonGroup();
			bg.add(wrapDisplay);
			bg.add(scrollDisplay);
		}
	}
	class PluginPanel extends JPanel {
		
		JCheckBox[] providerList;
		
		JPanel northPanel;
		
		PluginPanel() {
			super();
			northPanel = new JPanel();
			setLayout(new BorderLayout());
			add(BorderLayout.NORTH, northPanel);
			buildList();
		}
		
		void buildList() {
			DisplayPanelProvider dp;
			northPanel.removeAll();
			int n = DisplayProviderList.getDisplayPanelProviders().size();
			if (n == 0) return;
			providerList = new JCheckBox[n];
			northPanel.setLayout(new GridLayout(n+1, 1));
			northPanel.setBorder(new EmptyBorder(10, 10, 0, 0));
			northPanel.add(new JLabel("Select additional display panels ..."));
			for (int i = 0; i < n; i++) {
				dp = DisplayProviderList.getDisplayPanelProviders().get(i);
				providerList[i] = new JCheckBox(dp.getDisplayPanelName());
				northPanel.add(providerList[i]);
			}
			if (spectrogramParameters.showPluginDisplay != null) {
				for (int i = 0; i < Math.min(n, spectrogramParameters.showPluginDisplay.length); i++) {
					providerList[i].setSelected(spectrogramParameters.showPluginDisplay[i]);
				}
			}

		}
		boolean[] getList() {
			
			int n = DisplayProviderList.getDisplayPanelProviders().size();
			if (n == 0) return null;
			
			boolean[] selection = new boolean[n];
			for (int i = 0; i < n; i++) {
				selection[i] = providerList[i].isSelected();
			}
			
			return selection;
		}
	}
	class ObserverPanel extends JPanel{
		
			
		ObserverPanel() {
			super();
			setLayout(new BorderLayout());
//			if (overlayMarker != null) {
//				markObserversPanel = new MarkObserversPanel(overlayMarker);
//				add(BorderLayout.NORTH, markObserversPanel.getDialogComponent());
//			}
			if (panels != null) {
				markObserversPanel = new MarkObserversPanel(setUpMarkerList(panels));
				add(BorderLayout.NORTH, markObserversPanel.getDialogComponent());
			}
		}
		
		/**
		 * @param panels
		 */
		public void updateMarkers(SpectrogramPanel[] panels) {
			markObserversPanel.setOverlayMarkers(setUpMarkerList(panels));
		}

		private OverlayMarker[] setUpMarkerList(SpectrogramPanel[] panels) {
			OverlayMarker[] markers = new OverlayMarker[panels.length];
			for (int i=0; i<panels.length; i++) {
				markers[i] = panels[i].getMarker();
			}
			return markers;
		}
		
//		public void updateObserversList(SpectrogramPanel[] panels) {
//			markObserversPanel.updateList(setUpMarkerList(panels));
//		}
	}
	
	
	@Override
	public boolean getParams() {

		spectrogramParameters.hideViewerSpectrogram = !showViewerSpectrogram.isSelected();
		
//		int iset = sourceList.getSelectedIndex();
//		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
//				.getFFTDataBlocks();
//		FFTDataBlock fftBlock;
////		FFTDataSource fftDataSource;
//		int ch;
//		if (iset >= 0 && iset < fftBlocks.size()) {
//			fftBlock = (FFTDataBlock) fftBlocks.get(iset);
////			fftDataSource = (FFTDataSource) fftBlock.getParentProcess();
//			spectrogramParameters.fftBlockIndex = iset;
//		} else {
//			return false;
//		}
		try {
			fftBlock = (FFTDataBlock) sourcePanel.getSource();
		}
		catch (ClassCastException e) {
			fftBlock = null;
		}
		if (fftBlock == null) {
			return showWarning("Invalid source data block " + sourcePanel.getSource());
		}

		int ch;
		try {
			spectrogramParameters.nPanels = dialogSourcePanel.getNumPanels();
			for (int i = 0; i < Math.min(spectrogramParameters.nPanels, currentNumPanels); i++) {
				ch = (Integer) panelChannelList[i].getSelectedItem();
//				ch = Integer.valueOf(str);
				spectrogramParameters.channelList[i] = ch;
			}

			spectrogramParameters.frequencyLimits[0] = Double.valueOf(
					minFData.getText());
			spectrogramParameters.frequencyLimits[1] = Double.valueOf(
					maxFData.getText());

			spectrogramParameters.amplitudeLimits[0] = Double.valueOf(
					minAmplitude.getText());
			spectrogramParameters.amplitudeLimits[1] = Double.valueOf(
					maxAmplitude.getText());
			spectrogramParameters.setColourMap(ColourArrayType.values()[colourList.getSelectedIndex()]);

			spectrogramParameters.timeScaleFixed = secs.isSelected();
			spectrogramParameters.displayLength = Double.valueOf(
					secsPerScreen.getText());
			spectrogramParameters.pixelsPerSlics = Integer.valueOf(pixsPerSlice
					.getText());
			
//			spectrogramParameters.showWaveform = showWaveforms.isSelected();
//			spectrogramParameters.autoScaleWaveform = autoScaleWaveforms.isSelected();

		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}

//		spectrogramParameters.fftBlockIndex = iset;
		spectrogramParameters.sourceName = fftBlock.getLongDataName();
		spectrogramParameters.windowName = spectrogramParameters.sourceName;
		spectrogramParameters.showPluginDisplay = pluginPanel.getList();
//		spectrogramParameters.useSpectrogramMarkObserver = observerPanel.getList();
		spectrogramParameters.wrapDisplay = wrapDisplay.isSelected();

		if (markObserversPanel != null) {
			markObserversPanel.getParams();
		}

		return true;
	}
	
}
