package clickDetector.waveCorrector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector.ChannelGroupDetector;
import clickDetector.ClickLocalisation;
import fftManager.FastFFT;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.debug.Debug;
import PamView.PamSymbol;
import PamView.PamSymbolType;

public class WaveCorrector extends PamDialog {

	private static WaveCorrector singleInstance;
	private ClickControl clickControl;
	private Boolean returnValue;
	private ClickDetection click;
	private  int[] channels;
	private int[] waveStarts = new int[2];
	private int markLength;
	private WaveformPlot waveformPlot;
	private WaveformAxes waveformAxes;
	private CorrelationAxes correlationAxes;
	private CorrelationPlot correlationPlot;
	private Dimension preferredSize = new Dimension(400, 180);
	private double[][] waves = new double[2][];
	//current time offset set by the user (samples)
	private double timeOffset;
	private double sampleRate = 1;
	private double waveMax;
	double[] corrFunction;
	double[][] interpolatedMaxima;
	private JLabel maxS, currS, maxT, currT, maxC, currC, currDelay, maxDelay;
	private JButton setMaxButton;
//	private double corrMax;
	//the current time of the maximum in the cross correlation function; (samples)
	private double maxTime;
	Correlations correlations = new Correlations();

	private WaveCorrector(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, "Click Corrections", true);
		this.clickControl = clickControl;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel waveOuter = new JPanel();
		waveOuter.setBorder(new TitledBorder("Waveforms"));
		waveOuter.setLayout(new BorderLayout());
		waveformAxes = new WaveformAxes();
		waveOuter.add(waveformAxes, BorderLayout.CENTER);

		JPanel corrOuter = new JPanel(new BorderLayout());
		corrOuter.setBorder(new TitledBorder("Correlation function"));
		corrOuter.add(correlationAxes = new CorrelationAxes(), BorderLayout.CENTER);

		mainPanel.add(waveOuter);
		mainPanel.add(corrOuter);
		
