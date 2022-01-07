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

/**
 * 
 * @author David McLaren
 */

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import fftManager.FFTPluginPanelProvider.FFTPluginPanel;

import PamUtils.PamUtils;
import PamView.dialog.ChannelListScroller;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;



public class FFTPluginParamsDialog extends PamDialog implements ActionListener{
//		
//		private static FFTPluginParamsDialog singleInstance;
		private FFTDataDisplayOptions plotOptions;
		private JTextField nameTextField;
		private JPanel t;
		private JPanel checkBoxPanel;
		private JPanel  p, valuesPanel;
		private JTextField minValTextField;
		private JTextField maxValTextField;
		private JTextField smoothingFactorTextField;
		private JCheckBox useSpecValues;
		private FFTPluginPanel pluginPanel;
		private JCheckBox[] channelCheckBoxes;
		private int[] usedChannelList;
		
		
		private FFTPluginParamsDialog(Frame parentFrame, FFTPluginPanel pluginPanel, 
				FFTDataDisplayOptions plotOptions) {
			super(parentFrame, "Spectra plotting options" , false);

			this.pluginPanel = pluginPanel;
			this.plotOptions = plotOptions.clone();
			
			
			t = new JPanel();
//			t.setBorder(new TitledBorder("Spectra plotting options"));
//			BoxLayout tLayout = new BoxLayout(t, BoxLayout.Y_AXIS);
//			t.setLayout(tLayout);
			t.setLayout(new BorderLayout());
			
			valuesPanel = new JPanel();
			valuesPanel.setLayout(new GridBagLayout());
			valuesPanel.setBorder(new TitledBorder("Y Axis scale"));
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = c.gridy = 0;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.WEST;
			addComponent(valuesPanel, useSpecValues = new JCheckBox("Apply spectrogram scale"), c);
			c.gridwidth = 1;
			c.gridy++;
			addComponent(valuesPanel, new PamLabel("maximum (dB)"), c);
			c.gridx++;
			addComponent(valuesPanel, maxValTextField = new JTextField(5), c);
			c.gridy++;
			c.gridx = 0;
			addComponent(valuesPanel, new PamLabel("minimum (dB)"), c);
			c.gridx++;
			addComponent(valuesPanel, minValTextField = new JTextField(5), c);
			c.gridy++;
			c.gridx = 0;
			addComponent(valuesPanel, new PamLabel("smoothing factor (1:30)"), c);
			c.gridx++;
			addComponent(valuesPanel, smoothingFactorTextField = new JTextField(5), c);
//			valuesPanel.add(new PamLabel("maximum (dB)"));
//			valuesPanel.add(maxValTextField = new JTextField());
//			valuesPanel.add(new PamLabel("minimum (dB)"));
//			valuesPanel.add(minValTextField = new JTextField());
//			valuesPanel.add(new PamLabel("smoothing factor (1:30)"));
//			valuesPanel.add(smoothingFactorTextField = new JTextField());
			useSpecValues.addActionListener(new UseSpecValues());
						
			t.add(BorderLayout.CENTER, valuesPanel);
			
			checkBoxPanel = new JPanel();
			checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
			JPanel checkBoxOuterPanel = new JPanel(new BorderLayout());
			checkBoxOuterPanel.setBorder(new TitledBorder("Channels"));

			int plottableChannels = pluginPanel.getPlottableChannels();
			int nChannels = PamUtils.getNumChannels(plottableChannels);
			usedChannelList = new int[nChannels];
			channelCheckBoxes = new JCheckBox[nChannels];
			int chan;
			for(int i = 0; i < nChannels; i++){
				chan = PamUtils.getNthChannel(i, plottableChannels);
				usedChannelList[i] = chan;
				channelCheckBoxes[i] = new JCheckBox(String.format("Channel %d", chan));
				channelCheckBoxes[i].setSelected((plotOptions.plottedChannels & 1<<chan) != 0);
				checkBoxPanel.add(channelCheckBoxes[i]);
			}
			
			checkBoxOuterPanel.add(new ChannelListScroller(checkBoxPanel), BorderLayout.CENTER);
			t.add(BorderLayout.SOUTH, checkBoxOuterPanel);
			
			
//			p = new JPanel(new BorderLayout());
//			p.add(BorderLayout.CENTER, t);
			
			//p.setPreferredSize(new Dimension(200,400));
			setDialogComponent(t);
		}

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		//public static SpectrogramParameters showDialog(Frame parentFrame, SpectrogramParameters spectrogramParameters) {
		public static  FFTDataDisplayOptions showDialog(Frame parentFrame, 
				FFTPluginPanel pluginPanel, FFTDataDisplayOptions plotOptions) {

			FFTPluginParamsDialog dialog = new FFTPluginParamsDialog(parentFrame, 
					pluginPanel, plotOptions);
			
			dialog.setParams();
			
			dialog.setVisible(true);

			return dialog.plotOptions;
		}
		
