package clickDetector;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ListIterator;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import PamDetection.AbstractLocalisation;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import clickDetector.tdPlots.ClickDetSymbolChooser;

/**
 * The implementation of DisplayPanelProvider should be created
 * as a single instance (or one for each instance of the detector 
 * which has this display). Other displays within Pamguard may then optionally 
 * create actual displays that have an all singing all dancing 
 * display. 
 * @author Doug Gillespie
 *
 */
public class ClickSpectrogramPlugin implements DisplayPanelProvider {

	ClickControl clickControl;

	/**
	 * The constructor stores a reference to the click detector and
	 * registers with the DisplayProviderList.
	 * @param clickControl
	 */
	public ClickSpectrogramPlugin(ClickControl clickControl) {
		this.clickControl = clickControl;
		DisplayProviderList.addDisplayPanelProvider(this);
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new BTDisplayPanel(this, displayPanelContainer);
	}

	@Override
	public String getDisplayPanelName() {
		return clickControl.getUnitType() + " - " + clickControl.getUnitName();
	}

	/**
	 * There may be several actual DisplayPanels if lots of 
	 * different displays all want one.
	 * The outer class must keep a list of them all. 
	 * @author Doug Gillespie
	 *
	 */
	class BTDisplayPanel extends DisplayPanel implements PamObserver {

		//DisplayPanel panel;

		//		BufferedImage btImage;

		//		PamSymbol clickSymbol;

		PamDataBlock sourceDataBlock;

		public BTDisplayPanel(DisplayPanelProvider displayPanelProvider, DisplayPanelContainer displayPanelContainer) {
			super(displayPanelProvider, displayPanelContainer);
			//			clickSymbol = new PamSymbol(PamSymbol.SYMBOL_CIRCLE, 6, 6, true, Color.RED, Color.RED);
			/*
			 * Find the click data block and become an observer of it.
			 */
			sourceDataBlock = clickControl.clickDetector.getOutputClickData();
			sourceDataBlock.addObserver(this);
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		@Override
		public String getObserverName() {
			return "Plug in BT display panel";
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public void noteNewSettings() {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeObservable(PamObservable o) {
			// TODO Auto-generated method stub

		}

		float sampleRate = 1;
		private ClickDataBlock clickDataBlock;

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			this.sampleRate = sampleRate;
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub

		}

		/**
		 * PamObserver implementation to draw clicks on 
		 * the display whenever they are created by
		 * the click detector and added to the 
		 * PamDataBlock
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit arg) {

			clickDataBlock = (ClickDataBlock) o;
			if (!displayPanelContainer.wrapDisplay()) {
				return;
			}
			ClickDetection click = (ClickDetection) arg;

			long t = click.getTimeMilliseconds();

			BufferedImage image = getDisplayImage();
			if (image == null) return;

			double xScale = getInnerWidth() / displayPanelContainer.getXDuration();
			int x = (int) ((t - displayPanelContainer.getCurrentXTime()) * xScale + displayPanelContainer.getCurrentXPixel());

			double angle = click.getAngle();
			int y = (int)(getInnerHeight() / 180. * angle);

			PamSymbol symbol = ClickDetSymbolChooser.getClickSymbol(clickControl.
					getClickIdentifier(), click, clickControl.clickParameters.spectrogramColour);
			symbol.draw(image.getGraphics(), new Point(x,y));
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void destroyPanel() {
			if (sourceDataBlock != null) {
				sourceDataBlock.deleteObserver(this);
			}
		}

		int lastClear = 0;
		/**
		 * gets called every time the display scale changes
		 * can start by clearing space in front of the cursor position
		 * on the image
		 */
		@Override
		public void containerNotification(DisplayPanelContainer displayContainer, int noteType)	{		
			int clearOffset = 4;
			if (getDisplayImage() == null) return;
			//			if (displayContainer.wrapDisplay()) {
			//				int thisClear = (int) displayContainer.getCurrentXPixel();
			//				if (lastClear == thisClear) return;
			//				clearImage(lastClear+clearOffset, thisClear+clearOffset, true);
			//				int y = getInnerHeight() / 2;
			//				Graphics g = getDisplayImage().getGraphics();
			//				g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
			//				g.drawLine(lastClear-1, y, thisClear, y);
			//				lastClear = thisClear;
			//				repaint();
			//			}
			//			else {
			drawScrollingImage();
			repaint(100);
			//			}
		}

		private void drawScrollingImage() {
			if (getDisplayImage() == null) {
				return;
			}
			BufferedImage image = getDisplayImage();
			Graphics g = getDisplayImage().getGraphics();
			g.setColor(PamColors.getInstance().getColor(PamColor.PlOTWINDOW));
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			// now draw the clicks, starting with the latest. 
			if (clickDataBlock == null) {
				return;
			}
			ClickDetection click;
			double xScale = getInnerWidth() / displayPanelContainer.getXDuration();
			long currentXTime = displayPanelContainer.getCurrentXTime();
			long currentXStart = (long) (displayPanelContainer.getCurrentXTime() - displayPanelContainer.getXDuration());
			double currentXPixel = displayPanelContainer.getCurrentXPixel();
			long t;
			int x;
			int y;
			double angle;
			PamSymbol symbol;
			boolean wrap = displayPanelContainer.wrapDisplay();
			synchronized (clickDataBlock.getSynchLock()) {
				ListIterator<ClickDetection> it = clickDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
				while (it.hasPrevious()) {
					click = it.previous();
					t = click.getTimeMilliseconds();
					if (t < currentXStart) {
						break;
					}
					x = (int) ((t - currentXTime) * xScale + currentXPixel);
					if (x < 0) x += image.getWidth();
					angle = getClickPlotAngle(click);
					y = (int)(getInnerHeight() / 180. * angle);

					symbol = ClickDetSymbolChooser.getClickSymbol(clickControl.
							getClickIdentifier(), click, clickControl.clickParameters.spectrogramColour);
					symbol.draw(g, new Point(x,y));
				}
			}
			if (wrap) {
				g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
				x = (int) currentXPixel;
				g.drawLine(x, 0, x, image.getHeight());
			}
		}
		
		@Override
		public void receiveSourceNotification(int type, Object object) {
		}



	}

	private double getClickPlotAngle(ClickDetection click) {
		AbstractLocalisation loc = click.getLocalisation();
		if (loc != null) {
			double[] surfaceAngle = loc.getPlanarAngles();
			if (surfaceAngle != null && surfaceAngle.length > 0) {
				return Math.toDegrees(surfaceAngle[0]);
			}
		}
		return click.getAngle();

	}
	
	
}
