package dataPlotsFX.layout;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.ExtMapMouseHandler;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.ExtPopMenuSimple;
import PamView.paneloverlay.overlaymark.MarkExtraInfo;
import PamView.paneloverlay.overlaymark.MarkExtraInfoSource;
import PamView.paneloverlay.overlaymark.OverlayMark;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;
import pamViewFX.fxNodes.pamScrollers.AbstractPamScrollerFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxPlotPanes.PamHiddenSidePane;
import soundPlayback.PlaybackProgressMonitor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDControl;
import dataPlotsFX.TDGraphParametersFX;
import dataPlots.layout.DataListInfo;
import dataPlotsFX.data.DataTypeInfo;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.data.TDScaleInfoData;
import dataPlotsFX.overlaymark.OverlayMarkerManager;
import dataPlotsFX.overlaymark.popUpMenu.TDPopUpMenuAdv;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.sound.SoundOutputManager;
import fftManager.FFTDataBlock;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;
import pamScrollSystem.coupling.ScrollerCoupling;

/**
 * A TDGraph allows users to plot multiple types of data on a single time based
 * graph. That means clicks, whistles, FFT, Ishmael detections etc. Each graph
 * can have multiple panels displaying different data. e.g a spectrogram will
 * display two channels on two separate panels (otherwise we're just averaging
 * data between channels) A bearing time display, on the other hand can display
 * any number of channels on just one panel.
 * 
 * @author Jamie Macaulay, Doug Gillespie.
 */
public class TDGraphFX extends PamBorderPane {

	/**
	 * List of data info for this graph. This is basically an array of wrapper
	 * classes for the data blocks that the graph displays.
	 */
	private ArrayList<TDDataInfoFX> dataList;

	/**
	 * ArrayList containing all the plot panels.
	 */
	private ArrayList<TDPlotPane> tdPlotPanels;

	// /**
	// * ArrayList containing all the plot panels.
	// */
	// private ArrayList<FoundDataUnitFX> selectedDataUnits;

	/**
	 * Reference to tdControl.
	 */
	private TDControl tdControl;

	/**
	 * Reference to the main display panel.
	 */
	private TDDisplayFX tdDisplay;

	/**
	 * The grid pane holds the one or more plot panels in the graph.
	 */
	private PamGridPane plotPanels;

	/**
	 * The layered pane contains all the plot panels but also any overlayed nodes,
	 * e.g buttons to open hiding dialogs.
	 */
	private PamHiddenSidePane stackPane;

	/**
	 * List of unique available units for plotting. We may have multiple types of
	 * data units, but those data units can have more than one type of measurement
	 * associated with them.
	 */
	private ArrayList<DataTypeInfo> availableAxisNames = new ArrayList<>();

	/**
	 * The data axis for this graph.
	 */
	private PamAxisFX graphAxis;

	/**
	 * The projector for the graph. Handles all time and data value to pixel (and
	 * vice versa) calculations.
	 */
	private TDProjectorFX graphProjector;

	/**
	 * The current scale info for the graph. This basically deals with the y axis (x
	 * axis when vertical), defines min/max limits and the plot panes that should be
	 * shown
	 */
	private TDScaleInfo currentScaleInfo;

	/**
	 * Pane which holds the axis.
	 */
	private PamAxisPane2 axisPanel;

	/**
	 * Class which holds information on settings for this particular graph.
	 */
	private TDGraphParametersFX graphParameters;

	/**
	 * The hiding tab pane which contains settings panes for data blocks associated
	 * with the graph.
	 */
	private TDHidingTabPane settingsTabs;

	/**
	 * Pane which allows users to select which type of data to display on the axis
	 * e.g. bearing, amplitude...
	 */
	private TDDataSelPaneFX tdAxisSelPane;

	/**
	 * Timer that repaints after time diff has been reached
	 */
	private Timeline timeline;

	/**
	 * Last time the graph was actually drawn.
	 */
	long lastTime = 0;

	/**
	 * Flag to indicate the display has a darker background. This would typically be
	 * a spectrogram (perhaps not an inverted grey) or if the user has a selected a
	 * darker colour for the display background
	 */
	public static final int DARK_TD_DISPLAY = 0;

	/**
	 * Flag to indicate the display has a lighter background. e.g. the default
	 * background for bearing time display.
	 */
	public static final int LIGHT_TD_DISPLAY = 1;

	/**
	 * This is the last visible time of the data.
	 */
	double scrollStart;

	/**
	 * Color of the line which shows current wrap location.
	 */
	private Paint wrapColor = Color.RED;

	/**
	 * The number of plot panels
	 */
	private IntegerProperty nPlots = new SimpleIntegerProperty(1);

	private int graphId;

	/**
	 * Manages overlay marks on the TDGraph.
	 */
	private OverlayMarkerManager overlayMarker;

	/**
	 * The limits before the last zoom;
	 */
	private double[] lastLimitsZoom = null;

	/**
	 * Pane with controls to change TDGraphParameter general settings.
	 */
	private TDGraphSettingsPane tdSettingsPane;

	/**
	 * Create the graph.
	 * 
	 * @param tdControl   - the controlled unit for the display
	 * @param mainDisplay - the main display holding this graph.
	 */
	public TDGraphFX(TDControl tdControl, TDDisplayFX mainDisplay, int graphId) {
		this.tdControl = tdControl;
		this.tdDisplay = mainDisplay;
		this.graphId = graphId;
		this.graphParameters = new TDGraphParametersFX();
		graphParameters.autoScale = false;

		// create an arrayList of dataInfos
		dataList = new ArrayList<TDDataInfoFX>();

		// create an ArrayList of the current plot panels.
		tdPlotPanels = new ArrayList<TDPlotPane>();

		// //create a list of currently selected data units.
		// selectedDataUnits= new ArrayList<FoundDataUnitFX>();

		// create pane for plot panels.
		plotPanels = new PamGridPane();

		// create the graph projector. Has to be done here, so also need to set in
		// marker.
		graphProjector = new TDProjectorFX(this);

		// create the overlay marker manager.
		overlayMarker = new OverlayMarkerManager(this);

		// create the Y axis for this panel.
		graphAxis = new PamAxisFX(0, 1, 0, 1, 0, 10, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE,
				"%4d");
		graphAxis.setCrampLabels(true);

		// create pane to hold axis.
		axisPanel = new PamAxisPane2(graphAxis, Side.LEFT);

		layoutPlotPanes(tdDisplay.getTDParams().orientation);

		/**
		 * Create the data selection hiding pane. This pane contains the controls to
		 * allow users to switch y axis unit (e.g. bearing, frequency etc.) and select
		 * which data blocks are enabled for viewing plus access settings for those data
		 * blocks.
		 */
		tdAxisSelPane = new TDDataSelPaneFX(this);
		tdAxisSelPane.setPrefWidth(250);
		// put pane inside a scroll pane for graph resizing
		PamScrollPane scrollPane = new PamScrollPane(tdAxisSelPane);
		// scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		// scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		/**
		 * Every data block added to the graph can have an associated settings node,
		 * which allows users to quickly change the properties of data units from that
		 * data block displayed on the graph. e.g. for the spectrogram there are a few
		 * slider bars allowing users to quickly change colour settings. The settings
		 * pane contains a tabbed pane which holds settings panes for subscribed data
		 * block to the graph.
		 */
		settingsTabs = new TDHidingTabPane(this);
		// settingsTabs.setMinHeight(1000);
		// settingsTabs.heightProperty().addListener((a,b,c)->{
		// System.out.println("Height: " + settingsTabs.getHeight() + " " +
		// settingsTabs.getHeight() );
		// });

		// put inside a scroll pane so tht on low dpi displays can still access controls
		PamScrollPane scrollPane2 = new PamScrollPane(settingsTabs);
		// scrollPane2.setFitToWidth(true);
		scrollPane2.setFitToHeight(true);
		scrollPane2.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane2.getStyleClass().clear();

		/**
		 * Create a stack pane to hold everything- means we can add overlay controls and
		 * buttons Note: icons are set later in setOverlayColour(LIGHT_TD_DISPLAY);
		 */
		stackPane = new PamHiddenSidePane(null, null, scrollPane, scrollPane2);

		// stackPane.setMinHidePaneHeight(300);
		stackPane.getChildren().add(plotPanels);
		plotPanels.toBack(); // need to send to back so hiding panes are not behind in the stack....

		// //test
		// Button test = new Button("TEST");
		// test.setOnAction((action)->{
		// System.out.println("Current mark: " +
		// this.getOverlayMarkerManager().getCurrentMarker().getOverlayMark());
		// if (
		// this.getOverlayMarkerManager().getCurrentMarker().getOverlayMark()==null)
		// return;
		// for (int i=0; i<4; i++){
		// System.out.print(" "
		// +this.getOverlayMarkerManager().getCurrentMarker().getOverlayMark().getLimits()[i]);
		// }
		// System.out.println("");
		//
		// });
		// stackPane.getChildren().add(test);

		/**
		 * Need to compensate for the fact that the holder pane (hiding pane) contains
		 * another Node (hide button). Since the whole hiding pane is set to resize with
		 * the hiding-tab-pane we must set the end spacing so the hiding pane does not
		 * distort when resizing.
		 */
		settingsTabs.startSpacingProperty().bind(getSettingsPane().getHideButton().widthProperty());
		settingsTabs.setHolderPane(getSettingsPane());

		// add settings panes if any.
		layoutSettingsPanes();

		// add plots to center of main pane
		this.setCenter(stackPane);

		// layout axis.
		layoutTDGraph(tdDisplay.getTDParams().orientation);

		// set the default overlay style.
		setOverlayColour(LIGHT_TD_DISPLAY);
		
		//show the left hiding pane byu default. 
		stackPane.getLeftHidingPane().showHidePane(true);
	}

