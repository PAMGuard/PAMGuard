package pamViewFX.fxPlotPanes;

import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * The PamDataPane contains a pane with two hiding panes, a left pane and a right pane. 
 * <p>
 * A convenience class as this type of display is used a lot for various plots, graphs etc. . 
 * @author Jamie Macaulay
 *
 */
public class PamHiddenSidePane extends PamStackPane {
	
	/**
	 * The y position
	 */
	private int yPos=5; 
	

	/**
	 * The hiding left pane. 
	 */
	private HidingPane leftHidingPane; 
	
	/**
	 * The hiding right pane. 
	 */
	private HidingPane rightHidingPane;
	
	private double minHeight=5;
	

	/**
	 * Create a stack pane with no hiding panes added. 
	 * Hiding panes can be manually added later. 
	 */
	public PamHiddenSidePane(){
		//setClipping(this, rightHidingPane, minHeight);
	}
	
	
	/**
	 * Create a stack pane with two overlaid hiding panes, a left hiding pane and a right hiding pane. 
	 * @param iconleft - the icon for show button on the left hiding pane
	 * @param iconRight - the show button icon on the right for the right hiding pane
	 * @param leftPane - the  pane to hold in the left hiding pane
	 * @param rightPane - the pane to hold in the right hiding pane. 
	 */
	public PamHiddenSidePane(Node iconleft, Node iconRight, Region leftPane, Region rightPane){
		if (leftPane!=null) leftHidingPane=createHidingPane(leftPane, iconleft, Side.LEFT); 
		if (rightPane!=null) rightHidingPane=createHidingPane(rightPane, iconRight, Side.RIGHT); 
		//have to be careful - if overlaid, then hiding panes will appear outside node unless clipped. 
		setClipping(this, leftHidingPane, minHeight);
		setClipping(this, rightHidingPane, minHeight);
	}
	

	/**
	 * Create a stack pane with two overlaid hiding panes, a left hiding pane and a right hiding pane. 
	 * @param iconleft - the icon for show button on the left hiding pane
	 * @param iconRight - the show button icon on the right for the right hiding pane
	 * @param leftPane - the  pane to hold in the left hiding pane
	 * @param rightPane - the pane to hold in the right hiding pane. 
	 */
	public PamHiddenSidePane(Region holder, Node iconleft, Node iconRight, Region leftPane, Region rightPane){
		if (leftPane!=null){
			leftHidingPane=createHidingPane(leftPane, iconleft, Side.LEFT); 
			//have to be careful - if overlaid, then hiding panes will appear outside node unless clipped. 
			setClipping(holder, leftHidingPane, minHeight);
		}
		if (rightPane!=null){
			rightHidingPane=createHidingPane(rightPane, iconRight, Side.RIGHT); 
			setClipping(holder, rightHidingPane, minHeight);
		}

	}
	
	/**
	 * Create a left hiding pane. If a left pane exists it is removed and replace by a new pane. 
	 * @param leftPane - the pane to hold in the left hiding pane. 
	 * @param iconleft - the icon for the left show button
	 */
	public void createLeftPane(Pane leftPane,  Node iconleft){
		if (leftHidingPane!=null) this.getChildren().remove(leftHidingPane);
		leftHidingPane=createHidingPane(leftPane, iconleft, Side.LEFT); 
		setClipping(this, leftHidingPane, 200);

	}
	
	/**
	 * Create a right hiding pane. If a right pane exists it is removed and replace by a new pane. 
	 * @param rightPane - the pane to hold in the right hiding pane. 
	 * @param iconRight - the icon for the right show button
	 */
	public void createRightPane(Pane rightPane,  Node iconRight){
		if (rightHidingPane!=null) this.getChildren().remove(rightHidingPane);
		rightHidingPane=createHidingPane(rightPane, iconRight, Side.RIGHT); 
		setClipping(this, rightHidingPane, 200);

	}
	
