package pamViewFX.fxNodes.pamAxis;

import java.text.DecimalFormat;

import PamguardMVC.PamConstants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.NumberAxis.DefaultFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * Axis which utilises the javafx number axis instead of the graphics inside pamaxis. 
 * <p>
 * A PamAxisFX is still used to handle conversion of data values and pixels etc. 
 * <p>
 * Note: Eventually PamAxisFX should be removed from this and a replaced by a much simpler class
 * that handles pixel to data conversion. 
 * @author Jamie Macaulay
 *
 */
public class PamAxisPane2 extends StackPane {

	/**
	 * The axis. Always holds at least one axis. However and axis pane may contain more
	 * than one axis. 
	 * 
	 *  */
	private NumberAxis[] axis = new NumberAxis[PamConstants.MAX_CHANNELS]; 

	/*
	 * The pamAxis. Holds information on the axis. 
	 */
	private PamAxisFX pamAxisFX;

	/**
	 * Occasionally there will be more than one axis on a plot. 
	 */
	private IntegerProperty nPlots= new SimpleIntegerProperty(1);

	/**
	 * The axis side. 
	 */
	private Side side;

	/**
	 * Holds the axis
	 */
	private Pane axisHolder; 
	
	/**
	 * Cramp labelss ensure all graph labels stay within the bounds of the plot. Means
	 * that graphs can be stacked closely toegtehr without text getting mixed up. 
	 */
	private boolean cramp = true; //TODO implement property. 
	
	/**
	 * If true then the axis is reversed so that the minimum number is at the top. 
	 */
	BooleanProperty reverseAxis = new SimpleBooleanProperty();
	
	/**
	 * The main holder for the time axis. 
	 */
	private PamBorderPane mainPane = new PamBorderPane(); 
	

	public PamAxisPane2(PamAxisFX pamAxisFX, Side side){
		this.pamAxisFX=pamAxisFX;

		this.side=side; 		
		
		//08/07/2017. Needed to initialise axis to prevent weird binding changes
		//when new layout is called. 
		for (int i=0; i<axis.length; i++) {
			axis[i]=createAxis(); 
		}

		layoutAxis(); 
		
		this.getChildren().add(mainPane);
	}

	private void layoutAxis(){

		mainPane.setCenter(null);

		if (side == Side.BOTTOM || side == Side.TOP){
			axisHolder = new PamHBox(); 
		}
		else {
			axisHolder = new PamVBox(); 
		}

		for (int i=0; i<Math.max(1,this.getNPlotsProperty().get()); i++){
			axisHolder.getChildren().add(axis[i]); 
			if (side == Side.BOTTOM || side == Side.TOP){
				HBox.setHgrow(axis[i], Priority.ALWAYS);
			}
			else {
				VBox.setVgrow(axis[i], Priority.ALWAYS);
			}
		}
		
		mainPane.setCenter(axisHolder);

		//set axis binding
		setPamAxisBinding(); 
	}

	/**
	 * Create a number axis to diaplay in the pane. 
	 * @return the number axis. 
	 */
	private NumberAxis createAxis(){
		
		final NumberAxis axis = new NumberAxis(); 

		//set the binding to all max, min, proeprties. 
		setNumberAxisBinding(axis); 
		
		//make sure the reverse axis property has a listener. 
		reverseAxis.bind(pamAxisFX.reverseProperty());
		this.reverseAxis.addListener((obsval, oldval, newval)->{
			setNumberAxisBinding(axis); 
		});

		//set the axis side
		axis.setSide(side);
		//disable aurto ranging 
		axis.setAutoRanging(false);

		//add custom number formatter. 
		axis.setTickLabelFormatter(new ScaledNumberFormatter(axis));
		
		//don;t want silly animations. 
		axis.setAnimated(false);
		
		return axis;
	}

	/**
	 * Set the number axis binding so that the numbers can be in different directions of the y axis. 
	 * @param axis - the axis to set binding for. 
	 */
	private void setNumberAxisBinding() {
		for (int i=0; i<this.axis.length; i++) {
			setNumberAxisBinding(axis[i]);
		}
	}
	
	/**
	 * Set the number axis binding so that the numbers can be in different directions of the y axis. 
	 * @param axis - the axis to set binding for. 
	 */
	private void setNumberAxisBinding(NumberAxis axis) {
		
		//unbind everything
		axis.lowerBoundProperty().unbind();
		axis.upperBoundProperty().unbind();
		axis.lowerBoundProperty().unbind();
		axis.upperBoundProperty().unbind();
		axis.tickUnitProperty().unbind();
		axis.lowerBoundProperty().unbind();
		axis.lowerBoundProperty().unbind();
		axis.labelProperty().unbind();
		
		//if the axis is reversed we nagate the numbers
		if (!this.reverseAxis.get()) {
			//if normal direction
			axis.lowerBoundProperty().bind(pamAxisFX.minValProperty());
			axis.upperBoundProperty().bind(pamAxisFX.maxValProperty());
		}
		else {
			//if reversed direction 
			axis.lowerBoundProperty().bind(pamAxisFX.maxValProperty().negate());
			axis.upperBoundProperty().bind(pamAxisFX.minValProperty().negate());
		}
		
		//set the tick marks
		axis.tickUnitProperty().bind(pamAxisFX.maxValProperty().subtract(pamAxisFX.minValProperty()).divide(10));
		
		//cramp labels?
		axis.lowerBoundProperty().addListener((obsVal, oldVal, newVal)->{
			setTicksCramp(axis, cramp); 
		});
		axis.lowerBoundProperty().addListener((obsVal, oldVal, newVal)->{
			setTicksCramp(axis, cramp); 
		});
		
		//the axis label. 
		axis.labelProperty().bind(pamAxisFX.labelProperty());


	}
	
	
	/**
	 * Formats number son the axis. Actually quite hard - for example we want dB not to have a decimal points for ICI. 
	 * @author Jamie Macaulay
	 *
	 */
	class ScaledNumberFormatter extends StringConverter<Number> {
		
