package pamViewFX.symbol;

import java.util.ArrayList;
import java.util.List;

import PamView.symbol.ManagedSymbolData;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamguardMVC.PamDataBlock;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.picker.SymbolPicker;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Symbol chooser pane which gives user a set of potential user options. This was based on the original 
 * symbol chooser which gave users a set of symbol choices. StandardSymbolOptionPane2 is absed on the new
 * symbol chooser which provides a list of symbol modifiers. 
 * 
 * @author Jamie Macaulay
 *
 */
@Deprecated
public class StandardSymbolOptionPaneFX  extends FXSymbolOptionsPane<StandardSymbolOptions> {

	private RadioButton[] optionButtons;

	/**
	 * The symbol manager associated with the pane
	 */
	private StandardSymbolManager standardSymbolManager;

	/**
	 * The symbol chooser
	 */
	private StandardSymbolChooser standardSymbolChooser;

	/**
	 * The line length 
	 */
	private PamSpinner<Double> lineLength;

	/**
	 * Picks the line colour
	 */
	private ColorPicker lineColorBox;

	/**
	 * Toggle group for radio buttons. 
	 */
	private ToggleGroup group;

	/**
	 * 
	 */
	private CheckBox genericCheckBox;

	/**
	 * Picks the symbol fill. 
	 */
	private ColorPicker symbolFillColourPicker;

	/**
	 * Picks the symbol shape
	 */
	private SymbolPicker symbolPicker;

	/**
	 * Picks the symbol line colour. 
	 */
	private ColorPicker symbolLineColourPicker;

	/**
	 * The main holder pane
	 */
	private PamBorderPane mainPane;

	/**
	 * Line params holder
	 */
	private PamGridPane linBox; 

	/**
	 * Symbol params holder
	 */
	private Pane symbolBox;

	/**
	 * VBox which holds a list of nodes. Handy is adding extra options
	 * to the pane.
	 */
	private PamVBox vBoxHolder;

	private PamDataBlock dataBlock;

	/**
	 * Choice box which allows users to select which annotation type they would like to colour by. 
	 */
	private ChoiceBox annotationChoiceBox; 


	public StandardSymbolOptionPaneFX(StandardSymbolManager standardSymbolManager, StandardSymbolChooser standardSymbolChooser){
		super(null); 
		this.standardSymbolManager = standardSymbolManager;
		this.standardSymbolChooser = standardSymbolChooser;
		dataBlock = standardSymbolManager.getPamDataBlock();

		mainPane=new PamBorderPane(); 
		mainPane.setCenter(vBoxHolder = createPane());
		
		//add a listener to make sure the pane changes properly 
		standardSymbolManager.addSymbolChnageListener((params)->{
			ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
			msd.useGeneric = genericCheckBox.isSelected();
			
			StandardSymbolChooser chooser = getWhichChooser();
			StandardSymbolOptions thisParams = chooser.getSymbolOptions();
			setParams(thisParams); 
			
			super.notifySettingsListeners();
		}); 
	}
	
	
	/**
	 * Pane which holds a list of the options, including the selection check boxes. 
	 * Handy for adding extra controls. 
	 * @return vBox which holds the main symbol option controls. 
	 */
	public PamVBox getVBoxHolder(){
		return vBoxHolder;
	}


