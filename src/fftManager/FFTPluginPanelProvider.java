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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Acquisition.AcquisitionProcess;
import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.PamColors;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import Spectrogram.SpectrogramDisplay;





public class FFTPluginPanelProvider implements DisplayPanelProvider {

	private FFTDataBlock fftDataBlock;



	public FFTPluginPanelProvider(FFTDataBlock fftDataBlock) {
		// hold a reference to the Controller running this display
//		this.pamFFTControl = pamFFTControl;
		this.fftDataBlock = fftDataBlock;
		// tell the provider list that I'm available.
		DisplayProviderList.addDisplayPanelProvider(this);
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new FFTPluginPanel(this, displayPanelContainer);
	}

	@Override
	public String getDisplayPanelName() {
		return fftDataBlock.getDataName();
		//		return "Channel Spectra";
	}

	public class FFTPluginPanel extends DisplayPanel implements PamObserver, PamSettings {

		private FFTPluginPanelProvider fFTPluginPanelProvider;
//		private PamFFTProcess pamFFTProcess;
		private FFTDataDisplayOptions plotOptions = new FFTDataDisplayOptions();


		private PamAxis westAxis;
		private PamAxis southAxis;
		//double[] spectrum;
		//double[][] spectra;
		//int [] channelId = new int[PamConstants.MAX_CHANNELS];
		int [] updatesPerChannel;
		int tempCounter = 0;
		double maxVal =.1;
		double minSpectrumVal = 0;
		int x0, y0, x1, y1;
		AcquisitionProcess acquisitionProcess;
		long previousMillis=0;
		long currentMillis=0;
		
		/**
		 * An array, where each position is a channel/sequence number, and the stored value at that
		 * position is the corresponding index number in the spectraArrayList for that channel/sequence.  For example, if the channelMap
		 * = 1100, then data for channel 2 would be in spectraArrayList[0] and data for channel 3 would be in spectraArrayList[1].  So...
		 * <ul>
		 * <li>channelNumToIndex[2]=0 (channel 2 data is stored in spectraArrayList[0])</li>
		 * <li>channelNumToIndex[3]=1 (channel 3 data is stored in spectraArrayList[0])</li>
		 * </ul>
		 * Note that since channelNumToIndex is an int array, the default values are all 0.  Therefore channelNumToIndex[0]=0,
		 * channelNumToIndex[1]=0, ... channelNumToIndex[PamConstants.MAX_CHANNELS]=0.  This could be confusing because they
		 * all point to spectraArrayList[0], which is really only holding data for channel 2.  So use this with caution, and make
		 * sure that the channel/sequence used really is a valid channel/sequence.
		 */
		int[] channelNumToIndex;
		//int numAveragingFFTs;
		boolean isViewer;
		
		ArrayList<AverageChannelSpectrum> spectraArrayList;

		public FFTPluginPanel(FFTPluginPanelProvider fFTPluginPanelProvider, 
				DisplayPanelContainer displayPanelContainer) {
			super(fFTPluginPanelProvider, displayPanelContainer);
			this.fFTPluginPanelProvider = fFTPluginPanelProvider;
			isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
			setupPanel();

			westAxis = new PamAxis(0, 0, 1, 1, getScaleMin(), getScaleMax(), true, "dB", "%.0f");
			westAxis.setInterval(20);
			//southAxis = new PamAxis(0, 0, 10, 10, 0, pamFFTProcess.getSampleRate()/2/1000, true, "kHz", "%.0f");
			southAxis = new PamAxis(0, 0, 1, 1, 0, fftDataBlock.getSampleRate()/2/1000, false, "kHz", "%.0f");
			southAxis.setLabelPos(PamAxis.LABEL_NEAR_CENTRE);
			southAxis.setTickPosition(PamAxis.BELOW_RIGHT);
			//			southAxis.setFractionalScale(true);
			//			southAxis.setInterval(pamFFTProcess.getSampleRate()/2/1000/4);
			southAxis.setFormat("%.1f");
			setupSouthAxis(fftDataBlock.getSampleRate());

			//			System.out.println("Number of channels saved to plotOptions" + plotOptions.numChannels);
			//spectra = new double[numChannels][pamFFTProcess.getFftLength()];

			//			System.out.println("length of fft in dp constructor: " + pamFFTProcess.getFftLength());

			updatesPerChannel = new int[PamConstants.MAX_CHANNELS];

			x0 = 0;
			y0 = 0;

			spectraArrayList = new ArrayList<AverageChannelSpectrum>();

			prepareChannels();

			getInnerPanel().addMouseMotionListener(new FFTPanelMouse());

			PamSettingManager.getInstance().registerSettings(this);
		}

		
		
