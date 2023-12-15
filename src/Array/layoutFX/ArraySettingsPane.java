package Array.layoutFX;

import Array.PamArray;
import PamController.PamController;
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
	
	private StreamersPane streamerPane; 
	
	private Pane mainPane;
	
	private HydrophonesPane hydrophonePane;

//	private Pane holder;

	private Label hydrophoneLabel; 

	public ArraySettingsPane() {
		super(null);
		mainPane = createArrayPane();
//		mainPane.setStyle("-fx-background-color: red;");
		mainPane.setMaxWidth(Double.MAX_VALUE);
		mainPane.setPrefWidth(800);
		mainPane.setStyle("-fx-padding: 0,0,0,0");

//		mainPane.setMinWidth(800);

//		mainPane.setCenter(createArrayPane());
//		
//		mainPane.getAdvPane().setCenter(new Label("Advanced Settings"));
		
		
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
		
		
//		holder = new StackPane(); 
//		holder.getChildren().add(mainPane);
//		holder.setStyle("-fx-padding: 0,0,0,0");

	}

	/**
	 * Create the main pane. 
	 * @return the main array pane. 
	 */
	private Pane createArrayPane() {
		
		Label arrayLabel = new Label("Array"); 
		arrayLabel.setPadding(new Insets(5,5,5,5));
		PamGuiManagerFX.titleFont1style(arrayLabel);
		
		streamerPane = new StreamersPane(); 
		streamerPane.setMaxWidth(Double.MAX_VALUE);
		
		hydrophoneLabel = new Label("Hydrophones"); 
		PamGuiManagerFX.titleFont1style(hydrophoneLabel);
		hydrophoneLabel.setPadding(new Insets(5,5,5,5));

		
		hydrophonePane = new HydrophonesPane(); 
		hydrophonePane.setMaxWidth(Double.MAX_VALUE);

//		PamButton advancedButton = new PamButton(); 
//		advancedButton.setOnAction((action)->{
//			mainPane.flipToBack();
//		});
//		advancedButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog")); 

//		PamHBox advancedPane = new PamHBox(); 
//		advancedPane.setSpacing(5);
//		advancedPane.setAlignment(Pos.CENTER_RIGHT);
//		advancedPane.getChildren().addAll(new Label("Advanced"), advancedButton);
		
		PamVBox vBox = new PamVBox(); 

		vBox.setSpacing(5);
		vBox.getChildren().addAll(arrayLabel, streamerPane, hydrophoneLabel,
				hydrophonePane); 
		



		return vBox; 
	}
	
	/**
	 * Set correct text for the receiver in the current medium (e.g. air or water); 
	 */
	private void setReceieverLabels() {
		hydrophonePane.setRecieverLabels();
		streamerPane.setRecieverLabels();
		
		hydrophoneLabel.setText(PamController.getInstance().getGlobalMediumManager().getRecieverString(true) + "s");

//		if (singleInstance!=null) {
//			singleInstance.setTitle("Pamguard "+ PamController.getInstance().getGlobalMediumManager().getRecieverString(false) +" array");
//		}
	}
	
	
	@Override
	public PamArray  getParams(PamArray  currParams) {
		currParams = hydrophonePane.getParams(currParams); 
		currParams = streamerPane.getParams(currParams); 
		
		System.out.println("Array settings pane: No. streamers: " + currParams.getStreamerCount());
		return currParams;
	}

	@Override
	public void setParams(PamArray  input) {
		System.out.println("Hydrophone array is: "+ input); 
		setReceieverLabels();
		hydrophonePane.setParams(input); 
		streamerPane.setParams(input); 
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
