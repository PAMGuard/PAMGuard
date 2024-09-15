package IshmaelDetector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import Layout.RepeatedAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

/**
 * IshDetGraphics displays a detection function from an Ishmael-type 
 * detector (EnergySum, MatchFilt, SgramCorr).  Detection function
 * units are of type double[], with minimum length 1 (this is about
 * the minimum you could require!).  
 * @author Dave Mellinger
 * @author Modified by Jamie Macaulay 2019
 *
 */
public class IshDetGraphics implements DisplayPanelProvider, PamSettings {
	/**
	 * Reference to the Ishmael detector control. 
	 */
	private IshDetControl ishDetControl;	//should produce vectors of length >= 1

	/**
	 * Reference to the Ishmale data block 
	 */
	private PamDataBlock ishDetDataBlock = null;

	/**
	 * True for the very first line draw
	 */
	private boolean firstTime = true;			

	/**
	 * The active channels. 
	 */
	private int activeChannels = 0; 

	/**
	 * 	Count for auto scaling.
	 */
	private int autoCount=0;

	/**
	 * Auto-scaling maximum. 
	 */
	private double autoScaleMax = -Double.MAX_VALUE; 


	/**
	 * Display parameters for the Ishamel 
	 */
	private IshDisplayParams ishDisplayParams = new IshDisplayParams(); 

	public IshDetGraphics(IshDetControl ishDetControl, PamDataBlock ishDetDataBlock){
		super();
		DisplayProviderList.addDisplayPanelProvider(this);
		this.ishDetControl   = ishDetControl;
		this.ishDetDataBlock = ishDetDataBlock;
		PamSettingManager.getInstance().registerSettings(this); 
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new IshDisplayPanel(this, displayPanelContainer);
	}

	@Override
	public String getDisplayPanelName() {
		return ishDetControl.getUnitName() + " graphics";
	}


	public void prepareForRun() {
		firstTime = true;
	}


	/**
	 * The Ishamel display panel. 
	 * @author
	 *
	 */
	class IshDisplayPanel extends DisplayPanel implements PamObserver {

		int clearLastX;

		PerChannelInfo perChannelInfo[] = new PerChannelInfo[PamConstants.MAX_CHANNELS];

		/**
		 * Pop up menu for display params. 
		 */
		private JMenuItem scaleMenuItem;

		/**
		 * The last recorded height in pixels of the image. Used for resizing calcs
		 */
		private int lastImageHeight;

		PamAxis westAxis, eastAxis;

		private long lastSample;

		private PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS2, 6, 6, true, Color.GREEN, Color.GREEN);

		/**
		 * Holds some display infor for each channel. 
		 * @author
		 *
		 */
		public class PerChannelInfo {

			int xAdvance;	
			int yLo, yHi;			//Y-bounds of my display region
			int lastX, lastY, lastThreshy;		//where the last drawn line ended, in pixels
			float yScale;
			public IshDetFnDataUnit lastDataUnit;

			public PerChannelInfo(int yLo, int yHi, float yScale) {
				this.yLo = yLo;
				this.yHi = yHi;
				this.yScale = yScale;
				lastX = Integer.MIN_VALUE;
				lastThreshy = Integer.MIN_VALUE;
				xAdvance = -2;		//special case meaning "skip first 2 values"
				lastY = yHi;
			}
		}



		@Override
		protected JPopupMenu createPopupMenu() {
			/// TODO Auto-generated method stub
			JPopupMenu menu = new JPopupMenu();
			//menuAutoScale = new JCheckBoxMenuItem(" Test");
			scaleMenuItem = new JMenuItem("Scale settings...");
			scaleMenuItem.addActionListener( action ->{ 
				PamController.getInstance();
				//show a dialog with options to change Ishmael detectior scale settings. 
				IshDisplayParams newIshDisplayParams = IshDisplayDialog.showDialog(PamController.getMainFrame(), ishDisplayParams); 
				//System.out.println("NewIshDisplayParams: " + newIshDisplayParams.verticalScaleFactor);
				if (newIshDisplayParams!=null) {
					ishDisplayParams=newIshDisplayParams;
					firstTime = true; //forces the per channel info to update.  
				}
			});
			menu.add(scaleMenuItem);
			return menu;
		}


		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		public IshDisplayPanel(DisplayPanelProvider displayPanelProvider, 
				DisplayPanelContainer displayPanelContainer)
		{
			super(displayPanelProvider, displayPanelContainer);
			if (ishDetDataBlock != null) {
				ishDetDataBlock.addObserver(this); //call my update() when unit added
			}
			westAxis = new PamAxis(0, 1, 0, 1, 0, 1, PamAxis.ABOVE_LEFT, "", PamAxis.LABEL_NEAR_CENTRE, "%3.2f");
			westAxis.setCrampLabels(true);
			makeChannelInfo();
		}