		private void setParams() {

			useSpecValues.setSelected(plotOptions.useSpecValues);
			maxValTextField.setText(String.valueOf(plotOptions.maxVal));
			minValTextField.setText(String.valueOf(plotOptions.minVal));
			smoothingFactorTextField.setText(String.valueOf(plotOptions.smoothingFactor));
			enableControls();
		}
		
		@Override
		public boolean getParams() {

			plotOptions.useSpecValues = useSpecValues.isSelected();
			try {
				double tempMax = Double.valueOf(maxValTextField.getText());
				double tempMin = Double.valueOf(minValTextField.getText());
				int tempSmoothingFactor = Integer.valueOf(smoothingFactorTextField.getText());
				
				if(tempMax>tempMin && tempSmoothingFactor>=1 && tempSmoothingFactor<31){
					plotOptions.maxVal = tempMax;
					plotOptions.minVal = tempMin;
					plotOptions.smoothingFactor = tempSmoothingFactor;
					
				}else{
					JOptionPane.showMessageDialog(null, "Maximum value must be greater than" +
							"minimum value",
							"Improper number format", JOptionPane.ERROR_MESSAGE);
				}
				
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Please check values",
						"Improper number format", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			int chan;
			boolean use;
			int channels = 0;
			for (int i = 0; i < usedChannelList.length; i++) {
				if (channelCheckBoxes[i].isSelected()) {
					channels |= 1<<usedChannelList[i];
				}
			}
			plotOptions.plottedChannels = channels;
			
			return (plotOptions.plottedChannels != 0);
		}
			
			
		private class UseSpecValues implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
			
		}
		
		private void enableControls() {
			maxValTextField.setEnabled(!useSpecValues.isSelected());
			minValTextField.setEnabled(!useSpecValues.isSelected());
		}
			
			
		/*	if (!validFields()) {
				
				JOptionPane.showMessageDialog(null, "Please check values",
						"Improper number format", JOptionPane.ERROR_MESSAGE);
				
				return false;
			}
			
			try {
				simSourceParameters.sourceName =  nameTextField.getText();
				simSourceParameters.color=newColor;
				simSourceParameters.sourceSpeed = Double.valueOf(sourceSpeedField.getText()); 
				simSourceParameters.trueCourse = Double.valueOf(sourceCourseField.getText()); 
				simSourceParameters.repetitionRateSeconds = Double.valueOf(repetitionRateField.getText());
				
			}
			catch (Exception Ex) {
				return false;
			}
			


			return true;
			
		}*/
		
		
		
		/*private boolean validFields(){
			
			try {
				double tempMax = Double.valueOf(singleInstance.maxValTextField.getText());
				double tempMin = Double.valueOf(singleInstance.minValTextField.getText());
				
				if(tempMax>tempMin){
					return true;
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			}
			return false;
		}
		*/
		
		
		@Override
		public void cancelButtonPressed() {
			plotOptions = null;
		}


		@Override
		public void restoreDefaultSettings() {
			// TODO Auto-generated method stub
			
		}

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}