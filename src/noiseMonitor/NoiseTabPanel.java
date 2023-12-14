package noiseMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import noiseBandMonitor.NoiseBandSettings;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.BasicKeyItem;
import PamView.ColourArray;
import PamView.LineKeyItem;
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamTabPanel;
import PamView.PamColors.PamColor;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamRadioButton;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamController.soundMedium.GlobalMedium;

public class NoiseTabPanel implements PamTabPanel {

//	private NoiseControl noiseControl;
	private PamControlledUnit pamControlledUnit;
	
	private NoiseDisplaySettings noiseDisplaySettings = new NoiseDisplaySettings();

	private JPanel mainPanel;

	private PamPanel leftPanel; 

	private PamPanel centerPanel;

	private PlotAxesPanel plotAxesPanel;

	private PlotPanel plotPanel;
	
	private SpecPlotPanel specPlotPanel;

	private DataSelectionPanel dataSelectionPanel;

	private ChannelSelectionPanel channelSelectionPanel;

	private StatsSelectionPanel statsSelectionPanel;

	private PamAxis timeAxis, levelAxis;

	private PamAxis specLevelAxis;
	private PamAxis freqAxis;

	private SelectionChanged selectionChanged = new SelectionChanged();

	private PamScroller timeScroller;

	private NoiseObserver noiseObserver;

	//	private long displayStartMillis;

	private Color[][] channelColours;

	private PamSymbolType[] symbolTypes = {PamSymbolType.SYMBOL_CIRCLE, PamSymbolType.SYMBOL_SQUARE, 
			PamSymbolType.SYMBOL_TRIANGLEU, PamSymbolType.SYMBOL_TRIANGLED, PamSymbolType.SYMBOL_DIAMOND,
			PamSymbolType.SYMBOL_STAR, PamSymbolType.SYMBOL_TRIANGLEU};
	
	private int[] symbolTypeLUT = new int[6];

	public RangeSpinner rangeSpinner;

	private PamLabel startTime, mouseTime;

	private PamPanel timeLabelPanel;

	private SpecPlotAxesPanel specAxisPanel;

	private boolean isViewer;

	private NoiseDataBlock noiseDataBlock;

	private double[] bandCorrection;


	public NoiseTabPanel(PamControlledUnit pamControlledUnit, NoiseDataBlock noiseDataBlock) {
		this.pamControlledUnit = pamControlledUnit;
		this.noiseDataBlock = noiseDataBlock;
		
		PamSettingManager.getInstance().registerSettings(new DisplaySettingManager());
		
		mainPanel = new JPanel(new BorderLayout());
		leftPanel = new PamPanel(PamColor.BORDER);
		centerPanel = new PamPanel(PamColor.BORDER);
		JPanel centTopPanel = new PamPanel(PamColor.BORDER);
		JPanel centBotPanel = new PamPanel(PamColor.BORDER);

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (isViewer) {
			centerPanel.setLayout(new GridLayout(2, 1));
			centerPanel.add(centTopPanel);
			centerPanel.add(centBotPanel);
		}
		else {
			centerPanel.setLayout(new GridLayout(2, 1));
			centerPanel.add(centTopPanel);
			centerPanel.add(centBotPanel);
		}

		mainPanel.add(BorderLayout.WEST, leftPanel);
		mainPanel.add(BorderLayout.CENTER, centerPanel);

		centBotPanel.setLayout(new BorderLayout());
		centBotPanel.add(BorderLayout.CENTER, plotAxesPanel = new PlotAxesPanel());
		timeLabelPanel = new PamPanel(new BorderLayout());			
		//		timeLabelPanel.setBorder(new EmptyBorder(0, plotAxesPanel.getInsets().left+100, 0, 0));
		timeLabelPanel.add(BorderLayout.WEST, startTime = new PamLabel("   "));
		timeLabelPanel.add(BorderLayout.EAST, mouseTime = new PamLabel("  "));
		centBotPanel.add(BorderLayout.NORTH, timeLabelPanel);

//		if (!isViewer) {
			centTopPanel.setLayout(new BorderLayout());
			centTopPanel.add(BorderLayout.CENTER, specAxisPanel = new SpecPlotAxesPanel());
//		}

		dataSelectionPanel = new DataSelectionPanel();
		channelSelectionPanel = new ChannelSelectionPanel();
		statsSelectionPanel = new StatsSelectionPanel();
		JPanel topLeft = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		topLeft.setLayout(new BorderLayout());
		leftPanel.add(BorderLayout.NORTH, topLeft);
		topLeft.add(BorderLayout.NORTH, channelSelectionPanel);
		topLeft.add(BorderLayout.SOUTH, statsSelectionPanel);
		leftPanel.add(BorderLayout.CENTER, dataSelectionPanel);

		noiseObserver = new NoiseObserver();

		newSettings();

	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}


