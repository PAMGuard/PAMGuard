package pamViewFX.fxNodes.popOver;

import org.controlsfx.control.PopOver;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * Pop over which has some extra functionality including the ability to resize.
 * @author Jamie Macaulay
 *
 */
public class PamPopOver extends PopOver {
	
	/**
	 * The start of the drag. 
	 */
	private volatile Point2D dragStart = null;
	
	/**
	 * The labe which can be dragged t resize the pop up menu.  
	 */
	private Labeled dragLabel; 
	
	public PamPopOver(Pane content) {
		super();
		super.setContentNode(makeResizableContent(content));
		content.setMaxSize(5000, 5000);
	}
	
	/**
	 * Add a button to the content pane on the bottom left which makes the pane resizable
	 * @param content - the content pane.
	 * @return content pane with resizing abilities. 
	 */
	private Pane makeResizableContent(Pane content) {
		StackPane stackPane = new StackPane();
		stackPane.getChildren().add(content);
		
		//not make a button which can be dragged
		dragLabel =new Label();
//		dragLabel.setGraphic(PamGlyphDude.createPamGlyph(
//				MaterialDesignIcon.DRAG, PamGuiManagerFX.iconSize-5));
		dragLabel.setGraphic(PamGlyphDude.createPamIcon("mdi2d-drag", PamGuiManagerFX.iconSize-5));
		//dragLabel.setStyle("-fx-background-color:hover: transparent;");
		
		//set on mouse pressed
		dragLabel.setOnMouseEntered((action)->{
			content.getScene().setCursor(Cursor.NW_RESIZE); //Change cursor to crosshair
		});
		
		//set on mouse pressed
		dragLabel.setOnMouseExited((action)->{
			content.getScene().setCursor(Cursor.DEFAULT); //Change cursor to crosshair
		});
		
		//set on mouse pressed
		dragLabel.setOnMousePressed((action)->{
			dragStart = PamPopOver.this.getContentNode().localToScene(new Point2D(0,0)); 
		});
		
		dragLabel.setOnMouseDragged((action)->{
//			Platform.runLater(()->{
				resizePopMenu(action, content);
//			});
		});
		
		dragLabel.setOnMouseReleased((action)->{
			dragStart=null; 
		});

		StackPane.setAlignment(dragLabel, Pos.BOTTOM_RIGHT);
		stackPane.getChildren().add(dragLabel);
		dragLabel.toFront();

		return stackPane;
	}
	
	/**
	 * Set whether the pop up menu has a label whihc can be dragged to resize it. If false label
	 * is not visible and resize cannot be performed. 
	 * @param resize - true to allow resizing. 
	 */
	public void setResizeAbility(boolean resize) {
			this.dragLabel.setVisible(resize);
			this.dragLabel.setDisable(!resize);
	}
	
	/**
	 * Resize the pop up menu. 
	 * @param action - the action event. 
	 * @param content - the content pane. 
	 */
	private void resizePopMenu(MouseEvent action, Pane content) {
		
		//so if the size is increased so that the edges of the pop over 
		//leave the scene then the whole thing crashed. Simply use scene coords instead of screen to solve this. 
//		
//		double mouseSceneX=Math.min(action.getSceneX(), content.getScene().getWidth()-1);
//		double mouseSceneY=Math.min(action.getSceneY(), content.getScene().getHeight()-1);
		if (dragStart==null) dragStart = PamPopOver.this.getContentNode().localToScene(new Point2D(0,0)); 
		
		double sizeX = Math.max(10, action.getSceneX()-dragStart.getX());
		double sizeY = Math.max(10, action.getSceneY()-dragStart.getY());
		
		//System.out.println("PamPopOver: X: "+sizeX + " "+ sizeY + " " +content.getScene());
	
		content.setMinSize(sizeX, sizeY);
		content.layout();

		//need to consume to stop pop over fro dragging
		action.consume();
	}
	
}
