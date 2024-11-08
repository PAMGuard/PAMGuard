package noiseOneBand;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ListIterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.SymbolKeyItem;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
/**
 * Display panel for dBHt data - can be incorporated into a spectrogram 
 * plug in or a stand alone display window. 
 * Actually contains three separate panels, to allow for an outer axis panel 
 * a scroll bar and an inner plot panel. 
 * @author Doug Gillespie
 *
 */
public class OneBandDisplayPanel {

	private PlotPanel plotPanel;
	
	private PlotPanel pulsePanel;

	private AxisPanel axisPanel;

	private ScrollBarPanel scrollBarPanel;

	private OneBandControl oneBandControl;

	private OneBandTabPanel oneBandTabPanel;

	private long panelStartMillis;

	private long panelEndMillis;

	private OneBandDataBlock measurementDataBlock;
	private OneBandDataBlock pulseDataBlock;

	private Color[] defaultColours = {Color.BLUE, Color.GREEN, Color.RED, Color.orange};
	
	private HashMap<Integer, Color> chanColours;
	/**
	 * Pixel start position on screen - 0 unless display is wrapped. 
	 */
	private int panelStartPixels;

	/**
	 * Flag to say display is wrapped - used when this is a spectrogram plug in. 
	 */
	private boolean wrapDisplay;

	private boolean showScrollBar;

	private PamScroller hScroller;

	private RangeSpinner rangeSpinner;

	private boolean isViewer;

	protected OneBandDisplayParams displayParams = new OneBandDisplayParams();
	private OneBandDisplayParams pulseDisplayParams = new OneBandDisplayParams();

	public PamPanel pulseHidingPanel;

	OneBandDisplayPanel(OneBandControl oneBandControl, OneBandTabPanel oneBandTabPanel) {
		this.oneBandControl = oneBandControl;
		this.oneBandTabPanel = oneBandTabPanel;
		measurementDataBlock = oneBandControl.getOneBandProcess().getMeasureDataBlock();
		pulseDataBlock = oneBandControl.getPulseProcess().getPulseDataBlock();
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		hScroller = new PamScroller("Filtered Noise Scroller", AbstractPamScrollerAWT.HORIZONTAL, 1000, 600000, isViewer);
		plotPanel = new PlotPanel(PlotPanel.PANEL_FILTER);
		pulsePanel = new PlotPanel(PlotPanel.PANEL_PULSE);
		scrollBarPanel = new ScrollBarPanel();
		axisPanel = new AxisPanel();
		rangeSpinner.setSpinnerValue(displayParams.timeRange);
		measurementDataBlock.addObserver(new DataMonitor());
		pulseDataBlock.addObserver(new DataMonitor());
		PamSettingManager.getInstance().registerSettings(new SettingsSaver(PlotPanel.PANEL_FILTER));
		PamSettingManager.getInstance().registerSettings(new SettingsSaver(PlotPanel.PANEL_PULSE));
		newParams();
	}

	public JComponent getDisplayPanel() {
		return axisPanel;
	}
	
	/**
	 * Set the channel colours based on the current channelMap
	 */
	public void setChannelColours() {
		int[] chans = PamUtils.getChannelArray(oneBandControl.oneBandParameters.channelMap);
		if (chans!=null) {
			chanColours = new HashMap<>(chans.length);
			for (int i=0; i<chans.length; i++) {
				Color col =  PamColors.getInstance().getWhaleColor(chans[i]+1);
				chanColours.put(chans[i], col);
			}
		}
	}

	/**
	 * Called when the spinner changes the time range. 
	 * @param newValue new time range in seconds. 
	 */
	public void timeRangeChanged(double newValue) {
		axisPanel.timeAxis.setRange(0, newValue);
		hScroller.setVisibleMillis((long) (newValue*1000));
		repaintAll();

	}

	void newParams() {
		setChannelColours();
		plotPanel.newParams();
		pulsePanel.newParams();
		rangeSpinner.setSpinnerValue(displayParams.timeRange / 1000.);
		plotPanel.createKey();
//		pulseHidingPanel.setVisible(oneBandControl.getParameters().detectPulses);
		repaintAll();
	}

