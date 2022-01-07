package dataPlots.layout;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dataPlots.TDControl;
import dataPlots.data.DataLineInfo;
import dataPlots.data.FoundDataUnit;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDDataProviderRegister;
import dataPlots.mouse.MouseSelectionListener;
import dataPlots.mouse.PlotMouseAdapter;
import dataPlots.mouse.PlotMouseListener;
import dataPlots.mouse.PlotMouseMotionListener;
import dataPlots.mouse.PlotZoomerAdapter;
import dataPlotsFX.data.TDScaleInfo;
import pamScrollSystem.PamScroller;
import Layout.PamAxis;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamView.PamColors.PamColor;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Panel for a data graph. A single graph may display data from multiple sources, but will only
 * show ones that have a common axis type at any one time. 
 * <br>To allow for multi channels of data, each graphs may contain multiple plots. Currently
 * the number of plots cannot be altered by the user, but can be altered by some of the 
 * TDDataInfo classes. This was primarily implemented to display spectrogram data on multiple 
 * panels. This has some advantages over putting in multiple graphs in that each plot within a 
 * graph will have the same size and basic data type.
 * @author Doug Gillespie
 *
 */
public class TDGraph {

	private TDControl tdControl;

	private PamPanel graphOuterPanel;

	private ArrayList<GraphPlotPanel> graphPlotPanels;

	private int graphNumber;

	private PamAxis graphAxis;

	private ArrayList<TDDataInfo> dataList;

	private boolean showTopLabel=true; 

	private JLabel topLabel;

	protected GraphParameters graphParameters = new GraphParameters();

	private TDGraphHidingDialog hidingDialog;

	/**
	 * Mouse motion listeners-used primarily for interacting with detection data displayed on graph
	 */
	private ArrayList<PlotMouseMotionListener> plotMouseMotionListeners;

	/**
	 * Mouse plot Listeners--used primarily for interacting with detection data displayed on grap
	 */
	private ArrayList<PlotMouseListener> plotMouseListeners;

	/**
	 * Plot zoomer adapters -each zoomer adapter controls the zoomers for this graph. Likely to be only one zoome rper graph
	 */
	private ArrayList<PlotZoomerAdapter> plotZoomerAdapters;

	/**
	 * List of selected dataunits. More than one data unit may be selected on a graph. 
	 */
	ArrayList<FoundDataUnit> selectedDataUnits; 

	/**
	 * List of unique available units for plotting. 
	 */
	private ArrayList<String> availableAxisNames = new ArrayList<>();

	private HidingDialogPanel hidingDialogPanel;

	private JPanel graphInnerPanel;

	private CompoundHidingDialog compoundHidingDialog;

	/**
	 * Button which sits on west side of display and opens dialg to change the axis of the graph.
	 */
	private JButton hidingPanelButton;
	
	/**
	 * Button which sits on the east side of the tdgraph and opens up dialogs to change settings for the currently displayed data unit types. 
	 */
	private JButton compoundShowButton;

	private TDLayeredPane layeredPane;

	//	/**
	//	 * List of zoomers for each panel; 
	//	 */
	//	private ArrayList<Zoomer> tdGraphZoomer;
	//	
	//	private ArrayList<TDGraphZoomer> tdGraphZoomable;

	private PamPanel topPanel;

	public TDGraph(TDControl tdControl, int graphNumber) {
		super();
		this.tdControl = tdControl;
		this.graphNumber = graphNumber;

		dataList = new ArrayList<TDDataInfo>();

		//initialise selected data units. 
		selectedDataUnits= new ArrayList<FoundDataUnit>();
		
		//create the layered pane.
		layeredPane = new TDLayeredPane();
		CornerLayoutContraint clc = new CornerLayoutContraint();
		layeredPane.setLayout(new CornerLayout(clc));

		hidingDialog = new TDGraphHidingDialog(tdControl, this);
		compoundHidingDialog = new CompoundHidingDialog(this);
		
		graphOuterPanel = new PamPanel(PamColor.BORDER);
		graphOuterPanel.setLayout(new BorderLayout());
		graphOuterPanel.add(BorderLayout.CENTER, layeredPane);

		clc.anchor = CornerLayoutContraint.FIRST_LINE_START;
		hidingDialogPanel = new HidingDialogPanel(clc.anchor, hidingDialog);
		hidingDialogPanel.setOpacity(0.75f);
		layeredPane.add(hidingDialogPanel.getShowButton(), clc, JLayeredPane.POPUP_LAYER);

		graphInnerPanel = new JPanel();
		graphInnerPanel.setLayout(new GridLayout(0, 1));
		
		clc.anchor=CornerLayoutContraint.FILL;	
		layeredPane.add(graphInnerPanel, clc, JLayeredPane.FRAME_CONTENT_LAYER);

		//add a label into the top panel.
		topLabel = new JLabel("Graph panel " + graphNumber);
		topPanel=new PamPanel(new BorderLayout());
		if (!showTopLabel) graphOuterPanel.add(BorderLayout.NORTH, topPanel);

		//here we add plot panels to the graph and zoomers. 
		graphPlotPanels = new ArrayList<>();
		// add one panel by default
		for (int i = 0; i < 1; i++) {
			GraphPlotPanel gp = new GraphPlotPanel(i);
			graphPlotPanels.add(gp);
			graphInnerPanel.add(BorderLayout.CENTER, gp);
		}
		
		graphAxis = new PamAxis(0, 1, 0, 1, 0, 10, PamAxis.ABOVE_LEFT, "Graph Units", PamAxis.LABEL_NEAR_CENTRE, "%4d");
		graphAxis.setCrampLabels(true);

		/////FIXME-test mouse listener and zoomer. Should be removed in the future so default is not to have this functionality/////
		MouseSelectionListener mouseSelectionListener=new MouseSelectionListener(); 
		addPlotMouseListener(mouseSelectionListener);
		addPlotMouseMotionListener(mouseSelectionListener);
		//add zoomables 
		addPlotZoomer(new PlotZoomerAdapter(this)); 
		//////////////////////////////////////////////

		layoutGraph();
	} 
	
