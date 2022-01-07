package PamguardMVC;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import Layout.RepeatedAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.PamColors;

/**
 * RawDataDisplay is used to create plug in panels that can be used at the 
 * bottom of spectrogram windows (and eventually other displays). 
 * Timing is controlled by the spectrogram, which calls the 
 * containerNotification function each time it updates (adds new
 * fft data). The RawDisplayPanel then finds the correct raw data
 * from the associated RawDataBlock and draws as necessary.  
 * @author Doug Gillespie
 * @see Layout.DisplayPanelProvider
 * @see Layout.DisplayPanelContainer
 * @see Layout.DisplayPanel
 * @see PamguardMVC.PamRawDataBlock
 *
 */
public class RawDataDisplay implements DisplayPanelProvider {
	
	PamRawDataBlock rawDataBlock;
	
	
	/**
	 * The constructor is called from within each RawDataBlock as it
	 * is constructed
	 * @param rawDataBlock
	 */
	public RawDataDisplay(PamRawDataBlock rawDataBlock) {
		this.rawDataBlock = rawDataBlock;
		DisplayProviderList.addDisplayPanelProvider(this);
	}
	
	/**
	 * A name that can be used when constructing options
	 * menus, etc.
	 */
	public String getDisplayPanelName() {
		return rawDataBlock.getDataName();
	}

	/**
	 * Implementation of abstract method to create a new DisplayPanel.
	 * Each provider may create any number of display panels for
	 * different parts of the Pamguard display
	 */
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new RawDisplayPanel(this, displayPanelContainer);
	}

	
	/**
	 * There may be several actual DisplayPanels if lots of 
	 * different displays all want one.
	 * The outer class must keep a list of them all. 
	 * @author Doug Gillespie
	 *
	 */
	public class RawDisplayPanel extends DisplayPanel implements PamObserver, PamSettings {

		RawDataDisplayOptions waveOptions = new RawDataDisplayOptions();
		
		RepeatedAxis westAxis, eastAxis;
						
		public RawDisplayPanel(DisplayPanelProvider displayPanelProvider, DisplayPanelContainer displayPanelContainer) {
			super(displayPanelProvider, displayPanelContainer);
			rawDataBlock.addObserver(this, true);
			int nChan = PamUtils.getNumChannels(rawDataBlock.getChannelMap());
			PamSettingManager.getInstance().registerSettings(this);
			westAxis = new RepeatedAxis(0, 10, 0, 20, -1, 1, true, "", "%2.2g");
			westAxis.setInterval(1);
			westAxis.setRepeatCount(nChan);
			eastAxis = new RepeatedAxis(0, 10, 0, 20, -1, 1, false, "", "%2.2g");
			eastAxis.setInterval(1);
			eastAxis.setRepeatCount(nChan);
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}
		
		public String getObserverName() {
			return "Plug in BT display panel";
		}
		
		JCheckBoxMenuItem menuAutoScale;
		JMenuItem zoomIn;
		JMenuItem zoomOut;
		
		@Override
		protected JPopupMenu createPopupMenu() {
			JPopupMenu menu = new JPopupMenu();
			menuAutoScale = new JCheckBoxMenuItem(" Auto Scale Waveform");
			menuAutoScale.addActionListener(new OptionsListener());
			menu.add(menuAutoScale);
			zoomIn = new JMenuItem("Zoom In");
			zoomIn.addActionListener(new ZoomInListener());
			menu.add(zoomIn);
			zoomOut = new JMenuItem("Zoom Out");
			zoomOut.addActionListener(new ZoomOutListener());
			menu.add(zoomOut);
			return menu;
		}
		
		void checkMenuItem() {
			menuAutoScale.setSelected(waveOptions.autoScale);
			zoomIn.setEnabled(!waveOptions.autoScale);
			zoomOut.setEnabled(!waveOptions.autoScale);
		}

		/**
		 * Implementation of PamObserver
		 */
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 1000; // is this really necessary !
		}

		/**
		 * Implementation of PamObserver
		 */
		public void noteNewSettings() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * Implementation of PamObserver
		 */
		public void removeObservable(PamObservable o) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * Implementation of PamObserver
		 */
		float sampleRate;
		public void setSampleRate(float sampleRate, boolean notify) {
			this.sampleRate = sampleRate;
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
			// don't do anything by default
		}

		/**
		 * do the drawing here to avoid problems with
		 * datablocks not all arriving at same time.
		 */
		double scale = 1;
		double minMin = 0;
		double maxMax = 0;
		int lastPixel = -500;

