package dbht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

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
public class DbHtDisplayPanel {

	private PlotPanel plotPanel;

	private AxisPanel axisPanel;

	private ScrollBarPanel scrollBarPanel;

	private DbHtControl dbHtControl;

	private DbHtTabPanel dbHtTabPanel;

	private long panelStartMillis;

	private long panelEndMillis;

	private DbHtDataBlock measurementDataBlock;

	private double minPlotValue, maxPlotValue;

	private Color[] defaultColours = {Color.BLUE, Color.GREEN, Color.RED};
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

	protected DbHtDisplayParams displayParams = new DbHtDisplayParams();

	DbHtDisplayPanel(DbHtControl dbHtControl, DbHtTabPanel dbHtTabPanel) {
		this.dbHtControl = dbHtControl;
		this.dbHtTabPanel = dbHtTabPanel;
		measurementDataBlock = dbHtControl.getDbHtProcess().getMeasureDataBlock();
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		hScroller = new PamScroller("dBHt Scroller", AbstractPamScrollerAWT.HORIZONTAL, 1000, 600000, isViewer);
		plotPanel = new PlotPanel();
		scrollBarPanel = new ScrollBarPanel();
		axisPanel = new AxisPanel();
		rangeSpinner.setSpinnerValue(displayParams.timeRange);
		measurementDataBlock.addObserver(new DataMonitor());
		PamSettingManager.getInstance().registerSettings(new SettingsSaver());
		newParams();
	}

