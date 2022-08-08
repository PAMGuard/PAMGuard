package PamController;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.flipPane.FlipPane;


/**
 * A pane which can flip to show another pane (referred to as the advanced settings pane). 
 * @author Jamie Macaulay
 *
 * @param <T>
 */
public abstract class FlipSettingsPane<T> extends SettingsPane<T> {
	
	/**
	 * The holder pane. 
	 */
	private FlipPane flipPane;
	
	/**
	 * The label at the top of the advanced pane. 
	 */
	private Label advLabel;
	
	/**
	 * The advanced pane. 
	 */
	private PamBorderPane advPane;

	/**
	 * The button which flips back to the front pane. 
	 */
	private PamButton backButton; 

	public FlipSettingsPane(Object ownerWindow) {
		super(ownerWindow);
		
		flipPane = new FlipPane(); 
		flipPane.setFlipTime(200);
		
		this.advPane = createAdvSettingsPane() ; 
		
		flipPane.getBack().getChildren().add(advPane);


	}
	
	@Override
	public Node getContentNode() {
		flipPane.getFront().getChildren().clear();
		flipPane.getFront().getChildren().add(getFrontContentNode());
		return flipPane; 
	}
	
	/**
	 * Flip the pane to show the advanced settings pane. 
	 */
	public void flipToBack() {
		flipPane.flipToBack();
	}
	/**
	 * Flip the pane to show the primary pane. 
	 */
	public void flipToFront() {
		flipPane.flipToBack();
	}
	
	
	/**
	 * Set the contents of the advanced pane. 
	 * @param contentNode - the content to set. 
	 */
	public void setAdvPaneContents(Node contentNode) {
		this.advPane.setCenter(contentNode);
	}
	
	
	/**
	 * Get the content node for the flip pane. 
	 * @return
	 */
	public abstract Pane getFrontContentNode();
	
	
	/**
	 * Create the advanced settings pane which can be accessed by DAQ panes if needed. 
	 */
	private PamBorderPane createAdvSettingsPane() {
		
		backButton = new PamButton(); 
		backButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", Color.WHITE, PamGuiManagerFX.iconSize));
		
		backButton.setOnAction((action)->{
			flipPane.flipToFront(); 
		});
		
		PamBorderPane advPane = new PamBorderPane(); 
		//advPane.setPadding(new Insets(5,5,5,5));
		
		PamHBox buttonHolder = new PamHBox(); 
		
		buttonHolder.setBackground(null);
		//buttonHolder.setStyle("-fx-background-color: red;");
		buttonHolder.setAlignment(Pos.CENTER_LEFT);
		buttonHolder.getChildren().addAll(backButton, advLabel = new Label("Adv. Settings")); 
		advLabel.setAlignment(Pos.CENTER);
		advLabel.setMaxWidth(Double.MAX_VALUE); //need to make sure label is in center. 
		PamGuiManagerFX.titleFont2style(advLabel);
		
		advLabel.setAlignment(Pos.CENTER);
		HBox.setHgrow(advLabel, Priority.ALWAYS);
		
		advPane.setTop(buttonHolder);
		
		return advPane; 
		
	}
	
	public PamBorderPane getAdvPane() {
		return advPane;
	}

	

}