//		private double currentXPixel;
//
//		private long currentXTime;
		private double currentXPixel = 0;

		private double[] scrollRemainder = new double[PamConstants.MAX_CHANNELS];
		
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			
			RawDataUnit rawDataUnit = (RawDataUnit) dataUnit;
			
			BufferedImage image = getDisplayImage();
			if (image == null) return;

			boolean wrap = getDisplayPanelContainer().wrapDisplay();
			
			PamDataBlock dataBlock = (PamDataBlock) o;
			int totalChannels = PamUtils.getNumChannels(dataBlock.channelMap);
			westAxis.setRepeatCount(totalChannels);
			int thisChannel = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());

			/*
			 * automatic scaling of waveform display.
			 */
			int currentPixel = 0;

			
			if (thisChannel == 0) {
				currentPixel = (int) getDisplayPanelContainer().getCurrentXPixel();
				
				if (waveOptions.autoScale && (currentPixel < lastPixel)) {
					// just started a new wrap - so check the scale
					// inlcudes a bit of hysteresis
					if ((maxMax - minMin) * scale < 1){
						scale *= 2;
						setAxisRange(1./scale);
					}
					else while ((maxMax - minMin) * scale > 2) {
						scale /= 2;
						setAxisRange(1./scale);
					}
				}
				lastPixel = currentPixel;
			}
			
			/*
			 * Calculate y axis origin for this channel
			 * based on channel number and total number of channels 
			 */
			int yExt = getInnerHeight() / totalChannels;
			int y0 = thisChannel * yExt + yExt / 2;
			int y1 = thisChannel * yExt;
			int y2 = (thisChannel+1) * yExt;
			double yScale = yExt/2 * scale;
			// need to work out from the dataUnits start time in 
			// milliseconds where it should start drawing ...
			double containerDrawPix = 0;
			long containerTime = 0;
			double pixelsPerMilli = 0;
			synchronized (displayPanelContainer) {
				containerDrawPix = displayPanelContainer.getCurrentXPixel();
				containerTime = displayPanelContainer.getCurrentXTime();
				pixelsPerMilli = getInnerWidth()/ displayPanelContainer.getXDuration();
			}
//			System.out.println(String.format("DR Ch %d t = %d", dataUnit.getChannelBitmap(), dataUnit.timeMilliseconds));
			currentPixel = (int) containerDrawPix;
			
			Graphics2D g2d = (Graphics2D) image.getGraphics();
//			g2d.setColor(Color.green);
//			g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
//			g2d.setColor(Color.pink);
//			g2d.drawLine(200, 0, (int) containerDrawPix, 30);
			
			
			double drawStartPix = containerDrawPix - (containerTime-dataUnit.getTimeMilliseconds()) * pixelsPerMilli;
			if (!wrap) {
				drawStartPix = currentXPixel;
			}
			// of course, all this is strictly non integer !
