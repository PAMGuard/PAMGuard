package clickDetector;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamView.ColourArray;
import PamView.panel.JBufferedPanel;
import clickDetector.ClickDisplayManager.ClickDisplayInfo;
import pamMaths.WignerTransform;
import soundtrap.STClickControl;

public class WignerPlot extends ClickDisplay implements PamSettings {


	private ClickControl clickControl;

	private WignerAxes wignerAxes;

	private WignerGraph wignerGraph;

	private ColourArray colorArray;

	private static final int NCOLOURS = 256;

	private PamAxis southAxis, westAxis;

	private WignerPlotOptions wignerPlotOptions = new WignerPlotOptions();

	public WignerPlot(ClickControl clickControl,
			ClickDisplayManager clickDisplayManager,
			ClickDisplayInfo clickDisplayInfo) {
		super(clickControl, clickDisplayManager, clickDisplayInfo);
		this.clickControl = clickControl;
		this.setAxisPanel(wignerAxes = new WignerAxes());
		this.setPlotPanel(wignerGraph = new WignerGraph());
		colorArray = ColourArray.createRainbowArray(NCOLOURS);
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to explicitly cast
	 * from STClickControl to ClickControl, or else constructor fails
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public WignerPlot(STClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}


	@Override
	public void noteNewSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Wigner Plot";
	}

	private ClickDetection lastClick;
	private double[][] wignerData;
	private double wignerMin, wignerMax;
	private BufferedImage wignerImage;

	private int plottedChan;

	private int frequencyScale = 100;

	@Override
	public void clickedOnClick(ClickDetection click) {

		lastClick = click;

		if (click == null) {
			return;
		}

		// by default, use the loudest channel
		double amp = click.getAmplitude(0);
		int chan = 0;
		for (int i = 1; i < click.getNChan(); i++) {
			if (click.getAmplitude(i) > amp) {
				chan = i;
				amp = click.getAmplitude(i);
			}
		}
		createPlot(chan);
	}

	private void createPlot(int chan) {
		plottedChan = chan;
		double sampleRate = clickControl.getClickDetector().getSampleRate();
		double[] clickWave = lastClick.getWaveData()[plottedChan];
		int bin1 = 0, bin2 = clickWave.length, peakBin = 0;
		double peakVal, newVal;
		if (wignerPlotOptions.limitLength && bin2 > wignerPlotOptions.manualLength) {
			peakVal = clickWave[0];
			for (int i = 0; i < clickWave.length; i++) {
				if ((newVal=clickWave[i]) > peakVal) {
					peakBin = i;
					peakVal = newVal;
				}
			}
			bin1 = peakBin - wignerPlotOptions.manualLength/2;
			bin1 = Math.min(Math.max(0, bin1), bin2-wignerPlotOptions.manualLength-1);
			bin2 = bin1 + wignerPlotOptions.manualLength;
			clickWave = Arrays.copyOfRange(clickWave, bin1, bin2);
		}
		if (clickWave.length < 2) {
			System.out.println("Very short wave");
			return;
		}

//		westAxis.setMaxVal(sampleRate / 2 / 1000);
//		westAxis.setInterval(westAxis.getMaxVal() / 4);
		wignerAxes.setFrequencyZoom();
		double msStart =  bin1 / sampleRate * 1000;
		double msLen = bin2 / sampleRate * 1000;
		if (msLen < 2) {
			msLen *= 1000;
			msStart *= 1000;
			southAxis.setLabel("Time (micro-s)");
		}
		else {
			southAxis.setLabel("Time (ms)");
		}
		southAxis.setMinVal(msStart);
		southAxis.setMaxVal(msLen);
		wignerData = WignerTransform.wignerTransform(clickWave);
		wignerMin = WignerTransform.getMinValue(wignerData);
		wignerMax = WignerTransform.getMaxValue(wignerData);
		createImage();
		repaint(10);
	}

