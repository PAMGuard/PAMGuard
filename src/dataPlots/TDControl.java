package dataPlots;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLayeredPane;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.CornerLayoutContraint;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import dataPlots.layout.GraphParameters;
import dataPlots.layout.TDAxes;
import dataPlots.layout.TDGraph;
import dataPlots.layout.TDGraphContainer;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
import userDisplay.UserDisplayComponentAdapter;

public class TDControl extends UserDisplayComponentAdapter implements PamSettings {

	private TDAxes tdAxes;
	
	private TDParameters tdParameters = new TDParameters();
	
	int runMode = PamController.getInstance().getRunMode();

	private PamScroller timeScroller;
	
	private RangeSpinner timeRangeSpinner;
	
	private ArrayList<TDGraph> graphs = new ArrayList<TDGraph>();
	
	private TimeRangeListener timeRangeListener;

	private HidingControlPanel hidingControlPanel;

	private boolean isViewer;

	private PamObserver dataObserver;
	
	private JCheckBox pauseButton;
	
	/**
	 * Show a graph control panel. If true a graph control panel is present. If false then there is not control panel. 
	 */
	private boolean showControlPanel=true;
	
	/**
	 * Show a label above each graph. 
	 */
	private boolean showGraphLabels=false; 
	
	
	public TDControl(TDParameters tdParameters) {
		this.tdParameters = tdParameters;
		PamSettingManager.getInstance().registerSettings(this);
		create();
	}
	
	public TDControl(TDParameters tdParameters, boolean registerSettings) {
		this.tdParameters = tdParameters;
		if (registerSettings) {
			PamSettingManager.getInstance().registerSettings(this);
		}
		create();
	}
	
	/**
	 * Create a new time display. 
	 * @param tdParameters - time display paramaters. 
	 * @param registerSettings - register settings or not. 
	 * @param showControlPanel- show a control panel on the graph allowing users to add/remnove  panels. 
	 */
	public TDControl(TDParameters tdParameters, boolean registerSettings, boolean showControlPanel) {
		this.tdParameters = tdParameters;
		this.showControlPanel=showControlPanel;
		if (registerSettings) {
			PamSettingManager.getInstance().registerSettings(this);
		}
		create();
	}
	
	public TDControl() {
		super();
		PamSettingManager.getInstance().registerSettings(this);
		create();
	}
	
	private void create() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			timeRangeListener = new ViewerTimeRanges();
			isViewer = true;
		}
		else {
			timeRangeListener = new NormalTimeRanges();
		}
		pauseButton = new JCheckBox("Pause scroll");
		dataObserver = new DataObserver();
		if (tdParameters.graphParameters == null) {
			tdParameters.graphParameters = new ArrayList<GraphParameters>();
		}
		if (tdParameters.graphParameters.size() == 0) {
			tdParameters.graphParameters.add(new GraphParameters());
		}
		tdAxes = new TDAxes(this);
		layoutGraphs();
		createGraphs();
		layoutGraphs();
		timeRangeSpinner.setSpinnerValue(tdParameters.visibleTimeRange);
//		timeScroller.setVisibleAmount((long) (tdParameters.visibleTimeRange * 1000)); 
	}
	
	private void createGraphs() {
		if (tdParameters.graphParameters != null) {
			int g = 0;
			for (GraphParameters gp:tdParameters.graphParameters) {
				TDGraph aGraph = new TDGraph(this, g++);
				graphs.add(aGraph);
				aGraph.setGraphParameters(gp);
				aGraph.setShowTopLabel(showGraphLabels);
			}
		}
	}

	private abstract class TimeRangeListener implements RangeSpinnerListener, PamScrollObserver {
		/**
		 * Called from the range spinner. 
		 */
		@Override
		public void valueChanged(double oldValue, double newValue) {
			timeScroller.setVisibleMillis((long) (newValue * 1000)); 
			tdParameters.visibleTimeRange = newValue;
			for (TDGraph aGraph:graphs) {
				aGraph.timeRangeSpinnerChange(oldValue, newValue);
			}
			repaintAll(100);
		}
		
	}
	
	private class NormalTimeRanges extends TimeRangeListener {

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			repaintAll(100);
		}
		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			repaintAll(100);