		@Override
		public PamObserver getObserverObject() {
			return this;
		}
		
		private void setupPanel() {
			fftDataBlock.addObserver(this);
			try {
				acquisitionProcess = (AcquisitionProcess) fftDataBlock.getSourceProcess();

			}
			catch (ClassCastException ex) {
				acquisitionProcess = null;
			}
		}

		private double getScaleMax() {
			if (plotOptions.useSpecValues) {
				SpectrogramDisplay sd = getSpectrogramDisplay();
				if (sd != null) {
					double[] al = sd.getSpectrogramParameters().amplitudeLimits;
					return al[1];
				}
			}
			return plotOptions.maxVal;
		}

		private double getScaleMin() {
			if (plotOptions.useSpecValues) {
				SpectrogramDisplay sd = getSpectrogramDisplay();
				if (sd != null) {
					double[] al = sd.getSpectrogramParameters().amplitudeLimits;
					return al[0];
				}
			}
			return plotOptions.minVal;
		}

		private SpectrogramDisplay getSpectrogramDisplay()
		{
			try {
				return (SpectrogramDisplay) displayPanelContainer;
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public PamAxis getWestAxis() {
			return westAxis;
		}

		//		void checkChannelNumbers() {
		//			FFTParameters fftParameters = pamFFTControl.fftParameters;
		//			int fftChannels = fftParameters.channelMap;
		//			
		//			
		//		}
		int getPlottableChannels() {
//			return fftDataBlock.getChannelMap();
			return fftDataBlock.getSequenceMap();
		}

		@Override
		public PamAxis getSouthAxis() {
			return southAxis;
		}


		//JCheckBoxMenuItem menuAutoScale;
		JMenuItem scaleMenuItem;
		@Override
		protected JPopupMenu createPopupMenu() {
			/// TODO Auto-generated method stub
			JPopupMenu menu = new JPopupMenu();
			//menuAutoScale = new JCheckBoxMenuItem(" Test");
			scaleMenuItem = new JMenuItem("Scale settings...");
			scaleMenuItem.addActionListener(new OptionsListener());
			menu.add(scaleMenuItem);
			return menu;
		}


		class OptionsListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog();
			}

		}

		void showDialog() {

			FFTDataDisplayOptions newParams = FFTPluginParamsDialog
			.showDialog(null, this, plotOptions);
			if (newParams == null) return;
			plotOptions = newParams.clone();
			prepareChannels();
			setAxisRange();

		}

		synchronized private void prepareChannels() {
//			plotOptions.plottedChannels &= fftDataBlock.getChannelMap();
			plotOptions.plottedChannels &= fftDataBlock.getSequenceMap();
			int nChannels = PamUtils.getNumChannels(plotOptions.plottedChannels);
			channelNumToIndex = new int[PamConstants.MAX_CHANNELS];
			int chan;
			spectraArrayList = new ArrayList<AverageChannelSpectrum>();
			for (int i = 0; i < nChannels; i++) {
				chan = PamUtils.getNthChannel(i, plotOptions.plottedChannels);
				channelNumToIndex[chan] = i;
				spectraArrayList.add(new AverageChannelSpectrum(plotOptions.smoothingFactor, fftDataBlock.getFftLength()/2));
			}
			updatesPerChannel = new int[PamConstants.MAX_CHANNELS];
		}

		void setAxisRange() {
			westAxis.setRange(getScaleMin(), getScaleMax());
			displayPanelContainer.panelNotify(DisplayPanelContainer.DRAW_BORDER);
		}

