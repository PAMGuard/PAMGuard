package pamViewFX.fxNodes.utilityPanes;

import org.controlsfx.control.SegmentedButton;

import PamController.SettingsPane;
import PamUtils.LatLong;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane with controls to to set the values for a Latitude and Longitude. The pane allows users to
 * change the Latitude and Longitude in both Degrees, Decimal minutes and Degrees, Minutes, Seconds
 * @author Jamie Macaulay 
 *
 */
public class LatLongPane extends SettingsPane<LatLong>{

	/**
	 * The current latitude and longitude
	 */
	private LatLong latLong;

	/**
	 * The radio button to select decimal minutes 
	 */
	private ToggleButton decimalMinutes;

	/**
	 * Radio button to input minutes and seconds. 
	 */
	private ToggleButton minutesSeconds;

	/**
	 * Lat long strip
	 */
	private LatLongStrip latStrip, longStrip;

	private PamBorderPane mainPane;

	private ToggleButton decimal;


	public LatLongPane(String title) {
		super(null); 

		latLong= new LatLong(); 

		mainPane = new PamBorderPane(); 

		PamHBox top = new PamHBox();
		top.setSpacing(5);
		top.getChildren().add(new Label("Unit type :"));
		
		decimalMinutes = new ToggleButton("Degrees, Decimal minutes");
		minutesSeconds = new ToggleButton("Degrees, Minutes, Seconds");
		decimal = new ToggleButton("Decimal");
		
		 SegmentedButton segmentedButton = new SegmentedButton();    
		 segmentedButton.getButtons().addAll(decimalMinutes, minutesSeconds, decimal);
		 top.getChildren().add(segmentedButton);

//		ToggleGroup bg = new ToggleGroup();
//		decimalMinutes.setToggleGroup(bg);
//		minutesSeconds.setToggleGroup(bg);
//		decimal.setToggleGroup(bg);

		decimalMinutes.setOnAction((action)->{
			actionPerformed(action);
		});

		minutesSeconds.setOnAction((action)->{
			actionPerformed(action);
		});
		
		decimal.setOnAction((action)->{
			actionPerformed(action);
		});
		mainPane.setTop(top);

		PamVBox cent = new PamVBox();
		cent.setSpacing(5);
		cent.setPadding(new Insets(5,0,5,0));

		cent.getChildren().add(latStrip = new LatLongStrip(true));
		cent.getChildren().add(longStrip = new LatLongStrip(false));
		
		mainPane.setCenter(cent);

	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(javafx.event.ActionEvent action) {

		if (action.getSource() == decimalMinutes) {
			LatLong.setFormatStyle(LatLong.FORMAT_DECIMALMINUTES);
			//			if (latStrip != null) {
			//				latStrip.setDecimalMinutes(true);
			//				longStrip.setDecimalMinutes(true);
			//			}
			latStrip.showControls(LatLong.FORMAT_DECIMALMINUTES);
			longStrip.showControls(LatLong.FORMAT_DECIMALMINUTES);
		}
		else if (action.getSource() == minutesSeconds) {
			LatLong.setFormatStyle(LatLong.FORMAT_MINUTESSECONDS);
			//			if (latStrip != null) {
			//				latStrip.setDecimalMinutes(false);
			//				longStrip.setDecimalMinutes(false);
			////				latStrip.showControls();
			////				longStrip.showControls();
			//			}
			latStrip.showControls(LatLong.FORMAT_MINUTESSECONDS);
			longStrip.showControls(LatLong.FORMAT_MINUTESSECONDS);
		}


	}



	private void showLatLong() {
		decimalMinutes.setSelected(LatLong.getFormatStyle() == LatLong.FORMAT_DECIMALMINUTES);
		minutesSeconds.setSelected(LatLong.getFormatStyle() == LatLong.FORMAT_MINUTESSECONDS);
		latStrip.showControls(LatLong.getFormatStyle() );
		longStrip.showControls(LatLong.getFormatStyle() );
		latStrip.sayValue(latLong.getLatitude());
		longStrip.sayValue(latLong.getLongitude());
	}



	/* (non-Javadoc)
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public LatLong getParams(LatLong currentParams) {
		latLong = new LatLong(latStrip.getValue(), longStrip.getValue());
		if (Double.isNaN(latLong.getLatitude()) || Double.isNaN(latLong.getLongitude())) {
			return null;
		}
		return latLong;
	}

	@Override
	public void setParams(LatLong input) {
		this.latLong=input; 
		showLatLong();
	}

	@Override
	public String getName() {
		return "Lat Long";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	//	public static LatLong showDialog(Window parentFrame, LatLong latLong, String title) {
	//		
	//		if (latLong == null){
	//			latLong = new LatLong(57.33, -10.8);
	//		}
	//		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
	//			singleInstance = new LatLongDialog(parentFrame, title);
	//		}
	//		LatLongDialog.latLong = latLong;
	//		singleInstance.setTitle(title);
	//		singleInstance.showLatLong();
	//		singleInstance.setVisible(true);
	//		
	//		return LatLongDialog.latLong;
	//	}
	//	public static LatLong showDialog(Frame parentFrame, LatLong latLong) {
	//		
	//		return showDialog(parentFrame, latLong, "Enter Lat Long");
	//	}


}