	/**
	 * Have to be careful if overlaid hidden panes, then hiding panes will appear outside node unless clipped. 
	 * We also may want an overlapping hiding pane if the holder become too small 
	 */
	public static void setClipping(Region holder, HidingPane rightHidingPane, double minHeight){
//		if (rightHidingPane==null){
//			
//		} return; 
		//have to be careful - if overlaid, then hiding panes will appear outside node unless clipped. 
		Rectangle clipRectangle = new Rectangle();
		 holder.setClip(clipRectangle)  ; 	
    	//rightHidingPane.setMinHeight(minHeight); // FIXME- should be here but cauyses a crash?

    	
		 holder.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
			
			//TODO - overlapping pane only implmented for RIGHT AND LEFT. 
			
		    clipRectangle.setWidth(newValue.getWidth());
		    clipRectangle.setHeight(Math.max(newValue.getHeight(), minHeight));
		    
//	    	System.out.println("Clipping: Hello : " + newValue.getHeight() + " " + minHeight);

		    
		    if (newValue.getHeight()<minHeight) {
		    	
//		    	System.out.println(" CLIP " + newValue.getHeight() + " " + minHeight);

		    	//first- do we need to have the hiding pane move up or down; 
		    	boolean up=true; //TODO 
		    	if (up) {
				    clipRectangle.setLayoutY(newValue.getHeight()-minHeight);
				    rightHidingPane.setTranslateY(newValue.getHeight()-minHeight);
		    	}
		    	else{
					 rightHidingPane.setLayoutY(0);
		    	}
		    	rightHidingPane.setMinHeight(minHeight);
		    	rightHidingPane.toFront();
		    	rightHidingPane.getShowButton().toFront();
		    }
		    else {
		    	//reset stuff
		    	clipRectangle.setLayoutY(0);
		    	rightHidingPane.setTranslateY(0);
		    }
		 
		});
	}


	private boolean checkOverlapPos() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Sets the hiding pane to have a minimum height. If the hiding is below this height then 
	 * @param height the minimum height.
	 */
	public void setMinHidePaneHeight(double height){
		this.minHeight=height; 
	}

	
	/**
	 * Create and add a hiding pane to the display. 
	 * @param displayPane - the pane to add to the hiding pane.
	 * @param icon - the icon for the show button.
	 * @param trayPos - the y position of the show button from the top of the pane. 
	 * @param side the side of the display the hiding pane should open on
	 * @param pos the position of the hiding pane oin the stack pane.
	 * @return the hiding pane. 
	 */
	public HidingPane createHidingPane(Region displayPane, Node icon, Side side){
		//create the hiding pane
		HidingPane hidingPane=new HidingPane(side, displayPane,  this, true);
		hidingPane.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		hidingPane.getStyleClass().add("pane-trans");
		
		//the stack pane holds all the different settings panes
		this.getChildren().add(hidingPane);

		PamButton showButton=hidingPane.getShowButton();
		showButton.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		showButton.setPrefHeight(60);
		
		//style show button
		showButton.setGraphic(icon);
		switch(side){
		case LEFT: 
			StackPane.setAlignment(hidingPane, Pos.TOP_LEFT);
			StackPane.setAlignment(showButton, Pos.TOP_LEFT);
			showButton.getStyleClass().add("close-button-right");
			break; 
		case RIGHT:
			StackPane.setAlignment(hidingPane, Pos.TOP_RIGHT);
			StackPane.setAlignment(showButton, Pos.TOP_RIGHT);
			showButton.getStyleClass().add("close-button-left");
			 break;
		default:
			break; 
		}
		
		//set the location of the show button to be in the middle of the pane
		showButton.translateYProperty().bind(displayPane.heightProperty().divide(2).subtract(showButton.heightProperty().divide(2)));

		hidingPane.getHideButton().translateYProperty().setValue(yPos);
		
		//add to pane
		this.getChildren().add(showButton);
		showButton.toFront();
		return hidingPane;
	}
	
	/**
	 * Get the hiding pane on the right side of the pane.
	 * @return the right HidingPane. 
	 */
	public HidingPane getLeftHidingPane() {
		return leftHidingPane;
	}

	/**
	 * Get the hiding pane on the left side of the pane. 
	 * @return the left HidingPane.
	 */
	public HidingPane getRightHidingPane() {
		return rightHidingPane;
	}

}
