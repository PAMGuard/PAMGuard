package pamViewFX.fxNodes.orderedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;

/**
 * Creates a VBox where the children and can be dragged to reorder. 
 * <p>
 * Note: this will work in a JFXPanel but will return an error - FIXME 
 * @author Jamie Macaulay 
 *
 */
public class PamDraggableList<T extends Pane> extends PamVBox {


	private static final String TAB_DRAG_KEY = "vbox2_drag";


	private ObjectProperty<Pane> draggingTab;

	private BorderPane draggingPane;

	/**
	 * True to have a button in the pane that deletes the child form the list
	 */
	private boolean removeButton = true;

	/**
	 * True if the drag button is located right of the pane - otherwise left. 
	 */
	private boolean rightDragButton = false;

	/**
	 * Constructor for a draggable  list pane.
	 * @param panes
	 */
	public PamDraggableList() {
		this(null, true, false); 
	}

	/**
	 * Constructor for a draggable  list pane.
	 * @param panes
	 */
	public PamDraggableList(List<T> panes) {
		this(panes, true, false); 
	}

	public PamDraggableList(List<T> panes, boolean removeButton, boolean rightDrag){

		this.removeButton = removeButton; 
		this.rightDragButton = rightDrag; 
		draggingTab = new SimpleObjectProperty<Pane>();

		if (panes!=null) {
			for(int i=0;i<panes.size();i++) {
				addDraggablePane(panes.get(i)); 
			}
		}
	}

	/**
	 * Add a draggble pane to the list. The pane is added to the end of the list. 
	 * @param childPane - the pane to add. 
	 */
	public void addDraggablePane(T childPane) {
		addDraggablePane(childPane, this.getChildren().size()); 
	}

