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
package fftManager;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import noiseMonitor.ResolutionPanel;

import spectrogramNoiseReduction.SpectrogramNoiseDialogPanel;
import spectrogramNoiseReduction.SpectrogramNoiseProcess;


import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import Spectrogram.WindowFunction;

public class FFTParametersDialog extends PamDialog implements FFTLengthModeled {

	private FFTParameters fftParameters;

	private static FFTParametersDialog singleInstance;

//	JComboBox sourceList;
	private SourcePanel sourcePanel;
	
	private JTextField fftLengthData, fftHopData;
	
	private JSpinner fftLengthSpinner;
	
	private FFTLengthModel fftLengthModel;
	
	private JComboBox windowFunction;
	
	private JButton defaultOverlap;
	
	private JTabbedPane tabbedPane;
	
	private JCheckBox clickRemoval;
	
	private JTextField clickThreshold, clickPower;
	
	private SpectrogramNoiseDialogPanel spectrogramNoiseDialogPanel;
	
	private ResolutionPanel resolutionPanel = new ResolutionPanel();
	
	private boolean paramsChanged;
	
	private final String clickRemoveMessage = 
		"<html>Click removal measures the standard deviation of<p>" +
		"the time series data and then multiplies the signal<p>" +
		"by a factor which increases rapidly for large signal<p>" +
		"components. This has the effect of reducing the<p>magnitude " +
		"of short duration transient signals such as<p>echolocation clicks</html>";
	
//	JCheckBox[] selectChannel;

	public static FFTParameters showDialog(Frame parentFrame, 
			FFTParameters oldfftParameters, SpectrogramNoiseProcess spectrogramNoiseProcess) {
		
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new FFTParametersDialog(parentFrame, spectrogramNoiseProcess);
		}

		singleInstance.fftParameters = oldfftParameters.clone();

		singleInstance.setParameters();
		singleInstance.paramsChanged = false;
		singleInstance.setVisible(true);
		
		return singleInstance.fftParameters;
	}

	private FFTParametersDialog(Frame parentFrame, SpectrogramNoiseProcess spectrogramNoiseProcess) {
		
		super(parentFrame, "FFT Parameters", true);
		
		tabbedPane = new JTabbedPane();
		
		JPanel specPanel = new JPanel(new BorderLayout());
		JPanel waveNoisePanel = new JPanel(new BorderLayout());
		JPanel specNoisePanel = new JPanel(new BorderLayout());
		tabbedPane.add("FFT", specPanel);
		tabbedPane.add("Click Removal", waveNoisePanel);
		tabbedPane.add("Spectral Noise Removal", specNoisePanel);
		
//		JPanel g = new JPanel();
//		g.setBorder(new EmptyBorder(10,10,10,10));
//		g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));
		
		sourcePanel = new SourcePanel(this, "Raw data source for FFT", RawDataUnit.class, true, true);
		specPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
//		g.add(sourcePanel.getPanel());

		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createTitledBorder("FFT Parameters"));
		f.setLayout(new GridBagLayout());
		GridBagConstraints con = new PamGridBagContraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		con.anchor = GridBagConstraints.EAST;
//		con.weightx = 1;
		con.gridx = con.gridy = 0;
		addComponent(f, new JLabel("  FFT Length "), con);
		con.gridx++;
		fftLengthModel = new FFTLengthModel(this);
		fftLengthSpinner = new JSpinner(fftLengthModel);
		fftLengthData = new JTextField(6);
		fftLengthSpinner.setEditor(fftLengthData);
		addComponent(f, fftLengthSpinner, con);
		con.gridx = 0;
		con.gridy++;
		addComponent(f, new JLabel("     FFT Hop "), con);
		con.gridx++;
		addComponent(f, fftHopData = new JTextField(6), con);
		con.gridx++;
		addComponent(f, defaultOverlap = new JButton("Default (50%)"), con);
		defaultOverlap.addActionListener(new DefaultOverlap());
		FFTChangeListener fftChangeListener = new FFTChangeListener();
		fftLengthData.addPropertyChangeListener("value", fftChangeListener);
		fftHopData.addPropertyChangeListener("value", fftChangeListener);
		fftLengthData.getDocument().addDocumentListener(fftChangeListener);
		fftHopData.getDocument().addDocumentListener(fftChangeListener);
		con.gridx = 0;
		con.gridy++;
		con.gridx = 0;
		con.gridy++;
		con.gridwidth = 2;
		addComponent(f, new JLabel("      Window "), con);
		con.gridx++;
		addComponent(f, windowFunction = new JComboBox(), con);
		windowFunction.addActionListener(fftChangeListener);
		specPanel.add(BorderLayout.CENTER, f);
		con.gridx = 1;
		con.gridy++;
		addComponent(f, resolutionPanel.getPanel(), con);
		
		sourcePanel.addSelectionListener(fftChangeListener);
		
		
		JPanel c = new JPanel();
		c.setBorder(BorderFactory.createTitledBorder("Click Supression"));
		c.setLayout(new GridBagLayout());
		con.gridx = con.gridy = 0;
		con.gridwidth = 2;
