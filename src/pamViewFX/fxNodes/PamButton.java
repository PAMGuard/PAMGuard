package pamViewFX.fxNodes;

//import com.sun.javafx.scene.control.skin.ButtonSkin;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * PAMGuard implementation of JavaFX Button. 
 * <p>
 * Ripple effect from Lukasz Silwinski. http://plumblog.herokuapp.com/2014/12/10/JavaFX-button-with-ripple-effect/
 * @author Jamie Macaulay
 *
 */
public class PamButton extends Button {

	private Circle circleRipple;
	private Rectangle rippleClip = new Rectangle();
	private Duration rippleDuration =  Duration.millis(200);
	private double lastRippleHeight = 0;
	private double lastRippleWidth = 0;
	//	    private Color rippleColor = new Color(0, 0, 0, 0.11);
	private Color rippleColor = new Color(0,0,0,0.05); 
	//private Color rippleColor =  Color.WHITE; 

	public PamButton() {
		super();
		createRippleEffect();	
	}

	public PamButton(String arg0, Node arg1) {
		super(arg0, arg1);
		createRippleEffect();	
	}

	public PamButton(String arg0) {
		super(arg0);
		createRippleEffect();	
	}


	//	    public MaterialDesignButton(String text) {
	//	        super(text);
	//	        getStyleClass().addAll("md-button");
	//	        createRippleEffect();
	//	    }

	@Override
	protected Skin<?> createDefaultSkin() {
		final ButtonSkin buttonSkin = new ButtonSkin(this);
		// Adding circleRipple as fist node of button nodes to be on the bottom
		this.getChildren().add(0, circleRipple);
		return buttonSkin;
	}
	private void createRippleEffect() {		
		
//        Region colour = (Region) this.lookup("button:hover");
//        Background background=colour.getBackground();
		
		circleRipple = new Circle(0.1, rippleColor);
		circleRipple.setOpacity(0.0);
		// Optional box blur on ripple - smoother ripple effect
		//circleRipple.setEffect(new BoxBlur(3, 3, 2));
		// Fade effect bit longer to show edges on the end of animation
		final FadeTransition fadeTransition = new FadeTransition(rippleDuration, circleRipple);
		fadeTransition.setInterpolator(Interpolator.EASE_OUT);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		final Timeline scaleRippleTimeline = new Timeline();
		final SequentialTransition parallelTransition = new SequentialTransition();
		parallelTransition.getChildren().addAll(
				scaleRippleTimeline,
				fadeTransition
				);
		// When ripple transition is finished then reset circleRipple to starting point  
		parallelTransition.setOnFinished(event -> {
			circleRipple.setOpacity(0.0);
			circleRipple.setRadius(0.1);
		});
		this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			parallelTransition.stop();
			// Manually fire finish event
			parallelTransition.getOnFinished().handle(null);
			circleRipple.setCenterX(event.getX());
			circleRipple.setCenterY(event.getY());
			// Recalculate ripple size if size of button from last time was changed
			if (getWidth() != lastRippleWidth || getHeight() != lastRippleHeight)
			{
				lastRippleWidth = getWidth();
				lastRippleHeight = getHeight();
				rippleClip.setWidth(lastRippleWidth);
				rippleClip.setHeight(lastRippleHeight);
				// try block because of possible null of Background, fills ...
				try {
					rippleClip.setArcHeight(this.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
					rippleClip.setArcWidth(this.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
					circleRipple.setClip(rippleClip);
				} catch (Exception e) {
				}
				// Getting 45% of longest button's length, because we want edge of ripple effect always visible
				double circleRippleRadius = Math.max(getHeight(), getWidth()) * 0.65;
				final KeyValue keyValue = new KeyValue(circleRipple.radiusProperty(), circleRippleRadius, Interpolator.EASE_OUT);
				final KeyFrame keyFrame = new KeyFrame(rippleDuration, keyValue);
				scaleRippleTimeline.getKeyFrames().clear();
				scaleRippleTimeline.getKeyFrames().add(keyFrame);
			}
			parallelTransition.playFromStart();
		});
	}

	public void setRippleColor(Color color) {
		circleRipple.setFill(color);
	}


}
