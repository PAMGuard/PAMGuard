package pamViewFX.fxNodes.table;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * Create a VBox or HBox with create (+ icon), edit (settings icon) and delete (- icon) buttons. 
 * @author Jamie Macualay 
 *
 */
public class TableButtonPane extends Pane {
	
	/**
	 *Pane holding the buttons. 
	 */
	Pane mainPane; 
	
	/**
	 * Button to add new item
	 */
	Button addButton; 
	
	/**
	 * Button to edit/open settings
	 */
	Button settingsButton; 
	
	/**
	 * Button to remove/delete
	 */
	Button deleteButton; 
	
//	public 	Image addIcon=new Image(getClass().getResourceAsStream("/resources/Add_Icon.png"));
//	
//	public 	Image deleteIconWhite=new Image(getClass().getResourceAsStream("/Resources/deleteWhite.png"));
//
//
//	public 	Image settingsIcon=new Image(getClass().getResourceAsStream("/Resources/SettingsButtonSmallWhite.png"));

	//public 	Image deleteIcon=new Image(getClass().getResourceAsStream("/resources/deleteGrey.png"));

	
	public TableButtonPane(Orientation orientation){
		if (orientation==Orientation.VERTICAL) {
			mainPane=new VBox(); 
			((VBox) mainPane).setSpacing(10); 
		}
		else {
			mainPane=new HBox(); 
			((HBox) mainPane).setSpacing(10); 
		}
		mainPane.setPadding(new Insets(15, 12, 15, 12));
		
		addButton=new Button();
//		addButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.PLUS, Color.WHITE, PamGuiManagerFX.iconSize));
		addButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		HBox.setHgrow(addButton, Priority.ALWAYS);

		
		settingsButton=new Button(); 
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, Color.WHITE, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", Color.WHITE, PamGuiManagerFX.iconSize));
		HBox.setHgrow(settingsButton, Priority.ALWAYS);

		deleteButton=new Button(); 
		//deleteButton.setGraphic(Glyph.create("FontAwesome|DELETE").size(22).color(Color.WHITE));
//		deleteButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.DELETE, Color.WHITE, PamGuiManagerFX.iconSize));
		deleteButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-delete", Color.WHITE, PamGuiManagerFX.iconSize));
		HBox.setHgrow(deleteButton, Priority.ALWAYS);
		
		settingsButton.setMaxWidth(Double.MAX_VALUE);
		deleteButton.setMaxWidth(Double.MAX_VALUE);
		addButton.setMaxWidth(Double.MAX_VALUE);
		mainPane.getChildren().addAll(addButton, settingsButton, deleteButton);
		
		this.getChildren().add(mainPane);

	}
	
	/**
	 * Get the add button. 
	 * @return the button with add icon. 
	 */
	public Button getAddButton() {
		return addButton;
	}

	/**
	 * Get the delete button.
	 * @return the button with delete icon. 
	 */
	public Button getDeleteButton() {
		return deleteButton;
	}

	/**
	 * Get the settings button.
	 * @return the button with settings icon. 
	 */
	public Button getSettingsButton() {
		return settingsButton;
	}


}
