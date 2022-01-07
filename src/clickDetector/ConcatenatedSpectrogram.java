package clickDetector;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.panel.JBufferedPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import clickDetector.ClickDisplayManager.ClickDisplayInfo;
import fftManager.FastFFT;
import soundtrap.STClickControl;

public class ConcatenatedSpectrogram extends ClickDisplay implements PamSettings {

	private static final int NCOLOURS = 256;

	private PamAxis southAxis, westAxis;

	private CSAxes csAxes;

	private CSGraph csGraph;

	private ColourArray colorArray;

	private ConcatenatedSpectParams concatenatedSpectParams = new ConcatenatedSpectParams();

	private BufferedImage currentImage;

	private SuperDetection currentEvent;

	private long eventUpdateTime;

	private Object synchObject = new Object();

	public ConcatenatedSpectrogram(ClickControl clickControl, ClickDisplayManager clickDisplayManager,
			ClickDisplayInfo clickDisplayInfo) {
		super(clickControl, clickDisplayManager, clickDisplayInfo);
		csAxes = new CSAxes();
		setAxisPanel(csAxes = new CSAxes());
		setPlotPanel(csGraph = new CSGraph());
		csGraph.addMouseListener(new MouseAction());
		csAxes.addMouseListener(new MouseAction());
		PamSettingManager.getInstance().registerSettings(this);
		makeColourArray();
	}

	private void makeColourArray() {
		makeColourArray(concatenatedSpectParams.getColourMap());
	}
	private void makeColourArray(ColourArrayType colType) {
		colorArray = ColourArray.createStandardColourArray(NCOLOURS, colType);
	}
	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to
	 * explicitly cast from STClickControl to ClickControl, or else constructor
	 * fails
	 * 
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public ConcatenatedSpectrogram(STClickControl clickControl, ClickDisplayManager clickDisplayManager,
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}

