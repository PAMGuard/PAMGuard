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

package clickDetector;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import clickDetector.dialogs.WaveDisplayDialog;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.waveCorrector.WaveCorrector;
import soundtrap.STClickControl;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.JBufferedPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

/**
 * Display which shows the raw click waveform. 
 * 
 * @author Doug Gillespie
 *
 */
public class ClickWaveform extends ClickDisplay implements PamObserver {

	private ClickControl clickControl;

	private ClickDetection storedClick = null;

	//	double[][] lastWaveData;

	private PamAxis msAxis, binsAxis;

	private int log2Amplitude;

	private double maxAmplitude;

	private Object storedWaveformLock = new Object();

	protected boolean isViewer;

	private WaveMarker waveMarker;

	private WavePlot plotPanel;

	public ClickWaveform(ClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {

		super(clickControl, clickDisplayManager, clickDisplayInfo);

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		this.clickControl = clickControl;

		PamAxisPanel waveAxisPanel;

		setAxisPanel(waveAxisPanel = new WaveAxis());

		setPlotPanel(plotPanel = new WavePlot());

		getPlotPanel().addMouseListener(new WaveMouseListener());
		getPlotPanel().addMouseMotionListener(new WaveMouseListener());
		if (isViewer) {
			getPlotPanel().addKeyListener(new WaveKeyListener());
		}
		
		waveMarker = new WaveMarker();

		msAxis = new PamAxis(0, 100, 0, 100, 0, 1, true, "ms", "%1.1f");
		binsAxis = new PamAxis(0, 100, 0, 100, 0, 1, false, "bins", "%1.0f");
		waveAxisPanel.setNorthAxis(msAxis);
		waveAxisPanel.setSouthAxis(binsAxis);
		msAxis.setLabelPos(PamAxis.LABEL_NEAR_MAX);
		binsAxis.setLabelPos(PamAxis.LABEL_NEAR_MAX);		

		clickControl.getClickDataBlock().addObserver(this);

	}

	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to explicitly cast
	 * from STClickControl to ClickControl, or else constructor fails
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public ClickWaveform(STClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	public void newClick(ClickDetection newClick) {
		showClick(newClick);
	}

	public void showClick(ClickDetection click) {
		synchronized (storedWaveformLock) {
			storedClick = click;
			setYScale();
			waveMarker.clear();
		}
		repaint(100);
	}

	private double[][] getWaveData() {
		synchronized (storedWaveformLock) {
			if (storedClick == null) {
				return null;
			}
			if (clickControl.clickParameters.viewFilteredWaveform) {
				return storedClick.getFilteredWaveData(clickControl.clickParameters.waveformFilterParams);
			}
			else {
				return storedClick.getWaveData();
			}
		}
	}

	private double[] getEnvelopeData(int iChan) {
		if (storedClick == null) {
			return null;
		}
		if (clickControl.clickParameters.viewFilteredWaveform) {
			return storedClick.getFilteredAnalyticWaveform(clickControl.clickParameters.waveformFilterParams, iChan);
		}
		else {
			return storedClick.getAnalyticWaveform(iChan);
		}
	}


	@Override
	public void repaint(int tm) {
		if (storedClick == null) {
			return;
		}
		synchronized (storedWaveformLock) {
			if (clickControl.clickParameters.waveFixedXScale) {
				msAxis.setRange(0,  clickControl.clickParameters.maxLength * 1000
						/ storedClick.getClickDetector().getSampleRate());
				binsAxis.setRange(0, clickControl.clickParameters.maxLength);
			}
			else {
				msAxis.setRange(0, storedClick.getSampleDuration() * 1000
						/ storedClick.getClickDetector().getSampleRate());
				binsAxis.setRange(0, storedClick.getSampleDuration());
			}
		}
		super.repaint(tm);
	}

	private void setYScale() {
		/* work out the y scale */
		maxAmplitude = 0;
		if (storedClick == null) return;
		double[][] wav = getWaveData();
		for (int i = 0; i < storedClick.getNChan(); i++) {
//			maxAmplitude = Math.max(maxAmplitude, storedClick.getAmplitude(i));
			double[] w = wav[i];
			for (int j = 0; j < w.length; j++) {
				maxAmplitude = Math.max(maxAmplitude, Math.abs(w[j]));
			}
		}
		log2Amplitude = (int) Math.ceil(Math.log(maxAmplitude) / Math.log(2));
		// double yMax = 0;
		// for (int i = 0; i < storedClick.nChan; i++) {
		// for (int j = 0; j < storedClick.duration; j++) {
		// yMax = Math.max(Math.abs(lastWaveData[i][j]), yMax);
		// }
		// }
		// // scale it up in factors of two until it nearly fills the screen
		// double yScale = 1;
		// while (yScale * yMax < (double) yGap / 2.) {
		// yScale *= 2.;
		// }
		// yScale /= 2.;
	}

	//	public void repaintAll() {
	//		this.
	//	}

	class WaveAxis extends PamAxisPanel {

		WaveAxis() {
			//			PamColors.getInstance().registerComponent(this,
			//					PamColors.PamColor.BORDER);
			this.SetBorderMins(10, 20, 10, 20);
		}

		@Override
		public void paintComponent(Graphics g) {
			//only allow antialiasing in pamguard viewer mode. Want drawing to be as fast as possible in online mode
			super.paintComponent(g);

			if (storedClick == null) {
				return;
			}
			/*
			 * Paint the scale information
			 */
			String txt = String.format("x2");
			Insets inRect = getInsets();
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D strb = fm.getStringBounds(txt, g);
			int x = (int) (inRect.left - strb.getWidth()*3/2 - 1);
			int y = (int) (inRect.top + strb.getHeight()*3/2);
			//			x = inRect.left;
			//			y = getHeight()-8;
			g.drawString(txt, x, y);
			txt = String.format("%d", -log2Amplitude);
			x += strb.getWidth();
			y -= strb.getHeight()/2;
			g.drawString(txt, x, y);

			// paint other information about the click
			String typeStr = "";
//			int eventId = storedClick.eventId;
//			if (eventId > 0) {
//				typeStr = String.format("Event %d", eventId);
//			}

			OfflineEventDataUnit event = (OfflineEventDataUnit) storedClick.getSuperDetection(OfflineEventDataUnit.class);
			if (event != null) {
				typeStr = appendInfoString(typeStr, String.format("%s Event id %d", event.getEventType(), event.getDatabaseIndex()));
			}
			
			byte type = storedClick.getClickType();
			if (clickControl.getClickIdentifier() != null && type != 0) {
				String typeName = clickControl.getClickIdentifier().getSpeciesName(type);
				if (typeName == null) {
					typeName = new Byte(type).toString();
				}
				typeStr = appendInfoString(typeStr, typeName);
			}
			if (storedClick.isEcho()) {
				typeStr = appendInfoString(typeStr, "Echo");
			}
			if (typeStr != null) {
				x = getWidth()/2;
				strb = fm.getStringBounds(typeStr, g);
				x -= strb.getWidth()/2;
				y = (int) (strb.getHeight() + 5);
				g.drawString(typeStr, x, y);
			}
			/**
			 * Show absolute time to the nearest millisecond
			 */
			String timeStr = PamCalendar.formatTime(storedClick.getTimeMilliseconds(), true, true);
			x = inRect.left;
			y = getHeight()-10;
			g.drawString(timeStr, x, y);

			//			if (storedClick != null) {
			//				String species = clickControl.getClickIdentifier().getSpeciesName(storedClick.getClickType());
			//				if (species != null) {
			//					Insets insets = getInsets();
			//					FontMetrics fm = g.getFontMetrics();
			//					g.drawString(species, insets.left, fm.getHeight());
			//				}
			//			}

			// msAxis.SetPosition(0, y, r.width, y);
			// binsAxis.SetPosition(0, y, getWidth(), y);
			// msAxis.DrawAxis(g);
			// binsAxis.DrawAxis(g);
		}

		/**
		 * Build up an information string, adding ,'s if necessary
		 * @param typeStr
		 * @param string
		 * @return
		 */
		private String appendInfoString(String typeStr, String string) {
			String newString = typeStr;
			if (newString == null) {
				newString = "";
			}
			if (newString.length() > 0) {
				newString += ", ";
			}
			newString += string;
			return newString;
		}
	}

	class WavePlot extends JBufferedPanel {

		double xScale;
		
		public WavePlot() {
			super();
			setFocusable(true);
		}

		/**
		 * Convert a sample number into a pixel number
		 * @param sample sample number
		 * @return pixel number
		 */
		public int samplesToXPix(double sample) {
			return (int) (sample * xScale);
		}
		
		/**
		 * Convert an x pixel number into a sample number
		 * @param pixs pixel number
		 * @return sample number
		 */
		public double xPixsToSamples(int pixs) {
			if (xScale == 0) {
				return 0;
			}
			return pixs / xScale;
		}
		
		/**
		 * Convert a y pixel number into a channel number. 
		 * @param pixs y pixel number
		 * @return channel number
		 */
		public int yPixsToChannel(int pixs) {
			if (storedClick == null) {
				return -1;
			}
			int chanMap = storedClick.getChannelBitmap();
			int nChan = PamUtils.getNumChannels(chanMap);
			int chanIndex = (pixs*nChan)/getHeight();
			chanIndex = Math.max(0, Math.min(chanIndex, nChan-1));
//			return PamUtils.getNthChannel(chanIndex, chanMap);
			return chanIndex;
		}
		
		@Override
		public void paintPanel(Graphics g, Rectangle clipRect) {
			paintMarks(g, clipRect);
			paintWaveform(g, clipRect);
		}
		
		/**
		 * Paint the markers being made by the mouse. 
		 * @param g
		 * @param clipRect
		 */
		public void paintMarks(Graphics g, Rectangle clipRect) {
			if (storedClick == null) {
				return;
			}
			int nChan = storedClick.getNChan();
			int pixsPerChan = getHeight()/nChan;
			int x, y, w, h;
			for (int i = 0; i < waveMarker.nMarks; i++) {
				x = samplesToXPix(waveMarker.markStartSample[i]);
				w = samplesToXPix(waveMarker.markLength);
				y = pixsPerChan * waveMarker.markedChannel[i];
				h = pixsPerChan;
//				System.out.println(String.format("Paint mark %d x=%d; y=%d, w=%d, h=%d", i, x, y, w, h));
				g.setColor(Color.gray);
				g.fillRect(x, y, w, h);
			}
		}

		/**
		 * Paint the waveforms. 
		 * @param g
		 * @param clipRect
		 */
		public void paintWaveform(Graphics g, Rectangle clipRect) {

			if (isViewer==true){
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
			}
			boolean singlePlot = clickControl.clickParameters.singleWavePlot;
			//			super.paintComponent(g);
			//			if (true) return;
			PamColors pamColors = PamColors.getInstance();
			if (storedClick == null)
				return;
			String txt;
			Rectangle r = getBounds();
			int yGap = r.height;
			if (!singlePlot) yGap /= storedClick.getNChan();
			// draw some axis
			int x0, y0, x1, y1, x2, y2;
			// double yMax;
			x1 = 0;
			x2 = r.width;
			if (!singlePlot) {
			for (int i = 1; i < storedClick.getNChan(); i++) {
				y1 = yGap * i;
				g.drawLine(x1, y1, x2, y1);
			}
			}
			double[][] lastWaveData = getWaveData();
			synchronized (storedWaveformLock) {
				if (lastWaveData == null)
					return;


				double yScale = yGap / Math.pow(2, log2Amplitude) / 2;
				if (clickControl.clickParameters.waveFixedXScale) {
					xScale = (double) r.width / (double) clickControl.clickParameters.maxLength;
				}
				else { 
					xScale = (double) r.width / (double) storedClick.getSampleDuration();
				}

				for (int i = 0; i < storedClick.getNChan(); i++) {
					g.setColor(pamColors.getChannelColor(i));
					y0 = yGap / 2;
					if (!singlePlot) {
						y0 += yGap * i;
					}

					drawWave(g, lastWaveData[i], y0, yScale, xScale);
					int channel = PamUtils.getNthChannel(i, storedClick.getChannelBitmap());
					g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
					txt = String.format("ch %d", channel);
					if ((storedClick.triggerList & (1<<channel)) > 0) {
						txt += " (T)";
					}
					g.drawString(txt, 2, y0 + yGap/2 - 2);
					
					txt = String.format("UID %d", storedClick.getUID());
					g.drawString(txt, 2, g.getFontMetrics().getHeight() + 2);

					if (clickControl.clickParameters.waveShowEnvelope) {
						/*
						 * Now also draw the analytic waveform
						 * 
						 */
						g.setColor(Color.GRAY);
						drawWave(g, getEnvelopeData(i), y0, yScale, xScale);
						drawWave(g, getEnvelopeData(i), y0, -yScale, xScale);
					}
				}
			}

		}
		private void drawWave(Graphics g, double[] wave, int y0, double yScale, double xScale) {
			int x1, x2, y1, y2;
			if (wave == null || wave.length <= 0) {
				return;
			}
			y1 = y0 - (int) (wave[0] * yScale);
			x1 = 0;
			int len = wave.length;
			for (int j = 1; j < len; j++) {
				y2 = y0 - (int) (wave[j] * yScale);
				x2 = (int) (j * xScale);
				g.drawLine(x1, y1, x2, y2);
				y1 = y2;
				x1 = x2;
			}
		}
	}

	@Override
	public String getName() {
		return "Click Waveform Display";
	}

	public String getObserverName() {
		return getName();
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public void noteNewSettings() {

	}

	public void removeObservable(PamObservable o) {

	}

	public void setSampleRate(float sampleRate, boolean notify) {
		setYScale();		
	}

	//	@Override
	//	public void newOfflineStore() {
	//		super.newOfflineStore();
	//		setSampleRate(clickControl.getClicksOffline().getSampleRate(), false);
	//	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}

	public void addData(PamObservable o, PamDataUnit arg) {

		if (clickDisplayManager.isBAutoScroll() && !isViewer) {
			ClickDetection click = (ClickDetection) arg;
			newClick(click);
		}

	}
	
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}
	
	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	@Override
	public void clickedOnClick(ClickDetection click) {
		synchronized (storedWaveformLock) {
			newClick(click);		
		}

	}



	private boolean maybeShowPopup(MouseEvent e) {
		//		if (mouseDown) return; 
		if (e.isPopupTrigger()) {
			JPopupMenu menu = getPopupMenu();
			if (menu != null) {
				menu.show(e.getComponent(), e.getX(), e.getY());
				return true;
			}
		}
		return false;
	}

	private JPopupMenu getPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Plot options ...");
		menuItem.addActionListener(new SettingsMenuAction(this));
		menu.add(menuItem);
		menu.add(getCopyMenuItem());
		return menu;
	}

	private class WaveMouseListener extends MouseAdapter {
		
		private boolean isDragged = false;
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			isDragged = false;
			if (maybeShowPopup(e)) {
				return;		
			}
			else if (e.getButton() == MouseEvent.BUTTON1){
				waveMarker.mousePressed(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!isDragged) {
				if (maybeShowPopup(e)) {
					return;
				}
			}
//			else {
			if (e.getButton() == MouseEvent.BUTTON1) {
				waveMarker.mouseReleased(e);
			}
//			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
				isDragged = true;
				waveMarker.mouseDragged(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			super.mouseMoved(e);
		}
		
	}
	
	private class WaveKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			
			if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
				waveMarker.clear();
			}
			
		}
		
		
	}
	
	private class WaveMarker {
		private double[] markStartSample = new double[2];
		private int[] markedChannel = new int[2];
		private int nMarks = 0;
		private double markLength;
		private int currentChan = -1;
		private double pressedPos;
		
		public WaveMarker() {
			clear();
		}

		public void clear() {
			for (int i = 0; i < markedChannel.length; i++) {
				markedChannel[i] = -1;
			}
			currentChan = -1;
			nMarks = 0;
			plotPanel.repaint();
		}

		public int mousePressed(MouseEvent e) {
			currentChan = plotPanel.yPixsToChannel(e.getY());
			pressedPos = plotPanel.xPixsToSamples(e.getX());
			if (nMarks == 1) {
				nMarks = 2;	
				markStartSample[1] = pressedPos;
				markedChannel[1] = currentChan;
				plotPanel.repaint();
			}
			return nMarks;
		}
		
		public int mouseDragged(MouseEvent e) {
			if (currentChan < 0) {
				return 0;
			}
			int x = Math.max(0, e.getX());
			if (nMarks == 0) {
				// start of the marking process. 
				nMarks = 1;
				markStartSample[0] = pressedPos;
				markedChannel[0] = currentChan;
			}
			if (nMarks == 1) {
				markLength = Math.abs(plotPanel.xPixsToSamples(x) - pressedPos);
				markStartSample[0] = Math.min(plotPanel.xPixsToSamples(x), pressedPos);
			}
			else {
				markedChannel[1] = plotPanel.yPixsToChannel(e.getY());
				markStartSample[1] = plotPanel.xPixsToSamples(x);
				markStartSample[1] = Math.min(markStartSample[1], storedClick.getSampleDuration()-markLength);
			}
			plotPanel.repaint(100);

			return nMarks;
		}
		
		public int mouseReleased(MouseEvent e) {
			if (nMarks == 2) {
				runWaveCorrector();
				clear();
			}
			return nMarks;
		}

		
	}
	

	
	private void runWaveCorrector() {
		Window frame = clickControl.getGuiFrame();
		//check that waves are OK length
		if (waveMarker.markLength<4) return;
		Boolean newTimeSet=WaveCorrector.showDialog(frame, clickControl, storedClick, waveMarker.markedChannel,
				waveMarker.markStartSample, waveMarker.markLength);
		//if a new time has been set then we need to make changes to other displays
		if (newTimeSet==true){
			//TODO
			//display changes. 
			//need to update bt display. 
		}
	}
	
	private class SettingsMenuAction implements ActionListener {

		private ClickWaveform clickWaveform;

		public SettingsMenuAction(ClickWaveform clickWaveform) {
			this.clickWaveform = clickWaveform;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			ClickParameters newParams = WaveDisplayDialog.showDialog(clickControl.getPamView().getGuiFrame(),
					clickWaveform, clickControl.clickParameters);
			if (newParams != null) {
				clickControl.clickParameters = newParams.clone();
				repaint(10);
			}
		}

	}
}
