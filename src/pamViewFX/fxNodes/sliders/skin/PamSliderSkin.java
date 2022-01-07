package pamViewFX.fxNodes.sliders.skin;


import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

//import com.sun.javafx.scene.control.skin.SliderSkin;

public class PamSliderSkin extends SliderSkin {
	
	/**
	 * Reference to the slider track. 
	 */
	private Node track; 
	
	private Slider slider; 

	public PamSliderSkin(Slider slider) {
		super(slider);
		this.slider=slider; 
        track = slider.lookup(".track");
        setTrackColor(Color.RED);
	}
	
	@Override
	protected void layoutChildren(final double x, final double y,
			final double w, final double h) {
		super.layoutChildren(x, y, w, h);
	};
	


	public void setTrackColor(Color trackCol){
        track = slider.lookup(".track");
//        int r =  (int) (trackCol.getRed() * 255);
//        int g =  (int) (trackCol.getGreen() * 255);
//        int b =  (int) (trackCol.getBlue() * 255);
//        String str = String.format("#%02X%02X%02X;", r, g, b);
        //28/03/2017 - had to change to css as adding to a scroll pane seemed ot override background. 
        track.setStyle("-fx-background-color: " + PamUtilsFX.color2Hex(trackCol));
		//((Region) track).setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(2), Insets.EMPTY)));
	}

}
