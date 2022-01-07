package gpl.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.ListIterator;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.PamAxis;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.debug.Debug;
import gpl.GPLControlledUnit;
import gpl.GPLParameters;
import gpl.GPLStateDataBlock;
import gpl.GPLStateDataUnit;

public class GPLDisplayPanel extends DisplayPanel {

	private GPLControlledUnit gplControl;
	private GPLStateDataBlock stateDataBlock;
	private DataObserver dataObserver;
	private double xDuration;
	boolean logScale = true;
	private PamAxis westAxis;

	public GPLDisplayPanel(GPLControlledUnit gplControl, DisplayPanelProvider displayPanelProvider, DisplayPanelContainer displayPanelContainer) {
		super(displayPanelProvider, displayPanelContainer);
		this.gplControl = gplControl;
		stateDataBlock = gplControl.getGplProcess().getStateDataBlock();
		dataObserver = new DataObserver();
		stateDataBlock.addObserver(dataObserver);
		westAxis = new PamAxis(0, 1, 0, 1, 0, 1, 0, "Log(level)", PamAxis.ABOVE_LEFT, "%d");
	}

	@Override
	public void destroyPanel() {
		stateDataBlock.deleteObserver(dataObserver);
	}

	@Override
	public void containerNotification(DisplayPanelContainer displayContainer, int noteType) {
		repaint(100);
	}

	@Override
	public void prepareImage() {
		drawImage();
	}

	private void drawImage() {
		DisplayPanelContainer displayContainer = getDisplayPanelContainer();
		 double xPix = displayContainer.getCurrentXPixel();
		long xTime = displayContainer.getCurrentXTime();
		xDuration = displayContainer.getXDuration();
		long minTime = (long) (xTime-xDuration);
		BufferedImage image = getDisplayImage();
		int imHeight = image.getHeight();
		int imWidth = image.getWidth();
		double xScale = imWidth / xDuration;
		Graphics g = getDisplayImage().getGraphics();
		PamColors pamColours = PamColors.getInstance();
		g.setColor(pamColours.getColor(PamColor.PlOTWINDOW));
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		GPLParameters params = gplControl.getGplParameters();
		double yMax = params.thresh * 4;
		double yMin = 0;
		if (logScale) {
			yMax = 4;
			yMin = -1; // log scale
			westAxis.setInterval(1);
		}
		westAxis.setRange(yMin, yMax);
		
		int yTh = getYPix(imHeight, yMin, yMax, params.noise_ceiling)-2;
		g.setColor(pamColours.getColor(PamColor.AXIS));
		g.drawLine(0, yTh, imWidth, yTh);
		g.setColor(pamColours.getColor(PamColor.AXIS));
		String str = String.format(" Noise ceil %d", (int) params.noise_ceiling);
		g.drawString(str, 0, yTh);

		yTh = getYPix(imHeight, yMin, yMax, params.thresh)-2;
		g.setColor(Color.GREEN);
		g.drawLine(0, yTh, imWidth, yTh);
		g.setColor(pamColours.getColor(PamColor.AXIS));
		str = String.format(" Threshold %d", (int) params.thresh);
		g.drawString(str, 0, yTh);
		
		
//		System.out.println("Draw state with data n = " + stateDataBlock.getUnitsCount());
		synchronized (stateDataBlock.getSynchLock()) {
			ListIterator<GPLStateDataUnit> it = stateDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			int[] prevX = new int[PamConstants.MAX_CHANNELS];
			int[] prevY = new int[PamConstants.MAX_CHANNELS];
			Arrays.fill(prevX, Integer.MIN_VALUE);
			PamColors pamColors = PamColors.getInstance();
			int nChan = PamUtils.getNumChannels(stateDataBlock.getChannelMap());
			while (it.hasPrevious()) {
				GPLStateDataUnit state = it.previous();
				int chan = PamUtils.getSingleChannel(state.getSequenceBitmap());
				if (chan < 0) {
					continue;
				}
				Color col = pamColors.getChannelColor(chan);
				long t = state.getTimeMilliseconds();
				if (t < minTime) {
					break;
				}
				double base = state.getBaseline();
				double floor = state.getThreshfloor();
				double ceil = state.getCeilnoise();
				int xt = (int) (xPix + (t-xTime) * xScale);
				if (xt < 0) xt += imWidth;
				int y = getYPix(imHeight, yMin, yMax, base);
//				System.out.println("x = " + xt + " y = " + y);
				if (prevX[chan] != Integer.MIN_VALUE && xt <= prevX[chan]) {
					boolean on = y < yTh || prevY[chan] < yTh;
					g.setColor(nChan > 1 ? col : on ? Color.red : col);
					g.drawLine(prevX[chan], prevY[chan], xt, y);
				}
				prevX[chan] = xt;
				prevY[chan] = y;
			}
		}
		
	}
	
	@Override
	public PamAxis getWestAxis() {
		return westAxis;
	}

	private int getYPix(int imHeight, double yMin, double yMax, double yVal) {
		if (logScale) {
			if (yVal <= 0) {
				return imHeight-1;
			}
			yVal = Math.log10(Math.abs(yVal));
		}
		int h = (int) (imHeight - (imHeight * (yVal-yMin) / (yMax-yMin)));
		h = Math.max(1, h);
		h = Math.min(imHeight-1, h);
		return h;
	}

	class DataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "GPL display Panel";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			GPLDisplayPanel.this.newData(pamDataUnit);
		}

		@Override
		public long getRequiredDataHistory(PamObservable observable, Object arg) {
			return Math.max((long) xDuration, 60000) + (long) gplControl.getGplParameters().backgroundTimeSecs*1000;
		}
		
	}
	
	private void newData(PamDataUnit dataUnit) {
		repaint(100);
	}
}