		JPanel ctrlPanel = new JPanel();
		JPanel ctrlPanelOuter = new JPanel(new BorderLayout());
		ctrlPanelOuter.add(BorderLayout.WEST, ctrlPanel);
		mainPanel.add(ctrlPanelOuter);
		ctrlPanelOuter.setBorder(new TitledBorder("Correlation Values"));
		ctrlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		addComponent(ctrlPanel, new JLabel("Cursor: ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, currS = new JLabel("   ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" sample; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, currT = new JLabel("   ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" ms; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" Time Delay: ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, currDelay=new JLabel("  ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" ms; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" C(t) ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, currC = new JLabel("   ", JLabel.LEFT), c);

		c.gridx = 0;
		c.gridy++;
		addComponent(ctrlPanel, new JLabel("Maximum: ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, maxS = new JLabel("   ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" samples; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, maxT = new JLabel("   ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" ms; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" Time Delay: ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, maxDelay=new JLabel("  ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" ms; ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, new JLabel(" C(t) ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, maxC = new JLabel("   ", JLabel.LEFT), c);
		c.gridx++;
		addComponent(ctrlPanel, setMaxButton = new JButton("Set"), c);
		setMaxButton.addActionListener(new SetMaxButton());

		setDialogComponent(mainPanel);
		setResizable(true);		
	}

	public static Boolean showDialog(Window frame, ClickControl clickControl, ClickDetection clickDetection, 
			int[] channels, double[] waveStarts, double markLength) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.clickControl != clickControl) {
			singleInstance = new WaveCorrector(frame, clickControl);
		}
		//assume that the new time value is not being used unless OK button is pressed. 
		singleInstance.returnValue=false;
		//setup the initial displays showing waveforms and their cross correlation function
		singleInstance.setupView(clickDetection, channels, waveStarts, markLength);
		singleInstance.setVisible(true);
		

		return singleInstance.returnValue;
	}
	

	private void setupView(ClickDetection clickDetection, int[] channels,
			double[] waveStarts, double markLength) {
		click = clickDetection;
		this.channels = channels;

		double[][] clickWaveform = clickDetection.getWaveData();

		this.markLength = (int) markLength;
		for (int i = 0; i < 2; i++) {
			this.waveStarts[i] = (int) waveStarts[i];
			this.waveStarts[i] = (int) Math.min(this.waveStarts[i], clickDetection.getSampleDuration() - this.markLength);
			this.waveStarts[i] = (int) Math.max(this.waveStarts[i], 0);
		}
		//FIXME-Doug you put this in and it breaks the waveform corrector. 
//		int startDiff = this.waveStarts[1]-this.waveStarts[0];
//		if (startDiff < markLength/4) {
//			this.waveStarts[0] = this.waveStarts[1] = Math.min(this.waveStarts[0], this.waveStarts[1]);
//		}

		setTitle(isICI() ? "ICI Measurement" : "Delay / Bearing correction");
		sampleRate = clickControl.getClickDetector().getSampleRate();

		/*
		 * Separate out the two bits of waveform. 
		 * Channel numbers are really indexes within the current click, not absolute channel numbers. 
		 * once opened, the click waveforms must be packed to reach the next binary exp value. 
		 */
		int totalLength = FastFFT.nextBinaryExp(this.markLength);
		int channelMap = click.getChannelBitmap();
		waveMax = 0;
		for (int i = 0; i < 2; i++) {
			int chan = channels[i];
			if (chan >= clickWaveform.length) {
				waves[i] = null;
				continue;
			}
			waves[i] = Arrays.copyOfRange(clickWaveform[chan], this.waveStarts[i], this.waveStarts[i]+this.markLength);
			for (int s = 0; s < waves[i].length; s++) {
				waveMax = Math.max(waveMax, Math.abs(waves[i][s]));
			}
//			waves[i] = Arrays.copyOf(waves[i], totalLength);
		}
		
		corrFunction = correlations.getCorrelation(waves[0], waves[1], true);
		interpolatedMaxima = correlations.getInterpolatedMaxima(corrFunction);
		double maxValue = 0.;
		maxTime = 0.;
		if (interpolatedMaxima != null && interpolatedMaxima[0].length > 0) {
			int nMaxima = interpolatedMaxima[0].length;
			for (int i = 0; i < nMaxima; i++) {
				if (interpolatedMaxima[1][i] > maxValue) {
					maxValue = interpolatedMaxima[1][i];
					maxTime = interpolatedMaxima[0][i]-corrFunction.length/2;
				}
			}
		}
		setTimeOffset(maxTime);
		displayTimeValue(maxTime, maxS, maxT, maxC, maxDelay);

		waveformAxes.sortAxes(sampleRate);
		correlationAxes.sortAxes(sampleRate);

	}
	
	private class SetMaxButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			setMax();
		}
		
	}

	/**
	 * If the user has marked out two waveform sections on the same channel then assume measuring ICI. If two waveform sections are marked
	 * out on different channels then the user is examining the cross correlation function. 
	 * @return true if user is looking at ICI. 
	 */
	private boolean isICI() {
		if (channels == null || channels.length != 2) {
			return false;
		}
		return (channels[0] == channels[1]);
	}

	public void setMax() {
		setTimeOffset(maxTime);		
	}

	@Override
	public boolean getParams() {

//		//check- do we have a new time delay?
//		if (timeOffset==maxTime)return true; 
		
		if (isICI()){
			click.setICI(timeOffset);
		}
		else{
			printDelays(click.getClickLocalisation().getTimeDelays());
			//set new time delays; 
			setNewDelays();
			//need to calculate the new bearings.
			reprocessBearings(click.getClickLocalisation().getTimeDelays());
//			printDelays(click.getClickLocalisation().getTimeDelays());
			//now flag the click for update
			if (click.getDataUnitFileInformation()!=null) click.getDataUnitFileInformation().setNeedsUpdate(true);
			
		}
		
		return true;
	}

	/**
	 * Print out a list of time delays
	 * @param timeDelays- seconds or samples
	 */
	private void printDelays(double[] timeDelays){
		System.out.println("");
		System.out.println("Time Delays: ");
		for (int i=0; i<timeDelays.length; i++){
			System.out.print(" "+timeDelays[i]);
		}
		System.out.println("");
	}
	
	/**
	 * Set new time delays. 
	 */
	private void setNewDelays(){
		//now here's a slightly tricky for larger groups-=must figure out which time delays we're changing
		//time delays are referenced throughout PAMGUARD by indexM1 and indexM2 functions- note we are dealing with channels within the group i.e. channel 0 1 2 3 4, not absolute channels, making life a little easier
		//define indexM1 and indexM2
		int N=PamUtils.getNumChannels(click.getChannelBitmap());
		
		int[][] index=AbstractLocalisation.getTimeDelayChIndex(N);

		//loop through these and check for the position when both have both our channels
		int pos=-1;
		boolean contains;
		for (int i=0; i<index.length; i++){
			contains=true; 
			for (int j=0; j<channels.length; j++){
				if (index[i][0]!=channels[j] && index[i][1]!=channels[j]){
					contains=false;
					break; 
				}
			}
			if (contains){
				pos=i;
				break;
			}
		}
		//this is the time delay between the start of the selected waveform areas
		double timeDelay=getTrueDelay();
		//the user may have selected the channels in any order- might need to flip the time delay. 
		if (index[pos][1]==channels[0]) timeDelay=-timeDelay;
		//now we need to add the offset to the real time
		click.setDelayInSamples(pos, timeDelay);
	}
	
	/**
	 * Get the true time delay, the cross correlation plus the space between the start of the two waveform clips. 
	 * @return the time delay in samples. 
	 */
	private double getTrueDelay(){
		double timeDelay=waveStarts[1]-waveStarts[0];
		//now offset this by the cross correlation value
		timeDelay=timeDelay-timeOffset; 
		return timeDelay;
	}
	
	/**reset all the time delays to their automatic value;
	 * 
	 */
	private void resetDelays(){
		//TODO
	}
	
	
	
	/**
	 * After a new delay has been added must recalculate bearing info.
	 * @param delaySecs time delays(seconds)
	 */
	private void reprocessBearings(double[] delaySecs){
		double[][] angles = null;
	
		 prepareLocalisers() ;
		BearingLocaliser bearingLocaliser = click.getChannelGroupDetector().getBearingLocaliser();
		if (bearingLocaliser != null) {
			angles = bearingLocaliser.localise(delaySecs, click.getTimeMilliseconds());
		}
		if (click.getClickLocalisation() != null) {
			click.getClickLocalisation().setAnglesAndErrors(angles);
			click.getClickLocalisation().setArrayAxis(bearingLocaliser.getArrayAxis());
			click.getClickLocalisation().setSubArrayType(bearingLocaliser.getArrayType());
		}
	}
	
	private void prepareLocalisers() {
		int n = clickControl.getClickDetector().getnChannelGroups();
		ChannelGroupDetector gd;
		BearingLocaliser bl;
		int[] groupPhones;
		for (int i = 0; i < n; i++) {
			gd = clickControl.getClickDetector().getChannelGroupDetector(i);

			int[] phones = gd.getGroupHydrophones();
			bl = gd.getBearingLocaliser();
			if (bl != null) {
				bl.prepare(phones,0, Correlations.defaultTimingError(clickControl.getClickDetector().getSampleRate()));
			}
		}
	}
	

	@Override
	public void cancelButtonPressed() {
		//indicate that the new time value is not being used. 
		returnValue=false;
	}

	@Override
	public void restoreDefaultSettings() {
		//here we want an option to restore the normal delays 
		resetDelays();
	}
	
	private void setTimeOffset(double newOffset) {
		timeOffset = newOffset;
		redrawEverything();
		displayTimeValue(timeOffset, currS, currT, currC, currDelay);
	}

	/**
	 * Display the time offset and corresponding correlation value
	 * @param timeOffset2 time offset in samples
	 * @param timeLabel time label
	 * @param corrLabel correlation label. 
	 */
	private void displayTimeValue(double time, JLabel sampsLabel, JLabel timeLabel, JLabel corrLabel, JLabel curreDelay) {
//		Debug.out.printf("t = %3.2f s corr by %3d samp\n", time, waveStarts[0]-waveStarts[1]);
		time += (waveStarts[0]-waveStarts[1]);
		double sr = clickControl.getClickDetector().getSampleRate();
		double millis = time * 1000. / sr;
		timeLabel.setText(String.format("%7.3f", millis));
		sampsLabel.setText(String.format("%7.3f", time));
		double corrVal = correlations.parabolicHeight(time+corrFunction.length/2, corrFunction);
		corrLabel.setText(String.format("%6.3f", corrVal));
		
		curreDelay.setText(String.format("%7.3f", this.getTrueDelay()*1000./sr));;
	}

	private class WaveformAxes extends PamAxisPanel {

		PamAxis samplesAxis, microsAxis; 
		public WaveformAxes() {
			super();
			this.SetBorderMins(10, 10, 10, 10);
			waveformPlot = new WaveformPlot();
			JPanel innerPanel = new JPanel(new BorderLayout());
			innerPanel.setBorder(PamBorder.createInnerBorder());
			innerPanel.add(BorderLayout.CENTER, waveformPlot);
			setPlotPanel(waveformPlot);
			setInnerPanel(innerPanel);
			setAutoInsets(true);
			samplesAxis = new PamAxis(0, 1, 2, 3, 0, 2, PamAxis.BELOW_RIGHT, "Samples", PamAxis.LABEL_NEAR_CENTRE, "%d");
			microsAxis = new PamAxis(0, 1, 2, 3, 0, 2, PamAxis.ABOVE_LEFT, "micro-seconds", PamAxis.LABEL_NEAR_CENTRE, "%d");
			this.setNorthAxis(microsAxis);
			this.setSouthAxis(samplesAxis);
		}

		public void sortAxes(double sampleRate) {
			samplesAxis.setMinVal(0);
			int maxSample = markLength*3/2;
			samplesAxis.setMaxVal(maxSample);

			double lenSecs = (double) maxSample / sampleRate;
			microsAxis.setMinVal(0);
			if (lenSecs < 0.002) {
				microsAxis.setMaxVal(maxSample * 1.e6 / sampleRate);
				microsAxis.setFormat("%d");
				microsAxis.setLabel("micro-s");
			}
			else {
				microsAxis.setMaxVal(maxSample * 1.e3 / sampleRate);
				microsAxis.setFormat("%3.1f");
				microsAxis.setLabel("ms");
			}

		}
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			//			g.setColor(Color.BLUE);
			//			g.drawLine(getWidth(), 0, 0, getHeight());
		}

	}

	private class WaveformPlot extends PamPanel {

		public WaveformPlot() {
			super(PamColor.PlOTWINDOW);
			setPreferredSize(preferredSize);
		}

		@Override
		public void paintComponent(Graphics g) {
			//anti aliasing
			Graphics2D g2 = (Graphics2D) g;
				  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_ON);
				  g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				      RenderingHints.VALUE_RENDER_QUALITY);
			
			super.paintComponent(g);
			
			if (waves == null) {
				return;
			}
			double offsetSamples;
			double yScale = 0.48 * getHeight() / (Math.max(waveMax, .0000000001));
			int x1, x2, y1, y2;
			int y0 = getHeight()/2;
			for (int i = 0; i < 2; i++) {
				if (waves[i] == null) {
					continue;
				}
				offsetSamples = (i*2-1) * timeOffset/2 + markLength/4;
				x1 = (int) waveformAxes.samplesAxis.getPosition(offsetSamples);
				y1 = y0 - (int) (waves[i][0] * yScale);
				g.setColor(PamColors.getInstance().getChannelColor(i));
				for (int s = 1; s < markLength; s++) {
					x2 = (int) waveformAxes.samplesAxis.getPosition(offsetSamples+s);
					y2 = y0 - (int) (waves[i][s] * yScale);
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
			}
		}

	}
	private class CorrelationAxes extends PamAxisPanel {

