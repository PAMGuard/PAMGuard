package pamViewFX.fxNodes.sliders.skin;


import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

//import com.sun.javafx.scene.control.skin.SliderSkin;

public class PamSliderSkin extends SliderSkin {

	/**
	 * Reference to the slider track. 
	 */
	private Pane track; 

	private Slider slider;

	private StackPane topBar;

	private Pane thumb; 

	public PamSliderSkin(Slider slider) {
		super(slider);
		this.slider=slider; 
		track = (Pane) slider.lookup(".track");
		thumb = (Pane) slider.lookup(".thumb");
		initTopBar();
		setTrackColor(Color.RED);
	}


	/**
	 * Create the top bar. 
	 */
	public void initTopBar(){
		topBar=new StackPane();
		//TODO-need to sort for horizontal 
		if (slider.getOrientation()==Orientation.VERTICAL){
			topBar.layoutXProperty().bind(track.layoutXProperty());
		}
		else {
			topBar.layoutYProperty().bind(track.layoutYProperty());
		}
		//		topBar.setStyle("-fx-background-color: red;");
		//topBar.setSty
		getChildren().add(topBar);
		thumb.toFront();
	}


	@Override
	protected void layoutChildren(final double x, final double y,
			final double w, final double h) {
		super.layoutChildren(x, y, w, h);
		double rangeStart;
		if (track==null) return; 
		//now resize the top bar. 
		if (slider.getOrientation()==Orientation.VERTICAL){
			rangeStart=thumb.getLayoutY()+thumb.getHeight(); 
			topBar.layoutYProperty().setValue(0);
			topBar.resize(track.getWidth(), rangeStart+1);
		}
		else {
			rangeStart=thumb.getLayoutX()+thumb.getWidth(); 
			topBar.layoutXProperty().setValue(0);
			topBar.resize(rangeStart+1, track.getHeight());
		}
	};



	public void setTrackColor(Color trackCol){
		track = (Pane) slider.lookup(".track");
		thumb = (Pane) slider.lookup(".thumb");

		//        int r =  (int) (trackCol.getRed() * 255);
		//        int g =  (int) (trackCol.getGreen() * 255);
		//        int b =  (int) (trackCol.getBlue() * 255);
		//        String str = String.format("#%02X%02X%02X;", r, g, b);
		//28/03/2017 - had to change to css as adding to a scroll pane seemed ot override background. 

		if (topBar!=null) topBar.setStyle("-fx-background-color: " + PamUtilsFX.color2Hex(trackCol) + "; -fx-background-radius: " + 6 + "; -fx-border-radius: "
		+ 6 + " ; -fx-border-color: none;");

		//((Region) track).setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(2), Insets.EMPTY)));
	}

}
