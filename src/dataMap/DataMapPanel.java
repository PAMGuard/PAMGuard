package dataMap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import PamView.PamTabPanel;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorderPanel;
/**
 * The main panel managed by DataMapControl
 * In reality, this does most of the actual controlling. 
 * @author Doug Gillespie
 *
 */
public class DataMapPanel extends PamBorderPanel implements PamTabPanel {
	
	private DataMapControl dataMapControl;

	private SummaryPanel summaryPanel;
	
	private ScalePanel scalePanel;
	
	protected ScrollingDataPanel scrollingDataPanel;

	private Dimension graphDimension;
	
	protected DataMapPanel(DataMapControl dataMapControl) {
		this.dataMapControl = dataMapControl;
		summaryPanel = new SummaryPanel(dataMapControl, this);
		scalePanel = new ScalePanel(dataMapControl, this);
		scrollingDataPanel = new ScrollingDataPanel(dataMapControl, this);
		JPanel northPanel = new PamBorderPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		int layout = 1;
		int extraInset = 0;
		if (layout == 0) {
			northPanel.add(BorderLayout.CENTER, summaryPanel.getComponent());
			northPanel.add(BorderLayout.EAST, scalePanel.getComponent());
		}
		else if (layout == 1) {
			JPanel hiddenPanel = new JPanel(new BorderLayout());
			hiddenPanel.add(BorderLayout.CENTER, summaryPanel.getComponent());
			hiddenPanel.add(BorderLayout.EAST, scalePanel.getComponent());
			HidingPanel hidingPanel = new HidingPanel(northPanel, hiddenPanel, HidingPanel.HORIZONTAL, true);
			hidingPanel.setTitle("Data summary and time scales");
			northPanel.add(BorderLayout.CENTER, hidingPanel);
			extraInset = -hidingPanel.getHideButton().getPreferredSize().width;
		}
		else {
			HidingDialogPanel hidingSummary = new HidingDialogPanel(CornerLayoutContraint.FIRST_LINE_START, summaryPanel);
			northPanel.add(BorderLayout.WEST, hidingSummary.getShowButton());
			hidingSummary.getShowButton().setToolTipText("Show Data Summary");
			HidingDialogPanel hidingScale = new HidingDialogPanel(CornerLayoutContraint.FIRST_LINE_END, scalePanel);
			northPanel.add(BorderLayout.EAST, hidingScale.getShowButton());
			hidingScale.getShowButton().setToolTipText("Show Time Scale Slider");
		}
		
		this.add(BorderLayout.NORTH, northPanel);
		this.add(BorderLayout.CENTER, scrollingDataPanel.getPanel());

		Insets panelInsets = new Insets(0, 70, 0, 10);
		if (layout == 1) {
			panelInsets.left += extraInset;
		}
		northPanel.setBorder(new EmptyBorder(panelInsets));
	}
	
	/**
	 * Create a new set of data graphs to go into the panel. 
	 * @return
	 */
	public int createDataGraphs() {
		/**
		 * First check the limits of the database and binary stores. 
		 */
		setGraphDimensions();
		
		return scrollingDataPanel.createDataGraphs();
	}
	
	/**
	 * Based on the scale and on the total length of data
	 * work out how big the little panels need to be 
	 */
	private void setGraphDimensions() {
		long totalLength = dataMapControl.getLastTime() - dataMapControl.getFirstTime();
		graphDimension = new Dimension(2000, 100);
	}
	
	/**
	 * @return the graphDimension for use with the DataStreamPanels. 
	 */
	public Dimension getGraphDimension() {
		return graphDimension;
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getPanel() {
		return this;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Called from ScalePanel when anything 
	 * to do with scaling changes. 
	 */
	public void scaleChanged() {
		if (scalePanel == null || scrollingDataPanel == null) {
			return;
		}
		scalePanel.getParams(dataMapControl.dataMapParameters);
		scrollingDataPanel.scaleChange();
	}

	public void frameResized() {
		scrollingDataPanel.frameResized();
	}

	public void newDataSources() {
		scrollingDataPanel.newDataSources();
		summaryPanel.newDataSources();
	}

	public void newSettings() {
		scalePanel.setParams(dataMapControl.dataMapParameters);
	}

	/**
	 * Called when mouse moves over a data graph to set time
	 * on scale Panel. Set null to clear cursor info on panel.
	 * @param timeMillis time in millis or null. 
	 */
	public void dataGraphMouseTime(Long timeMillis) {
		summaryPanel.setCursorTime(timeMillis);
	}

	public void repaintAll() {
		scrollingDataPanel.repaintAll();
	}

	public void updateDataMap(OfflineDataMap dataMap, OfflineDataMapPoint dataMapPoint) {
		long currentLast = dataMapControl.getLastTime();
		if (dataMapPoint != null) {
			currentLast = Math.max(currentLast, dataMapPoint.getEndTime());
			dataMapControl.setLastTime(currentLast);
		}
		summaryPanel.newDataSources();
	}

	/**
	 * @return the summaryPanel
	 */
	public SummaryPanel getSummaryPanel() {
		return summaryPanel;
	}
	
}