	/**
	 * Get the pane which contains control to change generic graph settings.
	 * 
	 * @return the settings pane for the TDGraph;
	 */
	public SettingsPane<TDGraphParametersFX> getTDGraphSettingsPane() {
		if (tdSettingsPane == null) {
			tdSettingsPane = new TDGraphSettingsPane(this, this);
			tdSettingsPane.setParams(graphParameters);
					
			
			tdSettingsPane.addSettingsListener(() -> {
				TDGraphParametersFX params = tdSettingsPane.getParams(graphParameters);
				if (params != null) {
					graphParameters = params;
				}
				for (int i = 0; i < this.tdPlotPanels.size(); i++) {
					tdPlotPanels.get(i).setPopUpMenuType(graphParameters.popUpMenuType);
				}
				this.repaint(100);
			});
		}
		return tdSettingsPane;
	}

	/**
	 * Layout main components on the tdGraph, mainly the plot panes and the axis.
	 * 
	 * @param orientation - orientation of graph. Vertical or horizontal.
	 */
	public void layoutTDGraph(Orientation orientation) {
		this.setLeft(null);
		this.setTop(null);
		// layout axis
		// axisPanel.setOrientation(orientation);
		if (orientation == Orientation.HORIZONTAL) {
			this.setLeft(axisPanel);
			axisPanel.setOrientation(Orientation.VERTICAL);
			axisPanel.setPrefWidth(TDDisplayFX.dataAxisSize);
		} else {
			this.setTop(axisPanel);
			axisPanel.setOrientation(Orientation.HORIZONTAL);
			axisPanel.setPrefHeight(TDDisplayFX.dataAxisSize);
		}

		// layout out all the plot panes.
		layoutPlotPanes(orientation);
	}

