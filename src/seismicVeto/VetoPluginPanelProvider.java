package seismicVeto;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

/**
 * Provide a graphics planel, or panels that can be added to the bottom of spectrogram
 * displays. The implementation of DisplayPanelProvider is only there to let Pamguard
 * know that panels can be created. On request, this will create any number
 * of vetoPluginPanels which will get added to the displays. 
 * 
 * There are two basic ways that these things can update thier data. One is to 
 * follow cue of the redrawing of the spectrogram, the other is to subscribe to 
 * the some data block coming out of the detector. In this instance, we subscribe to 
 * the backgroundDataBlock in the vetoProcess.  
 * 
 * Note that we're handling several channels of data here !
 *  
 * @author Doug
 *
 */
public class VetoPluginPanelProvider implements DisplayPanelProvider {
	
	private VetoController vetoController;
	/*
	 * data block used exclusively for parsing data on SNR levels to 
	 * plug in display panels 
	 */
	PamDataBlock<VetoBackgroundDataUnit> backgroundDataBlock;

	public VetoPluginPanelProvider(VetoController vetoController) {
		// hold a reference to the Controller running this display
		this.vetoController = vetoController;
		// tell the provider list that I'm available.
		DisplayProviderList.addDisplayPanelProvider(this);
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new VetoPluginPanel(this, displayPanelContainer);
	}

	@Override
	public String getDisplayPanelName() {
		return "Seismic Veto trigger function";
	}

	/**
	 * The class that actually does the display work. 
	 * @author Doug
	 *
	 */
	public class VetoPluginPanel extends DisplayPanel implements PamObserver{

		private VetoPluginPanelProvider vetoPluginPanelProvider;
		
		private VetoProcess vetoProcess;
		
		PamAxis westAxis;
		
		double minValue = -20;
		
		double maxValue = +100;

		
		public VetoPluginPanel(VetoPluginPanelProvider vetoPluginPanelProvider, DisplayPanelContainer displayPanelContainer) {
			super(vetoPluginPanelProvider, displayPanelContainer);
			this.vetoPluginPanelProvider = vetoPluginPanelProvider;
			vetoProcess = vetoController.vetoProcess;
			westAxis = new PamAxis(0, 0, 1, 1, minValue, maxValue, true, "dB", "%.0f");

			// subscribe to the background data block
			backgroundDataBlock = vetoProcess.backgroundDataBlock;
			backgroundDataBlock.addObserver(this);
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		@Override
		public PamAxis getWestAxis() {
			return westAxis;
		}

//		private int lastClear = 0;		
//		private final int clearOffset = 4;
		@Override
		/*
		 * This one gets called every time the spectrogram display advances. Use this oportunity to clear
		 * the display ahead of the current drawing position and also to redraw the 0 and threshold lines
		 * on the display. 
		 */
		public void containerNotification(DisplayPanelContainer displayContainer, int noteType) {

			// copy some code out of the click panel to clear the display.			int clearOffset = 4;
//			if (getDisplayImage() == null) return;
//			int thisClear = (int) displayContainer.getCurrentXPixel();
//			if (lastClear == thisClear) return;
//			clearImage(lastClear+clearOffset, thisClear+clearOffset, true);
//			Graphics g = getDisplayImage().getGraphics();
//			g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
//			int y = getYPixel(vetoController.vetoParameters.threshold);
//			g.drawLine(lastClear-1, y, thisClear, y);
//			y = getYPixel(0);
//			g.drawLine(lastClear-1, y, thisClear, y);
//			lastClear = thisClear;
//			
//			
//			repaint();
			
		}
		
		int getYPixel(double value) {
			return (int) (getInnerHeight() * (maxValue - value) / (maxValue - minValue));
		}

		@Override
		public void destroyPanel() {

			if (backgroundDataBlock != null) {
				backgroundDataBlock.deleteObserver(this);
			}
			
		}

		@Override
		public String getObserverName() {
			return "veto plug in panel";
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) this.displayPanelContainer.getXDuration();
		}

		@Override
		public void noteNewSettings() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeObservable(PamObservable o) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub
			
		}

		int[] lastPlottedValues = new int[32];
		int[] lastXValue = new int[32];
		/**
		 * new data have arrived - work out what channel it's from and plot it.
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			/*
			 * Get the channel and y pixel for this data point
			 */
			VetoBackgroundDataUnit backgroundDataUnit = (VetoBackgroundDataUnit) arg;
			int channel = PamUtils.getSingleChannel(backgroundDataUnit.getChannelBitmap());
			double dbValue = backgroundDataUnit.getBackground();
			int yPix = getYPixel(dbValue);

			// get the graphics handle we're writing to. 
			BufferedImage image = getDisplayImage();
			if (image == null) return;
			Graphics g = image.getGraphics();
			
			// work out what the X coordinate would have been at the time of 
			// the current data block 
			double xScale = getInnerWidth() / displayPanelContainer.getXDuration();
			int currentX = (int) ((backgroundDataUnit.getTimeMilliseconds() - displayPanelContainer.getCurrentXTime()) * 
					xScale + displayPanelContainer.getCurrentXPixel());
			if (currentX >= getInnerWidth()) {
				currentX -= getInnerWidth();
			}
				
			
			// set the colour using standard colours. 
			g.setColor(PamColors.getInstance().getChannelColor(channel));
			
			// handle wrap around. i.e don't draw lines from the far right of 
			// the screen back to the left !
			if (currentX >= lastXValue[channel]) {
				g.drawLine(lastXValue[channel], lastPlottedValues[channel], currentX, yPix);
				if (backgroundDataUnit.isVetoOn()) {
					// draw an extra line near the top of the window. 
					int y = channel;
					g.drawLine(lastXValue[channel], y, currentX, y);
				}
			}
			
			// clear ahead
			int clearChannel = PamUtils.getLowestChannel(((PamDataBlock) o).getChannelMap());
			if (clearChannel == channel && lastXValue[channel] != currentX) {
				int step = 2;
				clearImage(lastXValue[channel] + step, currentX+1+step, true);
				// draw the axis
				g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
				int y = getYPixel(vetoController.vetoParameters.threshold);
				g.drawLine(lastXValue[channel]+ step, y, currentX+1+step, y);
				y = getYPixel(0);
				g.drawLine(lastXValue[channel]+ step, y, currentX+1+step, y);
			}
			
			/*
			 * store the x,y coordinates for the next line that needs ot be drawn. 
			 */
			lastPlottedValues[channel] = yPix;
			lastXValue[channel] = currentX;
			repaint(100);
			
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			
		}
		@Override
		public void receiveSourceNotification(int type, Object object) {
			// don't do anything by default
		}

	}

}