		@Override
		public void containerNotification(DisplayPanelContainer displayContainer, int noteType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void destroyPanel() {
			if (fftDataBlock != null) {
				fftDataBlock.deleteObserver(this);
			}

		}

		@Override
		public String getObserverName() {
			return "Spectra plug in panel";
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public void noteNewSettings() {
			prepareChannels();
			// TODO Auto-generated method stub

		}

		@Override
		public void removeObservable(PamObservable o) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			setupSouthAxis(sampleRate);
		}

		private double frequencyScale = 1;
		private void setupSouthAxis(float sampleRate) {
			if (southAxis == null) return;
			if (sampleRate <= 2000) {
				frequencyScale = 1;
			}
			else if (sampleRate < 2e6){
				frequencyScale = 1000;
			}
			else {
				frequencyScale = 1e6;
			}
			southAxis.setRange(0, sampleRate/2/frequencyScale);
			displayPanelContainer.panelNotify(DisplayPanelContainer.DRAW_BORDER);
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			// TODO Auto-generated method stub
			
			if (acquisitionProcess == null) {
				setupPanel();
			}
			if (acquisitionProcess == null) {
				return;
			}

			currentMillis = PamCalendar.getTimeInMillis();
			FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
			ComplexArray fftData = fftDataUnit.getFftData();
			float sampleRate = fftDataBlock.getSampleRate();
//			int currentChannel = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
			int currentChannel = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
			int currentChannelIndex = channelNumToIndex[currentChannel];

			boolean shouldPlot = ((plotOptions.plottedChannels & 1<<currentChannel) != 0);

			if(shouldPlot && currentChannelIndex < spectraArrayList.size()){
				spectraArrayList.get(currentChannelIndex).addNewSpectrum(fftData);
			}

			/*
			 * Include less than clause so that it will restart when it's starting 
			 * again while analysing a file.  
			 */
			if(isViewer || ((currentMillis<previousMillis | currentMillis-previousMillis>100) && 
					currentChannel == PamUtils.getHighestChannel(plotOptions.plottedChannels))){
				previousMillis = currentMillis;
				Rectangle r = getPanel().getBounds();
				double xScale, yScale;
				BufferedImage image = getDisplayImage();
				if (image == null) return;
				this.clearImage();
				Graphics2D g2d = (Graphics2D)image.getGraphics();
				xScale = (double) r.width / (double) (fftData.length() - 1);
				yScale = r.height / (getScaleMax() - getScaleMin());

				int nChannels = spectraArrayList.size();
				for(int i = 0; i < nChannels; i++){
					currentChannel = PamUtils.getNthChannel(i, plotOptions.plottedChannels);
					g2d.setColor(PamColors.getInstance().getChannelColor(currentChannel));
					g2d.drawString("channel " + currentChannel, r.width-100, 20*(i+1));
					x0 = 0;
					
//					acquisitionProcess.prepareFastAmplitudeCalculation(i);
					int chanToUse = fftDataBlock.getARealChannel(currentChannel);
					acquisitionProcess.prepareFastAmplitudeCalculation(chanToUse);
					
					double[] magVal = spectraArrayList.get(i).getAverageChannelSpectra();
					double gain = fftDataBlock.getCumulativeGain(currentChannel);
					gain = 20*Math.log10(gain);
//					y0 = (int) westAxis.getPosition(acquisitionProcess.rawAmplitude2dB(spectraArrayList.get(i).getAverageChannelSpectra()[0]/fftData.length, 0, false));
//					y0 = (int) westAxis.getPosition(acquisitionProcess.fftAmplitude2dB(magVal[0], 0, sampleRate, magVal.length*2, false, true));
					y0 = (int) westAxis.getPosition(acquisitionProcess.fftAmplitude2dB(magVal[0], chanToUse, sampleRate, magVal.length*2, false, true)-gain);
//					System.out.println("y0 = " + y0);
					for (int j = 1; j < fftData.length(); j++) {
						x1 = (int) (j * xScale);
//						 magVal = spectraArrayList.get(i).getAverageChannelSpectra()[j];
//						y1 = (int) westAxis.getPosition(acquisitionProcess.rawAmplitude2dB(magVal/fftData.length, 0, false));	
//						y1 = (int) westAxis.getPosition(acquisitionProcess.fftAmplitude2dB(magVal[j], 0, sampleRate, magVal.length*2, false, true));
						double amplitude = acquisitionProcess.fftAmplitude2dB(magVal[j], chanToUse, sampleRate, magVal.length*2, false, true)-gain;
						y1 = (int) westAxis.getPosition(amplitude);
						g2d.drawLine(x0, y0, x1, y1);
						x0 = x1;
						y0 = y1;
					}
					updatesPerChannel[i]=0;
				}


				/*for(int i = 0; i<numChannels;i++){
						for (int j = 1; j < fftData.length; j++) {
							spectra[i][j] = 0;
						}
				}*/

				repaint();
			}


		}


		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
			// don't do anything by default
		}