		@Override
		public void containerNotification(DisplayPanelContainer displayContainer, 
				int noteType) {
		}

		@Override
		public void destroyPanel() {
			if (ishDetDataBlock != null)
				ishDetDataBlock.deleteObserver(this);
		}

		@Override
		public String getObserverName() {
			return displayPanelProvider.getDisplayPanelName();
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public void noteNewSettings() {	
		}

		@Override
		public void removeObservable(PamObservable o) {
		}


		@Override
		public void receiveSourceNotification(int type, Object object) {
			// don't do anything by default
		}

		/**
		 * The sample rates in samples per second. 
		 */
		private float sampleRate;

		/**
		 * The interval between auto-scaling occurring. 
		 */
		private int autoscaleCount=250;

		/**
		 * Reference to the yScale. 
		 */
		private float yScale = 1; 

		private double currentXPixel = 0;

		private double scrollRemainder;

		private long currentImageTime = 0;

		private double currentImageX;
		
		private boolean lastWrap = false;

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			this.sampleRate = sampleRate;
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit1) {
			IshDetFnDataUnit ishDataUnit = (IshDetFnDataUnit)dataUnit1; 
			/*
			 * Need a serious rethink to work out how to stabilise this display, 
			 * particularly for scrolling with multiple channels.
			 * Key parameters are display time (millis) and display X from 
			 * spectrogram, then need to a) work oout how much to scroll the 
			 * underlying image and b) work out the x limits for the new data 
			 * based on the scroll time.  
			 */			
			double[][] ishDetectorData = ishDataUnit.getDetData();
			if (ishDetectorData == null || ishDetectorData.length == 0) {
				return;
			}
			int ishTypes = ishDetectorData.length;
			int ishSamples = ishDetectorData[0].length;
			
			checkYScale(ishDetectorData[0]);
			boolean wrap = getDisplayPanelContainer().wrapDisplay();
			if (wrap != lastWrap) {
				clearImage();
				lastWrap = wrap;
			}

			if (ishDataUnit.getStartSample() < lastSample) {
				clearImage();
				firstTime = true;
			}
			lastSample = ishDataUnit.getStartSample();
			
			long spectrogramMillis = 0;
			double spectrogramX = 0;
			double pixelsPerMilli = 0;
			synchronized (displayPanelContainer) {
				spectrogramX = displayPanelContainer.getCurrentXPixel();
				spectrogramMillis = displayPanelContainer.getCurrentXTime();
				pixelsPerMilli = getInnerWidth()/ displayPanelContainer.getXDuration();
			}

			// need to know if this is the first channel in the datablock. 
			int firstBlockChan = PamUtils.getLowestChannel(ishDetDataBlock.getChannelMap());
			int firstUnitChan = PamUtils.getLowestChannel(ishDataUnit.getChannelBitmap());
			boolean firstChan = (firstBlockChan == firstUnitChan);
			
			PerChannelInfo chanInfo = perChannelInfo[firstUnitChan];

			
			BufferedImage image = getDisplayImage();
			if (image == null) return;
			// graphics handle to the image. 
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			
			int threshY = (int) westAxis.getPosition(ishDetControl.ishDetParams.thresh);

			/*
			 * The spectrogram x and t are changing fast and asynchronously, so when the 
			 * first channel arrives, we need to work out a t and a x for the image, then stick 
			 * with them, possibly through multiple calls for multiple channels. 
			 */
			if (firstChan) {
				if (!wrap) {
					/*
					 *  need to scroll. If currentImageTime is 0, then the display has just
					 *  been cleared, so it doesn't really matter where we start. Otherwise 
					 *  we'll have to shift left to realign the times.  
					 */
					if (currentImageTime == 0) {
						currentImageTime = spectrogramMillis;
						currentImageX = imageWidth;
					}
					else {
						double scrollPix = (spectrogramMillis - currentImageTime) * pixelsPerMilli;
						int intScrollPix = (int) Math.round(scrollPix);
						if (intScrollPix > 0) {
							g2d.drawImage(image, 0, 0, imageWidth-intScrollPix, imageHeight, intScrollPix, 0, imageWidth, imageHeight, null);
							// and clear the end bit (important!)
							g2d.setColor(plotBackground);
							clearImage(imageWidth-intScrollPix, imageWidth);
						}
						currentImageTime = (long) (spectrogramMillis-(scrollPix-intScrollPix)/pixelsPerMilli);
						currentImageX = imageWidth;
						for (int i = 0; i < perChannelInfo.length; i++) {
							if (perChannelInfo[i].lastX != Integer.MIN_VALUE) {
								perChannelInfo[i].lastX -= intScrollPix;
							}
						}
						g2d.setColor(Color.RED);
						g2d.drawLine(imageWidth-intScrollPix, threshY, imageWidth, threshY);
					}
				}
				else {
					currentImageTime = spectrogramMillis;
					currentImageX = spectrogramX;
				}
			}
			/*
			 * should now have an imageX and and image time to scale and draw everything off.
			 * note that everything above was just calculated for the display, and the actual data
			 * may have happened a while ago, so recalclate the positions of the start and end of 
			 * the actual data.  
			 */
			int dataStartX =  (int) (currentImageX + (ishDataUnit.getTimeMilliseconds()-currentImageTime)*pixelsPerMilli);
			int dataEndX =  (int) (currentImageX + (ishDataUnit.getEndTimeInMilliseconds()-currentImageTime)*pixelsPerMilli);
			if (wrap) {
				clearImage(dataStartX, dataEndX, true);
				g2d.setColor(Color.RED);
				g2d.drawLine(dataStartX, threshY, dataEndX, threshY);
			}
			double xScale = (double) (dataEndX-dataStartX)/(double) ishSamples;
			g2d.setColor(PamColors.getInstance().getChannelColor(firstUnitChan));
			for (int i = 0; i < ishSamples; i++) {
				int x = (int) (dataStartX + i*xScale)-1;
				if (wrap && x > imageWidth) {
					x -= imageWidth;
					chanInfo.lastX -= imageWidth;
				}
				int y = (int) westAxis.getPosition(ishDetectorData[0][i]);
				if (chanInfo.lastX > -imageWidth && x>=chanInfo.lastX-2) {
					g2d.drawLine(chanInfo.lastX, chanInfo.lastY, x, y);
				}
				chanInfo.lastX = x;
				chanInfo.lastY = y;
			}

		}
		
