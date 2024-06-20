package pamViewFX;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import PamController.SettingsPane;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * Simple pane to select a tab and create a new tab if neeeded. 
 * @author Jamie Macaulay
 *
 */
public class TabSelectionPane extends SettingsPane<PamGuiTabFX> {

	/**
	 * Reference to the PmaGuiFX stage
	 */
	private PamGuiFX pamGuiFX;
	
	/*
	 *Shows the available tabs 
	 */
	private ComboBox<String> tabChoice;
	
	/**
	 * Add a tab
	 */
	private PamButton addButton;
	
	private PamBorderPane mainPane = new PamBorderPane(); 	

	/**
	 * Create a tab selection pane
	 * @param pamTabPane
	 */
	public TabSelectionPane(PamGuiFX pamTabPane){
		super(null);
		this.pamGuiFX=pamTabPane;
		mainPane.setCenter(createTabSelectionPane());
		mainPane.setPrefWidth(400);
	}
	
	
	private Node createTabSelectionPane(){
		
		PamHBox holderBox=new PamHBox(); 
		
		holderBox.setSpacing(5); 
		tabChoice=new ComboBox<String>(); 
		tabChoice.setMinWidth(100);
		PamHBox.setHgrow(tabChoice, Priority.ALWAYS); //make sure choice nox is big enough
		tabChoice.setEditable(false);

		//listener for adding tabs
		addButton=new PamButton(); 
//		addButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.PLUS, Color.WHITE, PamGuiManagerFX.iconSize)); 
		addButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize)); 
		addButton.setOnAction((action)->{
			pamGuiFX.addPamTab(new TabInfo("Display " +(pamGuiFX.getTabs().size()+1)), null, true); 
			populateChoiceBox();
			//select the tab which has just been added. 
			tabChoice.getSelectionModel().selectLast();
		});
		addButton.prefHeightProperty().bind(tabChoice.heightProperty());
		
		//listener for users changing tab names. When a tab is renamed in the dialog box 
		//it is renamed in the main displau 
		tabChoice.getEditor().textProperty().addListener(( observable, oldValue,  newValue) -> {
			pamGuiFX.renameTab(tabChoice.getSelectionModel().getSelectedItem(), 
					tabChoice.getSelectionModel().getSelectedIndex()); 
//			int i = tabChoice.getSelectionModel().getSelectedIndex(); 
//			pamGuiFX.getPamTab(i).setText(tabChoice.getItems().get(i));
		});
		
	
		holderBox.getChildren().addAll(new Label("Select Tab"), tabChoice, addButton);
		holderBox.setAlignment(Pos.CENTER);
		
		PamVBox vboxholder=new PamVBox(); 
		vboxholder.setSpacing(5);
		Label title=new Label("Select Tab");
		PamGuiManagerFX.titleFont2style(title);
//		title.setFont(PamGuiManagerFX.titleFontSize2);
		vboxholder.getChildren().addAll(title, holderBox);
		
		populateChoiceBox();
		tabChoice.getSelectionModel().selectLast();


		return  new PamBorderPane(vboxholder); 
	}
	
	private void populateChoiceBox(){
		tabChoice.getItems().clear();
		for (int i=0; i<pamGuiFX.getTabs().size(); i++){
			tabChoice.getItems().add(pamGuiFX.getTabs().get(i).getLabel().getText()); 
		}
	}

	@Override
	public PamGuiTabFX getParams(PamGuiTabFX g) {
		
		for (int i=0; i<pamGuiFX.getTabs().size(); i++){
			if (tabChoice.getSelectionModel().getSelectedItem() == pamGuiFX.getTabs().get(i).getLabel().getText()){
				return pamGuiFX.getTabs().get(i);
			}
		}
		return null;
	}

	@Override
	public void setParams(PamGuiTabFX input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Tab Selection";
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