	private void selectionChanged() {
		if (noiseDisplaySettings.selectedChannels == null) {
			noiseDisplaySettings.selectedChannels = new boolean[PamConstants.MAX_CHANNELS];
		}
		
//		int nChan = PamUtils.getNumChannels(noiseDataBlock.getChannelMap());
		int nChan = PamUtils.getNumChannels(noiseDataBlock.getSequenceMap());
		for (int i = 0; i < nChan; i++) {
			noiseDisplaySettings.selectedChannels[channelSelectionPanel.channelLUT[i]] = channelSelectionPanel.checkBoxes[i].isSelected();
		}
		noiseDisplaySettings.selectedStats = 0;
		for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
			if (statsSelectionPanel.statsBoxes[i].isSelected()) {
				noiseDisplaySettings.selectedStats |= 1<<i;
			}
		}
		noiseDisplaySettings.selectedStats &= noiseDataBlock.getStatisticTypes();
		for (int i = 0; i < 2; i++) {
			if (statsSelectionPanel.bandBoxes[i].isSelected()) {
				noiseDisplaySettings.bandOption = i;
			}
		}
		
		int noiseStats = noiseDataBlock.getStatisticTypes();
		int nStats = PamUtils.getNumChannels(noiseStats);
		for (int i = 0; i < nStats; i++) {
			symbolTypeLUT[i] = PamUtils.getNthChannel(i, noiseStats);
		}

		for (int i = 0; i < dataSelectionPanel.checkBoxes.length; i++) {
			noiseDisplaySettings.setSelectData(i, dataSelectionPanel.checkBoxes[i].isSelected());
		}
		/*
		 * Now a bit of setting up of colours. 
		 */
		if (dataSelectionPanel.checkBoxes == null) {
			return;
		}
//		nChan = PamUtils.getHighestChannel(noiseDataBlock.getChannelMap());
		nChan = PamUtils.getHighestChannel(noiseDataBlock.getSequenceMap());
		Color c1, c2;
		channelColours = new Color[nChan+1][];
		int nThings = dataSelectionPanel.checkBoxes.length;
		for (int i = 0; i <= nChan; i++) {
			c1 = PamColors.getInstance().getChannelColor(i*2);
			c2 = PamColors.getInstance().getChannelColor(i*2+1);
			if (nThings == 1) {
				channelColours[i] = new Color[1];
				channelColours[i][0] = c1;
			}
			else {
				channelColours[i] = ColourArray.createMergedArray(nThings, c1, c2).getColours();
			}
		}
		
		int nBands = noiseDataBlock.getNumMeasurementBands();
		bandCorrection = new double[nBands];
		if (noiseDisplaySettings.bandOption == NoiseDisplaySettings.SPECTRUM_LEVEL) {
			double[] loE = noiseDataBlock.getBandLoEdges();
			double[] hiE = noiseDataBlock.getBandHiEdges();
			for (int i = 0; i < nBands; i++) {
				bandCorrection[i] = -10.*Math.log10(hiE[i]-loE[i]);
			}
		}
		
		setAxisLabels();

		plotPanel.createKey();

		plotAxesPanel.repaint();
		specPlotPanel.specPlotAxesPanel.repaint();
		plotPanel.repaint();
	}

private void setAxisLabels() {
		// TODO Auto-generated method stub
		String label;
		if (noiseDisplaySettings.bandOption == NoiseDisplaySettings.BAND_ENERGY) {
			String dBRef = GlobalMedium.getdBRefString(PamController.getInstance().getGlobalMediumManager().getCurrentMedium());
			label = "Band Energy (" + dBRef + ")\n";
		}
		else {
			label = "Spectrum Level (dB re \u00B5Pa/\u221AHz\n)";
		}
		levelAxis.setLabel(label);
		specLevelAxis.setLabel(label);
	}

