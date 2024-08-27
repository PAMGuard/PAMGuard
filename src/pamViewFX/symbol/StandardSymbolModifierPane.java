package pamViewFX.symbol;

import java.util.ArrayList;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.picker.SymbolPicker;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;


/**
 * A symbol modifier pane that allows users to select a colour for symbol, symbol line and symbol shape.
 * 
 * @author Jamie Macaulay. 
 *
 */
public class StandardSymbolModifierPane extends SymbolModifierPane {


	/**
	 * List of standardised controls for the symbol chooser. 
	 */
	ArrayList<SymbolChooserControls> symbolChooserControls = new ArrayList<SymbolChooserControls>(); 

	/**
	 * Indicates that params are being set and notifiers should not be called. 
	 */
	protected boolean setParams = false; 

	/**
	 * The symbol box holder pane. 
	 */
	private PamVBox symbolBox;


	/**
	 * True if the check boxes are used. 
	 */
	private boolean useCheckBox;

	/**
	 * Check whether check boxes have been shown. If not then it is assumed all mod options
	 * have been set to true. 
	 * @return true of user selectable check boxes are shown 
	 */
	public boolean isUseCheckBox() {
		return useCheckBox;
	}


	/**
	 * Create a default set of check boxes that allow the user to select how they wish to alter t
	 * 
	 * @param symbolModifer - the symbol modifier. 
	 */
	public StandardSymbolModifierPane(SymbolModifier symbolModifer) {
		this(symbolModifer, Orientation.VERTICAL, true, 0); 

	}

	/**
	 * Create a standard symbol modifier pane. This shows the modifiable options
	 * such as fill, line, symbol and can also show user selectable options for each
	 * mod e.g. a colour selector for fill.
	 * 
	 * @param symbolModifer - the symbol modifier
	 * @param orientation   - the orientation - VERTICAL or HORIZONTAL layout.
	 * @param checkBox      - true to show check boxes for each selectable option.
	 * @param selectable    - a bitmap of mod options in which to allow the user to
	 *                      manually select the colour/symbol etc. Set to zero for
	 *                      only check boxes.
	 */
	public StandardSymbolModifierPane(SymbolModifier symbolModifer, Orientation orientation, boolean checkBox, int selectable ) {
		super(symbolModifer);
		this.useCheckBox = checkBox; 
		this.setCenter(createSymbolPane(symbolModifer, orientation, checkBox, selectable));
		setParams(); 
	}

	public StandardSymbolOptions getParams(){
		
		StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) getSymbolModifier().getSymbolChooser().getSymbolOptions();

		//bit of a hack but set the fill and line colour in the symbol picker to refelect the current chosen colours...
		//this is because get params is called anytime there is a change. 

		SymbolModifierParams params = this.getSymbolModifier().getSymbolModifierParams(); 
		//standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());

		int mod = 0;

		//figure out which bit maps are being used. 
		for (int i=0; i<symbolChooserControls.size(); i++) {
			if (symbolChooserControls.get(i).selectModBox.isSelected()){
				mod = mod | symbolChooserControls.get(i).getModFlag(); 
			}
		}

		params.modBitMap = mod; 

		//System.out.println("StandardSymbolModifierPane : getParams(): new mod: " +mod); 