	/**
	 * Class to make sure the layered pane properly repaints oberlayed buttons. 
	 * @author Jamie Macaulay
	 *
	 */
	class TDLayeredPane extends JLayeredPane{
		
		private static final long serialVersionUID = 1L;

		@Override
		public void repaint(){
			super.repaint();
			repaintButtons();
		}
		
		@Override
		public void repaint(long millis){
			super.repaint(millis);
			repaintButtons();
		}
		
		protected void repaintButtons(){
			if (compoundShowButton!=null) compoundShowButton.repaint();
			if (hidingPanelButton!=null) hidingPanelButton.repaint();
		}
	}


	/**
	 * Add a mouse plot adapter to each panel on the graph.
	 * @param plotMouseAdapater- an extension of MouseAdapter desgined for primarily for picking detections on a graph, however maybe extended to many other functions. 
	 */
	public void addPlotListener(PlotMouseAdapter plotMouseAdapater){
		addPlotMouseListener(plotMouseAdapater);
		addPlotMouseMotionListener(plotMouseAdapater);
	}

	/**
	 * Add a plot zoomer adapter to the graph. Zoomers essentially control marking out of multiple detections using some of kind of selection tool e.g. dragging a box. 
	 * @param plotZoomerAdapter - plot zoomer adapter. 
	 */
	public void addPlotZoomer(PlotZoomerAdapter plotZoomerAdapter){
		if (plotZoomerAdapters==null) plotZoomerAdapters=new ArrayList<PlotZoomerAdapter>(); 
		this.plotZoomerAdapters.add(plotZoomerAdapter); 
	}

	/**
	 * Clear all the zoomers associated with this graph
	 */
	public void removeAllZoomers(){
		plotZoomerAdapters=new ArrayList<PlotZoomerAdapter>(); 
	}

	/**
	 * Get a zoomer for this graph. 
	 * @param i which zoomer to get
	 * @return a zoomer from the list of zoomers associated with this graph. 
	 */
	public PlotZoomerAdapter getPlotZoomer(int i){
		return plotZoomerAdapters.get(i);
	}

	/**
	 * Get the number of PlotZoomerAdapters associated with this graph
	 * @return the number of PlotZoomerAdapters
	 */
	public int getNumPlotZoomers(){
		if (plotZoomerAdapters==null) return 0; 
		return plotZoomerAdapters.size();
	}

	//	/**
	//	 * Clear any existing zoomables and add new zoomables to each plot panel. A zoomable is a legacy name for a class which alows
	//	 * users to mark out multiple detections on a display. 
	//	 */
	//	private void addZoomables(){
	//		tdGraphZoomer=new ArrayList<Zoomer>();
	//		tdGraphZoomable=new ArrayList<TDGraphZoomer>();
	//		TDGraphZoomer tempZoomer;
	//		for (int i = 0; i < graphPlotPanels.size(); i++) {
	//			//add zoomables
	//			tdGraphZoomer.add(new Zoomer(tempZoomer=new TDGraphZoomer(this, i),graphPlotPanels.get(i))); 
	//			tdGraphZoomable.add(tempZoomer);
	//		}
	//	}

	//	/**
	//	 * Get the zoomer for a specific panel 
	//	 * @param iPanel the panel number
	//	 * @return zoomer for the panel 
	//	 */
	//	public Zoomer getZoomer(int iPanel){
	//		if (tdGraphZoomer==null) return null; 
	//		return tdGraphZoomer.get(iPanel);
	//	} 

	/**
	 * Set a panel at the top of the graph;
	 * @param topPanel
	 */
	public void setTopPanel(PamPanel topPanel){
		graphOuterPanel.invalidate();
		graphOuterPanel.remove(this.topPanel);
		this.topPanel=topPanel;
		graphOuterPanel.add(BorderLayout.NORTH, topPanel);
		graphOuterPanel.validate();
	}

	//	/**
	//	 * Get the td zoomable for a panel 
	//	 * @param iPanel the panel number
	//	 * @return zoomable for the panel 
	//	 */
	//	public TDGraphZoomer getZoomable(int iPanel){
	//		if (tdGraphZoomable==null) return null; 
	//		return tdGraphZoomable.get(iPanel);
	//	} 

	public void addDataItem(TDDataProvider dataProvider) {
		addDataItem(dataProvider.createDataInfo(this));
	}

	/**
	 * Add a data item to be plotted on this display
	 * @param dataInfo item to add
	 */
	public void addDataItem(TDDataInfo dataInfo) {
		dataList.add(dataInfo);
		tdControl.subscribeScrollDataBlocks();
		checkAxis();
		displayGraphInfo();
		listAvailableAxisNames();
	}

	public void removeGraph() {
		//		for (TDDataInfo dataInfo:dataList) {
		//			removeDataItem(dataInfo);
		//		}
		while (dataList.size() > 0) {
			removeDataItem(dataList.get(dataList.size()-1));
		}
	}

