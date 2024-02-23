package pamViewFX.fxNodes.utilityPanes;


import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamComboBox;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamRadioButton;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamChart.LogarithmicAxis;
import pamViewFX.fxNodes.pamChart.PamLineChart;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamController;
import PamController.SettingsPane;


public class FilterPaneFX extends SettingsPane<FilterParams> {

	private String[] filterNames = { "None", "IIR Butterworth", "IIR Chebyshev", 
			"FIR Filter (Window Method)", "Arbitrary FIR Filter"};

	/**
	 * Combo box for selecting filter types. 
	 */
	private PamComboBox<String> filterTypes;

	/**
	 * Text field for upper frequency
	 */
	private PamSpinner<Double> highCut;

	/**
	 * Text field for lower frequency
	 */
	private PamSpinner<Double> lowPass;

	/**
	 * Spinner to allow user to change filter order. 
	 */
	private PamSpinner<Integer> filterOrder;

	/**
	 * Spinner to allow user to change filter order. 
	 */
	private PamSpinner<Integer> ripple;

	/**
	 * The current filter. 
	 */
	private FilterParams filterParams;

	//radio buttons for filter type
	private PamRadioButton highPassrb;

	private PamRadioButton bandPassrb;

	private PamRadioButton bandStoprb;

	private PamRadioButton lowPassrb;

	private PamRadioButton logScale;

	private PamRadioButton linScale;

	/**
	 * Some default frequency values for the high and low pass frequency spinner. 
	 */
	double[] defaultFrequencyVals={10.,100.,1000.,2000.,5000.,10000.,20000.,30000.,40000.,50000.,60000.,80000.,100000.,120000.,150000.,200000.};

	/**
	 * The VBox which holds most of the controls. This can be used to add extra custom controls. 
	 */
	private PamVBox controlPane;

	/**
	 * The current sample rate. This should be set explicitly by a listener if linked to a source pane. 
	 */
	private float sampleRate=100000;

	/**
	 * The current filter method. 
	 */
	private FilterMethod filterMethod;

	/**
	 * Plot pane for the graph axis. 
	 */
	private PamLineChart<Number, Number> plotLogChart;

	/**
	 * Line chart without logarithmic frequency axis.
	 */
	private PamLineChart<Number, Number> plotLinChart;

	/**
	 * Log axis for log chart
	 */
	private LogarithmicAxis logarithmAxis;

	/**
	 * Holds the graph
	 */
	private PamBorderPane pamBorderPane;

	/**
	 * Ripple label
	 */
	private Label rippleLabel;

	private PamBorderPane mainPane = new PamBorderPane();
	
	public FilterPaneFX () {
		this(Orientation.HORIZONTAL);
	}


	public FilterPaneFX (Orientation orientaiton) {
		super(null);
		if (orientaiton == Orientation.HORIZONTAL) {
			mainPane.setLeft(createFilterPane());
			mainPane.setCenter(createBodeGraph()); 
		}
		else {
			mainPane.setTop(createFilterPane());
			mainPane.setCenter(createBodeGraph()); 
		}
	}