		PamAxis samplesAxis, microsAxis; 

		public CorrelationAxes() {
			super();
			this.SetBorderMins(10, 10, 10, 10);
			correlationPlot = new CorrelationPlot();
			JPanel innerPanel = new JPanel(new BorderLayout());
			innerPanel.setBorder(PamBorder.createInnerBorder());
			innerPanel.add(BorderLayout.CENTER, correlationPlot);
			setPlotPanel(correlationPlot);
			setInnerPanel(innerPanel);
			setAutoInsets(true);
			samplesAxis = new PamAxis(0, 1, 2, 3, 0, 2, PamAxis.BELOW_RIGHT, "Samples", PamAxis.LABEL_NEAR_CENTRE, "%d");
			microsAxis = new PamAxis(0, 1, 2, 3, 0, 2, PamAxis.ABOVE_LEFT, "micro-seconds", PamAxis.LABEL_NEAR_CENTRE, "%d");
			this.setNorthAxis(microsAxis);
			this.setSouthAxis(samplesAxis);
		}

		public void sortAxes(double sampleRate) {
			samplesAxis.setMinVal(-markLength/2);
			samplesAxis.setInterval(markLength/2);
			samplesAxis.setMaxVal(markLength/2);

			double lenSecs = (double) markLength / sampleRate;
			if (lenSecs < 0.002) {
				microsAxis.setMinVal(-markLength/2 * 1.e6 / sampleRate);
				microsAxis.setInterval(markLength/2 * 1.e6 / sampleRate);
				microsAxis.setMaxVal(markLength/2 * 1.e6 / sampleRate);
				microsAxis.setFormat("%d");
				microsAxis.setLabel("micro-s");
			}
			else {
				microsAxis.setMinVal(-markLength/2 * 1.e3 / sampleRate);
				microsAxis.setInterval(markLength/2 * 1.e3 / sampleRate);
				microsAxis.setMaxVal(markLength/2 * 1.e3 / sampleRate);
				microsAxis.setFormat("%3.1f");
				microsAxis.setLabel("ms");
			}			
		}

	}

