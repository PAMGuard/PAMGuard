package noiseBandMonitor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BandCombineOp;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Filters.ANSIStandard;
import Filters.ButterworthMethod;
import Filters.FIRFilterMethod;
import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IIRFilterMethod;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;

public class NoiseBandDialog extends PamDialog {

	private static NoiseBandDialog singleInstance;

	private NoiseBandSettings noiseBandSettings;

	private NoiseBandControl noiseBandControl;

	private BodePlotAxis bodePlotAxis;

	private BodePlotGraph bodePlotGraph;

	private JCheckBox logFreqScale;
	
	private JCheckBox showGrid;
	
	private JCheckBox showDecimators;
	
	private JCheckBox[] showStandard = new JCheckBox[3];

	private SourcePanel sourcePanel;

	private PamDataBlock rawDatablock;

	private JTextField topOctave, bottomOctave;
//
//	private JRadioButton octave, thirdOctave;
	private JComboBox<BandType> bandType;
	
	private SpinnerNumberModel topBandSpinner;

	private SpinnerNumberModel filterOrder;

	private JTextField filterGamma;

	private SpinnerNumberModel nDecimators;

	private JComboBox filterType;
	
	private FilterPropertyTable filterPropertyTable;
	
	private JTextField outputInterval;
	

	private boolean setupComplete = false;

	ArrayList<FilterMethod> decimationFilters = new ArrayList<FilterMethod>();
	ArrayList<FilterMethod> bandFilters = new ArrayList<FilterMethod>();
	int[] decimatorIndexes;

	BandData bandData;

	private double[] axisGains = {-80, -10, -120};

	private BandAnalyser bandAnalyser;

	private int selectedBand = -1;
	
	private int selectedDecimator = -1;

	private NoiseBandDialog(Window parentFrame, NoiseBandControl noiseBandControl) {
		super(parentFrame, noiseBandControl.getUnitName(),true);
		this.noiseBandControl = noiseBandControl;
		bodePlotAxis = new BodePlotAxis();
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, bodePlotAxis);

		JPanel paramsPanel = new JPanel();
		mainPanel.add(BorderLayout.WEST, paramsPanel);
		paramsPanel.setLayout(new BorderLayout());
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		paramsPanel.add(BorderLayout.NORTH, optionsPanel);

