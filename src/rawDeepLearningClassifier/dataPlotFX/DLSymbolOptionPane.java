package rawDeepLearningClassifier.dataPlotFX;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.comboBox.ColorComboBox;
import pamViewFX.fxNodes.sliders.ColourRangeSlider;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.symbol.StandardSymbolModifierPane;
import rawDeepLearningClassifier.dlClassification.DLClassName;


/*
 * Symbol Options for the annotation pane
 */
public class DLSymbolOptionPane extends StandardSymbolModifierPane {
	
	
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
	 * Combo box which allows a user to select colour
	 */
	private ColorComboBox colourBox;
	
	private boolean initialised=true;

	private DLSymbolModifier dlSymbolModifier; 

	public DLSymbolOptionPane(SymbolModifier symbolModifer) {
		super(symbolModifer, Orientation.HORIZONTAL, true, 0);
		this.setBottom(createProbPane());
		this.dlSymbolModifier = (DLSymbolModifier) symbolModifer; 
		initialised=true; 
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

		colourBox = new ColorComboBox(ColorComboBox.COLOUR_ARRAY_BOX); 
		colourBox.setPrefWidth(50); 
		colourBox.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
			colourRangeSlider.setColourArrayType( dlSymbolModifier.getSymbolModifierParams().colArray);
		});

		showOnlyBinary = new CheckBox("Show only binary classificaiton"); 
		showOnlyBinary.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});

		PamHBox classHolder = new PamHBox(); 
		classHolder.setSpacing(5);
		classHolder.setAlignment(Pos.CENTER_LEFT);
		classHolder.getChildren().addAll(new Label("Show Class"), classNameBox); 
		
		PamHBox colorHolder = new PamHBox(); 
		colorHolder.setSpacing(5);
		colorHolder.setAlignment(Pos.CENTER_LEFT);
		colorHolder.getChildren().addAll(colourRangeSlider, colourBox); 
		
		holder.getChildren().addAll(classHolder, new Label("Probability"), colorHolder); 
		

		holder.getChildren().add(showOnlyBinary); 
		holder.setPadding(new Insets(5,0,5,0));

		setParams = false; 
		
		return holder; 
	}
	
	@Override
	public StandardSymbolOptions getParams(){
		StandardSymbolOptions standardSymbolOptions  = super.getParams(); 
		

		//bit messy but works / 
		DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams(); 
		
		//need to chekc this here. 
		//checkClassNamesBox(symbolOptions); 


		symbolOptions.clims=new double[] {colourRangeSlider.getLowValue(), colourRangeSlider.getHighValue()};
		
		symbolOptions.colArray = ColourArray.getColorArrayType(this.colourBox.getSelectionModel().getSelectedItem()); 
		
		symbolOptions.classIndex = classNameBox.getSelectionModel().getSelectedIndex(); 
		
		symbolOptions.showOnlyBinary = showOnlyBinary.isSelected(); 
		
		dlSymbolModifier.checkColourArray(); 

		//System.out.println("Get params: " ); 

		return standardSymbolOptions; 

	}
	
	private void checkClassNamesBox(DLSymbolModifierParams symbolOptions) {
		
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
		symbolOptions.classIndex = Math.min(symbolOptions.classIndex, nClass-1); 
		classNameBox.getSelectionModel().select(Math.max(symbolOptions.classIndex, 0));
	}

	@Override
	public void setParams() {
		
		if (!initialised) return; 

			super.setParams();

			setParams = true; 

//			StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) getSymbolModifier().getSymbolChooser().getSymbolOptions();
			
			DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams(); 
			//now set frequency parameters 
			colourRangeSlider.setLowValue(symbolOptions.clims[0]);
			colourRangeSlider.setHighValue(symbolOptions.clims[1]);
			colourRangeSlider.setColourArrayType( symbolOptions.colArray);

			
			//set the combo box class types. 
			checkClassNamesBox( symbolOptions); 
			
			
			//color box.
			colourBox.setValue(symbolOptions.colArray);
			
			//set the selected. 
			showOnlyBinary.setSelected(symbolOptions.showOnlyBinary);

			setParams = false; 
	} 

	
	

}
