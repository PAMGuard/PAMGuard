package pamViewFX.fxNodes.pamDialogFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * A dialog which has no modal 
 * @author Jamie Macaulay 
 *
 */
public class PamInternalDialogFX extends StackPane {

	/**
	 * Ok button
	 */
	private PamButton okButton;
	
	/**
	 * Cancel button
	 */
	private PamButton cancelButton;

	/**
	 * The node which holds the internal pane. 
	 */
	private Pane holder;

	/**
	 * Main pane which holds conttent. 
	 */
	private PamBorderPane mainPane;

	
	
	/**
	 * Create the internal dialog. 
	 * @param holder
	 */
	public PamInternalDialogFX(String title, Pane holder) {
		
		this.holder=holder; 
		this.mainPane= new PamBorderPane(); 
		mainPane.setPadding(new Insets(5,5,5,5));
		
		PamStylesManagerFX stylesManager = PamStylesManagerFX.getPamStylesManagerFX();
		this.getStylesheets().add(stylesManager.getCurStyle().getSlidingDialogCSS());
		
		//title and drag pane 
		if (title!=null) {
			PamHBox titleBox = new PamHBox(); 
			Label titleLabel = new Label(title); 
//			titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
			PamGuiManagerFX.titleFont2style(titleLabel);
			titleBox.getChildren().add(titleLabel); 
			makeDragable(titleBox); 
			mainPane.setTop(titleBox);
		}

		//the button pane 
		PamHBox buttonBox = new PamHBox(); 
		buttonBox.setSpacing(5);
		buttonBox.setPadding(new Insets(5,5,5,5));
		
		okButton = new PamButton("Ok"); 
		okButton.setOnAction((action)->{
			close();
		});

		cancelButton = new PamButton("Cancel"); 
		cancelButton.setOnAction((action)->{
			close();
		});

		buttonBox.getChildren().addAll(okButton, cancelButton); 
		buttonBox.setMaxHeight(35);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		mainPane.setBottom(buttonBox);
		
		this.getChildren().add(mainPane);
		
		this.setStyle("-fx-background-color: -fx-darkbackground;");
		this.setMinSize(900, 500);
		this.setMaxSize(900, 500);

	}
	

	public void setOnClosed(InternalDialogAction dialogAction) {
		okButton.setOnAction((action)->{
			close();
			dialogAction.dialogClosed(InternalDialogAction.OK);
		});
		
		cancelButton.setOnAction((action)->{
			close();
			dialogAction.dialogClosed(InternalDialogAction.CANCEL);
		});
		
	}
	
	/**
	 * Show the dialog within the parent
	 */
	public void show() {
		holder.getChildren().add(this);
		this.setLayoutX(holder.getWidth()/2-this.getMinWidth()/2);
		this.setLayoutY(holder.getHeight()/2-this.getMinHeight()/2);
		this.toFront();
		holder.layout();
	}
	
	public void close() {
		holder.getChildren().remove(this);
		holder.layout();
	}
	
	/**
	 * Set the content
	 * @param node
	 */
	public void setContent(Pane node) {
		mainPane.setCenter(node);
		//node.minWidthProperty().bind(mainPane.widthProperty());
		//node.minHeightProperty().bind(mainPane.heightProperty().subtract(60));
		//node.toBack();
	}

	/*
	 * Brings the dialog to the front. 
	 */
	public void makeFocusable() {    
		this.setOnMouseClicked(mouseEvent -> {
			toFront();
		});    
	}

	/**
	 * Make any node located within the  children of the dialog draggable. 
	 * @param what - the port of the dialog to make resizbale (e.g. a top bar)
	 */
	public void makeDragable(Node what) {
		final Delta dragDelta = new Delta();
		
		what.setOnMouseEntered((mouseEvent)->{
		        this.getScene().setCursor(Cursor.HAND); //Change cursor to hand
		});
		
		what.setOnMouseExited((mouseEvent)->{
	        this.getScene().setCursor(Cursor.DEFAULT); //Change cursor to hand
	});
		
		what.setOnMousePressed(mouseEvent -> {
			dragDelta.x = getLayoutX() - mouseEvent.getScreenX();
			dragDelta.y = getLayoutY() - mouseEvent.getScreenY();
			//also bring to front when moving
			toFront();
		});
		what.setOnMouseDragged(mouseEvent -> {
			setLayoutX(mouseEvent.getScreenX() + dragDelta.x);
			setLayoutY(mouseEvent.getScreenY() + dragDelta.y);
		});
	}

	//just for encapsulation
	private static class Delta {
		double x, y;
	}
	
	
	//current state
	private boolean RESIZE_BOTTOM;
	private boolean RESIZE_RIGHT;

	/**
	 * Make the dialog resizable. 
	 * @param mouseBorderWidth - the width/height of the dialog's resize border. 
	 */
	public void makeResizable(double mouseBorderWidth) {
	    this.setOnMouseMoved(mouseEvent -> {
	        //local window's coordiantes
	        double mouseX = mouseEvent.getX();
	        double mouseY = mouseEvent.getY();
	        //window size
	        double width = this.boundsInLocalProperty().get().getWidth();
	        double height = this.boundsInLocalProperty().get().getHeight();
	        //if we on the edge, change state and cursor
	        if (Math.abs(mouseX - width) < mouseBorderWidth
	                && Math.abs(mouseY - height) < mouseBorderWidth) {
	            RESIZE_RIGHT = true;
	            RESIZE_BOTTOM = true;
	            this.setCursor(Cursor.NW_RESIZE);
	        } else {
	            RESIZE_BOTTOM = false;
	            RESIZE_RIGHT = false;
	            this.setCursor(Cursor.DEFAULT);
	        }

	    });
	    this.setOnMouseDragged(mouseEvent -> {
	        //resize root
	        Region region = (Region) getChildren().get(0);
	        //resize logic depends on state
	        if (RESIZE_BOTTOM && RESIZE_RIGHT) {
	            region.setPrefSize(mouseEvent.getX(), mouseEvent.getY());
	        } else if (RESIZE_RIGHT) {
	            region.setPrefWidth(mouseEvent.getX());
	        } else if (RESIZE_BOTTOM) {
	            region.setPrefHeight(mouseEvent.getY());
	        }
	    });
	}
	
	/**
	 * Called whenever the dialog is closed. 
	 * @author Jamie Macaulay 
	 *
	 */
	public interface InternalDialogAction {
		
		public static final int OK=0; 
		
		public static final int CANCEL =0; 

		
		public void dialogClosed(int type); 
	}



}