		sourcePanel = new SourcePanel(this, "Raw Data Source", RawDataUnit.class, true, true);
		sourcePanel.addSelectionListener(new SourcePanelListener());
		optionsPanel.add(sourcePanel.getPanel());
		
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new TitledBorder("Output"));
		outputPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(outputPanel, new JLabel("Output Interval ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(outputPanel, outputInterval = new JTextField(5), c);
		c.gridx++;
		addComponent(outputPanel, new JLabel(" s", JLabel.RIGHT), c);
		optionsPanel.add(outputPanel);

		JPanel bandPanel = new JPanel();
		bandPanel.setLayout(new GridBagLayout());
		optionsPanel.add(bandPanel);
		bandPanel.setBorder(new TitledBorder("Measurement Bands"));
		c = new PamGridBagContraints();
//		addComponent(bandPanel, octave = new JRadioButton("Octave"), c);
//		c.gridx++;
//		c.gridwidth = 2;
//		addComponent(bandPanel, thirdOctave = new JRadioButton("Third Octave"), c);
//		c.gridwidth = 1;
//		ButtonGroup bbGroup = new ButtonGroup();
//		bbGroup.add(octave);
//		bbGroup.add(thirdOctave);
//		octave.addActionListener(bl);
//		thirdOctave.addActionListener(bl);
		addComponent(bandPanel, new JLabel("Band Type: ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(bandPanel, bandType = new JComboBox<BandType>(), c);
		bandType.addItem(BandType.OCTAVE);
		bandType.addItem(BandType.THIRDOCTAVE);
//		bandType.addItem(BandType.TWELTHOCTAVE);
		c.gridwidth = 1;
		BandListener bl = new BandListener();
		bandType.addActionListener(bl);
		c.gridx = 0;
		c.gridy++;
		topBandSpinner = new SpinnerNumberModel(30, 0, 30, 1);
		topBandSpinner.addChangeListener(new TopBandListener());
		addComponent(bandPanel, new JLabel("Top band number "), c);
		c.gridx++;
		addComponent(bandPanel, new JSpinner(topBandSpinner) , c);
		JButton maxButton = new JButton("Max");
		maxButton.addActionListener(new MaxButton());
		c.gridx++;
		addComponent(bandPanel, maxButton, c);
		c.gridx = 0;
		c.gridy++;
		addComponent(bandPanel, new JLabel("Number of decimators ", JLabel.RIGHT), c);
		c.gridx++;
		nDecimators = new SpinnerNumberModel(2, 0, 20, 1);
		nDecimators.addChangeListener(new NDEcimatorsListener());
		addComponent(bandPanel, new JSpinner(nDecimators), c);
		

		JPanel filtPanel = new JPanel();
		optionsPanel.add(filtPanel);
		filtPanel.setLayout(new GridBagLayout());
		filtPanel.setBorder(new TitledBorder("Filters"));
		c = new PamGridBagContraints();
		addComponent(filtPanel, new JLabel("Filter Type ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(filtPanel, filterType = new JComboBox(), c);
		filterType.addActionListener(new FilterTypeListener());
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(filtPanel, new JLabel("Filter Order ", JLabel.RIGHT), c);
		c.gridx++;
		filterOrder = new SpinnerNumberModel(2, 2, 20, 1);
		filterOrder.addChangeListener(new FilterOrderListener());
		addComponent(filtPanel, new JSpinner(filterOrder), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(filtPanel, new JLabel("Filter Gamma ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(filtPanel, filterGamma = new JTextField(), c);
		filterType.addItem("Butterworth");
		filterType.addItem("FIR Filter");

		filterPropertyTable = new FilterPropertyTable(noiseBandControl, this);
		paramsPanel.add(BorderLayout.CENTER, filterPropertyTable.getMainPanel());

		setSize(800, 500);
		setDialogComponent(mainPanel);
		setResizable(true);
		
		setHelpPoint("sound_processing.NoiseBands.Docs.NoiseBands");
	}

	public static NoiseBandSettings showDialog(Window parentFrame, NoiseBandControl noiseBandControl) {

		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.noiseBandControl != noiseBandControl) {
			singleInstance = new NoiseBandDialog(parentFrame, noiseBandControl);
		}
		singleInstance.setupComplete = false;
//		System.out.println("********************************************************");
//		System.out.println("NOISE BAND DIALOG: " + 	singleInstance.noiseBandSettings.rawDataSource);
//		System.out.println("********************************************************");
		singleInstance.noiseBandSettings = noiseBandControl.noiseBandSettings.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.noiseBandSettings;
	}

	private void setParams() {
		switch(noiseBandSettings.filterType) {
		case BUTTERWORTH:
			filterType.setSelectedIndex(0);
			break;
		case FIRWINDOW:
			filterType.setSelectedIndex(1);
			break;
		}
		sourcePanel.setSource(noiseBandControl.getNoiseBandProcess().getSourceDataBlock());
		sourcePanel.setChannelList(noiseBandSettings.channelMap);
		filterGamma.setText(Double.toString(noiseBandSettings.firGamma));
		
		outputInterval.setText(new Integer(noiseBandSettings.outputIntervalSeconds).toString());
		
//		octave.setSelected(noiseBandSettings.bandType == BandType.OCTAVE);
//		thirdOctave.setSelected(noiseBandSettings.bandType == BandType.THIRDOCTAVE);
		bandType.setSelectedItem(noiseBandSettings.bandType);
		topBandSpinner.setValue(noiseBandSettings.highBandNumber);
		
		nDecimators.setValue(noiseBandSettings.endDecimation);
		setFilterParams(noiseBandSettings.filterType);
		logFreqScale.setSelected(noiseBandSettings.logFreqScale);
		showGrid.setSelected(noiseBandSettings.showGrid);
		showDecimators.setSelected(noiseBandSettings.showDecimators);
		for (int i = 0; i < 3; i++) {                          
			showStandard[i].setSelected(noiseBandSettings.getShowStandard(i));
		}

		setupComplete = true;

		findDataSource();
		repaintAll();
	}

	private void setFilterParams(FilterType ft) {
		if (filterOrder != null && ft != null && noiseBandSettings != null) {
			filterOrder.setValue(ft == FilterType.BUTTERWORTH ? noiseBandSettings.iirOrder : noiseBandSettings.firOrder);
		}
	}

	@Override
	public boolean getParams() {
		if (!setupComplete) {
			return false;
		}
		PamDataBlock dataBlock = sourcePanel.getSource();
		if (dataBlock == null) {
			return showWarning("You must select a source of raw audio data");
		}
		noiseBandSettings.rawDataSource = dataBlock.getLongDataName();
		noiseBandSettings.channelMap = sourcePanel.getChannelList();
		if (noiseBandSettings.channelMap == 0) {
			return showWarning("You must select at least one data channel");
		}
		noiseBandSettings.filterType = getFilterType();
		switch(noiseBandSettings.filterType) {
		case BUTTERWORTH:
			noiseBandSettings.iirOrder = (Integer) filterOrder.getValue();
			if (noiseBandSettings.iirOrder < 4) {
				showWarning("The IIR filter order should be at least 4 for accurate measurement");
			}
			if (noiseBandSettings.iirOrder%2 == 1) {
				return showWarning("The IIR filter order nust be even, 6 or greater recommended");
			}
			break;
		case FIRWINDOW:
			noiseBandSettings.firOrder = (Integer) filterOrder.getValue();
			try {
				noiseBandSettings.firGamma = Double.valueOf(filterGamma.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid value for FIR Filter Gamma");
			}
			break;
		default:
			return showWarning("Invalid filter type selection");
		}
		noiseBandSettings.endDecimation = (Integer) nDecimators.getValue();
		noiseBandSettings.logFreqScale = logFreqScale.isSelected();
		noiseBandSettings.showGrid = showGrid.isSelected();
		noiseBandSettings.showDecimators = showDecimators.isSelected();
		for (int i = 0; i < 3; i++) {                          
			noiseBandSettings.setShowStandard(i, showStandard[i].isSelected());
		}
		noiseBandSettings.bandType = (BandType) bandType.getSelectedItem();
//		if (octave.isSelected()) {
//			noiseBandSettings.bandType = BandType.OCTAVE;
//		}
//		else {
//			noiseBandSettings.bandType = BandType.THIRDOCTAVE;
//		}
		noiseBandSettings.highBandNumber = (Integer) topBandSpinner.getValue();
		try {
			noiseBandSettings.outputIntervalSeconds = Integer.valueOf(outputInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid output interval");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		noiseBandSettings = null;
	}


	@Override
	public void restoreDefaultSettings() {
		noiseBandSettings = new NoiseBandSettings();
		setParams();
	}

	private class BodePlotAxis extends PamAxisPanel {

		protected PamAxis xAxis, yAxis;
		public BodePlotAxis() {
			super();
			xAxis = new PamAxis(0, 0, 10, 10, 0, 1000,	PamAxis.BELOW_RIGHT, "Frequency", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
			yAxis = new PamAxis(0, 0, 10, 10, -80, 0, PamAxis.ABOVE_LEFT, "Gain (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
			setSouthAxis(xAxis);
			setWestAxis(yAxis);
			setAutoInsets(true);
			PamBorderPanel innerPanel = new PamBorderPanel();
			innerPanel.setLayout(new BorderLayout());

			JPanel northPanel = new JPanel();
			northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			logFreqScale = new JCheckBox("Log Scale");
			logFreqScale.addActionListener(new DisplayOptionListener());
			northPanel.add(logFreqScale);
			northPanel.add(showGrid = new JCheckBox("Show Grid"));
			showGrid.addActionListener(new DisplayOptionListener());
			northPanel.add(showDecimators = new JCheckBox("Show Decimators"));
			showDecimators.addActionListener(new DisplayOptionListener());
			northPanel.add(new JLabel("    Show ANSI standards:"));
			for (int i = 0; i < 3; i++) {
				northPanel.add(showStandard[i] = new JCheckBox(String.format("Class %d", i)));
				showStandard[i].addActionListener(new DisplayOptionListener());
			}
			innerPanel.add(BorderLayout.NORTH, northPanel);

			bodePlotGraph = new BodePlotGraph();
			innerPanel.add(BorderLayout.CENTER, bodePlotGraph);
			setPlotPanel(bodePlotGraph);
			setInnerPanel(innerPanel);

			addMouseListener(new BodePlotAxisMouse());
		}


	}
	private class BodePlotAxisMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				toggleGain();
			}
		}

	}
	private class BodePlotGraph extends JPanelWithPamKey {

		Stroke bandStroke, selBandStroke;
		Stroke ansiStroke, singleStroke;
		Color[] standardColours = {Color.MAGENTA, Color.CYAN, Color.ORANGE};
		
		public BodePlotGraph() {
			super();
			setBorder(BorderFactory.createBevelBorder(1));
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(800, 650));
			singleStroke = new BasicStroke(1);
			bandStroke = new BasicStroke(2);
			selBandStroke = new BasicStroke(3);
			float[] dashes = {2, 2};
			ansiStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0);
			BodePlotMouse mouse = new BodePlotMouse();
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
		}


		@Override
		public void paint(Graphics g) {
			super.paint(g);

			if (showGrid.isSelected()) {
			bodePlotAxis.xAxis.drawGrid(g, getSize(), getInsets(), logFreqScale.isSelected()?1:0);
			bodePlotAxis.yAxis.drawGrid(g, getSize(), getInsets(), 0);
			}

			if (showDecimators.isSelected()) {
				paintDecimators(g);
			}
			paintStandard(g);
//			paintBands(g);
			paintBandAnalysis(g);
		}

		private void paintBandAnalysis(Graphics g) {
			if (bandAnalyser == null) {
				return;
			}
			BandPerformance[] bandPerformances = bandAnalyser.getBandPerformances();
			if (bandPerformances == null) {
				return;
			}
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(Color.blue);
			for (int i = 0; i < bandPerformances.length; i++) {
				if (i == selectedBand) {
					g2d.setStroke(selBandStroke);
				}
				else {
					g2d.setStroke(bandStroke);
				}
				paintBandPerformance(g, bandPerformances[i]);
			}
		}


		private void paintBandPerformance(Graphics g,
				BandPerformance bandPerformance) {
			double[] f = bandPerformance.getFrequencyList();
			double[] gain = bandPerformance.getGainListdB();
			int x1, x2, y1, y2;
			for (int i = 1; i < f.length; i++) {
				x1 = (int) bodePlotAxis.xAxis.getPosition(f[i-1]);
				x2 = (int) bodePlotAxis.xAxis.getPosition(f[i]);
				y1 = (int) bodePlotAxis.yAxis.getPosition(gain[i-1]);
				y2 = (int) bodePlotAxis.yAxis.getPosition(gain[i]);
				g.drawLine(x1, y1, x2, y2);
			}
		}


		private void paintDecimators(Graphics g) {
			if (decimationFilters == null) {
				return;
			}
			g.setColor(Color.red);
			Graphics2D g2d = (Graphics2D) g;
			int iDecimator = 0;
			for (FilterMethod aMethod:decimationFilters) {
				if (iDecimator == selectedDecimator) {
					g2d.setStroke(selBandStroke);
				}
				else {
					g2d.setStroke(singleStroke);
				}
				paintFilter(g, aMethod);
				int xPos = (int) bodePlotAxis.xAxis.getPosition(aMethod.getSampleRate()/4);
				int yPos = (int) bodePlotAxis.yAxis.getPosition(0);
				g.drawLine(xPos, yPos, xPos, yPos+getHeight()/20);
				iDecimator++;
			}
		}
		
		private void paintBands(Graphics g) {
			if (bandFilters == null) {
				return;
			}
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(bandStroke);
			g.setColor(Color.blue);
			for (FilterMethod aMethod:bandFilters) {
				paintFilter(g, aMethod);
			}
		}
		
		private void paintFilter(Graphics g, FilterMethod aFilter) {

			float sampleRate;
			double freqVal;
			int maxPixel;
			int gainPixel;
			Integer prevGainPixel = null;
			PamAxis xAxis = bodePlotAxis.xAxis;
			PamAxis yAxis = bodePlotAxis.yAxis;
			double gainVal, filterConstant;
			sampleRate = (float) aFilter.getSampleRate();
			maxPixel = (int) xAxis.getPosition(sampleRate/2.);
			filterConstant = aFilter.getFilterGainConstant();
			for (int iPix = 0; iPix <= maxPixel; iPix++) {
				freqVal = xAxis.getDataValue(iPix);
				gainVal = aFilter.getFilterGain(freqVal / sampleRate * Math.PI * 2) / filterConstant;
				if (gainVal > 0) {
					gainVal = 20.*Math.log10(gainVal);
				}
				gainPixel = (int) yAxis.getPosition(gainVal);
				if (prevGainPixel != null) {
					g.drawLine(iPix-1, prevGainPixel, iPix, gainPixel);
				}
				prevGainPixel = gainPixel;
			}
		}


		public void paintStandard(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;
			// dashed stroke is VERY slow to draw
//			g2d.setStroke(ansiStroke);
			int iBand = 0;
			g2d.setColor(getStandardColor(0));
			if (bandFilters == null) {
				return;
			}
			for (FilterMethod aMethod:bandFilters) {
				if (iBand == selectedBand) {
					g2d.setStroke(selBandStroke);
				}
				else {
					g2d.setStroke(singleStroke);
				}
//				g2d.setColor(getStandardColor(iBand++));
				paintStandard(g, aMethod);
				iBand++;
			}
		}
		
		private Color getStandardColor(int iBand) {
			return standardColours[iBand%standardColours.length];
		}


		private void paintStandard(Graphics g, FilterMethod aMethod) {
			double centerFrequency = aMethod.getFilterParams().getCenterFreq();
			double[] relFreq = ANSIStandard.getRelFreq(noiseBandSettings.bandType);
			if (relFreq == null) {
				return;
			}
			double[] minAtten;
			double[] maxAtten;
			int x1, x2, y1, y2;
			PamAxis xAxis = bodePlotAxis.xAxis;
			PamAxis yAxis = bodePlotAxis.yAxis;
			for (int i = 0; i < 3; i++) {
				if (showStandard[i].isSelected() == false) {
					continue;
				}
				minAtten = ANSIStandard.getMinAttenuation(i);
				maxAtten = ANSIStandard.getMaxAttenuation(i);
				for (int f = 1; f < relFreq.length; f++) {
					x1 = (int) (xAxis.getPosition(centerFrequency * relFreq[f-1]));
					x2 = (int) (xAxis.getPosition(centerFrequency * relFreq[f]));
					y1 = (int) yAxis.getPosition(-minAtten[f-1]);
					y2 = (int) yAxis.getPosition(-minAtten[f]);
					g.drawLine(x1, y1, x2, y2);
				}
				for (int f = 1; f < relFreq.length; f++) {
					x1 = (int) (xAxis.getPosition(centerFrequency / relFreq[f-1]));
					x2 = (int) (xAxis.getPosition(centerFrequency / relFreq[f]));
					y1 = (int) yAxis.getPosition(-minAtten[f-1]);
					y2 = (int) yAxis.getPosition(-minAtten[f]);
					g.drawLine(x1, y1, x2, y2);
				}
				for (int f = 1; f < relFreq.length; f++) {
					x1 = (int) (xAxis.getPosition(centerFrequency * relFreq[f-1]));
					x2 = (int) (xAxis.getPosition(centerFrequency * relFreq[f]));
					y1 = (int) yAxis.getPosition(-maxAtten[f-1]);
					y2 = (int) yAxis.getPosition(-maxAtten[f]);
					g.drawLine(x1, y1, x2, y2);
				}
				for (int f = 1; f < relFreq.length; f++) {
					x1 = (int) (xAxis.getPosition(centerFrequency / relFreq[f-1]));
					x2 = (int) (xAxis.getPosition(centerFrequency / relFreq[f]));
					y1 = (int) yAxis.getPosition(-maxAtten[f-1]);
					y2 = (int) yAxis.getPosition(-maxAtten[f]);
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}

	}
	
	private class BodePlotMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			findResponseLine(e);
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			findResponseLine(e);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			findResponseLine(e);
		}
	}

	private class DisplayOptionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			repaintAll();
		}
	}
	private class SourcePanelListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (setupComplete == false) {
				return;
			}
			findDataSource();
			repaintAll();
			enableControls();
		}


	}
	private class FilterTypeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (setupComplete == false) {
				return;
			}
			FilterType ft = getFilterType();
			setFilterParams(ft);
			repaintAll();
			enableControls();
		}
	}
	private class FilterOrderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (setupComplete == false) {
				return;
			}
			repaintAll();

		}

	}
	private class NDEcimatorsListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (setupComplete == false) {
				return;
			}
			noiseBandSettings.endDecimation = (Integer) nDecimators.getValue();
			repaintAll();
		}

	}
	private class TopBandListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			repaintAll();
		}

	}

	private class MaxButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			topBandSpinner.setValue(topBandSpinner.getMaximum());
		}
	}
	private class BandListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			findDataSource();
			repaintAll();
		}
	}

	private void enableControls() {
		filterGamma.setEnabled(getFilterType() == FilterType.FIRWINDOW);
	}

	/*
	 * Try to find the nearest response line and highlight it. 
	 */
	public void findResponseLine(MouseEvent e) {
//		double f = bodePlotAxis.xAxis.getDataValue(e.getX());
//		double g = bodePlotAxis.yAxis.getDataValue(e.getY());
//		g = Math.pow(10., g/20.);
		if (bandAnalyser == null) {
			setSelectedBand(-1);
			return;
		}
		BandPerformance[] bfs = bandAnalyser.getBandPerformances();
		if (bfs == null) {
			setSelectedBand(-1);
			return;
		}
		double[] f;
		double[] g;
		int closestBand = -1;
		double closestDist = Integer.MAX_VALUE;
		double dist;
		double x, y;
		double ex = e.getX();
		double ey = e.getY();
		for (int b = 0; b < bfs.length; b++) {
			f = bfs[b].getFrequencyList();
			g = bfs[b].getGainListdB();
			for (int i = 0; i < f.length; i++) {
				x = bodePlotAxis.xAxis.getPosition(f[i]);
				y = bodePlotAxis.yAxis.getPosition(g[i]);
				dist = (x-ex)*(x-ex) + (y-ey)*(y-ey);
				if (dist < closestDist) {
					closestDist = dist;
					closestBand = b;
				}
			}
		}
		if (closestBand >= 0 && closestDist <= 100) {
			setSelectedBand(closestBand);
		}
		else {
			setSelectedBand(-1);
		}
	}

	protected void setSelectedBand(int iBand) {
		if (iBand != selectedBand) {
			selectedBand = iBand;
			bodePlotGraph.repaint();
			selectedDecimator = -1;
			if (decimatorIndexes != null) {
				if (selectedBand >= 0) {
					selectedDecimator = decimatorIndexes[selectedBand];
				}
			}
		}
	}

	FilterType getFilterType() {
		int filterInd = filterType.getSelectedIndex();
		switch(filterInd) {
		case 0:
			return FilterType.BUTTERWORTH;
		default:
			return FilterType.FIRWINDOW;
		}
	}

	public void repaintAll() {
		if (this.setupComplete == false) {
			return;
		}
		if (getParams() == false) {
			return;
		}
		makeFilters();
		bandAnalyser = new BandAnalyser(noiseBandControl, getSampleRate(), noiseBandSettings);
		bandAnalyser.calculatePerformance();
		filterPropertyTable.setBandPerformances(noiseBandSettings, bandAnalyser.getBandPerformances());
		setAxisScales();
		bodePlotGraph.repaint();
		bodePlotAxis.repaint();
	}

	private void makeFilters() {
		decimationFilters = noiseBandControl.makeDecimatorFilters(noiseBandSettings, getSampleRate());
		bandFilters = noiseBandControl.makeBandFilters(noiseBandSettings, decimationFilters, getSampleRate());
		decimatorIndexes = noiseBandControl.getDecimatorIndexes();
	}
	

	private void setAxisScales() {
		bodePlotAxis.yAxis.setRange(axisGains[noiseBandSettings.scaleToggleState], 1);
		bodePlotAxis.xAxis.setLogScale(logFreqScale.isSelected());
		double minFreq = 0;
		if (logFreqScale.isSelected()) {
			minFreq = getSampleRate()/2/Math.pow(2,Math.max(noiseBandSettings.endDecimation+1,1));
			minFreq = Math.floor(Math.log10(minFreq));
			minFreq = Math.pow(10., minFreq);
		}
		bodePlotAxis.xAxis.setRange(minFreq, getSampleRate()/2);
	}


	public void toggleGain() {
		if (++noiseBandSettings.scaleToggleState >= axisGains.length) {
			noiseBandSettings.scaleToggleState = 0;
		}
		repaintAll();
	}

	private void findDataSource() {
		rawDatablock = sourcePanel.getSource();
		if (rawDatablock != null) {
			// set up the top band listener.
			BandType b = (BandType) bandType.getSelectedItem();
			double bandRatio = b.getBandRatio();
			int bandStep = b.getBandStep();
			
//			if (octave.isSelected()) {
//				bandRatio = Math.pow(2,1./2.);
//				bandStep = 3;
//			}
//			else {
//				bandRatio = Math.pow(2,1./6.);
//				bandStep = 1;
//			}
			double highestEdge = rawDatablock.getSampleRate() / 2 / bandRatio;
			int highestBand = BandData.getLowBandNumber(highestEdge);
			double highestCentre = BandData.calcFreq(highestBand);
			highestBand -= highestBand%bandStep;
			topBandSpinner.setMaximum(highestBand);
			topBandSpinner.setStepSize(bandStep);
			int currVal = (Integer) topBandSpinner.getValue();
			if (currVal > highestBand) {
				topBandSpinner.setValue(highestBand);
			}
			else if (currVal%bandStep != 0) {
				topBandSpinner.setValue(currVal - currVal%bandStep);
			}
			

//			System.out.println(String.format("HighestBand edge %3.1fHz, number %d, centre %3.1fHz", 
//					highestEdge, highestBand, highestCentre));
		}
	}

	private float getSampleRate() {
		if (rawDatablock != null) {
			return rawDatablock.getSampleRate();
		}
		return 1.f;
	}
}
