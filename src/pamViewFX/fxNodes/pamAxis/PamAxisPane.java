package pamViewFX.fxNodes.pamAxis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Pane which contains two or more axis and a plot panel. 
 * @author Jamie Macaulay
 *
 */
public class PamAxisPane extends Pane {
	
	/**
	 * The canvas is where all the drawing of axis etc takes place.. 
	 */
	protected Canvas canvas;
			
	/**
	 * Orientation of axis.
	 */
	private Orientation orientation; 

//	/**06/04/2016 removed and replaced with CSS- simply set background colour of the pane. 
//	 * Background colour for axis
	 
//	 */
//	private Color axisColour=new Color(0.85,0.85,0.85,1);
	
	/**
	 * Stroke colour.
	 */
	private Color strokeColor=Color.BLACK;
	
	
	/**
	 * left inset property. 
	 */
	private DoubleProperty leftPaddingProperty=new AxisInsetProperty(0); 
	

	/**
	 * left inset property. 
	 */
	private DoubleProperty rightPaddingProperty=new AxisInsetProperty(0); 

	/**
	 *The axis. This handles stores info on axis min and max. 
	 */
	private PamAxisFX axis;
	
	/**
	 * An axis usually represents just one panel, but it may also be used to represent multiple plots with the same scale.
	 * nPanels is the number of plots the axis is used for- this splits the axis into nPanels equally spaced axis. 
	 */
	private int nPanels=1; 
	

	public PamAxisPane(PamAxisFX axis, Orientation orientation){
		this.axis=axis;
		//create the canvas
        canvas = new Canvas(30, 30);
		//default orientation
		this.orientation=orientation;
		//add the canvas to the panel. 
		this.getChildren().add(canvas);
		//add listeners to allow canvas to resize and repaint with the graph changing size; 
		addResizeListeners();
		this.getStyleClass().add("pane");
		repaint();
	}
	
	/**
	 * Set whether the panel is for a horizontal or vertical axis. 
	 * @param orientation- horizontal or vertical. 
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		repaint();
	}
	
	/**
	 * Add a listeners to the canvas to check for resize and repaint. 
	 */
	public void addResizeListeners(){
		this.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
            	canvas.setWidth(arg0.getValue().doubleValue());
            	repaint();
            }
        });

        this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
            	canvas.setHeight(arg0.getValue().doubleValue());
            	repaint();
            }
        });
	}

	/**
	 * Repaint the axis.
	 */
	public void repaint(){
		if (this.getHeight()>0 && this.getWidth()>0){

			canvas.getGraphicsContext2D().setStroke(strokeColor);
			canvas.getGraphicsContext2D().setFill(Color.TRANSPARENT);
			canvas.getGraphicsContext2D().clearRect(0, 0,  canvas.getWidth(), canvas.getHeight());
			canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

			if (orientation==Orientation.HORIZONTAL) paintHorizontal(canvas);
			else  paintVertical(canvas);
			
		}
	}

	/**
	 * Draw the axis if horizontal.
	 * @param canvas
	 */
	public void paintHorizontal(Canvas canvas){
		if (this.getHeight()>0 && this.getWidth()>0){
			for (double i=0; i<nPanels; i++){
				int x1=(int) ((i/nPanels)*canvas.getWidth());
				int x2=(int) (((i+1.0)/nPanels)*canvas.getWidth());
//				System.out.println("x1: "+x1+" x2: "+x2+" canvas.getHeight(): "+canvas.getHeight());
				if (axis.getTickPosition()==PamAxisFX.ABOVE_LEFT){
					axis.drawAxis(canvas.getGraphicsContext2D(), x1,  (int) canvas.getHeight(), x2, (int) canvas.getHeight());
				}
				else{
					axis.drawAxis(canvas.getGraphicsContext2D(), 0, 0, x2, 0);
				}
			}
		}
//			if (axis.getTickPosition()==PamAxisFX.ABOVE_LEFT)
//				
//				//above axis paint
//				axis.drawAxis(canvas.getGraphicsContext2D(), 0, (int) canvas.getHeight(), (int) canvas.getWidth(), (int) canvas.getHeight());
//			else{
//				//below axis
//				axis.drawAxis(canvas.getGraphicsContext2D(), 0, 0, (int) canvas.getWidth(), 0);
//			}
//		}
	}
	
	/**
	 * Draw the axis if vertical
	 * @param canvas
	 */
	public void paintVertical(Canvas canvas){
		if (this.getHeight()>0 && this.getWidth()>0){
			for (double i=0; i<nPanels; i++){
				int y1=(int) ((i/nPanels)*canvas.getHeight());
				int y2=(int) (((i+1.0)/nPanels)*canvas.getHeight());
//				System.out.println("y1: "+y1+" y2: "+y2+" canvas.getWidth(): "+canvas.getWidth());
				if (axis.getTickPosition()==PamAxisFX.ABOVE_LEFT){
					axis.drawAxis(canvas.getGraphicsContext2D(), (int) canvas.getWidth(),y2, (int) canvas.getWidth(), y1);
				}
				else{
					axis.drawAxis(canvas.getGraphicsContext2D(), 0, y2, 0, 0);
				}
			}
//			if (axis.getTickPosition()==PamAxisFX.ABOVE_LEFT){
//				axis.drawAxis(canvas.getGraphicsContext2D(), (int) canvas.getWidth(), (int) canvas.getHeight(), (int) canvas.getWidth(), 0);
//			}
//			else{
//				axis.drawAxis(canvas.getGraphicsContext2D(), 0, (int) canvas.getHeight(), 0, 0);
//			}
		}
	}
	
	public DoubleProperty getWidthProperty(){
		return canvas.widthProperty();
	}
	
	public DoubleProperty getHeightProperty(){
		return canvas.heightProperty();
	}