//			System.out.println("Scroll range changed");
//			System.out.println(String.format("Scroller range changed get start %s, End %s, pos %s", PamCalendar.formatTime(timeScroller.getMinimumMillis()),
//					PamCalendar.formatTime(timeScroller.getMaximumMillis()),PamCalendar.formatTime(timeScroller.getValueMillis())));
		}

	}
	
	private class ViewerTimeRanges extends TimeRangeListener {

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			tdParameters.scrollStartMillis = pamScroller.getValueMillis();
			for (TDGraph aGraph:graphs) {
				aGraph.timeScrollValueChanged(pamScroller.getValueMillis());
			}
			repaintAll(100);
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			for (TDGraph aGraph:graphs) {
				aGraph.timeScrollRangeChanged(pamScroller.getMinimumMillis(), pamScroller.getMaximumMillis());
			}
			repaintAll();
		}
		
	}
	
	private class DataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			if (PamRawDataBlock.class == o.getClass()) {
				return 0;
			}
			return (long) (timeRangeSpinner.getSpinnerValue() * 1000);
		}

		@Override
		public String getObserverName() {
			return "Time Display";
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			scrollDisplayEnd(milliSeconds);
		}
		
	}
	
	private long lastUpdate;

	private HidingPanel hidingPanel;
	
	/**
	 * Called in real time ops to scroll 
	 * the display. 
	 * @param milliSeconds
	 */
	private void scrollDisplayEnd(long milliSeconds) {
		if (milliSeconds <= lastUpdate && milliSeconds > lastUpdate - timeRangeSpinner.getSpinnerValue()*1000) {
			return;
		}
		lastUpdate = milliSeconds;
//		long scrollPos = milliSeconds + 1000;//(long) (timeRangeSpinner.getSpinnerValue()*1000 * 0.95);
		long gap = Math.min(-100, timeScroller.getRangeMillis()/5);
		long scrollEnd = milliSeconds + gap;// + (long) (timeRangeSpinner.getSpinnerValue()*1000);
		long scrollStart = scrollEnd - (timeScroller.getMaximumMillis()-timeScroller.getMinimumMillis());//getDefaultLoadtime();
		long scrollPos = scrollEnd - (long) (timeRangeSpinner.getSpinnerValue()*1000);
//		System.out.println(String.format("Scroller set start %s, End %s, pos %s", PamCalendar.formatTime(scrollStart),
//				PamCalendar.formatTime(scrollEnd),PamCalendar.formatTime(scrollPos)));
		getTimeScroller().setRangeMillis(scrollStart, scrollEnd, true);
		getTimeScroller().setVisibleMillis((int) (timeRangeSpinner.getSpinnerValue()*1000));
//		if (scrollPos < timeScroller.getValueMillis() && scrollPos > timeScroller.getValueMillis()-3000) {
//			return;
//		}
//		key
		if (!pauseButton.isSelected()) {
			long oldPos = getTimeScroller().getValueMillis();
//			if (Math.abs(scrollPos-oldPos) == 0) return;
			getTimeScroller().setValueMillis(scrollPos);
//			System.out.println(String.format("Scroll to %d, jump %d", scrollPos%10000, scrollPos-oldPos));
//					System.out.println(String.format("Scroller get start %s, End %s, pos %s", 
//							PamCalendar.formatTime(timeScroller.getMinimumMillis(),true),
//							PamCalendar.formatTime(timeScroller.getMaximumMillis(),true),
//							PamCalendar.formatTime(timeScroller.getValueMillis(),true)));
			repaintAll(100);
		}
		else {
			//repaint handles in time changed listener. 
			timeScroller.setValueMillis(timeScroller.getValueMillis());
		}
	}

	/**
	 * Subscribe datablocks to the time scroller.
	 * <br> seems like a daft call sequence, but the time
	 * scroller will only hold one copy of each block, so 
	 * if a block is removed, it may be that it shouldn't be 
	 * removed - so easer just to remake the entire 
	 * list every time 
	 */
	public void subscribeScrollDataBlocks() {
		timeScroller.removeAllDataBlocks();
		for (TDGraph aGraph:graphs) {
			aGraph.subscribeScrollDataBlocks(timeScroller);
		}
	}

	public void layoutGraphs() {
		
		tdAxes.getAxisInnerPanel().removeAll();
		
		if (timeRangeSpinner != null) {
			tdAxes.getTimeRangePanel().remove(timeRangeSpinner.getComponent());
		}
		PamScroller scroller = createScroller();
		if (tdParameters.orientation == AbstractPamScrollerAWT.HORIZONTAL) {
			tdAxes.getAxisInnerPanel().add(BorderLayout.SOUTH, scroller.getComponent());
			timeScroller.addControl(timeRangeSpinner.getComponent());
		}
		else {
			tdAxes.getAxisInnerPanel().add(BorderLayout.EAST, scroller.getComponent());
//			CornerLayoutContraint c = new CornerLayoutContraint();
//			c.anchor = CornerLayoutContraint.LAST_LINE_END;
//			tdAxes.getLayeredPane().add(timeRangeSpinner.getComponent(), c, JLayeredPane.DEFAULT_LAYER);
			tdAxes.getTimeRangePanel().add(timeRangeSpinner.getComponent());
		}
		tdAxes.getTimeRangePanel().setVisible(tdParameters.orientation == AbstractPamScrollerAWT.VERTICAL);
		TDGraphContainer tdGraphContainer = new TDGraphContainer(this);
		tdAxes.getAxisInnerPanel().add(BorderLayout.CENTER, tdGraphContainer.getGraphContainer());
		for (int i = 0; i < graphs.size(); i++) {
			tdGraphContainer.getGraphContainer().add(graphs.get(i).getGraphOuterPanel());
		}
		
		//create a hiding panel north of the main panel to hold graph controls. 
		//TODO- for some reason we need to remove this- one of the most annoying bugs in a while. 
		if (hidingPanel!=null) tdAxes.getOuterPanel().remove(hidingPanel);
		hidingControlPanel = new HidingControlPanel(this);
		hidingPanel = new HidingTDPanel(tdAxes.getOuterPanel(), hidingControlPanel, HidingPanel.HORIZONTAL, true);
		if (showControlPanel) tdAxes.getOuterPanel().add(BorderLayout.NORTH, hidingPanel);
		
		CornerLayoutContraint clc = new CornerLayoutContraint();
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			clc.anchor = CornerLayoutContraint.FIRST_LINE_END;
			tdAxes.getLayeredPane().add(pauseButton, clc, JLayeredPane.DEFAULT_LAYER);
		}

		for (int i = 0; i < graphs.size(); i++) {
			graphs.get(i).layoutGraph();
		}
		
		hidingPanel.invalidate();
		tdAxes.getOuterPanel().validate();
				
	}
	
	private class HidingTDPanel extends HidingPanel{

		public HidingTDPanel(Component componentFrame,
				Component mainComponent, int direction, boolean canScroll) {
			super(componentFrame, mainComponent, direction, canScroll);
		}
		
		/**
		 * Show or hide the panel
		 * @param state true = show, false = hide. 
		 */
		@Override
		public void showPanel(boolean state) {			
//			if (graphs.size()>0) System.out.println("Height start: "+graphs.get(0).getGraphPlotPanel(0).getHeight());
			super.showPanel(state);
			tdAxes.getOuterPanel().validate();

//			if (graphs.size()>0) System.out.println("Height End: "+graphs.get(0).getGraphPlotPanel(0).getHeight());
			//must repaint the graphs when hiding panel is changed- need to repaint data unit.
			for (int i=0; i<graphs.size(); i++){
				for (int j=0; j<graphs.get(i).getNumPlotPanels(); j++){
//					System.out.println("Plot Height Panel: "+graphs.get(i).getGraphPlotPanel(j).getHeight());
					graphs.get(i).getGraphPlotPanel(j).fillDataImage();
				}
			}
			for (TDGraph aGraph:graphs) {
				aGraph.timeScrollValueChanged(timeScroller.getValueMillis());
			}
			repaintAll(100);
//			if (!state){
//				super.setPreferredSize(new Dimension(1,13));
//				super.setSize(new Dimension(1,13));
//			}
//			else super.setPreferredSize(null);
//			if (tdAxes.getOuterPanel()!=null && hidingPanel!=null){
//				tdAxes.getOuterPanel().repaint();
//				System.out.println("HidingPanel size: "+hidingPanel.getSize().toString()+"HidingControlPanel size: "+hidingControlPanel.getSize().toString() + " state "	+state);
//			}
		}
		
	}
	

	public PamScroller createScroller() {
		
//		if (timeScroller != null) return timeScroller;
		PamScroller oldScroller = timeScroller;
		RangeSpinner oldSpinner = timeRangeSpinner;
		
		timeScroller = new TDPamScroller("Time display", tdParameters.orientation, 100, 120000L, true);
		timeRangeSpinner = new RangeSpinner();
		timeScroller.addObserver(timeRangeSpinner);
		timeRangeSpinner.addRangeSpinnerListener(timeRangeListener);
		timeScroller.addObserver(timeRangeListener);
		timeScroller.setRangeMillis(0, tdParameters.scrollableTimeRange, false);
		timeRangeSpinner.setSpinnerValue(tdParameters.visibleTimeRange);
		if (oldSpinner != null) {
			timeRangeSpinner.setSpinnerValue(oldSpinner.getSpinnerValue());
		}
		if (oldScroller != null) {
			timeScroller.setRangeMillis(oldScroller.getMinimumMillis(), oldScroller.getMaximumMillis(), true);
			timeScroller.setValueMillis(oldScroller.getValueMillis());
			timeScroller.setPageStep(oldScroller.getPageStep());
			timeScroller.setBlockIncrement(oldScroller.getBlockIncrement());
			timeScroller.setVisibleMillis(oldScroller.getVisibleAmount());
			int ndb = oldScroller.getNumUsedDataBlocks();
			for (int i = 0; i < ndb; i++) {
				timeScroller.addDataBlock(oldScroller.getUsedDataBlock(i));
			}
			oldScroller.destroyScroller();
		}
		timeRangeSpinner.setSpinnerValue(timeRangeSpinner.getSpinnerValue());
		
		return timeScroller;
	}
	
	@Override
	public Component getComponent() {
		return tdAxes.getOuterPanel();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// pass it on to all graphs !
		for (TDGraph aGraph:graphs) {
			aGraph.notifyModelChanged(changeType);
		}
	}

	/**
	 * @return the tdParameters
	 */
	public TDParameters getTdParameters() {
		return tdParameters;
	}

	/**
	 * @return the tdAxes
	 */
	public TDAxes getTdAxes() {
		return tdAxes;
	}

	/**
	 * @return the runMode
	 */
	public int getRunMode() {
		return runMode;
	}

	/**
	 * @return the pamScroller
	 */
	public PamScroller getTimeScroller() {
		return timeScroller;
	}

	/**
	 * @return the graphs
	 */
	public ArrayList<TDGraph> getGraphs() {
		return graphs;
	}

	public void repaintAll() {
		repaintAll(10);
	}
	
	private long lastRepaintAll = 0;
	public void repaintAll(long millis) {
		long now = System.currentTimeMillis();
		if (now - lastRepaintAll < millis) return;
		lastRepaintAll = now;
		tdAxes.getLayeredPane().repaint(millis);
		tdAxes.getAxisPanel().repaint(millis);
		tdAxes.getAxisInnerPanel().repaint(millis);
		for (TDGraph aGraph:graphs) {
			aGraph.repaint(millis);
		}
		timeRangeSpinner.getComponent().repaint(millis);
	}

	/**
	 * @return the timeRangeSpinner
	 */
	public RangeSpinner getTimeRangeSpinner() {
		return timeRangeSpinner;
	}

	public void addGraph() {
		graphs.add(new TDGraph(this, graphs.size()));
		layoutGraphs();
//		hidingControlPanel.showComponent(true);
	}

	/**
	 * Remove graph with given index. 
	 * @param iGraph graph index. 
	 */
	public void removeGraph(int iGraph) {
		graphs.get(iGraph).removeGraph();
		graphs.remove(iGraph);
		layoutGraphs();
	}

	/**
	 * Called when settings loaded. 
	 * @param settings
	 * @return
	 */
	private boolean restoreSettings(TDParameters settings) {
		if (settings == null) {
			return false;
		}
		this.tdParameters = settings.clone();
		return true;
	}
	
	/**
	 * Called just before settings are saved. Will have to go 
	 * through all the graphs and get them to provide updated settings
	 * information to add to this since it's not kept up to date on the fly. 
	 * @return object to serialise.
	 */
	private Serializable prepareSerialisedSettings() {
		tdParameters.scrollableTimeRange = timeScroller.getRangeMillis();
		tdParameters.graphParameters = new ArrayList<>();
		for (TDGraph aGraph:graphs) {
			tdParameters.graphParameters.add(aGraph.prepareGraphParameters());
		}
		return tdParameters;
	}
	
	/*
	 * PamSettings interface
	 */
		@Override
		public String getUnitName() {
			return "User TDDisplay";
		}

		@Override
		public String getUnitType() {
			return "User TDDisplay";
		}

		@Override
		public Serializable getSettingsReference() {
			return prepareSerialisedSettings();
		}

		@Override
		public long getSettingsVersion() {
			return TDParameters.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			return TDControl.this.restoreSettings((TDParameters) pamControlledUnitSettings.getSettings());
		}

		/**
		 * @return the isViewer
		 */
		public boolean isViewer() {
			return isViewer;
		}

		public PamObserver getDataObserver() {
			return dataObserver;
		}

		/**
		 * Get the number of pixels in a time graph. All should be the 
		 * same, so just grab the first one. 
		 * @return number of time pixels in the plots. 
		 */
		public int getGraphTimePixels() {
			// get the first non-zero one. 
			for (TDGraph aGraph:graphs) {
				int pixs = aGraph.getTimePixels();
				if (pixs > 0) {
					return pixs;
				}
			}
			return 0;
		}
		
		/**
		 * Check whether the time display has a control panel. 
		 * @return true if there is a control panel. False if not control panel is present. 
		 */
		public boolean isShowControlPanel() {
			return showControlPanel;
		}

		/**
		 * Set whether the graphs has a control panel, allowing panels etc to be added. 
		 * @param showControlPanel - true to show a control panel. 
		 */
		public void setShowControlPanel(boolean showControlPanel) {
			this.showControlPanel = showControlPanel;
		}
		
		/**
		 * Set whether graphs within the display show their label. Generally this is only to save a little space. 
		 * @param show - true if graphs show a label describing graph on top right. 
		 */
		public void setShowGraphLabels(boolean show){
			this.showGraphLabels=show;
		}

		/**
		 * Get the pause button. 
		 * @return the pause button for the graph. 
		 */
		public JCheckBox getPauseButton() {
			return pauseButton;
		}

		@Override
		public String getFrameTitle() {
			return getUnitName();
		}
		
}