	private class CorrelationPlotMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				newMouseTime(e.getX());
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			newMouseTime(e.getX());
		}

		private void newMouseTime(int newX) {
			if (waveformAxes != null) {
				setTimeOffset(correlationAxes.samplesAxis.getDataValue(newX));
				redrawEverything();
			}
		}
	}

	private class CorrelationPlot extends PamPanel {

		private PamSymbol maxSymbol = new PamSymbol(PamSymbolType.SYMBOL_POINT, 3, 3, true, Color.blue, Color.blue);
		private PamSymbol lineSymbol = new PamSymbol(PamSymbolType.SYMBOL_POINT, 3, 3, true, Color.RED, Color.RED);
		public CorrelationPlot() {
			super(PamColor.PlOTWINDOW);
			setPreferredSize(preferredSize);
			CorrelationPlotMouse corrMouse = new CorrelationPlotMouse();
			addMouseListener(corrMouse);
			addMouseMotionListener(corrMouse);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}

		@Override
		public void paintComponent(Graphics g) {
			//anti aliasing
			Graphics2D g2 = (Graphics2D) g;
				  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_ON);
				  g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				      RenderingHints.VALUE_RENDER_QUALITY);
				  
			super.paintComponent(g);
			if (corrFunction == null) {
				return;
			}
