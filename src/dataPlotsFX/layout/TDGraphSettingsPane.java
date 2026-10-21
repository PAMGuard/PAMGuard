package dataPlotsFX.layout;

import dataPlotsFX.TDGraphParametersFX;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * 
 * Pane with control to change generic settings of a TDGraphFX. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TDGraphSettingsPane extends DynamicSettingsPane<TDGraphParametersFX> {

	/**
	 * The main pane. 
	 */
	private Pane mainPane;
	
	/**
	 * Control to select background colour of the graph 
	 */
	private ColorPicker backColPicker;
	
	/*
	 * Control to select colour of wrap line
	 */
	private ColorPicker wrapColPicker;
	
	/**
	 * Combo box to select the type of popup menu. 
	 */
	private ComboBox<String> popUpBox;

	/**
	 * Check box to show or hide the symbol legend.
	 */
	private CheckBox showLegendBox;

	/**
	 * Combo box to select the corner the legend is shown in.
	 */
	private ComboBox<String> legendPosBox;

	/**
	 * Pane which shwos which data blocks have been added to the graph.
	 */
	private PamVBox dataBlockLabelPane;

	/**
	 * Reference to the TDGraphFX
	 */
	private TDGraphFX tdGraph;

	/**
	 * The add menu button.
	 */
	private MenuButton addMenuButton;

	/**
	 * The remove menu button. 
	 */
	private MenuButton removeMenuButton;

	public TDGraphSettingsPane(TDGraphFX tdGraph, Object ownerWindow) {
		super(ownerWindow);
		this.tdGraph=tdGraph;
		mainPane = createPane();
		mainPane.getStylesheets().addAll(tdGraph.getTDDisplay().getCSSSettingsResource());
		mainPane.getStyleClass().add("pane");
	}
	
	/**
	 * The TDGrpah pane. 
	 * @return
	 */
	private Pane createPane(){
		
		PamBorderPane pane = new PamBorderPane();
		
		Separator seperator = new Separator(Orientation.VERTICAL);
				seperator.setPadding(new Insets(0,5,0,5));
		
		pane.setLeft(createGeneralSettingsPane());
		//pane.setCenter(seperator);
		//pane.setRight(createDataBlockPane());

		pane.setPadding(new Insets(10,10,10,10));
	
		return pane;
		
	}
	
	/**
	 * Create general settings pane. 
	 */
	private Pane createGeneralSettingsPane() {
		
		
		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		
		Label topLabel=new Label("General Graph Settings");
		PamGuiManagerFX.titleFont2style(topLabel);
//		topLabel.setFont(PamGuiManagerFX.titleFontSize2);
		holder.getChildren().add(topLabel);
		
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		
		backColPicker = new ColorPicker(); 
		backColPicker.setStyle("-fx-color-label-visible: false ;");
		backColPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
			notifySettingsListeners(); 
		});
		gridPane.add(new Label("Background Color"), 0, 0);
		gridPane.add(backColPicker, 1, 0);
		
		//wrapColPicker = new ColorPicker(); 
		//wrapColPicker.setStyle("-fx-color-label-visible: false ;");
		//gridPane.add(new Label("Wrap Line Color"), 0, 1);
		//gridPane.add(wrapColPicker, 1, 1);
		
		holder.getChildren().add(gridPane);
		
		popUpBox = new ComboBox<String>();
		popUpBox.getItems().add("Simple Pop Up Menu");
		popUpBox.getItems().add("Adv. Pop Up Menu");
		popUpBox.setOnAction((action)->{
			notifySettingsListeners(); 
		});
		
		holder.getChildren().add(new Label("Pop Up Menu Type"));
		holder.getChildren().add(popUpBox);

		Label legendLabel = new Label("Legend");
		PamGuiManagerFX.titleFont2style(legendLabel);
		VBox.setMargin(legendLabel, new Insets(5, 0, 0, 0));
		holder.getChildren().add(legendLabel);

		showLegendBox = new CheckBox("Show legend");
		showLegendBox.setTooltip(new Tooltip("Show a legend of the symbols used to draw data on the graph"));
		showLegendBox.setOnAction((action)->{
			legendPosBox.setDisable(!showLegendBox.isSelected());
			notifySettingsListeners();
		});
		holder.getChildren().add(showLegendBox);

		legendPosBox = new ComboBox<String>();
		// order matches the TDGraphParametersFX.LEGEND_ position flags.
		legendPosBox.getItems().addAll("Top left", "Top right", "Bottom left", "Bottom right");
		legendPosBox.setOnAction((action)->{
			notifySettingsListeners();
		});
		PamGridPane legendGrid = new PamGridPane();
		legendGrid.setHgap(5);
		legendGrid.add(new Label("Position"), 0, 0);
		legendGrid.add(legendPosBox, 1, 0);
		holder.getChildren().add(legendGrid);

		return holder;
	}
	
	
	/**
	 * Create pane which shows user which data blocks are present and allows data blocks to be 
	 * added or removed. 
	 * @return pane with datablock labels and add/remove controls. 
	 */
	private Pane createDataBlockPane() {
		
		//pane to show data blocks 
		dataBlockLabelPane = new PamVBox(); 
		dataBlockLabelPane.setSpacing(5);
		dataBlockLabelPane.setPadding(new Insets(5,5,5,5));
	
		//pane to add and remove data blocks
		PamVBox addRemovePane = new PamVBox();
		addRemovePane.setPadding(new Insets(0,0,0,5));
		
		addMenuButton = new MenuButton("Add"); 
		addMenuButton.setPrefHeight(PamGuiManagerFX.iconSize);
//		addMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, Color.WHITE, PamGuiManagerFX.iconSize));
		addMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		addMenuButton.showingProperty().addListener((obsVal, oldVal, newVal)->{
			addMenuButton.getItems().clear();
			TDControlPaneFX.createAddMenuItems(addMenuButton.getItems(), this.tdGraph);
		});
		TDControlPaneFX.createAddMenuItems(addMenuButton.getItems(), this.tdGraph);
		
		removeMenuButton = new MenuButton("Remove");
		removeMenuButton.setPrefHeight(PamGuiManagerFX.iconSize);
//		removeMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.REMOVE, Color.WHITE, PamGuiManagerFX.iconSize));
		removeMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-minus", Color.WHITE, PamGuiManagerFX.iconSize));
		removeMenuButton.showingProperty().addListener((obsVal, oldVal, newVal)->{
			removeMenuButton.getItems().clear();
			TDControlPaneFX.createRemoveMenuItems(removeMenuButton.getItems(), this.tdGraph);
		});
		TDControlPaneFX.createRemoveMenuItems(removeMenuButton.getItems(), this.tdGraph);
		
		addRemovePane.getChildren().addAll(addMenuButton, removeMenuButton);
		
		//label for pane
		Label topLabel=new Label("Add/Remove Datablocks");
//		topLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(topLabel);

		
		//bringing it all together
		PamBorderPane holder = new PamBorderPane();
		holder.setTop(topLabel);
		holder.setCenter(dataBlockLabelPane);
		holder.setRight(addRemovePane);
		
		return holder; 
	}
	