	private PamVBox createPane(){

		PamVBox vBox=new PamVBox();
		vBox.setSpacing(10);
		vBox.setPadding(new Insets(5, 0, 0, 5));

		vBox.getChildren().add(genericCheckBox= new CheckBox("Same on all displays"));
		genericCheckBox.setOnAction((action)->{
			
			ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
			msd.useGeneric = genericCheckBox.isSelected();
			
			StandardSymbolChooser chooser = getWhichChooser();
			StandardSymbolOptions params = chooser.getSymbolOptions();
			
//			System.out.println("StandardSymbolChooserPaneFX: " + chooser + 
//					" Fill Colour: " +params.symbolData.getFillColor().toString() + " Line Colour " +params.symbolData.getLineColor().toString() +  " Colour index: "+  
//					params.colourChoice + " symbol " + params.symbolData); 
			setParams(params); 
			
			super.notifySettingsListeners();
		});

		//create title 
		Label detectionsLabel = new Label("Colour s by");
		PamGuiManagerFX.titleFont2style(detectionsLabel);
		//detectionsLabel.setFont(PamGuiManagerFX.titleFontSize2);
		vBox.getChildren().add(detectionsLabel);
		
		ArrayList<DataAnnotationType<?>> annotationTypes = getColouringAnnotationTypes();
		//System.out.println("Annotation Types: " +  annotationTypes.size());


		//create radio buttons to change how clicks are coloured. 		
		optionButtons = new RadioButton[standardSymbolManager.getNColourChoices()];
		group = new ToggleGroup();
		for (int i = 0; i < standardSymbolManager.getNColourChoices(); i++) {
			optionButtons[i] = new RadioButton(standardSymbolManager.colourChoiceName(i));
			optionButtons[i].setToggleGroup(group);
			optionButtons[i].setOnAction((action)->{
				getParams(null);
				super.notifySettingsListeners();
			});
			if (showOption(i)) {
				vBox.getChildren().add(optionButtons[i]); 
			}
			//			optionButtons[i].addActionListener();
		}
		
//		// add an annotation radio button. 
//		if (annotationTypes.size()>0) {
//			optionButtons[ncols-1] = new RadioButton("Annotation");
//			optionButtons[ncols-1].setToggleGroup(group);
//			optionButtons[ncols-1].setOnAction((action)->{
//				getParams(null);
//				super.notifySettingsListeners();
//			});
//			vBox.getChildren().add(optionButtons[ncols-1]); 
//		}
		
		
		//if there is more than one type of annotation then the user has to selected which one they wish to chooser. 
		if (annotationTypes.size()>1) {
			Label annotationLabel = new Label("Annotation");
			PamGuiManagerFX.titleFont2style(annotationLabel);

			
			annotationChoiceBox = new ChoiceBox<DataAnnotationType>(); 
			annotationChoiceBox.getItems().addAll(annotationTypes); 
			vBox.getChildren().add(annotationChoiceBox); 
		}
		
//		System.out.println("Symbol option: HAS_SYMBOL " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)	
//		+ " flag: " + standardSymbolManager.getSymbolChoiceBitMap()); 
//		System.out.println("Symbol option: HAS_SPECIAL_COLOUR " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR)	
//		+ " flag: " + standardSymbolManager.getSymbolChoiceBitMap()); 
//		System.out.println("Symbol option: HAS_CHANNEL_OPTIONS " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_CHANNEL_OPTIONS)	
//		+ " flag: " + standardSymbolManager.getSymbolChoiceBitMap()); 
//
//		//some extra options 
//		optionButtons[StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR));
//		optionButtons[StandardSymbolOptions.COLOUR_SPECIAL].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR));
//		optionButtons[StandardSymbolOptions.COLOUR_HYDROPHONE].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_CHANNEL_OPTIONS));


		//bit of a hack- outside the line length option in case it's used by other subclasses
		linBox = new PamGridPane(); 
		linBox.setHgap(5);
		linBox.setVgap(2);
		vBox.getChildren().add(linBox);
		// if need line length information ....
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH) 
				|| standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE)) {

			Label linelength = new Label("Default Line");
			PamGuiManagerFX.titleFont2style(linelength);
//			linelength.setFont(PamGuiManagerFX.titleFontSize2);
			linBox.getChildren().add(linelength);


			if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE)){
				lineLength= new PamSpinner<Double>(0.0001,  Double.MAX_VALUE,  1000, 1000);
				lineLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			}

			if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH)){
				lineColorBox= new ColorPicker(); 
				lineColorBox.setStyle("-fx-color-label-visible: false ;");
				if (lineLength!=null){
					lineLength.prefHeightProperty().bind(lineColorBox.heightProperty());
				}

				lineColorBox.valueProperty().addListener((obsVal, oldVal, newVal)->{
					getParams(null);
					super.notifySettingsListeners();
				});

			}
			if (linelength != null) {
				linBox.add(new Label("Line Length"), 0, 0);
				linBox.add(lineLength, 0, 1);
			}
		}
		

		//linBox.add(new Label("Color"), 1, 0);
		//linBox.add(lineColorBox, 1, 1);