	/**
	 * Add a dragable pane to the list. The pane is added to the end of the list. 
	 * @param - the index at which the pane is to be inserted. 
	 * @param childPane - the pane to add. 
	 */
	public void addDraggablePane(T childPane, int index) {

//		Node dragIcon =  PamGlyphDude.createPamGlyph(MaterialIcon.DRAG_HANDLE); 
		Node dragIcon =  PamGlyphDude.createPamIcon("mdi2d-drag-horizontal-variant"); 

		dragIcon.setOnMouseEntered((e)->{
			dragIcon.getScene().setCursor(Cursor.OPEN_HAND); //Change cursor to hand
		});

		dragIcon.setOnMousePressed((e)->{
			dragIcon.getScene().setCursor(Cursor.CLOSED_HAND); //Change cursor to hand
		});

		dragIcon.setOnMouseExited((e)->{
			dragIcon.getScene().setCursor(Cursor.DEFAULT); //Change cursor to hand
		});

		final BorderPane pane=new BorderPane(childPane); 

		BorderPane.setAlignment(dragIcon, Pos.CENTER);
		pane.setPadding(new Insets(3,0,3,0));
		if (this.rightDragButton) {
			pane.setRight(dragIcon);
		}
		else {
			pane.setLeft(dragIcon);
		}
		BorderPane.setMargin(dragIcon, new Insets(0,5,0,0));
		
		//System.out.println("Add pane: " + pane + "index:  " + index); 
		//add the pane. 
		this.getChildren().add(index, pane);

		if (this.removeButton) {
//			Node removeIcon =  PamGlyphDude.createPamGlyph(MaterialDesignIcon.CLOSE); 
			Node removeIcon =  PamGlyphDude.createPamIcon("mdi2w-window-close"); 
			PamButton removeButton = new PamButton("", removeIcon); 
			removeButton.setOnAction((action)->{
				removePane(pane); 
			});
			pane.setRight(removeButton);
			BorderPane.setMargin(removeButton, new Insets(0,0,0,5));
		}


		pane.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				//System.out.println("setOnDragOver: " ); 

				final Dragboard dragboard = event.getDragboard();
				if (dragboard.hasString()
						&& TAB_DRAG_KEY.equals(dragboard.getString())
						&& draggingTab.get() != null) {
					event.acceptTransferModes(TransferMode.MOVE);
					event.consume();
				}
			}
		});


		pane.setOnDragEntered(e->{
			//System.out.println("setOnDragEntered: "  + " mouse y: " + e.getY()); 
			Pane parent = (Pane) pane.getParent();
			BorderPane bPane; 

			for (int j=0; j<parent.getChildren().size(); j++) {
				bPane = (BorderPane) parent.getChildren().get(j); 
				bPane.setTop(null); //remove the blue line
				bPane.setBottom(null); //remove the blue line
			}

			//clear lines
			//add a blue line to indicate where the node might be dropped. 
			Line line = new Line(0,0,pane.getWidth()-15,0); //-15 prevents weird increase in window size...
			if (canDrop((T) ((BorderPane) draggingPane).getCenter(),  parent.getChildren().indexOf(draggingPane), parent.getChildren().indexOf(pane))) {
				line.setStroke(Color.DODGERBLUE);
				line.setStrokeWidth(2);
			}
			else {
				line.setStroke(Color.RED);
				line.setStrokeWidth(2);
			}

			BorderPane.setAlignment(line, Pos.CENTER);
			if (parent.getChildren().indexOf(draggingPane)<parent.getChildren().indexOf(pane)) {
				pane.setBottom(line);
			}
			else {
				pane.setTop(line);
			}
			e.consume();

		}); 


		pane.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(final DragEvent event) {
				//System.out.println("Drag dropped"); 
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					Pane parent = (Pane) pane.getParent();

					BorderPane bPane; 
					for (int j=0; j<parent.getChildren().size(); j++) {
						bPane = (BorderPane) parent.getChildren().get(j); 
						bPane.setTop(null); //remove the blue line
						bPane.setBottom(null); //remove the blue line
					}


					Object source = event.getGestureSource();
					int sourceIndex = parent.getChildren().indexOf(source);
					int targetIndex = parent.getChildren().indexOf(pane);

					if (canDrop((T) ((BorderPane) source).getCenter(),  sourceIndex, targetIndex)) {
						List<Node> nodes = new ArrayList<Node>(parent.getChildren());
						if (sourceIndex < targetIndex) {
							Collections.rotate(
									nodes.subList(sourceIndex, targetIndex + 1), -1);
						} else {
							Collections.rotate(
									nodes.subList(targetIndex, sourceIndex + 1), 1);
						}
						parent.getChildren().clear();
						parent.getChildren().addAll(nodes);
						success = true;
					}
				}
				event.setDropCompleted(success);
				event.consume();

				paneOrderChanged(success); 

				dragIcon.getScene().setCursor(Cursor.DEFAULT); //Change cursor to hand

			}


		});


		dragIcon.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				//System.out.println("Drag detected"); 
				Dragboard dragboard = pane.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.putString(TAB_DRAG_KEY);

				dragboard.setContent(clipboardContent);

				dragIcon.getScene().setCursor(Cursor.CLOSED_HAND); //Change cursor to hand

				dragboard.setDragView(pane.snapshot(null, null));
				draggingTab.set(pane);
				draggingPane = pane; 
				event.consume();
			}
		}); 
	}

	/**
	 * Remove a pane.
	 * @param pane - the pane to remove. 
	 */
	public void removePane(BorderPane pane) {
		this.getChildren().remove(pane);
	}

	/**
	 * Get a temporary sorted list showing what the order would be if a pane was dropped. This 
	 * does not cause the primary list to be sorted.  
	 * @param sourceIndex - the current list
	 * @param targetIndex - the target index. 
	 * @return the temporarily sorted list. 
	 */
	public List<T> getTempSortedList(int sourceIndex, int targetIndex) {
		List<T> nodes = new ArrayList<T>(getSortedList());
		if (sourceIndex < targetIndex) {
			Collections.rotate(
					nodes.subList(sourceIndex, targetIndex + 1), -1);
		} else {
			Collections.rotate(
					nodes.subList(targetIndex, sourceIndex + 1), 1);
		}
		return nodes; 
	}
	/**
	 * Can the pane be dropped at the index. 
	 * @param source - the source pane 
	 * @param targetIndex - the new index to drop it at. 
	 * @return - true if it can be droppe.d 
	 */
	public boolean canDrop(T source, int sourceIndex, int targetIndex) {
		return true; 
	}

	public void paneOrderChanged(boolean success) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the sorted list of panes. 
	 * @return the sorted list of panes. 
	 */
	public List<T> getSortedList(){

		ArrayList<T> sortedList = new ArrayList<T>(); 

		BorderPane bPane; 
		for (int i=0; i<this.getChildren().size(); i++) {
			bPane = (BorderPane) this.getChildren().get(i); 
			sortedList.add((T) bPane.getCenter()); 
		}

		return sortedList; 
	}

}