//		JLabel lab = new JLabel(clickRemoveMessage);
//		addComponent(c, lab, con);
//		con.gridy++;
		addComponent(c, clickRemoval = new JCheckBox("Supress clicks"), con);
		clickRemoval.setToolTipText(clickRemoveMessage);
		con.gridy++;
		con.gridwidth = 1;
		con.gridx = 0;
		con.fill = GridBagConstraints.NONE;
		addComponent(c, new JLabel("Threshold (STD's)"), con);
		con.gridx++;
		addComponent(c, clickThreshold = new JTextField(6), con);
		con.gridy++;
		con.gridwidth = 1;
		con.gridx = 0;
		addComponent(c, new JLabel("    Power"), con);
		con.gridx++;
		addComponent(c, clickPower = new JTextField(6), con);
		
		waveNoisePanel.add(BorderLayout.NORTH, c);
		
		spectrogramNoiseDialogPanel = new SpectrogramNoiseDialogPanel(spectrogramNoiseProcess);
		specNoisePanel.add(BorderLayout.NORTH, spectrogramNoiseDialogPanel.getPanel());
		
		spectrogramNoiseDialogPanel.setSourcePanel(sourcePanel);
		
//		
//		f1.add(new JLabel("  FFT Length "));
//		f1.add(new JLabel("     FFT Hop "));
//		JPanel f2 = new JPanel();
//		f2.setLayout(new BoxLayout(f2, BoxLayout.Y_AXIS));
//		f2.add(fftLengthData = new JTextField(8));
//		f2.add(fftHopData = new JTextField(8));
//		f.add(f1);
//		f.add(f2);
//
//////		g.add(h);
//		g.add(f);
////		g.add(c);
//		specPanel.add(comp)

		setDialogComponent(tabbedPane);
		
		setHelpPoint("sound_processing.fftManagerHelp.docs.FFTEngine_Configuring");

	}
	
//	void showChannelList()
//	{
//		int channelMap = 0xFFFF;
//		PamDataBlock rawDataBlock = (PamDataBlock) sourceList.getSelectedItem();
//		if (rawDataBlock != null) channelMap = rawDataBlock.getChannelMap();
//		
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
//			selectChannel[i].setVisible((1<<i & channelMap) != 0 && 
//					rawDataBlock != null);
//		}
//		pack();
//	}

