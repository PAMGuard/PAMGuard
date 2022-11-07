package pamViewFX.symbol;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.ToggleSwitch;

import PamUtils.PamArrayUtils;
import PamView.symbol.ManagedSymbolData;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.UserSymbolModifier;
import PamguardMVC.PamDataBlock;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTitledPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.orderedList.PamDraggableList;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxSettingsPanes.SettingsListener;

/**
 * A symbol options pane which allows users to select a list of symbol modifiers. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardSymbolOptionsPane extends FXSymbolOptionsPane<StandardSymbolOptions> {

	private static final int SYMBOL_ORDER_CHANGE = 0x1;

	private static final int SYMBOL_ENABLED = 0x2;

	private static final int SYMBOL_MODIFIER_CHANGE = 0x3;

	private static final int DEFUALT_SYMBOL_CHANGE = 0x4;

	/**
	 * The standard symbol chooser associated with the pane. 
	 */
	private StandardSymbolChooser standardSymbolChooser;

	/**
	 * The symbol manager.
	 */
	private StandardSymbolManager standardSymbolManager;

	/**
	 * The VBox holde.r 
	 */
	private PamVBox vBoxHolder;

	/**
	 * The data block associated with the symbol chooser. 
	 */
	private PamDataBlock dataBlock;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;

	/**
	 * Check box which allows users to select whether symbols are generic across PAMGuar.d 
	 */
	private CheckBox genericCheckBox;


	private ArrayList<DragSymbolModiferPane> symbolListPanes;


	private SymbolDraggableList symbolDraggListPane;


	private UserSymbolModifier fixedSymbolModifier;


	private StandardSymbolModifierPane defaultSymbolPane;

	private boolean setParams; 

	public StandardSymbolOptionsPane(StandardSymbolManager standardSymbolManager, StandardSymbolChooser standardSymbolChooser) {
		super(null);

		this.standardSymbolManager = standardSymbolManager;
		this.standardSymbolChooser = standardSymbolChooser;
		dataBlock = standardSymbolManager.getPamDataBlock();

		mainPane=new PamBorderPane(); 
		mainPane.setTop(vBoxHolder = createPane());
		mainPane.setPadding(new Insets(5, 0, 0, 5));

		//add a listener to make sure the pane changes properly if the symbol is changed somewhere else in
		//PAMGuard
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
	 * Create a list of symbol modifier panes. 
	 * @param standardSymbolChooser - the symbol chooser. 
	 * @return a list of symbol panes. 
	 */
	private ArrayList<DragSymbolModiferPane> createSymbolPanes(ArrayList<SymbolModifier>  symbolModifiers) {
		ArrayList<DragSymbolModiferPane>  symbolModifierPanes = new 	 ArrayList<DragSymbolModiferPane>(); 

		//TODO
		DragSymbolModiferPane dragPane; 
		for (SymbolModifier symbolModifer: symbolModifiers) {
			dragPane = new DragSymbolModiferPane(symbolModifer); 
			symbolModifierPanes.add(dragPane); 

			dragPane.getSymbolModifierPane().addSettingsListener(()->{
//				System.out.println("StandardSymbolOptionsPane: Notify change from symbol options pane: "); 
				newSettings(SYMBOL_MODIFIER_CHANGE);
			});
		}

		return symbolModifierPanes; 
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

	/**
	 * Create the main pane. This contains a list of symbol options which can be enabled or disabled and dragged to
	 * different locations. 
	 * @return a list of panes. 
	 */
	private PamVBox createPane() {



		PamVBox vBox=new PamVBox();
		vBox.setSpacing(10);

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

		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){

			fixedSymbolModifier = new UserSymbolModifier(standardSymbolChooser); 
			//		fixedSymbolModifier.setSelectionBit(SymbolModType.FILLCOLOUR, true);
			//		fixedSymbolModifier.setSelectionBit(SymbolModType.LINECOLOUR, true);
			//		fixedSymbolModifier.setSelectionBit(SymbolModType.SHAPE, true);

			//create title 
			Label symbolLabel = new Label("Default Symbol");
			PamGuiManagerFX.titleFont2style(symbolLabel);
			
			defaultSymbolPane = new StandardSymbolModifierPane(fixedSymbolModifier, Orientation.HORIZONTAL, 
					false, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR | SymbolModType.SHAPE); 
			defaultSymbolPane.addSettingsListener(()->{
//				System.out.println("StandardSymbolOptionsPane: Notify change from symbol options pane: "); 
				newSettings(DEFUALT_SYMBOL_CHANGE);
			});
			
			
			vBox.getChildren().addAll(symbolLabel, defaultSymbolPane);

		}

		//create title 
		Label detectionsLabel = new Label("Modify Symbol");
		PamGuiManagerFX.titleFont2style(detectionsLabel);
		//detectionsLabel.setFont(PamGuiManagerFX.titleFontSize2);

		//create the panel for symbol chooser - the list remains the same - it is only whether the 
		//symbol modifier settings, whether they are enabled and the order of the list that can be changed
		//by the pane. 
		ArrayList<SymbolModifier>  symbolModifiers = standardSymbolChooser.getSymbolModifiers(); 
		this.symbolListPanes = createSymbolPanes(symbolModifiers); 
		this.symbolDraggListPane = new SymbolDraggableList(symbolListPanes); 

		vBox.getChildren().addAll(detectionsLabel, symbolDraggListPane); 
		
		setParams(standardSymbolChooser.getSymbolOptions()); 

		return vBox; 
	}


	/**
	 * Get the modifier order of the symbol panes. 
	 * @return the modifier order of symbol panes. 
	 */
	private int[] getModifierOrder() {

		//if the panes list, the symbol modifiers etc are not complementary arrays/lists then this 
		//is going to go horribly wrong. Not a bad things because that indicates a major error somewhere. 

		List<DragSymbolModiferPane> sortedPanelist = symbolDraggListPane.getSortedList(); 

		int[] modifierorder = new int[getWhichChooser().getSymbolModifiers().size()]; 
		for (int i=0; i<modifierorder.length; i++ ) {
			modifierorder[i] = getWhichChooser().getSymbolModifiers().indexOf(sortedPanelist.get(i).getSymbolModifier()); 
		}
		
//		System.out.println("Modifier Order: " + standardSymbolManager); 
//		PamArrayUtils.printArray(modifierorder); 


		return modifierorder;
	}




	@Override
	public StandardSymbolOptions getParams(StandardSymbolOptions currParams) {

		if (setParams) return null; 
		

		//System.out.println("StandardSymbolOptionsPane getParams()");

		ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
		msd.useGeneric = genericCheckBox.isSelected();
		StandardSymbolChooser chooser = getWhichChooser();
		StandardSymbolOptions params = chooser.getSymbolOptions();

		//get the parameters for the symbol modifier. 
		params.setModifierOrder(getModifierOrder());

		List<DragSymbolModiferPane> sortedPanelist = symbolDraggListPane.getSortedList(); 
		for (DragSymbolModiferPane symbolmodifierPane: sortedPanelist) {
			//this should modify the symbol modifier reference...
			symbolmodifierPane.getParams(); 
			params.setEnabled(symbolmodifierPane.toggle.isSelected(),chooser.getSymbolModifiers().indexOf(symbolmodifierPane.getSymbolModifier())); 
		}

		/**Symbols**/
		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
			defaultSymbolPane.getSymbolData(params.symbolData); 
			//System.out.println("StandardSymbolOptionsPane: params.symbolData: " + params.symbolData.getSymbol());
		}

		return params;
	}

	@Override
	public void setParams(StandardSymbolOptions input) {
		setParams=true; 
		
		//System.out.println
		defaultSymbolPane.setSymbolData(input.symbolData); 


		//set the modifier order	
		List<DragSymbolModiferPane> sortedPanelist = symbolDraggListPane.getSortedList(); 
		for (DragSymbolModiferPane symbolmodifierPane: sortedPanelist) {
			//this should modify the symbol modifier reference...
			symbolmodifierPane.setParams(); 
			
			int index =  getWhichChooser().getSymbolModifiers().indexOf(symbolmodifierPane.getSymbolModifier());
				
			if (index>=0) symbolmodifierPane.toggle.setSelected(input.isEnabled(getWhichChooser())[index]);

		}
		
		//		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_SYMBOL)){
		////			System.out.println("StandardSymbolChooserFX: Set Params: " + input.symbolData.getLineColor()); 
		//
		//			symbolFillColourPicker.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
		//			symbolLineColourPicker.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getLineColor()));; 
		//
		//			
		//			symbolPicker.setLineColour(PamUtilsFX.awtToFXColor(input.symbolData.getLineColor()));
		//			symbolPicker.setFillColour(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
		//			//29/03/2017- put this after setLineColor and setFillColor as sometimes wrong index was selected 
		//			//after fills.fills Not sure why but this fixes. 
		//			symbolPicker.setValue(input.symbolData.symbol);
		//
		//		}
		//
		//		/**Lines**/
		//		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH)) {
		//			try {
		//			lineLength.getValueFactory().setValue(input.mapLineLength);
		//			}
		//			catch (Exception e) {
		//				System.err.println("Symbol option pane FX: " + e.getMessage());
		//			}
		//		}
		//
		//		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE)) {
		//			this.lineColorBox.setValue(PamUtilsFX.awtToFXColor(input.symbolData.getFillColor()));
		//		}

		setParams=false;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Symbol Options";
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	/**
	 * Called whenever settings are updated. 
	 * @param type - the settings update type. 
	 */
	public void newSettings(int type){
		this.getParams(null); 
		this.notifySettingsListeners();
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}


	class SymbolDraggableList extends PamDraggableList<DragSymbolModiferPane>  {


		public SymbolDraggableList(List<DragSymbolModiferPane> panes) {
			super(panes, false, true); 
			// TODO Auto-generated constructor stub
		}

		@Override
		public void paneOrderChanged(boolean success) {
			newSettings(SYMBOL_ORDER_CHANGE); 
		}

	}

	/**
	 * Wrapper for the symbol modifier pane to allow it to be used in a titled pane in a draggable list. 
	 * @author Jamie Macaulay 
	 *
	 */
	class DragSymbolModiferPane extends PamBorderPane {

		/**
		 * Reference to the symbol modifier. 
		 */
		private SymbolModifier symbolmodifier;

		/*
		 * The sytmbol modifier pane. 
		 */
		private SymbolModifierPane symbolModifierPane;

		/**
		 * The toggle switch for enabling the symbol modifier. 
		 */
		private ToggleSwitch toggle;


		public DragSymbolModiferPane(SymbolModifier symbolModifer) {
			// a symbol modifier without an associated settings pane. 
			this.symbolmodifier = symbolModifer; 


			PamHBox holder = new PamHBox(); 
			holder.setSpacing(5);
			holder.setAlignment(Pos.CENTER_LEFT);


			PamTitledPane titledPane = new PamTitledPane(); 
			titledPane.setText(symbolmodifier.getName());
			titledPane.setTextOverrun(OverrunStyle.ELLIPSIS);
			
			//titledPane.setMaxWidth(300);
			
			//there is a bug in the titled pane whereby it sets the minimum width based on 
			//the text sixe. This prevents ttext wrap etc. form working. 
			titledPane.prefWidthProperty().bind(holder.widthProperty());
			titledPane.setMinWidth(10);
			
			titledPane.setWrapText(true);
			titledPane.setTooltip(new Tooltip(symbolmodifier.getName()));
			
			if (symbolModifer.getOptionsPane()!=null) {
				titledPane.setContent(symbolModifierPane = symbolmodifier.getOptionsPane());
			}
			else {
				titledPane.setContent(symbolModifierPane = new StandardSymbolModifierPane(symbolModifer));
			}
			
			titledPane.setAlignment(Pos.CENTER);
			PamHBox.setHgrow(titledPane, Priority.ALWAYS);
			titledPane.setExpanded(false);
			titledPane.setStyle("-fx-border-radius: 0 0 0 0;");
			titledPane.setStyle("-fx-background-color: transparent;"); 

			toggle = new ToggleSwitch(); 
			toggle.setSelected(true);
			toggle.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				titledPane.setDisable(!toggle.isSelected());
				newSettings(SYMBOL_ENABLED); 
			});
			
			//FX bug - must be set like this. 
			toggle.setMaxWidth(20); 

			holder.getChildren().addAll(toggle, titledPane); 

			this.setCenter(holder);
		}

		public void setParams() {
			symbolModifierPane.setParams();

		}

		public SymbolModifierPane getSymbolModifierPane() {
			return symbolModifierPane;
		}

		public void getParams() {
			symbolModifierPane.getParams();
		}


		public SymbolModifier getSymbolModifier() {
			return symbolmodifier;
		}

	}

	@Override

	public void addSettingsListener(SettingsListener settingsListener){
		super.addSettingsListener(settingsListener);
		//		for (DragSymbolModiferPane symbolModifierPane:  symbolListPanes) {
		//			symbolModifierPane.getSymbolModifierPane().addSettingsListener(settingsListener);
		//		}
	} 


	/**
	 * VBox holder. 
	 * @return the vBox holder. 
	 */
	public VBox getVBoxHolder() {
		return vBoxHolder;
	}


	public BorderPane getMainPane() {
		return mainPane;
	}

}