//	private NoiseSettings getCurrentNoiseSettings() {
//		return noiseControl.noiseSettings;
//	}

	public void newSettings() {
//		NoiseSettings ns = getCurrentNoiseSettings();
		channelSelectionPanel.createCheckBoxes();
		statsSelectionPanel.setParams();
		dataSelectionPanel.createCheckBoxes();
		if (noiseDataBlock != null) {
			noiseDataBlock.addObserver(noiseObserver);
		}
		if (noiseDisplaySettings.autoScale == false) {
			levelAxis.setMinVal(noiseDisplaySettings.levelMin);
			levelAxis.setMaxVal(noiseDisplaySettings.levelMax);
			if (specLevelAxis != null) {
				specLevelAxis.setMinVal(noiseDisplaySettings.levelMin);
				specLevelAxis.setMaxVal(noiseDisplaySettings.levelMax);
			}
		}
		rangeSpinner.setSpinnerValue(noiseDisplaySettings.displayLengthSeconds);
		timeAxis.setRange(0, noiseDisplaySettings.displayLengthSeconds);
		timeScroller.setVisibleMillis(noiseDisplaySettings.displayLengthSeconds*1000);
		selectionChanged();
		autoFreqScale();
	}

	/**
	 * Convert a channel index into a channel number
	 * @param chanIndex channel index
	 * @return channel number
	 */
	private int channelIndexToNumber(int chanIndex) {
		if (channelSelectionPanel.channelLUT == null ||
				chanIndex >= channelSelectionPanel.channelLUT.length) {
			return chanIndex;
		}
		return channelSelectionPanel.channelLUT[chanIndex];
	}

	public void newData(NoiseDataUnit noiseData) {
		if (noiseDisplaySettings.autoScale) {
			setAutoScale();
		}
		plotPanel.repaint();
		if (specPlotPanel != null) {
			specPlotPanel.repaint(noiseData);
		}
	}

	private void setAutoScale() {
		
		autoFreqScale();
		
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		if (noiseDataBlock == null) {
			return;
		}
		Iterator<NoiseDataUnit> noiseIterator;
		NoiseDataUnit[] lastChanUnit = new NoiseDataUnit[PamConstants.MAX_CHANNELS];
		NoiseDataUnit aUnit, lastUnit;
		double[][] noiseData, lastNoisedata = null;
		int chan;
		int nMeasures = noiseDataBlock.getNumUsedStats();
		int nBands = noiseDataBlock.getNumMeasurementBands();
		int haveData = 0;
		synchronized (noiseDataBlock.getSynchLock()) {
			noiseIterator = noiseDataBlock.getListIterator(0);
			while (noiseIterator.hasNext()) {
				aUnit = noiseIterator.next();

				noiseData = aUnit.getNoiseBandData();
//				chan = PamUtils.getSingleChannel(aUnit.getChannelBitmap());
				chan = PamUtils.getSingleChannel(aUnit.getSequenceBitmap());
				if (noiseDisplaySettings.selectedChannels[chan] == false) {
//					continue;
				}
				nBands = noiseData.length;
				if (nBands > 0) {
					nMeasures = noiseData[0].length;
				}
				lastUnit = lastChanUnit[chan];
				for (int i = 0; i < nMeasures; i++) {
					if (noiseDisplaySettings.isSelectData(i) == false) {
//						continue;
					}
					for (int m = 0; m < nBands; m++) {
						max = Math.max(max, noiseData[m][i]+bandCorrection[m]);
						min = Math.min(min, noiseData[m][i]+bandCorrection[m]);
						haveData++;
					}
				}
			}
		}
		if (haveData < 2) {
			return;
		}
		max = 10 * Math.ceil(max * 1.01 / 10.);
		min = 10 * Math.floor(min / 1.01 / 10.);
		//		max = Math.pow(10., Math.ceil(Math.log10(max * 1.01)));
		//		min = Math.pow(10., Math.floor(Math.log10(min / 1.01)));
		if (noiseDisplaySettings.levelMin != min || noiseDisplaySettings.levelMax != max) {
			levelAxis.setMinVal(noiseDisplaySettings.levelMin = min);
			levelAxis.setMaxVal(noiseDisplaySettings.levelMax = max);
			if (specLevelAxis != null) {
				specLevelAxis.setMinVal(min);
				specLevelAxis.setMaxVal(max);
			}
		}
	}

	private void autoFreqScale() {
		if (freqAxis == null) {
			return;
		}
		double topFreq = noiseDataBlock.getHighestFrequency10();
		if (noiseDataBlock == null) {
			return;
		}
		topFreq = noiseDataBlock.getSampleRate()/2;
		freqAxis.setMinVal(noiseDataBlock.getLowestFrequency10());
		freqAxis.setMaxVal(topFreq);
	}

	private class NoiseObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return pamControlledUnit.getUnitName() + " Display";
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return noiseDisplaySettings.displayLengthSeconds * 1000;
		}

		private long lastUpdate;
		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			if (milliSeconds - lastUpdate > 1000 || milliSeconds < lastUpdate) {
				lastUpdate = milliSeconds;
				long displayStartMillis = milliSeconds - noiseDisplaySettings.displayLengthSeconds * 1000;
				if (displayStartMillis < PamCalendar.getSessionStartTime()) {
					displayStartMillis = PamCalendar.getSessionStartTime();
				}
				timeScroller.setRangeMillis(displayStartMillis, displayStartMillis + 
						noiseDisplaySettings.displayLengthSeconds*1000, true);
				timeScroller.setVisibleMillis(noiseDisplaySettings.displayLengthSeconds*1000);
				repaintPlots();
				sayStartTime(displayStartMillis);
			}
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			NoiseDataUnit noiseData = (NoiseDataUnit) arg;
			newData(noiseData);
		}
	}

	private class TimeRangeListener implements RangeSpinnerListener, PamScrollObserver {

		@Override
		public void valueChanged(double oldValue, double newValue) {
			noiseDisplaySettings.displayLengthSeconds = (long) newValue;
			timeAxis.setRange(0, newValue);
			repaintPlots();
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			if (noiseDisplaySettings.autoScale) {
				setAutoScale();
			}
			repaintPlots();
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			repaintPlots();
			//			plotAxesPanel.setTimeInsets();
			sayStartTime(pamScroller.getValueMillis());
		}

	}

	private class SpecPlotAxesPanel extends PamAxisPanel {

		public SpecPlotAxesPanel() {
			super();
			JPanel plotFrame = new JPanel(new BorderLayout());
			setPlotPanel(specPlotPanel = new SpecPlotPanel(this));
			setInnerPanel(plotFrame);
			plotFrame.add(BorderLayout.CENTER, specPlotPanel);
			specLevelAxis = new PamAxis(0, 1, 0, 1, noiseDisplaySettings.levelMin, 
					noiseDisplaySettings.levelMax, PamAxis.ABOVE_LEFT, 
					"Level (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3.0f");
			freqAxis = new PamAxis(0, 1, 0, 1, 1, 49000,
					PamAxis.BELOW_RIGHT, 
					"Frequency (Hz)", PamAxis.LABEL_NEAR_CENTRE, "%d");
			freqAxis.setLogScale(true);
			autoFreqScale();
			setWestAxis(specLevelAxis);
			setSouthAxis(freqAxis);
			
			setMinNorth(10);
			setMinEast(10);
			setMinSouth(10);
			setMinWest(10);
			setAutoInsets(true);
		}

		
	}

	private class SpecPlotPanel extends JPanelWithPamKey {

		private SpecPlotAxesPanel specPlotAxesPanel;
		private NoiseDataUnit[] latestNoise = new NoiseDataUnit[PamConstants.MAX_CHANNELS];
		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.RED, Color.RED);
		 
		public SpecPlotPanel(SpecPlotAxesPanel specPlotAxesPanel) {
			this.specPlotAxesPanel = specPlotAxesPanel;
			setBorder(BorderFactory.createBevelBorder(1));
			SpecMouse specMouse = new SpecMouse();
			addMouseMotionListener(specMouse);
			addMouseListener(specMouse);
		}

		public void repaint(NoiseDataUnit noiseData) {
//			int chan = PamUtils.getSingleChannel(noiseData.getChannelBitmap());
			int chan = PamUtils.getSingleChannel(noiseData.getSequenceBitmap());
			latestNoise[chan] = noiseData;
			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (noiseDisplaySettings.showGrid) {
				specLevelAxis.drawGrid(g, getSize(), getInsets(), 0);
				freqAxis.drawGrid(g, getSize(), getInsets(), 0);
			}
			if (latestNoise == null) {
				return;
			}
			if (noiseDataBlock == null) {
				return;
			}
//			sortScales();
			NoiseDataUnit aUnit;
			double[][] noiseData;
			int chan;
			int nBands = noiseDataBlock.getNumMeasurementBands();
			int x1, x2, x0, y[];
			y = new int[NoiseDataBlock.NNOISETYPES];
//			int hChan = PamUtils.getHighestChannel(noiseDataBlock.getChannelMap());
			int hChan = PamUtils.getHighestChannel(noiseDataBlock.getSequenceMap());
			int topWidth;
			double[] loEdges = noiseDataBlock.getBandLoEdges();
			double[] hiEdges = noiseDataBlock.getBandHiEdges();
			int nStats = noiseDataBlock.getNumUsedStats();
			if (noiseDisplaySettings.selectedChannels == null) {
				return;
			};
			for (int iChan = 0; iChan <= hChan; iChan++) {
//				if (((1<<iChan) & noiseDataBlock.getChannelMap()) == 0) {
				if (((1<<iChan) & noiseDataBlock.getSequenceMap()) == 0) {
					continue;
				}
				if (noiseDisplaySettings.selectedChannels[iChan] == false) {
					continue;
				}
				aUnit = latestNoise[iChan];
				if (aUnit == null) {
					continue;
				}
				noiseData = aUnit.getNoiseBandData();
				if (noiseData.length != nBands) {
					continue;
				}
				for (int i = 0; i < nBands; i++) {
					x1 = (int) freqAxis.getPosition(loEdges[i]);
					x2 = (int) freqAxis.getPosition(hiEdges[i]);
					x0 = (x1+x2)/2;
					nStats = noiseData[i].length;
					for (int iM = 0; iM < nStats; iM++) {
						y[iM] = (int) specLevelAxis.getPosition(noiseData[i][iM]+bandCorrection[i]);
					}
					setSymbol(symbol, iChan, i, 0);
					symbol.draw(g, new Point(x0, y[0]));
					if (nStats == 2) {
						// draw the maximum value. 
						topWidth = Math.min((x2-x1)/2, 10);
						g.drawLine(x0, y[0], x0, y[1]);
						g.drawLine(x0-topWidth, y[1], x0+topWidth, y[1]);
					}
					else {
						g.drawLine(x1, y[1], x2, y[1]);
						g.drawLine(x1, y[2], x2, y[2]);
						g.drawLine(x1, y[3], x2, y[3]);
						g.drawLine(x1, y[2], x1, y[3]);
						g.drawLine(x2, y[2], x2, y[3]);
						if (nStats == NoiseDataBlock.NNOISETYPES) {
							topWidth = Math.min((x2-x1)/2, 10);

							g.drawLine(x0, y[2], x0, y[4]);
							g.drawLine(x0-topWidth, y[4], x0+topWidth, y[4]);
							g.drawLine(x0, y[3], x0, y[5]);
							g.drawLine(x0-topWidth, y[5], x0+topWidth, y[5]);
						}
					}
				}
			}
		}
	}
	/**
	 * Axis for time series plot
	 * @author Doug
	 *
	 */
	private class PlotAxesPanel extends PamAxisPanel {
		public PlotAxesPanel() {
			super();
			timeScroller = new PamScroller(pamControlledUnit.getUnitName(), 
					AbstractPamScrollerAWT.HORIZONTAL, 1000, 15*60*1000, isViewer);
			timeScroller.setShowTimes(true);
			rangeSpinner = new RangeSpinner();
			TimeRangeListener trl = new TimeRangeListener();
			rangeSpinner.addRangeSpinnerListener(trl);
			timeScroller.addControl(rangeSpinner.getComponent());
			timeScroller.addObserver(rangeSpinner);
			timeScroller.addObserver(trl);
			timeScroller.addDataBlock(noiseDataBlock);
			JPanel plotFrame = new JPanel(new BorderLayout());
			plotPanel = new PlotPanel(this);
			plotFrame.add(BorderLayout.SOUTH, timeScroller.getComponent());
			plotFrame.add(BorderLayout.CENTER, plotPanel);
			setInnerPanel(plotFrame);
			setPlotPanel(plotPanel);
			levelAxis = new PamAxis(0, 1, 0, 1, noiseDisplaySettings.levelMin, 
					noiseDisplaySettings.levelMax, PamAxis.ABOVE_LEFT, 
					"Level (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3.0f");
			timeAxis = new PamAxis(0, 1, 0, 1, 0, noiseDisplaySettings.displayLengthSeconds,
					PamAxis.ABOVE_LEFT, 
					"Time (s)", PamAxis.LABEL_NEAR_CENTRE, "%d");
			setWestAxis(levelAxis);
			setNorthAxis(timeAxis);

			setMinNorth(10);
			setMinEast(10);
			setMinSouth(10);
			setMinWest(10);
			setAutoInsets(true);

		}

		@Override
		public void setBorder(Border arg0) {
			super.setBorder(arg0);
			//			setTimeInsets();
		}



		public boolean setTimeInsets() {
			if (timeLabelPanel != null) {
				Insets insets = getInsets();
				if (insets == null) {
					return false;
				}
				timeLabelPanel.setBorder(new EmptyBorder(3, insets.left, 0, insets.right));
				return (insets.left > 1);
			}
			return false;
		}

	}

	private void setSymbol(PamSymbol symbol, int chan, int iMeasure, int iStat) {
		
		try{
			symbol.setSymbol(symbolTypes[symbolTypeLUT[iStat]]);
			symbol.setLineColor(channelColours[chan][iMeasure]);
			symbol.setFillColor(channelColours[chan][iMeasure]);
		}catch(Exception e){
			symbol.setSymbol(PamSymbolType.SYMBOL_CIRCLE);
			symbol.setLineColor(Color.BLACK);
			symbol.setFillColor(Color.BLACK);
		}
	}

	/**
	 * Plot panel for time series plot
	 * @author Doug
	 *
	 */
	private class PlotPanel extends JPanelWithPamKey {

		private PlotAxesPanel plotAxesPanel;

		private double xScale;

		private double yScale, yStart;

		public PlotPanel(PlotAxesPanel plotAxesPanel) {
			//			super(PamColor.PlOTWINDOW);
			setBorder(BorderFactory.createBevelBorder(1));
			this.plotAxesPanel = plotAxesPanel;
			setKeyPosition(CornerLayoutContraint.FIRST_LINE_START);
			PlotMouse plotMouse = new PlotMouse();
			addMouseListener(plotMouse);
			addMouseMotionListener(plotMouse);
		}

		public void createKey() {
			KeyPanel keyPanel = new KeyPanel("Key", PamKeyItem.KEY_SHORT);
			PamKeyItem keyItem;
			/*
			 * Only need to do the symbols once for all four measurement type
			 * USe the colour of the first entry for this. 
			 */
			if (noiseDisplaySettings.selectedStats == 0 || noiseDisplaySettings.showKey == false) { 
				setKeyPanel(null);
				return;
			}
			if (channelColours == null || channelColours.length == 0) {
				return;
			}
			if (channelColours[0].length == 0) {
				return;
			}
			PamSymbol aSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, 
					channelColours[0][0], channelColours[0][0]);
			for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
				if ((1<<i & noiseDisplaySettings.selectedStats) != 0) {
					aSymbol.setSymbol(symbolTypes[i]);
					keyItem = new BasicKeyItem(aSymbol.clone(), NoiseDataBlock.measureNames[i]);
					keyPanel.add(keyItem);
				}
			}