//		System.out.println("Symbol option: HAS_SYMBOL " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)
//			+ " flag: " + standardSymbolManager.getSymbolChoiceBitMap()); 
		
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
			//Default symbol option 
			Label linelength = new Label("Default Symbol");
			PamGuiManagerFX.titleFont2style(linelength);

			symbolFillColourPicker= new ColorPicker(); 
			symbolFillColourPicker.setStyle("-fx-color-label-visible: false ;");
			symbolFillColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
				if (!setParams){
				symbolPicker.setFillColour(newVal);
				getParams(null);
				super.notifySettingsListeners();
				}
			});

			symbolLineColourPicker= new ColorPicker(); 
			symbolLineColourPicker.setStyle("-fx-color-label-visible: false ;");
			symbolLineColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
				if (!setParams){
				symbolPicker.setLineColour(newVal);
				getParams(null);
				super.notifySettingsListeners();
				}
			});

			symbolPicker= new SymbolPicker(); 
			symbolFillColourPicker.prefHeightProperty().bind(symbolPicker.heightProperty());
			symbolLineColourPicker.prefHeightProperty().bind(symbolPicker.heightProperty());

			symbolPicker.setOnAction((action)->{
				if (!setParams){
					getParams(null);
					super.notifySettingsListeners();
				}
			});
			PamHBox.setHgrow(symbolPicker, Priority.ALWAYS);

			PamGridPane symbolBox = new PamGridPane(); 
			symbolBox.setHgap(5);
			symbolBox.setVgap(2);
			symbolBox.add(new Label("Symbol"), 0, 0);
			symbolBox.add(new Label("Fill/Line"), 1, 0);
			symbolBox.add(new Label("Border"), 2, 0);
			
			symbolBox.add(symbolPicker, 0, 1);
			symbolBox.add(symbolFillColourPicker, 1, 1);
			symbolBox.add(symbolLineColourPicker, 2, 1);
			
			PamVBox symbolBoxHolder = new PamVBox();
			symbolBoxHolder.setSpacing(5);
			symbolBoxHolder.getChildren().add(linelength);
			symbolBoxHolder.getChildren().addAll(symbolBox); 
			
			this.symbolBox=symbolBoxHolder; 

			vBox.getChildren().addAll(symbolBoxHolder);
		}

		setParams(getWhichChooser().getSymbolOptions());

		return vBox; 

	}
	
	
	/**
	 * Get a list of annotation types that have a symbol chooser. 
	 * @return list of annotation types with a symbol chooser 
	 */
	private ArrayList<DataAnnotationType<?>> getColouringAnnotationTypes() {
		ArrayList<DataAnnotationType<?>> annotationList = new ArrayList<DataAnnotationType<?>>();
		AnnotationHandler annotationHandler = dataBlock.getAnnotationHandler();
		dataBlock.getProcessAnnotations().size();
		//System.out.println("Available annotation handler: " + annotationHandler + "  " + dataBlock); 
		if (annotationHandler != null) {
			List<DataAnnotationType<?>> anTypes = annotationHandler.getAvailableAnnotationTypes();
			//System.out.println("Available annotation types: " + anTypes.size()); 
			for (DataAnnotationType<?> anType : anTypes) {
				if (anType.getSymbolModifier(this.standardSymbolChooser) != null) {
					annotationList.add(anType);
				}
			}
		}

		return annotationList;
	}

	private boolean showOption(int optionNumber) {
		//some extra options 
//		optionButtons[StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR));
//		optionButtons[StandardSymbolOptions.COLOUR_SPECIAL].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR));
//		optionButtons[StandardSymbolOptions.COLOUR_HYDROPHONE].setVisible(
//				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_CHANNEL_OPTIONS));
		switch (optionNumber) {
		case StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL:
			return standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR);
		case StandardSymbolOptions.COLOUR_SPECIAL:
			return standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SPECIAL_COLOUR);
		case StandardSymbolOptions.COLOUR_HYDROPHONE:
			return standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_CHANNEL_OPTIONS);
		}
		return true;
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Symbol Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * 
	 */
	boolean setParams = false; 
		

	@Override
	public void setParams(StandardSymbolOptions input) {
		setParams=true; 
		
		if (input.colourChoice>=0 && input.colourChoice<this.optionButtons.length){
			optionButtons[input.colourChoice].setSelected(true);
		}
		else {
			input.colourChoice=optionButtons.length; 
			optionButtons[input.colourChoice].setSelected(true);
		}

		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
//			System.out.println("StandardSymbolChooserFX: Set Params: " + input.symbolData.getLineColor()); 

			symbolFillColourPicker.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
			symbolLineColourPicker.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getLineColor()));; 

			
			symbolPicker.setLineColour(PamUtilsFX.awtToFXColor(input.symbolData.getLineColor()));
			symbolPicker.setFillColour(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
			//29/03/2017- put this after setLineColor and setFillColor as sometimes wrong index was selected 
			//after fills.fills Not sure why but this fixes. 
			symbolPicker.setValue(input.symbolData.symbol);

		}

		/**Lines**/
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH)) {
			try {
			lineLength.getValueFactory().setValue(input.mapLineLength);
			}
			catch (Exception e) {
				System.err.println("Symbol option pane FX: " + e.getMessage());
			}
		}

		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE)) {
			this.lineColorBox.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
		}
		
		setParams=false;
	}


	/**
	 * Select based on whether or not the generic button is presses. 
	 * @return
	 */
	private StandardSymbolChooser getWhichChooser() {
		if (genericCheckBox.isSelected()) {
			PamSymbolChooser chooser = standardSymbolManager.getSymbolChooser(PamSymbolManager.GENERICNAME, standardSymbolChooser.getProjector());
			if (StandardSymbolChooser.class.isAssignableFrom(chooser.getClass())) {
				return (StandardSymbolChooser) chooser;
			}
		}
		return standardSymbolChooser;
	}


	@Override
	public StandardSymbolOptions getParams(StandardSymbolOptions p) {
		

		ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
		msd.useGeneric = genericCheckBox.isSelected();
		StandardSymbolChooser chooser = getWhichChooser();
		StandardSymbolOptions params = chooser.getSymbolOptions();
		
//		System.out.println("StandardSymbolChooserPaneFX: getParams() " +params); 

		params.symbolData=params.symbolData.clone();

		//29/03/2017 had to put this in here as the symbol was the same object across 
		//params sometimes- caused by the setParams triggerring getParams call. 
		//params.symbolData=params.symbolData.clone(); 
		if (setParams) {
			System.out.println("StandardSymbolsOptionsPane: " + params.symbolData);
			return params; 
		}
		
		for (int i = 0; i < optionButtons.length; i++) {
			if (optionButtons[i].isSelected()) {
				params.colourChoice = i;
			}
		}

		/**Symbols**/
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
//			System.out.println("StandardSymbolChooserPaneFX: getParams() settingColours: line" 
//					+ PamUtilsFX.fxToAWTColor(symbolLineColourPicker.getValue()).toString() +" "+ params); 
			params.symbolData.setFillColor(PamUtilsFX.fxToAWTColor(symbolFillColourPicker.getValue()));
			params.symbolData.setLineColor(PamUtilsFX.fxToAWTColor(symbolLineColourPicker.getValue()));
		}
		
		if (params.symbolData!=null && params.symbolData.symbol!=null && symbolPicker!=null && symbolPicker.getValue()!=null){
			params.symbolData.symbol=symbolPicker.getValue().getSymbol();
		}

		/**Lines**/
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH)) {
			params.mapLineLength = lineLength.getValue();
		}
		
		//if generic params have chnaged other displays may need updating. 
		if (msd.useGeneric){
			standardSymbolManager.notifySymbolListeners(chooser); 
		}

