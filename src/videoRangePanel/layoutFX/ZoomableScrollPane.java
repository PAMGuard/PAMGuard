package videoRangePanel.layoutFX;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


/**
 * A scroll pane which allows the user to zoom the content node with scroll bars acting accordingly. 
 * @author Jamie Macaulay 
 *
 */
public class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 0.7;
    private double zoomIntensity = 0.002;
    private Node target;
    private Node zoomNode;
    
	private double zoomFactor=1;

    public ZoomableScrollPane(Node target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(true);
//        zoomNode.addEventHandler(MouseEvent.ANY, event -> {
//        	//don't allow the pane to scroll if control key is down. This
//        	//means some overlays can use mouse dragging. 
//            if(event.isControlDown()) event.consume();
//        });
        
        
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        updateScale();
    }

    /**
     * Get the zoom node. This is the main node which zooms and on whihc mouse
     * actions occur. 
     * @return the zoom node. 
     */
    public Node getZoomNode() {
		return zoomNode;
	}


	private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        
        //try speed up. 
        outerNode.setCache(true);
        outerNode.setCacheHint(CacheHint.SPEED);

        outerNode.setOnScroll(e -> {
            e.consume();
            double zoom=(e.getDeltaX()+e.getDeltaY() );
            //FIXME
            //02/08/2011. Was getting exceptions in JavaFX thread Quantum Renderer. 
            //Adding Platform.runLater seemed to solve the issue a little bit but 
            //still get problem sometimes????
            Platform.runLater(()->{;
            	onScroll(zoom, new Point2D(e.getX(), e.getY()));
            });
        });
        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }
    
    public void zoomResetScroll() {
//    	System.out.println("Reset scroll: ");
        scaleValue = 0.4;
        zoomIntensity = 0.002;
        zoomFactor=1;
//        zoomIn(0.01);  
        onScroll(1, new Point2D(this.getWidth()/2, this.getHeight()/2));
    }


    /**
     * Zoom into or out of the content node. 
     * @param wheelDelta - the change in zoom. 
     * @param mousePoint - the location of the mouse. 
     */
    public void onScroll(double wheelDelta, Point2D mousePoint) {
//    	System.out.println("Scroll scroll: ");

    	
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);        
 

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = zoomFactor*scaleValue;
        
        updateScale();
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

    /**
     * Zoom In
     * @param zoomincrement
     */
	public void zoomIn(double zoomincrement) {
		//beware of scroll bars messing this up. That's why you have to use content node to get width. 
		onScroll(zoomincrement,  new Point2D(((Region) this.getContent()).getWidth()/2, ((Region) this.getContent()).getHeight()/2)); 
		
	}

	/**
	 * Zoom out
	 * @param zoomincrement
	 */
	public void zoomOut(double zoomincrement) {
		//beware of scroll bars messing this up. That's why you have to use content node to get width. 
		onScroll(-zoomincrement, new Point2D(((Region) this.getContent()).getWidth()/2, ((Region) this.getContent()).getHeight()/2));
	}
	
	
	/**
	 * Get the xy position in pixels on the target. 
	 * @param pointx - point x on the scroll pane
	 * @param pointy - point y on the scroll pane
	 * @return - the location in pixels on the target node. (the node which zooms and pans) 
	 */
	public Point2D getContentXY(double pointx, double pointy) {
		Point2D screenP=this.localToScreen(pointx, pointy);
		return target.screenToLocal(screenP.getX(), screenP.getY()); 
	}
	
	
	/**
	 * Get the xy position in pixels on the scroll pane given pixels on target
	 * @param pointx - point x on the content
	 * @param pointy - point y on the content
	 * @return - the location in pixels on the scroll pane node. 
	 */
	public Point2D getPaneXY(double pointx, double pointy) {
		Point2D screenP=target.localToScreen(pointx, pointy);
		Point2D localP=this.screenToLocal(screenP.getX(), screenP.getY()); 
//		System.out.println("Image Point: " + pointx + " " + pointy + 
//				" localP " + localP.getX() + " " + localP.getY());
		return localP; 
	}
}