	/**
	 * Remove a data item from the plot list. 
	 * @param dataInfo item to remove. 
	 */
	public void removeDataItem(TDDataInfo dataInfo) {
		dataInfo.removeData();
		dataList.remove(dataInfo);
		tdControl.subscribeScrollDataBlocks();
		checkAxis();
		displayGraphInfo();
		listAvailableAxisNames();
	}



	/**
	 * Sort out the list of availableDataUnits
	 * and anything else that needs doing once
	 * data are added or removed.  
	 */
	public void listAvailableAxisNames() {
		availableAxisNames.clear();
		for (TDDataInfo dataInfo:dataList) {
			ArrayList<DataLineInfo> lineInfos = dataInfo.getDataLineInfos();
			for (DataLineInfo lineInfo:lineInfos) {
				if (!hasAvailableAxisName(lineInfo.name)) {
					availableAxisNames.add(lineInfo.name);
				}
			}
			hidingDialog.remakeDialog();
		}
		compoundHidingDialog.createDataList();
		if (hidingPanelButton != null) {
			layeredPane.remove(hidingPanelButton);
		}
		if (compoundHidingDialog.getComponent() != null) {
			CornerLayoutContraint clc = new CornerLayoutContraint();
			clc.anchor = CornerLayoutContraint.FIRST_LINE_END;
			HidingDialogPanel hidingDialogPanel = new HidingDialogPanel(clc.anchor, compoundHidingDialog);
			compoundHidingDialog.setHidingDialogPanel(hidingDialogPanel);
			hidingDialogPanel.setOpacity(0.75f);
			hidingDialogPanel.setSizingComponent(layeredPane);
			if (compoundShowButton!=null) layeredPane.remove(compoundShowButton);
			layeredPane.add(compoundShowButton=hidingDialogPanel.getShowButton(), clc, JLayeredPane.DEFAULT_LAYER);
		}
	}