//		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE)) {
//			params.lineData.setLineColor(PamUtilsFX.fxToAWTColor(lineColorBox.getValue())); 
//		}
		
//		System.out.println("StandardSymbolsOptionsPane: " + params.symbolData);
		
		return params;
	}


	/**
	 * @return the linBox
	 */
	public PamGridPane getLinBox() {
		return linBox;
	}

	/**
	 * @return the symbolBox
	 */
	public Pane getSymbolBox() {
		return symbolBox;
	}
	
	/**
	 * Disable the fill and line colour boxes (not the symbol chooser) in the symbol box pane. 
	 * @param dsiable - true to disable, false to enable. 
	 */
	public void disableSymbolColourBox(boolean disable) {
		symbolFillColourPicker.setDisable(disable);
		symbolLineColourPicker.setDisable(disable);
	}

	/**
	 * Disable the symbol chooser box
	 * @param dsiable - true to disable, false to enable. 
	 */
	public void disableSymbolChooserBox(boolean disable) {
		symbolPicker.setDisable(disable);
	}

	/**
	 * @param symbolBox the symbolBox to set
	 */
	public void setSymbolBox(Pane symbolBox) {
		this.symbolBox = symbolBox;
	}

	/**
	 * Get the colour picker for the symbol fill
	 * @return the colour picker ofr the symbol fill. 
	 */
	public ColorPicker getFillColorPicker() {
		return this.symbolFillColourPicker;
	}

}