//			int nChan = PamUtils.getNumChannels(noiseDataBlock.getChannelMap());
			int nChan = PamUtils.getNumChannels(noiseDataBlock.getSequenceMap());
			int chanNum;
			int nThings = dataSelectionPanel.checkBoxes.length;
			aSymbol.setSymbol(PamSymbolType.SYMBOL_LINESEGMENT);
			String txt;
			for (int iChan = 0; iChan < nChan; iChan++) {
				chanNum = channelIndexToNumber(iChan);
				if (noiseDisplaySettings.selectedChannels[chanNum] == false) {
					continue;
				}
				for (int iM = 0; iM < nThings; iM++) {
					if (noiseDisplaySettings.isSelectData(iM) == false) {
						continue;
					}
//					txt = String.format("Ch %d %s", channelSelectionPanel.channelLUT[iChan],
//							ns.getMeasurementBand(iM).getLongName());
					txt = String.format("Ch %d %s", channelSelectionPanel.channelLUT[iChan],
							noiseDataBlock.getBandLongName(iM));
					keyItem = new LineKeyItem(channelColours[iChan][iM], txt);
					keyPanel.add(keyItem);
				}
			}

			this.setKeyPanel(keyPanel);

		}


		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
//			long t1 = System.nanoTime();
			if (noiseDisplaySettings.showGrid) {
				levelAxis.drawGrid(g, getSize(), getInsets(), 0);
				timeAxis.drawGrid(g, getSize(), getInsets(), 0);
			}
			if (noiseDataBlock == null) {
				return;
			}
			sortScales();
			Iterator<NoiseDataUnit> noiseIterator;
			NoiseDataUnit[] lastChanUnit = new NoiseDataUnit[PamConstants.MAX_CHANNELS];
			NoiseDataUnit aUnit, lastUnit;
			double[][] noiseData, lastNoisedata = null;
			int chan;
			int nMeasures = noiseDataBlock.getNumMeasurementBands();
			int x1, x2=0, y1, y2;
			int xWin = getWidth() / 10;
			synchronized (noiseDataBlock.getSynchLock()) {
				noiseIterator = noiseDataBlock.getListIterator(0);
				int nStats = noiseDataBlock.getNumUsedStats();
				while (noiseIterator.hasNext()) {
					aUnit = noiseIterator.next();
					noiseData = aUnit.getNoiseBandData();
//					chan = PamUtils.getSingleChannel(aUnit.getChannelBitmap());
					chan = PamUtils.getSingleChannel(aUnit.getSequenceBitmap());
					if (noiseDisplaySettings.selectedChannels[chan] == false) {
						continue;
					}
					lastUnit = lastChanUnit[chan];
					x1 = timeToXPix(aUnit.getTimeMilliseconds());
					if (x1 < -xWin) {
						continue;
					}
					if (lastUnit != null) {
						x2 = timeToXPix(lastUnit.getTimeMilliseconds());
						lastNoisedata = lastUnit.getNoiseBandData();
					}
					nMeasures = noiseData.length;
					for (int i = 0; i < nMeasures; i++) {
						if (noiseDisplaySettings.isSelectData(i) == false) {
							continue;
						}
						for (int m = 0; m < nStats; m++) {
							if ((noiseDisplaySettings.selectedStats & noiseDataBlock.statIndexToBit(m)) == 0) {
								continue;
							}
							y1 = dBToYPix(noiseData[i][m]+bandCorrection[i]);
							setSymbol(symbol, chan, i, m);
							symbol.draw(g, new Point(x1, y1));
							if (lastNoisedata != null && lastNoisedata.length > i) {
								y2 = dBToYPix(lastNoisedata[i][m]+bandCorrection[i]);
								g.drawLine(x1, y1, x2, y2);
							}
						}
					}
					if (x1 > getWidth()) {
						break;
					}
					lastChanUnit[chan] = aUnit;
				}
			}
