package rawDeepLearningClassifier.dataPlotFX;

import org.controlsfx.control.SegmentedButton;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModifier;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.comboBox.ColorComboBox;
import pamViewFX.fxNodes.sliders.ColourRangeSlider;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.symbol.StandardSymbolModifierPane;
import rawDeepLearningClassifier.dlClassification.DLClassName;


/*
 * Symbol Options for the annotation pane
 */
public class DLSymbolOptionPane extends StandardSymbolModifierPane {
	
	
	private static final double CLASS_NAME_BOX_WIDTH = 130;

	/**
	 * The colour range slider for colouring probabilities. 
	 */
	private ColourRangeSlider colourRangeSlider;
	
	/**
	 * The combo box allowing users to select which class to show.  
	 */
	private ComboBox<String> classNameBox;

	/**
	 * Check box allowing users only to show only those detecitons which have passed binary classification. 
	 */
	private CheckBox showOnlyBinary;

	
	/**
	 * Color picker which allows a user to select the gradient for colouring predictions
	 */
	private ColorComboBox colourBox;
	
	/**
	 * Color picker which allows a user to select colour for each class. 
	 */
	private ColorPicker colourPicker;
	
	private boolean initialised=true;

	private DLSymbolModifier dlSymbolModifier;
	
	private ComboBox<String> classNameBox2;

	/**
	 * Pane which holds controls for changing the colour based on prediciton value
	 */
	private Pane probPane;

	/**
	 * Pane which holds controls for changing the colour based on the highest prediction value
	 */
	private Pane classPane;

	
	private PamBorderPane holder;

	/**
	 * Button to select how to colour. 
	 */
	private ToggleButton b1, b2; 

	public DLSymbolOptionPane(SymbolModifier symbolModifer) {
		super(symbolModifer, Orientation.HORIZONTAL, true, 0);
		
		probPane = 	createProbPane();
		classPane = 	createClassPane();
		classPane.setPadding(new Insets(0,0,5,0));

		b1 = new ToggleButton("Prediction");
		b1.setPrefWidth(80);
		b1.setStyle("-fx-border-radius: 5 0 0 5; -fx-background-radius: 5 0 0 5;");
		
		b2 = new ToggleButton("Class");
		b2.setPrefWidth(80);
		
		b1.setOnAction((a)->{
			setSettingsPane();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});
		b2.setStyle("-fx-border-radius: 0 5 5 0;  -fx-background-radius: 0 5 5 0;");
		
		
		b2.setOnAction((a)->{
			setSettingsPane();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});
		
		SegmentedButton segmentedButton = new SegmentedButton();    
		segmentedButton.getButtons().addAll(b1, b2);
		segmentedButton.setPadding(new Insets(5,0,5,0));
		
		BorderPane.setAlignment(segmentedButton, Pos.CENTER);
		b1.setSelected(true);
		
		showOnlyBinary = new CheckBox("Show only binary classificaiton"); 
		showOnlyBinary.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});
		showOnlyBinary.setTooltip(new Tooltip("Colour detections only if they passed decision threshold"));

		holder = new PamBorderPane(); 

		holder.setTop(segmentedButton);
		holder.setCenter(probPane);
		holder.setBottom(showOnlyBinary);

		this.setBottom(holder);
		
		this.dlSymbolModifier = (DLSymbolModifier) symbolModifer; 
		initialised=true; 
	}
	
	private void setSettingsPane() {
		if (b1.isSelected()) {
			holder.setCenter(probPane);
		}
		if (b2.isSelected()) {
			holder.setCenter(classPane);
		}
	}
	
	
	private Pane createClassPane() {
		
		classNameBox2 = new ComboBox<String>(); 
		classNameBox2.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});
		classNameBox2.setMaxWidth(Double.MAX_VALUE);
		
		classNameBox2.setOnAction((action)->{                
			colourPicker.setValue(PamUtilsFX.intToColor(dlSymbolModifier.getSymbolModifierParams().classColors[classNameBox2.getSelectionModel().getSelectedIndex()]));
		});
		
		classNameBox2.setPrefWidth(CLASS_NAME_BOX_WIDTH);

		colourPicker = new ColorPicker();
		colourPicker.setPrefWidth(60);
		colourPicker.setOnAction((action)->{                
			getParams();
			notifySettingsListeners();
		});
		
		HBox.setHgrow(classNameBox, Priority.ALWAYS);

		PamHBox classHolder = new PamHBox(); 
		classHolder.setSpacing(5);
		classHolder.setAlignment(Pos.CENTER_LEFT);
		classHolder.getChildren().addAll(classNameBox2, colourPicker); 
		
		return classHolder; 

	}


	private Pane createProbPane() {
				
		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		
		colourRangeSlider = new ColourRangeSlider(); 
		colourRangeSlider.setMin(0);
		colourRangeSlider.setMax(1);//always between 0 and 1 because it's a probability. 
		colourRangeSlider.setMajorTickUnit(0.25);
		colourRangeSlider.setShowTickMarks(true);
		colourRangeSlider.highValueProperty().addListener((obsVal, oldVal, newVal)->{
			getParams();
			notifySettingsListeners();
		});
		colourRangeSlider.lowValueProperty().addListener((obsVal, oldVal, newVal)->{
			getParams();
			notifySettingsListeners();
		});
		
		classNameBox = new ComboBox<String>(); 
		classNameBox.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});
		classNameBox.setPrefWidth(CLASS_NAME_BOX_WIDTH);


		colourBox = new ColorComboBox(ColorComboBox.COLOUR_ARRAY_BOX); 
		colourBox.setPrefWidth(50); 
		colourBox.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
			colourRangeSlider.setColourArrayType( dlSymbolModifier.getSymbolModifierParams().colArray);
		});

		PamHBox classHolder = new PamHBox(); 
		classHolder.setSpacing(5);
		classHolder.setAlignment(Pos.CENTER_LEFT);
		classHolder.getChildren().addAll(new Label("Show class"), classNameBox); 
		
		PamHBox colorHolder = new PamHBox(); 
		colorHolder.setSpacing(5);
		colorHolder.setAlignment(Pos.CENTER_LEFT);
		colorHolder.getChildren().addAll(colourRangeSlider, colourBox); 
		
		holder.getChildren().addAll(classHolder, new Label("Probability"), colorHolder); 
		

