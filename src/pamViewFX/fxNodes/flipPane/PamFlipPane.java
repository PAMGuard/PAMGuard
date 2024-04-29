package pamViewFX.fxNodes.flipPane;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.utilsFX.TextUtilsFX;

/**
 * Flip pane which has is supposed to be used for advanced settings. The front
 * of the pane is to be used for standard settings and the pane can then flip to
 * the back to show advanced settings. A back button is included in the back
 * pane which can be used to flip the pane back to the standard settings. The
 * front pane needs a control that calls flipPane.toBack() to show the advanced
 * pane.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamFlipPane extends FlipPane {

	public static final double FLIP_TIME =250; //milliseconds - the default is 700ms whihc is way too long. 

	/**
	 * The back is pressed- equivelent to an OK.
	 */
	public static final int OK_BACK_BUTTON = 0;
	
	/**
	 * The back is pressed- equivelent to an OK.
	 */
	public static final int NORESPONE_BACK_BUTTON = -1;

	/**
	 * The back button is cancelled
	 */
	public static final int CANCEL_BACK_BUTTON = 1;

	/**
	 * Advanced pane.
	 */
	private PamBorderPane advPane;

	private PamBorderPane frontPane;


	private PamButton backButton;

	/**
	 * Text field in the title of the advanced pane. This can be used to change settings. 
	 */
	private TextField advLabel;

	/**
	 * Label which sits before the text field in the advanced settings pane title
	 */
	private Label preLabel;

	/**
	 * Label after the the text field in the advanced pane label - this can be set to say "settings" for example with the text field
	 * then editable to change the name of a parameter. 
	 */
	private Label postLabel;

	/**
	 * Property for the response to the back button. The response can be OK_BACK_BUTTON or CANCEL_BACK_BUTTON. 
	 */
	private SimpleIntegerProperty backButtonResponse = new SimpleIntegerProperty();

	
	public PamFlipPane() {
		this(Orientation.HORIZONTAL);
	}


	public PamFlipPane(Orientation FLIP_DIRECTION) {
		super(FLIP_DIRECTION);
		this.advPane = createAdvSettingsPane();
		this.getFront().getChildren().add(frontPane = new PamBorderPane()); 
		this.getBack().getChildren().add(advPane); 
		this.setFlipTime(FLIP_TIME);
		
		this.flipFrontProperty().addListener((obsVal, oldval, newVal)->{
			if (!newVal.booleanValue()) {
				//need to set the back button so that listeners are triggered whenever the back 
				//button is pressed. 
				this.backButtonResponse.set(NORESPONE_BACK_BUTTON);
			}
		});
	}

	/**
	 * Get the front pane. 
	 * @return the front pane. 
	 */
	public PamBorderPane getFrontPane() {
		return frontPane;
	}

	/**
	 * Get the back pane. 
	 * @return the back pane. 
	 */
	public PamBorderPane getBackPane() {
		return advPane;
	}

	/**
	 * Convenience duplicate of getBackPane(). 
	 * @return the back pane. 
	 */
	public PamBorderPane getAdvPane() {
		return advPane;
	}


	/**
	 * Set the advanced pane content. 
	 * @param - the content to set. 
	 */
	public void setAdvPaneContent(Node content) {
		advPane.setCenter(content);
	}

	/**
	 * Set the front pane content. 
	 * @param - the content to set. 
	 */
	public void setFrontContent(Node content) {
		frontPane.setCenter(content);
	}


	/**
	 * Create the advanced settings pane which can be accessed by DAQ panes if needed. 
	 */
	private PamBorderPane createAdvSettingsPane() {

		backButton = new PamButton(); 
		backButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", Color.WHITE, PamGuiManagerFX.iconSize));
		//		backButton.setStyle("-fx-background-color: -color-base-6"); 
		//backButton.setStyle("-fx-padding: 0,0,0,0");

		//make the back button blue so users can easily see the button. 
		backButton.setStyle("-fx-background-radius: 0 5 5 0; -fx-border-radius: 0 5 5 0; -fx-background-color: -color-accent-6");

		backButton.setOnAction((action)->{
			System.out.println("FLIP BACK TO FRONT"); 
			this.backButtonResponse.setValue(OK_BACK_BUTTON); 
			this.flipToFront(); 
		});

		//Allow the user to cancel instead of pressing back button to confirm.
		ContextMenu contextMenu = new ContextMenu();
		//Creating the menu Items for the context menu
		MenuItem item1 = new MenuItem("Cancel");
		
//		item1.setStyle("-fx-highlight-color: red");
		
		item1.setOnAction((action)->{
			//	System.out.println("FLIP BACK TO FRONT"); 
			this.backButtonResponse.setValue(CANCEL_BACK_BUTTON); 
			this.flipToFront(); 
		});
		contextMenu.getItems().addAll(item1);

		backButton.setContextMenu(contextMenu);

		//create a title that can be set. 
		//backButton.setPrefWidth(150);
		PamBorderPane advPane = new PamBorderPane(); 
		//advPane.setPadding(new Insets(5,5,5,5));

		// holds the title of the advanced pane. This consists of a label for a graphic,
		// an editable text field and a label after the editable settings field
		PamHBox titleHolder = new PamHBox(); 
		titleHolder.getChildren().addAll(preLabel = new Label(), advLabel = new TextField("Adv. "), postLabel = new Label("Settings"));
		preLabel.setId("label-title2");
		postLabel.setId("label-title2");
		titleHolder.setAlignment(Pos.CENTER);
		postLabel.setTextAlignment(TextAlignment.LEFT);
		postLabel.setAlignment(Pos.CENTER_LEFT);

		advLabel.setAlignment(Pos.CENTER);
		//		advLabel.prefColumnCountProperty().bind(advLabel.textProperty().length().subtract(3));
		// Set Max and Min Width to PREF_SIZE so that the TextField is always PREF
		advLabel.setMinWidth(Region.USE_PREF_SIZE);
		advLabel.setMaxWidth(Region.USE_PREF_SIZE);

		//pretty complicated to make sure the text field is the same size as the text that is being typed. 
		advLabel.textProperty().addListener((ov, prevText, currText) -> {
			// Do this in a Platform.runLater because of Textfield has no padding at first time and so on
			Platform.runLater(() -> {
				Text text = new Text(currText);
				text.setFont(advLabel.getFont()); // Set the same font, so the size is the same
				double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
						+ advLabel.getPadding().getLeft() + advLabel.getPadding().getRight() // Add the padding of the TextField
						+ 2d; // Add some spacing
				advLabel.setPrefWidth(width); // Set the width
				advLabel.positionCaret(advLabel.getCaretPosition()); // If you remove this line, it flashes a little bit
			});
		});
		advLabel.setId("label-title2");
		advLabel.setStyle("-fx-background-color: transparent");

		titleHolder.setMaxWidth(Double.MAX_VALUE); //need to make sure label is in center. 

		//holds the back button and the title pane. 
		PamHBox buttonHolder = new PamHBox(); 
		buttonHolder.setBackground(null);
		//buttonHolder.setStyle("-fx-background-color: red;");
		buttonHolder.setAlignment(Pos.CENTER_LEFT);
		buttonHolder.getChildren().addAll(backButton, titleHolder); 

		advLabel.setAlignment(Pos.CENTER);
		advLabel.setMaxWidth(Double.MAX_VALUE); //need to make sure label is in center. 
		//		PamGuiManagerFX.titleFont2style(advLabel);


		advLabel.setAlignment(Pos.CENTER);
		HBox.setHgrow(titleHolder, Priority.ALWAYS);

		advPane.setTop(buttonHolder);

		return advPane; 

	}


	public TextField getAdvLabel() {
		return advLabel;
	}

	//	public void setAdvLabel(Label advLabel) {
	//		this.advLabel = advLabel;
	//	}

	public PamButton getBackButton() {
		return backButton;
	}

	/**
	 * Get the label located before the editable label in the title
	 * @return the label before the editable label
	 */
	public Label getPreAdvLabel() {
		return preLabel;
	}

	/**
	 * Get the label located after the editable label in the title
	 * @return the label after the editable label
	 */
	public Label getPostAdvLabel() {
		return postLabel;
	}

	/**
	 * True if the flip pane is showing the front. 
	 */
	public boolean isShowingFront() {
		return super.flipFrontProperty().get();
	}

	public void setAdvLabelEditable(boolean b) {
		this.advLabel.setEditable(b); 
	}
	
	/**
	 * The back button property. Called whenever the back button is pressed. 
	 * @return the back button integer proeprty. 
	 */
	public SimpleIntegerProperty backButtonProperty(){
		return this.backButtonResponse;
	}


}