//			long t2 = System.nanoTime();
//			System.out.println(String.format("Noise draw took %3.1f ms", (double)(t2-t1) / 1000000.));
		}

		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.RED, Color.RED);

		private long displayStart;
		private void sortScales() {
			xScale = (double) getWidth() / (noiseDisplaySettings.displayLengthSeconds * 1000);
			displayStart = timeScroller.getValueMillis();
			yScale = getHeight() / (noiseDisplaySettings.levelMax-noiseDisplaySettings.levelMin);
			yStart = noiseDisplaySettings.levelMin;
		}

		private int timeToXPix(long timeMillis) {
			return (int) ((timeMillis - displayStart) * xScale);
		}

		private int dBToYPix(double dB) {
			return (int) (getHeight() - (dB - yStart)*yScale);
		}

	}

	private class SpecMouse extends MouseAdapter {
	

		@Override
		public void mouseExited(MouseEvent e) {
			sayMouseData(null, null);
		}
	
		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			saySpecMouseData(e.getX(), e.getY());
		}
		
	}

	private class PlotMouse extends MouseAdapter {

		@Override
		public void mouseExited(MouseEvent e) {
			sayMouseData(null, null);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			sayMouseData(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
			if (isViewer) {
				showMouseSpectrum(e.getX());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showMenu(e);
			}
			if (isViewer) {
				showMouseSpectrum(e.getX());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showMenu(e);
			}
		}

	}

	private class StatsSelectionPanel extends PamPanel {
		private JCheckBox[] statsBoxes = new JCheckBox[NoiseDataBlock.NNOISETYPES];
		private PamRadioButton[] bandBoxes = new PamRadioButton[2];
		private String[] bandNames = {"Band energy", "Spec' level"};
		StatsSelectionPanel() {
			super(PamColor.BORDER);
			setBorder(new TitledBorder("Stats selection"));
			JPanel statPanel = new JPanel();
			this.setLayout(new BorderLayout());
			add(BorderLayout.WEST, statPanel);
			statPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.insets = new Insets(0, 0, 0, 2);
			int usedStats = noiseDataBlock.getStatisticTypes();
			for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
				statsBoxes[i] = new PamCheckBox(NoiseDataBlock.displayNames[i] + " Level");
				if ((1<<i & usedStats) != 0) {
					statPanel.add(statsBoxes[i], c);
					c.gridy ++;
					statsBoxes[i].addActionListener(selectionChanged);
				}
				else {
					statsBoxes[i].setSelected(false);
				}
			}
			// now another panel with band options. 
			JPanel bPanel = new JPanel(new BorderLayout());
			JPanel btPanel = new JPanel(new GridBagLayout());
			this.add(BorderLayout.EAST, bPanel);
			bPanel.add(BorderLayout.NORTH, btPanel);
			c .gridy = c.gridx = 0;
			ButtonGroup bg = new ButtonGroup();
			BandChanged bandChanged = new BandChanged();
			for (int i = 0; i < 2; i++) {
				bandBoxes[i] = new PamRadioButton(bandNames[i]);
				btPanel.add(bandBoxes[i], c);
				bg.add(bandBoxes[i]);
				bandBoxes[i].addActionListener(bandChanged);
				c.gridy++;
			}
			
		}

		void setParams() {
			/**
			 * To sort out !
			 */
			for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
				if ((1<<i & noiseDisplaySettings.selectedStats) != 0) {
					statsBoxes[i].setSelected(true);
				}
				else {
					statsBoxes[i].setSelected(false);
				}
			}
			for (int i = 0; i < 2; i++) {
				bandBoxes[i].setSelected(noiseDisplaySettings.bandOption == i);
			}
//			if (noiseDisplaySettings.selectedStats != null && noiseDisplaySettings.selectedStats.length == 4) {
//				mean.setSelected(noiseDisplaySettings.selectedStats[0]);
//				median.setSelected(noiseDisplaySettings.selectedStats[1]);
//				lo95.setSelected(noiseDisplaySettings.selectedStats[2]);
//				hi95.setSelected(noiseDisplaySettings.selectedStats[3]);
//			}
		}
	}

	private class ChannelSelectionPanel extends PamPanel{
		JCheckBox[] checkBoxes;
		int[] channelLUT;
		public ChannelSelectionPanel() {
			super(PamColor.BORDER);
			setBorder(new TitledBorder("Channel selection"));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		public void createCheckBoxes() {
//			int channelMap = noiseDataBlock.getChannelMap();
			int channelMap = noiseDataBlock.getSequenceMap();
			int n = PamUtils.getNumChannels(channelMap);
			int iChan;
			removeAll();
			if (n == 0) {
				return;
			}
			checkBoxes = new PamCheckBox[n];
			channelLUT = new int[n];
			for (int i = 0; i < n; i++) {
				iChan = PamUtils.getNthChannel(i, channelMap);
				channelLUT[i] = iChan;
				checkBoxes[i] = new PamCheckBox(String.format("Channel %d", iChan));
				add(checkBoxes[i]);
				checkBoxes[i].addActionListener(selectionChanged);
			}
			if (noiseDisplaySettings.selectedChannels != null) {
				int nn = Math.min(checkBoxes.length, noiseDisplaySettings.selectedChannels.length);
				for (int i = 0; i < nn; i++) {
					checkBoxes[i].setSelected(noiseDisplaySettings.selectedChannels[channelIndexToNumber(i)]);
				}
			}
			revalidate();
		}
	}

	private class DataSelectionPanel extends PamPanel {

		private PamPanel buttonPanel;

		JButton selectAll, clearAll;

		JCheckBox[] checkBoxes;

		private JScrollPane scrollPane;

		DataSelectionPanel() {
			super(PamColor.BORDER);
			setBorder(new TitledBorder("Data selection"));
			setLayout(new BorderLayout());
			PamPanel topBit = new PamPanel(PamColor.BORDER);
			buttonPanel = new PamPanel(PamColor.BORDER);
			this.add(BorderLayout.NORTH, topBit);
			scrollPane = new JScrollPane(buttonPanel);
			//			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setPreferredSize(new Dimension(50, 300));
			this.add(BorderLayout.CENTER, scrollPane);
			topBit.setLayout(new FlowLayout(FlowLayout.LEFT));
			topBit.add(selectAll = new JButton("Select All"));
			topBit.add(clearAll = new JButton("Select None"));
			selectAll.addActionListener(new SelectAll(true));
			clearAll.addActionListener(new SelectAll(false));

			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		}

		/**
		 * Create a selection checkbox for every item in the left panel. 
		 */
		private void createCheckBoxes() {
			buttonPanel.removeAll();
			int nBands = noiseDataBlock.getNumMeasurementBands();
			NoiseMeasurementBand mb;
			checkBoxes = new JCheckBox[nBands];
			for (int i = 0; i < nBands; i++) {
				buttonPanel.add(checkBoxes[i] = new PamCheckBox(noiseDataBlock.getBandLongName(i)));
				checkBoxes[i].addActionListener(selectionChanged);
			}
			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i].setSelected(noiseDisplaySettings.isSelectData(i));
			}
			buttonPanel.revalidate();
		}

		private void selectAll(boolean b) {
			if (checkBoxes == null) {
				return;
			}
			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i].setSelected(b);
			}
			selectionChanged();
		}
		private class SelectAll implements ActionListener {

			private boolean select;
			public SelectAll(boolean b) {
				select = b;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				selectAll(select);
			}
		}

	}

	private class SelectionChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			selectionChanged();
		}

	}

	private class BandChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			bandChanged();
		}

	}

	public void repaintPlots() {
		if (specAxisPanel != null) {
			specAxisPanel.repaint();
			specPlotPanel.repaint();
		}
		plotAxesPanel.repaint();
		plotPanel.repaint();
	}

	public void bandChanged() {
		selectionChanged();		
	}

	//	private boolean firstStartTime = false;
	public void sayStartTime(long valueMillis) {
		//		if (!firstStartTime) {
		//			firstStartTime = plotAxesPanel.setTimeInsets();
		//		}
		startTime.setText(String.format("Start %s", 
				PamCalendar.formatDateTime(valueMillis)));
		sayMouseData(lastMouseX, lastMouseY);
	}

	private Integer lastMouseX, lastMouseY;
	public void sayMouseData(Integer x, Integer y) {
		//		if (!firstStartTime) {
		//			return;
		//		}
		lastMouseX = x;
		lastMouseY = y;
		if (x == null) {
			mouseTime.setText("");
		}
		else {
			long t = (long) (x / plotPanel.xScale) + timeScroller.getValueMillis();
			double db = (plotPanel.getHeight()-y) / plotPanel.yScale + plotPanel.yStart;
			mouseTime.setText(String.format("Mouse %s, %3.1f dB  ", 
					PamCalendar.formatDateTime(t), db));
		}
	}

	private void saySpecMouseData(int x, int y) {
		double freq = freqAxis.getDataValue(x);
		double amp = specLevelAxis.getDataValue(y);
		mouseTime.setText(String.format("Mouse %s, %3.1fdB  ", 
				FrequencyFormat.formatFrequency(freq, true), amp));
	}

	/**
	 * Invoked when the mouse is dragged on the time display - finds the 
	 * closest data unit and displays it in the detail spectrum window. 
	 * @param x mouse x position. 
	 */
	private void showMouseSpectrum(int x) {
		long t = (long) (x / plotPanel.xScale) + timeScroller.getValueMillis();
		if (noiseDataBlock == null) {
			return;
		}
		NoiseDataUnit du;
		// may be multiple channels to find !
//		int nChan = PamUtils.getNumChannels(noiseDataBlock.getChannelMap());
		int nChan = PamUtils.getNumChannels(noiseDataBlock.getSequenceMap());
		int aChan;
		for (int i = 0; i < nChan; i++) {
//			aChan = PamUtils.getNthChannel(i, noiseDataBlock.getChannelMap());
			aChan = PamUtils.getNthChannel(i, noiseDataBlock.getSequenceMap());
			du = noiseDataBlock.getClosestUnitMillis(t, 1<<aChan);
			if (du != null) {
				specPlotPanel.repaint(du);
			}
		}
		
		
	}

	private JPopupMenu mouseMenu;
	public void showMenu(MouseEvent e) {

		if (mouseMenu == null) {
			mouseMenu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Display Options ...");
			menuItem.addActionListener(new DisplayOptions(pamControlledUnit.getGuiFrame()));
			mouseMenu.add(menuItem);

		}
		mouseMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	class DisplayOptions implements ActionListener {

		private Frame frame;

		/**
		 * @param frame
		 */
		public DisplayOptions(Frame frame) {
			super();
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			displayOptions(frame);
		}
	}

	public void displayOptions(Frame parentFrame) {
		NoiseDisplaySettings ns = NoiseDisplayDialog.showDialog(parentFrame, noiseDisplaySettings);
		if (ns != null) {
			noiseDisplaySettings = ns.clone();
			newSettings();
		}
	}

	class DisplaySettingManager implements PamSettings {

		@Override
		public Serializable getSettingsReference() {
			return noiseDisplaySettings;
		}

		@Override
		public long getSettingsVersion() {
			return NoiseDisplaySettings.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return pamControlledUnit.getUnitName();
		}

		@Override
		public String getUnitType() {
			return "Tab Panel Settings";
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			noiseDisplaySettings = ((NoiseDisplaySettings) pamControlledUnitSettings.getSettings()).clone();
			return noiseDisplaySettings != null;
		}
		
	}

}
