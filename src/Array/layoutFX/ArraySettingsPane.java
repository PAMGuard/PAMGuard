package Array.layoutFX;

import Array.PamArray;
import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.PamHBox;

/**
 * The main settings pane for settings up a hydrophone array. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ArraySettingsPane extends SettingsPane<PamArray >{
	
	private BasicArrayPane basicArrayPane; 
	
	private PamFlipPane mainPane;
	
	private HydrophonesPane hydrophonePane; 

	public ArraySettingsPane() {
		super(null);
		mainPane = new PamFlipPane();
		
		mainPane.getFront().getChildren().add(createArrayPane());
		
		mainPane.getAdvPane().setCenter(new Label("Advanced Settings"));
	}

	/**
	 * Create the main pane. 
	 * @return the main array pane. 
	 */
	private Pane createArrayPane() {
		
		Label arrayLabel = new Label("Array"); 
		PamGuiManagerFX.titleFont1style(arrayLabel);
		
		basicArrayPane = new BasicArrayPane(); 
		
		Label hydrophoneLabel = new Label("Hydrophone"); 
		PamGuiManagerFX.titleFont1style(hydrophoneLabel);
		
		hydrophonePane = new HydrophonesPane(); 
			
		PamButton advancedButton = new PamButton(); 
		advancedButton.setOnAction((action)->{
			mainPane.flipToBack();
		});
		advancedButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog")); 

		PamHBox advancedPane = new PamHBox(); 
		advancedPane.setSpacing(5);
		advancedPane.setAlignment(Pos.CENTER_RIGHT);
		advancedPane.getChildren().addAll(new Label("Advanced"), advancedButton);
		
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);
		vBox.getChildren().addAll(arrayLabel, basicArrayPane, hydrophoneLabel,
				hydrophonePane, advancedPane); 

		return vBox; 
	}
	
	
	@Override
	public PamArray  getParams(PamArray  currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(PamArray  input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Array Parameters";
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