//	/**
//	 * Get the background colour for the axis. 
//	 * @return the background colour fo the axis. 
//	 */
//	public Color getAxisColour() {
//		return axisColour;
//	}
//
//	/**
//	 * Set the background colour of the axis
//	 * @param axisColour- the colour to set the background of the axis to. 
//	 */
//	public void setAxisColour(Color axisColour) {
//		this.axisColour = axisColour;
//	}

	/**
	 * Get the stroke colour for the axis
	 * @return the stroke colour for the axis. 
	 */
	public Color getStrokeColor() {
		return strokeColor;
	}

	/**
	 * Set the stroke colour for the axis. 
	 * @param strokeColor- the stroke colour for the axis. 
	 */
	public void setStrokeColor(Color strokeColor) {
		this.strokeColor = strokeColor;
	}
	
	/**
	 * Get the number of panels the axis will plot. An axis usually represents just one panel, 
	 * but it may also be used to represent multiple plots with the same scale. 
	 * nPanels is the number of plots the axis is used for- this splits the axis into nPanels equally spaced axis. 
	 * @return the number of plot panels the axis represents. 
	 */
	public int getnPanels() {
		return nPanels;
	}

	/**
	 * Set the number of panels the axis will plot. An axis usually represents just one panel, 
	 * but it may also be used to represent multiple plots with the same scale. 
	 * nPanels is the number of plots the axis is used for- this splits the axis into nPanels equally spaced axis. 
	 * @param the number of plot panels the axis represents. 
	 */
	public void setNPanels(int nPanels) {
		this.nPanels = nPanels;
	}
	
	
	/**
	 * Simple property class which repaints axis when changed. 
	 * @author Jamie Macaulay
	 *
	 */
	class AxisInsetProperty extends SimpleDoubleProperty {
		
		AxisInsetProperty(){
			super();
			super.addListener((obsval,oldVal, newVal)->{
				repaint();
			});
		}
		
		AxisInsetProperty(double val){
			super(val); 
			super.addListener((obsval,oldVal, newVal)->{
				repaint();
			});
		}
		
	}
	
	/**
	 * Get the left padding property- this is the number of pixels the axis is shifted to the left. 
	 * @return the left padding property
	 */
	public DoubleProperty leftPaddingProperty() {
		return leftPaddingProperty;
	}


	/**
	 * Get the right padding property- this is the number of pixels the axis is shifted to the right. 
	 * @return the right padding property
	 */
	public DoubleProperty rightPaddingProperty() {
		return rightPaddingProperty;
	}
}