		private void checkYScale(double[] ishData) {
			double yScale = westAxis.getMaxVal(); // current value
			if (ishDisplayParams.autoScale) {
				// do something clever ...
				double max = getMax(ishData); // max of the data
				// and always make sure it's > the threshold value. 
				max = Math.max(max, ishDetControl.ishDetParams.thresh * 1.1);
				max = PamUtils.roundNumberUpP(max, 2);
				if (max > yScale) {
					yScale = max;
				}
			}
			else {
				yScale = ishDisplayParams.verticalScaleFactor;
			}
			
			if (westAxis.getMaxVal() != yScale) {
				westAxis.setMaxVal(yScale);
				displayPanelContainer.panelNotify(DisplayPanelContainer.DRAW_BORDER);
			}
		}
		
		private double getMax(double[] data) {
			double d = 0;
			for (int i = 0; i < data.length; i++) {
				d = Math.max(d, data[i]);
			}
			return d;
		}

		public void addDataOld(PamObservable o, PamDataUnit dataUnit1) {
			IshDetFnDataUnit ishDataUnit = (IshDetFnDataUnit)dataUnit1; 
			//This is called whenever new data arrives from my parent.
			//PamDataBlock inputDataBlock = (PamDataBlock)o;

			double[][] ishDetectorData = ishDataUnit.getDetData();
			if (ishDetectorData == null || ishDetectorData.length == 0) {
				return;
			}
			int ishTypes = ishDetectorData.length;
			int ishSamples = ishDetectorData[0].length;

			if (ishDataUnit.getStartSample() < lastSample) {
				clearImage();
				firstTime = true;
			}
			lastSample = ishDataUnit.getStartSample();
			/*
			 *  this is the number of samples at the original sample rate, some
			 *  detectors put out fewer levels than original samples, e.g. one per FFT.  
			 */
			Long ishDuration = ishDataUnit.getSampleDuration();
			if (ishDuration == null) {
				return;
			}
			int ishScale = ishDuration.intValue() / ishSamples;

			// need to know if this is the first channel in the datablock. 
			int firstBlockChan = PamUtils.getLowestChannel(ishDetDataBlock.getChannelMap());
			int firstUnitChan = PamUtils.getLowestChannel(ishDataUnit.getChannelBitmap());
			boolean firstChan = (firstBlockChan == firstUnitChan);

			//if (ishDetectorData.length != 1)
			//ishDetectorData[0] = 0;

			//int totalChannels = PamUtils.getNumChannels(dataBlock.getChannelMap());
			//			int chanIx = PamUtils.getSingleChannel(dataUnit.getChannelBitmap());
			int chanIx = PamUtils.getSingleChannel(ishDataUnit.getSequenceBitmap());
			BufferedImage image = getDisplayImage();

			if (image == null) return;

			// graphics handle to the image. 
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			boolean wrap = getDisplayPanelContainer().wrapDisplay();

			int len = ishDetectorData[0].length; //length of the detector output
			int len2 = ishDetectorData.length; //number of data streams; 
			//if the length is greater than 1 then the actual detector energy is at index 2,
			//otherwise it is at index 1 (the trigger data is same as the d data so point in replicating data)
			int detIndex = len2 > 1 ? 2 : 0; 

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
			int currentPixel = (int) containerDrawPix;

			//the height scale values. 
			float yScale = this.yScale;
			//if auto scale find the max value. 
			if (ishDisplayParams.autoScale) {
				for (int i = 0; i < len; i++) {
					if (ishDetectorData[0][i]>autoScaleMax) {
						autoScaleMax = ishDetectorData[0][i]; 
					}
				}
				if (autoCount==1 || autoCount%autoscaleCount==0) {
					//					if (yScale<autoScaleMax) {
					//						yScale = (float) (1.2*yScale);
					//					}
					//					else {
					//						//the maximum slowly decays. 
					//						yScale=(float) (0.8*yScale); 
					//					}
					yScale=(float) (1.05*autoScaleMax);
					autoScaleMax = -Double.MAX_VALUE; //reset
				}				
				autoCount++;
				//				System.out.println("Auto vert factor: " + yScale + "  " +  autoScaleMax + " autoCount: " +autoCount);
			}
			//if a static vertical scale factor then just use that. 
			else {
				yScale = (float) ishDisplayParams.verticalScaleFactor;
			}
			if (westAxis.getMaxVal() != yScale) {
				westAxis.setMaxVal(yScale);
				displayPanelContainer.panelNotify(DisplayPanelContainer.DRAW_BORDER);
			}
			this.yScale=yScale; 

			//check for resize; 
			if (lastImageHeight!=getInnerHeight() - 1) {
				lastImageHeight = getInnerHeight() - 1;
				firstTime=true;
			}

			if (firstTime) {
				clearLastX = 0;
				//				chan.lastDataUnit = null;
				activeChannels = ishDetControl.getActiveChannels(); 
				int nChansActive = PamUtils.getNumChannels(activeChannels); //no need to recalc this every iteration. 

				int chanN = 0;
				for (int i = 0; i < perChannelInfo.length; i++) {
					//					if ((activeChannels & (1 << i)) != 0) {
					int yLo = Math.round((float) chanN      / (float) nChansActive * lastImageHeight);
					int yHi = Math.round((float)(chanN + 1) / (float) nChansActive * lastImageHeight);
					perChannelInfo[i] = new PerChannelInfo(yLo, yHi, yScale);
					chanN++;
					//					}
					//					else perChannelInfo[i]=null; //need to reset to nul//						
					//System.out.println("nChansActive: " + nChansActive +" chanN: " + chanN + " yLo: "+ yLo + " yHi: " + yHi);
				}
				firstTime = false;
			}
			PerChannelInfo chan = perChannelInfo[chanIx];
			if (chan == null) return;

			/*
			 * Sort out x position and scaling
			 */
			double drawStartPix = containerDrawPix - (containerTime-ishDataUnit.getTimeMilliseconds()) * pixelsPerMilli;
			if (!wrap) {
				drawStartPix = currentXPixel;
			}
			/*
			 * Check for wrap and either shuffle the image along, or clear the end of it.  
			 */
			//calculate the current x (time) position. 
			//Figure out which channel this data is for.
			int xEnd = (int) displayPanelContainer.getCurrentXPixel();
			int xStart = chan.lastX;
			xEnd = (int) (xStart + ishDataUnit.getDurationInMilliseconds()*pixelsPerMilli); 
			int firstPixel = (int) Math.floor(drawStartPix);			/**
			 * OK, there is a a problem here...the position of the cursor does not
			 * necessarily correspond to the current time of the data unit which was
			 * previously assumed. So take the time of the cursor and count pixels back. 
			 */
			if (firstPixel > image.getWidth()) {
				firstPixel = (int) ((dataUnit1.getTimeMilliseconds()-displayPanelContainer.getCurrentXTime())*pixelsPerMilli);
			}
			//			double pixelsback = ((double) (displayPanelContainer.getCurrentXTime() - dataUnit1.getTimeMilliseconds()))/displayPanelContainer.getXDuration();
			//			pixelsback = pixelsback*getInnerWidth(); 

			long currentXTime = getDisplayPanelContainer().getCurrentXTime();
			/*
			 * Drawng off the currentXtime is a disaster, since it's out of synch with the incoming
			 * correlation data. 
			 */
			//			currentXTime = ishDataUnit.getEndTimeInMilliseconds();
			//convert to pixels. 
			//			xEnd= (int) (xEnd - pixelsback); 
			if (!wrap && firstChan) {
				/*
				 * If we just use the number of samples in the data, this 
				 * Doesn't work for FFT based data because of the FFT overlap.
				 * So we need to work out the millis difference between this unit
				 * and the preceding one.
				 * It's actually a bit more complex, for the correlation detectors there is quite a lag
				 * in the data, e.g. the first MF data may say it has 2095 samples, but the actual data
				 * started 4096 samples before now. This means we never ever have the latest data (the length
				 * of the kernel), so need to move everything left by that ammount.  Can we do it off the 
				 * millis time of the data unit. Need to work out two x values, for start and end of 
				 * each block.  
				 */
				// this is the absolute number of pixels we're going to scroll by. 
				double scrollPixs = ((double) (ishSamples * 1000. * ishScale) / sampleRate * pixelsPerMilli);
				if (chan.lastDataUnit != null && chan.lastDataUnit.getTimeMilliseconds() < ishDataUnit.getTimeMilliseconds()) {
					scrollPixs = (ishDataUnit.getTimeMilliseconds()-chan.lastDataUnit.getTimeMilliseconds())*pixelsPerMilli;
					//					scrollPixs = (currentXTime-ishDataUnit.getEndTimeInMilliseconds())*pixelsPerMilli;
				}
				int w = image.getWidth();
				// but we may be starting earlier, due to lag in correlation detectors. 
				//				double lag = currentXTime-ishDataUnit.getEndTimeInMilliseconds();
				double scrollXEnd = w;// - (lag*pixelsPerMilli);
				//				double scrollXStart = w - scrollPixs; //- ((currentXTime-ishDataUnit.getTimeMilliseconds())*pixelsPerMilli);
				//				scrollPixs = scrollXEnd-scrollXStart;
				int intScrollPixs = (int) scrollPixs;
				double scrollXStart = scrollXEnd-intScrollPixs;
				scrollRemainder += (scrollPixs-intScrollPixs);
				// deal with itty bitty non integer bits. 
				if (scrollRemainder >= 1) {
					scrollRemainder -= 1;
					intScrollPixs++;
				}
				// shuffle the image along. 
				g2d.drawImage(image, 0, 0, w-intScrollPixs, image.getHeight(), intScrollPixs, 0, w, image.getHeight(), null);

				//				if (wrap) {
				//				g2d.fillRect(w-intScrollPixs, 0, intScrollPixs, image.getHeight());
				//				}
				firstPixel = (int) scrollXStart;
				xStart = firstPixel;
				xEnd = (int) scrollXEnd;
				// clear end of imate. 
				g2d.setBackground(plotBackground);
				g2d.fillRect(firstPixel, 0, w-firstPixel, image.getHeight());
				// draw the threshold. 
				g2d.setColor(Color.RED);
				int yT = (int) westAxis.getPosition(ishDetControl.ishDetParams.thresh);
				g2d.drawLine(xStart-1, yT, xEnd, yT);
				chan.lastDataUnit = ishDataUnit;
				// shuffle all the chan lastX values. 
				for (int i = 0; i < perChannelInfo.length; i++) {
					if (perChannelInfo[i] != null) {
						if (perChannelInfo[i].lastX == 0) {
							perChannelInfo[i].lastX = xStart;
						}
						else {
							perChannelInfo[i].lastX -= intScrollPixs;
						}
					}
				}
			}
			else if (firstChan) {
				clearImage(xStart, xEnd, true);
			}






			//			//HACK to get autoscale working. This is code is a total mess. 
			chan.yScale=yScale;

			// sometimes xEnd < clearLastX, and the whole plugin window gets erased.  So make sure, in that case,
			// we don't call the clearImage method
			//			if (clearLastX != xEnd) {
			if (clearLastX < xEnd & wrap) {
				clearImage(clearLastX + 1, xEnd + 1, true);
				clearLastX = xEnd;
			}
			int w = image.getWidth();
			//			System.out.printf("Drawing between xpixels %d and %d 0f %d\n", xStart, xEnd, w);
			for (int i = 0; i < len; i++) {
				int x = (len == 1) ? xEnd-1 : 
					(int) Math.round(PamUtils.linterp(0,len-1,xStart,xEnd,i));

				//plot detection data
				int y = (int) (chan.yHi - (ishDetectorData[detIndex][i]/chan.yScale) * (chan.yHi-chan.yLo));
				y = (int) westAxis.getPosition(ishDetectorData[detIndex][i]);
				//				if (i == 0) {
				//					symbol.draw(g2d, new Point(x,image.getHeight()/2));
				//				}

				int threshY; 
				if (len2>1) {
					//if adaptive noise threshold show that with the the threshold value added. 
					threshY = (int) (chan.yHi - ((ishDetControl.ishDetParams.thresh+ishDetectorData[1][i])/chan.yScale) * (chan.yHi-chan.yLo));
				}
				else {
					//use static threshold 
					threshY = (int) (chan.yHi - (ishDetControl.ishDetParams.thresh/chan.yScale) * (chan.yHi-chan.yLo));
				}

				y = Math.min(chan.yHi, Math.max(y, chan.yLo));
				//if (x >= lastX) {

				//				g2d.setColor(PamColors.getInstance().getColor(PamColor.PlOTWINDOW)); 
				//				if (wrap) {
				//					g2d.fillRect(chan.lastX+1, 0, Math.abs(x-chan.lastX), image.getHeight());
				//				}

				g2d.setColor(PamColors.getInstance().getChannelColor(chanIx));		

				if (wrap) {
					if (x > image.getWidth()) {
						x -= image.getWidth();
					}
				}
				//detection function
				if (x >= chan.lastX-1) {
					g2d.drawLine(chan.lastX, chan.lastY, x, y);
				}
				if (wrap) {
					if (x >= chan.lastX && threshY >= chan.yLo && threshY < chan.yHi) {
						g2d.setColor(Color.RED);			//threshold line
						if (chan.lastThreshy == Integer.MIN_VALUE) {
							chan.lastThreshy = threshY;
						}
						g2d.drawLine(chan.lastX, chan.lastThreshy, x, threshY);
					}
				}
				chan.xAdvance = (chan.xAdvance <= 2) ? chan.xAdvance+1 
						: Math.max(chan.xAdvance, x - chan.lastX);

				chan.lastX = x;
				chan.lastY = y;
				chan.lastThreshy = threshY; 



			}
			//	clearImage(lastX+1, lastX+1+xAdvance, true);
			repaint();
		}


		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub

		}


		@Override
		public PamAxis getWestAxis() {
			return westAxis;
		}


		@Override
		public void clearImage() {
			super.clearImage();
			currentImageTime  = 0;
			makeChannelInfo();
		}	
		
		private void makeChannelInfo() {
			perChannelInfo = new PerChannelInfo[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < perChannelInfo.length; i++) {
				perChannelInfo[i] = new PerChannelInfo(0, 0, 0);
			}
		}
	}


	/**
	 * Check whether the display is set to first time 
	 * @return the firstTime
	 */
	public boolean isFirstTime() {
		return firstTime;
	}

	/**
	 * Set the display to first time. This will recalculate a variety of variables on first repaint. 
	 * Call after settings change. 
	 * @param firstTime the firstTime to set
	 */
	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

	@Override
	public String getUnitName() {
		return getUnitType() + ishDetControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Ish_Display_";
	}

	@Override
	public Serializable getSettingsReference() {
		return ishDisplayParams;
	}

	@Override
	public long getSettingsVersion() {
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		IshDisplayParams newParams = 
				(IshDisplayParams)pamControlledUnitSettings.getSettings();
		ishDisplayParams = newParams.clone();
		return true;
	}
}