	/**
	 * The buttons which sit in a stack pane on top of the display to open hiding
	 * panes can be hidden if the display is too dark or too light. e.g. if hiding
	 * pane buttons are grey then they are not easy to see when a grey spectrogram
	 * is shown. This function sets the correct colour of button for the current
	 * screen background;
	 * 
	 * @param displayCol. The current display colour type. DARK_TD_DISPLAY for
	 *                    darker backgrounds and LIGHT_TD_DISPLAY for light
	 *                    backgrounds.
	 */
	public void setOverlayColour(int displayCol) {
		Text chevronRight = null;
		Text settingsRight = null;
		switch (displayCol) {
		case DARK_TD_DISPLAY:
		//	System.out.println("SET DARK THEME FOR HIDING BUTTONS");
//			chevronRight = PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_RIGHT, Color.WHITE,
			chevronRight = PamGlyphDude.createPamIcon("mdi2c-chevron-right", Color.WHITE, PamGuiManagerFX.iconSize);
//			settingsRight = PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, Color.WHITE, PamGuiManagerFX.iconSize);
			settingsRight = PamGlyphDude.createPamIcon("mdi2c-cog", Color.WHITE, PamGuiManagerFX.iconSize);
			break;
		case LIGHT_TD_DISPLAY:
			//System.out.println("SET LIGHT THEME FOR HIDING BUTTONS");
//			chevronRight = PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_RIGHT, PamGuiManagerFX.iconColor,
			chevronRight = PamGlyphDude.createPamIcon("mdi2c-chevron-right", PamGuiManagerFX.iconColor,	PamGuiManagerFX.iconSize);
//			settingsRight = PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconColor,
			settingsRight = PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize);
			break;
		default:
			setOverlayColour(LIGHT_TD_DISPLAY);
			break;
		}
		stackPane.getLeftHidingPane().getShowButton().setGraphic(chevronRight);
		stackPane.getRightHidingPane().getShowButton().setGraphic(settingsRight);
	}

	// /**
	// * Create and add a hiding pane to the display.
	// * @param displayPane - the pane to add to the hiding pane.
	// * @param icon - the icon for the show button.
	// * @param trayPos - the y position of the show button from the top of the
	// pane.
	// * @param side the side of the display the hiding pane should open on
	// * @param pos the position of the hiding pane oin the stack pane.
	// * @return the hiding pane.
	// */
	// private HidingPane createHidingPane(Pane displayPane, Node icon, int trayPos,
	// Side side, Pos pos){
	// int yPos=trayPos*35+5;
	// //create the hiding pane
	// HidingPane settingsPane=new HidingPane(side, displayPane, stackPane, true);
	// //the stack pane holds all the different settings panes
	// stackPane.getChildren().add(settingsPane);
	// settingsPane.getStylesheets().add(tdDisplay.cssSettingsResource);
	// settingsPane.getStyleClass().add("pane-trans");
	//
	// StackPane.setAlignment(settingsPane, pos);
	// PamButton showButton=settingsPane.getShowButton();
	//
	// //style show button
	// showButton.setGraphic(icon);
	// showButton.getStylesheets().add(tdDisplay.cssSettingsResource);
	// switch(side){
	// case RIGHT:
	// showButton.getStyleClass().add("close-button-left-trans");
	// break;
	// case LEFT:
	// showButton.getStyleClass().add("close-button-right-trans");
	// break;
	// default:
	// break;
	// }
	//
	// //make show button same height as hide button
	// showButton.prefHeightProperty().bind(settingsPane.getHideButton().heightProperty());
	// //translate it so it sits slightly below the top of the pane.
	// showButton.translateYProperty().setValue(yPos);
	// settingsPane.getHideButton().translateYProperty().setValue(yPos);
	// //add to pane
	// stackPane.getChildren().add(showButton);
	// StackPane.setAlignment(showButton, pos);
	// return settingsPane;
	// }

	/**
	 * Set the number of plots within the graph. By default this is one, but some
	 * types of data will contain functionality which can change this e.g. a
	 * spectrogram displaying multiple channels of data
	 * 
	 * @param numPlots - number of plots to show.
	 */
	public void setNumberOfPlots(int numPlots) {
		if (numPlots == tdPlotPanels.size()) {
			return;
		}
		// first remove unwanted plots from the graphInnerPanel
		TDPlotPane aPlot;
		while (tdPlotPanels.size() > numPlots) {
			aPlot = tdPlotPanels.get(tdPlotPanels.size() - 1);
			tdPlotPanels.remove(aPlot);
		}
		// then add any we now need
		while (tdPlotPanels.size() < numPlots) {
			tdPlotPanels.add(aPlot = new TDPlotPane(tdPlotPanels.size()));
			Tooltip t = new TDTooltip(aPlot);
			Tooltip.install(aPlot, t);
		}

		// now sort out the display;
		layoutPlotPanes(tdDisplay.getTDParams().orientation);

	}

	/**
	 * Sort the number of plot panels to be displayed.
	 * 
	 * @param orientation - orientation of the display, VERTICAL or HORIZONTAL.
	 */
	private void layoutPlotPanes(Orientation orientation) {
		// first remove all the plot panels
		plotPanels.getChildren().removeAll(plotPanels.getChildren());

		int row = 0;
		int column = 0;
		// add plotPanels to grid pane
		int pos = 0;
		for (int iPanel = 0; iPanel < tdPlotPanels.size(); iPanel++) {
			if (this.getCurrentScaleInfo().getVisiblePlots()[iPanel]) {
				if (orientation == Orientation.HORIZONTAL)
					row = pos;
				if (orientation == Orientation.VERTICAL)
					column = pos;
				plotPanels.add(tdPlotPanels.get(iPanel), column, row);
				pos++;
			}
		}
		// make sure the axis is updated too.
		this.axisPanel.setnPlots(pos);

		/**
		 * 01/05/2017. Need to call a layout pass here otherwise some of the data units
		 * don;t know the axis and the initial repaint can mess up. Not good if in
		 * viewer mode and changing the plot pane.
		 */
		this.axisPanel.layout();

		// sort out the size of the grid pane panels - we want each to have equal space
		TDDisplayFX.sortGridContraints(plotPanels, orientation, plotPanels.getChildren().size());

	}

	/**
	 * Sort out the settings pane. The settingsPane contains tabs of the different
	 * the different settings panes for data blocks associated with this tdGraphFX.
	 */
	private void layoutSettingsPanes() {
		// first remove all tabs
		settingsTabs.getTabs().removeAll(settingsTabs.getTabs());
		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo.getGraphSettingsPane() == null)
				continue;
			settingsTabs.addSettingsPane(dataInfo.getGraphSettingsPane());
		}
	}

	/**
	 * Check that the axis of the graph is displaying the correct scale and info.
	 */
	public void checkAxis() {

		// find the first data block that can display data for the currently selected
		// axis
		currentScaleInfo = findFirstActiveScaleinfo();
		
		//System.out.println("TDGraphFX: First active scale info 1 : " + currentScaleInfo); 

		if (currentScaleInfo == null) {
			return;
		}

//		System.out.println("TDGraphFX: First active scale info 2: " + currentScaleInfo.getAxisName() + "  " + graphParameters.currentDataType.getTypeString()); 
		
		//needed to add this here because, if a new graph was created, the data type infos could be messed up and nothing would plot. 
		if (currentScaleInfo.getDataTypeInfo().dataType != graphParameters.currentDataType.dataType) {
			System.err.println("TDGraphFX: The graph paramters data type is not the same as the current dsata type"); 
			graphParameters.currentDataType = currentScaleInfo.getDataTypeInfo(); 
		}
		
		// get the axis minimum/maximum for the data block
		graphAxis.minValProperty().unbind();
		graphAxis.maxValProperty().unbind();

		// set the number of plots.
		nPlots.setValue(currentScaleInfo.getNVisiblePlots());

		// set the correct number of plots for this data block
		setNumberOfPlots(currentScaleInfo.getNPlots());

		// clear the channel iterators.
		clearChannelIterators();

		// must set the axis panel in case reversed
		graphAxis.setReversed(currentScaleInfo.getReverseAxis());

		graphAxis.setFractionalScale(true);

		// set the axis values. The value in graphAxis are bound to the PamAxisPane2.
		graphAxis.minValProperty().bind(currentScaleInfo.minValProperty().divide(currentScaleInfo.getUnitDivisor()));
		graphAxis.maxValProperty().bind(currentScaleInfo.maxValProperty().divide(currentScaleInfo.getUnitDivisor()));

		// setAxisName(graphParameters.currentAxisName);
		// if the axis has changed then set the background colour type for the display
		// i.e. change colour of overlayed buttons.
		// TODO- this may be better placed in another function...?
		setOverlayColour();

	}

	/**
	 * Clear all the channel iterators. This is important for drawing when multiple
	 * plot panes are used. The channel iterators are, for some reason, empty and
	 * saved in the datablocks and so calling them means no data units are present.
	 * They require clearing so that, on the first repaint, new channel iterators
	 * are created.
	 */
	private void clearChannelIterators() {
		for (int i = 0; i < this.dataList.size(); i++) {
			dataList.get(i).getDataBlock().clearChannelIterators();
		}
	}

	/**
	 * Get the current scale information
	 * 
	 * @return the current scale information.
	 */
	public TDScaleInfo getCurrentScaleInfo() {
		if (currentScaleInfo == null)
			currentScaleInfo = findFirstActiveScaleinfo();
		return currentScaleInfo;
	}

	/**
	 * Get the first scale information, i.e. the first data info that can actually
	 * plot
	 * 
	 * @return the first scale information
	 */
	private TDScaleInfo findFirstActiveScaleinfo() {
		TDDataInfoFX firstDataInfo = findFirstActiveDataInfo();
		if (firstDataInfo == null)
			return null;
		return firstDataInfo.getScaleInfo(graphParameters.autoScale);
	}

	/**
	 * Find the first data info that can actually plot on the current graph. e.g.
	 * say we have a graph who's axis is displaying bearing info. That graph can
	 * only plot data units which have an associated bearing, so we find the first
	 * TDDataInfoFX which has bearing info. Will initially look for a dataInfo with
	 * base priority in its scale information and then goes by position in dataInfo
	 * list.
	 * 
	 * @return the first {@link TDDataInfoFX} which can be plotted on the graph.
	 */
	private TDDataInfoFX findFirstActiveDataInfo() {
		DataTypeInfo axisName = graphParameters.currentDataType;
		TDScaleInfo scaleInfo;
		int firstIndex = -1;

		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo.hasAxisName(axisName.dataType, axisName.dataUnits)) {
				scaleInfo = dataInfo.getScaleInfo(graphParameters.autoScale);

				// keep track of the first found dataInfo that can be plotted
				if (firstIndex < 0)
					firstIndex = dataList.indexOf(dataInfo);
				// if scale information is found and has base priority then this is the first
				// dataInfo to use.
				if (scaleInfo != null && scaleInfo.getPlotPriority() == TDScaleInfo.BASE_PRIORITY)
					return dataInfo;
			}
		}
		// if none of the dataInfo has base priority then plot first data info
		if (firstIndex >= 0)
			return dataList.get(firstIndex);
		// else just return the first dataInfo.
		if (dataList.size() > 0)
			return dataList.get(0);
		return null;
	}

	/**
	 * Called when the user sets the type of axis units. These may not be available
	 * in all displayed data, so when this is set, some things may stop showing!
	 * 
	 * @param axisType the name of the axis to set.
	 */
	public void setAxisName(DataTypeInfo axisType) {

		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo == null) {
				System.out.println("The data info is NULL");
				return;
			}
			dataInfo.setCurrentAxisName(axisType.dataType, axisType.dataUnits);
		}

		if (axisType == null && availableAxisNames.size() > 0) {
			axisType = availableAxisNames.get(0);
		}

		graphParameters.currentDataType = axisType;
	

		// check the number of panels - this must come before the axis name is set or divisors etd are all wronf. 
		checkAxis();
		
		if (currentScaleInfo != null) {
			graphAxis.setLabel((TDScaleInfo.getAxisName(axisType)) + " (" + currentScaleInfo.getDivisorString()
					+ TDScaleInfo.getUnitName(axisType) + ")");
			;
		} else {
			graphAxis.setLabel((TDScaleInfo.getAxisName(axisType)) + " (" + TDScaleInfo.getUnitName(axisType) + ")");
		}

		graphProjector.setParmeterType(0, ParameterType.TIME);
		graphProjector.setParmeterType(1, axisType.dataType);


		//System.out.println("currentScaleInfo: 2 " +currentScaleInfo.getAxisName() + "  " + currentScaleInfo.getUnitDivisor() + " max val: " +  currentScaleInfo.getMaxVal()); 

		// set correct colour
		repaint(0);
	}

	/**
	 * Add a data item to the graph.
	 * 
	 * @param dataProvider - the {@link #TDDataProviderFX} to set.
	 */
	public void addDataItem(TDDataProviderFX dataProvider) {
		addDataItem(dataProvider.createDataInfo(this));
	}

	/**
	 * Add a data item to be plotted on this display.
	 * 
	 * @param dataInfo item to add (basically an augmented data block).
	 */
	public void addDataItem(TDDataInfoFX dataInfo) {
		dataList.add(dataInfo);
		tdDisplay.subscribeScrollDataBlocks();
		sortAxisandPanes();
		tdControl.dataModelToDisplay();
	}

	/**
	 * Remove a data block from the graph.
	 * 
	 * @param iRemove- data block in the data list to remove.
	 */
	public void removeDataItem(int iRemove) {
		dataList.remove(iRemove);
		// create a list of axis names for the data units.
		sortAxisandPanes();
		tdControl.dataModelToDisplay();
	}

	/**
	 * Function to sort axis hiding pane, axis, and axis name array when data has
	 * been added or removed.
	 */
	private void sortAxisandPanes() {
		// create a list of axis names for the data units.
		createAxisNamesList();
		// sort out axis
		checkAxis();
		// sort out data pane
		tdAxisSelPane.remakePane();
		// sort out hiding settings pane
		layoutSettingsPanes();
	}

	/**
	 * The buttons which sit in a stack pane on top of the display to open hiding
	 * panes can be hidden if the display is too dark or too light. e.g. if hiding
	 * pane buttons are grey then they are not easy to see when a grey spectrogram
	 * is shown. This function sets the correct colour of button for the current
	 * screen background based on the first active scale info.
	 */
	public void setOverlayColour() {
		int overlayCol = findFirstActiveDataInfo().getDisplayColType(); 
		
		if (overlayCol == TDGraphFX.LIGHT_TD_DISPLAY) {
			//check the colour...
			
		}
		
		setOverlayColour(overlayCol);
	}

	/**
	 * Subscribe data blocks to the time scroller system so their data get loaded in
	 * viewer mode.
	 * 
	 * @param timeScroller - the time scroller
	 */
	public void subscribeScrollDataBlocks(AbstractPamScrollerFX timeScroller) {
		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo == null) {
				continue;
			}
			timeScroller.addDataBlock(dataInfo.getDataBlock());
			if (!tdControl.isViewer()) {
				@SuppressWarnings("rawtypes")
				PamDataBlock sourceBlock;

				/**
				 * In viewer mode we want the source data block. i.e. we want raw sound
				 * acquisition for spectrogram so we can create a spectrogram on the fly.
				 */
				sourceBlock = dataInfo.getSourceDataBlock(); // in real time mode we don't want source data blocks.
				if (sourceBlock != null) {
					// System.out.println("TDGraphFX: Source data block subscribed:
					// "+sourceBlock.getDataName());
					// dataInfo.getDataBlock().addObserver(tdControl.getDataObserver(), false);
					sourceBlock.addObserver(tdControl.getDataObserver(), false);
				}
				/*
				 * Subscribe the data info to it's data block so it can tell data to be held in
				 * memory.
				 */
				dataInfo.getDataBlock().addObserver(dataInfo.getDataObserver());
			}
		}
	}
	
	
	/**
	 * Loop through the list of data blocks that are displayed in this panel, and return the first one that is
	 * an FFTDataBlock.  If one is not found, return null;
	 * 
	 * @return an FFTDataBlock, or null
	 */
	public FFTDataBlock getFFTDataBlock() {
		FFTDataBlock source = null;
		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo == null) {
				return null;
			}
			if (dataInfo.getDataBlock() instanceof FFTDataBlock) {
				source = (FFTDataBlock) dataInfo.getDataBlock();
				break;
			}
		}
		return source;
	}
	

	/**
	 * Get the axis for this tdGraph;
	 * 
	 * @return the axis for the graph (y axis when horizontal, x axis when vertical)
	 */
	public PamAxisFX getGraphAxis() {
		return graphAxis;
	}

	/**
	 * Get the graph projector.
	 * 
	 * @return the grtaph projector.
	 */
	public TDProjectorFX getGraphProjector() {
		return this.graphProjector;
	}

	/**
	 * Get the pane which holds the GUI components of the graph axis.
	 * 
	 * @return pane containing the graph axis.
	 */
	public PamAxisPane2 getGraphAxisPane() {
		return axisPanel;
	}

	/**
	 * @return the scrollStart
	 */
	public double getScrollStart() {
		return scrollStart;
	}

	/**
	 * A tgool tip for detections.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class TDTooltip extends Tooltip {

		private TDPlotPane plotPanel;

		/**
		 * 
		 * @param aPlot
		 */
		public TDTooltip(TDPlotPane aPlot) {
			super("Test tooltip");
			this.plotPanel = aPlot;
			/*
			 * See https://bugs.openjdk.java.net/browse/JDK-8090477 fr info about tool tip
			 * timing on these displys.
			 */

		}

		/**
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		private String getTipText(double x, double y) {
			Bounds bounds = plotPanel.getLayoutBounds();
			javafx.geometry.Point2D coordinates = plotPanel.localToScreen(0, 0);
			double X = coordinates.getX();
			double Y = coordinates.getY();
			Point mousePoint = MouseInfo.getPointerInfo().getLocation();
			mousePoint.x -= X;
			mousePoint.y -= Y;
			String str = graphProjector.getHoverText(mousePoint, plotPanel.iPanel);
			return PamUtilsFX.htmlToNormal(str);
		}

		private boolean makeTipText(double x, double y) {
			String str = getTipText(x, y);
			if (str == null) {
				return false;
			}
			// WebView web = new WebView();
			// WebEngine webEngine = web.getEngine();
			// webEngine.loadContent(str);
			// setMinSize(20, 20);
			// this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			// setGraphic(web);
			setText(str);
			return true;
		}

		@Override
		public void show(Node node, double x, double y) {
			if (makeTipText(x, y) == false) {
				return;
			}
			super.show(node, x, y);
		}

		@Override
		public void show(Window ownerWindow, double x, double y) {
			if (makeTipText(x, y) == false) {
				return;
			}
			super.show(ownerWindow, x, y);
		}

	}

	/**
	 * Panel which plots data.
	 * 
	 * @author Jamie Macaulay
	 */
	public class TDPlotPane extends PamBorderPane implements MarkExtraInfoSource {

		/**
		 * Repaint only the highlight canvas. This canvas shows overlay marks etc.
		 */
		public static final int HIGHLIGHT_CANVAS = 1 << 0;

		/**
		 * Repaint the front canvas. This usually shows all detections
		 */
		public static final int FRONT_CANVAS = 1 << 1;

		/**
		 * Repaint the back canvas. This shows things like 2D plots e.g. the spectrogram
		 */
		public static final int BASE_CANVAS = 1 << 2;

		/**
		 * Repaint all canvas
		 */
		public static final int ALL_CANVAS = HIGHLIGHT_CANVAS | FRONT_CANVAS | BASE_CANVAS;

		/**
		 * Indicates what plot panel this is.
		 */
		int iPanel;

		/**
		 * The canvas is where some data is drawn with base priority that we always want
		 * in the background e.g. spectrogram.
		 */
		protected Canvas baseCanvas;

		/**
		 * The canvas is where most of the drawing of detections etc takes place..
		 */
		protected Canvas drawCanvas;

		/**
		 * Canvas which draws overlay overlays such as highlighted data
		 */
		protected Canvas highLightCanvas;

		private ExtMapMouseHandler totalMouseHandler = new ExtMapMouseHandler(this, true);

		private TDPopUpMenuAdv advPopMenu;
		

		public TDPlotPane(int iPanel) {
			this.iPanel = iPanel;
			// create all canvas's
			baseCanvas = new Canvas(100, 100); // draw base plots e.g. spectrogram
			drawCanvas = new Canvas(100, 100); // draw most other detection data e.g. clicks, whistles etc.
			highLightCanvas = new Canvas(100, 100); // draw overlays such as highlight boxes.
			this.getChildren().addAll(baseCanvas, drawCanvas, highLightCanvas);
			
			
			baseCanvas.toBack();
			highLightCanvas.toFront();

			// add resize listeners so the canvas resizes with graph.
			addResizeListeners(baseCanvas); // could bind but need to repaint when resized so just use listeners
											// instead.
			addResizeListeners(drawCanvas);
			addResizeListeners(highLightCanvas);

			totalMouseHandler.subscribeFXPanel(this);
			totalMouseHandler.addMouseHandler(overlayMarker);


			// add extra functionality to the mouse
			totalMouseHandler.addMouseHandler(new PlotMouseHandler(this));

			/*
			 * also add a mouse handler from the scroll system which can be used to snap
			 * other displays to a box on this display. .
			 */
			
			//takes some time to load icons so instantiate here. 
			advPopMenu = new TDPopUpMenuAdv(this);
			setPopUpMenuType(graphParameters.popUpMenuType);


			repaint();
		}

		/**
		 * Set the pop up menu type. Type flags e.g. TDGraphParametersFX.ADV_POP_UP
		 * 
		 * @param type - type flag for the pop up menu.
		 */
		public void setPopUpMenuType(int type) {
			switch (type) {
			case TDGraphParametersFX.ADV_POP_UP:
				totalMouseHandler.setPopMenu(advPopMenu);
				break;
			case TDGraphParametersFX.SIMPLE_POP_UP:
				totalMouseHandler.setPopMenu(new ExtPopMenuSimple());
				break;
			}
		}

		/**
		 * Add a listeners to the canvas to check for resize and repaint.
		 */
		public void addResizeListeners(Canvas canvas) {
			this.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					canvas.setWidth(arg0.getValue().doubleValue());
					// must go up a level to repaint to handle hover lists
					TDGraphFX.this.repaint(10);
				}
			});

			this.heightProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					canvas.setHeight(arg0.getValue().doubleValue());
					// must go up a level to repaint to handle hover lists
					TDGraphFX.this.repaint(10);
				}
			});
		}

		/**
		 * Repaints all the canvas, including all data units
		 */
		public void repaint() {
			repaint(0, ALL_CANVAS); // 30 frames per second
		}

		/**
		 * Repaint all canvases in the plot pane. There are multiple canvases which are
		 * layered to avoid issue with say a spectrogram painting over the data units.
		 * The flag is a bitmap of which canvases to paint, all or perhaps just the
		 * canvas which highlights annotated detections in order to save time.
		 * 
		 * @param tm   - minimum time in millis since last repaint. If the time since
		 *             least repaint is less than this then
		 * @param flag - the type flag for the canvas that is to be painted.
		 * 
		 */
		public synchronized void repaint(long tm, int flag) {

			// clear the current canvas's
			if (hasCanvas(flag, BASE_CANVAS)) {
				baseCanvas.getGraphicsContext2D().clearRect(0, 0, baseCanvas.getWidth(), baseCanvas.getHeight());
				// fill with background colour- not only the base canvas.
				baseCanvas.getGraphicsContext2D().setFill(graphParameters.plotFill);
				baseCanvas.getGraphicsContext2D().fillRect(0, 0, baseCanvas.getWidth(), baseCanvas.getHeight());
			}
			if (hasCanvas(flag, FRONT_CANVAS)) {
				drawCanvas.getGraphicsContext2D().clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
			}
			if (hasCanvas(flag, HIGHLIGHT_CANVAS)) {
				highLightCanvas.getGraphicsContext2D().clearRect(0, 0, highLightCanvas.getWidth(),
						highLightCanvas.getHeight());
			}

			/**
			 * Go through each dataInfo (basically the data blocks the graph can display),
			 * work out if their data units can be displayed on the graph and if so plot the
			 * data.
			 */
			GraphicsContext gc = null;
			TDScaleInfo scaleInfo;
			boolean base = false;
			boolean hasBase = false; 
			synchronized (dataList) {
				for (TDDataInfoFX dataInfo : dataList) {
					base = false;
					if (!dataInfo.isShowing()) {
						// System.out.println("dataInfo.isShowing(): " + dataInfo.getDataName());
						continue;
					}

					scaleInfo = dataInfo.getScaleInfo();
					if (scaleInfo == null) {
						// System.out.println("scale info null " + dataInfo.getDataName() + "index:
						// "+dataInfo.getScaleInfoIndex());
						continue;
					}

					if (scaleInfo.getDataTypeInfo().equals(graphParameters.currentDataType) == false) {
						continue;
					}

					// figure out which canvas to draw data units on.
					switch (dataInfo.getScaleInfo().getPlotPriority()) {
					case TDScaleInfo.BASE_PRIORITY:
						gc = baseCanvas.getGraphicsContext2D();
						base = true;
						hasBase = true;
						break;
					case TDScaleInfo.INLIST_PRIORITY:
						gc = drawCanvas.getGraphicsContext2D();
						break;
					default:
						gc = drawCanvas.getGraphicsContext2D();
					}
					;

					// ok so only repaint if we have the right CANVAS
					if (base && hasCanvas(flag, BASE_CANVAS)) {
						paintDataUnits(gc, dataInfo, false);
					} else if (!base && hasCanvas(flag, FRONT_CANVAS)) {
						paintDataUnits(gc, dataInfo, false);
					}
				}

				if (hasCanvas(flag, HIGHLIGHT_CANVAS)) {
					// repaint overlaid marks
					repaintMarks();
				}

				// draw a line to show current time if the display is in wrap mode.
				// This only occurs in real time
				if (gc == null) {
					gc = drawCanvas.getGraphicsContext2D();
				}


				// draw a line between displays if there multiple plot panels
				if (gc != null && getCurrentScaleInfo() != null && iPanel != getCurrentScaleInfo().getNPlots() - 1) {
					gc.setStroke(Color.GRAY);
					gc.strokeLine(0, baseCanvas.getHeight(), baseCanvas.getWidth(), baseCanvas.getHeight());
				}
				
				drawSoundOutputMarker(gc);
			}
			
			//onlu show the wrap if there is not a base canvas - if there is a base canvas it is up
			//to the TDPlotInfoFX of the base canvas to draw a wrap line. This means that, for example, 
			//a spectorgram plot works better because wrap line stays in sync with the FFT chunks. 
			if (graphProjector.isWrap() && !hasBase && !getTDDisplay().isViewer()) {
				drawWrapLine(gc);
			}

		}

		/**
		 * Repaints only the marks on the canvas and highlighted data units. This speeds
		 * up marking graphics.
		 */
		public void repaintMarks() {
			highLightCanvas.getGraphicsContext2D().clearRect(0, 0, baseCanvas.getWidth(), baseCanvas.getHeight());
			overlayMarker.getCurrentMarker().drawMark(highLightCanvas.getGraphicsContext2D());
			// repaint highlighted data units.
			for (TDDataInfoFX dataInfo : dataList) {
				paintDataUnits(highLightCanvas.getGraphicsContext2D(), dataInfo, true);
			}
		}

		/**
		 * Draw line which shows the current position of sound playback.
		 * 
		 * @param gc - the graphics context to draw on.
		 */
		private void drawSoundOutputMarker(GraphicsContext gc) {
			if (gc == null) {
				return;
			}
			SoundOutputManager soundOut = tdDisplay.getSoundOutputManager();
			if (soundOut.getCurrentStatus() == PlaybackProgressMonitor.PLAY_END) {
				return;
			}
			if ((1 << iPanel & soundOut.getCurrentChannels()) == 0 && tdPlotPanels.size() > 1) {
				return;
			}
			long playPos = soundOut.getCurrentMillis();
			// double pixX =
			// tdDisplay.getTimeAxis().getPosition(((playPos-getLastWrapMillis()+tdDisplay.getTimeScroller().getVisibleMillis())/1000.));
			double pixX = tdDisplay.getTimeAxis().getPosition((playPos - scrollStart) / 1000.);
			gc.setStroke(wrapColor);
			gc.setLineWidth(1);
			gc.strokeLine(pixX, 0, pixX, this.getHeight());
		}

		/**
		 * Draw a line showing the current wrap location.
		 */
		private void drawWrapLine(GraphicsContext gc) {
			if (gc == null) {
				return;
			}
			scrollStart = tdDisplay.getTimeScroller().getValueMillis();
			double pixX = tdDisplay.getTimeAxis().getPosition(
					((scrollStart - getLastWrapMillis() + tdDisplay.getTimeScroller().getVisibleMillis()) / 1000.));
			gc.setStroke(wrapColor);
			gc.setLineWidth(1);
			// System.out.println("pix"+pixX);
			gc.strokeLine(pixX, 0, pixX, this.getHeight());
		}

		/**
		 * Paint all data units on a graphics context.
		 * 
		 * @param g          - graphics context to paint on.
		 * @param windowRect - the size of the window to paint in.
		 * @param dataInfo   - the {@link #TDDataInfoFX} to get data units from.
		 * @param highlight  - true to paint <b>only</b> highlighted data units.
		 */
		private void paintDataUnits(GraphicsContext g, TDDataInfoFX dataInfo, boolean highlight) {
			// PamDataBlock<PamDataUnit> dataBlock = dataInfo.getDataBlock();

			// scroll start is the end of the display i.e. the last visible time in the past
			// in real time mode.
			scrollStart = tdDisplay.getTimeScroller().getValueMillisD();

//			 System.out.println("TDGraph: Paint start at " +
//			 PamCalendar.formatTime(scrollStart)+" "+scrollStart + " scale info " + 
//					 getCurrentScaleInfo().getVisibleChannels()[iPanel] + " "+ highlight ) ;

			if (getCurrentScaleInfo().getVisibleChannels()[iPanel]) {
				if (!highlight) {
					dataInfo.drawData(iPanel, g, scrollStart, graphProjector);
				}
				else {
					dataInfo.drawHighLightData(iPanel, g, scrollStart, graphProjector);
				}
			}
		}

		/**
		 * Get the base canvas.
		 * 
		 * @return the base canvas.
		 */
		public Canvas getBaseCanvas() {
			return this.baseCanvas;
		}

		/**
		 * Get the channels shown on the display.
		 * 
		 * @return the channels shown on the display.
		 */
		public int getChannels() {
			return 1 << iPanel;
		}

		@Override
		public MarkExtraInfo getExtraInfo() {
			MarkExtraInfo me = new MarkExtraInfo();
			me.setChannelMap(1 << iPanel);
			return me;
		}

		/**
		 * 
		 * Get the TDGraph the TDPlotPane belongs to.
		 * 
		 * @return the TDGraphFX whihc owns the plot pane.
		 */
		public TDGraphFX getTDGraph() {
			return TDGraphFX.this;
		}

		/**
		 * Get pop up menu items for a mouse at certain point on the display
		 * 
		 * @param e - the mouse event
		 * @return a list of the pop menu items
		 */
		public List<MenuItem> getPopUpMenuItems(MouseEvent e) {
			return totalMouseHandler.getPopupMenuItems(e);
		}
	}

	/**
	 * Mouse handler for the TDGraphFX.
	 * 
	 * @author Doug Gillespie
	 *
	 */
	private class PlotMouseHandler extends ExtMouseAdapter {

		private TDPlotPane tdPlotPanel;

		public PlotMouseHandler(TDPlotPane tdPlotPanel) {
			this.tdPlotPanel = tdPlotPanel;
		}

		/*
		 * tdPlotPanel.setOnMousePressed(new EventHandler<MouseEvent>() {
		 * 
		 * @Override public void handle(MouseEvent event) { mousePressed(event); } });
		 * tdPlotPanel.setOnMouseReleased(new EventHandler<MouseEvent>() {
		 * 
		 * @Override public void handle(MouseEvent event) { mouseReleased(event); } });
		 * tdPlotPanel.setOnMouseMoved(new EventHandler<MouseEvent>() {
		 * 
		 * @Override public void handle(MouseEvent event) { mouseMoved(event); } });
		 * tdPlotPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
		 * 
		 * @Override public void handle(MouseEvent event) { mouseDragged(event); } });
		 * tdPlotPanel.setOnMouseExited(new EventHandler<MouseEvent>() {
		 * 
		 * @Override public void handle(MouseEvent event) { mouseExited(event); } }); }
		 */
		/*
		 * public boolean mousePressed(MouseEvent event) { if (event.isPopupTrigger()) {
		 * return showPopupMenu(event); } return false; }
		 * 
		 * public boolean mouseReleased(MouseEvent event) { if (event.isPopupTrigger())
		 * { return showPopupMenu(event); } return false; }
		 */
		public boolean mouseMoved(MouseEvent event) {
			sayMousePosition(event);
			return false;
		}

		public boolean mouseDragged(MouseEvent event) {
			sayMousePosition(event);
			return false;
		}

		public boolean mouseExited(MouseEvent event) {
			tdDisplay.getMousePositionData().setText(null);
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * PamView.paneloverlay.overlaymark.ExtMouseAdapter#getPopupMenuItems(javafx.
		 * scene.input.MouseEvent)
		 */
		@Override
		public List<MenuItem> getPopupMenuItems(MouseEvent e) {

			PamCoordinate screenPos = new Coordinate3d(e.getX(), e.getY());
			PamCoordinate dataPos = graphProjector.getDataPosition(screenPos);
			if (dataPos==null) return null;
			List<MenuItem> menuItems = new ArrayList<>();

			if (tdControl.isViewer()) {
				List<MenuItem> soundItems = tdDisplay.getSoundOutputManager().getMenuItems(tdPlotPanel,
						(long) dataPos.getCoordinate(0));
				if (soundItems != null) {
					menuItems.addAll(soundItems);
				}
			}

			ScrollerCoupling scrollCoupling = tdDisplay.getTimeScroller().getScrollerCoupling();
			if (scrollCoupling != null) {
				OverlayMark overlayMark = getOverlayMarkerManager().getCurrentMarker().getOverlayMark();
				List<MenuItem> scollItems = scrollCoupling.getPopupMenuItems(tdDisplay.getTimeScroller(), overlayMark,
						(long) dataPos.getCoordinate(0));
				if (scollItems != null) {
					menuItems.addAll(scollItems);
				}
			}
			return menuItems;
		}
		/*
		 * public boolean showPopupMenu(MouseEvent event) { if
		 * (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		 * showViewerPopup(tdPlotPanel, event); return true; } return false; }
		 */

		// private void showViewerPopup(TDPlotPanel tdPlotPanel, MouseEvent event) {
		// ContextMenu contextMenu = new ContextMenu();
		// PamCoordinate screenPos = new Coordinate3d(event.getX(), event.getY());
		// PamCoordinate dataPos = graphProjector.getDataPosition(screenPos);
		// contextMenu.getItems().addAll(tdDisplay.getSoundOutputManager().getMenuItems(tdPlotPanel.iPanel,
		// (long) dataPos.getCoordinate(0)));
		// contextMenu.show(tdPlotPanel, event.getScreenX(), event.getScreenY());
		// }

		private void sayMousePosition(MouseEvent event) {
			PamCoordinate screenPos = new Coordinate3d(event.getX(), event.getY());
			PamCoordinate dataPos = graphProjector.getDataPosition(screenPos);
			if (dataPos==null) return;
			String str = String.format("Mouse %s %s ", PamCalendar.formatDate((long) dataPos.getCoordinate(0)),
					PamCalendar.formatTime((long) dataPos.getCoordinate(0), true));
			// if (currentScaleInfo != null) {
			// str += String.format(", %s %3.1f %s ", currentScaleInfo.getDataType(),
			// dataPos.getCoordinate(1), currentScaleInfo.unit);
			// }
			// String fmt = String.format(", %s %%s", graphAxis.)
			str += String.format(", %3.2f %s  ", graphAxis.getDataValue(event.getY()), graphAxis.getLabel());
			tdDisplay.getMousePositionData().setText(str);
		}

	}

	/**
	 * Test function for drawing on canvas.
	 * 
	 * @param GraphicsContext gc - the graphics context to draw on .
	 */
	@SuppressWarnings("unused")
	private void drawShapes(GraphicsContext gc) {
		gc.setFill(Color.RED);
		for (int i = 0; i < 200; i++) {
			int height = (int) (Math.random() * gc.getCanvas().getHeight());
			int width = (int) (Math.random() * gc.getCanvas().getWidth());
			gc.fillOval(width, height, 10, 10);
			gc.strokeOval(width, height, 10, 10);
		}
	}

	/**
	 * Called when the user selects a specific data line for a specific data type.
	 * 
	 * @param dataInfo
	 * @param dataLine
	 */
	public void selectDataLine(TDDataInfoFX dataInfo, TDScaleInfo dataLine) {
		dataInfo.selectScaleInfo(dataLine);
		setAxisName(dataLine.getDataTypeInfo());
		checkAxis();
		repaint(0);
	}

	// /**
	// * Returns a list of currently selected data units.
	// * @return a list of currently selected data units.
	// */
	// public ArrayList<FoundDataUnitFX> getSelectedDataUnits() {
	// return selectedDataUnits;
	// }

	/**
	 * Repaint all the plot panels and all canvas's within those panels on this
	 * graph.
	 * 
	 * @param -   the repaint flag which specified what canvas should be repainted.
	 * @param tm- if within millis of last repaint don't repaint
	 */
	public synchronized void repaint(long tm) {
		repaint(tm, TDPlotPane.ALL_CANVAS);
	}

	/**
	 * Repaint all the plot panels on this graph.
	 * 
	 * @param -   the repaint flag which specified what canvas should be repainted.
	 * @param tm- if within millis of last repaint don't repaint
	 */
	public synchronized void repaint(long tm, int flag) {

		// Start of block moved over from the panel repaint(tm) function.
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastTime < tm) {
			// start a timer. If a repaint hasn't be called because diff is too short this
			// will ensure that
			// the last repaint which is less than diff is called. This means a final
			// repaint is always called
			if (timeline != null)
				timeline.stop();
			timeline = new Timeline(new KeyFrame(Duration.millis(tm), ae -> repaint(0, flag)));
			timeline.play();
			return;
		}
		lastTime = currentTime;

		// // End of block moved over from the panel repaint(tm) function.
		int n = 0;
		synchronized (graphProjector) {
			// only clear the however list if the the flag to repaint contains the
			// front_canvas
			// this prevents a background canvas from clearing the list.
			if (hasCanvas(flag, TDPlotPane.FRONT_CANVAS))
				graphProjector.clearHoverList();
			for (TDPlotPane plotPanel : tdPlotPanels) {
				// only repaint if a plot panel is shown
				/*
				 * Commented this - it stops the repaint and leaves the hover list blank, so end
				 * up with empty hover data and overlay marking won't work either.
				 */
				// if (this.getCurrentScaleInfo().getVisibleChannels()[n])
				plotPanel.repaint(0, flag); // always do, to make sure hover and mark information is up to date.
				n++;
			}
		}
		// System.out.println("Hoverdata N = " +
		// graphProjector.getHoverDataList().size());
		// axisPanel.repaint();
	}

	/**
	 * Repaint all marks on the plot panels, but not underlying dtections.
	 */
	public void repaintMarks() {
		int n = 0;
		for (TDPlotPane plotPanel : tdPlotPanels) {
			// only repaint if a plot panel is shown
			if (this.getCurrentScaleInfo().getVisibleChannels()[n])
				plotPanel.repaintMarks();
			n++;
			// System.out.println("Hoverdata N mark = " +
			// graphProjector.getHoverDataList().size());
		}
	}

	/**
	 * Get the list of data blocks that are displayed in the tdGraph
	 * 
	 * @return list of TDDataInfoFX (wrapper for a {@link #PamDataBlock}) that are
	 *         displayed by the tdGraph.
	 */
	public ArrayList<TDDataInfoFX> getDataList() {
		return dataList;
	}

	/**
	 * Get a list on y axis types which can be displayed on the graph.
	 * 
	 * @return an of strings representing the names of available y axis types.
	 */
	public ArrayList<DataTypeInfo> getAxisNames() {
		return this.availableAxisNames;
	}

	/**
	 * Creates a list of y axis values that can be shown on the graph. This depends
	 * on the data blocks that the graph is already subscribed to. For example if
	 * subscribed to a click data block axis names of bearing, amplitude, ICI etc.
	 * would be available.
	 */
	public void createAxisNamesList() {
		availableAxisNames.clear();
		for (TDDataInfoFX dataInfo : dataList) {
			if (dataInfo == null) {
				continue;
			}
			ArrayList<TDScaleInfo> scaleInfos = dataInfo.getScaleInfos();
			for (TDScaleInfo lineInfo : scaleInfos) {
				if (!hasAvailableAxisName(lineInfo.getDataTypeInfo())) {
					availableAxisNames.add(lineInfo.getDataTypeInfo());
				}
			}
		}
	}

	/**
	 * Check that the list of axis names does not already contain the name of an
	 * axis
	 * 
	 * @param units - axis name
	 * @return true of the list already contains the name of an axis.
	 */
	public boolean hasAvailableAxisName(DataTypeInfo units) {
		for (DataTypeInfo dataType : availableAxisNames) {
			if (dataType.equals(units)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the TDMainDsiplay this TDGraphFX belongs to. The Main display contains
	 * most of the time axis information.
	 * 
	 * @return the TDMainDisplay.
	 */
	public TDDisplayFX getTDDisplay() {
		return tdDisplay;
	}

	/**
	 * Get the hiding pane which allows users to change y axis types and data blocks
	 * which are displayed.
	 * 
	 * @return hiding pane which contains nodes for changing settings
	 */
	public HidingPane getAxisPane() {
		return stackPane.getLeftHidingPane();
	}

	/**
	 * Get the hiding pane which allows users to change data block specific display
	 * settings.
	 * 
	 * @return hiding pane which contains nodes for changing settings
	 */
	public HidingPane getSettingsPane() {
		return stackPane.getRightHidingPane();
	}

	/*********** Viewer Mode Functions **************/
	/**
	 * Called in viewer mode when the time scroller moves
	 * 
	 * @param valueMillis new scroll value in millis
	 */
	public void timeScrollValueChanged(double valueMillis) {
		for (TDDataInfoFX anInfo : dataList) {
			anInfo.timeScrollValueChanged(valueMillis);
		}
	}

	/**
	 * Called in viewer mode when the time scroll range moves.
	 * 
	 * @param minimumMillis new minimum in millis
	 * @param maximumMillis new maximum in millis.
	 */
	public void timeScrollRangeChanged(long minimumMillis, long maximumMillis) {
		for (TDDataInfoFX anInfo : dataList) {
			anInfo.timeScrollRangeChanged(minimumMillis, maximumMillis);
		}
	}

	/*********** Graph Parameters and Serialization **************/

	/**
	 * A bit different to the standard getter in that this only gets called just
	 * before the configuration is serialized into the .psf. It's time to pull any
	 * configuration information out about every line drawn on this boomin' thing !
	 * 
	 * @return graph parameters ready to serialised.
	 */
	public TDGraphParametersFX prepareGraphParameters() {
		graphParameters.clearScaleInformationData();
		graphParameters.dataListInfos = new ArrayList<>();
		for (TDDataInfoFX dataInfo : dataList) {
			DataListInfo dli = new DataListInfo(dataInfo.getDataProvider().getClass(), dataInfo.getDataName(),
					dataInfo.getScaleInfoIndex(), dataInfo.getStoredSettings());
			dli.isShowing = dataInfo.isShowing();
			graphParameters.dataListInfos.add(dli);
			graphParameters.plotFillS = graphParameters.plotFill.toString();
			// now need to save which data is displayed
			if (this.getCurrentScaleInfo() == null) {
				findFirstActiveScaleinfo();
			}
			if (getCurrentScaleInfo() != null) {
				graphParameters.currentDataType = this.getCurrentScaleInfo().getDataTypeInfo();
			}

			/**
			 * Now the scale information data ... Note when several data types are sharing
			 * an axis, it's likely that the data will have only been set in the first of
			 * each type do only do the one of each ParameterType !
			 * 
			 */
			ArrayList<TDScaleInfo> scaleInfos = dataInfo.getScaleInfos();
			for (TDScaleInfo scaleInfo : scaleInfos) {
				if (graphParameters.getScaleInfoData(scaleInfo.getDataTypeInfo()) == null) {
					graphParameters.setScaleInfoData(scaleInfo.getDataTypeInfo(), scaleInfo.getScaleInfoData());
				}
			}
		}

		return graphParameters;
	}

	/**
	 * This only gets called when the serialised settings from .psf file have been
	 * loaded, not at any other time !
	 * 
	 * @param graphParameters the graphParameters to set
	 */
	public void setGraphParameters(TDGraphParametersFX graphParams) {

		this.graphParameters = graphParams;
		
		
		/**
		 * TODO The data type is messed up when you add all the different data type
		 * infos because the currentInfo does not equal graphParameters.currentDataType
		 * when axis are checked and therefore the graphParameters.currentDataType is
		 * set to the current info. This was introduced so that data appeared when a new
		 * datainfo was added to a blank graph for the first time but broke the new
		 * settings. Keeping a record of the original settings then setting at the end
		 * of loading the paramters works but is a little messy. there is probably a
		 * more elegant solution to this whole thing.
		 */
		DataTypeInfo currentDataType = this.graphParameters.currentDataType; 
		
		
		//System.out.println("TDGraphFX: Bfr: The graph data type is: " + this.graphParameters.currentDataType.getTypeString()); 

		if (graphParameters.plotFillS != null)
			graphParameters.plotFill = Color.valueOf(graphParameters.plotFillS);
		else
			graphParameters.plotFill = Color.WHITE;

		if (graphParameters.dataListInfos != null) {
			for (DataListInfo listInfo : graphParameters.dataListInfos) {
				TDDataProviderFX dataProvider = TDDataProviderRegisterFX.getInstance()
						.findDataProvider(listInfo.providerClass, listInfo.providerName);
				if (dataProvider == null) {
					System.err.println("Unable to find data plot provider " + listInfo.providerName);
					continue;
				}
				TDDataInfoFX dataInfo = dataProvider.createDataInfo(this);
				dataInfo.setShowing(listInfo.isShowing);
				// dataInfo.setCurrentAxisName(graphParameters.currentAxisName);
				
				addDataItem(dataInfo);
				if (listInfo.listSettings != null) {
					dataInfo.setStoredSettings(listInfo.listSettings);
				}

				/**
				 * Now the scale information data ... Note that all data types sharing an axis
				 * should have the same values set in it, so can just put over the top on each.
				 */
				ArrayList<TDScaleInfo> scaleInfos = dataInfo.getScaleInfos();

				for (TDScaleInfo scaleInfo : scaleInfos) {

					TDScaleInfoData scaleData = graphParameters.getScaleInfoData(scaleInfo.getDataTypeInfo());
					if (scaleData != null) {
						/**
						 * Occasionally there can be an error in the serialized settings (from previous
						 * version of PG with a bug) where the TDScaleInfos resets minimum and maximum
						 * values to zero.
						 */
						if (scaleData.minVal != scaleData.maxVal) {
							// if the min and max values are equal then default settings will be used...
							scaleInfo.setScaleInfoData(scaleData);
						}
					}
				}
			}
		}

		//System.out.println("TDGraphFX: Aftr: The graph data type is: " + this.graphParameters.currentDataType.getTypeString()); 
		this.setAxisName(currentDataType);
		
		tdAxisSelPane.remakePane();
		tdAxisSelPane.selectAxisType();
		setAxisName(graphParameters.currentDataType);

	}

	/**
	 * Get the parameters for this graph.
	 * 
	 * @return thje paramters for the graph.
	 */
	public TDGraphParametersFX getGraphParameters() {
		return graphParameters;
	}

	/**
	 * Check whether the display is paused. This will universally apply to all
	 * TDGraphs in the main TDDisplay.
	 * 
	 * @return true if the display is paused. False if the display is scrolling.
	 */
	public boolean isPaused() {
		return getTDDisplay().getTDControl().isPaused();
	}

	/**
	 * Called whenever the visible time range is changed, usually by the time range
	 * spinner on tdDisplay scroll bar
	 * 
	 * @param oldMillis - the old value in millis
	 * @param newMillis - the new value in millis
	 */
	public void timeRangeSpinnerChange(long oldMillis, long newMillis) {
		for (TDDataInfoFX anInfo : dataList) {
			anInfo.timeRangeSpinnerChange(oldMillis, newMillis);
		}
	}

	/**
	 * Open the settings pane and expand all tabs.
	 * 
	 * @param show - true to show the pane. False to hide everything
	 */
	public void showSettingsPane(boolean show) {
		// open the hide pane.
		this.getSettingsPane().showHidePane(show);
		// expand all the hide tabs.
		for (int i = 0; i < this.settingsTabs.getTabs().size(); i++) {
			this.settingsTabs.getTabs().get(i).showTab(show);
		}
	}

	/**
	 * Expand the axis hiding pane.
	 */
	public void showAxisSettingsPane(boolean show) {
		this.getAxisPane().showHidePane(show);
	}

	/**
	 * Get the position of the last wrap in milliseconds
	 * 
	 * @return the last wrap position in milliseconds.
	 */
	public long getLastWrapMillis() {
		return this.tdDisplay.getWrapLastMillis();
	}

	/**
	 * Get the position of the wrap in the screen in pixels.
	 * 
	 * @return wrap positon in pixels.
	 */
	public double getWrapPix() {
		return this.tdDisplay.getWrapPix();
	}

	/**
	 * Get the base canvas for the tdGraph.
	 * 
	 * @param iPlot - the plot
	 * @return the base canvas for a TDPlot.
	 */
	public Canvas getBaseCanvas(int iPlot) {
		return this.tdPlotPanels.get(iPlot).getBaseCanvas();
	}

	/**
	 * Convenience class to get the graph orientation.
	 * 
	 * @return the graph orientation.
	 */
	public Orientation getOrientation() {
		return this.tdDisplay.getTDParams().orientation;
	}

	/**
	 * Convenience function to check whether the graph is wrapping.
	 * 
	 * @return true if the graph is wrapping. False if scrolling.
	 */
	public boolean isWrap() {
		return this.tdDisplay.isWrap();
	}

	/**
	 * The width property of the plot pane (without y axis)
	 * 
	 * @return the height property of the plot pane
	 */
	public ReadOnlyDoubleProperty getPlotWidthProperty() {
		return plotPanels.widthProperty();
	}

	/**
	 * The height property of the plot pane (without time axis)
	 * 
	 * @return the width property of the plot pane
	 */
	public ReadOnlyDoubleProperty getPlotHeightProperty() {
		return plotPanels.heightProperty();
	}

	/**
	 * Number of currently visible plots property. Note that usually this is one but
	 * in cases where channels are usually viewed separately, e.g. the spectrogram,
	 * there may be more than one panel. This is the number of visible panels.
	 * 
	 * @return the number of visible plots in the TDGraph property. .
	 */
	public ReadOnlyIntegerProperty nPanelsProperty() {
		return this.nPlots;
	}

	/**
	 * Get the number of currently visible plots. Note that usually this is one but
	 * in cases where channels are usually viewed separately, e.g. the spectrogram,
	 * there may be more than one panel. This is the number of visible panels.
	 * 
	 * @return the number of visible plots in the TDGraph.
	 */
	public int getNPanels() {
		return nPlots.get();
	}

	/**
	 * Check whether PG is running in real time.
	 * 
	 * @return true if running in real time
	 */
	public boolean isRunning() {
		return this.tdDisplay.isRunning();
	}

	/**
	 * @return the graphId
	 */
	public int getGraphId() {
		return graphId;
	}

	/**
	 * Get the unique name for the graph. This is used for mark observers and
	 * symbols managers to identify the display.
	 * 
	 * @return the unique name for the graph.
	 */
	public String getUniqueName() {
		String uName = getTDDisplay().getTDControl().getUniqueName() + " Graph " + getGraphId();
		if (getGraphId() < 0) {
			getGraphId();
		}
		return uName;
	}

	/**
	 * Get the overlay marker manager. This handles all mouse and touch interactions
	 * with the TDGrpahFX.
	 * 
	 * @return the OverlayMarkerManager for the TDGrpahFX.
	 */
	public OverlayMarkerManager getOverlayMarkerManager() {
		return this.overlayMarker;
	}

	/**
	 * Adds a button to the center of the graph which can be used to test the
	 * projector.
	 */
	@SuppressWarnings("unused")
	private void buttonTimeTest() {

		// TEST
		Button buttonTest = new PamButton("Doug;s Time Test");
		buttonTest.setOnAction((action) -> {
			long millisTest = this.tdDisplay.getTimeStart() + 1000;
			PamCoordinate result = this.getGraphProjector().getCoord3d(new Coordinate3d(millisTest, 150, 0));
			PamCoordinate resultBack = this.getGraphProjector().getDataPosition(result);
			// System.out.println("TDGraphFX (end of constructor): TEST FOUR DOUG: ");
			// System.out.println("TDGraphFX: Time in: " + millisTest + " " +
			// PamCalendar.formatDateTime(millisTest));
			// System.out.println("TDGraphFX: pixel positions: " +result.getCoordinate(0) +
			// " pixels from start");
			// System.out.println("TDGraphFX: millis back: " +resultBack.getCoordinate(0) +
			// " "+ PamCalendar.formatDateTime((long) resultBack.getCoordinate(0)));

		});
		stackPane.getChildren().add(buttonTest);
		// Test
	}

	/**
	 * Find the TDDataInfo for a data unit on the TDGraph. Note that if two
	 * TDataInfos are present (they should not be) then the first oin the list is
	 * returned.
	 * 
	 * @param dataUnit - the data unit to find the datainfo for
	 * @return the TDDataInfoFX which displays the data unit. null if no
	 *         TDDataInfoFX is found.
	 */
	public TDDataInfoFX findDataInfo(PamDataUnit dataUnit) {
		for (TDDataInfoFX dataInfo : this.dataList) {
			if (dataInfo.getDataBlock() == dataUnit.getParentDataBlock()) {
				return dataInfo;
			}
		}
		return null;
	}

	/**
	 * Check whether the display should be paused. This could be due to number of
	 * factors, e.g. the user having drawn a mark.
	 * 
	 * @return true if the display should be paused.
	 */
	public boolean needPaused() {
		if (overlayMarker.needPaused()) {
			return true;
		}
		return false;
	}

	/**
	 * Called whenever a zoom request is sent
	 * 
	 * @param zoomIn - true to zoom in. @return. True if a zoom has occurred due to
	 *               an overlay mark. False if no zoom occurred.
	 */
	public boolean zoomGraph(boolean zoomIn) {
		if (zoomIn) {
			// zoom in;
			OverlayMark overlayMark = getOverlayMarkerManager().getCurrentMarker().getOverlayMark();

			// System.out.println(this.getUniqueName() + " marker " +
			// getOverlayMarkerManager().getCurrentMarker().getOverlayMark());

			if (overlayMark != null) {

				// work out last limits;
				lastLimitsZoom = new double[4];
				lastLimitsZoom[0] = this.getTDDisplay().getTimeScroller().getValueMillis();
				lastLimitsZoom[1] = this.getTDDisplay().getTimeScroller().getValueMillis()
						+ this.getTDDisplay().getTimeScroller().getVisibleMillis();
				lastLimitsZoom[2] = this.getCurrentScaleInfo().getMinVal();
				lastLimitsZoom[3] = this.getCurrentScaleInfo().getMaxVal();

				double[] scaleLimits = overlayMark.getLimits();

				// HACK to stop previous marks interfering which have not quite been
				// destroyed...
				if (scaleLimits[0] == scaleLimits[1] || scaleLimits[2] == scaleLimits[3])
					return false;

				zoomToLimits(scaleLimits);

				return true;
			} else
				return false;
		} else {
			// do we have a last zoom in? If so lets go back to that. If not don't zoom and
			// leave to the tdDisplay to handle what to do.
			if (lastLimitsZoom != null) {
				zoomToLimits(lastLimitsZoom);
				lastLimitsZoom = null; // last limits are now null.
				return true;
			}
		}
		return false;
	}

	/**
	 * Zoom into our out to limits
	 * 
	 * @param four element array of limits. Format is (minX, maxX, minY, maxY)
	 */
	private void zoomToLimits(double[] scaleLimits) {
		// System.out.println("The limits are: minX: " + scaleLimits[0] + " maxX:
		// "+scaleLimits[1] + " minY: "+ scaleLimits[2] + " maxY: "+scaleLimits[3]);

		// now zoom;
		this.getCurrentScaleInfo().setMinVal(scaleLimits[2]);
		this.getCurrentScaleInfo().setMaxVal(scaleLimits[3]);

		this.getTDDisplay().getTimeScroller().setValueMillis((long) scaleLimits[0]);
		this.getTDDisplay().getTimeScroller().setVisibleMillis((long) (scaleLimits[1] - scaleLimits[0]));
	}

	/**
	 * Notifications from the PamController are passed to this function.
	 * 
	 * @param changeType - notification flag.
	 */
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamController.CHANGED_PROCESS_SETTINGS:
			// needed to pick up when sequence number maps have changed in output data
			// blocks.
			sortAxisandPanes();
			break;
		case PamController.OFFLINE_PROCESS_COMPLETE:
			// System.out.println("OFFLINE PROCESS COMPLETE");
			this.repaint(0);
			break;
		}
		// notify all the data infos.
		for (TDDataInfoFX tdDataInfo : this.getDataList()) {
			tdDataInfo.notifyChange(changeType);
		}

	}

	/**
	 * Called when we receive an update from the sound playback manager
	 * <p>
	 * Use this to update cursors on the main displays
	 */
	public void playbackUpdate() {
		repaint(100);
	}

	/**
	 * Get the current minimum axis value. This should be the same as the values at
	 * the end of the Y axis (apart from scaling factors) and should be used in
	 * preference to additional bound parameters.
	 * 
	 * @return Minimum Y axis value.
	 */
	public double getDataAxisMinVal() {
		if (currentScaleInfo == null)
			return 0;
		if (currentScaleInfo.getReverseAxis()) {
			return currentScaleInfo.getMaxVal();
		} else {
			return currentScaleInfo.getMinVal();
		}
	}

	/**
	 * Get the current maximum axis value. This should be the same as the values at
	 * the end of the Y axis (apart from scaling factors) and should be used in
	 * preference to additional bound parameters.
	 * 
	 * @return Maximum Y axis value.
	 */
	public double getDataAxisMaxVal() {
		if (currentScaleInfo == null)
			return 1;
		if (currentScaleInfo.getReverseAxis()) {
			return currentScaleInfo.getMinVal();
		} else {
			return currentScaleInfo.getMaxVal();
		}
	}

	/**
	 * True if the graph is scrolling.
	 * 
	 * @return the graph is scrolling.
	 */
	public boolean isScrolling() {
		return this.getTDDisplay().getTimeScroller().isScrollerChanging();
	}

	/**
	 * Calculate the canvas map to repaint.
	 * 
	 * @param canvasReapint - all canvas flags to repaint e.g.
	 *                      TDPlotPane.BASE_CANAVAS
	 * @return bitmap of canvas's to repaint. This can be input into repaint
	 *         function.
	 */
	public static int canvasMap(int... canvasRepaint) {
		int canvasMap = 0;
		for (int arg : canvasRepaint) {
			canvasMap = canvasMap | arg;
		}
		return canvasMap;
	}

	/**
	 * Check whether a canvas repaint flag contains a flag.
	 * 
	 * @param canvasRepaint - a bitmap of canvas maps
	 * @param canvasFlag    - the canvas flag to check for e.g.
	 *                      TDPlotPane.BASE_CANVAS.
	 * @return true if the flag is contained in the canvasRepaint.
	 */
	public static boolean hasCanvas(int canvasRepaint, int canvasFlag) {
		if ((canvasFlag & canvasRepaint) != 0)
			return true;
		return false;
	}

	/**
	 * Print a list of the current data infos.
	 */
	public void printDataInfos() {
		for (int i = 0; i < this.dataList.size(); i++) {
			System.out.println(dataList.get(i).getDataName());
		}
	}

}