		return standardSymbolOptions; 

	}

	public void setParams( ){
		//TODO
		
//		StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) getSymbolModifier().getSymbolChooser().getSymbolOptions();
//		SymbolModifierParams params = standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());
		SymbolModifierParams params = this.getSymbolModifier().getSymbolModifierParams(); 

		setParams = true; 

		//figure out which bit maps are being used. 
		for (int i=0; i<symbolChooserControls.size(); i++) {
				//set the check box to selected. 
				symbolChooserControls.get(i).selectModBox.setSelected((params.modBitMap & symbolChooserControls.get(i).modFlag)!=0);
		}
	
		setParams = false; 
		//set the paramters to reflect the symbol options. 
	}

	private void controlsChanged() {
		//TODO
		//		symbolPicker.setFillColour(newVal);
		//		symbolPicker.setFillColour(newVal);
	}


	/**
	 * Create the pane for the symbol manager. 
	 * @param orientaiton - the orientation of the pane. 
	 * @param checkBox - the selectable check box. 
	 * @param selectable - a bitmap of the symbol types that can be user selectable (0 for not user selectabnle)
	 * @return the pane. 
	 */
	@SuppressWarnings("unchecked")
	public Pane createSymbolPane(SymbolModifier symbolmodifier, Orientation orientaiton, boolean checkBox, int userSelectable) {


		if (getSymbolModifier().canModify(SymbolModType.SHAPE)){
			SymbolChooserPane symbolChooserPane = new SymbolChooserPane(SymbolModType.SHAPE, "Symbol"); 
			symbolChooserControls.add(symbolChooserPane); 
		}

		if (getSymbolModifier().canModify(SymbolModType.LINECOLOUR)){
			ColorChooserPane symbolChooserPane = new ColorChooserPane(SymbolModType.LINECOLOUR, "Border"); 
			symbolChooserControls.add(symbolChooserPane); 
		}

		if (getSymbolModifier().canModify(SymbolModType.FILLCOLOUR)){
			ColorChooserPane symbolChooserPane = new ColorChooserPane(SymbolModType.FILLCOLOUR, "Fill"); 
			symbolChooserControls.add(symbolChooserPane);
		}


		//now add everything to panes. 
		PamGridPane symbolBox = new PamGridPane(); 
		symbolBox.setHgap(5);
		symbolBox.setVgap(5);

		int ind1 = 0; 
		int ind2 = 0; 

		if (checkBox) {
			if (orientaiton == Orientation.VERTICAL) {
				ind2=0; 
				for (SymbolChooserControls symbolChooserControl: symbolChooserControls) {
					symbolBox.add(symbolChooserControl.getSelectModBox()  , ind1, ind2);
					ind2++; 
				}
			}
			else {
				ind2=0; 
				for (SymbolChooserControls symbolChooserControl: symbolChooserControls) {
					symbolChooserControl.getSelectModBox().setText(symbolChooserControl.getName());
					symbolBox.add(symbolChooserControl.getSelectModBox()  , ind2, ind1);
					ind2++; 
				}
			}
			ind1++; 
		}

		//now add specifc control panes to manually change symbol colours
		ind2=0; 

		if (orientaiton == Orientation.VERTICAL) {
			for (SymbolChooserControls symbolChooserControl: symbolChooserControls) {
				symbolBox.add( new Label(symbolChooserControl.getName()), ind1, ind2);

				if ((userSelectable & symbolChooserControl.modFlag) != 0) {
					symbolBox.add(symbolChooserControl.getSettingsPane(), ind1+1, 0);
				}
				ind2++; 
			}
		}
		else {
			for (SymbolChooserControls symbolChooserControl: symbolChooserControls) {
				if (!checkBox) symbolBox.add( new Label(symbolChooserControl.getName()), ind2, ind1);

				if ((userSelectable & symbolChooserControl.modFlag) != 0) {
					symbolBox.add(symbolChooserControl.getSettingsPane(), ind2, ind1+1);
				}
				ind2++; 
			}
		}

		//add settings listeners to all the value properties. 
		for (int i=0; i<symbolChooserControls.size(); i++) {
			//listener for the symbol property chooser
			if (symbolChooserControls.get(i).getValueProperty()!=null) {
				symbolChooserControls.get(i).getValueProperty().addListener((obsVal, oldVal, newVal)->{
					if (!setParams){
						getParams();
						controlsChanged(); 
						notifySettingsListeners();
					}
				});
			}
			//listener for the check box
			symbolChooserControls.get(i).getSelectModBox().selectedProperty().addListener((obsVal, oldVal, newVal)->{
				//System.out.println("Notify change: " + setParams + " No. listeners: "  + this.settingsListeners.size()); 
				if (!setParams){
					getParams();
					notifySettingsListeners();
				}
			});
		}
		
		
		PamVBox symbolBoxHolder = new PamVBox();
		symbolBoxHolder.setSpacing(5);
		symbolBoxHolder.getChildren().addAll(symbolBox); 

		this.symbolBox=symbolBoxHolder; 
		
	

		return symbolBoxHolder; 
	}

	//	/**
	//	 * Create the pane for the symbol manager. 
	//	 * @param orientaiton - the orientation of the pane. 
	//	 * @param checkBox - the selectable check box. 
	//	 * @param selectable - are the symbols and colours users selectable. 
	//	 * @return the pane. 
	//	 */
	//	public Pane createSymbolPane(SymbolModifier symbolmodifier, Orientation orientaiton, boolean checkBox, boolean[] selectable) {
	//
	//		//if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
	//		//Default symbol option
	//
	//		//		Label linelength = new Label("Default Symbol");
	//		//		PamGuiManagerFX.titleFont2style(linelength);
	//
	//		symbolFillColourPicker= new ColorPicker(); 
	//		symbolFillColourPicker.setStyle("-fx-color-label-visible: false ;");
	//		symbolFillColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
	//			if (!setParams){
	//				symbolPicker.setFillColour(newVal);
	//				getParams();
	//				super.notifySettingsListeners();
	//			}
	//		});
	//
	//		symbolLineColourPicker= new ColorPicker(); 
	//		symbolLineColourPicker.setStyle("-fx-color-label-visible: false ;");
	//		symbolLineColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
	//			if (!setParams){
	//				symbolPicker.setLineColour(newVal);
	//				getParams();
	//				super.notifySettingsListeners();
	//			}
	//		});
	//
	//		symbolPicker= new SymbolPicker(); 
	//		symbolFillColourPicker.prefHeightProperty().bind(symbolPicker.heightProperty());
	//		symbolLineColourPicker.prefHeightProperty().bind(symbolPicker.heightProperty());
	//
	//		symbolPicker.setOnAction((action)->{
	//			if (!setParams){
	//				getParams();
	//				super.notifySettingsListeners();
	//			}
	//		});
	//		PamHBox.setHgrow(symbolPicker, Priority.ALWAYS);
	//
	//		PamGridPane symbolBox = new PamGridPane(); 
	//		symbolBox.setHgap(5);
	//		symbolBox.setVgap(5);
	//
	//
	//		int ind = 0; 
	//		if (checkBox) {
	//			symbolpickerCheckBox  = new CheckBox("");
	//			symbolpickerCheckBox.setOnAction((action)->{
	//				this.symbolPicker.setDisable(symbolpickerCheckBox.isSelected());
	//			});
	//
	//			symbolFillCheckBox  = new CheckBox("");
	//			symbolFillCheckBox.setOnAction((action)->{
	//				this.symbolFillColourPicker.setDisable(symbolFillCheckBox.isSelected());
	//			});
	//
	//			symbolLineCheckBox  = new CheckBox("");
	//			symbolLineCheckBox.setOnAction((action)->{
	//				this.symbolLineColourPicker.setDisable(symbolLineCheckBox.isSelected());
	//			});
	//
	//			if (orientaiton == Orientation.VERTICAL) {
	//				if (getSymbolModifier().canModify(SymbolModType.SHAPE)) symbolBox.add(symbolpickerCheckBox  , ind, 0);
	//				if (getSymbolModifier().canModify(SymbolModType.FILLCOLOUR))  symbolBox.add(symbolFillCheckBox, ind, 1);
	//				if (getSymbolModifier().canModify(SymbolModType.LINECOLOUR))  symbolBox.add(symbolLineCheckBox, ind, 2);
	//			}
	//			else {
	//				symbolpickerCheckBox.setText("Symbol");
	//				symbolFillCheckBox.setText("Fill");
	//				symbolLineCheckBox.setText("Border");
	//
	//				if (getSymbolModifier().canModify(SymbolModType.SHAPE)) symbolBox.add(symbolpickerCheckBox, 0, ind);
	//				if (getSymbolModifier().canModify(SymbolModType.FILLCOLOUR)) symbolBox.add(symbolFillCheckBox, 1, ind);
	//				if (getSymbolModifier().canModify(SymbolModType.LINECOLOUR)) symbolBox.add(symbolLineCheckBox, 2, ind);
	//			}
	//			ind++; 
	//		}
	//
	//		if (orientaiton == Orientation.VERTICAL) {
	//			if (getSymbolModifier().canModify(SymbolModType.SHAPE))  symbolBox.add( new Label("Symbol"), ind, 0);
	//			if (getSymbolModifier().canModify(SymbolModType.FILLCOLOUR))  symbolBox.add( new Label("Fill/Line"), ind, 1);
	//			if (getSymbolModifier().canModify(SymbolModType.LINECOLOUR))  symbolBox.add(new Label("Border"), ind, 2);
	//
	//			ind++; 
	//
	//			if (selectable[0] && getSymbolModifier().canModify(SymbolModType.SHAPE))  symbolBox.add(symbolPicker, ind, 0);
	//			if (selectable[1] && getSymbolModifier().canModify(SymbolModType.FILLCOLOUR))  symbolBox.add(symbolFillColourPicker, ind, 1);
	//			if (selectable[2] && getSymbolModifier().canModify(SymbolModType.LINECOLOUR))  symbolBox.add(symbolLineColourPicker, ind, 2);
	//
	//		}
	//		else {
	//			//Horizontal 
	//			if (!checkBox) {
	//				if (getSymbolModifier().canModify(SymbolModType.SHAPE))  symbolBox.add(new Label("Symbol"), 0, ind);
	//				if (getSymbolModifier().canModify(SymbolModType.FILLCOLOUR))  symbolBox.add(new Label("Fill/Line"), 1, ind);
	//				if (getSymbolModifier().canModify(SymbolModType.LINECOLOUR))  symbolBox.add(new Label("Border"), 2, ind);
	//				ind++;
	//			}
	//
	//			if (selectable[0] && getSymbolModifier().canModify(SymbolModType.SHAPE))  symbolBox.add(symbolPicker, 0, ind);
	//			if (selectable[1] && getSymbolModifier().canModify(SymbolModType.FILLCOLOUR))  symbolBox.add(symbolFillColourPicker, 1, ind);
	//			if (selectable[2]  && getSymbolModifier().canModify(SymbolModType.LINECOLOUR) )  symbolBox.add(symbolLineColourPicker, 2, ind);			
	//		}
	//
	//
	//
	//		PamVBox symbolBoxHolder = new PamVBox();
	//		symbolBoxHolder.setSpacing(5);
	//		symbolBoxHolder.getChildren().addAll(symbolBox); 
	//
	//		this.symbolBox=symbolBoxHolder; 
	//
	//		return symbolBoxHolder; 
	//	}


	/**
	 * The symbol chooser pane which allows users to select a particular property 
	 * @author Jamie Macaulay
	 *
	 */
	public class SymbolChooserControls {


		private String name;

		private int modFlag;

		private CheckBox selectModBox; 

		private Region settingsPane =  null; 

		public SymbolChooserControls(int modFlag, String name) {
			this.selectModBox = new CheckBox(""); //by default do not include the name in the check box 
			selectModBox.setSelected(true);
			this.name =name; 
			this.modFlag = modFlag; 
		}

		/**
		 * The object property for extra settings. Can be used to notify if 
		 * additional symbol settings have changed. Can be null. 
		 * @return the object property.
		 */
		public ObjectProperty getValueProperty() {
			return null;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getModFlag() {
			return modFlag;
		}

		public void setModFlag(int modFlag) {
			this.modFlag = modFlag;
		}

		public CheckBox getSelectModBox() {
			return selectModBox;
		}

		public void setSelectModBox(CheckBox selectModBox) {
			this.selectModBox = selectModBox;
		}

		public Region getSettingsPane() {
			return settingsPane;
		}

		public void setSettingsPane(Region settingsPane) {
			this.settingsPane = settingsPane;
		}

		public void getSymbolData(SymbolData symbolData) {
			//do nothing by default
			
		}
		
		public void setSymbolData(SymbolData symbolData) {
			//do nothing by default
		}
	}

	
	/**
	 * Called whenever a colour chooser changes it colour
	 */
	private void colourChanged(SymbolData symbolData) {
		for (SymbolChooserControls symbolChooserControl: symbolChooserControls) {
			if ((symbolChooserControl.modFlag & SymbolModType.SHAPE) !=0) {
				//bit hackey but needed to set colours...
				((SymbolChooserPane) symbolChooserControl).setColours(symbolData);
			}
			
		}
	}


	/**
	 * The colour chooser pane allows users to select a colour for a symbol. 
	 * @author Jamie Macaulay
	 *
	 */
	public class ColorChooserPane extends SymbolChooserControls {


		private PamColorPicker symbolFillColourPicker; 


		public ColorChooserPane(int modFlag, String name) {
			super(modFlag, name);

			symbolFillColourPicker= new PamColorPicker(); 
			symbolFillColourPicker.setStyle("-fx-color-label-visible: false ;");
//			symbolFillColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
//				//send a notification to the pane that a colour has changed
//			});

			this.setSettingsPane(symbolFillColourPicker); 
		}

		/**
		 * Set the default opacity for the colour picker. When a new colour is selected,
		 * then the opacity will default to the set value.
		 * 
		 * @param opacity - the default opacity to set. Default is 1.
		 */
		public void setDefaultOpacity(double opacity) {
			symbolFillColourPicker.setDefaultOpacity(opacity);
		}
		
		
		/**
		 * Get the default opacity for the colour picker. When a new colour is selected,
		 * then the opacity will default to the set value.
		 * 
		 * @return opacity - the default opacity of the colour picker.
		 */
		public double getDefaultOpacity() {
			return symbolFillColourPicker.getDefaultOpacity();
		}


		public ColorPicker getSymbolColourPicker() {
			return symbolFillColourPicker;
		}

		public void setSymbolColourPicker(PamColorPicker symbolFillColourPicker) {
			this.symbolFillColourPicker = symbolFillColourPicker;
		}

		@Override
		public ObjectProperty<Color> getValueProperty() {
			return symbolFillColourPicker.valueProperty();
		}
		
		public void getSymbolData(SymbolData symbolData) {
			if ((getModFlag() & SymbolModType.FILLCOLOUR) != 0) {
				symbolData.setFillColor(PamUtilsFX.fxToAWTColor(symbolFillColourPicker.getValue()));
			}
			
			if ((getModFlag() & SymbolModType.LINECOLOUR) != 0) {
				symbolData.setLineColor(PamUtilsFX.fxToAWTColor(symbolFillColourPicker.getValue()));
			}
			colourChanged(symbolData); 
		}
		
		public void setSymbolData(SymbolData symbolData) {
			if ((getModFlag() & SymbolModType.FILLCOLOUR) != 0) {
				symbolFillColourPicker.setValue(PamUtilsFX.awtToFXColor(symbolData.getFillColor()));
			}
			
			if ((getModFlag() & SymbolModType.LINECOLOUR) != 0) {
				symbolFillColourPicker.setValue(PamUtilsFX.awtToFXColor(symbolData.getLineColor()));
			}
		}

	}

	/**
	 * The symbol chooser pane which allows users to select a symbol type. 
	 * @author Jamie Macaulay
	 *
	 */
	public class SymbolChooserPane extends SymbolChooserControls {

		private SymbolPicker symbolPicker;

		public SymbolChooserPane(int modFlag, String name) {
			super(modFlag, name);

			symbolPicker= new SymbolPicker(); 

			this.setSettingsPane(symbolPicker); 
		}

		public SymbolPicker getSymbolPicker() {
			return symbolPicker;
		}

		@Override
		public ObjectProperty<PamSymbolFX> getValueProperty() {
			return symbolPicker.valueProperty();
		}
		
		boolean disableGetParams = false; 
		
		public void getSymbolData(SymbolData symbolData) {
			if (disableGetParams) return; 
			//System.out.println("get Symbol Data: " + symbolPicker.getValue()); 
			if (symbolPicker.getValue()==null) return; 
			symbolData.setSymbol(symbolPicker.getValue().getSymbol()); 
			
//			disableGetParams = true; 
////			//bit of a HACK but works to keep up to date.
//			symbolPicker.setFillColour(PamUtilsFX.awtToFXColor(symbolData.getFillColor()));
//			symbolPicker.setLineColour(PamUtilsFX.awtToFXColor(symbolData.getLineColor()));
//			disableGetParams =false;

		}
		
		public void setSymbolData(SymbolData symbolData) {
			symbolPicker.setValue(symbolData.getSymbol());
			
//			//bit of a HACK but works to keep up to date. 
			setColours(symbolData); 
		}
		
		public void setColours(SymbolData symbolData){
			//got to be careful here. Setting the colour will trigger an object property change which 
			//triggers the dynamic setting listeners. Need to disable the listeners first using setParams = true; 
			setParams = true; 
			symbolPicker.setFillColour(PamUtilsFX.awtToFXColor(symbolData.getFillColor()));
			symbolPicker.setLineColour(PamUtilsFX.awtToFXColor(symbolData.getLineColor()));
			setParams =false;
		}

	}

	/**
	 * Set the symbol data based on the current panes. 
	 * @param symbolData - the symbol data to set. 
	 */
	public void getSymbolData(SymbolData symbolData) {
		
		if (symbolData==null) return; 

		//figure out which bit maps are being used. 
		for (int i=0; i<symbolChooserControls.size(); i++) {
			if (symbolChooserControls.get(i).selectModBox.isSelected() || !useCheckBox){
				symbolChooserControls.get(i).getSymbolData(symbolData); 
			}
		}
	}

	/**
	 * Set the symbol data based on the current panes. 
	 * @param symbolData - the symbol data to set. 
	 */
	public void setSymbolData(SymbolData symbolData) {
		
		if (symbolData==null) return; 
		//figure out which bit maps are being used. 
		for (int i=0; i<symbolChooserControls.size(); i++) {
			symbolChooserControls.get(i).setSymbolData(symbolData); 
		}
	}
	
	/**
	 * Get the main holder pane. 
	 * @return the pane. 
	 */
	public PamVBox getSymbolBox() {
		return symbolBox;
	}
	
	/**
	 * Set the default opacity for selected colours from the colour picker. The
	 * default opacity is the the opacity when a user selects a new colour. The user
	 * can alter this with advanced colour controls.
	 * 
	 * @param opacity - the default opacity for fill colours.
	 */
	public void setDefaultFillOpacity(double opacity) {

		//set the opacity ofr all fill colour controls
		for (int i=0; i<symbolChooserControls.size(); i++) {
			if ((symbolChooserControls.get(i).getModFlag()  & SymbolModType.FILLCOLOUR) != 0){
				((ColorChooserPane) symbolChooserControls.get(i)).setDefaultOpacity(opacity); 
			}
		}

	}

}