//	public void actionPerformed(ActionEvent e) {
//
//		if (e.getSource() == sourceList) {
//			showChannelList();
//		}
//	}

	void setParameters() {

		// and fill in the data source list (may have changed - or might in later versions)
		ArrayList<PamDataBlock> rd = PamController.getInstance().getRawDataBlocks();
		PamDataBlock  datablock = PamController.getInstance().getRawDataBlock(fftParameters.dataSource);
		
		// this line crashed the code when datablock is null and is not necessary !
//		fftParameters.channelMap = datablock.getChannelMap(); //Xiao Yan Deng
		sourcePanel.setSourceList();
		sourcePanel.setSource(datablock);
		sourcePanel.setChannelList(fftParameters.channelMap);
		pack();
		if (datablock == null) {
			resolutionPanel.setParams(null);
		}
		else {
			resolutionPanel.setParams(datablock.getSampleRate(), fftParameters.fftLength,
					fftParameters.fftHop);
		}

		fftLengthData.setText(String.format("%d", fftParameters.fftLength));
		fftHopData.setText(String.format("%d", fftParameters.fftHop));
		
		windowFunction.removeAllItems();
		String[] winNames = WindowFunction.getNames();
		for (int i = 0; i < winNames.length; i++) {
			windowFunction.addItem(winNames[i]);
		}
		windowFunction.setSelectedIndex(fftParameters.windowFunction);
		
		clickRemoval.setSelected(fftParameters.clickRemoval);
		if (fftParameters.clickThreshold == 0) {
			fftParameters.clickThreshold = ClickRemoval.defaultClickThreshold;
		}
		if (fftParameters.clickPower == 0) {
			fftParameters.clickPower = ClickRemoval.defaultClickPower;
		}
		clickThreshold.setText(String.format("%.1f", fftParameters.clickThreshold));
		clickPower.setText(String.format("%d", fftParameters.clickPower));
		
		spectrogramNoiseDialogPanel.setParams(fftParameters.spectrogramNoiseSettings);
		
	}

	@Override
	public boolean getParams() {
		try {
//			fftParameters.rawDataSource = sourceList.getSelectedItem().toString();
			fftParameters.dataSource = sourcePanel.getSourceIndex();
			fftParameters.dataSourceName = sourcePanel.getSourceName();
			fftParameters.fftLength = Integer.valueOf(fftLengthData.getText());
			fftParameters.fftHop = Integer.valueOf(fftHopData.getText());
			fftParameters.channelMap = sourcePanel.getChannelList();
			
			fftParameters.clickRemoval = clickRemoval.isSelected();
			if (fftParameters.clickRemoval) {
				fftParameters.clickThreshold = Double.valueOf(clickThreshold.getText());
				fftParameters.clickPower = Integer.valueOf(clickPower.getText());
				if (fftParameters.clickPower < 2 || fftParameters.clickPower%2 == 1) {
					JOptionPane.showMessageDialog(getOwner(), "Power must be a positive even number");
					return false;
				}
			}
			
		} catch (Exception ex) {
			return false;
		}
		if (fftParameters.channelMap == 0) return false;
		
		fftParameters.windowFunction = windowFunction.getSelectedIndex();
		
		if (FFTParameters.isValidLength(fftParameters.fftLength) == false) {
			String msg = "Using FFT Lenghts which are not a power of two will significantly increase execution times";
			int ans = WarnOnce.showWarning(getOwner(), "FFT Lengh is not a power of 2", msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
//		System.out.println("FFTParametersDialog getChannelList fftParameters.channelMap:" + fftParameters.channelMap);
		boolean nOk = spectrogramNoiseDialogPanel.getParams(fftParameters.spectrogramNoiseSettings);
		if (!nOk) {
			return false;
		}
		fftParameters.spectrogramNoiseSettings.channelList = fftParameters.channelMap;
		
		// add a warning here if the parameters have been changed
		if (paramsChanged) {
//			String	msg = "<html>If your FFT settings have been changed, you should check the parameters of any downstream modules/processes ";
//			msg += "to ensure those changes have been processed properly.</html>";
//			int newAns = WarnOnce.showWarning(this, "FFT parameter change", msg, WarnOnce.OK_OPTION);
			paramsChanged = false;
		}
		
		return true;
	}
	
	private void fillResolutionPanel() {
		PamRawDataBlock sourceData = (PamRawDataBlock) sourcePanel.getSource();
		if (sourceData == null) {
			resolutionPanel.setParams(null);
			return;
		}
		int fftLen, fftHop;
		try {
			fftLen = Integer.valueOf(fftLengthData.getText());
			fftHop = Integer.valueOf(fftHopData.getText());
		}
		catch (NumberFormatException e) {
			resolutionPanel.setParams(null);
			return;
		}
		resolutionPanel.setParams(sourceData.getSampleRate(), fftLen, fftHop);
		paramsChanged=true;
	}
	
	public void setFFTLength(int l) {
		fftLengthData.setText(String.format("%d", l));
	}
	
	@Override
	public int getFFTLength() {
		int l = -1;
		if (fftLengthData == null) {
			return 0;
		}
		try {
			l = Integer.valueOf(fftLengthData.getText());
		}
		catch (NumberFormatException e) {
			return -1;
		}
		return l;
	}
	

	@Override
	public void cancelButtonPressed() {
		fftParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		clickThreshold.setText(String.format("%.1f", ClickRemoval.defaultClickThreshold));
		clickPower.setText(String.format("%d", ClickRemoval.defaultClickThreshold));
	}
	class FFTChangeListener implements PropertyChangeListener, DocumentListener, ActionListener {
		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			fillResolutionPanel();
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			fillResolutionPanel();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			fillResolutionPanel();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			fillResolutionPanel();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			fillResolutionPanel();
		}
	}
	class DefaultOverlap implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int l = getFFTLength();
			if (l < 0) {
				return;
			}
			l = l/2;
			fftHopData.setText(String.format("%d",l));
		}
		
	}
	

}