	void repaintAll() {
		axisPanel.repaint();
		plotPanel.repaint();
		if (pulsePanel.isVisible()) {
			pulsePanel.repaint();
		}
	}

	private void clockUpdate(long milliSeconds, long sampleNumber) {
		// round end time up by the measurement interval. 
		int mi = oneBandControl.getParameters().measurementInterval*1000;
		long t = (milliSeconds + mi*2);
		t /= mi;
		t *= mi;
		long oldStart = panelStartMillis;
		panelEndMillis = t;
		long newStart = panelEndMillis - (long) (rangeSpinner.getSpinnerValue()*1000);
		if (newStart >= oldStart || newStart < oldStart-(long) (rangeSpinner.getSpinnerValue()*1000)) {
			panelStartMillis = newStart;
			repaintAll();
		}
	}

	private void newDataUnit(OneBandDataUnit newDataUnit) {
		if (displayParams.autoScale) {
			plotPanel.checkAutoScale();
		}
		if (pulseDisplayParams.autoScale) {
			pulsePanel.checkAutoScale();
		}
		repaintAll();
	}

	public void popupMenu(MouseEvent e, PlotPanel plotPanel) {
		JPopupMenu m = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Display Options ...");
		mi.addActionListener(new OptionsMenu(oneBandControl.getGuiFrame(), plotPanel.panelType));
		m.add(mi);
		int availChannels = oneBandControl.getParameters().channelMap;
		int selChannels = displayParams.getDisplayChannels(availChannels);
		int[] chans = PamUtils.getChannelArray(availChannels);
		if (chans != null && chans.length > 0) {
			JMenu chanMen = new JMenu("Channels...");
			m.add(chanMen);
			for (int i = 0; i < chans.length; i++) {
				JCheckBoxMenuItem cm = new JCheckBoxMenuItem("Show channel " + chans[i]);
				chanMen.add(cm);
				cm.setSelected((selChannels & 1<<chans[i]) != 0);
				cm.addActionListener(new ChanSel(cm, chans[i]));
			}
		}
		
		m.show(e.getComponent(), e.getX(), e.getY());
	}

	private class ChanSel implements ActionListener {
		int chan;
		private JCheckBoxMenuItem cm;

		public ChanSel(JCheckBoxMenuItem cm, int chan) {
			super();
			this.cm = cm;
			this.chan = chan;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean isSel = cm.isSelected();
			int curSel = displayParams.getDisplayChannels(-1);
			curSel = PamUtils.SetBit(curSel, chan, isSel);
			displayParams.setDisplayChannels(curSel);
			newParams();
		}
		
	}
	
	protected OneBandDisplayParams getDisplayParams(int displayType) {
		switch (displayType) {
		case PlotPanel.PANEL_FILTER:
			return displayParams;
		default:
			return pulseDisplayParams;
		}
	}

	class OptionsMenu implements ActionListener {
		Frame parentFrame;
		private int plotType;
		public OptionsMenu(Frame parentFrame, int plotType) {
			this.parentFrame = parentFrame;
			this.plotType = plotType;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			oneBandTabPanel.optionsMenu(parentFrame, plotType);
		}
	}

	class MouseActions extends MouseAdapter {

		private PlotPanel plotPanel;