	private void createImage() {
		wignerImage = new BufferedImage(wignerData.length, wignerData[0].length, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = wignerImage.getRaster();
		int val;
		for (int i = 0; i < wignerData.length; i++) {
			for (int j = 0; j < wignerData[i].length; j++) {
				val = (int) ((wignerData[i][j]-wignerMin) / (wignerMax-wignerMin) * (NCOLOURS-1));
				//				System.out.print(val + ", ");
				val = Math.max(0, Math.min(NCOLOURS-1, val));
				raster.setPixel(i, wignerData[i].length-1-j, colorArray.getIntColourArray(val));
			}
			//			System.out.println();
		}
	}


	class WignerAxes extends PamAxisPanel {

		public WignerAxes() {
			super();
			southAxis = new PamAxis(0, 0, 1, 1, 0, 1, false, "Time (ms)", "%.1f");
			westAxis = new PamAxis(0, 0, 1, 1, 0, 1, true, "Frequency kHz", "%d");
			setSouthAxis(southAxis);
			setWestAxis(westAxis);
			this.SetBorderMins(10, 20, 10, 20);
		}

		void setFrequencyZoom() {
			double sampleRate = clickControl.getClickDetector().getSampleRate();
			double maxVal = sampleRate /2 * frequencyScale / 100;
			if (maxVal > 2000) {
				westAxis.setMaxVal(maxVal / 1000);
				westAxis.setLabel("Frequency kHz");
			}
			else {
				westAxis.setMaxVal(maxVal);
				westAxis.setLabel("Frequency Hz");
			}
			westAxis.setInterval(westAxis.getMaxVal() / 4);
			repaint();
		}

	}

	class WignerGraph extends JBufferedPanel {

		@Override
		public void paintPanel(Graphics g, Rectangle clipRectangle) {

			if (wignerImage == null) {
				return;
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);


			int imHeight = wignerImage.getHeight() * frequencyScale / 100;
			g2.drawImage(wignerImage, 0, 0, getWidth(), getHeight(), 0, wignerImage.getHeight()-imHeight, 
					wignerImage.getWidth(), wignerImage.getHeight(), null);

			FontMetrics fm = g2.getFontMetrics();
			int ch = PamUtils.getNthChannel(plottedChan, lastClick.getChannelBitmap());
			g2.drawString(String.format("Ch %d", ch), 2, fm.getHeight()+2);

		}

		public WignerGraph() {
			super();
			MouseFuncs mouseFuncs = new MouseFuncs();
			addMouseListener(mouseFuncs);
			addMouseWheelListener(mouseFuncs);
		}

	}

	class PlotOptions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Point pt = new Point();
			pt = wignerGraph.getLocationOnScreen();
			pt.y -= 10;
			pt.x += 10;
			WignerPlotOptions newoptions = WignerPlotdialog.showDialog(
					clickControl.getGuiFrame(), pt, wignerPlotOptions);
			if (newoptions != null) {
				wignerPlotOptions = newoptions.clone();
				clickedOnClick(lastClick);
			}
		}

	}

	class SelectChannel implements ActionListener {
		int channel;

		public SelectChannel(int channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			createPlot(channel);
		}
	}

	class MouseFuncs extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JCheckBoxMenuItem jBoxMenuItem;
				JPopupMenu menu = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem("Plot options ...");
				menuItem.addActionListener(new PlotOptions());
				menu.add(menuItem);
				if (lastClick != null) {
					menu.addSeparator();
					for (int i = 0; i < PamUtils.getNumChannels(lastClick.getChannelBitmap()); i++) {
						jBoxMenuItem = new JCheckBoxMenuItem("Channel " + 
								PamUtils.getNthChannel(i, lastClick.getChannelBitmap()));
						jBoxMenuItem.setSelected(i==plottedChan);
						jBoxMenuItem.addActionListener(new SelectChannel(i));
						menu.add(jBoxMenuItem);
					}
				}
				menu.addSeparator();
				menu.add(getCopyMenuItem());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoomFrequencyScale(e.getWheelRotation());
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return wignerPlotOptions;
	}

	public void zoomFrequencyScale(int wheelRotation) {
		frequencyScale  += wheelRotation;
		frequencyScale = Math.min(Math.max(frequencyScale, 2), 100);
		wignerAxes.setFrequencyZoom();
		wignerGraph.repaint();
	}

	@Override
	public long getSettingsVersion() {
		return WignerPlotOptions.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return clickControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Wigner Plot Options";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		wignerPlotOptions = ((WignerPlotOptions) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}


}