		@Override
		public Serializable getSettingsReference() {
			return plotOptions;
		}


		@Override
		public long getSettingsVersion() {
			return FFTDataDisplayOptions.serialVersionUID;
		}


		@Override
		public String getUnitName() {
			return getDisplayPanelName();
			//			return  displayPanelProvider.getDisplayPanelName();
		}


		@Override
		public String getUnitType() {
			return "FFTPluginPanelDisplayOptions";
		}


		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			plotOptions = ((FFTDataDisplayOptions) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}

		private void setMousePosition(Point pt) {
			double f = southAxis.getDataValue(pt.x) * frequencyScale; // scale is variable
			double dB = westAxis.getDataValue(pt.y);
			this.getInnerPanel().setToolTipText(
					String.format("%s, %3.1f dB", PamUtils.formatFrequency(f), dB));
			//			System.out.println(this.getInnerPanel().getToolTipText());
		}

		class FFTPanelMouse extends MouseAdapter {

			@Override
			public void mouseMoved(MouseEvent e) {
				setMousePosition(e.getPoint());
			}

		}

		private class AverageChannelSpectrum{

			int numAveragingFfts;
			int fftSize;

			double[] averageChannelSpectra;
			ArrayList<double[]> spectrumList = new ArrayList<double[]>();

			public AverageChannelSpectrum(int numAveragingFfts, int fftSize){
				this.fftSize = fftSize;
				this.numAveragingFfts = numAveragingFfts;
				averageChannelSpectra = new double[fftSize];

				for(int i = 0; i<numAveragingFfts; i++){
					spectrumList.add(new double[fftSize]);
				}

			}

			public void addNewSpectrum(ComplexArray fftData){
				//System.out.println("fftData.length: " + fftData.length);
				//System.out.flush();

				double[] fftMags = fftData.mag();
				spectrumList.remove(0);
				spectrumList.add(fftMags);
				for(int j = 0;j<fftSize;j++){
					averageChannelSpectra[j] = 0;
				}

				for(int i = 0; i<numAveragingFfts; i++){
					for(int j = 0;j<fftSize;j++){
						averageChannelSpectra[j] = (averageChannelSpectra[j]+ spectrumList.get(i)[j]);
					}	
				}

				for(int j = 0;j<fftSize;j++){
					averageChannelSpectra[j] = averageChannelSpectra[j]/numAveragingFfts;
				}
			}

			public double[] getAverageChannelSpectra() {


				return averageChannelSpectra;
			}
		}

		@Override
		public void spectrogramMousePosition(int chan, Point point,
				long mouseTime, double mouseFreq) {
			if (fftDataBlock == null) {
				return;
			}
//			System.out.println("Plug in data blcok " + fftDataBlock);
			/*
			 * Might as well update the image for all channels, 
			 */
//			int chans = fftDataBlock.getChannelMap();
			int chans = fftDataBlock.getSequenceMap();
			int nChan = PamUtils.getNumChannels(chans);
			for (int i = 0; i < nChan; i++) {
				int aChan = PamUtils.getNthChannel(i, chans);
				FFTDataUnit fftDataUnit = fftDataBlock.getClosestUnitMillisUsingSeq(mouseTime, 1<<aChan);
				if (fftDataUnit == null) {
					continue;
				}
				else {
					addData(fftDataBlock, fftDataUnit);
				}
			}
		}


	}



}
