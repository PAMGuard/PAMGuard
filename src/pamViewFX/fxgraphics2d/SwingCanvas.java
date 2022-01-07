package pamViewFX.fxgraphics2d;

import javafx.scene.canvas.Canvas;

/**
 * Extension of canvas which allows Swing Graphics2D code to be used in JavaFX
 * @author Jamie Macaulay
 *
 */
public class SwingCanvas extends Canvas {
	
    private FXGraphics2D g2;
    

    public SwingCanvas(){
        this.g2 = new FXGraphics2D(this.getGraphicsContext2D());
    }
    
    /**
     * All existing Graphics2D works here and will plot on canvas. 
     * @return the graphics handle. 
     */
   public FXGraphics2D getGraphics2D(){
	   return g2; 
   }


}