	public boolean hasAvailableAxisName(String units) {
		for (String str:availableAxisNames) {
			if (str.equals(units)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the first scale information, i.e. the first data info that 
	 * can actually plot
	 * @return
	 */
	private TDScaleInfo findFirstActiveScaleinfo() {
		TDDataInfo firstDataInfo = findFirstActiveDataInfo();
		if (firstDataInfo == null) return null;
		return firstDataInfo.getScaleInformation(tdControl.getTdParameters().orientation, graphParameters.autoScale);
	}

	private TDDataInfo findFirstActiveDataInfo() {
		String axisName = null;
		if (graphAxis != null) {
			axisName = graphAxis.getLabel();
		}
		for (TDDataInfo dataInfo:dataList) {
			if (dataInfo.hasAxisName(axisName)) {
				return dataInfo;
			}
		}
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}
	
	public void checkAxis() {
//		if (dataList.size() == 0) {
//			return;
//		}
//		TDScaleInformation scaleInfo = dataList.get(0).getScaleInformation(tdControl.getTdParameters().orientation, graphParameters.autoScale);
		
		TDScaleInfo scaleInfo = findFirstActiveScaleinfo();
		if (scaleInfo == null) {
			return;
		}
		setNumberOfPlots(scaleInfo.getNPlots());
		double axisMin = 0;
		double axisMax = 0;
		
		if (scaleInfo != null) {
			axisMin = scaleInfo.getMinVal();
			axisMax = scaleInfo.getMaxVal();
		}
		
		for (int i = 1; i < dataList.size(); i++) {
			TDDataInfo dataInfo = dataList.get(i);
			if (dataInfo.isShowing() == false) {
				continue;
			}
		
			scaleInfo = dataList.get(i).getScaleInformation(tdControl.getTdParameters().orientation, graphParameters.autoScale);
			if (scaleInfo == null) {
				continue;
			}
			
			
			axisMin = Math.min(axisMin, scaleInfo.getMinVal());
			axisMax = Math.max(axisMax, scaleInfo.getMaxVal());
		}
		
		//now if the reverse axis is true 
		
		graphAxis.setRange(axisMin, axisMax);
//		graphAxis.setLabel(dataList.get(0).getCurrentDataUnits().unit);
		graphAxis.setFractionalScale(true);
//		setAxisName(graphParameters.currentAxisName);

	}

	private void displayGraphInfo() {
		if (showTopLabel) topLabel.setText(getGraphLabel());
		else topLabel.setText("");
	}



	public String getGraphLabel() {
		if (dataList.size() == 0) {
			return "No data selected";
		}
		else {
			String dataLabel = dataList.get(0).getDataName();
			for (int i = 1; i < dataList.size(); i++) {
				dataLabel += "; " + dataList.get(i).getDataName();
			}
			return dataLabel;
		}
	}

	/**
	 * Can be called at any time the orientation changes to 
	 * re-do the layout of the graph windows. 
	 */
	public void layoutGraph() {
		int orientation = tdControl.getTdParameters().orientation;
		checkAxis();
		if (orientation == PamScroller.HORIZONTAL) {
			graphInnerPanel.setLayout(new GridLayout(0, 1));
			graphOuterPanel.setBorder(null);
			graphAxis.setTickPosition(PamAxis.ABOVE_LEFT);
		}
		else {
			graphOuterPanel.setBorder(new EmptyBorder(0, 1, 0, 1));
			graphAxis.setTickPosition(PamAxis.BELOW_RIGHT);
			graphInnerPanel.setLayout(new GridLayout(1, 0));
		}
	}

	public int getAxisExtent(Graphics g) {
		if (graphAxis == null) {
			return 0;
		}
		/*
		 * Might be good to first work out the extent by resetting the format 
		 * and the decimal point needs of the axis labels. 
		 */
		String newForm = graphAxis.getAutoLabelFormat();
		//		System.out.println(newForm);
		graphAxis.setFormat(newForm);
		return graphAxis.getExtent(g);
	}


	/**
	 * @return the graphOuterPanel
	 */
	public PamPanel getGraphOuterPanel() {
		return graphOuterPanel;
	}

	/**
	 * Get the total number of graph plot panels. 
	 * @return number of panels
	 */
	public int getNumPlotPanels() {
		return graphPlotPanels.size();
	}
	/**
	 * @param iPanel index of the panel. 
	 * @return the graphPlotPanel
	 */
	public GraphPlotPanel getGraphPlotPanel(int iPanel) {
		return graphPlotPanels.get(iPanel);
	}

	/**
	 * @return the graphNumber
	 */
	public int getGraphNumber() {
		return graphNumber;
	}

	/**
	 * @return the graphAxis
	 */
	public PamAxis getGraphAxis() {
		return graphAxis;
	}

	/**
	 * Subscribe data blocks to the time scroller system 
	 * so their data get loaded in viewer mode. 
	 * @param timeScroller
	 */
	public void subscribeScrollDataBlocks(PamScroller timeScroller) {
		for (TDDataInfo dataInfo:dataList) {
			timeScroller.addDataBlock(dataInfo.getDataBlock());			
			if (!tdControl.isViewer()) {
				PamDataBlock sourceBlock = dataInfo.getDataBlock().getSourceDataBlock();
				if (sourceBlock != null) {
					//					dataInfo.getDataBlock().addObserver(tdControl.getDataObserver(), false);
					sourceBlock.addObserver(tdControl.getDataObserver(), false);
				}
			}
		}
	}

	public class GraphPlotPanel extends PamPanel {

		private static final long serialVersionUID = 1L;

		private int plotNumber;

		private BufferedImage bufferedImage;

		public GraphPlotPanel(int plotNumber) {
			super(PamColor.PlOTWINDOW);
			this.plotNumber = plotNumber;
			setToolTipText(" ");
			//			setLayout(new CornerLayout(null));
			this.setBorder(PamBorder.createInnerBorder());
			GraphMouse graphMouse = new GraphMouse(this);
			addMouseListener(graphMouse);
			addMouseMotionListener(graphMouse);
		}

		/**
		 * Fill the main buffered image. 
		 */
		public void fillDataImage() {
			//System.out.println("GraphLotPanel: redraw image");
			checkImage();
			bufferedImage.getGraphics().setColor(getBackground());
			int height=bufferedImage.getHeight();
			bufferedImage.getGraphics().fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
			Rectangle r = this.getBounds();
			synchronized (dataList) {
				for (TDDataInfo dataInfo:dataList) {

					if (!dataInfo.isShowing()) {
						continue;
					}
					DataLineInfo lineInfo = dataInfo.getCurrentDataLine();
					if (lineInfo == null) {
						continue;
					}
					if (lineInfo.name.equals(graphParameters.currentAxisName) == false) {
						continue;
					}
					paintData(bufferedImage.getGraphics(), r, dataInfo, false);
				}
			}
		}
		
		/**
		 * Check the image has the same dimension as the panel. 
		 * @return true if it's changed, false otherwise. 
		 */
		private boolean checkImage() {
			Rectangle r = this.getBounds();
			Insets i = getInsets();
			int w = Math.max(r.width-i.right-i.left,1);
			int h = Math.max(r.height-i.top-i.bottom,1);
			if (bufferedImage == null || bufferedImage.getWidth() != w || bufferedImage.getHeight() != h) {
				bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				return true;
			}
			return false;
		}

//		private long lastPaint = 0;
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					RenderingHints.VALUE_ANTIALIAS_ON);
//			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
//						RenderingHints.VALUE_RENDER_QUALITY);
//			long now = System.currentTimeMillis();
//			if (now - lastPaint < 100) return;
//			System.out.println("Millis since last paint of plot panel = " + new Long(now-lastPaint).toString());
//			lastPaint = now;
			if (checkImage()) {
				//if the image size has changed, will have to repaint it. 
				fillDataImage();
			}
			super.paint(g);
			Insets i = getInsets();
			g.drawImage(bufferedImage, i.left, i.top, getWidth()-i.left-i.right, getHeight()-i.top-i.bottom, 
					0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);

			/**
			 * Now draw any other information, such as highlighted data units and zoom boxes
			 * on top of the main image. In this way the main image will only get recreated
			 * if one of the repaint(...) functions is called or if th epanel size has 
			 * changed. 
			 */
			overlayHighlights(g);
		}

		/**
		 * Overlay graphics on the image without redrawing all the data units. 
		 * @param g
		 */
		private void overlayHighlights(Graphics g){
			Rectangle r = this.getBounds();
			for (TDDataInfo dataInfo:dataList) {
				if (!dataInfo.isShowing()) {
					continue;
				}
				DataLineInfo lineInfo = dataInfo.getCurrentDataLine();
				if (lineInfo == null) {
					continue;
				}
				if (lineInfo.name.equals(graphParameters.currentAxisName) == false) {
					continue;
				}
				//paint highlighted data
				paintData(g, r, dataInfo, true);
				//paint zoomer data
				paintZoomerData(g);
			}

		}

		/**
		 * Paint zoomer selection data onto the buffered image. 
		 */
		private void paintZoomerData(Graphics g){
			if (plotZoomerAdapters==null) return; 
			//paint zoom shapes
			for (int i=0; i<plotZoomerAdapters.size(); i++){
				if (plotZoomerAdapters.get(i).getZoomable(plotNumber).isComplete()) plotZoomerAdapters.get(i).getZoomer(plotNumber).paintShape( g, this, true);
				plotZoomerAdapters.get(i).getZoomer(plotNumber).paintShape( g, this, false);
			}	
		}

		/**
		 * Find the closest data unit to a point on the graph. 
		 * @param pt- point on the graph panel.
		 * @return the closest data unit. 
		 */
		public FoundDataUnit findClosestUnit(Point pt) {
			double minDist = Integer.MAX_VALUE;
			double dist;
			FoundDataUnit foundDataUnit = new FoundDataUnit(null, null, FoundDataUnit.SINGLE_SELECTION);
			ListIterator<PamDataUnit> it;
			PamDataUnit dataUnit;
			Point dataPoint;
			//loop through all the different types of data that is displayed on the graph
			for (TDDataInfo dataInfo:dataList) {
				synchronized (dataInfo.getDataBlock().getSynchLock()) {
					it = dataInfo.getDataBlock().getListIterator(0);
					while (it.hasNext()) {
						dataUnit = it.next();
						dataPoint = getDataUnitPoint(dataInfo, dataUnit);
						if (dataPoint == null) continue;
						dist = Math.pow(dataPoint.x-pt.x,2) + Math.pow(dataPoint.y-pt.y,2);
						if (dist < minDist) {
							minDist = dist;
							foundDataUnit.dataInfo = dataInfo;
							foundDataUnit.dataUnit = dataUnit;
							foundDataUnit.distance = dist;
						}
					}
				}
			}
			foundDataUnit.distance = Math.sqrt(foundDataUnit.distance);
			return foundDataUnit;
		}
		
		/**
		 * Find all data units within the current marked area on the graph panel. 
		 * @param zoomer to use for the graph. 
		 * @return an array of all data units found in the marked area. Note these are wrapped within a FoundDataUnit class which provide a bit more info on data info. 
		 */
		public ArrayList<FoundDataUnit> findUnitsWithinMark(PlotZoomerAdapter zoomer){
			ArrayList<FoundDataUnit> unitsInMark=new ArrayList<FoundDataUnit>();
			ListIterator<PamDataUnit> it;
			PamDataUnit dataUnit;
			FoundDataUnit foundDataUnit;
			//loop through all the different types of data that is displayed on the graph
			for (TDDataInfo dataInfo:dataList) {
				synchronized (dataInfo.getDataBlock().getSynchLock()) {
					it = dataInfo.getDataBlock().getListIterator(0);
					while (it.hasNext()) {
						dataUnit = it.next();
						//check if this data unit is in a marked area
						if (zoomer.dataUnitInMarkArea(dataInfo, dataUnit, plotNumber)){
							//create a found data unit
							foundDataUnit = new FoundDataUnit(dataInfo, dataUnit, FoundDataUnit.MARKED_AREA_DETECTION);
							unitsInMark.add(foundDataUnit);
						}
					}
				}
			}
			return unitsInMark;
		} 

		/**
		 * Paint specified data set. 
		 * @param g - graphics handle.  
		 * @param dataInfo
		 */
		private void paintData(Graphics g, Rectangle windowRect, TDDataInfo dataInfo, boolean highlight) {
			PamDataBlock<PamDataUnit> dataBlock = dataInfo.getDataBlock();

			long scrollStart = tdControl.getTimeScroller().getValueMillis();
			//			System.out.println("Paint start at " + PamCalendar.formatTime(scrollStart));
			double tScale;
			if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
				tScale = getWidth() / (tdControl.getTimeRangeSpinner().getSpinnerValue() * 1000.); 
			}
			else {
				tScale = getHeight() / (tdControl.getTimeRangeSpinner().getSpinnerValue() * 1000.); 
			}
			//					tdControl.getTdAxes().getTimeAxis().getPosition(secs);

			if (!highlight) {
				dataInfo.drawData(plotNumber, g, windowRect, tdControl.getTdParameters().orientation, 
					tdControl.getTdAxes().getTimeAxis(), scrollStart, graphAxis);
			}
			//			scadataInfo.getScaleInformation();
			else dataInfo.drawHighLightData(plotNumber, g, windowRect, tdControl.getTdParameters().orientation, 
					tdControl.getTdAxes().getTimeAxis(), scrollStart, graphAxis);
		}

		/**
		 * Get the pixel location of a data unit on the graph. 
		 * @param dataInfo
		 * @param dataUnit
		 * @return point of the data unit in pixels- pixels represent pixels on the tdGraph, not the entire screen. 
		 */
		public Point getDataUnitPoint(TDDataInfo dataInfo, PamDataUnit dataUnit) {
			Double val = dataInfo.getDataValue(dataUnit);
			if (val == null) {
				return null;
			}
			double dataPixel = graphAxis.getPosition(val);
			// convert to seconds
			long scrollStart = tdControl.getTimeScroller().getValueMillis();
			double secs = (dataUnit.getTimeMilliseconds() - scrollStart) / 1000.;
			double tC = tdControl.getTdAxes().getTimeAxis().getPosition(secs);
			if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
				return new Point((int) tC, (int) dataPixel);
			}
			else {
				return new Point((int) dataPixel, (int) tC);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			FoundDataUnit foundDataUnit = findClosestUnit(event.getPoint());
			if (foundDataUnit == null || foundDataUnit.dataUnit == null || foundDataUnit.distance > 20) {
				return null;
			}
			return foundDataUnit.dataInfo.getToolTipText(foundDataUnit.dataUnit);
		}

		/**
		 * Repaint the panel without repainting the main buffered image representing
		 * the data. This is so that highlighted data units and zoom boxes can 
		 * be efficiently redrawn without doing the whole data thing. 
		 */
		public void repaintHighlights() {
			super.repaint();
			layeredPane.repaint();
		}

		/**
		 * 
		 * Repaint the panel without repainting the main buffered image representing
		 * the data. This is so that highlighted data units and zoom boxes can 
		 * be efficiently redrawn without doing the whole data thing. 
		 * @param tm paint to re-occur within tm milliseconds. 
		 */
		public void repaintHighlights(long tm) {
			super.repaint(tm);
			layeredPane.repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#repaint()
		 */
		@Override
		public void repaint() {
			fillDataImage();
			super.repaint();
			layeredPane.repaint();
//			long now = System.currentTimeMillis();
//			System.out.println("TDGraph repaint called after millis " + new Long(now-rl));
//			rl = now;
		}
		long rl = 0;

		/* (non-Javadoc)
		 * @see java.awt.Component#repaint(int, int, int, int)
		 */
		@Override
		public void repaint(int arg0, int arg1, int arg2, int arg3) {
			fillDataImage();
			super.repaint(arg0, arg1, arg2, arg3);
			layeredPane.repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#repaint(long)
		 */
		@Override
		public void repaint(long arg0) {
			fillDataImage();
			super.repaint(arg0);
			layeredPane.repaint();
//			long now = System.currentTimeMillis();
//			System.out.println("TDGraph repaint(...) called after millis " + new Long(now-lllll));
//			lllll = now;
		}
//		long lllll;
	}

	/**
	 * Get notifications from the main controller.
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		for (TDDataInfo dataInfo:dataList) {
			dataInfo.notifyModelChanged(changeType);
		}
		switch (changeType) {
		case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			newDataLoaded(changeType);
			break;
		}
	}

	private void newDataLoaded(int changeType) {
		checkDataSelection();
		checkAxis();
		repaint(0);
	}

	/**
	 * Called when new data are loaded or when available plottable
	 * things have changes to ensure that the data line is actually selected. 
	 */
	private void checkDataSelection() {
		setAxisName(graphParameters.currentAxisName);
	}

	/**
	 * List containing info on data units which can be displayed on the graph. 
	 * @return the tdDataInfo data list. 
	 */
	public ArrayList<TDDataInfo> getDataList() {
		return dataList;
	}

	/**
	 * @return the availableDataUnits
	 */
	protected ArrayList<String> getAvailableDataUnits() {
		return availableAxisNames;
	}

	/**
	 * Get a list of data sources that can provide data to the 
	 * given axis name. 
	 * @param axisName
	 * @return a list of data sources that can provide data for that axis type. 
	 */
	public String getDataNamesForAxis(String axisName) {
		String str = null;
		for (TDDataInfo dataInfo:dataList) {
			if (dataInfo.hasAxisName(axisName) == false) {
				continue;
			}
			if (str == null) {
				str = new String(dataInfo.getDataName());
			}
			else {
				str += "; " + dataInfo.getDataName();
			}
		}
		return str;
	}

	/**
	 * Called when the user sets the type of axis units. 
	 * These may not be available in all displayed data, so 
	 * when this is set, some things may stop showing !
	 * @param unitsType
	 */
	public void setAxisName(String axisName) {
		for (TDDataInfo dataInfo:dataList) {
			dataInfo.setCurrentAxisName(axisName);
		}
		if (axisName == null && availableAxisNames.size() > 0) {
			axisName = availableAxisNames.get(0);
		}
		graphParameters.currentAxisName = axisName;
		graphAxis.setLabel(axisName);
		// check the number of panels.
		
		checkAxis();
//		System.out.println("setAxisName() "+System.currentTimeMillis());
		tdControl.repaintAll(100);
	}

	/**
	 * Set the number of plots within the graph. By default this
	 * is one, but some types of data will contain functinality which 
	 * can change this. 
	 * @param numPlots number of plots to show. 
	 */
	public void setNumberOfPlots(int numPlots) {
		if (numPlots == graphPlotPanels.size()) {
			return;
		}
		// first remove unwanted plots from the graphInnerPanel
		GraphPlotPanel aPlot;
		while (graphPlotPanels.size() > numPlots) {
			aPlot = graphPlotPanels.get(graphPlotPanels.size()-1);
			graphPlotPanels.remove(aPlot);
			graphInnerPanel.remove(aPlot);
		}
		// then add any we now need
		while (graphPlotPanels.size() < numPlots) {
			graphInnerPanel.add(aPlot = new GraphPlotPanel(graphPlotPanels.size()));	
			graphPlotPanels.add(aPlot);		
		}
		for (int i=0; i<plotZoomerAdapters.size(); i++){
			plotZoomerAdapters.get(i).addZoomables();
		}
		graphInnerPanel.invalidate();
	}

	/**
	 * Called when the user selects a specific data line for a specific 
	 * data type. 
	 * @param dataInfo
	 * @param dataLine
	 */
	public void selectDataLine(TDDataInfo dataInfo, DataLineInfo dataLine) {
		dataInfo.selectDataLine(dataLine);
		setAxisName(dataLine.name);
		checkAxis();
		tdControl.repaintAll();
	}

	/**
	 * @return the graphParameters
	 */
	public GraphParameters getGraphParameters() {
		return graphParameters;
	}

	/**
	 * A bit different to the standard getter in that this
	 * only gets called just before teh configuration is
	 * serialized into the psf. It's time to pull any configuration 
	 * information out about every line drawn on this boomin' think !
	 * @return graph parameters ready to serialised. 
	 */
	public GraphParameters prepareGraphParameters() {
		graphParameters.dataListInfos = new ArrayList<>();
		for (TDDataInfo dataInfo:dataList) {
			DataListInfo dli = new DataListInfo(dataInfo.getDataProvider().getClass(), 
					dataInfo.getDataName(), dataInfo.getCurrentDataLineIndex(), dataInfo.getStoredSettings());
			dli.isShowing = dataInfo.isShowing();
			graphParameters.dataListInfos.add(dli);
		}
		return graphParameters;
	}
	/**
	 * This only gets called when the serialised settings from psf file 
	 * have been loaded, not at any other time !
	 * @param graphParameters the graphParameters to set
	 */
	public void setGraphParameters(GraphParameters graphParameters) {
		this.graphParameters = graphParameters;
		if (graphParameters.dataListInfos != null) {
			for (DataListInfo listInfo:graphParameters.dataListInfos) {
				TDDataProvider dataProvider = TDDataProviderRegister.getInstance().findDataProvider(listInfo.providerClass, listInfo.providerName);
				if (dataProvider == null) {
					System.err.println("Unable to find data plot provider " + listInfo.providerName);
					continue;
				}
				TDDataInfo dataInfo = dataProvider.createDataInfo(this);
				dataInfo.setShowing(listInfo.isShowing);
				//				dataInfo.setCurrentAxisName(graphParameters.currentAxisName);
				addDataItem(dataInfo);
				if (listInfo.listSettings != null) {
					dataInfo.setStoredSettings(listInfo.listSettings);
				}
			}
		}
		setAxisName(graphParameters.currentAxisName);
	}

	/**
	 * Show the options dialog for a particular data type
	 * @param dataInfo
	 */
	public void showOptionsDialog(TDDataInfo dataInfo) {
		boolean ok = dataInfo.editOptions(PamController.getMainFrame());
		if (ok) {
			checkAxis();
			tdControl.repaintAll();
		}
	}

	/**
	 * @return the tdControl
	 */
	public TDControl getTdControl() {
		return tdControl;
	}

	/**
	 *  get the number of pixels on the time axis of a plot
	 * @return  the number of pixels on the time axis of a plot
	 */
	public int getTimePixels() {
		if (graphPlotPanels.size() == 0) {
			return 0;
		}
		GraphPlotPanel pp = graphPlotPanels.get(0);
		Insets pi = pp.getInsets();
		if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
			return pp.getWidth() - pi.left - pi.right;
		}
		else {
			return pp.getHeight() - pi.top - pi.bottom;
		}
	}

	/**
	 * Called in viewer mode when the time scroller moves
	 * @param valueMillis new scroll value in millis
	 */
	public void timeScrollValueChanged(long valueMillis) {
		for (TDDataInfo anInfo:dataList) {
			anInfo.timeScrollValueChanged(valueMillis);
		}
	}

	/**
	 * Called in viewer mode when the time scroll range moves. 
	 * @param minimumMillis new minimum in millis
	 * @param maximumMillis new maximum in millis. 
	 */
	public void timeScrollRangeChanged(long minimumMillis, long maximumMillis) {
		for (TDDataInfo anInfo:dataList) {
			anInfo.timeScrollRangeChanged(minimumMillis,  maximumMillis);
		}		
	}

	/**
	 * Repaint all graph components. 
	 * @param millis paint delay
	 */
	public void repaint(long millis) {
		getGraphOuterPanel().repaint(millis);
		for (int i = 0; i < getNumPlotPanels(); i++) {
			getGraphPlotPanel(i).repaint(millis);
		}
	}

	/**
	 * Called when the time range spinner on the main display panel changes. 
	 * @param oldValue old value (seconds)
	 * @param newValue new value (seconds)
	 */
	public void timeRangeSpinnerChange(double oldValue, double newValue) {
		for (TDDataInfo anInfo:dataList) {
			anInfo.timeRangeSpinnerChange(oldValue, newValue);
		}			
	}

	private class GraphMouse extends MouseAdapter {

		private GraphPlotPanel graphPlotPanel;

		public GraphMouse(GraphPlotPanel graphPlotPanel) {
			super();
			this.graphPlotPanel = graphPlotPanel;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseListener pm:plotMouseListeners) {
				pm.mouseClicked(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseListener pm:plotMouseListeners) {
				pm.mouseEntered(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseListener pm:plotMouseListeners) {
				pm.mouseExited(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseListener pm:plotMouseListeners) {
				pm.mousePressed(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseListener pm:plotMouseListeners) {
				pm.mouseReleased(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		//		@Override
		//		public void mouseWheelMoved(MouseWheelEvent e) {
		//			// TODO Auto-generated method stub
		//			super.mouseWheelMoved(e);
		//		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseMotionListener pm:plotMouseMotionListeners) {
				pm.mouseDragged(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (plotMouseListeners == null) return;
			FoundDataUnit foundDataUnit = graphPlotPanel.findClosestUnit(e.getPoint());
			for (PlotMouseMotionListener pm:plotMouseMotionListeners) {
				pm.mouseMoved(TDGraph.this, graphPlotPanel.plotNumber, e, foundDataUnit);
			}
		}
	}

	/**
	 * Add a plot mouse listener. This is similar to a normal 
	 * mouse listener except that as well as the mouse event
	 * it gets the graph reference, panel number and any data unit close 
	 * to the mouse position.  
	 * @param plotMouseListener
	 */
	public void addPlotMouseListener(PlotMouseListener plotMouseListener) {
		if (plotMouseListeners == null) {
			plotMouseListeners = new ArrayList<>();
		}
		plotMouseListeners.add(plotMouseListener);
	}

	/**
	 * Remove a plot mouse listener. 
	 * @param plotMouseListener
	 * @return
	 */
	public boolean removePlotMouseListener(PlotMouseListener plotMouseListener) {
		if (plotMouseListeners == null) {
			return false;
		}
		return plotMouseListeners.remove(plotMouseListener);
	}

	/**
	 * Add a plot mouse motion listener. This is similar to a normal 
	 * mouse motion listener except that as well as the mouse event
	 * it gets the graph reference, panel number and any data unit close 
	 * to the mouse position.  
	 * @param plotMouseMotionListener
	 */
	public void addPlotMouseMotionListener(PlotMouseMotionListener plotMouseMotionListener) {
		if (plotMouseMotionListeners == null) {
			plotMouseMotionListeners = new ArrayList<>();
		}
		plotMouseMotionListeners.add(plotMouseMotionListener);
	}

	/**
	 * Remove a plot mouse motion listener. 
	 * @param plotMouseMotionListener
	 * @return
	 */
	public boolean removePlotMouseMotionListener(PlotMouseMotionListener plotMouseMotionListener) {
		if (plotMouseMotionListeners == null) {
			return false;
		}
		return plotMouseMotionListeners.remove(plotMouseMotionListener);
	}

	/**
	 * Remove all plot mouse motion listeners from the tdGraph. 
	 * @return true of removed..false if the array holding listeners is currently null
	 */
	public boolean removeAllPlotMouseMotionListeners(){
		if (plotMouseMotionListeners == null) {
			return false;
		}
		plotMouseMotionListeners=new ArrayList<PlotMouseMotionListener>();
		return true; 
	}

	/**
	 * Remove all plot mouse listeners from the tdgraph.
	 * @return true of removed..false if the array holding listeners is currently null
	 */
	public boolean removeAllPlotMouseListeners(){
		if (plotMouseListeners == null) {
			return false;
		}
		plotMouseListeners=new ArrayList<PlotMouseListener>();
		return true; 
	}


	/**
	 * Set whether to show a top label for this graph. 
	 * @param showTopLabel - true to show top label
	 */
	public void setShowTopLabel(boolean showTopLabel) {
		this.showTopLabel = showTopLabel;
	}

	/**
	 * Get selected data units for this graph. 
	 * @return an array of selected data units. 
	 */
	public ArrayList<FoundDataUnit> getSelectedDataUnits() {
		return selectedDataUnits;
	}

	/**
	 * Set selected data units for the graph. 
	 * @param selectedDataUnits. Data units, which, if displayed, will be highlighted. 
	 */
	public void setSelectedDataUnits(ArrayList<FoundDataUnit> selectedDataUnits) {
		this.selectedDataUnits = selectedDataUnits;
	}

	/**
	 * Clear all selected data units. 
	 */
	public void clearSelectedDataUnits(){
		selectedDataUnits=new ArrayList<FoundDataUnit>();
	}

	/**
	 * Add a selected data unit to the current selected data unit list. 
	 * @param pamDataUnit
	 */
	public void addSelectedDataUnit(FoundDataUnit pamDataUnit){
		selectedDataUnits.add(pamDataUnit); 
	}
	
	/**
	 * Add a list of data units to the end current selected data units. 
	 * @param pamDataUnits
	 */
	public void addSelectedDataUnit(ArrayList<FoundDataUnit> pamDataUnits){
		selectedDataUnits.addAll(pamDataUnits); 
	}
	
	/**
	 * Check for duplicate values in the selected data units and delete.
	 */
	public void removeListDuplicates(ArrayList<FoundDataUnit> selectedDataUnits){

	    int size = selectedDataUnits.size();
	    int duplicates=0; 

	    for (int i = 0; i < size - 1; i++) {
	        // start from the next item after strings[i]
	        // since the ones before are checked
	        for (int j = i + 1; j < size; j++) {
	            // no need for if ( i == j ) here
	            if (!selectedDataUnits.get(j).dataUnit.equals(selectedDataUnits.get(i).dataUnit))
	                continue;
	            duplicates++;
	            selectedDataUnits.remove(j);
	            // decrease j because the array got re-indexed
	            j--;
	            // decrease the size of the array
	            size--;
	        } // for j
	    } // for i
		
	}
	
	/**
	 * Get the hiding panel which contains settings for all the current datablocks associated with the graph. 
	 * @return CompoundHidingDialog  for this graph. 
	 */
	public CompoundHidingDialog getCompoundHidingDialog() {
		return compoundHidingDialog;
	}

}
