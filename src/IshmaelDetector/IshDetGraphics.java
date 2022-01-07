package IshmaelDetector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
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
	private boolean firstTime;			

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
	IshDisplayParams ishDisplayParams = new IshDisplayParams(); 

	public IshDetGraphics(IshDetControl ishDetControl, PamDataBlock ishDetDataBlock){
		super();
		DisplayProviderList.addDisplayPanelProvider(this);
		this.ishDetControl   = ishDetControl;
		this.ishDetDataBlock = ishDetDataBlock;
		PamSettingManager.getInstance().registerSettings(this); 
	}

	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new IshDisplayPanel(this, displayPanelContainer);
	}

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
						
			public PerChannelInfo(int yLo, int yHi, float yScale) {
				this.yLo = yLo;
				this.yHi = yHi;
				this.yScale = yScale;
				lastX = 0;
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
				//show a dialog with options to change Ishmael detectior scale settings. 
				IshDisplayParams newIshDisplayParams = IshDisplayDialog.showDialog(PamController.getInstance().getMainFrame(), ishDisplayParams); 
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

		public String getObserverName() {
			return displayPanelProvider.getDisplayPanelName();
		}

		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		public void noteNewSettings() {	
		}

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

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub

		}

		public void setSampleRate(float sampleRate, boolean notify) {
			this.sampleRate = sampleRate;
		}

		public void addData(PamObservable o, PamDataUnit dataUnit1) {
			IshDetFnDataUnit dataUnit = (IshDetFnDataUnit)dataUnit1; 
			//This is called whenever new data arrives from my parent.
			//PamDataBlock inputDataBlock = (PamDataBlock)o;

			double[][] ishDetectorData = dataUnit.getDetData();
		
			//if (ishDetectorData.length != 1)
			//ishDetectorData[0] = 0;

			//int totalChannels = PamUtils.getNumChannels(dataBlock.getChannelMap());
			//			int chanIx = PamUtils.getSingleChannel(dataUnit.getChannelBitmap());
			int chanIx = PamUtils.getSingleChannel(dataUnit.getSequenceBitmap());

			BufferedImage image = getDisplayImage();
			
			if (image == null) return;

			int len = ishDetectorData[0].length; //length of the detector output
			int len2 = ishDetectorData.length; //number of data streams; 
			//if the length is greater than 1 then the actual detector energy is at index 2,
			//otherwise it is at index 1 (the trigger data is same as the d data so point in replicating data)
			int detIndex = len2 > 1 ? 2 : 0; 
			
			
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
			this.yScale=yScale; 
			
			//check for resize; 
			if (lastImageHeight!=getInnerHeight() - 1) {
				lastImageHeight = getInnerHeight() - 1;
				firstTime=true;
			}
			
			if (firstTime) {
				clearLastX = 0;
				activeChannels = ishDetControl.getActiveChannels(); 
				int nChansActive = PamUtils.getNumChannels(activeChannels); //no need to recalc this every iteration. 

				int chanN = 0;
				for (int i = 0; i < perChannelInfo.length; i++) {
					if ((activeChannels & (1 << i)) != 0) {
						int yLo = Math.round((float) chanN      / (float) nChansActive * lastImageHeight);
						int yHi = Math.round((float)(chanN + 1) / (float) nChansActive * lastImageHeight);
						perChannelInfo[i] = new PerChannelInfo(yLo, yHi, yScale);
						chanN++;
					}
					else perChannelInfo[i]=null; //need to reset to nul//						
					//System.out.println("nChansActive: " + nChansActive +" chanN: " + chanN + " yLo: "+ yLo + " yHi: " + yHi);
				}
				firstTime = false;
			}
		
			//Figure out which channel this data is for.
			PerChannelInfo chan = perChannelInfo[chanIx];
			if (chan == null) return;
			
			//calculate the current x (time) position. 
			int xEnd = (int) displayPanelContainer.getCurrentXPixel();
			int xStart = chan.lastX;
			
			/**
			 * OK, there is a a problem here...the position of the cursor does not
			 * necessarily correspond to the current time of the data unit which was
			 * previously assumed. So take the time of the cursor and count pixels back. 
			 */
			double pixelsback = ((double) (displayPanelContainer.getCurrentXTime() - dataUnit1.getTimeMilliseconds()))/displayPanelContainer.getXDuration();
			pixelsback = pixelsback*getInnerWidth(); 
			
			//convert to pixels. 
			xEnd= (int) (xEnd - pixelsback); 
		

//			//HACK to get autoscale working. This is code is a total mess. 
			chan.yScale=yScale;
			
			// sometimes xEnd < clearLastX, and the whole plugin window gets erased.  So make sure, in that case,
			// we don't call the clearImage method
//			if (clearLastX != xEnd) {
			if (clearLastX < xEnd) {
				clearImage(clearLastX + 1, xEnd + 1, true);
				clearLastX = xEnd;
			}
			for (int i = 0; i < len; i++) {
				int x = (len == 1) ? xEnd : 
					(int) Math.round(PamUtils.linterp(0,len-1,xStart,xEnd,i));
				
				//plot detection data
				int y = (int) (chan.yHi - (ishDetectorData[detIndex][i]/chan.yScale) * (chan.yHi-chan.yLo));
				
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
				Graphics2D g2d = (Graphics2D) image.getGraphics();
								
				g2d.setColor(PamColors.getInstance().getColor(PamColor.PlOTWINDOW)); 
				g2d.fillRect(chan.lastX+1, 0, Math.abs(x-chan.lastX), image.getHeight());
				
				g2d.setColor(PamColors.getInstance().getChannelColor(chanIx));		

				//detection function
				if (x >= chan.lastX) {
					g2d.drawLine(chan.lastX, chan.lastY, x, y);
				}
				if (x >= chan.lastX && threshY >= chan.yLo && threshY < chan.yHi) {
					g2d.setColor(Color.RED);			//threshold line
					g2d.drawLine(chan.lastX, chan.lastThreshy, x, threshY);
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