	@Override
	public void noteNewSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Concatenated Spectrogram";
	}

	class CSAxes extends PamAxisPanel {

		public CSAxes() {
			super();
			southAxis = new PamAxis(0, 0, 1, 1, 0, 1, false, "Click", "%d");
			westAxis = new PamAxis(0, 0, 1, 1, 0, 1, true, "Frequency kHz", "%d");
			setSouthAxis(southAxis);
			setWestAxis(westAxis);
			this.SetBorderMins(20, 20, 10, 20);
		}

		public void updateClick() {
			if (currentEvent != null) {
				southAxis.setRange(0, currentEvent.getSubDetectionsCount());
				double fMax = clickControl.getClickDataBlock().getSampleRate()/2;
				if (fMax < 2000) {
					westAxis.setLabel("Frequency Hz");
					westAxis.setRange(0, fMax);
				}
				else {
					westAxis.setLabel("Frequency kHz");
					westAxis.setRange(0, fMax/1000);
				}
			}
			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (currentEvent != null) {
				String txt = String.format("Event %d UID %d, %d clicks", currentEvent.getDatabaseIndex(), 
						currentEvent.getUID(), currentEvent.getSubDetectionsCount());
				Insets insets = this.getInsets();
//				Graphics2D g2 = (Graphics2D) g;
//				FontMetrics fm = g2.getFontMetrics();
				g.drawString(txt, insets.left, insets.top-1);
			}
		}
	}

	private class CSGraph extends JBufferedPanel {

		@Override
		public void paintPanel(Graphics g, Rectangle clipRectangle) {
			if (currentImage == null) {
				return;
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			int imHeight = currentImage.getHeight();// * frequencyScale / 100;
			g2.drawImage(currentImage, 0, 0, getWidth(), getHeight(), 0, currentImage.getHeight() - imHeight,
					currentImage.getWidth(), currentImage.getHeight(), null);

			setToolTipText("No data");

		}

		@Override
		public String getToolTipText(MouseEvent event) {
			//			return String.format("x %d y %d", event.getX(), event.getY());
			double fMax = clickControl.getClickDataBlock().getSampleRate()/2;
			double h = getHeight();
			double f = (h-event.getY())/h*fMax;
			PamDataUnit currClick = null;
			int iC = 0;
			if (currentEvent != null) {
				int nEv = currentEvent.getSubDetectionsCount();
				iC = (int) ((double) event.getX() / (double) getWidth() * nEv);
				iC = Math.max(0, Math.min(iC, nEv-1));
				currClick = currentEvent.getSubDetection(iC);
			}
			if (currClick == null) {
				return String.format("%s", FrequencyFormat.formatFrequency(f, true));
			}
			else {
				return String.format("<html>%s<br>Click %d at %s</html>", FrequencyFormat.formatFrequency(f, true), 
						iC, PamCalendar.formatTime(currClick.getTimeMilliseconds()));
			}
		}
	}
	
	private class MouseAction extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

	}

	public void showPopupMenu(MouseEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(e);
			}
		});
		popMenu.add(menuItem);
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void showDialog(ActionEvent e) {
		ConcatenatedSpectParams newParams = ConcatenatedSpectrogramdialog.showDialog(PamController.getMainFrame(), 
				getFrame().getLocationOnScreen(), concatenatedSpectParams);
		if (newParams != null) {
			concatenatedSpectParams = newParams;
			makeColourArray();
			selectEvent(currentEvent);
		}
	}

	@Override
	public String getUnitName() {
		return clickControl.getUnitName();
	}
	
	@Override
	public String getUnitType() {
		return "Concatenated Spectrogram";
	}

	@Override
	public Serializable getSettingsReference() {
		return concatenatedSpectParams;
	}

	@Override
	public long getSettingsVersion() {
		return ConcatenatedSpectParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		concatenatedSpectParams = ((ConcatenatedSpectParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public void clickedOnClick(ClickDetection click) {
		if (click == null) {
			return;
		}
		if (needUpdate(click) == false) {
			return;
		}
		SuperDetection event = click.getSuperDetection(0);
		selectEvent(event);
	}
	
	private void selectEvent(SuperDetection event) {
		if (event == null) {
			return;
		}
		BufferedImage newImage = createCSImage(event);
		synchronized (synchObject) {
			currentImage = newImage;
		}
		repaint(0);
		csAxes.updateClick();
		csGraph.repaint();
	}

	private BufferedImage createCSImage(SuperDetection event) {
		if (event == null) {
			return null;
		}
		currentEvent = event;
		eventUpdateTime = event.getLastChangeTime();
		int nClick = event.getSubDetectionsCount();
		int fftLen = clickControl.getClickParameters().maxLength;
		fftLen = FastFFT.nextBinaryExp(fftLen);
		int nFreq = fftLen / 2;
		double[][] powerSpectra = new double[nClick][nFreq];
		for (int i = 0; i < nClick; i++) {
			PamDataUnit du = event.getSubDetection(i);
			if (du instanceof ClickDetection == false) {
				continue;
			}
			ClickDetection aClick = (ClickDetection) du;
			int nChan = aClick.getNChan();
			for (int c = 0; c < nChan; c++) {
				double[] ps = aClick.getPowerSpectrum(c, fftLen);
				for (int f = 0; f < nFreq; f++) {
					powerSpectra[i][f] += ps[f];
				}
				if (nClick > 1000) {
					aClick.freeClickMemory();
				}
			}
		}
		// normalise every click ...
		if (concatenatedSpectParams.normaliseAll) {
			for (int i = 0; i < nClick; i++) {
				double max = 0;
				for (int f = 0; f < nFreq; f++) {
					max = Math.max(max, powerSpectra[i][f]);
				}
				for (int f = 0; f < nFreq; f++) {
					powerSpectra[i][f] /= max;
				}
			}
			
		}
		if (concatenatedSpectParams.logVal) {
			for (int i = 0; i < nClick; i++) {
				for (int f = 0; f < nFreq; f++) {
					powerSpectra[i][f] = 10*Math.log10(powerSpectra[i][f]);
				}
			}
			
		}
		
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;

		for (int i = 0; i < nClick; i++) {
			for (int f = 0; f < nFreq; f++) {
				if (powerSpectra[i][f] == 0) {
					continue;
				}
				maxVal = Math.max(maxVal, powerSpectra[i][f]);
				minVal = Math.min(minVal, powerSpectra[i][f]);
			}
		}
		
		if (concatenatedSpectParams.logVal) {
			minVal = maxVal - concatenatedSpectParams.maxLogValS;
		}
		else {
			minVal = 0;
		}
		
		BufferedImage newImage = new BufferedImage(nClick, nFreq, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = newImage.getRaster();
		int val;
		for (int i = 0; i < nClick; i++) {
			for (int f = 0; f < nFreq; f++) {
				val = (int) ((powerSpectra[i][f] - minVal) / (maxVal - minVal) * (NCOLOURS - 1));
				val = Math.max(0, Math.min(NCOLOURS - 1, val));
				raster.setPixel(i, nFreq - 1 - f, colorArray.getIntColourArray(val));
			}
		}
		/*
		 * 
		 * val = (int) ((wignerData[i][j]-wignerMin) / (wignerMax-wignerMin) *
		 * (NCOLOURS-1)); // System.out.print(val + ", "); val = Math.max(0,
		 * Math.min(NCOLOURS-1, val)); raster.setPixel(i, wignerData[i].length-1-j,
		 * colorArray.getIntColourArray(val));
		 */
		return newImage;
	}

	private boolean needUpdate(ClickDetection click) {
		PamDataUnit event = click.getSuperDetection(0);
		if (event == null) {
			return false;
		}
		if (currentEvent == null) {
			return true;
		}
		if (currentEvent != event) {
			return true;
		}
		return (currentEvent.getLastChangeTime() != event.getLastChangeTime() || true);
	}

}
