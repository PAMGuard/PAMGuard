package Array.layoutFX;

import Array.PamArray;
import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;


import pamViewFX.PamGuiManagerFX;

import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.PamBorderPane;

import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.flipPane.FlipPane;

import pamViewFX.fxNodes.PamHBox;

/**
 * The main settings pane for settings up a hydrophone array. 
 * 
 * @author Jamie Macaulay
 * @param <FlipPane>
 *
 */
public class ArraySettingsPane extends SettingsPane<PamArray >{
	
	private StreamerPane basicArrayPane; 
	
	private PamFlipPane mainPane;
	
	private HydrophonesPane hydrophonePane;

	private Pane holder; 

	public ArraySettingsPane() {
		super(null);
		mainPane = new PamFlipPane();
		
		mainPane.setFrontContent(createArrayPane());
		
		mainPane.getAdvPane().setCenter(new Label("Advanced Settings"));
		
		
//		//mainPane.getFront().setStyle("-fx-background-color: grey;");
//		mainPane.setStyle("-fx-background-color: red;");
//		
//		FlipPane aflipPane = new FlipPane(); 
//		aflipPane.setStyle("-fx-background-color: red;");
//		
//		PamHBox stackPane = new PamHBox(); 
//		stackPane.setStyle("-fx-background-color: red;");
//		
//		Button button = new Button(); 
//		button.setOnAction((action)->{
//			System.out.println(" 1 " + stackPane.getPadding());
//			System.out.println(" 2 " +PamBorderPane.getMargin(stackPane));
//			System.out.println(" 3 " + holder.getPadding());
//		});
//		
//		stackPane.getChildren().add(button);
//
//		
//		mainPane.setPadding(new Insets(0,0,0,0));
		
		
		holder = new StackPane(); 
		holder.getChildren().add(mainPane);
		holder.setStyle("-fx-padding: 0,0,0,0");

	}

	/**
	 * Create the main pane. 
	 * @return the main array pane. 
	 */
	private Pane createArrayPane() {
		
		Label arrayLabel = new Label("Array"); 
		arrayLabel.setPadding(new Insets(5,5,5,5));
		PamGuiManagerFX.titleFont1style(arrayLabel);
		
		basicArrayPane = new StreamerPane(); 
		
		Label hydrophoneLabel = new Label("Hydrophone"); 
		PamGuiManagerFX.titleFont1style(hydrophoneLabel);
		hydrophoneLabel.setPadding(new Insets(5,5,5,5));

		
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
		hydrophonePane.setParams(input); 
		
	}

	@Override
	public String getName() {
		return "Array Parameters";
	}

	@Override
	public Node getContentNode() {

		return holder;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