//	/**
//	 * Set the data block labels 
//	 */
//	private void setDataBlockLabels() {
//		dataBlockLabelPane.getChildren().clear();
//		
//		for (int i=0; i<this.tdGraph.getDataList().size(); i++) {
//			dataBlockLabelPane.getChildren().add(
//				new Label(this.tdGraph.getDataList().get(i).getDataBlock().getDataName()));
//		}
//	}; 
	

	@Override
	public TDGraphParametersFX getParams(TDGraphParametersFX currParams) {
		//a colour picked here is the user's own, so stop it following the colour
		//scheme - until the next time the scheme is changed, which wins again.
		if (!backColPicker.getValue().equals(currParams.plotFill)) {
			currParams.plotFillFollowsScheme = false;
		}
		currParams.plotFill=backColPicker.getValue();
		currParams.popUpMenuType=this.popUpBox.getSelectionModel().getSelectedIndex();
		currParams.showLegend = showLegendBox.isSelected();
		if (legendPosBox.getSelectionModel().getSelectedIndex() >= 0) {
			currParams.legendPosition = legendPosBox.getSelectionModel().getSelectedIndex();
		}
		return currParams;
	}

	@Override
	public void setParams(TDGraphParametersFX input) {
		//setDataBlockLabels();
		
//		addMenuButton.getItems().clear();
//		removeMenuButton.getItems().clear();
//		
//		TDControlPaneFX.createAddMenuItems(addMenuButton.getItems(), this.tdGraph);
//		TDControlPaneFX.createRemoveMenuItems(removeMenuButton.getItems(), this.tdGraph);
		
		this.backColPicker.setValue(input.plotFill);
		if (input.popUpMenuType<0 || input.popUpMenuType>=popUpBox.getItems().size()) {
			input.popUpMenuType=0;
		}
		this.popUpBox.getSelectionModel().select(input.popUpMenuType);

		showLegendBox.setSelected(input.showLegend);
		if (input.legendPosition<0 || input.legendPosition>=legendPosBox.getItems().size()) {
			input.legendPosition=TDGraphParametersFX.LEGEND_BOTTOM_LEFT;
		}
		legendPosBox.getSelectionModel().select(input.legendPosition);
		legendPosBox.setDisable(!input.showLegend);
	}

	@Override
	public String getName() {
		return "TDGraph Params";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
	}

}