//			if (corrMax == 0) {
//				return;
//			}
			Point pt = new Point();
			double yScale = 0.48 * getHeight() / 1.;
			int y0 = getHeight()/2;
			int x1, x2, y1, y2;
			int nP = corrFunction.length;
			x1 = (int) correlationAxes.samplesAxis.getPosition(timeOffset);
			g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
			g.drawLine(x1, 0, x1, getHeight());
			
			x1 = (int) correlationAxes.samplesAxis.getPosition(-nP/2);
			y1 = y0 - (int) (corrFunction[0]*yScale);
			pt.x = (int) correlationAxes.samplesAxis.getPosition(-nP/2);
			pt.y = y0 - (int) (corrFunction[0]*yScale);  
			lineSymbol.draw(g, pt);
			g.setColor(Color.RED);
			for (int i = 1; i < nP; i++) {
				x2 = (int) correlationAxes.samplesAxis.getPosition(i-nP/2);
				y2 = y0 - (int) (corrFunction[i]*yScale);
				g.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
				pt.x = x1;
				pt.y = y1; 
				lineSymbol.draw(g, pt);
			}
			if (interpolatedMaxima != null) {
				double[] t = interpolatedMaxima[0];
				double[] a = interpolatedMaxima[1];
				for (int i = 0; i < t.length; i++) {
					pt.x = (int) correlationAxes.samplesAxis.getPosition(t[i]-nP/2);
					pt.y = y0 - (int) (a[i]*yScale);  
					maxSymbol.draw(g, pt);
				}
			}
//			g.drawString(String.format("Max %3.2f", corrMax), 3, y0);
		}

	}

	public void redrawEverything() {
		waveformPlot.repaint(10);
		correlationPlot.repaint(10);
	}

}