//			drawStartPix = 200 + (dataUnit.timeMilliseconds-containerTime) * pixelsPerMilli;
			int firstPixel = (int) Math.floor(drawStartPix);
			int nSamples = (int) ((drawStartPix-firstPixel) / pixelsPerMilli * sampleRate / 1000);
			int startSample = 0;
			double[] rawData = rawDataUnit.getRawData();
			if (!wrap) {
				double scrollPixs = (rawDataUnit.getSampleDuration() * 1000. / sampleRate * pixelsPerMilli);
				int intScrollPixs = (int) scrollPixs;
				scrollRemainder[thisChannel] += (scrollPixs-intScrollPixs);
				if (scrollRemainder[thisChannel] >= 1) {
					scrollRemainder[thisChannel] -= 1;
					intScrollPixs++;
				}
				int w = image.getWidth();
				g2d.drawImage(image, 0, y1, w-intScrollPixs, y2, intScrollPixs, y1, w, y2, null);
				g2d.setBackground(plotBackground);
				g2d.fillRect(w-intScrollPixs+1, y1, w, y2-y1);
				firstPixel = w-intScrollPixs;
			}
			while (startSample < rawDataUnit.getSampleDuration()) {
				nSamples = (int) Math.min(nSamples, rawDataUnit.getSampleDuration() - startSample);
				if (firstPixel < 0) firstPixel += getInnerWidth();
				if (wrap) {
					if (firstPixel >= getInnerWidth()) firstPixel -= getInnerWidth();
					g2d.setColor(plotBackground);
					g2d.drawLine(firstPixel, y1, firstPixel, y2);
					g2d.setColor(Color.RED);
					g2d.drawLine(firstPixel+1, y1, firstPixel+1, y2);
				}
				g2d.setColor(PamColors.getInstance().getChannelColor(thisChannel));
				drawSamples(g2d, rawData, firstPixel, startSample, nSamples, y0, yScale);
				startSample += nSamples;
				nSamples = (int) (sampleRate / (pixelsPerMilli * 1000));
				firstPixel++;
			}
			currentXPixel += rawDataUnit.getSampleDuration() * 1000. / sampleRate * pixelsPerMilli;
			repaint();
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			
		}
		
		protected void setAxisRange(double range) {
			eastAxis.setRange(-range, range);
			eastAxis.setInterval(range);
			westAxis.setRange(-range, range);
			westAxis.setInterval(range);
			displayPanelContainer.panelNotify(DisplayPanelContainer.DRAW_BORDER);
		}
		
		protected void drawSamples(Graphics2D g2d, double[] wavData, int pixel, int startSample, int nSamples, int y0, double yScale) {
			double max = -1e10;
			double min = 1e10;
			if (pixel < 0) pixel += getInnerWidth();
			for (int i = startSample; i < startSample + nSamples; i++) {
				if (i >= wavData.length) {
					break;
				}
				max = Math.max(max, wavData[i]);
				min = Math.min(min, wavData[i]);
			}
			minMin = Math.min(minMin, min);
			maxMax = Math.max(maxMax, max);
			int y1 = (int) (y0 - min * yScale);
			int y2 = (int) (y0 - max * yScale);
			if (y1 == y2) y1--;
			g2d.drawLine(pixel, y1, pixel, y2);
//			lastDrawnPixel = pixel;
		}
		
		@Override
		public void destroyPanel() {
			rawDataBlock.deleteObserver(this);
		}
		
		/**
		 * Not used - all timing is controlled by the arrival of new data blocks.
		 */
		@Override
		public void containerNotification(DisplayPanelContainer displayContainer, int noteType)	{
//			currentXPixel = displayContainer.getCurrentXPixel();
//			currentXTime = displayContainer.getCurrentXTime();
		}

		/**
		 * Implementation of PamSettings
		 */
		public Serializable getSettingsReference() {
			return waveOptions;
		}

		public long getSettingsVersion() {
			return RawDataDisplayOptions.serialVersionUID;
		}

		public String getUnitName() {
			return displayPanelProvider.getDisplayPanelName();
		}

		public String getUnitType() {
			return "waveform panel";
		}

		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			waveOptions = (RawDataDisplayOptions) pamControlledUnitSettings.getSettings();
			checkMenuItem();
			return true;
		}

		class OptionsListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				waveOptions.autoScale = (!waveOptions.autoScale);		
				if (waveOptions.autoScale == false) {
					scale = 1;
					setAxisRange(1);
				}
				checkMenuItem();
			}
			
		}
		
		class ZoomInListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				scale = scale*2;
				setAxisRange(1./scale);
				checkMenuItem();
			}
		}
		
		class ZoomOutListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				scale = scale/2;
				setAxisRange(1./scale);
				checkMenuItem();
			}
		}
		

		@Override
		public PamAxis getWestAxis() {
			return westAxis;
		}

		@Override
		public PamAxis getEastAxis() {
			return eastAxis;
		}
	}
	
	
}
		
