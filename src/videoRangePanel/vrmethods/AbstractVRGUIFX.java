package videoRangePanel.vrmethods;

import java.awt.Point;
import java.util.ArrayList;

import PamUtils.LatLong;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import videoRangePanel.LocationManager;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.layoutFX.VRSettingsPane;


/**
 * Contains more generic nodes for JavaFX implementation of video range GUI. 
 * @author Jamie Macaulay
 *
 */
public abstract class AbstractVRGUIFX implements VROverlayFX {

	/**
	 * Reference to the vr control. 
	 */
	protected VRControl vrControl;

	/**
	 * The GPS text field
	 */
	private TextField gpsTextBox;

	/**
	 * combo box whihc shows current height data. 
	 */
	private ComboBox<VRHeightData> heights;

	/**
	 * 
	 */
	private AbstractVRMethod abstractMethod;

	/**
	 * Allow inputs to trigger listener for the height combo box. Disabled when combo box is being set. 
	 */
	boolean allowHeightInput = true; 

	public AbstractVRGUIFX(AbstractVRMethod abstractMethod) {
		this.abstractMethod=abstractMethod; 
		this.vrControl=abstractMethod.vrControl; 
	}


	//	/**
	//	 * Get the draw canvas. 
	//	 */
	//	public Canvas getDrawCanvas() {
	//		return ((VRDisplayFX) vrControl.getVRPanel()).getImagePane().getDrawCanvas(); 
	//	}
	//	

	/**
	 * Convert a Point to a Point2D. 
	 * @param point the Point to convert. 
	 * @return the 2D point. 
	 */
	protected Point2D point2D(Point point) {
		return new Point2D(point.getX(), point.getY()); 
	}

	/**
	 * Convert a Point2D to a Point. 
	 * @param point the Point2D to convert. 
	 * @return the 2D point. 
	 */
	protected Point point(Point2D point2d) {
		Point point = new Point(); 
		point.setLocation(point2d.getX(), point2d.getY()); 
		return point; 
	}


	/**
	 * Convert an point on the image to a point on the node
	 * @param local - the node on which to find local co-ordinates
	 * @param imagePoint - point on an image in image co-ordinates
	 * @return the local co-ordinates. 
	 */
	public Point2D imageToLocal(Node local, Point imagePoint){	
		return local.screenToLocal(
				point2D(vrControl.getVRPanel().imageToScreen(imagePoint))); 
	}


	/**
	 *Convert a local point to an image point. 
	 * @param local - the node on which to find local co-ordinates
	 * @param imagePoint - point on an image in image co-ordinates
	 * @return the local co-ordinates. 
	 */
	public Point2D localToImage(Node local, Point2D localPoint){
		Point2D screenPoint = local.localToScreen(localPoint); 
		return point2D(vrControl.getVRPanel().screenToImage(point(screenPoint))); 
	}




	/**
	 * Create a panel to show camera position. 
	 */
	public Pane createCameraHeightPane(){

		heights = new ComboBox<VRHeightData>();
		heights.setMaxWidth(Double.MAX_VALUE);
		heights.setOnAction((action)->{
			if (this.allowHeightInput) {
				this.vrControl.getVRParams().setCurrentHeightIndex(heights.getSelectionModel().getSelectedIndex());
				this.vrControl.update(VRControl.HEIGHT_CHANGE);
			}
		});

		PamButton settingsButton = new PamButton(); 
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		settingsButton.setStyle("-fx-border-color: -fx_border_col;"); 
		settingsButton.setOnAction((action)->{
//			SwingUtilities.invokeLater(()->{
//				vrControl.settingsButtonAWT(null,VRParametersDialog.HEIGHT_TAB);
//			});
			vrControl.settingsButtonFX(VRSettingsPane.HEIGHTTAB);
		});
		heights.prefHeightProperty().bind(settingsButton.heightProperty());


		PamHBox hbox = new PamHBox(); 
		hbox.setSpacing(5);
		PamHBox.setHgrow(heights, Priority.ALWAYS);

		hbox.getChildren().addAll(heights, settingsButton);


		return hbox; 
	}



	/**
	 * Populate the height combo box with values.
	 */
	public void populateHeightBox() {
		this.allowHeightInput=false;
		VRHeightData currentheight = vrControl.getVRParams().getCurrentheightData();
		heights.getItems().clear();
		ArrayList<VRHeightData> heightDatas = vrControl.getVRParams().getHeightDatas();
		for (int i = 0; i < heightDatas.size(); i++) {
			heights.getItems().add(heightDatas.get(i));
		}
		if (currentheight != null) {
			heights.getSelectionModel().select(currentheight);
		}
		this.allowHeightInput=true;
	}




	/**
	 * Create a panel to show GPS location and allow user to change GPS location method. 
	 */
	public Pane createCameraLocationPane(){

		PamHBox gpsHolder = new PamHBox(); 
		gpsHolder.setSpacing(5);
		gpsHolder.setAlignment(Pos.CENTER_LEFT);
		//gpsHolder.setStyle("-fx-background-color: red");

		gpsTextBox=new TextField();
		gpsTextBox.setEditable(false);
		gpsTextBox.setFocusTraversable(false);
		gpsTextBox.setText("no GPS data");
		gpsTextBox.setMaxWidth(Double.MAX_VALUE); //grow with pane.
		PamHBox.setHgrow(gpsTextBox, Priority.ALWAYS);


		PamButton settingsButton = new PamButton(); 
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		settingsButton.setStyle("-fx-border-color: -fx_border_col;"); 
		settingsButton.setOnAction((action)->{
//			SwingUtilities.invokeLater(()->{
//				vrControl.settingsButtonAWT(null,VRParametersDialog.CAMERA_POS);
//			}); 
			vrControl.settingsButtonFX(VRSettingsPane.IMAGELOCATIONTAB);
		});
		gpsTextBox.prefHeightProperty().bind(settingsButton.heightProperty());

		gpsHolder.getChildren().addAll(gpsTextBox, settingsButton); 

		return gpsHolder; 
	}


	/** 
	 * Set GPS text. 
	 * @param gpsInfo
	 * @param gps
	 */
	public void setGPSText(LatLong gpsInfo, TextField gps){
		if (gps==null) return;
		if (gpsInfo==null){
			gps.setText("no GPS Data");
			gps.setTooltip(new Tooltip("no GPS Data"));
			return;
		}
		if (vrControl.getLocationManager().getLastSearch()==LocationManager.NO_GPS_DATA_FOUND)  {
			gps.setText("no GPS Data");
			gps.setTooltip(new Tooltip("no GPS Data"));
			return;
		}
		String type=vrControl.getLocationManager().getTypeString(vrControl.getLocationManager().getLastSearch());
		gps.setText(type+": "+ gpsInfo);
		gps.setTooltip(new Tooltip(type+": "+ gpsInfo));
		gps.selectPositionCaret(0);

	}


	/**
	 * Update the controls 
	 * @param flag - the update flag 
	 */
	public void update(int updateType){
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			populateHeightBox(); 
			setGPSText(abstractMethod.getGPSinfo(),gpsTextBox);
			//setMapText();
			//newCalibration();
			break;
		case VRControl.IMAGE_CHANGE:
			setGPSText(abstractMethod.getGPSinfo(),gpsTextBox);
			break;
			//		case VRControl.METHOD_CHANGED:
			//			break;
		}
	}


	@Override
	public Pane getRibbonPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Pane getSettingsPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}




}