		private NumberAxis baseAxis;
		
		//private DefaultFormatter baseFormatter;

		private DecimalFormat formatter;

		public ScaledNumberFormatter(NumberAxis baseAxis) {
			this.baseAxis = baseAxis;
			//baseFormatter = new	NumberAxis.DefaultFormatter(baseAxis);
			
			formatter = new DecimalFormat("#.##"); 
		}

		@Override
		public Number fromString(String string) {
			//return new Double((reverseAxis.get() ? -1:1)*baseFormatter.fromString(string).doubleValue() / pamAxisFX.getLabelScale());
			return  (reverseAxis.get() ? -1:1)*Double.parseDouble(string) / pamAxisFX.getLabelScale();

		}

		@Override
		public String toString(Number value) {
			try {
				//double labelScale = pamAxisFX.getLabelScale();
				
//				if (labelScale == 0 || labelScale == 1.) {
				//System.out.println("Axis values: " + value); 
					if (!reverseAxis.get()) return formatter.format(value);
					else return formatter.format(-value.doubleValue());
//				}
//				else {
//					if (!reverseAxis.get()) return pamAxisFX.formatValue(value.doubleValue());
//					else return baseFormatter.toString(-value.doubleValue());
//				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				return "Err";
			}
		}
		
	}
	

	/**
	 * Set tick cramping.
	 * @param axis
	 * @param cramp2
	 */
	private void setTicksCramp(NumberAxis axis, boolean cramp2) {
		for (int i=0; i<axis.getTickMarks().size(); i++){
		}		
	}

	public void setPamAxisBinding(){

		pamAxisFX.x1Property().unbind(); 
		pamAxisFX.x2Property().unbind(); 
		pamAxisFX.y1Property().unbind(); 
		pamAxisFX.y2Property().unbind(); 
		
		switch (side){
		case BOTTOM:
			pamAxisFX.x1Property().setValue(0);
			pamAxisFX.x2Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.y1Property().bind(getNumberAxis().heightProperty());
			pamAxisFX.y2Property().bind(getNumberAxis().heightProperty());
			break;
		case LEFT:
			pamAxisFX.x1Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.x2Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.y1Property().bind(getNumberAxis().heightProperty());
			pamAxisFX.y2Property().setValue(0);
			break;
		case RIGHT:
			pamAxisFX.x1Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.x2Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.y1Property().bind(getNumberAxis().heightProperty());
			pamAxisFX.y2Property().setValue(0);
			break;
		case TOP:
			pamAxisFX.x1Property().setValue(0);
			pamAxisFX.x2Property().bind(getNumberAxis().widthProperty());
			pamAxisFX.y1Property().bind(getNumberAxis().heightProperty());
			pamAxisFX.y2Property().bind(getNumberAxis().heightProperty());
			break;
		default:
			break;

		}
	}

	/**
	 * All the axis are the same size. This returns the first axis
	 * @return
	 */
	public NumberAxis getNumberAxis(){
		return this.axis[0]; 
	} 
	
	/**
	 * All the axis are the same size. This returns an array of all axis
	 * @return
	 */
	public NumberAxis[] getAllNumberAxis(){
		return this.axis; 
	} 


	/**
	 * 
	 * @param orientation
	 */
	public void setOrientation(Side orientation) {
		getNumberAxis().setSide(orientation);
	}


	/**
	 * Convenience function. Exactly the same as PamAxisFX
	 * <p>
	 * The axis knows all about scale and can tell us the 
	 * pixel value for any given data value. 
	 * @param datavalue - the value of the data
	 * @return position in pixels along the axis for a given data value.
	 */
	public double getPosition(double dataValue){
		return getNumberAxis().getDisplayPosition((reverseAxis.get() ? -1:1)*dataValue);
	}


	/**
	 * Convenience function. Exactly the same as PamAxisFX.
	 * <p>
	 * Converts a position on the plot into a data value 
	 * based on the axis max, min and scale. 
	 * <p>
	 * This is the exact compliment of getPosition()
	 * @param position along the axis in pixels.
	 * @return data value. 
	 */
	public double getDataValue(double pixelVal) {
		return (reverseAxis.get() ? -1:1)*getNumberAxis().getValueForDisplay(pixelVal).doubleValue(); 
	}

	/**
	 * Convenience function
	 * 
	 * @return
	 */
	private double getMaxVal() {
		return getNumberAxis().getUpperBound();
	}

	/**
	 * 
	 * @return
	 */
	private double getMinVal() {
		return getNumberAxis().getLowerBound();
	}

	/**
	 * 
	 * @param orientation
	 */
	public void setOrientation(Orientation orientation) {
		if (orientation == Orientation.HORIZONTAL) setOrientation(Side.TOP); 
		else setOrientation(Side.LEFT);
	}



	public IntegerProperty getNPlotsProperty() {
		return nPlots;
	}



	public void setnPlots(int nPlots) {
		this.nPlots.setValue(nPlots);
		this.layoutAxis();
	}
	
	/**
	 * Get the PAM axis
	 * @return
	 */
	public PamAxisFX getPamAxis(){
		return this.pamAxisFX; 
	}

//	/**
//	 * Set the axis to be reversed so the minimum number is at the top. 
//	 * @param reverseAxis the reverse axis. 
//	 */
//	public void setReversed(boolean reverseAxis) {
//		this.reversed.setValue(reverseAxis); 
//		this.setNumberAxisBinding();
//	}




}