	/**
	 * Create the filter pane. This contains controls to change filter types and shows a graph of the current filter. 
	 * @return pane with controls to change filter params.
	 */
	public Node createFilterPane(){
		controlPane=new PamVBox();
		controlPane.setSpacing(5);

		//label title
		Label title=new Label("Filter type");
		PamGuiManagerFX.titleFont2style(title);

		//		title.setFont(PamGuiManagerFX.titleFontSize2);
		controlPane.getChildren().add(title);

		//create combo box
		filterTypes=new PamComboBox<String>();
		for (int i=0; i<filterNames.length; i++){
			filterTypes.getItems().add(filterNames[i]);
		}
		filterTypes.setOnAction((event) -> {
			setSelectedFilterType();
			updateBodeGraph();
			enableControls();
		});
		filterTypes.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(filterTypes, Priority.ALWAYS);
		controlPane.getChildren().add(filterTypes);

		//Radio buttons and frequency response
		Label freqResponse=new Label("Frequency Response");
		//		freqResponse.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(freqResponse);

		controlPane.getChildren().add(freqResponse);

		//radio buttons
		final ToggleGroup group = new ToggleGroup();
		//		group.selectedToggleProperty().addListener((obs, old_val, new_val)-> {
		//			if (group.getSelectedToggle() != null) {
		//
		//			}           
		//		});
		PamVBox radioButtonBox=new PamVBox();
		radioButtonBox.setSpacing(3);

		highPassrb = new PamRadioButton("High Pass");
		highPassrb.setOnAction((action)->{
			filterParams.filterBand=FilterBand.HIGHPASS;
			updateBodeGraph();
			enableControls();
		});
		highPassrb.setToggleGroup(group);
		radioButtonBox.getChildren().add(highPassrb);

		bandPassrb = new PamRadioButton("Band Pass");
		bandPassrb.setToggleGroup(group);
		bandPassrb.setOnAction((action)->{
			filterParams.filterBand=FilterBand.BANDPASS;
			enableControls();
			updateBodeGraph();
		});
		radioButtonBox.getChildren().add(bandPassrb);

		bandStoprb = new PamRadioButton("Band Stop");
		bandStoprb.setToggleGroup(group);
		bandStoprb.setOnAction((action)->{
			filterParams.filterBand=FilterBand.BANDSTOP;
			enableControls();
			updateBodeGraph();
		});
		radioButtonBox.getChildren().add(bandStoprb);

		lowPassrb = new PamRadioButton("Low Pass");
		lowPassrb.setToggleGroup(group);
		lowPassrb.setOnAction((action)->{
			filterParams.filterBand=FilterBand.LOWPASS;
			enableControls();
			updateBodeGraph();
		});
		radioButtonBox.getChildren().add(lowPassrb);

		//create frequency text boxes
		PamGridPane gridPaneFreq=new PamGridPane(); 
		gridPaneFreq.setHgap(5); 
		gridPaneFreq.setVgap(5);

		//lost of filter order sizes- anyone using >500 will be crazy anyway
		//		ObservableList<Double> freqList=FXCollections.observableArrayList();
		//		for (int i=0; i<defaultFrequencyVals.length; i++){
		//			freqList.add(defaultFrequencyVals[i]); 
		//		}

		highCut=new PamSpinner<Double>(10.,500000.,2000.,2000.);
		highCut.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		highCut.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after>=lowPass.getValue()) highCut.getValueFactory().setValue(Math.max(10,lowPass.getValue()-100)); 
			if (after>sampleRate/2.) highCut.getValueFactory().setValue(sampleRate/2.); 
			filterParams.highPassFreq=highCut.getValue().floatValue();
			this.updateBodeGraph();
		});
		highCut.setEditable(true);
		//highCut.setPrefColumnCount(6);
		gridPaneFreq.add(new Label("High Pass"), 0, 0);
		gridPaneFreq.add(highCut, 1, 0);
		gridPaneFreq.add(new Label("Hz"), 2, 0);

		lowPass=new PamSpinner<Double>(10.,500000.,2000.,2000.);
		lowPass.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		lowPass.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after<=highCut.getValue()) lowPass.getValueFactory().setValue(Math.min(sampleRate/2.,highCut.getValue()+100));
			if (after>sampleRate/2.) lowPass.getValueFactory().setValue(sampleRate/2.); 
			filterParams.lowPassFreq=lowPass.getValue().floatValue();
			enableControls();
			this.updateBodeGraph();
		});
		lowPass.setEditable(true);
		//lowCut.setPrefColumnCount(6);
		gridPaneFreq.add(new Label("Low Pass"), 0, 1);
		gridPaneFreq.add(lowPass, 1, 1);
		gridPaneFreq.add(new Label("Hz"), 2, 1);

		PamHBox filterSettings=new PamHBox(); //holds radio buttons and frquency text fields. 
		filterSettings.getChildren().addAll(radioButtonBox, gridPaneFreq);
		filterSettings.setSpacing(15);
		controlPane.getChildren().add(filterSettings);


		//next create filter order settings
		Label filterOrderLabel=new Label("Filter Order");
		PamGuiManagerFX.titleFont2style(filterOrderLabel);

		//		filterOrderLabel.setFont(PamGuiManagerFX.titleFontSize2);
		controlPane.getChildren().add(filterOrderLabel);


		PamGridPane gridPaneOrder=new PamGridPane(); 
		gridPaneOrder.setHgap(5); 
		gridPaneOrder.setVgap(5);

		filterOrder=new PamSpinner<Integer>(1,500,2,1);
		filterOrder.setEditable(true);
		filterOrder.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		filterOrder.getValueFactory().valueProperty().addListener((obs, before, after)->{
			filterParams.filterOrder=filterOrder.getValue();
			enableControls();
			this.updateBodeGraph();
		});
		gridPaneOrder.add(new Label("Filter Order"), 0,0); 
		gridPaneOrder.add( filterOrder, 1,0); 

		ripple=new PamSpinner<Integer>(1,500,2,1);
		ripple.setEditable(true);
		ripple.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		ripple.getValueFactory().valueProperty().addListener((obs, before, after)->{
			filterParams.passBandRipple=ripple.getValue();
			enableControls();
			this.updateBodeGraph();
		});
		gridPaneOrder.add(rippleLabel=new Label("Pass Band Ripple"), 0,1); 
		gridPaneOrder.add( ripple, 1,1); 

		controlPane.getChildren().add(gridPaneOrder);


		// TODO Auto-generated method stub
		return controlPane;
	}

	/**
	 * Set correct filter type from filter type combo box selection. 
	 */
	private void setSelectedFilterType(){
		if (filterTypes.getSelectionModel().getSelectedIndex() == 0)
			filterParams.filterType = FilterType.NONE;
		else if (filterTypes.getSelectionModel().getSelectedIndex() == 1)
			filterParams.filterType = FilterType.BUTTERWORTH;
		else if (filterTypes.getSelectionModel().getSelectedIndex() == 2)
			filterParams.filterType = FilterType.CHEBYCHEV;
		else if (filterTypes.getSelectionModel().getSelectedIndex() == 3)
			filterParams.filterType = FilterType.FIRWINDOW;
		else if (filterTypes.getSelectionModel().getSelectedIndex() == 4)
			filterParams.filterType = FilterType.FIRARBITRARY;
	}

	@Override
	public FilterParams getParams(FilterParams f) {
		try {
			//select fiilter type from combo box. 
			setSelectedFilterType();

			if (highPassrb.isSelected())
				filterParams.filterBand = FilterBand.HIGHPASS;
			if (bandPassrb.isSelected())
				filterParams.filterBand = FilterBand.BANDPASS;
			if (bandStoprb.isSelected())
				filterParams.filterBand = FilterBand.BANDSTOP;
			if (lowPassrb.isSelected())
				filterParams.filterBand = FilterBand.LOWPASS;

			if (highCut.getValue() > 0)
				filterParams.highPassFreq =highCut.getValue().floatValue(); 
			if (lowPass.getValue() > 0)
				filterParams.lowPassFreq = lowPass.getValue().floatValue(); 
			if (filterOrder.getValue() > 0)
				filterParams.filterOrder = filterOrder.getValue();
			if (ripple.getValue() > 0)
				switch (filterParams.filterType) {
				case CHEBYCHEV:
					filterParams.passBandRipple = ripple.getValue();
					break;
				case FIRWINDOW:
				case FIRARBITRARY:
					filterParams.chebyGamma = ripple.getValue();
					break;
				}

		}
		catch (NumberFormatException ex) {
			ex.printStackTrace();
			return null;
		}
		filterParams.scaleType = getScaleType();
		double niquist = sampleRate / 2.;
		if (filterParams.filterBand == FilterBand.BANDPASS || 
				filterParams.filterBand == FilterBand.BANDSTOP || 
				filterParams.filterBand == FilterBand.LOWPASS) {
			if (filterParams.lowPassFreq > niquist) {
				PamController.getInstance();
				PamDialogFX.showWarning(PamController.getMainStage(), "Filter Settings", "The low pass cut off frequency is too high");
				return null; 
			}
		}
		if (filterParams.filterBand == FilterBand.BANDPASS  || 
				filterParams.filterBand == FilterBand.BANDSTOP || 
				filterParams.filterBand == FilterBand.HIGHPASS) {
			if (filterParams.highPassFreq > niquist) {
				PamDialogFX.showWarning(PamController.getMainStage(),"Filter Settings", "The high pass cut off frequency is too high");
				return null; 
			}
		}

		/**
		 * Here - need to change the filter type if it's an FIR or IIRF filter. 
		 */
		//		filterMethod(filterParams);
		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);

		return filterParams;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);
	}


	public Node createBodeGraph(){

		pamBorderPane=new PamBorderPane(); 
		//pamBorderPane.setPadding(new Insets(0, 0, 0, 10));
		pamBorderPane.setPrefWidth(500);

		final ToggleGroup group = new ToggleGroup();
		logScale = new PamRadioButton("Log Scale");
		logScale.setOnAction((action)->{
			setGraphLogAxis(logScale.isSelected());
		});
		logScale.setToggleGroup(group);
		linScale  = new PamRadioButton("Linear Scale");
		linScale.setToggleGroup(group);
		linScale.setOnAction((action)->{
			setGraphLogAxis(!linScale.isSelected());
		});

		final PamHBox scalePane=new PamHBox(); 
		scalePane.setPadding(new Insets(5));
		scalePane.setSpacing(5);
		scalePane.getChildren().addAll(linScale, logScale); 

		plotLogChart=createBodeChart(logarithmAxis=new LogarithmicAxis(10,50000)); 
		plotLinChart=createBodeChart(new NumberAxis(0, 50000, 10000)); 

		pamBorderPane.setTop(scalePane);
		pamBorderPane.setCenter(plotLogChart);

		return pamBorderPane;

	}

	/**
	 * Convenience function to stop code repitition. Creates a line chart fro pane with specified x axis. 
	 * @param xAxis - x axis
	 * @return a chart in correct format for filter pane. 
	 */
	private PamLineChart<Number, Number> createBodeChart(ValueAxis<Number> xAxis){
		PamLineChart<Number, Number> plotChart=new PamLineChart<Number, Number>(xAxis, new NumberAxis());
		plotChart.setLegendVisible(false);
		plotChart.setCreateSymbols(false);

		plotChart.getXAxis().setLabel("Frequency (Hz)");
		plotChart.getYAxis().setLabel("dB");

		plotChart.getYAxis().setAutoRanging(false);
		plotChart.getXAxis().setSide(Side.TOP);

		/**
		 * HACK. Make sure the graph updates once the axis has a width. 
		 */
		xAxis.widthProperty().addListener(new ChangeListener<Number>() {

			private boolean axisInitialised=true; 

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (axisInitialised) {
					if (filterMethod!=null) updateBodeGraph(); 
					axisInitialised=false; 
				}
			}
		});
		return plotChart;
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void setParams(FilterParams filterParams) {
		this.filterParams=filterParams.clone();

		setSettings();
	}

	/**
	 * Set controls to input params.
	 */
	private void setSettings() {

		if (filterParams == null) {
			filterParams = new FilterParams();
		}
		switch (filterParams.filterType) {
		case NONE:
			filterTypes.getSelectionModel().select(0);
			break;
		case BUTTERWORTH:
			filterTypes.getSelectionModel().select(1);
			break;
		case CHEBYCHEV:
			filterTypes.getSelectionModel().select(2);
			break;
		case FIRWINDOW:
			if (filterNames.length > 3) {
				filterTypes.getSelectionModel().select(3);
			}
			break;
		case FIRARBITRARY:
			if (filterNames.length > 4) {
				filterTypes.getSelectionModel().select(4);
			}
		}
		switch (filterParams.filterBand) {
		case HIGHPASS:
			highPassrb.setSelected(true);
			break;
		case BANDPASS:
			bandPassrb.setSelected(true);
			break;
		case BANDSTOP:
			bandStoprb.setSelected(true);
			break;
		case LOWPASS:
			lowPassrb.setSelected(true);
			break;
		}


		//		highCut.setText(String.format("%1.1f", filterParams.highPassFreq));
		highCut.getValueFactory().setValue(Double.valueOf(Float.valueOf(filterParams.highPassFreq).doubleValue()));
		//		lowCut.setText(String.format("%1.1f", filterParams.lowPassFreq));
		lowPass.getValueFactory().setValue(Double.valueOf(Float.valueOf(filterParams.lowPassFreq).doubleValue()));

		filterOrder.getValueFactory().setValue(filterParams.filterOrder);
		setRippleParam();

		logScale.setSelected(filterParams.scaleType == FilterParams.SCALE_LOG);
		linScale.setSelected(filterParams.scaleType == FilterParams.SCALE_LIN);

		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);

		//update control disable status
		enableControls();
		//update graph
		setYAxisRange(yScales[0],2);
		setXAxisRange(10, sampleRate/2.);

	}

	void setRippleParam() {
		int filtType = filterTypes.getSelectionModel().getSelectedIndex();
		switch(filtType) {
		case 2:
			this.ripple.getValueFactory().setValue((int) filterParams.passBandRipple);
			break;
		case 3:
		case 4:
			this.ripple.getValueFactory().setValue((int) filterParams.chebyGamma);
			break;
		}
	}

	private void enableControls() {
		int filterType = filterTypes.getSelectionModel().getSelectedIndex();
		boolean haveFilter = filterType > 0;
		lowPassrb.setDisable(!haveFilter);
		highPassrb.setDisable(!haveFilter);
		bandPassrb.setDisable(!haveFilter);
		bandStoprb.setDisable(!haveFilter);
		highCut.setDisable(lowPassrb.isSelected() || !haveFilter);
		lowPass.setDisable(highPassrb.isSelected() || !haveFilter);
		filterOrder.setDisable(!haveFilter);
		ripple.setDisable(!haveFilter || filterType < 2);
		logScale.setDisable(!haveFilter);
		linScale.setDisable(!haveFilter);
		switch (filterType) {
		case 2:
			rippleLabel.setText("Pass band ripple ");
			break;
		case 3:
		case 4:
			rippleLabel.setText("Gamma  ");
			break;
		}
		boolean isArb = filterType == 4;
		//		normalPanel.setVisible(isArb == false);
		//		arbPanel.setVisible(isArb);
	}

	@Override
	public String getName() {
		return "Filter Pane";
	}

	/**
	 * A VBox which holds all the main controls for the pane but not the graph. Can be used to add in custom controls such
	 * as a source level pane. 
	 * @return a VBox holding the majority of controls in the pane.
	 */
	public PamVBox getControlPane() {
		return controlPane;
	}


	/************Bode Graph************/

	private int yScaleIndex = 0;

	private double[] yScales = {-90, -60, -30};


	/**
	 * Set the graph axis to a log or linear scale. 
	 * @param log - true to set chart with frequency log scale. 
	 */
	private void setGraphLogAxis(boolean log){
		if (log){
			pamBorderPane.setCenter(plotLogChart);

		}
		else{
			pamBorderPane.setCenter(plotLinChart);
		}
		//setYAxisRange(yScales[0],10);
		updateBodeGraph();
	}

	/**
	 * Update the bode graph
	 */
	public void updateBodeGraph(){
		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);
		if (logScale.isSelected()){
			plotLogChart.getData().removeAll(plotLogChart.getData());
			plotLogChart.getData().add(createFilterPoints((ValueAxis<Number>) plotLogChart.getXAxis()));
		}
		else{
			plotLinChart.getData().removeAll(plotLinChart.getData());
			plotLinChart.getData().add(createFilterPoints((ValueAxis<Number>) plotLinChart.getXAxis()));
		}
	}

	/**
	 * Set y axis of graphs
	 */
	private void setXAxisRange(double min, double max){
		logarithmAxis.setUpperBound(max);
		logarithmAxis.setLowerBound(min);
		((NumberAxis) plotLinChart.getXAxis()).setLowerBound(0);
		((NumberAxis) plotLinChart.getXAxis()).setUpperBound(max);
	}

	//Set x axis of graphs. 
	private void setYAxisRange(double min, double max){
		//System.out.println("Set y axis range: min "+min+" max "+max); 
		((NumberAxis) plotLinChart.getYAxis()).setLowerBound(min);
		((NumberAxis) plotLinChart.getYAxis()).setUpperBound(max);
		plotLinChart.getYAxis().requestLayout();
		plotLinChart.getYAxis().requestAxisLayout();

		((NumberAxis) plotLogChart.getYAxis()).setLowerBound(min);
		((NumberAxis) plotLogChart.getYAxis()).setUpperBound(max);
		plotLogChart.getYAxis().requestLayout();
		plotLogChart.getYAxis().requestAxisLayout();
	}

	/**
	 * Get the scale type from the graph. 
	 * @return
	 */
	private int getScaleType() {
		if (logScale.isSelected()) {
			return FilterParams.SCALE_LOG;
		}
		else {
			return FilterParams.SCALE_LIN;
		}
	}

	/**
	 * Create a data series for a filter. The function bases the data points on the pixel length of the chart frequency axis. This
	 * is important because a log scale is used which would mess up if a data point was created at regular frequency, rather than pixel
	 * intervals. 
	 * @param axis - the frequency axis to use (because the axis switches between a linear and log axis. )
	 * @return a data series to add to the line chart
	 */
	private Series<Number, Number> createFilterPoints(ValueAxis<Number> axis){
		
		if (filterMethod==null) return null;

		Series<Number, Number> series = new Series<Number, Number>();
		/*
		 * It's on a log scale, so set up enough points to fill the plot on
		 * a log scale
		 */
		//total number of pixels on axis
		//		double pixels=Math.abs(axis.getDisplayPosition(axis.getLowerBound())-axis.getDisplayPosition(axis.getUpperBound()));
		double pixels=axis.getWidth();
		int nPoints = (int) Math.max(pixels, 1024);

		//System.out.println("Update bode graph WIDTH: "+ pixels+ " height: " +this.plotLogChart.getYAxis().getHeight()+ this.plotLogChart.getWidth());

		double xScale = pixels / (double) nPoints;

		double[] freqPoints = new double[nPoints];
		double[] gainPoints = new double[nPoints];
		double[] phasePoints = new double[nPoints];

		int i=0;
		while (i < freqPoints.length) {
			freqPoints[i] = axis.getValueForDisplay(i*xScale).doubleValue();
			gainPoints[i] = filterMethod.getFilterGain(
					freqPoints[i] / sampleRate * 2 * Math.PI)
					/ filterMethod.getFilterGainConstant();
			phasePoints[i] = filterMethod.getFilterPhase(
					freqPoints[i] / sampleRate * 2 * Math.PI);
			//			System.out.println("freqPoints[i]: "+freqPoints[i] +" gainPoints[i] "+gainPoints[i]+
			//					" nPoints: "+nPoints+" xScale "+xScale+ " sampleRate: "+sampleRate + " pixels "+pixels); 
			series.getData().add(new Data<Number, Number>(freqPoints[i], 20. * Math.log10(gainPoints[i]))); 

			i++;
		}

		return series; 

	}

	private void checkYScale() {
		double yScale = 0;
		if (plotLogChart.getYAxis() == null) return;

		if (filterTypes.getSelectionModel().getSelectedItem() == "Arbitrary FIR Filter") {
			double[] g = filterParams.getArbGainsdB();
			if (g != null && g.length >= 1) {
				yScale = g[0];
				for (int i = 1; i < g.length; i++) {
					yScale = Math.max(yScale, g[i]);
				}
				// now round to nearest 10dB
				yScale = 10 * Math.round(yScale/10.);
			}
		}
		//TODO
		//if (getScaleType) ((LogarithmicAxis) plotPane.getYAxis()).setRange(yScale+yScales[yScaleIndex], yScale);
	}

	@Override
	public void paneInitialized() {
		this.updateBodeGraph();
	}

}