		public MouseActions(PlotPanel plotPanel) {
			this.plotPanel = plotPanel;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu(e, plotPanel);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu(e, plotPanel);
			}
		}

	}
	private class DataMonitor extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Noise Display";
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			clockUpdate(milliSeconds, sampleNumber);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			if (o == measurementDataBlock) {
				OneBandDataUnit du = (OneBandDataUnit) dataUnit;
				clockUpdate(du.getTimeMilliseconds(), du.getStartSample());
			}
			newDataUnit((OneBandDataUnit) dataUnit);
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) ((rangeSpinner.getSpinnerValue() + 5) * 1000);
//			return Math.max(PamCalendar.getTimeInMillis() - panelStartMillis, 0) + 5000;
		}


	}

	private class AxisPanel extends PamAxisPanel {
		private PamAxis timeAxis;

		AxisPanel() {
			timeAxis = new PamAxis(0, 1, 0, 1, 0, displayParams.timeRange, 
					PamAxis.ABOVE_LEFT, "Time (seconds)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
			setNorthAxis(timeAxis);
			setWestAxis(plotPanel.ampAxis);
			setInnerPanel(scrollBarPanel);
			setPlotPanel(plotPanel);
			SetBorderMins(30, 30, 30, 30);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (panelStartMillis > 0) {
				timeAxis.setExtraAxisStartLabel(PamCalendar.formatDateTime(panelStartMillis));
				timeAxis.setExtraAxisEndLabel(PamCalendar.formatDateTime(panelEndMillis));
			}
			super.paintComponent(g);
			if (pulseHidingPanel != null && pulseHidingPanel.isVisible()) {
				Point p1 = this.getLocationOnScreen();
				Point p2 = pulsePanel.getLocationOnScreen();
				int x0 = p2.x-p1.x;
				int y0 = p2.y-p1.y;
				pulsePanel.ampAxis.drawAxis(g, x0, y0+plotPanel.getHeight(), x0, y0);
			}
		}

	}

	private class ScrollBarPanel extends JPanel {
		ScrollBarPanel() {
			setLayout(new BorderLayout());
			setBorder(PamBorder.createInnerBorder());
			rangeSpinner = new RangeSpinner();
			rangeSpinner.addRangeSpinnerListener(new TimeRangeListener());
			hScroller.addObserver(new TimeScrollListener());
			hScroller.addControl(rangeSpinner.getComponent());
			hScroller.addObserver(rangeSpinner);
			hScroller.addDataBlock(measurementDataBlock);
			add(BorderLayout.SOUTH, hScroller.getComponent());
			JPanel splitPanel = new JPanel();
			splitPanel.setLayout(new GridLayout(2, 0));
			splitPanel.add(plotPanel);
			pulseHidingPanel = new PamPanel(PamColors.PamColor.BORDER);
			pulseHidingPanel.setLayout(new BorderLayout());
			pulseHidingPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
			pulseHidingPanel.add(BorderLayout.CENTER, pulsePanel);
			splitPanel.add(pulseHidingPanel);
			add(BorderLayout.CENTER, splitPanel);
		}		
	}

	private class TimeRangeListener implements RangeSpinnerListener {

		@Override
		public void valueChanged(double oldValue, double newValue) {
			/**
			 * Note that time values are in seconds, not milliseconds. 
			 */
			panelEndMillis = (long) (panelStartMillis + newValue*1000);
			displayParams.timeRange = (long) (newValue*1000L);
			timeRangeChanged(newValue);
		}

	}

	private class TimeScrollListener implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {

		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			panelStartMillis = pamScroller.getValueMillis();
			panelEndMillis = panelStartMillis + (long)(rangeSpinner.getSpinnerValue()*1000);
			if (displayParams.autoScale) {
				plotPanel.checkAutoScale();
			}
			if (pulseDisplayParams.autoScale) {
				pulsePanel.checkAutoScale();
			}
			repaintAll();
		}

	}

	private class PlotPanel extends JPanelWithPamKey {

		private PamSymbol[] measureSymbols = new PamSymbol[OneBandControl.NMEASURES];
		private PamSymbol[] channelSymbols;
		KeyPanel keyPanel;
		private int panelType;
		private OneBandDataBlock dataBlock;
		private static final int PANEL_FILTER = 0;
		private static final int PANEL_PULSE = 1;
		private PamAxis ampAxis;
		private double minPlotValue, maxPlotValue;

		public PlotPanel(int panelType) {
			super();
			this.panelType = panelType;
			measureSymbols[0] = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, defaultColours[0], Color.BLUE);
			measureSymbols[1] = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, defaultColours[1], Color.GREEN);
			measureSymbols[2] = new PamSymbol(PamSymbolType.SYMBOL_STAR, 10, 10, true, defaultColours[2], Color.RED);
			measureSymbols[3] = new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 10, 10, true, defaultColours[3], defaultColours[3]);
			createKey();
			this.addMouseListener(new MouseActions(this));
			switch (panelType) {
			case PANEL_FILTER:
				setToolTipText("Noise display");
				dataBlock = measurementDataBlock;
				ampAxis = new PamAxis(0, 1, 0, 1, displayParams.minAmplitude, displayParams.maxAmplitude, 
						PamAxis.ABOVE_LEFT, "Filter output level (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
				ampAxis.setCrampLabels(true);
				break;
			case PANEL_PULSE:
				setToolTipText("Noise pulse display");
				dataBlock = pulseDataBlock;
				ampAxis = new PamAxis(0, 1, 0, 1, displayParams.minAmplitude, displayParams.maxAmplitude, 
						PamAxis.ABOVE_LEFT, "Pulse output level (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
				ampAxis.setCrampLabels(true);
				break;
			}
		}

		public void newParams() {
			OneBandDisplayParams params = getDisplayParams(this.panelType);
			if (!params.autoScale) {
				ampAxis.setRange(params.minAmplitude, params.maxAmplitude);
			}
			for (int i = 0; i < OneBandControl.NMEASURES; i++) {
				measureSymbols[i].setHeight(params.symbolSize);
				measureSymbols[i].setWidth(params.symbolSize);
			}

			// if the channelMap has been loaded, set up the channel colours
			int[] chans = PamUtils.getChannelArray(oneBandControl.oneBandParameters.channelMap);
			if (chans!=null) {
				channelSymbols = new PamSymbol[chans.length];
				for (int i=0; i<chans.length; i++) {
					Color col =  chanColours.get(chans[i]);
					channelSymbols[i] = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true,  col, col);
				}
			}

			// create the key
			createKey();
		}

		private void checkAutoScale() {
			if (minPlotValue == 0 && maxPlotValue == 0) {
				return;
			}
			double rangeMax = Math.ceil(maxPlotValue/10.)*10;
			double rangeMin = Math.floor(minPlotValue/10.)*10;
			if (rangeMax - rangeMin < 10.) rangeMax += 10;
			if (Math.abs(rangeMax-ampAxis.getMaxVal()) >= 10. ||
					Math.abs(rangeMin-ampAxis.getMinVal()) >= 10.) {
				ampAxis.setRange(rangeMin, rangeMax);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			long millis = (long) axisPanel.timeAxis.getDataValue(event.getX());
			millis *= 1000;
			millis += panelStartMillis;
			double dB = ampAxis.getDataValue(event.getY());
			return String.format("Mouse %s, %3.1fdB", PamCalendar.formatDateTime(millis), dB);
		}

		void createKey() {
			if (keyPanel == null) {
				String name = oneBandControl.getUnitName();
				if (panelType == PANEL_PULSE) {
					name = "Pulses";
				}
				keyPanel = new KeyPanel(name, PamKeyItem.KEY_SHORT);
				this.setKeyPanel(keyPanel);
				this.setKeyPosition(CornerLayoutContraint.FIRST_LINE_START);
			}
			keyPanel.clear();
			OneBandDisplayParams params = getDisplayParams(panelType);
			for (int i = OneBandControl.NMEASURES-1; i >= 0; i--) {
				if ((params.showWhat & 1<<i) != 0) {
					keyPanel.add(new SymbolKeyItem(measureSymbols[i], OneBandControl.getMeasurementName(i)));
				}
			}
			if (displayParams.colourByChannel) {
				int selChans = params.getDisplayChannels(oneBandControl.oneBandParameters.channelMap);
				int[] chans = PamUtils.getChannelArray(selChans);
				if (chans!=null) {
					for (int i=0; i<chans.length; i++) {
						keyPanel.add(new SymbolKeyItem(channelSymbols[i], "Channel " + String.valueOf(chans[i])));
					}
				}
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			OneBandDisplayParams dParams = getDisplayParams(panelType);
			int selChannels = dParams.getDisplayChannels(-1);
			if (dParams.showGrid) {
				ampAxis.drawGrid(g, new Dimension(getWidth(), getHeight()), 0);
				axisPanel.timeAxis.drawGrid(g, new Dimension(getWidth(), getHeight()), 0);
			}
			boolean first = true;
			Color col;
			synchronized (dataBlock.getSynchLock()) {
//				LoopOverEachDbhtMeasure:
				for (int i = 0; i < OneBandControl.NMEASURES; i++) {
					if (((1<<i) & dParams.showWhat) == 0) {
						continue;
					}
					measureSymbols[i].setLineColor(defaultColours[i]);
					measureSymbols[i].setFillColor(defaultColours[i]);
					ListIterator<OneBandDataUnit> it = dataBlock.getListIterator(PamDataBlock.ITERATOR_END);
					OneBandDataUnit du;
					long millis;
					int singleChan;
					Point pt = new Point();
					int prevx = -9999, prevy = -9999;
//					LoopOverEachDbhtDataUnit:
					while (it.hasPrevious()) {
						du = it.previous();
						int chanMap = du.getChannelBitmap();
						if ((chanMap & selChannels) == 0) {
							continue;
						}
						millis = du.getTimeMilliseconds();
						if (millis > panelEndMillis) {
							continue;
						}
						if (millis < panelStartMillis) {
							break;
						}
						
						if (du.getMeasure(i)==OneBandProcess.nanValue){
							continue;
						}
						
						pt.x = (int) axisPanel.timeAxis.getPosition((millis-panelStartMillis)/1000.);
						pt.y = (int) ampAxis.getPosition(du.getMeasure(i));
						if (displayParams.colourByChannel) {
							singleChan = PamUtils.getSingleChannel(chanMap);
							col = PamColors.getInstance().getWhaleColor(singleChan+1);
							measureSymbols[i].setLineColor(col);
							measureSymbols[i].setFillColor(col);
						}
						measureSymbols[i].draw(g, pt);
//						if (displayParams.drawLine) {
//							if (prevx != -9999) {
//								g.drawLine(prevx, prevy, pt.x, pt.y);
//							}
//							prevx = pt.x;
//							prevy = pt.y;
//						}
						if (first) {
							maxPlotValue = minPlotValue = du.getMeasure(i);
							first = false;
						}
						else {
							minPlotValue = Math.min(minPlotValue, du.getMeasure(i));
							maxPlotValue = Math.max(maxPlotValue, du.getMeasure(i));
						}
					}
				}
			}
		}

	}
	
	private class SettingsSaver implements PamSettings {

		private int panelType;
		
		public SettingsSaver(int panelType) {
			super();
			this.panelType = panelType;
		}

		@Override
		public Serializable getSettingsReference() {
			switch( panelType) {
			case PlotPanel.PANEL_FILTER:
				return displayParams;
			case PlotPanel.PANEL_PULSE:
				return pulseDisplayParams;
			}
			return null;
		}

		@Override
		public long getSettingsVersion() {
			return OneBandDisplayParams.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return oneBandControl.getUnitName();
		}

		@Override
		public String getUnitType() {
			switch( panelType) {
			case PlotPanel.PANEL_FILTER:
				return "OneBand Display Options";
			case PlotPanel.PANEL_PULSE:
				return "OneBand Pulse Options";
			}
			return "Noise Display Options";
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			switch( panelType) {
				case PlotPanel.PANEL_FILTER:
					displayParams = ((OneBandDisplayParams)pamControlledUnitSettings.getSettings()).clone();
				case PlotPanel.PANEL_PULSE:
					pulseDisplayParams = ((OneBandDisplayParams)pamControlledUnitSettings.getSettings()).clone();
				}
			return true;
		}
		
	}

	public void setDisplayParams(int plotType, OneBandDisplayParams newParams) {
		switch (plotType) {
		case PlotPanel.PANEL_FILTER:
			 displayParams = newParams;
			 break;
		case PlotPanel.PANEL_PULSE:
			 pulseDisplayParams = newParams;
			 break;
		}
		newParams();
		
	}

}