//		holder.getChildren().add(showOnlyBinary); 
		holder.setPadding(new Insets(5,0,5,0));

		setParams = false; 
		
		return holder; 
	}
	
	
	/**
	 * get parameters for colouring by class. 
	 * @param symbolOptions - the symbol options. 
	 * @return
	 */
	public DLSymbolModifierParams getClassColParams(DLSymbolModifierParams symbolOptions ) {

		int index = classNameBox2.getSelectionModel().getSelectedIndex()>=0 ? classNameBox2.getSelectionModel().getSelectedIndex():0;
		
		symbolOptions.classColors[index] = PamUtilsFX.colorToInt(colourPicker.getValue());
		
		symbolOptions.classIndex2 = classNameBox2.getSelectionModel().getSelectedIndex(); 
		
		return symbolOptions; 
	}
	
	/**
	 * 
	 * @param symbolOptions
	 * @return
	 */
	public DLSymbolModifierParams getPredictionColParams(DLSymbolModifierParams symbolOptions ) {
		
		symbolOptions.clims=new double[] {colourRangeSlider.getLowValue(), colourRangeSlider.getHighValue()};
		
		symbolOptions.colArray = ColourArray.getColorArrayType(this.colourBox.getSelectionModel().getSelectedItem()); 
		
		symbolOptions.classIndex = classNameBox.getSelectionModel().getSelectedIndex(); 
		
		return symbolOptions; 
	}
	
	
	@Override
	public StandardSymbolOptions getParams(){
		StandardSymbolOptions standardSymbolOptions  = super.getParams(); 
		
		//bit messy but works
		DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams(); 
		
		//need to check this here. 
		//checkClassNamesBox(symbolOptions); 
		
		if (b1.isSelected()) symbolOptions.colTypeSelection = DLSymbolModifierParams.PREDICITON_COL;
		if (b2.isSelected()) symbolOptions.colTypeSelection = DLSymbolModifierParams.CLASS_COL;
		
		//get parameters for colouring 
		symbolOptions = getClassColParams(symbolOptions);

		//get parameters for colouring by prediction value
		symbolOptions = getPredictionColParams(symbolOptions) ;
		
		symbolOptions.showOnlyBinary = showOnlyBinary.isSelected(); 
		
		dlSymbolModifier.checkColourArray(); 

		return standardSymbolOptions; 
	}
	
	
	private int checkClassNamesBox(DLSymbolModifierParams symbolOptions, ComboBox<String> classNameBox) {
		
		DLClassName[] classNames = dlSymbolModifier.getDLAnnotType().getDlControl().getDLModel().getClassNames(); 
		
//		for (int i =0; i<classNames.length; i++) {
//			System.out.println("DLSymbolOptionsPane: classNames: " + i + "  " + classNames[i].className); 
//		}

		int nClass = dlSymbolModifier.getDLAnnotType().getDlControl().getDLModel().getNumClasses(); 

		classNameBox.getItems().clear();
		for (int i=0; i<nClass; i++) {
			if (classNames!=null && classNames.length>i) {
				classNameBox.getItems().add(classNames[i].className); 
			}
			else {
				classNameBox.getItems().add("Class: " +  i);
			}
		}
		
		return nClass;
	
	}
	
	/**
	 * Set parameters for controls to change the colour gradient based on prediction. 
	 * @param symbolOptions - the symbol options
	 */
	private void setPredictionColParams(DLSymbolModifierParams symbolOptions) {
				
		//now set frequency parameters 
		colourRangeSlider.setLowValue(symbolOptions.clims[0]);
		colourRangeSlider.setHighValue(symbolOptions.clims[1]);
		colourRangeSlider.setColourArrayType( symbolOptions.colArray);

		
		int nClass = checkClassNamesBox( symbolOptions, classNameBox); 
		symbolOptions.classIndex = Math.min(symbolOptions.classIndex, nClass-1); 
		classNameBox.getSelectionModel().select(Math.max(symbolOptions.classIndex, 0));
		
		//color box.
		colourBox.setValue(symbolOptions.colArray);
	}
	
	/**
	 * Set parameters for controls to change the colour gradient based on prediction. 
	 * @param symbolOptions - the symbol options
	 */
	private void setClassColParams(DLSymbolModifierParams symbolOptions) {
		
		int nClass = checkClassNamesBox( symbolOptions, classNameBox2); 
		
		symbolOptions.classIndex = Math.min(symbolOptions.classIndex, nClass-1); 
		classNameBox2.getSelectionModel().select(Math.max(symbolOptions.classIndex2, 0));
		
		
		int index = symbolOptions.classIndex2>=0? symbolOptions.classIndex2 : 0;
		
		if (symbolOptions.classColors==null) {
			symbolOptions.setDefaultClassColors(nClass);
		}
		
		//set the correct colour
		colourPicker.setValue(PamUtilsFX.intToColor(symbolOptions.classColors[index]));
	}

	@Override
	public void setParams() {
		
		if (!initialised) return; 

			super.setParams();

			setParams = true; 

			//get the symbool options
			DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams(); 
			
//			b1.setSelected(false);
//			b2.setSelected(false);
			if (symbolOptions.colTypeSelection == DLSymbolModifierParams.PREDICITON_COL) b1.setSelected(true);
			if (symbolOptions.colTypeSelection == DLSymbolModifierParams.PREDICITON_COL) b2.setSelected(true);
			
			setSettingsPane();
			
			symbolOptions.colTypeSelection = b1.isSelected() ? DLSymbolModifierParams.PREDICITON_COL : DLSymbolModifierParams.CLASS_COL;
			
			//set the parameters for colouring by prediction
			setPredictionColParams(symbolOptions);
			
			//set the class colour parameters
			setClassColParams(symbolOptions);
			
			//set the selected. 
			showOnlyBinary.setSelected(symbolOptions.showOnlyBinary);
			

			setParams = false; 
	} 

	
	

}