	public JComponent getDisplayPanel() {
		return axisPanel;
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
		if (!displayParams.autoScale) {
			axisPanel.ampAxis.setRange(displayParams.minAmplitude, displayParams.maxAmplitude);
		}
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			plotPanel.measureSymbols[i].setHeight(displayParams.symbolSize);
			plotPanel.measureSymbols[i].setWidth(displayParams.symbolSize);
		}
		rangeSpinner.setSpinnerValue(displayParams.timeRange / 1000.);
		plotPanel.createKey();
		repaintAll();
	}

	void repaintAll() {
		axisPanel.repaint();
		plotPanel.repaint();
	}

	private void clockUpdate(long milliSeconds, long sampleNumber) {
		// round end time up by the measurement interval. 
		int mi = dbHtControl.getDbHtParameters().measurementInterval*1000;
		long t = (milliSeconds + mi*2);
		t /= mi;
		t *= mi;
		long oldStart = panelStartMillis;
		panelEndMillis = t;
		panelStartMillis = panelEndMillis - (long) (rangeSpinner.getSpinnerValue()*1000);
		if (panelStartMillis != oldStart) {
			repaintAll();
		}
	}

	private void newDataUnit(DbHtDataUnit newDataUnit) {
		if (displayParams.autoScale) {
			checkAutoScale();
		}
		repaintAll();
	}

	private void checkAutoScale() {
		if (minPlotValue == 0 && maxPlotValue == 0) {
			return;
		}
		double rangeMax = Math.ceil(maxPlotValue/10.)*10;
		double rangeMin = Math.floor(minPlotValue/10.)*10;
		if (rangeMax - rangeMin < 10.) rangeMax += 10;
		if (Math.abs(rangeMax-axisPanel.ampAxis.getMaxVal()) >= 10. ||
				Math.abs(rangeMin-axisPanel.ampAxis.getMinVal()) >= 10.) {
			axisPanel.ampAxis.setRange(rangeMin, rangeMax);
		}
	}
	public void popupMenu(MouseEvent e) {
		JPopupMenu m = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Display Options ...");
		mi.addActionListener(new OptionsMenu(PamController.getInstance().
				getGuiFrameManager().getFrame(dbHtControl.getFrameNumber())));
		m.add(mi);
		m.show(e.getComponent(), e.getX(), e.getY());
	}

	class OptionsMenu implements ActionListener {
		Frame parentFrame;
		public OptionsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			dbHtTabPanel.optionsMenu(parentFrame);
		}
	}

	class MouseActions extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu(e);
			}
		}

	}
	private class DataMonitor extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "dBHt Display";
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			clockUpdate(milliSeconds, sampleNumber);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			DbHtDataUnit du = (DbHtDataUnit) dataUnit;
			clockUpdate(du.getTimeMilliseconds(), du.getStartSample());
			newDataUnit((DbHtDataUnit) dataUnit);
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return Math.max(PamCalendar.getTimeInMillis() - panelStartMillis, 0) + 5000;
		}

	}

	private class AxisPanel extends PamAxisPanel {
		private PamAxis timeAxis;
		private PamAxis ampAxis;

		AxisPanel() {
			timeAxis = new PamAxis(0, 1, 0, 1, 0, displayParams.timeRange, 
					PamAxis.ABOVE_LEFT, "Time (seconds)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
			ampAxis = new PamAxis(0, 1, 0, 1, displayParams.minAmplitude, displayParams.maxAmplitude, 
					PamAxis.ABOVE_LEFT, "dB Above Hearing Threshold (dBHt)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
			setNorthAxis(timeAxis);
			setWestAxis(ampAxis);
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
			add(BorderLayout.CENTER, plotPanel);
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
				checkAutoScale();
			}
			repaintAll();
		}

	}

	private class PlotPanel extends JPanelWithPamKey {

		private PamSymbol[] measureSymbols = new PamSymbol[3];
		KeyPanel keyPanel;
		public PlotPanel() {
			super();
			measureSymbols[0] = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, defaultColours[0], Color.BLUE);
			measureSymbols[1] = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, defaultColours[1], Color.GREEN);
			measureSymbols[2] = new PamSymbol(PamSymbolType.SYMBOL_STAR, 10, 10, true, defaultColours[2], Color.RED);
			createKey();
			this.addMouseListener(new MouseActions());
			setToolTipText("dBHt display");
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			long millis = (long) axisPanel.timeAxis.getDataValue(event.getX());
			millis *= 1000;
			millis += panelStartMillis;
			double dB = axisPanel.ampAxis.getDataValue(event.getY());
			return String.format("Mouse %s, %3.1fdB", PamCalendar.formatDateTime(millis), dB);
		}

		void createKey() {
			if (keyPanel == null) {
				keyPanel = new KeyPanel(dbHtControl.getUnitName(), PamKeyItem.KEY_SHORT);
				this.setKeyPanel(keyPanel);
				this.setKeyPosition(CornerLayoutContraint.FIRST_LINE_START);
			}
			keyPanel.clear();
			for (int i = 2; i >= 0; i--) {
				if ((displayParams.showWhat & 1<<i) != 0) {
					keyPanel.add(new SymbolKeyItem(measureSymbols[i], DbHtControl.measureNames[i]));
				}
			}
			
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (displayParams.showGrid) {
				axisPanel.ampAxis.drawGrid(g, new Dimension(getWidth(), getHeight()), 0);
				axisPanel.timeAxis.drawGrid(g, new Dimension(getWidth(), getHeight()), 0);
			}
			boolean first = true;
			Color col;
			synchronized (measurementDataBlock.getSynchLock()) {
//				LoopOverEachDbhtMeasure:
				for (int i = 0; i < DbHtControl.NMEASURES; i++) {
					if (((1<<i) & displayParams.showWhat) == 0) {
						continue;
					}
					measureSymbols[i].setLineColor(defaultColours[i]);
					measureSymbols[i].setFillColor(defaultColours[i]);
					ListIterator<DbHtDataUnit> it = measurementDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
					DbHtDataUnit du;
					long millis;
					int singleChan;
					Point pt = new Point();
					int prevx = -9999, prevy = -9999;
//					LoopOverEachDbhtDataUnit:
					while (it.hasPrevious()) {
						du = it.previous();
						millis = du.getTimeMilliseconds();
						if (millis > panelEndMillis) {
							continue;
						}
						if (millis < panelStartMillis) {
							break;
						}
						
						if (du.getMeasure(i)==DbHtProcess.nanValue){
							continue;
						}
						
						pt.x = (int) axisPanel.timeAxis.getPosition((millis-panelStartMillis)/1000.);
						pt.y = (int) axisPanel.ampAxis.getPosition(du.getMeasure(i));
						if (displayParams.colourByChannel) {
							singleChan = PamUtils.getSingleChannel(du.getChannelBitmap());
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

		@Override
		public Serializable getSettingsReference() {
			return displayParams;
		}

		@Override
		public long getSettingsVersion() {
			return DbHtDisplayParams.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return dbHtControl.getUnitName();
		}

		@Override
		public String getUnitType() {
			return "dBHt Display Options";
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			displayParams = ((DbHtDisplayParams)pamControlledUnitSettings.getSettings()).clone();
			return true;
		}
		
	}

}
